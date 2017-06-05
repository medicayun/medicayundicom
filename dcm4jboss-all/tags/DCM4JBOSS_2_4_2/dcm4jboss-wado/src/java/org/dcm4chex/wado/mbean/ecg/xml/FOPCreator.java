/*
 * Created on 16.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg.xml;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.mbean.ECGSupport;
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
public class FOPCreator implements XMLResponseObject{

	private static final NumberFormat cmFormatter = new DecimalFormat("##.##cm", new DecimalFormatSymbols( new Locale( "en", "us")));
	private static Logger log = Logger.getLogger( ECGSupport.class.getName() );
	
	private TransformerHandler th;
	private XMLUtil util;

	private Float pageHeight;
	private Float pageWidth;
	private Float graphHeight;
	private Float graphWidth;
	private WaveformGroup[] waveformGroups;
	private WaveformInfo info;

	/**
	 * @param wfgrps
	 * @param wfInfo
	 * @param float1
	 * @param float2
	 */
	public FOPCreator(WaveformGroup[] wfgrps, WaveformInfo wfInfo, Float width, Float height) {
		waveformGroups = wfgrps;
		info = wfInfo;
		pageWidth = width;
		pageHeight = height;
		graphHeight = new Float( pageHeight.floatValue() - 4.0f );
		graphWidth = new Float( pageWidth.floatValue() - 2.0f );
		log.info("page (h*w):"+pageHeight+"*"+pageWidth);
		log.info("graph (h*w):"+graphHeight+"*"+graphWidth);
	}

	public void toXML( OutputStream out ) throws TransformerConfigurationException, SAXException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

		th = tf.newTransformerHandler();
		th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		th.setResult( new StreamResult(out) );
		th.startDocument();

		toXML();
		
		th.endDocument();
	    try {
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void embedXML( TransformerHandler th ) throws TransformerConfigurationException, SAXException {
		this.th = th;
		toXML();
	}
	
	
	private void toXML() throws SAXException, TransformerConfigurationException {
		util = new XMLUtil( th );
		util.startElement( "fo:root", "xmlns:fo", "http://www.w3.org/1999/XSL/Format" );
		{
			addLayoutMasterSet();
			util.startElement( "fo:page-sequence", "master-reference", "page" );
			{
				addHeader();
				util.startElement( "fo:flow", "flow-name", "xsl-region-body");
				int len = waveformGroups.length;
				for ( int i = 0 ; i < len ; i++ ) {
					addGraphicHeader( waveformGroups[i] );
					addGraphic( waveformGroups[i] );
					addGraphicFooter( waveformGroups[i] );
				}
				addFooter();
				util.endElement( "fo:flow" );
			}
			util.endElement( "fo:page-sequence" );
			//addTestPage();
		}
		util.endElement( "fo:root" );
	}

	/**
	 * @throws SAXException
	 * 
	 */
	private void addTestPage() throws SAXException {
		util.startElement( "fo:page-sequence", "master-reference", "test" );
		util.startElement( "fo:flow", "flow-name", "xsl-region-body");
		util.startElement( "fo:block", XMLUtil.EMPTY_ATTRIBUTES );
		util.startElement( "fo:instream-foreign-object", "xmlns:svg", "http://www.w3.org/2000/svg" );
		SVGCreator creator = new SVGCreator( null, null, graphWidth, graphHeight  );
			creator.setXMLUtil( new XMLUtil( th, "svg" ) );
			creator.addSVGStart();
			creator.addDefs();
			creator.addGrid(pageWidth.intValue(), pageHeight.intValue());
			util.endElement("svg:svg");
		util.endElement( "fo:instream-foreign-object" );
	util.endElement( "fo:block" );
		util.endElement( "fo:flow" );
		util.endElement( "fo:page-sequence" );
	}

	/**
	 * @throws SAXException
	 * 
	 */
	private void addLayoutMasterSet() throws SAXException {
		util.startElement( "fo:layout-master-set", XMLUtil.EMPTY_ATTRIBUTES );
		{
			AttributesImpl attr = util.newAttribute( "master-name", "page");
			util.addAttribute( attr, "page-height", cmFormatter.format( pageHeight ) );
			util.addAttribute( attr, "page-width", cmFormatter.format( pageWidth ) );
			util.addAttribute( attr, "margin-left", "5mm" );
			util.addAttribute( attr, "margin-right", "5mm" );
			util.addAttribute( attr, "margin-top", "10mm" );
			util.addAttribute( attr, "margin-bottom", "0mm" );
			util.startElement( "fo:simple-page-master", attr );
			{
				util.startElement( "fo:region-before", "extent", "1cm" );
				util.endElement( "fo:region-before" );
				util.startElement( "fo:region-body", "margin-top", "1cm" );
				util.endElement( "fo:region-body" );
				util.startElement( "fo:region-after", "extent", "0.5cm" );
				util.endElement( "fo:region-after" );
			}
			util.endElement( "fo:simple-page-master" );
			AttributesImpl attr2 = util.newAttribute( "master-name", "test");
			util.addAttribute( attr2, "page-height", cmFormatter.format( pageHeight ) );
			util.addAttribute( attr2, "page-width", cmFormatter.format( pageWidth ) );
			util.addAttribute( attr2, "margin-left", "0mm" );
			util.addAttribute( attr2, "margin-right", "0mm" );
			util.addAttribute( attr2, "margin-top", "0mm" );
			util.addAttribute( attr2, "margin-bottom", "0mm" );
			util.startElement( "fo:simple-page-master", attr2 );
			{
				util.startElement( "fo:region-before", "extent", "0cm" );
				util.endElement( "fo:region-before" );
				util.startElement( "fo:region-body", "margin-top", "0cm" );
				util.endElement( "fo:region-body" );
				util.startElement( "fo:region-after", "extent", "0cm" );
				util.endElement( "fo:region-after" );
			}
			util.endElement( "fo:simple-page-master" );
		}
		util.endElement( "fo:layout-master-set" );
	}
	
	private void addHeader() throws SAXException {
		util.startElement( "fo:static-content", "flow-name", "xsl-region-before");
		{
			AttributesImpl attr = util.newAttribute( "font-size", "12pt");
			util.addAttribute( attr, "text-align", "center" );
			util.addAttribute( attr, "font-weight", "bold" );
			util.startElement( "fo:block", attr);
				util.addValue( info.getPatientName() );
			util.endElement( "fo:block");
		}
		util.endElement( "fo:static-content" );
	}
	/**
	 * @param group
	 * @throws SAXException
	 * 
	 */
	private void addGraphicHeader(WaveformGroup group) throws SAXException {
		AttributesImpl attr = util.newAttribute( "font-size", "10pt");
		util.startElement( "fo:block", attr);
		addTable( "none",
				  new String[]{"15mm","40mm","20mm","40mm","15mm","25mm","50mm","20mm","3mm","20mm"},
				  new String[]{"left","left","left","left","left","left","left","right","left","left"},
				  new String[][]{ {"2",null,info.getAcquisDate(),info.getAcquisTime(),null,null,null,"Department:",null,"" },
								  {null,null,null,null,null,null,null,"Room:",null,"" },
				  				  {null,null,info.getBirthday(),info.getSex(),"0/0",info.getPatientSize(),info.getPatientWeight(),"Operator:",null,"" }
								}
				);
		util.endElement( "fo:block");
	}

	/**
	 * @param group
	 * @param out
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 * 
	 */
	private void addGraphic(WaveformGroup group) throws TransformerConfigurationException, SAXException {
		util.startElement( "fo:block", XMLUtil.EMPTY_ATTRIBUTES );
			util.startElement( "fo:instream-foreign-object", "xmlns:svg", "http://www.w3.org/2000/svg" );
			SVGCreator creator = new SVGCreator( group, null, graphWidth, graphHeight  );
				creator.embedXML( th );
			util.endElement( "fo:instream-foreign-object" );
		util.endElement( "fo:block" );
	}

	/**
	 * @throws SAXException
	 * 
	 */
	private void addGraphicFooter( WaveformGroup group ) throws SAXException {
		AttributesImpl attr = util.newAttribute( "font-size", "10pt");
		util.addAttribute( attr, "text-align", "center" );
		util.startElement( "fo:block", attr);
		{
			util.singleElement("fo:inline", null, "25mm/sec" );
			util.singleElement("fo:inline", null, "10mm/mV" );
			util.singleElement("fo:inline", null, group.getFilterText() );
		}
		util.endElement( "fo:block");
	}
	
	private void addTable( String border, String[] columnWidths, String[] textAligns, String[][] data) throws SAXException {
		AttributesImpl tblAttr = util.newAttribute( "border-style", border);
		util.addAttribute( tblAttr, "table-layout", "fixed" );
		util.startElement( "fo:table", tblAttr);
		AttributesImpl colAttr = util.newAttribute( "column-number", border);
		for ( int i = 0 ; i < columnWidths.length ; i++ ) {
			util.singleElement("fo:table-column", util.newAttribute( "column-width", columnWidths[i]), null );
		}
		util.startElement( "fo:table-body", null);
		int rows = data.length;
		int cols = data[0].length;
		AttributesImpl cellAttr = util.newAttribute( "border-style", "none");
		for ( int row = 0 ; row < rows ; row++ ) {
			util.startElement( "fo:table-row", null);
			for ( int col = 0 ; col < cols ; col++ ) {
				util.startElement( "fo:table-cell", cellAttr );
				util.singleElement("fo:block", util.newAttribute( "text-align", textAligns[col]), data[row][col] );
				util.endElement( "fo:table-cell");
			}
			util.endElement( "fo:table-row");
		}
		util.endElement( "fo:table-body");
		util.endElement( "fo:table");
	}

	/**
	 * 
	 */
	private void addFooter() {
		// TODO Auto-generated method stub
		
	}


}
