package com.coremedia.iso.boxes.mdat;

/**
 *
 */
public class ByteArraySampleImpl implements Sample {
    public byte[] data;

    public ByteArraySampleImpl(byte[] data) {
        this.data = data;
    }

    public int getSize() {
        return data.length;
    }

    public byte[] getBytes() {
        return data;
    }
}
