/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
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

import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3846 $ $Date: 2004-12-24 00:33:09 +0800 (周五, 24 12月 2004) $
 */
public interface DirRecord
{
    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static int INACTIVE = 0x0000;
    /**  Description of the Field */
    public final static int IN_USE = 0xFFFF;
    public static final String PATIENT = "PATIENT";
    public static final String RT_PLAN = "RT PLAN";
    public static final String RT_TREAT_RECORD = "RT TREAT RECORD";
    public static final String RT_STRUCTURE_SET = "RT STRUCTURE SET";
    public static final String RT_DOSE = "RT DOSE";
    public static final String KEY_OBJECT_DOC = "KEY OBJECT DOC";
    public static final String SR_DOCUMENT = "SR DOCUMENT";
    public static final String PRESENTATION = "PRESENTATION";
    public static final String VOI_LUT = "VOI LUT";
    public static final String MODALITY_LUT = "MODALITY LUT";
    public static final String WAVEFORM = "WAVEFORM";
    public static final String CURVE = "CURVE";
    public static final String OVERLAY = "OVERLAY";
    public static final String IMAGE = "IMAGE";
    public static final String STORED_PRINT = "STORED PRINT";
    public static final String SERIES = "SERIES";
    public static final String STUDY = "STUDY";

    public void reload() throws IOException, DcmValueException;
    
    /**
     *  Gets the type attribute of the DirRecord object
     *
     * @return    The type value
     */
    public String getType();


    /**
     *  Gets the inUseFlag attribute of the DirRecord object
     *
     * @return    The inUseFlag value
     */
    public int getInUseFlag();


    /**
     *  Gets the refFileIDs attribute of the DirRecord object
     *
     * @return    The refFileIDs value
     */
    public String[] getRefFileIDs();


    /**
     *  Gets the refSOPClassUID attribute of the DirRecord object
     *
     * @return    The refSOPClassUID value
     */
    public String getRefSOPClassUID();


    /**
     *  Gets the refSOPInstanceUID attribute of the DirRecord object
     *
     * @return    The refSOPInstanceUID value
     */
    public String getRefSOPInstanceUID();


    /**
     *  Gets the refSOPTransferSyntaxUID attribute of the DirRecord object
     *
     * @return    The refSOPTransferSyntaxUID value
     */
    public String getRefSOPTransferSyntaxUID();


    /**
     *  Gets the dataset attribute of the DirRecord object
     *
     * @return    The dataset value
     */
    public Dataset getDataset();


    /**
     *  Gets the firstChild attribute of the DirRecord object
     *
     * @return                  The firstChild value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChild()
        throws IOException;


    /**
     *  Gets the firstChild attribute of the DirRecord object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstChild value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChild(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the firstChildBy attribute of the DirRecord object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstChildBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstChildBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException;


    /**
     *  Gets the nextSibling attribute of the DirRecord object
     *
     * @return                  The nextSibling value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSibling()
        throws IOException;


    /**
     *  Gets the nextSibling attribute of the DirRecord object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The nextSibling value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSibling(boolean onlyInUse)
        throws IOException;


    /**
     *  Gets the nextSiblingBy attribute of the DirRecord object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The nextSiblingBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getNextSiblingBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException;


    /**
     *  Description of the Method
     *
     * @param  type          Description of the Parameter
     * @param  keys          Description of the Parameter
     * @param  ignorePNCase  Description of the Parameter
     * @return               Description of the Return Value
     */
    public boolean match(String type, Dataset keys, boolean ignorePNCase);
}

