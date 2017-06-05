/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DAFormat;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.PrivateTags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1592 $ $Date: 2005-03-11 20:10:07 +0800 (周五, 11 3月 2005) $
 * @since 05.10.2004
 *
 */
public abstract class AbstractModel {

    public static final String DATE_FORMAT = "yyyy/MM/dd";

    public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private static final int DATETIME_FORMAT_LEN = DATETIME_FORMAT.length();

    private static DcmObjectFactory dof = DcmObjectFactory.getInstance();

    protected Dataset ds;
    
    private List childs = new ArrayList();
    private List childsPK = null;

    protected AbstractModel() {
        this.ds = dof.newDataset();
    }

    protected AbstractModel(Dataset ds) {
        if (ds == null) throw new NullPointerException();
        this.ds = ds;
    }
    
    public boolean update( Dataset dsNew ) {
    	if ( ds.getInt(PrivateTags.SeriesPk, -1) != dsNew.getInt(PrivateTags.SeriesPk, -1) ) {
    		return false;
    	}
    	this.ds = dsNew;
    	ds.setPrivateCreatorID(PrivateTags.CreatorID);
    	return true;
    }

    public String getSpecificCharacterSet() {
        return ds.getString(Tags.SpecificCharacterSet);
    }

    public void setSpecificCharacterSet(String value) {
        ds.putCS(Tags.SpecificCharacterSet, value);
    }

    public final Dataset toDataset() {
        return ds;
    }

    protected String getDate(int dateTag) {
        try {
	        final Date d = ds.getDate(dateTag);
	        return d == null ? null : new SimpleDateFormat(DATE_FORMAT).format(d);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected void setDate(int dateTag, String s) {
        if (s == null || s.length() == 0) {
            ds.putDA(dateTag);
        } else {
            try {
                Date date = new SimpleDateFormat(DATE_FORMAT).parse(s);
                ds.putDA(dateTag, date);
            } catch (ParseException e) {
                throw new IllegalArgumentException(s);
            }
        }
    }

    protected String getDateTime(int dateTag, int timeTag) {
        try {
	        final Date d = ds.getDateTime(dateTag, timeTag);
	        if (d == null) return null;
	        String s = new SimpleDateFormat(DATETIME_FORMAT).format(d);
	        while (s.endsWith("00"))
	            s = s.substring(0, s.length() - 3);
	        return s;
	    } catch (IllegalArgumentException e) {
	        return null;
	    }
    }

    protected void setDateTime(int dateTag, int timeTag, String s) {
        int l;
        if (s == null || (l = s.length()) == 0) {
            ds.putDA(dateTag);
            ds.putTM(timeTag);
        } else {
            final int l1 = Math.min(l, DATETIME_FORMAT_LEN);
            final String f = DATETIME_FORMAT.substring(0, l1);
            try {
                Date date = new SimpleDateFormat(f).parse(s.substring(0, l1));
                ds.putDA(dateTag, date);
                if (l <= 13)
                    ds.putTM(timeTag);
                else
                    ds.putTM(timeTag, date);
            } catch (ParseException e) {
                throw new IllegalArgumentException(s);
            }
        }
    }

    protected String getDateRange(int dateTag) {
        try {
	        Date[] range = ds.getDateRange(dateTag);
	        if (range == null || range.length == 0)
	            return null;
	        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
	        StringBuffer sb = new StringBuffer();
	        if (range[0] != null)
	            sb.append(f.format(range[0]));
	        sb.append('-');
	        if (range[1] != null)
	            sb.append(f.format(range[1]));
	        return sb.toString();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected void setDateRange(int dateTag, String s) {
        if (s == null || s.length() == 0) {
            ds.putDA(dateTag);
        } else {            
            String[] s2 = StringUtils.split(s, '-');
            DAFormat f = new DAFormat();
            String range = toDA(s2[0], false, f) + '-' + toDA(s2[s2.length-1], true, f);
            ds.putDA(dateTag, range);
	    }
	
	}

    private String toDA(String s, boolean end, DAFormat f) {
        if (s.length() == 0)
            return "";
        Calendar cal = Calendar.getInstance();
        cal.clear();
        String[] s3 = StringUtils.split(s, '/');
        cal.set(Calendar.YEAR, Integer.parseInt(s3[0]));
        if (s3.length > 1 && s3[1].length() > 0) {
            cal.set(Calendar.MONTH, Integer.parseInt(s3[1])-1);
            if (s3.length > 2 && s3[2].length() > 0) {
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s3[2]));
            } else {
                if (end) {
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.DATE, -1);
                }
            }
        } else {
            if (end) {
                cal.add(Calendar.YEAR, 1);            
                cal.add(Calendar.DATE, -1);
            }
        }
        return f.format(cal.getTime());
    }
    
    /**
     * Returns the childs of this model as List.
     * 
     * @return List of childs.
     */
    public List listOfChilds() {
    	return childs;
    }
    
    /**
     * Set the childs for this model.
     * <p>
     * This method should not be used directly! Use dedicated method of implementation class instead.
     * <p>
     * If param is null, return without any changes!
     * <p>
     * Resets <code>childsPK</code> to obtain new child pk's with <code>containsPK</code> method.
     * 
     * @param list New list of childs.
     */
    public void setChilds( List list ) {
    	if ( list != null ) {
    		childs = list;
    		childsPK = null;
    	}
    }
    
    /**
     * Checks if this model contains given child.
     * 
     * @param model A childs model
     * @return true if this model contains given child.
     */
    public boolean contains( AbstractModel model ) {
    	return childs.contains( model );
    }
    
    /**
     * Checks if this model contains a child with given pk.
     * <p>
     * Use carefully, because pk's are only unique within a model type!
     * 
     * @param pk The childs pk
     * @return 
     */
    public boolean containsPK( int pk ) {
    	return childPKs().contains( new Integer( pk ) );
    }
    
    /**
     * Gets the list of all clients pk's.
     * <p>
     * This method is used primary in method <code>containsPK</code>
     * 
     * @return List of childs pk's.
     */
    public List childPKs(){
    	if ( childsPK != null ) return childsPK;
    	childsPK = new ArrayList();
    	Iterator iter = listOfChilds().iterator();
    	while ( iter.hasNext() ) {
    		childsPK.add( new Integer( ((AbstractModel) iter.next() ).getPk() ) );
    	}
    	return childsPK;
    }
    
    /**
     * Get the pk of this model instance.
     * 
     * @return pk
     */
    public abstract int getPk();
    
}