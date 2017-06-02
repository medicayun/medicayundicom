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

package org.dcm4che.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
public class MLLPOutputStream extends FilterOutputStream {
    
    // Constants -----------------------------------------------------
    private static final int START_BYTE = 0x0b;
    private static final int END_BYTE   = 0x1c;
    
    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    public MLLPOutputStream(OutputStream out) {
        super(out);
    }
    
    // Methods -------------------------------------------------------
    public void writeMessage(byte[] msg) throws IOException {
        out.write(START_BYTE);
        out.write(msg);
        out.write(END_BYTE);
        out.write('\r');
    }
}
