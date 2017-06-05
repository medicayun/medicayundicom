/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 05.10.2004
 *
 */
public class StructuredReportModel extends InstanceModel {

    public String documentTitle;

    public String completionFlag;

    public String verificationFlag;

    public StructuredReportModel(Dataset ds) {
        super(ds);
    }

    public final String getCompletionFlag() {
        return ds.getString(Tags.CompletionFlag);
    }

    public final String getDocumentTitle() {
        Dataset item = ds.getItem(Tags.ConceptNameCodeSeq);
        return item == null ? null : item.getString(Tags.CodeMeaning);
    }

    public final String getVerificationFlag() {
        return ds.getString(Tags.VerificationFlag);
    }
}