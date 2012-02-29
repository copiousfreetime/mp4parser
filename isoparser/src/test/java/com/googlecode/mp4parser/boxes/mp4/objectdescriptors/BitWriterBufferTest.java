package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 2/29/12
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class BitWriterBufferTest {
    @Test
    public void testSimple() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        BitWriterBuffer bitWriterBuffer =  new BitWriterBuffer(bb);
        bitWriterBuffer.writeBits(15, 4);
        bb.rewind();
        int test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(15<<4, test);
    }

    @Test
    public void testSimpleOnByteBorder() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        BitWriterBuffer bitWriterBuffer =  new BitWriterBuffer(bb);
        bitWriterBuffer.writeBits(15, 4);
        bitWriterBuffer.writeBits(15, 4);
        bitWriterBuffer.writeBits(15, 4);
        bb.rewind();
        int test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(255, test);
        test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(15<<4, test);
    }

    @Test
    public void testSimpleCrossByteBorder() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        BitWriterBuffer bitWriterBuffer =  new BitWriterBuffer(bb);
        bitWriterBuffer.writeBits(15, 4);
        bitWriterBuffer.writeBits(31, 5);
        bitWriterBuffer.writeBits(7, 3);
        bb.rewind();
        int test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(255, test);
        test = IsoTypeReader.readUInt8(bb);
        Assert.assertEquals(15<<4, test);
    }

}
