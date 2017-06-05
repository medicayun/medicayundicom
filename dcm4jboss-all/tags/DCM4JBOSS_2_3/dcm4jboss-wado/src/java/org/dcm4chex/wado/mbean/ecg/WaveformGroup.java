/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import java.nio.ByteBuffer;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveformGroup {

	private int grpIndex;
	private int nrOfChannels;
	private int nrOfSamples;
	private int bitsAlloc;
	private float sampleFreq;//in Hz
	private String muxGrpLabel;
	private String sampleInterpretation;
	private WaveFormChannel[] channels = null;
	private ByteBuffer data = null;
	
	/**
	 * @param elem
	 */
	public WaveformGroup(DcmElement elem, int grpIndex) {
		if ( elem == null ) throw new NullPointerException( "WaveFormSequence missing!");
		Dataset ds = elem.getItem( grpIndex );
		this.grpIndex = grpIndex;
		nrOfChannels = ds.getInt( Tags.NumberOfWaveformChannels, 0 );
		nrOfSamples = ds.getInt( Tags.NumberOfWaveformSamples, 0 );
		sampleFreq = ds.getFloat( Tags.SamplingFrequency, 0f );
		muxGrpLabel = ds.getString( Tags.MultiplexGroupLabel );
		bitsAlloc = ds.getInt( Tags.WaveformBitsAllocated, 0 );
		sampleInterpretation = ds.getString( Tags.WaveformSampleInterpretation );
		data = ds.getByteBuffer( Tags.WaveformData );
		prepareChannels( ds.get( Tags.ChannelDefinitionSeq ) );
	}
	
	/**
	 * @return Returns the nrOfSamples.
	 */
	public int getNrOfSamples() {
		return nrOfSamples;
	}
	/**
	 * @return Returns the nrOfChannels.
	 */
	public int getNrOfChannels() {
		return nrOfChannels;
	}
	
	/**
	 * @return Returns the bitsAlloc.
	 */
	public int getBitsAlloc() {
		return bitsAlloc;
	}
	/**
	 * @return Returns the sampleFreq.
	 */
	public float getSampleFreq() {
		return sampleFreq;
	}
	public WaveFormChannel getChannel( int idx ) {
		return channels[idx];
	}

	/**
	 * @return
	 */
	public String getFilterText() {
		return channels[0].getLowFreq()+"-"+channels[0].getHighFreq()+" Hz";
	}
	
	/**
	 * @param element
	 */
	private void prepareChannels( DcmElement chDefs ) {
		int len = chDefs.vm();
		channels = new WaveFormChannel[ len ];
		WaveFormChannel ch;
		for ( int i = 0 ; i < len ; i++ ) {
			ch = new WaveFormChannel( chDefs.getItem(i), getWaveFormBuffer(i) );
			channels[i] = ch;
		}
		
	}
	
	/**
	 * @param i
	 * @return
	 */
	private WaveFormBuffer getWaveFormBuffer(int idx) {
		if ( this.bitsAlloc == 8 ) {
			return new WaveForm8Buffer( data, idx, nrOfChannels, sampleInterpretation );
		} else if ( bitsAlloc == 16 ) {
			return new WaveForm16Buffer( data, idx, nrOfChannels, sampleInterpretation );
		} else {
			return null;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WaveFormGroup(").append(grpIndex).append("):").append(muxGrpLabel);
		sb.append(" channels:").append( nrOfChannels ).append(" samples:").append( nrOfSamples );
		sb.append(" sampleFreq:").append( sampleFreq ).append(" channelDefs:").append( channels );
		return sb.toString();
	}


}
