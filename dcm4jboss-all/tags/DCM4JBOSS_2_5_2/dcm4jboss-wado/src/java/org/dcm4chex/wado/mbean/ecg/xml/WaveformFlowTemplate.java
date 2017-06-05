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
public class WaveformFlowTemplate implements WaveformTemplate {

	public static final float MIN_WIDTH = 1000;
	public static final float MIN_HEIGHT = 1000;
	public static final float CAL_PULSE_WIDTH = 120;
	
	private float width;
	private float height;
	private int nrOfChannels;
	private boolean useCalPulse;
	private WaveformArea[] calPulseAreas;
	private WaveformArea[] wfAreas;

	public WaveformFlowTemplate( float w, float h, int nrOfChannels, boolean useCalPulse ) {
		if ( w < MIN_WIDTH | h < MIN_HEIGHT ) {
			throw new IllegalArgumentException("Template size too small ( "+w+" x "+h+" )! Min size: "+MIN_WIDTH+" x "+ MIN_HEIGHT );
		}
		width = w;
		height = h;
		this.nrOfChannels = nrOfChannels;
		this.useCalPulse = useCalPulse;
		initAreas();
	}
	
	private void initAreas() {
		float lineHeight = height / nrOfChannels;
		float wfWidth = width;
		float wfX = 0;
		wfAreas = new WaveformArea[ nrOfChannels ];
		if ( useCalPulse ) {
			wfWidth -= CAL_PULSE_WIDTH;
			wfX += CAL_PULSE_WIDTH;
			calPulseAreas = new WaveformArea[ nrOfChannels ];
		}
		float topY = 0;
		for ( int i = 0 ; i < nrOfChannels ; i++, topY += lineHeight ) {
			if ( useCalPulse ) calPulseAreas[i] = WaveformArea.getCalPulseArea( 0, topY, CAL_PULSE_WIDTH, lineHeight );
			wfAreas[i] = WaveformArea.getWaveformArea( wfX, topY, wfWidth, lineHeight, i, null, null );
		}
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.mbean.ecg.xml.WaveformTemplate#getWidth()
	 */
	public float getWidth() {
		return width;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.mbean.ecg.xml.WaveformTemplate#getHeight()
	 */
	public float getHeight() {
		return height;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.mbean.ecg.xml.WaveformTemplate#getAreas()
	 */
	public WaveformArea[] getAreas() {
		if ( useCalPulse ) {
			WaveformArea[] all = new WaveformArea[ calPulseAreas.length << 1 ];
			System.arraycopy( calPulseAreas, 0, all, 0, calPulseAreas.length );
			System.arraycopy( wfAreas, 0, all, calPulseAreas.length, calPulseAreas.length << 1 );
			return all;
		}
		return wfAreas;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.mbean.ecg.xml.WaveformTemplate#getCalPulseAreas()
	 */
	public WaveformArea[] getCalPulseAreas() {
		return calPulseAreas;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.mbean.ecg.xml.WaveformTemplate#getWaveformAreas()
	 */
	public WaveformArea[] getWaveformAreas() {
		return wfAreas;
	}

	public String getFooterText() {
		return null;
	}

}
