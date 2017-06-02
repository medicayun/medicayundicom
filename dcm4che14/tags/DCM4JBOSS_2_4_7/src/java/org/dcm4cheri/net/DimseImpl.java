/*$Id: DimseImpl.java 3838 2004-12-09 17:46:38Z gunterze $*/
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

package org.dcm4cheri.net;

import org.dcm4che.net.Dimse;
import org.dcm4che.net.DataSource;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.DictionaryFactory;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DimseImpl implements Dimse {

    protected static final DcmObjectFactory objFact =
            DcmObjectFactory.getInstance();
    private final int pcid;
    private final Command cmd;
    private Dataset ds;
    private InputStream in;
    private final DataSource src;
    private String tsUID;
        
    public DimseImpl(int pcid, String tsUID, Command cmd, InputStream in) {
        this.pcid = pcid;
        this.cmd = cmd;
        this.ds = null;
        this.src = null;
        this.in = in;
        this.tsUID = tsUID;
    }

    public DimseImpl(int pcid, Command cmd, Dataset ds, DataSource src) {
        this.pcid = pcid;
        this.cmd = cmd;
        this.ds = ds;
        this.src = src;
        this.in = null;
        this.tsUID = null;
        this.cmd.putUS(Tags.DataSetType,
                ds == null && src == null ? Command.NO_DATASET : 0);
    }

    public final int pcid() {
        return pcid;
    }
    
    public final Command getCommand() {
        return cmd;
    }
    
    public final String getTransferSyntaxUID() {
        return tsUID;
    }
    
    final void setTransferSyntaxUID(String tsuid) {
        this.tsUID = tsuid;
    }
    
    public final Dataset getDataset() throws IOException {
        if (ds != null) {
            return ds;
        }
        if (in == null) {
            return null;
        }
        if (tsUID == null) {
            throw new IllegalStateException();
        }
        ds = objFact.newDataset();
        ds.readDataset(in, DcmDecodeParam.valueOf(tsUID), -1);
        in.close();
        in = null;
        return ds;
    }
    
    public final InputStream getDataAsStream() {
        return in;
    }
    
    public void writeTo(OutputStream out, String tsUID) throws IOException {
        if (src != null) {
            src.writeTo(out, tsUID);
            return;
        }
        if (ds == null) {
            throw new IllegalStateException("Missing Dataset");
        }
        ds.writeDataset(out, DcmDecodeParam.valueOf(tsUID));       
    }

    private static UIDDictionary DICT =
      DictionaryFactory.getInstance().getDefaultUIDDictionary();
    
    public String toString() {
       return "[pc-" + pcid + "] " + cmd.toString();
    }
    
    public void closeDataStream() {
       if (in != null)
           try {
               in.close();
           } catch (IOException ignore) {               
           }           
    }
}