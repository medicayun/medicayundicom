/*$Id: Request.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

/**
 * The <code>Request</code> interface represents an item of
 * the <i>Referenced Request Sequence</i> (DICOM Tag: <code>(0040,A370)</code>)
 * in <i>DICOM SR Document General Module</i>.
 *
 * <p>
 *  The <i>Referenced Request Sequence</i> itself is defined as:<br> 
 *  <pre>
 *      Identifies Requested Procedures which are being 
 *      fulfilled (completely or partially) by creation 
 *      of this Document. One or more Items may be 
 *      included in this sequence. Required if this 
 *      Document fulfills at least one Requested Procedure.
 *  </pre>
 * </p>
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.2 SR DOCUMENT GENERAL MODULE"
 */
public interface Request {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM <i>Study Instance UID</i>.
     * <br>DICOM Tag: <code>(0020,000D)</code>
     * <br>
     * Unique identifier for the Study.
     *
     * @return  the Study Instance UID.
     */
    public String getStudyInstanceUID();
    
    /**
     * Returns the DICOM <i>Accession Number</i>.
     * <br>DICOM Tag: <code>(0008,0050)</code>
     * <br>
     * A departmental IS generated number which identifies 
     * the order for the Study.
     *
     * @return  the Accession Number.
     */
    public String getAccessionNumber();
    
    /**
     * Returns the DICOM <i>Placer Order Number</i>.
     * <br>DICOM Tag: <code>(0040,2016)</code>
     * <br>
     * The order number assigned to the Imaging Service Request 
     * by the party placing the order.
     *
     * @return  the Placer Order Number.
     */
    public String getPlacerOrderNumber();
    
    /**
     * Returns the DICOM <i>Filler Order Number</i>.
     * <br>DICOM Tag: <code>(0040,2017)</code>
     * <br>
     * The order number assigned to the Imaging Service Request 
     * by the party filling the order.
     *
     * @return  the Filler Order Number.
     */
    public String getFillerOrderNumber();
    
    /**
     * Returns the DICOM <i>Requested Procedure ID</i>.
     * <br>DICOM Tag: <code>(0040,1001)</code>
     * <br>
     * Identifier of the related Requested Procedure.
     *
     * @return  the Requested Procedure ID.
     */
    public String getProcedureID();
    
    /**
     * Returns the DICOM <i>Requested Procedure Description</i>.
     * <br>DICOM Tag: <code>(0032,1060)</code>
     * <br>
     * Institution-generated administrative description or 
     * classification of Requested Procedure.
     *
     * @return  the Requested Procedure Description.
     */
    public String getProcedureDescription();
    
    /**
     * Returns the single item of a 
     * DICOM <i>Requested Procedure Code Sequence</i>.
     * <br>DICOM Tag: <code>(0032,1064)</code>
     * <br>
     * A sequence that conveys the requested procedure. 
     * Zero or one Item may be included in this sequence.
     *
     * @return  the Requested Procedure Code or <code>null</code>
     *          if the <i>Requested Procedure Code Sequence</i>
     *          had no entry.
     */
    public Code getProcedureCode();
    
    /**
     * Compares two <code>Request</code> objects for equality.
     * <br>
     * <b>Note:</b> Only the <i>study instance UID</i> of the
     *             <code>Request</code> objects will the compared.
     *
     * @param obj  the <code>Request</code> object to be compared
     *             with this instance.
     * @return <code>true</code> if this instance and <code>obj</code>
     *         are equal <code>false</code> otherwise.
     */
    public boolean equals(Object obj);
    
    public void toDataset(Dataset ds);
    
}//end interface Request
