package com.google.code.mp4parser.example;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.apple.AppleAlbumBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Parsing and navigating to a specific box.
 * Removing a Box
 * Adding a Box.
 */
public class AddItunesAlbumAttributes_1 {
    public static void main(String[] args) throws IOException {
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapperImpl(new File(args[0]));
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
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
