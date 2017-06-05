/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import javax.management.ObjectName;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4chex.cdw.common.*;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4127 $ $Date: 2004-06-24 21:34:08 +0800 (周四, 24 6月 2004) $
 * @since 22.06.2004
 *
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {
        
    protected static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    
    private static final String GET_DCM_HANDLER = "getDcmHandler";

    protected static final String[] ONLY_DEFAULT_TS = { UIDs.ImplicitVRLittleEndian,};

    protected static final String[] NATIVE_LE_TS = {
            UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian,};

    protected static final AssociationFactory asf = AssociationFactory
            .getInstance();

    protected boolean acceptExplicitVRLE = true;

    protected ObjectName dcmServerName;

    protected SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    protected DcmHandler dcmHandler;

    public final ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public final void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    final SpoolDirDelegate getSpoolDir() {
        return spoolDir;
    }

    public final boolean isAcceptExplicitVRLE() {
        return acceptExplicitVRLE;
    }

    public final void setAcceptExplicitVRLE(boolean acceptExplicitVRLE) {
        this.acceptExplicitVRLE = acceptExplicitVRLE;
        updatePresContextsIfRunning();
    }

    protected String[] getTransferSyntaxes() {
        return acceptExplicitVRLE ? NATIVE_LE_TS : ONLY_DEFAULT_TS;
    }

    protected void startService() throws Exception {
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, GET_DCM_HANDLER,
                null, null);
        bindDcmServices();
        updatePresContexts();
    }

    protected void stopService() throws Exception {
        removePresContexts();
        unbindDcmServices();
        dcmHandler = null;
    }

    protected abstract void bindDcmServices();

    protected abstract void unbindDcmServices();

    protected abstract void updatePresContexts();

    protected abstract void removePresContexts();

    protected void bindDcmServices(String[] cuids, DcmService service) {
        DcmServiceRegistry reg = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++)
            reg.bind(cuids[i], service);
    }

    protected void unbindDcmServices(String[] cuids) {
        DcmServiceRegistry reg = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++)
            reg.unbind(cuids[i]);
    }

    protected void putPresContexts(String[] asuids, String[] tsuids) {
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < asuids.length; i++)
            policy.putPresContext(asuids[i], tsuids);
    }

    protected void updatePresContextsIfRunning() {
        if (getState() == STARTED)
            updatePresContexts();
    }
}
