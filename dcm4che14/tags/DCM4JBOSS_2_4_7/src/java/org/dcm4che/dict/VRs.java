/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4che.dict;


/** 
 * Provides VR constants and VR related utility functions.
 *
 * <p>
 * Further Information regarding Value Representation (DICOM data types)
 * can be found at: <br>
 * <code>PS 3.5 - 2000 Section 6.2 Page 15</code>
 * </p>
 * 
 * @author gunter.zeilinger@tiani.com (Gunter Zeilinger)
 * @author hauer@psicode.com (Sebastian Hauer)
 * @version 1.0
 * @since version 0.1
 */
public class VRs  {
  
    /**
     * Private constructor.
     */
    private VRs() {}
  
    public static String toString(int vr) {
        return (vr == NONE
                ? "NONE"
                : new String(new byte[]{(byte)(vr>>8), (byte)(vr)}));
    }

    public static int valueOf(String str) {
        if ("NONE".equals(str))
            return VRs.NONE;
        
        if (str.length() != 2)
            throw new IllegalArgumentException(str);
        
        return ((str.charAt(0) & 0xff) << 8) | (str.charAt(1) & 0xff);
    }
    
    /**
     * NULL element for VRs. Use as VR value for Data Elements, 
     * Item (FFFE,E000), Item Delimitation Item (FFFE,E00D), and
     * Sequence Delimitation Item (FFFE,E0DD).
     */
    public static final int NONE = 0x0000;
    
    public static final int AE = 0x4145;
    
    public static final int AS = 0x4153;
    
    public static final int AT = 0x4154;
    
    public static final int CS = 0x4353;
    
    public static final int DA = 0x4441;
    
    public static final int DS = 0x4453;
    
    public static final int DT = 0x4454;
    
    public static final int FL = 0x464C;
    
    public static final int FD = 0x4644;
    
    public static final int IS = 0x4953;
    
    public static final int LO = 0x4C4F;
    
    public static final int LT = 0x4C54;
    
    public static final int OB = 0x4F42;
    
    public static final int OF = 0x4F46;

    public static final int OW = 0x4F57;
    
    public static final int PN = 0x504E;
    
    public static final int SH = 0x5348;
    
    public static final int SL = 0x534C;
    
    public static final int SQ = 0x5351;
    
    public static final int SS = 0x5353;
    
    public static final int ST = 0x5354;
    
    public static final int TM = 0x544D;
    
    public static final int UI = 0x5549;
    
    public static final int UL = 0x554C;
    
    public static final int UN = 0x554E;
    
    public static final int US = 0x5553;
    
    public static final int UT = 0x5554;
        
    public static boolean isLengthField16Bit(int vr) {
        switch (vr) {
            case AE: case AS: case AT: case CS: case DA: case DS: case DT:
            case FL: case FD: case IS: case LO: case LT: case PN: case SH:
            case SL: case SS: case ST: case TM: case UI: case UL: case US:
                return true;
            default:
                return false;
        }
    }//end isLengthField16Bit()
  
    public static int getPadding(int vr) {
        switch (vr) {
            case AE: case AS: case CS: case DA: case DS: case DT: case IS:
            case LO: case LT: case PN: case SH: case SL: case ST: case TM:
            case UT:
                return ' ';
            default:
                return 0;
        }
    }
    
    public static boolean isStringValue(int vr) {
        switch (vr) {
            case AE: case AS: case CS: case DA: case DS: case DT: case IS:
            case LO: case LT: case PN: case SH: case ST: case TM: case UI:
            case UT:
                return true;
        }
        return false;
    }
    
}//end class VR
