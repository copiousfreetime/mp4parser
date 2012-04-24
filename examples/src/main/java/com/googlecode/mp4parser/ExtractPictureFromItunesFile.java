package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.apple.AppleDataBox;
import com.googlecode.mp4parser.util.Path;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ExtractPictureFromItunesFile {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile(new FileInputStream(args[0]).getChannel());
        Path p = new Path(isoFile);

        AppleDataBox data = (AppleDataBox) p.getPath("/moov/udta/meta/ilst/covr/data");
        String ext;
        if ((data.getFlags() & 0x1) == 0x1) {
            ext = "jpg";
        } else if ((data.getFlags() & 0x2) == 0x2) {
            ext = "png";
        } else {
            System.err.println("Unknown Image Type");
            ext = "unknown";
        }

        FileOutputStream fos = new FileOutputStream("image." + ext);
        fos.write(data.getData());
        fos.close();
    }
}
