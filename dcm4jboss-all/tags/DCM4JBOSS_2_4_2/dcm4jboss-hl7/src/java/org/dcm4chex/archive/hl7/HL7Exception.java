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
 * @version $Revision: 1357 $ $Date: 2004-12-30 08:55:56 +0800 (周四, 30 12月 2004) $
 * @since 27.10.2004
 *
 */
public abstract class HL7Exception extends Exception {


    public HL7Exception(String message) {
        super(message);
    }

    public HL7Exception(String message, Throwable cause) {
        super(message, cause);
    }
    
    public abstract String getAcknowledgementCode();

    public static class AE extends HL7Exception {

        public AE(String message) {
            super(message);
        }

        public AE(String message, Throwable cause) {
            super(message, cause);
        }

        public String getAcknowledgementCode() {
            return "AE";
        }
    }

    public static class AR extends HL7Exception {

        public AR(String message) {
            super(message);
        }

        public AR(String message, Throwable cause) {
            super(message, cause);
        }

        public String getAcknowledgementCode() {
            return "AR";
        }

    }

}
