package com.coremedia.iso.gui;

import com.coremedia.iso.IsoBufferWrapper;
import com.googlecode.mp4parser.h264.IsoSampleNALUnitReader;
import com.googlecode.mp4parser.h264.model.NALUnit;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 7/11/11
 * Time: 6:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        SampleListModel.Entry sampleEntry = (SampleListModel.Entry) value;
        IsoSampleNALUnitReader isoSampleNALUnitReader;
        value = "Sample " + (index + 1) + "@" + sampleEntry.offset  + " - " + sampleEntry.sample.size() + "bytes";
        if ("vide".equals(sampleEntry.handlerType)) {
            try {
                isoSampleNALUnitReader = new IsoSampleNALUnitReader(sampleEntry.sample, sampleEntry.nalLengthSize);
                ArrayList<NALUnit> nals = new ArrayList<NALUnit>();

                do {
                    IsoBufferWrapper isoBufferWrapper = isoSampleNALUnitReader.nextNALUnit();
                    if (isoBufferWrapper == null) break;
                    nals.add(NALUnit.read(isoBufferWrapper));
                } while (true);

               value = "Sample " + (index + 1) + "@" + sampleEntry.offset  + " - " + sampleEntry.sample.size() + "bytes " + nals;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}
