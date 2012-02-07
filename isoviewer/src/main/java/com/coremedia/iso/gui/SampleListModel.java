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

    public SampleListModel(SampleList list) {
        this.list = list;
    }

    public int getSize() {
        return list.size();
    }

    public Object getElementAt(int index) {
        return new Entry(list.get(index), list.getOffset(index));
    }

    public static class Entry {
        public Entry(IsoBufferWrapper sample, long offset) {
            this.sample = sample;
            this.offset = offset;
        }

        IsoBufferWrapper sample;
        long offset;
    }
}
