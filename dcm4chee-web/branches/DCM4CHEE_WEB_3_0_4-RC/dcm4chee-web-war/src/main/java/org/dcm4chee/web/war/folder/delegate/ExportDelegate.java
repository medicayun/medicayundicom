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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.war.folder.delegate;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision: 18618 $ $Date: 2016-06-23 15:36:17 +0800 (周四, 23 6月 2016) $
 * @since Aug 18, 2009
 */
public class ExportDelegate extends BaseMBeanDelegate {

    private static ExportDelegate delegate;

    private ExportDelegate() {
        super();
    }

    public void export(String destAET, String patID, String[] studyIUIDs, String[] seriesIUIDs, String[] sopIUIDs, 
            DimseRSPHandler handler) throws Exception {
            server.invoke(serviceObjectName, "move", 
                    new Object[]{null, destAET, patID, studyIUIDs, seriesIUIDs, sopIUIDs, handler, false}, 
                    new String[]{String.class.getName(), String.class.getName(), String.class.getName(),
                    String[].class.getName(), String[].class.getName(), String[].class.getName(), 
                    DimseRSPHandler.class.getName(), boolean.class.getName()});
    }

    public java.io.File retrieveFileFromTar(File file, ObjectName tarRetrieverServicename) throws Exception {
        return (java.io.File) server.invoke(tarRetrieverServicename, "retrieveFile", 
                new Object[]{file.getFileSystem().getDirectoryPath(), file.getFilePath()}, 
                new String[]{String.class.getName(), String.class.getName()});
        
    }
    @Override
    public String getServiceNameCfgAttribute() {
        return "moveScuServiceName";
    }

    public static ExportDelegate getInstance() {
        if (delegate==null)
            delegate = new ExportDelegate();
        return delegate;
    }

}
