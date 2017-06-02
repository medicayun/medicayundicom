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

package org.dcm4chee.web.war.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.folder.StudyListPage;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Sept 15, 2010
 */
public class SelectAllLink extends AjaxFallbackLink<Object> {
    private static final long serialVersionUID = 1L;
    
    private ArrayList<Component> updateComponents = new ArrayList<Component>();
    private List<? extends AbstractDicomModel> models;
    private boolean selectState;
    private int selectLevel;
    private boolean selectChilds;
    private ResourceReference selectImg;
    
    public SelectAllLink(String id, List<? extends AbstractDicomModel> models, 
            int selectLevel, boolean select) {
        super(id);
        this.models = models;
        this.selectLevel = selectLevel;
        this.selectState = select;
        selectImg = select ? 
                new ResourceReference(StudyListPage.class, "images/select.png") : 
                    new ResourceReference(StudyListPage.class, "images/deselect.png");
    }
    public SelectAllLink(String id, List<? extends AbstractDicomModel> models, 
            int selectLevel, boolean select, Component updateComponent) {
        this(id, models, selectLevel, select);
        this.addUpdateComponent(updateComponent);
    }
    public SelectAllLink(String id, List<? extends AbstractDicomModel> models, 
            int selectLevel, boolean select, Component updateComponent, boolean selectChilds) {
        this(id, models, selectLevel, select);
        this.addUpdateComponent(updateComponent);
        this.selectChilds = selectChilds;
    }
    public SelectAllLink(String id, List<? extends AbstractDicomModel> models) {
        this(id, models, models.get(0).levelOfModel(), true);
    }
    
    public SelectAllLink addUpdateComponent(Component c) {
        if (!c.getOutputMarkupId())
            c.setOutputMarkupId(true);
        updateComponents.add(c);
        return this;
    }
    
    public SelectAllLink setImage(ResourceReference img) {
        this.selectImg = img;
        return this;
    }
    
    public SelectAllLink setSelectChilds(boolean b) {
        this.selectChilds = b;
        return this;
    }
    
    @Override
    public void onClick(AjaxRequestTarget target) {
        for (AbstractDicomModel m : models) {
            setSelected(m);
        }
        addToTarget(target);
    }
    
    @Override
    protected void onComponentTag(final ComponentTag tag) {
        if (selectImg != null && tag.isOpenClose()) {
            tag.setType(XmlTag.OPEN);
        }
        super.onComponentTag(tag);
    }
    @Override
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        if (selectImg != null) {
            final AppendingStringBuffer buffer = new AppendingStringBuffer();
            buffer.append("\n<img src=\"")
            .append(getImageUrl())
            .append("\" alt=\"(")
            .append(this.selectState ? '+' : '-').append(")\" />\n");
            replaceComponentTagBody(markupStream, openTag, buffer);
        }
    }
    private CharSequence getImageUrl() {
        CharSequence url = RequestCycle.get().urlFor(selectImg, null);
        return RequestCycle.get().getOriginalResponse().encodeURL(
                Strings.replaceAll(url, "&", "&amp;"));
    }

    private void setSelected(AbstractDicomModel m) {
        if (selectChilds) {
            if (selectState) {
                if (m.levelOfModel() == selectLevel) {
                    m.setSelected(true);
                    return;
                }
            } else {
                if (m.levelOfModel() >= selectLevel)
                    m.setSelected(false);
            }
            List<? extends AbstractDicomModel> childs = m.getDicomModelsOfNextLevel();
            if (childs != null) {
                for (AbstractDicomModel m1 : m.getDicomModelsOfNextLevel()) {
                    setSelected(m1);
                }
            }
        } else if (m.levelOfModel() == selectLevel) {
            m.setSelected(this.selectState);
        } else if (m.levelOfModel() < selectLevel) {
            if (m.isCollapsed())
                m.expand();
            for (AbstractDicomModel m1 : m.getDicomModelsOfNextLevel()) {
                setSelected(m1);
            }
        }
    }

    private void addToTarget(AjaxRequestTarget target) {
        for (int i = 0, len = updateComponents.size() ; i < len ; i++) {
            target.addComponent(updateComponents.get(i));
        }
    }

}
