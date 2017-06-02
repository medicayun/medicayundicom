package org.dcm4chee.xds.common;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.helpers.ISO8601DateFormat;
import org.jboss.util.id.GUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDSPerformanceLogger {
	private final static Logger log = LoggerFactory.getLogger("Performance");	
	
	private static final String PERFORMANCE_LOG = "PerformanceLog"; 
	private static final String TUID = "TUID";
	private static final String SERVICE = "Service";
	private static final String EVENT_TYPE = "EventType";
	private static final String SUB_EVENT = "SubEvent";
	private static final String SUB_EVENTS = "SubEvents";
	private static final String START_TIMESTAMP = "StartTimestamp";
	private static final String END_TIMESTAMP = "EndTimestamp";
	private static final String TIME_TAKEN = "TimeTaken";
	private static final String LT = "<"; 	// less-than
	private static final String LTS = "</"; // less-than, slash
	private static final String GT = ">";	// greater-than
	
	private static DateFormat df = new ISO8601DateFormat();
	
	private String tuid;
	private String service;
	private String eventType;
	private StringBuilder eventProperties;
	private Date startTimestamp;
	private Date endTimestamp;
	
	private StringBuilder subEvents;
	private StringBuilder subEventProperties;
	private String subEventType;
	private Date subEventStartTimestamp;
	private Date subEventEndTimestamp;
	
	public XDSPerformanceLogger(String service, String eventType) {
		this.tuid = new GUID().toString(); 
		this.service = service;
		this.eventType = eventType;
		this.eventProperties = new StringBuilder();
		this.startTimestamp = new Date();
		this.subEvents = new StringBuilder();
	}
	
	public void flush() {
		this.endTimestamp = new Date();
		StringBuilder sb = new StringBuilder(LT + PERFORMANCE_LOG + GT);
		addNode(sb, TUID, tuid);
		addNode(sb, SERVICE, service);
		addNode(sb, EVENT_TYPE, eventType);
		addNode(sb, START_TIMESTAMP, df.format(startTimestamp));
		addNode(sb, END_TIMESTAMP, df.format(endTimestamp));
		addNode(sb, TIME_TAKEN, String.valueOf((endTimestamp.getTime() - startTimestamp.getTime())));
		sb.append(eventProperties.toString());
		addNode(sb, SUB_EVENTS, subEvents.toString());
		sb.append(LTS + PERFORMANCE_LOG + GT);
		log.info(sb.toString());
	}
	
	public void setEventProperty(String name, String value) {
		addNode(eventProperties, name, XmlEscapeUtil.escape(value));
	}

	public void startSubEvent(String subEventType) {
		this.subEventType = subEventType;
		this.subEventProperties = new StringBuilder();
		this.subEventStartTimestamp = new Date();
	}

	public void setSubEventProperty(String name, String value) {
		addNode(subEventProperties, name, XmlEscapeUtil.escape(value));
	}
	
	public void endSubEvent() {
		subEventEndTimestamp = new Date();
		subEvents.append(LT + SUB_EVENT + GT);
		addNode(subEvents, EVENT_TYPE, subEventType);
		addNode(subEvents, START_TIMESTAMP, df.format(subEventStartTimestamp));
		addNode(subEvents, END_TIMESTAMP, df.format(subEventEndTimestamp));
		addNode(subEvents, TIME_TAKEN, String.valueOf((subEventEndTimestamp.getTime() - subEventStartTimestamp.getTime())));
		subEvents.append(subEventProperties.toString());
		subEvents.append(LTS + SUB_EVENT + GT);
	}
	
	private void addNode(StringBuilder builder, String name, String value) {
		builder.append(LT + name + GT + value + LTS + name + GT); 
	}
	
	/**
	 * Utility for escaping XML character values.
	 */
	private static class XmlEscapeUtil {

        private static XmlEscapeUtil houdini = new XmlEscapeUtil();
        private Map<Integer, String> encodedValues = new HashMap<Integer, String>();
        
        /**
         * Escapes the characters in a {@code String} using XML entities. Supports the five
         * basic XML entities (gt, lt, quot, amp, apos). Unicode characters greater than
         * 0x7f (ASCII limit) are escaped to their numerical \\u equivalent.
         * @param str the {@code String} to escape, may be null
         * @return a new escaped {@code String}, {@code null} if null string input
         */
        public static String escape(String s) {
            if ( s == null ) return null;
            StringBuffer b = new StringBuffer(s.length());
            houdini.escape(b, s);
            return b.toString();
        }
        
        private XmlEscapeUtil() {
            // pumping a multi-dimensional array into a map allows us the flexibility of
            // pumping the same array into an inverse map if the need for an unescape method
            // comes up - and it doesn't take any extra effort to code it this way
            String[][] correspondingValues = {
                    {"quot", "34"},  // " - double-quote
                    {"amp", "38"},   // & - ampersand
                    {"lt", "60"},    // < - less-than
                    {"gt", "62"},    // > - greater-than
                    {"apos", "39"}}; // ' - apostrophe
                    
            for ( int i = 0; i < correspondingValues.length; i++ ) {
                encodedValues.put(Integer.parseInt(correspondingValues[i][1]), correspondingValues[i][0]);
            }
        }
        
        private void escape(StringBuffer buffer, String str) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                char c = str.charAt(i);
                String entityName = encodedValues.get((int) c);
                if ( entityName == null ) {
                    if (c > 0x7F) {
                        buffer.append("&#");
                        buffer.append(Integer.toString(c, 10));
                        buffer.append(';');
                    } else {
                        buffer.append(c);
                    }
                } else {
                    buffer.append('&');
                    buffer.append(entityName);
                    buffer.append(';');
                }
            }
        }
    }
}
