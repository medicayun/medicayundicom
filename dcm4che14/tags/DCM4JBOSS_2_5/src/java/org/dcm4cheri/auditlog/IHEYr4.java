/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4cheri.auditlog;

import java.util.Date;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.ISO8601DateFormat;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 27, 2002
 * @version    $Revision: 3835 $ $Date: 2004-12-03 22:12:29 +0800 (周五, 03 12月 2004) $
 */
class IHEYr4 {

    interface Message {

        void writeTo(StringBuffer sb);
    }

    // Constants -----------------------------------------------------
    // Variables -----------------------------------------------------
    private static String localHostName;

    private Message msg;

    private String host;

    private long millis;

    // Constructors --------------------------------------------------
    private IHEYr4(Message msg, String host, long millis) {
        this.msg = msg;
        this.host = host;
        this.millis = millis;
    }

    public static IHEYr4 newInstancesStored(RemoteNode rnode,
            InstancesAction action, String host, long millis) {
        return new IHEYr4(new RnodeWithInstanceActionDescription(
                "InstancesStored", rnode, action, "RemoteNode"), host, millis);
    }

    public static IHEYr4 newBeginStoringInstances(RemoteNode rnode,
            InstancesAction action, String host, long millis) {
        return new IHEYr4(new RnodeWithInstanceActionDescription(
                "BeginStoringInstances", rnode, action, "Rnode"), host, millis);
    }

    public static IHEYr4 newInstancesSent(RemoteNode rnode,
            InstancesAction action, String host, long millis) {
        return new IHEYr4(new RnodeWithInstanceActionDescription(
                "InstancesSent", rnode, action, "RNode"), host, millis);
    }

    public static IHEYr4 newActorStartStop(String actorName,
            String applicationAction, User user, String host, long millis) {
        return new IHEYr4(
                new ActorStartStop(actorName, applicationAction, user), host,
                millis);
    }

    public static IHEYr4 newActorConfig(String description, User user,
            String configType, String host, long millis) {
        return new IHEYr4(new ActorConfig(description, user, configType), host,
                millis);
    }

    public static IHEYr4 newStudyDeleted(InstancesAction action, String desc,
            String host, long millis) {
        return new IHEYr4(new StudyDeleted(action, desc), host, millis);
    }

    public static IHEYr4 newPatientRecord(String action, Patient patient,
            User user, String desc, String host, long millis) {
        return new IHEYr4(new PatientRecord(action, patient, user, desc),
                host, millis);

    }

    public static IHEYr4 newProcedureRecord(String action,
            String placerOrderNumber, String fillerOrderNumber, String suid,
            String accessionNumber, Patient patient, User user, String desc,
            String host, long millis) {
        return new IHEYr4(new ProcedureRecord(action, placerOrderNumber,
                fillerOrderNumber, suid, accessionNumber, patient, user, desc),
                host, millis);

    }

    public static IHEYr4 newDicomQuery(Dataset keys, RemoteNode requestor,
            String cuid, String host, long millis) {
        return new IHEYr4(new DicomQuery(keys, requestor, cuid), host, millis);
    }

    public static IHEYr4 newSecurityAlert(String alertType, User user,
            String description, String host, long millis) {
        return new IHEYr4(new SecurityAlert(alertType, user, description),
                host, millis);
    }

    public static IHEYr4 newUserAuthenticated(String localUserName,
            String action, String host, long millis) {
        return new IHEYr4(new UserAuthenticated(localUserName, action), host,
                millis);
    }

    public static IHEYr4 newExport(MediaDescription media, User user,
            String host, long millis) {
        return new IHEYr4(new Export(media, user), host, millis);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><IHEYr4>");
        msg.writeTo(sb);
        sb.append("<Host>").append(host).append("</Host><TimeStamp>");
        sb.append(new ISO8601DateFormat().format(new Date(millis)));
        sb.append("</TimeStamp></IHEYr4>");
        return sb.toString();
    }
}

