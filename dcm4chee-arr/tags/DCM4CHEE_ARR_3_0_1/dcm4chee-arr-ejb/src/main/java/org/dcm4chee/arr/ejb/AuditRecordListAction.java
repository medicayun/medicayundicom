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
package org.dcm4chee.arr.ejb;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.dcm4chee.arr.util.XSLTUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RequestParameter;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelectionIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$
 * @since 29.07.2006
 */
@Stateful
@Name("auditRecordList")
@Scope(ScopeType.SESSION)
public class AuditRecordListAction implements Serializable, AuditRecordList {
    
    private static final long serialVersionUID = 8681124252579882410L;

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int YEAR = 4;
    private static final int MONTH = 7;
    private static final int DAY_OF_MONTH = 10;
    private static final int HOUR_OF_DAY = 13;
    private static final int MINUTE = 16;
    private static final int SECOND = 19;
    
    private static Logger log = LoggerFactory.getLogger(AuditRecordList.class);

    @PersistenceContext(type=PersistenceContextType.EXTENDED)
    private Session session;
    
    @DataModel
    private List<AuditRecord> records;
    
    @DataModelSelectionIndex
    private int selectedIndex = -1;
    
    @RequestParameter
    private Integer page;
    
    private int curPage = 1;
    private int pageSize = 20;
    private int count = 0;
    private boolean showXml = false;   
    private boolean orderByEventDateTime = false;
    
    private String dateTimeRange = today();
    private String[] eventIDs = { "" };
    private String[] eventTypes = { "" };
    private String[] eventActions = { "" };
    private String[] eventOutcomes = { "" };
    
    private String userID1 = "";
    private String altUserID1 = "";
    private String userName1 = "";
    private String userIsRequestor1 = "any";
    private String[] roleIDs1 = { "" };
    private String[] napTypes1 = { "" };
    private String napID1 = "";

    private String userID2 = "";
    private String altUserID2 = "";
    private String userName2 = "";
    private String userIsRequestor2 = "any";
    private String[] roleIDs2 = { "" };
    private String[] napTypes2 = { "" };
    private String napID2 = "";
    
    private String siteID = "";
    private String sourceID = "";
    private String[] sourceTypes = { "" };
    
