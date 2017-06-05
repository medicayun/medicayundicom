/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import java.nio.ByteBuffer;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveForm8Buffer implements WaveFormBuffer {
	ByteBuffer buffer;
	int leadNr;
	int nrOfLeads;
	int offset = 0;
	String sampleIterpretation;
	
	public WaveForm8Buffer( ByteBuffer bBuf, int leadNr, int nrOfLeads, String sampleIterpretation ) {
		buffer = bBuf.asReadOnlyBuffer();
		this.leadNr = leadNr;
		this.nrOfLeads = nrOfLeads;
		offset = leadNr;
		this.sampleIterpretation = sampleIterpretation;
	}
	
	public int getValue() {
		int v = buffer.get( offset );
		offset += nrOfLeads;
		return v;
	}
	
	public int getValue( int sampleNr ) {
		offset = sampleNr * nrOfLeads + leadNr;
		return getValue();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WaveForm8Buffer:").append(leadNr).append(" of ").append(nrOfLeads);
		sb.append(" leads, sampleIterpretation:").append(sampleIterpretation).append("buffersize:").append(buffer.limit());
		return sb.toString();
	}

}
