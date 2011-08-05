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

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.coremedia.iso.gui.hex.JHexEditor;

import javax.sound.midi.Track;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTreeUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The main UI class for the ISO viewer. Contains all other UI components.
 */
public class IsoViewerFrame extends JFrame {
    private Class<? extends IsoFile> isoFileClazz;
    private JTree tree;
    private JList trackList;
    private JPanel detailPanel;
    private File file;
    private IsoFile isoFile;
    private JSplitPane rawDataSplitPane;
    private JMenuItem save = new JMenuItem("Save");


    public IsoViewerFrame(Class<? extends IsoFile> isoFileClazz) {
        super("MP4 Viewer");
        createMenu();
        createLayout();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.isoFileClazz = isoFileClazz;
    }

    protected void createLayout() {
        tree = new JTree(new Object[0]);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                com.coremedia.iso.boxes.Box node = (com.coremedia.iso.boxes.Box) e.getPath().getLastPathComponent();
                showDetails(node);
            }
        });
        tree.setCellRenderer(new BoxNodeRenderer());

        detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        rawDataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rawDataSplitPane.setBorder(null);
        rawDataSplitPane.setOneTouchExpandable(true);
        JScrollPane scrollPane = new JScrollPane(detailPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rawDataSplitPane.setTopComponent(scrollPane);
        rawDataSplitPane.setBottomComponent(new JHexEditor(new IsoBufferWrapperImpl(new byte[0])));
        rawDataSplitPane.setResizeWeight(0.8);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.add("Box Structure", new JScrollPane(tree));
        jTabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int index = ((JTabbedPane) e.getSource()).getSelectedIndex();
                if (index == 0) {
                    Object selected = tree.getSelectionPath().getLastPathComponent();
                    if (selected != null) {
                        showDetails(selected);
                    }
                } else if (index == 1) {
                    if (trackList.getSelectedValue() != null) {
                        showSamples((TrackBox) trackList.getSelectedValue());
                    }
                }
            }
        });

        trackList = new JList();
        trackList.setCellRenderer(new TrackListRenderer());
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showSamples((TrackBox) ((JList) e.getSource()).getSelectedValue());
            }
        });

        jTabbedPane.add("Tracks & Samples", trackList);
        splitPane.setLeftComponent(jTabbedPane);
        splitPane.setRightComponent(rawDataSplitPane);
        splitPane.setResizeWeight(0.6);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    private void showSamples(TrackBox tb) {
        detailPanel.removeAll();
        JComponent detailPane = new JPanel(new BorderLayout());

        JList jlist = new JList();
        jlist.setCellRenderer(new SampleListRenderer());
        jlist.setModel(new SampleListModel(new SampleList(tb)));
        jlist.setLayoutOrientation(JList.VERTICAL);
        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlist.setPrototypeCellValue(new SampleListModel.Entry(new IsoBufferWrapperImpl(new byte[1000]), 1000000000));
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.getViewport().add(jlist);
        detailPane.add(new JLabel("Track " + tb.getTrackHeaderBox().getTrackId()), BorderLayout.PAGE_START);
        detailPane.add(jScrollPane, BorderLayout.CENTER);
        jlist.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    rawDataSplitPane.setBottomComponent(new JHexEditor(((SampleListModel.Entry) ((JList) e.getSource()).getSelectedValue()).sample));
                    ;
                }
            }
        });
        detailPanel.add(detailPane);
        detailPanel.revalidate();
    }

    protected void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menuBar.add(menu);


        save.setEnabled(false);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    File tmpFile = new File(file.getParentFile(), file.getName() + ".tmp");
                    OutputStream os = new FileOutputStream(tmpFile);
                    isoFile.getBox(new IsoOutputStream(os));
                    os.close();
                    file.delete();
                    tmpFile.renameTo(file);
                    tmpFile.delete();
                    tmpFile.deleteOnExit();
                    open(file);
                } catch (IOException e1) {
                    JDialog dialog = new JDialog(IsoViewerFrame.this, "Error", true);
                    JLabel jLabel = new JLabel();
                    jLabel.setText(e1.getMessage());
                    dialog.add(jLabel);
                    dialog.setVisible(true);
                    e1.printStackTrace();
                }
            }
        });

        JMenuItem open = new JMenuItem("Open...");
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (file != null) {
                    fileChooser.setCurrentDirectory(file.getParentFile());
                }
                int state = fileChooser.showOpenDialog(IsoViewerFrame.this);
                if (state == JFileChooser.APPROVE_OPTION) {
                    open(fileChooser.getSelectedFile());

                }
            }
        });


        menu.add(open);
        menu.add(save);

        setJMenuBar(menuBar);
    }

    public void open(File file) {
        this.file = file;
        try {
            final Constructor<? extends IsoFile> constructor = isoFileClazz.getConstructor(IsoBufferWrapper.class);
            this.isoFile = constructor.newInstance(new IsoBufferWrapperImpl(file));
            long start = System.nanoTime();
            final List<LogRecord> messages = new LinkedList<LogRecord>();
            Handler myTemperaryLogHandler = new Handler() {
                @Override
                public void publish(LogRecord record) {
                    messages.add(record);
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }
            };
            Logger.getLogger("").addHandler(myTemperaryLogHandler);
            isoFile.parse();

            Logger.getAnonymousLogger().removeHandler(myTemperaryLogHandler);
            System.err.println("Parsing took " + ((System.nanoTime() - start) / 1000000d) + "ms.");
            tree.setModel(new IsoFileTreeModel(isoFile));
            trackList.setModel(new TrackListModel(isoFile));
            if (!messages.isEmpty()) {
                String message = "";
                for (LogRecord logRecord : messages) {
                    message += logRecord.getMessage() + "\n";
                }
                JOptionPane.showMessageDialog(this,
                        message,
                        "Parser Messages",
                        JOptionPane.WARNING_MESSAGE);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        save.setEnabled(true);
        setTitle(file.getAbsolutePath());
    }

    public void showDetails(Object object) {
        Cursor oldCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JComponent detailPane = new JPanel();
            if (object instanceof com.coremedia.iso.boxes.Box) {
                detailPane = new GenericBoxPane((AbstractBox) object);
            }
            detailPanel.removeAll();
            detailPanel.add(detailPane, BorderLayout.CENTER);
            detailPanel.revalidate();
            byte[] bytes;
            if (object instanceof com.coremedia.iso.boxes.Box) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int) ((AbstractBox) object).getSize());

                ((AbstractBox) object).getBox(new IsoOutputStream(new FilterOutputStream(baos) {
                    int count = 0;

                    @Override
                    public void write(int b) throws IOException {
                        if (count < 10000) {
                            count++;
                            out.write(b);
                        }
                    }

                    @Override
                    public void write(byte[] b) throws IOException {
                        if (count < 10000) {
                            super.write(b);
                        }
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        if (count < 10000) {
                            super.write(b, off, len);
                        }
                    }
                }));
                bytes = baos.toByteArray();
            } else {
                bytes = new byte[0];
            }
            rawDataSplitPane.setBottomComponent(new JHexEditor(new IsoBufferWrapperImpl(bytes)));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            setCursor(oldCursor);

        }
    }
}
