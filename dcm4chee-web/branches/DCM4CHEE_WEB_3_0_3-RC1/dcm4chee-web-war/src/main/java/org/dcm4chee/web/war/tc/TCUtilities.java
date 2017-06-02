package org.dcm4chee.web.war.tc;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.war.common.AutoSelectInputTextBehaviour;
import org.dcm4chee.web.war.tc.TCObject.DicomCode;
import org.dcm4chee.web.war.tc.TCObject.ITextOrCode;
import org.dcm4chee.web.war.tc.keywords.TCKeyword;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogue;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;
import org.dcm4chee.web.war.tc.keywords.TCKeywordInput;
import org.dcm4chee.web.war.tc.keywords.TCKeywordTextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since Dec 02, 2011
 */
public class TCUtilities 
{
    private static final Logger log = LoggerFactory.getLogger(TCUtilities.class);
 
    public static enum PopupAlign {
        TopLeft("left top"),
        TopRight("right top"),
        BottomLeft("left bottom"),
        BottomRight("right bottom"),
        Center("center center");
        
        private String align;
        
        private PopupAlign(String align)
        {
            this.align = align;
        }
        
        public String getAlign()
        {
            return align;
        }
    }
    
    public static boolean equals(Object o1, Object o2)
    {
        if (o1==null && o2==null) return true;
        else if (o1!=null && o2==null) return false;
        else if (o1==null && o2!=null) return false;
        else return o1.equals(o2);
    }

    public static String getOpenWindowJavascript(Page page, String title, boolean includeReturnStatement)
    {
        return getOpenWindowJavascript(page, title, new TCPopupSettings(), includeReturnStatement);
    }

    public static String getOpenWindowJavascript(Page page, String title, TCPopupSettings settings, boolean includeReturnStatement)
    {
        settings.setTarget("'" + RequestCycle.get().urlFor(page) + "'");

        if (title!=null)
        {
            settings.setWindowName(title);
        }
        
        boolean center = false;
        
        //center window, if neither top nor left have been explicitly specified
        if (settings.getTop()<0 && settings.getLeft()<0)
        {
            //..but width and height are available
            if (settings.getWidth()>=0 && settings.getHeight()>=0)
            {
                settings.setTop(-9999);
                settings.setLeft(-9999);
                center = true;
            }
        }
        
        StringBuffer sbuf = new StringBuffer(
                settings.getPopupJavaScript());
        
        final String returnStatement = "return false;";
        int i = sbuf.indexOf(returnStatement);
        if (!includeReturnStatement && i>=0)
        {
        	sbuf.delete(i, i+returnStatement.length());
        }
        else if (includeReturnStatement && i<0)
        {
        	sbuf.append(returnStatement);
        }
        
        
        sbuf.insert(0, "try {\n");
        sbuf.append("} catch(e) {\n    console.error(e);\n}");
        
        String script = sbuf.toString();
        
        if (center)
        {
            script = script.replaceFirst("top=-9999", "top=' + (screen.availHeight/2-" + settings.getHeight()/2 + ") + '");
            script = script.replaceFirst("left=-9999", "left=' + (screen.availWidth/2-" + settings.getWidth()/2 + ") + '");
        }
        
        return script;
    }
    
    public static TCInput createInput(final String componentId,
            TCQueryFilterKey key, Object value) {
        return createInput(componentId, key, value, true);
    }
    
    public static TCInput createInput(final String componentId,
            TCQueryFilterKey key, Object value, boolean checkExclusive) {
        TCKeywordCatalogueProvider p = TCKeywordCatalogueProvider.getInstance();

        if (p.hasCatalogue(key)) 
        {
            TCKeywordCatalogue cat = p.getCatalogue(key);
            TCKeyword keyword = null;
            String svalue = null;
            if (value instanceof Code)
            {
                svalue = ((Code) value).getCodeValue();
            }
            else if (value instanceof DicomCode)
            {
                svalue = ((DicomCode)value).getValue();
            }
            else if (value instanceof ITextOrCode) {
                DicomCode code = ((ITextOrCode)value).getCode();
                if (code!=null) {
                    svalue = code.getValue();
                }
                else {
                    svalue = ((ITextOrCode)value).getText();
                }
            }
            
            if (svalue!=null && !svalue.trim().isEmpty()) {
                keyword = cat.findKeyword(svalue);
            }
            
            if (keyword==null && svalue!=null) {
                keyword = new TCKeyword(svalue, null, false);
            }
            
            TCKeywordInput input = cat.createInput(componentId, keyword);
            
            if (checkExclusive)
            {
                input.setExclusive(TCKeywordCatalogueProvider.getInstance().
                        isCatalogueExclusive(key));
            }
            
            return input;
        } 
        else 
        {
            return new TCKeywordTextInput(componentId, 
                    value!=null?value.toString():null);
        }
    }
    
