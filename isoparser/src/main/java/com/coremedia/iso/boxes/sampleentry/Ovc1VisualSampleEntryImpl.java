package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static com.coremedia.iso.boxes.CastUtils.l2i;


public class Ovc1VisualSampleEntryImpl extends SampleEntry {
    private byte[] vc1Content;
    public static final String TYPE = "ovc1";


    @Override
    protected long getContentSize() {
        long size = 8;

        for (Box box : boxes) {
            size += box.getSize();
        }
        size += vc1Content.length;
        return size;
    }

    @Override
    public void _parseDetails() {
        super._parseDetails();
        vc1Content = new byte[content.remaining()];
        content.get(vc1Content);
        content = null;
    }

    @Override
    protected void getContent(WritableByteChannel os) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(l2i(getContentSize()));
        bb.put(new byte[6]);
        IsoTypeWriter.writeUInt16(bb, getDataReferenceIndex());
        bb.put(vc1Content);
        os.write(bb);
    }

    @Override
    public void parse(ReadableByteChannel in, ByteBuffer header, long size, BoxParser boxParser) throws IOException {
        content = ChannelHelper.readFully(in, size);
    }

    protected Ovc1VisualSampleEntryImpl() {
        super(TYPE);
    }

}
