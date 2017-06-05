/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface WaveFormBuffer {
	/**
	 * Returns the value of the next sample.
	 * @return value of next sample.
	 */
	int getValue();
	
	/**
	 * Return the value for given sample.
	 * <p>
	 * A following call of getValue() will return the value of sample (sampleNr + 1)!
	 * @param sampleNr Sample number
	 * @return Value for given sample number.
	 */
	int getValue( int sampleNr );
	
	/**
	 * This method reset the buffer. 
	 * Therefore next getValue() call will return first sample!
	 *
	 */
	void reset();
}