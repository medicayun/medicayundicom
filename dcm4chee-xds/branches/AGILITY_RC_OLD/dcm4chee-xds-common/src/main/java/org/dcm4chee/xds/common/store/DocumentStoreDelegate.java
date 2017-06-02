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
 * Franz Willer <franz.willer@gmail.com>
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

import java.util.Collection;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.transform.Source;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentStoreDelegate {

	private ObjectName documentStoreService;
	private static MBeanServer server = MBeanServerLocator.locate();

	private Logger log = LoggerFactory.getLogger(DocumentStoreDelegate.class);
			
	public ObjectName getDocumentStoreService() {
		return documentStoreService;
	}

	public void setDocumentStoreService(ObjectName documentStoreService) {
		this.documentStoreService = documentStoreService;
	}
	
	public XDSDocument storeDocument(XDSDocument xdsDoc) throws XDSException {
		return storeDocument(xdsDoc,null);
	}

	/**
	 * @param storedDocuments
	 */
	public boolean commitDocuments(Collection storedDocuments) {
		log.info("**** Commit Documents after 'Register Document Set'! :"+storedDocuments);
		try {
	        return ((Boolean) server.invoke(documentStoreService,
	                "commitDocuments",
	                new Object[] { storedDocuments },
	                new String[] { Collection.class.getName() } ) ).booleanValue();
		} catch ( Exception x ) {
			log.error("Commit of document storage failed!");
			return false;
		}
	}
	
	/**
	 * @param storedDocuments
	 */
	public boolean rollbackDocuments(Collection storedDocuments) {
		log.info("**** Rollback Documents after failed 'Provide and Register Document Set'! :"+storedDocuments);
		try {
			return ((Boolean) server.invoke(documentStoreService,
	                "rollbackDocuments",
	                new Object[] { storedDocuments },
	                new String[] { Collection.class.getName() } )).booleanValue();
		} catch ( Exception x ) {
			log.error("Rollback of document storage failed!",x);
			return false;
		}
	}
	
	public XDSDocument retrieveDocument(String docUid, String mime) throws XDSException {
		log.info("#### Retrieve Document:"+docUid);
		try {
	        return (XDSDocument) server.invoke(documentStoreService,
	                "retrieveDocument",
	                new Object[] { docUid, mime },
	                new String[] { String.class.getName(), String.class.getName() } );
		} catch ( Exception x ) {
			log.error( "Exception occured in retrieveDocument!", x );
			throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,"Retrieve Document Failed! document UID:"+docUid,x);
		}
	}

	public XDSDocument storeDocument(XDSDocument xdsDoc, Source metadata) throws XDSException {
		log.info("storeDocument xdsDoc:"+xdsDoc+" metadata:"+metadata);
		try {
	        return (XDSDocument) server.invoke(documentStoreService,
	                "storeDocument",
	                new Object[] { xdsDoc, metadata },
	                new String[] { XDSDocument.class.getName(), Source.class.getName() } );
		} catch ( Exception x ) {
			if ( x instanceof XDSException ) {
				throw (XDSException)x;
			} else if ( x.getCause() instanceof XDSException ){
				throw (XDSException) x.getCause();
			} else {
				log.error( "Exception occured in storeDocument!", x );
				throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,"Store Document Failed!",x);
			}
		}
	}	
}
