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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.jdbc.QueryStudiesCmd;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1700 $ $Date: 2005-04-15 20:50:21 +0800 (周五, 15 4月 2005) $
 * @since 14.01.2004
 * 
 * @ejb.bean name="ContentManager" type="Stateless" view-type="remote"
 *           jndi-name="ejb/ContentManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"

 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient" 
 * @ejb.ejb-ref ejb-name="Study" view-type="local" ref-name="ejb/Study" 
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series" 
 */
public abstract class ContentManagerBean implements SessionBean {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;

    public void setSessionContext(SessionContext arg0)
        throws EJBException, RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
            studyHome =
                (StudyLocalHome) jndiCtx.lookup("java:comp/env/ejb/Study");
            seriesHome =
                (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
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
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getStudy(int studyPk) throws FinderException {
        return studyHome.findByPrimaryKey(new Integer(studyPk)).getAttributes(true);
    }
    
    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getSeries(int seriesPk) throws FinderException {
        return seriesHome.findByPrimaryKey(new Integer(seriesPk)).getAttributes(true);
    }
    
    /**
     * @ejb.interface-method
     */
    public int countStudies(Dataset filter) {
        try {
            return new QueryStudiesCmd(filter).count();
        } catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public List listStudies(Dataset filter, int offset, int limit) {
        try {
            return new QueryStudiesCmd(filter).list(offset, limit);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listStudiesOfPatient(int patientPk) throws FinderException {
        Collection c =
            patHome.findByPrimaryKey(new Integer(patientPk)).getStudies();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            StudyLocal study = (StudyLocal) it.next();
            result.add(study.getAttributes(true));
        }
        return result;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listSeriesOfStudy(int studyPk) throws FinderException {
        Collection c =
            studyHome.findByPrimaryKey(new Integer(studyPk)).getSeries();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            result.add(series.getAttributes(true));
        }
        return result;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listInstancesOfSeries(int seriesPk) throws FinderException {
        Collection c =
            seriesHome.findByPrimaryKey(new Integer(seriesPk)).getInstances();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            InstanceLocal inst = (InstanceLocal) it.next();
            result.add(inst.getAttributes(true));
        }
        return result;
    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getSOPInstanceRefMacro(int studyPk) throws FinderException {
    	Dataset ds = dof.newDataset();
    	StudyLocal sl = studyHome.findByPrimaryKey( new Integer( studyPk ) );
    	ds.putUI( Tags.StudyInstanceUID, sl.getStudyIuid() );
		DcmElement refSerSq = ds.putSQ(Tags.RefSeriesSeq);
		Iterator iterSeries = sl.getSeries().iterator();
		SeriesLocal series;
		String aet;
		int pos;
		while ( iterSeries.hasNext() ) {
			series = (SeriesLocal) iterSeries.next();
			Dataset serDS = refSerSq.addNewItem();
			serDS.putUI(Tags.SeriesInstanceUID, series.getSeriesIuid() );
			aet = series.getRetrieveAETs();
			pos = aet.indexOf('\\');
			if ( pos != -1 ) aet = aet.substring(0,pos);
			serDS.putAE( Tags.RetrieveAET, aet );
			serDS.putAE( Tags.StorageMediaFileSetID, series.getFilesetId() );
			serDS.putAE( Tags.StorageMediaFileSetUID, series.getFilesetIuid() );
			DcmElement refSopSq = serDS.putSQ(Tags.RefSOPSeq);
			Iterator iterInstances = series.getInstances().iterator();
			InstanceLocal instance;
			while ( iterInstances.hasNext() ) {
				instance = (InstanceLocal) iterInstances.next();
				Dataset instDS = refSopSq.addNewItem();
				instDS.putUI( Tags.RefSOPInstanceUID, instance.getSopIuid() );
				instDS.putUI( Tags.RefSOPClassUID, instance.getSopCuid() );
			}
		} 
    	return ds;
    }
    
    /**
     * @throws FinderException
     *
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getPatientForStudy(int studyPk) throws FinderException {
    	StudyLocal sl = studyHome.findByPrimaryKey( new Integer( studyPk ) );
    	return sl.getPatient().getAttributes(false);
    }    
}
