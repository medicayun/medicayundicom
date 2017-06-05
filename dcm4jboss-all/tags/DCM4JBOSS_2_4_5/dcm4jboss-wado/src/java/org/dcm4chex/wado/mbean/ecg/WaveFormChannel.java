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
	public WaveFormChannel(Dataset ch, WaveFormBuffer buffer, float fCorr) {
		this.fCorr = fCorr;
		chNr = ch.getInt( Tags.WaveformChannelNumber, -1);
		chSource = ch.get(Tags.ChannelSourceSeq).getItem().getString( Tags.CodeMeaning);
		bitsStored = ch.getInt( Tags.WaveformBitsStored, -1 );
		minValue = ch.getInteger( Tags.ChannelMinimumValue, 0 );
		maxValue = ch.getInteger( Tags.ChannelMaximumValue, (1 << bitsStored)-1 );
		label = ch.getString( Tags.ChannelLabel );
		status = ch.getString( Tags.ChannelStatus );
		
		sensitivity = channelSensitivity = ch.getFloat( Tags.ChannelSensitivity, 1f );
		sensitivityCorrection = ch.getFloat( Tags.ChannelSensitivityCorrectionFactor, 1f );
		sensitivityUnit = getSensitivityUnit( ch, "" );
		if ( "uV".equals( sensitivityUnit) ) {
			sensitivity *= getSensitifityUnitFactor( "" );
		}
		if ( fCorr != 1f ) sensitivity *= fCorr;
		sensitivity *= sensitivityCorrection; 
		
		lowFreq = ch.getFloat( Tags.FilterLowFrequency );
		highFreq = ch.getFloat( Tags.FilterHighFrequency );
		this.buffer = buffer;
		if ( log.isDebugEnabled() ) logInfo();
	}
	
	public WaveFormChannel(Dataset ch, WaveFormBuffer buffer, float fCorr, float pixPerUnit) {
		this( ch, buffer, fCorr);
	}
	/**
	 * Returns the sensitivity correction factor for given unit string.
	 * <p>
	 * The base unit is mV! Therefore uV will return 0.001f!
	 * 
	 * @param unit The unit String
	 * @return the correction factor for given unit.
	 */
	private float getSensitifityUnitFactor(String unit) {
		if ( "uV".equals(unit) ) return 0.001f;
		return 1f;
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

}
