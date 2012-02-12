package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Apple Name box. Allowed as subbox of "----" box.
 *
 * @see AppleGenericBox
 */
public final class AppleNameBox extends AbstractFullBox {
    public static final String TYPE = "name";
    private String name;

    public AppleNameBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 4 + Utf8.convert(name).length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void _parseDetails() {
        parseVersionAndFlags();
        name = IsoTypeReader.readString(content, content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        bb.put(Utf8.convert(name));
    }
}
