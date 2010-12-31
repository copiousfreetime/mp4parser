package com.google.code.mp4parser.example;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.PropertyBoxParserImpl;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.UserDataBox;
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
        properties.load(AddItunesAlbumAttributes_2.class.getResourceAsStream("/com/google/code/mp4parser/example/just_a_few.properties"));
        PropertyBoxParserImpl boxParser = new PropertyBoxParserImpl(properties);
        IsoFile isoFile = new IsoFile(isoBufferWrapper, boxParser);
        isoFile.parse();
        MovieBox movieBox = isoFile.getBoxes(MovieBox.class)[0];
        UserDataBox userDataBox = movieBox.getBoxes(UserDataBox.class)[0];
        MetaBox metaBox = userDataBox.getBoxes(MetaBox.class)[0];
        AppleItemListBox appleItemListBox = metaBox.getBoxes(AppleItemListBox.class)[0];
        AppleAlbumBox[] appleAlbumBoxes = appleItemListBox.getBoxes(AppleAlbumBox.class);
        for (AppleAlbumBox appleAlbumBox : appleAlbumBoxes) {
            appleItemListBox.removeBox(appleAlbumBox);
        }
        AppleAlbumBox albumBox = new AppleAlbumBox();
        albumBox.setValue("my album");
        appleItemListBox.addBox(albumBox);
        System.err.println(isoFile);

        isoFile.getBox(new IsoOutputStream(new ByteArrayOutputStream()));
    }
}
