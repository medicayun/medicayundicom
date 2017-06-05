/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
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
    public Dataset getMPPS(String iuid) throws FinderException {
        final MPPSLocal mpps = mppsHome.findBySopIuid(iuid);
        final PatientLocal pat = mpps.getPatient();
        Dataset attrs = mpps.getAttributes();            
		attrs.putAll(pat.getAttributes(false));
		return attrs;
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
                ser.markDeleted(true);
            }
            if (ser != null)
                try {
                	StudyLocal study = ser.getStudy();
                	if ( study.getSeries().size() == 1)
                		study.setHidden(true);
                	else
                		study.updateDerivedFields(true, true, true, true, true, true, true);
                } catch (FinderException e1) {
                    throw new DcmServiceException(Status.ProcessingFailure, e1);
                }
        }
    }
}
