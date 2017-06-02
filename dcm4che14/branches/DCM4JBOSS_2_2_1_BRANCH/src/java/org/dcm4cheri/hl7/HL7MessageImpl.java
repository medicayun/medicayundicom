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

package org.dcm4cheri.hl7;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.MSHSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3656 $ $Date: 2003-06-10 01:17:51 +0800 (周二, 10 6月 2003) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7MessageImpl implements HL7Message {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final MSHSegmentImpl msh;
    private final ArrayList segs = new ArrayList(4);
    
    // Static --------------------------------------------------------
    static int indexOfNextCRorLF(byte[] data, int start) {
        for (int i = start;  i < data.length; ++i) {
            if (data[i] == (byte)'\r' || data[i] == (byte)'\n') {
                return i;
            }
            if (data[i] == (byte)'\\') {
                ++i;
            }
        }
        return data.length;
    }
    
    // Constructors --------------------------------------------------
    HL7MessageImpl(byte[] data)
    throws HL7Exception {
        int pos = indexOfNextCRorLF(data, 0);
        msh = new MSHSegmentImpl(data, 0, pos);
        while (++pos < data.length) {
            int nextPos = indexOfNextCRorLF(data, pos);
            int len = nextPos - pos;
            if (len > 0) {
                segs.add(new HL7SegmentImpl(data, pos, len));
            }
            pos = nextPos;
        }
    }
    
    // Public --------------------------------------------------------
    public MSHSegment header() {
        return msh;
    }
    
    public List segments() {
        return Collections.unmodifiableList(segs);
    }
    
    public String toString() {
        return segs.toString();
    }
    
    StringBuffer toVerboseStringBuffer(StringBuffer sb) {
        sb.append(msh.getMessageType()).append('^').append(msh.getTriggerEvent())
        .append(" message\t// ")
        .append(HL7SegmentImpl.getName(msh.getMessageType(), "????"))
        .append(" - ")
        .append(HL7SegmentImpl.getName(msh.getTriggerEvent(), "????"));
        sb.append("\n\t");
        msh.toVerboseStringBuffer(sb);
        for (Iterator it =  segs.iterator(); it.hasNext();) {
            sb.append("\n\t");
            ((HL7SegmentImpl)it.next()).toVerboseStringBuffer(sb);
        }
        return sb;
    }
    
    public String toVerboseString() {
        return toVerboseStringBuffer(new StringBuffer()).toString();
    }
}
