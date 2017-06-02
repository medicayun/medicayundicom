/*$Id: DcmHandler.java 3754 2004-03-22 19:39:31Z gunterze $*/
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
public interface DcmHandler {
    
    public void startCommand() throws IOException;
    
    public void endCommand() throws IOException;
    
    public void startDcmFile() throws IOException;
    
    public void endDcmFile() throws IOException;
    
    public void startFileMetaInfo(byte[] preamble) throws IOException;
    
    public void endFileMetaInfo() throws IOException;
    
    public void startDataset() throws IOException;
    
    public void endDataset() throws IOException;
    
    public void setDcmDecodeParam(DcmDecodeParam param);
    
    public void startElement(int tag, int vr, long pos) throws IOException;

    public void endElement() throws IOException;

    public void startSequence(int length) throws IOException;

    public void endSequence(int length) throws IOException;

    public void startItem(int id, long pos, int length) throws IOException;

    public void endItem(int len) throws IOException;

    public void value(byte[] data, int start, int length) throws IOException;

    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException;
}

