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
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.dcm.mppsscu;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2712 $ $Date: 2006-08-28 08:14:15 +0800 (周一, 28 8月 2006) $
 * @since 15.11.2004
 * 
 */
public class MPPSScuService extends ServiceMBeanSupport implements
        MessageListener, NotificationListener {

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian };

    private static final int ERR_MPPS_RJ = -2;

    private static final int ERR_ASSOC_RJ = -1;

    private static final int PCID_MPPS = 1;

    private static final String NONE = "NONE";

    private static final String[] EMPTY = {};

    private static final int[] EXCLUDE_TAGS = { Tags.SOPClassUID,
            Tags.SOPInstanceUID };

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private String callingAET;

    private String[] forwardAETs = EMPTY;

    private ObjectName mppsScpServiceName;

    private String queueName;

    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;

    private int concurrency = 1;

    public final int getConcurrency() {
        return concurrency;
    }

    public final void setConcurrency(int concurrency) throws Exception {
        if (concurrency <= 0)
            throw new IllegalArgumentException("Concurrency: " + concurrency);
        if (this.concurrency != concurrency) {
            final boolean restart = getState() == STARTED;
            if (restart)
                stop();
            this.concurrency = concurrency;
            if (restart)
                start();
        }
    }

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final String getForwardAETs() {
        return forwardAETs.length > 0 ? StringUtils.toString(forwardAETs, '\\')
                : NONE;
    }

    public final void setForwardAETs(String forwardAETs) {
        this.forwardAETs = NONE.equalsIgnoreCase(forwardAETs) ? EMPTY
                : StringUtils.split(forwardAETs, '\\');
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final int getReceiveBufferSize() {
        return tlsConfig.getReceiveBufferSize();
    }

    public final void setReceiveBufferSize(int size) {
        tlsConfig.setReceiveBufferSize(size);
    }

    public final int getSendBufferSize() {
        return tlsConfig.getSendBufferSize();
    }

    public final void setSendBufferSize(int size) {
        tlsConfig.setSendBufferSize(size);
    }

    public final boolean isTcpNoDelay() {
        return tlsConfig.isTcpNoDelay();
    }

    public final void setTcpNoDelay(boolean on) {
        tlsConfig.setTcpNoDelay(on);
    }

    public final String getQueueName() {
        return queueName;
    }

    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }

    public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }

    protected void startService() throws Exception {
        JMSDelegate.startListening(queueName, this, concurrency);
        server.addNotificationListener(mppsScpServiceName, this,
                MPPSScpService.NOTIF_FILTER, null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(mppsScpServiceName, this,
                MPPSScpService.NOTIF_FILTER, null);
        JMSDelegate.stopListening(queueName);
    }

    public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
        for (int i = 0; i < forwardAETs.length; i++) {
            MPPSOrder order = new MPPSOrder(mpps, forwardAETs[i]);
            try {
                log.info("Scheduling " + order);
                JMSDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
                        0L);
            } catch (JMSException e) {
                log.error("Failed to schedule " + order, e);
            }
        }

    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            MPPSOrder order = (MPPSOrder) om.getObject();
            log.info("Start processing " + order);
            try {
                sendMPPS(order.isCreate(), order.getDataset(), 
                        order.getDestination());
                log.info("Finished processing " + order);
            } catch (Exception e) {
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order, e);
                } else {
                    log.warn("Failed to process " + order
                            + ". Scheduling retry.", e);
                    JMSDelegate.queue(queueName, order, 0, System
                            .currentTimeMillis()
                            + delay);
                }
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }

    public void sendMPPS(boolean create, Dataset mpps, String aet)
            throws DcmServiceException, InterruptedException, IOException,
            UnkownAETException, SQLException {
        AEData aeData = new AECmd(aet).getAEData();
        if (aeData == null) {
            throw new UnkownAETException("Unkown Destination AET: " + aet);
        }
        AssociationFactory af = AssociationFactory.getInstance();
        Association a = af.newRequestor(tlsConfig.createSocket(aeData));
        a.setAcTimeout(acTimeout);
        a.setDimseTimeout(dimseTimeout);
        a.setSoCloseDelay(soCloseDelay);
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(aet);
        rq.setCallingAET(callingAET);
        rq.addPresContext(af.newPresContext(PCID_MPPS,
                UIDs.ModalityPerformedProcedureStep, NATIVE_TS));
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) {
            throw new DcmServiceException(ERR_ASSOC_RJ,
                    "Association not accepted by " + aet + ": " + ac);
        }
        ActiveAssociation aa = af.newActiveAssociation(a, null);
        try {
            aa.start();
            if (a.getAcceptedTransferSyntaxUID(PCID_MPPS) == null) {
                throw new DcmServiceException(ERR_MPPS_RJ,
                        "MPPS not supported by remote AE: " + aet);
            }
            DcmObjectFactory dof = DcmObjectFactory.getInstance();
            Command cmdRq = dof.newCommand();
            if (create) {
                cmdRq.initNCreateRQ(a.nextMsgID(),
                        UIDs.ModalityPerformedProcedureStep, mpps
                                .getString(Tags.SOPInstanceUID));
            } else {
                cmdRq.initNSetRQ(a.nextMsgID(),
                        UIDs.ModalityPerformedProcedureStep, mpps
                                .getString(Tags.SOPInstanceUID));
            }
            Dimse dimseRq = af.newDimse(PCID_MPPS, cmdRq, mpps
                    .exclude(EXCLUDE_TAGS));            
            final Dimse dimseRsp = aa.invoke(dimseRq).get();
            final Command cmdRsp = dimseRsp.getCommand();
            final int status = cmdRsp.getStatus();
            switch (status) {
            case 0x0000:
                break;
            case 0x0116:
                log.warn("Received Warning Status 116H " +
                        "(=Attribute Value Out of Range) from remote AE "
                        + aet);
                break;
            default:
                log.error("Received Error Status "
                        + Integer.toHexString(status) + "H, Error Comment: "
                        + cmdRsp.getString(Tags.ErrorComment));
                throw new DcmServiceException(status, cmdRsp
                        .getString(Tags.ErrorComment));
            }
        } finally {
            try {
                aa.release(true);
            } catch (Exception e) {
                log.warn("Failed to release " + aa.getAssociation());
            }
        }
    }
}