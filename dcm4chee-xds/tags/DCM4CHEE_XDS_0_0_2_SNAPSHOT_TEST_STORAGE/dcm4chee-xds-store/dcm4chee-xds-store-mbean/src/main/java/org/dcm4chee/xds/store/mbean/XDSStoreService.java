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
package org.dcm4chee.xds.store.mbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4chee.docstore.Availability;
import org.dcm4chee.docstore.BaseDocument;
import org.dcm4chee.docstore.DocumentStore;
import org.dcm4chee.docstore.Feature;
import org.dcm4chee.docstore.spi.DocumentStorage;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 11, 2008
 */
public class XDSStoreService extends ServiceMBeanSupport {

    private static final String MIME_METADATA = "application/metadata+xml";

    private static final int BUFFER_SIZE = 65535;

    private static final String NONE = "NONE";

    private DocumentStore docStore;

    private String storeBeforeRegisterPool;
    private String storeAfterRegisterPool;
    private Logger log = LoggerFactory.getLogger(XDSStoreService.class);
    private byte[] buf = new byte[65535];

    private boolean storeMetadata;
    private boolean ignoreMetadataPersistenceErrors;
    private boolean forceMetadataPersistence;
    private String metadataStoragePool;

    public XDSStoreService () {
    }

    public String getStoreBeforeRegisterPool() {
        return storeBeforeRegisterPool;
    }

    public void setStoreBeforeRegisterPool(String poolName) {
        storeBeforeRegisterPool = poolName;
    }


    public String getStoreAfterRegisterPool() {
        return storeAfterRegisterPool;
    }

    public void setStoreAfterRegisterPool(String storeAfterRegisterPool) {
        this.storeAfterRegisterPool = storeAfterRegisterPool;
    }

    public boolean isStoreMetadata() {
        return storeMetadata;
    }

    public void setStoreMetadata(boolean storeMetadata) {
        this.storeMetadata = storeMetadata;
    }

    public String getMetadataStoragePool() {
        return metadataStoragePool == null ? NONE : metadataStoragePool;
    }

    public void setMetadataStoragePool(String pool) {
        this.metadataStoragePool = NONE.equals(pool) ? null : pool;
    }

    public boolean isIgnoreMetadataPersistenceErrors() {
        return ignoreMetadataPersistenceErrors;
    }

    public void setIgnoreMetadataPersistenceErrors(
            boolean ignoreMetadataPersistenceErrors) {
        this.ignoreMetadataPersistenceErrors = ignoreMetadataPersistenceErrors;
    }

    public boolean isForceMetadataPersistence() {
        return forceMetadataPersistence;
    }

    public void setForceMetadataPersistence(boolean forceMetadataPersistence) {
        this.forceMetadataPersistence = forceMetadataPersistence;
    }

