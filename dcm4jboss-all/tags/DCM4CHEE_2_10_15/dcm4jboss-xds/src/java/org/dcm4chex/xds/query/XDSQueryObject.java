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

package org.dcm4chex.xds.query;

import java.io.IOException;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.common.SoapBodyProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public abstract class XDSQueryObject implements SoapBodyProvider {

    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_DEPRECATED = "Deprecated";
    
    public static final String XMLNS_DEFAULT = "xmlns";
    public static final String XMLNS_RIM = "xmlns:rim";
    public static final String XMLNS_RS = "xmlns:rs";
    public static final String XMLNS_XSI = "xmlns:xsi";
    public static final String XMLNS_Q = "xmlns:q";
    
    public static final String RETURN_TYPE_OBJREF = "ObjectRef";
    public static final String RETURN_TYPE_LEAF = "LeafClass";

    public static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
    
    private String returnType = RETURN_TYPE_OBJREF;
    private boolean returnComposedObjects = true;
    private URL xslt;
    TransformerHandler th;
    
    private static Logger log = Logger.getLogger(XDSQueryObject.class);
    
    /**
     * @return the returnComposedObjects
     */
    public boolean isReturnComposedObjects() {
        return returnComposedObjects;
    }


    /**
     * @param returnComposedObjects the returnComposedObjects to set
     */
    public void setReturnComposedObjects(boolean returnComposedObjects) {
        this.returnComposedObjects = returnComposedObjects;
    }


    /**
     * @return the returnType
     */
    public String getReturnType() {
        return returnType;
    }


    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }


    /**
     * @return the xslt
     */
    public URL getXslt() {
        return xslt;
    }


    /**
     * @param xslt the xslt to set
     */
    public void setXslt(URL xslt) {
        this.xslt = xslt;
    }


    private void initTransformHandler() {
        try {
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            if (xslt != null) {
                try {
                    th = tf.newTransformerHandler(new StreamSource(xslt.openStream(),
                        xslt.toExternalForm()));
                } catch ( IOException x ) {
                    log.error("Cant open xsl file:"+xslt, x );
                }
            } else {
                th = tf.newTransformerHandler();
                th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
            }
        } catch ( Throwable t ) {
            t.printStackTrace();
        }
    }
    
    public Document getDocument() {
        initTransformHandler();
        DOMResult result = new DOMResult();
        th.setResult( result );
        try {
            th.startDocument();
            addAdhocQueryRequest();
            th.endDocument();
        } catch (SAXException x) {
            log.error( "Cant build query request!",x);
            return null;
        }
        return (Document) result.getNode();
    }
    
    public abstract void addAdhocQueryRequest() throws SAXException;
    
    /**
     * Returns the root tag of the response message.
     * <p>
     * This tag is used to check and process the SOAP response message.
     * @return
     */
    public abstract String getResponseTag();

}
