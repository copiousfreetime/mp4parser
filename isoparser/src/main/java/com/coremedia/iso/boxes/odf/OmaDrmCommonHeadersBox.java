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

package com.coremedia.iso.boxes.odf;


import com.coremedia.iso.*;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.googlecode.mp4parser.ByteBufferByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Common Headers Box defines a structure for required headers in a DCF file.
 * See OMA-TS-DRM-DCF-V2_0-*  specification for details.
 */
public class OmaDrmCommonHeadersBox extends AbstractFullBox implements ContainerBox {
    public static final String TYPE = "ohdr";

    private List<Box> extendedHeaders;
    private int encryptionMethod;
    private int paddingScheme;
    private long plaintextLength;
    private String contentId;
    private String rightsIssuerUrl;
    private String textualHeaders;
    private BoxParser boxParser;

    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box boxe : extendedHeaders) {
            //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            // I hereby finally decide to use isInstance

            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof ContainerBox) {
                boxesToBeReturned.addAll(((ContainerBox) boxe).getBoxes(clazz, recursive));
            }
        }
        return boxesToBeReturned;
    }

    public OmaDrmCommonHeadersBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
        contentId = "";
        rightsIssuerUrl = "";
        textualHeaders = "";
        extendedHeaders = new LinkedList<Box>();
    }

    public List<Box> getBoxes() {
        return extendedHeaders;
    }

    public void setTextualHeaders(Map<String, String> m) {
        textualHeaders = "";
        if (m != null) {
            for (String key : m.keySet()) {
                String value = m.get(key);
                textualHeaders += key + ":";
                textualHeaders += value + "\0";
            }
        }
    }

    public void setRightsIssuerUrl(String rightsIssuerUrl) {
        assert rightsIssuerUrl != null;
        this.rightsIssuerUrl = rightsIssuerUrl;
    }

    public void setContentId(String contentId) {
        assert contentId != null;
        this.contentId = contentId;
    }

    public void setPlaintextLength(long plaintextLength) {
        this.plaintextLength = plaintextLength;
    }

    public void setPaddingScheme(int paddingScheme) {
        assert paddingScheme == 0 || paddingScheme == 1;
        this.paddingScheme = paddingScheme;
    }

    public void setEncryptionMethod(int encryptionMethod) {
        assert encryptionMethod == 0 || encryptionMethod == 1 || encryptionMethod == 2;
        this.encryptionMethod = encryptionMethod;
    }

    public int getEncryptionMethod() {
        return encryptionMethod;
    }

    public int getPaddingScheme() {
        return paddingScheme;
    }

    public long getPlaintextLength() {
        return plaintextLength;
    }

    public String getContentId() {
        return contentId;
    }

    public String getRightsIssuerUrl() {
        return rightsIssuerUrl;
    }

    public String getTextualHeaders() {
        return textualHeaders;
    }

    @Override
    public void parse(ReadableByteChannel in, ByteBuffer header, long size, BoxParser boxParser) throws IOException {
        super.parse(in, header, size, boxParser);
        this.boxParser = boxParser;
    }

    protected long getContentSize() {
        long contentLength;
        contentLength = 20 +
                Utf8.utf8StringLengthInBytes(contentId) + Utf8.utf8StringLengthInBytes(rightsIssuerUrl) +
                Utf8.utf8StringLengthInBytes(textualHeaders);
        for (Box boxe : extendedHeaders) {
            contentLength += boxe.getSize();
        }

        return contentLength;
    }

    @Override
    public void _parseDetails() {
        parseVersionAndFlags();
        encryptionMethod = IsoTypeReader.readUInt8(content);
        paddingScheme = IsoTypeReader.readUInt8(content);
        plaintextLength = IsoTypeReader.readUInt64(content);
        int contentIdLength = IsoTypeReader.readUInt16(content);
        int rightsIssuerUrlLength = IsoTypeReader.readUInt16(content);
        int textualHeadersLength = IsoTypeReader.readUInt16(content);
        contentId = IsoTypeReader.readString(content, contentIdLength);
        rightsIssuerUrl = IsoTypeReader.readString(content, rightsIssuerUrlLength);
        textualHeaders = IsoTypeReader.readString(content, textualHeadersLength);

        while (content.remaining() > 8) {
            try {
                extendedHeaders.add(boxParser.parseBox(new ByteBufferByteChannel(content), this));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        deadBytes = content.slice();
    }


    protected void getContent(ByteBuffer bb) throws IOException {
        IsoTypeWriter.writeUInt8(bb, encryptionMethod);
        IsoTypeWriter.writeUInt8(bb, paddingScheme);
        IsoTypeWriter.writeUInt64(bb, plaintextLength);
        IsoTypeWriter.writeUInt16(bb, Utf8.utf8StringLengthInBytes(contentId));
        IsoTypeWriter.writeUInt16(bb, Utf8.utf8StringLengthInBytes(rightsIssuerUrl));
        IsoTypeWriter.writeUInt16(bb, Utf8.utf8StringLengthInBytes(textualHeaders));
        bb.put(Utf8.convert(contentId));
        bb.put(Utf8.convert(rightsIssuerUrl));
        bb.put(Utf8.convert(textualHeaders));

        for (Box boxe : extendedHeaders) {
            boxe.getBox(new ByteBufferByteChannel(bb));
        }

    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("OmaDrmCommonHeadersBox[");
        buffer.append("encryptionMethod=").append(getEncryptionMethod()).append(";");
        buffer.append("paddingScheme=").append(getPaddingScheme()).append(";");
        buffer.append("plaintextLength=").append(getPlaintextLength()).append(";");
        buffer.append("contentId=").append(getContentId()).append(";");
        buffer.append("rightsIssuerUrl=").append(getRightsIssuerUrl()).append(";");
        buffer.append("textualHeaders=").append(getTextualHeaders());
        for (Box box : getBoxes()) {
            buffer.append(";");
            buffer.append(box.toString());
        }
        buffer.append("]");
        return buffer.toString();
    }


    public void setBoxes(List<Box> extendedHeaders) {
        this.extendedHeaders = extendedHeaders;
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (Box extendedHeader : extendedHeaders) {
            sizeOfChildren += extendedHeader.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
