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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final char[] HEX_STRINGS = "0123456789abcdef".toCharArray();

    private String name;
    private String domain;
    private static HashMap<String, DocumentStore> singletons = new HashMap<String, DocumentStore>();

    private Set<DocumentStorage> retrieveDocStorages = new HashSet<DocumentStorage>();
    private static DocumentStorageRegistry registry;

    private static Logger log = Logger.getLogger( DocumentStore.class.getName() );

    private DocumentStore(String name, String domain) {
        this.name = name;
        this.domain = domain == null ? name : domain;
        Collection<DocumentStorage> domainStores = registry.getDocumentStorages(domain);
        if ( log.isDebugEnabled() ) log.debug("Constructor: domainStores(domain="+domain+"):"+domainStores);
        if ( domainStores != null )
            retrieveDocStorages.addAll(domainStores);

    }

    /**
     * Create a named DocumentStore instance for a storage domain.
     * <p/>
     * When domain is not specified (null) the domain is set to name.
     * <p/>
     * Initialize the DocumentStorage registry with default configuration 
     * (resource:dcm4chee-docstore/dcm4chee-docstore-cfg.xml := &lt;DCM4CHEE&gt;/server/default/conf/dcm4chee-docstore/dcm4chee-docstore-cfg.xml)
     * 
     * @param name
     * @param domain
     * @return DocumentStore for given name and domain. 
     */
    public static final DocumentStore getInstance(String name, String domain) {
        return getInstance(name, domain, null);
    }

    /**
     * Create a named DocumentStore instance for a storage domain with additional Document Storage configuration.
     * <p/>
     * With <code>configUrl</code> you can add a custom DocumentStorage Configuration for to the default configuration.
     * <p/>
     * The additional configuration applies only once for each name!
     * <p/>
     * Note: Existing domain configurations will be replaced with the newer ones.
     * 
     * @param name		Name of this DocumentStore
     * @param domain	Domain of this DocumentStore
     * @param configUrl URL to additional DocumentStore Configuration.
     * @return DocumentStore for given name and domain. 
     */
    public static final DocumentStore getInstance(String name, String domain, String configUrl) {
        if ( registry == null ) {
            registry = new DocumentStorageRegistry();
            registry.config(null);
        }
        DocumentStore store = singletons.get(name);
        if ( store == null) {
            if ( configUrl != null ) {
                registry.config(configUrl);
            }
            store = new DocumentStore(name, domain); 
            singletons.put(name, store);
        }
        return store;
    }

    /**
     * Set List of Storages for retrieve according given features.
     * <p/>
     * <dl>
     * <dt>List of DocumentStorage for retrieve:</dt>
     * <dd> 1) Get all DocumentStorage objects that support all of given features (from all domains).</dd>
     * <dd> 2) Add all DocumentStorage objects for the domain of this DocumentStore instance. </dd>
     * </dl>
     * @param features
     */
    public void setRetrieveFeatures(Set<Feature> features) {
        retrieveDocStorages.clear();
        for ( Set<DocumentStorage> storages : registry.getDocumentStorages(features).values() ) {
            retrieveDocStorages.addAll(storages);
        }
        if ( log.isDebugEnabled() ) log.debug("retrieveDocStorages(features="+features+"):"+retrieveDocStorages);
        Collection<DocumentStorage> domainStores = registry.getDocumentStorages(domain);
        if ( log.isDebugEnabled() ) log.debug("setRetrieveFeatures: domainStores(domain="+domain+"):"+domainStores);
        if ( domainStores != null )
            retrieveDocStorages.addAll(domainStores);
        if ( log.isDebugEnabled() ) log.debug("resulting retrieveDocStorages:"+retrieveDocStorages);
    }

    /**
     * Name of this DocumentStore instance.
     * @return Name
     */
    public String getName() {
        return name;
    }
    /**
     * Storage Domain of this DocumentStore.
     * @return
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Get a DocumentStorage of a named pool.
     * <p/>
     * Return the document storage with the best Storage Availability of the given pool.
     * 
     * @param pool Name of a storage pool.
     * @return A DocumentStorage or null if the pool doesn't exist.
     */
    public DocumentStorage getDocStorageFromPool(String pool) {
        return pool == null ? null : selectStorageByAvailability(registry.getDocumentStoragesOfPool(pool));
    }
    /**
     * Select a DocumentStorage from a named pool or from the domain when pool does not exist.
     * <p/>
     * Return the document storage with the best Storage Availability of the given pool.
     * 
     * @param pool Name of a storage pool.
     * @return A DocumentStorage or null if the pool doesn't exist.
     */
    public DocumentStorage selectDocStorageFromPoolOrDomain(String pool) {
        Collection<DocumentStorage> c = null;
        if ( pool != null ) {
            c = registry.getDocumentStoragesOfPool(pool);
        } 
        if ( c == null || c.isEmpty() ) {
            c = registry.getDocumentStorages(domain);
        }
        return selectStorageByAvailability(c);
    }

    /**
     * Return the document storage with the best Storage Availability of the given collection.
     * 
     * @param storages Collection of DocumentStorages objects.
     * @return A DocumentStorage or null if the collection is null or empty.
     */
    public DocumentStorage selectStorageByAvailability(Collection<DocumentStorage> storages) {
        if ( storages == null || storages.size() < 1 ) return null;
        DocumentStorage storage = null;
        for ( DocumentStorage st : storages  ) {
            if ( storage == null) {
                storage = st;
            } else if (st.getStorageAvailability().compareTo(storage.getStorageAvailability() ) < 0 ) {
                storage = st;
            }
        }
        if (log.isDebugEnabled()) log.debug("selected Storage:"+storage);
        return storage;
    }

    /**
     * List of DocumentStorage that are used for retrieve documents.
     * @return
     */
    public Set<DocumentStorage> getRetrieveDocStorages() {
        return retrieveDocStorages;
    }

    /**
     * The DocumentStorage for creating documents.
     * @return
     */
    public DocumentStorage getNamedDocStorage(String storageName) {
        return registry.getDocumentStorage(this.domain, storageName);
    }

    public Availability getAvailability(String docUid) {
        log.debug("getAvailability docUid"+docUid);
        Availability availability, bestAvailability = Availability.NONEEXISTENT;
        for ( DocumentStorage storage : retrieveDocStorages ) {
            availability = storage.getAvailabilty(docUid);
            if ( availability.compareTo(bestAvailability) < 0 ) {
                bestAvailability = availability;
            }
        }
        return bestAvailability;
    }
    
    /**
     * Get a document for given document UID and content type.
     * <p/>
     * If several Documents are found via the list of DocumentStorage (retrieveDocStorages) 
     * the one with the best availability is returned.
     * If <code>mime</code> is null no content type check is performed.
     * 
     * @param docUid
     * @param mime
     * @return
     */
    public BaseDocument getDocument(String docUid, String mime) {
        log.debug("getDocument docUid"+docUid+" mime:"+mime);
        BaseDocument doc, docFound = null;
        for ( DocumentStorage storage : retrieveDocStorages ) {
            try {
                doc = (BaseDocument) storage.retrieveDocument(docUid, mime);
                if (doc != null ) {
                    log.debug("Document found in "+doc+"! availability:"+doc.getAvailability());
                    if ( docFound == null || doc.getAvailability().compareTo(docFound.getAvailability()) < 0 )
                        docFound = doc;
                }
            } catch (IOException x) {
                log.error("Failed to retrieve from storage "+storage.getName()+"! Ignored.", x);
            }
        }
        return docFound;
    }

    /**
     * Creates a new empty (place holder) document.
     * <p/>
     * This can be used to use the output stream of the document to write the content.
     *  
     * @param docUid
     * @param mime
     * @return
     * @throws IOException
     */
    public BaseDocument createDocument(String docUid, String mime) throws IOException {
        return createDocument(null,docUid, mime);
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
        return storeDocument(null,docUid, dh);
    }

    /**
     * Delete document from this storage domain.
     * <p/>
     * Deletes the document with docUid from all DocumentStorages of this domain.
     * 
     * @param docUid
     * @return
     */
    public boolean deleteDocument(String docUid) {
        Collection<DocumentStorage> c = registry.getDocumentStorages(domain);
        if ( c == null ) {
            log.warn("Storage domain '"+domain+"' does not exist!");
            return false;
        }
        return deleteDocumentfromStorages(docUid, c);
    }

    /**
     * Creates a new empty (place holder) document in given storage pool.
     * <p/>
     * This can be used to use the output stream of the document to write the content.
     *  
     * @param docUid
     * @param mime
     * @return
     * @throws IOException
     */
    public BaseDocument createDocument(String pool, String docUid, String mime) throws IOException {
        return selectDocStorageFromPoolOrDomain(pool).createDocument(docUid, mime);
    }

    /**
     * Store/Create a document in given storage pool with content given in Datahandler.
     * 
     * @param docUid
     * @param dh
     * @return
     * @throws IOException
     */
    public BaseDocument storeDocument(String pool, String docUid, DataHandler dh) throws IOException {
        return selectDocStorageFromPoolOrDomain(pool).storeDocument(docUid, dh);
    }

    /**
     * Delete document.
     * Deletes the document with docUid from given pool.
     * Documents stored on other pools are NOT deleted!
     * 
     * @param docUid
     * @return true if at least one document is deleted from given pool
     */
    public boolean commitDocument(String pool, String docUid) {
        Collection<DocumentStorage> c = registry.getDocumentStoragesOfPool(pool);
        if ( c == null ) {
            log.warn("Storage pool '"+pool+"' does not exist!");
            return false;
        }
        return commitDocumentfromStorages(docUid, c);
    }

    /**
     * Delete document.
     * Deletes the document with docUid from given pool.
     * Documents stored on other pools are NOT deleted!
     * 
     * @param docUid
     * @return true if at least one document is deleted from given pool
     */
    public boolean deleteDocument(String pool, String docUid) {
        Collection<DocumentStorage> c = registry.getDocumentStoragesOfPool(pool);
        if ( c == null ) {
            log.warn("Storage pool '"+pool+"' does not exist!");
            return false;
        }
        return deleteDocumentfromStorages(docUid, c);
    }

    private boolean deleteDocumentfromStorages(String docUid, Collection<DocumentStorage> c) {
        boolean deleted = false;
        for ( DocumentStorage st : c ) {
            deleted |= st.deleteDocument(docUid);
        }
        return deleted;
    }
    public boolean commitDocumentfromStorages(String docUid, Collection<DocumentStorage> c) {
        boolean commited = false;
        for ( DocumentStorage st : c ) {
            commited |= st.commitDocument(docUid);
        }
        return commited;
    }

    public static final String toHexString(byte[] hash) {
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
