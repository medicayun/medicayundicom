package org.dcm4chex.arr.ejb.session;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.util.ISO8601DateFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author joseph foraci
 *
 * Implementation for ArrMsgParser interface to parse certain fields of
 * interest from an IHE v5.4 audit message
 */
final class ArrMsgParserImpl extends DefaultHandler implements ArrMsgParser {
	private static final HashSet eventSet = new HashSet(Arrays.asList(new String[]
		{
			"Import","InstancesStored","ProcedureRecord","ActorStartStop","ActorConfig","Export",
			"DICOMInstancesDeleted","PatientRecord","OrderRecord","BeginStoringInstances",
			"InstancesSent","DICOMInstancesUsed","StudyDeleted","DicomQuery","SecurityAlert",
			"UserAuthenticated","AuditLogUsed","NetworkEntry"
		}));
    
	private static final String E_ROOT = "IHEYr4";
	private static final String E_TIMESTAMP = "TimeStamp";
	private static final String E_HOST = "Host";
    private static final String E_AET = "AET";
    private static final String E_USERNAME = "LocalUser";
    private static final String E_USERNAME2 = "LocalUsername";
    private static final String E_PATIENTNAME = "PatientName";
    private static final String E_PATIENTID = "PatientID";
    
	private final SAXParserFactory spf = SAXParserFactory.newInstance();
	private SAXParser parser = null;
    
    private int retcode;
    
	private String type = null;
	private String host = null;
	private Date timeStamp = null;
    private String aet = null;
    private String userName = null;
    private String patientName = null;
    private String patientId = null;
    
	private StringBuffer chrBuff = null;
	private String lastElem = null;
    
	//for xml schema, does not work with the crimson sax ref impl
	/*private static final String JAXP_SCHEMA_LANGUAGE =
	"http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA =
	"http://www.w3.org/2001/XMLSchema";*/

	public ArrMsgParserImpl(boolean validating)
		throws ArrInputException
	{
		try {
			spf.setNamespaceAware(true);
			spf.setValidating(validating);
			parser = spf.newSAXParser();
			//parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		}
		catch (SAXException se) {
			throw new ArrInputException("SAX parser exception", se);
		}
		catch (ParserConfigurationException pce) {
			throw new ArrInputException("SAX parser conf exception", pce);
		}
	}

	/**
	* @see org.dcm4che.arr.ArrMsgParser#parse(File)
	*/
	public int parse(File file)
		throws ArrInputException
	{
		try {
			return parse(new FileInputStream(file));
		}
		catch (FileNotFoundException fnf) {
			throw new ArrInputException("File not found", fnf);
		}
	}

	/**
	* @see org.dcm4che.arr.ArrMsgParser#parse(String)
	*/
	public int parse(String str)
		throws ArrInputException
	{
		return parse(new ByteArrayInputStream(str.getBytes()));
	}

	/**
	* Handles most of the parsing details.
	*
	* @see org.dcm4che.arr.ArrMsgParser#parse(InputStream)
	*/
	public int parse(InputStream is)
		throws ArrInputException
	{
        timeStamp = null;
        host = null;
        type = null;
        retcode = 0;
		try {
			parser.parse(is, this);
		}
		catch (IOException ioe) {
			throw new ArrInputException("I/O exception", ioe);
		}
		catch (SAXException se) {
			throw new ArrInputException("SAX parser exception", se);
		}
        finally {
            //check for unparsed elements and set bitfields appropriately
            if (type==null || host==null || timeStamp==null)
                retcode = retcode | ArrMsgParser.INVALID_INCOMPLETE;
        }
        return retcode;
	}

	public void startDocument()
		throws SAXException
	{
        //printCharBuff();
	}
	public void endDocument()
		throws SAXException
	{
        //printCharBuff();
	}

	public void startElement(String nsURI,
		String sname, String qname,
		Attributes attrs)
		throws SAXException
	{
        //printCharBuff();
		String el = sname;
		if ("".equals(el))
			el = qname;
		if (lastElem != null && lastElem.equals(E_ROOT) &&
			eventSet.contains(el))
			type = el;
		lastElem = el;
		/*System.out.print("<" + el);
		if (attrs != null) {
			for (int i=0; i<attrs.getLength(); i++) {
				System.out.print(" " + attrs.getQName(i) + "=\"" + attrs.getValue(i) + "\"");
			}
		}
		System.out.print(">");*/
	}
	public void endElement(String nsURI,
		String sname, String qname)
		throws SAXException
	{
        //printCharBuff();
        lastElem = null;
		String el = sname;
		if ("".equals(el))
			el = qname;
		//System.out.print("</" + el + ">");
	}

    public void characters(char[] buf, int offset, int len)
        throws SAXException
    {
    	String s = new String(buf, offset, len);
        if (lastElem != null) {
    		if (lastElem.equals(E_TIMESTAMP)) {
    			ISO8601DateFormat isoDateFmt = new ISO8601DateFormat();
    			isoDateFmt.setLenient(true); //should be default anyway
    			timeStamp = isoDateFmt.parse(s.trim(), new ParsePosition(0));
    		}
    		else if (lastElem.equals(E_HOST)) {
    			host = s.trim();
    		}
            else if (lastElem.equals(E_AET)) {
                aet = s.trim();
            }
            else if (lastElem.equals(E_USERNAME) || lastElem.equals(E_USERNAME2)) {
                userName = s.trim();
            }
            else if (lastElem.equals(E_PATIENTNAME)) {
                patientName = s.trim();
            }
            else if (lastElem.equals(E_PATIENTID)) {
                patientId = s.trim();
            }
        }
        /*if (chrBuff == null)
            chrBuff = new StringBuffer();
        chrBuff.append(s);*/
    }

    private void printCharBuff()
    {
        if (chrBuff != null)
            System.out.print(chrBuff);
        chrBuff = null;
    }

    /**
     * Treat errors as invalid schema
     */
    public void error(SAXParseException spe)
        throws SAXException
    {
        //System.out.println("\n<<"+e.toString()+">>");
        retcode = retcode | ArrMsgParser.INVALID_SCHEMA;
    }
    
    /**
     * Treat any fatal error as violation of well-formed contraint.
     * Throw SAX parser exception back up to SAXParser::parse() invoker
     */
    public void fatalError(SAXParseException spe)
        throws SAXException
    {
        retcode = retcode | ArrMsgParser.INVALID_XML;
        throw spe;
    }

    public String getType()
    {
    	return type;
    }

    public String getHost()
    {
    	return host;
    }

    public Date getTimeStamp()
    {
    	return timeStamp;
    }

    public String getAet()
    {
        return aet;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public String getPatientId()
    {
        return patientId;
    }
}
