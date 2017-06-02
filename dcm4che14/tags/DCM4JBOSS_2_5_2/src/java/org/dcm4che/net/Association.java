/*  $Id: Association.java 3739 2003-12-05 14:09:34Z gunterze $
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4che.net;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3739 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020728 gunter:</b>
 * <ul>
 * <li> add {@link #listAcceptedPresContext(String)}
 * <li> add {@link #countAcceptedPresContext()}
 * </ul>
 * <p><b>20020802 gunter:</b>
 * <ul>
 * <li> add {@link #getProperty}
 * <li> add {@link #putProperty}
 * </ul>
 * <p><b>20020810 gunter:</b>
 * <ul>
 * <li> add properties rqTimeout, acTimeout, dimseTimeout
 * <li> rename property TCPCloseTimeout to soCloseDelay
 * <li> remove timeout param from connect(), accept(), read()
 * </ul>
 */
public interface Association {
    
    public static int IDLE = 1;
    
    public static int AWAITING_READ_ASS_RQ = 2;
    public static int AWAITING_WRITE_ASS_RP = 3;
    public static int AWAITING_WRITE_ASS_RQ = 4;
    public static int AWAITING_READ_ASS_RP = 5;
    
    public static int ASSOCIATION_ESTABLISHED = 6;
    
    public static int AWAITING_READ_REL_RP = 7;
    public static int AWAITING_WRITE_REL_RP = 8;
    public static int RCRS_AWAITING_WRITE_REL_RP = 9;
    public static int RCAS_AWAITING_READ_REL_RP = 10;
    public static int RCRS_AWAITING_READ_REL_RP = 11;
    public static int RCAS_AWAITING_WRITE_REL_RP = 12;
    
    public static int ASSOCIATION_TERMINATING = 13;
    
    int getState();
    
    String getStateAsString();
    
    void addAssociationListener(AssociationListener l);
    
    void removeAssociationListener(AssociationListener l);
    
    int nextMsgID();
    
    PDU connect(AAssociateRQ rq) throws IOException;
    
    PDU accept(AcceptorPolicy policy) throws IOException;
    
    Dimse read() throws IOException;
    
    void write(Dimse dimse) throws IOException;
    
    PDU release(int timeout) throws IOException;
    
    void abort(AAbort aa) throws IOException;
    
    int getMaxOpsInvoked();
    
    int getMaxOpsPerformed();
    
    String getAcceptedTransferSyntaxUID(int pcid);
    
    PresContext getAcceptedPresContext(String asuid, String tsuid);
    
    PresContext getProposedPresContext(int pcid);
    
    List listAcceptedPresContext(String asuid);
    
    int countAcceptedPresContext();
    
    AAssociateRQ getAAssociateRQ();
    
    AAssociateAC getAAssociateAC();
    
    AAssociateRJ getAAssociateRJ();
    
    AAbort getAAbort();
    
    Socket getSocket();
    
    String getCallingAET();

    String getCalledAET();
    
    Object getProperty(Object key);
    
    void putProperty(Object key, Object value);
    
    /** Getter for property rqTimeout.
     * @return Value of property rqTimeout.
     */
    int getRqTimeout();
    
    /** Setter for property rqTimeout.
     * @param rqTimeout New value of property rqTimeout.
     */
    void setRqTimeout(int rqTimeout);
    
    /** Getter for property dimseTimeout.
     * @return Value of property dimseTimeout.
     */
    int getDimseTimeout();
    
    /** Setter for property dimseTimeout.
     * @param dimseTimeout New value of property dimseTimeout.
     */
    void setDimseTimeout(int dimseTimeout);
    
    /** Getter for property soCloseDelay.
     * @return Value of property soCloseDelay.
     */
    int getSoCloseDelay();
    
    /** Setter for property soCloseDelay.
     * @param soCloseDelay New value of property soCloseDelay.
     */
    void setSoCloseDelay(int soCloseDelay);
    
    /** Getter for property acTimeout.
     * @return Value of property acTimeout.
     */
    int getAcTimeout();
    
    /** Setter for property acTimeout.
     * @param acTimeout New value of property acTimeout.
     */
    void setAcTimeout(int acTimeout);
    
    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    boolean isPackPDVs();
    
    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    void setPackPDVs(boolean packPDVs);
    
}