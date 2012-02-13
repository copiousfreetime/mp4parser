package com.coremedia.iso.boxes.mdat;

import java.nio.channels.FileChannel;

import static com.coremedia.iso.boxes.CastUtils.l2i;

public class FileChannelSampleImpl implements Sample {
    public FileChannel fileChannel;
    public long offset;
    public long size;

    public FileChannelSampleImpl(long offset, long size, FileChannel fileChannel) {
        this.size = size;
        this.offset = offset;
        this.fileChannel = fileChannel;
    }

    public int getSize() {
        return l2i(size);
    }
}
