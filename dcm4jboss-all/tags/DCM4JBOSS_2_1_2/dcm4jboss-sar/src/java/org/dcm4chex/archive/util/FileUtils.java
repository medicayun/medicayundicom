/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.util;

import java.io.File;

import org.jboss.system.server.ServerConfigLocator;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1438 $ $Date: 2005-01-28 09:56:27 +0800 (周五, 28 1月 2005) $
 * @since 19.09.2004
 *
 */
public class FileUtils {

    public static final long MEGA = 1000000L;

    public static final long GIGA = 1000000000L;

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
}
