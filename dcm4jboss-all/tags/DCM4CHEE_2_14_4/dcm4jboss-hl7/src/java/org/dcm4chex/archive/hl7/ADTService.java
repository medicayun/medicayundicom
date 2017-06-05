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
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.PatientMatching;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdate;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdateHome;
import org.dcm4chex.archive.exceptions.PatientAlreadyExistsException;
import org.dcm4chex.archive.exceptions.PatientMergedException;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.regenstrief.xhl7.HL7XMLLiterate;
import org.xml.sax.ContentHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2009-05-13 17:04:42 +0800 (周三, 13 5月 2009) $
 * @since 26.12.2004
 */

public class ADTService extends AbstractHL7Service {

    private static final int ID = 0;

    private static final int ISSUER = 1;

    private String pidXslPath;

    private String mrgXslPath;

    private String patientArrivingMessageType;

    private String[] patientMergeMessageTypes;

    private String changePatientIdentifierListMessageType;

    private String deletePatientMessageType;

    private String pixUpdateNotificationMessageType;

    private String[] issuersOfOnlyOtherPatientIDs;
    private Pattern ignoredIssuersOfPatientIDPattern;

    private PatientMatching patientMatching;

    private boolean ignoreDeleteErrors;

    private boolean handleEmptyMrgAsUpdate;

    private ObjectName contentEditServiceName;

    public final String getPatientArrivingMessageType() {
        return patientArrivingMessageType;
    }

    public final void setPatientArrivingMessageType(
            String patientArrivingMessageType) {
        this.patientArrivingMessageType = patientArrivingMessageType;
    }

    public final String getPatientMergeMessageTypes() {
        return StringUtils.toString(patientMergeMessageTypes, ',');
    }

    public final void setPatientMergeMessageTypes(String messageTypes) {
        this.patientMergeMessageTypes = split(messageTypes);
    }

    public ObjectName getContentEditServiceName() {
        return contentEditServiceName;
    }

    public void setContentEditServiceName(ObjectName serviceName) {
        this.contentEditServiceName = serviceName;
    }

    private static String[] split(String s) {
        StringTokenizer stk = new StringTokenizer(s, ", ");
        String[] tmp = new String[stk.countTokens()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = stk.nextToken();
        }
        return tmp;
    }

    public final String getChangePatientIdentifierListMessageType() {
        return changePatientIdentifierListMessageType;
    }

    public final void setChangePatientIdentifierListMessageType(
            String changePatientIdentifierListMessageType) {
        this.changePatientIdentifierListMessageType = changePatientIdentifierListMessageType;
    }

    public final String getDeletePatientMessageType() {
        return deletePatientMessageType;
    }

    public final void setDeletePatientMessageType(
            String deletePatientMessageType) {
        this.deletePatientMessageType = deletePatientMessageType;
    }

    public final String getPixUpdateNotificationMessageType() {
        return pixUpdateNotificationMessageType;
    }

    public final void setPixUpdateNotificationMessageType(String messageType) {
        this.pixUpdateNotificationMessageType = messageType;
    }

    public final String getIssuersOfOnlyOtherPatientIDs() {
        return issuersOfOnlyOtherPatientIDs == null ? "-"
                : StringUtils.toString(issuersOfOnlyOtherPatientIDs, ',');
    }

    public final void setIssuersOfOnlyOtherPatientIDs(String s) {
        issuersOfOnlyOtherPatientIDs = s.trim().equals("-") ? null : split(s);
    }

    public final String getIgnoredIssuersOfPatientIDPattern() {
        return ignoredIssuersOfPatientIDPattern == null ? "NONE" 
                : ignoredIssuersOfPatientIDPattern.pattern();  
    }

    public final void setIgnoredIssuersOfPatientIDPattern(String ignoredIssuersOfPatientIDPattern) {
        if (ignoredIssuersOfPatientIDPattern.compareToIgnoreCase("NONE") == 0) {
                this.ignoredIssuersOfPatientIDPattern = null;
        } else {
                this.ignoredIssuersOfPatientIDPattern = 
                    Pattern.compile(ignoredIssuersOfPatientIDPattern);
        }
    }

