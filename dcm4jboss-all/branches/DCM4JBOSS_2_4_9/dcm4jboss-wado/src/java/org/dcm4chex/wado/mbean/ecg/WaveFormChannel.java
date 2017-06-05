/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import org.apache.log4j.Logger;
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

	private WaveformGroup wfGrp;
	private int chNr;
	private String chSource;
	private int bitsStored;
	private Integer minValue;
	private Integer maxValue;
	private String label;
	private String status;
	
	private float sensitivity;
	private float channelSensitivity;
	private String sensitivityUnit;
	private float sensitivityCorrection;
	private float fCorr;
	private float pixPerUnit;
	
	private WaveFormBuffer buffer;
	private Float lowFreq;
	private Float highFreq;
	
	private static Logger log = Logger.getLogger( WaveFormChannel.class.getName() );

	/**
	 * @param buffer
	 * @param item
	 */
	public WaveFormChannel(WaveformGroup grp, Dataset ch, WaveFormBuffer buffer, float fCorr) {
		wfGrp = grp;
		this.fCorr = fCorr;
		chNr = ch.getInt( Tags.WaveformChannelNumber, -1);
		chSource = ch.get(Tags.ChannelSourceSeq).getItem().getString( Tags.CodeMeaning);
		try {
			bitsStored = ch.getInt( Tags.WaveformBitsStored, -1 );
		} catch ( UnsupportedOperationException x ) {
			bitsStored = ch.getFloat( Tags.WaveformBitsStored ).intValue();
		}
		minValue = ch.getInteger( Tags.ChannelMinimumValue );//min (max) should have the same VR as waveform data!
		maxValue = ch.getInteger( Tags.ChannelMaximumValue );
		label = ch.getString( Tags.ChannelLabel );
		status = ch.getString( Tags.ChannelStatus );
		
		sensitivity = channelSensitivity = ch.getFloat( Tags.ChannelSensitivity, 1f );
		sensitivityCorrection = ch.getFloat( Tags.ChannelSensitivityCorrectionFactor, 1f );
		sensitivityUnit = getSensitivityUnit( ch, "" );
		if ( fCorr != 1f ) sensitivity *= fCorr;
		sensitivity *= sensitivityCorrection; 
		
		lowFreq = ch.getFloat( Tags.FilterLowFrequency );
		highFreq = ch.getFloat( Tags.FilterHighFrequency );
		this.buffer = buffer;
		if ( log.isDebugEnabled() ) logInfo();
	}
	
	/**
	 * @return Returns the wfGrp.
	 */
	public WaveformGroup getWaveformGroup() {
		return wfGrp;
	}
	/**
	 * @param ch
	 * @return
	 */
	private String getSensitivityUnit(Dataset ch, String def) {
		DcmElement sensSeq = ch.get(Tags.ChannelSensitivityUnitsSeq);
		if ( sensSeq != null) {
			Dataset ds = sensSeq.getItem();
			if ( ds != null) {
				if ( "UCUM".equals(ds.getString(Tags.CodingSchemeDesignator)) ) {
					return ds.getString( Tags.CodeValue );
				} else {
					return ds.getString(Tags.CodeMeaning);
				}
			}
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

	/**
	 * @return Returns the sensitivity.
	 */
	public float getSensitivity() {
		return sensitivity;
	}
	
	
	
	public int getRawValue() {
		return buffer.getValue();
	}
	
	public int getRawValue( int sampleNr ) {
		return buffer.getValue( sampleNr );
	}
	
	/**
	 * @return Returns the maxValue.
	 */
	public Integer getMaxValue() {
		return maxValue;
	}
	/**
	 * @return Returns the minValue.
	 */
	public Integer getMinValue() {
		return minValue;
	}
	/**
	 * @return Returns the sensitivityUnit.
	 */
	public String getUnit() {
		return sensitivityUnit;
	}
	/**
	 * This method reset the channel. 
	 * Therefore next getValue() call will return first sample!
	 *
	 */
	public void reset() {
		buffer.reset();
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WaveFormChannel: chNr").append(chNr).append(" label:").append(label);
		sb.append(" bitsStored:").append( bitsStored).append(" min:").append(minValue).append(" max:").append(maxValue);
		sb.append(" status:").append(status);
		return sb.toString();
	}

	/**
	 * @param ch
	 */
	private void logInfo() {
		log.debug("WaveFormchannel: chNr:"+chNr);
		log.debug("   chSource:"+chSource);
		log.debug("   bitsStored:"+bitsStored);
		log.debug("   minValue:"+minValue);
		log.debug("   maxValue:"+maxValue);
		log.debug("   status:"+status);
		log.debug("   lowFreq:"+lowFreq);
		log.debug("   highFreq:"+highFreq);
		log.debug("   sensitivityUnit:"+sensitivityUnit);
		log.debug("   calculated sensitivity:"+sensitivity);
		log.debug("      ChannelSensitivity:"+channelSensitivity);
		log.debug("      sensitivityCorrection:"+sensitivityCorrection);
		log.debug("      fCorr:"+fCorr);
		log.debug("   Buffer:"+buffer);
	}
	
	/**
	 * @return
	 */
	public float getValue() {
		return getRawValue() * sensitivity;//TODO all the other things to get the real value!
	}
	
	/**
	 * Calculates min and max value for given samples (start..(end-1)).
	 * <p>
	 * Return the float array with min value at index 0 and max at index 1.
	 * <p>
	 * This method reset the channel. Therefore next getValue() call will return first sample!
	 * 
	 * @param start The first sample
	 * @param end The last sample (exclusive)
	 * 
	 * @return array with min and max value.
	 */
	public float[] calcMinMax(int start, int end) {
		int min = buffer.getValue(start);
		int max = min;
		int value;
		for ( int i = start+1 ; i < end ; i++ ) {
			value = buffer.getValue(i);
			if ( value < min ) min = value;
			else if ( value > max ) max = value;
		}
		buffer.reset();
		return new float[]{ min*sensitivity, max*sensitivity};
	}

}
