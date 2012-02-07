package com.googlecode.mp4parser;


import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.HandlerBox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VideoAudioChecker {

    public static void main(String[] args) throws IOException {

        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapperImpl(readFully(SubTitleExample.class.getResourceAsStream("/count-video.mp4")));
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
        isoFile.parse();
    }
    public TYPE getType(IsoFile isoFile) {

        List<HandlerBox> handlerBoxes =
                isoFile.getBoxes(HandlerBox.class, true);
        for (HandlerBox handlerBox : handlerBoxes) {
            if ("vide".equals(handlerBox.getHandlerType()))
                return TYPE.VIDEO;
        }
        return TYPE.AUDIO;
    }

    private enum TYPE {
        AUDIO,
        VIDEO;
    }


    static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }
}
