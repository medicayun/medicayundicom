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

package org.dcm4chex.archive.dcm.storescp;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2710 $ $Date: 2006-08-28 07:02:16 +0800 (周一, 28 8月 2006) $
 * @since 03.08.2003
 */
public class StoreScpService extends AbstractScpService {

    private final TimerSupport timer = new TimerSupport(this);

    private final NotificationListener checkPendingSeriesStoredListener = 
        new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            try {
                log.info("Check for Pending Series Stored");
                checkPendingSeriesStored();
            } catch (Exception e) {
                log.error("Check for Pending Series Stored failed:", e);
            }
        }
    };

    private Integer listenerID;
    private long checkPendingSeriesStoredInterval;
    private long pendingSeriesStoredTimeout;

    /**
     * Map containing accepted Image SOP Class UID. key is name (as in config
     * string), value is real uid)
     */
    private Map imageCUIDS = new LinkedHashMap();

    /**
     * Map containing accepted Image Transfer Syntax UIDs. key is name (as in
     * config string), value is real uid)
     */
    private Map imageTSUIDS = new LinkedHashMap();

    /**
     * Map containing accepted Waveform SOP Class UID. key is name (as in config
     * string), value is real uid)
     */
    private Map waveformCUIDS = new LinkedHashMap();

    /**
     * Map containing accepted Waveform Transfer Syntax UIDs. key is name (as in
     * config string), value is real uid)
     */
    private Map waveformTSUIDS = new LinkedHashMap();

    /**
     * Map containing accepted Video SOP Class UID. key is name (as in config
     * string), value is real uid)
     */
    private Map videoCUIDS = new LinkedHashMap();

    /**
     * Map containing accepted Video Transfer Syntax UIDs. key is name (as in
     * config string), value is real uid)
     */
    private Map videoTSUIDS = new LinkedHashMap();

    /**
     * Map containing accepted SR SOP Class UID. key is name (as in config
     * string), value is real uid)
     */
    private Map srCUIDS = new LinkedHashMap();

    /**
     * Map containing accepted SR Transfer Syntax UIDs. key is name (as in
     * config string), value is real uid)
     */
    private Map srTSUIDS = new LinkedHashMap();

    /**
     * Map containing accepted other SOP Class UIDs. key is name (as in config
     * string), value is real uid)
     */
    private Map otherCUIDS = new LinkedHashMap();

    private ObjectName fileSystemMgtName;
    private ObjectName mwlScuServiceName;

    private int bufferSize = 8192;

    private boolean md5sum = true;

    private StoreScp scp = new StoreScp(this);

    public final String getCheckPendingSeriesStoredInterval() {
        return RetryIntervalls
                .formatIntervalZeroAsNever(checkPendingSeriesStoredInterval);
    }

    public void setCheckPendingSeriesStoredInterval(String interval) {
        long oldInterval = checkPendingSeriesStoredInterval;
        checkPendingSeriesStoredInterval = RetryIntervalls
                .parseIntervalOrNever(interval);
        if (getState() == STARTED
                && oldInterval != checkPendingSeriesStoredInterval) {
            timer.stopScheduler("CheckPendingSeriesStored", listenerID,
                    checkPendingSeriesStoredListener);
            listenerID = timer.startScheduler("CheckPendingSeriesStored",
                    checkPendingSeriesStoredInterval,
                    checkPendingSeriesStoredListener);
        }
    }

    public final String getPendingSeriesStoredTimeout() {
        return RetryIntervalls.formatInterval(pendingSeriesStoredTimeout);
    }

    public void setPendingSeriesStoredTimeout(String interval) {
        pendingSeriesStoredTimeout = RetryIntervalls
                .parseIntervalOrNever(interval);
    }

    public final boolean isMd5sum() {
        return md5sum;
    }

    public final void setMd5sum(boolean md5sum) {
        this.md5sum = md5sum;
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final boolean isStudyDateInFilePath() {
        return scp.isStudyDateInFilePath();
    }

    public final void setStudyDateInFilePath(boolean enable) {
        scp.setStudyDateInFilePath(enable);
    }

    public final boolean isYearInFilePath() {
        return scp.isYearInFilePath();
    }

    public final void setYearInFilePath(boolean enable) {
        scp.setYearInFilePath(enable);
    }

    public final boolean isMonthInFilePath() {
        return scp.isMonthInFilePath();
    }

    public final void setMonthInFilePath(boolean enable) {
        scp.setMonthInFilePath(enable);
    }

    public final boolean isDayInFilePath() {
        return scp.isDayInFilePath();
    }

    public final void setDayInFilePath(boolean enable) {
        scp.setDayInFilePath(enable);
    }

    public final boolean isHourInFilePath() {
        return scp.isHourInFilePath();
    }

    public final void setHourInFilePath(boolean enable) {
        scp.setHourInFilePath(enable);
    }

    public final boolean isAcceptMissingPatientID() {
        return scp.isAcceptMissingPatientID();
    }

    public final void setAcceptMissingPatientID(boolean accept) {
        scp.setAcceptMissingPatientID(accept);
    }

    public final boolean isAcceptMissingPatientName() {
        return scp.isAcceptMissingPatientName();
    }

    public final void setAcceptMissingPatientName(boolean accept) {
        scp.setAcceptMissingPatientName(accept);
    }

    public final boolean isSerializeDBUpdate() {
        return scp.isSerializeDBUpdate();
    }

    public final void setSerializeDBUpdate(boolean serialize) {
        scp.setSerializeDBUpdate(serialize);
    }

    public final String getGeneratePatientID() {
        return scp.getGeneratePatientID();
    }

    public final void setGeneratePatientID(String pattern) {
        scp.setGeneratePatientID(pattern);
    }

    public final String getIssuerOfPatientIDRules() {
        return scp.getIssuerOfPatientIDRules();
    }

    public final void setIssuerOfPatientIDRules(String rules) {
        scp.setIssuerOfPatientIDRules(rules);
    }

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public final ObjectName getMwlScuServiceName() {
        return mwlScuServiceName;
    }

    public final void setMwlScuServiceName(ObjectName mwlScuServiceName) {
        this.mwlScuServiceName = mwlScuServiceName;
    }

    public final String getAcceptPatientID() {
        return scp.getAcceptPatientID();
    }

    public final void setAcceptPatientID(String acceptPatientID) {
        scp.setAcceptPatientID(acceptPatientID);
    }

    public final String getIgnorePatientID() {
        return scp.getIgnorePatientID();
    }

    public final void setIgnorePatientID(String ignorePatientID) {
        scp.setIgnorePatientID(ignorePatientID);
    }

    public final String getIgnorePatientIDCallingAETs() {
        return scp.getIgnorePatientIDCallingAETs();
    }

    public final void setIgnorePatientIDCallingAETs(String aets) {
        scp.setIgnorePatientIDCallingAETs(aets);
    }

    public String getCoerceWarnCallingAETs() {
        return scp.getCoerceWarnCallingAETs();
    }

    public void setCoerceWarnCallingAETs(String aets) {
        scp.setCoerceWarnCallingAETs(aets);
    }

    public boolean isStoreDuplicateIfDiffHost() {
        return scp.isStoreDuplicateIfDiffHost();
    }

    public void setStoreDuplicateIfDiffHost(boolean storeDuplicate) {
        scp.setStoreDuplicateIfDiffHost(storeDuplicate);
    }

    public boolean isStoreDuplicateIfDiffMD5() {
        return scp.isStoreDuplicateIfDiffMD5();
    }

    public void setStoreDuplicateIfDiffMD5(boolean storeDuplicate) {
        scp.setStoreDuplicateIfDiffMD5(storeDuplicate);
    }

    public final String getCompressionRules() {
        return scp.getCompressionRules().toString();
    }

    public void setCompressionRules(String rules) {
        scp.setCompressionRules(new CompressionRules(rules));
    }

    public final int getUpdateDatabaseMaxRetries() {
        return scp.getUpdateDatabaseMaxRetries();
    }

    public final void setUpdateDatabaseMaxRetries(int updateDatabaseMaxRetries) {
        scp.setUpdateDatabaseMaxRetries(updateDatabaseMaxRetries);
    }

    public final int getMaxCountUpdateDatabaseRetries() {
        return scp.getMaxCountUpdateDatabaseRetries();
    }

    public final void resetMaxCountUpdateDatabaseRetries() {
        scp.setMaxCountUpdateDatabaseRetries(0);
    }

    public final long getUpdateDatabaseRetryInterval() {
        return scp.getUpdateDatabaseRetryInterval();
    }

    public final void setUpdateDatabaseRetryInterval(long interval) {
        scp.setUpdateDatabaseRetryInterval(interval);
    }

    public String getAcceptedImageSOPClasses() {
        return toString(imageCUIDS);
    }

    public void setAcceptedImageSOPClasses(String s) {
        updateAcceptedSOPClass(imageCUIDS, s, scp);
    }

    public String getAcceptedTransferSyntaxForImageSOPClasses() {
        return toString(imageTSUIDS);
    }

    public void setAcceptedTransferSyntaxForImageSOPClasses(String s) {
        updateAcceptedTransferSyntax(imageTSUIDS, s);
    }

    public String getAcceptedVideoSOPClasses() {
        return toString(videoCUIDS);
    }

    public void setAcceptedVideoSOPClasses(String s) {
        updateAcceptedSOPClass(videoCUIDS, s, scp);
    }

    public String getAcceptedTransferSyntaxForVideoSOPClasses() {
        return toString(videoTSUIDS);
    }

    public void setAcceptedTransferSyntaxForVideoSOPClasses(String s) {
        updateAcceptedTransferSyntax(videoTSUIDS, s);
    }

    public String getAcceptedSRSOPClasses() {
        return toString(srCUIDS);
    }

    public void setAcceptedSRSOPClasses(String s) {
        updateAcceptedSOPClass(srCUIDS, s, scp);
    }

    public String getAcceptedTransferSyntaxForSRSOPClasses() {
        return toString(srTSUIDS);
    }

    public void setAcceptedTransferSyntaxForSRSOPClasses(String s) {
        updateAcceptedTransferSyntax(srTSUIDS, s);
    }

    public String getAcceptedWaveformSOPClasses() {
        return toString(waveformCUIDS);
    }

    public void setAcceptedWaveformSOPClasses(String s) {
        updateAcceptedSOPClass(waveformCUIDS, s, scp);
    }

    public String getAcceptedTransferSyntaxForWaveformSOPClasses() {
        return toString(waveformTSUIDS);
    }

    public void setAcceptedTransferSyntaxForWaveformSOPClasses(String s) {
        updateAcceptedTransferSyntax(waveformTSUIDS, s);
    }

    public String getAcceptedOtherSOPClasses() {
        return toString(otherCUIDS);
    }

    public void setAcceptedOtherSOPClasses(String s) {
        updateAcceptedSOPClass(otherCUIDS, s, scp);
    }

    protected String[] getCUIDs() {
        return valuesToStringArray(otherCUIDS);
    }

    /**
     * @return Returns the checkIncorrectWorklistEntry.
     */
    public boolean isCheckIncorrectWorklistEntry() {
        return scp.isCheckIncorrectWorklistEntry();
    }

    /**
     * Enable/disable check if an MPPS with Discontinued reason 'Incorrect
     * worklist selected' is referenced.
     * 
     * @param checkIncorrectWorklistEntry
     *            The checkIncorrectWorklistEntry to set.
     */
    public void setCheckIncorrectWorklistEntry(boolean check) {
        scp.setCheckIncorrectWorklistEntry(check);
    }

    protected void startService() throws Exception {
        super.startService();
        timer.init();
        listenerID = timer.startScheduler("CheckPendingSeriesStored",
                checkPendingSeriesStoredInterval,
                checkPendingSeriesStoredListener);
    }

    protected void stopService() throws Exception {
        timer.stopScheduler("CheckPendingSeriesStored", listenerID,
                checkPendingSeriesStoredListener);
        super.stopService();
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        bindAll(valuesToStringArray(imageCUIDS), scp);
        bindAll(valuesToStringArray(videoCUIDS), scp);
        bindAll(valuesToStringArray(srCUIDS), scp);
        bindAll(valuesToStringArray(waveformCUIDS), scp);
        bindAll(valuesToStringArray(otherCUIDS), scp);
        dcmHandler.addAssociationListener(scp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        unbindAll(valuesToStringArray(imageCUIDS));
        unbindAll(valuesToStringArray(videoCUIDS));
        unbindAll(valuesToStringArray(srCUIDS));
        unbindAll(valuesToStringArray(waveformCUIDS));
        unbindAll(valuesToStringArray(otherCUIDS));
        dcmHandler.removeAssociationListener(scp);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        putPresContexts(policy, valuesToStringArray(imageCUIDS),
                enable ? valuesToStringArray(imageTSUIDS) : null);
        putPresContexts(policy, valuesToStringArray(videoCUIDS),
                enable ? valuesToStringArray(videoTSUIDS) : null);
        putPresContexts(policy, valuesToStringArray(srCUIDS),
                enable ? valuesToStringArray(srTSUIDS) : null);
        putPresContexts(policy, valuesToStringArray(waveformCUIDS),
                enable ? valuesToStringArray(waveformTSUIDS) : null);
        putPresContexts(policy, valuesToStringArray(otherCUIDS),
                enable ? valuesToStringArray(tsuidMap) : null);
    }

    public FileSystemDTO selectStorageFileSystem() throws DcmServiceException {
        try {
            FileSystemDTO fsDTO = (FileSystemDTO) server.invoke(
                    fileSystemMgtName, "selectStorageFileSystem", null, null);
            if (fsDTO == null)
                throw new DcmServiceException(Status.OutOfResources);
            return fsDTO;
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    boolean isLocalRetrieveAET(String aet) {
        try {
            return aet.equals(server.getAttribute(fileSystemMgtName,
                    "RetrieveAETitle"));
        } catch (JMException e) {
            throw new RuntimeException(
                    "Failed to invoke getAttribute 'RetrieveAETitle'", e);
        }
    }

    boolean isFreeDiskSpaceOnDemand() {
        try {
            Boolean b = (Boolean) server.getAttribute(fileSystemMgtName,
                    "FreeDiskSpaceOnDemand");
            return b.booleanValue();
        } catch (JMException e) {
            throw new RuntimeException(
                    "Failed to invoke getAttribute 'FreeDiskSpaceOnDemand'", e);
        }
    }

    void callFreeDiskSpace() {
        try {
            server.invoke(fileSystemMgtName, "freeDiskSpace", null, null);
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke freeDiskSpace", e);
        }

    }

    void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this,
                eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
    }

    void logInstancesStored(Socket s, SeriesStored seriesStored) {
        if (auditLogName == null)
            return;
        final AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
        Dataset ian = seriesStored.getIAN();
        Dataset pps = ian.getItem(Tags.RefPPSSeq);
        String ppsiuid = pps != null ? pps.getString(Tags.RefSOPInstanceUID)
                : null;
        InstancesAction action = alf.newInstancesAction("Create", ian
                .getString(Tags.StudyInstanceUID), alf.newPatient(seriesStored
                .getPatientID(), seriesStored.getPatientName()));
        action.setMPPSInstanceUID(ppsiuid);
        action.setAccessionNumber(seriesStored.getAccessionNumber());
        DcmElement sq = ian.getItem(Tags.RefSeriesSeq).get(Tags.RefSOPSeq);
        int n = sq.countItems();
        for (int i = 0; i < n; i++) {
            action.addSOPClassUID(sq.getItem(i).getString(Tags.RefSOPClassUID));
        }
        action.setNumberOfInstances(n);
        RemoteNode remoteNode;
        if (s != null) {
            remoteNode = alf.newRemoteNode(s, seriesStored.getCallingAET());
        } else {
            try {
                InetAddress iAddr = InetAddress.getLocalHost();
                remoteNode = alf.newRemoteNode(iAddr.getHostAddress(), iAddr
                        .getHostName(), "LOCAL");
            } catch (UnknownHostException x) {
                remoteNode = alf.newRemoteNode("127.0.0.1", "localhost",
                        "LOCAL");
            }
        }
        try {
            server.invoke(auditLogName, "logInstancesStored", new Object[] {
                    remoteNode, action },
                    new String[] { RemoteNode.class.getName(),
                            InstancesAction.class.getName() });
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }

    /**
     * Imports a DICOM file.
     * <p>
     * The FileDTO object refers to an existing DICOM file (This method does NOT
     * check this file!) and the Dataset object holds the meta data for
     * database.
     * <p>
     * 
     * @param fileDTO
     *            Refers the DICOM file.
     * @param ds
     *            Dataset with metadata for database.
     * @param last
     *            last file to import
     */
    public void importFile(FileDTO fileDTO, Dataset ds, String prevseriuid,
            boolean last) throws Exception {
        Storage store = getStorage();
        String seriud = ds.getString(Tags.SeriesInstanceUID);
        if (prevseriuid != null && !prevseriuid.equals(seriud)) {
            SeriesStored seriesStored = store.makeSeriesStored(prevseriuid);
            if (seriesStored != null) {
                log.debug("Send SeriesStoredNotification - series changed");
                scp.doAfterSeriesIsStored(store, null, seriesStored);
                store.commitSeriesStored(seriesStored);
            }
        }
        String cuid = ds.getString(Tags.SOPClassUID);
        String iuid = ds.getString(Tags.SOPInstanceUID);
        FileMetaInfo fmi = DcmObjectFactory.getInstance().newFileMetaInfo(cuid,
                iuid, fileDTO.getFileTsuid());
        ds.setFileMetaInfo(fmi);
        String fsPath = fileDTO.getDirectoryPath();
        String filePath = fileDTO.getFilePath();
        File f = FileUtils.toFile(fsPath, filePath);
        scp.updateDB(store, ds, fileDTO.getFileSystemPk(), filePath, f, fileDTO
                .getFileMd5());
        if (last) {
            SeriesStored seriesStored = store.makeSeriesStored(seriud);
            if (seriesStored != null) {
                scp.doAfterSeriesIsStored(store, null, seriesStored);
                store.commitSeriesStored(seriesStored);
            }
        }
    }

    private void checkPendingSeriesStored() throws Exception {
        Storage store = getStorage();
        SeriesStored[] seriesStored = store
                .checkSeriesStored(pendingSeriesStoredTimeout);
        for (int i = 0; i < seriesStored.length; i++) {
            log.info("Detect pending " + seriesStored[i]);
            scp.doAfterSeriesIsStored(store, null, seriesStored[i]);
            store.commitSeriesStored(seriesStored[i]);
        }
    }

    Storage getStorage() throws RemoteException, CreateException,
            HomeFactoryException {
        return ((StorageHome) EJBHomeFactory.getFactory().lookup(
                StorageHome.class, StorageHome.JNDI_NAME)).create();
    }

    public List findMWLEntries(Dataset ds) throws Exception {
        return (List) server.invoke(mwlScuServiceName, "findMWLEntries",
                new Object[] { ds },
                new String[] { Dataset.class.getName() });
    }
    
}