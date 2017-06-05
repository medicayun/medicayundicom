/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1655 $ $Date: 2005-03-29 23:23:32 +0800 (周二, 29 3月 2005) $
 * @since 25.03.2005
 * 
 * @ejb.bean
 *  name="ConsistencyCheck"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/ConsistencyCheck"
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
 * @ejb.ejb-ref
 *  ejb-name="Series" 
 *  view-type="local"
 *  ref-name="ejb/Series" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Instance" 
 *  view-type="local"
 *  ref-name="ejb/Instance" 
 */
public abstract class ConsistencyCheckBean implements SessionBean {

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;
    
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    
    private static final Logger log = Logger.getLogger(ConsistencyCheckBean.class);

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
            studyHome = (StudyLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Study");
            seriesHome = (SeriesLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Series");
            instHome = (InstanceLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Instance");
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
        patHome = null;
        studyHome = null;
        seriesHome = null;
        instHome = null;
    }
    
    /**
     * Return studies to check consistency..
     * <p>
     * <DL>
     * <DD>1) Find (0-<code>limit</code>) studies with a creation date between <code>createdAfter and createdBefore</code>
     * 			and not checked before checkedAfter</DD>
     * </DL>
     * @param createdAfter 	Timestamp: studies must be created after this timestamp.
     * @param createdBefore	Timestamp: studies must be created before this timestamp.
     * @param checkedBefore	Timestamp: studies must be checked before this timestamp.
     * @param limit			Max number of returned studies.
     * 
     * @return int array with pk of studies to check.
     * @ejb.interface-method
     */
    public int[] findStudiesToCheck(Timestamp createdAfter, Timestamp createdBefore, Timestamp checkedBefore, int limit) throws FinderException {
    	if ( log.isDebugEnabled() ) log.debug("findStudiesToCheck: created between "+createdAfter+" - "+createdBefore+" checkedBefore"+checkedBefore+" limit:"+limit);
        Collection c = studyHome.findStudyToCheck( createdAfter, createdBefore, checkedBefore, limit );
        if ( c.size() < 1 ) return new int[0];
        Iterator iter = c.iterator();
        int[] ia = new int[c.size()];
        int i = 0;
        StudyLocal study;
        while ( iter.hasNext() ) {
        	ia[i++] = ((StudyLocal) iter.next()).getPk().intValue();
        }
        return ia;
    }
    

    

    /**
     * @ejb.interface-method
     */
    public boolean updateStudy(int study_pk) {
    	boolean updated = false;
        try {
            StudyLocal study = studyHome
                    .findByPrimaryKey(new Integer(study_pk));
            Collection col = study.getSeries();
            Iterator iter = col.iterator();
            SeriesLocal series;
            Collection instances;
            InstanceLocal instance;
            while ( iter.hasNext() ) {
            	series = (SeriesLocal) iter.next();
            	instances = series.getInstances();
            	Iterator iter1 = instances.iterator();
            	while ( iter1.hasNext() ) {
            		instance = (InstanceLocal) iter1.next();
            		if ( instance.updateDerivedFields(true,true) ) {
            			log.info("Instance "+instance.getSopIuid()+" updated!");
            			updated = true;
            		}
            	}
            	if ( series.updateDerivedFields( true, true, true, true, true ) ) {
        			log.info("Series "+series.getSeriesIuid()+" updated!");
        			updated = true;
            	}
            }
            if ( study.updateDerivedFields( true, true, true, true, true, true ) ) {
    			log.info("Study "+study.getStudyIuid()+" updated!");
    			updated = true;
            }
            study.setTimeOfLastConsistencyCheck( new Timestamp( System.currentTimeMillis() ) );
            return updated;
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }
    
 
}