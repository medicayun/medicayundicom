/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "SimpleServer.java".  Description:
 * "A simple TCP/IP-based HL7 server."
 *
 * The Initial Developer of the Original Code is University Health Network. Copyright (C)
 * 2002.  All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * GNU General Public License (the  “GPL”), in which case the provisions of the GPL are
 * applicable instead of those above.  If you wish to allow use of your version of this
 * file only under the terms of the GPL and not to allow others to use your version
 * of this file under the MPL, indicate your decision by deleting  the provisions above
 * and replace  them with the notice and other provisions required by the GPL License.
 * If you do not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the GPL.
 */

package org.dcm4chex.archive.hl7;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.GenericParser;

/**
 * <p>A simple TCP/IP-based HL7 server.  This server listens for connections
 * on a particular port, and creates a ConnectionManager for each incoming
 * connection.  </p>
 * <p>A single SimpleServer can only service requests that use a
 * single class of LowerLayerProtocol (specified at construction time).</p>
 * <p>The ConnectionManager uses a PipeParser of the version specified in
 * the constructor</p>
 * <p>ConnectionManagers currently only support original mode processing.</p>
 * <p>The ConnectionManager routes messages to various "Application"s based on
 * message type.  From the HL7 perspective, an Application is something that
 * does something with a message.</p>
 * @author  Bryan Tripp
 */
class HL7Server extends HL7Service {

    private final org.jboss.logging.Logger log;
    
    private int port = 2300;
    private ServerSocketFactory ssf = ServerSocketFactory.getDefault();

    /**
     * Creates a new instance of SimpleServer that listens
     * on the given port.  Exceptions are logged using ca.uhn.hl7v2.Log;
     */
    public HL7Server(org.jboss.logging.Logger log) {
        super(new GenericParser(), LowerLayerProtocol.makeLLP());
        this.log = log;
    }

    /**
     * Loop that waits for a connection and starts a ConnectionManager
     * when it gets one.
     */
    public void run() {
        try {
            ServerSocket ss = ssf.createServerSocket(port);
//            ss.setSoTimeout(3000);
            log.info("SimpleServer running on port " + ss.getLocalPort());
            while (keepRunning()) {
                try {
                    Socket newSocket = ss.accept();
                    log.info("Accepted connection from " + newSocket.getInetAddress().getHostAddress());
                    Connection conn = new Connection(parser, this.llp, newSocket);
                    newConnection(conn);
                }
                catch (InterruptedIOException ie) {
                    //ignore - just timed out waiting for connection
                }
                catch (Exception e) {
                    log.error( "Error while accepting connections: ", e);
                }
            }

            ss.close();
        }
        catch (Exception e) {
            log.error(e);
        }
    }
    /**
     * @return Returns the port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * @param port The port to set.
     */
    public final void setPort(int port) {
        this.port = port;
    }

    /**
     * @return Returns the ssf.
     */
    public final ServerSocketFactory getServerSocketFactory() {
        return ssf;
    }

    /**
     * @param ssf The ssf to set.
     */
    public final void setServerSocketFactory(ServerSocketFactory ssf) {
        this.ssf = ssf;
    }

}
