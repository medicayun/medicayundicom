/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.auditlog;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3558 $ $Date: 2002-09-01 21:44:11 +0800 (周日, 01 9月 2002) $
 * @since September 1, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public interface InstancesAction {
    
    // Constants -----------------------------------------------------
    
    // Methods -------------------------------------------------------
    void setAccessionNumber(String accessionNumber);
    
    void addStudyInstanceUID(String suid);

    String[] listStudyInstanceUIDs();
    
    void addSOPClassUID(String cuid);
    
    void clearSOPClassUIDs();
    
    String[] listSOPClassUIDs();
    
    void setUser(User user);
    
    void setNumberOfInstances(int numberOfInstances);
    
    void incNumberOfInstances(int inc);
    
    void setMPPSInstanceUID(String mppsUID);
    
    String getMPPSInstanceUID();

    void writeTo(StringBuffer sb);
}
