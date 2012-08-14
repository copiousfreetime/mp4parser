package com.googlecode.mp4parser.tools.smoothstreamingdownloader;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import nu.xom.ParsingException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 8/11/12
 * Time: 5:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SmoothStreamingTrackTest {
    public static void main(String[] args) throws IOException, ParsingException {
        URL anchor = SmoothStreamingTrackTest.class.getProtectionDomain().getCodeSource().getLocation();
        Movie m = new Movie();
        m.addTrack(new SmoothStreamingTrack(new File(anchor.getFile(), "testdata/Manifest").toURI(), "video", "70090"));
        DefaultMp4Builder builder = new DefaultMp4Builder();
        IsoFile isoFile = builder.build(m);
        RandomAccessFile raf = new RandomAccessFile("output.mp4", "rw");
        isoFile.getBox(raf.getChannel());
        raf.close();
    }

    @Test
    public void test() {

    }
}
