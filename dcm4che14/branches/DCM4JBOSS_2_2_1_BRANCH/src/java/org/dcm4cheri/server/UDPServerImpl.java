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
 * @version $Revision: 3831 $ $Date: 2004-12-01 17:37:55 +0800 (周三, 01 12月 2004) $
 */
public class UDPServerImpl implements LF_ThreadPool.Handler, UDPServer
{
    private static int instCount = 0;
    private final String name = "UDPServer-" + ++instCount;
    private final Handler handler;
    private static final Logger log = Logger.getLogger(UDPServerImpl.class);
    private final LF_ThreadPool threadPool = new LF_ThreadPool(this, name);
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

