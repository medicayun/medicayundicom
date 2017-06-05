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
import java.util.Collections;
import java.util.Comparator;
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
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.jdbc.QueryStudiesCmd;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1824 $ $Date: 2005-06-27 20:42:55 +0800 (周一, 27 6月 2005) $
 * @since 14.01.2004
 * 
 * @ejb.bean name="ContentManager" type="Stateless" view-type="remote"
 *           jndi-name="ejb/ContentManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"

 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient" 
 * @ejb.ejb-ref ejb-name="Study" view-type="local" ref-name="ejb/Study" 
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series" 
 * @ejb.ejb-ref ejb-name="Instance" view-type="local" ref-name="ejb/Instance" 
 */
public abstract class ContentManagerBean implements SessionBean {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;
    private InstanceLocalHome instanceHome;

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
            instanceHome =
                (InstanceLocalHome) jndiCtx.lookup("java:comp/env/ejb/Instance");
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
    public Dataset getStudyByIUID(String studyIUID) throws FinderException {
        return studyHome.findByStudyIuid(studyIUID).getAttributes(true);
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
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listFilesOfInstance(int instancePk) throws FinderException {
        Collection c =
            instanceHome.findByPrimaryKey(new Integer(instancePk)).getFiles();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            FileLocal file = (FileLocal) it.next();
            result.add(file.getFileDTO());
        }
        return result;
    }
    
    /**
     * @throws FinderException
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getSOPInstanceRefMacro( int studyPk, boolean insertModality ) throws FinderException {
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
			if ( insertModality ) {
				serDS.putCS( Tags.Modality, series.getModality() );
				serDS.putIS( Tags.SeriesNumber, series.getSeriesNumber() ); //Q&D 
			}
			DcmElement refSopSq = serDS.putSQ(Tags.RefSOPSeq);
			Collection col = series.getInstances();
			List l = ( col instanceof List ) ? (List)col : new ArrayList(col);
			Collections.sort( l, new InstanceNumberComparator() );
			Iterator iterInstances = l.iterator();
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
    
	public class InstanceNumberComparator implements Comparator {

		public InstanceNumberComparator() {
		}

		/**
		 * Compares the instance number of two InstanceLocal objects.
		 * <p>
		 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer 
		 * as the first argument is less than, equal to, or greater than the second.
		 * <p>
		 * Throws an Exception if one of the arguments is null or neither a InstanceContainer or InstanceLocal object.<br>
		 * Also both arguments must be of the same type!
		 * <p>
		 * If arguments are of type InstanceLocal, the getInstanceSize Method of InstanceCollector is used to get filesize.
		 *  
		 * @param arg0 	First argument
		 * @param arg1	Second argument
		 * 
		 * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
		 */
		public int compare(Object arg0, Object arg1) {
			InstanceLocal il1 = (InstanceLocal) arg0;
			InstanceLocal il2 = (InstanceLocal) arg1;
			return new Integer(il1.getInstanceNumber()).compareTo( new Integer(il2.getInstanceNumber()) );
		}
		
	}// end class
    
}
