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

package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Contains basic information about the (rtp-) hint samples in this track.
 */
public class RtpHintSampleEntry extends SampleEntry implements ContainerBox {
    public static final String TYPE1 = "rtp ";

    private int hintTrackVersion;
    private int highestCompatibleVersion;
    private long maxPacketSize;

    public RtpHintSampleEntry(String type) {
        super(type);
    }


    public int getHintTrackVersion() {
        return hintTrackVersion;
    }

    public int getHighestCompatibleVersion() {
        return highestCompatibleVersion;
    }

    public long getMaxPacketSize() {
        return maxPacketSize;
    }

    @Override
    protected long getContentSize() {
        long contentLength = 0;
        for (Box box : boxes) {
            contentLength += box.getSize();
        }
        return 16 + contentLength;
    }

    @Override
    public void _parseDetails() {
        _parseReservedAndDataReferenceIndex();
        hintTrackVersion = IsoTypeReader.readUInt16(content);
        highestCompatibleVersion = IsoTypeReader.readUInt16(content);
        maxPacketSize = IsoTypeReader.readUInt32(content);
        _parseChildBoxes();
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        _writeReservedAndDataReferenceIndex(bb);
        IsoTypeWriter.writeUInt16(bb, hintTrackVersion);
        IsoTypeWriter.writeUInt16(bb, highestCompatibleVersion);
        IsoTypeWriter.writeUInt32(bb, maxPacketSize);
        _writeChildBoxes(bb);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RtpHintSampleEntry[");
        builder.append("hintTrackVersion=").append(getHintTrackVersion()).append(";");
        builder.append("highestCompatibleVersion=").append(getHighestCompatibleVersion()).append(";");
        builder.append("maxPacketSize=").append(getMaxPacketSize());
        for (int i = 0; i < boxes.size(); i++) {
            if (i > 0) {
                builder.append(";");
            }
            builder.append(boxes.get(i).toString());
        }
        builder.append("]");
        return builder.toString();
    }
}
