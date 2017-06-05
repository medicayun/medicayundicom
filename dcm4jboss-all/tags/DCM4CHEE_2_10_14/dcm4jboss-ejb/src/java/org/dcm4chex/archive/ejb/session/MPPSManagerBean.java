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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocal;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocal;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 3206 $ $Date: 2007-03-14 23:33:21 +0800 (周三, 14 3月 2007) $
 * @since 21.03.2004
 *
 * @ejb.bean name="MPPSManager" type="Stateless" view-type="remote"
 * 	jndi-name="ejb/MPPSManager"
 * @ejb.transaction-type  type="Container"
 * @ejb.transaction type="Required"
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="MPPS" view-type="local" ref-name="ejb/MPPS" 
 * @ejb.ejb-ref ejb-name="MWLItem" view-type="local" ref-name="ejb/MWLItem"
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 * 
 */
public abstract class MPPSManagerBean implements SessionBean {

    private static Logger log = Logger.getLogger(MPPSManagerBean.class);
    private static final String NO_LONGER_BE_UPDATED_ERR_MSG =
        "Performed Procedure Step Object may no longer be updated";
    private static final int NO_LONGER_BE_UPDATED_ERR_ID = 0xA710;
	private static final int DELETED = 1;
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
	private SeriesLocalHome seriesHome;
    private MPPSLocalHome mppsHome;
	private MWLItemLocalHome mwlItemHome;
    private SessionContext sessionCtx;    

