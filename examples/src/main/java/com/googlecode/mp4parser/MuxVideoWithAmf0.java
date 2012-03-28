package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.TwoSecondIntersectionFinder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.Amf0Track;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Properties;

/**
 * Shows a simple use of the AMF0Track
 */
public class MuxVideoWithAmf0 {
    public static void main(String[] args) throws IOException {
        Movie video = MovieCreator.build(Channels.newChannel(Mp4WithAudioDelayExample.class.getResourceAsStream("/example-sans-amf0.mp4")));

        Properties props = new Properties();
        props.load(MuxVideoWithAmf0.class.getResourceAsStream("/amf0track.properties"));
        HashMap<Long, byte[]> samples = new HashMap<Long, byte[]>();
        for (String key : props.stringPropertyNames()) {
            samples.put(Long.parseLong(key), Base64.decodeBase64(props.getProperty(key)));
        }
        Track amf0Track = new Amf0Track(samples);
        amf0Track.getTrackMetaData().setStartTime(2400);
        video.addTrack(amf0Track);

        FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
        fragmentedMp4Builder.setIntersectionFinder(new TwoSecondIntersectionFinder());

        IsoFile out = fragmentedMp4Builder.build(video);
        FileOutputStream fos = new FileOutputStream(new File(String.format("output.mp4")));

        out.getBox(fos.getChannel());
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
