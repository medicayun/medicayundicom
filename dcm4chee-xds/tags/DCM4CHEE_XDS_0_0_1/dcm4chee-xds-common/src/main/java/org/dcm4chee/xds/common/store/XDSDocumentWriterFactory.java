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

package org.dcm4chee.xds.common.store;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDSDocumentWriterFactory {

    private int bufferSize = 8192;

    private static XDSDocumentWriterFactory singleton;
    
    private static Logger log = LoggerFactory.getLogger(XDSDocumentWriter.class);
    
    private XDSDocumentWriterFactory() {
    }
    
    public static XDSDocumentWriterFactory getInstance() {
        if ( singleton == null )
            singleton = new XDSDocumentWriterFactory();
        return singleton;
    }
    
    public XDSDocumentWriter getDocumentWriter(File f) throws IOException {
        long fileSize = f.length();
        if ( fileSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("File is to large! "+fileSize+" exceeds maximum of "+Integer.MAX_VALUE+" bytes");
        }
        FileDataSource ds = new FileDataSource(f);
        return new DataHandlerWriter(new DataHandler(ds));
    }
    
    public XDSDocumentWriter getDocumentWriter(InputStream inputStream) throws IOException {
        InputStreamDataSource ds = new InputStreamDataSource(inputStream);
        return new DataHandlerWriter(new DataHandler(ds));
    }
    
    public XDSDocumentWriter getDocumentWriter(AttachmentPart part) throws SOAPException, IOException {
    	if ( log.isDebugEnabled()) {
    		log.debug("part datahandler:"+part.getDataHandler().getClass().getName());
    		log.debug("part contentType:"+part.getContentType());
    	}
    	if ( "application/dicom".equalsIgnoreCase(part.getContentType() ) ) { //ImageIO isn't the right handler to handle basic DICOM objects like manifest KOS
    		InputStream is = part.getDataHandler().getInputStream();
    		return new CachedWriter( is );
    	}
        Object content = part.getContent();
        log.debug("part:"+part.getClass().getName());
        log.debug("content:"+content.getClass().getName());
        if ( content instanceof String ) {
            return new DataWriter(content.toString());
        } else if ( content instanceof StreamSource ) {
        	return new CachedWriter(((StreamSource) content).getInputStream());
        } else if ( content instanceof InputStream ) {
            return new CachedWriter( (InputStream) content );
        } else if ( content instanceof MimeMultipart ) {
            return new CachedWriter( (MimeMultipart) content );
        } else {
            throw new IllegalArgumentException("Cant get Writer for attachment! Reason: Unknown content:"+content.getClass().getName()+" contentType:"+part.getContentType());
        }
    }
    
    public XDSDocumentWriter getDocumentWriter(String s) throws IOException {
        return new DataWriter(s);
    }

    public XDSDocumentWriter getDocumentWriter(byte[] data) throws IOException {
        return new DataWriter(data);
    }
    
    public XDSDocumentWriter getDocumentWriter(DataHandler dh) throws IOException {
        return new DataHandlerWriter(dh);
    }
    
    // TODO: remove if not needed
    class CachedWriter implements XDSDocumentWriter {
    	public static final int MAX_MEMORY_CACHESIZE = 1<<26; //64 MBytes
        private byte[] buffer;
        private File cacheFile;
        private boolean deleteCacheFileOnClose = true;

        public CachedWriter( InputStream is ) throws IOException {
        	this(is, MAX_MEMORY_CACHESIZE);
        }
        public CachedWriter( InputStream is, int maxMemory ) throws IOException {
        	if ( is.available() > maxMemory ) {
        		writeCacheFile(null, is);
        	} else {
        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        		buffer = new byte[bufferSize];
        		int r;
		        while ((r = is.read(buffer)) > 0) {
		        	log.debug("-----write bytes to memory cache:"+r+" is.available:"+is.available());
		            baos.write(buffer, 0, r);
		            if ( baos.size() > maxMemory ) {
		            	writeCacheFile(baos, is);
		            	baos = null;
		            	break;
		            }
		        }
		        if ( baos != null ) 
		        	buffer = baos.toByteArray();
        	}
        }
        
        public CachedWriter( MimeMultipart mmp ) throws IOException {
        	BufferedOutputStream bos = null;
        	try {
				cacheFile = File.createTempFile("xds_cache", "tmp");
				bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
		        mmp.writeTo(bos);
        	} catch (MessagingException x) {
				log.error("Cant cache MimeMultipart!",x);
				throw new IOException("Cant cache MimeMultipart!");
			} finally {
        		if ( bos != null )
        			try {
        				bos.close();
        			} catch ( Exception ignore ) {}
        	}
        }

        public CachedWriter( File f ) throws IOException {
        	cacheFile = f;
        	deleteCacheFileOnClose = false;
        }

        private void writeCacheFile(ByteArrayOutputStream baos, InputStream is) throws IOException {
        	FileOutputStream fos = null;
        	try {
				cacheFile = File.createTempFile("xds_cache", "tmp");
				fos = new FileOutputStream(cacheFile);
				if ( baos != null ) {
		        	log.debug("-----write bytes from memory cache to file:"+baos.size());
					fos.write(baos.toByteArray());
				}
				int r;
		        while ((r = is.read(buffer)) > 0) {
		        	log.debug("-----write bytes to file:"+r+" is.available:"+is.available());
		            fos.write(buffer, 0, r);
		        }
        	} finally {
        		if ( fos != null )
        			try {
        				fos.close();
        			} catch ( Exception ignore ) {}
        	}
			
		}
		public int size() {
            return cacheFile == null ? buffer.length : (int)cacheFile.length();
        }

        public void writeTo(OutputStream os) throws IOException {
        	long t1 = System.currentTimeMillis();
        	if ( cacheFile == null ) {
		        log.debug("-----start writing! using buffer:"+buffer.length);
        		os.write(buffer);
        	} else {
		        int r;
		        FileInputStream is = new FileInputStream( cacheFile);
		        buffer = new byte[bufferSize];
		        log.debug("-----start writing! using cacheFile "+cacheFile+" ("+cacheFile.length()+" bytes) bufferSize:"+bufferSize);
		        while ((r = is.read(buffer)) > 0) {
		        	log.debug("-----write bytes:"+r+" is.available:"+is.available());
		            os.write(buffer, 0, r);
		        }
        	}
	        log.debug("-----finished writing after "+(System.currentTimeMillis() - t1)+ "ms");
        }

		public void close() throws IOException {
			if ( cacheFile != null && deleteCacheFileOnClose ) {
				cacheFile.delete();
			}
		}
		
        public DataHandler getDataHandler() {
        	return null;
        } 		
    }
    
    // TODO: remove if not needed
    class DataWriter implements XDSDocumentWriter {
        byte[] data;
        DataWriter( String s ) {
            this.data = s.getBytes();
        }
        DataWriter( byte[] data ) {
            this.data = data;
        }
        public void writeTo(OutputStream os) throws IOException {
        	log.info("@@@@@ Write data:"+data);
            os.write( data );
        }
        public void close() throws IOException {
        }
		public int size() {
			return data.length;
		}
        public DataHandler getDataHandler() {
        	return null;
        }
    }
    
    class DataHandlerWriter implements XDSDocumentWriter {
        DataHandler dh;
        DataHandlerWriter( DataHandler dh ){
            this.dh = dh;
        }
        public void writeTo(OutputStream os) throws IOException {
            dh.writeTo(os);
        }
        public void close() throws IOException {
        }
        public int size() {
            // TODO: remove size() from XDSDocumentWriter interface if only used for logging purposes  
            return 9999;
        }
        public DataHandler getDataHandler() {
        	return dh;
        }        
    }

}
