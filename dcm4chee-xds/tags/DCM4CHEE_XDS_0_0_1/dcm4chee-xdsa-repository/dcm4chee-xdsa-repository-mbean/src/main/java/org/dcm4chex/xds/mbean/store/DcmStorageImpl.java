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

import java.io.File;
import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.soap.AttachmentPart;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.XDSDocumentMetadata;
import org.jboss.mx.util.MBeanServerLocator;
import org.w3c.dom.Document;

public class DcmStorageImpl implements Storage {

	private ObjectName store2DcmServicename;

    private static MBeanServer server = MBeanServerLocator.locate();
    private static Logger log = Logger.getLogger(DcmStorageImpl.class.getName());

    private static DcmStorageImpl singleton;
    
    private DcmStorageImpl() {
    }
    
    public static final DcmStorageImpl getInstance() {
    	if ( singleton == null )
    		singleton = new DcmStorageImpl();
    	return singleton;
    }
	/**
	 * @return Returns the ridServiceName.
	 */
	public ObjectName getStore2DcmServicename() {
		return store2DcmServicename;
	}
	/**
	 * @param ridServiceName The ridServiceName to set.
	 */
	public void setStore2DcmServicename(ObjectName name) {
		this.store2DcmServicename = name;
	}

	public File get(String uid) throws IOException {
		return null;
	}

	public StoredDocument store(String uid, AttachmentPart part, XDSDocumentMetadata metadata) throws IOException {
		if ( uidExists(uid) ) {
			throw new IOException("Store Document failed! UID already exist!");
		}
		byte[] hash = null;
		long size = -1;
		try {
			//Document doc = metadata.getMetadata().getOwnerDocument();
			DOMSource metadataSrc = new DOMSource(metadata.getMetadata());
			hash = storeAttachment( uid, part, metadataSrc );
			size = (long)part.getSize();
		} catch ( Throwable t ) {
			throw (IOException) new IOException("Store document (uid:"+uid+") failed! Reason:"+t.getMessage()).initCause(t);
		}
		if ( hash == null )
			throw new IOException("Store document (uid:"+uid+") failed!");
		return new StoredDocumentAsDcm(uid, size, hash, true);
	}

	private boolean uidExists(String uid) {
		return false;
	}
	private byte[] storeAttachment( String uid, AttachmentPart part, Source metadata ) throws IOException {
        try {
            return (byte[]) server.invoke(store2DcmServicename,
                    "storeDocument",
                    new Object[] { part, metadata },
                    new String[] { AttachmentPart.class.getName(), Source.class.getName() });
        } catch (Exception e) {
            log.error("Failed to store Attachment for "+uid, e);
            return null;
        }
	}

}
