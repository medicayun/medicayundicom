/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
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
 */
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
 * @version    $Revision: 1696 $ $Date: 2005-04-15 18:25:21 +0800 (周五, 15 4月 2005) $
 */
public class EchoService extends ServiceMBeanSupport
{
    private static final AssociationFactory aFact =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact =
        DcmObjectFactory.getInstance();
    
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
    
    private ActiveAssociation openAssoc(AEData aeData) throws IOException, GeneralSecurityException {
	    Association assoc =
	        aFact.newRequestor( tlsConfig.createSocket(aeData) );
	
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

