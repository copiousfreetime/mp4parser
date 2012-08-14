package com.googlecode.mp4parser.tools.smoothstreamingfragmenter;


import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

public class FragmentFileSetTest {

    private File createTmpDir() {
        try {
            File tmpDir = File.createTempFile("FragmentFileSetTest", "testCommandLine");
            Assume.assumeTrue(tmpDir.delete());
            Assume.assumeTrue(tmpDir.mkdir());
            return tmpDir;
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        return null;
    }

    private void copyResource(String resource, File targetDir) {
        InputStream is = FragmentFileSetTest.class.getResourceAsStream(resource);
        Assume.assumeNotNull(is);
        int i = resource.lastIndexOf("/");
        String filename;
        if (i == -1) {
            filename = resource;
        } else {
            filename = resource.substring(i + 1);
        }
        try {
            FileOutputStream fos = new FileOutputStream(new File(targetDir, filename));
            IOUtils.copy(is, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void testCommandLine() throws IOException {
        File tmpDir = createTmpDir();
        File outputDir = createTmpDir();
        copyResource("/smoothstreaming/audio-96000.mp4", tmpDir);
        copyResource("/smoothstreaming/video-128h-75kbps.mp4", tmpDir);
        copyResource("/smoothstreaming/video-192h-155kbps.mp4", tmpDir);
        copyResource("/smoothstreaming/video-240h-231kbps.mp4", tmpDir);
        copyResource("/smoothstreaming/video-320h-388kbps.mp4", tmpDir);

        FragmentFileSet fragmentFileSet = new FragmentFileSet();
        File inputs[] = tmpDir.listFiles();
        Assert.assertNotNull(inputs);
        fragmentFileSet.inputFiles = Arrays.asList(inputs);
        fragmentFileSet.outputDir = outputDir;
        fragmentFileSet.run();
        System.err.println(outputDir);

        Assert.assertTrue(new File(outputDir, "Manifest").exists());
        Assert.assertTrue(new File(outputDir, "video").exists());
        Assert.assertTrue(new File(outputDir, "audio").exists());
        // todo This is merely a smoke test. I should have a better test.
        // but how does this better test look like? Get some samples?
        // search first and last sample of each file for a start?




    }

    class SizeDirectoryWalker extends DirectoryWalker<Long> {
        @Override
        protected void handleFile(File file, int depth, Collection<Long> results) throws IOException {
            super.handleFile(file, depth, results);
            results.add(file.length());
        }
    }
}
