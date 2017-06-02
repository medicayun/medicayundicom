/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.arr.seam.ejb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Apr 4, 2008
 */
public class DicomUtils {

    public static void format(InputStream in, String tsuid, int maxWidth,
            int maxValLen, StringBuffer out) throws IOException {
        DicomInputStream din = new DicomInputStream(in, tsuid);
        din.setHandler(new FormatDicomInputHandler(maxWidth, maxValLen, out));
        din.readDicomObject();
    }

    public static String format(byte[] in, String tsuid, int maxWidth,
            int maxValLen) throws IOException {
        StringBuffer out = new StringBuffer(512);
        format(new ByteArrayInputStream(in), tsuid, maxWidth, maxValLen, out);
        return out.toString();
    }

    static class FormatDicomInputHandler implements DicomInputHandler {

        private static final String LINE_SEPARATOR = 
                System.getProperty("line.separator", "\n");
        private final StringBuffer out;
        private final int maxWidth;
        private final int maxValLen;
        private final char[] cbuf = new char[64];
        private int maxEnd;

        public FormatDicomInputHandler(int maxWidth, int maxValLen, StringBuffer out) {
            this.maxWidth = maxWidth;
            this.maxValLen = maxValLen;
            this.out = out;
        }

        public boolean readValue(DicomInputStream in) throws IOException {
            switch (in.tag()) {
            case Tag.Item:
                if (in.sq().vr() != VR.SQ && in.valueLength() != -1) {
                    formatFragment(in);
                } else {
                    formatItem(in);
                }
                break;
            case Tag.ItemDelimitationItem:
            case Tag.SequenceDelimitationItem: 
                if (in.level() > 0)
                    formatItem(in);
                break;
            default:
                formatElement(in);
            }
            return true;
        }

        private void formatElement(DicomInputStream in) throws IOException {
            appendTag(in);
            appendVR(in);
            appendLen(in);
            if (hasItems(in)) {
                appendName(in);
                readItems(in);
            } else {
                appendValue(in);
                appendName(in);
            }
        }

        private void appendValue(DicomInputStream in) throws IOException {
            int tag = in.tag();
            VR vr = in.vr();
            byte[] val = in.readBytes(in.valueLength());
            DicomObject dcmobj = in.getDicomObject();
            boolean bigEndian = in.getTransferSyntax().bigEndian();
            out.append(" [");
            vr.promptValue(val, bigEndian, dcmobj.getSpecificCharacterSet(),
                    cbuf, maxValLen, out);
            out.append("]");
            if (tag == Tag.SpecificCharacterSet
                    || tag == Tag.TransferSyntaxUID
                    || TagUtils.isPrivateCreatorDataElement(tag)) {
                dcmobj.putBytes(tag, vr, val, bigEndian);
            }
            if (tag == 0x00020000) {
                in.setEndOfFileMetaInfoPosition(
                        in.getStreamPosition() + vr.toInt(val, bigEndian));
            }
        }

        private boolean hasItems(DicomInputStream in) {
            return in.valueLength() == -1 || in.vr() == VR.SQ;
        }

        private void readItems(DicomInputStream in) throws IOException {
            in.readValue(in);
            in.getDicomObject().remove(in.tag());
        }

        private void formatItem(DicomInputStream in) throws IOException {
            appendTag(in);
            appendLen(in);
            appendName(in);
            in.readValue(in);
        }

        private void formatFragment(DicomInputStream in) throws IOException {
            appendTag(in);
            appendLen(in);
            in.readValue(in);
            DicomElement sq = in.sq();
            byte[] data = sq.removeFragment(0);
            boolean bigEndian = in.getTransferSyntax().bigEndian();
            out.append(" [");
            sq.vr().promptValue(data, bigEndian, null, cbuf, maxValLen, out);
            out.append("]");
            appendName(in);
        }

        private void appendTag(DicomInputStream in) {
            maxEnd = out.length() + maxWidth;
            for (int i = in.level(); i > 0; --i)
                out.append('>');
            TagUtils.toStringBuffer(in.tag(), out);
        }

        private void appendVR(DicomInputStream in) {
            out.append(" ").append(in.vr());
        }

        private void appendLen(DicomInputStream in) {
            out.append(" #").append(in.valueLength());
        }

        private void appendName(DicomInputStream in) {
            out.append(" ").append(in.getDicomObject().nameOf(in.tag()));
            if (out.length() > maxEnd) {
                out.setLength(maxEnd);
            }
            out.append(LINE_SEPARATOR);
        }
    }

}
