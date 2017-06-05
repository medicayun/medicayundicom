package org.dcm4chex.archive.hl7;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.regenstrief.xhl7.HL7XMLLiterate;

public class ACK {

    public final String acknowledgmentCode;
	public final String messageControlID;
	public final String textMessage;

	public ACK(Document msg) {
        Element msa = msg.getRootElement().element("MSA");
        if (msa == null)
                throw new IllegalArgumentException("Missing MSA Segment");
        List fields = msa.elements(HL7XMLLiterate.TAG_FIELD);
        this.acknowledgmentCode = toString(fields.get(0));
        this.messageControlID = toString(fields.get(1));
        this.textMessage = toString(fields.get(1));
     }

    private static String toString(Object el) {
        return el != null ? ((Element) el).getText() : "";
    }
    
    public String toString() {
    	return "ACK[code=" +  acknowledgmentCode 
    			+ ", msgID=" + messageControlID + ','
    			+ ", errorMsg=" + textMessage + "]";
    }
}
