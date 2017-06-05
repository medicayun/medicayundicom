/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
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
