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

package org.dcm4chex.archive.ejb.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.session.MediaComposerBean;

/**
 * @author franz.willer
 *
 * Class to collect instances to studies.
 * <p>
 * 
 */
public class InstanceCollector {

	/** Holds the studies in a list. */
	private List studies = new ArrayList();
	private static Logger log = Logger.getLogger( MediaComposerBean.class.getName() );
	
	private boolean isSorted = false;
	
	private long totalSize = 0;
	
	private Comparator comparator = new SizeComparator();
	
	/**
	 * Adds (and collect) an instance to this collector.
	 * <p>
	 * Get the studyPk that refers to the instance and put the instance to the container for this study.
	 * 
	 * @param instance The instance to add(collect)
	 * 
	 * @return true if the instance can be added.
	 */
	public boolean add( InstanceLocal instance ) {
		isSorted = false; //invalidate sorted state if a new instance is added.
		Long studyPk = instance.getSeries().getStudy().getPk();
    	InstanceContainer container = new InstanceContainer( studyPk );//use this empty container to check if a container for studyPk is already in list.
    	int pos = studies.indexOf( container );
    	if ( pos != -1 ) {
    		container = (InstanceContainer) studies.get( pos );//OK is in list -> set container to container in list.
    	} else {
    		studies.add( container );// Not in container -> add new container to list.
    	}
    	return container.add( instance );//add the instance to the container.
	}
	
	private void removeStudy( int pos ) {
		if ( pos < 0 || pos >= studies.size() ) return;
		totalSize -= ( (InstanceContainer) studies.get(pos) ).getStudySize();
		studies.remove( pos );
	}
	
	/**
	 * Returns the number studies the instances are collected.
	 * 
	 * @return Number of studies in this collector.
	 */
	public int getNumberOfStudies() {
		return studies.size();
	}
	
	/**
	 * Returns the size of all instances in this container.
	 * 
	 * @return Returns the totalSize.
	 */
	public long getTotalSize() {
		return totalSize;
	}
	
	/**
	 * Return the InstanceContainer with the greatest study.
	 * 
	 * @return largest InstanceContainer or null if this container is empty.
	 */
	public InstanceContainer getLargestStudy() {
		if ( studies.size() < 1 ) return null;
		if ( ! isSorted ){
			Collections.sort( studies, comparator );
			isSorted = true;
		}
		return (InstanceContainer) studies.get( studies.size()-1 );
	}

	/**
	 * Return the InstanceContainer with the smallest study.
	 * 
	 * @return smallest InstanceContainer or null if this container is empty.
	 */
	public InstanceContainer getSmallestStudy() {
		if ( studies.size() < 1 ) return null;
		if ( ! isSorted ){
			Collections.sort( studies, comparator );
			isSorted = true;
		}
		return (InstanceContainer) studies.get( 0 );
	}
	
	/**
	 * Collect InstanceContainer to the given list for a given max size.
	 * <p>
	 * Use a simple algo: starts with largest entry and fill until maxSize is reached.
	 * <p>
	 * Removes the entries from this container if collected.
	 * <p>
	 * if parameter list is null, a NullPointerException is thrown!
	 * 
	 * @param maxSize	The max (file)size that the list is allowed to contain.
	 * 
	 * @return List of InstanceContainer or null if there is no entry smaller than maxSize.
	 */
	public long collectInstancesForSize( List list, long maxSize ) {
		if ( log.isDebugEnabled() ) log.debug("collect Instances for Size:"+maxSize);
		
		if ( studies.size() < 1 ) return 0L;

		if ( ! isSorted ){
			Collections.sort( studies, comparator );
			isSorted = true;
		}
		long fileSize = 0;
		int pos = Collections.binarySearch( studies, getCompareInstanceContainer( maxSize ), comparator );
		if ( pos >= 0 ) { //exactly hit -> return only found study!
			list.add( studies.get(pos) );
			removeStudy( pos );
			fileSize = ((InstanceContainer) list.get(0)).getStudySize();
		} else { // we have found the insertion point(the index of the first element greater than the key, or list.size()) coded as negative value
			pos = (pos * -1) - 2; // index of last element < maxSize (ret value of binarySearch:(-(insertion point) - 1) ).
			if ( pos < 0 ) { //no element < maxSize in list!
				if ( log.isDebugEnabled() ) log.debug("No element < maxSize in list!!! maxSize:"+maxSize);
				return -1L;//return -1 to indicate that no studies in the list with studySize <= maxSize!
			}
			InstanceContainer c = (InstanceContainer) studies.get(pos);
			fileSize = 0;
			if ( log.isDebugEnabled() ) log.debug("fileSize:"+fileSize+" StudySize:"+c.getStudySize()+" maxSize:"+maxSize);
			while ( (fileSize + c.getStudySize() ) <= maxSize ) { //fill list until maxSize is reached.
				if ( log.isDebugEnabled() ) log.debug("study found to assign for media:"+c);
				list.add( c );
				fileSize += c.getStudySize();
				removeStudy( pos );
				pos--;
				if ( pos < 0 ) break;//first (lowest size) element is added.
				c = (InstanceContainer) studies.get(pos);
				if ( log.isDebugEnabled() ) log.debug("fileSize:"+fileSize+" StudySize:"+c.getStudySize()+" maxSize:"+maxSize);
			}
		}
		return fileSize;
	}
	
