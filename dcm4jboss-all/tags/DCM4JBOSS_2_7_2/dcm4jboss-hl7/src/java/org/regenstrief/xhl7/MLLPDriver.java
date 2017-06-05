/*  HL7 MLLP Driver manages a pair of Input/Output Streams according to
 *               the HL7 Minimal Lower Layer Protocol.
 *
 *  Copyright (C) 2003, Regenstrief Institute. All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Written and maintained by Gunther Schadow <gschadow@regenstrief.org>
 *  Regenstrief Institute for Health Care
 *  1050 Wishard Blvd., Indianapolis, IN 46202, USA.
 *
 * $Id: MLLPDriver.java 1761 2005-06-01 23:31:46Z gunterze $
 */
package org.regenstrief.xhl7;

import java.lang.*;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// for test only
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.Reader;

/** HL7 MLLP Driver manages a pair of Input/Output Streams according to
              the HL7 Minimal Lower Layer Protocol.

  <p>
  The MLLP protocol is a half-duplex protocol, which means that the 
  driver must maintain a state and act differently depending on the 
  state. A driver is initially in state clear-to-send if it will be
  sending first. When in clear-to-send, something must be sent before
  one can begin receiving. When the driver is in sending state, the
  first reading call will terminate sending and begin receiving. Once
  in receiving mode, all output is buffered in a growing buffer that
  is only flushed to the stream when the peer has terminated sending.
  </p>

  @author Gunther Schadow
  @version $Id: MLLPDriver.java 1761 2005-06-01 23:31:46Z gunterze $
*/
public class MLLPDriver {

  private InputStream  _inputStream;
  private OutputStream _outputStream;
  private boolean _clearToSend;
  private boolean _sending;
  private boolean _receiving;
  private boolean _inputClosed;

  /** Instantiate an MLLP driver given a pair of input/output streams
      and an indicator specifying whether we start out  sending or 
      receiving.

      @param inputStream the input half of the MLLP managed connection.
      @param outputStream the output half of the MLLP managed connection.
      @param clearToSend whether we intend to send first.
      @throws java.io.IOException if an unspecific I/O error occurs.
  */
  public MLLPDriver(InputStream inputStream, 
		    OutputStream outputStream, 
		    boolean clearToSend) 
    throws IOException
  {
    // make sure we have a buffered input stream, our read operations
    // are expensive single byte reads. 
    if(!(inputStream instanceof BufferedInputStream))
      inputStream = new BufferedInputStream(inputStream);

    this._inputStream = inputStream;
    this._outputStream = outputStream;
    this._clearToSend = clearToSend;
    this._sending = false;
    this._receiving = false;
    this._inputClosed = false;
  }
  
  // MLLP transmission frame delimiter characters (sequences)

  /* COMMENTED OUT: I use these for testing, as its much easier to
     type on a terminal:  
  private static final byte SOT = '['; 
  private static final byte[] EOT = new byte[] { ']', '+' };
  *//* STANDARD: the following are the standard MLLP frame delimiters: */
  private static final byte SOT = 0x0b; 
  private static final byte[] EOT = new byte[] { 0x1c, 0x0d };
  /**/

  /** Send SOT and send byte buffer if any. */
  private void beginSending() throws IOException {
    if(this._sending)
      throw new IllegalStateException("sending already");
    this._outputStream.write(SOT);
    this._sending = true;
    if(_outputBuffer.size()>0) {
      _outputBuffer.writeTo(_outputStream);
      _outputBuffer.reset();
    }
  }
  
  /** If sending, sends the EOT sequence */
  private void endSending() throws IOException {
    if(! this._sending)
      throw new IllegalStateException("not sending");
    this._proxyOutputStream.flush();
    this._outputStream.write(EOT);
    this._outputStream.flush();
    this._sending = false;
    this._clearToSend = false;
  }
  
  /** Skip to SOT. */
  private void beginReceiving() throws IOException {
    if(this._receiving)
      throw new IllegalStateException("receiving already");      
    if(this._sending)
      throw new IllegalStateException("still sending");      
    if(!this._inputClosed) {
      for(int b = 0; b != SOT; b = this._inputStream.read()) {
	if(b == -1) {
	  this._inputClosed = true;
	  return;
	}
      }      
      this._receiving = true;
    }
  }
    
  /** Skip to EOT[1]. */
  private void endReceiving() throws IOException {
    if(this._sending)
      throw new IllegalStateException("still sending");      
    if(this._receiving && !this._inputClosed) {
      for(int b = 0; b != EOT[1]; b = this._inputStream.read()) {
	if(b == -1)
	  this._inputClosed = true;
      }
      this._receiving = false;
      this._clearToSend = true;
    }
    if(_outputBuffer.size()>0)
      beginSending();
  }
 
