package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.mdat.Segment;

import java.nio.channels.FileChannel;
import java.util.List;


public interface FileChannelSegmentTrack {
    List<Segment> getSamples();
    FileChannel getFileChannel();
}
