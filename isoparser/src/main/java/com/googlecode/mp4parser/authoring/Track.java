package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.mdat.Sample;

import java.util.List;

/**
 * Represents a Track. A track is a timed sequence of related samples.
 * <p/>
 * <b>NOTE: </b><br/
 * For media data, a track corresponds to a sequence of images or sampled audio; for hint tracks, a track
 * corresponds to a streaming channel.
 */
public interface Track {

    SampleDescriptionBox getSampleDescriptionBox();

    List<TimeToSampleBox.Entry> getDecodingTimeEntries();

    List<CompositionTimeToSample.Entry> getCompositionTimeEntries();

    long[] getSyncSamples();

    List<SampleDependencyTypeBox.Entry> getSampleDependencies();

    TrackMetaData getTrackMetaData();

    String getHandler();

    boolean isEnabled();

    boolean isInMovie();

    boolean isInPreview();

    boolean isInPoster();
    
    List<? extends Sample> getSamples();

    public AbstractMediaHeaderBox getMediaHeaderBox();

}
