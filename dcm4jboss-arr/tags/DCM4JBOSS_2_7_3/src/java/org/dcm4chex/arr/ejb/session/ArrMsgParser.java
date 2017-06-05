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
 * Joe Foraci <jforaci@users.sourceforge.net>
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

package org.dcm4chex.arr.ejb.session;

import java.util.Date;

import org.xml.sax.InputSource;

/**
 * @author joseph foraci
 *
 * This is the interface for a Audit Record Repository service that may be used
 * to parse a IHE audit message.
 */
public interface ArrMsgParser {
    public static final int INVALID_XML = 1;        //not well-formed xml data (or complete garbage)
    public static final int INVALID_SCHEMA = 2;     //xml data is well-formed, but data does not validate
    public static final int INVALID_INCOMPLETE = 4; //could not interpret some/all information such as Hostname, TimeStamp, or Type
    
	int parse(String xmlData)
		throws ArrInputException;
    
	int parse(InputSource is)
		throws ArrInputException;
    
	/**
	 * Get the audit type from parsed data (ie the name of the audit event
	 * element that was generated for the triggering event).
	 *
	 * @return A <code>String</code> representing the audit type or
	 *   <code>null</code> if it was not included.
	 */
	String getType();
    
	/** Get parsed Hostname from Host element
	 *
	 * @return A <code>String</code> representing the host mentioned or
	 *   <code>null</code> if it was not included.
	 */
	String getHost();
    
	/** Get parsed TimeStamp from Timestamp element
	 *
	 * @return A <code>Date</code> representing the time stamp mentioned or
	 *   <code>null</code> if it was not included.
	 */
	Date getTimeStamp();
    
    String getAet();
    
    String getUserName();
    
    String getPatientName();
    
    String getPatientId();
}
