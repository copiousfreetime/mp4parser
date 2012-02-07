package com.googlecode.mp4parser;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


public class DumpAmf0TrackToPropertyFile {
    public static void main(String[] args) throws IOException {
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(readFully(DumpAmf0TrackToPropertyFile.class.getResourceAsStream("/example.f4v")));
        Movie movie = new MovieCreator().build(ibw);


        for (Track track : movie.getTracks()) {
            if (track.getType() == Track.Type.AMF0) {
                long time = 0;
                Iterator<IsoBufferWrapper> samples = track.getSamples().iterator();
                Properties properties = new Properties();
                File f = File.createTempFile(DumpAmf0TrackToPropertyFile.class.getSimpleName(), "" + track.getTrackMetaData().getTrackId());
                for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
                    for (int i = 0; i < entry.getCount(); i++) {
                        IsoBufferWrapper sample = samples.next();
                        byte[] sampleBytes = sample.read((int) sample.size());
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
