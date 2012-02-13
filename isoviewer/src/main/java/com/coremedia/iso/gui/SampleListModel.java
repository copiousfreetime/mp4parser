package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.mdat.Sample;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.util.List;

/**
 *
 */
public class SampleListModel extends AbstractListModel {
    List<? extends Sample> list;

    public SampleListModel(List<? extends Sample> list) {
        this.list = list;
    }

    public int getSize() {
        return list.size();
    }

    public ByteBuffer getElementAt(int index) {
        return ByteBuffer.wrap(list.get(index).getBytes());

    }

}
