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
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackMetaData;
import com.coremedia.iso.boxes.rtp.RtpHintSampleEntry;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;
import com.coremedia.iso.mdta.Sample;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Shows a specific sample. Can parse and show details of a <code>HintSampleEntry</code>.
 *
 * @see com.coremedia.iso.boxes.sampleentry.AudioSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.VisualSampleEntry
 * @see com.coremedia.iso.boxes.rtp.RtpHintSampleEntry
 * @see com.coremedia.iso.boxes.sampleentry.TextSampleEntry
 */
public class GenericSamplePane extends JLabel {
  public GenericSamplePane(Sample<?> sample) {
    Font font = new Font("Courier New", Font.PLAIN, 12);
    setFont(font);
    setVerticalAlignment(JLabel.TOP);

    TrackMetaData<?> trackMetaData = sample.getParent().getParentTrack().getTrackMetaData();
    SampleDescriptionBox sampleDescriptionBox = trackMetaData.getSampleDescriptionBox();


    if (sampleDescriptionBox != null) {
      java.util.List<SampleEntry> sampleEntries = sampleDescriptionBox.getBoxes(SampleEntry.class, false);


      if (sampleEntries.size() > 0 && (sampleEntries.get(0) instanceof RtpHintSampleEntry)) {
        setText(createHintSampleUI(sample));
      } else if (sampleEntries.size() > 0 &&
              (sampleEntries.get(0) instanceof AudioSampleEntry) &&
              Arrays.equals(sampleEntries.get(0).getType(), IsoFile.fourCCtoBytes(AudioSampleEntry.TYPE1))) {
        setText(createSamrSampleUI(sample));
      }
    } else {
      setText("no SampleDescriptionBox found");
    }

      String description = sample.getDescription();
      if (description != null) {
          setText(description);
      }

    setFont(font);
    setVerticalAlignment(JLabel.TOP);
  }

  protected static void makeNameValueRow(StringBuffer buffer, String name, String value) {
    buffer.append("<tr><td><b>").append(name).append("</b></td><td>").append(value).append("</td></tr>");
  }

  public static int makeInteger(byte msb, byte lsb) {
    int rv = 0;
    rv += (msb < 0 ? msb + 256 : msb) << 8;
    rv += (lsb < 0 ? lsb + 256 : lsb);
    return rv;
  }

