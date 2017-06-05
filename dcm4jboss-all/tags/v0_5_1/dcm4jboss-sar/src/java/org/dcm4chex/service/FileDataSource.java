/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 896 $
 * @since 18.09.2003
 */
class FileDataSource implements DataSource
{

    private static final DcmParserFactory parserFact =
        DcmParserFactory.getInstance();
    private static final DcmObjectFactory objFact =
        DcmObjectFactory.getInstance();

    private final FileInfo fileInfo;
    private final byte[] buffer;

    public FileDataSource(FileInfo fileInfo, byte[] buffer)
    {
        this.fileInfo = fileInfo;
        this.buffer = buffer;
    }

    public void writeTo(OutputStream out, String tsUID) throws IOException
    {
        DcmEncodeParam enc = DcmEncodeParam.valueOf(tsUID);
        File file = fileInfo.toFile();
        FileInputStream fis = new FileInputStream(file);
        try
        {
            BufferedInputStream bis = new BufferedInputStream(fis);
            DcmParser parser = parserFact.newDcmParser(bis);
            Dataset ds = objFact.newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            updateAttrs(ds, fileInfo.patAttrs);
            updateAttrs(ds, fileInfo.studyAttrs);
            updateAttrs(ds, fileInfo.seriesAttrs);
            updateAttrs(ds, fileInfo.instAttrs);
            ds.writeDataset(out, enc);
            if (parser.getReadTag() == Tags.PixelData)
            {
                ds.writeHeader(
                    out,
                    enc,
                    Tags.PixelData,
                    parser.getReadVR(),
                    parser.getReadLength());
                if (parser.getReadLength() == -1)
                {
                    parser.parseHeader();
                    while (parser.getReadTag() == Tags.Item)
                    {
                        ds.writeHeader(
                            out,
                            enc,
                            Tags.Item,
                            VRs.NONE,
                            parser.getReadLength());
                        copy(bis, out, parser.getReadLength());
                        parser.parseHeader();
                    }
                    ds.writeHeader(
                        out,
                        enc,
                        Tags.SeqDelimitationItem,
                        VRs.NONE,
                        0);
                } else
                {
                    copy(bis, out, parser.getReadLength());
                }
                ds.clear();
                parser.parseDataset(parser.getDcmDecodeParam(), -1);
                ds.writeDataset(out, enc);
            }
        } finally
        {
            try
            {
                fis.close();
            } catch (IOException ignore)
            {}
            if (enc.encapsulated)
            {
                file.delete();
            }
        }

    }

    private void copy(InputStream bis, OutputStream out, int totLen)
        throws IOException
    {
        for (int len, toRead = totLen; toRead > 0; toRead -= len)
        {
            len = bis.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1)
            {
                throw new EOFException();
            }
            out.write(buffer, 0, len);
        }
    }

    /**
	 * @param ds
	 */
    private void updateAttrs(Dataset ds, byte[] attrs) throws IOException
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(attrs);
        DcmParser parser = parserFact.newDcmParser(bis);
        parser.setDcmHandler(ds.getDcmHandler());
        parser.parseDataset(DcmDecodeParam.EVR_LE, -1);
        bis.close();
    }

}
