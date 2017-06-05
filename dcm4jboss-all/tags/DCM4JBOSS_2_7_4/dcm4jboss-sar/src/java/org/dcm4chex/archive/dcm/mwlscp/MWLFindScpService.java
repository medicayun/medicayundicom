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

package org.dcm4chex.archive.dcm.mwlscp;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.util.DTFormat;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2130 $ $Date: 2005-12-07 01:11:55 +0800 (周三, 07 12月 2005) $
 * @since 31.01.2004
 */
public class MWLFindScpService extends AbstractScpService
	implements NotificationListener {

    private static final int NO_OP = 0;
    private static final int UPDATE_STATUS = 1;
    private static final int REMOVE_ITEM = 2;
    
    private static final String[] ON_MPPS_RECEIVED = {
    		"NO_OP", "UPDATE_STATUS", "REMOVE_ITEM"
    };

	private static final NotificationFilterSupport mppsFilter = 
		new NotificationFilterSupport();
	static {
		mppsFilter.enableType(MPPSScpService.EVENT_TYPE_MPPS_RECEIVED);
		mppsFilter.enableType(MPPSScpService.EVENT_TYPE_MPPS_LINKED);
	}
	private static final String QUERY_XSL = "mwl-cfindrq.xsl";
	private static final String RESULT_XSL = "mwl-cfindrsp.xsl";
	private static final String QUERY_XML = "-mwl-cfindrq.xml";
	private static final String RESULT_XML = "-mwl-cfindrsp.xml";
    
	private ObjectName mppsScpServiceName;
	
    private int onMPPSReceived = NO_OP;
    private File coerceConfigDir;
    private File logDir;
    

    private MWLFindScp mwlFindScp = new MWLFindScp(this);
    private Hashtable templates = new Hashtable();

	public final String getOnMPPSReceived() {
		return ON_MPPS_RECEIVED[onMPPSReceived];
	}
	
	public final void setOnMPPSReceived(String onMPPSReceived) {
		int tmp = Arrays.asList(ON_MPPS_RECEIVED).indexOf(onMPPSReceived);
		if (tmp == -1) {
			throw new IllegalArgumentException(onMPPSReceived);
		}
		this.onMPPSReceived = tmp;
	}
	
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }
    
    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

	public final String getCoerceConfigDir() {
		return coerceConfigDir.getPath();
	}

	public final void setCoerceConfigDir(String path) {
		this.coerceConfigDir = new File(path.replace('/', File.separatorChar));
	}

    public final String getLogCallingAETs() {
		return mwlFindScp.getLogCallingAETs();
	}

	public final void setLogCallingAETs(String aets) {
		mwlFindScp.setLogCallingAETs(aets);
	}
	
	Templates getQueryCoercionTemplatesFor(String aet)
	throws TransformerConfigurationException {
		return getCoercionTemplatesFor(aet, QUERY_XSL);
	}
	
	Templates getResultCoercionTemplatesFor(String aet)
	throws TransformerConfigurationException {
		return getCoercionTemplatesFor(aet, RESULT_XSL);
	}
	
	private Templates getCoercionTemplatesFor(String aet, String fname)
	throws TransformerConfigurationException {
		File f = FileUtils.resolve(new File(new File(coerceConfigDir, aet), fname));
		if (!f.exists())
			return null;
		Templates tpl = (Templates) templates.get(f);
		if (tpl == null) {
			tpl = TransformerFactory.newInstance().newTemplates(
					new StreamSource(f));
			templates.put(f, tpl);
		}
		return tpl;
	}


	File getQueryLogFile(Date now, String callingAET) {
		return getLogFile(now, callingAET, QUERY_XML);
	}
	
	File getResultLogFile(Date now, String callingAET) {
		return getLogFile(now, callingAET, RESULT_XML);
	}
	
	private File getLogFile(Date now, String callingAET, String suffix) {
		File dir = new File(logDir, callingAET);
		dir.mkdir();
		return new File(dir, new DTFormat().format(now) + suffix);
	}

	public void reloadStylesheets() {
		templates.clear();
	}	
    
    protected void startService() throws Exception {
        logDir = new File(ServerConfigLocator.locate().getServerHomeDir(), "log");
        server.addNotificationListener(mppsScpServiceName,
                this,
                mppsFilter,
                null);
        super.startService();
    }

    protected void stopService() throws Exception {
        super.stopService();
        server.removeNotificationListener(mppsScpServiceName,
                this,
                mppsFilter,
                null);
		templates.clear();
    }
    
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.ModalityWorklistInformationModelFIND, mwlFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.ModalityWorklistInformationModelFIND);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.ModalityWorklistInformationModelFIND,
                enable ? getTransferSyntaxUIDs() : null);
    }

    private MWLManagerHome getMWLManagerHome() throws HomeFactoryException {
        return (MWLManagerHome) EJBHomeFactory.getFactory().lookup(
                MWLManagerHome.class, MWLManagerHome.JNDI_NAME);
    }

	private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }
    
	private Dataset getMPPS(String iuid) throws Exception {
		MPPSManager mgr = getMPPSManagerHome().create();
		try {
			return mgr.getMPPS(iuid);
		} finally {
			try {
				mgr.remove();
			} catch (Exception ignore) {
			}
		}
	}
	
    public void handleNotification(Notification notif, Object handback) {
    	if (onMPPSReceived == NO_OP) return; 
        Dataset mpps = (Dataset) notif.getUserData();
		final String iuid = mpps.getString(Tags.SOPInstanceUID);
		final String status = mpps.getString(Tags.PPSStatus);
        DcmElement sq = mpps.get(Tags.ScheduledStepAttributesSeq);
        if (sq == null) {
        	// MPPS N-SET can be ignored for REMOVE_ITEM
        	// or if status == IN PROGRESS
        	if (onMPPSReceived == REMOVE_ITEM || "IN PROCESS".equals(status))
        		return;
        	try {
				mpps = getMPPS(iuid);
				sq = mpps.get(Tags.ScheduledStepAttributesSeq);
			} catch (Exception e) {
				log.error("Failed to load MPPS - " + iuid, e);
				return;
			}
        }
        MWLManager mgr;
        try {
            mgr = getMWLManagerHome().create();
        } catch (Exception e) {
            log.error("Failed to access MWL Manager:", e);
            return;
        }
        try {
            for (int i = 0, n = sq.vm(); i < n; ++i) {
                Dataset item = sq.getItem(i);
                String spsid = item.getString(Tags.SPSID);
                if (spsid != null) {
                	if (onMPPSReceived == REMOVE_ITEM) {
	                    try {
	                        if (mgr.removeWorklistItem(spsid) != null) {
	                        	log.info("Removed MWL item[spsid=" + spsid + "]");
	                        } else {
	                        	log.warn("No such MWL item[spsid=" + spsid + "]");
	                        }
	                    } catch (Exception e) {
	                        log.error("Failed to remove MWL item[spsid="
	                        		+ spsid + "]", e);
	                    }
                	} else { // onMPPSReceived == UPDATE_STATUS
	                    try {
	                        mgr.updateSPSStatus(spsid, status);
	                        log.info("Update MWL item[spsid=" + spsid
	                        		+ ", status=" + status + "]");
	                    } catch (Exception e) {
	                        log.error("Failed to update MWL item[spsid="
	                        		+ spsid + "]", e);
	                    }
                	}
                }
            }
        } finally {
            try {
                mgr.remove();
            } catch (Exception ignore) {
            }
        }
    }
    
}
