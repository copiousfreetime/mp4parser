package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

/**
 * Muxes 2 audio tracks with a video track.
 */
public class MuxExample {
    public static void main(String[] args) throws IOException {


        Movie countVideo = MovieCreator.build(Channels.newChannel(MuxExample.class.getResourceAsStream("/count-video.mp4")));
        Movie countAudioDeutsch = MovieCreator.build(Channels.newChannel(MuxExample.class.getResourceAsStream("/count-deutsch-audio.mp4")));
        Movie countAudioEnglish = MovieCreator.build(Channels.newChannel(MuxExample.class.getResourceAsStream("/count-english-audio.mp4")));

        Track audioTrackDeutsch = countAudioDeutsch.getTracks().get(0);
        audioTrackDeutsch.getTrackMetaData().setLanguage("deu");
        Track audioTrackEnglish = countAudioEnglish.getTracks().get(0);
        audioTrackEnglish.getTrackMetaData().setLanguage("eng");

        countVideo.addTrack(audioTrackDeutsch);
        countVideo.addTrack(audioTrackEnglish);

        {
            IsoFile out = new DefaultMp4Builder().build(countVideo);
            FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
            out.getBox(fos.getChannel());
            fos.close();
        }
        {
            FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
            fragmentedMp4Builder.setIntersectionFinder(new SyncSampleIntersectFinderImpl());
            IsoFile out = fragmentedMp4Builder.build(countVideo);
            FileOutputStream fos = new FileOutputStream(new File("output-frag.mp4"));
            out.getBox(fos.getChannel());
            fos.close();
        }
    }

}
