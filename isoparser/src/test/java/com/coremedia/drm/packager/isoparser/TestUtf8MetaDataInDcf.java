package com.coremedia.drm.packager.isoparser;

import junit.framework.TestCase;
import com.coremedia.iso.IsoFile;

import java.io.IOException;
import java.nio.channels.Channels;

/**
 * Tests UTF-8 capability.
 */
public class TestUtf8MetaDataInDcf extends TestCase {
    public void testUtf8() throws IOException {

        IsoFile isoFile = new IsoFile(Channels.newChannel(getClass().getResourceAsStream("/file6141.odf")));
        isoFile.parse();
        System.err.println(isoFile);
    }
}
