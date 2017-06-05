/* $Id: MPPSManagerBean.java 1078 2004-04-05 11:20:39Z umbertoc $
 * Copyright (c) 2004 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
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
 * @version $Revision: 1078 $ $Date: 2004-04-05 19:20:39 +0800 (周一, 05 4月 2004) $
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
    public void createMPPS(String iuid, Dataset ds)
        throws DcmServiceException {
        try {
            mppsHome.create(iuid, ds.subSet(PATIENT_ATTRS_EXC, true), getPatient(ds));
        } catch (CreateException ce) {
            try {
                mppsHome.findBySopIuid(iuid);
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
                patHome.create(ds.subSet(PATIENT_ATTRS_INC, false));
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
    public void updateMPPS(String iuid, Dataset ds)
        throws DcmServiceException {
        MPPSLocal mpps;
        try {
            mpps = mppsHome.findBySopIuid(iuid);
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
        mpps.setAttributes(ds);
        if (mpps.isIncorrectWorklistEntrySelected()) {
            Collection c = mpps.getSeries();
            for (Iterator it = c.iterator(); it.hasNext();) {
                SeriesLocal ser = (SeriesLocal) it.next();
                ser.hide();
            }
        }
    }
}
