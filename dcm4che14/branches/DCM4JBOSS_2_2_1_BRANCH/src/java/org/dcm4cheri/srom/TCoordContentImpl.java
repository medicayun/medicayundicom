/*$Id: TCoordContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

//java imports
import java.util.Date;


/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class TCoordContentImpl extends NamedContentImpl
        implements org.dcm4che.srom.TCoordContent {

            
    static class Point extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.Point {
                
        Point(KeyObject owner, Date obsDateTime, Template template, 
              Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 1) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.Point(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "POINT";  }        
    }//end inner class Point

    
    static class MultiPoint extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.MultiPoint {
                
        MultiPoint(KeyObject owner, Date obsDateTime, Template template, 
                   Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.MultiPoint(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "MULTIPOINT";  }        
    }//end inner class MultiPoint

    
    static class Segment extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.Segment {
                
        Segment(KeyObject owner, Date obsDateTime, Template template, 
                Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 2) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.Segment(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "SEGMENT";  }        
    }//end inner class Segment

    
    static class MultiSegment extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.MultiSegment {
                
        MultiSegment(KeyObject owner, Date obsDateTime, Template template, 
                     Code name, Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            
            if ((positions.size() & 1) != 0) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.MultiSegment(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "MULTISEGMENT";  }        
    }//end inner class MultiSegment

    
    static class Begin extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.Begin {
                
        Begin(KeyObject owner, Date obsDateTime, Template template, Code name,
              Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 1) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.Begin(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "BEGIN";  }        
    }//end inner class Begin

    
    static class End extends TCoordContentImpl
            implements org.dcm4che.srom.TCoordContent.End {
                
        End(KeyObject owner, Date obsDateTime, Template template, Code name,
            Positions positions) {
            super(owner, obsDateTime, template, name, positions);
            if (positions.size() != 1) {
                throw new IllegalArgumentException("" + positions);
            }
        }//end constructor
        
        Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
            return new TCoordContentImpl.End(newOwner,
                    getObservationDateTime(inheritObsDateTime),
                    template, name, positions);
        }

        public String getRangeType() {  return "END";  }        
    }//end inner class End

    //-- Positions ----------------------------------------------------
    static Positions newPositions(Dataset ds) throws DcmValueException {
        DcmElement e;
        if ((e = ds.get(Tags.RefSamplePositions)) != null) {
            return new SamplePositions(e.getInts());
        }
        if ((e = ds.get(Tags.RefTimeOffsets)) != null) {
            return new RelativePositions(e.getFloats());
        }
        if ((e = ds.get(Tags.RefDatetime)) != null) {
            return new AbsolutePositions(e.getDates());
        }
        throw new IllegalArgumentException("Missing Positions");
    }
    
    static class SamplePositions
            implements org.dcm4che.srom.TCoordContent.Positions.Sample {
        private final int[] indexes;
        
        SamplePositions(int[] indexes) {
            this.indexes = (int[])indexes.clone();
        }//end constructor
        
        public int size() { return indexes.length; }
        
        public int[] getIndexes() {
            return (int[])indexes.clone();
        }//end getIndexes()
        
        public String toString() {
            return "Sample[" + size() + "]";
        }
        
        public void toDataset(Dataset ds) {
            ds.putUL(Tags.RefSamplePositions, indexes);
        }
    }//end inner class SamplePositions

    
    static class RelativePositions
            implements org.dcm4che.srom.TCoordContent.Positions.Relative {
        private final float[] offsets;
        
        RelativePositions(float[] offsets) {
            this.offsets = (float[])offsets.clone();
        }//end constructor
        
        public int size() { return offsets.length; }
        
        public float[] getOffsets() {
            return (float[])offsets.clone();
        }//end getOffsets()
        
        public String toString() {
            return "Offset[" + size() + "]";
        }
        
        public void toDataset(Dataset ds) {
            ds.putDS(Tags.RefTimeOffsets, offsets);
        }
    }//end inner class RelativePositions

    
    static class AbsolutePositions
            implements org.dcm4che.srom.TCoordContent.Positions.Absolute {
        private final long[] dateTimes;
        
        AbsolutePositions(Date[] dateTimes) {
            this.dateTimes = new long[dateTimes.length];
            for (int i = 0; i < dateTimes.length; ++i) {
                this.dateTimes[i] = dateTimes[i].getTime();
            }
        }//end constructor
        
        public int size() { return dateTimes.length; }
        
        public Date[] getDateTimes() {
            Date[] dts = new Date[dateTimes.length];
            for (int i = 0; i < dateTimes.length; ++i) {
                dts[i] = new Date(dateTimes[i]);
            }
            return dts;
        }//end getDateTimes()
    
        public String toString() {
            return "DateTime[" + size() + "]";
        }
        
        public void toDataset(Dataset ds) {
            ds.putDT(Tags.RefDatetime, getDateTimes());
        }
    }//end inner class AbsolutePositions

    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected final Positions positions;

    // Constructors --------------------------------------------------

    // Constructors --------------------------------------------------
    TCoordContentImpl(KeyObject owner, Date obsDateTime, Template template,
                      Code name, Positions positions) {
        super(owner, obsDateTime, template, name);
        this.positions = positions;
    }//end generic TCoordContentImpl constructor

    
    // Methodes --------------------------------------------------------
    
    public final ValueType getValueType() {
        return ValueType.TCOORD;
    }//end getValueType()
    
    public final Positions getPositions() {
        return positions;
    }//end getPositions()

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putCS(Tags.TemporalRangeType, getRangeType());
        positions.toDataset(ds);
    }
}//end abstract class TCoordContentImpl
