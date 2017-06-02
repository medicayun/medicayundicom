/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.net;

import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;

import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplifeid and specialized version of
 * EDU.oswego.cs.dl.util.concurrent.FutureResult
 * in Doug Lee's util.concurrent package.
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="http://g.oswego.edu/index.html">Doug Lee</a>
 * @version $Revision: 3829 $ $Date: 2004-12-01 02:38:25 +0800 (周三, 01 12月 2004) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class FutureRSPImpl
implements DimseListener, AssociationListener, FutureRSP {
   
   // Constants -----------------------------------------------------
   private long setAfterCloseTO = 500;
   private long getFindRSPdelay = 10;
   
   // Attributes ----------------------------------------------------
   private boolean closed = false;
   private boolean ready = false;
   private Dimse rsp = null;
   private final ArrayList pending = new ArrayList();
   private IOException exception = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // FutureRSP implementation ----------------------------------------------
   public synchronized void set(Dimse rsp) {
      this.rsp = rsp;
      ready = true;
      notifyAll();
   }
   
   public synchronized void setException(IOException ex) {
      exception = ex;
      ready = true;
      notifyAll();
   }
   
   public synchronized Dimse get()
   throws InterruptedException, IOException {
      while (!ready && !closed) wait();
      
      // handle reverse order of last rsp and close indication, caused
      // by lausy Thread synchronisation
      if (!ready) wait(setAfterCloseTO);
      
      return doGet();
   }
   
   public synchronized List listPending() {
      return Collections.unmodifiableList(pending);      
   }
   
   public synchronized IOException getException() {
      return exception;
   }
   
   public synchronized boolean isReady() {
      return ready;
   }
   
   public synchronized Dimse peek() {
      return rsp;
   }
   
   // DimseListener implementation ---------------------------------
   public void dimseReceived(Association assoc, Dimse dimse) {
      if (dimse.getCommand().isPending()) {
          pending.add(dimse);
      } else {
         set(dimse);
      }
   }
   
   // AssociationListener implementation ----------------------------
   public void write(Association src, PDU pdu) {
   }
   
   public void received(Association src, Dimse dimse) {
   }
   
   public void error(Association src, IOException ioe) {
      setException(ioe);
   }
   
   public void closing(Association src) {
   }
   
   synchronized public void closed(Association src) {
      closed = true;
      notifyAll();
   }
   
   public void write(Association src, Dimse dimse) {
   }
   
   public void received(Association src, PDU pdu) {
   }
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private Dimse doGet() throws IOException {
      if (exception != null)
         throw exception;
      return rsp;
   }
   // Inner classes -------------------------------------------------
}
