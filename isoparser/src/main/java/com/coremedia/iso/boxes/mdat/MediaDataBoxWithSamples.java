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

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
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
    static int index = 0;


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

    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        long size = getContentSize();
        if (isSmall(size)) {
            ByteBuffer bb = ByteBuffer.allocate(8);
            IsoTypeWriter.writeUInt32(bb, 8 + size);
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            bb.rewind();
            writableByteChannel.write(bb);
        } else {
            ByteBuffer bb = ByteBuffer.allocate(16);
            IsoTypeWriter.writeUInt32(bb, 1);
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            IsoTypeWriter.writeUInt64(bb, 16 + size);
            bb.rewind();
            writableByteChannel.write(bb);
        }

        ArrayList<ByteBuffer> nuSamples = new ArrayList<ByteBuffer>(samples.size());
        for (Sample sample : samples) {
            nuSamples.add(sample.getBytes());
        }
        System.err.println("Reanable gathering bytechannel !");
        System.err.println("Reanable gathering bytechannel !");
        System.err.println("Reanable gathering bytechannel !");
       /* if (writableByteChannel instanceof GatheringByteChannel) {
            int STEPSIZE = 1024;
            for (int i = 0; i < Math.ceil((double) nuSamples.size() / STEPSIZE); i++) {
                List<ByteBuffer> sublist = nuSamples.subList(
                        i * STEPSIZE, // start
                        (i + 1) * STEPSIZE < nuSamples.size() ? (i + 1) * STEPSIZE : nuSamples.size()); // end
                ByteBuffer sampleArray[] = sublist.toArray(new ByteBuffer[sublist.size()]);
                do {
                    ((GatheringByteChannel) writableByteChannel).write(sampleArray);
                } while (sampleArray[sampleArray.length - 1].remaining() > 0);
            }

        } else */{
            for (ByteBuffer byteBuffer : nuSamples) {
                System.err.println(index++ + ": " + byteBuffer.limit());
                writableByteChannel.write(byteBuffer);
            }
        }
    }

    public void parse(ReadableByteChannel inFC, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {

    }

    private boolean isSmall(long size) {
        return ((size + 8) < 4294967296L);
    }

    public long getOffset() {
        Box b = this;
        long offset = 0;
        while (b.getParent() != null) {
            for (Box box : b.getParent().getBoxes()) {
                if (b == box) {
                    break;
                }
                offset += box.getSize();
            }
            b = b.getParent();
        }
        return offset;
    }


    public MediaDataBoxWithSamples() {

    }


    protected long getContentSize() {
        long size = 0;
        for (Sample sample : samples) {
            size += sample.getBytes().limit();
        }
        return size;
    }

    public void addSample(Sample sample) {
        samples.add(sample);
    }
}