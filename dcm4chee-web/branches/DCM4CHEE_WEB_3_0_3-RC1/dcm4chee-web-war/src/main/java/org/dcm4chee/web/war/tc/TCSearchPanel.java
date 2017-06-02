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
package org.dcm4chee.web.war.tc;

import java.util.Arrays;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.dao.tc.TCQueryFilter;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue;
import org.dcm4chee.web.war.common.AutoSelectInputTextBehaviour;
import org.dcm4chee.web.war.tc.TCObject.DicomCode;
import org.dcm4chee.web.war.tc.TCObject.ITextOrCode;
import org.dcm4chee.web.war.tc.TCUtilities.NullDropDownItem;
import org.dcm4chee.web.war.tc.keywords.TCKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since April 28, 2011
 */
public abstract class TCSearchPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory
            .getLogger(TCSearchPanel.class);

    private static enum Option {
        AuthorName, AuthorContact, AuthorAffiliation, History, Discussion, Title, Abstract, PatientSpecies
    }

    private boolean showSearch = true;

    private boolean showAdvancedOptions = false;

    public TCSearchPanel(final String id) {
        super(id, new Model<TCQueryFilter>(new TCQueryFilter()));

        setOutputMarkupId(true);

        final TCInput keywordInput = TCUtilities.createInput(
                "keywordInput", TCQueryFilterKey.Keyword, getFilterValue(TCQueryFilterKey.Keyword));
        final TCInput anatomyInput = TCUtilities.createInput(
                "anatomyInput", TCQueryFilterKey.Anatomy, getFilterValue(TCQueryFilterKey.Anatomy));
        final TCInput pathologyInput = TCUtilities.createInput(
                "pathologyInput", TCQueryFilterKey.Pathology, getFilterValue(TCQueryFilterKey.Pathology));
        final TCInput findingInput = TCUtilities.createInput(
                "findingInput", TCQueryFilterKey.Finding, getFilterValue(TCQueryFilterKey.Finding));
        final TCInput diagnosisInput = TCUtilities.createInput(
                "diagnosisInput", TCQueryFilterKey.Diagnosis, getFilterValue(TCQueryFilterKey.Diagnosis));
        final TCInput diffDiagnosisInput = TCUtilities.createInput(
                "diffDiagnosisInput", TCQueryFilterKey.DifferentialDiagnosis, getFilterValue(TCQueryFilterKey.DifferentialDiagnosis));
        final TextField<String> textText = new TextField<String>("textText",
                new Model<String>(""));
        textText.add(new AutoSelectInputTextBehaviour());
        
        final DropDownChoice<TCQueryFilterValue.AcquisitionModality> modalityChoice = TCUtilities.createDropDownChoice(
                "modalityChoice",
                new Model<TCQueryFilterValue.AcquisitionModality>(),
                Arrays.asList(TCQueryFilterValue.AcquisitionModality.values()),
                NullDropDownItem.All);
        final DropDownChoice<TCQueryFilterValue.PatientSex> patientSexChoice = TCUtilities.createEnumDropDownChoice(
                "patientSexChoice", new Model<TCQueryFilterValue.PatientSex>(),
                Arrays.asList(TCQueryFilterValue.PatientSex.values()), true,
                "tc.patientsex", NullDropDownItem.All);
        final DropDownChoice<TCQueryFilterValue.Category> categoryChoice = TCUtilities.createEnumDropDownChoice(
                "categoryChoice", new Model<TCQueryFilterValue.Category>(),
                Arrays.asList(TCQueryFilterValue.Category.values()), true,
                "tc.category", NullDropDownItem.All);
        final DropDownChoice<TCQueryFilterValue.Level> levelChoice = TCUtilities.createEnumDropDownChoice(
                "levelChoice", new Model<TCQueryFilterValue.Level>(),
                Arrays.asList(TCQueryFilterValue.Level.values()), true,
                "tc.level", NullDropDownItem.All);
        final DropDownChoice<TCQueryFilterValue.YesNo> diagnosisConfirmedChoice = TCUtilities.createEnumDropDownChoice(
                "diagnosisConfirmedChoice",
                new Model<TCQueryFilterValue.YesNo>(),
                Arrays.asList(TCQueryFilterValue.YesNo.values()), true,
                "tc.yesno", NullDropDownItem.All);

        final RadioGroup<Option> optionGroup = new RadioGroup<Option>(
                "optionGroup", new Model<Option>());
        optionGroup.add(new Radio<Option>("historyOption", new Model<Option>(
                Option.History)));
        optionGroup.add(new Radio<Option>("authorNameOption",
                new Model<Option>(Option.AuthorName)));
        optionGroup.add(new Radio<Option>("authorContactOption",
                new Model<Option>(Option.AuthorContact)));
        optionGroup.add(new Radio<Option>("authorOrganisationOption",
                new Model<Option>(Option.AuthorAffiliation)));
        optionGroup.add(new Radio<Option>("discussionOption",
                new Model<Option>(Option.Discussion)));
        optionGroup.add(new Radio<Option>("titleOption", new Model<Option>(
                Option.Title)));
        optionGroup.add(new Radio<Option>("abstractOption", new Model<Option>(
                Option.Abstract)));
        optionGroup.add(new Radio<Option>("patientSpeciesOption",
                new Model<Option>(Option.PatientSpecies)));

        final IndicatingAjaxButton searchBtn = new IndicatingAjaxButton(
                "doSearchBtn") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                	try {
                		findParent(TCPanel.class).getPopupManager().hideAllPopups(target);
                	}
                	catch (Exception e) {
                		log.error("Error while closing popups!", e);
                	}
                	
                    TCQueryFilter filter = (TCQueryFilter) TCSearchPanel.this
                            .getDefaultModelObject();
                    filter.clear();

                    Object keywordInputValue = getConvertedKeywordInputValue(
                            keywordInput.getValue());
                    if (keywordInputValue != null) {
                        if (Code.class.equals(keywordInputValue.getClass())) {
                            filter.setKeywordCode((Code) keywordInputValue);
                        } else if (String.class.equals(keywordInputValue
                                .getClass())) {
                            filter.setKeyword((String) keywordInputValue);
                        }
                    }

                    if (showAdvancedOptions)
                    {
	                    Object anatomyInputValue = getConvertedKeywordInputValue(
	                            anatomyInput.getValue());
	                    if (anatomyInputValue != null) {
	                        if (Code.class.equals(anatomyInputValue.getClass())) {
	                            filter.setAnatomyCode((Code) anatomyInputValue);
	                        } else if (String.class.equals(anatomyInputValue
	                                .getClass())) {
	                            filter.setAnatomy((String) anatomyInputValue);
	                        }
	                    }
	
	                    Object pathologyInputValue = getConvertedKeywordInputValue(
	                            pathologyInput.getValue());
	                    if (pathologyInputValue != null) {
	                        if (Code.class.equals(pathologyInputValue.getClass())) {
	                            filter.setPathologyCode((Code) pathologyInputValue);
	                        } else if (String.class.equals(pathologyInputValue
	                                .getClass())) {
	                            filter.setPathology((String) pathologyInputValue);
	                        }
	                    }
	
	                    Object findingInputValue = getConvertedKeywordInputValue(
	                            findingInput.getValue());
	                    if (findingInputValue != null) {
	                        if (Code.class.equals(findingInputValue.getClass())) {
	                            filter.setFindingCode((Code) findingInputValue);
	                        } else if (String.class.equals(findingInputValue
	                                .getClass())) {
	                            filter.setFinding((String) findingInputValue);
	                        }
	                    }
	
	                    Object diagnosisInputValue = getConvertedKeywordInputValue(
	                            diagnosisInput.getValue());
	                    if (diagnosisInputValue != null) {
	                        if (Code.class.equals(diagnosisInputValue.getClass())) {
	                            filter.setDiagnosisCode((Code) diagnosisInputValue);
	                        } else if (String.class.equals(diagnosisInputValue
	                                .getClass())) {
	                            filter.setDiagnosis((String) diagnosisInputValue);
	                        }
	                    }
	
	                    Object diffDiagnosisInputValue = getConvertedKeywordInputValue(
	                            diffDiagnosisInput.getValue());
	                    if (diffDiagnosisInputValue != null) {
	                        if (Code.class.equals(diffDiagnosisInputValue
	                                .getClass())) {
	                            filter.setDiffDiagnosisCode((Code) diffDiagnosisInputValue);
	                        } else if (String.class.equals(diffDiagnosisInputValue
	                                .getClass())) {
	                            filter.setDiffDiagnosis((String) diffDiagnosisInputValue);
	                        }
	                    }
	
	                    filter.setAcquisitionModality(modalityChoice
	                            .getModelObject());
	                    filter.setPatientSex(patientSexChoice.getModelObject());
	                    filter.setCategory(categoryChoice.getModelObject());
	                    filter.setLevel(levelChoice.getModelObject());
	                    filter.setDiagnosisConfirmed(diagnosisConfirmedChoice
	                            .getModelObject());
	
	                    Option selectedOption = optionGroup.getModelObject();
	                    if (selectedOption != null) {
	                        if (Option.History.equals(selectedOption)) {
	                            filter.setHistory(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.AuthorName.equals(selectedOption)) {
	                            filter.setAuthorName(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.AuthorContact.equals(selectedOption)) {
	                            filter.setAuthorContact(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.AuthorAffiliation
	                                .equals(selectedOption)) {
	                            filter.setAuthorAffiliation(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.Title.equals(selectedOption)) {
	                            filter.setTitle(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.Abstract.equals(selectedOption)) {
	                            filter.setAbstract(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.PatientSpecies.equals(selectedOption)) {
	                            filter.setPatientSpecies(textText
	                                    .getDefaultModelObjectAsString());
	                        } else if (Option.Discussion.equals(selectedOption)) {
	                            filter.setDiscussion(textText
	                                    .getDefaultModelObjectAsString());
	                        }
	                    }
                    }

                    Component[] toUpdate = doSearch(filter);

                    if (toUpdate != null && target != null) {
                        for (Component c : toUpdate) {
                            target.addComponent(c);
                        }
                    }
                    
                    target.appendJavascript("updateKeywordChooserButtons();");
                } catch (Throwable t) {
                    log.error("Searching for teaching-files failed!", t);
                }

                if (target != null) {
                    target.addComponent(form);
                }
            }

            @Override
            public void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                try {
                    return TCPanel.getMaskingBehaviour().getAjaxCallDecorator();
                } catch (Exception e) {
                    log.error("Failed to get IAjaxCallDecorator: ", e);
                }
                return null;
            }
        };

        searchBtn.setOutputMarkupId(true);
        searchBtn
                .add(new Image("doSearchImg", ImageManager.IMAGE_COMMON_SEARCH)
                        .add(new ImageSizeBehaviour("vertical-align: middle;")));
        searchBtn.add(new Label("doSearchText", new ResourceModel(
                "tc.search.dosearch.text")).add(
                new AttributeModifier("style", true, new Model<String>(
                        "vertical-align: middle;"))).setOutputMarkupId(true));

        AjaxButton resetBtn = new IndicatingAjaxButton("resetSearchBtn") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                TCQueryFilter filter = (TCQueryFilter) TCSearchPanel.this
                        .getDefaultModelObject();
                filter.clear();

                keywordInput.resetValue();
                anatomyInput.resetValue();
                pathologyInput.resetValue();
                findingInput.resetValue();
                diagnosisInput.resetValue();
                diffDiagnosisInput.resetValue();
                modalityChoice.setModelObject(null);
                levelChoice.setModelObject(null);
                patientSexChoice.setModelObject(null);
                categoryChoice.setModelObject(null);
                diagnosisConfirmedChoice.setModelObject(null);
                textText.setModelObject(null);
                optionGroup.setModelObject(null);

                target.addComponent(form);
                target.appendJavascript("updateKeywordChooserButtons();");
            }

            @Override
            public void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
        };
        resetBtn.add(new Image("resetSearchImg",
                ImageManager.IMAGE_COMMON_RESET).add(new ImageSizeBehaviour(
                "vertical-align: middle;")));
        resetBtn.add(new Label("resetSearchText", new ResourceModel(
                "tc.search.reset.text")).add(new AttributeModifier("style",
                true, new Model<String>("vertical-align: middle;"))));

        final WebMarkupContainer wmc = new WebMarkupContainer("advancedOptions");
        wmc.setOutputMarkupPlaceholderTag(true);
        wmc.setVisible(false);

        wmc.add(anatomyInput.getComponent());
        wmc.add(pathologyInput.getComponent());
        wmc.add(findingInput.getComponent());
        wmc.add(diagnosisInput.getComponent());
        wmc.add(diffDiagnosisInput.getComponent());
        wmc.add(modalityChoice);
        wmc.add(patientSexChoice);
        wmc.add(categoryChoice);
        wmc.add(levelChoice);
        wmc.add(diagnosisConfirmedChoice);
        wmc.add(optionGroup);
        wmc.add(textText);
        wmc.add(resetBtn);

        final MarkupContainer advancedOptionsToggleLink = new AjaxFallbackLink<String>(
                "advancedOptionsToggle") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                showAdvancedOptions = !showAdvancedOptions;

                wmc.setVisible(showAdvancedOptions);

                target.addComponent(wmc);
                target.addComponent(this);
                
                if (showAdvancedOptions)
                {
                    target.appendJavascript("updateKeywordChooserButtons();");
                }
            }
        }.add(new Label("advancedOptionsToggleText",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return showAdvancedOptions ? getString("tc.search.advancedOptions.hide.Text")
                                : getString("tc.search.advancedOptions.show.Text");
                    }
                })).add(
                (new Image("advancedOptionsToggleImg",
                        new AbstractReadOnlyModel<ResourceReference>() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public ResourceReference getObject() {
                                return showAdvancedOptions ? ImageManager.IMAGE_COMMON_COLLAPSE
                                        : ImageManager.IMAGE_COMMON_EXPAND;
                            }
                        })).add(new ImageSizeBehaviour()));
        advancedOptionsToggleLink.setOutputMarkupId(true);

        final Form<?> form = new Form<Object>("searchForm");
        form.add(keywordInput.getComponent());
        form.add(wmc);
        form.add(searchBtn);
        form.setDefaultButton(searchBtn);
        form.setOutputMarkupPlaceholderTag(true);

        form.add(advancedOptionsToggleLink);

        add(form);

        add(new AjaxFallbackLink<Object>("searchToggle") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                showSearch = !showSearch;

                form.setVisible(showSearch);

                target.addComponent(TCSearchPanel.this);
            }
        }.add((new Image("searchToggleImg",
                new AbstractReadOnlyModel<ResourceReference>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public ResourceReference getObject() {
                        return showSearch ? ImageManager.IMAGE_COMMON_COLLAPSE
                                : ImageManager.IMAGE_COMMON_EXPAND;
                    }
                })).add(new ImageSizeBehaviour())));
    }
    
    public void redoSearch(AjaxRequestTarget target)
    {
        redoSearch(target, null);
    }
    
    public void redoSearch(AjaxRequestTarget target, String iuid)
    {
        Component[] toUpdate = doSearch((TCQueryFilter)getDefaultModel().getObject());

        if (toUpdate != null && target != null) {
            for (Component c : toUpdate) {
                target.addComponent(c);
            }
        }
    }

    protected abstract Component[] doSearch(TCQueryFilter filter);

    private Object getFilterValue(TCQueryFilterKey key)
    {
        TCQueryFilter filter = (TCQueryFilter) getDefaultModelObject();
        return filter != null ? filter.getValue(key) : null;
    }
    
    private Object getConvertedKeywordInputValue(Object value)
    {
    	DicomCode code = null;
    	String text = null;
    	
        if (value instanceof TCKeyword)
        {
            TCKeyword kw = (TCKeyword) value;
            
            if (kw != null) {
                code = kw.getCode();
                text = kw.getName();
            }
        }
        else if (value instanceof ITextOrCode)
        {
        	code = ((ITextOrCode)value).getCode();
        	text = ((ITextOrCode)value).getText();
        }
        
        if (code!=null)
        {
        	return code.toCode();
        }
        else if (text!=null && text.length()>0)
        {
        	return text;
        }

        return value;
    }
}
