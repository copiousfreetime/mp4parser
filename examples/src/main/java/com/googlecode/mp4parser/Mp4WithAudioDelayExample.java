package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Mp4WithAudioDelayExample {
    public static void main(String[] args) throws IOException {


        Movie video = MovieCreator.build(Channels.newChannel(Mp4WithAudioDelayExample.class.getResourceAsStream("/count-video.mp4")));
        Movie audio = MovieCreator.build(Channels.newChannel(Mp4WithAudioDelayExample.class.getResourceAsStream("/count-english-audio.mp4")));

        List<Track> videoTracks = video.getTracks();
        video.setTracks(new LinkedList<Track>());

        List<Track> audioTracks = audio.getTracks();


        for (Track videoTrack : videoTracks) {
            video.addTrack(new AppendTrack(videoTrack, videoTrack));
        }
        for (Track audioTrack : audioTracks) {
            audioTrack.getTrackMetaData().setStartTime(10.0);
            video.addTrack(audioTrack);
        }

        IsoFile out = new DefaultMp4Builder().build(video);
        FileOutputStream fos = new FileOutputStream(new File(String.format("output.mp4")));
        out.getBox(fos.getChannel());
        fos.close();
    }

}
