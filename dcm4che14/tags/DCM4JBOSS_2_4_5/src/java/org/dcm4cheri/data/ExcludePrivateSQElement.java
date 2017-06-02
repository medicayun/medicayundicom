/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.dict.VRs;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3803 $ $Date: 2004-09-21 04:36:32 +0800 (周二, 21 9月 2004) $
 * @since 20.09.2004
 *
 */
class ExcludePrivateSQElement extends DcmElementImpl implements DcmElement {

    private final SQElement sqElem;
    private int totlen = -1;

    public ExcludePrivateSQElement(SQElement sqElem) {
        super(sqElem.tag());
        this.sqElem = sqElem;
    }

    public final int vr() {
        return VRs.SQ;
    }

    public final int vm() {
        return sqElem.vm();
    }
    
    public Dataset getItem(int index) {
        return new FilterDataset.ExcludePrivate(sqElem.getItem(index));
    }
    
    public int calcLength(DcmEncodeParam param) {
        totlen = param.undefSeqLen ? 8 : 0;
        for (int i = 0, n = vm(); i < n; ++i)
            totlen += getItem(i).calcLength(param) +
                    (param.undefItemLen ? 16 : 8);
        return totlen;
    }
    
    public int length() {
        return totlen;
    }        
}
