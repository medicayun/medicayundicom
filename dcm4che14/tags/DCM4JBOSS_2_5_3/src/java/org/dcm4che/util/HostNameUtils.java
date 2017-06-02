/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4che.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3678 $ $Date: 2003-07-03 03:55:06 +0800 (周四, 03 7月 2003) $
 * @since July 2, 2003
 */

public class HostNameUtils {

    private static String localHostName = null;
    private static String localHostAddress = null;

    public static String getLocalHostName() {
        if (localHostName == null) {
            init();
        }
        return localHostName;
    }
    
    public static String getLocalHostAddress() {
        if (localHostAddress == null) {
            init();
        }
        return localHostAddress;
    }
    
    private static void init() {
        try {
            InetAddress ia  = InetAddress.getLocalHost();
            localHostName = skipDomain(ia.getHostName());
            localHostAddress = ia.getHostAddress();
        } catch (UnknownHostException e) {
            localHostName = "localhost";
            localHostAddress = "127.0.0.1";
        }
    }
    
    public static String skipDomain(String fqdn) {
        if (Character.isDigit(fqdn.charAt(0))) {
            return fqdn;
        }
        int pos = fqdn.indexOf('.');
        return pos == -1 ? fqdn : fqdn.substring(0, pos);        
    }

    private HostNameUtils() {}
    
}