  private static String createSamrSampleUI(Sample<?> sample) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<html><h1>");
    buffer.append("SAMR Sample");
    buffer.append("</h1>");
    buffer.append("<table>");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      sample.getContent(new IsoOutputStream(baos));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    byte[] smple = baos.toByteArray();
    int frameType = (smple[0] & 0xf0) >> 4;
    makeNameValueRow(buffer, "frameType", Integer.toString(frameType));
    int frameQualityIndicator = (smple[0] & 0x08) >> 3;
    makeNameValueRow(buffer, "frameQualityIndicator", Integer.toString(frameQualityIndicator));
    int modeIndicator = (smple[0] & 0x07);
    makeNameValueRow(buffer, "modeIndicator", Integer.toString(modeIndicator));
    int modeRequest = (smple[1] & 0xE0) >> 5;
    makeNameValueRow(buffer, "modeRequest", Integer.toString(modeRequest));
    int codecCRC = ((smple[1] & 0x1F) << 3) + ((smple[2] & 0xE0) >> 5);
    makeNameValueRow(buffer, "codecCRC", Integer.toString(codecCRC));
    buffer.append("</table></html>");
    return buffer.toString();
  }

  private static String createHintSampleUI(Sample<?> sample) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<html><h1>");
    buffer.append("RTP Hint Sample");
    buffer.append("</h1>");
    buffer.append("<table>");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      sample.getContent(new IsoOutputStream(baos));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    byte[] smple = baos.toByteArray();
    int offset = 0;
    int packetcount = makeInteger(smple[offset++], smple[offset++]);
    makeNameValueRow(buffer, "packet count", Integer.toString(packetcount));
    makeNameValueRow(buffer, "reserved", Integer.toString(makeInteger(smple[offset++], smple[offset++])));
    buffer.append("</table> <hr> ");
    for (int i = 0; i < packetcount; i++) {
      buffer.append("<h2>RTP Packet</h2>");
      buffer.append("<table>");
      makeNameValueRow(buffer, "relative time", Integer.toString(
              (makeInteger(smple[offset++], smple[offset++]) << 16) + makeInteger(smple[offset++], smple[offset++])));
      int pAndX = smple[offset++];
      makeNameValueRow(buffer, "P_bit / X_bit", "" + (pAndX & 1 << 6) + " / " + ((pAndX & 1 << 5) >> 5));
      int mAndPayload = smple[offset++];
      makeNameValueRow(buffer, "M_bit / payload", "" + ((mAndPayload & 128) >> 7) + " / " + (mAndPayload & 127));
      int rtpSequenceSeed = makeInteger(smple[offset++], smple[offset++]);
      makeNameValueRow(buffer, "RTP Sequence Seed", "" + rtpSequenceSeed);
      offset++; // 8 reserved bits
      int flags = smple[offset++];
      makeNameValueRow(buffer, "extra / bframe / repeat flag", "" + ((flags & 4) >> 2) + " / " + ((flags & 2) >> 1) + " / " + (flags & 1) + "");
      int entrycount = makeInteger(smple[offset++], smple[offset++]);
      makeNameValueRow(buffer, "entry count", "" + entrycount);

      if (((flags & 4) >> 2) == 1) {
        int extralength = (makeInteger(smple[offset], smple[offset + 1]) >> 16) + makeInteger(smple[offset + 2], smple[offset + 3]);
        makeNameValueRow(buffer, "extrainformationlength",
                "" + extralength);
        offset += extralength;
      }
      for (int j = 0; j < entrycount; j++) {
        int type = smple[offset];
        String typeString;
        switch (type) {
          case 0:
            typeString = "RtpNoOpConstructor";
            break;
          case 1:
            typeString = "RtpImmediateConstructor";
            break;
          case 2:
            typeString = "RtpSampleConstructor";
            break;
          case 3:
            typeString = "RtpSampleDescriptionConstructor";
            break;
          default:
            typeString = "unknownConstructor";

        }
        buffer.append("<tr><td colspan='2'><table><tr><td colspan='2'> <h3>").append(typeString).append("</h3> </td></tr>");
        makeNameValueRow(buffer, "type", Integer.toString(type));
        switch (type) {
          case 0:
            makeNameValueRow(buffer, "just padding", "no content");
            break;
          case 1:
            int count = smple[offset + 1];
            String value = "0x";
            for (int k = 0; k < count; k++) {
              value += smple[offset + 1 + k] < 16 ? " 0" : " ";
              value += Integer.toHexString(smple[offset + 1 + k] < 0 ? smple[offset + 1 + k] + 256 : smple[offset + 1 + k]).toUpperCase();
            }
            makeNameValueRow(buffer, "count", Integer.toString(count));
            makeNameValueRow(buffer, "data", value);
            break;
          case 2:
            makeNameValueRow(buffer, "trackRefIndex", Byte.toString(smple[offset + 1]));
            makeNameValueRow(buffer, "length", Integer.toString(makeInteger(smple[offset + 2], smple[offset + 3])));
            makeNameValueRow(buffer, "sampleNumber", Integer.toString(
                    (makeInteger(smple[offset + 4], smple[offset + 5]) >> 16) + makeInteger(smple[offset + 6], smple[offset + 7])));
            makeNameValueRow(buffer, "sampleOffset", Integer.toString(
                    (makeInteger(smple[offset + 8], smple[offset + 9]) >> 16) + makeInteger(smple[offset + 10], smple[offset + 11])));
            break;
          case 3:
            makeNameValueRow(buffer, "trackRefIndex", Byte.toString(smple[offset + 1]));
            makeNameValueRow(buffer, "length", Integer.toString(makeInteger(smple[offset + 2], smple[offset + 3])));
            makeNameValueRow(buffer, "sampleDescriptionIndex", Integer.toString(
                    (makeInteger(smple[offset + 4], smple[offset + 5]) >> 16) + makeInteger(smple[offset + 6], smple[offset + 7])));
            makeNameValueRow(buffer, "sampleDescriptionOffset", Integer.toString(
                    (makeInteger(smple[offset + 8], smple[offset + 9]) >> 16) + makeInteger(smple[offset + 10], smple[offset + 11])));
            break;
          default:
            break;
        }

        offset += 16;
        buffer.append("</table></td></tr>");
      }


      buffer.append("</table><hr>");

    }
    return buffer.toString();
  }
}
