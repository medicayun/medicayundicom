/*$Id: AAssociateRQImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.PDUException;

import java.util.Iterator;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class AAssociateRQImpl extends AAssociateRQACImpl
        implements AAssociateRQ {

    static AAssociateRQImpl parse(UnparsedPDUImpl raw) throws PDUException {
        return (AAssociateRQImpl)new AAssociateRQImpl().init(raw);
    }

    AAssociateRQImpl() {
    }
    
    protected int type() {
        return 1;
    }
    
    protected int pctype() {
        return 0x20;
    }
    
    protected String typeAsString() {
       return "AAssociateRQ";
    }

    protected void append(PresContext pc, StringBuffer sb) {
       sb.append("\n\tpc-").append(pc.pcid())
         .append(":\tas=").append(DICT.lookup(pc.getAbstractSyntaxUID()));
       for (Iterator it = pc.getTransferSyntaxUIDs().iterator(); it.hasNext();)
         sb.append("\n\t\tts=").append(DICT.lookup((String)it.next()));       
    }
    
    protected void appendPresCtxSummary(StringBuffer sb) {
       sb.append("\n\tpresCtx:\toffered=").append(presCtxs.size());
    }
}
