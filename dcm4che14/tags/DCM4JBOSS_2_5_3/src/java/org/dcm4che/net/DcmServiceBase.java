/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.net;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.UIDGenerator;
import org.dcm4che.dict.Tags;

import java.io.IOException;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3531 $ $Date: 2002-08-03 16:37:43 +0800 (周六, 03 8月 2002) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020715 gunter:</b>
 * <ul>
 * <li> add MultiDimseRsp#release and invoke it from #doMultiRsp to
 *      enable release of resources used by MultiDimseRsp
 * </ul>
 */
public class DcmServiceBase implements DcmService {
    // Constants -----------------------------------------------------
    public static final int SUCCESS               = 0x0000;
    public static final int PENDING               = 0xFF00;
    public static final int NO_SUCH_SOP_CLASS     = 0x0118;
    public static final int UNRECOGNIZE_OPERATION = 0x0211;
    
    public static final DcmService VERIFICATION_SCP = new DcmServiceBase() {
        protected void doCEcho(ActiveAssociation assoc, Dimse rq,
            Command rspCmd)
        throws IOException {
            rspCmd.putUS(Tags.Status, SUCCESS);
        }
    };
    public static final DcmService NO_SUCH_SOP_CLASS_SCP =
        new DcmServiceBase(new DcmServiceException(NO_SUCH_SOP_CLASS));
    
    // Attributes ----------------------------------------------------
    protected static final DcmObjectFactory objFact =
        DcmObjectFactory.getInstance();
    
    protected static final AssociationFactory fact =
        AssociationFactory.getInstance();
    
    protected static final UIDGenerator uidGen = UIDGenerator.getInstance();
    
    protected final DcmServiceException defEx;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public DcmServiceBase(DcmServiceException defEx) {
        this.defEx = defEx;
    }
    
    public DcmServiceBase() {        
        this.defEx = new DcmServiceException(UNRECOGNIZE_OPERATION);
    }
    
    // Public --------------------------------------------------------
    
    // DcmService implementation -------------------------------------
    public void c_store(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCStoreRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            rqCmd.getAffectedSOPInstanceUID(),
            SUCCESS);
        try {
            doCStore(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void c_get(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCGetRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            SUCCESS);
        try {
            doMultiRsp(assoc, rq, rspCmd, doCGet(assoc, rq, rspCmd));
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
            Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
            assoc.getAssociation().write(rsp);
            doAfterRsp(assoc, rsp);
        }
    }
    
    public void c_find(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCFindRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            PENDING);
        try {
            doMultiRsp(assoc, rq, rspCmd, doCFind(assoc, rq, rspCmd));
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
            Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
            assoc.getAssociation().write(rsp);
            doAfterRsp(assoc, rsp);
        }
    }
    
    public void c_move(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCMoveRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            PENDING);
        try {
            doMultiRsp(assoc, rq, rspCmd, doCMove(assoc, rq, rspCmd));
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
            Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
            assoc.getAssociation().write(rsp);
            doAfterRsp(assoc, rsp);
        }
    }
    
    public void c_echo(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCEchoRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            SUCCESS);
        try {
            doCEcho(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void n_event_report(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initNEventReportRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            rqCmd.getAffectedSOPInstanceUID(),
            SUCCESS);
        Dataset rspData = null;
        try {
            rspData = doNEventReport(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void n_get(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initNGetRSP(
            rqCmd.getMessageID(),
            rqCmd.getRequestedSOPClassUID(),
            rqCmd.getRequestedSOPInstanceUID(),
            SUCCESS);
        Dataset rspData = null;
        try {
            rspData = doNGet(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void n_set(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initNSetRSP(
            rqCmd.getMessageID(),
            rqCmd.getRequestedSOPClassUID(),
            rqCmd.getRequestedSOPInstanceUID(),
            SUCCESS);
        Dataset rspData = null;
        try {
            rspData = doNSet(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void n_action(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initNActionRSP(
            rqCmd.getMessageID(),
            rqCmd.getRequestedSOPClassUID(),
            rqCmd.getRequestedSOPInstanceUID(),
            SUCCESS);
        Dataset rspData = null;
        try {
            rspData = doNAction(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void n_create(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initNCreateRSP(
            rqCmd.getMessageID(),
            rqCmd.getAffectedSOPClassUID(),
            createUID(rqCmd.getAffectedSOPInstanceUID()),
            SUCCESS);
        Dataset rspData = null;
        try {
            rspData = doNCreate(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    public void n_delete(ActiveAssociation assoc, Dimse rq)
    throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initNDeleteRSP(
            rqCmd.getMessageID(),
            rqCmd.getRequestedSOPClassUID(),
            rqCmd.getRequestedSOPInstanceUID(),
            SUCCESS);
        Dataset rspData = null;
        try {
            rspData = doNDelete(assoc, rq, rspCmd);
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
        }
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    protected void doAfterRsp(ActiveAssociation assoc, Dimse rsp) {
    }
    
    protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected MultiDimseRsp doCGet(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected MultiDimseRsp doCMove(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected void doCEcho(ActiveAssociation assoc, Dimse rq, Command rspCmd)
    throws IOException, DcmServiceException {
        //      rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected Dataset doNEventReport(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected Dataset doNGet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    protected Dataset doNDelete(ActiveAssociation assoc, Dimse rq,
        Command rspCmd)
    throws IOException, DcmServiceException {
        rq.getDataset(); // read out dataset
        throw defEx;
    }
    
    // Private -------------------------------------------------------
    private void doMultiRsp(ActiveAssociation assoc, Dimse rq, Command rspCmd,
        MultiDimseRsp mdr)
    throws IOException, DcmServiceException {
        try {
            assoc.addCancelListener(rspCmd.getMessageIDToBeingRespondedTo(),
                mdr.getCancelListener());
            do {
                Dataset rspData = mdr.next(assoc, rq, rspCmd);
                Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
                assoc.getAssociation().write(rsp);
                doAfterRsp(assoc, rsp);
            } while (rspCmd.isPending());
        } finally {
            mdr.release();
        }
    }
    
    private static String createUID(String uid) {
        return uid != null ? uid : uidGen.createUID();
    }
    
    // Inner classes -------------------------------------------------
    public static interface MultiDimseRsp {
        DimseListener getCancelListener();
        
        Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)
        throws DcmServiceException;
        
        void release();
    }
}
