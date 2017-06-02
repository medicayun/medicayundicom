/*
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
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
 
package org.dcm4che.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since Jun 19, 2003
 * @version $Revision: 3829 $ $Date: 2004-12-01 02:38:25 +0800 (周三, 01 12月 2004) $
 *
 */
public class AssociationRequestor {

	private static final AssociationFactory assocFact =
		AssociationFactory.getInstance();
	private static final DcmObjectFactory dcmFact =
		DcmObjectFactory.getInstance();
	private static final int ECHO_PCID = 1;
	static final String CONNECTED = "Connected";
	
	private final PropertyChangeSupport pcs =
		new PropertyChangeSupport(this);

			
	private String host = "localhost";
	private int port = 104;
	private int acTimeout = 5000;
	private int dimseTimeout = 0;
	private int soCloseDelay = 500;
	private boolean packPDVs = false;

	private AAssociateRQ aarq =
		assocFact.newAAssociateRQ();
	private DcmServiceRegistry services =
		assocFact.newDcmServiceRegistry();
	
	private ActiveAssociation active;

	public AssociationRequestor() {
		aarq.addPresContext(assocFact.newPresContext(ECHO_PCID, UIDs.Verification));
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener( listener);
	} 

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener( listener);
	} 

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
    
    public boolean bindService(String uid, DcmService service) {
        return services.bind(uid, service);
    }
	
    public boolean unbindService(String uid) {
        return services.unbind(uid);
    }
    
	/**
	 * @return
	 */
	public int getMaxPDULength() {
		return aarq.getMaxPDULength();
	}

	/**
	 * @param maxPduLength
	 */
	public void setMaxPDULength(int maxLength) {
		aarq.setMaxPDULength(maxLength);
	}

	/**
	 * @return
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return
	 */
	public String getCalledAET() {
		return aarq.getCalledAET();
	}

	/**
	 * @param calledAET
	 */
	public void setCalledAET(String calledAET) {
		aarq.setCalledAET(calledAET);
	}

	/**
	 * @return
	 */
	public String getCallingAET() {
		return aarq.getCallingAET();
	}

	/**
	 * @param callingAET
	 */
	public void setCallingAET(String callingAET) {
		aarq.setCallingAET(callingAET);
	}

	/**
	 * @return
	 */
	public int getAcTimeout() {
		return acTimeout;
	}

	/**
	 * @param acTimeout
	 */
	public void setAcTimeout(int acTimeout) {
		this.acTimeout = acTimeout;
	}

	/**
	 * @return
	 */
	public int getDimseTimeout() {
		return dimseTimeout;
	}

	/**
	 * @param dimseTimeout
	 */
	public void setDimseTimeout(int dimseTimeout) {
		this.dimseTimeout = dimseTimeout;
	}

	/**
	 * @return
	 */
	public boolean isPackPDVs() {
		return packPDVs;
	}

	/**
	 * @param packPDVs
	 */
	public void setPackPDVs(boolean packPDVs) {
		this.packPDVs = packPDVs;
	}

	/**
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return
	 */
	public int getSoCloseDelay() {
		return soCloseDelay;
	}

	/**
	 * @param soCloseDelay
	 */
	public void setSoCloseDelay(int soCloseDelay) {
		this.soCloseDelay = soCloseDelay;
	}
	
	public boolean isConnected() {
		return active != null;
	}
	
	public int addPresContext(String asuid, String[] tsuids) {
		int pcid = aarq.nextPCID();
		aarq.addPresContext(assocFact.newPresContext(pcid, asuid, tsuids));
		return pcid;
	}	

	public void removePresContext(int pcid) {
		aarq.removePresContext(pcid);		
	}

	public synchronized void connect() throws UnknownHostException, IOException {
		if (active != null) {
			throw new IllegalStateException(
				"Existing connection: " + active);
		}
		Association tmp = assocFact.newRequestor(newSocket());
		tmp.setAcTimeout(acTimeout);
		tmp.setDimseTimeout(dimseTimeout);
		tmp.setSoCloseDelay(soCloseDelay);
		tmp.setPackPDVs(packPDVs);
		tmp.addAssociationListener(assocListener);
		PDU pdu = tmp.connect(aarq);
		if (!(pdu instanceof AAssociateAC)) {
			throw new IOException("Association rejected: " + pdu);
		}
		active = assocFact.newActiveAssociation(tmp, services);
		pcs.firePropertyChange(CONNECTED, false, true);
		active.start();		
	}
	
	public String getAcceptedTransferSyntaxUID(int pcid) {
		try {
			return active.getAssociation().getAcceptedTransferSyntaxUID(pcid);
		} catch (NullPointerException npe) {
			throw new IllegalStateException("No established Association");
		}
	}
	
	public Dimse invokeAndWaitForRSP(int pcid, Command cmd) throws InterruptedException, IOException {
		return invokeAndWaitForRSP(pcid, cmd, (Dataset) null);
	}
	
	public Dimse invokeAndWaitForRSP(int pcid, Command cmd, Dataset ds) throws InterruptedException, IOException {
		try {
			Dimse rq = assocFact.newDimse(pcid, cmd, ds);
			FutureRSP f = active.invoke(rq);
			return f.get();
		} catch (NullPointerException npe) {
			throw new IllegalStateException("No established Association");
		}
	}
	
	public Dimse invokeAndWaitForRSP(int pcid, Command cmd, DataSource ds) throws InterruptedException, IOException {
		try {
			Dimse rq = assocFact.newDimse(pcid, cmd, ds);
			FutureRSP f = active.invoke(rq);
			return f.get();
		} catch (NullPointerException npe) {
			throw new IllegalStateException("No established Association");
		}
	}
	
	public int nextMsgID() {
		try {
			return active.getAssociation().nextMsgID();
		} catch (NullPointerException npe) {
			throw new IllegalStateException("No established Association");
		}
	}
	
	public void echo() throws InterruptedException, IOException {
		Command cmd = dcmFact.newCommand().initCEchoRQ(nextMsgID());
		invokeAndWaitForRSP(ECHO_PCID, cmd);
	} 
	
	public synchronized void release() throws InterruptedException, IOException {
		if (active != null) {
			active.release(true);
		}
	}

	/**
	 * @return
	 */
	private Socket newSocket() throws UnknownHostException, IOException {
		return new Socket(host, port);
	}
	
	private final AssociationListener assocListener =
		new AssociationListener() {

			public void write(Association src, PDU pdu) {
				
			}

			public void received(Association src, PDU pdu) {
				
			}

			public void write(Association src, Dimse dimse) {
				
			}

			public void received(Association src, Dimse dimse) {
				
			}

			public void error(Association src, IOException ioe) {
				
			}

			public void closing(Association src) {
			    
			}
			
			public void closed(Association src) {
				pcs.firePropertyChange(CONNECTED, true, false);
				active = null;				
			}
		};
}
