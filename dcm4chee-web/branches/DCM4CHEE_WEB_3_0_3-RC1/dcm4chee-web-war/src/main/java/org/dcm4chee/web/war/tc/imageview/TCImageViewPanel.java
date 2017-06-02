package org.dcm4chee.web.war.tc.imageview;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.dcm4che2.data.Tag;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.tc.TCPopupManager;
import org.dcm4chee.web.war.tc.TCPopupManager.AbstractTCPopup;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition.PopupAlign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since Jan 25, 2012
 */
public class TCImageViewPanel extends Panel 
{
    private static final Logger log = LoggerFactory.getLogger(TCImageViewPanel.class);

    private Label numberLabel;
    private TCWadoImage currentImage;
    private Component next;
    private Component prev;
    private AjaxLink<Void> seriesChooser;
    private TCImageViewThumbnailPanel list;
    
    public TCImageViewPanel(final String id)
    {
        this(id, null);
    }
    
    public TCImageViewPanel(final String id, ListModel<? extends TCImageViewStudy> model)
    {
        super(id, model);

        final List<SeriesProxy> seriesList = createSeriesProxies(getModelObject());

        prev = new AjaxLink<Void>("tc-view-images-prev-btn") {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                try
                {
                    int index = list.getPreviousImageIndex();
                    TCImageViewImage image = list.getPreviousImage();
                    if (image!=null)
                    {
                        list.setCurrentImage(image, target);

                        target.addComponent(numberLabel);
                        target.appendJavascript(getSetCurrentImageJavascript(currentImage, image));
                        target.appendJavascript(getUpdatePreviousLinkJavascript(this, index));
                        target.appendJavascript(getUpdateNextLinkJavascript(next, index, list.getImageCount()));
                    }
                }
                catch (Exception e)
                {
                    log.error("Displaying previous image of teaching-file failed!", e);
                }
            }
        }
        .add(new TooltipBehaviour("tc.view.images","previous"))
        .setOutputMarkupId(true)
        .setOutputMarkupPlaceholderTag(true);
        
