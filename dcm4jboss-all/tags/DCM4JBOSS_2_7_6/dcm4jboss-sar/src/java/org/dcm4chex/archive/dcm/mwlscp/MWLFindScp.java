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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.xml.transform.Templates;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.util.DAFormat;
import org.dcm4che.util.TMFormat;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.jdbc.MWLQueryCmd;
import org.dcm4chex.archive.util.XSLTUtils;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2121 $ $Date: 2005-12-06 21:59:12 +0800 (周二, 06 12月 2005) $
 */
public class MWLFindScp extends DcmServiceBase {
    private final MWLFindScpService service;
	private final Logger log;
    private LinkedHashSet logCallingAETs = new LinkedHashSet();

	public MWLFindScp(MWLFindScpService service) {
        this.service = service;
		this.log = service.getLog();
    }

    public final String getLogCallingAETs() {
		return set2str(logCallingAETs, '\\');
	}

	public final void setLogCallingAETs(String aets) {
		str2set(aets, logCallingAETs, '\\');
	}

	private static String set2str(LinkedHashSet set, char delim) {
		if (set.isEmpty())
            return "NONE";
        StringBuffer sb = new StringBuffer();
        Iterator it = set.iterator();
        sb.append(it.next());
        while (it.hasNext())
            sb.append(delim).append(it.next());
        return sb.toString();
	}

	private static void str2set(String aets, LinkedHashSet set, char delim) {
		set.clear();
        if ("NONE".equals(aets))
            return;
        set.addAll(Arrays.asList(StringUtils
                .split(aets, delim)));
	}
	
	protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq, Command rspCmd)
	throws IOException, DcmServiceException {
		Association a = assoc.getAssociation();
		String callingAET = a.getCallingAET();
		Date now = new Date();
		Dataset rqData = rq.getDataset();
		log.debug("Identifier:\n");
		log.debug(rqData);
		if (logCallingAETs.contains(callingAET))
			try {
				XSLTUtils.writeTo(rqData, service.getQueryLogFile(now, callingAET));
			} catch (Exception e) {
				log.warn("Logging of query attributes failed:", e);
			}
		try {
			Templates stylesheet = service.getQueryCoercionTemplatesFor(callingAET);
			if (stylesheet != null)
			{
				Dataset coerced = XSLTUtils.coerce(rqData, stylesheet,
						toXsltParam(a, now));
				log.debug("Coerce attributes:\n");
				log.debug(coerced);
				log.debug("Coerced Identifier:\n");
				log.debug(rqData);
			}
		} catch (Exception e) {
			log.warn("Coercion of query attributes failed:", e);
		}
		
		MWLQueryCmd queryCmd;
		try {
			queryCmd = new MWLQueryCmd(rqData);
			queryCmd.execute();
		} catch (Exception e) {
			log.error("Query DB failed:", e);
			throw new DcmServiceException(Status.ProcessingFailure, e);
		}
		return new MultiCFindRsp(queryCmd);
	}

	private Map toXsltParam(Association a, Date now) {
		HashMap param = new HashMap();
		param.put("calling", a.getCallingAET());
		param.put("called", a.getCalledAET());
		param.put("date", new DAFormat().format(now));
		param.put("time", new TMFormat().format(now));
		return null;
	}

	private class MultiCFindRsp implements MultiDimseRsp {
        private final MWLQueryCmd queryCmd;
        private boolean canceled = false;

        public MultiCFindRsp(MWLQueryCmd queryCmd) {
            this.queryCmd = queryCmd;
        }

        public DimseListener getCancelListener() {
            return new DimseListener() {
                public void dimseReceived(Association assoc, Dimse dimse) {
                    canceled = true;
                }
            };
        }

        public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws DcmServiceException
        {
            if (canceled) {
                rspCmd.putUS(Tags.Status, Status.Cancel);
                return null;                
            }
            try {
                if (!queryCmd.next()) {
                    rspCmd.putUS(Tags.Status, Status.Success);
                    return null;
                }
        		Association a = assoc.getAssociation();
        		String callingAET = a.getCallingAET();
        		Date now = new Date();
                rspCmd.putUS(Tags.Status, Status.Pending);
                Dataset rspData = queryCmd.getDataset();
				log.debug("Identifier:\n");
				log.debug(rspData);				
				if (logCallingAETs.contains(callingAET))
					try {
						XSLTUtils.writeTo(rspData,
								service.getResultLogFile(now, callingAET));
					} catch (Exception e) {
						log.warn("Logging of result attributes failed:", e);
					}
				try {
					Templates stylesheet = 
						service.getResultCoercionTemplatesFor(callingAET);
					if (stylesheet != null)
					{
						Dataset coerced = XSLTUtils.coerce(rspData, stylesheet,
								toXsltParam(a, now));
						log.debug("Coerce attributes:\n");
						log.debug(coerced);
						log.debug("Coerced Identifier:\n");
						log.debug(rspData);
					}
				} catch (Exception e) {
					log.warn("Coercion of result attributes failed:", e);
				}
                return rspData;
            } catch (SQLException e) {
                log.error("Retrieve DB record failed:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);                
            } catch (Exception e) {
                log.error("Corrupted DB record:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);                
            }                        
        }

        public void release() {
            queryCmd.close();
        }
    }
}
