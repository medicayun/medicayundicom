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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4chex.xds.mbean;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4chex.xds.common.XDSResponseObject;
import org.xml.sax.SAXException;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2371 $ $Date: 2006-03-27 15:12:50 +0200 (Mo, 27 Mrz 2006) $
 * @since Mar 08, 2006
 */
public class SOAPMessageResponse implements XDSResponseObject {

	SOAPMessage message;
	public SOAPMessageResponse( SOAPMessage message ) {
		this.message = message;
	}
	public void execute(HttpServletResponse response)
			throws TransformerConfigurationException, SAXException, IOException {
		response.setContentType(getContentType());
		response.setStatus(getReturnCode());
		OutputStream os = response.getOutputStream();
		try {
			message.writeTo(os);
		} catch (SOAPException x) {
			throw (IOException) new IOException("Cant write SOAP response!").initCause(x);
		}
		os.close();
	}

	public String getContentType() {
		return "text/xml";
	}

	public int getReturnCode() {
		return HttpServletResponse.SC_OK;
	}

	public String getMessage() {
		return message.getContentDescription();
	}
}
