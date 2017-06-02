/*
 * Created on 17.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4che.log;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author franz.willer
 *
 * A log4j filter to filter messages according to MDC values.
 */
public class MDCFilter extends Filter {

	private String key;
	private String value;
	private boolean acceptOnMatch = true;
	
	/**
	 * Checks if the value of MDC with <code>key</code> match with <code>value</code>.
	 * Returns NEUTRAL if the check fails. 
	 * Otherwise return ACCEPT if <code>acceptOnMatch is true</code> or DENY if false.
	 *  
	 * @see org.apache.log4j.spi.Filter#decide(org.apache.log4j.spi.LoggingEvent)
	 */
	public int decide(LoggingEvent e) {
		if ( value.equals( e.getMDC(key) ) ) {
			return acceptOnMatch ? Filter.ACCEPT : Filter.DENY;
		}
		return Filter.NEUTRAL; 
	}

	/**
	 * Determines if decide returns ACCEPT or DENY if matching.
	 *  
	 * @return Returns the acceptOnMatch.
	 */
	public boolean isAcceptOnMatch() {
		return acceptOnMatch;
	}
	
	/**
	 * @param acceptOnMatch The acceptOnMatch to set.
	 */
	public void setAcceptOnMatch(boolean acceptOnMatch) {
		this.acceptOnMatch = acceptOnMatch;
	}
	/**
	 * Returns the key value that is used to get value from MDC.
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * Returns the matching value.
	 * 
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
