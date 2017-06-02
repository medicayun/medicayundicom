/*
 * Created on 17.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4che.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.or.ObjectRenderer;
import org.dcm4che.data.Dataset;

/**
 * @author franz.willer
 *
 * Log4j renderer for Dataset objects.
 * <p>
 * This renderer is used to improve performance for DEBUG level. 
 * <p>
 * <DL>
 * <DT>Configuration in log4j.xml (first tag in configuration tag!):</DT>
 * <DD>   &lt;renderer renderedClass="org.dcm4che.data.Dataset" renderingClass="org.dcm4che.log.DatasetRenderer" /&gt;</DD>
 * </DL>
 */
public class DatasetRenderer implements ObjectRenderer {

	private static Map dumpParam = new Hashtable();
	private static int excludeValueLengthLimit = 128;
	
	/**
	 * Configures the DatasetRenderer with default dump parameters.
	 * <p>
	 * <dl>
	 * <dt>dump parameters:"</dt>
	 * <dd>  maxlen: max len of a single line. (default=128)</dd>
	 * <dd>  vallen: max len of the (text) value. (default=64)</dd>
	 * <dd>  prefix: line prefix, used to indent the lines.(default='\t')</dd>
	 * <dd>  excludeValueLengthLimit:used to exclude attributes with values greater this limit. (default=128)</dd>
	 * </dl>
	 * This parameters can be defined in the system parameter 'DatasetRenderer.dumpParam':
	 * <p>
	 * &lt;maxlen&gt;,&lt;vallen&gt;,&lt;prefix&gt;,&lt;excludeValueLengthLimit&gt;
	 *
	 */
	static {
		String cfg = System.getProperty("DatasetRenderer.dumpParam", "128,64,\t,128");
		StringTokenizer st = new StringTokenizer( cfg, ",");
		if ( st.hasMoreTokens() ) dumpParam.put("maxlen", Integer.valueOf( st.nextToken() ));
		if ( st.hasMoreTokens() ) dumpParam.put("vallen", Integer.valueOf( st.nextToken() ));
		if ( st.hasMoreTokens() ) dumpParam.put("prefix", st.nextToken() );
		if ( st.hasMoreTokens() ) excludeValueLengthLimit = Integer.parseInt( st.nextToken() );
	}
	/**
	 * Returns dump parameter maxlen, vallen and prefix in a Map.
	 * 
	 * @return dump parameter.
	 */
	public static Map getDumpParam() {
		return dumpParam;
	}
	
	/**
	 * Returns the limit of value length to exclude attributes.
	 * <p>
	 * If an attribute value exceeds this limit, it will be excluded from the dump.
	 * 
	 * @param limit Number of characters from the String representation of the value.
	 */
	public static void setExcludeValueLengthLimit( int limit ) {
		excludeValueLengthLimit = limit;
	}
	/**
	 * Render a Dataset object.
	 * 
	 * @see org.apache.log4j.or.ObjectRenderer#doRender(java.lang.Object)
	 */
	public String doRender(Object arg0) {
		StringWriter w = new StringWriter();
        try {
        	Dataset ds = (Dataset) arg0;
            ds.dumpDataset(w, dumpParam, excludeValueLengthLimit);
            return w.toString();
        } catch (Exception e) {
        	e.printStackTrace( new PrintWriter( w ));
            return "Failed to dump dataset:" + w;
        }
	}

}
