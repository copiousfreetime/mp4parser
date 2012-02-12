package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

public class MpegSampleEntry extends SampleEntry implements ContainerBox {

    private BoxParser boxParser;

    public MpegSampleEntry(String type) {
        super(type);
    }

    @Override
    public void _parseDetails() {
        _parseReservedAndDataReferenceIndex();
        _parseChildBoxes();

    }

    @Override
    protected long getContentSize() {
        long contentSize = 8;
        for (Box boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public String toString() {
        return "MpegSampleEntry" + Arrays.asList(getBoxes());
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        _writeReservedAndDataReferenceIndex(bb);
        _writeChildBoxes(bb);
    }
}
