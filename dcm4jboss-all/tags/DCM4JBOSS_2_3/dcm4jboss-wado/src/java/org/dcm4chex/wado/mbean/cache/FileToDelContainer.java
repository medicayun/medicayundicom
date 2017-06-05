/*
 * Created on 06.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author franz.willer
 *
 * Helper class to get files that should be deleted.
 * <p>
 * 
 */
public class FileToDelContainer {

	/** Holds a sorted list of File objects */
	private List list;
	
	/** Amount of Disk space that have to be deleted. */
	private long sizeToDel = 0;
	
	/** current size of all Files in this container. */
	private long currSize = 0;

	/** the comparator that is used to sort the list. (via last modification time). */
	private Comparator comparator= new FileLenComparator();
	
	/**
	 * Create a container that holds a list of files that should be deleted.
	 * <p>
	 * This container holds a list of the oldest (lastModified) files to free a minimum of 
	 * <code>minSizeToDel</code> bytes disk space.
	 * 
	 * @param dir 			Directory where the files should be deleted.
	 * @param minSizeToDel 	Minimum size in bytes that should be deleted.
	 */
	public FileToDelContainer( File dir, long minSizeToDel ) {
		list = new ArrayList();
		sizeToDel = minSizeToDel;
		searchDirectory( dir );
	}

	/**
	 * Search the given directory to select the files to delete.
	 * 
	 * @param dir
	 */
	public void searchDirectory(File dir) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for ( int i = 0, len = files.length ; i < len ; i++ ) {
			if ( files[i].isFile() ) {
				addFile( files[i] );
			} else {
				searchDirectory( files[i] );
			}
		}
		if ( currSize < sizeToDel ) Collections.sort( list, comparator );// list is not sorted, because sizeToDel not reached! 
	}
	
	/**
	 * Returns the List of files in this container.
	 * 
	 * @return
	 */
	public List getFilesToDelete(){
		return list;
	}
	
	/**
	 * Add a file to this container.
	 * <p>
	 * <DL>
	 * <DT>The given file is added if:</DT>
	 * <DD>1) All files in this container doesnt reach <code>sizeToDel</code>, or</DD>
	 * <DD>2) If <code>file</code> is newer than the oldest file in the list.</DD>
	 * </DL>
	 *  
	 * @param file File to add.
	 * 
	 * @return true if this container is changed.
	 */
	public boolean addFile( File file ) {
		if ( file == null ) return false;
		boolean ret = false;
		if ( currSize < sizeToDel ) { // add until sizeToDel is reached.
			ret = list.add( file );
			currSize += file.length();
			if ( currSize >= sizeToDel ) Collections.sort( list, comparator); //sort now to have correct order for replace
		} else {
			File last = (File) list.get( list.size()-1 );
			if ( file.lastModified() < last.lastModified() ) { //replace (is newer than the oldest from list) 
				if ( currSize - last.length() + file.length() >= sizeToDel ) { //remove last only if sizeToDel is fullfilled with new file.
					list.remove( last );
					currSize -= last.length();
				}
				ret = list.add( file );
				currSize += file.length();
				Collections.sort( list, comparator);//we dont know if the new file is the oldest.
			}
		}
		return ret;
	}
	
}