    public static <T> DropDownChoice<T> createDropDownChoice(
            final String id, IModel<T> model, List<T> options,
            final NullDropDownItem nullItem) {
        return createDropDownChoice(id, model, options, nullItem, null);
    }
    
    public static <T> DropDownChoice<T> createDropDownChoice(
            final String id, IModel<T> model, List<T> options,
            final NullDropDownItem nullItem, final TCChangeListener<T> l) {
        DropDownChoice<T> choice = new SelfUpdatingDropDownChoice<T>(id, model!=null?model.getObject():null, options) {

            private static final long serialVersionUID = 1L;

            @Override
            protected String getNullValidKey() {
                if (nullItem!=null)
                {
                    return nullItem.getKey();
                }
                return null;
            }
            
            @Override
            protected void valueUpdated(T value, AjaxRequestTarget target)
            {
                if (l!=null)
                {
                    l.valueChanged(value);
                }
            }
        };

        choice.setNullValid(nullItem!=null && 
                !NullDropDownItem.NotValid.equals(nullItem));

        return choice;
    }
    
    public static <T extends Enum<T>> DropDownChoice<T> createEnumDropDownChoice(
            final String id, IModel<T> model, List<T> options,
            boolean localizeValues, final String localizePrefix) {
        return createEnumDropDownChoice(id, model, options, localizeValues, 
                localizePrefix, NullDropDownItem.NotValid);
    }
    
    public static <T extends Enum<T>> DropDownChoice<T> createEnumDropDownChoice(
            final String id, IModel<T> model, List<T> options,
            boolean localizeValues, final String localizePrefix, final NullDropDownItem nullItem) {
        return createEnumDropDownChoice(id, model, options, localizeValues, localizePrefix, nullItem, null);
    }
    
    public static <T extends Enum<T>> DropDownChoice<T> createEnumDropDownChoice(
            final String id, IModel<T> model, List<T> options,
            boolean localizeValues, final String localizePrefix, final NullDropDownItem nullItem, final TCChangeListener<T> l) {
        DropDownChoice<T> choice = new SelfUpdatingDropDownChoice<T>(id, model!=null?model.getObject():null, options) {

            private static final long serialVersionUID = 1L;

            @Override
            protected String getNullValidKey() {
                if (nullItem!=null)
                {
                    return nullItem.getKey();
                }
                return null;
            }
            
            @Override
            protected void valueUpdated(T value, AjaxRequestTarget target)
            {
                if (l!=null)
                {
                    l.valueChanged(value);
                }
            }
        };

        choice.setNullValid(nullItem!=null && 
                !NullDropDownItem.NotValid.equals(nullItem));

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
    
    public static interface TCChangeListener<T> extends Serializable
    {
        public void valueChanged(T value);
    }
    
    public static enum NullDropDownItem
    {
        NotValid(null),
        All("nullitem.all.text"),
        Undefined("nullitem.undefined.text");
        
        private String key;
        
        private NullDropDownItem(String keySuffix)
        {
            this.key = keySuffix!=null ?
                    TCPanel.ModuleName + "." + keySuffix : null;
        }
        
        public String getKey()
        {
            return key;
        }
    }
    
    public static class SelfUpdatingCheckBox extends CheckBox 
    {
        private Boolean selected;

        public SelfUpdatingCheckBox(String id, Boolean value) 
        {
            super(id);
            this.selected = value!=null ? value : Boolean.FALSE;
            setModel(new PropertyModel<Boolean>(this, "selected"));
            add(new AjaxFormComponentUpdatingBehavior("onchange"){
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    selectionChanged(isSelected());
                }
            });
        }

        public boolean isSelected(){ return selected!=null?selected.booleanValue():false; }
        public void setSelected(Boolean selected) { this.selected = 
            selected!=null?selected:Boolean.FALSE; }
        protected void selectionChanged(boolean selected) {}
    }
    
    public static class SelfUpdatingTextField extends TextField<String> 
    {
        private String text;

