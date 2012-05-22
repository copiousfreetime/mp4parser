package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.NullMediaHeaderBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Iterator;
import java.util.Properties;


public class DumpAmf0TrackToPropertyFile {
    public static void main(String[] args) throws IOException {
        Movie movie = MovieCreator.build(Channels.newChannel(DumpAmf0TrackToPropertyFile.class.getResourceAsStream("/example.f4v")));


        for (Track track : movie.getTracks()) {
            if (track.getHandler().equals("data") && (track.getMediaHeaderBox() instanceof NullMediaHeaderBox)) {
                long time = 0;
                Iterator<ByteBuffer> samples = track.getSamples().iterator();
                Properties properties = new Properties();
                File f = File.createTempFile(DumpAmf0TrackToPropertyFile.class.getSimpleName(), "" + track.getTrackMetaData().getTrackId());
                for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
                    for (int i = 0; i < entry.getCount(); i++) {
                        ByteBuffer sample = samples.next();
                        byte[] sampleBytes = new byte[sample.limit()];
                        sample.rewind();
                        sample.get(sampleBytes);
                        properties.put("" + time, new String(Base64.encodeBase64(sampleBytes, false, false)));
                        time += entry.getDelta();
                    }
                }
                FileOutputStream fos = new FileOutputStream(f);
                System.err.println(properties);
                properties.store(fos, "");

            }
        }
    }


}
