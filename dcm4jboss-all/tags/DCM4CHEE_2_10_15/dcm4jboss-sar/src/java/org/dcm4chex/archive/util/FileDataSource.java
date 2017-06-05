/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.FileImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 3188 $
 * @since 18.09.2003
 */
public class FileDataSource implements DataSource {

    private static final Logger log = Logger.getLogger(FileDataSource.class);;
    private final File file;
    private final Dataset mergeAttrs;
    private final byte[] buffer;
	
	/** if true use Dataset.writeFile instead of writeDataset */
	private boolean writeFile = false;
	private boolean withoutPixeldata = false;
	private boolean excludePrivate = false;

	// buffer == null => send no Pixeldata
    public FileDataSource(File file, Dataset mergeAttrs, byte[] buffer) {
        this.file = file;
        this.mergeAttrs = mergeAttrs;
        this.buffer = buffer;
    }
	/**
	 * @return Returns the writeFile.
	 */
	public final boolean isWriteFile() {
		return writeFile;
	}
	/**
	 * Set the write method (file or net).
	 * <p>
	 * If true, this datasource use writeFile instead of writeDataset. 
	 * Therefore the FileMetaInfo will be only written if writeFile is set to true explicitly!
	 * 
	 * @param writeFile The writeFile to set.
	 */
	public final void setWriteFile(boolean writeFile) {
		this.writeFile = writeFile;
	}
	
    public final boolean isWithoutPixeldata() {
		return withoutPixeldata;
	}
    
	public final void setWithoutPixeldata(boolean withoutPixelData) {
		this.withoutPixeldata = withoutPixelData;
	}
	
	public boolean isExcludePrivate() {
		return excludePrivate;
	}
    
	public void setExcludePrivate(boolean excludePrivate) {
		this.excludePrivate = excludePrivate;
	}
    
	public Dataset getMergeAttrs() {
		return mergeAttrs;
	}
    /**
     * 
     * @param out
     * @param tsUID
     * @param writeFile
     * @throws IOException
     */
    public void writeTo(OutputStream out, String tsUID) throws IOException {
        
        log.info("M-READ file:" + file);
        FileImageInputStream fiis = new FileImageInputStream(file);
        try {
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(fiis);
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(null, Tags.PixelData);
            if (!parser.hasSeenEOF() && parser.getReadTag() != Tags.PixelData) {
                parser.unreadHeader();
                parser.parseDataset(parser.getDcmDecodeParam(), -1);
            }
            ds.putAll(mergeAttrs);
            String tsOrig = DecompressCmd.getTransferSyntax(ds);
            if ( writeFile) {
            	if ( tsUID != null ) {
            		if ( tsUID.equals( UIDs.ExplicitVRLittleEndian) || ! tsUID.equals( tsOrig ) ) { //can only decompress here!
            			tsUID = UIDs.ExplicitVRLittleEndian;
            			ds.setFileMetaInfo( DcmObjectFactory.getInstance().newFileMetaInfo(ds, tsUID));
            		}
            	} else {
            		tsUID = tsOrig;
            	}
            }
            DcmEncodeParam enc = DcmEncodeParam.valueOf(tsUID);
            if (withoutPixeldata || parser.getReadTag() != Tags.PixelData) {
				log.debug("Dataset:\n");
				log.debug(ds);
				write(ds, out, enc);
                return;                
            }
            int len = parser.getReadLength();
            if (len == -1 && !enc.encapsulated) {
                DecompressCmd cmd = new DecompressCmd(ds, tsOrig, parser);
                len = cmd.getPixelDataLength();
				log.debug("Dataset:\n");
				log.debug(ds);
				write(ds, out, enc);
                ds.writeHeader(out, enc, Tags.PixelData, VRs.OW, (len+1)&~1);
                try {
	                cmd.decompress(enc.byteOrder, out);
				} catch (IOException e) {
				    throw e;
				} catch (Throwable e) {
				    throw new RuntimeException("Decompression failed:", e);
				}
				if ((len&1)!=0)
                    out.write(0);
            } else {
                log.debug("Dataset:\n");
                log.debug(ds);
                write(ds, out, enc);
                if (len == -1) {
                    ds.writeHeader(out, enc, Tags.PixelData, VRs.OB, len);
                    parser.parseHeader();
                    int itemlen;
                    while (parser.getReadTag() == Tags.Item) {
                        itemlen = parser.getReadLength();
                        ds.writeHeader(out, enc, Tags.Item, VRs.NONE, itemlen);
                        copy(fiis, out, itemlen, buffer);
                        parser.parseHeader();
                    }
                    ds.writeHeader(out, enc, Tags.SeqDelimitationItem,
                            VRs.NONE, 0);
                } else {
                    ds.writeHeader(out, enc, Tags.PixelData, VRs.OW, len);
                    copy(fiis, out, len, buffer);
                }
            }
            parser.parseDataset(parser.getDcmDecodeParam(), -1);
            ds.subSet(Tags.PixelData, -1).writeDataset(out, enc);
        } finally {
            try {
                fiis.close();
            } catch (IOException ignore) {
            }
        }
    }
    
    private void write(Dataset ds, OutputStream out, DcmEncodeParam enc) throws IOException {
		if ( writeFile ) {
			if ( excludePrivate ) {
				Dataset dsOut = ds.excludePrivate();
				dsOut.setFileMetaInfo(ds.getFileMetaInfo());
				dsOut.writeFile(out,enc); 
			} else { 
				ds.writeFile(out, enc);
			}
		} else {
			if ( excludePrivate ) 
				ds.excludePrivate().writeDataset(out,enc); 
			else 
				ds.writeDataset(out, enc);
		}
        return;                
    	
    }

    private void copy(FileImageInputStream fiis, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = fiis.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) { throw new EOFException(); }
            out.write(buffer, 0, len);
        }
    }
}
