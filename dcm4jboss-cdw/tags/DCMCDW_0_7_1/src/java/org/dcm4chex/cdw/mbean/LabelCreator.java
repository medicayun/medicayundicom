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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.fop.apps.Driver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.messaging.MessageHandler;
import org.apache.log4j.Logger;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 4167 $ $Date: 2004-08-16 07:15:37 +0800 (周一, 16 8月 2004) $
 * @since 15.08.2004
 *
 */
class LabelCreator {

    private final Driver fop = new Driver();

    private boolean active = false;

    private int renderer;

    private String ext;

    private final Logger log = Logger.getLogger(LabelCreator.class);

    public LabelCreator() {
        Configuration.put("baseDir", "resource:xsl/");
        Log4JLogger logger = new Log4JLogger(log);
        fop.setLogger(logger);
        MessageHandler.setScreenLogger(logger);
    }

    public final String getCreateLabel() {
        return active ? (renderer == Driver.RENDER_PS ? "PS" : "PDF") : "NO";
    }

    public final void setCreateLabel(String format) {
        if ("PS".equalsIgnoreCase(format)) {
            fop.setRenderer(renderer = Driver.RENDER_PS);
            ext = ".ps";
            active = true;
        } else if ("PDF".equalsIgnoreCase(format)) {
            fop.setRenderer(renderer = Driver.RENDER_PDF);
            ext = ".pdf";
            active = true;
        } else {
            active = false;
        }
    }

    public void createLabel(MediaCreationRequest rq, DicomDirDOM dom)
            throws MediaCreationException {
        if (!active) return;
        File f = new File(rq.getFilesetDir().getParent(), rq
                .getFilesetDir().getName()
                + ext);
        log.info("Creating Label to for " + rq);
        rq.setLabelFile(f);
        OutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(rq
                    .getLabelFile()));
        } catch (FileNotFoundException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
        try {
            fop.setOutputStream(out);
            dom.createLabel(rq, fop.getContentHandler());
        } finally {
            fop.reset();
            try {
                out.close();
            } catch (IOException ignore) {
            }
        }
    }
}