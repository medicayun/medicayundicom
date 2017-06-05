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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

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
import org.dcm4chex.archive.ejb.interfaces.DTO2Dataset;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
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
    public void mergePatient(PatientDTO dominantDTO, PatientDTO[] priorDTOs) {

        PatientLocal dominantPat = updateOrCreate(dominantDTO);
        PatientLocal priorPat;
        for (int i = 0; i < priorDTOs.length; i++) {
            priorPat = updateOrCreate(priorDTOs[i]);
            dominantPat.getStudies().addAll(priorPat.getStudies());
            dominantPat.getMpps().addAll(priorPat.getMpps());
            dominantPat.getMwlItems().addAll(priorPat.getMwlItems());
            priorPat.setMergedWith(dominantPat);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void mergePatient(Dataset dominant, Dataset[] priors) {

        PatientLocal dominantPat = updateOrCreate(dominant);
        PatientLocal priorPat;
        for (int i = 0; i < priors.length; i++) {
            priorPat = updateOrCreate(priors[i]);
            dominantPat.getStudies().addAll(priorPat.getStudies());
            dominantPat.getMpps().addAll(priorPat.getMpps());
            dominantPat.getMwlItems().addAll(priorPat.getMwlItems());
            priorPat.setMergedWith(dominantPat);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void updatePatient(PatientDTO dto) {
        updateOrCreate(dto);
    }

    /**
     * @ejb.interface-method
     */
    public void updatePatient(Dataset attrs) {
        updateOrCreate(attrs);
    }

    private PatientLocal updateOrCreate(PatientDTO dto) {
        try {
            Collection c = isNullOrEmpty(dto.getIssuerOfPatientID()) ? patHome
                    .findByPatientId(dto.getPatientID()) : patHome
                    .findByPatientIdWithIssuer(dto.getPatientID(), dto
                            .getIssuerOfPatientID());
            if (c.isEmpty()) { return patHome
                    .create(DTO2Dataset.toDataset(dto)); }
            if (c.size() > 1) { throw new FinderException("Patient ID[id="
                    + dto.getPatientID() + ",issuer="
                    + dto.getIssuerOfPatientID() + " ambiguous"); }
            PatientLocal pat = (PatientLocal) c.iterator().next();
            update(pat, dto);
            return pat;
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
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

    private void update(PatientLocal pat, PatientDTO dto) {
        boolean modified = false;
        if (needUpdate(pat.getIssuerOfPatientId(), dto.getIssuerOfPatientID())) {
            pat.setIssuerOfPatientId(dto.getIssuerOfPatientID());
            modified = true;
        }

        if (needUpdate(pat.getPatientName(), dto.getPatientName())) {
            pat.setPatientName(dto.getPatientName());
            modified = true;
        }

        if (needUpdate(pat.getPatientSex(), dto.getPatientSex())) {
            pat.setPatientSex(dto.getPatientSex());
            modified = true;
        }
        final String newBirthDate = dto.getPatientBirthDate();
        if (newBirthDate != null) {
            Date oldDate = pat.getPatientBirthDate();
            if (newBirthDate.length() != 0) {
                try {
                    Date newDate = new SimpleDateFormat(PatientDTO.DATE_FORMAT)
                            .parse(dto.getPatientBirthDate());
                    if (!newDate.equals(oldDate)) {
                        pat.setPatientBirthDate(newDate);
                        modified = true;
                    }
                } catch (ParseException e) {
                } //do nothing
            } else if (oldDate != null) {
                pat.setPatientBirthDate(null);
                modified = true;
            }
        }
        if (modified = true) {
            Dataset oldPat = pat.getAttributes(false);
            DTO2Dataset.updateDataset(oldPat, dto);
            pat.setAttributes(oldPat);
        }
    }

    static boolean needUpdate(String toUpdate, String newVal) {
        if (newVal == null) { // no update
            return false;
        }
        if (toUpdate == null) { return newVal.length() != 0; }
        return !toUpdate.equals(newVal);
    }

    static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

}