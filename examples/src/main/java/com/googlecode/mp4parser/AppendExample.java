package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.*;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class AppendExample {
    public static void main(String[] args) throws IOException {


        Movie video = new MovieCreator().build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-video.mp4")));
        Movie audio = new MovieCreator().build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-english-audio.mp4")));

        List<Track> videoTracks = video.getTracks();
        video.setTracks(new LinkedList<Track>());

        List<Track> audioTracks = audio.getTracks();


        for (Track videoTrack : videoTracks) {
            video.addTrack(new AppendTrack(videoTrack, videoTrack));
        }
        for (Track audioTrack : audioTracks) {
            video.addTrack(new AppendTrack(audioTrack, audioTrack));
        }

        IsoFile out = new DefaultMp4Builder().build(video);
        FileOutputStream fos = new FileOutputStream(new File(String.format("output.mp4")));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        out.getBox(new IsoOutputStream(bos));
        bos.close();
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
