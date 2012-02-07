package com.googlecode.mp4parser;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.googlecode.mp4parser.authoring.tracks.RawH264Track;
import com.googlecode.mp4parser.h264.model.NALUnit;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class RawH264Example {
    public static void main(String[] args) throws IOException {
        RawH264Track track = new RawH264Track(new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-samurai_h640w_track1.h264")));
        for (IsoBufferWrapper buf : track.getSamples()) {
            System.err.println("--------------------------------------------");
            while (buf.remaining() > 0) {
                int length = (int) buf.readUInt32();
                NALUnit nalUnit = NALUnit.read(buf.getSegment(buf.position(), length));
                buf.skip(length);
                System.err.println(nalUnit);
            }
            System.err.println("--------------------------------------------");
        }
    }
}
