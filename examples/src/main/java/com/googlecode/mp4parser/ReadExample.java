package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

/**
 *
 */
public class ReadExample {
    public static void main(String[] args) throws IOException {
        FileChannel fc = new RandomAccessFile("/media/scratch/ThreeHundredFourtyThreeMB.mp4", "rw").getChannel();
        IsoFile isoFile = new IsoFile(fc);

        HandlerBox hdlr = (HandlerBox) Path.getPath(isoFile, "/moov[0]/trak[0]/mdia[0]/hdlr[0]");
        hdlr.setName("onetwothreefourfivesix");



    }


}
