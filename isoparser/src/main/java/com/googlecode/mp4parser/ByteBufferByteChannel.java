package com.googlecode.mp4parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Creates a <code>ReadableByteChannel</code> that is backed by a <code>ByteBuffer</code>.
 */
public class ByteBufferByteChannel implements ByteChannel {
    ByteBuffer src;

    public ByteBufferByteChannel(ByteBuffer src) {
        this.src = src;
    }

    public int read(ByteBuffer dst) {
        byte[] b = dst.array();
        int r = dst.remaining();
        src.get(b, dst.position(), r);
        return r;
    }

    public boolean isOpen() {
        return true;
    }

    public void close() throws IOException {
    }

    public int write(ByteBuffer src) throws IOException {
        int r = src.remaining();
        src.put(src);
        return r;
    }
}
