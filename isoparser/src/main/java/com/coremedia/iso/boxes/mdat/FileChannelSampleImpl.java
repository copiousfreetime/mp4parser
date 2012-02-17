package com.coremedia.iso.boxes.mdat;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    public ByteBuffer getBytes() {
        ByteBuffer bb = ByteBuffer.allocate(l2i(size));
        try {
            fileChannel.position(offset);
            fileChannel.read(bb);
            return bb;
        } catch (IOException e) {
            throw new RuntimeException("WTF? ask Sebastian. 98628743295", e);
        }
    }
}
