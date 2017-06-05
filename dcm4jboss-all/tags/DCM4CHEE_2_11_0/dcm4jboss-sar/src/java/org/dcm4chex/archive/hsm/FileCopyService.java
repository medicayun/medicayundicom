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
 * Portions created by the Initial Developer are Copyright (C) 2005
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

package org.dcm4chex.archive.hsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarOutputStream;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.BufferedOutputStream;
import org.dcm4che.util.Executer;
import org.dcm4che.util.MD5Utils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.ejb.interfaces.MD5;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3444 $ $Date: 2007-07-04 14:16:06 +0800 (周三, 04 7月 2007) $
 * @since Nov 9, 2005
 */
public class FileCopyService extends AbstractFileCopyService {

    private static final String NONE = "NONE";    
    private static final String SRC_PARAM = "%p";
    private static final String FS_PARAM = "%d";
    private static final String FILE_PARAM = "%f";
    private static final int MD5SUM_ENTRY_LEN = 52;
    
    private String[] tarCopyCmd = null;   
    private File tarOutgoingDir;
    private File absTarOutgoingDir;
    
    public final String getTarCopyCommand() {
        if (tarCopyCmd == null) {
            return NONE;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tarCopyCmd.length; i++) {
            sb.append(tarCopyCmd[i]);
        }
        return sb.toString();
    }

    public final void setTarCopyCommand(String cmd) {
        if (NONE.equalsIgnoreCase(cmd)) {
            this.tarCopyCmd = null;
            return;
        }
        String[] a = StringUtils.split(cmd, '%');
        try {
            String[] b = new String[a.length + a.length - 1];
            b[0] = a[0];
            for (int i = 1; i < a.length; i++) {
                String s = a[i];
                b[2 * i - 1] = ("%" + s.charAt(0)).intern();
                b[2 * i] = s.substring(1);
            }
            this.tarCopyCmd = b;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(cmd);
        }
    }
    
    public boolean copyFilesOfStudy(String studyIUID) throws SQLException {
        log.info("Start copy files of study "+studyIUID);
        Dataset queryDs = DcmObjectFactory.getInstance().newDataset();
        queryDs.putCS(Tags.QueryRetrieveLevel, "SERIES");
        queryDs.putUI(Tags.StudyInstanceUID, studyIUID);
        queryDs.putUI(Tags.SeriesInstanceUID);
        QueryCmd cmd = QueryCmd.create(queryDs, true, false);
        cmd.execute();
        while ( cmd.next() ) {
            if ( ! copyFilesOfSeries(cmd.getDataset().getString(Tags.SeriesInstanceUID) ) ) {
                return false;
            }
        }
        cmd.close();
        return true;
    }
    public boolean copyFilesOfSeries(String seriesIUID) throws SQLException {
        Dataset queryDs = DcmObjectFactory.getInstance().newDataset();
        queryDs.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        queryDs.putUI(Tags.StudyInstanceUID);
        queryDs.putUI(Tags.SeriesInstanceUID, seriesIUID);
        queryDs.putUI(Tags.SOPInstanceUID);
        QueryCmd cmd = QueryCmd.create(queryDs, true, false);
        cmd.execute();
        Dataset ds = null;
        Dataset ian = DcmObjectFactory.getInstance().newDataset();
        Dataset refSeries = ian.putSQ(Tags.RefSeriesSeq).addNewItem();
        DcmElement refSOPs = refSeries.putSQ(Tags.RefSOPSeq);
        while ( cmd.next() ) {
            ds = cmd.getDataset();
            refSeries.putUI(Tags.SeriesInstanceUID, ds.getString(Tags.SeriesInstanceUID));
            refSOPs.addNewItem().putUI(Tags.RefSOPInstanceUID, ds.getString(Tags.SOPInstanceUID));
        }
        cmd.close();
        if ( ds != null ) {
            ian.putUI(Tags.StudyInstanceUID, ds.getString(Tags.StudyInstanceUID));
            refSeries.putUI(Tags.SeriesInstanceUID, ds.getString(Tags.SeriesInstanceUID));
            schedule(createOrder(ian),0l);
            log.info("Copy files of series "+seriesIUID+" scheduled!");
            return true;
        } else {
            log.info("No instances found for file copy! QueryDS:");
            log.info(queryDs);
            return false;
        }

    }

