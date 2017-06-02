package org.dcm4chee.xero.metadata.access;

import java.util.Map;

import org.dcm4chee.xero.metadata.access.MapFactory;

/** A simple factory that returns a fixed object */
public class TestFactory implements MapFactory<Object> {
	public static Object staticValue = new Object();
	
	public Object create(Map<String, Object> src) {
		return staticValue;
	}

}
