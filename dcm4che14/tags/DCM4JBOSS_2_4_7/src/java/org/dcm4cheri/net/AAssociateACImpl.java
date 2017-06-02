/*$Id: AAssociateACImpl.java 3516 2002-07-28 10:47:44Z gunterze $*/
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

import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.PDUException;

import java.util.Iterator;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3516 $ $Date: 2002-07-28 18:47:44 +0800 (周日, 28 7月 2002) $
 * @since May, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020728 gunter:</b>
 * <ul>
 * <li> add {@link #countAcceptedPresContext}
 * </ul>
 */
final class AAssociateACImpl extends AAssociateRQACImpl
        implements AAssociateAC {

    static AAssociateACImpl parse(UnparsedPDUImpl raw) throws PDUException {
        return (AAssociateACImpl)new AAssociateACImpl().init(raw);
    }

    AAssociateACImpl() {
    }
    
    public int countAcceptedPresContext() {
       int accepted = 0;
       for (Iterator it = presCtxs.values().iterator(); it.hasNext();) {
          if(((PresContext)it.next()).result() == 0)
             ++accepted;
       }
       return accepted;
    }
        

    protected int type() {
        return 2;
    }
    
    protected int pctype() {
        return 0x21;
    }

    protected String typeAsString() {
       return "AAssociateAC";
    }

    protected void append(PresContext pc, StringBuffer sb) {
       sb.append("\n\tpc-").append(pc.pcid())
         .append(":\t").append(pc.resultAsString())
         .append("\n\t\tts=").append(DICT.lookup(pc.getTransferSyntaxUID()));       
    }

    protected void appendPresCtxSummary(StringBuffer sb) {
       int accepted = countAcceptedPresContext();       
       sb.append("\n\tpresCtx:\taccepted=").append(accepted)
         .append(", rejected=").append(presCtxs.size() - accepted);
    }
}
