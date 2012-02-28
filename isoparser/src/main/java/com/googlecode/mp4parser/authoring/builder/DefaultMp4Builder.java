package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.mdat.Sample;
import com.googlecode.mp4parser.authoring.DateHelper;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import sun.nio.ch.DirectBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.logging.Logger;

import static com.coremedia.iso.boxes.CastUtils.l2i;

/**
 * Creates a plain MP4 file from a video. Plain as plain can be.
 */
public class DefaultMp4Builder implements Mp4Builder {
    Set<StaticChunkOffsetBox> chunkOffsetBoxes = new HashSet<StaticChunkOffsetBox>();
    private static Logger LOG = Logger.getLogger(DefaultMp4Builder.class.getName());

    HashMap<Track, List<? extends Sample>> track2Sample = new HashMap<Track, List<? extends Sample>>();
    HashMap<Track, long[]> track2SampleSizes = new HashMap<Track, long[]>();
    private FragmentIntersectionFinder intersectionFinder = new TwoSecondIntersectionFinder();

    public void setIntersectionFinder(FragmentIntersectionFinder intersectionFinder) {
        this.intersectionFinder = intersectionFinder;
    }

    public IsoFile build(Movie movie) throws IOException {
        LOG.info("Creating movie " + movie);
        for (Track track : movie.getTracks()) {
            // getting the samples may be a time consuming activity
            List<? extends Sample> samples = track.getSamples();
            track2Sample.put(track, samples);
            long[] sizes = new long[samples.size()];
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = samples.get(i).getSize();
            }
            track2SampleSizes.put(track, sizes);
        }

        IsoFile isoFile = new IsoFile();
        // ouch that is ugly but I don't know how to do it else
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");

        isoFile.addBox(new FileTypeBox("isom", 0, minorBrands));
        isoFile.addBox(createMovieBox(movie));
        InterleaveChunkMdat mdat = new InterleaveChunkMdat(movie);
        isoFile.addBox(mdat);

