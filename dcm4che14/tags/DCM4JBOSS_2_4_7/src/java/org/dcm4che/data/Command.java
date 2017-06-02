/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4che.data;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/** Defines behavior of <code>CommandSet</code> container objects.
 *
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3638 $ $Date: 2003-04-28 17:18:45 +0800 (周一, 28 4月 2003) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020726 gunter:</b>
 * <ul>
 * <li> Fix C_CANCEL_RQ value 
 * </ul>
 */
public interface Command extends DcmObject {
    
    // Constants -----------------------------------------------------
    public static final int C_STORE_RQ         = 0x0001;
    public static final int C_STORE_RSP        = 0x8001;
    public static final int C_GET_RQ           = 0x0010;
    public static final int C_GET_RSP          = 0x8010;
    public static final int C_FIND_RQ          = 0x0020;
    public static final int C_FIND_RSP         = 0x8020;
    public static final int C_MOVE_RQ          = 0x0021;
    public static final int C_MOVE_RSP         = 0x8021;
    public static final int C_ECHO_RQ          = 0x0030;
    public static final int C_ECHO_RSP         = 0x8030;
    public static final int N_EVENT_REPORT_RQ  = 0x0100;
    public static final int N_EVENT_REPORT_RSP = 0x8100;
    public static final int N_GET_RQ           = 0x0110;
    public static final int N_GET_RSP          = 0x8110;
    public static final int N_SET_RQ           = 0x0120;
    public static final int N_SET_RSP          = 0x8120;
    public static final int N_ACTION_RQ        = 0x0130;
    public static final int N_ACTION_RSP       = 0x8130;
    public static final int N_CREATE_RQ        = 0x0140;
    public static final int N_CREATE_RSP       = 0x8140;
    public static final int N_DELETE_RQ        = 0x0150;
    public static final int N_DELETE_RSP       = 0x8150;
    public static final int C_CANCEL_RQ        = 0x0FFF;

    public static final int MEDIUM             = 0x0000;
    public static final int HIGH               = 0x0001;
    public static final int LOW                = 0x0002;

    public static final int NO_DATASET         = 0x0101;
    
    public int getMessageID();

    public int getMessageIDToBeingRespondedTo();

    public String getAffectedSOPClassUID();

    public String getAffectedSOPInstanceUID();

    public String getRequestedSOPClassUID();

    public String getRequestedSOPInstanceUID();

    public int getCommandField();

    public int getStatus();

    public boolean isPending();

    public boolean isRequest();

    public boolean isResponse();

    public boolean hasDataset();

    public Command initCStoreRQ(int msgID, String sopClassUID,
            String sopInstUID, int priority);

    public Command setMoveOriginator(String aet, int msgID);
    
    public Command initCStoreRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);

    public Command initCFindRQ(int msgID, String sopClassUID, int priority);
        
    public Command initCFindRSP(int msgID, String sopClassUID, int status);
    
    public Command initCCancelRQ(int msgID);

    public Command initCGetRQ(int msgID, String sopClassUID, int priority);

    public Command initCGetRSP(int msgID, String sopClassUID, int status);

    public Command initCMoveRQ(int msgID, String sopClassUID, int priority,
            String moveDest);

    public Command initCMoveRSP(int msgID, String sopClassUID, int status);

    public Command initCEchoRQ(int msgID, String sopClassUID);

    public Command initCEchoRQ(int msgID);
    
    public Command initCEchoRSP(int msgID, String sopClassUID, int status);
    
    public Command initCEchoRSP(int msgID);

    public Command initNEventReportRQ(int msgID, String sopClassUID,
            String sopInstanceUID, int eventTypeID);
    
    public Command initNEventReportRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public Command initNGetRQ(int msgID, String sopClassUID,
            String sopInstUID, int[] attrIDs);
    
    public Command initNGetRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public Command initNSetRQ(int msgID, String sopClassUID,
            String sopInstUID);
    
    public Command initNSetRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public Command initNActionRQ(int msgID, String sopClassUID,
            String sopInstUID, int actionTypeID);
    
    public Command initNActionRSP(int msgID, String sopClassUID,
            String sopInstUID, int status) ;

    public Command initNCreateRQ(int msgID, String sopClassUID,
            String sopInstanceUID);

    public Command initNCreateRSP(int msgID, String sopClassUID,
            String sopInstUID, int status) ;

    public Command initNDeleteRQ(int msgID, String sopClassUID,
            String sopInstUID);
    
    public Command initNDeleteRSP(int msgID, String sopClassUID,
            String sopInstUID, int status);
    
    public void write(DcmHandler handler) throws IOException;

    public void write(OutputStream out) throws IOException;

    public void read(InputStream in) throws IOException;
    
    public String cmdFieldAsString();
}