    private String makeCommand(String srcParam, String fsID, String fileID) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tarCopyCmd.length; i++) {
            sb.append(tarCopyCmd[i] == SRC_PARAM ? srcParam
                    : tarCopyCmd[i] == FS_PARAM ? fsID
                    : tarCopyCmd[i] == FILE_PARAM ? fileID
                    : tarCopyCmd[i]);
        }
        return sb.toString();
    }
    
    public final String getTarOutgoingDir() {
        return tarOutgoingDir.getPath();
    }

    public final void setTarOutgoingDir(String tarOutgoingDir) {
        this.tarOutgoingDir = new File(tarOutgoingDir);
        this.absTarOutgoingDir = FileUtils.resolve(this.tarOutgoingDir);
    }
    
    protected BaseJmsOrder createOrder(Dataset ian) {
        return new FileCopyOrder(ian, ForwardingRules.toAET(destination), getRetrieveAET());
    }

    protected void process(BaseJmsOrder order) throws Exception {
        String destPath = ((FileCopyOrder)order).getDestinationFileSystemPath();
        List fileInfos = ((FileCopyOrder)order).getFileInfos();
        if (destPath.startsWith("tar:")) {
            copyTar(fileInfos, destPath);
        } else {
            copyFiles(fileInfos, destPath);
        }
    }

    private void copyFiles(List fileInfos, String destPath)
            throws Exception {
        byte[] buffer = new byte[bufferSize];
        Storage storage = getStorageHome().create();
        Exception ex = null;
        MessageDigest digest = null;
        if (verifyCopy)
            digest = MessageDigest.getInstance("MD5");
        for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
            FileInfo finfo = (FileInfo) iter.next();
            File src = FileUtils.toFile(finfo.basedir + '/' + finfo.fileID);
            File dst = FileUtils.toFile(destPath + '/' + finfo.fileID);
            try {
                copy(src, dst, buffer);
                byte[] md5sum0 = finfo.md5 != null ? MD5Utils
                        .toBytes(finfo.md5) : null;
                if (md5sum0 != null && digest != null) {
                    byte[] md5sum = MD5Utils.md5sum(dst, digest, buffer);
                    if (!Arrays.equals(md5sum0, md5sum)) {
                        String prompt = "md5 sum of copy " + dst
                                + " differs from md5 sum in DB for file " + src;
                        log.warn(prompt);
                        throw new IOException(prompt);
                    }
                }
                storage.storeFile(finfo.sopIUID, finfo.tsUID, destPath,
                        finfo.fileID, (int) finfo.size, md5sum0, fileStatus);
                iter.remove();
            } catch (Exception e) {
                dst.delete();
                ex = e;
            }
        }
        if (ex != null)
            throw ex;
    }

    private void copy(File src, File dst, byte[] buffer) throws IOException {
        FileInputStream fis = new FileInputStream(src);
        try {
            File dir = dst.getParentFile();
            if (dir.mkdirs()) {
                log.info("M-WRITE dir:" + dir);
            }
            log.info("M-WRITE file:" + dst);
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(dst), buffer);
            try {
                bos.copyFrom(fis, (int) src.length());
            } finally {
                bos.close();
            }
        } catch (IOException e) {
            dst.delete();
            throw e;
        } finally {
            fis.close();
        }
    }

    private void copyTar(List fileInfos, String destPath) throws Exception {
        FileInfo file1Info = (FileInfo) fileInfos.get(0);
        String tarPath = mkTarPath(file1Info.fileID);
        if (tarCopyCmd == null) {
            File tarFile = FileUtils.toFile(destPath.substring(4), tarPath);
            log.info("M-WRITE " + tarFile);
            tarFile.getParentFile().mkdirs();
            mkTar(fileInfos, tarFile);
        } else {
            if (absTarOutgoingDir.mkdirs()) {
                log.info("M-WRITE " + absTarOutgoingDir);
            }
            int tarPathLen = tarPath.length();
            File tarFile = new File(absTarOutgoingDir,
                    tarPath.substring(tarPathLen - 21));
            try {
                log.info("M-WRITE " + tarFile);
                mkTar(fileInfos, tarFile);
                String cmd = makeCommand(tarFile.getPath(), destPath, tarPath);
                log.info("Copy to HSM: " + cmd);
                Executer ex = new Executer(cmd);
                int exit = ex.waitFor();
                if (exit != 0) {
                    throw new IOException("Non-zero exit code(" + exit 
                            + ") of " + cmd);
                }
            } finally {
                log.info("M-DELETE " + tarFile);
                tarFile.delete();
            }
        }
        Storage storage = getStorageHome().create();
        for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
            FileInfo finfo = (FileInfo) iter.next();
            String fileId = tarPath + '!' + mkTarEntryName(finfo.fileID);
            storage.storeFile(finfo.sopIUID, finfo.tsUID, destPath, fileId,
                    (int) finfo.size, MD5.toBytes(finfo.md5), fileStatus);
        }
    }
    
    private void mkTar(List fileInfos, File tarFile) throws Exception {
        try {
            TarOutputStream tar = new TarOutputStream(
                    new FileOutputStream(tarFile));
            try {
                writeMD5SUM(tar, fileInfos);
                for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
                    writeFile(tar, (FileInfo) iter.next());
                }
            } finally {
                tar.close();
            }
            if (verifyCopy) {
                VerifyTar.verify(tarFile, new byte[bufferSize]);
            }
        } catch (Exception e) {
            tarFile.delete();
            throw e;
        }
    }

    private void writeMD5SUM(TarOutputStream tar, List fileInfos)
            throws IOException {
        byte[] md5sum = new byte[fileInfos.size() * MD5SUM_ENTRY_LEN];
        final TarEntry tarEntry = new TarEntry("MD5SUM");
        tarEntry.setSize(md5sum.length);
        tar.putNextEntry(tarEntry);
        
        int i = 0;
        for (Iterator iter = fileInfos.iterator(); iter.hasNext(); 
                i += MD5SUM_ENTRY_LEN) {
            FileInfo fileInfo = (FileInfo) iter.next();
            MD5Utils.toHexChars(MD5.toBytes(fileInfo.md5), md5sum, i);
            
            md5sum[i+32] = ' ';
            md5sum[i+33] = ' ';
            System.arraycopy(
                    mkTarEntryName(fileInfo.fileID).getBytes("US-ASCII"), 0, 
                    md5sum, i+34, 17);
            md5sum[i+51] = '\n';
        }
        tar.write(md5sum);
        tar.closeEntry();
    }

    private void writeFile(TarOutputStream tar, FileInfo fileInfo) 
    throws IOException, FileNotFoundException {
        File file = FileUtils.toFile(fileInfo.basedir, fileInfo.fileID);
        TarEntry entry = new TarEntry(mkTarEntryName(fileInfo.fileID));
        entry.setSize(fileInfo.size);
        tar.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(file);
        try {
            tar.copyEntryContents(fis);
        } finally {
            fis.close();
        }
        tar.closeEntry();
    }
    
    private String mkTarEntryName(String filePath) {
        return filePath.substring(filePath.length() - 17);
    }

    private String mkTarPath(String filePath) {
        int len = filePath.length();
        StringBuffer sb = new StringBuffer(len + 4);
        sb.append(filePath).append(".tar").setCharAt(len - 9, '-');
        return sb.toString();
    }
}
