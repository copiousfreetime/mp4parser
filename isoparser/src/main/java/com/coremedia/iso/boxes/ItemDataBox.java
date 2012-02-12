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
    public void _parseDetails() {
        parseVersionAndFlags();
        data = content.slice();
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        bb.put(data);
    }
}
