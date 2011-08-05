package com.google.code.mp4parser.example;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.SyncSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.mdat.MediaDataBox;
import com.coremedia.iso.boxes.mdat.SampleList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Reads file and reorders all chunks.
 */

public class ReorderChunks {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("ReorderChunks srcMp4 targetMp4");
            System.exit(1);
        }
        File inputFile = new File(args[0]);
        if (!inputFile.exists()) {
            System.err.println("inputfile must exist");
            System.exit(1);
        }

        IsoFile f = new IsoFile(new IsoBufferWrapperImpl(inputFile));

        f.parse();
        MovieBox movieBox = f.getBoxes(MovieBox.class).get(0);


        removeExistingsMdats(f);
        List<TrackBox> trackBoxes = movieBox.getBoxes(TrackBox.class);

        // List of samples for every trackbox
        Map<TrackBox, List<IsoBufferWrapper>> trackToSample = new HashMap<TrackBox, List<IsoBufferWrapper>>();
        for (TrackBox trackBox : trackBoxes) {
            trackToSample.put(trackBox, new SampleList(trackBox));
        }
        // SampleTableBox for every Track
        Map<TrackBox, SampleTableBox> trackToSampleTableBox = new HashMap<TrackBox, SampleTableBox>();
        for (TrackBox trackBox : trackBoxes) {
            SampleTableBox sampleTableBox = (SampleTableBox) IsoFileConvenienceHelper.get(trackBox, "mdia/minf/stbl");
            trackToSampleTableBox.put(trackBox, sampleTableBox);
        }


        // Array of chunk lengths (number of samples per chunk)
        Map<TrackBox, long[]> trackToChunkLengths = new HashMap<TrackBox, long[]>();

        // we are splitting the chunks that all chunks start with a sync sample
        // there we search a track with sync samples whose chunk lengths will become
        // a reference for the other tracks.
        TrackBox syncSampleContainingTrack = null;

        syncSampleContainingTrack = findTrackWithSyncSamples(syncSampleContainingTrack, trackBoxes);
        if (syncSampleContainingTrack == null) {
            throw new InternalError("I need  a syncsamplebox somewhere");
        }
        SampleTableBox stbl = syncSampleContainingTrack.getMediaBox().getMediaInformationBox().getSampleTableBox();
        long[] syncSamples = stbl.getSyncSampleBox().getSampleNumber();
        long syncSampleContainingTrackSampleCount = stbl.getSampleSizeBox().getSampleCount();


        // Now that we know the sizes of the chunks of one track
        // we split the other tracks in the same proportions
        for (TrackBox trackBox : trackBoxes) {
            long[] chunkSizes = new long[syncSamples.length];
            SampleTableBox sampleTableBox = (SampleTableBox) IsoFileConvenienceHelper.get(trackBox, "mdia/minf/stbl");
            long sc = sampleTableBox.getSampleSizeBox().getSampleCount();
            // Since the number of sample differs per track enormously 25 fps vs Audio for example
            // we calculate the stretch. Stretch is the number of samples in current track that
            // are needed for the time one sample in reference track is presented.
            double stretch = (double) sc / syncSampleContainingTrackSampleCount;
            for (int i = 0; i < chunkSizes.length; i++) {
                long start = Math.round(stretch * (syncSamples[i] - 1));
                long end = 0;
                if (syncSamples.length == i + 1) {
                    end = Math.round(stretch * (syncSampleContainingTrackSampleCount));
                } else {
                    end = Math.round(stretch * (syncSamples[i + 1] - 1));
                }

                chunkSizes[i] = end - start;
                // The Stretch makes sure that there are as much audio and video chunks!
            }
            trackToChunkLengths.put(trackBox, chunkSizes);
            assert trackToSample.get(trackBox).size() == sum(chunkSizes): "The number of samples and the sum of all chunk lengths must be equal";
        }
        // All chunk lengths are now known



        Map<TrackBox, StaticChunkOffsetBox> trackToChunkOffsetBox = new HashMap<TrackBox, StaticChunkOffsetBox>();
        for (TrackBox trackBox : trackBoxes) {

            long sampleDescriptionIndex = checkAndGetSampleDescriptionIndex(trackToSampleTableBox.get(trackBox).getSampleToChunkBox());


            long[] chunkSizes = trackToChunkLengths.get(trackBox);
            List<SampleToChunkBox.Entry> sampleToChunkList = new ArrayList<SampleToChunkBox.Entry>(chunkSizes.length);

            for (int i = 0; i < chunkSizes.length; i++) {
                SampleToChunkBox.Entry entry = new SampleToChunkBox.Entry();
                entry.setFirstChunk(i + 1);
                entry.setSampleDescriptionIndex(sampleDescriptionIndex);
                entry.setSamplesPerChunk(chunkSizes[i]);
                sampleToChunkList.add(entry);
            }

            trackToSampleTableBox.get(trackBox).getSampleToChunkBox().setEntries(sampleToChunkList);

            StaticChunkOffsetBox staticChunkOffsetBox = new StaticChunkOffsetBox();
            staticChunkOffsetBox.setChunkOffsets(new long[chunkSizes.length]);
            trackToSampleTableBox.get(trackBox).setChunkOffsetBox(staticChunkOffsetBox);
            trackToChunkOffsetBox.put(trackBox, staticChunkOffsetBox);
        }

        MyMdat myMdat = new MyMdat(trackToSample, trackToChunkLengths);
        long mdatDataStart = f.getSize();
        mdatDataStart += myMdat.getHeader().length;


        for (int i = 0; i < syncSamples.length; i++) {
            for (TrackBox trackBox : trackToSample.keySet()) {
                long[] chunkOffsets = trackToChunkOffsetBox.get(trackBox).getChunkOffsets();
                chunkOffsets[i] = mdatDataStart;
                long[] chunkSizes = trackToChunkLengths.get(trackBox);
                long firstSampleOfChunk = 0;
                for (int j = 0; j < i; j++) {
                    firstSampleOfChunk += chunkSizes[j];
                }

                for (long j = firstSampleOfChunk; j < firstSampleOfChunk + chunkSizes[i]; j++) {
                    mdatDataStart += trackToSampleTableBox.get(trackBox).getSampleSizeBox().getSampleSizeAtIndex((int) j);
                }

            }

        }

        for (TrackBox trackBox : trackToSample.keySet()) {

            SampleSizeBox sampleSizeBox = trackToSampleTableBox.get(trackBox).getSampleSizeBox();
            if (sampleSizeBox.getSampleSize() == 0) {
                for (long l : sampleSizeBox.getEntrySize()) {
                    mdatDataStart += l;
                }
            } else {
                mdatDataStart += sampleSizeBox.getSampleSize() * sampleSizeBox.getSampleCount();
            }
        }

        f.addBox(myMdat);

        File outputFile = new File(args[1]);
        IsoOutputStream isoOutputStream = new IsoOutputStream(new FileOutputStream(outputFile));
        f.getBox(isoOutputStream);
        isoOutputStream.close();
    }

    private static long checkAndGetSampleDescriptionIndex(SampleToChunkBox sampleToChunkBox) {
        List<SampleToChunkBox.Entry> sampleToChunkList = sampleToChunkBox.getEntries();
        // It could be possible that there is more than one sampleDescriptionBox
        long sampleDescriptionIndex = sampleToChunkList.get(0).getSampleDescriptionIndex();
        for (SampleToChunkBox.Entry entry : sampleToChunkList) {
            if (entry.getSampleDescriptionIndex() != sampleDescriptionIndex) {
                throw new InternalError("Can't cope with a track with multiple sample description boxes");
            }
        }
        return sampleDescriptionIndex;
    }

    private static TrackBox findTrackWithSyncSamples(TrackBox syncSampleContainingTrack, List<TrackBox> trackBoxes) {
        for (TrackBox trackBox : trackBoxes) {
            SampleTableBox sampleTableBox = (SampleTableBox) IsoFileConvenienceHelper.get(trackBox, "mdia/minf/stbl");
            List<SyncSampleBox> syncSampleBoxes = sampleTableBox.getBoxes(SyncSampleBox.class);
            if (syncSampleBoxes.size() == 1) {
                if (syncSampleContainingTrack == null) {
                    syncSampleContainingTrack = trackBox;

                } else {
                    throw new InternalError("Cannot deal with more than one sync sample containing track");
                }
            }
        }
        return syncSampleContainingTrack;
    }

    private static void removeExistingsMdats(IsoFile f) {
        List<Box> mdats = new LinkedList<Box>();
        for (Box box : f.getBoxes()) {
            if (box instanceof MediaDataBox) {
                mdats.add(box);
            }
        }
        for (Box mdat : mdats) {
            f.removeBox(mdat);
        }
    }

    private static long sum(long[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }

    private static class MyMdat extends AbstractBox {
        Map<TrackBox, List<IsoBufferWrapper>> trackToSamples;
        private Map<TrackBox, long[]> trackToChunkLengths;

        private MyMdat(Map<TrackBox, List<IsoBufferWrapper>> trackToSamples, Map<TrackBox, long[]> trackToChunkLengths) {
            super(IsoFile.fourCCtoBytes("mdat"));
            this.trackToSamples = trackToSamples;
            this.trackToChunkLengths = trackToChunkLengths;
        }

        @Override
        protected long getContentSize() {
            long size = 0;
            for (TrackBox trackBox : trackToSamples.keySet()) {
                SampleTableBox sampleTableBox = (SampleTableBox) IsoFileConvenienceHelper.get(trackBox, "mdia/minf/stbl");
                SampleSizeBox sampleSizeBox = sampleTableBox.getSampleSizeBox();
                if (sampleSizeBox.getSampleSize() == 0) {
                    for (long l : sampleSizeBox.getEntrySize()) {
                        size += l;
                    }
                } else {
                    size += sampleSizeBox.getSampleSize() * sampleSizeBox.getSampleCount();
                }
            }
            return size;
        }

        @Override
        public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
            throw new InternalError("This box cannot be created by parsing");
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        protected void getContent(IsoOutputStream os) throws IOException {
            long aaa = 0;
            // all tracks have the same number of chunks
            for (int i = 0; i < trackToChunkLengths.values().iterator().next().length; i++) {
                for (TrackBox trackBox : trackToSamples.keySet()) {


                    long[] chunkSizes = trackToChunkLengths.get(trackBox);
                    long firstSampleOfChunk = 0;
                    for (int j = 0; j < i; j++) {
                        firstSampleOfChunk += chunkSizes[j];
                    }

                    for (long j = firstSampleOfChunk; j < firstSampleOfChunk + chunkSizes[i]; j++) {
                        if (j > Integer.MAX_VALUE) {
                            throw new InternalError("I cannot deal with a number of samples > Integer.MAX_VALUE");
                        }
                        aaa++;
                        System.err.println(j);
                        IsoBufferWrapper ibw = trackToSamples.get(trackBox).get((int) j);
                        while (ibw.remaining() >= 1024) {
                            os.write(ibw.read(1024));
                        }
                        while (ibw.remaining() > 0) {
                            os.write(ibw.read());
                        }

                    }

                }

            }
            System.err.println(aaa);

        }
    }
}
