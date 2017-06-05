/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.conf.ConfigurationException;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * FixPatientAttributes Bean
 * 
 * @ejb.bean
 *  name="FixPatientAttributes"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/FixPatientAttributes"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="Patient" 
 *  view-type="local"
 *  ref-name="ejb/Patient" 
 * 
 * @ejb.env-entry name="AttributeFilterConfigURL" type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-filter.xml"
 * 
 * @author <a href="mailto:franz.willer@gwi-ag.com">Franz Willer </a>
 * @version $Revision: 1951 $ $Date: 2005-09-05 19:35:55 +0800 (周一, 05 9月 2005) $
 *  
 */
public abstract class FixPatientAttributesBean implements SessionBean {

	private static Logger log = Logger.getLogger(FixPatientAttributesBean.class);

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome patHome;

    private AttributeFilter attrFilter;

    private SessionContext sessionCtx;

    public void setSessionContext(SessionContext ctx) {
        sessionCtx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
            .lookup("java:comp/env/ejb/Patient");
            attrFilter = new AttributeFilter((String) jndiCtx
                    .lookup("java:comp/env/AttributeFilterConfigURL"));
            try {
            } catch ( Throwable t ) {
            	t.printStackTrace();
            }
        } catch (NamingException e) {
            throw new EJBException(e);
        } catch (ConfigurationException e) {
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
        sessionCtx = null;
        patHome = null;
    }
    

    /**
     * Check patient attributes.
     * <p>
     * 
     * @param offset first patient to check (paging)
     * @param limit  number of patients to check (paging)
     * @param doUpdate true will update patient record, false leave patient record unchanged.
     * 
     * @return int[2] containing number of 'fixed/toBeFixed' patient records
     *                and number of checked patient records
     * 
     * @throws FinderException
     * @ejb.interface-method
     */
    public int[] checkPatientAttributes(int offset, int limit, boolean doUpdate) throws FinderException {
    	Collection col = patHome.findAll(offset,limit);
    	if ( col.isEmpty() ) return null;
    	PatientLocal patient;
    	Dataset patAttrs, filtered;
    	int[] result = { 0, 0 };
    	for ( Iterator iter = col.iterator() ; iter.hasNext() ; result[1]++) {
			patient = (PatientLocal) iter.next();
			patAttrs = patient.getAttributes(false);
			filtered = patAttrs.subSet(attrFilter.getPatientFilter());
			if (patAttrs.size() > filtered.size()) {
			    log.warn("Detect Patient Record [pk= " + patient.getPk() +
			    		"] with non-patient attributes:");
				log.warn(patAttrs);
				if (doUpdate) {
				    patient.setAttributes(filtered);
				    log.warn(
						"Remove non-patient attributes from Patient Record [pk= "
							+ patient.getPk() + "]");
				}
				result[0]++;
			}
     	}
    	return result;
    }

}