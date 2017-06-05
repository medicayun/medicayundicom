/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveFormChannel {

	private int chNr;
	private String chSource;
	private int bitsStored;
	private Integer minValue;
	private Integer maxValue;
	private String label;
	private String status;
	
	private float sensitivity;
	private String sensitivityUnit;
	
	private WaveFormBuffer buffer;
	private Float lowFreq;
	private Float highFreq;
	
	/**
	 * @param buffer
	 * @param item
	 */
	public WaveFormChannel(Dataset ch, WaveFormBuffer buffer) {
		chNr = ch.getInt( Tags.WaveformChannelNumber, -1);
		chSource = ch.get(Tags.ChannelSourceSeq).getItem().getString( Tags.CodeMeaning);
		bitsStored = ch.getInt( Tags.WaveformBitsStored, -1 );
		minValue = ch.getInteger( Tags.ChannelMinimumValue, -1 );
		maxValue = ch.getInteger( Tags.ChannelMaximumValue, -1 );
		label = ch.getString( Tags.ChannelLabel );
		status = ch.getString( Tags.ChannelStatus );
		sensitivity = ch.getFloat( Tags.ChannelSensitivity, -1f );
		sensitivityUnit = ch.get(Tags.ChannelSensitivityUnitsSeq).getItem().getString( Tags.CodeValue );
		lowFreq = ch.getFloat( Tags.FilterLowFrequency );
		highFreq = ch.getFloat( Tags.FilterHighFrequency );
		this.buffer = buffer;
	}
	
	/**
	 * @return Returns the chSource.
	 */
	public String getChSource() {
		return chSource;
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return
	 */
	public Float getLowFreq() {
		return lowFreq;
	}

	/**
	 * @return
	 */
	public Float getHighFreq() {
		return highFreq;
	}

	
	
	
	public int getRawValue() {
		return buffer.getValue();
	}
	
	public int getRawValue( int sampleNr ) {
		return buffer.getValue( sampleNr );
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WaveFormChannel: chNr").append(chNr).append(" label:").append(label);
		sb.append(" bitsStored:").append( bitsStored).append(" min:").append(minValue).append(" max:").append(maxValue);
		sb.append(" status:").append(status);
		return sb.toString();
	}

	/**
	 * @return
	 */
	public float getValue() {
		return getRawValue() * sensitivity;//TODO all the other things to get the real value!
	}

}
