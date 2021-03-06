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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.dcm.stgcmt;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.SeriesStored;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gmail.com
 * @version $Revision:  $ $Date: 2010-11-17 10:17:40 +0100 (Mi, 17 Nov 2010) $
 * @since Nov 17, 2010
 */
public class StgCmtScuBySeriesStoredService extends ServiceMBeanSupport {

    private static final String NONE = "NONE";
    private static final String NEW_LINE = System.getProperty("line.separator", "\n");

    protected ObjectName stgCmtServiceName;
    private ObjectName storeScpServiceName;
    
    Map<String, String> rqStgCmtOnReceiveFromAETs = new HashMap<String, String>();
    
    private NotificationListener seriesStoredListener;

    public ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }


    public final ObjectName getStgCmtServiceName() {
        return stgCmtServiceName;
    }

    public final void setStgCmtServiceName(ObjectName name) {
        this.stgCmtServiceName = name;
    }

    public String getRqStgCmtOnReceiveFromAETs() {
        if (rqStgCmtOnReceiveFromAETs.isEmpty())
            return NONE;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : rqStgCmtOnReceiveFromAETs.entrySet()) {
            sb.append(entry.getKey());
            if (!entry.getKey().equals(entry.getValue()))
                sb.append('=').append(entry.getValue());
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }
    public void setRqStgCmtOnReceiveFromAETs(String s) throws InstanceNotFoundException, ListenerNotFoundException {
        rqStgCmtOnReceiveFromAETs.clear();
        if (!NONE.equals(s)) {
            StringTokenizer st = new StringTokenizer(s, ";\n\r\t");
            String tk = null;
            int pos;
            while (st.hasMoreTokens()) {
                tk = st.nextToken();
                pos = tk.indexOf('=');
                if (pos == -1) {
                    rqStgCmtOnReceiveFromAETs.put(tk, tk);
                } else {
                    rqStgCmtOnReceiveFromAETs.put(tk.substring(0, pos), tk.substring(++pos));
                }
            }
        }
        updateSeriesStoredListener();
    }
    
    private void updateSeriesStoredListener() throws InstanceNotFoundException, ListenerNotFoundException {
        if (server != null) {
            if (rqStgCmtOnReceiveFromAETs.isEmpty()) {
                if (seriesStoredListener != null) {
                    server.removeNotificationListener(storeScpServiceName, seriesStoredListener, SeriesStored.NOTIF_FILTER, null);
                    seriesStoredListener = null;
                }
            } else if (seriesStoredListener == null){
                seriesStoredListener = new NotificationListener() {
                    public void handleNotification(Notification notif, Object handback) {
                        onSeriesStored((SeriesStored) notif.getUserData());
                    }
                };
                server.addNotificationListener(storeScpServiceName, seriesStoredListener, SeriesStored.NOTIF_FILTER, null);
            }
        }
    }

    protected void startService() throws Exception {
        super.startService();
        updateSeriesStoredListener();
    }

    protected void stopService() throws Exception {
        updateSeriesStoredListener();
        super.stopService();
    }

    
    private void onSeriesStored(SeriesStored stored) {
        String aet = rqStgCmtOnReceiveFromAETs.get(stored.getSourceAET());
        if (aet != null) {
            Dataset actionInfo = stored.getIAN().get(Tags.RefSeriesSeq).getItem();
            String callingAet = null;
            int pos = aet.indexOf(':'); 
            if (pos != -1) {
                callingAet = aet.substring(0, pos);
                aet = aet.substring(++pos);
            }
            try {
                log.info("Queue StgCmt Order! calling:"+callingAet+" called:"+aet);
                server.invoke(stgCmtServiceName, "queueStgCmtOrder", new Object[] {
                        callingAet, aet, actionInfo, Boolean.FALSE }, new String[] {
                        String.class.getName(), String.class.getName(),
                        Dataset.class.getName(), boolean.class.getName() });
            } catch (Exception x) {
                log.error("Failed to queue StorageCommit Order! calledAet:"+aet, x);
                log.debug(actionInfo);
            }
        }
    }
    
}
