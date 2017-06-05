/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.infohazard.maverick.flow.ControllerContext;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1244 $ $Date: 2004-12-02 04:36:53 +0800 (周四, 02 12月 2004) $
 * @since 14.10.2004
 *
 */
public class AuditLoggerDelegate {

    public static final String CREATE = "Create";

    public static final String MODIFY = "Modify";
    
    public static final String ACCESS = "Access";
    
    public static final String DELETE = "Delete";

    private static Logger log = Logger.getLogger(AuditLoggerDelegate.class);

    private static MBeanServer server;

    private static ObjectName auditLogName;


    private static void init(ControllerContext ctx) throws Exception {
        if (auditLogName != null) return;
        AuditLoggerDelegate.server = MBeanServerLocator.locate();
        String s = ctx.getServletConfig().getInitParameter("auditLoggerName");
        AuditLoggerDelegate.auditLogName = new ObjectName(s);
    }

    public static void logActorConfig(ControllerContext ctx, String desc,
            String type) {
        try {
            init(ctx);
            AuditLoggerDelegate.server.invoke(auditLogName,
                    "logActorConfig",
                    new Object[] { desc, type},
                    new String[] { String.class.getName(),
                            String.class.getName(),});
        } catch (Exception e) {
            log.warn("Failed to log ActorConfig:", e);
        }
    }

    public static void logStudyDeleted(ControllerContext ctx, String patid,
            String patname, String suid, int numberOfInstances, String desc) {
        try {
            init(ctx);
            AuditLoggerDelegate.server.invoke(auditLogName,
                    "logStudyDeleted",
                    new Object[] { patid, patname, suid,
                            new Integer(numberOfInstances), desc},
                    new String[] { String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            Integer.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Failed to log studyDeleted:", e);
        }
    }

    public static void logPatientRecord(ControllerContext ctx, String action,
            String patid, String patname, String desc) {
        try {
            init(ctx);
            AuditLoggerDelegate.server.invoke(auditLogName,
                    "logPatientRecord",
                    new Object[] { action, patid, patname, desc},
                    new String[] { String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName()});
        } catch (Exception e) {
            log.warn("Failed to log patientRecord:", e);
        }
    }

    public static void logProcedureRecord(ControllerContext ctx, String action,
            String patid, String patname, String placerOrderNo,
            String fillerOrderNo, String suid, String accNo, String desc) {
        try {
            init(ctx);
            AuditLoggerDelegate.server.invoke(auditLogName,
                    "logProcedureRecord",
                    new Object[] { action, patid, patname, placerOrderNo,
                            fillerOrderNo, suid, accNo, desc},
                    new String[] { String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName(), String.class.getName(),
                            String.class.getName()});
        } catch (Exception e) {
            log.warn("Failed to log procedureRecord:", e);
        }
    }

    public static boolean equals(String prevVal, String newVal) {
        return prevVal == null || prevVal.length() == 0
        ? newVal == null || newVal.length() == 0
                : prevVal.equals(newVal);
    }
    
    public static boolean isModified(String name, String prevVal, String newVal,
            StringBuffer desc) {
        if (prevVal == null || prevVal.length() == 0
                ? newVal == null || newVal.length() == 0
                : prevVal.equals(newVal))
            return false;
        desc.append(name);
        desc.append(" changed from \"");
        if (prevVal != null)
            desc.append(prevVal);
        desc.append("\" to \"");
        if (newVal != null)
            desc.append(newVal);
        desc.append("\", ");
        return true;
    }
    
    public static String trim(StringBuffer sb) {
        int len = sb.length();
        char ch;
        while (len > 0 && ((ch = sb.charAt(len-1)) == ' ' || ch == ',')) --len;
        sb.setLength(len);
        return sb.toString();
    }
    
}