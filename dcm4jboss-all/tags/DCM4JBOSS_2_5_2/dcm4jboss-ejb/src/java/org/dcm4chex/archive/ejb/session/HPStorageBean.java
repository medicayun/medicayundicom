/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.session;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.dict.Status;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.interfaces.HPLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1909 $ $Date: 2005-08-18 01:24:52 +0800 (周四, 18 8月 2005) $
 * @since Aug 17, 2005
 * 
 * @ejb.bean name="HPStorage" type="Stateless" view-type="remote" 
 * 			 jndi-name="ejb/HPStorage"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="HP" view-type="local" ref-name="ejb/HP"
 */
public abstract class HPStorageBean implements SessionBean {

	private HPLocalHome hpHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
			hpHome = (HPLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/HP");
            try {
            } catch ( Throwable t ) {
            	t.printStackTrace();
            }
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
		hpHome = null;
    }
	
    /**
     * @ejb.interface-method
     */
    public void store(org.dcm4che.data.Dataset ds) throws DcmServiceException {
		try {
			hpHome.create(ds);
		} catch (CreateException e) {
			throw new DcmServiceException(Status.ProcessingFailure, e);
		}
    }
}
