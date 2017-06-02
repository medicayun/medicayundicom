/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.util;

import java.io.IOException;
import javax.net.ssl.SSLSocket;
import java.util.EventObject;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3559 $ $Date: 2002-09-02 05:35:41 +0800 (周一, 02 9月 2002) $
 * @since September 1, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class HandshakeFailedEvent extends EventObject {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private final IOException ex;
    
    // Constructors --------------------------------------------------
    public HandshakeFailedEvent(SSLSocket sock, IOException ex) {
        super(sock);
        this.ex = ex;
    }
    
    // Methods -------------------------------------------------------
    public SSLSocket getSocket() {
        return (SSLSocket) source;
    }
    
    public IOException getException() {
        return ex;
    }
}
