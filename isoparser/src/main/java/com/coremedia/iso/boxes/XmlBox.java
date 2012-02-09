package com.coremedia.iso.boxes;

import com.coremedia.iso.*;

import java.io.IOException;

/**
 *
 */
public class XmlBox extends AbstractFullBox {
    String xml;
    public static final String TYPE = "xml ";

    public XmlBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @Override
    protected long getContentSize() {
        return Utf8.utf8StringLengthInBytes(xml);
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeStringNoTerm(xml);
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long a = in.remaining();
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long b = in.remaining();
        size -= (a - b);
        assert size < Integer.MAX_VALUE;
        xml = in.readString((int) size);
    }
}
