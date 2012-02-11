package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * Abstract Chunk Offset Box
 */
public abstract class ChunkOffsetBox extends AbstractFullBox {

    public ChunkOffsetBox(String type) {
        super(type);
    }

    public abstract long[] getChunkOffsets();


    public String toString() {
        return this.getClass().getSimpleName() + "[entryCount=" + getChunkOffsets().length + "]";
    }

}
