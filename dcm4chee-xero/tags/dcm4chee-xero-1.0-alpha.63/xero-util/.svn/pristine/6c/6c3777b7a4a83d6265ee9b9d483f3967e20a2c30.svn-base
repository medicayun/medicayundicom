/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.metadata.servlet;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * An XML namespace prefix mapper that is designed to be consistent and provide
 * the most efficient possible namespace for each XML node.
 * @author Andrew Cowan (amidx)
 */
public class ConsistentNamespacePrefixMapper extends NamespacePrefixMapper
{
   private static final String DEFAULT_PREFIX = "";
   
   private static final String XERO_NAMESPACE = "http://www.dcm4chee.org/xero/search/study";
   private static final String W3C_DOMAIN = "http://www.w3.org";
   
   /**
    * 
    * @see com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String, java.lang.String, boolean)
    */
   @Override
   public String getPreferredPrefix(String namespaceURI, String suggestion, boolean requirePrefix)
   {
      if(namespaceURI.startsWith(XERO_NAMESPACE))
         return DEFAULT_PREFIX;
      else if(namespaceURI.startsWith(W3C_DOMAIN))
         if(namespaceURI.endsWith("svg"))
            return "s";
         else if(namespaceURI.endsWith("xhtml"))
            return "h";
         else if(namespaceURI.endsWith("xlink"))
            return "l";
         else if(namespaceURI.endsWith("XMLSchema-instance"))
            return "i";
         
      // Return null to indicate that other namespaces should be generated as required.   
      // TODO: Maybe return 'suggestion' ???
      return null;
   }

}
