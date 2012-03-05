package com.coremedia.iso.gui;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 7/11/11
 * Time: 6:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleListRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        value = "Sample " + (index + 1);
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}
