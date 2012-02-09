package com.coremedia.iso;

import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.UserBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.logging.Logger;

import static com.coremedia.iso.boxes.CastUtils.l2i;

/**
 * This BoxParser handles the basic stuff like reading size and extracting box type.
 */
public abstract class AbstractBoxParser implements BoxParser {

    private static Logger LOG = Logger.getLogger(AbstractBoxParser.class.getName());

    public abstract AbstractBox createBox(byte[] type, byte[] userType, byte[] parent);

    /**
     * Parses the next size and type, creates a box instance and parses the box's content.
     *
     * @param byteChannel   the FileChannel pointing to the ISO file
     * @param parent the current box's parent (null if no parent)
     * @return the box just parsed
     * @throws java.io.IOException if reading from <code>in</code> fails
     */
    public Box parseBox(ReadableByteChannel byteChannel, ContainerBox parent) throws IOException {
        FileChannelIsoBufferWrapperImpl in = new FileChannelIsoBufferWrapperImpl(byteChannel);


        ByteBuffer header = ChannelHelper.readFully(byteChannel, 8);

        long size = IsoTypeReader.readUInt32(header);
        // do plausibility check
        if (size < 8 && size > 1) {
            LOG.severe("Plausibility check failed: size < 8 (size = " + size + "). Stop parsing!");
            return null;
        }


        byte[] type = new byte[4];
        header.get(type);
        String prefix = "";
        boolean iWant = false;
        if (iWant) {
            ContainerBox t = parent.getParent();
            while (t != null) {
                prefix = IsoFile.bytesToFourCC(t.getType()) + "/" + prefix;
                t = t.getParent();
            }
        }
        byte[] usertype = null;
        long contentSize;

        if (size == 1) {
            size = in.readUInt64();
            contentSize = size - 16;
        } else if (size == 0) {
            //throw new RuntimeException("Not supported!");
            contentSize = -1;
            size = 1;
        } else {
            contentSize = size - 8;
        }
        if (Arrays.equals(type, IsoFile.fourCCtoBytes(UserBox.TYPE))) {
            usertype = in.read(16);
            contentSize -= 16;
        }
        Box box = createBox(type, usertype,
                parent.getType());
        box.setParent(parent);
        LOG.finest("Parsing " + IsoFile.bytesToFourCC(box.getType()));
        // System.out.println("parsing " + Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);


        if (l2i(size - contentSize) == 8) {
            // default - no large box - no uuid
            // do nothing header's already correct
            header.rewind();
        } else if (l2i(size - contentSize) == 16) {
            header = ByteBuffer.allocate(16);
            IsoTypeWriter.writeUInt32(header, 1);
            header.put(type);
            IsoTypeWriter.writeUInt64(header, size);
        } else if (l2i(size - contentSize) == 24) {
            header = ByteBuffer.allocate(24);
            IsoTypeWriter.writeUInt32(header, size);
            header.put(type);
            header.put(usertype);
        } else if (l2i(size - contentSize) == 32) {
            header = ByteBuffer.allocate(32);
            IsoTypeWriter.writeUInt32(header, size);
            header.put(type);
            IsoTypeWriter.writeUInt64( header, size);
            header.put(usertype);
        } else {
            throw new RuntimeException("I didn't expect that");
        }



        box.parse(byteChannel, header, contentSize, this);
        // System.out.println("box = " + box);


        assert size == box.getSize() :
                "Reconstructed Size is not equal to the number of parsed bytes! (" +
                        IsoFile.bytesToFourCC(box.getType()) + ")"
                        + " Actual Box size: " + size + " Calculated size: " + box.getSize();
        return box;
    }


}
