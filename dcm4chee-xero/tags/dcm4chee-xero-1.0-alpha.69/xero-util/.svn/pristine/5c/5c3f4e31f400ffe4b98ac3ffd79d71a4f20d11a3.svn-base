// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.metadata.json;

import java.io.IOException;
import java.io.Writer;


/**
 * Class that will pretty print JSON output for easier viewing for users.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class JSONPrettyPrinter extends Writer
{
   private static final char TAB = '\t';
   private static final char CR = '\n';
   
   private final Writer writer;
   
   private int indent = 0;
   private StringBuilder str;
   
   public JSONPrettyPrinter(Writer writer)
   {
      this.writer = writer;
      this.str = new StringBuilder();
   }

   /**
    * Pass through to underlying writer.
    * @see java.io.Writer#write()
    */
   @Override
   public void write(char[] cbuf, int off, int len) throws IOException
   {
      str.setLength(0);
      indent(str,cbuf,off,len);
      writer.write(str.toString());
   }

   private void indent(StringBuilder str, char[] chars,int off, int len)
   {
      for(int i=off;i<len;i++)
      {
         char c = chars[i];
         switch(c)
         {
         case '[':
            indent++;
            break;
         case ']':
            indent--;
            break;
         case '{':
            if(indent > 0 )
            {
            str.append(CR);
            for(int level=0;level<indent;level++)
               str.append(TAB);
            }
            break;
         }
         
         str.append(c);
      }
   }

   /**
    * Pass through to underlying writer.
    * @see java.io.Writer#close()
    */
   @Override
   public void close() throws IOException
   {
      writer.close();
   }

   /**
    * Pass through to underlying writer.
    * @see java.io.Writer#flush()
    */
   @Override
   public void flush() throws IOException
   {
      writer.flush();
   }

   @Override
   public String toString()
   {
      return writer.toString();
   }

}
