/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.File;

import javax.management.JMException;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4189 $ $Date: 2004-09-26 04:36:31 +0800 (周日, 26 9月 2004) $
 * @since 23.06.2004
 *
 */
public class SpoolDirDelegate {

    private static final String ARCHIVE_HIGHWATER = "ArchiveHighWater";

    private static final String FILESET_HIGHWATER = "FilesetHighWater";

    private static final String REGISTER = "register";

    private static final String DELETE = "delete";

    private static final String COPY = "copy";

    private static final String MOVE = "move";

    private static final String DELETE_INSTANCE_FILE = "deleteInstanceFile";

    private static final String GET_MEDIA_FILESET_ROOT_DIR = "getMediaFilesetRootDir";

    private static final String GET_MEDIA_CREATION_REQUEST_FILE = "getMediaCreationRequestFile";

    private static final String GET_INSTANCE_FILE = "getInstanceFile";

    private static final String GET_LABEL_FILE = "getLabelFile";

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private final ServiceMBeanSupport service;

    private ObjectName spoolDirName;

    public SpoolDirDelegate(ServiceMBeanSupport service) {
        this.service = service;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDirName;
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        this.spoolDirName = spoolDirName;
    }

    public File getInstanceFile(String iuid) {
        return getSpoolFile(GET_INSTANCE_FILE, iuid);
    }

    public File getMediaCreationRequestFile(String iuid) {
        return getSpoolFile(GET_MEDIA_CREATION_REQUEST_FILE, iuid);
    }

    public File getMediaFilesetRootDir(String iuid) {
        return getSpoolFile(GET_MEDIA_FILESET_ROOT_DIR, iuid);
    }

    private File getSpoolFile(String op, String iuid) {
        try {
            return (File) service.getServer().invoke(spoolDirName,
                    op,
                    new Object[] { iuid},
                    new String[] { String.class.getName()});
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    public File getLabelFile(String iuid, String format) {
        try {
            return (File) service.getServer().invoke(spoolDirName,
                    GET_LABEL_FILE,
                    new Object[] { iuid, format},
                    new String[] { String.class.getName(),
                            String.class.getName()});
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    public void deleteRefInstances(Dataset rq) {
        if (Flag.isYES(rq
                .getString(Tags.PreserveCompositeInstancesAfterMediaCreation)))
                return;
        DcmElement refSOPs = rq.get(Tags.RefSOPSeq);
        for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
            Dataset item = refSOPs.getItem(i);
            deleteInstanceFile(item.getString(Tags.RefSOPInstanceUID));
        }
    }

    public void register(File f) {
        try {
            service.getServer().invoke(spoolDirName,
                    REGISTER,
                    new Object[] { f},
                    new String[] { File.class.getName()});
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    private boolean deleteInstanceFile(String iuid) {
        try {
            Boolean b = (Boolean) service.getServer().invoke(spoolDirName,
                    DELETE_INSTANCE_FILE,
                    new Object[] { iuid},
                    new String[] { String.class.getName()});
            return b.booleanValue();
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    public boolean delete(File f) {
        try {
            Boolean b = (Boolean) service.getServer().invoke(spoolDirName,
                    DELETE,
                    new Object[] { f},
                    new String[] { File.class.getName()});
            return b.booleanValue();
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    private boolean getBooleanAttribute(String attr) {
        try {
            Boolean b = (Boolean) service.getServer()
                    .getAttribute(spoolDirName, attr);
            return b.booleanValue();
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    public boolean isArchiveHighWater() {
        return getBooleanAttribute(ARCHIVE_HIGHWATER);
    }

    public boolean isFilesetHighWater() {
        return getBooleanAttribute(FILESET_HIGHWATER);
    }

    public boolean copy(File src, File dest, byte[] bbuf) {
        try {
            Boolean b = (Boolean) service.getServer().invoke(spoolDirName,
                    COPY,
                    new Object[] { src, dest, bbuf},
                    new String[] { File.class.getName(), File.class.getName(),
                            byte[].class.getName()});
            return b.booleanValue();
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

    public boolean move(File src, File dest) {
        try {
            Boolean b = (Boolean) service.getServer()
                    .invoke(spoolDirName,
                            MOVE,
                            new Object[] { src, dest},
                            new String[] { File.class.getName(),
                                    File.class.getName(),});
            return b.booleanValue();
        } catch (JMException e) {
            throw new ConfigurationException(e);
        }
    }

}