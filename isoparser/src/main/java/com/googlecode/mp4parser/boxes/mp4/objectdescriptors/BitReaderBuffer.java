package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;

import java.nio.ByteBuffer;

public class BitReaderBuffer {

    private ByteBuffer buffer;
    byte posInCurrent = -1;
    int current;

    public BitReaderBuffer(ByteBuffer buffer) {
        this.buffer = buffer;

    }

    public int readBits(int i) {
        if (posInCurrent == -1) {
            current = IsoTypeReader.readUInt8(buffer);
            posInCurrent = 0;
        }
        if (i <= (8 - posInCurrent)) {
            int a = current & ((1 << (8 - posInCurrent)) - 1);
            posInCurrent += i;
            a = a >> (8 - posInCurrent);
            if (posInCurrent == 8) {
                posInCurrent = -1;
            }
            return a;
        } else {
            int firstBits = 8 - posInCurrent;
            int a = readBits(firstBits);
            i -= firstBits;
            a = a << i;
            a += readBits(i);
            return a;
        }
    }


    public int remainingBits() {
        return buffer.remaining() * 8 + (8 - posInCurrent);
    }
}
