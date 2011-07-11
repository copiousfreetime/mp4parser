package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.Box;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 7/11/11
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class BoxNodeRenderer extends JLabel implements TreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                                                  boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
        if (value instanceof Box) {
            setText(IsoFile.bytesToFourCC(((Box)value).getType()) + "[@" + ((Box)value).getOffset() + "]");
        } else {
           setText(value.toString());
        }
        return this;
    }
}

