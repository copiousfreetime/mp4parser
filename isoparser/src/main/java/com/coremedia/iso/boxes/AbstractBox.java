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

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.ChannelHelper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * A basic ISO box. No full box.
 */
public abstract class AbstractBox implements Box {
    protected ByteBuffer content;

    public long getSize() {
        return getContentSize() + getHeaderSize() + (deadBytes == null ? 0 : deadBytes.capacity());
    }

    protected long getHeaderSize() {
        return 4 + // size
                4 + // type
                (getContentSize() >= 4294967296L ? 8 : 0) +
                (Arrays.equals(getType(), IsoFile.fourCCtoBytes(UserBox.TYPE)) ? 16 : 0);
    }

    /**
     * Gets the box's content size without header size. Flags and version do not belong to the
     * header - they belong to the content and add 4 bytes to the content size
     *
     * @return Gets the box's content size in bytes
     */
    protected abstract long getContentSize();

    protected byte[] type;
    private byte[] userType;
    private ContainerBox parent;


    protected AbstractBox(String type) {
        this.type = IsoFile.fourCCtoBytes(type);
    }

    protected AbstractBox(byte[] type) {
        this.type = type;
    }


    public byte[] getType() {
        return type;
    }


    public byte[] getUserType() {
        return userType;
    }

    public void setUserType(byte[] userType) {
        this.userType = userType;
    }

    public ContainerBox getParent() {
        return parent;
    }

    public void setParent(ContainerBox parent) {
        this.parent = parent;
    }


    public IsoFile getIsoFile() {
        return parent.getIsoFile();
    }

    /**
     * Pareses the given IsoBufferWrapper and returns the remaining bytes.
     *
     * @param in        the (part of the) iso file to parse
     * @param size      expected size of the box
     * @param boxParser creates inner boxes
     * @throws IOException in case of an I/O error.
     */
    public void parse(ReadableByteChannel in, long size, BoxParser boxParser) throws IOException {
        if (in instanceof FileChannel) {
            content = ((FileChannel) in).map(FileChannel.MapMode.READ_ONLY, ((FileChannel) in).position(), size);
        } else {
            assert size > Integer.MAX_VALUE;
            content = ChannelHelper.readFully(in, size);
        }
    }

    /**
     * Parses the boxes fields.
     */
    public final void parseDetails() {
        if (content != null) {
            ByteBuffer content = this.content;
            _parseDetails();
            assert this.content == null;
            if (content.remaining()>0) {
                deadBytes = content.slice();
            }
        }
    }

    /**
     * Implement the actual parsing of the box's fields here. External classes will always call
     * {@link #parseDetails()} which encapsulates the call to this method with some safeguards.
     */
    public abstract void _parseDetails();

    protected ByteBuffer deadBytes = null;

    public ByteBuffer getDeadBytes() {
        return deadBytes;
    }

    public void setDeadBytes(ByteBuffer newDeadBytes) {
        deadBytes = newDeadBytes;
    }

    public void getHeader(WritableByteChannel byteChannel) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IsoOutputStream ios = new IsoOutputStream(baos);
            if (isSmallBox()) {
                ios.writeUInt32((int) this.getContentSize() + 8);
                ios.write(getType());
            } else {
                ios.writeUInt32(1);
                ios.write(getType());
                ios.writeUInt64(getContentSize() + 16);
            }
            if (Arrays.equals(getType(), IsoFile.fourCCtoBytes(UserBox.TYPE))) {
                ios.write(userType);
            }

            assert baos.size() == getHeaderSize() :
                    "written header size differs from calculated size: " + baos.size() + " vs. " + getHeaderSize();
            byteChannel.write(ByteBuffer.wrap(baos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected boolean isSmallBox() {
        return (getContentSize() + 8) < 4294967296L;
    }


    public void getBox(WritableByteChannel os) throws IOException {
        getHeader(os);
        if (content == null) {
            getContent(os);
            if (deadBytes != null) {
                deadBytes.position(0);
                while (deadBytes.remaining() > 0) {
                    os.write(deadBytes);
                }
            }
        }
    }

    /**
     * Writes the box's content into the given <code>IsoOutputStream</code>. This MUST NOT include
     * any header bytes.
     *
     * @param os the box's content-sink.
     * @throws IOException in case of an exception in the underlying <code>OutputStream</code>.
     */
    protected abstract void getContent(WritableByteChannel os) throws IOException;
}