  /** Check if peer has closed his output channel (which is linked
      to our input channel, so when peer closes his output, he 
      closes our input.)
      
      @return true if peer has closed his output.
      @throws java.io.IOException if an unspecific I/O error occurs.
   */
  public boolean inputClosed() {
    return this._inputClosed;
  }
  
  /** Check if there is more incoming input. Blocks until a new
      MLLP frame is encountered on the input. 

      @return true if we are currently receiving an input frame, either
              a newly started input frame or one that is ongoing.
      @throws java.io.IOException if an unspecific I/O error occurs.
  */
  public boolean hasMoreInput() 
    throws IOException
  {
    if(this._inputClosed)
      return false;
    if(this._sending)
      endSending();
    if(!this._receiving)
      beginReceiving();
    return !this._inputClosed;
  }

  /** Make sure all output is flushed and turn the connection over to
      the peer. Call this when you are done sending your message to
      close your output frame.

      @throws java.io.IOException if an unspecific I/O error occurs.
  */
  public void turn() 
    throws IOException
  {
    this._proxyOutputStream.flush();
    if(this._sending)
      endSending();
  }

  /** Discard pending buffered output. Discarding current buffered output
      can be useful to delete any incomplete response that has been 
      generated while input was still read and if an error condition 
      occurred. 

      <p>
      Notice, however, as soon as the input frame is closed by the peer,
      the buffered output is flushed, hence, any error conditions 
      that occur after the input has been closed may lead to incomplete
      response messages even if pending output is discarded. If 
      a buffered input stream wrapps the MLLP managed input stream, 
      this is quite likely to occur. On the other hand, since (the
      current version anyway) of this driver always wraps the raw 
      input stream into a buffered input stream, an additional buffering
      may not be needed.
      </p>

      @throws java.io.IOException if an unspecific I/O error occurs.
  */
  public void discardPendingOutput() 
    throws IOException
  {
    this._outputBuffer.reset();
  }

  /** Return a stream from which one can read from the MLLP driver. You
      must use this and only this input stream proxy to ensure valid 
      MLLP behavior.
      
      @return an input stream managed through the MLLP driver.
   */
  public InputStream getInputStream() {
    return this._proxyInputStream;
  }

  private InputStream _proxyInputStream
  = new InputStream() {
      public int read() throws IOException {
	if(_inputClosed || _clearToSend) {
	  return -1;
	}
	if(_sending) {
	  endSending();
	}
	if(!_receiving) {
	  beginReceiving();
	  if(_inputClosed) {
	    return -1;
	  }
	}
	int b = _inputStream.read();
	if(b == EOT[0]) {
	  endReceiving();
	  return -1;
	} else {
	  if(b == -1)
	    _inputClosed = true;
	  return b;
	}
      }
    };
  
  /** Return a stream to which on can write to the MLLP driver. You
      must use this and only this output stream proxy to ensure valid 
      MLLP behavior.
      
      @returns an output stream managed through the MLLP driver.
  */  
  public OutputStream getOutputStream() {
    return this._proxyOutputStream;
  }
  
  /** The byte buffer needs to be inspected by the state machine. */
  private ByteArrayOutputStream _outputBuffer = new ByteArrayOutputStream();

  private OutputStream _proxyOutputStream 
  = new OutputStream() {
      public void write(int b) throws IOException {
	if(!_sending && _clearToSend)
	  beginSending();
	if(_sending)
	  _outputStream.write(b);
        else
	  _outputBuffer.write(b);
      }
      public void write(byte b[]) throws IOException {
	if(!_sending && _clearToSend)
	  beginSending();
	if(_sending)
	  _outputStream.write(b);
        else
	  _outputBuffer.write(b);
      }
      public void write(byte b[], int offset, int length) throws IOException {
	if(!_sending && _clearToSend)
	  beginSending();
	if(_sending)
	  _outputStream.write(b, offset, length);
        else
	  _outputBuffer.write(b, offset, length);
      }
      public void flush() throws IOException {
	if(!_sending && _clearToSend)
	  beginSending();
	if(_sending) {
	  _outputStream.flush();
	}
      }
      public void close() throws IOException {
	flush();
	_outputStream.close();
      }
    };


  /** Test the game from standard i/o. */
  public static void main(String args[]) 
  {
    try {
      MLLPDriver driver = new MLLPDriver(System.in, System.out, true);
      InputStream in = driver.getInputStream();
      PrintStream out = new PrintStream(driver.getOutputStream());
      out.println("hello, pleased to meet you");
      driver.turn();
      while(driver.hasMoreInput()) {
	StringBuffer sb = new StringBuffer();
	for(int c = in.read(); c != -1; c = in.read()) {
	  sb.append((char)c);
	}
	out.println("Let me read this back to you, you said " + sb.toString());
      }
    } catch(Exception ex) {
      System.err.println("EXEXE");
      ex.printStackTrace(System.err);
    }
  }
}
