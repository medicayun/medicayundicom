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
package org.dcm4chee.docstore;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;
import org.dcm4chee.docstore.spi.DocumentStorage;
/**
 * 
 * @author franz.willer@gmail.com
 * @version $Revision:  $ $Date:  $
 * @since 17.06.2008
 */
public class DocumentStore {
	private static final String DEFAULT_STORAGE = "SimpleFileStorage";
	private String name = "DefaultDocumentStore";
	private static HashMap<String, DocumentStore> singletons = new HashMap<String, DocumentStore>();
	
	private DocumentStorage writeDocStorage;
	private Set<DocumentStorage> retrieveDocStorages = new HashSet<DocumentStorage>();
	private DocumentStorageRegistry registry = new DocumentStorageRegistry();

	private static Logger log = Logger.getLogger( DocumentStore.class.getName() );
	
	private DocumentStore(String name) {
		//TODO configuration of DocumentStore for given name. 
		//Default is one SimpleFileStorage with given name for write and retrieve.
		writeDocStorage = registry.getDocumentStorage(DEFAULT_STORAGE, name);
		retrieveDocStorages.add(writeDocStorage);
	}
	
	/**
	 * Create a named DocumentStore instance.
	 * 
	 * @param name
	 * @return DocumentStore for given name. 
	 */
	public static final DocumentStore getInstance(String name) {
		DocumentStore store = singletons.get(name);
		if ( store == null) {
			store = new DocumentStore(name); 
			singletons.put(name, store);
		}
		return store;
	}

	/**
	 * Name of this DocumentStore instance.
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The DocumentStorage for creating documents.
	 * @return
	 */
	public DocumentStorage getWriteDocStorage() {
		return writeDocStorage;
	}

	/**
	 * List of DocumentStorage that are used for retrieve documents.
	 * @return
	 */
	public Set<DocumentStorage> getRetrieveDocStorages() {
		return retrieveDocStorages;
	}
	
	/**
	 * Get a document for given document UID and content type.
	 * If several Documents are found via the list of DocumentStorage (retrieveDocStorages) 
	 * the one with the best availability is returned.
	 * If <code>mime</code> is null no content type check is performed.
	 * 
	 * @param docUid
	 * @param mime
	 * @return
	 */
	public BaseDocument getDocument(String docUid, String mime) {
		DocumentStorage storage = null;
		BaseDocument doc, docFound = null;
		for ( Iterator<DocumentStorage> iter = retrieveDocStorages.iterator() ; iter.hasNext() ; ) {
			try {
				storage = iter.next();
				doc = (BaseDocument) storage.retrieveDocument(docUid);
				if (doc != null && (mime == null || mime.equals(doc.getMimeType())) ) {
					if ( docFound == null || doc.getAvailability().compareTo(docFound.getAvailability()) < 0 )
					docFound = doc;
				}
			} catch (IOException x) {
				log.error("Failed to retrieve from storage "+storage.getName()+"! Ignored. Try next:"+iter.hasNext(), x);
			}
		}
		return docFound;
	}
	
	/**
	 * Creates a new empty (place holder) document.
	 * This can be used to use the output stream of the document to write the content.
	 *  
	 * @param docUid
	 * @param mime
	 * @return
	 * @throws IOException
	 */
	public BaseDocument createDocument(String docUid, String mime) throws IOException {
		return writeDocStorage.createDocument(docUid, mime);
	}
	
	/**
	 * Store/Create a document with content given in Datahandler.
	 * 
	 * @param docUid
	 * @param dh
	 * @return
	 * @throws IOException
	 */
	public BaseDocument storeDocument(String docUid, DataHandler dh) throws IOException {
		return writeDocStorage.storeDocument(docUid, dh);
	}
	
	/**
	 * Delete document.
	 * Deletes the document with docUid from writeDocStorage.
	 * Documents stored on different DocumentStorage instances of the retrieve list are NOT deleted!
	 * 
	 * @param docUid
	 * @return
	 */
	public boolean deleteDocument(String docUid) {
		return this.writeDocStorage.deleteDocument(docUid);
	}
	
}
