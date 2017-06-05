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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocal;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1578 $ $Date: 2005-03-02 10:57:23 +0800 (周三, 02 3月 2005) $
 * @since 21.03.2004
 *
 * @ejb.bean name="MPPSManager" type="Stateless" view-type="remote"
 * 	jndi-name="ejb/MPPSManager"
 * @ejb.transaction-type  type="Container"
 * @ejb.transaction type="Required"
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="MPPS" view-type="local" ref-name="ejb/MPPS" 
 */
public abstract class MPPSManagerBean implements SessionBean {

    private static Logger log = Logger.getLogger(MPPSManagerBean.class);
    private static final String NO_LONGER_BE_UPDATED_ERR_MSG =
        "Performed Procedure Step Object may no longer be updated";
    private static final int NO_LONGER_BE_UPDATED_ERR_ID = 0xA710;
    private static final int[] PATIENT_ATTRS_EXC = {
            Tags.PatientName,
            Tags.PatientID,
            Tags.PatientBirthDate,
            Tags.PatientSex,
            Tags.RefPatientSeq,         
    };
    private static final int[] PATIENT_ATTRS_INC = {
            Tags.PatientName,
            Tags.PatientID,
            Tags.PatientBirthDate,
            Tags.PatientSex,
    };
    private PatientLocalHome patHome;
    private MPPSLocalHome mppsHome;
    private SessionContext sessionCtx;    

    public void setSessionContext(SessionContext ctx) {
        sessionCtx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
            mppsHome = (MPPSLocalHome) jndiCtx.lookup("java:comp/env/ejb/MPPS");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }

    public void unsetSessionContext() {
        sessionCtx = null;
        mppsHome = null;
        patHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public void createMPPS(Dataset ds)
        throws DcmServiceException {
        try {
            mppsHome.create(ds.subSet(PATIENT_ATTRS_EXC, true, true), getPatient(ds));
        } catch (CreateException ce) {
            try {
                mppsHome.findBySopIuid(ds.getString(Tags.SOPInstanceUID));
                throw new DcmServiceException(Status.DuplicateSOPInstance);
            } catch (FinderException fe) {
                throw new DcmServiceException(Status.ProcessingFailure, ce);
            } finally {
                sessionCtx.setRollbackOnly();
            }
        }
    }

    private PatientLocal getPatient(Dataset ds) throws DcmServiceException {
        try {
            final String id = ds.getString(Tags.PatientID);
            Collection c = patHome.findByPatientId(id);
            for (Iterator it = c.iterator(); it.hasNext();) {
                PatientLocal patient = (PatientLocal) it.next();
                if (equals(patient, ds)) {
                    PatientLocal mergedWith = patient.getMergedWith();
                    if (mergedWith != null) {
                        patient = mergedWith;
                    }
                    return patient;
                }
            }
            PatientLocal patient =
                patHome.create(ds.subSet(PATIENT_ATTRS_INC));
            return patient;
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private boolean equals(PatientLocal patient, Dataset ds) {
        // TODO Auto-generated method stub
        return true;
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateMPPS(Dataset ds)
        throws DcmServiceException {
        MPPSLocal mpps;
        try {
            mpps = mppsHome.findBySopIuid(ds.getString(Tags.SOPInstanceUID));
        } catch (ObjectNotFoundException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        } catch (FinderException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        if (!"IN PROGRESS".equals(mpps.getPpsStatus())) {
            DcmServiceException e =
                new DcmServiceException(
                    Status.ProcessingFailure,
                    NO_LONGER_BE_UPDATED_ERR_MSG);
            e.setErrorID(NO_LONGER_BE_UPDATED_ERR_ID);
            throw e;
        }
        Dataset attrs = mpps.getAttributes();
        attrs.putAll(ds);
        mpps.setAttributes(attrs);
        if (mpps.isIncorrectWorklistEntrySelected()) {
            Collection c = mpps.getSeries();
            SeriesLocal ser = null;
            for (Iterator it = c.iterator(); it.hasNext();) {
                ser = (SeriesLocal) it.next();
                ser.setHidden(true);
            }
            if (ser != null)
                try {
                    ser.getStudy().updateDerivedFields(true, true, true, true, true, true);
                } catch (FinderException e1) {
                    throw new DcmServiceException(Status.ProcessingFailure, e1);
                }
        }
    }
}
