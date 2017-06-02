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

import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.dict.UIDs;

import org.dcm4cheri.util.StringUtils;

import java.util.HashMap;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3493 $ $Date: 2002-07-15 00:03:36 +0800 (周一, 15 7月 2002) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
class DcmServiceRegistryImpl
extends HashMap
implements DcmServiceRegistry
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public DcmServiceRegistryImpl()
   {
      put(UIDs.Verification, DcmServiceBase.VERIFICATION_SCP);
   }
   
   // Public --------------------------------------------------------
      
   // DcmServiceRegistryimplementation ------------------------------

   public boolean bind(String uid, DcmService service)
   {
      if (service == null)
         throw new NullPointerException();
      
      if (keySet().contains(StringUtils.checkUID(uid)))
         return false;
      
      put(uid, service);
      return true;
   }

   public boolean unbind(String uid)
   {
      return remove(uid) != null;
   }

   public DcmService lookup(String uid)
   {
      DcmService retval = (DcmService)get(StringUtils.checkUID(uid));
      return retval != null ? retval : DcmServiceBase.NO_SUCH_SOP_CLASS_SCP;
   }   
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
