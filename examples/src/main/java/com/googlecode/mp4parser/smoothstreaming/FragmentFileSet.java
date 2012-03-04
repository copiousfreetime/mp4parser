package com.googlecode.mp4parser.smoothstreaming;


import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.smoothstreaming.FlatPackageWriterImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;

public class FragmentFileSet {
    static String[] inputs = new String[]{
            "/smoothstreaming/audio-96000.mp4",
            "/smoothstreaming/video-128h-75kbps.mp4",
            "/smoothstreaming/video-192h-155kbps.mp4",
            "/smoothstreaming/video-240h-231kbps.mp4",
            "/smoothstreaming/video-320h-388kbps.mp4"
    };

    public static void main(String[] args) throws IOException {
        FlatPackageWriterImpl flatPackageWriter = new FlatPackageWriterImpl();
        flatPackageWriter.setOutputDirectory(new File("smooth"));
        MovieCreator movieCreator = new MovieCreator();
        Movie movie = new Movie();
        for (String input : inputs) {
            Movie m = movieCreator.build(Channels.newChannel(FragmentFileSet.class.getResourceAsStream(input)));
            for (Track track : m.getTracks()) {
                movie.addTrack(track);
            }

        }
        flatPackageWriter.write(movie);
    }
}