        next = new AjaxLink<Void>("tc-view-images-next-btn") {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                try
                {
                    int index = list.getNextImageIndex();
                    TCImageViewImage image = list.getNextImage();
                    if (image!=null)
                    {
                        list.setCurrentImage(image, target);

                        target.addComponent(numberLabel);
                        target.appendJavascript(getSetCurrentImageJavascript(currentImage, image));
                        target.appendJavascript(getUpdatePreviousLinkJavascript(prev, index));
                        target.appendJavascript(getUpdateNextLinkJavascript(this, index, list.getImageCount()));
                    }
                }
                catch (Exception e)
                {
                    log.error("Displaying next image of teaching-file failed!", e);
                }
            }
        }
        .add(new TooltipBehaviour("tc.view.images","next"))
        .setOutputMarkupId(true)
        .setOutputMarkupPlaceholderTag(true);
        
        list = new TCImageViewThumbnailPanel("tc-view-thumbnail-container", 
                !seriesList.isEmpty()?seriesList.get(0).getSeries():null) {
            @Override
            protected void imageClicked(TCImageViewImage image, AjaxRequestTarget target)
            {
                target.addComponent(numberLabel);
                target.appendJavascript(getSetCurrentImageJavascript(currentImage, getCurrentImage()));
                target.appendJavascript(getUpdatePreviousLinkJavascript(prev, getCurrentImageIndex()));
                target.appendJavascript(getUpdateNextLinkJavascript(next, getCurrentImageIndex(), getImageCount()));
            }
        };
        list.setMarkupId("tc-view-images-thumbnail-container");
        
        currentImage = new TCWadoImage("tc-view-images-container-content-image", list.getCurrentImageModel());
        currentImage.setOutputMarkupId(true);
        currentImage.add(new AjaxEventBehavior("ondblclick") {
			private static final long serialVersionUID = -60699490892951485L;
			@Override
			public void onEvent(AjaxRequestTarget target)
        	{
				target.appendJavascript(new TCImagePage(list.getCurrentImageModel()).getOpenInWindowJavascript(null));
        	}
        });
        
        numberLabel = new Label("tc-view-images-numberOf", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                return MessageFormat.format(getString("tc.view.images.number.text"),
                      list.getCurrentImageIndex()+1, list.getImageCount());  
            }
        });
        numberLabel.setOutputMarkupId(true);
        
        final SeriesListPopup seriesPopup = new SeriesListPopup(seriesList, getPopupManager()) {
            @Override
            protected void seriesClicked(TCImageViewSeries series, AjaxRequestTarget target)
            {
                hide(target);

                if (list.setSeries(series, target))
                {
                    target.addComponent(seriesChooser);
                    target.addComponent(numberLabel);
                    target.appendJavascript(getSetCurrentImageJavascript(currentImage, list.getCurrentImage()));
                    target.appendJavascript(getUpdatePreviousLinkJavascript(prev, list.getCurrentImageIndex()));
                    target.appendJavascript(getUpdateNextLinkJavascript(next, list.getCurrentImageIndex(), list.getImageCount()));
                }
            }
        };
        
        seriesChooser = new AjaxLink<Void>("tc-view-images-series-chooser") {
            @Override
            public void onClick(AjaxRequestTarget target) 
            {
                seriesPopup.show(target, new TCPopupPosition(
                        seriesChooser.getMarkupId(),
                        seriesPopup.getMarkupId(), 
                        PopupAlign.BottomLeft, PopupAlign.TopLeft));
            }
        };
        seriesChooser.add(new Image("tc-view-images-series-chooser-img", ImageManager.IMAGE_TC_ARROW_DOWN)
        .add(new ImageSizeBehaviour("vertical-align:middle;")));
        seriesChooser.add(new TooltipBehaviour("tc.view.images","serieschooser"));
        seriesChooser.setOutputMarkupId(true);
        seriesChooser.setMarkupId("tc-view-images-series-chooser");
        seriesChooser.add(new Label("tc-view-images-series-chooser-label", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject()
            {
                TCImageViewSeries current = list.getSeries();
                if (current!=null)
                {
                    for (SeriesProxy s : seriesList)
                    {
                        if (s.getSeries().equals(current))
                        {
                            return s.toString();
                        }
                    }
                }
                return null;
            }
        }));
        
        seriesChooser.setVisible(seriesList.size()>1);

        prev.add(new AttributeModifier("style",true,
                new Model<String>("visibility:"+
                        (list.getCurrentImageIndex()>0&&list.getImageCount()>1?"visible":"hidden"))));
        next.add(new AttributeModifier("style",true,
                new Model<String>("visibility:"+
                        (list.getCurrentImageIndex()<list.getImageCount()-1&&list.getImageCount()>1?"visible":"hidden"))));

        add(seriesChooser);
        add(seriesPopup);
        add(currentImage);          
        add(list);
        add(prev);
        add(numberLabel);
        add(next);
    }
    
    @SuppressWarnings("unchecked")
    public ListModel<? extends TCImageViewStudy> getModel()
    {
        return(ListModel<? extends TCImageViewStudy>) getDefaultModel();
    }
    
    public List<? extends TCImageViewStudy> getModelObject()
    {
        return getModel()!=null?getModel().getObject():null;
    }
    
    public void setModel(ListModel<? extends TCImageViewStudy> model)
    {
        setDefaultModel(model);
    }
    
    protected TCPopupManager getPopupManager()
    {
        return null;
    }

    private String getSetCurrentImageJavascript(TCWadoImage currentImage, TCImageViewImage image)
    {
        StringBuffer sbuf = new StringBuffer();

        sbuf.append("$('#").append(currentImage.getMarkupId(true)).append("')");
        sbuf.append(".attr('src','");
        sbuf.append(TCWadoImage.getWadoUrl(image.getSeries().getStudy().getStudyUID(),
                image.getSeries().getSeriesUID(),image.getInstanceUID(),image.getFrameNumber()));
        sbuf.append("');\n");
                        
        return sbuf.toString();
    }
    
    private String getUpdatePreviousLinkJavascript(Component prev, int index)
    {
        StringBuffer sbuf = new StringBuffer();
        
        sbuf.append("$('#").append(prev.getMarkupId(true)).append("')");
        sbuf.append(".css('visibility','").append(index<=0?"hidden":"visible").append("');\n");

        return sbuf.toString();
    }
    
    private String getUpdateNextLinkJavascript(Component next, int index, int imageCount)
    {
        StringBuffer sbuf = new StringBuffer();

        sbuf.append("$('#").append(next.getMarkupId(true)).append("')");
        sbuf.append(".css('visibility','").append(index>=imageCount-1?"hidden":"visible").append("');\n");
        
        return sbuf.toString();
    }
            
    private List<SeriesProxy> createSeriesProxies(List<? extends TCImageViewStudy> studies)
    {
        List<SeriesProxy> list = new ArrayList<SeriesProxy>();
        
        if (studies!=null)
        {
            for (TCImageViewStudy study : studies)
            {
                if (study.getSeriesCount()>0)
                {
                    for (TCImageViewSeries series : study.getSeries())
                    {
                        if (series.getImageCount()>0)
                        {
                            list.add(new SeriesProxy(series));
                        }
                    }
                }
            }
        }
        
        return list;
    }
    
    public class SeriesProxy implements Serializable
    {
        private TCImageViewSeries series;
        
        private final Pattern pattern = 
            Pattern.compile("\\$\\(.*?\\)", Pattern.DOTALL); //$NON-NLS-1$
        
        public SeriesProxy(TCImageViewSeries series)
        {
            this.series = series;
        }
        
        public TCImageViewSeries getSeries()
        {
            return series;
        }
        
        @Override
        public String toString()
        {
            String format = WebCfgDelegate.getInstance().getTCSeriesDisplayFormat();
            Matcher matcher = pattern.matcher(format);
            StringBuffer sbuffer = new StringBuffer();
            boolean error = false;
            
            while (matcher.find()) {
                String group = matcher.group();

                if (group!=null) {
                    try {
                        //get the tag descriptor to be replaced
                        String tag = group.substring(2,group.length()-1);

                        if ("ImageCount".equalsIgnoreCase(tag))
                        {
                            matcher.appendReplacement(sbuffer, 
                                    Integer.toString(series.getImageCount()));
                        }
                        else if ("NumberOfStudy".equalsIgnoreCase(tag))
                        {
                            matcher.appendReplacement(sbuffer,
                                    Integer.toString(getNumberOfStudy()));
                        }
                        else if ("NumberOfSeries".equalsIgnoreCase(tag))
                        {
                            matcher.appendReplacement(sbuffer,
                                    Integer.toString(getNumberOfSeries()));
                        }
                        else
                        {
                            matcher.appendReplacement(sbuffer, 
                                    getStringValue(Tag.forName(tag)));
                        }
                    }
                    catch (Exception e) {
                        log.warn("Parsing series display format failed! Using default...", e); //$NON-NLS-1$
                        error = true;
                    }
                }
            }

            if (!error)
            {
                matcher.appendTail(sbuffer);
            }
            else
            {
                int nStudy = getNumberOfStudy();
                int nSeries = getNumberOfSeries();
                int nImages = series.getImageCount();
                
                sbuffer.replace(0,sbuffer.length(),
                        MessageFormat.format(getString("tc.view.images.series.text"),
                        nStudy, nSeries, nImages));  
            }
            
            return sbuffer.toString(); 
        }
        
        private int getNumberOfStudy()
        {
            List<? extends TCImageViewStudy> studies = getModel().getObject();
            return studies.indexOf(series.getStudy())+1;
        }
        
        private int getNumberOfSeries()
        {
            return series.getStudy().getSeries().indexOf(series)+1;
        }
        
        private String getStringValue(int tag)
        {
            String value = series.getSeriesValue(tag);
            if (value==null)
            {
                value = series.getStudy().getStudyValue(tag);
            }
            if (value==null)
            {
                value = getString("tc.view.images.series.unknown.text");
            }
            return value;
        }
    }
    
    protected abstract class SeriesListPopup extends AbstractTCPopup 
    {
        public SeriesListPopup(List<SeriesProxy> series, TCPopupManager manager) 
        {
            super("tc-view-images-series-popup", manager, false, true, true, true);

            setMarkupId("tc-view-images-series-popup");
            
            RepeatingView list = new RepeatingView("tc-view-images-series-item");
            int i=1;
            for (final SeriesProxy proxy : series)
            {
                AjaxLink<String> link = new AjaxLink<String>("item"+i, new Model<String>(proxy.toString())) {
                    @Override
                    public void onClick(AjaxRequestTarget target)
                    {
                        seriesClicked(proxy.getSeries(), target);
                    }
                };
                
                link.add(new Label("tc-view-images-series-item-label", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return proxy.toString();
                    }
                }));
                
                list.add(link);
                i++;
            }
            
            add(list);
        }
        
        protected abstract void seriesClicked(TCImageViewSeries series, AjaxRequestTarget target);
    }
}
