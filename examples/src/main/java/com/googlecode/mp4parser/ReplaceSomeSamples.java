package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.authoring.tracks.ReplaceSampleTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

/**
 * Replaces some samples.
 */
public class ReplaceSomeSamples {
    public static void main(String[] args) throws IOException {

        Movie originalMovie = MovieCreator.build(Channels.newChannel(ReplaceSomeSamples.class.getResourceAsStream("/count-english-audio.mp4")));

        Track audio = originalMovie.getTracks().get(0);

        Movie nuMovie = new Movie();

        nuMovie.addTrack(new ReplaceSampleTrack(
                new ReplaceSampleTrack(
                        new ReplaceSampleTrack(
                                audio,
                                25, ByteBuffer.allocate(5)),
                        27, ByteBuffer.allocate(5)),
                29, ByteBuffer.allocate(5)));
        IsoFile out = new DefaultMp4Builder().build(nuMovie);
        FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
        out.getBox(fos.getChannel());
        fos.close();
    }

}
