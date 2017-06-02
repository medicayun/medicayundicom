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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
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

package org.dcm4chee.dashboard.mbean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.model.SystemPropertyModel;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 25.11.2009
 */
public class DashboardService extends ServiceMBeanSupport {

    private static Logger log = LoggerFactory.getLogger(DashboardService.class);
    
    private String domainName;
    private String[] jbossSystemTypesToQuery = new String[] {"Server", "ServerConfig", "ServerInfo"};
    private String newline = System.getProperty("line.separator");

    private String[] dataSourceList = new String[0];
    private String[] groupList = new String[0];
    private String[] otherList = new String[0];
    private String[] propertyList = new String[0];

    private String reportFilename = "";
    private String groupFilename = "";

    private int reportTablePagesize;
    
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDataSourceList(String dataSourceList) {
        this.dataSourceList = tokenize(dataSourceList);
    }

    public String getDataSourceList() {
        return arrayToString(this.dataSourceList);
    }

    public void setGroupList(String groupList) {
        this.groupList = tokenize(groupList);
    }

    public String getGroupList() {
        return arrayToString(this.groupList);
    }
    
    public void setOtherList(String otherList) {
        this.otherList = tokenize(otherList);
    }

    public String getOtherList() {
        return arrayToString(otherList);
    }

    public void setPropertyList(String propertyList) {
        this.propertyList = tokenize(propertyList);
    }

    public String getPropertyList() {
        return arrayToString(this.propertyList);
    }
    
    public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

    public String getReportFilename() {
        return reportFilename;
    }

    public void setGroupFilename(String groupFilename) {
        this.groupFilename = groupFilename;
    }

    public String getGroupFilename() {
        return groupFilename;
    }

    public int getReportTablePagesize() {
        return reportTablePagesize;
    }
    
    public void setReportTablePagesize(int reportTablePagesize) {
        this.reportTablePagesize = reportTablePagesize;
    }
    
    public String[] listAllFileSystemGroups() throws MalformedObjectNameException, NullPointerException {
        return this.groupList;
    }
    
    public File[] listFileSystemsOfGroup(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {
        return (File[]) this.server.invoke(
                        new ObjectName(groupname),
                        "listFileSystemDirectories", null, null);
    }
    
    public long getMinimumFreeDiskSpaceOfGroup(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException, AttributeNotFoundException {
        return ((Long) this.server.getAttribute(
                      new ObjectName(groupname),
                      "MinimumFreeDiskSpaceBytes")).longValue();
    }

    public long getExpectedDataVolumePerDay(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException, AttributeNotFoundException {
        return ((Long) this.server.getAttribute(
                       new ObjectName(groupname),
                      "ExpectedDataVolumePerDayBytes")).longValue();
    }

    public String getDefaultRetrieveAETitle(String groupname) throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException, AttributeNotFoundException {
        return ((String) server.getAttribute(
                       new ObjectName(groupname),
                       "DefaultRetrieveAETitle"));
    }
    
    public String[] listOtherFileSystems() throws MalformedObjectNameException, NullPointerException {
        return this.otherList;
    }

    public Map<String, List<SystemPropertyModel>> getSystemProperties() throws InstanceNotFoundException, MalformedObjectNameException, ReflectionException, MBeanException, NullPointerException {

        Map<String, List<SystemPropertyModel>> propertyMap = new HashMap<String, List<SystemPropertyModel>>();
        for (String property : this.propertyList) {
            try {
                String[] attributes = property.split(";");
                SystemPropertyModel propertyModel = new SystemPropertyModel(
                                attributes[0], 
                                attributes[1],
                                attributes[2], 
                                (String) this.server.invoke(new ObjectName("jboss:name=SystemProperties,type=Service"),
                                                    "get", 
                                                    new Object[] {attributes[2]}, new String[] {"java.lang.String"}));
                if (propertyModel.getValue() == null) {
                    for (String type : this.jbossSystemTypesToQuery) {
                        try {
                            propertyModel.setValue((String) this.server.getAttribute(
                                    new ObjectName("jboss.system:type=" + type), 
                                    attributes[2]));
                            break;
                        } catch (AttributeNotFoundException e) {}
                    }
                }
                if (!propertyMap.containsKey(attributes[0]))
                        propertyMap.put(attributes[0], new ArrayList<SystemPropertyModel>());
                propertyMap.get(attributes[0]).add(propertyModel);
            } catch (Exception ignore) {
            }
        }
        return propertyMap;
    }

    public ReportModel[] listAllReports(boolean groups) {
        try {
            List<ReportModel> reportList = new ArrayList<ReportModel>();
            if (new File(groups ? this.groupFilename : this.reportFilename).exists()) {
                String line;
                BufferedReader reader = new BufferedReader(new FileReader(groups ? this.groupFilename : this.reportFilename));
                while ((line = reader.readLine()) != null)
                    reportList.add((ReportModel) JSONObject.toBean(JSONObject.fromObject(line), ReportModel.class));
            }
            return reportList.toArray(new ReportModel[reportList.size()]);
        } catch (Exception e) {
            log.debug("Exception: ", e);
            return null;
        }
    }
    
    public void createReport(ReportModel report, boolean isGroup) {
        try {
            if (report.getUuid() == null)
                report.setUuid(UUID.randomUUID().toString());

            BufferedWriter writer = new BufferedWriter(new FileWriter(isGroup ? this.groupFilename : this.reportFilename, true));
            JSONObject jsonObject = JSONObject.fromObject(report);
            if (isGroup) jsonObject.put("groupUuid", JSONNull.getInstance());
            if (report.getDiagram() == null) jsonObject.put("diagram", JSONNull.getInstance());
            writer.write(jsonObject.toString());
            writer.newLine();
            writer.close();
            
            sort(this.groupFilename);
            sort(this.reportFilename);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }        
    }
    
    public void updateReport(ReportModel report) {
        modifyReport(report, false, false);
    }
    
    public void deleteReport(ReportModel report, boolean isGroup) {
        modifyReport(report, true, isGroup);
    }

    private void modifyReport(ReportModel report, boolean delete, boolean isGroup) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(isGroup ? this.groupFilename : this.reportFilename));
            File reportFile = new File(isGroup ? this.groupFilename : this.reportFilename);            
            String tempFilename = reportFile.getAbsolutePath().substring(0, reportFile.getAbsolutePath().length() - reportFile.getName().length()) 
                                + UUID.randomUUID().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!((ReportModel) JSONObject.toBean(JSONObject.fromObject(line), ReportModel.class)).getUuid().equals(report.getUuid())) {
                  writer.write(line);
                  writer.newLine();
                } else {
                    if (!delete) {
                        JSONObject jsonObject = JSONObject.fromObject(report);
                        if (report.getGroupUuid() == null) jsonObject.put("groupUuid", JSONNull.getInstance());
                        if (report.getDiagram() == null) jsonObject.put("diagram", JSONNull.getInstance());
                        writer.write(jsonObject.toString());
                        writer.newLine();
                    }
                }
            }
            reader.close();
            writer.close();
            reportFile.delete();
            new File(tempFilename).renameTo(reportFile);
            
