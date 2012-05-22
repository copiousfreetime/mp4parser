package com.googlecode.mp4parser;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.HandlerBox;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.List;

public class VideoAudioChecker {

    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile(Channels.newChannel(SubTitleExample.class.getResourceAsStream("/count-video.mp4")));
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
        VIDEO
    }

}
