package org.dcm4chee.web.war.tc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.secure.SecurityBehavior;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.folder.webviewer.Webviewer;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since Nov 25, 2011
 */
public class TCViewPanel extends Panel
{
    private WebviewerLinkProvider[] webviewerLinkProviders;
    
    private Map<AbstractTCViewTab, Integer> tabsToIndices =
        new HashMap<AbstractTCViewTab, Integer>();

    public TCViewPanel(final String id, IModel<TCEditableObject> model, final TCModel tc)
    {
        super(id, model==null ? new Model<TCEditableObject>() : model);

        initWebviewerLinkProvider();
        
        final ModalWindow webviewerSelectionWindow = new ModalWindow("tc-view-webviewer-selection-window");
        add(webviewerSelectionWindow);
        
        add(new AjaxLink<Void>("tc-print-btn") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    
                }
            }
            .add(new Image("tc-print-img", ImageManager.IMAGE_TC_PRINT).add(
                (new ImageSizeBehaviour("vertical-align: middle;"))))
            .setOutputMarkupId(true)
            .setVisible(false) //for now
        );

        add(Webviewer.getLink(tc, webviewerLinkProviders,
                    StudyPermissionHelper.get(),
                    new TooltipBehaviour("tc.view.", "webviewer"), webviewerSelectionWindow)
            .add(new SecurityBehavior(TCPanel.getModuleName()
                            + ":webviewerInstanceLink"))
            .setOutputMarkupId(true)
        );
        
        add(new Label("tc-view-title-text", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return TCViewPanel.this.getTC().getTitle();
            }
        }));
        
        final Label biblioTitleLabel = new Label("tc.view.bibliography.tab.title");
        biblioTitleLabel.setOutputMarkupId(true);
        
        final boolean showImagesTab = WebCfgDelegate.getInstance().isTCShowImagesInDialogEnabled();
        final TCViewOverviewTab overviewTab = new TCViewOverviewTab("tc-view-overview", getModel(), isEditable());
        final TCViewDiagnosisTab diagnosisTab = new TCViewDiagnosisTab("tc-view-diagnosis", getModel(), isEditable());
        final WebMarkupContainer imagesTab =  showImagesTab ?
        		new TCViewImagesTab("tc-view-images", getModel()) : new WebMarkupContainer("tc-view-images") {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
        				return false;
        			}
        		};
        		
        final TCViewGenericTextTab diffDiagnosisTab = new TCViewGenericTextTab("tc-view-diffDiagnosis", getModel(), isEditable()) {
            @Override
            public String getTabTitle()
            {
                return getString("tc.view.diffDiagnosis.tab.title");
            }
            @Override
            protected TCQueryFilterKey getKey() {
                return TCQueryFilterKey.DifferentialDiagnosis;
            }
        };
        final TCViewGenericTextTab findingTab = new TCViewGenericTextTab("tc-view-finding", getModel(), isEditable()) {
            @Override
            public String getTabTitle()
            {
                return getString("tc.view.finding.tab.title");
            }
            @Override
            protected TCQueryFilterKey getKey() {
                return TCQueryFilterKey.Finding;
            }
        };
        final TCViewGenericTextTab historyTab = new TCViewGenericTextTab("tc-view-history", getModel(), isEditable()) {
            @Override
            public String getTabTitle()
            {
                return getString("tc.view.history.tab.title");
            }
            @Override
            protected TCQueryFilterKey getKey() {
                return TCQueryFilterKey.History;
            }
        };
        final TCViewGenericTextTab discussionTab = new TCViewGenericTextTab("tc-view-discussion", getModel(), isEditable()) {
            @Override
            public String getTabTitle()
            {
                return getString("tc.view.discussion.tab.title");
            }
            @Override
            protected TCQueryFilterKey getKey() {
                return TCQueryFilterKey.Discussion;
            }
        };
        final TCViewGenericTextTab organSystemTab = new TCViewGenericTextTab("tc-view-organSystem", getModel(), isEditable()) {
            @Override
            public String getTabTitle()
            {
                return getString("tc.view.organSystem.tab.title");
            }
            @Override
            protected TCQueryFilterKey getKey() {
                return TCQueryFilterKey.OrganSystem;
            }
        };
               
        final TCViewBibliographyTab biblioTab = new TCViewBibliographyTab("tc-view-bibliography", getModel(), isEditable()) {
            @Override
            protected void tabTitleChanged(AjaxRequestTarget target)
            {
                if (target!=null)
                {
                    target.addComponent(biblioTitleLabel);
                }
            }
        };
        
        biblioTitleLabel.setDefaultModel(new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return biblioTab.getTabTitle();
            }
        });
        
        WebMarkupContainer content = new WebMarkupContainer("tc-view-content");
        content.setOutputMarkupId(true);
        content.setMarkupId(isEditable() ? 
                "tc-view-editable-content" : "tc-view-content");
                
        content.add(new Label("tc.view.overview.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return overviewTab.getTabTitle();
            }
        }));
        
        content.add(new Label("tc.view.diagnosis.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return diagnosisTab.getTabTitle();
            }
        }));
        
        content.add(new Label("tc.view.diffDiagnosis.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return diffDiagnosisTab.getTabTitle();
            }
        }));
        
        content.add(new Label("tc.view.finding.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return findingTab.getTabTitle();
            }
        }));
        
        content.add(new Label("tc.view.history.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return historyTab.getTabTitle();
            }
        }));
        
        content.add(new Label("tc.view.discussion.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return discussionTab.getTabTitle();
            }
        }));
        
        content.add(new Label("tc.view.organSystem.tab.title", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return organSystemTab.getTabTitle();
            }
        }));
        
        content.add(biblioTitleLabel);
        
        content.add((new WebMarkupContainer("tc.view.images.tab.item") {
			private static final long serialVersionUID = 1L;
			@Override
        	public boolean isVisible() {
        		return showImagesTab;
        	}
        }).add(new Label("tc.view.images.tab.title", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;
			@Override
        	public String getObject()
        	{
        		return imagesTab instanceof TCViewImagesTab ?
        				((TCViewImagesTab)imagesTab).getTabTitle() : null;
        	}
        })));
        
        tabsToIndices.put(overviewTab, 0);
        tabsToIndices.put(diagnosisTab, 1);
        tabsToIndices.put(diffDiagnosisTab, 2);
        tabsToIndices.put(findingTab, 3);
        tabsToIndices.put(historyTab, 4);
        tabsToIndices.put(discussionTab, 5);
        tabsToIndices.put(organSystemTab, 6);
        tabsToIndices.put(biblioTab, 7);
        
        if (imagesTab instanceof TCViewImagesTab)
        {
        	tabsToIndices.put((TCViewImagesTab)imagesTab, 8);
        }
        
        content.add(overviewTab);
        content.add(diagnosisTab);
        content.add(diffDiagnosisTab);
        content.add(findingTab);
        content.add(historyTab);
        content.add(discussionTab);
        content.add(organSystemTab);
        content.add(biblioTab);
       	content.add(imagesTab);
       	
        add(content);
    }
    
    public boolean isEditable()
    {
        return false;
    }
    
    
    public TCObject getTC()
    {
        return (TCObject) getDefaultModelObject();
    }
    
    public List<Integer> getDisabledTabIndices()
    {
        List<Integer> indices = new ArrayList<Integer>();
        for (Map.Entry<AbstractTCViewTab, Integer> me : tabsToIndices.entrySet())
        {
            if (!me.getKey().isTabEnabled())
            {
                indices.add(me.getValue());
            }
        }
        return indices;
    }
    
    public List<Integer> getHiddenTabIndices()
    {
        List<Integer> indices = new ArrayList<Integer>();
        for (Map.Entry<AbstractTCViewTab, Integer> me : tabsToIndices.entrySet())
        {
            if (!me.getKey().isTabVisible())
            {
                indices.add(me.getValue());
            }
        }
        return indices;
    }
    
    @Override
    protected void onComponentTag(ComponentTag tag)
    {
        super.onComponentTag(tag);
        
        tag.put("style", "height:100%;width:100%");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private IModel<TCEditableObject> getModel()
    {
        return (IModel) getDefaultModel();
    }
    
    private void initWebviewerLinkProvider() {
        List<String> names = WebCfgDelegate.getInstance()
                .getWebviewerNameList();
        if (names == null) {
            names = WebCfgDelegate.getInstance()
                    .getInstalledWebViewerNameList();
        }
        if (names == null || names.isEmpty()) {
            webviewerLinkProviders = null;
        } else {
            webviewerLinkProviders = new WebviewerLinkProvider[names.size()];
            Map<String, String> baseUrls = WebCfgDelegate.getInstance()
                    .getWebviewerBaseUrlMap();
            for (int i = 0; i < webviewerLinkProviders.length; i++) {
                webviewerLinkProviders[i] = new WebviewerLinkProvider(
                        names.get(i));
                webviewerLinkProviders[i]
                        .setBaseUrl(baseUrls.get(names.get(i)));
            }
        }
    }
    
    public abstract static class AbstractTCViewTab extends Panel
    {
        public AbstractTCViewTab(final String id, IModel<? extends TCObject> model)
        {
            super(id, model);
        }
        
        public TCObject getTC()
        {
            return (TCObject) super.getDefaultModelObject();
        }
        
        public boolean isEditable()
        {
            return false;
        }
        
        public boolean isEditing()
        {
            return false;
        }
        
        public void setEditing(boolean editing, AjaxRequestTarget target)
        {
            /* do nothing by default */
        }
        
        public boolean isTabEnabled()
        {
            return hasContent();
        }
        
        public boolean isTabVisible()
        {
            return true;
        }
        
        public abstract String getTabTitle();
        
        public abstract boolean hasContent();
        
        protected String getStringValue(TCQueryFilterKey key) {
            TCObject tc = getTC();

            String s = tc != null ? tc.getValueAsLocalizedString(key,this) : null;

            return s != null ? s : "";
        }
    }
        
    public abstract static class AbstractEditableTCViewTab extends
        AbstractTCViewTab
    {
        private boolean editing;
        
        public AbstractEditableTCViewTab(final String id, IModel<TCEditableObject> model, boolean editing)
        {
            super(id, model);
            
            this.editing = editing;
        }
        
        public AbstractEditableTCViewTab(final String id, IModel<TCEditableObject> model)
        {
            this(id, model, false);
        }
        
        @Override
        public TCEditableObject getTC()
        {
            return (TCEditableObject) super.getTC();
        }
        
        @Override
        public final boolean isEditable()
        {
            return true;
        }
        
        @Override
        public final boolean isEditing()
        {
            return editing;
        }
        
        public final void save()
        {
            if (isEditing())
            {
                saveImpl();
            }
        }

        protected abstract void saveImpl();
        
        protected void tabTitleChanged(AjaxRequestTarget target)
        {
            /* do nothing by default */
        }

        protected AttributeModifier createTextInputCssClassModifier()
        {
            return new AttributeAppender("class",true,new Model<String>(
                    isEditing() ? "tc-view-input-editable" : "tc-view-input-non-editable"), " ");
        }
    }
}
