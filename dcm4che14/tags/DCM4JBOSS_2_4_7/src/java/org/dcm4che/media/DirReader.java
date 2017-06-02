/*  Copyright (c) 2001,2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4che.media;

import java.io.File;
import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3656 $ $Date: 2003-06-10 01:17:51 +0800 (周二, 10 6月 2003) $
 */
public interface DirReader
{
    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static int CONSISTENCY = 0x0000;
    /**  Description of the Field */
    public final static int INCONSISTENCY = 0xFFFF;

    // Public --------------------------------------------------------
    /**
     *  Gets the fileSetInfo attribute of the DirReader object
     *
     * @return    The fileSetInfo value
     */
    public Dataset getFileSetInfo();


    /**
     *  Gets the refFile attribute of the DirReader object
     *
     * @param  root     Description of the Parameter
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(File root, String[] fileIDs);


    /**
     *  Gets the refFile attribute of the DirReader object
     *
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(String[] fileIDs);


    /**
     *  Gets the descriptorFile attribute of the DirReader object
     *
     * @param  root                   Description of the Parameter
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile(File root)
        throws DcmValueException;


    /**
     *  Gets the descriptorFile attribute of the DirReader object
     *
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile()
        throws DcmValueException;


    /**
     *  Gets the empty attribute of the DirReader object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The empty value
     * @exception  IOException  Description of the Exception
     */
    public boolean isEmpty(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the firstRecord attribute of the DirReader object
     *
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord()
        throws IOException;


    /**
     *  Gets the firstRecord attribute of the DirReader object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the firstRecordBy attribute of the DirReader object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstRecordBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecordBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException;


    /**
     *  Description of the Method
     *
     * @exception  IOException  Description of the Exception
     */
    public void close()
        throws IOException;

}

