package com.googlecode.mp4parser;

import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Gets the duration of a video.
 */
public class GetDuration {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(readFully(MuxExample.class.getResourceAsStream("/count-video.mp4"))));
        isoFile.parse();
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        System.err.println(lengthInSeconds);

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