        public SelfUpdatingTextField(String id, String text) 
        {
            super(id);
            this.text = text;
            setModel(new PropertyModel<String>(this, "text"));
            add(new AjaxFormComponentUpdatingBehavior("onchange"){
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    textUpdated(SelfUpdatingTextField.this.text);
                }
            });
            add(new AutoSelectInputTextBehaviour());
        }

        public String getText(){ return text; }
        public void setText(String text) { this.text = text; }
        protected void textUpdated(String text) {}
    }
    
    public static class SelfUpdatingTextArea extends TextArea<String> 
    {
        private String text;

        public SelfUpdatingTextArea(String id, String text) 
        {
            super(id);
            this.text = text;
            setModel(new PropertyModel<String>(this, "text"));
            add(new AjaxFormComponentUpdatingBehavior("onchange"){
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    textUpdated(SelfUpdatingTextArea.this.text);
                }
            });
            add(new AutoSelectInputTextBehaviour());
        }

        public String getText(){ return text; }
        public void setText(String text) { this.text = text; }
        protected void textUpdated(String text) {}
    }
    
    public static class SelfUpdatingDropDownChoice<T> extends DropDownChoice<T>
    {
        private T selectedValue;
        
        public SelfUpdatingDropDownChoice(final String id, T selectedValue, List<T> options)
        {
            super(id, null, options);
            this.selectedValue = selectedValue;
            setModel(new PropertyModel<T>(this, "selectedValue"));
            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target)
                {
                    valueUpdated(SelfUpdatingDropDownChoice.this.selectedValue, target);
                }
            });
        }
        
        public T getSelectedValue() { return selectedValue; }
        public void setSelectedValue(T value) { this.selectedValue = value; }
        protected void valueUpdated(T value, AjaxRequestTarget target) {}
    }
    
    public static class TCPopupSettings extends PopupSettings
    {
        public TCPopupSettings(int flags)
        {
            super(flags);
        }
        
        public TCPopupSettings()
        {
            super();
        }
        
        public int getWidth()
        {
            return getPrivateFieldValueInt("width");
        }
        
        public int getHeight()
        {
            return getPrivateFieldValueInt("height");
        }
        
        public int getTop()
        {
            return getPrivateFieldValueInt("top");
        }
        
        public int getLeft()
        {
            return getPrivateFieldValueInt("left");
        }
                
        private int getPrivateFieldValueInt(String fieldName)
        {
            try
            {
                Field field = PopupSettings.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.getInt(this);
            }
            catch (Exception e)
            {
                log.error(null, e);
                return -1;
            }
        }
    }
    
    public abstract static class AjaxMouseEventBehavior extends AjaxEventBehavior
    {
        public AjaxMouseEventBehavior(String event)
        {
            super(event);
        }
        
        @Override
        public CharSequence getCallbackUrl(final boolean onlyTargetActivePage) 
        {
            return super.getCallbackUrl(onlyTargetActivePage) +
                "&bubbling=' + event.target!=this + '" +
                "&mouseX=' + event.pageX + '" +
                "&mouseY=' + event.pageY + '" +
                "&targetX=' + $(this).offset().left + '" +
                "&targetY=' + $(this).offset().top + '" +
                "&targetWidth=' + $(this).width() + '" +
                "&targetHeight=' + $(this).height() + '";
        }
        
        @Override
        protected final void onEvent(AjaxRequestTarget target)
        {
            Request request = RequestCycle.get().getRequest();
            String bubbling = request.getParameter("bubbling");
            String mouseX = request.getParameter("mouseX");
            String mouseY = request.getParameter("mouseY");
            String targetX = request.getParameter("targetX");
            String targetY = request.getParameter("targetY");
            String targetWidth = request.getParameter("targetWidth");
            String targetHeight = request.getParameter("targetHeight");
            onEvent(target,
                    Boolean.valueOf(bubbling),
                    new Point( 
                        Double.valueOf(mouseX).intValue(), 
                        Double.valueOf(mouseY).intValue()),
                    new Rectangle(
                        Double.valueOf(targetX).intValue(),
                        Double.valueOf(targetY).intValue(),
                        Integer.valueOf(targetWidth),
                        Integer.valueOf(targetHeight)));
        }
        
        protected abstract void onEvent(AjaxRequestTarget target, boolean bubbling, Point mousePos, Rectangle srcBounds);
    }
}
