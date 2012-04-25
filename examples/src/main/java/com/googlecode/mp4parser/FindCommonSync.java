package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-25
 * Time: 11:55
 * To change this template use File | Settings | File Templates.
 */
//        /Users/magnus/Projects/castlabs/h3g/material/805_Die_Nebel_von_Avalon_PAL_D_SDVID0.mp4 /Users/magnus/Projects/castlabs/h3g/material/805_Die_Nebel_von_Avalon_PAL_D_SDVID1.mp4 /Users/magnus/Projects/castlabs/h3g/material/805_Die_Nebel_von_Avalon_PAL_D_SDVID2.mp4
//        /Users/magnus/Projects/castlabs/h3g/material/3AT_test_1280x720_HD_2_0DE_16x9_HDVID0.mp4 /Users/magnus/Projects/castlabs/h3g/material/3AT_test_1280x720_HD_2_0DE_16x9_HDVID1.mp4 /Users/magnus/Projects/castlabs/h3g/material/3AT_test_1280x720_HD_2_0DE_16x9_HDVID2.mp4
//    /Users/magnus/Projects/castlabs/h3g/material/805_Die_Nebel_von_Avalon_PAL_D_WP7VID1.mp4 /Users/magnus/Projects/castlabs/h3g/material/805_Die_Nebel_von_Avalon_PAL_D_WP7VID2.mp4 /Users/magnus/Projects/castlabs/h3g/material/805_Die_Nebel_von_Avalon_PAL_D_WP7VID3.mp4

public class FindCommonSync {

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static void main(String[] args) throws IOException {
        HashMap<Integer, Integer> common = new HashMap<Integer, Integer>();
        boolean first = true;
        for (int i = 0; i < args.length; i++) {
            Movie invideo = new MovieCreator().build(Channels.newChannel(new FileInputStream(args[i])));
            List<Track> tracks = invideo.getTracks();

            for (Track t : tracks) {
                String type = t.getMediaHeaderBox().getType();
                String ctype = t.getSampleDescriptionBox().getSampleEntry().getType();
                System.out.println("Track of type " + type + " (" + ctype + ")");
                if (type.equals("vmhd")) {
                    HashMap<Integer, Integer> previous = (HashMap<Integer, Integer>) common.clone();
                    common.clear();
                    System.out.println("Found video track in " + args[i]);
                    long[] syncSamples = t.getSyncSamples();
                    long timescale = t.getTrackMetaData().getTimescale();
                    long tts = t.getDecodingTimeEntries().get(0).getDelta();
                    for (int j = 0; j < syncSamples.length; j++) {
                        long time = 1000 * tts * (syncSamples[j] - 1) / timescale;
                        int inttime = (int)time;
                        if (first || previous.containsKey(inttime)) {
                            common.put(inttime, 1);
                        }
                    }
                    first = false;
                }
            }
        }
        // Print the common times
        Set<Integer> keys = common.keySet();

        List<Integer> inorder = asSortedList(keys);
        Integer previous = 0;
        int wrong = 0;
        for (Integer sync : inorder) {
            Integer delta = sync - previous;
            System.out.println("Common sync point: " + (double)sync / 1000.0 + " delta: " + (double)delta / 1000.0);
            if (delta > 3000) {
                System.out.println("WARNING WARNING! > 3sek");
                wrong++;
            }
            previous = sync;
        }
        int commonCount = inorder.size();
        System.out.println("Durations that are too long: " + wrong + "/" + commonCount);

    }
}
