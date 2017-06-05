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
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1357 $ $Date: 2004-12-30 08:55:56 +0800 (周四, 30 12月 2004) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="PatientUpdate"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/PatientUpdate"
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
 */
public abstract class PatientUpdateBean implements SessionBean {

    private PatientLocalHome patHome;

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
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
    }

    /**
     * @ejb.interface-method
     */
    public void mergePatient(Dataset dominant, Dataset prior) {
        PatientLocal dominantPat = updateOrCreate(dominant);
        PatientLocal priorPat= updateOrCreate(prior);
        dominantPat.getStudies().addAll(priorPat.getStudies());
        dominantPat.getMpps().addAll(priorPat.getMpps());
        dominantPat.getMwlItems().addAll(priorPat.getMwlItems());
        priorPat.setMergedWith(dominantPat);
    }

    /**
     * @ejb.interface-method
     */
    public void updatePatient(Dataset attrs) {
        updateOrCreate(attrs);
    }

    private PatientLocal updateOrCreate(Dataset ds) {
        try {
            String pid = ds.getString(Tags.PatientID);
            String issuer = ds.getString(Tags.IssuerOfPatientID);
            Collection c = issuer == null ? patHome.findByPatientId(pid)
                    : patHome.findByPatientIdWithIssuer(pid, issuer);
            if (c.isEmpty()) { return patHome.create(ds); }
            if (c.size() > 1) { throw new FinderException("Patient ID[id="
                    + pid + ",issuer=" + issuer + " ambiguous"); }
            PatientLocal pat = (PatientLocal) c.iterator().next();
            update(pat, ds);
            return pat;
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }

    private void update(PatientLocal pat, Dataset attrs) {
        Dataset ds = pat.getAttributes(false);
        ds.putAll(attrs);
        pat.setAttributes(ds);
    }

}