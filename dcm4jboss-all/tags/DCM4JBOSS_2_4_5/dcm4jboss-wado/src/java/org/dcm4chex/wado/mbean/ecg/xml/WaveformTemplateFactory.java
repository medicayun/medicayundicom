/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg.xml;

import org.dcm4chex.wado.mbean.ecg.WaveformGroup;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveformTemplateFactory {

	private float width;
	private float height;

	public WaveformTemplateFactory( float w, float h ) {
		width = w;
		height = h;
	}
	
	public static WaveformTemplate getInstance( WaveformGroup wfGrp, float w, float h ) {
		int nrOfChannels = wfGrp.getNrOfChannels();
		if ( nrOfChannels == 12 ) {
			return new TwelveLeadTemplate( w, h );
		}
		if ( nrOfChannels < 12 ) {
			return new WaveformFlowTemplate( w, h, nrOfChannels, false );
		}
		return null;
	}
}
