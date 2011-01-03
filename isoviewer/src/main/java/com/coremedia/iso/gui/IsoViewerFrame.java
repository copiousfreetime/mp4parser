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
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.gui.hex.JHexEditor;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Sample;
import com.coremedia.iso.mdta.Track;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The main UI class for the ISO viewer. Contains all other UI components.
 */
public class IsoViewerFrame extends JFrame {
    private JTree tree;
    private JPanel detailPanel;
    private File file;
    private IsoFile isoFile;
    private JSplitPane rawDataSplitPane;
    private JMenuItem save = new JMenuItem("Save");


    public IsoViewerFrame() {
        super("CoreMedia ISO Base Media File Format Viewer");
        createMenu();
        createLayout();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    protected void createLayout() {
        tree = new JTree(new Object[0]);
        tree.setRootVisible(true);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                com.coremedia.iso.gui.IsoFileTreeModel.IsoFileTreeNode node = (com.coremedia.iso.gui.IsoFileTreeModel.IsoFileTreeNode) e.getPath().getLastPathComponent();
                showDetails(node.getObject());
            }
        });

        detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        rawDataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rawDataSplitPane.setBorder(null);
        rawDataSplitPane.setOneTouchExpandable(true);
        rawDataSplitPane.setTopComponent(new JScrollPane(detailPanel));
        rawDataSplitPane.setBottomComponent(new JHexEditor(new byte[0]));
        rawDataSplitPane.setResizeWeight(0.5);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setLeftComponent(new JScrollPane(tree));
        splitPane.setRightComponent(rawDataSplitPane);
        splitPane.setResizeWeight(0.6);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);

        setContentPane(contentPane);
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
            this.isoFile = new IsoFile(new IsoBufferWrapper(file));
            long start = System.currentTimeMillis();
            final List<LogRecord> messages = new LinkedList<LogRecord>();
            Handler myTemperaryLogHandler = new Handler() {
                public void publish(LogRecord record) {
                    messages.add(record);
                }

                public void flush() {
                }

                public void close() throws SecurityException {
                }
            };
            Logger.getLogger("").addHandler(myTemperaryLogHandler);
            isoFile.parse();
            isoFile.parseMdats();
            IsoFileConvenienceHelper.switchToAutomaticChunkOffsetBox(isoFile);
            Logger.getAnonymousLogger().removeHandler(myTemperaryLogHandler);
            System.err.println("Parsing took " + ((System.currentTimeMillis() - start) / 1000) + "seconds.");
            tree.setModel(new IsoFileTreeModel(isoFile));
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
        }

        save.setEnabled(true);
        setTitle(file.getAbsolutePath());
    }

    public void showDetails(Object object) {
        Cursor oldCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JComponent detailPane = new DetailPaneFactory().createDetailPane(object);
            detailPanel.removeAll();
            detailPanel.add(detailPane, BorderLayout.CENTER);
            detailPanel.revalidate();
            byte[] bytes;
            if (object instanceof AbstractBox) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int) ((AbstractBox) object).getSize());

                ((AbstractBox) object).getBox(new IsoOutputStream(new FilterOutputStream(baos) {
                    int count = 0;

                    public void write(int b) throws IOException {
                        if (count < 10000) {
                            count++;
                            out.write(b);
                        }
                    }

                    public void write(byte[] b) throws IOException {
                        if (count < 10000) {
                            super.write(b);
                        }
                    }

                    public void write(byte[] b, int off, int len) throws IOException {
                        if (count < 10000) {
                            super.write(b, off, len);
                        }
                    }
                }));
                bytes = baos.toByteArray();
            } else if (object instanceof Track) {
                bytes = new byte[0];
            } else if (object instanceof Chunk) {
                Chunk chunk = (Chunk) object;
                List<Sample> s = chunk.getSamples();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (Sample sample : s) {
                    sample.getContent(new IsoOutputStream(baos));
                }
                bytes = baos.toByteArray();
                baos.close();
            } else if (object instanceof Sample) {
                Sample sample = (Sample) object;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                sample.getContent(new IsoOutputStream(baos));
                bytes = baos.toByteArray();
            } else {
                bytes = new byte[0];
            }
            rawDataSplitPane.setBottomComponent(new JHexEditor(bytes));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            setCursor(oldCursor);

        }
    }
}
