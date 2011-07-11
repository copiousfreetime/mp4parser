/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;

import javax.swing.*;
import java.io.File;

/**
 * Starts the ISO Viewer.<p>
 * <p/>
 * A possible command line argument is the file to show.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();

        }
        IsoViewerFrame frame = new IsoViewerFrame(IsoFile.class);

        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {
                frame.open(file);
            }
        }

        frame.pack();
        frame.setSize(1024, 600);
        frame.setVisible(true);
    }
}