    public void setSessionContext(SessionContext ctx) {
        sessionCtx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
			seriesHome = (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
            mppsHome = (MPPSLocalHome) jndiCtx.lookup("java:comp/env/ejb/MPPS");
			mwlItemHome = (MWLItemLocalHome) jndiCtx.lookup("java:comp/env/ejb/MWLItem");
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
		seriesHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public void createMPPS(Dataset ds)
        throws DcmServiceException {
        try {
        	PatientLocal pat = getPatient(ds);
            mppsHome.create(ds.subSet(PATIENT_ATTRS_EXC, true, true), pat);
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
            final String issuer = ds.getString(Tags.IssuerOfPatientID);
            Collection c = issuer != null 
                    ? patHome.findByPatientIdWithIssuer(id, issuer)
                    : patHome.findByPatientId(id);
            for (Iterator it = c.iterator(); it.hasNext();) {
                PatientLocal patient = (PatientLocal) it.next();
                if (equals(patient, ds)) {
                    PatientLocal mergedWith;
                    while ((mergedWith = patient.getMergedWith()) != null) {
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
    }
    
    
    /**
     * Links a mpps to a mwl entry (LOCAL).
     * <p>
     * This method can be used if MWL entry is locally available.
     * <p>
     * Sets SpsID and AccessionNumber from mwl entry.
     * <P>
     * Returns a Map with following key/value pairs.
     * <dl>
     * <dt>mppsAttrs: (Dataset)</dt>
     * <dd>  Attributes of mpps entry. (for notification)</dd>
     * <dt>mwlPat: (Dataset)</dt>
     * <dd>  Patient of MWL entry.</dd>
     * <dd>  (The dominant patient of patient merge).</dd>
     * <dt>mppsPat: (Dataset)</dt>
     * <dd>  Patient of MPPS entry.</dd>
     * <dd>  (The merged patient).</dd>
     * </dl>
     * @param spsID spsID to select MWL entry
     * @param mppsIUID Instance UID of mpps.
     * 
     * @return A map with mpps attributes and patient attributes to merge.
     * 
     * @ejb.interface-method
     */
    public Map linkMppsToMwl(String spsID, String mppsIUID) throws DcmServiceException {
    	log.info("linkMppsToMwl sps:"+spsID+" mpps:"+mppsIUID);
		MWLItemLocal mwlItem;
        MPPSLocal mpps;
        Map map = new HashMap();
        try {
			mwlItem = mwlItemHome.findBySpsId(spsID);
            mpps = mppsHome.findBySopIuid(mppsIUID);
            String accNo = mwlItem.getAccessionNumber();
            PatientLocal mwlPat = mwlItem.getPatient();
            PatientLocal mppsPat = mpps.getPatient();
            Dataset mwlAttrs = mwlItem.getAttributes();
            Dataset mppsAttrs = mpps.getAttributes();
    		Dataset ssa;
    		DcmElement ssaSQ = mppsAttrs.get(Tags.ScheduledStepAttributesSeq);
    		String ssaSpsID, studyIUID = null;
    		boolean spsNotInList = true;
    		for ( int i = 0, len = ssaSQ.countItems() ; i < len ; i++ ) {
    			ssa = ssaSQ.getItem(i);
    			if ( ssa != null ) {
    				if ( studyIUID == null ) { 
    					studyIUID = ssa.getString(Tags.StudyInstanceUID);
			    		if ( !studyIUID.equals( 
			    				mwlAttrs.getString(Tags.StudyInstanceUID) ) ) {
			    			log.info("StudyInstanceUID corrected for spsID "+spsID);
			    			mwlAttrs.putUI(Tags.StudyInstanceUID, ssa.getString(Tags.StudyInstanceUID) );
			    			mwlItem.setAttributes( mwlAttrs );
			    		}
    				}
    				ssaSpsID = ssa.getString(Tags.SPSID);
	    			if ( ssaSpsID == null || spsID.equals(ssaSpsID) ) {
	    				ssa.putSH(Tags.AccessionNumber,accNo);
	    				ssa.putSH(Tags.SPSID, spsID);
	    				ssa.putUI(Tags.StudyInstanceUID, studyIUID);
	    				spsNotInList = false;
	    			}
    			}
    		}
    		if ( spsNotInList ) {
    			ssa = ssaSQ.addNewItem();
    			Dataset spsDS = mwlAttrs.getItem(Tags.SPSSeq);
    			ssa.putUI(Tags.StudyInstanceUID, studyIUID);
    			ssa.putSH(Tags.SPSID, spsID);
    			ssa.putSH(Tags.AccessionNumber, accNo);
    			ssa.putSQ(Tags.RefStudySeq);
    			ssa.putSH(Tags.RequestedProcedureID, mwlAttrs.getString(Tags.RequestedProcedureID));
    			ssa.putLO(Tags.SPSDescription, spsDS.getString(Tags.SPSDescription));
    			DcmElement mppsSPCSQ = ssa.putSQ(Tags.ScheduledProtocolCodeSeq);
    			DcmElement mwlSPCSQ = spsDS.get(Tags.ScheduledProtocolCodeSeq);
    			if ( mwlSPCSQ != null && mwlSPCSQ.countItems() > 0 ) {
    				for ( int i = 0, len = mwlSPCSQ.countItems() ; i < len ; i++ ) {
    					mppsSPCSQ.addNewItem().putAll(mwlSPCSQ.getItem(i));
    				}
    			}
    			log.debug("add new scheduledStepAttribute item:");log.info(ssa);
    			log.debug("new mppsAttrs:");log.debug(mppsAttrs);
    		}
            mpps.setAttributes(mppsAttrs);
            mppsAttrs.putAll(mppsPat.getAttributes(false));
            map.put("mppsAttrs",mppsAttrs);
            map.put("mwlAttrs",mwlAttrs);
            if ( ! mwlPat.equals(mppsPat) ) {
        		map.put( "mwlPat", mwlPat.getAttributes(true));
        		map.put( "mppsPat",mppsPat.getAttributes(true));
            }
            return map;
        } catch (ObjectNotFoundException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        } catch (FinderException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    /**
     * Links a mpps to a mwl entry (external).
     * <p>
     * This Method can be used to link a MPPS entry with an MWL entry from an external Modality Worklist.
     * <p>
     * Sets SpsID and AccessionNumber from mwlDs.
     * <P>
     * Returns a Map with following key/value pairs.
     * <dl>
     * <dt>mppsAttrs: (Dataset)</dt>
     * <dd>  Attributes of mpps entry. (for notification)</dd>
     * <dt>mwlPat: (Dataset)</dt>
     * <dd>  Patient of MWL entry.</dd>
     * <dd>  (The dominant patient of patient merge).</dd>
     * <dt>mppsPat: (Dataset)</dt>
     * <dd>  Patient of MPPS entry.</dd>
     * <dd>  (The merged patient).</dd>
     * </dl>
     * @param mwlDs Datset of MWL entry
     * @param mppsIUID Instance UID of mpps.
     * 
     * @return A map with mpps attributes and patient attributes to merge.
     * @throws FinderException 
     * @throws CreateException 
     * 
     * @ejb.interface-method
     */
    public Map linkMppsToMwl(Dataset mwlAttrs, String mppsIUID) throws DcmServiceException, FinderException, CreateException {
        String spsID = mwlAttrs.get(Tags.SPSSeq).getItem().getString(Tags.SPSID);
        log.info("linkMppsToMwl sps:"+spsID+" mpps:"+mppsIUID);
        MPPSLocal mpps;
        Map map = new HashMap();
        mpps = mppsHome.findBySopIuid(mppsIUID);
        String accNo = mwlAttrs.getString(Tags.AccessionNumber);
        AttributeFilter filter = AttributeFilter.getPatientAttributeFilter(null);
        Dataset mwlPatDs = filter.filter(mwlAttrs);
        PatientLocal mppsPat = mpps.getPatient();
        Dataset mppsAttrs = mpps.getAttributes();
        Dataset ssa;
        DcmElement ssaSQ = mppsAttrs.get(Tags.ScheduledStepAttributesSeq);
        String ssaSpsID, studyIUID = null;
        boolean spsNotInList = true;
        for ( int i = 0, len = ssaSQ.countItems() ; i < len ; i++ ) {
            ssa = ssaSQ.getItem(i);
            if ( ssa != null ) {
                if ( studyIUID == null ) { 
                    studyIUID = ssa.getString(Tags.StudyInstanceUID);
                    if ( !studyIUID.equals( 
                            mwlAttrs.getString(Tags.StudyInstanceUID) ) ) {
                        log.warn("StudyInstanceUID of external MWL entry can not be corrected! spsID "+spsID);
                    }
                }
                ssaSpsID = ssa.getString(Tags.SPSID);
                if ( ssaSpsID == null || spsID.equals(ssaSpsID) ) {
                    ssa.putSH(Tags.AccessionNumber,accNo);
                    ssa.putSH(Tags.SPSID, spsID);
                    ssa.putUI(Tags.StudyInstanceUID, studyIUID);
                    spsNotInList = false;
                }
            }
        }
        if ( spsNotInList ) {
            ssa = ssaSQ.addNewItem();
            Dataset spsDS = mwlAttrs.getItem(Tags.SPSSeq);
            ssa.putUI(Tags.StudyInstanceUID, studyIUID);
            ssa.putSH(Tags.SPSID, spsID);
            ssa.putSH(Tags.AccessionNumber, accNo);
            ssa.putSQ(Tags.RefStudySeq);
            ssa.putSH(Tags.RequestedProcedureID, mwlAttrs.getString(Tags.RequestedProcedureID));
            ssa.putLO(Tags.SPSDescription, spsDS.getString(Tags.SPSDescription));
            DcmElement mppsSPCSQ = ssa.putSQ(Tags.ScheduledProtocolCodeSeq);
            DcmElement mwlSPCSQ = spsDS.get(Tags.ScheduledProtocolCodeSeq);
            if ( mwlSPCSQ != null && mwlSPCSQ.countItems() > 0 ) {
                for ( int i = 0, len = mwlSPCSQ.countItems() ; i < len ; i++ ) {
                    mppsSPCSQ.addNewItem().putAll(mwlSPCSQ.getItem(i));
                }
            }
            log.debug("add new scheduledStepAttribute item:");log.info(ssa);
            log.debug("new mppsAttrs:");log.debug(mppsAttrs);
        }
        mpps.setAttributes(mppsAttrs);
        mppsAttrs.putAll(mppsPat.getAttributes(false));
        map.put("mppsAttrs",mppsAttrs);
        map.put("mwlAttrs",mwlAttrs);
        if ( ! isSamePatient(mwlPatDs,mppsPat) ) {
            Collection col = patHome.findByPatientIdWithIssuer(mwlPatDs.getString(Tags.PatientID), 
                    mwlPatDs.getString(Tags.IssuerOfPatientID));
            PatientLocal mwlPat = col.isEmpty() ? 
                    patHome.create(mwlPatDs) : (PatientLocal) col.iterator().next();
            map.put( "mwlPat", mwlPat.getAttributes(true));
            map.put( "mppsPat",mppsPat.getAttributes(true));
        }
        return map;
    }
    
    private boolean isSamePatient(Dataset mwlPatDs, PatientLocal mppsPat) {
        String mppsPatId = mppsPat.getPatientId();
        if ( mppsPatId == null ) {
            log.warn("Link MPPS to MWL: MPPS patient without PatientID! try to check via Patient Name");
            String name = mppsPat.getPatientName();
            if (name == null) {
                log.error("Link MPPS to MWL: MPPS patient without Patient Name! Ignore differences to avoid merge!");
                return true;
            }
            return name.equals(mwlPatDs.getString(Tags.PatientName));
        } else if ( ! mppsPat.getPatientId().equals(mwlPatDs.getString(Tags.PatientID) ) )
            return false;
        String issuer = mppsPat.getIssuerOfPatientId();
        return ( issuer != null ) ? issuer.equals(mwlPatDs.getString(Tags.IssuerOfPatientID)) : true;
    }

    /**
     * @ejb.interface-method
     */
    public void unlinkMpps( String mppsIUID ) throws FinderException {
    	MPPSLocal mpps = mppsHome.findBySopIuid(mppsIUID);
    	Dataset mppsAttrs = mpps.getAttributes();
		DcmElement ssaSQ = mppsAttrs.get(Tags.ScheduledStepAttributesSeq);
		Dataset ds = null;
		String spsID;
		for ( int i = ssaSQ.countItems()-1 ; i >= 0 ; i-- ) {
			ds = ssaSQ.getItem(i);
			spsID = ds.getString(Tags.SPSID);
			if ( spsID != null ) {
				try {
					MWLItemLocal mwlItem = mwlItemHome.findBySpsId(spsID);
					Dataset mwlDS = mwlItem.getAttributes();
					mwlDS.getItem(Tags.SPSSeq).putCS(Tags.SPSStatus, "SCHEDULED");
					mwlItem.setAttributes(mwlDS);
				} catch ( FinderException ignore ) {}
			}
		}
		String studyIUID = ds.getString(Tags.StudyInstanceUID);
		ds.clear();
		ds.putUI(Tags.StudyInstanceUID, studyIUID);
		//add empty type 2 attributes.
		ds.putSH(Tags.SPSID, (String)null);
		ds.putSH(Tags.AccessionNumber, (String)null);
		ds.putSQ(Tags.RefStudySeq);
		ds.putSH(Tags.RequestedProcedureID, (String)null);
		ds.putLO(Tags.SPSDescription, (String)null);
		ds.putSQ(Tags.ScheduledProtocolCodeSeq);
		mppsAttrs.putSQ(Tags.ScheduledStepAttributesSeq).addItem(ds);
    	mpps.setAttributes(mppsAttrs);
    }
    
    /**
     * Delete a list of mpps entries.
     *  
     * @ejb.interface-method
     */
    public boolean deleteMPPSEntries( String[] iuids ) {
    	for ( int i = 0 ; i < iuids.length ; i++ ) {
    		try {
				mppsHome.findBySopIuid( iuids[i] ).remove();
			} catch (Exception x) {
				log.error("Cant delete mpps:"+iuids[i], x);
			}
    	}
    	return true;
    }
    
    /**
     * @ejb.interface-method
     */
    public Collection getSeriesIUIDs(String mppsIUID) throws FinderException {
        Collection col = new ArrayList();
    	Collection series = seriesHome.findByPpsIuid(mppsIUID);
        for ( Iterator iter = series.iterator() ; iter.hasNext() ; ) {
        	col.add( ( (SeriesLocal) iter.next()).getSeriesIuid() );
        }
        return col;
    }

    /**
     * @ejb.interface-method
     */
    public Collection getSeriesAndStudyDS(String mppsIUID) throws FinderException {
        Collection col = new ArrayList();
    	Collection seriess = seriesHome.findByPpsIuid(mppsIUID);
    	SeriesLocal series;
    	Dataset ds;
        for ( Iterator iter = seriess.iterator() ; iter.hasNext() ; ) {
            series = (SeriesLocal) iter.next();
            ds = series.getAttributes(true);
            ds.putAll(series.getStudy().getAttributes(true));
        	col.add( ds );
        }
        return col;
    }
 
    /**
     * Returns a StudyMgt Dataset.
     * 
     * @ejb.interface-method
     */
    public Dataset updateSeriesAndStudy(Collection seriesDS) throws FinderException {
        Dataset ds = null;
        String iuid;
        SeriesLocal series = null;
        Dataset dsN = DcmObjectFactory.getInstance().newDataset();
        DcmElement refSeriesSeq = dsN.putSQ( Tags.RefSeriesSeq );
        Dataset dsSer;
        for ( Iterator iter = seriesDS.iterator() ; iter.hasNext() ; ) {
            ds = (Dataset) iter.next();
            iuid = ds.getString(Tags.SeriesInstanceUID);
            series = seriesHome.findBySeriesIuid(iuid);
	        series.setAttributes(ds);
            dsSer = refSeriesSeq.addNewItem();
            dsSer.putAll(series.getAttributes(true));
            Iterator iter2 = series.getInstances().iterator();
            if ( iter2.hasNext()) {
                DcmElement refSopSeq = dsSer.putSQ( Tags.RefSOPSeq );
                InstanceLocal il;
                Dataset dsInst;
                while ( iter2.hasNext() ) {
                    il = (InstanceLocal) iter2.next();
                    dsInst = refSopSeq.addNewItem();
                    dsInst.putUI( Tags.RefSOPClassUID, il.getSopCuid() );
                    dsInst.putUI( Tags.RefSOPInstanceUID, il.getSopIuid() );
                    dsInst.putAE( Tags.RetrieveAET, il.getRetrieveAETs() );
                }
            }
        }
        if ( series != null ) {
           StudyLocal study = series.getStudy();
           study.setAttributes(ds);
           dsN.putAll( study.getAttributes(true) );
        }
        return dsN;
    }
}
