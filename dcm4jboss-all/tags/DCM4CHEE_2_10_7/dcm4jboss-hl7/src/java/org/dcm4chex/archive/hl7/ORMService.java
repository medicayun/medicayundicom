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

package org.dcm4chex.archive.hl7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2913 $ $Date: 2006-10-31 23:50:00 +0800 (周二, 31 10月 2006) $
 * @since 17.02.2005
 * 
 */

public class ORMService extends AbstractHL7Service {
    
    private static final String[] OP_CODES = { "NW", "XO", "CA", "SC(IP)",
        "SC(CM)", "SC(DC)", "NOOP" };
    
    private static final List OP_CODES_LIST = Arrays.asList(OP_CODES);

    private static final int NW = 0;

    private static final int XO = 1;

    private static final int CA = 2;

    private static final int SC_IP = 3;
    
    private static final int SC_CM = 4;

    private static final int SC_DC = 5;

    private static final int NOOP = 6;

    private List orderControls;
    
    private int[] ops = {};
    
    private ObjectName deviceServiceName;

    private String stylesheetURL = "resource:dcm4chee-hl7/orm2dcm.xsl";

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

    public String getOrderControlOperationMap() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ops.length; i++) {
            sb.append(orderControls.get(i)).append(':')
                .append(OP_CODES[ops[i]]).append("\r\n");
        }
        return sb.toString();
    }

    public void setOrderControlOperationMap(String s) {
        StringTokenizer stk = new StringTokenizer(s, " \r\n\t,;");
        int lines = stk.countTokens();
        int[] newops = new int[lines];
        List newocs = new ArrayList(lines);
        for (int i = 0; i < lines; i++) {
            String[] ocop = StringUtils.split(stk.nextToken(), ':');
            if (ocop.length != 2
                    || (newops[i] = OP_CODES_LIST.indexOf(ocop[1])) == -1) {
                throw new IllegalArgumentException(s);
            }
            newocs.add(ocop[0]);            
        }
        ops = newops;
        orderControls = newocs;
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
        int op = toOp(msg);
        try {
            Dataset ds = dof.newDataset();
            Transformer t = getTemplates(stylesheetURL).newTransformer();
            t.transform(new DocumentSource(msg), new SAXResult(ds
                    .getSAXHandler2(null)));
            final String pid = ds.getString(Tags.PatientID);
            if (pid == null)
                throw new HL7Exception("AR",
                        "Missing required PID-3: Patient ID (Internal ID)");
            final String pname = ds.getString(Tags.PatientName);
            if (pname == null)
                throw new HL7Exception("AR",
                        "Missing required PID-5: Patient Name");
            mergeProtocolCodes(ds);
            if (op == NW || op == XO) {
                ds = addScheduledStationInfo(ds);
            }
            MWLManager mwlManager = getMWLManagerHome().create();
            DcmElement spsSq = ds.remove(Tags.SPSSeq);
            Dataset sps;
            String spsid;
            for (int i = 0, n = spsSq.countItems(); i < n; ++i) {
                sps = spsSq.getItem(i);
                spsid = sps.getString(Tags.SPSID);
                ds.putSQ(Tags.SPSSeq).addItem(sps);
                switch (op) {
                case NW:
                    adjustAttributes(ds);
                    addMissingAttributes(ds);
                    log("Schedule", ds);
                    logDataset("Insert MWL Item:", ds);
                    mwlManager.addWorklistItem(ds);
                    break;
                case XO:
                    adjustAttributes(ds);
                    log("Update", ds);
                    logDataset("Update MWL Item:", ds);
                    if (!mwlManager.updateWorklistItem(ds)) {
                        log("No Such ", ds);
                        addMissingAttributes(ds);
                        log("->Schedule New ", ds);
                        logDataset("Insert MWL Item:", ds);
                        mwlManager.addWorklistItem(ds);
                    }
                    break;
                case CA:
                    log("Cancel", ds);
                    mwlManager.removeWorklistItem(spsid);
                    break;
                case SC_IP:
                    log("Change SPS status to IN PROGRESS", ds);
                    mwlManager.updateSPSStatus(spsid, "IN PROGRESS");
                    break;
                case SC_CM:
                    log("Change SPS status to COMPLETED", ds);
                    mwlManager.updateSPSStatus(spsid, "COMPLETED");
                    break;
                case SC_DC:
                    log("Change SPS status to DISCONTINUED", ds);
                    mwlManager.updateSPSStatus(spsid, "DISCONTINUED");
                    break;
                case NOOP:
                    log("NOOP", ds);
                    break;
                default:
                    throw new RuntimeException();
                }
            }
        } catch (HL7Exception e) {
            throw e;
        } catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
        return true;
    }

    private void log(String op, Dataset ds) {
        Dataset sps = ds.getItem(Tags.SPSSeq);
        log.info(op
                + " Procedure Step[id:"
                + (sps == null ? "<unknown>(SPSSeq missing)" : sps
                        .getString(Tags.SPSID)) + "] of Study[uid:"
                + ds.getString(Tags.StudyInstanceUID) + "] of Order[accNo:"
                + ds.getString(Tags.AccessionNumber) + "] for Patient [name:"
                + ds.getString(Tags.PatientName) + ",id:"
                + ds.getString(Tags.PatientID) + "]");
    }

    private MWLManagerHome getMWLManagerHome() throws HomeFactoryException {
        return (MWLManagerHome) EJBHomeFactory.getFactory().lookup(
                MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
    }

    private int toOp(Document msg) throws HL7Exception {
        List orc = msg.getRootElement().element("ORC").elements("field");
        String orderControl = getText(orc, 0);
        String orderStatus = getText(orc, 4);
        if (orderStatus.length() == 0) {
            // use Result Status (OBR-25), if no Order Status (ORC-5);
            List obr = msg.getRootElement().element("OBR").elements("field");
            orderStatus = getText(obr, 24);
        }
        int opIndex = orderControls.indexOf(orderControl + "(" + orderStatus + ")");
        if (opIndex == -1) {
            opIndex = orderControls.indexOf(orderControl);
            if (opIndex == -1) {
                throw new HL7Exception("AR", "Illegal Order Control Code ORC-1:"
                        + orderControl);                 
            }
        }
        return ops[opIndex];
    }

    private String getText(List fields, int i) throws HL7Exception {
        try {
            return ((Element) fields.get(i)).getText();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private Dataset addScheduledStationInfo(Dataset spsItems) throws Exception {
        return (Dataset) server.invoke(deviceServiceName,
                "addScheduledStationInfo", new Object[] { spsItems },
                new String[] { Dataset.class.getName() });
    }

    private void addMissingAttributes(Dataset ds) {
        Dataset sps = ds.getItem(Tags.SPSSeq);
        if (sps.vm(Tags.ScheduledStationAET) < 1) {
            log.info("No Scheduled Station AET - use default: "
                    + defaultStationAET);
            sps.putAE(Tags.ScheduledStationAET, defaultStationAET);
        }
        if (sps.vm(Tags.ScheduledStationName) < 1) {
            log.info("No Scheduled Station Name - use default: "
                    + defaultStationName);
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
        for (int i = 0, n = prevSpsSq.countItems(); i < n; ++i) {
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
