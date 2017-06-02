/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.server;

import java.io.File;
import java.util.Comparator;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
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
public interface PollDirSrv
{
   interface Handler
   {
      void openSession() throws Exception;
      boolean process(File file) throws Exception;
      void closeSession();
   }

   void setSortCrit(Comparator sortCrit);
   
   File getDoneDir();
   
   void setDoneDir(File doneDir);
   
   long getOpenRetryPeriod();
   
   void setOpenRetryPeriod(long openRetryPeriod);
   
   long getDeltaLastModified();
   
   void setDeltaLastModified(long deltaLastModified);

   int getDoneCount();
   
   int getFailCount();
   
   int getOpenCount();
   
   int getFailOpenCount();
   
   void resetCounter();

   void start(File dir, long period);
   
   void stop();     
}
