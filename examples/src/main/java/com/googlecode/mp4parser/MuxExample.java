package com.googlecode.mp4parser;

import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Muxes 2 audio tracks with a video track.
 */
public class MuxExample {
    public static void main(String[] args) throws IOException {
        IsoFile countVideoIsoFile = new IsoFile(new IsoBufferWrapperImpl(readFully(MuxExample.class.getResourceAsStream("/count-video.mp4"))));
        countVideoIsoFile.parse();
        Movie countVideo = new MovieCreator().build(countVideoIsoFile);

        IsoFile countAudioDeutschIsoFile = new IsoFile(new IsoBufferWrapperImpl(readFully(MuxExample.class.getResourceAsStream("/count-deutsch-audio.mp4"))));
        countAudioDeutschIsoFile.parse();
        Movie countAudioDeutsch = new MovieCreator().build(countAudioDeutschIsoFile);

        IsoFile countAudioEnglishIsoFile = new IsoFile(new IsoBufferWrapperImpl(readFully(MuxExample.class.getResourceAsStream("/count-english-audio.mp4"))));
        countAudioEnglishIsoFile.parse();
        Movie countAudioEnglish = new MovieCreator().build(countAudioEnglishIsoFile);

        Track audioTrackDeutsch = countAudioDeutsch.getTracks().get(0);
        audioTrackDeutsch.getTrackMetaData().setLanguage("deu");
        Track audioTrackEnglish = countAudioEnglish.getTracks().get(0);
        audioTrackEnglish.getTrackMetaData().setLanguage("eng");

        countVideo.addTrack(audioTrackDeutsch);
        countVideo.addTrack(audioTrackEnglish);

        IsoFile out = new DefaultMp4Builder().build(countVideo);
        FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
        out.getBox(new IsoOutputStream(fos));
        fos.close();
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
