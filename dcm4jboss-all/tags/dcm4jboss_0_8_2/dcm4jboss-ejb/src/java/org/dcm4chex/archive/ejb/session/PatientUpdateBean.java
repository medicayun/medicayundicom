/* $Id: PatientUpdateBean.java 1063 2004-03-22 19:38:23Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
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
import org.dcm4chex.archive.ejb.interfaces.DTO2Dataset;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1063 $ $Date: 2004-03-23 03:38:23 +0800 (周二, 23 3月 2004) $
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

    public void setSessionContext(SessionContext arg0)
        throws EJBException, RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
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
        patHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public void mergePatient(PatientDTO dominantDTO, PatientDTO[] priorDTOs) {

        PatientLocal dominantPat = updateOrCreate(dominantDTO);
        PatientLocal priorPat;
        for (int i=0; i<priorDTOs.length;i++)
        {
        	priorPat= updateOrCreate(priorDTOs[i]);
        	dominantPat.getStudies().addAll(priorPat.getStudies());
        	dominantPat.getMpps().addAll(priorPat.getMpps());
        	priorPat.setMergedWith(dominantPat);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void updatePatient(PatientDTO dto) {
    	updateOrCreate(dto);
    }
    
    private PatientLocal updateOrCreate(PatientDTO dto) {
        try {
            Collection c =
                isNullOrEmpty(dto.getIssuerOfPatientID())
                    ? patHome.findByPatientId(dto.getPatientID())
                    : patHome.findByPatientIdWithIssuer(
                        dto.getPatientID(),
                        dto.getIssuerOfPatientID());
            if (c.isEmpty()) {
                return patHome.create(DTO2Dataset.toDataset(dto));
            }
            if (c.size() > 1) {
                throw new FinderException(
                    "Patient ID[id="
                        + dto.getPatientID()
                        + ",issuer="
                        + dto.getIssuerOfPatientID()
                        + " ambiguous");
            }
            PatientLocal pat = (PatientLocal) c.iterator().next();
            update(pat, dto);
            return pat;
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
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
                    Date newDate =
                        new SimpleDateFormat(PatientDTO.DATE_FORMAT).parse(
                            dto.getPatientBirthDate());
                    if (!newDate.equals(oldDate)) {
                        pat.setPatientBirthDate(newDate);
                        modified = true;
                    }
                } catch (ParseException e) {} //do nothing
            } else if (oldDate != null) {
                pat.setPatientBirthDate(null);
                modified = true;
            }
        }
        if (modified = true) {
            Dataset oldPat = pat.getAttributes();
            DTO2Dataset.updateDataset(oldPat, dto);
            pat.setAttributes(oldPat);
        }
    }

    static boolean needUpdate(String toUpdate, String newVal) {
    	if (newVal == null) { // no update
    		return false;
    	}
    	if (toUpdate == null) {
    		return newVal.length() != 0;
    	}
        return !toUpdate.equals(newVal);
    }

    static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
