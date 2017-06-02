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

package org.dcm4chex.webview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * Container for DicomObject objects.
 * <p>
 * Objects are automatically sorted for StudyIUID and SeriesIUID.
 * <p>
 * Basically this class is designed to get objects per series of a single study.
 * <p>
 * It also provides a study level iterator to enable selection of studies when a query result 
 * contains more than one study.<br/>
 * In such a case the series methods (<code>getSeriesMap, iterateSeries, countSeries</code>) throws
 * an IllegalStateException!
 * 
 * @author franz.willer@agfa.com
 * @version $Revision$ $Date$
 * @since 07.06.2007
 *
 */
public class InstanceContainer {
    private Map mapStudies = new HashMap();

    private String lastStudyIuid;
    private String lastSeriesIuid;
    private Map lastSeriesMap;
    private Collection lastInstanceList;

    /**
     * Creates a new empty container.
     *
     */
    public InstanceContainer() {
    }
    
    /**
     * Creates a new container and add given list of DicomObject.
     * @param instances
     */
    public InstanceContainer( List instances ) {
        add(instances);
    }
    
    /**
     * Add given list of DicomObject.
     *
     * @param instances List of DicomObject
     */
    public void add(List instances) {
        if ( instances == null || instances.isEmpty() ) return;
        for ( Iterator iter = instances.iterator() ; iter.hasNext() ;) {
            add( (DicomObject) iter.next() );
        }
    }
    
    /**
     * Add a single DicomObject.
     * 
     * @param instance The DicomObject to add.
     */
    public void add(DicomObject instance) {
         if ( !instance.getString(Tag.SeriesInstanceUID).equals(lastSeriesIuid) ) {
            lastSeriesIuid = instance.getString(Tag.SeriesInstanceUID);
            if ( !instance.getString(Tag.StudyInstanceUID).equals(lastStudyIuid) ) {
                lastStudyIuid = instance.getString(Tag.StudyInstanceUID);
                lastSeriesMap = (Map) mapStudies.get(lastStudyIuid);
            }
            if ( lastSeriesMap == null ) {
                lastSeriesMap = new LinkedHashMap();
                mapStudies.put(lastStudyIuid, lastSeriesMap);
                lastInstanceList = null;
            } else {
                lastInstanceList = (Set) lastSeriesMap.get(lastSeriesIuid);
            }
            if ( lastInstanceList == null ) {
                lastInstanceList = new ArrayList();
                lastSeriesMap.put(lastSeriesIuid, lastInstanceList);
            }
        }
        lastInstanceList.add(instance);
    }

    /**
     * Check if this container is empty.
     * 
     * @return true if this container doesn't contain any object.
     */
    public boolean isEmpty() {
        return mapStudies.isEmpty();
    }
    
    /**
     * Get number of studies in this container.
     * <p/>
     * This should be usually always 1!
     * 
     * @return Number of studies.
     */
    public int countStudies() {
        return mapStudies.size();
    }
    
    /**
     * Get number of series in this container.
     * <p/>
     * Throws IllegalStateException if countStudies() > 1;
     * 
     * @return Number of series.
     */
    public int countSeries() {
        return getSeriesMap().size();
    }

    /**
     * return the Map of series.
     * <p>
     * key: Series Instance UID<br/>
     * value: List of DicomObject.
     * 
     * @return The series Map
     */
    public Map getSeriesMap() {
        if ( mapStudies.size() > 1 ) {
            throw new IllegalStateException("This InstanceContainer contains instances of more than one study!");
        }
        return mapStudies.isEmpty() ? new HashMap() : (Map) mapStudies.values().iterator().next();
    }
    
    /**
     * Get an Iterator over series in this container.
     * <p>
     * Each object is a List of DicomObject
     * @return
     */
    public Iterator iterateSeries() {
        return getSeriesMap().values().iterator();
    }
 
    /**
     * Get an Iterator over studies in this container.
     * <p>
     * Each object is a Map containing List of DicomObject (value) per Series (key=SeriesIUID) representing a study.
     * 
     * @return
     */
    public Iterator iterateStudies() {
        return mapStudies.values().iterator();
    }
}
