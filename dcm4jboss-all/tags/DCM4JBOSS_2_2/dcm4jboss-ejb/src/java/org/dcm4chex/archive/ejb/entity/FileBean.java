/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.sql.Timestamp;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.MD5;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1624 $ $Date: 2005-03-17 09:49:29 +0800 (周四, 17 3月 2005) $
 * 
 * @ejb.bean name="File" type="CMP" view-type="local" primkey-field="pk"
 * 	         local-jndi-name="ejb/File"
 * @ejb.persistence table-name="files"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * 
 * @ejb.finder signature="java.util.Collection findDereferencedInFileSystem(java.lang.String dirPath)"
 *             query="SELECT OBJECT(f) FROM File AS f WHERE f.instance IS NULL AND f.fileSystem.directoryPath = ?1"
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findDereferencedInFileSystem(java.lang.String dirPath)"
 *              strategy="on-find" eager-load-group="*"
 * @ejb.finder signature="java.util.Collection findToCheckMd5(java.lang.String dirPath, java.sql.Timestamp before, int limit)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findToCheckMd5(java.lang.String dirPath, java.sql.Timestamp before, int limit)"
 *              query="SELECT OBJECT(f) FROM File AS f WHERE f.fileSystem.directoryPath = ?1 AND (f.timeOfLastMd5Check IS NULL OR f.timeOfLastMd5Check < ?2) LIMIT ?3"
 *              strategy="on-find" eager-load-group="*"
 *              
 * @jboss.query 
 * 	signature="java.util.Collection ejbSelectGeneric(java.lang.String jbossQl, java.lang.Object[] args)"
 *  dynamic="true"
 *  strategy="on-load"
 *  page-size="20"
 *  eager-load-group="*"
 *              
 */
public abstract class FileBean implements EntityBean {

    private static final Logger log = Logger.getLogger(FileBean.class);

