package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

/**
 * Gets the duration of a video.
 */
public class GetDuration {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile(Channels.newChannel(MuxExample.class.getResourceAsStream("/count-video.mp4")));
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        System.err.println(lengthInSeconds);

    }

}
