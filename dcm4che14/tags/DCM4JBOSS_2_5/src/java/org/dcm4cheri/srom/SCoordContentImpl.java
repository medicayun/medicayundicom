/*$Id: SCoordContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

package org.dcm4cheri.srom;

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

//java imports
import java.util.Date;


/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class SCoordContentImpl extends NamedContentImpl
        implements SCoordContent {
            
    // Inner classes ---------------------------------------------------------
            
    static class Point extends SCoordContentImpl
            implements SCoordContent.Point {
                
        Point(KeyObject owner, Date obsDateTime, Template template, Code name,
                float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if (graphicData.length != 2)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Point(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "POINT";  }        
    }//end inner class Point
    
            
    static class MultiPoint extends SCoordContentImpl
            implements SCoordContent.MultiPoint {
                
        MultiPoint(KeyObject owner, Date obsDateTime, Template template, 
                   Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if ((graphicData.length & 1) != 0)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.MultiPoint(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "MULTIPOINT";  }        
    }//end inner class MultiPoint
            
    
    static class Polyline extends SCoordContentImpl
            implements SCoordContent.Polyline {
                
        Polyline(KeyObject owner, Date obsDateTime, Template template, 
                 Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if ((graphicData.length&1) != 0)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Polyline(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "POLYLINE";  }        
    }//end inner class Polyline
            
    
    static class Circle extends SCoordContentImpl
            implements SCoordContent.Circle {
                
        Circle(KeyObject owner, Date obsDateTime, Template template, 
               Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if (graphicData.length != 4)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Circle(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "CIRCLE";  }        
    }//end inner class Circle
            
    
    static class Ellipse extends SCoordContentImpl
            implements SCoordContent.Ellipse {
                
        Ellipse(KeyObject owner, Date obsDateTime, Template template, 
                Code name, float[] graphicData) {
            super(owner, obsDateTime, template, name, graphicData);
            if (graphicData.length != 8)
                throw new IllegalArgumentException("float[" 
                                                   + graphicData.length + "]");
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new SCoordContentImpl.Ellipse(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, graphicData);
        }//end clone()

        public String getGraphicType() {  return "ELLIPSE";  }        
    }//end inner class Ellipse
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected final float[] graphicData;

    // Constructors --------------------------------------------------
    SCoordContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, float[] graphicData) {
        super(owner, obsDateTime, template, name);
        this.graphicData = (float[])graphicData.clone();
    }//end generic SCoordContentImpl constructor
    
    // Methodes --------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(getGraphicType()).append(":[");
        if (graphicData.length > 8) {
            sb.append("N=").append(graphicData.length);
        } else {
            sb.append(graphicData[0]);
            for (int i = 1; i < graphicData.length; ++i) {
                sb.append(',').append(graphicData[i]);
            }
        }
        return sb.append("]").toString();
    }//end toString()

    
    public ValueType getValueType() {
        return ValueType.SCOORD;
    }//end getValueType()
    
    
    public float[] getGraphicData() {
        return (float[])graphicData.clone();
    }//end getGraphicData()


    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putCS(Tags.GraphicType, getGraphicType());
        ds.putFL(Tags.GraphicData, graphicData);
    }
}//end class SCoordContentImpl
