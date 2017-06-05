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
public class WaveformArea {

	/** Type identifier for calibration pulse area. */ 
	public static final int TYPE_CAL_PULSE = 1;
	/** Type identifier for waveform area. */
	public static final int TYPE_WAVEFORM = 2;
	
	private int type;
	private float leftX;
	private float topY;
	private float width;
	private float height;
	private int waveformIndex = -1;
	private String waveformDescription = null;
	private WaveformScalingInfo scalingInfo;
	
	private WaveformArea( int type, float x, float y, float w, float h ) {
		this.type = type;
		leftX = x;
		topY = y;
		width = w;
		height = h;
	}
	private WaveformArea( int type, float x, float y, float w, float h, int idx, String desc, WaveformScalingInfo scale ) {
		this( type, x, y, w, h );
		waveformIndex = idx;
		waveformDescription = desc;
		scalingInfo = scale;
	}

	public static WaveformArea getCalPulseArea( float x, float y, float w, float h ){
		return new WaveformArea( TYPE_CAL_PULSE, x, y, w, h );
	}

	public static WaveformArea getWaveformArea( float x, float y, float w, float h, int idx, String desc, WaveformScalingInfo scale ){
		return new WaveformArea( TYPE_WAVEFORM, x, y, w, h, idx, desc, scale );
	}
	
	/**
	 * Returns the type of this area.
	 * 
	 * @return The type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the left position of this area.
	 * 
	 * @return X-coord of the upper-left corner in mm.
	 */
	public float getLeftX() {
		return leftX;
	}

	/**
	 * Returns the top position of this area.
	 * 
	 * @return The Y coord of the upper-left corner in mm.
	 */
	public float getTopY() {
		return topY;
	}
	/**
	 * Returns the heigth of this area.
	 * 
	 * @return height in mm.
	 */
	public float getHeight() {
		return height;
	}
	/**
	 * Returns the width of this area.
	 * 
	 * @return width in mm.
	 */
	public float getWidth() {
		return width;
	}
	/**
	 * Returns a description of the waveform for this area.
	 * 
	 * @return Returns the waveformDescription.
	 */
	public String getWaveformDescription() {
		return waveformDescription;
	}
	/**
	 * Returns the channel index of the Waveform for this area.
	 * 
	 * @return Returns the waveformIndex or -1 if type is not TYPE_WAVEFORM.
	 */
	public int getWaveformIndex() {
		return waveformIndex;
	}
	/**
	 * @return Returns the scalingInfo.
	 */
	public WaveformScalingInfo getScalingInfo() {
		return scalingInfo;
	}
}
