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

package org.dcm4chex.archive.web.maverick.mpps;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.jdbc.MPPSQueryCmd;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MPPSDelegate {
		

	private static MBeanServer server;

	private static Logger log = Logger.getLogger( MPPSDelegate.class.getName() );

	private ObjectName mppsScpServiceName;


	public Logger getLogger() {
		return log;
	}
	
	/**
	 * Iinitialize the MPPS service delegator.
	 * <p>
	 * Set the name of the MppsService MBean with the servlet config param
	 * 'mppsScpServiceName'.
	 * 
	 * @param config
	 *            The ServletConfig object.
	 */
	public void init(ServletConfig config) {
		if (server != null)
			return;
		server = MBeanServerLocator.locate();
		String s = config.getInitParameter("mppsScpServiceName");
		try {
			mppsScpServiceName = new ObjectName(s);
		} catch (Exception e) {
			log.error("Exception in init! ", e);
		}

	}
	
		
		/**
		 * Makes the MBean call to get the list of worklist entries for given filter (ds).
		 * 
		 * @param filter	The WADO request.
		 * 
		 * @return The list of worklist entries ( Each item in the list is a Dataset of one scheduled procedure step).
		 */
		public List findMppsEntries( MPPSFilter filter ) {
			List resp = null;
			MPPSQueryCmd cmd = null;
			try {
				resp = new ArrayList();
				cmd = new MPPSQueryCmd( filter.toSearchDS(), filter.isEmptyAccNo() );
				cmd.execute();
				while ( cmd.next() ) {
					resp.add( cmd.getDataset() );
				}
			} catch ( Exception x ) {
				log.error( "Exception occured in getMWLEntries: "+x.getMessage(), x );
			}
			if ( cmd != null ) cmd.close();
	        return resp;
		}
		
		
		/**
		 * Deletes MPPS entries specified by an array of MPPS IUIDs.
		 * <p>
		 * 
		 * @param iuids  The List of Instance UIDs of the MPPS Entries to delete.
		 * @return
		 */
		public boolean deleteMPPSEntries(String[] iuids) {
			try {
				Object o = server.invoke(mppsScpServiceName, "deleteMPPSEntries",
						new Object[] { iuids }, new String[] { String[].class
								.getName() });
				return ((Boolean) o).booleanValue();
			} catch (Exception x) {
				log.error("Exception occured in deleteMPPSEntries: " + x.getMessage(),
						x);
			}
			return false;
		}

		/**
		 * @param mppsIUIDs
		 */
		public boolean unlinkMPPS(String[] mppsIUIDs) {
			try {
				for ( int i = 0 ; i < mppsIUIDs.length ; i++ ) {
					server.invoke(mppsScpServiceName, "unlinkMpps",
							new Object[] { mppsIUIDs[i] }, new String[] { String.class.getName() });
				}
				return true;
			} catch (Exception x) {
				log.error("Exception occured in unlinkMPPS: " + x.getMessage(),x);
				return false;
			}
		}

}
