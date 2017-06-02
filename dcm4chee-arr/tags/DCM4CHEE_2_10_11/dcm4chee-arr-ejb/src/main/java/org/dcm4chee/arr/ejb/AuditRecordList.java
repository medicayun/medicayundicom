/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla License Version
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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General License Version 2 or later (the "GPL"), or
 * the GNU Lesser General License Version 2.1 or later (the "LGPL"),
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
package org.dcm4chee.arr.ejb;

import javax.ejb.Local;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$
 * @since 29.07.2006
 */
@Local
interface AuditRecordList {

    int getPageSize();
    void setPageSize(int pageSize);
    
    int getMinPage();
    int getCurPage();
    int getMaxPage();
    int getLastPage();
    int getCount();
    int getFirstResult();
    
    boolean isOrderByEventDateTime();
    void setOrderByEventDateTime(boolean enable);
    
    String getDateTimeRange();
    void setDateTimeRange(String dt);

    String[] getEventIDs();
    void setEventIDs(String[] ids);
        
    String[] getEventTypes();
    void setEventTypes(String[] types);
    
    String[] getEventActions();
    void setEventActions(String[] actions);

    String[] getEventOutcomes();
    void setEventOutcomes(String[] outcomes);
    
    String getUserID1();
    void setUserID1(String id);

    String getAltUserID1();
    void setAltUserID1(String id);

    String getUserName1();
    void setUserName1(String name);
    
    String getUserIsRequestor1();
    void setUserIsRequestor1(String requestor);
    
    String[] getRoleIDs1();    
    void setRoleIDs1(String[] ids);

    String[] getNapTypes1();   
    void setNapTypes1(String[] types);
    
    String getNapID1();
    void setNapID1(String id);        
    
    String getUserID2();
    void setUserID2(String id);

    String getAltUserID2();
    void setAltUserID2(String id);

    String getUserName2();
    void setUserName2(String name);
    
    String getUserIsRequestor2();
    void setUserIsRequestor2(String requestor);
    
    String[] getRoleIDs2();    
    void setRoleIDs2(String[] ids);

    String[] getNapTypes2();   
    void setNapTypes2(String[] types);
    
    String getNapID2();
    void setNapID2(String id);        
    
    String getSiteID();
    void setSiteID(String id);

    String getSourceID();
    void setSourceID(String id);

    String[] getSourceTypes();
    void setSourceTypes(String[] types);
    
    String[] getObjectTypes();
    void setObjectTypes(String[] types);

    String[] getObjectRoles();
    void setObjectRoles(String[] roles);

    String[] getLifeCycles();
    void setLifeCycles(String[] lifeCycles);
    
    String[] getObjectIDTypes();
    void setObjectIDTypes(String[] types);

    String getObjectID();
    void setObjectID(String id);

    String getObjectName();
    void setObjectName(String name);
    
    String find();
    void selectPage();
    
    boolean isShowXml();
    void showXml();
    void showDetails();
    
    String getXml();
    String getDetails();
    String getRowClasses();
    
    void destroy();

}
