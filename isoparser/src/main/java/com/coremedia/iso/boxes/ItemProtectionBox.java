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

package com.coremedia.iso.boxes;

import com.coremedia.iso.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * The Item Protection Box provides an array of item protection information, for use by the Item Information Box.
 *
 * @see com.coremedia.iso.boxes.ItemProtectionBox
 */
public class ItemProtectionBox extends FullContainerBox {
    int protectionCount;

    public static final String TYPE = "ipro";

    public ItemProtectionBox() {
        super(TYPE);
    }

    public SchemeInformationBox getItemProtectionScheme() {
        if (!getBoxes(SchemeInformationBox.class).isEmpty()) {
            return getBoxes(SchemeInformationBox.class).get(0);
        } else {
            return null;
        }
    }
    public void parse(ReadableByteChannel in, ByteBuffer header, long size, BoxParser boxParser) throws IOException {
        content = ChannelHelper.readFully(in, 6);
        parseBoxes(size - 4, in, boxParser);
    }

    @Override
    public void _parseDetails() {
        parseVersionAndFlags();
        IsoTypeReader.readUInt16(content);
    }

    @Override
    public void getContentBeforeChildren(WritableByteChannel os) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(6);
        writeVersionAndFlags(bb);
        IsoTypeWriter.writeUInt16(bb, protectionCount);
        os.write(bb);
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt16(protectionCount);
        for (Box boxe : boxes) {
            boxe.getBox(os);
        }
    }

}
