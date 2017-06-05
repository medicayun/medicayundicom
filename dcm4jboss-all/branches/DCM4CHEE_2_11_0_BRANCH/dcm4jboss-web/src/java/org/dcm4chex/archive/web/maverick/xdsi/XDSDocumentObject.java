package org.dcm4chex.archive.web.maverick.xdsi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.Slot;

import org.apache.log4j.Logger;

public class XDSDocumentObject implements XDSRegistryObject {
    private ExtrinsicObject eo;
    String uri, url, creationTime;

    private static Logger log = Logger.getLogger( XDSDocumentObject.class.getName() );
    public XDSDocumentObject( ExtrinsicObject eo ) throws JAXRException {
        this.eo = eo;
        init();
    }
    
    private void init() throws JAXRException {
       	uri = getLongURI();
        try {
            url = URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("URL encoding of URI failed! UTF-8 encoding not supported! Use URI unencoded!");
            url = uri;
        }
        creationTime = getSlotValue("creationTime", null);
    }

    /**
	 * @return
     * @throws JAXRException
	 */
	private String getLongURI() throws JAXRException {
		Collection c = getSlotValues("URI");
		String s=null;
		if (c.size() == 1 ) {
			s = (String) c.iterator().next();
			if (Character.isDigit(s.charAt(0)) ) {
				s = s.substring(2);
			}
			return s;
		}
		String[] sa = new String[c.size()];
		StringBuffer sb = new StringBuffer();
		try {
			for ( Iterator iter = c.iterator() ; iter .hasNext() ; ) {
				s = (String) iter.next();
				sa[(int) s.charAt(0)-0x31] = s.substring(2);
			}
			for ( int i = 0 ; i < sa.length ; i++) {
				sb.append(sa[i]);
			}
		} catch ( Exception x) {
			throw new IllegalArgumentException("LONG URI contains Invalid Value:'"+s+"' all values:"+c);
		}
		return sb.toString();
	}

	public String getId() throws JAXRException {
        return eo.getKey().getId();
    }
    /**
     * Get Document Title (name of ExtrinsicObject)
     * 
     * @return
     * @throws JAXRException
     */
    public String getName() throws JAXRException {
        return eo.getName().getValue();
    }
    
    /**
     * Get MimeType of this document.
     * 
     * @return
     * @throws JAXRException
     */
    public String getMimeType() throws JAXRException {
        return eo.getMimeType();
    }
    
    public String getCreationTime() {
        return creationTime;
    }
    /**
     * Get URI.
     * @return
     * @throws JAXRException
     */
    public String getURI() throws JAXRException {
        return uri;
    }
    
    /**
     * Get the URL encoded String of URI.
     * @return
     * @throws JAXRException
     */
    public String getURL() throws JAXRException {
        return url;
    }
    
    /**
     * Get status of document as int.
     * 
     * @return
     * @throws JAXRException
     */
    public int getStatus() throws JAXRException {
        return eo.getStatus();
    }
    
    /**
     * Get status of document as String.
     * 
     * @return
     * @throws JAXRException
     */
    public String getStatusAsString() throws JAXRException {
        return XDSStatus.getStatusAsString(eo.getStatus());
    }
    
    public String getAuthorInstitution() throws JAXRException {
        return getSlotValue("authorInstitution", null);
    }

    public String getAuthorPerson() throws JAXRException {
        return getSlotValue("authorPerson", null);
    }
    public String getAuthorRole() throws JAXRException {
        return getSlotValue("authorRole", null);
    }

    public String getClassCode() throws JAXRException {
        Collection col = eo.getClassifications();
        Classification cl;
        if ( col.size() > 0 ) {
            cl = (Classification) col.iterator().next();
        }
        return "not implemented yet";
    }
    
    private String getSlotValue(String name, String def) throws JAXRException {
        Collection col;
        Slot sl = eo.getSlot(name);
        if ( sl != null ) {
            col = sl.getValues();
            if ( col != null && !col.isEmpty() ) {
                return (String) col.iterator().next();
            }
        }
        return def;
    }
    private Collection getSlotValues(String name) throws JAXRException {
        Collection col;
        Slot sl = eo.getSlot(name);
        return sl == null ? null : sl.getValues();
    }
    
}
