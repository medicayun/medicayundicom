/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
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
	private float sensitivityCorrection;
	
	/**
	 * @param buffer
	 * @param item
	 */
	public WaveFormChannel(Dataset ch, WaveFormBuffer buffer, float fCorr) {
		chNr = ch.getInt( Tags.WaveformChannelNumber, -1);
		chSource = ch.get(Tags.ChannelSourceSeq).getItem().getString( Tags.CodeMeaning);
		bitsStored = ch.getInt( Tags.WaveformBitsStored, -1 );
		minValue = ch.getInteger( Tags.ChannelMinimumValue, 0 );
		maxValue = ch.getInteger( Tags.ChannelMaximumValue, (1 << bitsStored)-1 );
		label = ch.getString( Tags.ChannelLabel );
		status = ch.getString( Tags.ChannelStatus );
		sensitivity = ch.getFloat( Tags.ChannelSensitivity, 1f );
		sensitivityCorrection = ch.getFloat( Tags.ChannelSensitivityCorrectionFactor, 1f );
		sensitivityUnit = getSensitivityUnit( ch, "" );
		if ( "uV".equals( sensitivityUnit) ) {
			sensitivity *= 0.001f;
		}
		if ( fCorr != 1f )
			sensitivity *= fCorr * sensitivityCorrection; 
		lowFreq = ch.getFloat( Tags.FilterLowFrequency );
		highFreq = ch.getFloat( Tags.FilterHighFrequency );
		this.buffer = buffer;
	}
	
	/**
	 * @param ch
	 * @return
	 */
	private String getSensitivityUnit(Dataset ch, String def) {
		DcmElement sensSeq = ch.get(Tags.ChannelSensitivityUnitsSeq);
		if ( sensSeq != null) {
			Dataset ds = sensSeq.getItem();
			if ( ds != null)
				return ds.getString( Tags.CodeValue );
		}
		return def;
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
