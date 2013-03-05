package com.googlecode.mp4parser.muxformats;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-23
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
public class H264Example {
    public static void main(String[] args) throws IOException {
        H264TrackImpl h264Track = new H264TrackImpl(new BufferedInputStream(new FileInputStream("/home/sannies2/Downloads/lv.h264")));
        AACTrackImpl aacTrack = new AACTrackImpl(new FileInputStream("/home/sannies2/Downloads/lv.aac").getChannel());
        Movie m = new Movie();
        m.addTrack(h264Track);
        m.addTrack(aacTrack);

        {
            IsoFile out = new DefaultMp4Builder().build(m);
            FileOutputStream fos = new FileOutputStream(new File("h264_output.mp4"));
            out.getBox(fos.getChannel());
            fos.close();
        }
    }
}