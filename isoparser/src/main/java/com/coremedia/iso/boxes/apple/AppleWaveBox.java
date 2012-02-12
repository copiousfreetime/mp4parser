package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 *
 */
public final class AppleWaveBox extends AbstractContainerBox {
    public static final String TYPE = "wave";

    public AppleWaveBox() {
        super(TYPE);
    }




}
