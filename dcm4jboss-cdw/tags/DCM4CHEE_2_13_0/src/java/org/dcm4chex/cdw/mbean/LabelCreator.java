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
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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
 * @version $Revision: 4290 $ $Date: 2006-06-20 22:44:17 +0800 (周二, 20 6月 2006) $
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
        Configuration.put("baseDir", "resource:dcm4chee-cdw/");
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