package org.dcm4chex.arr.ejb.session;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * @author joseph foraci
 *
 * This is the interface for a Audit Record Repository service that may be used
 * to parse a IHE audit message.
 */
public interface ArrMsgParser {
    public static final int INVALID_XML = 1;        //not well-formed xml data (or complete garbage)
    public static final int INVALID_SCHEMA = 2;     //xml data is well-formed, but data does not validate
    public static final int INVALID_INCOMPLETE = 4; //could not interpret some/all information such as Hostname, TimeStamp, or Type
    
	int parse(File file)
		throws ArrInputException;
    
	int parse(String xmlData)
		throws ArrInputException;
    
	int parse(InputStream is)
		throws ArrInputException;
    
	/**
	 * Get the audit type from parsed data (ie the name of the audit event
	 * element that was generated for the triggering event).
	 *
	 * @return A <code>String</code> representing the audit type or
	 *   <code>null</code> if it was not included.
	 */
	String getType();
    
	/** Get parsed Hostname from Host element
	 *
	 * @return A <code>String</code> representing the host mentioned or
	 *   <code>null</code> if it was not included.
	 */
	String getHost();
    
	/** Get parsed TimeStamp from Timestamp element
	 *
	 * @return A <code>Date</code> representing the time stamp mentioned or
	 *   <code>null</code> if it was not included.
	 */
	Date getTimeStamp();
    
    String getAet();
    
    String getUserName();
    
    String getPatientName();
    
    String getPatientId();
}
