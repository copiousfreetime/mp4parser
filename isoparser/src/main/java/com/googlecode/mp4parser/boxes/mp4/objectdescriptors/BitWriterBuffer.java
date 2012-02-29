package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.nio.ByteBuffer;

public class BitWriterBuffer {

    private ByteBuffer buffer;
    private int bitsLeft = 8;
    private int pos = 0;

    public BitWriterBuffer(ByteBuffer buffer) {
        this.buffer = buffer;

    }

    public void writeBits(int i, int numBits) {
        if (numBits <= bitsLeft) {
            int current = IsoTypeReader.byte2int(buffer.get(pos));
            current += i << (bitsLeft - numBits);
            buffer.put(pos, IsoTypeWriter.int2byte(current));
            bitsLeft -= numBits;
        } else {
            int bitsSecondWrite = numBits - bitsLeft;
            int a = i >> bitsSecondWrite;
            int b = i - (bitsSecondWrite << a);
            writeBits(a, bitsLeft);
            writeBits(b, bitsSecondWrite);
        }
        if (bitsLeft == 0) {
            pos++;
            bitsLeft = 8;
        }
    }


}
