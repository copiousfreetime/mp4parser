package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.TrackBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/7/12
 * Time: 6:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class SegmentSampleList extends SampleList<Segment> {


    public SegmentSampleList(TrackBox trackBox) {
        super(trackBox);
    }

    @Override
    public Segment get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);
        long size = offsets2Sizes.get(offset);
        return new Segment(offset, size);

    }


}
