/* $Id: HL7Utils.java 1034 2004-03-09 22:54:38Z gunterze $
 * Copyright (c) 2004 by TIANI MEDGRAPH AG
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
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;
/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1034 $ $Date: 2004-03-10 06:54:38 +0800 (周三, 10 3月 2004) $
 * @since 09.03.2004
 *  
 */
class HL7Utils {
	private final static String[] CHARSET = {"ISO_IR 100", "ISO_IR 101",
			"ISO_IR 109", "ISO_IR 110", "ISO_IR 144", "ISO_IR 127",
			"ISO_IR 126", "ISO_IR 138", "ISO_IR 148"};
	private static String toDicomCS(String hl7CS) {
		if (hl7CS != null) {
			if (hl7CS.startsWith("8859/")) {
				try {
					return CHARSET[Integer.parseInt(hl7CS.substring(5))];
				} catch (Exception e) {
				}
			}
		}
		return "ISO_IR 100";
	}
	private static String toDate(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() < 8) {
			return "";
		}
		return s.substring(0, 4) + "/" + s.substring(4, 6) + "/"
				+ s.substring(6, 8);
	}
	private static String toSex(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() == 1 && "FMO".indexOf(s.charAt(0)) != -1) {
			return s;
		}
		return "";
	}
	public static PatientDTO makePatientIODFromPID(Segment msh, Segment pid)
			throws HL7Exception {
		PatientDTO dto = new PatientDTO();
		dto.setSpecificCharacterSet(toDicomCS(Terser.get(msh, 18, 0, 1, 1)));
		dto.setPatientID(Terser.get(pid, 3, 0, 1, 1));
		dto.setIssuerOfPatientID(Terser.get(pid, 3, 0, 4, 1));
		dto.setPatientName(toPN(pid, 5));
		dto.setPatientBirthDate(toDate(Terser.get(pid, 7, 0, 1, 1)));
		dto.setPatientSex(toSex(Terser.get(pid, 8, 0, 1, 1)));
		return dto;
	}
	private static String toPN(Segment seg, int field) throws HL7Exception {
		String lastName = Terser.get(seg, field, 0, 1, 1);
		if (lastName == null) {
			return null;
		}
		if (lastName.length() == 0) {
			return "";
		}
		return lastName + '^' + maskNull(Terser.get(seg, 5, 0, 2, 1)) + '^'
				+ maskNull(Terser.get(seg, 5, 0, 3, 1)) + '^'
				+ maskNull(Terser.get(seg, 5, 0, 5, 1)) + '^'
				+ maskNull(Terser.get(seg, 5, 0, 4, 1));
	}
	private static String maskNull(String s) {
		return s == null ? "" : s;
	}
	public static PatientDTO makePatientIODFromMRG(Segment msh, Segment mrg)
			throws HL7Exception {
		PatientDTO dto = new PatientDTO();
		dto.setSpecificCharacterSet(toDicomCS(Terser.get(msh, 18, 0, 1, 1)));
		dto.setPatientID(Terser.get(mrg, 1, 0, 1, 1));
		dto.setIssuerOfPatientID(Terser.get(mrg, 1, 0, 4, 1));
		dto.setPatientName(toPN(mrg,7));
		return dto;
	}
}
