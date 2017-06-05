/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.management.ObjectName;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1783 $ $Date: 2005-06-08 23:16:13 +0800 (周三, 08 6月 2005) $
 * @since 17.02.2005
 * 
 */

public class ORMService extends AbstractHL7Service {

	private static final String[] CONTROL_CODES = { "NW", "XA", "CA", "DC" };

	private static final List CONTROL_CODES_LIST = Arrays.asList(CONTROL_CODES);

	private static final int NW = 0;

	private static final int XA = 1;

	private static final int CA = 2;

	private static final int DC = 3;
	
    private ObjectName deviceServiceName;

	private String stylesheetURL = "resource:xsl/hl7/orm2dcm.xsl";

	private String defaultStationAET = "UNKOWN";

	private String defaultStationName = "UNKOWN";

	private String defaultModality = "OT";

	public String getStylesheetURL() {
		return stylesheetURL;
	}

	public void setStylesheetURL(String stylesheetURL) {
		this.stylesheetURL = stylesheetURL;
		reloadStylesheets();
	}

	public final ObjectName getDeviceServiceName() {
		return deviceServiceName;
	}
	
	public final void setDeviceServiceName(ObjectName deviceServiceName) {
		this.deviceServiceName = deviceServiceName;
	}
	
	public final String getDefaultModality() {
		return defaultModality;
	}
	
	public final void setDefaultModality(String defaultModality) {
		this.defaultModality = defaultModality;
	}
	
	public final String getDefaultStationAET() {
		return defaultStationAET;
	}
	
	public final void setDefaultStationAET(String defaultStationAET) {
		this.defaultStationAET = defaultStationAET;
	}
	
	public final String getDefaultStationName() {
		return defaultStationName;
	}
	
	public final void setDefaultStationName(String defaultStationName) {
		this.defaultStationName = defaultStationName;
	}
	
	public boolean process(MSH msh, Document msg, ContentHandler hl7out)
			throws HL7Exception {
		int msgType = checkMessage(msg);
		try {
			Dataset ds = dof.newDataset();
			Transformer t = getTemplates(stylesheetURL).newTransformer();
			t.transform(new DocumentSource(msg), new SAXResult(ds
					.getSAXHandler2(null)));
			mergeProtocolCodes(ds);
			if (msgType == NW || msgType == XA) {
				ds = addScheduledStationInfo(ds);
			}
			MWLManager mwlManager = getMWLManagerHome().create();
			try {
				DcmElement spsSq = ds.remove(Tags.SPSSeq);
				Dataset sps;
				for (int i = 0, n = spsSq.vm(); i < n; ++i) {
					sps = spsSq.getItem(i);
					switch (msgType) {
					case NW:
						ds.putSQ(Tags.SPSSeq).addItem(sps);
						adjustAttributes(ds);
						addMissingAttributes(ds);
						log("Schedule", ds);
						logDataset("Insert MWL Item:", ds);
						mwlManager.addWorklistItem(ds);
						break;
					case XA:
						ds.putSQ(Tags.SPSSeq).addItem(sps);
						adjustAttributes(ds);
						log("Update", ds);
						logDataset("Update MWL Item:", ds);
						mwlManager.updateWorklistItem(ds);
						break;
					case CA:
					case DC:
						log("Cancel", ds);
						mwlManager.removeWorklistItem(sps.getString(Tags.SPSID));
						break;
					default:
						throw new RuntimeException();
					}
				}
			} finally {
				mwlManager.remove();
			}
		} catch (Exception e) {
			throw new HL7Exception("AE", e.getMessage(), e);
		}
		return true;
	}

	private void log(String op, Dataset ds) {
		Dataset sps = ds.getItem(Tags.SPSSeq);
        log.info(op + " Procedure Step[id:" + ( sps==null ? "<unknown>(SPSSeq missing)":sps.getString(Tags.SPSID) )
        		+ "] of Study[uid:" + ds.getString(Tags.StudyInstanceUID)
        		+ "] of Order[accNo:" + ds.getString(Tags.AccessionNumber)
        		+ "] for Patient [name:" + ds.getString(Tags.PatientName)
        		+ ",id:" + ds.getString(Tags.PatientID) + "]");
	}

