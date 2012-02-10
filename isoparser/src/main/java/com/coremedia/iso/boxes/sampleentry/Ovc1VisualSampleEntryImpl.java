package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/10/12
 * Time: 8:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class Ovc1VisualSampleEntryImpl extends SampleEntry {
    private byte[] vc1Content;
    public static final String TYPE = "ovc1";


    @Override
    protected long getContentSize() {
        long size = 8;

        for (Box box : boxes) {
            size += box.getSize();
        }
        return vc1Content.length + 8 ;
    }

    @Override
    protected void getContent(WritableByteChannel os) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Ovc1VisualSampleEntryImpl() {
        super(TYPE);
    }

}