    public String getPatientMatching() {
        return patientMatching.toString();
    }

    public void setPatientMatching(String s) {
        this.patientMatching = new PatientMatching(s.trim());
    }

    public final String getMrgStylesheet() {
        return mrgXslPath;
    }

    public void setMrgStylesheet(String path) {
        this.mrgXslPath = path;
    }

    public final String getPidStylesheet() {
        return pidXslPath;
    }

    public void setPidStylesheet(String path) {
        this.pidXslPath = path;
    }

    /**
     * @return Returns the ignoreNotFound.
     */
    public boolean isIgnoreDeleteErrors() {
        return ignoreDeleteErrors;
    }

    /**
     * @param ignoreNotFound
     *                The ignoreNotFound to set.
     */
    public void setIgnoreDeleteErrors(boolean ignore) {
        this.ignoreDeleteErrors = ignore;
    }

    public boolean isHandleEmptyMrgAsUpdate() {
        return handleEmptyMrgAsUpdate;
    }

    public void setHandleEmptyMrgAsUpdate(boolean handleEmptyMrgAsUpdate) {
        this.handleEmptyMrgAsUpdate = handleEmptyMrgAsUpdate;
    }

    public boolean process(MSH msh, Document msg, ContentHandler hl7out)
            throws HL7Exception {
        try {
            String msgtype = msh.messageType + '^' + msh.triggerEvent;
            if (pixUpdateNotificationMessageType.equals(msgtype)
                    && !containsPatientName(msg)) {
                return processUpdateNotificationMessage(msg);
            }
            Dataset pat = xslt(msg, pidXslPath);
            checkPID(pat);
            PatientUpdate update = getPatientUpdate();
            if (contains(patientMergeMessageTypes, msgtype)) {
                Dataset mrg = xslt(msg, mrgXslPath);
                try {
                    checkMRG(mrg);
                    update.mergePatient(pat, mrg, patientMatching);
                }
                catch (HL7Exception e) {
                    if (handleEmptyMrgAsUpdate) {
                        update.updatePatient(pat, patientMatching);
                    }
                    else {
                        throw e;
                    }
                }
            }
            else if (changePatientIdentifierListMessageType.equals(msgtype)) {
                Dataset mrg = xslt(msg, mrgXslPath);
                checkMRG(mrg);
                update.changePatientIdentifierList(pat, mrg, patientMatching);
            }
            else if (deletePatientMessageType.equals(msgtype)) {
                try {
                    movePatientToTrash(pat, patientMatching);
                }
                catch (Exception x) {
                    if (!ignoreDeleteErrors) {
                        throw x;
                    }
                }
            }
            else if (patientArrivingMessageType.equals(msgtype)) {
                update.patientArrived(pat, patientMatching);
            }
            else {
                update.updatePatient(pat, patientMatching);
            }
        }
        catch (HL7Exception e) {
            throw e;
        }
        catch (PatientAlreadyExistsException e) {
            throw new HL7Exception("AR", e.getMessage());
        }
        catch (PatientMergedException e) {
            throw new HL7Exception("AR", e.getMessage());
        }
        catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
        return true;
    }

