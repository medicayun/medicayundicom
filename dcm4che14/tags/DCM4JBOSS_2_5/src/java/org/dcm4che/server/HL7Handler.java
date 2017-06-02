/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.server;

import org.dcm4che.hl7.HL7Service;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3565 $ $Date: 2002-09-07 23:25:04 +0800 (周六, 07 9月 2002) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public interface HL7Handler extends Server.Handler {

    /** Getter for property soTimeout.
     * @return Value of property rqTimeout.
     */
    int getSoTimeout();
    
    /** Setter for property soTimeout.
     * @param rqTimeout New value of property rqTimeout.
     */
    void setSoTimeout(int timeout);
    
    /** Getter for property sendingApps.
     * @return Value of property sendingApps.
     */
    String[] getSendingApps();
    
    /** Setter for property sendingApps.
     * @param sendingApps New value of property sendingApps.
     */
    void setSendingApps(String[] sendingApps);
    
    boolean addSendingApp(String sendingApp);
    
    boolean removeSendingApp(String sendingApp);
    
    /** Getter for property receivingApps.
     * @return Value of property receivingApps.
     */
    String[] getReceivingApps();
    
    /** Setter for property receivingApps.
     * @param receivingApps New value of property receivingApps.
     */
    void setReceivingApps(String[] receivingApps);
    
    boolean addReceivingApp(String receivingApp);
    
    boolean removeReceivingApp(String receivingApp);
    
    HL7Service putService(String msgTypeEvent, HL7Service service);
    
}
