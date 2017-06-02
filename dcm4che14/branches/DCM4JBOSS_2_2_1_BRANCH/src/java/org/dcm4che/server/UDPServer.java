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

package org.dcm4che.server;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3831 $ $Date: 2004-12-01 17:37:55 +0800 (周三, 01 12月 2004) $
 */
public interface UDPServer {
    public interface Handler
    {
        void handle(DatagramPacket datagram) throws IOException;
    }
    
    void setMaxClients(int max);
    
    int getMaxClients();
    
    int getNumClients();

    void setMaxIdleThreads(int max);
    
    int getMaxIdleThreads();
    
    int getNumIdleThreads();
    
    /**
     * @deprecated use {@link #setPort}, {@link #start()} 
     */    
    void start(int port) throws IOException;
    
    void start() throws Exception;
    
    void stop();
    
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort();
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port);
}
