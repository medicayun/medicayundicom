/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.jboss.system.server.ServerConfigLocator;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1922 $ $Date: 2005-08-30 16:57:37 +0800 (周二, 30 8月 2005) $
 * @since 19.09.2004
 *
 */
public class FileUtils {

    private static final int BUFFER_SIZE = 512;
    
	public static final long MEGA = 1000000L;

    public static final long GIGA = 1000000000L;

    private static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static String toHex(int val) {
	    char[] ch8 = new char[8];
	    for (int i = 8; --i >= 0; val >>= 4) {
	        ch8[i] = HEX_DIGIT[val & 0xf];
	    }
	    return String.valueOf(ch8);
	}

    public static File createNewFile(File dir, int hash) throws IOException {
		File f;
		do {
			f = new File(dir, toHex(hash++));
		} while (!f.createNewFile());
		return f;
    }
	
    public static String slashify(File f) {
        return f.getPath().replace(File.separatorChar, '/');
    }
    
    public static File resolve(File f) {
        if (f.isAbsolute()) return f;
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath());
    }
    
    public static File toFile(String unixPath) {
        return resolve(new File(unixPath.replace('/', File.separatorChar)));
    }

    public static File toFile(String unixDirPath, String unixFilePath) {
        return resolve(new File(unixDirPath.replace('/', File.separatorChar),
                unixFilePath.replace('/', File.separatorChar)));
    }
    
    public static String formatSize(long size) {
        if (size < GIGA)
            return ((float) size / MEGA) + "MB";
        else
            return ((float) size / GIGA) + "GB";
    }

    public static long parseSize(String s, long minSize) {
        long u;
        if (s.endsWith("GB"))
            u = GIGA;
        else if (s.endsWith("MB"))
            u = MEGA;
        else
            throw new IllegalArgumentException(s);
        try {
            long size = (long) (Float.parseFloat(s.substring(0, s.length() - 2)) * u);
            if (size >= minSize)
                return size;
        } catch (IllegalArgumentException e) {
        }
        throw new IllegalArgumentException(s);
    }
    
    public static boolean equalsPixelData(File f1, File f2)
    		throws IOException {
    	InputStream in1 = new BufferedInputStream(new FileInputStream(f1));
    	try {
    		InputStream in2 = new BufferedInputStream(new FileInputStream(f2));
    		try {    			
                Dataset attrs = DcmObjectFactory.getInstance().newDataset();
    	    	DcmParserFactory pf = DcmParserFactory.getInstance();
				DcmParser p1 = pf.newDcmParser(in1);
    	    	DcmParser p2 = pf.newDcmParser(in2);
                p1.setDcmHandler(attrs.getDcmHandler());
    			p1.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
    			p2.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
                int bitsAlloc = attrs.getInt(Tags.BitsAllocated, 8);
                int bitsStored = attrs.getInt(Tags.BitsStored, bitsAlloc);
    			int totLen = p1.getReadLength();
    			if (totLen < 0 || totLen != p2.getReadLength()) {
    				return false;
    			}
    			byte[] b1 = new byte[BUFFER_SIZE];
    			byte[] b2 = new byte[BUFFER_SIZE];
                int[] mask = { 0xff, 0xff };
                int len, len2;
                if (bitsAlloc == 16 && bitsStored < 16) {
                    mask[p1.getDcmDecodeParam().byteOrder == ByteOrder.LITTLE_ENDIAN ? 1 : 0]
                            = 0xff >>> (16 - bitsStored);
                } 
                int pos = 0;
                while (pos < totLen) {
                    len = in1.read(b1, 0, Math.min(totLen - pos, BUFFER_SIZE));
                    if (len < 0) // EOF
                        return false;
                    int off = 0;
                    while (off < len) {
                        off += len2 = in2.read(b2, off, len - off);
                        if (len2 < 0) // EOF
                            return false;
                    }
                    for (int i=0; i<len; i++, pos++)
                        if (((b1[i] - b2[i]) & mask[pos & 1]) != 0)
                            return false;
                }
                return true;
    		} finally {
    			in2.close();
    		}
    	} finally {
    		in1.close();
    	}
    }
}
