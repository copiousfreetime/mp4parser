package com.googlecode.mp4parser.smoothstreaming;


import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.smoothstreaming.FlatPackageWriterImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class FragmentFileSet {
    static String[] inputs = new String[]{
            "/home/sannies/scm/svn/mp4parser-release/test-data/audio-96000.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-128h-75kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-192h-155kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-240h-231kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-320h-388kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-400h-580kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-480h-804kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-560h-1062kbps.mp4"
    };

    public static void main(String[] args) throws IOException {
        FlatPackageWriterImpl flatPackageWriter = new FlatPackageWriterImpl();
        flatPackageWriter.setOutputDirectory(new File("/home/sannies/smootstreaming"));
        MovieCreator movieCreator = new MovieCreator();
        Movie movie = new Movie();
        for (String input : inputs) {
            FileChannel fc = new FileInputStream(input).getChannel();
            Movie m = movieCreator.build(fc);
            for (Track track : m.getTracks()) {
                movie.addTrack(track);
            }

        }
        flatPackageWriter.write(movie);
    }
}
