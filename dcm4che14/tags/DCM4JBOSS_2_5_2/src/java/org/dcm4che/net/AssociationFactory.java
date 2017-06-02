/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che.net;

import org.dcm4che.Implementation;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3870 $ $Date: 2005-03-01 20:56:59 +0800 (周二, 01 3月 2005) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public abstract class AssociationFactory {
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------
    private static AssociationFactory instance = (AssociationFactory)
            Implementation.findFactory("dcm4che.net.AssociationFactory");

    public static AssociationFactory getInstance() {
        return instance;
    }

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------
    public abstract AAssociateRQ newAAssociateRQ();

    public abstract AAssociateAC newAAssociateAC();

    public abstract AAssociateRJ newAAssociateRJ(int result, int source,
            int reason);

    public abstract PDataTF newPDataTF(int maxLength);

    public abstract AReleaseRQ newAReleaseRQ();

    public abstract AReleaseRP newAReleaseRP();

    public abstract AAbort newAAbort(int source, int reason);

    public abstract PresContext newPresContext(int pcid, String asuid);

    public abstract PresContext newPresContext(int pcid, String asuid,
            String[] tsuids);

    public abstract PresContext newPresContext(int pcid, int result,
            String tsuid);

    public abstract AsyncOpsWindow newAsyncOpsWindow(int maxOpsInvoked,
            int maxOpsPerfomed);

    public abstract RoleSelection newRoleSelection(String uid, boolean scu,
            boolean scp);

    public abstract ExtNegotiation newExtNegotiation(String uid, byte[] info);

    public abstract PDU readFrom(InputStream in, byte[] buf) throws IOException;

    public abstract Association newRequestor(Socket s) throws IOException;

    public abstract Association newAcceptor(Socket s) throws IOException;

    public abstract ActiveAssociation newActiveAssociation(Association assoc,
            DcmServiceRegistry services);

    public abstract Dimse newDimse(int pcid, Command cmd);

    public abstract Dimse newDimse(int pcid, Command cmd, Dataset ds);

    public abstract Dimse newDimse(int pcid, Command cmd, DataSource src);

    public abstract AcceptorPolicy newAcceptorPolicy();

    public abstract DcmServiceRegistry newDcmServiceRegistry();

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
