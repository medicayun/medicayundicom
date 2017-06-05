/*
 * Created on 20.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.dcm4chex.archive.ejb.interfaces.MediaDTO;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaList extends ArrayList {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4048791281930941750L;

	/**
	 * @param list
	 * @param limit
	 * @param offset
	 */
	public MediaList(Collection list) {
		
		this();
		Iterator iter = list.iterator();
		while ( iter.hasNext() ) {
			this.add( new MediaData( (MediaDTO) iter.next() ) );
		}
	}

	/**
	 * 
	 */
	public MediaList() {
		
		super();
	}

	public boolean add( MediaData media ) {
		return super.add( media );
	}
}
