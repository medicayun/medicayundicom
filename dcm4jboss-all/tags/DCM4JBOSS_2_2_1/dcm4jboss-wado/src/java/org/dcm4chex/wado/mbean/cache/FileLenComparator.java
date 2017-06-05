/*
 * Created on 06.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.cache;

import java.io.File;
import java.util.Comparator;

/**
 * @author franz.willer
 *
 * Comparator that compares last modification timestamps of File objects.
 */
public class FileLenComparator implements Comparator {

	public FileLenComparator() {
		
	}

	/**
	 * Compares the modification time of two File objects.
	 * <p>
	 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer 
	 * as the first argument is less than, equal to, or greater than the second.
	 * <p>
	 * Throws an Exception if one of the arguments is null or not a File object.
	 *  
	 * @param arg0 	First argument
	 * @param arg1	Second argument
	 * 
	 * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
	 */
	public int compare(Object arg0, Object arg1) {
		File file1 = (File) arg0;
		File file2 = (File) arg1;
		return (int) ( file1.lastModified() - file2.lastModified() );
	}
	
}
