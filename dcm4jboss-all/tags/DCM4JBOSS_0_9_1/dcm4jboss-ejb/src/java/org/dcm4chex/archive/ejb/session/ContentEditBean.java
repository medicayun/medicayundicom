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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="ContentEdit"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/ContentEdit"
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
public abstract class ContentEditBean implements SessionBean {

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;

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
     * @throws CreateException
     * @ejb.interface-method
     */
    public void createPatient(Dataset ds) throws CreateException {
        patHome.create(ds);
    }

    /**
     * @ejb.interface-method
     */
    public void updatePatient(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final int pk = ds.getInt(PrivateTags.PatientPk, -1);
            PatientLocal patient = patHome
                    .findByPrimaryKey(new Integer(pk));
            patient.setAttributes(ds);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void updateStudy(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final int pk = ds.getInt(PrivateTags.StudyPk, -1);
            StudyLocal study = studyHome
                    .findByPrimaryKey(new Integer(pk));
            study.setAttributes(ds);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateSeries(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final int pk = ds.getInt(PrivateTags.SeriesPk, -1);
            SeriesLocal series = seriesHome
                    .findByPrimaryKey(new Integer(pk));
            series.setAttributes(ds);
            StudyLocal study = series.getStudy();
            study.updateModalitiesInStudy();
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public void deleteSeries(int series_pk) throws RemoteException {
        try {
            SeriesLocal series = seriesHome.findByPrimaryKey(new Integer(
                    series_pk));
            StudyLocal study = series.getStudy();
            series.remove();
            study.updateRetrieveAETs();
            study.updateAvailability();
            study.updateModalitiesInStudy();
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @ejb.interface-method
     */
    public void deleteStudy(int study_pk) throws RemoteException {
        try {
            studyHome.remove(new Integer(study_pk));
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @ejb.interface-method
     */
    public void deletePatient(int patient_pk) throws RemoteException {
        try {
            patHome.remove(new Integer(patient_pk));
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @ejb.interface-method
     */
    public void deleteInstance(int instance_pk) throws RemoteException {
        try {
            InstanceLocal instance = instHome.findByPrimaryKey(new Integer(
                    instance_pk));
            SeriesLocal series = instance.getSeries();
            instance.remove();
            if (series.updateRetrieveAETs()) {
                series.getStudy().updateRetrieveAETs();
            }
            if (series.updateAvailability()) {
                series.getStudy().updateAvailability();
            }
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }
}