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
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.coremedia.iso.gui.hex.JHexEditor;
import com.googlecode.mp4parser.util.Path;
import org.jdesktop.application.Action;
import org.jdesktop.application.Resource;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The main UI class for the ISO viewer. Contains all other UI components.
 */
public class IsoViewerPanel extends JPanel {


    private JTree tree;
    private JList trackList;
    private JPanel detailPanel;
    private JSplitPane rawDataSplitPane;

    @Resource
    private String trackViewDetailPaneHeader = "T %s";
    @Resource
    private String tabbedPaneHeaderTrack = "T&S";
    @Resource
    private String tabbedPaneHeaderBox = "BS";

    private Object details;


    public IsoViewerPanel() {

    }


    public void createLayout() {
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
        jTabbedPane.add(tabbedPaneHeaderBox, new JScrollPane(tree));
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

        jTabbedPane.add(tabbedPaneHeaderTrack, trackList);
        splitPane.setLeftComponent(jTabbedPane);
        splitPane.setRightComponent(rawDataSplitPane);
        splitPane.setResizeWeight(0.6);

        this.setLayout(new BorderLayout());
        this.add(splitPane, BorderLayout.CENTER);


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
        detailPane.add(new JLabel(String.format(trackViewDetailPaneHeader, tb.getTrackHeaderBox().getTrackId())), BorderLayout.PAGE_START);
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

    JFileChooser fileChooser = new JFileChooser();


    @Action(name = "open-iso-file")
    public void open() {

        int state = fileChooser.showOpenDialog(IsoViewerPanel.this);
        if (state == JFileChooser.APPROVE_OPTION) {
            try {
                IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(fileChooser.getSelectedFile()));
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
                Enumeration<TreePath> treePathEnumeration = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
                List<String> openPath = new LinkedList<String>();
                Path oldMp4Path = null;
                if (treePathEnumeration != null) {
                    oldMp4Path = new Path((IsoFile) tree.getModel().getRoot());

                    while (treePathEnumeration.hasMoreElements()) {
                        TreePath treePath = treePathEnumeration.nextElement();
                        openPath.add(oldMp4Path.createPath((Box) treePath.getLastPathComponent()));
                    }
                }

                tree.setModel(new IsoFileTreeModel(isoFile));
                tree.revalidate();
                Path nuMp4Path = new Path(isoFile);
                if (!openPath.isEmpty()) {

                    for (String s : openPath) {
                        Box expanded = nuMp4Path.getPath(s);
                        List path = new LinkedList();
                        while (expanded != null) {
                            path.add(expanded);
                            expanded = expanded.getParent();
                        }
                        if (path.size() > 0) {
                            Collections.reverse(path);
                            TreePath tp = new TreePath(path.toArray());
                            tree.expandPath(tp);
                        }
                    }
                }


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
                if (details instanceof Box && oldMp4Path != null) {
                    String path = oldMp4Path.createPath((Box) details);
                    Box nuDetail = nuMp4Path.getPath(path);
                    if (nuDetail != null) {
                        showDetails(nuDetail);
                    } else {
                        showDetails(isoFile);
                    }
                } else {
                    showDetails(isoFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void showDetails(Object object) {
        details = object;
        Cursor oldCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JComponent detailPane = new JPanel();
            if (object instanceof com.coremedia.iso.boxes.Box) {
                detailPane = new GenericBoxPane((com.coremedia.iso.boxes.Box) object);
            }
            detailPanel.removeAll();
            detailPanel.add(detailPane, BorderLayout.CENTER);
            detailPanel.revalidate();
            IsoBufferWrapper displayMe;
            if (object instanceof com.coremedia.iso.boxes.Box) {
                displayMe = ((Box) object).getIsoFile().getOriginalIso().getSegment(((Box) object).getOffset(), ((Box) object).getSize());
            } else {
                displayMe = new IsoBufferWrapperImpl(new byte[]{});
            }
            rawDataSplitPane.setBottomComponent(new JHexEditor(displayMe));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            setCursor(oldCursor);

        }
    }
}
