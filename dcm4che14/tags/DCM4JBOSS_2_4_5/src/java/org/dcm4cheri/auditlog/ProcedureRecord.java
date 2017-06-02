/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.User;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3835 $ $Date: 2004-12-03 22:12:29 +0800 (周五, 03 12月 2004) $
 * @since 12.10.2004
 *
 */
public class ProcedureRecord implements IHEYr4.Message {

    private final String action;

    private final String placerOrderNumber;

    private final String fillerOrderNumber;

    private final String suid;

    private final String accessionNumber;

    private final Patient patient;

    private final User user;

    private final String desc;

    public ProcedureRecord(String action, String placerOrderNumber,
            String fillerOrderNumber, String suid, String accessionNumber,
            Patient patient, User user, String desc) {
        this.action = action;
        this.placerOrderNumber = placerOrderNumber;
        this.fillerOrderNumber = fillerOrderNumber;
        this.suid = suid;
        this.accessionNumber = accessionNumber;
        this.patient = patient;
        this.user = user;
        this.desc = desc != null && desc.length() != 0 ? desc : null;
    }

    public void writeTo(StringBuffer sb) {
        sb.append("<ProcedureRecord><ObjectAction>").append(action)
                .append("</ObjectAction>").append("<PlacerOrderNumber><![CDATA[")
                .append(placerOrderNumber).append("]]></PlacerOrderNumber>")
                .append("<FillerOrderNumber><![CDATA[").append(fillerOrderNumber)
                .append("]]></FillerOrderNumber>").append("<SUID>").append(suid)
                .append("</SUID>");
        if (accessionNumber != null)
                sb.append("<AccessionNumber><![CDATA[").append(accessionNumber)
                        .append("]]></AccessionNumber>");
        patient.writeTo(sb);
        user.writeTo(sb);
        if (desc != null)
            sb.append("<Description><![CDATA[").append(desc).append("]]></Description>");
        sb.append("</ProcedureRecord>");
    }

}