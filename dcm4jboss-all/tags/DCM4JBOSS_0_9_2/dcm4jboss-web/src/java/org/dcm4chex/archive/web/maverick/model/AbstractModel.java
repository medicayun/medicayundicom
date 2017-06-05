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
import java.util.Calendar;
import java.util.Date;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.DAFormat;
import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 05.10.2004
 *
 */
public abstract class AbstractModel {

    public static final String DATE_FORMAT = "yyyy/MM/dd";

    public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private static final int DATETIME_FORMAT_LEN = DATETIME_FORMAT.length();

    private static DcmObjectFactory dof = DcmObjectFactory.getInstance();

    protected final Dataset ds;

    protected AbstractModel() {
        this.ds = dof.newDataset();
    }

    protected AbstractModel(Dataset ds) {
        if (ds == null) throw new NullPointerException();
        this.ds = ds;
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
        final Date d = ds.getDate(dateTag);
        return d == null ? null : new SimpleDateFormat(DATE_FORMAT).format(d);
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
        final Date d = ds.getDateTime(dateTag, timeTag);
        if (d == null) return null;
        String s = new SimpleDateFormat(DATETIME_FORMAT).format(d);
        while (s.endsWith("00"))
            s = s.substring(0, s.length() - 3);
        return s;
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
}