/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below. 
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.notif;

import org.dcm4che.data.Dataset;

/**
 * @author Franz Willer franz.willer@gwi-ag.com
 * @version $Id: Export.java 3068 2007-01-11 20:43:22Z javawilli $
 * @since Jan 10, 2007
 */
public class Export {

    private final Dataset kos;
    private String srcId;
    private String srcHost;
    private String destId;
    private String destHost;
    private String user;
    private boolean success = true;

    public Export(Dataset kos, String user, String srcId, String srcHost, String destId, String destHost) {
        this.kos = kos;
        this.user = user;
        this.srcId = srcId;
        this.srcHost = srcHost;
        this.destId = destId;
        this.destHost = destHost;
    }

    public final Dataset getKeyObject() {
        return kos;
    }


    /**
     * @return the defaultUser
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the dest
     */
    public String getDestinationID() {
        return destId;
    }

    /**
     * @return the destURL
     */
    public String getDestHost() {
        return destHost;
    }

    /**
     * @return the srcId
     */
    public String getSourceId() {
        return srcId;
    }

    public String getSourceHost() {
        return srcHost;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

}
