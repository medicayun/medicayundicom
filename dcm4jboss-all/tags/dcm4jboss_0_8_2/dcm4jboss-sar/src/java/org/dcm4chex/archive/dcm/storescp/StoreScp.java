/*
 * $Id: StoreScp.java 1134 2004-06-15 11:16:25Z gunterze $ Copyright (c)
 * 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.dcm.storescp;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.StorageRules;
import org.dcm4chex.archive.ejb.interfaces.DuplicateStorageException;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderQueue;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderQueueHome;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderValue;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1134 $
 * @since 03.08.2003
 */
public class StoreScp extends DcmServiceBase implements AssociationListener {

    private static final AuditLoggerFactory alf = AuditLoggerFactory
            .getInstance();

    private static final String STORESCP = "org.dcm4chex.service.StoreScp";

    private static final int[] TYPE1_ATTR = { Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID, Tags.SOPInstanceUID, Tags.SOPClassUID,};

    private static final AssociationFactory af = AssociationFactory
            .getInstance();

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final DcmParserFactory pf = DcmParserFactory.getInstance();

    private final StoreScpService service;

    private int bufferSize = 512;

    private int updateDatabaseMaxRetries = 2;

    private int forwardPriority = 0;

    private ForwardingRules forwardingRules = new ForwardingRules("");

    private CompressionRules compressionRules = new CompressionRules("");

    private StorageRules storageRules = new StorageRules("archive");

    private HashSet warningAsSuccessSet = new HashSet();

    public StoreScp(StoreScpService service) {
        this.service = service;
    }

    public final String[] getMaskWarningAsSuccessForCallingAETs() {
        return (String[]) warningAsSuccessSet
                .toArray(new String[warningAsSuccessSet.size()]);
    }

    public final void setMaskWarningAsSuccessForCallingAETs(String[] aets) {
        warningAsSuccessSet.clear();
        warningAsSuccessSet.addAll(Arrays.asList(aets));
    }

    public final CompressionRules getCompressionRules() {
        return compressionRules;
    }

    public final void setCompressionRules(CompressionRules compressionRules) {
        this.compressionRules = compressionRules;
    }

    public final ForwardingRules getForwardingRules() {
        return forwardingRules;
    }

    public final void setForwardingRules(ForwardingRules forwardingRules) {
        this.forwardingRules = forwardingRules;
    }

    public final int getForwardPriority() {
        return forwardPriority;
    }

    public final void setForwardPriority(int forwardPriority) {
        this.forwardPriority = forwardPriority;
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final StorageRules getStorageRules() {
        return storageRules;
    }

    public final void setStorageRules(StorageRules rules) {
        this.storageRules = rules;
    }

    public final int getUpdateDatabaseMaxRetries() {
        return updateDatabaseMaxRetries;
    }

    public final void setUpdateDatabaseMaxRetries(int updateDatabaseMaxRetries) {
        this.updateDatabaseMaxRetries = updateDatabaseMaxRetries;
    }

    void checkReadyToStart() {
        if (service.getRetrieveAETs().length == 0)
                throw new IllegalStateException("No Retrieve AET configured!");
    }

    protected void doCStore(ActiveAssociation activeAssoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        InputStream in = rq.getDataAsStream();
        Association assoc = activeAssoc.getAssociation();
        File file = null;
        try {
            DcmDecodeParam decParam = DcmDecodeParam.valueOf(rq
                    .getTransferSyntaxUID());
            Dataset ds = objFact.newDataset();
            DcmParser parser = pf.newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, Tags.PixelData);
            service.logDataset("Dataset:\n", ds);
            checkDataset(rqCmd, ds);

            Calendar today = Calendar.getInstance();
            File dir = toStorageDir(storageRules.getStorageLocationFor(assoc));
            file = makeFile(dir, ds);
            MessageDigest md = MessageDigest.getInstance("MD5");
            String compressTSUID = parser.getReadTag() == Tags.PixelData
                    && parser.getReadLength() != -1 ? compressionRules
                    .getTransferSyntaxFor(assoc, ds) : null;
            ds.setFileMetaInfo(objFact.newFileMetaInfo(rqCmd
                    .getAffectedSOPClassUID(), rqCmd
                    .getAffectedSOPInstanceUID(),
                    compressTSUID != null ? compressTSUID : rq
                            .getTransferSyntaxUID()));

            storeToFile(parser, ds, file, md);

            final String dirPath = dir.getCanonicalPath().replace(
                    File.separatorChar, '/');
            final String filePath = file.getCanonicalPath().replace(
                    File.separatorChar, '/').substring(dirPath.length() + 1);
            try {
                Dataset coercedElements = updateDB(assoc, ds, dirPath,
                        filePath, (int) file.length(), md.digest());
                if (coercedElements.isEmpty()
                        || warningAsSuccessSet.contains(assoc.getCallingAET())) {
                    rspCmd.putUS(Tags.Status, Status.Success);
                } else {
                    int[] coercedTags = new int[coercedElements.size()];
                    Iterator it = coercedElements.iterator();
                    for (int i = 0; i < coercedTags.length; i++) {
                        coercedTags[i] = ((DcmElement) it.next()).tag();
                    }
                    rspCmd.putAT(Tags.OffendingElement, coercedTags);
                    rspCmd.putUS(Tags.Status, Status.CoercionOfDataElements);
                    ds.putAll(coercedElements);
                }
                updateStoredStudiesInfo(assoc, ds);
                updateInstancesStored(assoc, ds);
            } catch (DuplicateStorageException e) {
                service.getLog().warn(
                        "ignore attempt to store instance[uid="
                                + rqCmd.getAffectedSOPInstanceUID()
                                + "] in directory[" + file.getParent()
                                + "] duplicated");
                deleteFailedStorage(file);
            }
        } catch (DcmServiceException e) {
            service.getLog().warn(e.getMessage(), e);
            deleteFailedStorage(file);
            throw e;
        } catch (Throwable e) {
            service.getLog().error(e.getMessage(), e);
            deleteFailedStorage(file);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        } finally {
            in.close();
        }
    }

