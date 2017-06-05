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
public class TwelveLeadTemplate implements WaveformTemplate {

	public static final float MIN_WIDTH = 1000;
	public static final float MIN_HEIGHT = 1000;
	public static final float CAL_PULSE_WIDTH = 120;
	
	private float width;
	private float height;
	private WaveformArea[] calPulseAreas = new WaveformArea[6];
	private WaveformArea[] wfAreas = new WaveformArea[12];

	public TwelveLeadTemplate( float w, float h ) {
		if ( w < MIN_WIDTH | h < MIN_HEIGHT ) {
			throw new IllegalArgumentException("Template size too small ( "+w+" x "+h+" )! Min size: "+MIN_WIDTH+" x "+ MIN_HEIGHT );
		}
		width = w;
		height = h;
		initAreas();
	}
	
	private void initAreas() {
		float lineHeight = height / 6f;
		float wfWidth = ( width - CAL_PULSE_WIDTH ) / 2f;
		float rightWaveformX = CAL_PULSE_WIDTH + wfWidth;
		float topY = 0;
		for ( int i = 0 ; i < 6 ; i++, topY += lineHeight ) {
			calPulseAreas[i] = WaveformArea.getCalPulseArea( 0, topY, CAL_PULSE_WIDTH, lineHeight );
			wfAreas[i] = WaveformArea.getWaveformArea( CAL_PULSE_WIDTH, topY, wfWidth, lineHeight, i, null );
			wfAreas[i+6] = WaveformArea.getWaveformArea( rightWaveformX, topY, wfWidth, lineHeight, i+6, null );
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
		WaveformArea[] all = new WaveformArea[18];
		System.arraycopy( calPulseAreas, 0, all, 0, 6 );
		System.arraycopy( wfAreas, 0, all, 6, 12 );
		return all;
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


}
