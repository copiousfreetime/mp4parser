package com.coremedia.iso.boxes.mdat;

import java.nio.ByteBuffer;

/**
 *
 */
public class ByteArraySampleImpl implements Sample {
    public ByteBuffer data;

    public ByteArraySampleImpl(byte[] data) {
        this.data = ByteBuffer.wrap(data);
    }

    public ByteArraySampleImpl(ByteBuffer data) {
        this.data = data;
    }

    public int getSize() {
        return data.limit();
    }

    public ByteBuffer getBytes() {
        return data;
    }
}
