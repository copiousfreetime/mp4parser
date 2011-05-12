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

import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.TrackMetaDataContainer;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Sample;
import com.coremedia.iso.mdta.Track;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * Shows the contents of a box as hex dump.
 */
public class HexDumpComponent extends JComponent {
    private String[] lines;

    public HexDumpComponent() {
        Font font = new Font("Courier New", Font.PLAIN, 12);
        setFont(font);
    }

    public void setData(Object o, RandomAccessFile raf) throws IOException {
        byte[] bytes;
        if (o instanceof AbstractBox) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream((int) ((AbstractBox) o).getSize());

            ((AbstractBox) o).getBox(new IsoOutputStream(new FilterOutputStream(baos) {
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
        } else if (o instanceof Track) {
            Track<?> track = (Track<?>) o;
            bytes = new byte[0];
        } else if (o instanceof Chunk) {
            Chunk<?> chunk = (Chunk<?>) o;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IsoOutputStream isoOutputStream = new IsoOutputStream(byteArrayOutputStream);
            List<? extends Sample<? extends TrackMetaDataContainer>> samples = chunk.getSamples();
            for (Sample<?> sample : samples) {
                sample.getContent(isoOutputStream);
            }
            isoOutputStream.close();
            bytes = byteArrayOutputStream.toByteArray();
        } else if (o instanceof Sample) {
            Sample<?> sample = (Sample<?>) o;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            sample.getContent(new IsoOutputStream(baos));
            bytes = baos.toByteArray();
        } else {
            bytes = new byte[0];
        }

        String text = makeTextRawDataDump(bytes);

        lines = text.split("\n");
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(lines[0]), lines.length * fm.getHeight()));


        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        if (lines != null) {
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int height = fontMetrics.getHeight();
            int baseline = fontMetrics.getAscent();

            g.setColor(getForeground());
            Rectangle clipBounds = g.getClipBounds();
            int start = clipBounds.y / height - 2;
            int end = start + clipBounds.height / height + 4;
            if (start < 0) {
                start = 0;
            }
            if (end > lines.length) {
                end = lines.length;
            }
            for (int i = start; i < end; i++) {
                g.drawString(lines[i], 0, baseline + i * height);
            }
        }
    }

    protected String makeTextRawDataDump(byte[] inArray) {
        int length = inArray.length;
        ByteArrayInputStream in = new ByteArrayInputStream(inArray);
      StringBuilder buffer = new StringBuilder();
        int rows = (length + 15) / 16;
        if (rows > 0xfff) {
            rows = 0x1000;
        }
        for (int i = 0; i < rows; i++) {
            if (i > 0) {
                buffer.append("\n");
            }
            buffer.append("0x");
            String hexString = Integer.toHexString(i);
            for (int k = 0; k < 3 - hexString.length(); k++) {
                buffer.append("0");
            }
            buffer.append(hexString);
            buffer.append("0");

            buffer.append(" ");

            int rowLength;
            if (i == rows - 1) {
                if (rows * 16 <= length) {
                    rowLength = 16;
                } else {
                    rowLength = length % 16;
                }
            } else {
                rowLength = 16;
            }
            byte[] row = new byte[rowLength];
            int read = 0;
            while (read < rowLength) {
                read += in.read(row, read, rowLength - read);
            }

            for (int j = 0; j < 16; j++) {
                int index = i * 16 + j;
                buffer.append(" ");
                if (index < length) {
                    buffer.append(Integer.toHexString((row[j] >> 4) & 0xf));
                    buffer.append(Integer.toHexString(row[j] & 0xf));
                } else {
                    buffer.append("  ");
                }
            }

            buffer.append("  ");

            for (int j = 0; j < 16; j++) {
                int index = i * 16 + j;
                if (index < length) {
                    char c = (char) row[j];
                    if (!Character.isISOControl(c) && getFont().canDisplay(c)) {
                        buffer.append(c);
                    } else {
                        buffer.append('.');
                    }
                } else {
                    buffer.append(" ");
                }
            }
        }

        if (rows * 16 < length) {
            buffer.append("\n\ndata truncated\nlast ");
            buffer.append(length - rows * 16);
            buffer.append(" bytes not shown");
        }
        return buffer.toString();

    }
}