    private void movePatientToTrash(Dataset pat, PatientMatching matching)
            throws Exception {
        try {
            server.invoke(contentEditServiceName, "movePatientToTrash",
                    new Object[] { pat, matching },
                    new String[] { Dataset.class.getName(),
                                   PatientMatching.class.getName() });
        } catch (MBeanException e) {
            throw e.getTargetException();
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (ReflectionException e) {
            throw e;
        }
    }

    private static boolean contains(String[] ss, String v) {
        for (String s : ss) {
            if (s.equals(v)) {
                return true;
            }
        }
        return false;
    }

    public void updatePatient(Document msg) throws HL7Exception {
        try {
            Dataset pid = xslt(msg, pidXslPath);
            checkPID(pid);
            getPatientUpdate().updatePatient(pid, patientMatching);
        }
        catch (HL7Exception e) {
            throw e;
        }
        catch (PatientMergedException e) {
            throw new HL7Exception("AR", e.getMessage());
        }
        catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
    }

    public void mergePatient(Document msg) throws HL7Exception {
        try {
            Dataset pid = xslt(msg, pidXslPath);
            Dataset mrg = xslt(msg, mrgXslPath);
            checkPID(pid);
            checkMRG(mrg);
            getPatientUpdate().mergePatient(pid, mrg, patientMatching);
        }
        catch (HL7Exception e) {
            throw e;
        }
        catch (PatientMergedException e) {
            throw new HL7Exception("AR", e.getMessage());
        }
        catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
    }

    private void checkPID(Dataset pid) throws HL7Exception {
        if (!pid.containsValue(Tags.PatientID))
            throw new HL7Exception("AR",
                    "Missing required PID-3: Patient ID (Internal ID)");
        if (!pid.containsValue(Tags.PatientName))
            throw new HL7Exception("AR", "Missing required PID-5: Patient Name");
    }

    private void checkMRG(Dataset mrg) throws HL7Exception {
        if (!mrg.containsValue(Tags.PatientID))
            throw new HL7Exception("AR",
                    "Missing required MRG-1: Prior Patient ID - Internal");
    }

    private boolean containsPatientName(Document msg) {
        Element pidSegm = msg.getRootElement().element("PID");
        if (pidSegm == null) {
            return false;
        }
        List pidfds = pidSegm.elements(HL7XMLLiterate.TAG_FIELD);
        if (pidfds.size() < 5) {
            return false;
        }
        String pname = ((Element) pidfds.get(4)).getTextTrim();
        return (pname != null && pname.length() > 0);
    }

    private boolean processUpdateNotificationMessage(Document msg)
            throws Exception {
    	String fn = "processUpdateNotificationMessage: ";
        List pids;
        try {
            pids = new PID(msg).getPatientIDs();
        }
        catch (IllegalArgumentException e) {
            throw new HL7Exception("AR", e.getMessage());
        }

        if (ignoredIssuersOfPatientIDPattern != null) {
            ArrayList filteredPids = new ArrayList();
            for (int i = 0, n = pids.size(); i < n; ++i) {
            	String[] pid = (String[]) pids.get(i);
            	StringBuilder issuer = new StringBuilder();
            	for (int s = 1; s < pid.length; s++) {
            		if (s != 1) {
            			issuer.append('&');
            		}
            		issuer.append(pid[s]);
            	}
            	Matcher matcher = ignoredIssuersOfPatientIDPattern.matcher(issuer);
            	if (matcher.matches()) {
                	if (log.isDebugEnabled()) {
                		log.debug(fn + "filtering out patient ID '" + pid[0] + "' with issuer '" + issuer + "' because the issuer matches the expression '" + ignoredIssuersOfPatientIDPattern.pattern() + "'");
                	}
            	} else {
            		log.debug(fn + "keeping patient ID '" + pid[0] + "' with issuer '" + issuer + "'");
            		filteredPids.add(pid);
            	}
            }
            pids = filteredPids;
        }
        
        PatientUpdate patUpdate = getPatientUpdate();
        for (int i = 0, n = pids.size(); i < n; ++i) {
            String[] pid = (String[]) pids.get(i);
            if (!contains(issuersOfOnlyOtherPatientIDs, pid[ISSUER])) {
                Dataset ds = toDataset(pid);
                DcmElement opids = ds.putSQ(Tags.OtherPatientIDSeq);
                for (int j = 0, m = pids.size(); j < m; ++j) {
                    String[] opid = (String[]) pids.get(j);
                    if (opid != pid) {
                        opids.addItem(toDataset(opid));
                    }
                }
                patUpdate.updateOtherPatientIDsOrCreate(ds, patientMatching);
            }
        }
        return true;
    }

    private Dataset toDataset(String[] pid) {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        ds.putLO(Tags.PatientID, pid[ID]);
        ds.putLO(Tags.IssuerOfPatientID, pid[ISSUER]);
        return ds;
    }

    private PatientUpdate getPatientUpdate() throws Exception {
        PatientUpdateHome patHome = (PatientUpdateHome) EJBHomeFactory
                .getFactory().lookup(PatientUpdateHome.class,
                        PatientUpdateHome.JNDI_NAME);
        return patHome.create();
    }

}