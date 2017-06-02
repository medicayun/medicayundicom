/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.apache.log4j.Logger;

import org.dcm4che.srom.Content;
import org.dcm4che.srom.HL7SRExport;
import org.dcm4che.srom.ImageContent;
import org.dcm4che.srom.Patient;
import org.dcm4che.srom.RefSOP;
import org.dcm4che.srom.SOPInstanceRef;
import org.dcm4che.srom.SRDocument;
import org.dcm4che.srom.ContainerContent;
import org.dcm4che.srom.TextContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3752 $ $Date: 2004-02-28 01:19:32 +0800 (周六, 28 2月 2004) $
 */
public class HL7SRExportImpl implements HL7SRExport {
    
    private static final Logger log = Logger.getLogger(HL7SRExportImpl.class);
    private static final SimpleDateFormat DATETIME_FORMAT =
        new SimpleDateFormat("yyyyMMddHHmmss");
    
    private static final Random rnd = new Random();    
    private String sendingApplication;
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;

    public HL7SRExportImpl(
            String sendingApplication, String sendingFacility,
            String receivingApplication, String receivingFacility) {
        setSendingApplication(sendingApplication);
        setSendingFacility(sendingFacility);
        setReceivingApplication(receivingApplication);
        setReceivingFacility(receivingFacility);
    }
    
    /** Getter for property sendingApplication.
     * @return Value of property sendingApplication.
     */
    public final String getSendingApplication() {
        return sendingApplication;
    }
    
    /** Setter for property sendingApplication.
     * @param sendingApplication New value of property sendingApplication.
     */
    public final void setSendingApplication(String sendingApplication) {
        if (sendingApplication == null) {
            throw new NullPointerException();
        }
        this.sendingApplication = sendingApplication;
    }
    
    /** Getter for property sendingFacility.
     * @return Value of property sendingFacility.
     */
    public final String getSendingFacility() {
        return sendingFacility;
    }
    
    /** Setter for property sendingFacility.
     * @param sendingFacility New value of property sendingFacility.
     */
    public final void setSendingFacility(String sendingFacility) {
        if (sendingFacility == null) {
            throw new NullPointerException();
        }
        this.sendingFacility = sendingFacility;
    }
    
    /** Getter for property receivingApplication.
     * @return Value of property receivingApplication.
     */
    public final String getReceivingApplication() {
        return receivingApplication;
    }
    
    /** Setter for property receivingApplication.
     * @param receivingApplication New value of property receivingApplication.
     */
    public final void setReceivingApplication(String receivingApplication) {
        if (receivingApplication == null) {
            throw new NullPointerException();
        }
        this.receivingApplication = receivingApplication;
    }
    
    /** Getter for property receivingFacility.
     * @return Value of property receivingFacility.
     */
    public final String getReceivingFacility() {
        return receivingFacility;
    }
    
    /** Setter for property receivingFacility.
     * @param receivingFacility New value of property receivingFacility.
     */
    public final void setReceivingFacility(String receivingFacility) {
        if (receivingFacility == null) {
            throw new NullPointerException();
        }
        this.receivingFacility = receivingFacility;
    }
    
    public String nextMessageControlID() {
        return "dcm4che" + Integer.toHexString(rnd.nextInt());
    }
    
