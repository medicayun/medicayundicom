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
package org.dcm4chee.web.service.contentedit;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.ProcedureRecordMessage;
import org.dcm4che2.audit.message.StudyDeletedMessage;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.dcm4che2.audit.util.InstanceSorter;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.PersonName;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.common.DicomEditLocal;
import org.dcm4chee.web.dao.folder.MppsToMwlLinkLocal;
import org.dcm4chee.web.dao.vo.EntityTree;
import org.dcm4chee.web.dao.vo.MppsToMwlLinkResult;
import org.dcm4chee.web.service.common.FileImportOrder;
import org.dcm4chee.web.service.common.HttpUserInfo;
import org.dcm4chee.web.service.common.TemplatesDelegate;
import org.dcm4chee.web.service.common.XSLTUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Jan 29, 2009
 */
public class ContentEditService extends ServiceMBeanSupport {

    private static final String NONE ="NONE";
    private static final String MWL2STORE_XSL = "mwl-cfindrsp2cstorerq.xsl";

    private static final int[] EXCLUDE_PPS_ATTRS = new int[]{
        Tag.ReferencedPerformedProcedureStepSequence, 
        Tag.PerformedProcedureStepStartDate, Tag.PerformedProcedureStepStartTime};

    private Code rejectNoteCode = new Code();

    private String[] moveDestinationAETs;
    
    private DicomEditLocal dicomEdit;
    private MppsToMwlLinkLocal mpps2mwl;

    private boolean auditEnabled = true;

    private boolean forceNewRejNoteStudyIUID;
    
    private ObjectName rejNoteServiceName;
    private ObjectName ianScuServiceName;
    private ObjectName moveScuServiceName;
    private ObjectName storeScpServiceName;

    private boolean processIAN;
    private boolean processRejNote;
    private boolean dcm14Stylesheet;
    
    private String modifyingSystem;
    private String modifyReason;
        
    private static final TransformerFactory tf = TransformerFactory.newInstance();
    protected TemplatesDelegate templates = new TemplatesDelegate(this);
    private String dcm2To14TplName, dcm14To2TplName;
    private Templates dcm2To14Tpl, dcm14To2Tpl;
    
    public String getUIDRoot() {
        return UIDUtils.getRoot();
    }
    
    public void setUIDRoot(String root) {
        UIDUtils.setRoot(root);
    }
    
    public String getRejectionNoteCode() {
        return rejectNoteCode.toString()+"\r\n";
    }

    public void setRejectionNoteCode(String code) {
        rejectNoteCode = new Code(code);
    }

    public boolean isProcessIAN() {
        return processIAN;
    }

    public void setProcessIAN(boolean processIAN) {
        this.processIAN = processIAN;
    }

    public boolean isProcessRejNote() {
        return processRejNote;
    }

    public void setProcessRejNote(boolean processRejNote) {
        this.processRejNote = processRejNote;
    }
    
