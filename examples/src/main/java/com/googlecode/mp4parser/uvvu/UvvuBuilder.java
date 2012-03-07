package com.googlecode.mp4parser.uvvu;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FreeBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.ItemDataBox;
import com.coremedia.iso.boxes.ItemLocationBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.ProgressiveDownloadInformationBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
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
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 3/7/12
 * Time: 8:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class UvvuBuilder extends FragmentedMp4Builder {


    @Override
    protected Box createMoov(Movie movie) {
        MovieBox movieBox = new MovieBox();

        movieBox.addBox(createMvhd(movie));
        movieBox.addBox(new AssetInformationBox());
        movieBox.addBox(createMeta());


        for (Track track : movie.getTracks()) {
            movieBox.addBox(createTrak(track, movie));
        }
        movieBox.addBox(createMvex(movie));
        movieBox.addBox(new FreeBox(65535));
        // metadata here
        return movieBox;

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
        metaBox.addBox(new HandlerBox());
        metaBox.addBox(new XmlBox());
        metaBox.addBox(new ItemLocationBox());
        metaBox.addBox(new ItemDataBox());
        return metaBox;
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
        isoFile.addBox(new ProgressiveDownloadInformationBox());
        isoFile.addBox(new BaseLocationBox());
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
        IsoFile mine = uvvuBuilder.build(m);
        Test.walk(mine, "");

    }

}
