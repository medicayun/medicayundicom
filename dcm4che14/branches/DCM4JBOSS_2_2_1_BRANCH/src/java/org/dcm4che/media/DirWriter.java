/*$Id: DirWriter.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

package org.dcm4che.media;

import org.dcm4che.data.Dataset;

import java.io.IOException;
import java.io.File;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface DirWriter extends DirReader {
    
    public DirRecord add(DirRecord parent, String type, Dataset ds,
            String[] fileIDs, String classUID, String instUID, String tsUID)
            throws IOException;
    
    public DirRecord add(DirRecord parent, String type, Dataset ds)
            throws IOException;
    
    public int remove(DirRecord rec) throws IOException;

    public DirRecord replace(DirRecord old, String type, Dataset ds,
            String[] fileIDs, String classUID, String instUID, String tsUID)
            throws IOException;
    
    public DirRecord replace(DirRecord old, String type, Dataset ds)
            throws IOException;    

    public DirWriter compact() throws IOException;
    
    public void commit() throws IOException;
    
    public void rollback() throws IOException;

    public String[] toFileIDs(File file) throws IOException;

    public boolean isAutoCommit();
    
    public void setAutoCommit(boolean autoCommit) throws IOException;        
}

