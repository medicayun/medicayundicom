/*$Id: SCoordContent.java 3493 2002-07-14 16:03:36Z gunterze $*/
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


/**
 * The <code>SCoordContent</code> interface represents a
 * <i>DICOM SR Spatial Coordinate</i> of value type <code>SCOORD</code>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.6 Spatial Coordinates Macro"
 */
public interface SCoordContent extends Content {
    
    /**
     * The <code>Point</code> interface represents a
     * <i>DICOM SR Spatial Coordinate</i> of value type <code>SCOORD</code>.
     * The <i>Graphic Type</i> <code>(0070,0023)</code> is 
     * <code>"POINT"</code>.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6 Spatial Coordinates Macro"
     */
    interface Point extends SCoordContent {}
    
    /**
     * The <code>MultiPoint</code> interface represents a
     * <i>DICOM SR Spatial Coordinate</i> of value type <code>SCOORD</code>.
     * The <i>Graphic Type</i> <code>(0070,0023)</code> is 
     * <code>"MULTIPOINT"</code>.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6 Spatial Coordinates Macro"
     */
    interface MultiPoint extends SCoordContent {}
    
    /**
     * The <code>Polyline</code> interface represents a
     * <i>DICOM SR Spatial Coordinate</i> of value type <code>SCOORD</code>.
     * The <i>Graphic Type</i> <code>(0070,0023)</code> is 
     * <code>"POLYLINE"</code>.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6 Spatial Coordinates Macro"
     */
    interface Polyline extends SCoordContent {}
    
    /**
     * The <code>Circle</code> interface represents a
     * <i>DICOM SR Spatial Coordinate</i> of value type <code>SCOORD</code>.
     * The <i>Graphic Type</i> <code>(0070,0023)</code> is 
     * <code>"CIRCLE"</code>.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6 Spatial Coordinates Macro"
     */
    interface Circle extends SCoordContent {}
    
    /**
     * The <code>Ellipse</code> interface represents a
     * <i>DICOM SR Spatial Coordinate</i> of value type <code>SCOORD</code>.
     * The <i>Graphic Type</i> <code>(0070,0023)</code> is 
     * <code>"ELLIPSE"</code>.
     *
     * @author  gunter.zeilinger@tiani.com
     * @version 1.0
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6 Spatial Coordinates Macro"
     */
    interface Ellipse extends SCoordContent {}

    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Returns the <i>Graphic Type</i>.
     * <br>DICOM Tag: <code>(0070,0023)</code>
     * <br>Tag Name: <code>Graphic Type</code>
     * <br>
     * When annotation applies to an image, 
     * this attribute defines the type of geometry 
     * of the annotated region of interest. 
     * The following return Values are specified 
     * for image spatial coordinate geometries:<br>
     * <br>
     * <dl>
     *   <dt>"POINT"</dt>
     *   <dd> a single pixel denoted by a single (column,row) pair.</dd>
     *
     *   <dt>"MULTIPOINT"</dt>
     *   <dd> multiple pixels each denoted by an (column,row) pair.</dd>
     *
     *   <dt>"POLYLINE"</dt>
     *   <dd> a closed polygon with vertices denoted by (column,row) pairs.</dd>
     *  
     *   <dt>"CIRCLE"</dt>
     *   <dd> a circle defined by two (column,row) pairs. 
     *        The first point is the central pixel. 
     *        The second point is a pixel on the perimeter of the circle. </dd> 
     *
     *   <dt>"ELLIPSE"</dt>
     *   <dd> an ellipse defined by four pixel (column,row) pairs, 
     *        the first two points specifying the endpoints of the 
     *        major axis and the second two points specifying the 
     *        endpoints of the minor axis of an ellipse. </dd>
     * </dl>
     *
     * @return the <i>Graphic Type</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6.1.2 Graphic Type"
     */
    public String getGraphicType();    

    /**
     * Returns the <i>Graphic Data</i>.
     * <br>DICOM Tag: <code>(0070,0022)</code>
     * <br>Tag Name: <code>Graphic Data</code>
     * <p>
     * Depending on the <i>Graphic Type</i> different sized <code>float</code>
     * arrays will be returned: 
     * <p>
     *
     * <dl>
     *   <dt>POINT</dt>
     *   <dd> 
     *      a single pixel denoted by a single (column,row) pair.<br>
     *      <pre>
     *          graphicData[0] = column;
     *          graphicData[1] = row;
     *      </pre>
     *   </dd>
     *
     *   <dt>MULTIPOINT</dt>
     *   <dd> 
     *      multiple pixels each denoted by an (column,row) pair.
     *      <pre>
     *          graphicData[0] = column_1;
     *          graphicData[1] = row_1;
     *          .
     *          .
     *          graphicData[n-1] = column_X;
     *          graphicData[n] = row_X;
     *      </pre>
     *   </dd>
     *
     *   <dt>POLYLINE</dt>
     *   <dd> 
     *      a closed polygon with vertices denoted by (column,row) pairs.
     *      <pre>
     *          graphicData[0] = column_1;
     *          graphicData[1] = row_1;
     *          .
     *          .
     *          graphicData[n-1] = column_X;
     *          graphicData[n] = row_X;
     *      </pre>
     *   </dd>
     *  
     *   <dt>CIRCLE</dt>
     *   <dd> 
     *      a circle defined by two (column,row) pairs. 
     *      The first point is the central pixel. 
     *      The second point is a pixel on the perimeter of the circle.
     *      <pre>
     *          graphicData[0] = central_pixel_column;
     *          graphicData[1] = central_pixel_row;
     *          graphicData[2] = perimeter_point_column;
     *          graphicData[3] = perimeter_point_row;
     *      </pre>
     *   </dd> 
     *
     *   <dt>ELLIPSE</dt>
     *   <dd> 
     *      an ellipse defined by four pixel (column,row) pairs, 
     *      the first two points specifying the endpoints of the 
     *      major axis and the second two points specifying the 
     *      endpoints of the minor axis of an ellipse.
     *      <pre>
     *          graphicData[0] = MAJOR_axis_X_column;
     *          graphicData[1] = MAJOR_axis_X_row;
     *          graphicData[2] = MAJOR_axis_Y_column;
     *          graphicData[3] = MAJOR_axis_Y_row;
     *          graphicData[4] = minor_axis_X_column;
     *          graphicData[5] = minor_axis_X_row;
     *          graphicData[6] = minor_axis_Y_column;
     *          graphicData[7] = minor_axis_Y_row;
     *      </pre>
     *   </dd>
     * </dl>
     * 
     * 
     * @return the <i>Graphic Data</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.6.1.1 Graphic Data"
     */
    public float[] getGraphicData();
    
}//end interface SCoordContent
