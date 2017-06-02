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

package org.dcm4chee.dashboard.ui.messaging;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.mbean.DashboardDelegator;
import org.dcm4chee.dashboard.ui.DashboardPanel;
import org.dcm4chee.dashboard.ui.common.DashboardTreeTable;
import org.dcm4chee.icons.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 17.03.2010
 */
public class QueuePanel extends Panel {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(QueuePanel.class);

    private static final ResourceReference CSS = new CompressedResourceReference(DashboardPanel.class, "dashboard-style.css");
    
    public static final String CONNECTION_FACTORY = "java:ConnectionFactory";

    public QueuePanel(String id) {
        super(id);
        
        if (QueuePanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(QueuePanel.CSS));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();

            Connection connection = null;
            InitialContext initialContext = null;
            
            try {
                Session session = ((QueueConnectionFactory) new InitialContext().lookup(CONNECTION_FACTORY))
                                    .createQueueConnection()
                                    .createSession(false, Session.AUTO_ACKNOWLEDGE);

                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new QueueModel());

                String[] queueNames = DashboardDelegator.getInstance((((AuthenticatedWebApplication) getApplication()).getInitParameter("DashboardServiceName"))).listQueueNames();
                for (String queueName : queueNames) {
                        QueueModel queueModel = new QueueModel(queueName);
                        DefaultMutableTreeNode queueNode;
                        queueModel.setQueue(true);
                        queueModel.setQueueDepth(0);
                        rootNode.add(queueNode = new DefaultMutableTreeNode(queueModel));

                    try {
                        Enumeration<Message> e = session.createBrowser((Queue) new InitialContext().lookup(queueName)).getEnumeration();
                        int j = 0;
                        while(e.hasMoreElements()) {
                            Message m = e.nextElement();
                            
                            QueueModel queueMessage = new QueueModel(m.getJMSMessageID());
                            queueMessage.setQueue(false);
                            queueMessage.setQueueDepth(null);                       
                            queueNode.add(new DefaultMutableTreeNode(queueMessage));
                            j++;
                        }
                        queueModel.setQueueDepth(j);
                    } catch (Exception ignore) {}
                }

                QueueTreeTable queueTreeTable = new QueueTreeTable("queue-tree-table", 
                       new DefaultTreeModel(rootNode), new IColumn[] {
                   new PropertyTreeColumn(new ColumnLocation(
                           Alignment.LEFT, 80, Unit.PERCENT), 
                           new ResourceModel(
                                   "dashboard.queue.table.column.name").wrapOnAssignment(this).getObject(), 
                                   "userObject.jndiName"),
                   new PropertyRenderableColumn(new ColumnLocation(
                           Alignment.RIGHT, 20, Unit.PERCENT),
                           new ResourceModel(
                                   "dashboard.queue.table.column.queuedepth").wrapOnAssignment(this).getObject(),
                                   "userObject.queueDepth")
               });
               queueTreeTable.getTreeState().setAllowSelectMultiple(true);
               queueTreeTable.getTreeState().collapseAll();
               queueTreeTable.setRootLess(true);
               addOrReplace(queueTreeTable);
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            throw new WicketRuntimeException(e.getLocalizedMessage(), e);
        } finally {
            if (initialContext != null) {
                try {
                    initialContext.close();
                } catch (NamingException ignore) {}
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignore) {}
           }
        }
    }
    
    private class QueueTreeTable extends DashboardTreeTable {

        private static final long serialVersionUID = 1L;

        public QueueTreeTable(String id, TreeModel model, IColumn[] columns) {
            super(id, model, columns);
        }

        @Override
        protected Component newNodeIcon(MarkupContainer parent, String id, final TreeNode node) {

            return new WebMarkupContainer(id) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);

                    tag.put("style", "background-image: url('" + 
                            (((QueueModel) ((DefaultMutableTreeNode) node).getUserObject()).isQueue() ?  
                                    getRequestCycle().urlFor(ImageManager.IMAGE_DASHBOARD_QUEUE)
                                    : getRequestCycle().urlFor(ImageManager.IMAGE_DASHBOARD_QUEUE_MESSAGE)) 
                                    +"')"
                    );
                    tag.put("title", ((QueueModel) ((DefaultMutableTreeNode) node).getUserObject()).getJndiName());
                }
            };
        }
    };

    protected class QueueModel implements Serializable {

        private static final long serialVersionUID = -1L;

        private String jndiName;
        private Integer queueDepth;
        private Integer scheduledMessageCount;
        
        private boolean isQueue;
        
        public QueueModel() {}
        
        public QueueModel(String jndiName) {
            this.jndiName = jndiName;
        }
        
        public void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }
        
        public String getJndiName() {
            return jndiName;
        }
        
        public void setQueueDepth(Integer queueDepth) {
            this.queueDepth = queueDepth;
        }
        
        public Integer getQueueDepth() {
            return queueDepth;
        }

        public void setScheduledMessageCount(Integer scheduledMessageCount) {
            this.scheduledMessageCount = scheduledMessageCount;
        }

        public Integer getScheduledMessageCount() {
            return scheduledMessageCount;
        }
        
        public void setQueue(boolean isQueue) {
            this.isQueue = isQueue;
        }

        public boolean isQueue() {
            return isQueue;
        }
    }
}
