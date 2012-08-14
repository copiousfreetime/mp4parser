package com.googlecode.mp4parser.tools.smoothstreamingdownloader;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.util.Path;
import nu.xom.*;
import org.apache.commons.io.IOUtils;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SmoothStreamingTrack implements Track {
    String fourCC;
    String codecPrivateData;
    TrackMetaData trackMetaData;
    List<IsoFile> fragments = new LinkedList<IsoFile>();

    public SmoothStreamingTrack(URI manifestFile, String type, String bitRate) throws IOException, ParsingException {
        Builder parser = new Builder();
        Document doc = parser.build(manifestFile.toASCIIString());
        Nodes levelNodes = doc.query(String.format("/SmoothStreamingMedia/StreamIndex[@Type='%s']/QualityLevel[@Bitrate=%s]", type, bitRate));
        Nodes fragments = doc.query(String.format("/SmoothStreamingMedia/StreamIndex[@Type='%s']/c", type));
        if (levelNodes.size() != 1) {
            throw new IOException("Something's wrong");
        }
        Element level = (Element) levelNodes.get(0);
        fourCC = level.getAttribute("FourCC").getValue();
        codecPrivateData = level.getAttribute("CodecPrivateData").getValue();

        trackMetaData = new TrackMetaData();
        if ("avc1".equalsIgnoreCase(fourCC)) {
            // video!
            trackMetaData.setHeight(Double.parseDouble(level.getAttribute("MaxHeight").getValue()));
            trackMetaData.setWidth(Double.parseDouble(level.getAttribute("MaxWidth").getValue()));
            trackMetaData.setTimescale(10000000L); // That's the default but it may be some other value
        }

        String url = ((Element) level.getParent()).getAttribute("Url").getValue();
        long time = 0;
        for (int i = 0; i < fragments.size(); i++) {
            URI fragment = manifestFile.resolve(url.replace("{bitrate}", bitRate).replace("{start time}", Long.toString(time)));
            this.fragments.add(new IsoFile(Channels.newChannel(fragment.toURL().openStream())));
            time += Long.parseLong(((Element) fragments.get(i)).getAttribute("d").getValue());
        }


    }

    @Override
    public SampleDescriptionBox getSampleDescriptionBox() {
        SampleDescriptionBox stsd = new SampleDescriptionBox();
        if ("avc1".equalsIgnoreCase(fourCC)) {
            VisualSampleEntry vse = new VisualSampleEntry("avc1");
            vse.setHorizresolution((int) trackMetaData.getWidth());
            vse.setHeight((int) trackMetaData.getHeight());


            AvcConfigurationBox avcC = new AvcConfigurationBox();
            vse.addBox(avcC);
            AvcConfigurationBox.AVCDecoderConfigurationRecord record = new AvcConfigurationBox.AVCDecoderConfigurationRecord();
            ByteBuffer spsAndPps = ByteBuffer.wrap(Hex.decodeHex(codecPrivateData));

            ByteBuffer allSequenceParameterSets = findNextNal(spsAndPps);
            List<byte[]> sequenceParameterSets = new LinkedList<byte[]>();
            allSequenceParameterSets.position(0);
            while (allSequenceParameterSets.position() <= allSequenceParameterSets.limit()) {
                int length = IsoTypeReader.readUInt16(allSequenceParameterSets);
                sequenceParameterSets.add((byte[]) allSequenceParameterSets.duplicate().slice().limit(length).array());
                allSequenceParameterSets.position(allSequenceParameterSets.position() + length);
            }

            ByteBuffer allPictureParameterSets = findNextNal(spsAndPps);
            List<byte[]> pictureParameterSets = new LinkedList<byte[]>();
            allPictureParameterSets.position(0);
            while (allPictureParameterSets.position() <= allPictureParameterSets.limit()) {
                int length = IsoTypeReader.readUInt16(allPictureParameterSets);
                pictureParameterSets.add((byte[]) allPictureParameterSets.duplicate().slice().limit(length).array());
                allPictureParameterSets.position(allPictureParameterSets.position() + length);
            }

            avcC.setSequenceParameterSets(sequenceParameterSets);
            avcC.setPictureParameterSets(pictureParameterSets);
            stsd.addBox(vse);
        }
        return stsd;
    }

    @Override
    public List<TimeToSampleBox.Entry> getDecodingTimeEntries() {
        List<TimeToSampleBox.Entry> entries = new LinkedList<TimeToSampleBox.Entry>();
        for (IsoFile fragment : fragments) {
            TrackRunBox trun = (TrackRunBox) Path.getPath(fragment, "/moof[0]/traf[0]/trun[0]");
            for (TrackRunBox.Entry entry : trun.getEntries()) {
                entries.add(new TimeToSampleBox.Entry(entry.getSampleDuration(), 1));
            }
        }
        return entries;

    }

    @Override
    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        List<CompositionTimeToSample.Entry> entries = new LinkedList<CompositionTimeToSample.Entry>();
        for (IsoFile fragment : fragments) {
            TrackRunBox trun = (TrackRunBox) Path.getPath(fragment, "/moof[0]/traf[0]/trun[0]");
            for (TrackRunBox.Entry entry : trun.getEntries()) {
                entries.add(new CompositionTimeToSample.Entry(entry.getSampleCompositionTimeOffset(), 1));
            }
        }
        return entries;
    }

    @Override
    public long[] getSyncSamples() {
        int numSyncSamples = 0;
        for (IsoFile fragment : fragments) {
            TrackRunBox trun = (TrackRunBox) Path.getPath(fragment, "/moof[0]/traf[0]/trun[0]");
            for (TrackRunBox.Entry entry : trun.getEntries()) {
                if (!entry.getSampleFlags().isSampleIsDifferenceSample()) {
                    numSyncSamples++;
                }
            }
        }
        long[] syncSamples = new long[numSyncSamples];
        int sampleNum = 1;
        int syncSampleNum = 0;
        for (IsoFile fragment : fragments) {
            TrackRunBox trun = (TrackRunBox) Path.getPath(fragment, "/moof[0]/traf[0]/trun[0]");
            for (TrackRunBox.Entry entry : trun.getEntries()) {
                if (!entry.getSampleFlags().isSampleIsDifferenceSample()) {
                    syncSamples[syncSampleNum++] = sampleNum;
                }
                sampleNum++;
            }
        }
        return syncSamples;

    }

    @Override
    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return null; // no sample dependencies for us
    }

    @Override
    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    @Override
    public String getHandler() {
        if ("avc1".equalsIgnoreCase(fourCC)) {
            return "vide";
        }
        throw new RuntimeException();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isInMovie() {
        return true;
    }

    @Override
    public boolean isInPreview() {
        return true;
    }

    @Override
    public boolean isInPoster() {
        return true;
    }

    @Override
    public List<ByteBuffer> getSamples() {
        List<ByteBuffer> samples = new ArrayList<ByteBuffer>();
        for (IsoFile fragment : fragments) {
            TrackFragmentBox traf = (TrackFragmentBox) Path.getPath(fragment, "/moof[0]/traf[0]");
            samples.addAll(new SampleList(traf));
        }
        return samples;
    }

    @Override
    public Box getMediaHeaderBox() {
        if ("avc1".equalsIgnoreCase(fourCC)) {
            return new VideoMediaHeaderBox();

        }
        throw new RuntimeException();
    }

    @Override
    public SubSampleInformationBox getSubsampleInformationBox() {
        return null; // no subsamples for us
    }

    private ByteBuffer findNextNal(ByteBuffer bb) {
        byte[] test = new byte[]{-1, -1, -1, -1};
        int start = -1;

        int c;
        while ((c = bb.get()) != -1) {
            test[0] = test[1];
            test[1] = test[2];
            test[2] = test[3];
            test[3] = (byte) c;
            if (test[0] == 0 && test[1] == 0 && test[2] == 0 && test[3] == 1) {

                if (start == -1) {
                    start = bb.position();
                    test = new byte[]{-1, -1, -1, -1};
                } else {
                    break;
                }

            }
        }
        if (test[0] == 0 && test[1] == 0 && test[2] == 0 && test[3] == 1) {
            bb.position(bb.position() - 4);
        }
        int length = bb.position() - start;
        ByteBuffer nu = bb.duplicate();
        nu.position(start);
        nu.slice();
        nu.limit(length);
        return nu;
    }

}
