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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.SPSStatus;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2509 $ $Date: 2006-05-31 21:32:55 +0800 (周三, 31 5月 2006) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="PatientUpdate"
 *  type="Stateless"
 *  view-type="both"
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

	private static Logger log = Logger.getLogger(PatientUpdateBean.class);
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
        dominantPat.getGsps().addAll(priorPat.getGsps());
        priorPat.setMergedWith(dominantPat);
    }
    
    /**
     * Update patient data as well as relink study with the patient if the patient
     * is different than original one.
     * 
     * @ejb.interface-method
     */
    public void updatePatient(StudyLocal study, Dataset attrs) {
		String pid = attrs.getString(Tags.PatientID);

		// If the patient id is not included, then we don't have to do any
		// patient update. Although patient id is type 2 in DICOM, but for DC,
		// we enforce this.
		if (pid == null || pid.length() == 0)
			return;
		
		PatientLocal newPatient = updateOrCreate(attrs);
		
		// Case 1: it's matching the same patient. Do nothing
		if(study.getPatient().getPatientId().equals(pid))
			return;
			
		// Case 2: there's no matching, a new patient is created. The study is updated.
		// Case 3: it's matching another existing patient. The study is updated.
		study.setPatient(newPatient);
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

    /**
     * @ejb.interface-method
     */
    public void deletePatient(Dataset ds) {
    	try {
    		PatientLocal pat = findPatient(ds);
    		if (pat != null)
    			patHome.remove(pat.getPk());
    	} catch (Exception e) {
    		throw new EJBException(e.getMessage(),e);
    	}
    }

	private PatientLocal findPatient(Dataset ds) throws FinderException {
		String pid = ds.getString(Tags.PatientID);
		String issuer = ds.getString(Tags.IssuerOfPatientID);
		Collection c = issuer == null ? patHome.findByPatientId(pid)
				: patHome.findByPatientIdWithIssuer(pid, issuer);
		if (c.isEmpty())
		{
			 log.info("No Patient with PID:"+pid+",issuer:"+issuer + " found.");
			 return null;
		}
		if (c.size() > 1) { throw new FinderException("Patient ID[id="
				+ pid + ",issuer=" + issuer + " ambiguous"); }
		PatientLocal pat = (PatientLocal) c.iterator().next();
		return pat;
	}

    /**
     * @throws FinderException 
     * @ejb.interface-method
     */
    public void patientArrived(Dataset ds) throws FinderException {
		PatientLocal pat = findPatient(ds);
		if (pat != null)
		{
			Collection c = pat.getMwlItems();
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				MWLItemLocal mwlitem = (MWLItemLocal) iter.next();
				mwlitem.updateSpsStatus(SPSStatus.ARRIVED);
			}
		}
    }
    
}