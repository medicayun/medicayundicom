/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.ppsscp;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1374 $ $Date: 2005-01-04 10:04:05 +0800 (周二, 04 1月 2005) $
 * @since 10.03.2004
 */
public class PPSScpService extends AbstractScpService {

    private PPSScp mppsScp = new PPSScp(this);
    
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.GeneralPurposePerformedProcedureStepSOPClass, mppsScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.GeneralPurposePerformedProcedureStepSOPClass);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.GeneralPurposePerformedProcedureStepSOPClass,
                enable ? getTransferSyntaxUIDs() : null);
    }

}
