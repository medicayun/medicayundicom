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
import java.util.HashSet;
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
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.GPSPSStatus;
import org.dcm4chex.archive.ejb.interfaces.GPSPSLocal;
import org.dcm4chex.archive.ejb.interfaces.GPSPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-04-14 17:05:56 +0800 (周四, 14 4月 2005) $
 * @since 28.03.2005
 * 
 * @ejb.bean name="GPWLManager" type="Stateless" view-type="remote"
 *           jndi-name="ejb/GPWLManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="GPSPS" view-type="local" ref-name="ejb/GPSPS"
 */

public abstract class GPWLManagerBean implements SessionBean {

    private static final int MAY_NO_LONGER_BE_UPDATED = 0xA501;
    private static final int WRONG_TRANSACTION_UID = 0xA502;
    private static final int ALREADY_IN_PROGRESS = 0xA503;
	private static final int[] PATIENT_ATTRS = { Tags.PatientName,
            Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

    private static Logger log = Logger.getLogger(GPWLManagerBean.class);

    private PatientLocalHome patHome;

    private GPSPSLocalHome gpspsHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
            gpspsHome = (GPSPSLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/GPSPS");
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
        gpspsHome = null;
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
	public Dataset removeWorklistItem(String iuid) 
			throws EJBException, RemoveException, FinderException {
		return getWorklistItem(iuid, true);
	}
	
	private Dataset getWorklistItem(String iuid, boolean remove) 
			throws RemoveException, FinderException {
		GPSPSLocal gpsps;
		try {
            gpsps = gpspsHome.findBySopIuid(iuid);
		} catch (ObjectNotFoundException onf) {
			return null;
		}
        final PatientLocal pat = gpsps.getPatient();
        Dataset attrs = gpsps.getAttributes();            
		attrs.putAll(pat.getAttributes(false));
		if (remove) {
			gpsps.remove();
		}
		return attrs;
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
            String iuid = ds.getString(Tags.SOPInstanceUID);
            if (iuid == null) {
                iuid = UIDGenerator.getInstance().createUID();
                ds.putUI(Tags.SOPInstanceUID, iuid);
            }
            gpspsHome.create(ds.subSet(PATIENT_ATTRS, true, true),
                    getPatient(ds));
            return iuid;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void updateWorklistItem(Dataset ds) {
        try {
            final String iuid = ds.getString(Tags.SOPInstanceUID);
            GPSPSLocal gpsps = gpspsHome.findBySopIuid(iuid);
            Dataset attrs = gpsps.getAttributes();
            attrs.putAll(ds.subSet(PATIENT_ATTRS, true, true));
            gpsps.setAttributes(attrs);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void modifyStatus(String iuid, Dataset ds) throws DcmServiceException {
		try {
			GPSPSLocal gpsps = gpspsHome.findBySopIuid(iuid);
			String tsuid = ds.getString(Tags.TransactionUID);
			String status = ds.getString(Tags.GPSPSStatus);
			int statusAsInt = GPSPSStatus.toInt(status);
			switch(gpsps.getGpspsStatusAsInt()) {
			case GPSPSStatus.IN_PROGRESS:
				if (statusAsInt == GPSPSStatus.IN_PROGRESS)
					throw new DcmServiceException(ALREADY_IN_PROGRESS);					
				else if (!tsuid.equals(gpsps.getTransactionUid()))
					throw new DcmServiceException(WRONG_TRANSACTION_UID);
				break;
			case GPSPSStatus.COMPLETED:
			case GPSPSStatus.DISCONTINUED:
				throw new DcmServiceException(MAY_NO_LONGER_BE_UPDATED);
			}
	        Dataset attrs = gpsps.getAttributes();
	        attrs.putCS(Tags.GPSPSStatus, status);
	        gpsps.setTransactionUid(
	        		statusAsInt == GPSPSStatus.IN_PROGRESS ? tsuid : null);
	        addActualHumanPerformers(attrs, ds.get(Tags.ActualHumanPerformersSeq));
	        gpsps.setAttributes(attrs);
		} catch (ObjectNotFoundException e) {
			throw new DcmServiceException(Status.NoSuchObjectInstance);
		} catch (DcmServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new DcmServiceException(Status.ProcessingFailure, e);
		}
    }

	private void addActualHumanPerformers(Dataset attrs, DcmElement src) {
		if (src == null || src.vm() == 0) return;
        HashSet perfs = new HashSet();
        DcmElement dest = attrs.get(Tags.ActualHumanPerformersSeq);
        if (dest == null) {
        	dest = attrs.putSQ(Tags.ActualHumanPerformersSeq);
        } else {
        	Dataset item, code;
        	for (int i = 0, n = dest.vm(); i < n; ++i) {
        		item = dest.getItem(i);
        		code = item.getItem(Tags.HumanPerformerCodeSeq);
        		perfs.add(code.getString(Tags.CodeValue) + '\\'
        				+ code.getString(Tags.CodingSchemeDesignator));
        	}
        }
    	Dataset item, code;
    	for (int i = 0, n = src.vm(); i < n; ++i) {
    		item = src.getItem(i);
    		code = item.getItem(Tags.HumanPerformerCodeSeq);
    		if (code != null) {
	    		if (perfs.add(code.getString(Tags.CodeValue) + '\\'
	    				+ code.getString(Tags.CodingSchemeDesignator))) {
	    			dest.addItem(item);
	    		}
    		}
    	}        
	}
}
