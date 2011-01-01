package com.google.code.mp4parser.example;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.PropertyBoxParserImpl;
import com.coremedia.iso.boxes.apple.AppleAlbumBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Parsing and navigating to a specific box.
 * Removing a Box.
 * Adding a Box.
 * Only using a specific set of boxes
 */
public class AddItunesAlbumAttributes_2 {
    public static void main(String[] args) throws IOException {
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapper(new File(args[0]));
        Properties properties = new Properties();

        properties.setProperty("ilst-\u00A9alb", "com.coremedia.iso.boxes.apple.AppleAlbumBox()");
        properties.setProperty("moov", "com.coremedia.iso.boxes.MovieBox()");
        properties.setProperty("meta", "com.coremedia.iso.boxes.MetaBox()");
        properties.setProperty("udta", "com.coremedia.iso.boxes.UserDataBox()");
        properties.setProperty("meta-ilst", "com.coremedia.iso.boxes.apple.AppleItemListBox()");
        properties.setProperty("default", "com.coremedia.iso.boxes.UnknownBox(type)");
        PropertyBoxParserImpl boxParser = new PropertyBoxParserImpl(properties);
        IsoFile isoFile = new IsoFile(isoBufferWrapper, boxParser);
        isoFile.parse();
        AppleItemListBox appleItemListBox =
                (AppleItemListBox) IsoFileConvenienceHelper.get(isoFile, "moov/udta/meta/ilst");
        AppleAlbumBox appleAlbumBox =
                (AppleAlbumBox) IsoFileConvenienceHelper.get(appleItemListBox, "\u00a9alb");
        if (appleAlbumBox != null) {
            appleItemListBox.removeBox(appleAlbumBox);
        }
        AppleAlbumBox albumBox = new AppleAlbumBox();
        albumBox.setValue("my album");
        appleItemListBox.addBox(albumBox);
        System.err.println(isoFile);

        isoFile.getBox(new IsoOutputStream(new ByteArrayOutputStream()));
    }
}
