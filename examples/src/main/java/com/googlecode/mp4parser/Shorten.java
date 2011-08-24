package com.googlecode.mp4parser;

import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Shorten {
    public static void main(String[] args) throws IOException {
        Movie movie = new MovieCreator().build(new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-distantplanet_h1080p/suckerpunch-distantplanet_h1080p.mov")));
        // I know that there is just one track
        List<Track> nuTracks = new LinkedList<Track>();
        for (Track track : movie.getTracks()) {
            if (track.getType() == Track.Type.VIDEO) {
                long startSample = track.getSyncSamples()[3];
                nuTracks.add(new CroppedTrack(track, startSample, startSample + 250));
            }
        }

        movie.setTracks(nuTracks);
        IsoFile out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
        out.getBox(new IsoOutputStream(fos));
        fos.close();


    }


    static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }


}