    private File toStorageDir(String location) {
        File dir = new File(location);
        return dir.isAbsolute() ? dir : new File(ServerConfigLocator.locate()
                .getServerHomeDir(), location);
    }

    private void deleteFailedStorage(File file) {
        if (file == null) return;
        service.getLog().info("M-DELETE file:" + file);
        file.delete();
    }

    private Dataset updateDB(Association assoc, Dataset ds, String dirPath,
            String filePath, int fileLength, byte[] md5)
            throws DcmServiceException, RemoteException, CreateException,
            HomeFactoryException, DuplicateStorageException {
        Storage storage = getStorageHome().create();
        try {
            int retry = 0;
            for (;;) {
                try {
                    return storage.store(assoc.getCallingAET(), assoc
                            .getCalledAET(), ds, service.getRetrieveAETs(),
                            dirPath, filePath, fileLength, md5);
                } catch (DuplicateStorageException e) {
                    throw e;
                } catch (Exception e) {
                    if (retry++ >= updateDatabaseMaxRetries) {
                        service.getLog().error(
                                "failed to update DB with entries for received "
                                        + dirPath + "/" + filePath, e);
                        throw new DcmServiceException(Status.ProcessingFailure,
                                e);
                    }
                    service.getLog().warn(
                            "failed to update DB with entries for received "
                                    + dirPath + "/" + filePath + " - retry", e);
                }
            }
        } finally {
            try {
                storage.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private File makeFile(File basedir, Dataset ds) throws IOException {
        Calendar today = Calendar.getInstance();
        File dir = new File(basedir, String.valueOf(today.get(Calendar.YEAR))
                + File.separator + toDec(today.get(Calendar.MONTH) + 1)
                + File.separator + toDec(today.get(Calendar.DAY_OF_MONTH))
                + File.separator
                + toHex(ds.getString(Tags.StudyInstanceUID).hashCode())
                + File.separator
                + toHex(ds.getString(Tags.SeriesInstanceUID).hashCode()));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (int hash = ds.getString(Tags.SOPInstanceUID).hashCode();; ++hash) {
            File f = new File(dir, toHex(hash));
            if (f.createNewFile()) { return f; }
        }
    }

    private File toFile(String basedir, String[] fileIDs) {
        File dir = new File(basedir, fileIDs[0] + File.separatorChar
                + fileIDs[1] + File.separatorChar + fileIDs[2]
                + File.separatorChar + fileIDs[3] + File.separatorChar
                + fileIDs[4]);
        File file;
        while ((file = new File(dir, fileIDs[5])).exists()) {
            fileIDs[5] = toHex((int) Long.parseLong(fileIDs[5], 16) + 1);
        }
        return file;
    }

    private StorageHome getStorageHome() throws HomeFactoryException {
        return (StorageHome) EJBHomeFactory.getFactory().lookup(
                StorageHome.class, StorageHome.JNDI_NAME);
    }

    private MoveOrderQueueHome getMoveOrderQueueHome()
            throws HomeFactoryException {
        return (MoveOrderQueueHome) EJBHomeFactory.getFactory().lookup(
                MoveOrderQueueHome.class, MoveOrderQueueHome.JNDI_NAME);
    }

    private void storeToFile(DcmParser parser, Dataset ds, File file,
            MessageDigest md) throws Exception {
        service.getLog().info("M-WRITE file:" + file);
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(file));
        DigestOutputStream dos = new DigestOutputStream(os, md);
        try {
            DcmDecodeParam decParam = parser.getDcmDecodeParam();
            DcmEncodeParam encParam = DcmEncodeParam.valueOf(ds
                    .getFileMetaInfo().getTransferSyntaxUID());
            CompressCmd compressCmd = null;
            if (!decParam.encapsulated && encParam.encapsulated) {
                compressCmd = CompressCmd.createCompressCmd(service, ds);
                compressCmd.coerceDataset(ds);
            }
            ds.writeFile(dos, encParam);
            if (parser.getReadTag() != Tags.PixelData) return;
            int len = parser.getReadLength();
            InputStream in = parser.getInputStream();
            byte[] buffer = new byte[bufferSize];
            if (encParam.encapsulated) {
                ds.writeHeader(dos, encParam, Tags.PixelData, VRs.OB, -1);
                if (decParam.encapsulated) {
                    parser.parseHeader();
                    while (parser.getReadTag() == Tags.Item) {
                        len = parser.getReadLength();
                        ds.writeHeader(dos, encParam, Tags.Item, VRs.NONE, len);
                        copy(in, dos, len, buffer);
                        parser.parseHeader();
                    }
                } else {
                    int read = compressCmd.compress(decParam.byteOrder, parser
                            .getInputStream(), dos);
                    in.skip(parser.getReadLength() - read);
                }
                ds.writeHeader(dos, encParam, Tags.SeqDelimitationItem,
                        VRs.NONE, 0);
            } else {
                ds.writeHeader(dos, encParam, Tags.PixelData, parser
                        .getReadVR(), len);
                copy(in, dos, len, buffer);
            }
            parser.parseDataset(encParam, -1);
            ds.subSet(Tags.PixelData, -1).writeDataset(dos, encParam);
        } finally {
            try {
                dos.close();
            } catch (IOException ignore) {
            }
        }
    }

    private void copy(InputStream in, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = in.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) { throw new EOFException(); }
            out.write(buffer, 0, len);
        }
    }

