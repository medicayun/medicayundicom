package org.dcm4chee.xds.store.mbean;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dcm4chee.docstore.DocumentStorageRegistry;
import org.dcm4chee.docstore.DocumentStore;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;

import junit.framework.TestCase;

public class XDSStoreServiceDocsizeTest extends XDSStoreTestBase {

    private static Logger log = Logger.getLogger(XDSStoreServiceDocsizeTest.class);

    public XDSStoreServiceDocsizeTest() {
        super();
    }

    public void testCheckSize() throws IOException, XDSException {
        String docUid = "docUID_s0";
        XDSDocument retrDoc;
        XDSDocumentWriter docWriter = fac.getDocumentWriter(0, MIME_TEXT_PLAIN);
        XDSDocument xdsDoc = new XDSDocument(docUid, MIME_TEXT_PLAIN, docWriter);
        XDSDocument doc = xdsStore.storeDocument(xdsDoc);
        assertEquals( "Stored XDSDocument has wrong size!", 0, doc.getSize());
        retrDoc = xdsStore.retrieveDocument(docUid, MIME_TEXT_PLAIN);
        assertEquals( "Retrieved XDSDocument has wrong size!", 0,retrDoc.getSize() );
        for ( int i = 1 ; i < 10000 ; i *= 10) {
            docUid = "docUID_s"+i;
            xdsDoc = new XDSDocument(docUid, MIME_TEXT_PLAIN, fac.getDocumentWriter(i, MIME_TEXT_PLAIN) );
            doc = xdsStore.storeDocument(xdsDoc);
            assertEquals( "Stored XDSDocument has wrong size!", i, doc.getSize());
            retrDoc = xdsStore.retrieveDocument(docUid, MIME_TEXT_PLAIN);
        }
    }

}
