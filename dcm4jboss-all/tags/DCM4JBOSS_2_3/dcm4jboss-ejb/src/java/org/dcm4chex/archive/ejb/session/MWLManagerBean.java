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
import javax.ejb.RemoveException;
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
 * @version $Revision: 1690 $ $Date: 2005-04-14 17:05:56 +0800 (周四, 14 4月 2005) $
 * @since 10.12.2003
 * 
 * @ejb.bean name="MWLManager" type="Stateless" view-type="remote"
 *           jndi-name="ejb/MWLManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="MWLItem" view-type="local" ref-name="ejb/MWLItem"
 * 
 */
public abstract class MWLManagerBean implements SessionBean {
	private static final int[] PATIENT_ATTRS = { Tags.PatientName,
			Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

	private static Logger log = Logger.getLogger(MWLManagerBean.class);

	private PatientLocalHome patHome;

	private MWLItemLocalHome mwlItemHome;

	public void setSessionContext(SessionContext ctx) {
		Context jndiCtx = null;
		try {
			jndiCtx = new InitialContext();
			patHome = (PatientLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Patient");
			mwlItemHome = (MWLItemLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/MWLItem");
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
    public Dataset getWorklistItem(String spsid) throws FinderException {
		try {
			return getWorklistItem(spsid, false);
		} catch (RemoveException e) {
			throw new EJBException(e);
		}
    }
    
	/**
	 * @ejb.interface-method
	 */
	public Dataset removeWorklistItem(String spsid) 
			throws EJBException, RemoveException, FinderException {
		return getWorklistItem(spsid, true);
	}
	
	private Dataset getWorklistItem(String spsid, boolean remove) 
			throws RemoveException, FinderException {
		MWLItemLocal mwlItem;
		try {
			mwlItem = mwlItemHome.findBySpsId(spsid);
		} catch (ObjectNotFoundException onf) {
			return null;
		}
        final PatientLocal pat = mwlItem.getPatient();
        Dataset attrs = mwlItem.getAttributes();            
		attrs.putAll(pat.getAttributes(false));
		if (remove) {
			mwlItem.remove();
		}
		return attrs;
	}

	/**
	 * @throws FinderException 
	 * @ejb.interface-method
	 */
	public void updateSPSStatus(String spsid, String status) throws FinderException {
		MWLItemLocal mwlItem = mwlItemHome.findBySpsId(spsid);
		Dataset ds = mwlItem.getAttributes();
		ds.getItem(Tags.SPSSeq).putCS(Tags.SPSStatus, status);
		mwlItem.setAttributes(ds);
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
		PatientLocal patient = patHome.create(ds.subSet(PATIENT_ATTRS));
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
			MWLItemLocal mwlItem = mwlItemHome.create(ds.subSet(
					PATIENT_ATTRS, true, true), getPatient(ds));
			return mwlItem.getSpsId();
		} catch (Exception e) {
			throw new EJBException(e);
		}
	}

	/**
	 * @ejb.interface-method
	 */
	public void updateWorklistItem(Dataset ds) {
		try {
			Dataset sps = ds.getItem(Tags.SPSSeq);
			MWLItemLocal mwlItem = mwlItemHome.findBySpsId(sps.getString(Tags.SPSID));
            Dataset attrs = mwlItem.getAttributes();
            attrs.putAll(ds.subSet(PATIENT_ATTRS, true, true));
			mwlItem.setAttributes(attrs);
		} catch (Exception e) {
			throw new EJBException(e);
		}
	}
}