        /*
        dataOffset is where the first sample starts. In this special mdat the samples always start
        at offset 16 so that we can use the same offset for large boxes and small boxes
         */
        long dataOffset = mdat.getDataOffset();
        for (StaticChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes) {
            long[] offsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += dataOffset;
            }
        }


        return isoFile;
    }

    private MovieBox createMovieBox(Movie movie) {
        MovieBox movieBox = new MovieBox();
        List<Box> movieBoxChildren = new LinkedList<Box>();
        MovieHeaderBox mvhd = new MovieHeaderBox();
        mvhd.setCreationTime(DateHelper.convert(new Date()));
        mvhd.setModificationTime(DateHelper.convert(new Date()));

        long movieTimeScale = getTimescale(movie);
        long duration = 0;

        for (Track track : movie.getTracks()) {
            long tracksDuration = getDuration(track) * movieTimeScale / track.getTrackMetaData().getTimescale();
            if (tracksDuration > duration) {
                duration = tracksDuration;
            }


        }

        mvhd.setDuration(duration);
        mvhd.setTimescale(movieTimeScale);
        // find the next available trackId
        long nextTrackId = 0;
        for (Track track : movie.getTracks()) {
            nextTrackId = nextTrackId < track.getTrackMetaData().getTrackId() ? track.getTrackMetaData().getTrackId() : nextTrackId;
        }
        mvhd.setNextTrackId(++nextTrackId);
        movieBoxChildren.add(mvhd);
        for (Track track : movie.getTracks()) {
            if (track.getType() != Track.Type.UNKNOWN) {
                movieBoxChildren.add(createTrackBox(track, movie));
            }
        }
        // metadata here
        movieBox.setBoxes(movieBoxChildren);
        return movieBox;

    }

    private TrackBox createTrackBox(Track track, Movie movie) {

        LOG.info("Creating Mp4TrackImpl " + track);
        TrackBox trackBox = new TrackBox();
        TrackHeaderBox tkhd = new TrackHeaderBox();
        int flags = 0;
        if (track.isEnabled()) {
            flags += 1;
        }

        if (track.isInMovie()) {
            flags += 2;
        }

        if (track.isInPreview()) {
            flags += 4;
        }

        if (track.isInPoster()) {
            flags += 8;
        }
        tkhd.setFlags(flags);

        tkhd.setAlternateGroup(track.getTrackMetaData().getGroup());
        tkhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        // We need to take edit list box into account in trackheader duration
        // but as long as I don't support edit list boxes it is sufficient to
        // just translate media duration to movie timescale
        tkhd.setDuration(getDuration(track) * getTimescale(movie) / track.getTrackMetaData().getTimescale());
        tkhd.setHeight(track.getTrackMetaData().getHeight());
        tkhd.setWidth(track.getTrackMetaData().getWidth());
        tkhd.setLayer(track.getTrackMetaData().getLayer());
        tkhd.setModificationTime(DateHelper.convert(new Date()));
        tkhd.setTrackId(track.getTrackMetaData().getTrackId());
        tkhd.setVolume(track.getTrackMetaData().getVolume());
        trackBox.addBox(tkhd);

        EditBox edit = new EditBox();
        EditListBox editListBox = new EditListBox();
        editListBox.setEntries(Collections.singletonList(
                new EditListBox.Entry(editListBox, (long) (track.getTrackMetaData().getStartTime() * getTimescale(movie)), -1, 1)));
        edit.addBox(editListBox);
        trackBox.addBox(edit);

        MediaBox mdia = new MediaBox();
        trackBox.addBox(mdia);
        MediaHeaderBox mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        mdhd.setDuration(getDuration(track));
        mdhd.setTimescale(track.getTrackMetaData().getTimescale());
        mdhd.setLanguage(track.getTrackMetaData().getLanguage());
        mdia.addBox(mdhd);
        HandlerBox hdlr = new HandlerBox();
        mdia.addBox(hdlr);
        switch (track.getType()) {
            case VIDEO:
                hdlr.setHandlerType("vide");
                break;
            case SOUND:
                hdlr.setHandlerType("soun");
                break;
            case HINT:
                hdlr.setHandlerType("hint");
                break;
            case TEXT:
                hdlr.setHandlerType("text");
                break;
            case AMF0:
                hdlr.setHandlerType("data");
                break;
            default:
                throw new RuntimeException("Dont know handler type " + track.getType());
        }

        MediaInformationBox minf = new MediaInformationBox();
        switch (track.getType()) {
            case VIDEO:
                VideoMediaHeaderBox vmhd = new VideoMediaHeaderBox();
                minf.addBox(vmhd);
                break;
            case SOUND:
                SoundMediaHeaderBox smhd = new SoundMediaHeaderBox();
                minf.addBox(smhd);
                break;
            case HINT:
                HintMediaHeaderBox hmhd = new HintMediaHeaderBox();
                minf.addBox(hmhd);
                break;
            case TEXT:
            case AMF0:
            case NULL:
                NullMediaHeaderBox nmhd = new NullMediaHeaderBox();
                minf.addBox(nmhd);
                break;
        }
        // dinf: all these three boxes tell us is that the actual
        // data is in the current file and not somewhere external
        DataInformationBox dinf = new DataInformationBox();
        DataReferenceBox dref = new DataReferenceBox();
        dinf.addBox(dref);
        DataEntryUrlBox url = new DataEntryUrlBox();
        url.setFlags(1);
        dref.addBox(url);
        minf.addBox(dinf);
        //

        SampleTableBox stbl = new SampleTableBox();

        stbl.addBox(track.getSampleDescriptionBox());

        List<TimeToSampleBox.Entry> decodingTimeToSampleEntries = track.getDecodingTimeEntries();
        if (decodingTimeToSampleEntries != null && !track.getDecodingTimeEntries().isEmpty()) {
            TimeToSampleBox stts = new TimeToSampleBox();
            stts.setEntries(track.getDecodingTimeEntries());
            stbl.addBox(stts);
        }

        List<CompositionTimeToSample.Entry> compositionTimeToSampleEntries = track.getCompositionTimeEntries();
        if (compositionTimeToSampleEntries != null && !compositionTimeToSampleEntries.isEmpty()) {
            CompositionTimeToSample ctts = new CompositionTimeToSample();
            ctts.setEntries(compositionTimeToSampleEntries);
            stbl.addBox(ctts);
        }

        long[] syncSamples = track.getSyncSamples();
        if (syncSamples != null && syncSamples.length > 0) {
            SyncSampleBox stss = new SyncSampleBox();
            stss.setSampleNumber(syncSamples);
            stbl.addBox(stss);
        }

        if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {
            SampleDependencyTypeBox sdtp = new SampleDependencyTypeBox();
            sdtp.setEntries(track.getSampleDependencies());
            stbl.addBox(sdtp);
        }
        int chunkSize[] = getChunkSizes(track, movie);
        SampleToChunkBox stsc = new SampleToChunkBox();
        stsc.setEntries(new LinkedList<SampleToChunkBox.Entry>());
        long lastChunkSize = Integer.MIN_VALUE; // to be sure the first chunks hasn't got the same size
        for (int i = 0; i < chunkSize.length; i++) {
            // The sample description index references the sample description box
            // that describes the samples of this chunk. My Tracks cannot have more
            // than one sample description box. Therefore 1 is always right
            // the first chunk has the number '1'
            if (lastChunkSize != chunkSize[i]) {
                stsc.getEntries().add(new SampleToChunkBox.Entry(i + 1, chunkSize[i], 1));
                lastChunkSize = chunkSize[i];
            }
        }
        stbl.addBox(stsc);

        SampleSizeBox stsz = new SampleSizeBox();
        stsz.setSampleSizes(track2SampleSizes.get(track));

        stbl.addBox(stsz);
        // The ChunkOffsetBox we create here is just a stub
        // since we haven't created the whole structure we can't tell where the
        // first chunk starts (mdat box). So I just let the chunk offset
        // start at zero and I will add the mdat offset later.
        StaticChunkOffsetBox stco = new StaticChunkOffsetBox();
        this.chunkOffsetBoxes.add(stco);
        long offset = 0;
        long[] chunkOffset = new long[chunkSize.length];
        // all tracks have the same number of chunks
        LOG.fine("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId());
        for (int i = 0; i < chunkSize.length; i++) {
            // The filelayout will be:
            // chunk_1_track_1,... ,chunk_1_track_n, chunk_2_track_1,... ,chunk_2_track_n, ... , chunk_m_track_1,... ,chunk_m_track_n
            // calculating the offsets
            LOG.finer("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId() + " chunk " + i);
            for (Track current : movie.getTracks()) {
                LOG.finest("Adding offsets of track_" + current.getTrackMetaData().getTrackId());
                int[] chunkSizes = getChunkSizes(current, movie);
                long firstSampleOfChunk = 0;
                for (int j = 0; j < i; j++) {
                    firstSampleOfChunk += chunkSizes[j];
                }
                if (current == track) {
                    chunkOffset[i] = offset;
                }
                for (int j = l2i(firstSampleOfChunk); j < firstSampleOfChunk + chunkSizes[i]; j++) {
                    offset += track2SampleSizes.get(current)[j];
                }
            }
        }
        stco.setChunkOffsets(chunkOffset);
        stbl.addBox(stco);
        minf.addBox(stbl);
        mdia.addBox(minf);

        return trackBox;
    }

    private class InterleaveChunkMdat implements Box {
        List<Track> tracks;
        List<ByteBuffer> samples = new LinkedList<ByteBuffer>();
        ContainerBox parent;

        long contentSize = 0;

        public ContainerBox getParent() {
            return parent;
        }

        public void setParent(ContainerBox parent) {
            this.parent = parent;
        }

        public void parse(ReadableByteChannel inFC, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        }

        private InterleaveChunkMdat(Movie movie) {

            tracks = movie.getTracks();
            Map<Track, int[]> chunks = new HashMap<Track, int[]>();
            for (Track track : movie.getTracks()) {
                chunks.put(track, getChunkSizes(track, movie));
            }

            for (int i = 0; i < chunks.values().iterator().next().length; i++) {
                for (Track track : tracks) {

                    int[] chunkSizes = chunks.get(track);
                    long firstSampleOfChunk = 0;
                    for (int j = 0; j < i; j++) {
                        firstSampleOfChunk += chunkSizes[j];
                    }

                    for (int j = l2i(firstSampleOfChunk); j < firstSampleOfChunk + chunkSizes[i]; j++) {

                        Sample s = DefaultMp4Builder.this.track2Sample.get(track).get(j);
                        contentSize += s.getBytes().limit();
                        samples.add((ByteBuffer) s.getBytes().rewind());
                    }

                }

            }

        }

        public long getDataOffset() {
            Box b = this;
            long offset = 16;
            while (b.getParent() != null) {
                for (Box box : b.getParent().getBoxes()) {
                    if (b == box) {
                        break;
                    }
                    offset += box.getSize();
                }
                b = b.getParent();
            }
            return offset;
        }


        public String getType() {
            return "mdat";
        }

        public long getSize() {
            return 16 + contentSize;
        }

        private boolean isSmallBox(long contentSize) {
            return (contentSize + 8) < 4294967296L;
        }


        public void getBox(WritableByteChannel writableByteChannel) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(16);
            long size = getSize();
            if (isSmallBox(size)) {
                IsoTypeWriter.writeUInt32(bb, size);
            } else {
                IsoTypeWriter.writeUInt32(bb, 1);
            }
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            if (isSmallBox(size)) {
                bb.put(new byte[8]);
            } else {
                IsoTypeWriter.writeUInt64(bb, size);
            }
            bb.rewind();
            writableByteChannel.write(bb);
            if (writableByteChannel instanceof GatheringByteChannel) {
                List<ByteBuffer> nuSamples = unifyAdjacentBuffers(samples);

                int STEPSIZE = 1024;
                for (int i = 0; i < Math.ceil((double) nuSamples.size() / STEPSIZE); i++) {
                    List<ByteBuffer> sublist = nuSamples.subList(
                            i * STEPSIZE, // start
                            (i + 1) * STEPSIZE < nuSamples.size() ? (i + 1) * STEPSIZE : nuSamples.size()); // end
                    ByteBuffer sampleArray[] = sublist.toArray(new ByteBuffer[sublist.size()]);
                    do {
                        ((GatheringByteChannel) writableByteChannel).write(sampleArray);
                    } while (sampleArray[sampleArray.length - 1].remaining() > 0);
                }
                //System.err.println(bytesWritten);
            } else {
                for (ByteBuffer sample : samples) {
                    sample.rewind();
                    writableByteChannel.write(sample);
                }
            }
        }

    }

    /**
     * Gets the chunk sizes for the given track.
     *
     * @param track
     * @param movie
     * @return
     */
    int[] getChunkSizes(Track track, Movie movie) {

        int[] referenceChunkStarts = intersectionFinder.sampleNumbers(track, movie);
        int[] chunkSizes = new int[referenceChunkStarts.length];


        for (int i = 0; i < referenceChunkStarts.length; i++) {
            int start = referenceChunkStarts[i] - 1;
            int end;
            if (referenceChunkStarts.length == i + 1) {
                end = track.getSamples().size() - 1;
            } else {
                end = referenceChunkStarts[i + 1] - 1;
            }

            chunkSizes[i] = end - start;
            // The Stretch makes sure that there are as much audio and video chunks!
        }
        assert DefaultMp4Builder.this.track2Sample.get(track).size() == sum(chunkSizes) : "The number of samples and the sum of all chunk lengths must be equal";
        return chunkSizes;


    }


    private static long sum(int[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }

    protected static long getDuration(Track track) {
        long duration = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    public long getTimescale(Movie movie) {
        long timescale = movie.getTracks().iterator().next().getTrackMetaData().getTimescale();
        for (Track track : movie.getTracks()) {
            timescale = gcd(track.getTrackMetaData().getTimescale(), timescale);
        }
        return timescale;
    }

    public static long gcd(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public List<ByteBuffer> unifyAdjacentBuffers(List<ByteBuffer> samples) {
        ArrayList<ByteBuffer> nuSamples = new ArrayList<ByteBuffer>(samples.size());
        for (ByteBuffer buffer : samples) {
            int lastIndex = nuSamples.size() - 1;
            if (lastIndex >= 0 && buffer.hasArray() && nuSamples.get(lastIndex).hasArray() && buffer.array() == nuSamples.get(lastIndex).array() &&
                    nuSamples.get(lastIndex).arrayOffset() + nuSamples.get(lastIndex).limit() == buffer.arrayOffset()) {
                ByteBuffer oldBuffer = nuSamples.remove(lastIndex);
                ByteBuffer nu = ByteBuffer.wrap(buffer.array(), oldBuffer.arrayOffset(), oldBuffer.limit() + buffer.limit()).slice();
                // We need to slice here since wrap([], offset, length) just sets position and not the arrayOffset.
                nuSamples.add(nu);
            } else if (lastIndex >= 0 &&
                    buffer instanceof MappedByteBuffer && nuSamples.get(lastIndex) instanceof MappedByteBuffer &&
                    nuSamples.get(lastIndex).limit() == nuSamples.get(lastIndex).capacity() - buffer.capacity()) {
                // This can go wrong - but will it?
                ByteBuffer oldBuffer = nuSamples.get(lastIndex);
                oldBuffer.limit(buffer.limit() + oldBuffer.limit());
            } else {
                nuSamples.add(buffer);
            }
        }
        return nuSamples;
    }
}
