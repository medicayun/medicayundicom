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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package org.dcm4cheri.server;

import org.dcm4che.server.UDPServer;
import org.dcm4cheri.util.LF_ThreadPool;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-06 00:26:16 +0800 (周四, 06 10月 2005) $
 */
public class UDPServerImpl implements LF_ThreadPool.Handler, UDPServer
{
    private static int instCount = 0;
    private final String name = "UDPServer-" + ++instCount;
    private final Handler handler;
    private static final Logger log = Logger.getLogger(UDPServerImpl.class);
    private LF_ThreadPool threadPool = new LF_ThreadPool(this, name);
    private DatagramSocket ss;
    private int port;

    public UDPServerImpl(Handler handler)
    {
	if (handler==null)
	    throw new NullPointerException();
	this.handler = handler;
    }

    public void setMaxClients(int max)
    {
	threadPool.setMaxRunning(max);
    }

    public int getMaxClients()
    {
	return threadPool.getMaxRunning();
    }

    public int getNumClients()
    {
	return threadPool.running()-1;
    }

    public void setMaxIdleThreads(int max)
    {
	threadPool.setMaxWaiting(max);
    }

    public int getMaxIdleThreads()
    {
	return threadPool.getMaxWaiting();
    }

    public int getNumIdleThreads()
    {
	return threadPool.waiting();
    }
    /**
     * @deprecated use {@link #setPort}, {@link #start()} 
     */
    public void start(int port)
	throws SocketException, IOException
    {
	setPort(port);
	start();
    }

    public void start()
	throws SocketException, IOException
    {
	checkNotRunning();
	if (log.isInfoEnabled())
	    log.info("Start Server listening at port " + port);
	InetAddress iaddr = InetAddress.getByAddress(new byte[] {0,0,0,0});
	ss = new DatagramSocket(port,iaddr);
	ss.setSoTimeout(0);
	new Thread(new Runnable() {
		public void run() {
		    threadPool.join();
		}
	    }, name).start();
    }

    public void stop()
    {
	if (ss == null)
	    return;
	InetAddress iaddr = ss.getInetAddress();
	int port = ss.getLocalPort();
	if (log.isInfoEnabled())
	    log.info("Stop Server listening at port " + port);
	ss.close();
	ss = null;
	threadPool.shutdown();
	LF_ThreadPool tp = new LF_ThreadPool(this, name);
	tp.setMaxRunning( threadPool.getMaxRunning());
	tp.setMaxWaiting( threadPool.getMaxWaiting());
	threadPool = tp;
    }

    public void run(LF_ThreadPool pool)
    {
	if (ss == null)
	    return;
	final int BufSize = 32768;
	byte[] buff = new byte[BufSize];
	DatagramPacket dp = new DatagramPacket(buff,buff.length);
	try {
	    ss.receive(dp);
	    if (log.isInfoEnabled())
		log.info("handling request on " + ss);
	    pool.promoteNewLeader();
	    handler.handle(dp);
	}
	catch(IOException ioe) {
	    log.error(ioe);
	}
	if (log.isInfoEnabled())
	    log.info("finished - " + ss);
    }

    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort()
    {
	return port;
    }

    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port)
    {
	this.port = port;
    }

    private void checkNotRunning() {
        if (ss != null) {
            throw new IllegalStateException("Already Running - " + threadPool);
        }
    }
}