    private String[] objectTypes = { "" };
    private String[] objectRoles = { "" };
    private String[] lifeCycles = { "" };
    private String[] objectIDTypes = { "" };
    private String objectID = "";
    private String objectName = "";

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return count;
    }

    public int getCurPage() {
        return curPage;
    }
    
    public int getMinPage() {
        return Math.max(1, Math.min(curPage, getLastPage() - 5) - 4);
    }
    
    public int getMaxPage() {
        return Math.min(getLastPage(), Math.max(5, curPage) + 4);
    }
    
    public int getLastPage() {
        return (count-1) / pageSize + 1;
    }
    
    public int getFirstResult() {
        return (curPage-1) * pageSize;
    }

    private static String today() {
	return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }
    
    public String getDateTimeRange() {
        return dateTimeRange;
    }

    public void setDateTimeRange(String dt) {
        this.dateTimeRange = dt;
    }

    public boolean isOrderByEventDateTime() {
        return orderByEventDateTime;
    }

    public void setOrderByEventDateTime(boolean enable) {
        this.orderByEventDateTime = enable;
    }

    public String[] getEventIDs() {
        return eventIDs;
    }

    public void setEventIDs(String[] ids) {
        this.eventIDs = ids;
    }
        
    public String[] getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(String[] types) {
        this.eventTypes = types;
    }

    public String[] getEventActions() {
        return eventActions;
    }

    public void setEventActions(String[] actions) {
        this.eventActions = actions;
    }

    public String[] getEventOutcomes() {
        return eventOutcomes;
    }

    public void setEventOutcomes(String[] outcomes) {
        this.eventOutcomes = outcomes;
    }
    
    public String getUserID1() {
        return userID1;
    }

    public void setUserID1(String id) {
        this.userID1 = id;
    }

    public String getAltUserID1() {
        return altUserID1;
    }

    public void setAltUserID1(String id) {
        this.altUserID1 = id;
    }

    public String getUserName1() {
        return userName1;
    }

    public void setUserName1(String name) {
        this.userName1 = name;
    }
    
    public String getUserIsRequestor1() {
        return userIsRequestor1;
    }

    public void setUserIsRequestor1(String requestor) {
        this.userIsRequestor1 = requestor;
    }

    public String[] getRoleIDs1() {
	return roleIDs1;
    }
    
    public void setRoleIDs1(String[] ids) {
	this.roleIDs1 = ids;
    }

    public String[] getNapTypes1() {
	return napTypes1;
    }
    
    public void setNapTypes1(String[] types) {
	this.napTypes1 = types;
    }
    
    public String getNapID1() {
        return napID1;
    }

    public void setNapID1(String id) {
        this.napID1 = id;
    }
    
    public String getUserID2() {
        return userID2;
    }

    public void setUserID2(String id) {
        this.userID2 = id;
    }

    public String getAltUserID2() {
        return altUserID2;
    }

    public void setAltUserID2(String id) {
        this.altUserID2 = id;
    }

    public String getUserName2() {
        return userName2;
    }

    public void setUserName2(String name) {
        this.userName2 = name;
    }
    
    public String getUserIsRequestor2() {
        return userIsRequestor2;
    }

    public void setUserIsRequestor2(String requestor) {
        this.userIsRequestor2 = requestor;
    }

    public String[] getRoleIDs2() {
	return roleIDs2;
    }
    
    public void setRoleIDs2(String[] ids) {
	this.roleIDs2 = ids;
    }

    public String[] getNapTypes2() {
	return napTypes2;
    }
    
    public void setNapTypes2(String[] types) {
	this.napTypes2 = types;
    }
    
    public String getNapID2() {
        return napID2;
    }

    public void setNapID2(String id) {
        this.napID2 = id;
    }
    
    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String id) {
        this.siteID = id;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String id) {
        this.sourceID = id;
    }

    public String[] getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(String[] types) {
        this.sourceTypes = types;
    }

    public String[] getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(String[] types) {
        this.objectTypes = types;
    }

    public String[] getObjectRoles() {
        return objectRoles;
    }

    public void setObjectRoles(String[] roles) {
        this.objectRoles = roles;
    }

    public String[] getLifeCycles() {
        return lifeCycles;
    }

    public void setLifeCycles(String[] lifeCycles) {
        this.lifeCycles = lifeCycles;
    }

    public String[] getObjectIDTypes() {
        return objectIDTypes;
    }

    public void setObjectIDTypes(String[] types) {
        this.objectIDTypes = types;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String id) {
        this.objectID = id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String name) {
        this.objectName = name;
    }

    @Factory("records")
    public String find() {
	curPage = 1;
	updateResults();
	return ("browse");
    }
    
    public void selectPage() {
	curPage = page;
	updateResults();
    }
        
    @SuppressWarnings("unchecked")
    private void updateResults() {        
        Criteria recordsCriteria = buildCriteria(session.createCriteria(AuditRecord.class));
        
        count = ((Integer)recordsCriteria
                .addOrder(Order.desc(orderByEventDateTime?"eventDateTime":"receiveDateTime"))
                .setProjection(Projections.rowCount())
                .list().iterator().next()).intValue();
        int from = getFirstResult();
        int to = Math.max(0, Math.min(count, getFirstResult() + pageSize));
   
        log.info(String.format("Found total %d records. Displaying from %d to %d",  count, (from+1), to));
        
        records = recordsCriteria
                .setProjection(null)
                .setResultTransformer( Criteria.ROOT_ENTITY )
                .setFirstResult(from)
                .setMaxResults(pageSize)
                .addOrder(Order.desc(orderByEventDateTime?"eventDateTime":"receiveDateTime"))
                .list();
        
        selectedIndex = -1;
    }
    
    private Criteria buildCriteria(Criteria criteria) {
        addCriteriaForEvent(criteria);
        addCriteriaForAuditSource(criteria);
        addCriteriaForParticipantObject(criteria);

        if(hasCriteriaForActiveParticipant1()) {
            if(hasCriteriaForActiveParticipant2()) {
                // !It's NOT working, see http://opensource.atlassian.com/projects/hibernate/browse/HHH-879
                // Wait for the fix. For now, we just use ONE ActiveParticipant criteria from JSF xhtml and don't have
                // to change the code here. (Sep 20, 2006)
                criteria.add( Expression.disjunction()
                        .add(addCriteriaForActiveParticipant1(criteria))
                        .add(addCriteriaForActiveParticipant2(criteria)));
            }
            else
                criteria.add(addCriteriaForActiveParticipant1(criteria));
        }
        else if(hasCriteriaForActiveParticipant2()) {
            criteria.add(addCriteriaForActiveParticipant2(criteria));
        }        
        
        return criteria;
    }
    
    private Criteria addCriteriaForEvent(Criteria criteria) {
        // Date/time
        Date[] dtRange;
        try {
            dtRange = parseDateTimeRange();
        } catch (Exception e) {
            dateTimeRange = today();
            try {
                dtRange = parseDateTimeRange();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
        if(dtRange != null)
            criteria.add(Expression.between(orderByEventDateTime?"eventDateTime":"receiveDateTime", 
                    dtRange[0], dtRange[1]));
        
        // Event ID
        if(hasCriteria(eventIDs))
            criteria.createAlias("eventID", "ei").add(getCodeStringCriteria("ei", eventIDs));
            
        // Event Type
        if(hasCriteria(eventTypes))
            criteria.createAlias("eventType", "et").add(getCodeStringCriteria("et", eventTypes));
        
        // Event Outcome
        if(hasCriteria(eventOutcomes))            
            criteria.add(Expression.in("eventOutcome",toIntegerArray(eventOutcomes)));
        
        // Event Action
        if(hasCriteria(eventActions))            
            criteria.add(Expression.in("eventAction",eventActions));
        
        return criteria;
    }
    
    private Criteria addCriteriaForAuditSource(Criteria criteria) {        
        // Site ID
        if(hasCriteria(siteID))
            criteria.add(Expression.ilike("enterpriseSiteID", siteID, MatchMode.START));
        
        // Source ID
        if(hasCriteria(sourceID))
            criteria.add(Expression.ilike("sourceID", sourceID, MatchMode.START));
        
        // Source Type
        if(hasCriteria(sourceTypes))
            criteria.add(Expression.in("sourceType",toIntegerArray(sourceTypes)));
        
        return criteria;
    }
    
    private Criterion addCriteriaForActiveParticipant1(Criteria criteria) {        
        criteria.createAlias("activeParticipant", "ap1");
        Conjunction criterion = Expression.conjunction();
        
        // User ID
        if(hasCriteria(userID1))
            criterion.add(Expression.ilike("ap1.userID", userID1, MatchMode.START));
        
        // Alt User ID
        if(hasCriteria(altUserID1))
            criterion.add(Expression.ilike("ap1.alternativeUserID", altUserID1, MatchMode.START));

        // User Name
        if(hasCriteria(userName1))
            criterion.add(Expression.ilike("ap1.userName", userName1, MatchMode.START));

        // Is Requestor
        if(hasCriteriaBoolean(userIsRequestor1))
            criterion.add(Expression.eq("ap1.userIsRequestor", Boolean.valueOf(userIsRequestor1)));
        
        // Role ID
        if(hasCriteria(roleIDs1)) {
            criteria.createAlias("ap1.roleID", "rid1");
            criterion.add(getCodeStringCriteria("rid1", roleIDs1));
        }
        
        // Network Access Point Type
        if(hasCriteria(napTypes1))
            criterion.add(Expression.in("ap1.networkAccessPointType",toIntegerArray(napTypes1)));
        
        // NAP ID
        if(hasCriteria(napID1))
            criterion.add(Expression.ilike("ap1.networkAccessPointID", napID1, MatchMode.START));
        
        return criterion;
    }

    private Criterion addCriteriaForActiveParticipant2(Criteria criteria) {        
        criteria.createAlias("activeParticipant", "ap2");
        Conjunction criterion = Expression.conjunction();
        
        // User ID
        if(hasCriteria(userID2))
            criterion.add(Expression.ilike("ap2.userID", userID2, MatchMode.START));
        
        // Alt User ID
        if(hasCriteria(altUserID2))
            criterion.add(Expression.ilike("ap2.alternativeUserID", altUserID2, MatchMode.START));

        // User Name
        if(hasCriteria(userName2))
            criterion.add(Expression.ilike("ap2.userName", userName2, MatchMode.START));

        // Is Requestor
        if(hasCriteriaBoolean(userIsRequestor2))
            criterion.add(Expression.eq("ap2.userIsRequestor", Boolean.valueOf(userIsRequestor2)));
        
        // Role ID
        if(hasCriteria(roleIDs2)) {
            criteria.createAlias("ap2.roleID", "rid2");
            criterion.add(getCodeStringCriteria("rid2", roleIDs2));
        }
        
        // Network Access Point Type
        if(hasCriteria(napTypes2))
            criterion.add(Expression.in("ap2.networkAccessPointType",toIntegerArray(napTypes2)));
        
        // NAP ID
        if(hasCriteria(napID2))
            criterion.add(Expression.ilike("ap2.networkAccessPointID", napID2+"%"));
        
        return criterion;
    }

    private Criteria addCriteriaForParticipantObject(Criteria criteria) {
        if(hasCriteria(objectTypes) || hasCriteria(objectRoles)
                || hasCriteria(lifeCycles) || hasCriteria(objectIDTypes)
                || hasCriteria(objectID) || hasCriteria(objectName) ) {
            criteria.createAlias("participantObject", "po");
        }
        else
            return criteria;
        
        // Object Type
        if(hasCriteria(objectTypes))
            criteria.add(Expression.in("po.objectType",toIntegerArray(objectTypes)));
        
        // Object Role
        if(hasCriteria(objectRoles))
            criteria.add(Expression.in("po.objectRole",toIntegerArray(objectRoles)));
        
        // Data Life Cycle
        if(hasCriteria(lifeCycles))
            criteria.add(Expression.in("po.dataLifeCycle",toIntegerArray(lifeCycles)));
        
        // Object ID Type
        if(hasCriteria(objectIDTypes)) {
            // Split the IDTypes into two sets, one for code, which is String;
            // one for RFC, which is integer
            List<Integer> rfcIDTypes = new ArrayList<Integer>();
            List<String> codeIDTypes = new ArrayList<String>();
            for(String idType : objectIDTypes) {
                if(idType.indexOf("^") > 0)
                    codeIDTypes.add(idType);
                else
                    rfcIDTypes.add(Integer.valueOf(idType));
            }
            
            if(codeIDTypes.size() > 0)
                criteria.createAlias("po.objectIDType", "pooit");
            
            if(rfcIDTypes.size() > 0) {
                if(codeIDTypes.size() > 0) {
                    criteria.add(Expression.or(
                            getCodeStringCriteria("pooit", codeIDTypes.toArray(new String[]{})),
                            Expression.in("po.objectIDTypeRFC", rfcIDTypes.toArray(new Integer[]{}))));                    
                }
                else
                    criteria.add(Expression.in("po.objectIDTypeRFC", rfcIDTypes.toArray(new Integer[]{})));
            }
            else
                criteria.add(getCodeStringCriteria("pooit", codeIDTypes.toArray(new String[]{})));            
        }
        
        // Object ID
        if(hasCriteria(objectID))
            criteria.add(Expression.ilike("po.objectID", objectID, MatchMode.START));

        // Object Name
        if(hasCriteria(objectName))
            criteria.add(Expression.ilike("po.objectName", objectName, MatchMode.START));

        return criteria;
    }
    
    private static Disjunction getCodeStringCriteria(String alias, String[] codeStrings) {
        Disjunction disjuncation = Expression.disjunction();
        for(String codeString : codeStrings) {
            String[] arr = codeString.split("\\^");
            disjuncation.add(Expression.conjunction()
                    .add(Expression.eq(alias+".value", arr[0]))
                    .add(Expression.eq(alias+".designator", arr[1])));
        }
        return disjuncation;
    }
    
    private boolean hasCriteriaForActiveParticipant1() {
        return hasCriteria(userID1) || hasCriteria(altUserID1) 
        || hasCriteria(userName1) || hasCriteriaBoolean(userIsRequestor1)
        || hasCriteria(roleIDs1) || hasCriteria(napTypes1) || hasCriteria(napID1);
    }

    private boolean hasCriteriaForActiveParticipant2() {
        return hasCriteria(userID2) || hasCriteria(altUserID2) 
        || hasCriteria(userName2) || hasCriteriaBoolean(userIsRequestor2)
        || hasCriteria(roleIDs2) || hasCriteria(napTypes2) || hasCriteria(napID2);
    }
    
    private boolean hasCriteriaBoolean(String var) {
        return var != null && var.trim().length() > 0 && !var.equalsIgnoreCase("any"); 
    }
    
    private boolean hasCriteria(String var) {
        return var != null && var.trim().length() > 0; 
    }
    
    private boolean hasCriteria(String[] vars) {
        if(vars == null)
            return false;
        
        if(vars.length == 1 && vars[0].trim().length() == 0)
            return false;
        
        // when "Any" is also selected, it's considered the same as "Any"
        for(String elm : vars)
            if(elm.trim().length() == 0)
                return false;
        
        return true;
    }
    
    private Integer[] toIntegerArray(String[] vars) {
        Integer[] ints = new Integer[vars.length];
        for(int i = 0; i < vars.length; i++)
            ints[i] = Integer.valueOf(vars[i]);
        return ints;
    }
    
    private Date[] parseDateTimeRange() throws ParseException {
	int dtlen = dateTimeRange.length();
        if(dtlen == 0)
            return null;
        
	if (dtlen < YEAR) {
	    throw new ParseException("Missing year", dtlen);
	}
	String dtformat = DATE_TIME_FORMAT.substring(0, dtlen);
	Date[] dtRange = new Date[2];
	dtRange[0] = new SimpleDateFormat(dtformat).parse(dateTimeRange);
	Calendar cal = Calendar.getInstance();
	cal.setTime(dtRange[0]);
	cal.set(Calendar.MILLISECOND, 999);
	if (dtlen < SECOND) {
	    cal.set(Calendar.SECOND, 59);
	    if (dtlen < MINUTE) {
		cal.set(Calendar.MINUTE, 59);
		if (dtlen < HOUR_OF_DAY) {
		    cal.set(Calendar.HOUR_OF_DAY, 23);
		    if (dtlen < DAY_OF_MONTH) {
		        if (dtlen < MONTH) {
		            cal.set(Calendar.MONTH, 11);
		        }
		        cal.add(Calendar.MONTH, 1);
		        cal.add(Calendar.DAY_OF_MONTH, -1);
		    }
		}
	    }
	}
	dtRange[1] = cal.getTime();
	return dtRange;
    }
    
    public boolean isShowXml() {
        return showXml;
    }

    public void showXml() {
        showXml = true;
    }

    public void showDetails() {
        showXml = false;
    }
    
    public String getXml() {
        if (selectedIndex == -1) {
            return "No Record selected";
        }
        return XSLTUtils.toXML(records.get(selectedIndex).getXmldata());
    }
    
    public String getDetails() {
        if (selectedIndex == -1) {
            return "No Record selected";
        }
        return XSLTUtils.toDetails(records.get(selectedIndex).getXmldata());
    }
    
    public String getRowClasses() {
	int n = records.size();
	if (n == 0) {
	    return "";
	}
	StringBuffer sb = new StringBuffer((n + 3)/2 * 9);
	for (int i = 0; i < n; i++) {
	    sb.append((i & 1) == 0 ? "odd" : "even");
	    if (i == selectedIndex) {
		sb.append(" selected");
	    }
	    sb.append(',');
	}
	return sb.substring(0, sb.length()-1);
    }

    @Destroy @Remove
    public void destroy() {}
    
}
