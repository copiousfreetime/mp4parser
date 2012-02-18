package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackBox;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static com.coremedia.iso.boxes.CastUtils.l2i;

/**
 * This is no very fast implementation but it does its job especially in the isoviewer.
 */
public class ByteArraySampleList extends SampleList<ByteArraySampleImpl> {
    IsoFile isoFile;
    HashMap<MediaDataBox, Long> mdatStartCache = new HashMap<MediaDataBox, Long>();
    HashMap<MediaDataBox, Long> mdatEndCache = new HashMap<MediaDataBox, Long>();


    public ByteArraySampleList(TrackBox trackBox) {
        super(trackBox);
        this.isoFile = trackBox.getIsoFile();

        long currentOffset = 0;
        for (Box b : isoFile.getBoxes()) {
            long currentSize = b.getSize();
            if ("mdat".equals(b.getType())) {
                if (b instanceof MediaDataBox) {
                    long contentOffset = currentOffset + ((MediaDataBox) b).getHeader().limit();
                    mdatStartCache.put((MediaDataBox) b, contentOffset);
                    mdatEndCache.put((MediaDataBox) b, contentOffset + currentSize);
                } else {
                    throw new RuntimeException("Sample need to be in mdats and mdats need to be instanceof MediaDataBox");
                }
            }
            currentOffset += currentSize;
        }


    }


    @Override
    public ByteArraySampleImpl get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);
        int sampleSize = l2i(offsets2Sizes.get(offset));
        for (MediaDataBox mediaDataBox : mdatStartCache.keySet()) {
            long start = mdatStartCache.get(mediaDataBox);
            long end = mdatEndCache.get(mediaDataBox);
            if ((start <= offset) && (offset + sampleSize <= end)) {
                ByteBuffer bb = mediaDataBox.getContent();
                bb.position(l2i(offset - start));
                ByteBuffer sample = bb.slice();
                sample.limit(sampleSize);
                return new ByteArraySampleImpl(sample);
            }
        }
        
        throw new RuntimeException("The sample with offset " + offset + " and size " + sampleSize + " is NOT located within an mdat");
    }
}
