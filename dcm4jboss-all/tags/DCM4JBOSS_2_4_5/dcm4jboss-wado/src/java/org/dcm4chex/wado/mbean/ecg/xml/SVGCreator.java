/*
 * Created on 13.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg.xml;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.mbean.ecg.WaveFormChannel;
import org.dcm4chex.wado.mbean.ecg.WaveformGroup;
import org.dcm4chex.wado.mbean.ecg.WaveformInfo;
import org.dcm4chex.wado.mbean.xml.XMLResponseObject;
import org.dcm4chex.wado.mbean.xml.XMLUtil;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SVGCreator implements XMLResponseObject{

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
	
	private static final float PIX_PER_MV = -100f;// (negative because x=0 is top)  1mV/cm
	private static final float PIX_PER_SEC = 250f;// is400ms/cm -> 25mm/s / 1mm = 10 pix
	
	private static final NumberFormat cmFormatter = new DecimalFormat("##.##cm", new DecimalFormatSymbols( new Locale( "en", "us")));
	
	private static Logger log = Logger.getLogger( SVGCreator.class.getName() );
	
    private TransformerHandler th = null;
    private XMLUtil util = null;

    private String width = "26.60cm";
	private String height = "20.3cm";
	private int viewBoxY1 = 0;
	private int viewBoxX1 = 0;
	private float viewBoxWidth = 2660;
	private float viewBoxHeight = 2030;
	
	private float graphicXOffset = 0;
	private float graphicYOffset = 0;
	
	private int graphicWidthCm = 25;//in cm
	private int graphicHeightCm = 19;//in cm
	
	private int graphicWidth = graphicWidthCm*100;//in pix
	private int graphicHeight = graphicHeightCm*100;//in pix
	
	private WaveformGroup waveForms;
	private WaveformInfo info;
	private String nameSpace = null;
	private float scaling = 0.28169f;
	

	/**
	 * 
	 * @param wfgrp
	 * @param wfInfo
	 * @param width width in cm
	 * @param height height in cm
	 */
	public SVGCreator(WaveformGroup wfgrp, WaveformInfo wfInfo, Float width, Float height) {
		waveForms = wfgrp;
		info = wfInfo;
		if ( width != null ) {
			setPageWidth( width.floatValue() );
		} else {
			float prefWidth = PIX_PER_SEC / wfgrp.getSampleFreq() * (float) wfgrp.getNrOfSamples();
			prefWidth *= 2;//two columns!
			prefWidth += 110;//space for calPulse
			if ( log.isDebugEnabled() ) log.debug("calculated width:"+prefWidth );
			if ( prefWidth > this.viewBoxWidth ) { //if prefWidth < default -> dont change default width!
				setPageWidth( prefWidth / 100 );//in cm
			}
		}
		if ( height != null ) {
			this.height = cmFormatter.format( height );
			viewBoxHeight = height.floatValue()*100;
			graphicHeightCm = height.intValue();//round to cm
			graphicHeight = graphicHeightCm*100;
		}
	}
	
	private void setPageWidth( float width ) {
		this.width = cmFormatter.format( width );
		viewBoxWidth = width*100;
		graphicWidthCm = new Float(width).intValue();//round to cm
		graphicWidth = graphicWidthCm*100;
		float delta = viewBoxWidth - graphicWidth;
		graphicXOffset = delta / 2;
	}
	
	/**
	 * @return Returns the viewBoxX1.
	 */
	public int getViewBoxX1() {
		return viewBoxX1;
	}
	/**
	 * @param viewBoxX1 The viewBoxX1 to set.
	 */
	public void setViewBoxX1(int viewBoxX1) {
		this.viewBoxX1 = viewBoxX1;
	}
	/**
	 * @return Returns the viewBoxX2.
	 */
	public float getViewBoxWidth() {
		return viewBoxWidth;
	}
	/**
	 * @param width The width to set.
	 */
	public void setViewBoxWidth(float width) {
		this.viewBoxWidth = width;
	}
	/**
	 * @return Returns the viewBoxY1.
	 */
	public int getViewBoxY1() {
		return viewBoxY1;
	}
	/**
	 * @param viewBoxY1 The viewBoxY1 to set.
	 */
	public void setViewBoxY1(int viewBoxY1) {
		this.viewBoxY1 = viewBoxY1;
	}
	/**
	 * @return Returns the viewBoxY2.
	 */
	public float getViewBoxHeight() {
		return viewBoxHeight;
	}
	/**
	 * @param height The height to set.
	 */
	public void setViewBoxHeight(float height) {
		this.viewBoxHeight = height;
	}
	/**
	 * @return Returns the width.
	 */
	public String getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(String width) {
		this.width = width;
	}
	public void toXML( OutputStream out ) throws TransformerConfigurationException, SAXException {
			        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

    	th = tf.newTransformerHandler();
    	th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult( new StreamResult(out) );
        nameSpace = null;
        th.startDocument();
        toXML();
        th.endDocument();
	    try {
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void embedXML( TransformerHandler th ) throws TransformerConfigurationException, SAXException {
		this.th = th;
		nameSpace = "svg";
		if ( log.isDebugEnabled() ) log.debug("embedXML called!");
		toXML();
	}
	
	protected void setXMLUtil( XMLUtil util ) {
		this.util = util;
	}
	
    private void toXML() throws SAXException {
		if ( log.isDebugEnabled() ) log.debug("toXML called!");
        util = new XMLUtil( th, nameSpace );
        addSVGStart();
        	addTitleAndDesc();
        	if ( info != null ) {
        		addTextSegment();
	        	addHeaderSeparator();
	        	addShortmeasSegment();
	        	addSeveritySegment();
	        	addInterpContinue();
	        	addLeftstatementSegment();
	        	addRightstatementSegment();
	        	addInterpSeparator();
        	}
        	addFooter();
        	addG("scaling", "translate("+this.graphicXOffset*scaling+","+(this.graphicYOffset+100)*scaling+"), scale("+scaling +")",null,null,null);
        	addDefs();
        	addGrid( graphicWidthCm, graphicHeightCm );
    		if ( log.isDebugEnabled() ) log.debug("grid added!");
    		addGraphic( graphicWidthCm, graphicHeightCm );
    		if ( log.isDebugEnabled() ) log.debug("graph added!");
        	util.endElement("g");
        util.endElement( "svg" );
				
        
	}
	
	private void addTitleAndDesc() throws SAXException {
		util.startElement("title", EMPTY_ATTRIBUTES );
		util.addValue( "TITLE" );
		util.endElement("title" );
		util.startElement("desc", EMPTY_ATTRIBUTES );
		util.addValue( "Description" );
		util.endElement("desc" );
	}
	
	private void addTextSegment() throws SAXException {
		addG( "textSegment", "translate(0,30)",null,"40", null);
		addText( "20","0","10",null,null, "2");//?? what is 2 ?
		addText( "20","25","10",null,null, "");//?? what is "" ?
		addText( "140","0","10",null,null, info.getAcquisDate());
		addText( "140","25","10",null,null, info.getBirthday());
		addText( "210","0","10",null,null, info.getAcquisTime());
		addText( "210","25","10",null,null, info.getSex());
		addText( "300","0","14",null,null, info.getPatientName());
		addText( "300","25","10",null,null, "0/0");//?? what is 0/0 ?
		addText( "330","25","10",null,null, info.getPatientSize());
		addText( "400","25","10",null,null, info.getPatientWeight());

		Properties props = new Properties();
		props.setProperty("text-anchor", "end");
		addText( "700","15","10","30",props, "Department:");
		addText( "700","30","10","30",props, "Room:");
		addText( "700","50","10","30",props, "Operator:");
		
		util.endElement( "g" );
		this.graphicYOffset += 240;
	}

	private void addHeaderSeparator() throws SAXException {
		addPath( "headerSeperator", "fill:none; stroke:black","5","M 0 90 H "+this.viewBoxWidth*scaling );
	}
	private void addShortmeasSegment() {
		//TODO
	}
	private void addSeveritySegment() {
		//TODO
	}
	private void addInterpContinue() {
		//TODO
	}
	private void addLeftstatementSegment() {
		//TODO
	}
	private void addRightstatementSegment() {
		//TODO
	}
	private void addInterpSeparator() {
		//TODO
	}
	private void addFooter() throws SAXException {
		addG( "footer", "translate(0,"+(this.graphicYOffset+graphicHeight+150)*this.scaling+")", "black", "40", null );
		{
			float endX = (graphicXOffset + graphicWidth - 60)*scaling;
			Properties props = new Properties();
			props.setProperty("id","filtertext");
			props.setProperty("text-anchor", "end");
			addText( String.valueOf(endX), "0", null, null, props, waveForms.getFilterText() );
			addText( String.valueOf( endX - 500 ), "0", null, null, null, "25mm/sec" );
			addText( String.valueOf( endX - 400 ), "0", null, null, null, "10mm/mV" );
		}
		util.endElement( "g" );
	}
	
	protected void addDefs() throws SAXException {
		util.startElement("defs", EMPTY_ATTRIBUTES );
	    	addPath( "1mmXd", "fill:none; stroke:red", null, "M 0 0 V "+graphicHeight );
	    	addPath( "1mmYd", "fill:none; stroke:red", null, "M 0 0 H "+graphicWidth );
	    	//Y
	    	addG( "1cmY", null, null, null, null );
	    		String s1 = "M 0 ";
	    		String s2 = " H "+(graphicWidth);
	    		float step = 10;
	    		for ( int i = 0 ; i < 4 ; i++, step += 10 ) {
	    	    	addPath( "1mmY", "fill:none; stroke:pink", null, s1+step+s2 );
	    	    	addPath( "1mmY", "fill:none; stroke:pink", null, s1+(step+50)+s2 );
	    		}
	    		util.endElement("g" );
    		//X
	    	addG( "1cmX", null, null, null, null );
	    		step = 10;
	    		s1 = "M ";
	    		s2 = " 0 V "+graphicHeight;
	    		for ( int i = 0 ; i < 4 ; i++, step += 10 ) {
	    	    	addPath( "1mmX", "fill:none; stroke:pink", null, s1+step+s2 );
	    	    	addPath( "1mmX", "fill:none; stroke:pink", null, s1+(step+50)+s2 );
	    		}
	    	util.endElement("g" );
	    util.endElement("defs" );
		
	}
	
	protected void addGrid( int width, int height ) throws SAXException {
    	addG( "Xlines", null, null, null, null );
    		float step = 0;
	    	for ( int i = 0 ; i < width ; i++, step+=100 ){
				addUse("#1cmX","translate("+step+",0)",null);
	    	}
	    	util.endElement("g" );

		addG( "Ylines", null, null, null, null );
		addUse("#1mmYd","translate(0,0)",null);
			step = 0;
	    	for ( int i = 0 ; i < height ; i++, step+=100 ){
				addUse("#1cmY","translate(0,"+(step)+")",null);
	    		addUse("#1mmYd","translate(0,"+(step+50)+")",null);
	    		addUse("#1mmYd","translate(0,"+(step+100)+")",null);
	    	}
	    util.endElement("g" );
    	addG( "Xlinesd", null, null, null, null );
		addUse("#1mmXd","translate(0,0)",null);
		step = 50;
    	for ( int i = 0 ; i < width ; i++, step+=100 ){
			addUse("#1mmXd","translate("+step+",0)",null);
			addUse("#1mmXd","translate("+(step+50)+",0)",null);
    	}
    	util.endElement("g" );
		
	}
	
	private void addGraphic( int width, int height ) throws SAXException{
		WaveformTemplate tmpl = WaveformTemplateFactory.getInstance( waveForms, (width-1)*100, height*100 );
		WaveformArea[] calPulseAreas = tmpl.getCalPulseAreas();
		WaveformArea[] wfAreas = tmpl.getWaveformAreas();
		WaveformArea area;
		if ( calPulseAreas != null ) {
			for ( int i = 0 ; i < calPulseAreas.length ; i++ ) {
				area = calPulseAreas[i];
				addCalPulse( i, area.getLeftX()+10, area.getTopY(), area.getHeight() );
			}
		}
		if ( wfAreas != null ) {
			for ( int i = 0 ; i < wfAreas.length ; i++ ) {
				area = wfAreas[i];
				addWaveform( area.getWaveformIndex(), area.getLeftX(), area.getTopY(), area.getHeight(), area.getWidth() );
			}
		}
	}

	/**
	 * @param topPos
	 * @throws SAXException
	 */
	private void addCalPulse(int row, float xOffset, float topPos, float height) throws SAXException {
		float baseLineY = topPos + height/2f;
		addG( "calpulse row"+row, "translate("+(graphicXOffset+xOffset)+","+baseLineY+")",null, null, null );
			addPath( "calpulse", "fill:none; stroke:black", "3", "M 0 0 H 25 V -100 H 75 V 0 H 100");
		util.endElement("g");
		
	}
	
	private void addWaveform(int lead, float xOffset, float topPos, float height, float width) throws SAXException {
		float baseLineY = topPos + height/2f;
		if ( log.isDebugEnabled() ) log.debug("topPos:"+topPos);
		addG( "lead"+lead, "translate("+(graphicXOffset+xOffset)+","+baseLineY+")",null, null, null );
			addText( "0", "-100", "30", "green", null, waveForms.getChannel( lead ).getChSource());
			addPath( "lead"+lead, "fill:none;stroke:black", "5", getWaveFormString( waveForms.getChannel( lead ), width ));
		    addPath( "waveseparator", "fill:none;stroke:green", "5" ,"M 0 -90 L 0 -20 M 0 90 L 0 20 ");
		   util.endElement("g");
	}

	/**
	 * @param channel
	 * @return
	 */
	private String getWaveFormString(WaveFormChannel channel, float width) {
		StringBuffer sb = new StringBuffer();
		float xDelta = PIX_PER_SEC / waveForms.getSampleFreq();
		int len = waveForms.getNrOfSamples();
		if ( log.isDebugEnabled() ) log.debug("NrOfSamples:"+len);
		if ( len * xDelta > width ) {
			log.info("correction: (len*xdelta):"+(len*xDelta)+">"+width);
			len = new Float( width / xDelta).intValue();
			log.info("xDelta:"+xDelta+" --> width/xDelta:"+( width / xDelta)+" len:"+len);
		}
		sb.append("M 0 0 L ");
		float currX = 0f;
		for ( int i = 0 ; i < len ; i++ ) {
			sb.append( currX ).append( " " ).append( channel.getValue() * PIX_PER_MV ).append(" ");
			currX += xDelta;
		}
		return sb.toString();
	}

	/**
	 * @param fontSize TODO
	 * @param props TODO
	 * @param i
	 * @param j
	 * @param string
	 * @param string2
	 * @throws SAXException
	 */
	private void addText(String x, String y, String fontSize, String fill, Properties props, String text) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        util.addAttribute( attr, "x", x );
        util.addAttribute( attr, "y", y );
        util.addAttribute( attr, "fill", fill );
        util.addAttribute( attr, "font-size", fontSize );
        util.addAttributes( attr, props );
        util.startElement("text", attr );
        	util.addValue(text);
        util.endElement("text" );
		
	}

	/**
	 * @param strokeWidth TODO
	 * @param string
	 * @param string2
	 * @param string3
	 * @throws SAXException
	 */
	private void addPath(String id, String style, String strokeWidth, String d) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        util.addAttribute( attr, "id", id );
        util.addAttribute( attr, "style", style );
        util.addAttribute( attr, "d", d );
		
        util.startElement("path", attr );
        util.endElement("path" );
		
	}
	
	private void addG( String id, String transform, String fill, String fontSize, Properties props ) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        util.addAttribute( attr, "id", id );
        util.addAttribute( attr, "transform", transform );
        util.addAttribute( attr, "fill", fill );
        util.addAttribute( attr, "fontSize", fontSize );
        util.addAttributes( attr, props );

        util.startElement("g", attr );
        
	}

	/**
	 * @param xlinkHref
	 * @param transform
	 * @param object
	 * @throws SAXException
	 */
	private void addUse(String xlinkHref, String transform, Properties props) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        util.addAttribute( attr, "xlink:href", xlinkHref );
        util.addAttribute( attr, "transform", transform );
        util.addAttributes( attr, props );

        util.startElement("use", attr );
        util.endElement( "use" );
	}
	
	protected void addSVGStart() throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        util.addAttribute( attr, "xmlns:xlink", "http://www.w3.org/1999/xlink" );
        //util.addAttribute( attr, "onload", "initialize(evt)" );
        
        util.addAttribute( attr, "width", width );
        util.addAttribute( attr, "height", height );

        util.addAttribute( attr, "viewBox", getViewBoxString() );

        util.addAttribute( attr, "preserveAspectRatio", "xMinYMin splice" );

        util.startElement( "svg" , attr );
	}

	/**
	 * @return
	 */
	private String getViewBoxString() {
		StringBuffer sb = new StringBuffer();
		sb.append( viewBoxX1 ).append(" ").append( viewBoxY1);
		sb.append( viewBoxWidth ).append(" ").append( viewBoxHeight);
		return null;
	}

	
}
