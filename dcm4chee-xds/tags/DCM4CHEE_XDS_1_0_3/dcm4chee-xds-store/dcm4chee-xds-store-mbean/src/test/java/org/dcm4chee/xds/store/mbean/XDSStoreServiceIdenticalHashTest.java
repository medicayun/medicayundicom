package org.dcm4chee.xds.store.mbean;

import java.io.IOException;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDSStoreServiceIdenticalHashTest extends XDSStoreTestBase {

    private static Logger log = LoggerFactory.getLogger(XDSStoreServiceIdenticalHashTest.class);

    public XDSStoreServiceIdenticalHashTest() {
        super();
    }
    
    public void testStoreDocumentNonIdenticalHash() throws IOException, XDSException {
        XDSDocumentWriter docWriter = fac.getDocumentWriter("testDocumentContent1", MIME_TEXT_XML);
        XDSDocumentWriter docWriter1 = fac.getDocumentWriter("testDocumentContent2", MIME_TEXT_XML);
        XDSDocument xdsDoc = new XDSDocument("docUID1", MIME_TEXT_XML, docWriter);
        XDSDocument xdsDoc1 = new XDSDocument("docUID1", MIME_TEXT_XML, docWriter1);
        xdsStore.storeDocument(xdsDoc);
        try {
            xdsStore.storeDocument(xdsDoc1);
            fail("XDSException with errorCode "+XDSConstants.XDS_ERR_NON_IDENTICAL_HASH+" must be thrown!");
        } catch (XDSException x) {
            assertEquals( "XDSException errorCode!", XDSConstants.XDS_ERR_NON_IDENTICAL_HASH, x.getErrorCode());
        }
    }

    public void testStoreDocumentIdenticalHash() throws IOException, XDSException {
        XDSDocumentWriter docWriter = fac.getDocumentWriter("testDocumentContent", MIME_TEXT_XML);
        XDSDocument xdsDoc = new XDSDocument("docUID_ih_2", MIME_TEXT_XML, docWriter);
        XDSDocument doc = xdsStore.storeDocument(xdsDoc);
        assertEquals( "XDSDocument Status (after initial store)!", XDSDocument.CREATED, doc.getStatus());
        doc = xdsStore.storeDocument(xdsDoc);
        assertEquals( "XDSDocument Status (2nd store with identical hash)!", XDSDocument.STORED, doc.getStatus());
    }

}
