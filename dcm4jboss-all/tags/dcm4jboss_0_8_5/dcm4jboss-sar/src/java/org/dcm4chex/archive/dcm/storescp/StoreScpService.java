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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.config.StorageRules;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1158 $ $Date: 2004-08-29 05:54:50 +0800 (周日, 29 8月 2004) $
 * @since 03.08.2003
 */
public class StoreScpService extends AbstractScpService {

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
            UIDs.RTTreatmentSummaryRecordStorage,};

    private Set retrieveAETSet;

    private boolean acceptStorageCommitment = true;

    private boolean acceptJPEGBaseline = true;

    private boolean acceptJPEGExtended = false;

    private boolean acceptJPEGLossless14 = true;

    private boolean acceptJPEGLossless = true;

    private boolean acceptJPEGLSLossless = false;

    private boolean acceptJPEGLSLossy = false;

    private boolean acceptJPEG2000Lossless = false;

    private boolean acceptJPEG2000Lossy = false;

    private boolean acceptRLELossless = false;

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    private StoreScp scp = new StoreScp(this);

    private StgCmtScp stgCmtScp = new StgCmtScp(this);

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
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

    public String getStorageRules() {
        return scp.getStorageRules().toString();
    }

    public void setStorageRules(String rules) {
        scp.setStorageRules(new StorageRules(rules));
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

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final String getRetrieveAETs() {
        if (retrieveAETSet == null) return null;
        StringBuffer sb = new StringBuffer();
        Iterator it = retrieveAETSet.iterator();
        sb.append(it.next());
        while (it.hasNext())
            sb.append(',').append(it.next());
        return sb.toString();
    }

    public final void setRetrieveAETs(String aets) {
        String[] a = StringUtils.split(aets, ',');
        if (a.length == 0) { throw new IllegalArgumentException(
                "Missing Retrieve AET"); }
        this.retrieveAETSet = Collections.unmodifiableSet(new HashSet(Arrays
                .asList(a)));
    }

    final Set getRetrieveAETSet() {
        return retrieveAETSet;
    }

    final String[] getRetrieveAETArray() {
        return (String[]) retrieveAETSet.toArray(new String[retrieveAETSet
                .size()]);
    }

    final String getRetrieveAET() {
        return (String) retrieveAETSet.iterator().next();
    }

    public boolean isAcceptStorageCommitment() {
        return acceptStorageCommitment;
    }

    public void setAcceptStorageCommitment(boolean storageCommitmentPushModel) {
        this.acceptStorageCommitment = storageCommitmentPushModel;
        updatePolicy();
    }

    public final boolean isAcceptJPEG2000Lossless() {
        return acceptJPEG2000Lossless;
    }

    public final void setAcceptJPEG2000Lossless(boolean acceptJPEG2000Lossless) {
        this.acceptJPEG2000Lossless = acceptJPEG2000Lossless;
        updatePolicy();
    }

    public final boolean isAcceptJPEG2000Lossy() {
        return acceptJPEG2000Lossy;
    }

    public final void setAcceptJPEG2000Lossy(boolean acceptJPEG2000Lossy) {
        this.acceptJPEG2000Lossy = acceptJPEG2000Lossy;
        updatePolicy();
    }

    public final boolean isAcceptJPEGBaseline() {
        return acceptJPEGBaseline;
    }

    public final void setAcceptJPEGBaseline(boolean acceptJPEGBaseline) {
        this.acceptJPEGBaseline = acceptJPEGBaseline;
        updatePolicy();
    }

    public final boolean isAcceptJPEGExtended() {
        return acceptJPEGExtended;
    }

    public final void setAcceptJPEGExtended(boolean acceptJPEGExtended) {
        this.acceptJPEGExtended = acceptJPEGExtended;
        updatePolicy();
    }

    public final boolean isAcceptJPEGLossless() {
        return acceptJPEGLossless;
    }

    public final void setAcceptJPEGLossless(boolean acceptJPEGLossless) {
        this.acceptJPEGLossless = acceptJPEGLossless;
        updatePolicy();
    }

    public final boolean isAcceptJPEGLossless14() {
        return acceptJPEGLossless14;
    }

    public final void setAcceptJPEGLossless14(boolean acceptJPEGLossless14) {
        this.acceptJPEGLossless14 = acceptJPEGLossless14;
        updatePolicy();
    }

    public final boolean isAcceptJPEGLSLossless() {
        return acceptJPEGLSLossless;
    }

    public final void setAcceptJPEGLSLossless(boolean acceptJPEGLSLossless) {
        this.acceptJPEGLSLossless = acceptJPEGLSLossless;
        updatePolicy();
    }

    public final boolean isAcceptJPEGLSLossy() {
        return acceptJPEGLSLossy;
    }

    public final void setAcceptJPEGLSLossy(boolean acceptJPEGLSLossy) {
        this.acceptJPEGLSLossy = acceptJPEGLSLossy;
        updatePolicy();
    }

    public final boolean isAcceptRLELossless() {
        return acceptRLELossless;
    }

    public final void setAcceptRLELossless(boolean acceptRLELossless) {
        this.acceptRLELossless = acceptRLELossless;
        updatePolicy();
    }

    public final int getBufferSize() {
        return scp.getBufferSize();
    }

    public final void setBufferSize(int bufferSize) {
        scp.setBufferSize(bufferSize);
    }

    protected void startService() throws Exception {
        scp.checkReadyToStart();
        super.startService();
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        for (int i = 0; i < IMAGE_CUIDS.length; ++i) {
            services.bind(IMAGE_CUIDS[i], scp);
        }
        for (int i = 0; i < OTHER_CUIDS.length; ++i) {
            services.bind(OTHER_CUIDS[i], scp);
        }
        services.bind(UIDs.StorageCommitmentPushModel, stgCmtScp);
        dcmHandler.addAssociationListener(scp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        for (int i = 0; i < IMAGE_CUIDS.length; ++i) {
            services.unbind(IMAGE_CUIDS[i]);
        }
        for (int i = 0; i < OTHER_CUIDS.length; ++i) {
            services.unbind(OTHER_CUIDS[i]);
        }
        services.unbind(UIDs.StorageCommitmentPushModel);
        dcmHandler.removeAssociationListener(scp);
    }

    private String[] getImageTS() {
        ArrayList list = new ArrayList();
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
        if (acceptJPEGLSLossless) {
            list.add(UIDs.JPEGLSLossless);
        }
        if (acceptJPEG2000Lossless) {
            list.add(UIDs.JPEG2000Lossless);
        }
        if (acceptJPEGLossless14) {
            list.add(UIDs.JPEGLossless14);
        }
        if (acceptJPEGLossless) {
            list.add(UIDs.JPEGLossless);
        }
        if (acceptRLELossless) {
            list.add(UIDs.RLELossless);
        }
        if (acceptExplicitVRLE) {
            list.add(UIDs.ExplicitVRLittleEndian);
        }
        list.add(UIDs.ImplicitVRLittleEndian);
        return (String[]) list.toArray(new String[list.size()]);
    }

    protected void initPresContexts(AcceptorPolicy policy) {
        addPresContexts(policy, IMAGE_CUIDS, getImageTS());
        addPresContexts(policy, OTHER_CUIDS, getTransferSyntaxUIDs());
        if (acceptStorageCommitment)
                policy.putPresContext(UIDs.StorageCommitmentPushModel,
                        getTransferSyntaxUIDs());
    }

    void sendReleaseNotification(Association assoc) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(assoc);
        super.sendNotification(notif);
    }
}