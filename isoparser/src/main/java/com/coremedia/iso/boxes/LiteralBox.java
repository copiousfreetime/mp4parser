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
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * A box with no internal structure. We readByte it and write it without looking into the content.
 */
public abstract class LiteralBox extends AbstractBox {

    public LiteralBox(byte[] type) {
        super(type);
    }

    public String toString() {
        return "UnknownBox[type=" + getType() + ";contentLength=" + (content != null ? content.capacity() : "?") + "]";
    }


    public void setContent(ByteBuffer content) {
        this.content = content;
    }
}
