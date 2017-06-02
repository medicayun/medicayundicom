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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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
class Configuration extends Properties
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------  
   private static String replace(String val, String from, String to) {
      return from.equals(val) ? to : val;
   }
   
   // Constructors --------------------------------------------------
   public Configuration(URL url) {
      InputStream in = null;
      try {
         load(in = url.openStream());
      } catch (Exception e) {
         throw new RuntimeException("Could not load configuration from "
               + url, e);
      } finally {
         if (in != null) {
            try { in.close(); } catch (IOException ignore) {}
         }
      }
   }
   
   // Public --------------------------------------------------------
   public String getProperty(String key, String defaultValue,
                             String replace, String to) {
      return replace(getProperty(key, defaultValue), replace, to);
   }
   
   public List tokenize(String s, List result) {
      StringTokenizer stk = new StringTokenizer(s, ", ");
      while (stk.hasMoreTokens()) {
         String tk = stk.nextToken();
         if (tk.startsWith("$")) {
            tokenize(getProperty(tk.substring(1),""), result);
         } else {
            result.add(tk);
         }
      }
      return result;
   }
   
   public String[] tokenize(String s) {
      if (s == null)
         return null;
      
      List l = tokenize(s, new LinkedList());      
      return (String[])l.toArray(new String[l.size()]);
   }
       
}
