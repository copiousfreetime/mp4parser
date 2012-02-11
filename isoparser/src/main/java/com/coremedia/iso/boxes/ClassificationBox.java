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
import java.nio.channels.WritableByteChannel;

import static com.coremedia.iso.boxes.CastUtils.l2i;

/**
 * Classification of the media according to 3GPP 26.244.
 */
public class ClassificationBox extends AbstractFullBox {
    public static final String TYPE = "clsf";


    private String classificationEntity;
    private int classificationTableIndex;
    private String language;
    private String classificationInfo;

    public ClassificationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String getClassificationEntity() {
        return classificationEntity;
    }

    public int getClassificationTableIndex() {
        return classificationTableIndex;
    }

    public String getClassificationInfo() {
        return classificationInfo;
    }

    public void setClassificationEntity(String classificationEntity) {
        this.classificationEntity = classificationEntity;
    }

    public void setClassificationTableIndex(int classificationTableIndex) {
        this.classificationTableIndex = classificationTableIndex;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setClassificationInfo(String classificationInfo) {
        this.classificationInfo = classificationInfo;
    }

    protected long getContentSize() {
        return 4 + 2 + 2 + Utf8.utf8StringLengthInBytes(classificationInfo) + 1;
    }

    @Override
    public void _parseDetails() {
        parseVersionAndFlags();
        byte[] cE = new byte[4];
        content.get(cE);
        classificationEntity = IsoFile.bytesToFourCC(cE);
        classificationTableIndex = IsoTypeReader.readUInt16(content);
        language = IsoTypeReader.readIso639(content);
        classificationInfo = IsoTypeReader.readString(content);
    }

    @Override
    protected void getContent(WritableByteChannel os) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(l2i(getContentSize()));

        bb.put (IsoFile.fourCCtoBytes(classificationEntity));
        IsoTypeWriter.writeUInt16(bb, classificationTableIndex);
        IsoTypeWriter.writeIso639(bb, language);
        bb.put(Utf8.convert(classificationInfo));
        bb.put((byte) 0);
        os.write(bb);
    }


    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("ClassificationBox[language=").append(getLanguage());
        buffer.append("classificationEntity=").append(getClassificationEntity());
        buffer.append(";classificationTableIndex=").append(getClassificationTableIndex());
        buffer.append(";language=").append(getLanguage());
        buffer.append(";classificationInfo=").append(getClassificationInfo());
        buffer.append("]");
        return buffer.toString();
    }
}
