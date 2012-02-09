package com.coremedia.iso;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 */
public class FileChannelIsoBufferWrapperImpl extends AbstractIsoBufferWrapper {

    ReadableByteChannel file;

    public FileChannelIsoBufferWrapperImpl(ReadableByteChannel file) throws IOException {
        this.file = file;
    }




    public byte[] read(int byteCount) throws IOException {
        byte[] result = new byte[byteCount];
        this.read(result);
        return result;
    }


    public int read() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1);
        file.read(byteBuffer);
        byte b = byteBuffer.get(0);
        return b < 0 ? b + 256 : b;
    }

    public int read(byte[] b) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        return file.read(byteBuffer);
    }



}
