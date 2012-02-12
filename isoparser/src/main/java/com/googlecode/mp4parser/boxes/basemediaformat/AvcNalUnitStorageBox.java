/*
 * Copyright 2011 castLabs, Berlin
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

package com.googlecode.mp4parser.boxes.basemediaformat;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;import java.lang.Override;import java.lang.RuntimeException;import java.lang.System;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

/**
 * The AVC NAL Unit Storage Box SHALL contain an AVCDecoderConfigurationRecord,
 * as defined in section 5.2.4.1 of the ISO 14496-12.
 */
public class AvcNalUnitStorageBox extends AbstractBox {
    byte[] data;

    public AvcNalUnitStorageBox() {
        super(IsoFile.fourCCtoBytes("avcn"));
    }


    public AvcNalUnitStorageBox(AvcConfigurationBox avcConfigurationBox) {
        super(IsoFile.fourCCtoBytes("avcn"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            avcConfigurationBox.getBox(new IsoOutputStream(baos));
        } catch (IOException e) {
            // cannot happen ?! haha!
            throw new RuntimeException(e);
        }
        ByteArrayOutputStream headerBaos = new ByteArrayOutputStream();
        avcConfigurationBox.getHeader(Channels.newChannel(headerBaos));
        byte[] header = headerBaos.toByteArray();
        data = new byte[baos.size() - header.length];
        System.arraycopy(baos.toByteArray(), header.length, data, 0, data.length);
    }

    @Override
    protected long getContentSize() {
        return data.length;
    }


    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void _parseDetails() {
        data = new byte[content.remaining()];
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        bb.put(data);
    }
}
