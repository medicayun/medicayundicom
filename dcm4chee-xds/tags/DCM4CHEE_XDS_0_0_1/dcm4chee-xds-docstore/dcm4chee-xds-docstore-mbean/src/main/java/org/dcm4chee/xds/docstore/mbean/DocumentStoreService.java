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
package org.dcm4chee.xds.docstore.mbean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.store.BasicXDSDocument;
import org.dcm4chee.xds.common.store.StoredDocument;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentIdentifier;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.docstore.spi.XDSDocumentStorage;
import org.dcm4chee.xds.docstore.spi.XDSDocumentStorageRegistry;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 11, 2008
 */
public class DocumentStoreService extends ServiceMBeanSupport {
	
	private static final char[] HEX_STRINGS = "0123456789abcdef".toCharArray();

	private XDSDocumentStorageRegistry storageRegistry;
	private XDSDocumentStorage documentStorageBeforeRegister;
	private Logger log = LoggerFactory.getLogger(DocumentStoreService.class);
	private byte[] buf = new byte[65535];
	
	public DocumentStoreService () {
		storageRegistry = new XDSDocumentStorageRegistry();
	}
	
	public String getDocumentStorageBeforeRegister() {
		if (documentStorageBeforeRegister == null ) {
			setDocumentStorageBeforeRegister("XDSFileStorage");
		}
		return documentStorageBeforeRegister.getName();
	}
	
	public void setDocumentStorageBeforeRegister(String name) {
		XDSDocumentStorage docStore = storageRegistry.getXDSDocumentStorage(name);
		if ( docStore == null)
			throw new IllegalArgumentException("Unknown XDSDocumentStorage! name:"+name);
		documentStorageBeforeRegister = docStore;
	}
	
	
	public StoredDocument storeDocument(XDSDocument xdsDoc) throws XDSException {
		log.info("#### Store Document:"+xdsDoc.getDocumentUID());
		if (documentStorageBeforeRegister == null) {
			throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,"Configuration error! No DocumentStorage set (DocumentStorageBeforeRegister)",null);
		}
		return documentStorageBeforeRegister.storeDocument(xdsDoc);
	}

	public BasicXDSDocument retrieveDocument(String docUid) throws IOException {
		log.info("#### Retrieve Document:"+docUid);
		return documentStorageBeforeRegister.retrieveDocument(docUid);
	}
	
	public boolean documentExists(String docUid){
		log.info("#### Document Exists?:"+docUid);
		return false;
	}

	public boolean commitDocuments(Collection storedDocuments) {
		log.info("#### Commit Documents:"+storedDocuments);
		return true;
	}
	public boolean rollbackDocuments(Collection storedDocuments) {
		log.info("#### Rollback Documents:"+storedDocuments);
		if ( storedDocuments == null || storedDocuments.size() < 1 ) 
			return true;
		XDSDocumentIdentifier doc;
		boolean success = true;
		for ( Iterator iter = storedDocuments.iterator() ; iter.hasNext() ; ) {
			doc = (StoredDocument) iter.next();
			log.debug("Delete XDSDocument:"+doc);
			success = success & documentStorageBeforeRegister.deleteDocument(doc);
		}
		return success;
	}
	
	public Set listDocumentStorageProvider() {
		return storageRegistry.getAllXDSDocumentStorageProviderNames();
	}
	public String computeHash(String filename) throws NoSuchAlgorithmException, IOException {
		FileInputStream fis = new FileInputStream(new File(filename));
		MessageDigest md  = MessageDigest.getInstance("SHA1");
        DigestInputStream dis = new DigestInputStream(fis, md);
        //BufferedInputStream bis = new BufferedInputStream(dis);
        while (dis.read(buf) != -1);
		String hash = toHexString( md.digest() );
		log.info("SHA1 read digest:"+hash);
		return hash;
	}
	public String computeHash(String filename, String alg) throws NoSuchAlgorithmException, IOException {
		FileInputStream fis = new FileInputStream(new File(filename));
		if ( alg == null || alg.trim().length() < 1)
			alg = "SHA1";
		MessageDigest md  = MessageDigest.getInstance(alg);
		DigestOutputStream dos = new DigestOutputStream(new OutputStream(){
			@Override
			public void write(int b) throws IOException {}}, md);
		int len;
		int size = 0;
		while ( (len = fis.read(buf)) > 0 ) {
			dos.write(buf, 0, len);
			size += len;
		}
		String hash = toHexString( md.digest() );
		dos.close();
		log.info("SHA1 write digest (alg:"+alg+"):"+hash);
		return hash;
	}
	private String toHexString(byte[] hash) {
		StringBuffer sb = new StringBuffer();
		int h;
   		for(int i=0 ; i < hash.length ; i++) {
   			h = hash[i] & 0xff;
        	sb.append(HEX_STRINGS[h>>>4]);
        	sb.append(HEX_STRINGS[h&0x0f]);
    	}
   		return sb.toString();
	}
}
