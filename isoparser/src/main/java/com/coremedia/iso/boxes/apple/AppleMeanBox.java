package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Apple Meaning box. Allowed as subbox of "----" box.
 *
 * @see com.coremedia.iso.boxes.apple.AppleGenericBox
 */
public final class AppleMeanBox extends AbstractFullBox {
    public static final String TYPE = "mean";
    private String meaning;

    public AppleMeanBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 4 + Utf8.utf8StringLengthInBytes(meaning);
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeStringNoTerm(meaning);
    }

    @Override
    public void _parseDetails() {
        parseVersionAndFlags();
        meaning =  IsoTypeReader.readString(content, content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        bb.put(Utf8.convert(meaning));
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }


    
}
