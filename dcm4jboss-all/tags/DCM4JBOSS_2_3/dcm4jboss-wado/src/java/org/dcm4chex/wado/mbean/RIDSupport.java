/*
 * Created on 12.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Driver;
import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.srom.SRDocumentFactory;
import org.dcm4che.util.ISO8601DateFormat;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.RIDResponseObject;
import org.dcm4chex.wado.mbean.WADOSupport.NeedRedirectionException;
import org.dcm4chex.wado.mbean.WADOSupport.NoImageException;
import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.dcm4chex.wado.mbean.xml.IHEDocumentList;
import org.jboss.mx.util.MBeanServerLocator;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDSupport {
	private static final String SUMMARY = "SUMMARY";
	private static final String SUMMARY_RADIOLOGY = "SUMMARY-RADIOLOGY";
	private static final String SUMMARY_CARDIOLOGY = "SUMMARY-CARDIOLOGY";
	private static final String SUMMARY_CARDIOLOGY_ECG = "SUMMARY-CARDIOLOGY-ECG";
	
	private static final String CONTENT_TYPE_XHTML = "text/xhtml";
	public static final String CONTENT_TYPE_XML = "text/xml";
	public static final String CONTENT_TYPE_HTML = "text/html";
	public static final String CONTENT_TYPE_JPEG = "image/jpeg";
	public static final String CONTENT_TYPE_PDF = "application/pdf";
	
	private static Logger log = Logger.getLogger( RIDService.class.getName() );
    private static final DcmObjectFactory factory = DcmObjectFactory.getInstance();
    private static final SRDocumentFactory srFact = SRDocumentFactory.getInstance();

    private static final String FOBSR_XSL_URI = "resource:xsl/fobsr.xsl";
    private final Driver fop = new Driver();

    private static MBeanServer server;
	private Set ecgSopCuids;
	private String ridSummaryXsl;
	private static List allConceptNameCodes;
	private static List cardiologyConceptNameCodes;
	private static List radiologyConceptNameCodes;
	private boolean useXSLInstruction;
	private static ObjectName fileSystemMgtName;
	
	private ECGSupport ecgSupport = null;
	
	private Dataset patientDS = null;
	private WADOSupport wadoSupport;

	public RIDSupport( MBeanServer mbServer ) {
		if ( server != null ) {
			server = mbServer;
		} else {
			server = MBeanServerLocator.locate();
		}
	}
	
	protected WADOSupport getWADOSupport() {
		if ( wadoSupport == null ) {
			wadoSupport = new WADOSupport(RIDSupport.server);
		}
		return wadoSupport;
	}
	
	private ECGSupport getECGSupport() {
		if ( ecgSupport == null )
			ecgSupport = new ECGSupport( this );
		return ecgSupport;
	}

	/**
	 * @return Returns the sopCuids.
	 */
	public Set getECGSopCuids() {
		if ( ecgSopCuids == null ) setDefaultECGSopCuids();
		return ecgSopCuids;
	}
	/**
	 * @param sopCuids The sopCuids to set.
	 */
	public void setECGSopCuids(Set sopCuids) {
		if ( sopCuids != null && ! sopCuids.isEmpty() )
			ecgSopCuids = sopCuids;
		else {
			setDefaultECGSopCuids();
		}
	}
	
	/**
	 * 
	 */
	private void setDefaultECGSopCuids() {
		ecgSopCuids = new HashSet();
		ecgSopCuids.add( UIDs.TwelveLeadECGWaveformStorage );
		ecgSopCuids.add( UIDs.GeneralECGWaveformStorage );
		ecgSopCuids.add( UIDs.AmbulatoryECGWaveformStorage );
		ecgSopCuids.add( UIDs.HemodynamicWaveformStorage );
		ecgSopCuids.add( UIDs.CardiacElectrophysiologyWaveformStorage );
		ecgSopCuids.add( UIDs.BasicVoiceAudioWaveformStorage );
	}

	private static List getCardiologyConceptNameCodes() {
		if ( cardiologyConceptNameCodes == null ) {
			cardiologyConceptNameCodes = new ArrayList();
			cardiologyConceptNameCodes.add( createCodeDS( "18745-0" ) );//Cardiac Catheteization Report
			cardiologyConceptNameCodes.add( createCodeDS( "11522-0" ) );//Echocardiography Report
		}
		return cardiologyConceptNameCodes;
	}
	private static List getRadiologyConceptNameCodes() {
		if ( radiologyConceptNameCodes == null ) {
			radiologyConceptNameCodes = new ArrayList();
			radiologyConceptNameCodes.add( createCodeDS( "11540-2" ) );//CT Abdomen Report
			radiologyConceptNameCodes.add( createCodeDS( "11538-6" ) );//CT Chest Report
			radiologyConceptNameCodes.add( createCodeDS( "11539-4" ) );//CT Head Report
			radiologyConceptNameCodes.add( createCodeDS( "18747-6" ) );//CT Report
			radiologyConceptNameCodes.add( createCodeDS( "18748-4" ) );//Diagnostic Imaging Report
			radiologyConceptNameCodes.add( createCodeDS( "18760-9" ) );//Ultrasound Report
			radiologyConceptNameCodes.add( createCodeDS( "11541-0" ) );//MRI Head Report
			radiologyConceptNameCodes.add( createCodeDS( "18755-9" ) );//MRI Report
			radiologyConceptNameCodes.add( createCodeDS( "18756-7" ) );//MRI Spine Report
			radiologyConceptNameCodes.add( createCodeDS( "18757-5" ) );//Nuclear Medicine Report
			radiologyConceptNameCodes.add( createCodeDS( "11525-3" ) );//Ultrasound Obstetric and Gyn Report
			radiologyConceptNameCodes.add( createCodeDS( "18758-3" ) );//PET Scan Report
			radiologyConceptNameCodes.add( createCodeDS( "11528-7" ) );//Radiology Report
		}
		return radiologyConceptNameCodes;
	}
	
	private static List getAllConceptNameCodes() {
		if ( allConceptNameCodes == null ) {
			allConceptNameCodes = new ArrayList();
			allConceptNameCodes.addAll( getRadiologyConceptNameCodes() );
			allConceptNameCodes.addAll( getCardiologyConceptNameCodes() );
		}
		return allConceptNameCodes;
	}
	
	private static Dataset createCodeDS( String value ) {
		Dataset ds = factory.newDataset();
        ds.putSH(Tags.CodeValue, value);
        ds.putSH(Tags.CodingSchemeDesignator, "LN");
        return ds;
	}
	/**
	 * @return Returns the ridSummaryXsl.
	 */
	public String getRIDSummaryXsl() {
		return ridSummaryXsl;
	}
	/**
	 * @param ridSummaryXsl The ridSummaryXsl to set.
	 */
	public void setRIDSummaryXsl(String ridSummaryXsl) {
		this.ridSummaryXsl = ridSummaryXsl;
	}
	/**
	 * @return Returns the useXSLInstruction.
	 */
	public boolean isUseXSLInstruction() {
		return useXSLInstruction;
	}
	/**
	 * @param useXSLInstruction The useXSLInstruction to set.
	 */
	public void setUseXSLInstruction(boolean useXSLInstruction) {
		this.useXSLInstruction = useXSLInstruction;
	}
	
	/**
	 * Set the name of the FileSystemMgtBean.
	 * <p>
	 * This bean is used to retrieve the DICOM object.
	 * 
	 * @param fileSystemMgtName The fileSystemMgtName to set.
	 */
	public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
		RIDSupport.fileSystemMgtName = fileSystemMgtName;
	}

	/**
	 * Get the name of the FileSystemMgtBean.
	 * <p>
	 * This bean is used to retrieve the DICOM object.
	 * 
	 * @return Returns the fileSystemMgtName.
	 */
	public ObjectName getFileSystemMgtName() {
		return fileSystemMgtName;
	}
	
	/**
	 * @param reqObj
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 */
	public RIDResponseObject getRIDSummary(RIDRequestObject reqObj) throws SQLException, IOException, TransformerConfigurationException, SAXException {
		String contentType = checkContentType( reqObj, new String[]{CONTENT_TYPE_HTML,CONTENT_TYPE_XML } );
		if ( contentType == null ) {
			return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_ACCEPTABLE, "Client doesnt support text/xml, text/html or text/xhtml !");
		}
		Dataset queryDS;
		String reqType = reqObj.getRequestType();
		if (log.isDebugEnabled() ) log.debug(" Summary request type:"+reqObj.getRequestType());
		if ( SUMMARY_CARDIOLOGY_ECG.equals( reqType ) ) {
			return getECGSummary( reqObj );
		} else {
			queryDS = getRadiologyQueryDS( reqObj );
			if ( queryDS == null )
				return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Patient with patientID="+reqObj.getParam("patientID")+ " not found!");
			

		}
	    IHEDocumentList docList= new IHEDocumentList();
	    initDocList( docList, reqObj, queryDS );
	    List conceptNames = null;
	    if ( reqType.equals( SUMMARY ) ) {
	    	conceptNames = getAllConceptNameCodes();
	    } else if ( reqType.equals( SUMMARY_CARDIOLOGY) ) {
	    	conceptNames = getCardiologyConceptNameCodes();
	    } else {
	    	conceptNames = getRadiologyConceptNameCodes();
	    }
		for ( Iterator iter = conceptNames.iterator() ; iter.hasNext() ; ) {
        	queryDS.remove( Tags.ConceptNameCodeSeq );//remove for next search.
        	DcmElement cnSq = queryDS.putSQ(Tags.ConceptNameCodeSeq);
        	cnSq.addItem( (Dataset) iter.next() );
        	fillDocList( docList, queryDS );
		}
		if ( docList.size() < 1 ) {
			log.info("No documents found: patientDS:"+patientDS);
			if ( patientDS != null ) {
				log.info("patientDS last:"+patientDS.getString( Tags.PatientName));
	        	PersonName pn = patientDS.getPersonName(Tags.PatientName );
	        	if ( pn != null ) {
		        	log.info("family:"+ pn.get( PersonName.FAMILY ));
		        	log.info("givenName:"+ pn.get( PersonName.GIVEN ));
	        	}
				
				docList.setQueryDS( patientDS );
			}
		}
		if ( ! contentType.equals(CONTENT_TYPE_XML) ) { // transform to (x)html only if client supports (x)html.
			docList.setXslt( ridSummaryXsl );
		}
		if ( useXSLInstruction ) docList.setXslFile( ridSummaryXsl );
		log.info("ContentType:"+contentType);
		return new RIDTransformResponseObjectImpl(docList, contentType, HttpServletResponse.SC_OK, null);
	}
	
	/**
	 * Checks if one of the given content types are allowed.
	 * <p>
	 * 
	 * 
	 * @param reqObj	The RID request object.
	 * @param types		Array of content types that can be used.
	 * 
	 * @return The content type that is allowed by the request or null.
	 */
	protected String checkContentType(RIDRequestObject reqObj, String[] types) {
		List allowed = reqObj.getAllowedContentTypes();
		String s;
		if ( log.isDebugEnabled() ) log.debug(" check against:"+allowed);
		for ( int i = 0, len = types.length ; i < len ; i++ ) {
			s = types[i];
			if ( log.isDebugEnabled() ) log.debug(" check "+s+":"+allowed.contains( s )+" ,"+s.substring( 0, s.indexOf( "/") )+"/*: "+allowed.contains( s.substring( 0, s.indexOf( "/") )+"/*" ) );
			if ( allowed.contains( s ) || allowed.contains( s.substring( 0, s.indexOf( "/") )+"/*" ) ) {
				return s;
			}
		}
		if ( log.isDebugEnabled() ) log.debug(" check */*:"+allowed.contains("*/*") );
		if ( allowed.contains("*/*") ) {
			return types[0];
		}
		return null;
	}

	private RIDResponseObject getECGSummary( RIDRequestObject reqObj ) throws SQLException {
		Dataset queryDS = getECGQueryDS( reqObj );
		if ( queryDS == null )
			return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Patient with patientID="+reqObj.getParam("patientID")+ " not found!");
	    IHEDocumentList docList= new IHEDocumentList();
	    initDocList( docList, reqObj, queryDS );
	    queryDS.putUI( Tags.SOPClassUID, (String[]) getECGSopCuids().toArray( new String[0] ) );
    	fillDocList( docList, queryDS );//TODO Is it ok to put all SOP Class UIDs in one query?
		if ( useXSLInstruction ) docList.setXslFile( ridSummaryXsl );
		return new RIDTransformResponseObjectImpl(docList, CONTENT_TYPE_XML, HttpServletResponse.SC_OK, null);
	}
	
	private void initDocList( IHEDocumentList docList, RIDRequestObject reqObj, Dataset queryDS ) {
	    String reqURL = reqObj.getRequestURL();
	    docList.setReqURL(reqURL);
	    int pos = reqURL.indexOf('?');
	    if ( pos != -1 ) reqURL = reqURL.substring(0, pos);
	    docList.setDocRIDUrl( reqURL.substring( 0, reqURL.lastIndexOf("/") ) );//last path element should be the servlet name! 
	    docList.setQueryDS( queryDS );
	    String docCode = reqObj.getRequestType();
		docList.setDocCode( docCode );
		if ( SUMMARY.equalsIgnoreCase( docCode )) 
			docList.setDocDisplayName( "List of radiology and cardiology reports");
		else if (SUMMARY_RADIOLOGY.equals( docCode ) )
			docList.setDocDisplayName( "List of radiology reports");
		else if (SUMMARY_CARDIOLOGY.equals( docCode ) )
			docList.setDocDisplayName( "List of cardiology reports");
		else if (SUMMARY_CARDIOLOGY_ECG.equals( docCode ) )
			docList.setDocDisplayName( "List of ECG's");
		String mrr = reqObj.getParam("mostRecentResults");
		docList.setMostRecentResults( Integer.parseInt(mrr));
		String ldt = reqObj.getParam("lowerDateTime");
		if ( ldt != null ) {
			try {
				docList.setLowerDateTime( new ISO8601DateFormat().parse( ldt ) );
			} catch (ParseException e) {} //Is checked in request object!
		}
		String udt = reqObj.getParam("upperDateTime");
		if ( udt != null ) {
			try {
				docList.setUpperDateTime( new ISO8601DateFormat().parse( udt ) );
			} catch (ParseException e) {} //Is checked in request object!
		}
	}
	
	private void fillDocList( IHEDocumentList docList, Dataset queryDS ) throws SQLException {
		QueryCmd qCmd = QueryCmd.create( queryDS, false );
		try {
			qCmd.execute();
		    Dataset ds = factory.newDataset();
			while ( qCmd.next() ) {
				ds = qCmd.getDataset();
				Date date = ds.getDateTime( Tags.ContentDate, Tags.ContentTime );
				if ( checkDate( docList, date ) ) {
					if ( log.isDebugEnabled() ) {
						log.debug("Add to docList! ds:");
						log.debug(ds);
					}
					docList.add( ds );
				}
			}
		} catch ( SQLException x ) {
			qCmd.close();
			throw x;
		}
		qCmd.close();
	}


	/**
	 * @param docList
	 * @param date
	 * @return
	 */
	private boolean checkDate(IHEDocumentList docList, Date date) {
		Date ldt = docList.getLowerDateTime();
		Date udt = docList.getUpperDateTime();
		if ( ldt == null && udt == null ) return true;
		if ( ldt != null ) {
			if ( udt != null ) {
				return ( ldt.compareTo( date ) <= 0 ) && ( udt.compareTo( date ) >= 0 );
			} else {
				return ( ldt.compareTo( date ) <= 0 );
			}
		} else {
			return ( udt.compareTo( date ) >= 0 );
		}
	}

	/**
	 * @param reqObj
	 * @return
	 */
	private Dataset getRadiologyQueryDS(RIDRequestObject reqObj) {
		String patID = reqObj.getParam( "patientID" );
		String[] pat = splitPatID( patID );
		pat = checkPatient( pat );
		log.info("getRadiologyQueryDS: pat:"+pat);
		if ( pat == null ) return null;
		Dataset ds = factory.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
		ds.putLO(Tags.PatientID, pat[0]);
		if ( pat[1] != null ) { // Issuer of patientID is known. 
			ds.putLO(Tags.IssuerOfPatientID, pat[1]);
		}
		ds.putCS( Tags.Modality, "SR" );
		//Concept name sequence will be used as search criteria. -> within a loop over all radiology specific concept names.
		return ds;
	}


	/**
	 * @param pat
	 * @return
	 */
	private String[] checkPatient(String[] pat) {
		log.info("checkPatient:"+pat[0]+","+pat[1]);
		Dataset ds = factory.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, "PATIENT");
		ds.putLO(Tags.PatientID, pat[0]);
		Dataset ds1;
		QueryCmd qCmd = null;
		String issuer = null;
		boolean foundWithoutIssuer = false;
		try {
			qCmd = QueryCmd.create( ds, false );
			qCmd.execute();
			while ( qCmd.next() ) {
				ds1 = qCmd.getDataset();
				if ( pat[1] != null ) {
					issuer = ds1.getString( Tags.IssuerOfPatientID );
					if ( log.isDebugEnabled() ) log.debug("checkPatient: issuer:"+issuer);
					if ( pat[1].equals( issuer ) ) {
						patientDS = ds1;
						break;
					} else if ( issuer == null ) {
						patientDS = ds1;
						foundWithoutIssuer = true;
					}
				} else {
					if ( foundWithoutIssuer ) { //OK one more record -> result is not ambigous
						log.info("Request issuer unknown; More than one patient found with given ID!");
						qCmd.close();
						patientDS = null;
						return null;//Throw Exception instead?
					}
					foundWithoutIssuer = true; //to indicate one record is found
					patientDS = ds1;
				}
			}
			qCmd.close();
		} catch ( SQLException x ) {
			log.error( "Unexpected Error in isQueryDSMatching:"+x.getMessage(), x);
		}
		if ( qCmd != null ) qCmd.close();
		if ( log.isDebugEnabled() ) log.debug("checkPatient: result: issuer:"+issuer+" vs:"+pat[1]+" foundWithoutIssuer:"+foundWithoutIssuer);
		if ( pat[1] != null && pat[1].equals( issuer ) ) return pat; //OK fully match.
		if ( foundWithoutIssuer ) {
			pat[1] = null;
			return pat;
		}
		return null;
	}


	/**
	 * @param reqObj
	 * @return
	 */
	private Dataset getECGQueryDS(RIDRequestObject reqObj) {
		String patID = reqObj.getParam( "patientID" );
		String[] pat = splitPatID( patID );
		pat = checkPatient( pat );
		if ( log.isDebugEnabled() ) log.debug("getECGQueryDS: pat:"+pat);
		if ( pat == null ) return null;
		Dataset ds = factory.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
		ds.putLO(Tags.PatientID, pat[0]);
		if ( pat[1] != null ) { // Issuer of patientID is known. 
			ds.putLO(Tags.IssuerOfPatientID, pat[1]);
		}
		return ds;
	}

	/**
	 * @param reqVO
	 * @return
	 */
	public RIDResponseObject getRIDDocument(RIDRequestObject reqObj) {
		String uid = reqObj.getParam("documentUID");
		Dataset queryDS = factory.newDataset();
		queryDS.putCS(Tags.QueryRetrieveLevel, "IMAGE");
		queryDS.putUI(Tags.SOPInstanceUID, uid );
		QueryCmd cmd = null;
		try {
			cmd = QueryCmd.create( queryDS, false );
			cmd.execute();
			if ( cmd.next() ) {
				Dataset ds = cmd.getDataset();
				String cuid = ds.getString( Tags.SOPClassUID );
				if ( getECGSopCuids().contains( cuid ) ) {
					cmd.close();
					return getECGSupport().getECGDocument( reqObj, ds );
				} else {
					cmd.close();
					return getDocument( reqObj );
				}
			} else {
				cmd.close();
				return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Object with documentUID="+uid+ " not found!");
			}
		} catch (SQLException x) {
			log.error("Cant get RIDDocument:", x);
			if ( cmd != null ) cmd.close();
			return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cant get Document! Reason: unexpected error:"+x.getMessage() );
		}
	}
	
	private RIDResponseObject getDocument( RIDRequestObject reqObj ) {
		String docUID = reqObj.getParam("documentUID");
		if ( log.isDebugEnabled() ) log.debug(" Document UID:"+docUID);
		String contentType = reqObj.getParam("preferredContentType");
		if ( contentType == null ) {
			contentType = CONTENT_TYPE_PDF;
		} else {
			if ( contentType.equals( CONTENT_TYPE_JPEG )) {
				if ( this.checkContentType( reqObj, new String[]{ CONTENT_TYPE_JPEG } ) == null ) {
					return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_BAD_REQUEST, "Display actor doesnt accept preferred content type!");
				}
				RIDResponseObject resp = handleJPEG( reqObj );
				if ( resp != null ) return resp; 
				contentType = CONTENT_TYPE_PDF; //cant be rendered as image (SR) make PDF instead.
			} else if ( ! contentType.equals( CONTENT_TYPE_PDF) ) {
				return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_ACCEPTABLE, "preferredContentType '"+contentType+"' is not supported! Only 'application/pdf' and 'image/jpeg' are supported !");
			}
		}
		if ( this.checkContentType( reqObj, new String[]{ CONTENT_TYPE_PDF } ) == null ) {
			return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_BAD_REQUEST, "Display actor doesnt accept preferred content type!");
		}
		WADOCache cache = WADOCacheImpl.getRIDCache();
		File outFile = cache.getFileObject(null, null, reqObj.getParam("documentUID"), contentType );
		try {
			if ( !outFile.exists() ) {
				File inFile = getDICOMFile( docUID );
				if ( inFile == null ) {
					return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Object with documentUID="+docUID+ "not found!");
				}
				outFile = renderSRFile( inFile, outFile );
			}
			return new RIDStreamResponseObjectImpl( new FileInputStream( outFile ),contentType, HttpServletResponse.SC_OK, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param reqObj
	 * @return
	 */
	private RIDResponseObject handleJPEG(final RIDRequestObject reqObj) {
		File file;
		try {
			file = getWADOSupport().getJpg( "rid", "rid", reqObj.getParam("documentUID"), null, null, null );
			if ( file != null ) {
				return new RIDStreamResponseObjectImpl( new FileInputStream( file ), CONTENT_TYPE_JPEG, HttpServletResponse.SC_OK, null );
			} else {
				return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_NOT_FOUND, "Requested Document not found! documentID:"+reqObj.getParam("documentUID") );
			}
		} catch (NeedRedirectionException e) {
			return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_NOT_FOUND, "Requested Document is not on this Server! Try to get document from:"+e.getHostname() );
		} catch (NoImageException e) {
			return null;
		} catch (Exception e) {
			return new RIDStreamResponseObjectImpl( null, CONTENT_TYPE_JPEG, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error:"+e.getMessage() );
		}
	}

	/**
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws TransformerException
	 */
	private File renderSRFile(File inFile, File outFile ) throws IOException, TransformerException {
        outFile.getParentFile().mkdirs();
        File tmpFile = new File( outFile.toString()+".tmp");
        tmpFile.deleteOnExit();
        DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(inFile)));
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        OutputStream tmpOut = new FileOutputStream( tmpFile);
        th.setResult(new StreamResult( tmpOut ));
        DcmParser parser = DcmParserFactory.getInstance().newDcmParser(in);
        parser.setSAXHandler2(th,DictionaryFactory.getInstance().getDefaultTagDictionary(), null, 4000, null );//4000 ~ one text page.
        parser.parseDcmFile(null, -1);
        tmpOut.close();
        
        OutputStream out = new FileOutputStream( outFile );
        fop.setRenderer(Driver.RENDER_PDF);
        fop.setOutputStream( out );
        Templates template = tf.newTemplates(new StreamSource(FOBSR_XSL_URI));
        
        Transformer t = template.newTransformer();
        t.transform(new StreamSource( new FileInputStream(tmpFile)), new SAXResult( fop.getContentHandler() ) );
        out.close();
        tmpFile.delete();
        if ( ! outFile.exists() ) return null;
        return outFile;
	}

	/**
	 * Returns the DICOM file for given arguments.
	 * <p>
	 * Use the FileSystemMgtService MBean to localize the DICOM file.
	 * 
	 * @param instanceUID	Unique identifier of the instance.
	 * 
	 * @return The File object or null if not found.
	 * 
	 * @throws IOException
	 */
	private File getDICOMFile( String instanceUID ) throws IOException {
	    File file;
	    Object dicomObject = null;
		try {
	        dicomObject = server.invoke(fileSystemMgtName,
	                "locateInstance",
	                new Object[] { instanceUID },
	                new String[] { String.class.getName() } );
	        
	    } catch (Exception e) {
	        log.error("Failed to get DICOM file", e);
	    }
	    if ( dicomObject == null ) return null; //not found!
	    if ( dicomObject instanceof File ) return (File) dicomObject; //We have the File!
	    if ( dicomObject instanceof String ) {
	    	log.info("Requested DICOM file is not local! You can retrieve it from:"+dicomObject);
	    }
		return null;
	}
	
	/**
	 * @param patientID patient id in form patientID^^^issuerOfPatientID
	 * @return String[] 0..patientID 1.. issuerOfPatientID
	 */
	private String[] splitPatID(String patientID) {
		String[] pat = new String[]{null,null};
		String[] sa = StringUtils.split(patientID, '^');
		return new String[]{ sa[0], sa.length > 3 ? sa[3] : null };
	}

	
}