    public String getMoveDestinationAETs() {
        return moveDestinationAETs == null ? NONE : StringUtils.join(moveDestinationAETs, '\\');
    }
    public void setMoveDestinationAETs(String aets) {
        this.moveDestinationAETs = NONE.equals(aets) ? null : StringUtils.split(aets, '\\');
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public boolean isForceNewRejNoteStudyIUID() {
        return forceNewRejNoteStudyIUID;
    }

    public void setForceNewRejNoteStudyIUID(boolean forceNewRejNoteStudyIUID) {
        this.forceNewRejNoteStudyIUID = forceNewRejNoteStudyIUID;
    }

    public String getModifyingSystem() {
        return modifyingSystem;
    }

    public void setModifyingSystem(String modifyingSystem) {
        this.modifyingSystem = modifyingSystem;
    }

    public String getModifyReason() {
        return modifyReason;
    }

    public void setModifyReason(String modifyReason) {
        this.modifyReason = modifyReason;
    }

    public final String getCoerceConfigDir() {
        return templates.getConfigDir();
    }

    public final void setCoerceConfigDir(String path) {
        templates.setConfigDir(path);
    }

    public ObjectName getRejectionNoteServiceName() {
        return rejNoteServiceName;
    }
    public void setRejectionNoteServiceName(ObjectName name) {
        this.rejNoteServiceName = name;
    }

    public ObjectName getIANScuServiceName() {
        return ianScuServiceName;
    }
    public void setIANScuServiceName(ObjectName name) {
        this.ianScuServiceName = name;
    }

    public void setMoveScuServiceName(ObjectName name) {
        this.moveScuServiceName = name;
    }
    public ObjectName getMoveScuServiceName() {
        return moveScuServiceName;
    }

    public ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public void setStoreScpServiceName(ObjectName name) {
        storeScpServiceName = name;
    }
    
    public final ObjectName getTemplatesServiceName() {
        return templates.getTemplatesServiceName();
    }

    public final void setTemplatesServiceName(ObjectName serviceName) {
        templates.setTemplatesServiceName(serviceName);
    }

    public boolean isDcm14Stylesheet() {
        return dcm14Stylesheet;
    }

    public void setDcm14Stylesheet(boolean dcm14Stylesheet) {
        this.dcm14Stylesheet = dcm14Stylesheet;
    }

    public String getDcm2To14Tpl() {
        return dcm2To14TplName;
    }

    public void setDcm2To14Tpl(String name) throws TransformerConfigurationException, MalformedURLException {
        new URL(name);
        dcm2To14Tpl = tf.newTemplates(new StreamSource(name));
        dcm2To14TplName = name;
    }

    public String getDcm14To2Tpl() {
        return dcm14To2TplName;
    }

    public void setDcm14To2Tpl(String name) throws MalformedURLException, TransformerConfigurationException {
        new URL(name);
        dcm14To2Tpl = tf.newTemplates(new StreamSource(name));
        dcm14To2TplName = name;
    }

    public DicomObject moveInstanceToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveInstanceToTrash(iuid);
        if (entityTree.isEmpty()) 
            return null;
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances:");
        processRejectionNote(kos);
        processIANs(entityTree, Availability.UNAVAILABLE);
        return kos;
    }

