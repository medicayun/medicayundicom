/*$Id: TCoordContent.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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
 * The <code>TCoordContent</code> interface represents a
 * <i>DICOM SR Temporal Coordinate</i> of value type <code>TCOORD</code>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.7 Temporal Coordinates Macro"
 */
public interface TCoordContent extends Content {

    
    /**
     * The <code>Point</code> interface represents
     * a single temporal point.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Point extends TCoordContent {}
    
    /**
     * The <code>MultiPoint</code> interface represents
     * multiple temporal points.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface MultiPoint extends TCoordContent {}
    
    /**
     * The <code>Segment</code> interface represents
     * a range between two temporal points.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Segment extends TCoordContent {}
    
    /**
     * The <code>MultiSegment</code> interface represents
     * multiple segments, each denoted by two temporal points.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface MultiSegment extends TCoordContent {}
    
    /**
     * The <code>Begin</code> interface represents
     * a range beginning at one temporal point, and extending beyond 
     * the end of the acquired data.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Begin extends TCoordContent {}
    
    /**
     * The <code>End</code> interface represents
     * a range beginning before the start of the acquired data, and 
     * extending to (and including) the identified temporal point.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface End extends TCoordContent {}
    
    /**
     * Generic <code>Positions</code> interface
     * for <i>DICOM SR Temporal Coordinate</i> positions.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7 Temporal Coordinates Macro"
     */
    interface Positions {
        
        /**
         * Returns the number of position values.
         *
         * @return number of position values.
         */
        public int size();
        
        public void toDataset(Dataset ds);
    
        /**
         * The <code>Sample</code> interface
         * represents <i>DICOM SR Referenced Sample Positions</i>.
         * <br>
         * DICOM Tag: <code>(0040,A132)</code>
         * <br>
         * List of samples within a multiplex group specifying 
         * temporal points of the referenced data. Position of 
         * first sample is 1.
         *
         * @author  gunter.zeilinger@tiani.com
         * @version 1.0
         *
         * @see "DICOM Part 3: Information Object Definitions,
         * Annex C.18.7 Temporal Coordinates Macro"
         */
        interface Sample extends Positions {
            public int[] getIndexes();
        }
        
        /**
         * The <code>Relative</code> interface
         * represents <i>DICOM SR Referenced Time Offsets</i>.
         * <br>
         * DICOM Tag: <code>(0040,A138)</code>
         * <br>
         * Specifies temporal points for reference by number 
         * of seconds after start of data.
         *
         * @author  gunter.zeilinger@tiani.com
         * @version 1.0
         *
         * @see "DICOM Part 3: Information Object Definitions,
         * Annex C.18.7 Temporal Coordinates Macro"
         */
        interface Relative extends Positions {
            public float[] getOffsets();
        }
        
        /**
         * The <code>Absolute</code> interface
         * represents <i>DICOM SR Referenced Datetime</i>.
         * <br>
         * DICOM Tag: <code>(0040,A13A)</code>
         * <br>
         * Specifies temporal points for reference by absolute time.
         *
         * @author  gunter.zeilinger@tiani.com
         * @version 1.0
         *
         * @see "DICOM Part 3: Information Object Definitions,
         * Annex C.18.7 Temporal Coordinates Macro"
         */
        interface Absolute extends Positions {
            public Date[] getDateTimes();
        }
    }//end inner interface Positions        

    // Public --------------------------------------------------------
    
    /**
     * Returns the <i>Temporal Range Type</i>.
     * <br>DICOM Tag: <code>(0040,A130)</code>
     * <br>Tag Name: <code>Temporal Range Type</code>
     * <br>
     * This Attribute defines the type of temporal extent of the 
     * region of interest. A temporal point (or instant of time) 
     * may be defined by a waveform sample offset (for a single 
     * waveform multiplex group only), time offset, or absolute time.
     * The following return Values are specified for temporal coordinates:<br>
     * <br>
     * <dl>
     *   <dt>"POINT"</dt>
     *   <dd> a single temporal point.</dd>
     *
     *   <dt>"MULTIPOINT"</dt>
     *   <dd> multiple temporal points.</dd>
     *
     *   <dt>"SEGMENT"</dt>
     *   <dd> a range between two temporal points.</dd>
     *  
     *   <dt>"MULTISEGMENT"</dt>
     *   <dd> multiple segments, each denoted by two temporal points.</dd> 
     *
     *   <dt>"BEGIN"</dt>
     *   <dd> a range beginning at one temporal point, and extending beyond 
     *        the end of the acquired data.</dd> 
     *
     *   <dt>"END"</dt>
     *   <dd> a range beginning before the start of the acquired data, and 
     *        extending to (and including) the identified temporal point.</dd> 
     * </dl>
     *
     * @return the <i>Temporal Range Type</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.7.1 Temporal Range Type"
     */
    public String getRangeType();
    
    /**
     * Returns the positions.
     *
     * @return  the positions.
     */
    public Positions getPositions();
    
}//end interface TCoordContent
