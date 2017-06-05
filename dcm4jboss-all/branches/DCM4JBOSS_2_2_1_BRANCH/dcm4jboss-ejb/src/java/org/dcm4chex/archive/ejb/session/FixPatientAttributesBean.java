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
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TransactionRolledbackLocalException;
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
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

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
 * @ejb.ejb-ref
 *  ejb-name="Study" 
 *  view-type="local"
 *  ref-name="ejb/Study" 
 * 
 * @ejb.env-entry name="AttributeFilterConfigURL" type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-filter.xml"
 * 
 * @author <a href="mailto:franz.willer@gwi-ag.com">Franz Willer </a>
 * @version $Revision: 2083 $ $Date: 2005-11-21 20:41:56 +0800 (周一, 21 11月 2005) $
 *  
 */
public abstract class FixPatientAttributesBean implements SessionBean {

	private static Logger log = Logger.getLogger(FixPatientAttributesBean.class);

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;

    private AttributeFilter attrFilter;

    private SessionContext sessionCtx;

    public void setSessionContext(SessionContext ctx) {
        sessionCtx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
            .lookup("java:comp/env/ejb/Patient");
            studyHome = (StudyLocalHome) jndiCtx
            .lookup("java:comp/env/ejb/Study");
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
    	log.info("Check patient attributes: offset:"+offset+" limit:"+limit+" doUpdate:"+doUpdate);
    	Collection col = patHome.findAll(offset,limit);
    	log.info( "Found "+col.size()+" patients to check!");
    	if ( col.isEmpty() ) return null;
    	PatientLocal patient;
    	Dataset patAttrs, filtered;
    	int[] result = { 0, 0 };
    	for ( Iterator iter = col.iterator() ; iter.hasNext() ; result[1]++) {
			patient = (PatientLocal) iter.next();
			try {
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
			} catch ( TransactionRolledbackLocalException ignore ){
				log.warn("Study object ["+result[1]+"] not longer available! Ignored!");
			} catch ( NoSuchObjectLocalException ignore ){
				log.warn("Patient object ["+result[1]+"] not longer available! Ignored!");
			}
     	}
    	log.info( result[1]+" patients checked! "+result[0]+" patients with non-patient attributes!");
    	return result;
    }

    /**
     * Check study attributes.
     * <p>
     * 
     * @param studyPks Array of StudyBean primary keys.
     * @param doUpdate true will update study record, false leave study record unchanged.
     * 
     * @return number of 'fixed/toBeFixed' study records
     * 
     * @throws FinderException
     * @ejb.interface-method
     */
    public int checkStudyAttributes(List studyPks, boolean doUpdate) {
    	log.info("Check "+studyPks.size()+"study attributes: doUpdate:"+doUpdate);
    	StudyLocal study;
    	Dataset studyAttrs, filtered;
    	int result = 0;
    	Integer studyPk;
    	for ( Iterator iter = studyPks.iterator() ; iter.hasNext() ; ) {
    		studyPk = (Integer) iter.next();
			try {
				study = (StudyLocal) studyHome.findByPrimaryKey( studyPk );
				studyAttrs = study.getAttributes(false);
				filtered = studyAttrs.subSet(attrFilter.getStudyFilter());
				if (studyAttrs.size() > filtered.size()) {
				    log.warn("Detect Study Record [pk= " + study.getPk() +
				    		"] with non-study attributes:");
					log.warn(studyAttrs);
					if (doUpdate) {
					    study.setAttributes(filtered);
					    log.warn(
							"Remove non-study attributes from Study Record [pk= "
								+ study.getPk() + "]");
					}
					result++;
				}
			} catch ( FinderException ignore ){
				log.warn("Study object [pk="+studyPk+"] not longer exists! Ignored!");
			} catch ( TransactionRolledbackLocalException ignore ){
				if ( ignore.getCause() instanceof NoSuchObjectLocalException )
					log.warn("Study object [pk="+studyPk+"] not longer available! Ignored!");
				else
					log.warn("Study object (pk="+studyPk+") check ignored! cause:"+ignore.getCause());
			} catch ( NoSuchObjectLocalException ignore ){
				log.warn("Study object [pk="+studyPk+"] not longer available! Ignored!");
			}
     	}
    	log.info( studyPks.size()+" studies checked! "+result+" studies with non-study attributes!");
    	return result;
    }
    
}