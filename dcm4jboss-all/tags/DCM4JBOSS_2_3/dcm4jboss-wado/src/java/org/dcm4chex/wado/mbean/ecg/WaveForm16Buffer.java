/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveForm16Buffer implements WaveFormBuffer {
	ShortBuffer buffer;
	int leadNr;
	int nrOfLeads;
	int offset = 0;
	String sampleIterpretation;
	
	public WaveForm16Buffer( ByteBuffer bBuf, int leadNr, int nrOfLeads, String sampleIterpretation ) {
		buffer = bBuf.asShortBuffer();
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
	
}
