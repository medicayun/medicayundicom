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

package org.dcm4che.net;

import java.io.IOException;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3493 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public interface DcmService
{
   void c_store(ActiveAssociation assoc, Dimse rq)
   throws IOException;
   
   void c_get(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void c_find(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void c_move(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void c_echo(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void n_create(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void n_set(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void n_get(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void n_delete(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void n_action(ActiveAssociation assoc, Dimse rq)
   throws IOException;

   void n_event_report(ActiveAssociation assoc, Dimse rq)
   throws IOException;
}
