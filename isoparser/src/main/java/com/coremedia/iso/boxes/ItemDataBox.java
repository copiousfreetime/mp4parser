package com.coremedia.iso.boxes;

import com.coremedia.iso.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 */
public class ItemDataBox extends AbstractFullBox {
    ByteBuffer data;
    public static final String TYPE = "idat";

    public ItemDataBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    @Override
    protected long getContentSize() {
        return data.capacity();
    }

    @Override
    protected void getContent(WritableByteChannel os) throws IOException {
        os.write(data);
    }

    @Override
    public void parse(ReadableByteChannel in, long size, BoxParser boxParser) throws IOException {
        parseVersionAndFlags(in, size);
        data = ChannelHelper.readFully(in, size - 4);
    }
}
