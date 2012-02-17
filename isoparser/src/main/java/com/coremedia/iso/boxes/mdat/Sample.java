package com.coremedia.iso.boxes.mdat;

import java.nio.ByteBuffer;

public interface Sample {
    int getSize();
    ByteBuffer getBytes();
}
