package org.dcm4chee.xero.util;

/** This class has some simple methods to define timings */
public class Timing {

	public static String nanoDurToString(long dur) {
		return (dur/1e6)+" ms";
	}
	public static String nanoTimeToString(long lastTime) {
		return nanoDurToString(System.nanoTime()-lastTime);
	}
}
