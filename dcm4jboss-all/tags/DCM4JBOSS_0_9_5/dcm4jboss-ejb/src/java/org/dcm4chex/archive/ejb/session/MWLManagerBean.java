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
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocal;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1163 $ $Date: 2004-09-20 06:23:30 +0800 (周一, 20 9月 2004) $
 * @since 10.12.2003
 * 
 * @ejb.bean
 *  name="MWLManager"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/MWLManager"
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
 *  ejb-name="MWLItem" 
 *  view-type="local"
 *  ref-name="ejb/MWLItem" 
 * 
 */
public abstract class MWLManagerBean implements SessionBean {
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
    private static Logger log = Logger.getLogger(MWLManagerBean.class);
    private PatientLocalHome patHome;
    private MWLItemLocalHome mwlItemHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
            mwlItemHome =
                (MWLItemLocalHome) jndiCtx.lookup(
                    "java:comp/env/ejb/MWLItem");
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
        mwlItemHome = null;
        patHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public Dataset removeWorklistItem(String id) {
        try {
            MWLItemLocal mwlItem = mwlItemHome.findBySpsId(id);
            Dataset ds = mwlItem.getAttributes();
            mwlItem.remove();
            return ds;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    private PatientLocal getPatient(Dataset ds) throws FinderException,
            CreateException {
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
    }

    private boolean equals(PatientLocal patient, Dataset ds) {
        // TODO Auto-generated method stub
        return true;
    }
    /**
     * @ejb.interface-method
     */
    public String addWorklistItem(Dataset ds) {
        try {
            MWLItemLocal mwlItem = mwlItemHome.create(ds, getPatient(ds));
            return mwlItem.getSpsId();
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