	/**
	 * Splits a (large) InstanceContainer into two ore more InstanceContainer.
	 * <p>
	 * Use a simple algo: starts with largest instance and fill until maxSize is reached.
	 * <p>
	 * This algo is also unsafe if instances are greater than maxSize.
	 * 
	 * @param container The large InstanceContainer.
	 * @param maxSize	The max size of an InstanceContainer.
	 * @param log
	 * 
	 * @return A list of InstanceContainer.
	 */
	public List split( InstanceContainer container, long maxSize ) {
		List list = new ArrayList();
		InstanceContainer cNew = new InstanceContainer( container.getStudyPk() );
		List instances = container.getInstances();
		Collections.sort( instances, comparator );
		int pos = instances.size() - 1;//point to largest instance;
		long currSize = 0;
		for ( int i = pos ; i>=0 ; i-- ) { 
			InstanceLocal instance = (InstanceLocal) instances.get(i);
			currSize += getInstanceSize( instance );
			if ( currSize > maxSize ) {
				list.add( cNew );
				cNew = new InstanceContainer( container.getStudyPk() );
				currSize = getInstanceSize( instance );
			}
			cNew.add( instance );
		}
		list.add( cNew );
		removeStudy( studies.indexOf( container ) );
		return list;
	}

	/**
	 * Returns the size of the given instance.
	 * <p>
	 * If the instance contains more than one file, the size of the latest (with highest pk) file is used.
	 *  
	 * @param instance The instance
	 * 
	 * @return The file size of the instance.
	 */
	public long getInstanceSize(InstanceLocal instance) {
		Collection col = instance.getFiles();
		if ( col.size() == 1 ) { FileLocal l;
			return ( (FileLocal) col.iterator().next() ).getFileSize();
		} else {
			long size = 0;
			int pk = Integer.MIN_VALUE;
			Iterator iter = col.iterator();
			FileLocal file;
			while ( iter.hasNext() ) {
				file = (FileLocal) iter.next();
				if ( file.getPk().intValue() > pk ) {
					pk = file.getPk().intValue();
					size = file.getFileSize();
				}
			}
			return size;
		}
	}
	
	/**
	 * Returns a InstanceContainer instance for comparing file size.
	 * <p>
	 * The created InstanceContainer has studyId=-1, no instances and the given study size.
	 * <p>
	 * This container can be used for Collections.binarySearch.
	 * 
	 * @param studySize The studySize to set.
	 * 
	 * @return A special InstanceContainer instance for comparison (binary search) reason.
	 */
	private InstanceContainer getCompareInstanceContainer( long studySize ) {
		InstanceContainer c = new InstanceContainer( new Long(-1) );
		c.studySize = studySize;
		return c;
	}
	
	/**
	 * 
	 * @author franz.willer
	 *
	 * Class to hold instances for a study.
	 * <p>
	 * 
	 */
	public class InstanceContainer {	
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 3617294532191269944L;
		
		/** Holds the study pk for this container. */
		private Long studyPk;
		/** current (file)size of this container */
		private long studySize = 0;
		/** List of instances */
		private List instances = new ArrayList();
		
		/** 
		 * Creates an InstanceContainer for given study 
		 * 
		 * @param studyPk The pk of the referenced study. 
		 */
		public InstanceContainer( Long studyPk ) {
			this.studyPk = studyPk;
		}

		/**
		 * @return Returns the studyPk.
		 */
		public Long getStudyPk() {
			return studyPk;
		}
		
		/**
		 * Returns current study size.
		 * 
		 * @return Returns the studySize;
		 */
		public long getStudySize() {
			return studySize;
		}
		
		/**
		 * Adds an instance to this container.
		 * <p>
		 * Checks if the given instance is from the study this container is refering.
		 * <p>
		 * compute the current studySize with the latest file object from the given instance.
		 * 
		 * @param instance Instance to add.
		 * 
		 * @return true if the instance is added, false otherwise.
		 */
		public boolean add( InstanceLocal instance ) {
			if ( ! studyPk.equals( instance.getSeries().getStudy().getPk() ) ) return false;
			if ( instances.add( instance ) ) {
				long instanceSize = getInstanceSize( instance );
				studySize += instanceSize;
				totalSize += instanceSize;
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Returns all instances of this container.
		 * 
		 * @return Returns the instances.
		 */
		public List getInstances() {
			return instances;
		}
		

		public boolean equals( Object o ) {
			if ( o == null || !(o instanceof InstanceContainer) ) return false;
			return ((InstanceContainer) o).getStudyPk() == getStudyPk();
		}
		
		public int hashCode() {
			return studyPk.hashCode();
		}
		
		public String toString() {
			return "Study:"+studyPk+" size:"+studySize+" nrOfInstances:"+instances.size();
		}
		
	} //end class InstanceContainer

	public class SizeComparator implements Comparator {

		public SizeComparator() {
		}

		/**
		 * Compares the study size of two InstanceContainer or two InstanceLocal objects.
		 * <p>
		 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer 
		 * as the first argument is less than, equal to, or greater than the second.
		 * <p>
		 * Throws an Exception if one of the arguments is null or neither a InstanceContainer or InstanceLocal object.<br>
		 * Also both arguments must be of the same type!
		 * <p>
		 * If arguments are of type InstanceLocal, the getInstanceSize Method of InstanceCollector is used to get filesize.
		 *  
		 * @param arg0 	First argument
		 * @param arg1	Second argument
		 * 
		 * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
		 */
		public int compare(Object arg0, Object arg1) {
			if ( arg0 instanceof InstanceContainer ) {
				InstanceContainer ic1 = (InstanceContainer) arg0;
				InstanceContainer ic2 = (InstanceContainer) arg1;
				return new Long( ic1.getStudySize() ).compareTo( new Long( ic2.getStudySize() ) );
				
			}
			InstanceLocal il1 = (InstanceLocal) arg0;
			InstanceLocal il2 = (InstanceLocal) arg1;
			return new Long ( getInstanceSize(il1) ).compareTo( new Long( getInstanceSize(il2) ) );
		}
		
	}// end class
	
}//end class InstanceCollector

