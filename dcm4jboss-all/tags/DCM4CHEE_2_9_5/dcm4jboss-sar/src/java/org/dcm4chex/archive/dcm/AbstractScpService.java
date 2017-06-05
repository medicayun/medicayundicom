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

package org.dcm4chex.archive.dcm;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.PDataTF;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.DAFormat;
import org.dcm4che.util.DTFormat;
import org.dcm4che.util.TMFormat;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.XSLTUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2524 $ $Date: 2006-06-12 17:44:24 +0800 (周一, 12 6月 2006) $
 * @since 31.08.2003
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {

    protected static final String ANY = "ANY";
    protected static final String NONE = "NONE";

    protected ObjectName dcmServerName;

    protected ObjectName auditLogName;
    
    protected DcmHandler dcmHandler;

    protected String[] calledAETs;

    protected String[] callingAETs;
        
    /** Map containing accepted Transfer Syntax UIDs.
     * key is name (as in config string), value is real uid) */
    protected Map tsuidMap = new LinkedHashMap();

    protected int maxPDULength = PDataTF.DEF_MAX_PDU_LENGTH;
    protected int maxOpsInvoked = 1;
    protected int maxOpsPerformed = 1;
    protected String[] logCallingAETs = {};
    protected File logDir;
    protected File coerceConfigDir;
    protected Hashtable templates = new Hashtable();
        
    public final ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public final void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public final ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public final void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public final String getCalledAETs() {
        return calledAETs == null ? "":StringUtils.toString(calledAETs, '\\');
    }
    
    public final void setCalledAETs(String calledAETs) {
    	if ( getCalledAETs().equals(calledAETs)) return;
        disableService();
        this.calledAETs = StringUtils.split(calledAETs, '\\');
        enableService();
    }

    public final String getLogCallingAETs() {
        return StringUtils.toString(logCallingAETs, '\\');
    }

    public final void setLogCallingAETs(String aets) {
        logCallingAETs = StringUtils.split(aets, '\\');
    }

	public final int getMaxPDULength() {
		return maxPDULength;
	}
	
	public final void setMaxPDULength(int maxPDULength) {
		if ( this.maxPDULength == maxPDULength ) return;
		this.maxPDULength = maxPDULength;
		enableService();
	}
	
    public final int getMaxOpsInvoked() {
		return maxOpsInvoked;
	}

	public final void setMaxOpsInvoked(int maxOpsInvoked) {
		if ( this.maxOpsInvoked == maxOpsInvoked ) return;
		this.maxOpsInvoked = maxOpsInvoked;
		enableService();
	}

	public final int getMaxOpsPerformed() {
		return maxOpsPerformed;
	}

	public final void setMaxOpsPerformed(int maxOpsPerformed) {
		if ( this.maxOpsPerformed == maxOpsPerformed ) return;
		this.maxOpsPerformed = maxOpsPerformed;
		enableService();
	}

    public final String getCoerceConfigDir() {
        return coerceConfigDir.getPath();
    }

    public final void setCoerceConfigDir(String path) {
        this.coerceConfigDir = new File(path.replace('/', File.separatorChar));
    }
    
	protected void enableService() {
        if (dcmHandler == null) return;
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < calledAETs.length; ++i) {
            AcceptorPolicy policy1 = policy.getPolicyForCalledAET(calledAETs[i]);
            if (policy1 == null) {
                policy1 = AssociationFactory.getInstance().newAcceptorPolicy();
                policy1.setCallingAETs(callingAETs);
                policy.putPolicyForCalledAET(calledAETs[i], policy1);                
                policy.addCalledAET(calledAETs[i]);
            } else {
                if (policy1.getCallingAETs().length > 0) {
                    if (callingAETs == null) {
                        policy1.setCallingAETs(null);
                    } else {
                        for (int j = 0; j < callingAETs.length; j++) {
                            policy1.addCallingAET(callingAETs[j]);
                        }
                    }
                }
            }
            policy1.setMaxPDULength(maxPDULength);
 			policy1.setAsyncOpsWindow(maxOpsInvoked, maxOpsPerformed);
            updatePresContexts(policy1, true);
        }
    }

    private void disableService() {
        if (dcmHandler == null) return;
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < calledAETs.length; ++i) {
            AcceptorPolicy policy1 = policy.getPolicyForCalledAET(calledAETs[i]);
            if (policy1 != null) {
                updatePresContexts(policy1, false);
                if (policy1.listPresContext().isEmpty()) {
                    policy.putPolicyForCalledAET(calledAETs[i], null);
                    policy.removeCalledAET(calledAETs[i]);
                }
            }
        }
    }

    public final String getCallingAETs() {
        return callingAETs != null ? StringUtils.toString(callingAETs, '\\') : ANY;
    }

    public final void setCallingAETs(String callingAETs) {
    	if ( getCallingAETs().equals(callingAETs)) return;
        this.callingAETs = ANY.equalsIgnoreCase(callingAETs) ? null 
                : StringUtils.split(callingAETs, '\\');
        enableService();
    }
    
    protected void updateAcceptedSOPClass(Map cuidMap, String newval, DcmService scp) {
        Map tmp = parseUIDs(newval);
        if (cuidMap.keySet().equals(tmp.keySet())) return;
        disableService();
        if (scp != null)
            unbindAll(valuesToStringArray(cuidMap));
        cuidMap.clear();
        cuidMap.putAll(tmp);
        if (scp != null)
            bindAll(valuesToStringArray(cuidMap), scp);
        enableService();
    }
    
