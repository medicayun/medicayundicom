/*$Id: HL7SRExport.java 3569 2002-09-09 14:24:15Z gunterze $*/
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

package org.dcm4che.srom;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface HL7SRExport {
    
    /** Getter for property sendingApplication.
     * @return Value of property sendingApplication.
     */
    String getSendingApplication();
    
    /** Setter for property sendingApplication.
     * @param sendingApplication New value of property sendingApplication.
     */
    void setSendingApplication(String sendingApplication);
    
    /** Getter for property sendingFacility.
     * @return Value of property sendingFacility.
     */
    String getSendingFacility();
    
    /** Setter for property sendingFacility.
     * @param sendingFacility New value of property sendingFacility.
     */
    void setSendingFacility(String sendingFacility);
    
    /** Getter for property receivingApplication.
     * @return Value of property receivingApplication.
     */
    String getReceivingApplication();
    
    /** Setter for property receivingApplication.
     * @param receivingApplication New value of property receivingApplication.
     */
    void setReceivingApplication(String receivingApplication);
    
    /** Getter for property receivingFacility.
     * @return Value of property receivingFacility.
     */
    String getReceivingFacility();
    
    /** Setter for property receivingFacility.
     * @param receivingFacility New value of property receivingFacility.
     */
    void setReceivingFacility(String receivingFacility);
    
    String nextMessageControlID();
    
    byte[] toHL7(SRDocument doc, String messageControlID,
            String issuerOfPatientID, String patientAccountNumber,
            String placerOrderNumber, String fillerOrderNumber,
            String universalServiceID);
}
