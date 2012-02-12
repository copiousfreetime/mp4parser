package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.coremedia.iso.boxes.CastUtils.l2i;

/**
 * This is no very fast implementation but it does its job especially in the isoviewer.
 */
public class ByteArraySampleList extends SampleList<byte[]> {
    IsoFile isoFile;

    public ByteArraySampleList(TrackBox trackBox) {
        super(trackBox);
        this.isoFile = trackBox.getIsoFile();
    }


    @Override
    public byte[] get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);

        int sampleSize = l2i(offsets2Sizes.get(offset));
        List<Box> boxes = isoFile.getBoxes();
        long currentOffset = 0;
        for (Box b : boxes) {
            long currentSize = b.getSize();
            if (currentOffset < offset && (currentOffset + currentSize < offset)) {
                if ("mdat".equals(b.getType()) && b instanceof MediaDataBox) {
                    long contentOffset = currentOffset + ((MediaDataBox) b).getHeader().capacity();
                    byte[] sampleBytes = new byte[sampleSize];
                    ((MediaDataBox) b).getContent().get(sampleBytes, l2i(offset - contentOffset), sampleSize);
                    return sampleBytes;
                } else {
                    throw new RuntimeException("Sample need to be in mdats and mdats need to be instanceof MediaDataBox");
                }
            }
            currentOffset += currentSize;
        }

        throw new RuntimeException("Could not find an mdat at offset: " + offset);
    }
}
