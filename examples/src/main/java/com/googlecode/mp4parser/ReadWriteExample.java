package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ReadWriteExample {

    private static String[] files = new String[]{
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-128h-75kbps.mp4"
/*            "/home/sannies/scm/svn/mp4parser-release/test-data/video-192h-155kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-240h-231kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-320h-388kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-400h-580kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-480h-804kbps.mp4",
            "/home/sannies/scm/svn/mp4parser-release/test-data/video-560h-1062kbps.mp4"*/
    };

    public static void main(String[] args) throws IOException {
        MovieCreator mc = new MovieCreator();

        for (String file : files) {
            Movie video = mc.build(new RandomAccessFile(file, "r").getChannel());

            IsoFile out1 = new FragmentedMp4Builder().build(video);
            IsoFile out2 = new DefaultMp4Builder().build(video);

            File parentDir1 = new File(file).getParentFile();
            parentDir1 = new File(parentDir1, "abc");
            File outf1 = new File(parentDir1, new File(file).getName());
            FileChannel fc1 = new RandomAccessFile(outf1.getAbsolutePath(), "rw").getChannel();
            fc1.position(0);
            out1.getBox(fc1);
            fc1.truncate(fc1.position());
            fc1.close();

            File parentDir2 = new File(file).getParentFile();
            parentDir2 = new File(parentDir2, "abc");
            File outf2 = new File(parentDir2, new File(file).getName());
            FileChannel fc2 = new RandomAccessFile(outf2.getAbsolutePath() + "_reg", "rw").getChannel();
            fc2.position(0);
            out2.getBox(fc2);
            fc2.truncate(fc2.position());
            fc2.close();


        }


    }


}
