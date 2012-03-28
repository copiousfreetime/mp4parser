package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class AppendExample {
    public static void main(String[] args) throws IOException {


        Movie video = MovieCreator.build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-video.mp4")));
        Movie audio = MovieCreator.build(Channels.newChannel(AppendExample.class.getResourceAsStream("/count-english-audio.mp4")));

        List<Track> videoTracks = video.getTracks();
        video.setTracks(new LinkedList<Track>());

        List<Track> audioTracks = audio.getTracks();


        for (Track videoTrack : videoTracks) {
            video.addTrack(new AppendTrack(videoTrack, videoTrack));
        }
        for (Track audioTrack : audioTracks) {
            video.addTrack(new AppendTrack(audioTrack, audioTrack));
        }

        IsoFile out1 = new FragmentedMp4Builder().build(video);
        IsoFile out2 = new DefaultMp4Builder().build(video);

        {
            FileChannel fc = new RandomAccessFile(String.format("output1.mp4"), "rw").getChannel();
            fc.position(0);
            out1.getBox(fc);
            fc.close();
        }
        {
            FileChannel fc = new RandomAccessFile(String.format("output2.mp4"), "rw").getChannel();
            fc.position(0);
            out2.getBox(fc);
            fc.close();
        }


    }


}
