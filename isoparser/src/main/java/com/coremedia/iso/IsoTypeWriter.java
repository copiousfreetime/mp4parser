package com.coremedia.iso;

import java.nio.ByteBuffer;

public final class IsoTypeWriter {

    public static void writeUInt64(ByteBuffer bb, long u)  {

        writeUInt32(bb, ((u >> 32) & 0xFFFFFFFFl));
        writeUInt32(bb, u & 0xFFFFFFFFl);

    }
    public static void writeUInt32(ByteBuffer bb, long u)  {

        writeUInt16(bb, (int) ((u >> 16) & 0xFFFF));
        writeUInt16(bb, (int) u & 0xFFFF);

    }


    public static void writeUInt24(ByteBuffer bb, int i)  {
        i = i & 0xFFFFFF;
        writeUInt16(bb, i) ;
        writeUInt8(bb, i);

    }


    public static void writeUInt16(ByteBuffer bb, int i)  {
        i = i & 0xFFFF;
        writeUInt8(bb, i >> 8);
        writeUInt8(bb, i & 0xFF);
    }

    public static void writeUInt8(ByteBuffer bb, int i) {
        bb.put(int2byte(i));
    }

    private static byte int2byte(int i) {
        i = i & 0xFF;
        return (byte) (i > 127 ? i - 256 : i);
    }
}
