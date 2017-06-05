/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (c) 2013 by AGFA HealthCare
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.hsm.module.uri;

import java.io.File;
import java.net.URISyntaxException;

public class Uri {
    private final class SCHEMES {
        public final static int UNKNOWN = 0;

        // Local
        @SuppressWarnings("unused")
        public final static int FILE = 1;

        // SMB/CIFS
        public final static int CIFS = 2;

        // SSH/SCP
        public final static int SSH = 3;

        // SFTP
        public final static int SFTP = 4;

        // RSH/RCMD/RCP
        public final static int RSH = 5;
    }

    public static int isUri(String uri) {
        if (uri.startsWith("ssh://") || uri.startsWith("scp://"))
            return SCHEMES.SSH;
        if (uri.startsWith("sftp://"))
            return SCHEMES.SFTP;
        if (uri.startsWith("rsh://") || uri.startsWith("rcp://"))
            return SCHEMES.RSH;
        if (uri.startsWith("smb://"))
            return SCHEMES.CIFS;
        // if (uri.startsWith("file://"))
        // return Schemes.File;
        return SCHEMES.UNKNOWN;
    }

    protected static void copyFrom(String src, String dst, String identity) throws Exception {
        switch (isUri(src)) {
        case SCHEMES.SSH:
            Ssh.scpCopyFrom(src, dst, identity);
            break;
        case SCHEMES.SFTP:
            Ssh.sftpCopyFrom(src, dst, identity);
            break;
        case SCHEMES.CIFS:
            Cifs.copyFrom(src, dst);
            break;
        default:
            throw new URISyntaxException("Unknown URI:", src);
        }
    }

    protected static void copyTo(String src, String dstUrl, String dst, String identity) throws Exception {
        switch (isUri(dstUrl)) {
        case SCHEMES.SSH:
            Ssh.scpCopyTo(src, dstUrl, dst, identity);
            break;
        case SCHEMES.SFTP:
            Ssh.sftpCopyTo(src, dstUrl, dst, identity);
            break;
        case SCHEMES.CIFS:
            Cifs.copyTo(src, dstUrl + "/" + dst);
            break;
        default:
            throw new URISyntaxException("Unknown URI:", dstUrl);
        }
    }

    protected static void exec(String cmdUri, String identity) throws Exception {
        switch (isUri(cmdUri)) {
        case SCHEMES.SSH:
            Ssh.exec(cmdUri, identity);
            break;
        default:
            throw new URISyntaxException("Unknown URI:", cmdUri);
        }
    }

    public static long exists(String uri, String identity) throws Exception {
        switch (isUri(uri)) {
        case SCHEMES.SSH:
            return Ssh.scpFileLength(uri, identity);
        case SCHEMES.SFTP:
            return Ssh.sftpFileLength(uri, identity);
        case SCHEMES.CIFS:
            return Cifs.fileLength(uri);
        default:
            if (new File(uri).exists()) {
                return new File(uri).length();
            }
        }
        return -1;
    }
}