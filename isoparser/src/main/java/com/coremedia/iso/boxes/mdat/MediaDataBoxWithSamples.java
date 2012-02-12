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

package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * This box contains the media data. In video tracks, this box would contain video frames. A presentation may
 * contain zero or more Media Data Boxes. The actual media data follows the type field; its structure is described
 * by the metadata (see {@link com.coremedia.iso.boxes.SampleTableBox}).<br>
 * In large presentations, it may be desirable to have more data in this box than a 32-bit size would permit. In this
 * case, the large variant of the size field is used.<br>
 * There may be any number of these boxes in the file (including zero, if all the media data is in other files). The
 * metadata refers to media data by its absolute offset within the file (see {@link com.coremedia.iso.boxes.StaticChunkOffsetBox});
 * so Media Data Box headers and free space may easily be skipped, and files without any box structure may
 * also be referenced and used.
 */
public class MediaDataBoxWithSamples implements Box {
    ContainerBox parent;
    LinkedList<Sample> samples = new LinkedList<Sample>();


    public ContainerBox getParent() {
        return parent;
    }

    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }

    public long getSize() {
        long size = getContentSize();
        if (isSmall(size)) {
            return 8 + size;
        } else {
            return 16 + size;
        }
    }

    public String getType() {
        return "mdat";
    }

    public byte[] getUserType() {
        return new byte[0];
    }

    public void addSample(FileChannelSampleImpl s) {
        Sample last = samples.peekLast();
        if (s.getClass().isInstance(last)) {
            FileChannelSampleImpl l = (FileChannelSampleImpl) last;
            if ((l.fileChannel == s.fileChannel) && (l.offset + l.size == s.offset)) {
                l.size += s.size;
            } else {
                samples.add(s);
            }
        } else {
            samples.add(s);
        }
    }

    public void addSample(ByteArraySampleImpl s) {
        Sample last = samples.peekLast();
        if (s.getClass().isInstance(last)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(((ByteArraySampleImpl) last).data);
                baos.write(s.data);
                ((ByteArraySampleImpl) last).data = baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Should not happen. Even though ...", e);
            }
        }
    }

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        long size = getContentSize();
        if (isSmall(size)) {
            ByteBuffer bb = ByteBuffer.allocate(8);
            IsoTypeWriter.writeUInt32(bb, size);
            bb.put(IsoFile.fourCCtoBytes("mdat"));
        } else {
            ByteBuffer bb = ByteBuffer.allocate(16);
            IsoTypeWriter.writeUInt32(bb, 1);
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            IsoTypeWriter.writeUInt64(bb, size);
        }
        for (Sample sample : samples) {
            if (sample instanceof ByteArraySampleImpl) {
                writableByteChannel.write (ByteBuffer.wrap(((ByteArraySampleImpl) sample).data));
            } else if (sample instanceof FileChannelSampleImpl) {
                ((FileChannelSampleImpl) sample).fileChannel.transferTo(
                        ((FileChannelSampleImpl) sample).offset,
                        ((FileChannelSampleImpl) sample).size,
                        writableByteChannel);
            }
        }
    }

    public void parse(ReadableByteChannel inFC, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {

    }

    private boolean isSmall(long size) {
        return ((size + 8) < 4294967296L);


    }

    public MediaDataBoxWithSamples() {

    }


    protected long getContentSize() {
        long size = 0;
        for (Sample sample : samples) {
            if (sample instanceof ByteArraySampleImpl) {
                size += ((ByteArraySampleImpl) sample).data.length;
            } else if (sample instanceof FileChannelSampleImpl) {
                size += ((FileChannelSampleImpl) sample).size;
            }
        }
        return size;
    }
}