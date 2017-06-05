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
package org.dcm4chex.xds.mbean.store;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2371 $ $Date: 2006-03-27 21:12:50 +0800 (周一, 27 3月 2006) $
 * @since Mar 08, 2006
 */
public class StorageImpl implements Storage {

	private static final String DEFAULT_ROOT = "xds_repository";
	private static final int[] DEFAULT_DIRECTORY_TREE = new int[]{307, 317, 331};
	private static final int BUFFER_SIZE = 65535;

	private File rootDir;
	private int[] directoryTree;
	
	private static Logger log = Logger.getLogger(StorageImpl.class.getName());

	private static StorageImpl singleton;
	
	private StorageImpl(){
	}
	
	public static StorageImpl getInstance() {
		if ( singleton == null ) singleton = new StorageImpl();
		return singleton;
	}
	/**
	 * @return Returns the baseDir.
	 */
	public File getRootDir() {
		if ( rootDir == null ) {
    		File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
			rootDir = new File(serverHomeDir, DEFAULT_ROOT);
		}
		return rootDir;
	}
	/**
	 * @param baseDir The baseDir to set.
	 */
	public void setRootDir(File newRoot) {
		this.rootDir = newRoot;
	}
	/**
	 * @return Returns the directoryTree.
	 */
	public int[] getDirectoryTree() {
		if ( directoryTree == null ) {
			directoryTree = DEFAULT_DIRECTORY_TREE;
		}
		return directoryTree;
	}
	/**
	 * @param directoryTree The directoryTree to set.
	 */
	public void setDirectoryTree(int[] directoryTree) {
		this.directoryTree = directoryTree;
	}
	public XDSFile store(String uid, AttachmentPart part) throws IOException {
		File docFile = getDocFile( uid );
		if ( docFile == null ) return null;
		byte[] hash = null;
		try {
			hash = saveAttachment( part, docFile );
		} catch ( Throwable t ) {
			throw (IOException) new IOException("Store document (uid:"+uid+") failed! Reason:"+t.getMessage()).initCause(t);
		}
		return new XDSFile(docFile, hash);
	}

	public File get(String uid) {
		return null;
	}
	
	
	private File getDocFile( String uid ) throws IOException {
		File file = getRootDir();
		if ( directoryTree != null ) {
			file = new File( file, getSubDirName( uid ) );
		}
		if ( !file.exists()) {
			if ( !file.mkdirs() ) {
				log.error("Cant create Directory:"+file+ "(uid:"+uid+")!");
				throw new IOException("Cant create directory "+file+" (uid:"+uid+")!");
			}
		}
		return new File( file, uid ); 
	}
	
	private String getSubDirName(String uid) {
		if ( getDirectoryTree() == null ) return uid;
		int hash = uid.hashCode();
		StringBuffer sb = new StringBuffer();
		int modulo;
		for ( int i = 0 ; i < directoryTree.length ; i++ ) {
			if ( directoryTree[i] == 0 ) {
				sb.append(Integer.toHexString(hash)).append(File.separatorChar);
			} else {
				modulo = hash % directoryTree[i];
				if ( modulo < 0 ) {
					modulo *= -1;
				}
				sb.append(modulo).append(File.separatorChar);
			}
		}
		return sb.toString();
	}

	private byte[] saveAttachment(AttachmentPart part, File docFile) throws SOAPException, IOException, MessagingException, NoSuchAlgorithmException {
		log.info("Save Attachment "+part.getContentId()+" (size="+part.getSize()+") to file "+docFile);
		Object content = part.getContent();
		BufferedOutputStream bos = null;
		MessageDigest md = null;
		try {
            md = MessageDigest.getInstance("SHA1");
            DigestOutputStream dos = new DigestOutputStream(new FileOutputStream(docFile), md);
            bos = new BufferedOutputStream(dos);
			if ( content instanceof String ) {
				dos.write(content.toString().getBytes());
			} else {
				if ( content instanceof StreamSource ) {
					content = ((StreamSource) content).getInputStream();
				}
				if ( content instanceof InputStream ) {
					InputStream is = (InputStream) content;
					byte[] buffer = new byte[BUFFER_SIZE];
					for ( int len; (len = is.read(buffer)) > 0; ) {
						dos.write(buffer, 0, len);
					}
				} else if ( content instanceof MimeMultipart ) {
					MimeMultipart mmp = (MimeMultipart) content;
					mmp.writeTo( dos );
				} else {
					throw new IllegalArgumentException("Unknown content:"+content.getClass().getName()+" contentType:"+part.getContentType());
				}
			}
		} finally {
			if ( bos != null ) bos.close();
		}
		return md == null ? null : md.digest();
	}
	
}
