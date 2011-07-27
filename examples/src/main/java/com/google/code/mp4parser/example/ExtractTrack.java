package com.google.code.mp4parser.example;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.mdat.MediaDataBox;
import com.coremedia.iso.boxes.mdat.SampleList;

import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 7/27/11
 * Time: 9:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractTrack {
    public static void main(String[] args) throws IOException {
        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("First argument is the file");
            System.exit(123);
        }
        long trackNumber = 0;
        try {
            trackNumber = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Second argument is the track number");
            System.exit(123);
        }



        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(new File(args[0]));
        IsoFile isoFile = new IsoFile(ibw);


        List<TrackBox> trackBoxes = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class);
        TrackBox myTrackBox = null;
        for (TrackBox trackBox : trackBoxes) {
            if (trackBox.getTrackHeaderBox().getTrackId() == trackNumber) {
                myTrackBox = trackBox;
            }
        }
        if (myTrackBox == null) {
            System.err.println("Cannot find track with id " + trackNumber);
            System.exit(123);
        }
        List<Box> allMovieBoxChildren =  isoFile.getBoxes(MovieBox.class).get(0).getBoxes();
        trackBoxes.remove(myTrackBox);
        allMovieBoxChildren.removeAll(trackBoxes);
        isoFile.getBoxes(MovieBox.class).get(0).setBoxes(allMovieBoxChildren);
        // no we deleted all unused tracks
        SampleList sampleList = new SampleList(myTrackBox);
        // and have all samples of the track right here!

        // Since we have to completely rewrite all mdat boxes we will remove them
        List<MediaDataBox> allMediaDataBoxes = isoFile.getBoxes(MediaDataBox.class);
        List<Box> allTopLevel = isoFile.getBoxes();
        allTopLevel.removeAll(allMediaDataBoxes);
        isoFile.setBoxes(allTopLevel);
        // fine! all mdats removed.

        //get sample to chunk box.
        SampleTableBox stbl =  myTrackBox.getMediaBox().getMediaInformationBox().getSampleTableBox();
        //stbl.getSampleToChunkBox().getEntries()


    }
}
