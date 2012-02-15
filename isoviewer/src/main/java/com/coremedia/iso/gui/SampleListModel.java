package com.coremedia.iso.gui;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import sun.security.util.Resources_zh_CN;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.List;

/**
 *
 */
public class SampleListModel extends AbstractListModel {
    SampleList list;
    long trackId;
    String handlerType;
    int nalLengthSize;

    public SampleListModel(SampleList list, long trackId, String handlerType, int nalLengthSize) {
        this.list = list;
        this.trackId = trackId;
        this.handlerType = handlerType;
        this.nalLengthSize = nalLengthSize;
    }

    public int getSize() {
        return list.size();
    }

    public Object getElementAt(int index) {
        IsoBufferWrapper sample = list.get(index);
        long offset = list.getOffset(index);
        return new Entry(sample, offset, trackId, handlerType, nalLengthSize);
    }

    public static class Entry {
        public Entry(IsoBufferWrapper sample, long offset, long trackId, String handlerType, int nalLengthSize) {
            this.sample = sample;
            this.offset = offset;
            this.trackId = trackId;
            this.handlerType = handlerType;
            this.nalLengthSize = nalLengthSize;
        }

        IsoBufferWrapper sample;
        long offset;
        long trackId;
        String handlerType;
        int nalLengthSize;
    }
}
