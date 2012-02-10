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

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for all sample entries.
 *
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.VisualSampleEntry
 * @see com.coremedia.iso.boxes.rtp.RtpHintSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.TextSampleEntry
 */
public abstract class SampleEntry extends AbstractBox implements ContainerBox {


    private int dataReferenceIndex;
    protected List<Box> boxes = new LinkedList<Box>();


    protected SampleEntry(String type) {
        super(type);
    }

    public void setType(String type) {
        this.type = IsoFile.fourCCtoBytes(type);
    }

    public int getDataReferenceIndex() {
        return dataReferenceIndex;
    }

    public void setDataReferenceIndex(int dataReferenceIndex) {
        this.dataReferenceIndex = dataReferenceIndex;
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = new LinkedList<Box>(boxes);
    }

    public void addBox(AbstractBox b) {
        boxes.add(b);
    }

    public boolean removeBox(Box b) {
        return boxes.remove(b);
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box boxe : boxes) { //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            if (clazz == boxe.getClass()) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof ContainerBox) {
                boxesToBeReturned.addAll(((ContainerBox) boxe).getBoxes(clazz, recursive));
            }
        }
        // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
        return boxesToBeReturned;
        //return (T[]) boxesToBeReturned.toArray();
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @Override
    public abstract void parse(ReadableByteChannel in, ByteBuffer header, long size, BoxParser boxParser) throws IOException;

    /**
     * Parses the 6 reserved byte and the data reference index.
     */
    @Override
    public void _parseDetails() {
        content.get(new byte[6]); // ignore 6 reserved bytes;
        dataReferenceIndex = IsoTypeReader.readUInt16(content);
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (Box box : boxes) {
            sizeOfChildren += box.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
