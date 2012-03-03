package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class BitReaderBuffer {

    private ByteBuffer buffer;
    int position;

    public BitReaderBuffer(ByteBuffer buffer) {
        this.buffer = buffer;

    }

    public int readBits(int i) {
        byte b = buffer.get(position / 8);
        int v = b < 0 ? b + 256 : b;
        int left = 8 - position % 8;
        int rc;
        if (i <= left) {
            rc = (v << (position % 8) & 0xFF) >> ((position % 8) + (left - i));
            position += i;
        } else {
            int now = left;
            int then = i - left;
            rc = readBits(now);
            rc = rc << then;
            rc += readBits(then);
        }
        buffer.position((int) Math.ceil((double) position / 8));
        return rc;
    }


    public int remainingBits() {
        return buffer.remaining() * 8 - position;
    }
}