	private MWLManagerHome getMWLManagerHome() throws HomeFactoryException {
		return (MWLManagerHome) EJBHomeFactory.getFactory().lookup(
				MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
	}

	private int checkMessage(Document msg) throws HL7Exception {
		// TODO check message, throw HL7Exception.AR if check failed
		String orc1 = msg.getRootElement().element("ORC").elementText("field");
		int msgType = CONTROL_CODES_LIST.indexOf(orc1);
		if (msgType == -1)
			throw new HL7Exception("AR", "Illegal Order Control Code ORC-1:"
					+ orc1);
		return msgType;
	}
	
	private Dataset addScheduledStationInfo(Dataset spsItems) throws Exception {
        return (Dataset) server.invoke(deviceServiceName,
        		"addScheduledStationInfo", new Object[] { spsItems },
        		new String[] { Dataset.class.getName() });
	}
	
	private void addMissingAttributes(Dataset ds) {
		Dataset sps = ds.getItem(Tags.SPSSeq);
		if (sps.vm(Tags.ScheduledStationAET) < 1) {
			log.info("No Scheduled Station AET - use default: " + defaultStationAET);
			sps.putAE(Tags.ScheduledStationAET, defaultStationAET);
		}
		if (sps.vm(Tags.ScheduledStationName) < 1) {			
			log.info("No Scheduled Station Name - use default: " + defaultStationName);
			sps.putSH(Tags.ScheduledStationName, defaultStationName);
		}
		if (sps.vm(Tags.Modality) < 1) {			
			log.info("No Modality - use default: " + defaultModality);
			sps.putCS(Tags.Modality, defaultModality);
		}
		if (sps.vm(Tags.SPSStartDate) < 1) {
			log.info("No SPS Start Date - use current date/time");
			Date now = new Date();
			sps.putDA(Tags.SPSStartDate, now);
			sps.putTM(Tags.SPSStartTime, now);
		}
	}
	
	private void adjustAttributes(Dataset ds) {
		Dataset sps = ds.getItem(Tags.SPSSeq);
		String val;
		Dataset code;
		if ((val = sps.getString(Tags.RequestingPhysician)) != null) {
			log.info("Detect Requesting Physician on SPS Level");
			ds.putPN(Tags.RequestingPhysician, val);
			sps.remove(Tags.RequestingPhysician);
		}
		if ((val = sps.getString(Tags.RequestingService)) != null) {
			log.info("Detect Requesting Service on SPS Level");
			ds.putLO(Tags.RequestingService, val);
			sps.remove(Tags.RequestingService);
		}
		if ((val = sps.getString(Tags.StudyInstanceUID)) != null) {
			log.info("Detect Study Instance UID on SPS Level");
			ds.putUI(Tags.StudyInstanceUID, val);
			sps.remove(Tags.StudyInstanceUID);
		}
		if ((val = sps.getString(Tags.RequestedProcedurePriority)) != null) {
			log.info("Detect Requested Procedure Priority on SPS Level");
			ds.putCS(Tags.RequestedProcedurePriority, val);
			sps.remove(Tags.RequestedProcedurePriority);
		}
		if ((val = sps.getString(Tags.RequestedProcedureID)) != null) {
			log.info("Detect Requested Procedure ID on SPS Level");
			ds.putSH(Tags.RequestedProcedureID, val);
			sps.remove(Tags.RequestedProcedureID);
		}
		if ((val = sps.getString(Tags.RequestedProcedureDescription)) != null) {
			log.info("Detect Requested Procedure Description on SPS Level");
			ds.putLO(Tags.RequestedProcedureDescription, val);
			sps.remove(Tags.RequestedProcedureDescription);
		}
		if ((code = sps.getItem(Tags.RequestedProcedureCodeSeq)) != null) {
			log.info("Detect Requested Procedure Code on SPS Level");
			ds.putSQ(Tags.RequestedProcedureCodeSeq).addItem(code);
			sps.remove(Tags.RequestedProcedureCodeSeq);
		}				
	}

	private void mergeProtocolCodes(Dataset orm) {
		DcmElement prevSpsSq = orm.remove(Tags.SPSSeq);
		DcmElement newSpsSq = orm.putSQ(Tags.SPSSeq);
		HashMap spcSqMap = new HashMap();
		DcmElement spcSq0, spcSqI;
		Dataset sps;
		String spsid;
		for (int i = 0, n = prevSpsSq.vm(); i < n; ++i) {
			sps = prevSpsSq.getItem(i);
			spsid = sps.getString(Tags.SPSID);
			spcSqI = sps.get(Tags.ScheduledProtocolCodeSeq);
			spcSq0 = (DcmElement) spcSqMap.get(spsid);
			if (spcSq0 != null) {
				spcSq0.addItem(spcSqI.getItem());
			} else {
				spcSqMap.put(spsid, spcSqI);
				newSpsSq.addItem(sps);
			}
		}
	}
}
