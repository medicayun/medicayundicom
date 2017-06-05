/*
 * $Id: MoveScuService.java 964 2004-02-11 20:15:27Z gunterze $ Copyright
 * (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.service;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderQueue;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderQueueHome;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderValue;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.service.util.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 964 $ $Date: 2004-02-12 04:15:27 +0800 (周四, 12 2月 2004) $
 * @since 17.12.2003
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class MoveScuService
    extends ServiceMBeanSupport
    implements org.dcm4chex.service.MoveScuServiceMBean {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private static final AssociationFactory af =
        AssociationFactory.getInstance();
    private static final String[] NATIVE_TS =
        { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    private static final int PCID = 1;
    private static final String DEFAULT_AET = "MOVE_SCU";
    private static final int INVOKE_FAILED_STATUS = -1;

    private DataSourceFactory dsf = new DataSourceFactory(log);
    private RetryIntervalls retryIntervalls = new RetryIntervalls();
    private MoveOrderQueue queue;
    private int invoked = 0;
    private int maxConcurrentMoves = 1;

    /**
     * @jmx.managed-attribute
     */
    public String getDataSource() {
        return dsf.getJNDIName();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDataSource(String name) {
        dsf.setJNDIName(name);
    }

    /**
     * @jmx.managed-attribute
     */
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    /**
     * @jmx.managed-attribute
     */
    public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getMaxConcurrentMoves() {
        return maxConcurrentMoves;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setMaxConcurrentMoves(int maxConcurrentMoves) {
        this.maxConcurrentMoves = maxConcurrentMoves;
    }

    private String getAET() {
        String aet = getServiceName().getKeyProperty("aet");
        return aet != null ? aet : DEFAULT_AET;
    }

    /**
     * @jmx.managed-operation
     */
    public void run() {
        MoveOrderValue order;
        while (invoked < maxConcurrentMoves
            && (order = fetchNextOrder()) != null) {
            try {
                process(order);
            } catch (Exception e) {
                log.error("Failed to invoke " + order, e);
                queueFailedMoveOrder(order, INVOKE_FAILED_STATUS);
            }
        }
    }

    private void process(final MoveOrderValue order)
        throws
            SQLException,
            ConfigurationException,
            InterruptedException,
            IOException {
        ActiveAssociation moveAssoc =
            openAssociation(queryAEData(order.getRetrieveAET()));
        ++invoked;
        try {
            Command cmd = dof.newCommand();
            cmd.initCMoveRQ(
                moveAssoc.getAssociation().nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                order.getDICOMPriority(),
                order.getMoveDestination());
            Dataset ds = dof.newDataset();
            ds.putCS(Tags.QueryRetrieveLevel, order.getQueryRetrieveLevel());
            putUI(ds, Tags.StudyInstanceUID, order.getStudyIuids());
            putUI(ds, Tags.StudyInstanceUID, order.getStudyIuids());
            putUI(ds, Tags.SeriesInstanceUID, order.getSeriesIuids());
            putUI(ds, Tags.SOPInstanceUID, order.getSopIuids());
            moveAssoc.invoke(af.newDimse(PCID, cmd, ds), new DimseListener() {

                public void dimseReceived(Association assoc, Dimse dimse) {
                    Command cmd = dimse.getCommand();
                    final int status = cmd.getStatus();
                    if (status == Status.Pending || status == Status.Success) {
                        return;
                    }
                    Dataset ds = null;
                    try {
                        ds = dimse.getDataset();
                    } catch (IOException e) {
                        log.warn("Failed to read Move Response Identifier:", e);
                    }
                    String[] failedUIDs =
                        ds != null
                            ? ds.getStrings(Tags.FailedSOPInstanceUIDList)
                            : null;
                    if (failedUIDs != null) {
                        order.setSopIuids(
                            StringUtils.toString(failedUIDs, '\\'));
                    }
                    queueFailedMoveOrder(order, status);
                }
            });
        } finally {
            try {
                moveAssoc.release(true);
            } catch (Exception e) {
                log.warn("Failed to release " + moveAssoc.getAssociation());
            }
            --invoked;
        }
    }

    private static void putUI(Dataset ds, int tag, String uids) {
        if (uids != null && uids.length() != 0) {
            ds.putUI(tag, StringUtils.split(uids, '\\'));
        }
    }

    private synchronized MoveOrderValue fetchNextOrder() {
        try {
            if (queue == null) {
                MoveOrderQueueHome home =
                    (MoveOrderQueueHome) EJBHomeFactory.getFactory().lookup(
                        MoveOrderQueueHome.class,
                        MoveOrderQueueHome.JNDI_NAME);
                queue = home.create();
            }
            return queue.dequeue();
        } catch (Throwable e) {
            log.error("Failed to fetch Move Order from Queue", e);
            return null;
        }
    }

    private synchronized void queueFailedMoveOrder(
        MoveOrderValue order,
        int failureStatus) {
        order.addFailure(failureStatus);
        order.setScheduledTime(
            retryIntervalls.scheduleNextRetry(order.getFailureCount()));
        try {
            queue.queue(order);
        } catch (Throwable e) {
            log.error("Failed to (re-)queue " + order, e);
        }
    }

    private AEData queryAEData(String aet)
        throws SQLException, ConfigurationException {
        AECmd aeCmd = new AECmd(dsf.getDataSource(), aet);
        AEData aeData = aeCmd.execute();
        if (aeData == null) {
            throw new ConfigurationException("Unkown Retrieve AET:" + aet);
        }
        return aeData;
    }

    private ActiveAssociation openAssociation(AEData moveSCP)
        throws IOException {
        Association a = af.newRequestor(createSocket(moveSCP));
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(moveSCP.getTitle());
        rq.setCallingAET(getAET());
        rq.addPresContext(
            af.newPresContext(
                PCID,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                NATIVE_TS));
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) {
            throw new IOException("Association not accepted by " + moveSCP);
        }
        ActiveAssociation aa = af.newActiveAssociation(a, null);
        aa.start();
        if (a.getAcceptedTransferSyntaxUID(PCID) == null) {
            try {
                aa.release(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new IOException(
                "Presentation Context for Retrieve rejected by " + moveSCP);
        }
        return aa;
    }

    private Socket createSocket(AEData moveSCP) throws IOException {
        return new Socket(moveSCP.getHostName(), moveSCP.getPort());
    }
}
