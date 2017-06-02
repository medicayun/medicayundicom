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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.dao.tc.TCQueryFilter;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue;
import org.dcm4chee.web.war.tc.TCDetails.DicomCode;
import org.dcm4chee.web.war.tc.TCPanel.PopupCloseables;
import org.dcm4chee.web.war.tc.keywords.TCKeyword;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogue;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogue.TCKeywordInput;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;
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

        final IKeywordAwareInput keywordInput = createKeywordInput(
                "keywordInput", TCQueryFilterKey.Keyword);
        final IKeywordAwareInput anatomyInput = createKeywordInput(
                "anatomyInput", TCQueryFilterKey.Anatomy);
        final IKeywordAwareInput pathologyInput = createKeywordInput(
                "pathologyInput", TCQueryFilterKey.Pathology);
        final IKeywordAwareInput findingInput = createKeywordInput(
                "findingInput", TCQueryFilterKey.Finding);
        final IKeywordAwareInput diagnosisInput = createKeywordInput(
                "diagnosisInput", TCQueryFilterKey.Diagnosis);
        final IKeywordAwareInput diffDiagnosisInput = createKeywordInput(
                "diffDiagnosisInput", TCQueryFilterKey.DifferentialDiagnosis);
        final TextField<String> textText = new TextField<String>("textText",
                new Model<String>(""));

        final DropDownChoice<TCQueryFilterValue.AcquisitionModality> modalityChoice = createEnumDropDownChoice(
                "modalityChoice",
                new Model<TCQueryFilterValue.AcquisitionModality>(),
                Arrays.asList(TCQueryFilterValue.AcquisitionModality.values()),
                false, null);
        final DropDownChoice<TCQueryFilterValue.PatientSex> patientSexChoice = createEnumDropDownChoice(
                "patientSexChoice", new Model<TCQueryFilterValue.PatientSex>(),
                Arrays.asList(TCQueryFilterValue.PatientSex.values()), true,
                "tc.patientsex");
        final DropDownChoice<TCQueryFilterValue.Category> categoryChoice = createEnumDropDownChoice(
                "categoryChoice", new Model<TCQueryFilterValue.Category>(),
                Arrays.asList(TCQueryFilterValue.Category.values()), true,
                "tc.category");
        final DropDownChoice<TCQueryFilterValue.Level> levelChoice = createEnumDropDownChoice(
                "levelChoice", new Model<TCQueryFilterValue.Level>(),
                Arrays.asList(TCQueryFilterValue.Level.values()), true,
                "tc.level");
        final DropDownChoice<TCQueryFilterValue.YesNo> diagnosisConfirmedChoice = createEnumDropDownChoice(
                "diagnosisConfirmedChoice",
                new Model<TCQueryFilterValue.YesNo>(),
                Arrays.asList(TCQueryFilterValue.YesNo.values()), true,
                "tc.yesno");

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
                // close popups
                PopupCloseables.getInstance().closeAll(target);

                try {
                    TCQueryFilter filter = (TCQueryFilter) TCSearchPanel.this
                            .getDefaultModelObject();
                    filter.clear();

                    Object keywordInputValue = keywordInput.getSearchObject();
                    if (keywordInputValue != null) {
                        if (Code.class.equals(keywordInputValue.getClass())) {
                            filter.setKeywordCode((Code) keywordInputValue);
                        } else if (String.class.equals(keywordInputValue
                                .getClass())) {
                            filter.setKeyword((String) keywordInputValue);
                        }
                    }

                    Object anatomyInputValue = anatomyInput.getSearchObject();
                    if (anatomyInputValue != null) {
                        if (Code.class.equals(anatomyInputValue.getClass())) {
                            filter.setAnatomyCode((Code) anatomyInputValue);
                        } else if (String.class.equals(anatomyInputValue
                                .getClass())) {
                            filter.setAnatomy((String) anatomyInputValue);
                        }
                    }

                    Object pathologyInputValue = pathologyInput
                            .getSearchObject();
                    if (pathologyInputValue != null) {
                        if (Code.class.equals(pathologyInputValue.getClass())) {
                            filter.setPathologyCode((Code) pathologyInputValue);
                        } else if (String.class.equals(pathologyInputValue
                                .getClass())) {
                            filter.setPathology((String) pathologyInputValue);
                        }
                    }

                    Object findingInputValue = findingInput.getSearchObject();
                    if (findingInputValue != null) {
                        if (Code.class.equals(findingInputValue.getClass())) {
                            filter.setFindingCode((Code) findingInputValue);
                        } else if (String.class.equals(findingInputValue
                                .getClass())) {
                            filter.setFinding((String) findingInputValue);
                        }
                    }

                    Object diagnosisInputValue = diagnosisInput
                            .getSearchObject();
                    if (diagnosisInputValue != null) {
                        if (Code.class.equals(diagnosisInputValue.getClass())) {
                            filter.setDiagnosisCode((Code) diagnosisInputValue);
                        } else if (String.class.equals(diagnosisInputValue
                                .getClass())) {
                            filter.setDiagnosis((String) diagnosisInputValue);
                        }
                    }

                    Object diffDiagnosisInputValue = diffDiagnosisInput
                            .getSearchObject();
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

                    Component[] toUpdate = doSearch(filter);

                    if (toUpdate != null && target != null) {
                        for (Component c : toUpdate) {
                            target.addComponent(c);
                        }
                    }
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
                PopupCloseables.getInstance().closeAll(target);

                TCQueryFilter filter = (TCQueryFilter) TCSearchPanel.this
                        .getDefaultModelObject();
                filter.clear();

                keywordInput.resetSearchObject();
                anatomyInput.resetSearchObject();
                pathologyInput.resetSearchObject();
                findingInput.resetSearchObject();
                diagnosisInput.resetSearchObject();
                diffDiagnosisInput.resetSearchObject();
                modalityChoice.setModelObject(null);
                levelChoice.setModelObject(null);
                patientSexChoice.setModelObject(null);
                categoryChoice.setModelObject(null);
                diagnosisConfirmedChoice.setModelObject(null);
                textText.setModelObject(null);
                optionGroup.setModelObject(null);

                target.addComponent(form);
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

        wmc.add(anatomyInput.getInputComponent());
        wmc.add(pathologyInput.getInputComponent());
        wmc.add(findingInput.getInputComponent());
        wmc.add(diagnosisInput.getInputComponent());
        wmc.add(diffDiagnosisInput.getInputComponent());
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
        form.add(keywordInput.getInputComponent());
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

    protected abstract Component[] doSearch(TCQueryFilter filter);

    private static <T extends Enum<T>> DropDownChoice<T> createEnumDropDownChoice(
            final String id, IModel<T> model, List<T> options,
            boolean localizeValues, final String localizePrefix) {
        DropDownChoice<T> choice = new DropDownChoice<T>(id, model, options) {

            private static final long serialVersionUID = 1L;

            @Override
            protected String getNullValidKey() {
                return TCPanel.ModuleName + ".search.null.text";
            }
        };

        choice.setNullValid(true);

        if (localizeValues) {
            choice.setChoiceRenderer(new EnumChoiceRenderer<T>(choice) {

                private static final long serialVersionUID = 1L;

                @Override
                protected String resourceKey(T object) {
                    String key = localizePrefix != null ? localizePrefix + "."
                            + object.name() : object.name();

                    return key.toLowerCase();
                }
            });
        }

        return choice;
    }

    private IKeywordAwareInput createKeywordInput(final String componentId,
            TCQueryFilterKey key) {
        TCKeywordCatalogueProvider p = TCKeywordCatalogueProvider.getInstance();

        if (p.hasCatalogue(key)) {
            TCKeywordCatalogue cat = p.getCatalogue(key);
            TCQueryFilter filter = (TCQueryFilter) getDefaultModelObject();
            Object value = filter != null ? filter.getValue(key) : null;
            TCKeyword keyword = value instanceof Code ? cat
                    .findKeyword(((Code) value).getCodeValue()) : null;

            final TCKeywordInput input = cat.createInput(componentId, keyword);

            return new IKeywordAwareInput() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object getSearchObject() {
                    TCKeyword kw = input.getSelectedKeyword();
                    if (kw != null) {
                        DicomCode code = kw.getCode();
                        if (kw.isValid() && code != null) {
                            return code.toCode();
                        } else {
                            String s = kw.getName();
                            return s != null && s.length() > 0 ? s : null;
                        }
                    }

                    return null;
                }

                @Override
                public void resetSearchObject() {
                    input.resetSelectedKeyword();
                }

                @Override
                public Component getInputComponent() {
                    return input.getComponent();
                }
            };
        } else {
            final TextField<String> tf = new TextField<String>("text",
                    new Model<String>(""));
            final Fragment fragment = new Fragment(componentId, "text-input",
                    TCSearchPanel.this);
            fragment.add(tf);

            return new IKeywordAwareInput() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object getSearchObject() {
                    return tf.getDefaultModelObjectAsString();
                }

                @Override
                public void resetSearchObject() {
                    tf.setDefaultModelObject(null);
                }

                @Override
                public Component getInputComponent() {
                    return fragment;
                }
            };
        }
    }

    private interface IKeywordAwareInput extends Serializable {
        public Object getSearchObject();

        public void resetSearchObject();

        public Component getInputComponent();
    }
}
