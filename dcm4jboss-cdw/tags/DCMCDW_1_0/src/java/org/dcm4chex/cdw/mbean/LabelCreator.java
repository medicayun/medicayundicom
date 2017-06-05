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
 * @version $Revision: 4189 $ $Date: 2004-09-26 04:36:31 +0800 (周日, 26 9月 2004) $
 * @since 15.08.2004
 *
 */
class LabelCreator {

    private static final String NO = "NO";

    private static final String PS = "PS";

    private static final String PDF = "PDF";

    private final Driver fop = new Driver();

    private int renderer = 0;

    private final Logger log = Logger.getLogger(LabelCreator.class);

    public LabelCreator() {
        Configuration.put("baseDir", "resource:xsl/");
        Log4JLogger logger = new Log4JLogger(log);
        fop.setLogger(logger);
        MessageHandler.setScreenLogger(logger);
    }

    public final boolean isActive() {
        return renderer > 0;
    }

    public final String getLabelFileFormat() {
        switch (renderer) {
        case Driver.RENDER_PDF:
            return PDF;
        case Driver.RENDER_PS:
            return PS;
        default:
            return NO;
        }
    }

    public final void setLabelFileFormat(String format) {
        if (PS.equalsIgnoreCase(format)) {
            fop.setRenderer(renderer = Driver.RENDER_PS);
        } else if (PDF.equalsIgnoreCase(format)) {
            fop.setRenderer(renderer = Driver.RENDER_PDF);
        } else {
            renderer = 0;
        }
    }

    public void createLabel(MediaCreationRequest rq, DicomDirDOM dom)
            throws MediaCreationException {
        if (renderer == 0) return;
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