package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * aligned(8) class SampleToGroupBox
 * extends FullBox('sbgp', version = 0, 0)
 * {
 * unsigned int(32) grouping_type;
 * unsigned int(32) entry_count;
 * for (i=1; i <= entry_count; i++)
 * {
 * unsigned int(32) sample_count;
 * unsigned int(32) group_description_index;
 * }
 * }
 */
public class SampleToGroupBox extends AbstractFullBox {
    public static final String TYPE = "sbgp";
    private long groupingType;
    private long entryCount;
    private List<Entry> entries = new ArrayList<Entry>();

    public SampleToGroupBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 12 + entryCount * 8;
    }


    @Override
    public void _parseDetails() {
        parseVersionAndFlags();
        groupingType = IsoTypeReader.readUInt32(content);
        entryCount = IsoTypeReader.readUInt32(content);

        for (int i = 0; i < entryCount; i++) {
            Entry entry = new Entry();
            entry.setSampleCount(IsoTypeReader.readUInt32(content));
            entry.setGroupDescriptionIndex(IsoTypeReader.readUInt32(content));
            entries.add(entry);
        }
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);

        IsoTypeWriter.writeUInt32(bb, groupingType);
        IsoTypeWriter.writeUInt32(bb, entryCount);
        for (Entry entry : entries) {
            IsoTypeWriter.writeUInt32(bb, entry.getSampleCount());
            IsoTypeWriter.writeUInt32(bb, entry.getGroupDescriptionIndex());
        }
    }

    public static class Entry {
        private long sampleCount;
        private long groupDescriptionIndex;

        public long getSampleCount() {
            return sampleCount;
        }

        public void setSampleCount(long sampleCount) {
            this.sampleCount = sampleCount;
        }

        public long getGroupDescriptionIndex() {
            return groupDescriptionIndex;
        }

        public void setGroupDescriptionIndex(long groupDescriptionIndex) {
            this.groupDescriptionIndex = groupDescriptionIndex;
        }
    }
}
