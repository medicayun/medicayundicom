/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1762 $ $Date: 2005-06-02 07:34:32 +0800 (周四, 02 6月 2005) $
 * @since 27.10.2004
 *
 */
public class HL7Exception extends Exception {
	
	private static final long serialVersionUID = 3256446906287995185L;
	
	private final String ackCode;

    public HL7Exception(String ackCode, String message) {
        this(ackCode, message, null);
    }

    public HL7Exception(String ackCode, String message, Throwable cause) {
        super(message, cause);
		this.ackCode = ackCode;
    }
    
    public String getAcknowledgementCode() {
		return ackCode;
    }
}
