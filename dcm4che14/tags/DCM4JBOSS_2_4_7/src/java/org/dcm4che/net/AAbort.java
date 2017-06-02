/*$Id: AAbort.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface AAbort extends PDU {
    
    public static final int SERVICE_USER = 0;
    public static final int SERVICE_PROVIDER = 2;
    
    public static final int REASON_NOT_SPECIFIED = 0;
    public static final int UNRECOGNIZED_PDU = 1;
    public static final int UNEXPECTED_PDU = 2;
    public static final int UNRECOGNIZED_PDU_PARAMETER = 4;
    public static final int UNEXPECTED_PDU_PARAMETER = 5;
    public static final int INVALID_PDU_PARAMETER_VALUE = 6;

    /** Returns Source field value.
     * @return Source field value. */    
    public int source();

    /** Returns Reason field value.
     * @return Reason field value. */    
    public int reason();
}

