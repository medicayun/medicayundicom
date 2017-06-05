/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.gpwlscp;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-04-07 17:49:53 +0800 (周四, 07 4月 2005) $
 * @since 03.04.2005
 */

public class GPWLScpService extends AbstractScpService {

    private GPWLFindScp gpwlFindScp = new GPWLFindScp(this);
    private GPSPSScp spspsScp = new GPSPSScp(this);

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    protected void startService() throws Exception {
        super.startService();
    }

    protected void stopService() throws Exception {
        super.stopService();
    }
    
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.GeneralPurposeWorklistInformationModelFIND, gpwlFindScp);
        services.bind(UIDs.GeneralPurposeScheduledProcedureStepSOPClass, spspsScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.GeneralPurposeWorklistInformationModelFIND);
        services.unbind(UIDs.GeneralPurposeScheduledProcedureStepSOPClass);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.GeneralPurposeWorklistInformationModelFIND,
                enable ? getTransferSyntaxUIDs() : null);
        policy.putPresContext(UIDs.GeneralPurposeScheduledProcedureStepSOPClass,
                enable ? getTransferSyntaxUIDs() : null);
    }
}
