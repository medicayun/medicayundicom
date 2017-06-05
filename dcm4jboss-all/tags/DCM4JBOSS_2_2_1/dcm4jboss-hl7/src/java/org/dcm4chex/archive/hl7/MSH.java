/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.regenstrief.xhl7.HL7XMLLiterate;


public class MSH {

    public final String sendingApplication;

    public final String sendingFacility;

    public final String receivingApplication;

    public final String receivingFacility;

    public final String messageType;

    public final String triggerEvent;

    public final String processingID;

    public final String versionID;

    public final String characterSet;

    public MSH(Document msg) {
        Element msh = msg.getRootElement().element("MSH");
        if (msh == null)
                throw new IllegalArgumentException("Missing MSH Segment");
        List fields = msh.elements(HL7XMLLiterate.TAG_FIELD);
        this.sendingApplication = toString(fields.get(0));
        this.sendingFacility = toString(fields.get(1));
        this.receivingApplication = toString(fields.get(2));
        this.receivingFacility = toString(fields.get(3));
        Element el = (Element) fields.get(6);
        this.messageType = toString(el);
        this.triggerEvent = toString(el != null ? el
                .element(HL7XMLLiterate.TAG_COMPONENT) : null);
        this.processingID = toString(fields.get(8));
        this.versionID = toString(fields.get(9));
        this.characterSet = fields.size() > 15 ? toString(fields.get(15)) : null;
    }

    private static String toString(Object el) {
        return el != null ? ((Element) el).getText() : "";
    }
    
    public String toString() {
    	return messageType + "^" + triggerEvent + "["
    		+  sendingApplication + '@' + sendingFacility
    		+ "->" + receivingApplication + '@' + receivingFacility
    		+ ", pid=" +  processingID + ", vers=" +  versionID
    		+ ", charset=" +  characterSet + "]";
    }
}