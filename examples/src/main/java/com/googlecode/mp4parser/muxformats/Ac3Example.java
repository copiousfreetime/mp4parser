package com.googlecode.mp4parser.muxformats;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AC3TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class Ac3Example {
    public static void main(String[] args) throws IOException {
        AC3TrackImpl ac3Track = new AC3TrackImpl(Ac3Example.class.getResourceAsStream("/count-english.ac3"));
        Movie m = new Movie();
        m.addTrack(ac3Track);
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        IsoFile isoFile = mp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("output.mp4");
        isoFile.getBox(fos.getChannel());
        fos.close();
    }
}
