/* $Id: HL7MergePatientService.java 1050 2004-03-17 11:55:19Z umbertoc $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.hl7;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdate;

import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.SegmentFinder;
/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1050 $ $Date: 2004-03-17 19:55:19 +0800 (周三, 17 3月 2004) $
 * @since 08.03.2004
 * 
 * @jmx.mbean extends="org.dcm4chex.archive.hl7.HL7AcceptServiceMBean"
 */
public class HL7MergePatientService extends HL7AcceptService
		implements
			org.dcm4chex.archive.hl7.HL7MergePatientServiceMBean {
	
	private final Application handler = new DefaultApplication() {
		public Message processMessage(Message in) throws ApplicationException {
			Message out = null;
			PatientDTO dominant = null;
			PatientDTO prior = null;
			try {
				Segment msh = (Segment) in.get("MSH");
				SegmentFinder finder = new SegmentFinder(in);
				Segment pid = finder.findSegment("PID",0);
				Segment mrg = finder.findSegment("MRG",0);
				dominant = HL7Utils.makePatientIODFromPID(msh, pid);
				prior = HL7Utils.makePatientIODFromMRG(msh, mrg);
				PatientUpdate update = getPatientUpdateHome().create();
				try {
					update.mergePatient(dominant, new PatientDTO[]{prior});
				} finally {
					update.remove();
				}
				//get default ACK
				out = makeACK(msh);
			} catch (Exception e) {
				log.error("Failed to merge " + prior + " with " + dominant, e);
				throw new ApplicationException(
						"Couldn't create response message: " + e.getMessage());
			}
			return out;
		}
	};
	protected Application getApplication() {
		return handler;
	}
}
