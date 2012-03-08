package com.googlecode.mp4parser.uvvu;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 3/7/12
 * Time: 9:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("/home/sannies/scm/svn/mp4parser/uvu_michi.uvu", "r");
        IsoFile isoFile = new IsoFile(randomAccessFile.getChannel());
        System.err.println(walk(isoFile, ""));
    }

    public static String walk(ContainerBox cb, String s) {
        for (Box box : cb.getBoxes()) {
            System.err.println(s + box.getType() + " - " + box.getSize() );
            if (box instanceof  ContainerBox) {
                walk((ContainerBox) box, " " + s);
            }
        }

        return s;

    }
}