    private void checkDataset(Command rqCmd, Dataset ds)
            throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) { throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "Missing Type 1 Attribute " + Tags.toString(TYPE1_ATTR[i])); }
        }
        if (!rqCmd.getAffectedSOPInstanceUID().equals(
                ds.getString(Tags.SOPInstanceUID))) { throw new DcmServiceException(
                Status.DataSetDoesNotMatchSOPClassError,
                "SOP Instance UID in Dataset differs from Affected SOP Instance UID"); }
        if (!rqCmd.getAffectedSOPClassUID().equals(
                ds.getString(Tags.SOPClassUID))) { throw new DcmServiceException(
                Status.DataSetDoesNotMatchSOPClassError,
                "SOP Class UID in Dataset differs from Affected SOP Class UID"); }
    }

    private static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4) {
            ch8[i] = HEX_DIGIT[val & 0xf];
        }
        return String.valueOf(ch8);
    }

    private String toDec(int val) {
        return String.valueOf(new char[] { HEX_DIGIT[val / 10],
                HEX_DIGIT[val % 10]});
    }

    private void mkdir(File dir) {
        if (dir.mkdir()) {
            service.getLog().info("M-WRITE dir:" + dir);
        }
    } // Implementation of AssociationListener

    public void write(Association src, PDU pdu) {
    }

    public void received(Association src, PDU pdu) {
    }

    public void write(Association src, Dimse dimse) {
    }

    public void received(Association src, Dimse dimse) {
    }

    public void error(Association src, IOException ioe) {
    }

    public void close(Association assoc) {
        logInstancesStored(assoc);
        Map storedStudiesInfo = (Map) assoc.getProperty(STORESCP);
        if (storedStudiesInfo != null) {
            forward(forwardingRules.getForwardDestinationsFor(assoc),
                    storedStudiesInfo.values().iterator());
        }
    }

    private void updateStoredStudiesInfo(Association assoc, Dataset ds) {
        Map storedStudiesInfo = (Map) assoc.getProperty(STORESCP);
        if (storedStudiesInfo == null) {
            assoc.putProperty(STORESCP, storedStudiesInfo = new HashMap());
        }
        Dataset refSOP = getRefImageSeq(ds,
                getRefSeriesSeq(ds, storedStudiesInfo)).addNewItem();
        refSOP.putUI(Tags.RefSOPClassUID, ds.getString(Tags.SOPClassUID));
        refSOP.putUI(Tags.RefSOPInstanceUID, ds.getString(Tags.SOPInstanceUID));
    }

    private DcmElement getRefSeriesSeq(Dataset ds, Map storedStudiesInfo) {
        final String siud = ds.getString(Tags.StudyInstanceUID);
        Dataset info = (Dataset) storedStudiesInfo.get(siud);
        if (info != null) { return info.get(Tags.RefSeriesSeq); }
        storedStudiesInfo.put(siud, info = dof.newDataset());
        info.putLO(Tags.PatientID, ds.getString(Tags.PatientID));
        info.putPN(Tags.PatientName, ds.getString(Tags.PatientName));
        info.putSH(Tags.StudyID, ds.getString(Tags.StudyID));
        info.putUI(Tags.StudyInstanceUID, siud);
        return info.putSQ(Tags.RefSeriesSeq);
    }

    private DcmElement getRefImageSeq(Dataset ds, DcmElement seriesSq) {
        final String siud = ds.getString(Tags.SeriesInstanceUID);
        Dataset info;
        for (int i = 0, n = seriesSq.vm(); i < n; ++i) {
            info = seriesSq.getItem(i);
            if (siud.equals(info.getString(Tags.SeriesInstanceUID))) { return info
                    .get(Tags.RefImageSeq); }
        }
        info = seriesSq.addNewItem();
        info.putUI(Tags.SeriesInstanceUID, siud);
        return info.putSQ(Tags.RefImageSeq);
    }

    private static String firstOf(String s, char delim) {
        int delimPos = s.indexOf(delim);
        return delimPos == -1 ? s : s.substring(0, delimPos);
    }

    private void forward(String[] destAETs, Iterator scns) {
        if (destAETs.length == 0) { return; }

        MoveOrderQueue orderQueue;
        try {
            orderQueue = getMoveOrderQueueHome().create();
        } catch (Exception e) {
            service.getLog().error("Failed to access Move Order Queue", e);
            return;
        }
        final MoveOrderValue order = new MoveOrderValue();
        order.setScheduledTime(new Date());
        order.setRetrieveAET(service.getRetrieveAET());
        order.setPriority(forwardPriority);
        while (scns.hasNext()) {
            Dataset scn = (Dataset) scns.next();
            DcmElement refSeriesSeq = scn.get(Tags.RefSeriesSeq);
            order.setStudyIuids(scn.getString(Tags.StudyInstanceUID));
            for (int i = 0, n = refSeriesSeq.vm(); i < n; ++i) {
                Dataset refSeries = refSeriesSeq.getItem(i);
                DcmElement refSOPSeq = refSeries.get(Tags.RefImageSeq);
                StringBuffer sopIUIDs = new StringBuffer();
                for (int j = 0, m = refSOPSeq.vm(); j < m; ++j) {
                    if (j != 0) {
                        sopIUIDs.append('\\');
                    }
                    sopIUIDs.append(refSOPSeq.getItem(j).getString(
                            Tags.RefSOPInstanceUID));
                }
                order.setSeriesIuids(refSeries
                        .getString(Tags.SeriesInstanceUID));
                order.setSopIuids(sopIUIDs.toString());

                for (int k = 0; k < destAETs.length; ++k) {
                    order.setMoveDestination(destAETs[k]);
                    try {
                        orderQueue.queue(order);
                    } catch (RemoteException e) {
                        service.getLog().error("Failed to queue " + order, e);
                    }
                }
            }
        }
    }

    void updateInstancesStored(Association assoc, Dataset ds) {
        if (service.getAuditLogger() == null) { return; }
        try {
            InstancesAction stored = (InstancesAction) assoc
                    .getProperty("InstancesStored");
            String suid = ds.getString(Tags.StudyInstanceUID);
            if (stored != null
                    && !stored.listStudyInstanceUIDs()[0].equals(suid)) {
                logInstancesStored(assoc);
                stored = null;
            }
            if (stored == null) {
                stored = alf.newInstancesAction("Create", suid, alf.newPatient(
                        ds.getString(Tags.PatientID), ds
                                .getString(Tags.PatientName)));
                stored.setAccessionNumber(ds.getString(Tags.AccessionNumber));
                assoc.putProperty("InstancesStored", stored);
            }
            stored.incNumberOfInstances(1);
            stored.addSOPClassUID(ds.getString(Tags.SOPClassUID));
        } catch (Exception e) {
            service.getLog().error("Could not audit log InstancesStored:", e);
        }
    }

    void logInstancesStored(Association assoc) {
        if (service.getAuditLogger() == null) { return; }
        InstancesAction stored = (InstancesAction) assoc
                .getProperty("InstancesStored");
        if (stored != null) {
            service.getAuditLogger()
                    .logInstancesStored(
                            alf.newRemoteNode(assoc.getSocket(), assoc
                                    .getCallingAET()), stored);
        }
        assoc.putProperty("InstancesStored", null);
    }
}