    private static final String[] COMPRESSABLE_TRANSFER_SYNTAX = new String[] {
            UIDs.ImplicitVRLittleEndian, UIDs.ExplicitVRLittleEndian };

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
            UIDs.PositronEmissionTomographyImageStorage, UIDs.RTImageStorage, };

    /**
     * Auto-generated Primary Key
     * 
     * @ejb.interface-method 
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="created_time"
     */
    public abstract java.sql.Timestamp getCreatedTime();

    public abstract void setCreatedTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="md5_check_time"
     */
    public abstract java.sql.Timestamp getTimeOfLastMd5Check();

    /**
     * @ejb.interface-method
     */
    public abstract void setTimeOfLastMd5Check(java.sql.Timestamp time);

    /**
     * File Path (relative path to Directory).
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="filepath"
     */
    public abstract String getFilePath();

    public abstract void setFilePath(String path);

    /**
     * Transfer Syntax UID
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="file_tsuid"
     */
    public abstract String getFileTsuid();

    public abstract void setFileTsuid(String tsuid);

    /**
     * MD5 checksum as hex string
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="file_md5"
     */
    public abstract String getFileMd5Field();

    public abstract void setFileMd5Field(String md5);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="file_status"
     */
    public abstract int getFileStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setFileStatus(int status);

    /**
     * MD5 checksum in binary format
     * 
     * @ejb.interface-method
     */
    public byte[] getFileMd5() {
        return MD5.toBytes(getFileMd5Field());
    }

    public void setFileMd5(byte[] md5) {
        setFileMd5Field(MD5.toString(md5));
    }

    /**
     * File Size
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="file_size"
     */
    public abstract int getFileSize();

    public abstract void setFileSize(int size);

    /**
     * @ejb.interface-method
     * @ejb.relation name="instance-files"
     * 	             role-name="files-of-instance"
     * @jboss.relation fk-column="instance_fk"
     * 	               related-pk-field="pk"
     */
    public abstract void setInstance(InstanceLocal inst);

    /**
     * @ejb.interface-method
     */
    public abstract InstanceLocal getInstance();

    /**
     * @ejb.interface-method
     * @ejb.relation name="filesystem-files"
     * 	             role-name="files-of-filesystem"
     *               target-role-name="filesystem-of-file"
     *               target-ejb="FileSystem"
     *               target-multiple="yes"
     * @jboss.relation fk-column="filesystem_fk"
     * 	               related-pk-field="pk"
     */
    public abstract void setFileSystem(FileSystemLocal fs);

    /**
     * @ejb.interface-method
     */
    public abstract FileSystemLocal getFileSystem();

    /**
     * @ejb.interface-method
     */
    public boolean isRedundant() {
        InstanceLocal inst = getInstance();
        return inst == null || inst.getFiles().size() > 1;
    }

    /**
     * @ejb.interface-method
     * @jboss.method-attributes read-only="true"
     */
    public FileDTO getFileDTO() {
        FileSystemLocal fs = getFileSystem();
        FileDTO retval = new FileDTO();
        retval.setPk(getPk().intValue());
        retval.setRetrieveAET(fs.getRetrieveAET());
        retval.setDirectoryPath(fs.getDirectoryPath());
        retval.setFilePath(getFilePath());
        retval.setFileTsuid(getFileTsuid());
        retval.setFileSize(getFileSize());
        retval.setFileMd5(getFileMd5());
        retval.setFileStatus(getFileStatus());
        return retval;
    }

    /**
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "File[pk=" + getPk() + ", filepath=" + getFilePath()
                + ", tsuid=" + getFileTsuid() + ", filesystem->"
                + getFileSystem() + ", inst->" + getInstance() + "]";
    }

    /**
     * @ejb.home-method
     */
    public java.util.Collection ejbHomeQueryFilesToCompress(String[] srcaet,
            String[] dirPaths, Timestamp before, int limit)
            throws FinderException {
        // generate JBossQL query
        int i = 0;
        int argsCount = before != null ? 2 : 1;
        Object[] args = new Object[argsCount];
        StringBuffer jbossQl = new StringBuffer(
                "SELECT OBJECT(f) FROM File AS f WHERE f.fileStatus = 0 AND ");
        addIN(jbossQl, "f.fileTsuid", COMPRESSABLE_TRANSFER_SYNTAX);
        addIN(jbossQl, "f.instance.sopCuid", IMAGE_CUIDS);
        addIN(jbossQl, "f.instance.series.sourceAET", srcaet);
        addIN(jbossQl, "f.fileSystem.directoryPath", dirPaths);
        if (before != null) {
            args[i++] = before;
            jbossQl.append("(f.createdTime IS NULL OR f.createdTime < ?" + i
                    + ")");
        }
        args[i++] = new Integer(limit);
        jbossQl.append(" LIMIT ?").append(i);
        if (log.isDebugEnabled())
            log.debug("Execute JBossQL: " + jbossQl);
        // call dynamic-ql query
        return ejbSelectGeneric(jbossQl.toString(), args);
    }

    /*
     SELECT OBJECT(f) FROM File AS f WHERE f.fileTsuid IN ('1.2.840.10008.1.2.2', '1.2.840.10008.1.2.1', '1.2.840.10008.1.2') AND f.instance.sopCuid IN ('1.2.840.10008.5.1.4.1.1.2', '1.2.840.10008.5.1.4.1.1.2.1', '1.2.840.10008.5.1.4.1.1.4.1', '1.2.840.10008.5.1.4.1.1.4', '1.2.840.10008.5.1.4.1.1.4.2', '1.2.840.10008.5.1.4.1.1.7.4', '1.2.840.10008.5.1.4.1.1.77.1') AND f.instance.series.sourceAET = 'DB2_PACS_TIANI' AND f.fileSystem.directoryPath = ?1 AND (f.createdTime IS NULL OR f.createdTime < ?2) AND  LIMIT ?3
     */
    private void addIN(StringBuffer jbossQl, String col, Object[] values) {
        if (values != null) {
            jbossQl.append(col);
            if (values.length == 1) {
                jbossQl.append(" = '").append(values[0]).append("' AND ");
            } else {
                jbossQl.append(" IN ('").append(values[0]);
                for (int i = 1; i < values.length; i++) {
                    jbossQl.append("', '").append(values[i]);
                }
                jbossQl.append("') AND ");
            }
        }
    }

    /**
     * @ejb.select query=""
     *  transaction-type="Supports"
     */
    public abstract Collection ejbSelectGeneric(String jbossQl, Object[] args)
            throws FinderException;

    /**
     * Create file.
     * 
     * @ejb.create-method
     */
    public Integer ejbCreate(String path, String tsuid, int size, byte[] md5,
            InstanceLocal instance, FileSystemLocal filesystem)
            throws CreateException {
        setFilePath(path);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        setFileStatus(0);
        return null;
    }

    public void ejbPostCreate(String path, String tsuid, int size, byte[] md5,
            InstanceLocal instance, FileSystemLocal filesystem)
            throws CreateException {
        setInstance(instance);
        setFileSystem(filesystem);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
}
