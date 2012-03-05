package com.googlecode.mp4parser.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Mp4ParserPerformance extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView) findViewById(R.id.text);
        String text = "";

        File sdCard = Environment.getExternalStorageDirectory();
        /*        try {
         FileChannel fc = new RandomAccessFile(new File(sdCard, "suckerpunch-distantplanet_h1080p.mov").getAbsolutePath(), "r").getChannel();
         ByteBuffer content = fc.map(FileChannel.MapMode.READ_ONLY, 0, 20000000) ;
         ArrayList<ByteBuffer> bb = new ArrayList<ByteBuffer>(1200);
         for (int i = 0; i < 1200; i++) {
             content.position(i*1000);
             ByteBuffer part = content.slice();
             part.limit(1000);
             bb.add(part );
         }
         FileOutputStream fos = new FileOutputStream(new File(sdCard, String.format("output.mp4")));
         FileChannel outFC = fos.getChannel();
         outFC.write(bb.toArray(new ByteBuffer[800]));
         fos.close();
         outFC.close();
         tv.append("5");

     } catch (FileNotFoundException e) {
         throw new RuntimeException(e);
     } catch (IOException e) {
         throw new RuntimeException(e);
     }   */


        try {
            //Debug.startMethodTracing("mp4");

            long a = System.currentTimeMillis();
            tv.append("1");
//            Movie movie = new MovieCreator().build(new RandomAccessFileIsoBufferWrapperImpl(new File(sdCard, "suckerpunch-distantplanet_h1080p.mov")));
            Movie movie = new MovieCreator().build(new RandomAccessFile(new File(sdCard, "suckerpunch-distantplanet_h1080p.mov").getAbsolutePath(), "r").getChannel());
            tv.append("2");
            Log.v("PERF", "Parsing took " + (System.currentTimeMillis() - a));
            text += "Parsing took " + Long.toString(System.currentTimeMillis() - a) + "\n";
            List<Track> tracks = movie.getTracks();
            movie.setTracks(new LinkedList<Track>());
            // remove all tracks we will create new tracks from the old

            double startTime = 35.000;
            double endTime = 145.000;

            boolean timeCorrected = false;

            // Here we try to find a track that has sync samples. Since we can only start decoding
            // at such a sample we SHOULD make sure that the start of the new fragment is exactly
            // such a frame
            for (Track track : tracks) {
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (timeCorrected) {
                        // This exception here could be a false positive in case we have multiple tracks
                        // with sync samples at exactly the same positions. E.g. a single movie containing
                        // multiple qualities of the same video (Microsoft Smooth Streaming file)

                        throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                    }
                    startTime = correctTimeToNextSyncSample(track, startTime);
                    endTime = correctTimeToNextSyncSample(track, endTime);
                    timeCorrected = true;
                }
            }

            for (Track track : tracks) {
                long currentSample = 0;
                double currentTime = 0;
                long startSample = -1;
                long endSample = -1;

                for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                    TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
                    for (int j = 0; j < entry.getCount(); j++) {
                        // entry.getDelta() is the amount of time the current sample covers.

                        if (currentTime <= startTime) {
                            // current sample is still before the new starttime
                            startSample = currentSample;
                        }
                        if (currentTime <= endTime) {
                            // current sample is after the new start time and still before the new endtime
                            endSample = currentSample;
                        } else {
                            // current sample is after the end of the cropped video
                            break;
                        }
                        currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                        currentSample++;
                    }
                }
                movie.addTrack(new CroppedTrack(track, startSample, endSample));
            }
            a = System.currentTimeMillis();
            tv.append("3");
            IsoFile mp4 = new DefaultMp4Builder().build(movie);
            Log.v("PERF", "Building took " + (System.currentTimeMillis() - a));
            text += "Building took " + (System.currentTimeMillis() - a) + "\n";
            tv.append("4");
            FileOutputStream fos = new FileOutputStream(new File(sdCard, String.format("output-%f-%f.mp4", startTime, endTime)));
            FileChannel outFC = fos.getChannel();
            a = System.currentTimeMillis();
            mp4.getBox(outFC);
            long fileSize = outFC.size();
            fos.close();
            outFC.close();
            tv.append("5");
            long systemEndTime = System.currentTimeMillis();
            Log.v("PERF", "Writing took " + (systemEndTime - a));
            text += "Writing took " + (systemEndTime - a) + "\n";
            text += "Writing speed " + (fileSize / (systemEndTime - a) / 1000) + " MB/s\n";
            tv.setText(text);
            Debug.stopMethodTracing();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static double correctTimeToNextSyncSample(Track track, double cutHere) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            for (int j = 0; j < entry.getCount(); j++) {
                if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                    // samples always start with 1 but we start with zero therefore +1
                    timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
                }
                currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
        }
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                return timeOfSyncSample;
            }
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

}
