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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.dashboard.ui.report.display;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.ui.util.CSSUtils;
import org.dcm4chee.dashboard.ui.util.DatabaseUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.csvreader.CsvWriter;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 18.11.2009
 */
public class DisplayReportTablePanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(DisplayReportTablePanel.class);

    private ReportModel report;
    private Map<String, String> parameters;

    public DisplayReportTablePanel(String id, ReportModel report, Map<String, String> parameters) {
        super(id);
        this.report = report;
        this.parameters = parameters;
    }
    
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();

        Connection jdbcConnection = null;
        try {

            RepeatingView columnHeaders = new RepeatingView("column-headers"); 
            add(columnHeaders);
            RepeatingView reportRows = new RepeatingView("report-rows"); 
            add(reportRows);
    
            final Document document = 
                DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .newDocument();

            final List<String[]> csvList = new ArrayList<String[]>();
            
            Node node1 = document.createElement("report");
            Node node2 = document.createElement("header");
            node1.appendChild(node2);        
    
            Node node3 = document.createElement("title");
            node2.appendChild(node3);
            node3.appendChild(document.createTextNode(report.getTitle()));
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(report.getCreated());
            node3 = document.createElement("created");
            node2.appendChild(node3);
            node3.appendChild(document.createTextNode(new SimpleDateFormat("dd.MM.yyyy hh:mm").format(calendar.getTime())));
    
            node3 = document.createElement("statement");
            node2.appendChild(node3);
            node3.appendChild(document.createTextNode(report.getStatement()));

            csvList.add(new String[] {report.getTitle(), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(calendar.getTime()), report.getStatement()});
            
            node2 = document.createElement("table");
            node1.appendChild(node2);
            document.appendChild(node1);

            jdbcConnection = DatabaseUtils.getDatabaseConnection(report.getDataSource());
            ResultSet resultSet = DatabaseUtils.getResultSet(jdbcConnection, report.getStatement(), parameters);

            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
                columnHeaders.add(new Label(columnHeaders.newChildId(), resultSet.getMetaData().getColumnName(i)));
            
            resultSet.beforeFirst();
            while (resultSet.next()) {
                
                WebMarkupContainer parent = new WebMarkupContainer(reportRows.newChildId());
                parent.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(resultSet.getRow() - 1))));
                reportRows.add(parent);
                RepeatingView columnValues = new RepeatingView("column-values");
                parent.add(columnValues);
                
                Node rowNode = document.createElement("row");
                
                List<String> columnList = new ArrayList<String>();
                
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    columnValues.add(new WebMarkupContainer(columnValues.newChildId()).add(new Label("column-value", resultSet.getString(i))));
                    columnList.add(resultSet.getString(i));
                    Element columnNode = document.createElement("column");
                    columnNode.setAttribute("name", resultSet.getMetaData().getColumnName(i));
                    rowNode.appendChild(columnNode);
                    columnNode.appendChild(document.createTextNode(resultSet.getString(i)));
                }
                node2.appendChild(rowNode);

                csvList.add(columnList.toArray(new String[0]));
            }
            resultSet.close();

            addOrReplace(new Link<Object>("table-download-xml") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {        

                    RequestCycle.get().setRequestTarget(new IRequestTarget() {

                        public void respond(RequestCycle requestCycle) {

                            WebResponse wr = null;
                            try {
                                StringWriter writer = new StringWriter();
                                TransformerFactory
                                    .newInstance()
                                    .newTransformer()
                                    .transform(new DOMSource(document), new StreamResult(writer));
                                
                                wr = (WebResponse) requestCycle.getResponse();
                                wr.setContentType("text/xml");
                                wr.setHeader( "content-disposition", "attachment;filename=table.xml");
                        
                                OutputStream os = wr.getOutputStream();
                                os.write(writer.toString().getBytes());
                                os.close();
                            } catch (Exception e) {
                                log.error(this.getClass().toString() + ": " + "respond: " + e.getMessage());
                                log.debug("Exception: ", e);
                            }
                            wr.close();
                        }

                        @Override
                        public void detach(RequestCycle arg0) {
                        }
                    });
                }
            }
            .add(new Label("table-download-xml-image", "XML"))
            .add(new TooltipBehaviour("dashboard.report.reporttable.", "xml.downloadlink"))
//            .add(new Image("table-download-xml-image", ImageManager.IMAGE_REPORT_XML)
//                .add(new ImageSizeBehaviour())
//                )
            );

            addOrReplace(new Link<Object>("table-download-csv") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {        

                    RequestCycle.get().setRequestTarget(new IRequestTarget() {

                        public void respond(RequestCycle requestCycle) {

                            WebResponse wr = null;
                            try {
                                wr = (WebResponse) requestCycle.getResponse();
                                wr.setContentType("text/xml");
                                wr.setHeader( "content-disposition", "attachment;filename=table.csv");
                        
                                OutputStream os = wr.getOutputStream();
                                CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(os)), ';');
                                for (String[] row : csvList)
                                    csvWriter.writeRecord(row);
                                csvWriter.close();
                                os.close();
                            } catch (IOException e) {
                                log.error(this.getClass().toString() + ": " + "respond: " + e.getMessage());
                                log.debug("Exception: ", e);
                            }
                            wr.close();
                        }

                        @Override
                        public void detach(RequestCycle arg0) {
                        }
                    });
                }
            }
            .add(new Label("table-download-csv-image", "CSV"))
            .add(new TooltipBehaviour("dashboard.report.reporttable.", "csv.downloadlink"))
//            .add(new Image("table-download-csv-image", ImageManager.IMAGE_REPORT_CSV)
//                .add(new ImageSizeBehaviour())
//                )
            );

            addOrReplace(new Image("table-print-image", ImageManager.IMAGE_DASHBOARD_REPORT_PRINT)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("dashboard.report.reporttable.", "table.printbutton")));

            addOrReplace(new Label("error-message", "").setVisible(false));
            addOrReplace(new Label("error-reason", "").setVisible(false));
        } catch (Exception e) {
            addOrReplace(((DynamicDisplayPage) this.getPage()).new PlaceholderLink("table-download-xml"));
            addOrReplace(((DynamicDisplayPage) this.getPage()).new PlaceholderLink("table-download-csv"));
            addOrReplace(new Image("table-print-image"));
            addOrReplace(new Label("error-message", new ResourceModel("dashboard.report.reporttable.statement.error").wrapOnAssignment(this).getObject()).add(new AttributeModifier("class", true, new Model<String>("message-error"))));
            addOrReplace(new Label("error-reason", e.getMessage()).add(new AttributeModifier("class", true, new Model<String>("message-error"))));
        } finally {
            try {
                jdbcConnection.close();
            } catch (Exception ignore) {
            }
        }
    }    
}
