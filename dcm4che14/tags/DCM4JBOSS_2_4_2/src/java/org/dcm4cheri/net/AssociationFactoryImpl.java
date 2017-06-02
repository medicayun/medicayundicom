/*$Id: AssociationFactoryImpl.java 3660 2003-06-22 22:33:03Z gunterze $*/
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

package org.dcm4cheri.net;

import org.dcm4che.net.AAbort;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.Association;
import org.dcm4che.net.AReleaseRQ;
import org.dcm4che.net.AReleaseRP;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.RoleSelection;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;

import org.dcm4cheri.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class AssociationFactoryImpl extends AssociationFactory {
    
    public AssociationFactoryImpl() {
    }

    public AAssociateRQ newAAssociateRQ() {
        return new AAssociateRQImpl();
    }
    
    public AAssociateAC newAAssociateAC() {
        return new AAssociateACImpl();
    }
    
    public AAssociateRJ newAAssociateRJ(int result, int source, int reason) {
        return new AAssociateRJImpl(result, source, reason);
    }
    
    public PDataTF newPDataTF(int maxLength) {
        return new PDataTFImpl(maxLength);
    }
        
    public AReleaseRQ newAReleaseRQ() {
        return AReleaseRQImpl.getInstance();
    }
    
    public AReleaseRP newAReleaseRP() {
        return AReleaseRPImpl.getInstance();
    }

    public AAbort newAAbort(int source, int reason) {
        return new AAbortImpl(source, reason);
    }
    
    public PresContext newPresContext(int pcid, String asuid, String[] tsuids) {
        return new PresContextImpl(0x020, pcid, 0,
            StringUtils.checkUID(asuid),
            StringUtils.checkUIDs(tsuids));
    }
    
	public PresContext newPresContext(int pcid, String asuid) {
		return new PresContextImpl(0x020, pcid, 0,
			StringUtils.checkUID(asuid),
			new String[]{ UIDs.ImplicitVRLittleEndian });
	}
	
    public PresContext newPresContext(int pcid, int result, String tsuid) {
        return new PresContextImpl(0x021, pcid, result, null,
                new String[]{ StringUtils.checkUID(tsuid) } );
    }
    
    public AsyncOpsWindow newAsyncOpsWindow(
            int maxOpsInvoked, int maxOpsPerfomed) {
        return new AsyncOpsWindowImpl(maxOpsInvoked, maxOpsPerfomed);
    }
    
    public RoleSelection newRoleSelection(String uid, boolean scu, boolean scp)
    {
        return new RoleSelectionImpl(uid, scu, scp);
    }
    
    public ExtNegotiation newExtNegotiation(String uid, byte[] info) {
        return new ExtNegotiationImpl(uid, info);
    }
    
    public PDU readFrom(InputStream in, byte[] buf)
            throws IOException {
        UnparsedPDUImpl raw = new UnparsedPDUImpl(in, buf);
        switch (raw.type()) {
            case 1:
                return AAssociateRQImpl.parse(raw);
            case 2:
                return AAssociateACImpl.parse(raw);
            case 3:
                return AAssociateRJImpl.parse(raw);
            case 4:
                return PDataTFImpl.parse(raw);
            case 5:
                return AReleaseRQImpl.parse(raw);
            case 6:
                return AReleaseRPImpl.parse(raw);
            case 7:
                return AAbortImpl.parse(raw);
            default:
                throw new PDUException("Unrecognized " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.UNRECOGNIZED_PDU));
        }
    }
    
    public Association newRequestor(Socket s) throws IOException {
        return new AssociationImpl(s, true);
    }
    
    public Association newAcceptor(Socket s) throws IOException {
        return new AssociationImpl(s, false);
    }

    public ActiveAssociation newActiveAssociation(Association assoc,
            DcmServiceRegistry services) {
         return new ActiveAssociationImpl(assoc, services);
    }

    public Dimse newDimse(int pcid, Command cmd) {
        return new DimseImpl(pcid, cmd, null, null);
    }
    
    public Dimse newDimse(int pcid, Command cmd, Dataset ds) {
        return new DimseImpl(pcid, cmd, ds, null);
    }
    
    public Dimse newDimse(int pcid, Command cmd, DataSource src) {
        return new DimseImpl(pcid, cmd, null, src);
    }
    
    public AcceptorPolicy newAcceptorPolicy() {
       return new AcceptorPolicyImpl();
    }
   
    public DcmServiceRegistry newDcmServiceRegistry() {
       return new DcmServiceRegistryImpl();
    }

}
