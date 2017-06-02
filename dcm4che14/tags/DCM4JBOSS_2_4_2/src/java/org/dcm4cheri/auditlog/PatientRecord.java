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
public class PatientRecord implements IHEYr4.Message {

    private final String action;

    private final Patient patient;

    private final User user;
    
    private final String desc;

    public PatientRecord(final String action, final Patient patient,
            final User user, final String desc) {
        this.action = action;
        this.patient = patient;
        this.user = user;
        this.desc = desc != null && desc.length() != 0 ? desc : null;
    }

    public void writeTo(StringBuffer sb) {
        sb.append("<PatientRecord><ObjectAction>").append(action)
                .append("</ObjectAction>");
        patient.writeTo(sb);
        user.writeTo(sb);
        if (desc != null)
            sb.append("<Description><![CDATA[").append(desc)
            	.append("]]></Description>");
        sb.append("</PatientRecord>");
    }

}