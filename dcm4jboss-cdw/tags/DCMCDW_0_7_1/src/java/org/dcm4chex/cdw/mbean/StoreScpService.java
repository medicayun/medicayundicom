/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4174 $ $Date: 2004-08-20 16:15:43 +0800 (周五, 20 8月 2004) $
 * @since 22.06.2004
 *
 */
public class StoreScpService extends AbstractScpService {

    private static final int[] TYPE1_ATTR = { Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID, Tags.SOPInstanceUID, Tags.SOPClassUID,};

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

    private boolean acceptJPEGBaseline = true;

    private boolean acceptJPEGExtended = false;

    private boolean acceptJPEGLossless14 = false;

    private boolean acceptJPEGLossless = true;

    private boolean acceptJPEGLSLossless = false;

    private boolean acceptJPEGLSLossy = false;

    private boolean acceptJPEG2000Lossless = false;

    private boolean acceptJPEG2000Lossy = false;

    private boolean acceptRLELossless = false;

    private int bufferSize = 512;

    private final DcmService service = new DcmServiceBase() {

        protected void doCStore(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            StoreScpService.this.doCStore(assoc, rq, rspCmd);
        }

    };

    public final boolean isAcceptJPEG2000Lossless() {
        return acceptJPEG2000Lossless;
    }

    public final void setAcceptJPEG2000Lossless(boolean acceptJPEG2000Lossless) {
        this.acceptJPEG2000Lossless = acceptJPEG2000Lossless;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEG2000Lossy() {
        return acceptJPEG2000Lossy;
    }

    public final void setAcceptJPEG2000Lossy(boolean acceptJPEG2000Lossy) {
        this.acceptJPEG2000Lossy = acceptJPEG2000Lossy;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEGBaseline() {
        return acceptJPEGBaseline;
    }

    public final void setAcceptJPEGBaseline(boolean acceptJPEGBaseline) {
        this.acceptJPEGBaseline = acceptJPEGBaseline;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEGExtended() {
        return acceptJPEGExtended;
    }

    public final void setAcceptJPEGExtended(boolean acceptJPEGExtended) {
        this.acceptJPEGExtended = acceptJPEGExtended;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEGLossless() {
        return acceptJPEGLossless;
    }

    public final void setAcceptJPEGLossless(boolean acceptJPEGLossless) {
        this.acceptJPEGLossless = acceptJPEGLossless;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEGLossless14() {
        return acceptJPEGLossless14;
    }

    public final void setAcceptJPEGLossless14(boolean acceptJPEGLossless14) {
        this.acceptJPEGLossless14 = acceptJPEGLossless14;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEGLSLossless() {
        return acceptJPEGLSLossless;
    }

    public final void setAcceptJPEGLSLossless(boolean acceptJPEGLSLossless) {
        this.acceptJPEGLSLossless = acceptJPEGLSLossless;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptJPEGLSLossy() {
        return acceptJPEGLSLossy;
    }

    public final void setAcceptJPEGLSLossy(boolean acceptJPEGLSLossy) {
        this.acceptJPEGLSLossy = acceptJPEGLSLossy;
        updatePresContextsIfRunning();
    }

    public final boolean isAcceptRLELossless() {
        return acceptRLELossless;
    }

    public final void setAcceptRLELossless(boolean acceptRLELossless) {
        this.acceptRLELossless = acceptRLELossless;
        updatePresContextsIfRunning();
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private String[] getImageTransferSyntaxes() {
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

    protected void bindDcmServices() {
        bindDcmServices(IMAGE_CUIDS, service);
        bindDcmServices(OTHER_CUIDS, service);
    }

    protected void unbindDcmServices() {
        unbindDcmServices(IMAGE_CUIDS);
        unbindDcmServices(OTHER_CUIDS);
    }

    protected void updatePresContexts() {
        putPresContexts(IMAGE_CUIDS, getImageTransferSyntaxes());
        putPresContexts(OTHER_CUIDS, getTransferSyntaxes());
    }

    protected void removePresContexts() {
        putPresContexts(IMAGE_CUIDS, null);
        putPresContexts(OTHER_CUIDS, null);
    }

    private void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws DcmServiceException, IOException {
        InputStream in = rq.getDataAsStream();
        try {
            Command rqCmd = rq.getCommand();
            String cuid = rqCmd.getAffectedSOPClassUID();
            String iuid = rqCmd.getAffectedSOPInstanceUID();
            String tsuid = rq.getTransferSyntaxUID();
            DcmDecodeParam decParam = DcmDecodeParam.valueOf(tsuid);
            String fileTS = decParam.encapsulated ? tsuid : UIDs.ExplicitVRLittleEndian;
            DcmEncodeParam encParam = DcmEncodeParam.valueOf(fileTS);
            Dataset ds = dof.newDataset();
            DcmParser parser = pf.newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, Tags.PixelData);
            checkDataset(rqCmd, ds);
            ds.setFileMetaInfo(dof.newFileMetaInfo(cuid, iuid, fileTS));
            File f = spoolDir.getInstanceFile(iuid);
            log.info("M-WRITE " + f);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
            try {
                ds.writeFile(out, encParam);
                if (parser.getReadTag() != Tags.PixelData) return;
                int len = parser.getReadLength();
                byte[] buffer = new byte[bufferSize];
                if (decParam.encapsulated) {
                    ds.writeHeader(out, encParam, Tags.PixelData, VRs.OB, -1);
                    parser.parseHeader();
                    while (parser.getReadTag() == Tags.Item) {
                        len = parser.getReadLength();
                        ds.writeHeader(out, encParam, Tags.Item, VRs.NONE, len);
                        copy(in, out, len, buffer);
                        parser.parseHeader();
                    }
                    ds.writeHeader(out, encParam, Tags.SeqDelimitationItem,
                            VRs.NONE, 0);
                } else {
                    ds.writeHeader(out, encParam, Tags.PixelData, parser
                            .getReadVR(), len);
                    copy(in, out, len, buffer);
                }
                parser.parseDataset(decParam, -1);
                ds.subSet(Tags.PixelData, -1).writeDataset(out, encParam);
            } finally {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        } catch (Throwable t) {
            throw new DcmServiceException(Status.ProcessingFailure, t);
        } finally {
            in.close();
        }
    }

    private void checkDataset(Command rqCmd, Dataset ds)
            throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) { throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "Missing Type 1 Attribute " + Tags.toString(TYPE1_ATTR[i])); }
        }
        if (!rqCmd.getAffectedSOPInstanceUID().equals(ds
                .getString(Tags.SOPInstanceUID))) { throw new DcmServiceException(
                Status.DataSetDoesNotMatchSOPClassError,
                "SOP Instance UID in Dataset differs from Affected SOP Instance UID"); }
        if (!rqCmd.getAffectedSOPClassUID().equals(ds
                .getString(Tags.SOPClassUID))) { throw new DcmServiceException(
                Status.DataSetDoesNotMatchSOPClassError,
                "SOP Class UID in Dataset differs from Affected SOP Class UID"); }
    }

    private void copy(InputStream in, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = in.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) { throw new EOFException(); }
            out.write(buffer, 0, len);
        }
    }
}