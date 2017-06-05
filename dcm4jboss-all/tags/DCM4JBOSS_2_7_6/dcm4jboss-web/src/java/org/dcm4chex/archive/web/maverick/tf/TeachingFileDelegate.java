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

package org.dcm4chex.archive.web.maverick.tf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.flow.ControllerContext;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TeachingFileDelegate {

	public static final String TF_ATTRNAME = "teachingFile";
	
    private static MBeanServer server;
	private static ObjectName keyObjectServiceName;
	private static ObjectName exportManagerServiceName;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static Logger log = Logger.getLogger( TeachingFileDelegate.class.getName() );

    
	
	public TeachingFileDelegate() {
	}
	
    public void init(ControllerContext ctx) throws Exception {
        if (keyObjectServiceName != null) return;
        server = MBeanServerLocator.locate();
        String s = ctx.getServletConfig().getInitParameter("keyObjectServiceName");
        keyObjectServiceName = new ObjectName(s);
        s = ctx.getServletConfig().getInitParameter("exportManagerServiceName");
        exportManagerServiceName = new ObjectName(s);
    }
	
    /**
     * Move one ore more model instances to another parent.
     * <p>
     * The move is restricted with following rules:<br>
     * 1) the destinations model type must be the same as the source parents model type.<br>
     * 2) the destination must be different to the source parent.<br>
     * 3) all source models must have the same parent.<br>
     * 4) the destination and the source parent must have the same parent.<br>
     * 
     * @return the name of the next view.
     */
    public boolean exportTF(TFModel tfModel) throws Exception{
    	if ( tfModel.getNumberOfInstances() < 1 ) {
    		throw new IllegalArgumentException("No Instances selected!");
    	}
    	Collection items = getObserverContextItems(getObserverPerson(tfModel.getUser()));
    	if (tfModel.getManifestModel().isUseManifest()){
    		Dataset basicDS = lookupContentManager().getInstanceInfo( 
    				tfModel.getInstances().iterator().next().toString(), false);
    		Dataset manifestSR = tfModel.getManifestModel().getSR(basicDS, items);
    		storeExportSelection( manifestSR );
    		tfModel.getInstances().add( manifestSR.getString(Tags.SOPInstanceUID));
    	}
    	Dataset rootInfo = getRootInfo(tfModel);
    	List contentItems = getContentItems( tfModel );
    	contentItems.addAll(items);
    	Dataset keyObjectDS = getKeyObject( tfModel.getInstances(), rootInfo, contentItems);
    	storeExportSelection( keyObjectDS );
    	return true;
    }
    
	private Collection getObserverContextItems(String personName) {
		Dataset ds = dof.newDataset();
		ds.putCS(Tags.RelationshipType, "HAS OBS CONTEXT");
		ds.putCS(Tags.ValueType,"CODE");
		DcmElement cnSq = ds.putSQ(Tags.ConceptNameCodeSeq);
		Dataset cnDS = cnSq.addNewItem();
		cnDS.putSH(Tags.CodeValue, "121005");
		cnDS.putSH(Tags.CodingSchemeDesignator, "DCM");
		cnDS.putLO(Tags.CodeMeaning, "ObserverType");
		DcmElement ccSq = ds.putSQ(Tags.ConceptCodeSeq);
		Dataset ccDS = ccSq.addNewItem();
		ccDS.putSH(Tags.CodeValue, "121006");
		ccDS.putSH(Tags.CodingSchemeDesignator, "DCM");
		ccDS.putLO(Tags.CodeMeaning, "Person");

		Dataset ds1 = dof.newDataset();
		ds1.putCS(Tags.RelationshipType, "HAS OBS CONTEXT");
		ds1.putCS(Tags.ValueType,"PNAME");
		DcmElement cnSq1 = ds1.putSQ(Tags.ConceptNameCodeSeq);
		Dataset cnDS1 = cnSq1.addNewItem();
		cnDS1.putSH(Tags.CodeValue, "121008");
		cnDS1.putSH(Tags.CodingSchemeDesignator, "DCM");
		cnDS1.putLO(Tags.CodeMeaning, "Person Observer Name");
		ds1.putPN(Tags.PersonName, personName);
		ArrayList col = new ArrayList();
		col.add(ds);
		col.add(ds1);
		return col;
	}
    
    /**
	 * @param user
	 * @return
	 */
	public String getObserverPerson(String user) {
        try {
            return (String) server.invoke(exportManagerServiceName,
                    "getObserverPerson",
                    new Object[] {user},
                    new String[] {String.class.getName()});
        } catch (Exception e) {
            log.warn("Failed to get Observer person for user "+user+" ! Reason:"+ e.getCause());
            return null;
        }
	}

	private Dataset getBasicDS(Collection instances ) throws Exception {
    	String iuid = instances.iterator().next().toString();
    	return lookupContentManager().getInstanceInfo( iuid, false);
    }
	/**
	 * @param tfModel
	 * @return
	 */
	private List getContentItems(TFModel tfModel) {
		List items = new ArrayList();
		if( tfModel.selectedDelayReason() != null ) {
			Dataset delayReasonDS = DcmObjectFactory.getInstance().newDataset();
			delayReasonDS.putCS(Tags.RelationshipType,"HAS CONCEPT MOD");
			delayReasonDS.putCS( Tags.ValueType, "CODE");
			DcmElement cnSq = delayReasonDS.putSQ(Tags.ConceptNameCodeSeq);
			Dataset cnDS = cnSq.addNewItem();
			cnDS.putSH(Tags.CodeValue, "113011");
			cnDS.putSH(Tags.CodingSchemeDesignator, "DCM");
			cnDS.putLO(Tags.CodeMeaning, "Document Title Modifier");
			DcmElement codeSq = delayReasonDS.putSQ(Tags.ConceptCodeSeq);
			Dataset codeDS = codeSq.addNewItem();
			codeDS.putSH(Tags.CodeValue, tfModel.selectedDelayReasonCode());
			codeDS.putSH(Tags.CodingSchemeDesignator, tfModel.selectedDelayReasonDesignator());
			codeDS.putLO(Tags.CodeMeaning, tfModel.selectedDelayReason());
			items.add(delayReasonDS);
		}
		String disp = tfModel.getDisposition();
		if ( disp != null && disp.length() > 0) {
			Dataset dispositionDS = DcmObjectFactory.getInstance().newDataset();
			dispositionDS.putCS(Tags.RelationshipType,"CONTAINS");
			dispositionDS.putCS(Tags.ValueType,"TEXT");
			DcmElement cnSq1 = dispositionDS.putSQ(Tags.ConceptNameCodeSeq);
			Dataset cnDS1 = cnSq1.addNewItem();
			cnDS1.putSH(Tags.CodeValue, "113012");
			cnDS1.putSH(Tags.CodingSchemeDesignator, "DCM");
			cnDS1.putLO(Tags.CodeMeaning, "Key Object Description");
			dispositionDS.putLO(Tags.TextValue, disp );
			items.add(dispositionDS);
		}
		return items;
	}

	/**
	 * @param tfModel
	 * @return
	 */
	private Dataset getRootInfo(TFModel tfModel) {
		Dataset rootInfo = DcmObjectFactory.getInstance().newDataset();
    	DcmElement sq = rootInfo.putSQ(Tags.ConceptNameCodeSeq);
    	Dataset item = sq.addNewItem();
    	item.putSH(Tags.CodeValue,tfModel.selectedDocTitleCode());
    	item.putSH(Tags.CodingSchemeDesignator,tfModel.selectedDocTitleDesignator());
		item.putLO(Tags.CodeMeaning, tfModel.selectedDocTitle());
		return rootInfo;
	}

	/**
	 * @param stickyPatients
	 * @param stickyStudies
	 * @param stickySeries
	 * @param stickyInstances
	 * @return
	 */
	public Set getSelectedInstances(Set stickyPatients, Set stickyStudies, Set stickySeries, Set stickyInstances) {
		if ( stickyPatients.size() > 1 ) {
			throw new IllegalArgumentException("Instances from different patients are selected! Please check your selection.");
		}
		Set instances = new HashSet();
		if ( stickyPatients.size() == 1 ) {
			addPatientInstances(instances, Integer.valueOf(stickyPatients.iterator().next().toString()));
		}
		if ( stickyStudies.size() > 0 ) {
			for ( Iterator iter = stickyStudies.iterator() ; iter.hasNext() ; ) {
				addStudyInstances( instances, Integer.valueOf(iter.next().toString()) );
			}
		}
		if ( stickySeries.size() > 0 ) {
			for ( Iterator iter = stickySeries.iterator() ; iter.hasNext() ; ) {
				addSeriesInstances( instances, Integer.valueOf(iter.next().toString()) );
			}
		}
		if ( stickyInstances.size() > 0 ) {
			Set set = new HashSet();
			for ( Iterator iter = stickyInstances.iterator() ; iter.hasNext() ; ) {
				set.add( Integer.valueOf(iter.next().toString()));
			}
			try {
				Collection col = lookupContentManager().getSOPInstanceRefMacros(set);
				for ( Iterator iter = col.iterator() ; iter.hasNext() ; ) {
					addSOPInstanceRefs( instances, (Dataset) iter.next() );
				}
			} catch (Exception x) {
				log.error("Cant add selected instances ! :"+set,x);
				throw new IllegalArgumentException("Cant add selected instances!");
			}
		}

		return instances;
	}
	/**
	 * @param instances
	 * @param integer
	 */
	private void addSeriesInstances(Set instances, Integer seriesPk) {
		List l;
		try {
			l = lookupContentManager().listInstancesOfSeries(seriesPk.intValue());
			for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
				instances.add( ((Dataset) iter.next()).getString(Tags.SOPInstanceUID));
			}
		} catch (Exception x) {
			log.error("Cant add instances of series (pk="+seriesPk+")!",x);
			throw new IllegalArgumentException("Cant add instances of series!");
		}
	}
	
	/**
	 * @param integer
	 */
	private void addStudyInstances(Set instances, Integer studyPk) {
		try {
			Dataset ds = lookupContentManager().getSOPInstanceRefMacro(studyPk.intValue(), false);
			addSOPInstanceRefs( instances, ds );
		} catch (Exception x) {
			log.error("Cant add instances of study (pk="+studyPk+")!",x);
			throw new IllegalArgumentException("Cant add instances of study!");
		}
		
	}
	/**
	 * @param integer
	 */
	private void addPatientInstances(Set instances, Integer patPk) {
		try {
			List l = lookupContentManager().listStudiesOfPatient(patPk.intValue());
			for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
				addStudyInstances( instances, ((Dataset) iter.next()).getInteger(PrivateTags.StudyPk));
			}
		} catch (Exception x) {
			log.error("Cant add instances of patient (pk="+patPk+")!",x);
			throw new IllegalArgumentException("Cant add instances of patient!");
		}
	}

	private void addSOPInstanceRefs( Set instances, Dataset ds) {
		DcmElement refSerSq = ds.get(Tags.RefSeriesSeq);
		DcmElement refSopSq;
		Dataset seriesItem;
		for ( int i = 0 ; i < refSerSq.vm() ; i++) {
			seriesItem = refSerSq.getItem(i);
			refSopSq = seriesItem.get(Tags.RefSOPSeq);
			for ( int j = 0 ; j < refSopSq.vm() ; j++ ) {
				instances.add(refSopSq.getItem(j).getString(Tags.RefSOPInstanceUID));
			}
		}
	}
	
    public Dataset getKeyObject(Collection iuids, Dataset rootInfo, List contentItems) {
    	Object o = null;
        try {
            o = server.invoke(keyObjectServiceName,
                    "getKeyObject",
                    new Object[] { iuids, rootInfo, contentItems },
                    new String[] { Collection.class.getName(), Dataset.class.getName(), Collection.class.getName() });
        } catch (RuntimeMBeanException x) {
        	 log.warn("RuntimeException thrown in KeyObject Service:"+x.getCause());
        	throw new IllegalArgumentException(x.getCause().getMessage());
        } catch (Exception e) {
            log.warn("Failed to create Key Object:", e);
            throw new IllegalArgumentException("Error: KeyObject Service cant create manifest Key Object! Reason:"+e.getClass().getName());
        }
        return (Dataset) o;
    }

	/**
	 * @param keyObjectDS
	 * @param string
	 */
	private void storeExportSelection(Dataset keyObjectDS) throws Exception {
        try {
            server.invoke(exportManagerServiceName,
                    "storeExportSelection",
                    new Object[] { keyObjectDS, new Integer(0) },
                    new String[] { Dataset.class.getName(), int.class.getName() });
        } catch (Exception e) {
            log.warn("Failed to store Export Selection:", e);
            throw e;
        }
 	}
	
	protected Collection getConfiguredDispositions() throws Exception {
        try {
            return (Collection) server.invoke(exportManagerServiceName,
                    "listConfiguredDispositions",
                    new Object[] {},
                    new String[] {});
        } catch (Exception e) {
            log.warn("Failed to store Export Selection:", e);
            throw e;
        }
	}

	private ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        return home.create();
    }
	
}