    public DicomObject[] moveInstancesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveInstancesToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances:");
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    public DicomObject moveSeriesToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesToTrash(iuid);
        if (entityTree.isEmpty())
            return null;
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
        processRejectionNote(kos);
        processIANs(entityTree, Availability.UNAVAILABLE);
        return kos;
    }
    public DicomObject[] moveSeriesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }

    public DicomObject[] moveSeriesOfPpsToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesOfPpsToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    
    public List<MPPS> deletePps(long[] ppsPks) {
        log.info("********* nrOf ppsPks:"+ppsPks.length);
        List<MPPS> mppss = lookupDicomEditLocal().deletePps(ppsPks);
        log.info("********* nrOf pps deleted:"+mppss.size());
        for (MPPS mpps : mppss) {
            logProcedureRecord(mpps.getPatient().getAttributes(), 
                mpps.getAttributes().getString(new int[]{Tag.ScheduledStepAttributesSequence,0,Tag.StudyInstanceUID}),
                mpps.getAccessionNumber(), 
                ProcedureRecordMessage.UPDATE, "MPPS deleted");
        }
        return mppss;
    }
    
    public DicomObject moveStudyToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveStudyToTrash(iuid);
        if (entityTree.isEmpty())
            return null;
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logStudyDeleted(kos);
        processRejectionNote(kos);
        processIANs(entityTree, Availability.UNAVAILABLE);
        return kos;
    }
    public DicomObject[] moveStudiesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveStudiesToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    public DicomObject[] movePatientToTrash(String pid, String issuer) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().movePatientToTrash(pid, issuer);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    public DicomObject[] movePatientsToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().movePatientsToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }

    public int moveInstancesToSeries(long[] instPks, long seriesPk) throws InstanceNotFoundException, MBeanException, ReflectionException {
        DicomObject targetAttrs = lookupDicomEditLocal().getCompositeObjectforSeries(seriesPk);
        EntityTree entityTree = lookupDicomEditLocal().moveInstancesToTrash(instPks);
        processAfterMoveEntities(entityTree, targetAttrs, null);
        return entityTree.getAllInstances().size();
    }

    public int moveInstanceToSeries(String sopIUID, String seriesIUID) throws InstanceNotFoundException, MBeanException, ReflectionException {
        DicomObject targetAttrs = lookupDicomEditLocal().getCompositeObjectforSeries(seriesIUID);
        EntityTree entityTree = lookupDicomEditLocal().moveInstanceToTrash(sopIUID);
        processAfterMoveEntities(entityTree, targetAttrs, null);
        return entityTree.getAllInstances().size();
    }
    
    public int moveSeriesToStudy(long[] seriesPks, long studyPk) throws InstanceNotFoundException, MBeanException, ReflectionException {
        DicomObject targetAttrs = lookupDicomEditLocal().getCompositeObjectforStudy(studyPk);
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesToTrash(seriesPks);
        processAfterMoveEntities(entityTree, targetAttrs, EXCLUDE_PPS_ATTRS);
        return entityTree.getAllInstances().size();
    }

    public int moveSeriesToStudy(String seriesIUID, String studyIUID) throws InstanceNotFoundException, MBeanException, ReflectionException {
        DicomObject targetAttrs = lookupDicomEditLocal().getCompositeObjectforStudy(studyIUID);
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesToTrash(seriesIUID);
        processAfterMoveEntities(entityTree, targetAttrs, EXCLUDE_PPS_ATTRS);
        return entityTree.getAllInstances().size();
    }

    public int moveStudiesToPatient(long[] studyPks, long patPk) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if ( studyPks == null || studyPks.length < 1) {
            return 0;
        }
        EntityTree entityTree = lookupDicomEditLocal().moveStudiesToPatient(studyPks, patPk);
        if (!entityTree.isEmpty()) {
            DicomObject kos = getRejectionNotes(entityTree)[0];
            logInstancesAccessed(kos, InstancesAccessedMessage.UPDATE, true, "Studies moved to patient:");
        }
        return entityTree.getAllInstances().size();
    }
    public int moveStudyToPatient(String studyIUID, String patId, String issuer) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveStudyToPatient(studyIUID, patId, issuer);
        if (!entityTree.isEmpty()) {
            DicomObject kos = getRejectionNotes(entityTree)[0];
            logInstancesAccessed(kos, InstancesAccessedMessage.UPDATE, true, "Studies moved to patient:");
        }
        return entityTree.getAllInstances().size();
    }
    
    public Study createStudy(DicomObject studyAttrs, long patPk) {
        return lookupDicomEditLocal().createStudy(studyAttrs, patPk);
    }
    public Study updateStudy(Study study) {
        return lookupDicomEditLocal().updateStudy(study);
    }
    public Series createSeries(DicomObject seriesAttrs, long studyPk) {
        return lookupDicomEditLocal().createSeries(seriesAttrs, studyPk);
    }
    public Series updateSeries(Series series) {
        return lookupDicomEditLocal().updateSeries(series);
    }

    private void processAfterMoveEntities(EntityTree entityTree, DicomObject targetAttrs, int[] excludeTagsForImport)
        throws InstanceNotFoundException, MBeanException,ReflectionException {
        if (!entityTree.isEmpty()) { 
            DicomObject[] rejNotes = getRejectionNotes(entityTree);
            for (DicomObject kos : rejNotes) {
                logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Instances for move entities:");
                processRejectionNote(kos);
            }
            processIANs(entityTree, Availability.UNAVAILABLE);
            importFiles(entityTree, targetAttrs, excludeTagsForImport);
        }
    }

    private void importFiles(EntityTree entityTree, DicomObject targetAttrs, int[] excludeTags) {
        FileImportOrder order = new FileImportOrder();
        DicomObject headerAttrs, studyAttrs, seriesAttrs;
        File file;
        for ( Map<Study, Map<Series, Set<Instance>>> studies : entityTree.getEntityTreeMap().values() ) {
            for (Map.Entry<Study, Map<Series, Set<Instance>>> entry : studies.entrySet() ) {
                if (targetAttrs.containsValue(Tag.StudyInstanceUID)) {
                    studyAttrs = null;
                } else {
                    studyAttrs = entry.getKey().getAttributes(false);
                }
                for (Map.Entry<Series, Set<Instance>> seriesEntry : entry.getValue().entrySet()) {
                    if (targetAttrs.containsValue(Tag.SeriesInstanceUID)) {
                        seriesAttrs = null;
                    } else {
                        seriesAttrs = seriesEntry.getKey().getAttributes(false);
                        seriesAttrs.putString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
                        seriesAttrs.putString(seriesAttrs.resolveTag(PrivateTag.CallingAET, PrivateTag.CreatorID), 
                                VR.AE, seriesEntry.getKey().getSourceAET());
                    }
                    for ( Instance i : seriesEntry.getValue()) {
                        if ( i.getFiles().size() < 1)
                            continue;
                        headerAttrs = new BasicDicomObject();
                        targetAttrs.copyTo(headerAttrs);
                        if ( studyAttrs != null) {
                            studyAttrs.copyTo(headerAttrs);
                        }
                        if ( seriesAttrs != null) {
                            seriesAttrs.copyTo(headerAttrs);
                        }
                        i.getAttributes(false).copyTo(headerAttrs);
                        headerAttrs.putString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
                        file = i.getFiles().iterator().next();
                        order.addFile(file, headerAttrs.exclude(excludeTags));
                    }
                }
            }
        }
        importFiles(order);
    }
    
    private void importFiles(FileImportOrder order) {
        log.info("import Files:"+order);
        try {
            server.invoke(storeScpServiceName, "importFile", 
                new Object[]{order}, new String[]{FileImportOrder.class.getName()});
        } catch (Throwable t) {
            log.error("Import files failed! order:"+order);
        }
    }

    public MppsToMwlLinkResult linkMppsToMwl(long[] mppsPks, long mwlPk, String system, String reason) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if ( system == null || system.trim().length() < 1) {
            system = modifyingSystem;
        }
        if ( reason == null || reason.trim().length() < 1) {
            reason = this.modifyReason;
        }
        MppsToMwlLinkResult result = lookupMppsToMwlLinkLocal().linkMppsToMwl(mppsPks, mwlPk, modifyingSystem, reason);
        log.info("MppsToMwlLinkResult:"+result);
        logMppsLinkRecord(result);
        updateSeriesAttributes(result);
        this.sendJMXNotification(result);
        log.info("MppsToMwlLinkResult:"+result);
        log.info("MppsToMwlLinkResult: studiesToMove:"+result.getStudiesToMove().size());
        if (result.getStudiesToMove().size() > 0) {
            Patient pat = result.getMwl().getPatient();
            log.info("Patient of some MPPS are not identical to patient of MWL! Move studies to Patient of MWL:"+
                    pat.getPatientID());
            long[] studyPks = new long[result.getStudiesToMove().size()];
            int i = 0;
            for ( Study s : result.getStudiesToMove()) {
                studyPks[i++] = s.getPk();
            }
            this.moveStudiesToPatient(studyPks, pat.getPk());
        }
        return result;
    }
    
    public void linkMppsToMwl(String mppsIUID, String rpId, String spsId, String system, String reason) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if ( system == null || system.trim().length() < 1) {
            system = modifyingSystem;
        }
        if ( reason == null || reason.trim().length() < 1) {
            reason = this.modifyReason;
        }
        MppsToMwlLinkResult result = lookupMppsToMwlLinkLocal().linkMppsToMwl(mppsIUID, rpId, spsId, system, reason);
        log.info("MppsToMwlLinkResult:"+result);
        logMppsLinkRecord(result);
        updateSeriesAttributes(result);
        this.sendJMXNotification(result);
        if (result.getStudiesToMove().size() > 0) {
            Patient pat = result.getMwl().getPatient();
            log.info("Patient of some MPPS are not identical to patient of MWL! Move studies to Patient of MWL:"+
                    pat.getPatientID());
            long[] studyPks = new long[result.getStudiesToMove().size()];
            int i = 0;
            for ( Study s : result.getStudiesToMove()) {
                studyPks[i++] = s.getPk();
            }
            this.moveStudiesToPatient(studyPks, pat.getPk());
        }
    }
    
    public boolean unlinkMpps(long pk) {
        MPPS mpps = lookupMppsToMwlLinkLocal().unlinkMpps(pk, modifyingSystem, modifyReason);
        if (mpps != null) {
            DicomObject mppsAttrs = mpps.getAttributes();
            DicomObject patAttrs = mpps.getPatient().getAttributes();
            StringBuilder sb = new StringBuilder();
            sb.append("Unlink MPPS iuid:").append(mppsAttrs.getString(Tag.SOPInstanceUID)).append(" from SPS ID(s): ");
            DicomElement ssaSQ = mppsAttrs.get(Tag.ScheduledStepAttributesSequence);
            for ( int i = 0, len = ssaSQ.countItems() ; i < len ; i++) {
                sb.append(ssaSQ.getDicomObject(i).getString(Tag.ScheduledProcedureStepID)).append(", ");
            }
            logProcedureRecord(patAttrs, ssaSQ.getDicomObject().getString(Tag.StudyInstanceUID),
                    mpps.getAccessionNumber(), 
                    ProcedureRecordMessage.UPDATE, sb.substring(0,sb.length()-2));
            return true;
        }
        return false;
    }
    
    private void updateSeriesAttributes(MppsToMwlLinkResult result) throws InstanceNotFoundException, MBeanException, ReflectionException {
        DicomObject coerce = getCoercionAttrs(result.getMwl().getAttributes());
        if ( coerce != null && !coerce.isEmpty()) {
            String[] mppsIuids = new String[result.getMppss().size()];
            int i = 0;
            for (MPPS m : result.getMppss()) {
                mppsIuids[i++] = m.getSopInstanceUID();
            }
            this.lookupMppsToMwlLinkLocal().updateSeriesAndStudyAttributes(mppsIuids, coerce);
        } else {
            log.warn("No Coercion attributes to update Study and Series Attributes after linking MPPS to MWL! coerce:"+coerce);
        }
    }
    private DicomObject getCoercionAttrs(DicomObject ds) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if ( ds == null ) return null;
        log.info("Dataset to get coercion ds:"+ds);
        try {
            XSLTUtils.dump(ds, null, "/tmp/mpps2mwl_coerce.xml", true);
        } catch (Exception x) {
            log.warn("Can't dump dicom object!", x);
        }
        DicomObject sps = ds.get(Tag.ScheduledProcedureStepSequence).getDicomObject();
        String aet = sps == null ? null : sps.getString(Tag.ScheduledStationAETitle);
        Templates tpl = templates.getTemplatesForAET(aet, MWL2STORE_XSL);
        log.info("found template for aet("+aet+"):"+tpl);
        if (tpl == null) {
            log.warn("Coercion template "+MWL2STORE_XSL+" not found! Can not store MWL attributes to series!");
            return null;
        }
        DicomObject out = new BasicDicomObject();
        try {
            log.info("dcm14Stylesheet:"+dcm14Stylesheet);
            if (dcm14Stylesheet) {
                Templates[] tpls = new Templates[]{dcm2To14Tpl,tpl,dcm14To2Tpl};
                XSLTUtils.xslt(ds, tpls, out, null);
            } else {
                XSLTUtils.xslt(ds, tpl, out, null);
            }
        } catch (Exception e) {
            log.error("Attribute coercion failed:", e);
            return null;
        }
        log.info("return coerced attributes:"+out);
        return out;
    }
    
    private DicomObject[] getRejectionNotes(EntityTree entityTree) {
        Map<Patient, Map<Study, Map<Series, Set<Instance>>>> entityTreeMap = entityTree.getEntityTreeMap();
        DicomObject[] rejNotes = new DicomObject[entityTreeMap.size()];
        int i = 0;
        for ( Map<Study, Map<Series, Set<Instance>>> studies : entityTreeMap.values()) {
            rejNotes[i] = toRejectionNote(studies);
            log.info("Rejection Note! KOS:"+rejNotes[i++]);
        }
        return rejNotes;
    }
    
    private DicomObject toRejectionNote(Map<Study, Map<Series, Set<Instance>>> entityTree) {
        String suid = forceNewRejNoteStudyIUID ? UIDUtils.createUID() : 
            entityTree.keySet().iterator().next().getStudyInstanceUID(); 
        DicomObject kos = newKeyObject(suid);
        DicomElement crpeSeq = kos.putSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
        entityTree.keySet().iterator().next().getPatient().getAttributes().copyTo(kos);
        for (Map.Entry<Study, Map<Series, Set<Instance>>> entry : entityTree.entrySet() ) {
            addProcedureEvidenceSequenceItem(crpeSeq, entry.getKey(), entry.getValue());
        }
        return kos;
    }
    
    private DicomObject newKeyObject(String studyIUID) {
        DicomObject kos = new BasicDicomObject();
        kos.putString(Tag.StudyInstanceUID,VR.UI, studyIUID);
        kos.putString(Tag.SeriesInstanceUID,VR.UI, UIDUtils.createUID());
        kos.putString(Tag.SOPInstanceUID,VR.UI, UIDUtils.createUID());
        kos.putString(Tag.SOPClassUID,VR.UI, UID.KeyObjectSelectionDocumentStorage);
        kos.putString(Tag.Modality, VR.CS, "KO");
        kos.putInt(Tag.InstanceNumber, VR.IS, 1);
        kos.putDate(Tag.ContentDate, VR.DA, new Date());
        kos.putDate(Tag.ContentTime, VR.TM, new Date());
        DicomElement cncSeq = kos.putSequence(Tag.ConceptNameCodeSequence);
        cncSeq.addDicomObject(rejectNoteCode.toCodeItem());
        kos.putString(Tag.ValueType, VR.CS, "CONTAINER");
        DicomElement tmplSeq = kos.putSequence(Tag.ContentTemplateSequence);
        DicomObject tmplItem = new BasicDicomObject();
        tmplItem.putString(Tag.TemplateIdentifier, VR.CS, "2010");
        tmplItem.putString(Tag.MappingResource, VR.CS, "DCMR");
        tmplSeq.addDicomObject(tmplItem);
        kos.putSequence(Tag.ReferencedPerformedProcedureStepSequence);
        kos.putString(Tag.SeriesDescription, VR.LO, "Rejection Note");
        return kos;
    }
        
    private void addProcedureEvidenceSequenceItem(DicomElement crpeSeq, Study study, Map<Series, Set<Instance>> series) {
        DicomObject item = new BasicDicomObject();
        crpeSeq.addDicomObject(item);
        item.putString(Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());
        DicomElement refSeriesSeq = item.putSequence(Tag.ReferencedSeriesSequence);
        DicomElement refSopSeq;
        DicomObject refSeriesSeqItem, refSopSeqItem;
        for ( Map.Entry<Series, Set<Instance>> instances : series.entrySet()) {
            refSeriesSeqItem = new BasicDicomObject();
            refSeriesSeq.addDicomObject(refSeriesSeqItem);
            refSeriesSeqItem.putString(Tag.SeriesInstanceUID, VR.UI, instances.getKey().getSeriesInstanceUID());
            refSopSeq = refSeriesSeqItem.putSequence(Tag.ReferencedSOPSequence);
            for ( Instance inst : instances.getValue()) {
                refSopSeqItem = new BasicDicomObject();
                refSopSeq.addDicomObject(refSopSeqItem);
                refSopSeqItem.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inst.getSOPInstanceUID());
                refSopSeqItem.putString(Tag.ReferencedSOPClassUID, VR.UI, inst.getSOPClassUID());
            }
        }
    }
    
    private DicomObject makeIAN(Study study, Map<Series, Set<Instance>> mapSeries, Availability availability) {
        log.debug("makeIAN: studyIUID:" + study.getStudyInstanceUID());
        Patient pat = study.getPatient();
        DicomObject ian = new BasicDicomObject();
        ian.putString(Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());
        ian.putString(Tag.AccessionNumber, VR.SH, study.getAccessionNumber());
        ian.putString(Tag.PatientID, VR.LO, pat.getPatientID());
        ian.putString(Tag.IssuerOfPatientID, VR.LO, pat.getIssuerOfPatientID());
        ian.putString(Tag.PatientName, VR.PN, pat.getPatientName());
        DicomElement refPPSSeq = ian.putSequence(Tag.ReferencedPerformedProcedureStepSequence);
        HashSet<String> mppsuids = new HashSet<String>();
        DicomElement refSeriesSeq = ian.putSequence(Tag.ReferencedSeriesSequence);

        for (Map.Entry<Series, Set<Instance>> entry : mapSeries.entrySet() ) {
            Series sl = entry.getKey();
            MPPS mpps = sl.getModalityPerformedProcedureStep();
            if (mpps != null) {
                String mppsuid = mpps.getSopInstanceUID();
                if (mppsuids.add(mppsuid)) {
                    DicomObject refmpps = new BasicDicomObject();
                    refPPSSeq.addDicomObject(refmpps);
                    refmpps.putString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
                    refmpps.putString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsuid);
                    refmpps.putSequence(Tag.PerformedWorkitemCodeSequence);
                }
            }
            DicomObject refSerItem = new BasicDicomObject();
            refSeriesSeq.addDicomObject(refSerItem);
            refSerItem.putString(Tag.SeriesInstanceUID, VR.UI, sl.getSeriesInstanceUID());
            DicomElement refSopSeq = refSerItem.putSequence(Tag.ReferencedSOPSequence);
            for (Instance instance : entry.getValue()) {
                DicomObject refSopItem = new BasicDicomObject();
                refSopSeq.addDicomObject(refSopItem);
                refSopItem.putString(Tag.RetrieveAETitle, VR.AE, instance.getRetrieveAETs());
                refSopItem.putString(Tag.InstanceAvailability, VR.CS, 
                        availability == null ? instance.getAvailability().name() : availability.name());
                refSopItem.putString(Tag.ReferencedSOPClassUID, VR.UI, instance.getSOPClassUID());
                refSopItem.putString(Tag.ReferencedSOPInstanceUID, VR.UI, instance.getSOPInstanceUID());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("IAN:"+ ian);
        }
        return ian;
    }
    
    private DicomEditLocal lookupDicomEditLocal() {
        if ( dicomEdit == null ) {
            try {
                InitialContext jndiCtx = new InitialContext();
                dicomEdit = (DicomEditLocal) jndiCtx.lookup(DicomEditLocal.JNDI_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dicomEdit;
    }
    
    private MppsToMwlLinkLocal lookupMppsToMwlLinkLocal() {
        if ( mpps2mwl == null ) {
            try {
                InitialContext jndiCtx = new InitialContext();
                mpps2mwl = (MppsToMwlLinkLocal) jndiCtx.lookup(MppsToMwlLinkLocal.JNDI_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mpps2mwl;
    }

    private void logInstancesAccessed(DicomObject kos,
            AuditEvent.ActionCode actionCode, boolean addIUID, String detailMessage) {
        if (!auditEnabled )
            return;
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage
                .isEnableDNSLookups());
        log.debug("log instances Accessed! actionCode:" + actionCode);
        try {
            InstancesAccessedMessage msg = new InstancesAccessedMessage(
                    actionCode);
            msg.addUserPerson(userInfo.getUserId(), null, null, userInfo
                    .getHostName(), true);
            msg.addPatient(kos.getString(Tag.PatientID), kos.getString(Tag.PatientName));
            ParticipantObject study;
            DicomElement crpeSeq = kos.get(Tag.CurrentRequestedProcedureEvidenceSequence);
            DicomObject item;
            for ( int i = 0, len = crpeSeq.countItems() ; i < len ; i++ ) {
                item = crpeSeq.getDicomObject(i);
                study = msg.addStudy(item.getString(Tag.StudyInstanceUID),
                        getStudyDescription(item, addIUID));
                if ( detailMessage != null )
                    study.addParticipantObjectDetail("Description", getStudySeriesDetail(detailMessage, item));
            }
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch (Exception x) {
            log.warn("Audit Log 'Instances Accessed' (actionCode:" + actionCode
                    + ") failed:", x);
        }
    }

    public void logMppsLinkRecord(MppsToMwlLinkResult result ) {
        MWLItem mwl = result.getMwl();
        String accNr = mwl.getAccessionNumber();
        String spsId = mwl.getScheduledProcedureStepID();
        String studyIuid = mwl.getStudyInstanceUID();
        DicomObject patAttrs = mwl.getPatient().getAttributes();
        for ( MPPS mpps : result.getMppss()) {
            String desc = "MPPS "+mpps.getSopInstanceUID()+" linked with MWL entry "+spsId;
            logProcedureRecord(patAttrs, studyIuid, accNr, ProcedureRecordMessage.UPDATE, desc);
        }
    }
    
    private void logProcedureRecord(DicomObject patAttrs, String studyIuid, 
            String accNr, ActionCode actionCode, String desc) {
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage
                .isEnableDNSLookups());
        log.debug("log Procedure Record! actionCode:" + actionCode);
        try {
            ProcedureRecordMessage msg = new ProcedureRecordMessage(actionCode);
            msg.addUserPerson(userInfo.getUserId(), null, null, userInfo
                    .getHostName(), true);
            PersonName pn = new PersonName(patAttrs.getString(Tag.PatientName));
            String pname = pn.get(PersonName.GIVEN);
            pname = pname == null ? pname = pn.get(PersonName.FAMILY) :
                pname+" "+pn.get(PersonName.FAMILY);
            msg.addPatient(patAttrs.getString(Tag.PatientID), pname);
            ParticipantObjectDescription poDesc = new ParticipantObjectDescription();
            if (accNr != null)
                poDesc.addAccession(accNr);
            ParticipantObject study = msg.addStudy(studyIuid, poDesc);
            study.addParticipantObjectDetail("Description", desc);
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch (Exception x) {
            log.warn("Audit Log 'Procedure Record' failed:", x);
        }
    }

    private String getStudySeriesDetail(String detailMessage, DicomObject crpeSeqItem) {
        DicomElement refSeries = crpeSeqItem.get(Tag.ReferencedSeriesSequence);
        StringBuffer sb = new StringBuffer();
        sb.append(detailMessage);
        int len = refSeries.countItems();
        if ( len > 0 ) {
            sb.append(refSeries.getDicomObject(0).getString(Tag.SeriesInstanceUID));
            for (int i = 1; i < len; i++) {
                sb.append(", ").append(refSeries.getDicomObject(i).getString(Tag.SeriesInstanceUID));
            }
        }
        return sb.toString();
    }

    private void logStudyDeleted(DicomObject kos) {
        if (!auditEnabled)
            return;
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage
                .isEnableDNSLookups());
        try {
            String patId = kos.getString(Tag.PatientID);
            String patName = kos.getString(Tag.PatientName);
            StudyDeletedMessage msg; 
            DicomElement crpeSeq = kos.get(Tag.CurrentRequestedProcedureEvidenceSequence);
            for ( int i = 0, len = crpeSeq.countItems() ; i < len ; i++ ) {
                msg = new StudyDeletedMessage();
                msg.addUserPerson(userInfo.getUserId(), null, null, userInfo
                        .getHostName(), true);
                msg.addPatient(patId, patName);
                DicomObject crpeSeqItem = kos.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject(i);
                msg.addStudy(crpeSeqItem.getString(Tag.StudyInstanceUID),
                        getStudyDescription(crpeSeqItem, true));
                msg.validate();
                Logger.getLogger("auditlog").info(msg);
            }
        } catch (Exception x) {
            log.warn("Audit Log 'Study Deleted' failed:", x);
        }
    }

    private ParticipantObjectDescription getStudyDescription(DicomObject crpeSeqItem, boolean addIUID) {
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        String accNr = crpeSeqItem.getString(Tag.AccessionNumber);
        if (accNr != null)
            desc.addAccession(accNr);
        addSOPClassInfo(desc, crpeSeqItem, addIUID);
        return desc;
    }

    private void addSOPClassInfo(ParticipantObjectDescription desc,
            DicomObject studyMgtDs, boolean addIUID) {
        DicomElement refSeries = studyMgtDs.get(Tag.ReferencedSeriesSequence);
        if (refSeries == null)
            return;
        String suid = studyMgtDs.getString(Tag.StudyInstanceUID);
        InstanceSorter sorter = new InstanceSorter();
        DicomObject ds;
        DicomElement refSopSeq;
        for (int i = 0, len = refSeries.countItems(); i < len; i++) {
            refSopSeq = refSeries.getDicomObject(i).get(Tag.ReferencedSOPSequence);
            if (refSopSeq != null) {
                for (int j = 0, jlen = refSopSeq.countItems(); j < jlen; j++) {
                    ds = refSopSeq.getDicomObject(j);
                    sorter.addInstance(suid,
                            ds.getString(Tag.ReferencedSOPClassUID),
                            ds.getString(Tag.ReferencedSOPInstanceUID), null);
                }
            }
        }
        
        for (String cuid : sorter.getCUIDs(suid)) {
            ParticipantObjectDescription.SOPClass sopClass = new ParticipantObjectDescription.SOPClass(cuid);
            sopClass.setNumberOfInstances(sorter.countInstances(suid, cuid));
            if ( addIUID ) {
                for ( String iuid : sorter.getIUIDs(suid, cuid) ) {
                    sopClass.addInstance(iuid);
                }
            }
            desc.addSOPClass(sopClass);
        }
    }
    
    private void processRejectionNote(DicomObject rejNote) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (processRejNote) {
            log.info("RejectionNote KOS:"+rejNote);
            server.invoke(rejNoteServiceName, "scheduleRejectionNote", 
                    new Object[]{rejNote}, new String[]{DicomObject.class.getName()});
        }
    }

    private ArrayList<DicomObject> getIANs(EntityTree entityTree, Availability availability) throws InstanceNotFoundException, MBeanException, ReflectionException {
        ArrayList<DicomObject> ians = new ArrayList<DicomObject>();
        Map<Patient, Map<Study, Map<Series, Set<Instance>>>> entityTreeMap = entityTree.getEntityTreeMap();
        for (Map<Study, Map<Series, Set<Instance>>> studyMap : entityTreeMap.values()) {
            for ( Map.Entry<Study, Map<Series, Set<Instance>>> studyEntry: studyMap.entrySet()) {
                ians.add(makeIAN(studyEntry.getKey(), studyEntry.getValue(), availability));
            }
        }
        return ians;
    }
    
    private ArrayList<DicomObject> processIANs(EntityTree entityTree, Availability availability) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (processIAN) {
            ArrayList<DicomObject> ians = getIANs(entityTree, availability);
            for (DicomObject ian : ians) {
                server.invoke(ianScuServiceName, "scheduleIAN", 
                        new Object[]{ian}, new String[]{DicomObject.class.getName()});
            }
            return ians;
        }
        return new ArrayList<DicomObject>();
    }

    public void sendJMXNotification(Object o) {
        if (log.isDebugEnabled()) {
            log.debug("Send JMX Notification: " + o);
        }
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this,
                eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
    }

}

