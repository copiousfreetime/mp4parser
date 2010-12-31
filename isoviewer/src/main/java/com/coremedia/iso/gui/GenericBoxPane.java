/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.gui;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.FullBox;
import com.coremedia.iso.gui.transferhelper.StringTransferValue;
import com.coremedia.iso.gui.transferhelper.TransferHelperFactory;
import com.coremedia.iso.gui.transferhelper.TransferValue;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Detailed view of a Box.
 */
public class GenericBoxPane extends JPanel {
    private AbstractBox box;
    GridBagLayout gridBagLayout;
    GridBagConstraints gridBagConstraints;

    private static final Collection<String> skipList = Arrays.asList(
            "class",
            "boxes",
            "deadBytes",
            "type",
            "userType",
            "size",
            "displayName",
            "contentSize",
            "offset",
            "header",
            "version",
            "flags",
            "isoFile",
            "parent",
            "data",
            "omaDrmData",
            "content",
            "tracks",
            "sampleSizeAtIndex",
            "numOfBytesToFirstChild");

    public GenericBoxPane(AbstractBox box) {
        this.box = box;
        gridBagLayout = new GridBagLayout();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(3, 3, 0, 0);
        this.setLayout(gridBagLayout);
        addHeader();
        addProperties();
    }


    private void add(String name, JComponent view) {
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = .01;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        JLabel nameLabel = new JLabel(name);
        gridBagLayout.setConstraints(nameLabel, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagLayout.setConstraints(view, gridBagConstraints);
        this.add(nameLabel);
        this.add(view);
        gridBagConstraints.gridy++;
    }

    protected void addHeader() {
        JLabel displayName = new JLabel();
        displayName.setText(box.getDisplayName());
        Font curFont = displayName.getFont();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        displayName.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 20));
        gridBagLayout.setConstraints(displayName, gridBagConstraints);
        this.add(displayName);
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridy++;

        try {
            add("type", new NonEditableJTextField(new String(box.getType(), "ISO-8859-1")));
        } catch (UnsupportedEncodingException e) {
            add("type", new NonEditableJTextField(new String(box.getType())));
        }
        add("size", new NonEditableJTextField(String.valueOf(box.getSize())));

        if (box.getDeadBytes().length > 0) {

            StringBuffer valueBuffer = new StringBuffer();
            valueBuffer.append("[");
            IsoBufferWrapper ibw = new IsoBufferWrapper(box.getDeadBytes());
            long length = ibw.size();


            boolean trucated = false;

            if (length > 1000) {
                trucated = true;
                length = 1000;
            }

            for (int j = 0; j < length; j++) {
                if (j > 0) {
                    valueBuffer.append(", ");
                }
                byte item = ibw.read();
                valueBuffer.append(item);
            }
            if (trucated) {
                valueBuffer.append(", ...");
            }
            valueBuffer.append("]");
            add("deadBytes", new NonEditableJTextField(valueBuffer.toString()));
        }
        if (box instanceof FullBox) {
            FullBox fullBox = (FullBox) box;
            add("version", new NonEditableJTextField(String.valueOf(fullBox.getVersion())));
            add("flags", new NonEditableJTextField(Integer.toHexString(fullBox.getFlags())));
        }
        gridBagConstraints.gridwidth = 2;
        gridBagLayout.setConstraints(new JSeparator(), gridBagConstraints);
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridy++;
    }

    protected void addProperties() {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(box.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            boolean editable = false;
            final List<TransferValue> editors = new LinkedList<TransferValue>();
            JButton apply = new JButton("Apply changes");
            apply.setEnabled(false);
            apply.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (TransferValue editor : editors) {
                        editor.go();
                    }
                    Container c = GenericBoxPane.this.getParent();
                    while (!(c instanceof IsoViewerFrame)) {
                        c = c.getParent();
                    }
                    ((IsoViewerFrame) c).showDetails(box);
                }
            });
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String name = propertyDescriptor.getName();
                if (!skipList.contains(name) &&
                        propertyDescriptor.getReadMethod() != null &&
                        !AbstractBox.class.isAssignableFrom(propertyDescriptor.getReadMethod().getReturnType())) {
                    Object value = propertyDescriptor.getReadMethod().invoke(box, (Object[]) null);
                    if (value == null) {
                        add(name, new NonEditableJTextField(""));
                    } else if (Number.class.isAssignableFrom(value.getClass())) {
                        if (propertyDescriptor.getWriteMethod() != null) {
                            JFormattedTextField jftf = new JFormattedTextField(NumberFormat.getNumberInstance());
                            jftf.setValue(value);
                            jftf.getDocument().addDocumentListener(new ActivateOnChange(apply));
                            editors.add(TransferHelperFactory.getNumberTransferHelper(value.getClass(), box, propertyDescriptor.getWriteMethod(), jftf));
                            add(name, jftf);
                            editable = true;
                        } else {
                            add(name, new NonEditableJTextField(value.toString()));
                        }
                    } else if (value.getClass().equals(String.class)) {
                        if (propertyDescriptor.getWriteMethod() != null) {
                            JTextField jtf = new JTextField(value.toString());
                            jtf.getDocument().addDocumentListener(new ActivateOnChange(apply));
                            editors.add(new StringTransferValue(jtf, box, propertyDescriptor.getWriteMethod()));
                            add(name, jtf);
                            editable = true;
                        } else {
                            add(name, new NonEditableJTextField(value.toString()));
                        }
                    } else if (value.getClass().isArray()) {
                        StringBuffer valueBuffer = new StringBuffer();
                        valueBuffer.append("[");
                        int length = Array.getLength(value);

                        boolean trucated = false;

                        if (length > 1000) {
                            trucated = true;
                            length = 1000;
                        }
                        for (int j = 0; j < length; j++) {
                            if (j > 0) {
                                valueBuffer.append(", ");
                            }
                            Object item = Array.get(value, j);
                            valueBuffer.append(item != null ? item.toString() : "");
                        }
                        if (trucated) {
                            valueBuffer.append(", ...");
                        }
                        valueBuffer.append("]");
                        value = valueBuffer.toString();
                        add(name, new NonEditableJTextField(value.toString()));
                    }


                }
            }
            if (editable) {
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy++;
                gridBagConstraints.fill = GridBagConstraints.NONE;
                gridBagConstraints.anchor = GridBagConstraints.EAST;
                gridBagLayout.setConstraints(apply, gridBagConstraints);
                add(apply);
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    class ActivateOnChange implements DocumentListener {
        JComponent toBeActivated;

        ActivateOnChange(JComponent toBeActivated) {
            this.toBeActivated = toBeActivated;
        }

        public void insertUpdate(DocumentEvent e) {
            toBeActivated.setEnabled(true);
        }

        public void removeUpdate(DocumentEvent e) {
            toBeActivated.setEnabled(true);
        }

        public void changedUpdate(DocumentEvent e) {
            toBeActivated.setEnabled(true);
        }
    }


}
