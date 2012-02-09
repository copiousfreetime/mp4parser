package com.coremedia.iso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public final class IsoTypeReader {

    public static long readUInt32(ByteBuffer bb) {
        long ch1 = readUInt8(bb);
        long ch2 = readUInt8(bb);
        long ch3 = readUInt8(bb);
        long ch4 = readUInt8(bb);
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

    }

    public static int readUInt24(ByteBuffer bb) {
        int result = 0;
        result += readUInt16(bb) << 8;
        result += byte2int(bb.get());
        return result;
    }


    public static int readUInt16(ByteBuffer bb) {
        int result = 0;
        result += byte2int(bb.get()) << 8;
        result += byte2int(bb.get());
        return result;
    }

    public static int readUInt8(ByteBuffer bb) {
        return byte2int(bb.get());
    }

    private static int byte2int(byte b) {
        return b < 0 ? b + 256 : b;
    }


    /**
     * Reads a zero terminated UTF-8 string.
     *
     * @param byteBuffer the data source
     * @return the string readByte
     * @throws Error in case of an error in the underlying stream
     */
    public static String readString(ByteBuffer byteBuffer) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = byteBuffer.get()) != 0) {
            out.write(read);
        }
        return Utf8.convert(out.toByteArray());
    }

    public static String readString(ByteBuffer byteBuffer, int length) {
        byte[] buffer = new byte[length];
        byteBuffer.get(buffer);
        return Utf8.convert(buffer);

    }

    public static long readUInt64(ByteBuffer byteBuffer) throws IOException {
        long result = 0;
        // thanks to Erik Nicolas for finding a bug! Cast to long is definitivly needed
        result += readUInt32(byteBuffer) << 32;
        if (result < 0) {
            throw new RuntimeException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
        }
        result += readUInt32(byteBuffer);

        return result;
    }
}