    public byte[] toHL7(SRDocument doc, String messageControlID,
            String issuerOfPatientID, String patientAccountNumber,
            String placerOrderNumber, String fillerOrderNumber,
            String universalServiceID) {
        if (messageControlID == null) {
            throw new NullPointerException();
        }
        if (patientAccountNumber == null) {
            throw new NullPointerException();
        }
        if (universalServiceID == null) {
            throw new NullPointerException();
        }
        if (placerOrderNumber == null) {
            throw new NullPointerException();
        }
        if (fillerOrderNumber == null) {
            throw new NullPointerException();
        }
        
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(bout);
            // Write MSH
            w.write("MSH|^~\\&|");
            w.write(sendingApplication);
            w.write("|");
            w.write(sendingFacility);
            w.write("|");
            w.write(receivingApplication);
            w.write("|");
            w.write(receivingFacility);
            w.write("|||ORU^R01|");
            w.write(messageControlID);
            w.write("|P|2.3.1|");
            w.write(toHL7Charset(doc.getSpecificCharacterSet()));
            w.write("\r");

            // Write PID
            Patient pat = doc.getPatient();
            w.write("PID|||");
            w.write(maskNull(pat.getPatientID()));
            w.write("^^^");
            w.write(issuerOfPatientID);
            w.write("||");
            w.write(maskNull(pat.getPatientName()));
            w.write("||");
            w.write(toString(pat.getPatientBirthDate()));
            w.write("|");
            w.write(toString(pat.getPatientSex()));
            w.write("||||||||||");
            w.write(patientAccountNumber);
            w.write("\r");

            // Write OBR
            w.write("OBR|1|");
            w.write(placerOrderNumber);
            w.write("|");
            w.write(fillerOrderNumber);
            w.write("|");
            w.write(universalServiceID);
            w.write("|||");
            w.write(toString(doc.getObservationDateTime(true)));
            w.write("||||||||||||||||||F|||||||");
         /* TODO Principal Results Interpreter
            Person Name value of the Content item that is related to the root of 
            the SR document with the relation ship HAS OBS CONTEXT and whose
            Concept Name Code is (121008,DCM,  Person Observer Name )    
          */
            w.write("\r");

            // Write OBX|1|HD|^SR Instance UID
            w.write("OBX|1|HD|^SR Instance UID||");
            w.write(maskNull(doc.getSOPInstanceUID()));
            w.write("||||||F");
            w.write("\r");

            ArrayList txts = new ArrayList();
            ArrayList imgs = new ArrayList();
            findTextAndImage(doc, doc, txts, imgs);

            // Write OBX Quardtruple for IMAGEs
            int setid = 1;
            for (int i = 0, n = imgs.size(); i < n; ++i) {
                SOPInstanceRef sopInstRef = (SOPInstanceRef) imgs.get(i);
                String subID = String.valueOf(i+1);
                w.write("OBX|");
                w.write(String.valueOf(++setid));
                w.write("|HD|^Study Instance UID|");
                w.write(subID);
                w.write("|");
                w.write(sopInstRef.getStudyInstanceUID());
                w.write("||||||F");
                w.write("\r");

                w.write("OBX|");
                w.write(String.valueOf(++setid));
                w.write("|HD|^Series Instance UID|");
                w.write(subID);
                w.write("|");
                w.write(sopInstRef.getSeriesInstanceUID());
                w.write("||||||F");
                w.write("\r");

                w.write("OBX|");
                w.write(String.valueOf(++setid));
                w.write("|HD|^SOP Instance UID|");
                w.write(subID);
                w.write("|");
                w.write(sopInstRef.getRefSOPInstanceUID());
                w.write("||||||F");
                w.write("\r");

                w.write("OBX|");
                w.write(String.valueOf(++setid));
                w.write("|HD|^SOP Class UID|");
                w.write(subID);
                w.write("|");
                w.write(sopInstRef.getRefSOPClassUID());
                w.write("||||||F");
                w.write("\r");
            }
            for (int i = 0, n = txts.size(); i < n; ++i) {
                w.write("OBX|");
                w.write(String.valueOf(++setid));
                w.write("|TX|^SR Text||");
                w.write((String) txts.get(i));
                w.write("||||||F");
                w.write("\r");
            }
            w.close();
            return bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private final static HashMap csMap = new HashMap();
    static {
        csMap.put("ISO_IR 100","8859/1");
        csMap.put("ISO_IR 101","8859/2");
        csMap.put("ISO_IR 109","8859/3");
        csMap.put("ISO_IR 110","8859/4");
        csMap.put("ISO_IR 144","8859/5");
        csMap.put("ISO_IR 127","8859/6");
        csMap.put("ISO_IR 126","8859/7");
        csMap.put("ISO_IR 138","8859/8");
        csMap.put("ISO_IR 148","8859/9");
    }
    
    private String toHL7Charset(String dicomCharset) {
        if (dicomCharset == null || dicomCharset.length() == 0) {
            return "";
        }
        String retval = (String) csMap.get(dicomCharset);
        if (retval == null) {
            log.warn("Unrecognized Charset: " + dicomCharset + " - use 8859/1 in MSH");
            return "8859/1";
        }
        return retval;
    }

    private void findTextAndImage(SRDocument doc, Content node,
            ArrayList txts, ArrayList imgs) {
        if (node instanceof TextContent) {
            TextContent txtNode = (TextContent) node;
            Content parent = txtNode.getParent();
            StringBuffer sb = new StringBuffer();
            if (parent instanceof ContainerContent) {
                ContainerContent container = (ContainerContent) parent;
                sb.append(container.getName().getCodeMeaning()).append(' ');
            }
            sb.append(txtNode.getText());
            txts.add(sb.toString());
        } else if (node instanceof ImageContent) {
            ImageContent imgNode = (ImageContent) node;
            RefSOP refSOP = imgNode.getRefSOP();
            SOPInstanceRef sopInstRef = doc.findCurrentEvidence(refSOP);
            if (sopInstRef == null) {
                sopInstRef = doc.findOtherEvidence(refSOP);
            }
            if (sopInstRef != null) {
                imgs.add(sopInstRef);
            } else {
                log.warn("Missing Evidence SOP Instance Reference Macro for "
                    + imgNode + " - cannot export image reference");
            }
        }
        for (Content child = node.getFirstChild(); child != null;
                child = child.getNextSibling()) {
            findTextAndImage(doc, child, txts, imgs);
        }
    }
    
    private String maskNull(String s) {
        return s != null ? s : "";
    }
    
    private String toString(Date d) {
        return d != null ? DATETIME_FORMAT.format(d) : "";
    }

    private String toString(Patient.Sex sex) {
        return sex != null ? sex.toString() : "";
    }
}
