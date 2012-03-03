package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.nio.ByteBuffer;

public class BitWriterBuffer {

    private ByteBuffer buffer;
    int position = 0;

    public BitWriterBuffer(ByteBuffer buffer) {
        this.buffer = buffer;

    }

    public void writeBits(int i, int numBits) {
        int left = 8 - position % 8;
        if (numBits <= left) {
            int current = (buffer.get(position / 8));
            current = current < 0 ? current + 256 : current;
            current += i << (left - numBits);
            buffer.put(position / 8, (byte) (current > 127 ? current - 256 : current));
            position += numBits;
        } else {
            int bitsSecondWrite = numBits - left;
            writeBits(i >> bitsSecondWrite, left);
            writeBits(i & (1 << bitsSecondWrite) - 1, bitsSecondWrite);
        }
        buffer.position((int) Math.ceil((double)position / 8));
    }


}
