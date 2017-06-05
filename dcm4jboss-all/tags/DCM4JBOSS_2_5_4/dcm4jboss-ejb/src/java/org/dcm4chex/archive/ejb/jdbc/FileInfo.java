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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.ejb.jdbc;

import java.util.Comparator;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 *  
 */
public class FileInfo {

    public static final Comparator ASC_ORDER = new Comparator() {

        public int compare(Object o1, Object o2) {
            return ((FileInfo) o1).pk - ((FileInfo) o2).pk;
        }
    };

    public static final Comparator DESC_ORDER = new Comparator() {

        public int compare(Object o1, Object o2) {
            return ((FileInfo) o2).pk - ((FileInfo) o1).pk;
        }
    };

    public final int pk;

    public final byte[] patAttrs;

    public final byte[] studyAttrs;

    public final byte[] seriesAttrs;

    public final byte[] instAttrs;

    public final String patID;

    public final String patName;

    public final String studyIUID;

    public final String sopIUID;

    public final String sopCUID;

    public final String extRetrieveAET;
    
    public final String fileRetrieveAET;

    public final String basedir;

    public final String fileID;

    public final String tsUID;

    public final String md5;

    public final int size;

    public final int status;

    public FileInfo(int pk, String patID, String patName, byte[] patAttrs,
            String studyIUID, byte[] studyAttrs, byte[] seriesAttrs,
            byte[] instAttrs, String sopIUID, String sopCUID,
            String extRetrieveAET, String fileRetrieveAET, String basedir,
            String fileID, String tsUID, String md5, int size, int status) {
        this.pk = pk;
        this.patID = patID;
        this.patName = patName;
        this.patAttrs = patAttrs;
        this.studyIUID = studyIUID;
        this.studyAttrs = studyAttrs;
        this.seriesAttrs = seriesAttrs;
        this.instAttrs = instAttrs;
        this.sopIUID = sopIUID;
        this.sopCUID = sopCUID;
        this.extRetrieveAET = extRetrieveAET;
        this.fileRetrieveAET = fileRetrieveAET;
        this.basedir = basedir;
        this.fileID = fileID;
        this.tsUID = tsUID;
        this.md5 = md5;
        this.size = size;
        this.status = status;
    }

    public String toString() {
        return "FileInfo[pk=" + pk + "iuid=" + sopIUID + ", cuid=" + sopCUID
                + ", extRetrieveAET=" + extRetrieveAET
                + ", fileRetrieveAET=" + fileRetrieveAET + ", basedir="
                + basedir + ", fileid=" + fileID + ", tsuid=" + tsUID;
    }
    
    public byte[] getFileMd5() {
        char[] md5Hex = md5.toCharArray();
        byte[] retval = new byte[16];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = (byte) ((Character.digit(md5Hex[i << 1], 16) << 4) + Character
                    .digit(md5Hex[(i << 1) + 1], 16));
        }
        return retval;
    }
}