//    protected String[] getTransferSyntaxUIDs() {
//        return valuesToStringArray(tsuids);
//    }

    protected static String[] valuesToStringArray(Map tsuid) {
         return (String[]) tsuid.values().toArray(new String[tsuid.size()]);
    }

    protected void bindAll(String[] cuids, DcmService scp) {
        if ( dcmHandler == null ) return; //nothing to do!
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++) {
            services.bind(cuids[i], scp);
        }
    }

    protected void unbindAll(String[]  cuids) {
        if ( dcmHandler == null ) return; //nothing to do!
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < cuids.length; i++) {
            services.unbind(cuids[i]);
        }
    }

    public String getAcceptedTransferSyntax() {
        return toString(tsuidMap);
    }

    public void setAcceptedTransferSyntax(String s) {
        updateAcceptedTransferSyntax(tsuidMap, s);
    }
       
    protected void updateAcceptedTransferSyntax(Map tsuidMap, String newval) {
        Map tmp = parseUIDs(newval);
        if (tsuidMap.keySet().equals(tmp.keySet())) return;
        tsuidMap.clear();
        tsuidMap.putAll(tmp);
        enableService();
    }
    
    protected String toString(Map uids) {
        if ( uids == null || uids.isEmpty() ) return "";
        String nl = System.getProperty("line.separator", "\n");
        StringBuffer sb = new StringBuffer();
        Iterator iter = uids.keySet().iterator();
        while ( iter.hasNext() ) {
            sb.append(iter.next()).append(nl);
        }
        return sb.toString();
    }
    
    protected static Map parseUIDs(String uids) {
        StringTokenizer st = new StringTokenizer(uids, " \t\r\n;");
        String uid,name;
        Map map = new LinkedHashMap();
        while ( st.hasMoreTokens() ) {
            uid = st.nextToken().trim();
            name = uid;
            
            if (isDigit(uid.charAt(0))) {
                if ( ! UIDs.isValid(uid) ) 
                    throw new IllegalArgumentException("UID "+uid+" isn't a valid UID!");
            } else {
                uid = UIDs.forName( name );
            }
            map.put(name,uid);
        }
        return map;
    }
    
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    protected void startService() throws Exception {
        logDir = new File(ServerConfigLocator.locate().getServerHomeDir(), "log");
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, "dcmHandler",
                null, null);
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        enableService();
    }

    protected void stopService() throws Exception {
        disableService();
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
        templates.clear();
    }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);
    
    protected abstract void updatePresContexts(AcceptorPolicy policy, boolean enable);

    protected void putPresContexts(AcceptorPolicy policy, String[] cuids,
            String[] tsuids) {
        for (int i = 0; i < cuids.length; i++) {
            policy.putPresContext(cuids[i], tsuids);
        }
    }
    
    protected void putRoleSelections(AcceptorPolicy policy, String[] cuids, boolean scu, boolean scp)
    {
    	for (int i = 0; i < cuids.length; i++) {
            policy.putRoleSelection(cuids[i], scu, scp);
        }
    }

    public File getLogFile(Date now, String callingAET, String suffix) {
        File dir = new File(logDir, callingAET);
        dir.mkdir();
        return new File(dir, new DTFormat().format(now) + suffix);
    }
    
    public Templates getCoercionTemplatesFor(String aet, String fname)
    throws TransformerConfigurationException {
        // check AET specific attribute coercion configuration
        File f = FileUtils.resolve(
                new File(new File(coerceConfigDir, aet), fname));
        if (!f.exists()) {
            // check general attribute coercion configuration
            f = FileUtils.resolve(new File(coerceConfigDir, fname));
            if (!f.exists()) {
                return null;
            }
        }
        Templates tpl = (Templates) templates.get(f);
        if (tpl == null) {
            tpl = TransformerFactory.newInstance().newTemplates(
                    new StreamSource(f));
            templates.put(f, tpl);
        }
        return tpl;
    }

    public void reloadStylesheets() {
        templates.clear();
    }
    
    private boolean contains(Object[] a, Object e) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(e)) {
                return true;
            }
        }
        return false;
    }

    public void logDIMSE(Association a, String suffix, Dataset ds) {
        String callingAET = a.getCallingAET();
        if (contains(logCallingAETs, callingAET )) {
            try {
                XSLTUtils.writeTo(ds, getLogFile(new Date(), callingAET, suffix));
            } catch (Exception e) {
                log.warn("Logging of attributes failed:", e);
            }
        }
    }

    public void coerceDIMSE(Association a, String xsl, Dataset rqData) {
        String callingAET = a.getCallingAET();
        try {
            Templates stylesheet = getCoercionTemplatesFor(callingAET, xsl);
            if (stylesheet != null)
            {
                Dataset coerced = XSLTUtils.coerce(rqData, stylesheet,
                        toXsltParam(a));
                log.debug("Coerce attributes:\n");
                log.debug(coerced);
                log.debug("Coerced Identifier:\n");
                log.debug(rqData);
            }
        } catch (Exception e) {
            log.warn("Coercion of query attributes failed:", e);
        }
    }

    private Map toXsltParam(Association a) {
        Date now = new Date();
        HashMap param = new HashMap();
        param.put("calling", a.getCallingAET());
        param.put("called", a.getCalledAET());
        param.put("date", new DAFormat().format(now ));
        param.put("time", new TMFormat().format(now));
        return param;
    }

}
