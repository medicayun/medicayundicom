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

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4189 $ $Date: 2004-09-26 04:36:31 +0800 (周日, 26 9月 2004) $
 * @since 23.06.2004
 *
 */
public class LabelPrintDelegate {

    private static final String PRINT = "print";

    private static final String IF_NOT_PRINTED = "IF_NOT_PRINTED";

    private boolean printLabel = false;

    private String keepLabelFiles = IF_NOT_PRINTED;

    private final ServiceMBeanSupport service;

    private ObjectName labelPrintName;

    public LabelPrintDelegate(ServiceMBeanSupport service) {
        this.service = service;
    }

    public final ObjectName getLabelPrintName() {
        return labelPrintName;
    }

    public final void setLabelPrintName(ObjectName spoolDirName) {
        this.labelPrintName = spoolDirName;
    }

    public final String getKeepLabelFiles() {
        return keepLabelFiles;
    }

    public final void setKeepLabelFiles(String s) {
        if (!(s.equals(IF_NOT_PRINTED) || Flag.isValid(s)))
                throw new IllegalArgumentException(s);
        this.keepLabelFiles = s;
    }

    public final boolean isPrintLabel() {
        return printLabel;
    }

    public final void setPrintLabel(boolean printLabel) {
        this.printLabel = printLabel;
    }

    public boolean print(MediaCreationRequest mcrq) {
        boolean keepSpoolFile = !Flag.isNO(keepLabelFiles);
        File f = mcrq.getLabelFile();
        try {
	        if (!printLabel) return false;
	        if (f == null) {
	            service.getLog()
	                    .warn("Failed to print label: No Label File created for "
	                            + mcrq);
	            return true;
	        }
            service.getServer().invoke(labelPrintName,
                    PRINT,
                    new Object[] { f},
                    new String[] { File.class.getName()});
            keepSpoolFile = Flag.isYES(keepLabelFiles);
            return true;
        } catch (JMException e) {
            service.getLog().warn("Failed to print label " + f, e);
            return false;
        } finally {
            if (keepSpoolFile)
                mcrq.setKeepLabelFile(true);
        }
    }

}