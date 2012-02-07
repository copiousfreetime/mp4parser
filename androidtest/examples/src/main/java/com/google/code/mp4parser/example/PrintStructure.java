package com.google.code.mp4parser.example;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 8/5/11
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrintStructure {
    public static void main(String[] args) throws IOException {
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapperImpl(new File(args[0]));
        PrintStructure ps = new PrintStructure();
        ps.print(isoBufferWrapper, 0, 0);
    }

    private void print(IsoBufferWrapper isoBufferWrapper, int level, long baseoffset) throws IOException {
        while (isoBufferWrapper.remaining() > 8) {
            long start = isoBufferWrapper.position();
            long size = isoBufferWrapper.readUInt32();
            String type = isoBufferWrapper.readString(4);
            long end = start + size;
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }

            System.out.println(type + "@" + (baseoffset + start) + " size: " + size);
            if (containers.contains(type)) {
                print(isoBufferWrapper.getSegment(start + 8, size), level + 1, baseoffset + start + 8);
            }
            if (type.equals("meta")) {
                isoBufferWrapper.position(start);
                byte[] metaContent = isoBufferWrapper.read((int) size);
            }
            isoBufferWrapper.position(end);

        }
    }

    List<String> containers = Arrays.asList(
            "moov",
            "trak",
            "mdia",
            "minf",
            "udta",
            "stbl"
    );
}
