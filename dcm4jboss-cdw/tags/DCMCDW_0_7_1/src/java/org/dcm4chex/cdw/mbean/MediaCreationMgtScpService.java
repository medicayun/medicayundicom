/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;

import javax.jms.JMSException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.JMSDelegate;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.Priority;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4164 $ $Date: 2004-07-22 21:26:59 +0800 (周四, 22 7月 2004) $
 * @since 22.06.2004
 *
 */
public class MediaCreationMgtScpService extends AbstractScpService {

    private static final String LOOKUP_MEDIA_WRITER_NAME = "lookupMediaWriterName";

    private static final int INITIATE = 1;

    private static final int CANCEL = 2;

    private static final String[] CUIDS = { UIDs.MediaCreationManagementSOPClass};

    private boolean keepSpoolFiles = false;

    private String defaultMediaApplicationProfile = "STD-GEN-CD";

    private String defaultRequestPriority = Priority.LOW;

    private int maxNumberOfCopies = 10;

    private boolean defaultLabelUsingInformationExtractedFromInstances = true;

    private String defaultLabelText = "";

    private String defaultLabelStyleSelection = "";

    private boolean defaultAllowMediaSplitting = false;

    private boolean mediaSplittingSupported = true;

    private boolean defaultAllowLossyCompression = false;

    private String defaultIncludeNonDICOMObjects = "NO";

    private boolean defaultIncludeDisplayApplication = false;

    private boolean defaultPreserveInstances = false;

    private boolean allowCancelAlreadyCreating = true;

    private final DcmService service = new DcmServiceBase() {

        protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            return MediaCreationMgtScpService.this.doNCreate(assoc, rq, rspCmd);
        }

        protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            return MediaCreationMgtScpService.this.doNAction(assoc, rq, rspCmd);
        }

        protected Dataset doNGet(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            return MediaCreationMgtScpService.this.doNGet(assoc, rq, rspCmd);
        }

    };

    public final boolean isKeepSpoolFiles() {
        return keepSpoolFiles;
    }

    public final void setKeepSpoolFiles(boolean keepSpoolFiles) {
        this.keepSpoolFiles = keepSpoolFiles;
    }

   public final String getDefaultMediaApplicationProfile() {
        return defaultMediaApplicationProfile;
    }

    /*
     public final boolean isSupportedApplicationProfile(String profile) {
     try {
     new URL(MediaApplicationProfile.toURI(profile)).openConnection();
     return true;
     } catch (Exception e) {
     return false;
     }
     }
     */
    public final void setDefaultMediaApplicationProfile(String profile) {
        //        if (!isSupportedApplicationProfile(profile))
        //                throw new IllegalArgumentException("profile:" + profile);
        this.defaultMediaApplicationProfile = profile;
    }

    public final boolean isAllowCancelAlreadyCreating() {
        return allowCancelAlreadyCreating;
    }

    public final void setAllowCancelAlreadyCreating(
            boolean allowCancelAlreadyCreating) {
        this.allowCancelAlreadyCreating = allowCancelAlreadyCreating;
    }

    public final boolean isDefaultAllowLossyCompression() {
        return defaultAllowLossyCompression;
    }

    public final void setDefaultAllowLossyCompression(
            boolean defaultAllowLossyCompression) {
        this.defaultAllowLossyCompression = defaultAllowLossyCompression;
    }

    public final boolean isDefaultAllowMediaSplitting() {
        return defaultAllowMediaSplitting;
    }

    public final void setDefaultAllowMediaSplitting(
            boolean defaultAllowMediaSplitting) {
        this.defaultAllowMediaSplitting = defaultAllowMediaSplitting;
    }

    public final boolean isDefaultIncludeDisplayApplication() {
        return defaultIncludeDisplayApplication;
    }

    public final void setDefaultIncludeDisplayApplication(
            boolean defaultIncludeDisplayApplication) {
        this.defaultIncludeDisplayApplication = defaultIncludeDisplayApplication;
    }

    public final String getDefaultIncludeNonDICOMObjects() {
        return defaultIncludeNonDICOMObjects;
    }

    public final void setDefaultIncludeNonDICOMObjects(
            String defaultIncludeNonDICOMObjects) {
        this.defaultIncludeNonDICOMObjects = defaultIncludeNonDICOMObjects;
    }

    public final String getDefaultLabelStyleSelection() {
        return defaultLabelStyleSelection;
    }

    public final void setDefaultLabelStyleSelection(
            String defaultLabelStyleSelection) {
        this.defaultLabelStyleSelection = defaultLabelStyleSelection;
    }

    public final String getDefaultLabelText() {
        return defaultLabelText;
    }

    public final void setDefaultLabelText(String defaultLabelText) {
        this.defaultLabelText = defaultLabelText;
    }

    public final boolean isDefaultLabelUsingInformationExtractedFromInstances() {
        return defaultLabelUsingInformationExtractedFromInstances;
    }

    public final void setDefaultLabelUsingInformationExtractedFromInstances(
            boolean defaultLabelUsingInformationExtractedFromInstances) {
        this.defaultLabelUsingInformationExtractedFromInstances = defaultLabelUsingInformationExtractedFromInstances;
    }

    public final boolean isDefaultPreserveInstances() {
        return defaultPreserveInstances;
    }

    public final void setDefaultPreserveInstances(
            boolean defaultPreserveInstances) {
        this.defaultPreserveInstances = defaultPreserveInstances;
    }

    public final String getDefaultRequestPriority() {
        return defaultRequestPriority;
    }

    public final void setDefaultRequestPriority(String priority) {
        if (!Priority.isValid(priority))
                throw new IllegalArgumentException("priority:" + priority);
        this.defaultRequestPriority = priority;
    }

    public final int getMaxNumberOfCopies() {
        return maxNumberOfCopies;
    }

    public final void setMaxNumberOfCopies(int maxNumberOfCopies) {
        if (maxNumberOfCopies < 1)
                throw new IllegalArgumentException("maxNumberOfCopies:"
                        + maxNumberOfCopies);
        this.maxNumberOfCopies = maxNumberOfCopies;
    }

    public final boolean isMediaSplittingSupported() {
        return mediaSplittingSupported;
    }

    public final void setMediaSplittingSupported(boolean mediaSplittingSupported) {
        this.mediaSplittingSupported = mediaSplittingSupported;
    }

    protected void bindDcmServices() {
        bindDcmServices(CUIDS, service);
    }

    protected void unbindDcmServices() {
        unbindDcmServices(CUIDS);
    }

    protected void updatePresContexts() {
        putPresContexts(CUIDS, getTransferSyntaxes());
    }

    protected void removePresContexts() {
        putPresContexts(CUIDS, null);
    }

    private Dataset doNCreate(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset info = rq.getDataset();
        if (log.isDebugEnabled()) {
            DcmElement refSOPs = info.get(Tags.RefSOPSeq);
            String prompt = refSOPs == null ? "N-Create Information:\n"
                    : "N-Create Information:\n(0008,1199) SQ #-1 *"
                            + refSOPs.vm() + " // Referenced SOP Sequence\n";
            logDataset(prompt, info.subSet(new int[] { Tags.RefSOPSeq}, true));
        }
        checkCreateAttributes(info, rspCmd);
        String iuid = rspCmd.getAffectedSOPInstanceUID();
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        if (f.exists())
                throw new DcmServiceException(Status.DuplicateSOPInstance);
        String cuid = rqCmd.getAffectedSOPClassUID();
        String tsuid = rq.getTransferSyntaxUID();
        FileMetaInfo fmi = dof.newFileMetaInfo(cuid, iuid, tsuid);
        info.setFileMetaInfo(fmi);
        info.putUI(Tags.SOPInstanceUID, iuid);
        info.putUI(Tags.SOPClassUID, cuid);
        info.putCS(Tags.ExecutionStatus, ExecutionStatus.IDLE);
        info.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.NORMAL);
        try {
            info.writeFile(f, null);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        return info;
    }

    private void logDataset(String prefix, Dataset ds) {
        try {
            StringWriter w = new StringWriter();
            w.write(prefix);
            ds.dumpDataset(w, null);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }

    private void checkCreateAttributes(Dataset ds, Command rspCmd)
            throws DcmServiceException {
        checkFlag(ds, Tags.LabelUsingInformationExtractedFromInstances,
                defaultLabelUsingInformationExtractedFromInstances, rspCmd);
        checkFlag(ds, Tags.AllowMediaSplitting, mediaSplittingSupported
                && defaultAllowMediaSplitting, rspCmd);
        if (!mediaSplittingSupported) disableMediaSplitting(ds, rspCmd);
        checkFlag(ds, Tags.AllowLossyCompression, defaultAllowLossyCompression,
                rspCmd);
        checkFlag(ds, Tags.IncludeDisplayApplication,
                defaultIncludeDisplayApplication, rspCmd);
        checkFlag(ds, Tags.PreserveCompositeInstancesAfterMediaCreation,
                defaultPreserveInstances, rspCmd);
        if (ds.vm(Tags.LabelText) <= 0)
                ds.putUT(Tags.LabelText, defaultLabelText);
        if (ds.vm(Tags.LabelText) <= 0)
                ds.putCS(Tags.LabelStyleSelection, defaultLabelStyleSelection);
        if (ds.vm(Tags.IncludeNonDICOMObjects) <= 0)
                ds.putCS(Tags.IncludeNonDICOMObjects,
                        defaultIncludeNonDICOMObjects);
        DcmElement refSOPs = ds.get(Tags.RefSOPSeq);
        if (refSOPs == null || refSOPs.vm() == 0)
                throw new DcmServiceException(Status.MissingAttribute,
                        "Missing or empty Referenced SOP Sequence");
        for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
            Dataset item = refSOPs.getItem(i);
            if (item.vm(Tags.RefSOPInstanceUID) <= 0)
                    throw new DcmServiceException(Status.MissingAttribute,
                            "Missing Referenced SOP Instance UID");
            if (item.vm(Tags.RefSOPClassUID) <= 0)
                    throw new DcmServiceException(Status.MissingAttribute,
                            "Missing Referenced SOP Class UID");
            if (item.vm(Tags.RequestedMediaApplicationProfile) <= 0)
                    item.putCS(Tags.RequestedMediaApplicationProfile,
                            defaultMediaApplicationProfile);
        }
    }

    private void checkFlag(Dataset ds, int tag, boolean defval, Command rspCmd) {
        String s = ds.getString(tag);
        if (!Flag.isValid(s)) {
            rspCmd.putUS(Tags.Status, Status.AttributeValueOutOfRange);
            rspCmd.putLO(Tags.ErrorComment, "" + ds.get(tag));
            s = null;
        }
        if (s == null) ds.putCS(tag, Flag.valueOf(defval));
    }

    private void disableMediaSplitting(Dataset ds, Command rspCmd) {
        if (Flag.isYes(ds.getString(Tags.AllowMediaSplitting))) {
            rspCmd.putUS(Tags.Status, Status.AttributeValueOutOfRange);
            rspCmd.putLO(Tags.ErrorComment, ""
                    + ds.get(Tags.AllowMediaSplitting));
            ds.putCS(Tags.AllowMediaSplitting, Flag.NO);
        }
    }

    private void checkRequest(Dataset mcrq) throws MediaCreationException {
        HashSet profiles = new HashSet();
        HashSet checkForDuplicate = new HashSet();
        DcmElement refSOPs = mcrq.get(Tags.RefSOPSeq);
        for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
            Dataset item = refSOPs.getItem(i);
            String iuid = item.getString(Tags.RefSOPInstanceUID);
            if (!checkForDuplicate.add(iuid))
                    throw new MediaCreationException(
                            ExecutionStatusInfo.DUPL_REF_INST,
                            "Duplicate referenced SOP Instance: " + iuid);
            String profile = item
                    .getString(Tags.RequestedMediaApplicationProfile);
            /*            if (profiles.add(profile))
             if (!isSupportedApplicationProfile(profile))
             throw new MediaCreationException(
             ExecutionStatusInfo.NOT_SUPPORTED);
             */
            File f = spoolDir.getInstanceFile(iuid);
            if (!f.exists())
                    throw new MediaCreationException(
                            ExecutionStatusInfo.NO_INSTANCE, "No Instance: "
                                    + iuid);
        }
    }

    private Dataset doNAction(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset actionInfo = rq.getDataset();
        if (log.isDebugEnabled())
                logDataset("N-Action Information:\n", actionInfo);

        String iuid = rqCmd.getAffectedSOPInstanceUID();

        File f = spoolDir.getMediaCreationRequestFile(iuid);
        if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);

        MediaCreationRequest mcrq = new MediaCreationRequest(f);
        Dataset attrs;
        try {
            attrs = mcrq.readAttributes(log);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        final String status = attrs.getString(Tags.ExecutionStatus,
                ExecutionStatus.IDLE);
        final int actionID = rqCmd.getInt(Tags.ActionTypeID, -1);
        switch (actionID) {
        case INITIATE:
            if (!ExecutionStatus.IDLE.equals(status))
                    throw new DcmServiceException(
                            Status.DuplicateInitiateMediaCreation);
            int numberOfCopies = actionInfo != null ? actionInfo.getInt(
                    Tags.NumberOfCopies, 1) : 1;
            if (numberOfCopies < 1 || numberOfCopies > maxNumberOfCopies) {
                rspCmd.putLO(Tags.ErrorComment, ""
                        + actionInfo.get(Tags.NumberOfCopies));
                throw new DcmServiceException(Status.InvalidArgumentValue);
            }
            String priority = actionInfo != null ? actionInfo.getString(
                    Tags.RequestPriority, defaultRequestPriority)
                    : defaultRequestPriority;
            if (!Priority.isValid(priority)) {
                rspCmd.putLO(Tags.ErrorComment, ""
                        + actionInfo.get(Tags.RequestPriority));
                throw new DcmServiceException(Status.InvalidArgumentValue);
            }
            try {
                checkRequest(attrs);
                mcrq.setMediaWriterName(lookupMediaWriterName(assoc
                        .getAssociation().getCalledAET()));
                mcrq.setPriority(priority);
                mcrq.setNumberOfCopies(numberOfCopies);
                mcrq.setFilesetID(attrs.getString(Tags.StorageMediaFileSetID));
                mcrq.setVolsetID(attrs.getString(Tags.StorageMediaFileSetID));
                attrs.putIS(Tags.NumberOfCopies, numberOfCopies);
                attrs.putCS(Tags.RequestPriority, priority);
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
                attrs.putCS(Tags.ExecutionStatusInfo,
                        ExecutionStatusInfo.QUEUED_BUILD);
                try {
                    mcrq.writeAttributes(attrs, log);
                } catch (IOException e) {
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                }
                try {
                    JMSDelegate.getInstance("MediaComposer").queue(log, mcrq);
                } catch (JMSException e) {
                    throw new MediaCreationException(
                            ExecutionStatusInfo.PROC_FAILURE, e);
                }
            } catch (MediaCreationException e) {
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
                attrs.putCS(Tags.ExecutionStatusInfo, e.getStatusInfo());
                try {
                    mcrq.writeAttributes(attrs, log);
                } catch (IOException ioe) {
                    // error already logged
                }
                if (!keepSpoolFiles) spoolDir.deleteRefInstances(attrs);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
            break;
        case CANCEL:
            if (ExecutionStatus.DONE.equals(status)
                    || ExecutionStatus.FAILURE.equals(status))
                    throw new DcmServiceException(
                            Status.MediaCreationRequestAlreadyCompleted);
            if (!allowCancelAlreadyCreating
                    && ExecutionStatus.CREATING.equals(status))
                    throw new DcmServiceException(
                            Status.MediaCreationRequestAlreadyInProgress);
            if (!f.delete())
                    throw new DcmServiceException(
                            Status.CancellationDeniedForUnspecifiedReason);
            spoolDir.deleteRefInstances(attrs);

            break;
        default:
            throw new DcmServiceException(Status.NoSuchActionType, "actionID:"
                    + actionID);
        }
        return null;
    }

    private String lookupMediaWriterName(String aet) throws DcmServiceException {
        try {
            return (String) server.invoke(dcmServerName,
                    LOOKUP_MEDIA_WRITER_NAME, new Object[] { aet},
                    new String[] { String.class.getName()});
        } catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private Dataset doNGet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset ds = rq.getDataset(); // should be null!
        String iuid = rqCmd.getAffectedSOPInstanceUID();
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        if (!f.exists())
                throw new DcmServiceException(Status.NoSuchObjectInstance);

        Dataset mcrq = dof.newDataset();
        log.info("M-READ " + f);
        try {
            mcrq.readFile(f, FileFormat.DICOM_FILE, -1);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        final int[] filter = rqCmd.getTags(Tags.AttributeIdentifierList);
        return filter != null ? mcrq.subSet(filter) : mcrq;
    }
}