    public XDSDocument storeDocument(XDSDocument xdsDoc) throws XDSException {
        log.info("#### Store Document:"+xdsDoc.getDocumentUID()+" without metadata");
        return storeDocument(xdsDoc, null);
    }	
    public XDSDocument storeDocument(XDSDocument xdsDoc, Source metadata) throws XDSException {
        String documentUID = xdsDoc.getDocumentUID();
        log.info("#### Store Document:"+documentUID+" to pool "+storeBeforeRegisterPool+"\nmetadata:"+metadata);
        boolean error = false;
        try {
            XDSDocument storedDoc;
            BaseDocument doc = docStore.createDocument(storeBeforeRegisterPool, 
                    xdsDoc.getDocumentUID(), xdsDoc.getMimeType());
            if ( doc != null ) {
                storedDoc = writeDocument(doc, xdsDoc.getXdsDocWriter());
            } else { //DocumentStorage does not support createDocument! (trust to get correct SHA1 hash)
                log.debug("DocumentStorage does not support createDocument! Use storeDocument with DataHandler and trust to get SHA1 cache!");
                doc = docStore.storeDocument(storeBeforeRegisterPool, 
                    documentUID, xdsDoc.getXdsDocWriter().getDataHandler());
                if (doc.getHash() == null) {
                    throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR, 
                            "SHA1 hash value missing! Storage does not support SHA1 hash! docUID:"+documentUID, null);
                   
                }
                if ( doc.getDataHandler() != null ) {
                    storedDoc = new XDSDocument( doc.getDocumentUID(), doc.getMimeType(), 
                        getXdsDocWriter(doc), doc.getHash(), null);
                } else {
                    storedDoc = new XDSDocument(doc.getDocumentUID(), doc.getMimeType(), 
                            doc.getSize(), doc.getHash(), "StoredDocument(no content provider)");
                }
            }
            
            if ( storeMetadata && metadata != null) {
                storeMetadata(metadata, doc);
            }
            return storedDoc;
        } catch ( XDSException x ) {
            throw x;
        } catch ( Throwable x ) {
                       log.error("Storage of document failed:"+documentUID, x);
            error = true;
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR, 
                    "Storage of document failed:"+documentUID, x);
        } finally {
            try {
                xdsDoc.getXdsDocWriter().close();
            } catch (IOException ignore) {
                log.warn("Error closing XDS Document Writer! Ignored",ignore );
            }
            if ( error) docStore.deleteDocument(documentUID);
        }
    }

    private XDSDocument writeDocument(BaseDocument doc, XDSDocumentWriter xdsDocWriter) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = null;
        DigestOutputStream dos = null;
        OutputStream out = doc.getOutputStream();
        if ( out != null ) {
            log.info("#### Write Document:"+doc.getDocumentUID());
            try {
                md = MessageDigest.getInstance("SHA1");
                dos = new DigestOutputStream(out, md);
                xdsDocWriter.writeTo(dos);
                log.info("#### File written:"+doc.getDocumentUID() );
            } finally {
                if ( dos != null ) {
                    try {
                        dos.close();
                    } catch (IOException ignore) {
                        log.error("Ignored error during close!",ignore);
                    }
                }
            }
        }
        return md == null ? null : 
            new XDSDocument( doc.getDocumentUID(), doc.getMimeType(), 
                    getXdsDocWriter(doc), DocumentStore.toHexString(md.digest()), null);
    }

    private XDSDocumentWriter getXdsDocWriter(BaseDocument doc) throws IOException {
        if ( doc.getDataHandler() == null) {
            return XDSDocumentWriterFactory.getInstance().getDocumentWriter(doc.getSize());
        } else {
            return XDSDocumentWriterFactory.getInstance().getDocumentWriter(doc.getInputStream(), doc.getSize());
        }
    }

    public XDSDocument retrieveDocument(String docUid, String mime) throws IOException {
        log.info("#### Retrieve Document from storage:"+docUid);
        BaseDocument doc = docStore.getDocument(docUid, mime);
        return doc == null || doc.getAvailability().equals( Availability.UNAVAILABLE) ? null  :
            new XDSDocument(docUid, mime, getXdsDocWriter(doc));
    }

    public boolean documentExists(String docUid, String mime){
        log.info("#### Document Exists?:"+docUid);
        return !docStore.getDocument(docUid, mime).getAvailability().equals( Availability.UNAVAILABLE);
    }

    public boolean commitDocuments(Collection<XDSDocument> documents) {
        log.info("#### Commit Documents:"+documents);
        return true;
    }
    public boolean rollbackDocuments(Collection<XDSDocument> documents) {
        log.info("#### Rollback Documents:"+documents);
        if ( documents == null || documents.size() < 1 ) 
            return true;
        boolean success = true;
        for ( XDSDocument doc : documents ) {
            log.debug("Delete XDSDocument:"+doc);
            success = success & docStore.deleteDocument(storeBeforeRegisterPool, doc.getDocumentUID());
        }
        return success;
    }

    public String computeHash(String filename) throws NoSuchAlgorithmException, IOException {
        FileInputStream fis = new FileInputStream(new File(filename));
        MessageDigest md  = MessageDigest.getInstance("SHA1");
        DigestInputStream dis = new DigestInputStream(fis, md);
        while (dis.read(buf) != -1);
        String hash = DocumentStore.toHexString( md.digest() );
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
        long size = 0;
        while ( (len = fis.read(buf)) > 0 ) {
            dos.write(buf, 0, len);
            size += len;
        }
        String hash = DocumentStore.toHexString( md.digest() );
        dos.close();
        if ( log.isDebugEnabled() ) log.debug("SHA1 write digest (alg:"+alg+"):"+hash);
        return hash;
    }

    /**
     * Store Metadata of an document (XDS metadata) as an extra document with mime type 'application/metadata+xml'
     * @param metadata
     * @param baseDocumetStorage 
     * @throws XDSException 
     */
    private void storeMetadata(Source metadata, BaseDocument doc) throws XDSException {
        DocumentStorage storage = getMetadataStorage(doc);
        if ( storage != null ) {
            BaseDocument metadataDoc = null;
            OutputStream out = null;
            try {
                metadataDoc = storage.createDocument( doc.getDocumentUID(), MIME_METADATA);
                if ( metadataDoc != null ) {
                    out = metadataDoc.getOutputStream();
                    if ( out != null ) {
                        writeXML(metadata, out);
                    }
                } else {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
                    writeXML(metadata, bos);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    DataSource ds = new ByteArrayDataSource(bos.toByteArray(), MIME_METADATA);
                    metadataDoc = storage.storeDocument(doc.getDocumentUID(), new DataHandler(ds));
                }
            } catch (Throwable x) {
                if ( ignoreMetadataPersistenceErrors ) {
                    log.debug("Storage of XDS Metadata failed!",x);
                    return;
                } else {
                    log.error("Storage of XDS Metadata failed!",x);
                    throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Storage of XDS Metadata failed!", x);
                }
            } finally {
                if ( out != null )
                    try { out.close(); } catch (IOException ignore) {}
            }
        } else if ( forceMetadataPersistence ){
            log.error("Storage of XDS Metadata is not supported!");
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Storage of XDS Metadata is not supported!", null );
        } else {
            log.debug("Storage of XDS Metadata is not supported!");
            return;
        }
    }

    private void writeXML(Source metadata, OutputStream out)
            throws TransformerConfigurationException,
            TransformerFactoryConfigurationError, TransformerException {
        StreamResult result = new StreamResult( out );
        TransformerHandler th = ((SAXTransformerFactory)TransformerFactory.newInstance()).newTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.getTransformer().transform(metadata, result);
    }

    private DocumentStorage getMetadataStorage(BaseDocument doc) {
        if ( log.isDebugEnabled() ) log.debug("metadataStoragePool:"+metadataStoragePool);
        if ( metadataStoragePool != null ) {
            return docStore.getDocStorageFromPool(metadataStoragePool);
        } else {
            DocumentStorage storage = doc.getStorage(); 
            if ( log.isDebugEnabled() ) log.debug("storage.hasFeature(Feature.MULTI_MIME):"+storage.hasFeature(Feature.MULTI_MIME));
            return storage.hasFeature(Feature.MULTI_MIME) ? storage : null;
        }
    }
    protected void startService() throws Exception {
        docStore = DocumentStore.getInstance("XDSStoreService", "XDS");
    }
}
