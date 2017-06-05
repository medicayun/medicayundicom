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
 * @version $Revision: 1237 $ $Date: 2004-11-30 06:28:36 +0800 (周二, 30 11月 2004) $
 * @since 19.09.2004
 *
 */
public class FileUtils {
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
}
