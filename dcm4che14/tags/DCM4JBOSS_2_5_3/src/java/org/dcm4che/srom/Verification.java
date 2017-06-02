/*$Id: Verification.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2001 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

import org.dcm4che.data.Dataset;

import java.util.Date;

/**
 * The <code>Verification</code> interface represents an
 * item of the <i>Verifying Observer Sequence</i> <code>(0040,A073)</code>.
 * <br>
 * The person or persons authorized to verify documents of this 
 * type and accept responsibility for the content of this document. 
 * One or more Items may be included in this sequence. Required if 
 * <i>{@link SRDocument#isVerified Verification Flag}</i> 
 * <code>(0040,A493)</code> is <code>VERIFIED</code>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.2 SR DOCUMENT GENERAL MODULE"
 */
public interface Verification extends Comparable {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Returns the verifying observer name.
     * <br>DICOM Tag: <code>(0040,A075)</code>
     * <br>Tag Name: <code>Verifying Observer Name</code>
     * <br>
     * The person authorized by the <i>Verifying Organization</i> 
     * <code>(0040,A027)</code>
     * to verify documents of this type and who accepts responsibility 
     * for the content of this document. 
     *
     * @return  the verifying observer name.
     */
    public String getVerifyingObserverName();
    
    /**
     * Returns the single item of the 
     * <i>Verifying Observer Identification Code Sequence</i>.
     * <br>
     * DICOM Tag: <code>(0040,A088)</code><br>
     * Tag Name: <code>Verifying Observer Identification Code Sequence</code>
     * <br>
     * Coded identifier of the Verifying Observer. Zero or one Items 
     * shall be permitted in this sequence. 
     *
     * @return  the single item of the 
     * <i>Verifying Observer Identification Code Sequence</i>.
     */
    public Code getVerifyingObserverCode();
    
    /**
     * Returns the verifying organization.
     * <br>DICOM Tag: <code>(0040,A027)</code>
     * <br>Tag Name: <code>Verifying Organization</code>
     * <br>
     * Organization to which the <i>Verifying Observer Name</i>
     * <code>(0040,A075)</code> is accountable for this document in the 
     * current interpretation procedure. 
     *
     * @return  the verifying organization.
     */
    public String getVerifyingOrganization();
    
    /**
     * Returns the verification date time.
     * <br>DICOM Tag: <code>(0040,A030)</code>
     * <br>Tag Name: <code>Verification DateTime</code>
     * <br>
     * Date and Time of verification by the 
     * <i>Verifying Observer Name</i> <code>(0040,A075)</code>.
     *
     * @return  the verification date time.
     */
    public Date getVerificationDateTime();   

    public void toDataset(Dataset ds);

}//end interface Verification
