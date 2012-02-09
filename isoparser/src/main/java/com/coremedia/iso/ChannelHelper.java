package com.coremedia.iso;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/9/12
 * Time: 9:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChannelHelper {
    public static ByteBuffer readFully(final ReadableByteChannel channel, long size)
            throws IOException {
        assert size < Integer.MAX_VALUE;
        ByteBuffer buf = ByteBuffer.allocateDirect((int) size);
        readFully(channel, buf, buf.remaining());
        return buf;
    }



    public static void readFully(final ReadableByteChannel channel, final ByteBuffer buf)
            throws IOException {
        readFully(channel, buf, buf.remaining());
    }

    public static int readFully(final ReadableByteChannel channel, final ByteBuffer buf, final int length)
            throws IOException {
        int n, count = 0;
        while (-1 != (n = channel.read(buf))) {
            count += n;
            if (count == length) {
                break;
            }
        }
        return count;
    }


    public static void writeFully(final WritableByteChannel channel, final ByteBuffer buf)
            throws IOException {
        do {
            int written = channel.write(buf);
            if (written < 0) {
                throw new EOFException();
            }
        } while (buf.hasRemaining());
    }



    public static void close(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException e) {
            // nop
        }

    }



}