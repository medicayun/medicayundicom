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

package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.Dataset;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.dcm4che.util.Base64;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3608 $ $Date: 2003-03-03 18:30:34 +0800 (周一, 03 3月 2003) $
 * @since August 27, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class DicomQuery implements IHEYr4.Message {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private String keys;
    private RemoteNode requestor;
    private String cuid;
    
    // Constructors --------------------------------------------------
    public DicomQuery(Dataset keys, RemoteNode requestor, String cuid) {
        this.keys = DicomQuery.encode(keys);
        this.requestor = requestor;
        this.cuid = cuid;
    }
    
    // Methods -------------------------------------------------------
    private static String encode(Dataset keys) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
        try {
            keys.writeDataset(bout, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize keys", e);
        }
        return Base64.byteArrayToBase64(bout.toByteArray());
    }
    
    public void writeTo(StringBuffer sb) {
        sb.append("<DicomQuery><Keys>")
          .append(keys)
          .append("</Keys><Requestor>");
        requestor.writeTo(sb);
        sb.append("</Requestor><CUID>")
          .append(cuid)
          .append("</CUID></DicomQuery>");
    }
    
}
