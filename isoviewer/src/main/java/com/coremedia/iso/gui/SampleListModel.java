package com.coremedia.iso.gui;

import javax.swing.AbstractListModel;
import java.nio.ByteBuffer;
import java.util.List;

/**
 *
 */
public class SampleListModel extends AbstractListModel {
    List<ByteBuffer> list;

    public SampleListModel(List<ByteBuffer> list) {
        this.list = list;
    }

    public int getSize() {
        return list.size();
    }

    public ByteBuffer getElementAt(int index) {
        return list.get(index);

    }

}
