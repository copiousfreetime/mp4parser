package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.mdat.Sample;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class ReadExample {
    public static void main(String[] args) throws IOException {
        MovieCreator mc = new MovieCreator();

        Movie video = mc.build(new RandomAccessFile("/home/sannies/scm/svn/mp4parser-release/test-data/xyz/video-128h-75kbps.mp4", "rw").getChannel());


        Iterator<? extends Sample> iter =  video.getTracks().get(0).getSamples().subList(55,65).iterator();
        for (int i = 55; i < 65; i++) {
            if (video.getTracks().get(0).getSamples().get(i).getBytes().equals(iter.next().getBytes())) {
                System.err.println(i + ": " + video.getTracks().get(0).getSamples().get(i).getBytes().limit());
            } else {
                System.err.println("errpor");
            }
        }

        //Movie video = mc.build(new RandomAccessFile(String.format("/home/sannies/suckerpunch-samurai_h640w.mp4"), "rw").getChannel());


    }


}
