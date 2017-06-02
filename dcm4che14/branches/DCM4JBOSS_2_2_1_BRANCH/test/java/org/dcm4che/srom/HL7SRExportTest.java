/*
 * ReferencedContentTest.java
 * JUnit based test
 *
 * Created on August 17, 2002, 2:40 PM
 */

package org.dcm4che.srom;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;

import java.io.*;

import junit.framework.*;

/**
 *
 * @author gunter
 */
public class HL7SRExportTest extends TestCase {
    
    public HL7SRExportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(HL7SRExportTest.class);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    private static final String SR_DCM = "data/sr_603mr.dcm";
    private static final String SR_HL7 = "data/sr_603mr.hl7";
    
    private DcmObjectFactory dsf = null;
    private SRDocumentFactory srf = null;
    private byte[] expected;
        
    protected void setUp() throws Exception {    
        dsf = DcmObjectFactory.getInstance();
        srf = SRDocumentFactory.getInstance();
        File f = new File(SR_HL7);
        InputStream in = new FileInputStream(f);        
        try {
            expected = new byte[(int)f.length()];
            in.read(expected);
        } finally {
            try { in.close(); } catch (IOException ignore) {};
        }
    }
    
    private void assertEquals(byte[] expected, byte[] value) {
        TestCase.assertNotNull(value);
        TestCase.assertEquals(expected.length, value.length);
        for (int i = 0; i < expected.length; ++i) {
            TestCase.assertEquals("byte[" + i + "]", expected[i], value[i]);
        }
    }
    
    public void testToHL7() throws Exception {
        Dataset ds = dsf.newDataset();
        InputStream in = new BufferedInputStream(
                new FileInputStream(new File(SR_DCM)));
        try {
            ds.readFile(in, null, -1);
        } finally {
            in.close();
        }
        SRDocument sr = srf.newSRDocument(ds);
        HL7SRExport export = srf.newHL7SRExport(
            "SendingApplication","SendingFacility",
            "ReceivingApplication","ReceivingFacility");
        byte[] msg = export.toHL7(sr, "MessageControlID",
            "IssuerOfPatientID", "PatientAccountNumber",
            "PlacerOrderNumber", "FillerOrderNumber",
            "UniversalServiceID");
//        new FileOutputStream(SR_HL7).write(msg);
        assertEquals(expected, msg);
     }

}
