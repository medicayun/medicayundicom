/*
 * Created on 10.03.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.xds.mbean.store;

import java.io.File;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XDSFile {

	private final File file;
	private final String hashString;
	
	public XDSFile( File file, byte[] hash) {
		this.file = file;
		StringBuffer sb = new StringBuffer();
		String h;
   		for(int i=0 ; i < hash.length ; i++) {
        	h = Integer.toHexString(hash[i] & 0xff);
        	if(h.length() == 1) h = "0" + h;
        	sb.append(h);
    	}
   		hashString = sb.toString();
	}
	
	public File getFile() {
		return file;
	}
	
	public String getHash() {
		return hashString;
	}
	
	public long getFileSize() {
		return file.length();
	}
	
	public String toString() {
		return file.toString();
	}
}
