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

package org.dcm4che.hl7;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3545 $ $Date: 2002-08-12 16:23:30 +0800 (周一, 12 8月 2002) $
 * @since August 11, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public abstract class HL7Exception extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>HL7Exception</code> without detail message.
     */
    public HL7Exception() {
    }
    
    
    /**
     * Constructs an instance of <code>HL7Exception</code> with the specified detail message.
     * @param msg the detail message.
     */
    public HL7Exception(String msg) {
        super(msg);
    }
    
    public abstract byte[] makeACK(MSHSegment msh);
    
    public static class AE extends HL7Exception {
        public AE() {
        }
        
        public AE(String msg) {
            super(msg);
        }
    
        public byte[] makeACK(MSHSegment msh) {
            return msh.makeACK_AE(getMessage(),null,null);
        }
        
    }

    public static class AR extends HL7Exception {
        public AR() {
        }
        
        public AR(String msg) {
            super(msg);
        }
    
        public byte[] makeACK(MSHSegment msh) {
            return msh.makeACK_AE(getMessage(),null,null);
        }
        
    }
}