            sort(this.groupFilename);
            sort(this.reportFilename);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }
    }

    private void sort(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            File reportFile = new File(filename);            
            String tempFilename = reportFile.getAbsolutePath().substring(0, reportFile.getAbsolutePath().length() - reportFile.getName().length()) 
                                + UUID.randomUUID().toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilename, true));
            String line;
            
            // store here
            List<ReportModel> reports = new ArrayList<ReportModel>();
            while ((line = reader.readLine()) != null) {
                reports.add((ReportModel) JSONObject.toBean(JSONObject.fromObject(line), ReportModel.class));
                
            }
            Collections.sort(reports, new Comparator<ReportModel>() {
           
                @Override
                public int compare(ReportModel rm1, ReportModel rm2) {
                    return (rm1.getTitle().toUpperCase().compareTo(rm2.getTitle().toUpperCase()));
                }
            });
            for (ReportModel rm : reports) {
                JSONObject jsonObject = JSONObject.fromObject(rm);
                if (rm.getGroupUuid() == null) jsonObject.put("groupUuid", JSONNull.getInstance());
                if (rm.getDiagram() == null) jsonObject.put("diagram", JSONNull.getInstance());
                writer.write(jsonObject.toString());
                writer.newLine();
            }
            reader.close();
            writer.close();
            reportFile.delete();
            new File(tempFilename).renameTo(reportFile);
        } catch (IOException e) {
            log.debug("Exception: ", e);
        }       
    }
    
    public String[] listQueueNames() throws MalformedObjectNameException, NullPointerException {
        List<String> queueNameList = new ArrayList<String>();
        for (ObjectInstance oi : 
            (Set<ObjectInstance>) this.server.queryMBeans(
                new ObjectName(this.domainName + ":service=Queue,*"), null)) {
            queueNameList.add(oi.getObjectName().getKeyProperty("name"));
        }
        return queueNameList.toArray(new String[queueNameList.size()]);
    }

    public int[] listQueueAttributes(String queueName) throws MalformedObjectNameException, NullPointerException {
        int[] queueAttributesList = new int[4];
        try {
            ObjectName queueON = new ObjectName(this.domainName + ":service=Queue,name="+queueName);
            queueAttributesList[0] = new Integer(server.getAttribute(queueON, "MessageCount").toString()).intValue();
            queueAttributesList[1] = new Integer(server.getAttribute(queueON, "DeliveringCount").toString()).intValue();
            queueAttributesList[2] = new Integer(server.getAttribute(queueON, "ScheduledMessageCount").toString()).intValue();
            queueAttributesList[3] = new Integer(server.getAttribute(queueON, "ConsumerCount").toString()).intValue();
        } catch (Exception ignore) {
            log.error("Cant get Queue attributes!", ignore);
            return null;
        }
        return queueAttributesList;        
    }

    private String[] tokenize(String sourceString) {
        StringTokenizer st = new StringTokenizer(sourceString, newline);
        List<String> tokens = new ArrayList<String>();        
        while (st.hasMoreTokens()) {
            String token =  st.nextToken();
            if (token.endsWith("\t") || 
                token.endsWith("\r") ||
                token.endsWith("\n"))
                    token = token.substring(0, token.length() - 1);
            if (token.length() > 0) tokens.add(token);
        }
        return tokens.toArray(new String[tokens.size()]);
    }
    
    private String arrayToString(String[] array) {
        String arrayString = "";
        for (String string : array) arrayString += (newline + string);
        return (arrayString.length() <= 0 ? arrayString : arrayString.substring(1)) + newline;
    }
}
