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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.management.ObjectName;
import javax.net.ssl.SSLException;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.interfaces.AEManagerHome;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * <description>
 *
 * @author     <a href="mailto:franz.willer@gwi-ag.com">Franz WIller</a>
 * @since      March 24, 2005
 * @version    $Revision: 2495 $ $Date: 2006-05-26 20:33:15 +0800 (周五, 26 5月 2006) $
 */
public class EchoService extends ServiceMBeanSupport
{
    private static final AssociationFactory aFact =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact =
        DcmObjectFactory.getInstance();
    
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
 
    private String callingAET;
    
	private static final int PCID_ECHO = 1;
    private static final String[] DEF_TS = { UIDs.ImplicitVRLittleEndian };
    

	/**
	 * @return Returns the callingAET.
	 */
	public String getCallingAET() {
		return callingAET;
	}
	/**
	 * @param callingAET The callingAET to set.
	 */
	public void setCallingAET(String callingAET) {
		this.callingAET = callingAET;
	}
    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final int getReceiveBufferSize() {
        return tlsConfig.getReceiveBufferSize();        
    }
    
    public final void setReceiveBufferSize(int size) {
        tlsConfig.setReceiveBufferSize(size);
    }

    public final int getSendBufferSize() {
        return tlsConfig.getSendBufferSize();        
    }
    
    public final void setSendBufferSize(int size) {
        tlsConfig.setSendBufferSize(size);
    }
        
    public final boolean isTcpNoDelay() {
        return tlsConfig.isTcpNoDelay();
    }

    public final void setTcpNoDelay(boolean on) {
        tlsConfig.setTcpNoDelay(on);
    }
        
	public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }
    
    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }
    
    public String[] echoAll() throws RemoteException, Exception {
    	List l = lookupAEManager().getAes();
    	String[] sa = new String[ l.size() ];
    	AEData aeData;
    	for ( int i = 0, len = sa.length; i < len ; i++ ) {
    		aeData = (AEData) l.get(i);
    		try {
    			sa[i] = aeData+" : "+echo( aeData, new Integer(3) );
    		} catch ( Exception x ) {
    			sa[i] = aeData+" failed:"+x.getMessage();
    		}
    	}
    	return sa;
    }

    public String echo( String aet, Integer nrOfTests ) throws Exception {
    	return echo( lookupAEManager().getAeByTitle( aet ), nrOfTests );
    }
    public String echo( AEData aeData, Integer nrOfTests ) throws InterruptedException, IOException, GeneralSecurityException {
	    int count = 0;
	    int len = nrOfTests.intValue();
	    ActiveAssociation active = null;
	    try {
	    	active = openAssoc( aeData );
	    } catch( Throwable t ) {
	    	log.error("Echo "+aeData+" failed!", t);
	    	return "Echo failed ("+aeData+")! Reason: unexpected Exception:"+t;
	    }
	    if (active != null) {
	        if (active.getAssociation().getAcceptedTransferSyntaxUID(PCID_ECHO) == null) {
	            return "Echo ("+aeData+") not accepted!";
	        } else
	        	try {
		            for (int j = 0; j < len; ++j, ++count) {
		                active.invoke(
		                    aFact.newDimse(
		                        PCID_ECHO,
		                        oFact.newCommand().initCEchoRQ(j)),
		                    null);
		            }
	        	} catch ( SSLException x ) {
	        		return "Echo failed: "+x.getMessage();
	        	}
	        active.release(true);
	    }
	    return "Echo ("+aeData+") done! "+count+" of "+nrOfTests+" successfully completed.";
	}
    
    public boolean checkEcho( AEData aeData ) {
    	ActiveAssociation active = null;
    	try {
    		active = openAssoc( aeData );
    		if (active != null && active.getAssociation().getAcceptedTransferSyntaxUID(PCID_ECHO) != null ) {
                active.invoke(
	                    aFact.newDimse(
	                        PCID_ECHO,
	                        oFact.newCommand().initCEchoRQ(0)),
	                    null);
	     		return true;	
    		}
    	} catch( Throwable ignore ) {
    	} finally {
    		if ( active != null ) try {
    			active.release(true);
    		} catch ( Exception ignoreit ) {}
    	}
		return false;
    }
    
    private ActiveAssociation openAssoc(AEData aeData) throws IOException, GeneralSecurityException {
	    Association assoc =
	        aFact.newRequestor( tlsConfig.createSocket(aeData) );
	
	    assoc.setAcTimeout(acTimeout);
	    assoc.setDimseTimeout(dimseTimeout);
	    assoc.setSoCloseDelay(soCloseDelay);
	    AAssociateRQ assocRQ = aFact.newAAssociateRQ();
	    assocRQ.setCallingAET( this.callingAET );
	    assocRQ.setCalledAET( aeData.getTitle() );
        assocRQ.addPresContext(
                aFact.newPresContext(PCID_ECHO, UIDs.Verification, DEF_TS));
	    PDU assocAC = assoc.connect(assocRQ);
	    if (!(assocAC instanceof AAssociateAC)) {
	        return null;
	    }
	    ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
	    retval.start();
	    return retval;
	}

    
	protected AEManager lookupAEManager() throws Exception
	{
		AEManagerHome home =
			(AEManagerHome) EJBHomeFactory.getFactory().lookup(
					AEManagerHome.class,
					AEManagerHome.JNDI_NAME);
		return home.create();
	}			
  
}

