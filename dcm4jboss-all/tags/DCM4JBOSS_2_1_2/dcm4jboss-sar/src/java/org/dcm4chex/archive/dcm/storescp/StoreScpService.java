/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.storescp;

import java.util.ArrayList;

import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;

import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.mbean.FileSystemInfo;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1460 $ $Date: 2005-02-01 02:46:57 +0800 (周二, 01 2月 2005) $
 * @since 03.08.2003
 */
public class StoreScpService extends AbstractScpService {

    private static final long MIN_OUT_OF_RESOURCES_THRESHOLD = 20 * FileUtils.MEGA;    

    public static final String IANS_KEY = "ians";

    public static final String EVENT_TYPE = "org.dcm4chex.archive.dcm.storescp";

    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

        public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };

    private static final String[] IMAGE_CUIDS = {
            UIDs.HardcopyGrayscaleImageStorage, UIDs.HardcopyColorImageStorage,
            UIDs.ComputedRadiographyImageStorage,
            UIDs.DigitalXRayImageStorageForPresentation,
            UIDs.DigitalXRayImageStorageForProcessing,
            UIDs.DigitalMammographyXRayImageStorageForPresentation,
            UIDs.DigitalMammographyXRayImageStorageForProcessing,
            UIDs.DigitalIntraoralXRayImageStorageForPresentation,
            UIDs.DigitalIntraoralXRayImageStorageForProcessing,
            UIDs.CTImageStorage, UIDs.UltrasoundMultiframeImageStorageRetired,
            UIDs.UltrasoundMultiframeImageStorage, UIDs.MRImageStorage,
            UIDs.EnhancedMRImageStorage,
            UIDs.NuclearMedicineImageStorageRetired,
            UIDs.UltrasoundImageStorageRetired, UIDs.UltrasoundImageStorage,
            UIDs.SecondaryCaptureImageStorage,
            UIDs.MultiframeSingleBitSecondaryCaptureImageStorage,
            UIDs.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
            UIDs.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
            UIDs.MultiframeColorSecondaryCaptureImageStorage,
            UIDs.XRayAngiographicImageStorage,
            UIDs.XRayRadiofluoroscopicImageStorage,
            UIDs.XRayAngiographicBiPlaneImageStorageRetired,
            UIDs.NuclearMedicineImageStorage, UIDs.VLImageStorageRetired,
            UIDs.VLMultiframeImageStorageRetired,
            UIDs.VLEndoscopicImageStorage, UIDs.VLMicroscopicImageStorage,
            UIDs.VLSlideCoordinatesMicroscopicImageStorage,
            UIDs.VLPhotographicImageStorage,
            UIDs.PositronEmissionTomographyImageStorage, UIDs.RTImageStorage,};

    private static final String[] OTHER_CUIDS = { UIDs.BasicTextSR,
            UIDs.EnhancedSR, UIDs.ComprehensiveSR, UIDs.MammographyCADSR,
            UIDs.GrayscaleSoftcopyPresentationStateStorage,
            UIDs.KeyObjectSelectionDocument, UIDs.RTDoseStorage,
            UIDs.RTStructureSetStorage, UIDs.RTBeamsTreatmentRecordStorage,
            UIDs.RTPlanStorage, UIDs.RTBrachyTreatmentRecordStorage,
            UIDs.RTTreatmentSummaryRecordStorage, UIDs.RawDataStorage,
            UIDs.AmbulatoryECGWaveformStorage,
            UIDs.BasicVoiceAudioWaveformStorage,
            UIDs.CardiacElectrophysiologyWaveformStorage,
            UIDs.GeneralECGWaveformStorage, UIDs.HemodynamicWaveformStorage,
            UIDs.TwelveLeadECGWaveformStorage};

    private ObjectName fileSystemMgtName;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
    
    private boolean acceptJPEGBaseline = true;

    private boolean acceptJPEGExtended = true;

    private boolean acceptJPEGLossless = true;

    private boolean acceptJPEGLossless14 = true;

    private boolean acceptJPEGLSLossless = true;

    private boolean acceptJPEGLSLossy = true;

    private boolean acceptJPEG2000Lossless = true;

    private boolean acceptJPEG2000Lossy = true;

    private boolean acceptRLELossless = false;

    private StoreScp scp = new StoreScp(this);

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
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
    
    public final String getOutOfResourcesThreshold() {
        return FileUtils.formatSize(scp.getOutOfResourcesThreshold());
    }
    
    public final void setOutOfResourcesThreshold(String str) {
        scp.setOutOfResourcesThreshold(FileUtils.parseSize(str,
        		MIN_OUT_OF_RESOURCES_THRESHOLD));
    }
    
    public final boolean isAcceptJPEG2000Lossless() {
        return acceptJPEG2000Lossless;
    }

    public final void setAcceptJPEG2000Lossless(boolean accept) {
        this.acceptJPEG2000Lossless = accept;
        enableService();
    }

    public final boolean isAcceptJPEG2000Lossy() {
        return acceptJPEG2000Lossy;
    }

    public final void setAcceptJPEG2000Lossy(boolean accept) {
        this.acceptJPEG2000Lossy = accept;
        enableService();
    }

    public final boolean isAcceptJPEGBaseline() {
        return acceptJPEGBaseline;
    }

    public final void setAcceptJPEGBaseline(boolean accept) {
        this.acceptJPEGBaseline = accept;
        enableService();
    }

    public final boolean isAcceptJPEGExtended() {
        return acceptJPEGExtended;
    }

    public final void setAcceptJPEGExtended(boolean accept) {
        this.acceptJPEGExtended = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLossless14() {
        return acceptJPEGLossless14;
    }

    public final void setAcceptJPEGLossless14(boolean accept) {
        this.acceptJPEGLossless14 = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLossless() {
        return acceptJPEGLossless;
    }

    public final void setAcceptJPEGLossless(boolean accept) {
        this.acceptJPEGLossless = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLSLossless() {
        return acceptJPEGLSLossless;
    }

    public final void setAcceptJPEGLSLossless(boolean accept) {
        this.acceptJPEGLSLossless = accept;
        enableService();
    }

    public final boolean isAcceptJPEGLSLossy() {
        return acceptJPEGLSLossy;
    }

    public final void setAcceptJPEGLSLossy(boolean accept) {
        this.acceptJPEGLSLossy = accept;
        enableService();
    }

    public final boolean isAcceptRLELossless() {
        return acceptRLELossless;
    }

    public final void setAcceptRLELossless(boolean accept) {
        this.acceptRLELossless = accept;
        enableService();
    }

    public final int getBufferSize() {
        return scp.getBufferSize();
    }

    public final void setBufferSize(int bufferSize) {
        scp.setBufferSize(bufferSize);
    }

    protected void startService() throws Exception {
        super.startService();
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        for (int i = 0; i < IMAGE_CUIDS.length; ++i) {
            services.bind(IMAGE_CUIDS[i], scp);
        }
        for (int i = 0; i < OTHER_CUIDS.length; ++i) {
            services.bind(OTHER_CUIDS[i], scp);
        }
        dcmHandler.addAssociationListener(scp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        for (int i = 0; i < IMAGE_CUIDS.length; ++i) {
            services.unbind(IMAGE_CUIDS[i]);
        }
        for (int i = 0; i < OTHER_CUIDS.length; ++i) {
            services.unbind(OTHER_CUIDS[i]);
        }
        dcmHandler.removeAssociationListener(scp);
    }

    private String[] getImageTS() {
        ArrayList list = new ArrayList();
        if (acceptJPEGLossless14) {
            list.add(UIDs.JPEGLossless14);
        }
        if (acceptJPEGLossless) {
            list.add(UIDs.JPEGLossless);
        }
        if (acceptJPEGLSLossless) {
            list.add(UIDs.JPEGLSLossless);
        }
        if (acceptRLELossless) {
            list.add(UIDs.RLELossless);
        }
        if (acceptJPEG2000Lossless) {
            list.add(UIDs.JPEG2000Lossless);
        }
        if (acceptExplicitVRLE) {
            list.add(UIDs.ExplicitVRLittleEndian);
        }
        list.add(UIDs.ImplicitVRLittleEndian);
        if (acceptJPEGBaseline) {
            list.add(UIDs.JPEGBaseline);
        }
        if (acceptJPEGExtended) {
            list.add(UIDs.JPEGExtended);
        }
        if (acceptJPEGLSLossy) {
            list.add(UIDs.JPEGLSLossy);
        }
        if (acceptJPEG2000Lossy) {
            list.add(UIDs.JPEG2000Lossy);
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        putPresContexts(policy, IMAGE_CUIDS, enable ? getImageTS() : null);
        putPresContexts(policy, OTHER_CUIDS, enable ? getTransferSyntaxUIDs() : null);
    }

    private void putPresContexts(AcceptorPolicy policy, String[] asuids, String[] tsuids) {
        for (int i = 0; i < asuids.length; i++) {
            policy.putPresContext(asuids[i], tsuids);
        }
    }

    void sendReleaseNotification(Association assoc) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(assoc);
        super.sendNotification(notif);
    }

    FileSystemInfo selectStorageFileSystem() {
        try {
            return (FileSystemInfo) server.invoke(fileSystemMgtName,
                    "selectStorageFileSystem",
                    null,
                    null);
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke isLocalFileSystem", e);
        }
    }

    void logInstancesStored(RemoteNode node, InstancesAction action) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName,
                    "logInstancesStored",
                    new Object[] { node, action},
                    new String[] { RemoteNode.class.getName(), 
                    	InstancesAction.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }
    
    boolean isLocalFileSystem(String dirpath) {
        try {
            Boolean b = (Boolean) server.invoke(fileSystemMgtName,
                    "isLocalFileSystem",
                    new Object[] { dirpath},
                    new String[] { String.class.getName()});
            return b.booleanValue();
        } catch (JMException e) {
            throw new RuntimeException("Failed to invoke isLocalFileSystem", e);
        }
    }
}