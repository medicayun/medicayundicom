/*$Id: DatasetSerializer.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

package org.dcm4che.data;

import java.io.IOException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DatasetSerializer implements java.io.Serializable {
    
    static final long serialVersionUID =  -4404056689087154718L;

    private transient Dataset ds;
    
    public DatasetSerializer() {}

    public DatasetSerializer(Dataset ds) {
        this.ds = ds; 
    }
    
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        boolean fmi = ds.getFileMetaInfo() != null;
        out.writeBoolean(fmi);
        ds.writeFile(out, fmi ? null : DcmEncodeParam.EVR_LE);
    }
    
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        ds = DcmObjectFactory.getInstance().newDataset();
        ds.readFile(in, in.readBoolean() ? FileFormat.DICOM_FILE
                                     : FileFormat.EVR_LE_STREAM, -1);
    }
    
    private Object readResolve() throws java.io.ObjectStreamException {
        return ds;
    }        
}
