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
package org.dcm4chee.web.service.mppsforward;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.pdu.PresentationContext;

import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.service.common.AbstractScuService;

/**
 * @author robert.david@agfa.com
 */
public class MppsForwardService extends AbstractScuService {

	private static final int[] MPPS_CREATE_TAGS = { Tag.SpecificCharacterSet,
        Tag.Modality, Tag.ProcedureCodeSequence,
        Tag.ReferencedPatientSequence, Tag.PatientName, Tag.PatientID,
        Tag.IssuerOfPatientID, Tag.PatientBirthDate, Tag.PatientSex,
        Tag.StudyID, Tag.PerformedStationAETitle, Tag.PerformedStationName,
        Tag.PerformedLocation, Tag.PerformedProcedureStepStartDate, 
        Tag.PerformedProcedureStepStartTime, Tag.PerformedProcedureStepEndDate, 
        Tag.PerformedProcedureStepEndTime, Tag.PerformedProcedureStepStatus, 
        Tag.PerformedProcedureStepID, Tag.PerformedProcedureStepDescription, 
        Tag.PerformedProcedureTypeDescription, Tag.PerformedProtocolCodeSequence, 
        Tag.ScheduledStepAttributesSequence };

	private static final int[] MPPS_SET_TAGS = { Tag.SpecificCharacterSet,
        Tag.PerformedProcedureStepEndDate, 
        Tag.PerformedProcedureStepEndTime, Tag.PerformedProcedureStepStatus, 
        Tag.PerformedSeriesSequence };

    public String sendMPPS(DicomObject mpps, String aet) throws Exception {

    	Association as = null;
    	final StringBuffer result = new StringBuffer();
    	try {
	    	TransferCapability tc = new TransferCapability();
	    	tc.setSopClass(UID.ModalityPerformedProcedureStepSOPClass);
	    	tc.setTransferSyntax(new String[] {UID.ImplicitVRLittleEndian});
	        this.setTransferCapability(new TransferCapability[] {tc});

	    	as = open(aet);
	    	
	    	if (as.getAssociateRQ().getPresentationContexts().size() == 0)
	    		throw new Exception("No Presentation Context for ASSOCIATE RQ");
	   	
	    	PresentationContext pc = as.getAssociateRQ().getPresentationContexts().iterator().next();
	    	
	    	DimseRSPHandler rspHandler = new DimseRSPHandler() {
	    		
				@Override
	            public void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
					result.append(cmd.toString());
					if (cmd.getString(Tag.ErrorID) != null)
						result.append("FORWARDING FAILED");
					else
						result.append("COMPLETED SUCCESSFULLY");
	            }
	    	};
	    	
	    	String cuid = UID.ModalityPerformedProcedureStepSOPClass;
	    	String iuid = mpps.getString(Tag.SOPInstanceUID);
	    	
	    	mpps.putString(Tag.PerformedProcedureStepStatus, VR.CS, "IN PROGRESS");
	    	as.ncreate(cuid, iuid, mpps.subSet(MPPS_CREATE_TAGS), pc.getTransferSyntax(), rspHandler);
	    	
	    	mpps.putString(Tag.PerformedProcedureStepStatus, VR.CS, "COMPLETED");
	    	as.nset(cuid, iuid, mpps.subSet(MPPS_SET_TAGS), pc.getTransferSyntax(), rspHandler);  	
	    	as.waitForDimseRSP();
	    	
	    	return result.toString();
        } finally {
            try {
                as.release(true);
            } catch (Exception e) {
                log.error("Failed to release association");
            }
        }
    }
    
    public String forwardMPPS(String mppsUID, String aet) throws Exception {
        StudyListLocal dao = (StudyListLocal) JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        MPPS mpps = dao.findMPPS(mppsUID);
        if (mpps != null) {
            DicomObject attrs = mpps.getAttributes();
            mpps.getPatient().getAttributes().copyTo(attrs);
            return sendMPPS(attrs, aet);
        }
        return ("MPPS with InstanceUID "+mppsUID+" not found!");
    }

}

