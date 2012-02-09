package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.TrackBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/7/12
 * Time: 6:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class ByteArraySampleList extends SampleList<byte[]> {
    IsoBufferWrapper isoBufferWrapper;

    public ByteArraySampleList(TrackBox trackBox) {
        super(trackBox);
        isoBufferWrapper = trackBox.getIsoFile().getOriginalIso();
    }

    @Override
    public byte[] get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);
        try {
            long size = offsets2Sizes.get(offset);
            if (size > Integer.MAX_VALUE) {
                throw new RuntimeException("no sample can be bigger than Integer.MAX_VALUE");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IsoOutputStream isoOutputStream = new IsoOutputStream(baos);
            isoBufferWrapper.transferSegment(offset, size, isoOutputStream);
            isoOutputStream.close();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
