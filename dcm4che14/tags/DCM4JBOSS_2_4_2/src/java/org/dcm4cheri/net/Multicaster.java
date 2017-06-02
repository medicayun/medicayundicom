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
import org.dcm4che.net.PDU;

import java.io.IOException;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
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
class Multicaster implements AssociationListener {
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private final AssociationListener a, b;
   
   // Static --------------------------------------------------------
   public static AssociationListener add(AssociationListener a,
                                         AssociationListener b) {
      if (a == null)  return b;
      if (b == null)  return a;
      return new Multicaster(a, b);
   }
   
   public static AssociationListener remove(AssociationListener l,
                                            AssociationListener oldl) {
      if (l == oldl || l == null) {
         return null;
      } if (l instanceof Multicaster) {
         return ((Multicaster)l).remove(oldl);
      }
      return null;
   }
   // Constructors --------------------------------------------------
   private Multicaster(AssociationListener a, AssociationListener b)
   {
      this.a = a; this.b = b;
   }
   
   // Public --------------------------------------------------------
   
   // AssociationListener implementation ----------------------------
   public void write(Association src, PDU pdu) {
      a.write(src, pdu);
      b.write(src, pdu);
   }
   
   public void write(Association src, Dimse dimse) {
      a.write(src, dimse);
      b.write(src, dimse);
   }
   
   public void received(Association src, PDU pdu) {
      a.received(src, pdu);
      b.received(src, pdu);
   }
   
   public void received(Association src, Dimse dimse) {
      a.received(src, dimse);
      b.received(src, dimse);
   }
   
   public void error(Association src, IOException ioe) {
      a.error(src, ioe);
      b.error(src, ioe);
   }
   
   public void closing(Association src) {
      a.closing(src);
      b.closing(src);
   }
      
   public void closed(Association src) {
       a.closed(src);
       b.closed(src);
    }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private AssociationListener remove(AssociationListener oldl) {
      if (oldl == a)  return b;
      if (oldl == b)  return a;
      AssociationListener a2 = remove(a, oldl);
      AssociationListener b2 = remove(b, oldl);
      if (a2 == a && b2 == b) {
         return this;
      }
      return add(a2, b2);
   }
   
   // Inner classes -------------------------------------------------
}
