/*
 * ReferencedContentTest.java
 * JUnit based test
 *
 * Created on August 17, 2002, 2:40 PM
 */

package org.dcm4che.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;

import junit.framework.*;

/**
 *
 * @author gunter
 */
public class ReferencedContentTest extends TestCase {
    
    public ReferencedContentTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    static SRDocumentFactory srf = SRDocumentFactory.getInstance();
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ReferencedContentTest.class);
        return suite;
    }
    
    private Patient pat;
    private Study study;
    private Series series;
    private Equipment equip;
    private Code title;
    private RefSOP refSOP;
    private ImageContent imgRef;
    private NumContent num;
    private TextContent text;
    private SCoordContent scoord;
    private SRDocument doc;
    
    protected void setUp() throws Exception {
        pat = srf.newPatient("P1234", "DOE^JOHN", Patient.Sex.MALE, null);
        study = srf.newStudy("1.2.3.4.5", "S12345", null, null, "A12345",
            null, null);
        series = srf.newSRSeries("1.2.3.4.5.6", 6, null);
        equip = srf.newEquipment("TIANI", "ReferencedContentTest", null);
        title = srf.newCode("11528-7", "LN", "Radiology Report");
        doc = srf.newSRDocument(pat, study, series, equip, UIDs.ComprehensiveSR,
            "1.2.3.4.5.6.7", 7, null, null, title, true);
        refSOP = srf.newRefSOP(UIDs.CTImageStorage, "1.2.3.4.5.1.1");
        imgRef = doc.createImageContent(null, null,
            null, refSOP, null, null, null);
        text = doc.createTextContent(null, null, 
            srf.newCode("121073", "DCM", "Impression"),
            "May be an artifact");
        num = doc.createNumContent(null, null,
            srf.newCode("121211", "DCM","Path Length"), 5.f,
            srf.newCode("cm", "UCUM", "1.4", "centimeter"));
        scoord = doc.createPolylineSCoordContent(null, null,
            srf.newCode("121055", "DCM", "Path"),
            new float[] { 543.f, 221.f, 503.f, 251.f });
    }
    
    public void testBackwardRef() throws Exception {
        doc.appendChild(Content.RelationType.CONTAINS, imgRef);
        doc.appendChild(Content.RelationType.CONTAINS, text);
        text.appendChild(Content.RelationType.INFERRED_FROM,
            doc.createReferencedContent(imgRef));
        doc.appendChild(Content.RelationType.CONTAINS, num);
        num.appendChild(Content.RelationType.INFERRED_FROM, scoord);
        scoord.appendChild(Content.RelationType.SELECTED_FROM,
            doc.createReferencedContent(imgRef));
        Dataset ds = doc.toDataset();

//        ds.writeFile(new java.io.FileOutputStream("testSR1.dcm"), null);
        
        SRDocument doc2 = srf.newSRDocument(ds);
        Content imgRef2 = doc2.getFirstChild();
        assertTrue(imgRef2 instanceof ImageContent);

        Content text2 = imgRef2.getNextSibling();
        assertTrue(text2 instanceof TextContent);
        Content ref1 = text2.getFirstChild();
        assertTrue(ref1 instanceof ReferencedContent);
        Content imgRef3 = ((ReferencedContent) ref1).getRefContent();
        assertSame(imgRef2, imgRef3);

        Content num2 = text2.getNextSibling();
        assertTrue(num2 instanceof NumContent);
        Content scoord2 = num2.getFirstChild();
        assertTrue(scoord2 instanceof SCoordContent);
        Content ref2 = scoord2.getFirstChild();
        assertTrue(ref2 instanceof ReferencedContent);
        Content imgRef4 = ((ReferencedContent) ref2).getRefContent();
        assertSame(imgRef2, imgRef4);
    }
    
    
    public void testForwardRef() throws Exception {
        doc.appendChild(Content.RelationType.CONTAINS, text);
        doc.appendChild(Content.RelationType.CONTAINS, num);
        doc.appendChild(Content.RelationType.CONTAINS, imgRef);
        text.appendChild(Content.RelationType.INFERRED_FROM,
            doc.createReferencedContent(imgRef));
        num.appendChild(Content.RelationType.INFERRED_FROM, scoord);
        scoord.appendChild(Content.RelationType.SELECTED_FROM,
            doc.createReferencedContent(imgRef));
        Dataset ds = doc.toDataset();
        
//        ds.writeFile(new java.io.FileOutputStream("testSR2.dcm"), null);
        
        SRDocument doc2 = srf.newSRDocument(ds);
        Content text2 = doc2.getFirstChild();
        assertTrue(text2 instanceof TextContent);        
        Content num2 = text2.getNextSibling();
        assertTrue(num2 instanceof NumContent);        
        Content imgRef2 = num2.getNextSibling();
        assertTrue(imgRef2 instanceof ImageContent);
        
        Content ref1 = text2.getFirstChild();
        assertTrue(ref1 instanceof ReferencedContent);
        Content imgRef3 = ((ReferencedContent) ref1).getRefContent();
        assertSame(imgRef2, imgRef3);

        Content scoord2 = num2.getFirstChild();
        assertTrue(scoord2 instanceof SCoordContent);
        Content ref2 = scoord2.getFirstChild();
        assertTrue(ref2 instanceof ReferencedContent);
        Content imgRef4 = ((ReferencedContent) ref2).getRefContent();
        assertSame(imgRef2, imgRef4);
    }
    
}
