package org.dcm4chee.xds.store.mbean;

import java.io.IOException;
import java.net.URL;

import org.dcm4chee.docstore.DocumentStorageRegistry;
import org.dcm4chee.docstore.DocumentStore;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class XDSStoreTestBase extends TestCase {

    public static final String MIME_TEXT_XML = "text/xml";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    
    
    protected static DocumentStorageRegistry registry;
    protected static XDSStoreService xdsStore;
    
    public static final XDSDocumentWriterFactory fac = XDSDocumentWriterFactory.getInstance();
    
    private static Logger log = LoggerFactory.getLogger(XDSStoreTestBase.class);

    public XDSStoreTestBase() {
        init();
    }
    private void init() {
        try {
            if ( registry == null ) {
                registry = new DocumentStorageRegistry();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                URL url = cl.getResource("test_docstore_cfg.xml");
                log.info("################## XDSStore Test docstore cfg file:"+url);
                registry.config(url.toExternalForm());
                DocumentStore.setDocumentStorageRegistry(registry);
                xdsStore = new XDSStoreService();
                xdsStore.startService();
                xdsStore.setStoreBeforeRegisterPool("uncommitted");
            }
        } catch (Exception e) {
            fail("Exception in init()!");
        }
    }
}
