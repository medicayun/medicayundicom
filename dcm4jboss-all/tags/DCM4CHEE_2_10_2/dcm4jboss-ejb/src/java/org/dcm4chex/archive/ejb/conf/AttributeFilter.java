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

package org.dcm4chex.archive.ejb.conf;

import java.util.Arrays;
import java.util.HashMap;

import org.dcm4che.data.Dataset;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2626 $ $Date: 2006-07-12 20:15:28 +0800 (周三, 12 7月 2006) $
 * @since 28.12.2003
 */
public final class AttributeFilter {
    private static final String CONFIG_URL = "resource:dcm4chee-attribute-filter.xml";
    private static boolean loaded = false;
    private static HashMap patient = new HashMap();
    private static HashMap study = new HashMap();
    private static HashMap series = new HashMap();
    private static HashMap instance = new HashMap();
    private int[] tags = {};
    private int[] noCoercion = {};
    private int[] vrs = {};
    private final String tsuid;
    private final boolean exclude;
    private final boolean excludePrivate;
    private boolean noFilter = false;

    // Test Driver
    public static void main(String[] args) {
        AttributeFilterLoader.loadFrom(patient, study, series, instance, args[0]);
    }
    
    public static AttributeFilter getPatientAttributeFilter(String cuid)
    throws ConfigurationException {
        load();
        return getAttributeFilter(cuid, patient);
    }

    public static AttributeFilter getStudyAttributeFilter(String cuid)
    throws ConfigurationException {
        load();
        return getAttributeFilter(cuid, study);
    }

    public static AttributeFilter getSeriesAttributeFilter(String cuid)
    throws ConfigurationException {
        load();
        return getAttributeFilter(cuid, series);
    }
    
    public static AttributeFilter getInstanceAttributeFilter(String cuid)
    throws ConfigurationException {
        load();
        return getAttributeFilter(cuid, instance);
    }

    private static void load() throws ConfigurationException {
        if (!loaded) {
            AttributeFilterLoader.loadFrom(patient, study, series, instance, 
                    CONFIG_URL);            
            loaded = true;
        }
    }

    static AttributeFilter getAttributeFilter(String cuid, HashMap map) {
        AttributeFilter filter = (AttributeFilter) map.get(cuid);
        if (filter == null) {
            filter = (AttributeFilter) map.get(null);
        }
        return filter;
    }

    AttributeFilter(String tsuid, boolean exclude, boolean excludePrivate) {
        this.tsuid = tsuid;
        this.exclude = exclude;
        this.excludePrivate = excludePrivate;
    }
    
    final void setNoCoercion(int[] noCoercion) {
        this.noCoercion = noCoercion;
    }

    final void setTags(int[] tags) {
        this.tags = tags;
    }

    final int[] getTags() {
        return this.tags;
    }
    
    final void setVRs(int[] vrs) {
        this.vrs = vrs;
    }

    final int[] getVRs() {
        return this.vrs;
    }
    
    public final boolean isNoFilter() {
        return noFilter;
    }
         
    final void setNoFilter(boolean noFilter) {
        this.noFilter = noFilter;
    }
    
    final boolean isExclude() {
        return exclude;
    }
    
    public boolean isCoercionForbidden(int tag) {
    	return Arrays.binarySearch(noCoercion, tag) >= 0;
    }
    
    public final String getTransferSyntaxUID() {
        return tsuid;
    }

    public Dataset filter(Dataset ds) {
        return ds.subSet(tags, vrs, exclude, excludePrivate);
    }

}
