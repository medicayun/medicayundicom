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

import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4128 $ $Date: 2004-06-28 08:25:30 +0800 (周一, 28 6月 2004) $
 * @since 23.06.2004
 *
 */
public class SpoolDirDelegate {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String GET_MEDIA_FILESET_ROOT_DIR = "getMediaFilesetRootDir";

    private static final String GET_MEDIA_CREATION_REQUEST_FILE = "getMediaCreationRequestFile";

    private static final String GET_INSTANCE_FILE = "getInstanceFile";

    private static final String DELETE_INSTANCE_FILE = "deleteInstanceFile";

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

    public boolean deleteInstanceFile(String iuid) {
        return getInstanceFile(iuid).delete();
    }

    private File getSpoolFile(String op, String iuid) {
        try {
            return (File) service.getServer().invoke(spoolDirName, op,
                    new Object[] { iuid},
                    new String[] { String.class.getName()});
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public void deleteRefInstances(Dataset rq) {
        if (Flag.isYes(rq.getString(Tags.PreserveCompositeInstancesAfterMediaCreation)))
            return;
        DcmElement refSOPs = rq.get(Tags.RefSOPSeq);
        for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
            Dataset item = refSOPs.getItem(i);
            deleteInstanceFile(item.getString(Tags.RefSOPInstanceUID));
        }
    }
}
