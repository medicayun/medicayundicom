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

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.imageio.stream.ImageInputStream;

/** Implementation of <code>Dataset</code> container objects.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since March 2002
 * @version $Revision: 3863 $ $Date: 2005-02-11 22:09:53 +0800 (周五, 11 2月 2005) $
 * @see "DICOM Part 5: Data Structures and Encoding, 7. The Data Set"
 */
final class DatasetImpl extends BaseDatasetImpl
        implements org.dcm4che.data.Dataset {

    private final Dataset parent;
    private Charset charset = null;
    private String privateCreatorID = null;
    private long itemOffset = -1L;
    DatasetImpl() {
        this(null);
    }
    
    DatasetImpl(Dataset parent) {
        this.parent = parent;
    }

    public void setPrivateCreatorID(String privateCreatorID) {
        this.privateCreatorID = privateCreatorID;
    }       

    public String getPrivateCreatorID() {
        return privateCreatorID != null ? privateCreatorID
                : parent != null ? parent.getPrivateCreatorID() : null;
    }
    
    public Charset getCharset() {
        return charset != null ? charset
                : parent != null ? parent.getCharset() : null;
    }

    public final Dataset getParent() {
        return parent;
    }

    public final Dataset setItemOffset(long itemOffset) {
        this.itemOffset = itemOffset;
        return this;
    }

    public final long getItemOffset() {
        if (itemOffset != -1L || list.isEmpty())
            return itemOffset;
        
        long elm1pos = ((DcmElement)list.get(0)).getStreamPosition();
        return elm1pos == -1L ? -1L : elm1pos - 8L;
    }

    public DcmElement putSQ(int tag) {
        return put(new SQElement(tag, this));
    }

    protected DcmElement put(DcmElement newElem) {
        if ((newElem.tag() >>> 16) < 4) {
            log.warn("Ignore illegal attribute " + newElem);
            return newElem;
        }
        if (newElem.tag() == Tags.SpecificCharacterSet) {
            try {
                this.charset = Charsets.lookup(newElem.getStrings(null));
            } catch (Exception ex) {
                log.warn("Failed to consider specified Charset!");
                this.charset = null;
            }
        }

        return super.put(newElem);
    }
    
    public DcmElement remove(int tag) {
        if (tag == Tags.SpecificCharacterSet)
            charset = null;
        return super.remove(tag);
    }
    
    public void clear() {
        super.clear();
        charset = null;
        totLen = 0;
    }
        
    public void readFile(InputStream in, FileFormat format, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDcmFile(format, stopTag);
    }
    
    public void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDataset(param, stopTag);
    }

    public void readFile(ImageInputStream in, FileFormat format, int stopTag)
            throws IOException, DcmValueException {
        DcmParserImpl parser = new DcmParserImpl(in);
        parser.setDcmHandler(getDcmHandler());
        parser.parseDcmFile(format, stopTag);
    }

    public void readFile(File f, FileFormat format, int stopTag)
    	throws IOException
	{
	    InputStream in = new BufferedInputStream(new FileInputStream(f));
	    try {
	        readFile(in, format, stopTag);
	    } finally {
	        try { in.close(); } catch (IOException ignore) {}
	    }
	}
    
}
