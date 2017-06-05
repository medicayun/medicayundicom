/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg.xml;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface WaveformTemplate {

	float getWidth();
	
	float getHeight();
	
	WaveformArea[] getAreas();
	
	WaveformArea[] getCalPulseAreas();
	
	WaveformArea[] getWaveformAreas();

	/**
	 * @return The text rendered as footer.
	 */
	String getFooterText();

}
