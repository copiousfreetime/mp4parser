package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.boxes.TrackBox;

import java.nio.channels.FileChannel;


public class SegmentSampleList extends SampleList<FileChannelSampleImpl> {
    FileChannel source;

    public SegmentSampleList(TrackBox trackBox, FileChannel source) {
        super(trackBox);
        this.source = source;
    }

    @Override
    public FileChannelSampleImpl get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);
        long size = offsets2Sizes.get(offset);
        return new FileChannelSampleImpl(size, offset, source);

    }


}
