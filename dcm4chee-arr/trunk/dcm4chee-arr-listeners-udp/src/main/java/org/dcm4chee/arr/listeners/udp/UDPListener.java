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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.arr.listeners.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.dcm4chee.arr.listeners.common.AbstractSyslogListener;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 24, 2007
 */
public class UDPListener extends AbstractSyslogListener {
    
    private static final int MAX_PACKAGE_SIZE = 65507;

    private int maxMsgSize = MAX_PACKAGE_SIZE;
    private DatagramSocket socket;
    private Thread thread;

    public final int getMaxMessageSize() {
        return maxMsgSize;
    }

    public void setMaxMessageSize(int maxMessageSize)  {
        if (maxMessageSize < 512 || maxMessageSize > MAX_PACKAGE_SIZE) {
            throw new IllegalArgumentException("maxMessageSize: " 
                    + maxMessageSize + " not in range 512..65507");
        }
        this.maxMsgSize = maxMessageSize;
    }

    protected void startServer() throws SocketException {
        if (socket != null) {
            stopServer();
        }
        socket = new DatagramSocket(port, laddr);
        int prevRcvBuf = socket.getReceiveBufferSize();
        if (rcvBuf == 0) {
            rcvBuf = prevRcvBuf;
        } else if (rcvBuf != prevRcvBuf) {
            socket.setReceiveBufferSize(rcvBuf);
            rcvBuf = socket.getReceiveBufferSize();
        }
        thread = new Thread(new Runnable() {
            public void run() {
                lastStartedAt = System.currentTimeMillis();
                SocketAddress lsa = socket.getLocalSocketAddress();
                log.info("Started UDP Server listening on " + lsa);
                byte[] data = new byte[maxMsgSize];
                DatagramPacket p = new DatagramPacket(data, data.length);
                boolean restart = false;
                while (socket != null && !socket.isClosed()) {
                    try {
                        socket.receive(p);
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            log.warn("UDP Server throws i/o exception - restart", e);
                            restart = true;
                        }
                        break;
                    }
                    onMessage(p.getData(), p.getOffset(), p.getLength(), 
                            p.getAddress());
                    p.setLength(data.length);
                }
                socket = null;
                thread = null;
                lastStoppedAt = System.currentTimeMillis();
                log.info("Stopped UDP Server listening on " + lsa);
                if (restart) {
                    try {
                        startServer();
                    } catch (SocketException e) {
                        log.error("Failed to restart UDP Server", e);
                    }
                }
            }
        });
        thread.start();
    }    

    protected void stopServer() {
        if (socket != null) {
            socket.close();
            try { thread.join(); } catch (Exception ignore) {}
            socket = null;
        }
    }
}
