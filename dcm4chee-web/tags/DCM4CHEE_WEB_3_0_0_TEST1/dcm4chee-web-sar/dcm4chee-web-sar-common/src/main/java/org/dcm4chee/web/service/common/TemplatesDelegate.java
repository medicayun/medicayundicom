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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.web.service.common;

import java.io.File;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.transform.Templates;

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 3309 $ $Date: 2007-05-07 01:38:48 +0200 (Mo, 07 Mai 2007) $
 * @since May 4, 2007
 */
public final class TemplatesDelegate {

    private final ServiceMBeanSupport service;
    
    private ObjectName templatesServiceName;

    private String configDir;
    
    public TemplatesDelegate(final ServiceMBeanSupport service) {
        this.service = service;
    }

    public final ObjectName getTemplatesServiceName() {
        return templatesServiceName;
    }

    public final void setTemplatesServiceName(ObjectName templatesServiceName) {
        this.templatesServiceName = templatesServiceName;
    }
    
    public final String getConfigDir() {
        return configDir;
    }

    public final void setConfigDir(String path) {
        this.configDir = path;
    }
    
    public Templates getTemplates(File f) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return (Templates) service.getServer().invoke(
                templatesServiceName, "getTemplates",
                new Object[] { f },
                new String[] { File.class.getName(),});
    }

    public Templates getTemplatesForAET(String aet, String fname) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return (Templates) service.getServer().invoke(
                templatesServiceName, "getTemplatesForAET",
                new Object[] { configDir, aet, fname},
                new String[] { String.class.getName(),
                        String.class.getName(),
                        String.class.getName(),});
    }
}
