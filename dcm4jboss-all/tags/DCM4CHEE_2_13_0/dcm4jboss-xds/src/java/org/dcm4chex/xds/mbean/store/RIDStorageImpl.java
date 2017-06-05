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
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.XDSDocumentMetadata;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Use RID Service from dcm4chee to store XDS files.
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 5474 $ $Date: 2007-11-21 16:41:28 +0800 (周三, 21 11月 2007) $
 * @since Mar 08, 2006
 */
public class RIDStorageImpl implements Storage {

	private static final int BUFFER_SIZE = 65535;

	private ObjectName ridServiceName;
	
    private static MBeanServer server = MBeanServerLocator.locate();;
	private static Logger log = Logger.getLogger(RIDStorageImpl.class.getName());

	private static RIDStorageImpl singleton;
	
	private RIDStorageImpl(){
	}
	
	public static RIDStorageImpl getInstance() {
		if ( singleton == null ) singleton = new RIDStorageImpl();
		return singleton;
	}
	
	/**
	 * @return Returns the ridServiceName.
	 */
	public ObjectName getRidServiceName() {
		return ridServiceName;
	}
	/**
	 * @param ridServiceName The ridServiceName to set.
	 */
	public void setRidServiceName(ObjectName ridServiceName) {
		this.ridServiceName = ridServiceName;
	}
	
	
	public StoredDocument store(String uid, AttachmentPart part, XDSDocumentMetadata metadata) throws IOException {
		File docFile = getDocFile( uid, part.getContentType() );
		if ( docFile == null ) return null;
		if ( docFile.exists() ) {
			// TODO: check hash ; ignore or response a Failure if hash is different
			throw new IOException("Store Document failed! Object UID already exist!");
		}
		if ( ! docFile.getParentFile().exists()) docFile.getParentFile().mkdirs();
		byte[] hash = null;
		try {
			hash = saveAttachment( part, docFile );
		} catch ( Throwable t ) {
			throw (IOException) new IOException("Store document (uid:"+uid+") failed! Reason:"+t.getMessage()).initCause(t);
		}
		return new StoredDocumentAsFile(docFile, hash);
	}

	public File get(String uid) {
		return null;
	}
	
	
	private File getDocFile( String uid, String contentType ) throws IOException {
        try {
            return (File) server.invoke(this.ridServiceName,
                    "getDocumentFile",
                    new Object[] { uid, contentType },
                    new String[] { String.class.getName(), String.class.getName() });
        } catch (Exception e) {
            log.error("Failed to get Document File for:"+uid, e);
            return null;
        }
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
					is.close();
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
