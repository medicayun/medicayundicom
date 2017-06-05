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
 * @version $Revision: 1164 $ $Date: 2004-09-20 06:26:37 +0800 (周一, 20 9月 2004) $
 * @since 19.09.2004
 *
 */
public class FileUtils {
    public static File resolve(File f) {
        if (f.isAbsolute()) return f;
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath());
    }

}
