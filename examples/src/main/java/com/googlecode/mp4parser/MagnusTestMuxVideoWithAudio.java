package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AC3TrackImpl;
import com.googlecode.mp4parser.authoring.tracks.EC3TrackImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.List;


public class MagnusTestMuxVideoWithAudio {

    public static void main(String[] args) throws IOException {
        Movie movie = new Movie();
        Movie invideo = new MovieCreator().build(Channels.newChannel(new FileInputStream(args[0])));
        List<Track> tracks = invideo.getTracks();

        for (Track t : tracks) {
            String type = t.getMediaHeaderBox().getType();
            String ctype = t.getSampleDescriptionBox().getSampleEntry().getType();
            System.out.println("Track of type " + type + " (" + ctype + ")");
            if (type.equals("vmhd")) {
                movie.addTrack(t);
                System.out.println("Adding track to outputfile");
            }
        }

        FileInputStream fin = new FileInputStream(args[1]);
        EC3TrackImpl ec3 = new EC3TrackImpl(fin);
        movie.addTrack(ec3);

        IsoFile out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(new File(args[2]));
        out.getBox(fos.getChannel());
        fos.close();



    }
}
