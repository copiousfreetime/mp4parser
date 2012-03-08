package com.googlecode.mp4parser.uvvu;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.FreeBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.ItemDataBox;
import com.coremedia.iso.boxes.ItemLocationBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.ProgressiveDownloadInformationBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.WriteListener;
import com.coremedia.iso.boxes.XmlBox;
import com.coremedia.iso.boxes.dece.TrickPlayBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessOffsetBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBaseMediaDecodeTimeBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentRandomAccessBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.boxes.basemediaformat.AvcNalUnitStorageBox;
import com.googlecode.mp4parser.boxes.ultraviolet.AssetInformationBox;
import com.googlecode.mp4parser.boxes.ultraviolet.BaseLocationBox;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class UvvuBuilder extends FragmentedMp4Builder {

    private String baseLocation = "";
    private String purchaseLocation = "";
    private String apid = "";
    private String metaXml = "";
    private ByteBuffer idatContent = ByteBuffer.allocate(0);

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public void setPurchaseLocation(String purchaseLocation) {
        this.purchaseLocation = purchaseLocation;
    }

    public void setApid(String apid) {
        this.apid = apid;
    }

    public void setMetaXml(String metaXml) {
        this.metaXml = metaXml;
    }

    @Override
    public Box createFtyp(Movie movie) {
        return new FileTypeBox("ccff", 0, Arrays.asList("isom", "avc1", "iso6"));
    }

    public Box createPdin(Movie movie) {
        ProgressiveDownloadInformationBox pdin = new ProgressiveDownloadInformationBox();
        LinkedList<ProgressiveDownloadInformationBox.Entry> entries =
                new LinkedList<ProgressiveDownloadInformationBox.Entry>();
        long size = 0;
        long durationInSeconds = 0;
        for (Track track : movie.getTracks()) {
            for (ByteBuffer byteBuffer : track.getSamples()) {
                size += byteBuffer.limit();
            }
            long tracksDuration = getDuration(track) / track.getTrackMetaData().getTimescale();
            if (tracksDuration > durationInSeconds) {
                durationInSeconds = tracksDuration;
            }

        }
        size *= 1.1; // just some room for metadata and errors - this is no exact science


        long videoRate = size / durationInSeconds;
        long dlRate = 10000;
        do {
//            long waitTime = (videoRate - dlRate) * durationInSeconds / dlRate;
            long waitTime = (videoRate * durationInSeconds) / dlRate - durationInSeconds;
            ProgressiveDownloadInformationBox.Entry entry =
                    new ProgressiveDownloadInformationBox.Entry(dlRate, waitTime > 0 ? waitTime + 3 : 0);
            entries.add(entry);
            dlRate *= 2; // double dlRate 10k 20k 40k 80k 160k 320k 640k 1.2m 2.5m 5m
        }
        while (videoRate > dlRate);
        pdin.setEntries(entries);
        return pdin;
    }


    @Override
    protected Box createMoov(Movie movie) {
        MovieBox movieBox = new MovieBox();

        movieBox.addBox(createMvhd(movie));
        movieBox.addBox(createAinf(movie));
        movieBox.addBox(createMeta());


        for (Track track : movie.getTracks()) {
            movieBox.addBox(createTrak(track, movie));
        }
        movieBox.addBox(createMvex(movie));
        movieBox.addBox(new FreeBox(65535));
        // metadata here
        return movieBox;

    }

    protected Box createAinf(Movie movie) {


        int height = 0;

        for (Track track : movie.getTracks()) {

            if ("vide".equals(track.getHandler())) {
                VisualSampleEntry vse = track.getSampleDescriptionBox().getBoxes(VisualSampleEntry.class).get(0);
                height = vse.getHeight();
            }
        }
        if (height == 0) {
            throw new RuntimeException("Could not determine height of video.");

        }
        String profileVersion;
        if (height >= 600) {
            profileVersion = "hdv1";
        } else if (height >= 360) {
            profileVersion = "sdv1";
        } else {
            profileVersion = "pdv1";
        }
        AssetInformationBox ainf = new AssetInformationBox();
        ainf.setProfileVersion(profileVersion);
        ainf.setApid(apid);
        return ainf;
    }

    @Override
    protected Box createTraf(int startSample, int endSample, Track track, int sequenceNumber) {
        TrackFragmentBox traf = new TrackFragmentBox();
        traf.addBox(createTfhd(startSample, endSample, track, sequenceNumber));
        traf.addBox(new TrackFragmentBaseMediaDecodeTimeBox());
        for (Box trun : createTruns(startSample, endSample, track, sequenceNumber)) {
            traf.addBox(trun);
        }
        if (track.getSampleDescriptionBox().getSampleEntry() instanceof VisualSampleEntry) {
            List<AvcConfigurationBox> avccs = track.getSampleDescriptionBox().getSampleEntry().getBoxes(AvcConfigurationBox.class);
            for (AvcConfigurationBox avcc : avccs) {
                traf.addBox(new AvcNalUnitStorageBox(avcc));
            }


        }
        traf.addBox(new TrickPlayBox());

        return traf;

    }

    protected Box createMeta() {
        MetaBox metaBox = new MetaBox();
        HandlerBox hdlr = new HandlerBox();
        hdlr.setHandlerType("cfmd");
        hdlr.setName("Required Metadata");
        metaBox.addBox(hdlr);
        XmlBox xmlBox = new XmlBox();
        xmlBox.setXml(metaXml + "\u0000");
        metaBox.addBox(xmlBox);

        metaBox.addBox(createIloc());
        metaBox.addBox(new ItemDataBox());
        return metaBox;
    }

    protected Box createIloc() {

        ItemLocationBox iloc = new ItemLocationBox();
        iloc.setVersion(1);
        int valueLength = 8;
        iloc.setIndexSize(valueLength);
        iloc.setOffsetSize(valueLength);
        iloc.setLengthSize(valueLength);
        iloc.setBaseOffsetSize(valueLength);

        ItemLocationBox.Extent extent = iloc.createExtent(0, idatContent.limit(), 0);
        ItemLocationBox.Item item = iloc.createItem(1, 1, 0, 0, new ItemLocationBox.Extent[]{extent});
        iloc.setItems(new ItemLocationBox.Item[]{item});

    /*    iloc.addWriteListener(new WriteListener() {
            public void beforeWrite(long offset) {
                long offsetToData = idat.calculateOffset() + idat.getHeader().length;
                item.setBaseOffsetAdjustingExtents(offsetToData);
            }
        }); */

        return iloc;
    }

    @Override
    protected Box createStbl(Movie movie, Track track) {
        SampleTableBox stbl = new SampleTableBox();
        stbl.addBox(track.getSampleDescriptionBox());
        stbl.addBox(new TimeToSampleBox());
        stbl.addBox(new SampleToChunkBox());
        stbl.addBox(new SampleSizeBox());
        stbl.addBox(new StaticChunkOffsetBox());
        return stbl;
    }

    public IsoFile build(Movie movie) throws IOException {

        IsoFile isoFile = new IsoFile();
        isoFile.addBox(createFtyp(movie));
        isoFile.addBox(createPdin(movie));
        isoFile.addBox(new BaseLocationBox(baseLocation, purchaseLocation));
        isoFile.addBox(createMoov(movie));

        for (Box box : createMoofMdat(movie)) {
            isoFile.addBox(box);
        }
        MovieFragmentRandomAccessBox mfra = new MovieFragmentRandomAccessBox();
        for (Track track : movie.getTracks()) {
            TrackFragmentRandomAccessBox tfra = new TrackFragmentRandomAccessBox();
            mfra.addBox(tfra);
        }
        MovieFragmentRandomAccessOffsetBox mfro = new MovieFragmentRandomAccessOffsetBox();
        mfra.addBox(mfro);
        mfro.setMfraSize(mfra.getSize());

        return isoFile;
    }

    public static void main(String[] args) throws IOException {
        MovieCreator mc = new MovieCreator();
        Movie m = mc.build(new FileInputStream("/home/sannies/scm/svn/mp4parser/uvu_source.mp4").getChannel());

        UvvuBuilder uvvuBuilder = new UvvuBuilder();
        uvvuBuilder.setApid("urn:dece:apid:IMDB:tt1632708:sd1");
        uvvuBuilder.setBaseLocation("iot.castlabs.decelc.com");
        uvvuBuilder.setMetaXml("<mdd:MetadataMovie xmlns:mdd=\"http://www.decellc.org/schema/mddece\"><mdd:ContentMetadata><mdd:ContentID>urn:dece:cid:IMDB:tt1632708</mdd:ContentID><mdd:DECEMediaProfile>HD</mdd:DECEMediaProfile><mdd:RunLength>PT1H49M</mdd:RunLength><mdd:Publisher xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/><mdd:ReleaseYear>2010</mdd:ReleaseYear><mdd:TitleDisplay19>Friends with Ben...</mdd:TitleDisplay19><mdd:TitleDisplay60>Friends with Benefits</mdd:TitleDisplay60><mdd:TitleSortable>Friends with Benefits</mdd:TitleSortable><mdd:Summary190>While trying to avoid the clich?s of Hollywood romantic comedies, Dylan and Jamie soon discover however that adding the act of sex to their friendship does lead to complications.</mdd:Summary190><mdd:DescriptionLanguage>en-US</mdd:DescriptionLanguage></mdd:ContentMetadata><mdd:RequiredImages><md:Width xmlns:md=\"http://www.movielabs.com/schema/md/v1.07/md\">320</md:Width><md:Height xmlns:md=\"http://www.movielabs.com/schema/md/v1.07/md\">473</md:Height><md:Encoding xmlns:md=\"http://www.movielabs.com/schema/md/v1.07/md\">image/jpeg</md:Encoding><md:TrackReference xmlns:md=\"http://www.movielabs.com/schema/md/v1.07/md\">urn:dece:container:imageindex:1</md:TrackReference></mdd:RequiredImages><mdd:TrackMetadata><mdd:Track><md:Video xmlns:md=\"http://www.movielabs.com/schema/md/v1.07/md\"><md:Picture><md:AspectRatio>1.0</md:AspectRatio><md:WidthPixels>1694</md:WidthPixels><md:HeightPixels>720</md:HeightPixels></md:Picture><md:ColorType>color</md:ColorType></md:Video><mdd:SegmentSize>104857600</mdd:SegmentSize></mdd:Track></mdd:TrackMetadata></mdd:MetadataMovie>");
        IsoFile mine = uvvuBuilder.build(m);
        Test.walk(mine, "");
        mine.getBox(new FileOutputStream("/home/sannies/scm/svn/mp4parser/uvu_me.uvu").getChannel());

    }

}
