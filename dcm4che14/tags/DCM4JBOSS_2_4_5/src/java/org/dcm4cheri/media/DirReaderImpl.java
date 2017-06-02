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
package org.dcm4cheri.media;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      May, 2002
 * @version    $Revision: 3656 $ $Date: 2003-06-10 01:17:51 +0800 (周二, 10 6月 2003) $
 */
class DirReaderImpl implements DirReader
{

    final static DcmParserFactory pfactory = DcmParserFactory.getInstance();
    final static DcmObjectFactory factory = DcmObjectFactory.getInstance();
    /**  Description of the Field */
    protected final File file;
    /**  Description of the Field */
    protected final ImageInputStream in;
    /**  Description of the Field */
    protected final DcmParser parser;
    /**  Description of the Field */
    protected Dataset fsi;
    /**  Description of the Field */
    protected int offFirstRootRec;
    /**  Description of the Field */
    protected int offLastRootRec;
    /**  Description of the Field */
    protected int seqLength;
    /**  Description of the Field */
    protected long offFirstRootRecValPos;
    /**  Description of the Field */
    protected long offLastRootRecValPos;
    /**  Description of the Field */
    protected long seqValuePos;


    /**
     * Creates a new instance of DirReaderImpl
     *
     * @param  file  Description of the Parameter
     * @param  in    Description of the Parameter
     */
    DirReaderImpl(File file, ImageInputStream in)
    {
        this.file = file != null ? file.getAbsoluteFile() : null;
        this.in = in;
        this.parser = pfactory.newDcmParser(in);
    }


    DirReaderImpl initReader()
        throws IOException
    {
        this.fsi = factory.newDataset();
        parser.setDcmHandler(fsi.getDcmHandler());
        this.seqValuePos =
                parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.DirectoryRecordSeq);
        parser.setDcmHandler(null);
        if (parser.getReadTag() != Tags.DirectoryRecordSeq) {
            throw new DcmValueException("Missing Directory Record Sequence");
        }
        this.seqLength = parser.getReadLength();

        DcmElement offFirstRootRecElem =
                fsi.get(Tags.OffsetOfFirstRootDirectoryRecord);
        if (offFirstRootRecElem == null || offFirstRootRecElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of First Directory Record");
        }
        this.offFirstRootRec = offFirstRootRecElem.getInt();
        this.offFirstRootRecValPos = offFirstRootRecElem.getStreamPosition() + 8;

        DcmElement offLastRootRecElem =
                fsi.get(Tags.OffsetOfLastRootDirectoryRecord);
        if (offLastRootRecElem == null || offLastRootRecElem.isEmpty()) {
            throw new DcmValueException(
                    "Missing Offset of Last Directory Record");
        }
        this.offLastRootRec = offLastRootRecElem.getInt();
        this.offLastRootRecValPos = offLastRootRecElem.getStreamPosition() + 8;
        return this;
    }


    /**
     *  Gets the fileSetInfo attribute of the DirReaderImpl object
     *
     * @return    The fileSetInfo value
     */
    public Dataset getFileSetInfo()
    {
        return fsi;
    }


    /**
     *  Gets the refFile attribute of the DirReaderImpl object
     *
     * @param  root     Description of the Parameter
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(File root, String[] fileIDs)
    {
        File retval = new File(root, fileIDs[0]);
        for (int i = 1; i < fileIDs.length; ++i) {
            retval = new File(retval, fileIDs[i]);
        }
        return retval;
    }


    /**
     *  Gets the refFile attribute of the DirReaderImpl object
     *
     * @param  fileIDs  Description of the Parameter
     * @return          The refFile value
     */
    public File getRefFile(String[] fileIDs)
    {
        if (file == null) {
            throw new IllegalStateException("Unkown root directory");
        }
        return getRefFile(file.getParentFile(), fileIDs);
    }


    /**
     *  Gets the descriptorFile attribute of the DirReaderImpl object
     *
     * @param  root                   Description of the Parameter
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile(File root)
        throws DcmValueException
    {
        String[] fileID = fsi.getStrings(Tags.FileSetDescriptorFileID);
        if (fileID == null || fileID.length == 0) {
            return null;
        }
        return getRefFile(root, fileID);
    }


    /**
     *  Gets the descriptorFile attribute of the DirReaderImpl object
     *
     * @return                        The descriptorFile value
     * @exception  DcmValueException  Description of the Exception
     */
    public File getDescriptorFile()
        throws DcmValueException
    {
        if (file == null) {
            throw new IllegalStateException("Unkown root directory");
        }
        return getDescriptorFile(file.getParentFile());
    }


    /**
     *  Gets the empty attribute of the DirReaderImpl object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The empty value
     * @exception  IOException  Description of the Exception
     */
    public boolean isEmpty(boolean onlyInUse)
        throws IOException
    {
        return getFirstRecord(onlyInUse) == null;
    }


    /**
     *  Gets the firstRecord attribute of the DirReaderImpl object
     *
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord()
        throws IOException
    {
        return getFirstRecord(true);
    }


    /**
     *  Gets the firstRecord attribute of the DirReaderImpl object
     *
     * @param  onlyInUse        Description of the Parameter
     * @return                  The firstRecord value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecord(boolean onlyInUse)
        throws IOException
    {
        if (offFirstRootRec == 0) {
            return null;
        }
        DirRecord retval = new DirRecordImpl(parser, offFirstRootRec);
        if (onlyInUse && retval.getInUseFlag() == DirRecord.INACTIVE) {
            return retval.getNextSibling(onlyInUse);
        }
        return retval;
    }


    /**
     *  Gets the firstRecordBy attribute of the DirReaderImpl object
     *
     * @param  type             Description of the Parameter
     * @param  keys             Description of the Parameter
     * @param  ignorePNCase     Description of the Parameter
     * @return                  The firstRecordBy value
     * @exception  IOException  Description of the Exception
     */
    public DirRecord getFirstRecordBy(String type, Dataset keys, boolean ignorePNCase)
        throws IOException
    {
        DirRecord dr = getFirstRecord(true);
        return (dr == null || dr.match(type, keys, ignorePNCase))
                 ? dr
                 : dr.getNextSiblingBy(type, keys, ignorePNCase);
    }


    /**
     *  Description of the Method
     *
     * @exception  IOException  Description of the Exception
     */
    public void close()
        throws IOException
    {
        in.close();
    }

}

