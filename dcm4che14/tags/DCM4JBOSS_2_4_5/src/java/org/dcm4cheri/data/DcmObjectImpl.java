/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4cheri.data;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.DTFormat;
import org.dcm4cheri.util.StringUtils;
import org.xml.sax.helpers.DefaultHandler;

/** 
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since March 2002
 * @version $Revision: 3888 $ $Date: 2005-04-28 20:53:02 +0800 (周四, 28 4月 2005) $
 */
abstract class DcmObjectImpl implements DcmObject {

    static UIDDictionary DICT =
        DictionaryFactory.getInstance().getDefaultUIDDictionary();

    /**  Description of the Field */
    protected final static Logger log = Logger.getLogger(DcmObjectImpl.class);

    /**  Description of the Field */
    protected ArrayList list = new ArrayList();
    private final static int MIN_TRUNCATE_STRING_LEN = 16;

    /**
     *  Gets the dcmHandler attribute of the DcmObjectImpl object
     *
     * @return    The dcmHandler value
     */
    public DcmHandler getDcmHandler() {
        return new DcmObjectHandlerImpl(this);
    }

    /**
     *  Gets the sAXHandler attribute of the DcmObjectImpl object
     *
     * @return    The sAXHandler value
     */
    public DefaultHandler getSAXHandler() {
        return new SAXHandlerAdapter(getDcmHandler());
    }

    public DefaultHandler getSAXHandler2(File basedir) {
        return new SAXHandlerAdapter2(getDcmHandler(), basedir);
    }
    
    /**
     *  Gets the privateCreatorID attribute of the DcmObjectImpl object
     *
     * @return    The privateCreatorID value
     */
    public String getPrivateCreatorID() {
        return null;
    }

    /**
     *  Sets the privateCreatorID attribute of the DcmObjectImpl object
     *
     * @param  privateCreatorID  The new privateCreatorID value
     */
    public void setPrivateCreatorID(String privateCreatorID) {
        throw new UnsupportedOperationException();
    }

    /**
     *  Gets the charset attribute of the DcmObjectImpl object
     *
     * @return    The charset value
     */
    public Charset getCharset() {
        return null;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public int size() {
        return list.size();
    }

    /**
     *  Gets the empty attribute of the DcmObjectImpl object
     *
     * @return    The empty value
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**  Description of the Method */
    public void clear() {
        list.clear();
    }

    public void shareElements() {
        synchronized (list) {
            final int size = list.size();
            for (int i = 0; i < size; ++i)
                list.set(i, ((DcmElement) list.get(i)).share());
        }
    }

    private int indexOf(int tag) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            DcmElementImpl midVal = (DcmElementImpl) list.get(mid);
            long cmp = (midVal.tag & 0xffffffffL) - (tag & 0xffffffffL);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return - (low + 1); // key not found
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public boolean contains(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return false;
            }
            if (tag == 0) {
                return false;
            }
        }
        return indexOf(tag) >= 0;
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public int vm(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return -1;
            }
            if (tag == 0) {
                return -1;
            }
        }
        int index = indexOf(tag);
        return index >= 0 ? ((DcmElement) list.get(index)).vm() : -1;
    }

    private int adjustPrivateTag(int tag, boolean create)
        throws DcmValueException {
        String creatorID = getPrivateCreatorID();
        // no adjustments, if creatorID not set
        if (creatorID == null) {
            return tag;
        }
        int gr = tag & 0xffff0000;
        int el = 0x10;
        int index = indexOf(gr | el);
        if (index >= 0) {
            DcmElement elm = (DcmElement) list.get(index);
            while (++index < list.size()) {
                if (creatorID.equals(elm.getString(getCharset()))) {
                    return gr | (el << 8) | (tag & 0xff);
                }
                elm = (DcmElement) list.get(index);
                if (elm.tag() != (gr | ++el)) {
                    break;
                }
            }
        }
        if (!create) {
            return 0;
        }
        doPut(StringElement.createLO(gr | el, creatorID, getCharset()));
        return gr | (el << 8) | (tag & 0xff);
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement get(int tag) {
        if (Tags.isPrivate(tag)) {
            try {
                tag = adjustPrivateTag(tag, false);
            } catch (DcmValueException e) {
                log.warn("Could not access Creator ID", e);
                return null;
            }
            if (tag == 0) {
                return null;
            }
        }
        int index = indexOf(tag);
        return index >= 0 ? (DcmElement) list.get(index) : null;
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement remove(int tag) {
        synchronized (list) {
            int index = indexOf(tag);
            return index >= 0 ? (DcmElement) list.remove(index) : null;
        }
    }

    /**
     *  Gets the byteBuffer attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The byteBuffer value
     */
    public ByteBuffer getByteBuffer(int tag) {
        DcmElement e = get(tag);
        return e != null ? e.getByteBuffer() : null;
    }

    /**
     *  Gets the string attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The string value
     */
    public String getString(int tag, String defVal) {
        return getString(tag, 0, defVal);
    }

    /**
     *  Gets the string attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The string value
     */
    public String getString(int tag) {
        return getString(tag, 0, null);
    }

    /**
     *  Gets the string attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The string value
     */
    public String getString(int tag, int index) {
        return getString(tag, index, null);
    }

    /**
     *  Gets the string attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The string value
     */
    public String getString(int tag, int index, String defVal) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return defVal;
        }
        try {
            return el.getString(index, getCharset());
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    /**
     *  Gets the strings attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The strings value
     */
    public String[] getStrings(int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }
        try {
            return el.getStrings(getCharset());
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the boundedString attribute of the DcmObjectImpl object
     *
     * @param  maxLen  Description of the Parameter
     * @param  tag     Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The boundedString value
     */
    public String getBoundedString(int maxLen, int tag, String defVal) {
        return getBoundedString(maxLen, tag, 0, defVal);
    }

    /**
     *  Gets the boundedString attribute of the DcmObjectImpl object
     *
     * @param  maxLen  Description of the Parameter
     * @param  tag     Description of the Parameter
     * @return         The boundedString value
     */
    public String getBoundedString(int maxLen, int tag) {
        return getBoundedString(maxLen, tag, 0, null);
    }

    /**
     *  Gets the boundedString attribute of the DcmObjectImpl object
     *
     * @param  maxLen  Description of the Parameter
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @return         The boundedString value
     */
    public String getBoundedString(int maxLen, int tag, int index) {
        return getBoundedString(maxLen, tag, index, null);
    }

    /**
     *  Gets the boundedString attribute of the DcmObjectImpl object
     *
     * @param  maxLen  Description of the Parameter
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The boundedString value
     */
    public String getBoundedString(
        int maxLen,
        int tag,
        int index,
        String defVal) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return defVal;
        }

        try {
            return el.getBoundedString(maxLen, index, getCharset());
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    /**
     *  Gets the boundedStrings attribute of the DcmObjectImpl object
     *
     * @param  maxLen  Description of the Parameter
     * @param  tag     Description of the Parameter
     * @return         The boundedStrings value
     */
    public String[] getBoundedStrings(int maxLen, int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }

        try {
            return el.getBoundedStrings(maxLen, getCharset());
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the integer attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The integer value
     */
    public Integer getInteger(int tag) {
        return getInteger(tag, 0);
    }

    /**
     *  Gets the integer attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The integer value
     */
    public Integer getInteger(int tag, int index) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return null;
        }

        try {
            return new Integer(el.getInt(index));
        } catch (DcmValueException e) {
            return null;
        }
    }

    public PersonName getPersonName(int tag) {
        return getPersonName(tag, 0);
    }

    public PersonName getPersonName(int tag, int index) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return null;
        }

        try {
            return el.getPersonName(index, getCharset());
        } catch (DcmValueException e) {
            return null;
        }
    }
    
    /**
     *  Gets the int attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The int value
     */
    public int getInt(int tag, int defVal) {
        return getInt(tag, 0, defVal);
    }

    /**
     *  Gets the int attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The int value
     */
    public int getInt(int tag, int index, int defVal) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return defVal;
        }

        try {
            return el.getInt(index);
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    /**
     *  Gets the ints attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The ints value
     */
    public int[] getInts(int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }

        try {
            return el.getInts();
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the tag attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The tag value
     */
    public int getTag(int tag, int defVal) {
        return getTag(tag, 0, defVal);
    }

    /**
     *  Gets the tag attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The tag value
     */
    public int getTag(int tag, int index, int defVal) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return defVal;
        }

        try {
            return el.getTag(index);
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    /**
     *  Gets the tags attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The tags value
     */
    public int[] getTags(int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }

        try {
            return el.getTags();
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the float attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The float value
     */
    public Float getFloat(int tag) {
        return getFloat(tag, 0);
    }

    /**
     *  Gets the float attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The float value
     */
    public Float getFloat(int tag, int index) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return null;
        }

        try {
            return new Float(el.getFloat(index));
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the float attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The float value
     */
    public float getFloat(int tag, float defVal) {
        return getFloat(tag, 0, defVal);
    }

    /**
     *  Gets the float attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The float value
     */
    public float getFloat(int tag, int index, float defVal) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return defVal;
        }

        try {
            return el.getFloat(index);
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    /**
     *  Gets the floats attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The floats value
     */
    public float[] getFloats(int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }

        try {
            return el.getFloats();
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the double attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The double value
     */
    public Double getDouble(int tag) {
        return getDouble(tag, 0);
    }

    /**
     *  Gets the double attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The double value
     */
    public Double getDouble(int tag, int index) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return null;
        }

        try {
            return new Double(el.getDouble(index));
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the double attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The double value
     */
    public double getDouble(int tag, double defVal) {
        return getDouble(tag, 0, defVal);
    }

    /**
     *  Gets the double attribute of the DcmObjectImpl object
     *
     * @param  tag     Description of the Parameter
     * @param  index   Description of the Parameter
     * @param  defVal  Description of the Parameter
     * @return         The double value
     */
    public double getDouble(int tag, int index, double defVal) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return defVal;
        }

        try {
            return el.getDouble(index);
        } catch (DcmValueException e) {
            return defVal;
        }
    }

    /**
     *  Gets the doubles attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The doubles value
     */
    public double[] getDoubles(int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }

        try {
            return el.getDoubles();
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the date attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The date value
     */
    public Date getDate(int tag) {
        return getDate(tag, 0);
    }

    /**
     *  Gets the date attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The date value
     */
    public Date getDate(int tag, int index) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return null;
        }

        try {
            return el.getDate(index);
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the dateRange attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The dateRange value
     */
    public Date[] getDateRange(int tag) {
        return getDateRange(tag, 0);
    }

    /**
     *  Gets the dateRange attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The dateRange value
     */
    public Date[] getDateRange(int tag, int index) {
        DcmElement el = get(tag);
        if (el == null || el.vm() <= index) {
            return null;
        }

        try {
            return el.getDateRange(index);
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the dates attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The dates value
     */
    public Date[] getDates(int tag) {
        DcmElement el = get(tag);
        if (el == null) {
            return null;
        }

        try {
            return el.getDates();
        } catch (DcmValueException e) {
            return null;
        }
    }

    /**
     *  Gets the dateTime attribute of the DcmObjectImpl object
     *
     * @param  dateTag  Description of the Parameter
     * @param  timeTag  Description of the Parameter
     * @return          The dateTime value
     */
    public Date getDateTime(int dateTag, int timeTag) {
        DcmElement date = get(dateTag);
        if (date == null || date.isEmpty()) {
            return null;
        }

        try {
            DcmElement time = get(timeTag);
            if (time == null || time.isEmpty()) {
                return date.getDate();
            }
            String dt = date.getString(null) + time.getString(null); 
            return new DTFormat().parse(dt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *  Gets the dateTimeRange attribute of the DcmObjectImpl object
     *
     * @param  dateTag  Description of the Parameter
     * @param  timeTag  Description of the Parameter
     * @return          The dateTimeRange value
     */
    public Date[] getDateTimeRange(int dateTag, int timeTag) {
        DcmElement time = get(timeTag);
        if (time == null || time.isEmpty()) {
            return getDateRange(dateTag);
        }
        DcmElement date = get(dateTag);
        if (date == null || date.isEmpty()) {
            return null;
        }
        try {
        	String[] dateRange = splitRange(date.getString(null));
        	String[] timeRange = splitRange(time.getString(null));
        	Date[] result = new Date[2];
        	DTFormat f = new DTFormat();
        	if (dateRange[0] != null) {
        		result[0] = f.parse(dateRange[0] 
        		          + (timeRange[0] == null ? "" : timeRange[0]));
        	}
        	if (dateRange[1] != null) {
        		result[1] = f.parse(dateRange[1] 
        		          + (timeRange[1] == null ? "235959.999" : timeRange[1]));
        	}
            return result; 
        } catch (ParseException e) {
            return null;
        } catch (DcmValueException e) {
            return null;
        }
    }

	private static String[] splitRange(String range) {
        int hypen = range.indexOf('-');
        String[] result = new String[2];
        if (hypen != 0) {
        	result[0] = hypen == -1 ? range : range.substring(0, hypen);
        }
        if (hypen != range.length() - 1) {
        	result[1] = hypen == -1 ? range : range.substring(hypen+1);
        }
		return result;
	}

	/**
     *  Gets the item attribute of the DcmObjectImpl object
     *
     * @param  tag  Description of the Parameter
     * @return      The item value
     */
    public Dataset getItem(int tag) {
        return getItem(tag, 0);
    }

    /**
     *  Gets the item attribute of the DcmObjectImpl object
     *
     * @param  tag    Description of the Parameter
     * @param  index  Description of the Parameter
     * @return        The item value
     */
    public Dataset getItem(int tag, int index) {
        DcmElement e = get(tag);
        if (e == null || e.vm() <= index) {
            return null;
        }

        return e.getItem(index);
    }

    /**
     *  Description of the Method
     *
     * @param  newElem  Description of the Parameter
     * @return          Description of the Return Value
     */
    protected DcmElement put(DcmElement newElem) {
        if (log.isDebugEnabled()) {
            log.debug("put " + newElem);
        }
        if ((newElem.tag() & 0xffff) == 0) {
            return newElem;
        }

        if (Tags.isPrivate(newElem.tag())) {
            try {
                ((DcmElementImpl) newElem).tag =
                    adjustPrivateTag(newElem.tag(), true);
            } catch (DcmValueException e) {
                log.warn("Could not access creator ID - ignore " + newElem, e);
                return newElem;
            }
        }
        return doPut(newElem);
    }

    private DcmElement doPut(DcmElement newElem) {
        synchronized (list) {
            final int size = list.size();
            final int newTag = newElem.tag();
            if (size == 0
                || (((DcmElementImpl) list.get(size - 1)).tag & 0xffffffffL)
                    < (newTag & 0xffffffffL)) {
                list.add(newElem);
            } else {
                int index = indexOf(newTag);
                if (index >= 0) {
                    list.set(index, newElem);
                } else {
                    list.add(- (index + 1), newElem);
                }
            }
            return newElem;
        }
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putAE(int tag) {
        return put(StringElement.createAE(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putAE(int tag, String value) {
        return put(
            value != null
                ? StringElement.createAE(tag, value)
                : StringElement.createAE(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putAE(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createAE(tag, values)
                : StringElement.createAE(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putAS(int tag) {
        return put(StringElement.createAS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putAS(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? StringElement.createAS(tag, value)
                : StringElement.createAS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putAS(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createAS(tag, values)
                : StringElement.createAS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putAT(int tag) {
        return put(ValueElement.createAT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putAT(int tag, int value) {
        return put(ValueElement.createAT(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putAT(int tag, int[] values) {
        return put(
            values != null
                ? ValueElement.createAT(tag, values)
                : StringElement.createAT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putAT(int tag, String value) {
        return value != null && value.length() != 0
            ? putAT(tag, Integer.parseInt(value, 16))
            : putAT(tag);
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putAT(int tag, String[] values) {
        if (values == null) {
            return putAT(tag);
        }
        int[] a = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            a[i] = Integer.parseInt(values[i], 16);
        }
        return putAT(tag, a);
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putCS(int tag) {
        return put(StringElement.createCS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putCS(int tag, String value) {
        return put(
            value != null
                ? StringElement.createCS(tag, value)
                : StringElement.createCS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putCS(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createCS(tag, values)
                : StringElement.createCS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putDA(int tag) {
        return put(StringElement.createDA(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putDA(int tag, Date value) {
        return put(
            value != null
                ? StringElement.createDA(tag, value)
                : StringElement.createDA(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putDA(int tag, Date[] values) {
        return put(
            values != null
                ? StringElement.createDA(tag, values)
                : StringElement.createDA(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag   Description of the Parameter
     * @param  from  Description of the Parameter
     * @param  to    Description of the Parameter
     * @return       Description of the Return Value
     */
    public DcmElement putDA(int tag, Date from, Date to) {
        return put(
            from != null
                || to != null
                    ? StringElement.createDA(tag, from, to)
                    : StringElement.createDA(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putDA(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? StringElement.createDA(tag, value)
                : StringElement.createDA(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putDA(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createDA(tag, values)
                : StringElement.createDA(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putDS(int tag) {
        return put(StringElement.createDS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putDS(int tag, float value) {
        return put(StringElement.createDS(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putDS(int tag, float[] values) {
        return put(
            values != null
                ? StringElement.createDS(tag, values)
                : StringElement.createDS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putDS(int tag, String value) {
        return put(
            value != null || value.length() != 0
                ? StringElement.createDS(tag, value)
                : StringElement.createDS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putDS(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createDS(tag, values)
                : StringElement.createDS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putDT(int tag) {
        return put(StringElement.createDT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putDT(int tag, Date value) {
        return put(
            value != null
                ? StringElement.createDT(tag, value)
                : StringElement.createDT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putDT(int tag, Date[] values) {
        return put(
            values != null
                ? StringElement.createDT(tag, values)
                : StringElement.createDT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag   Description of the Parameter
     * @param  from  Description of the Parameter
     * @param  to    Description of the Parameter
     * @return       Description of the Return Value
     */
    public DcmElement putDT(int tag, Date from, Date to) {
        return put(
            from != null
                || to != null
                    ? StringElement.createDT(tag, from, to)
                    : StringElement.createDT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putDT(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? StringElement.createDT(tag, value)
                : StringElement.createDT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putDT(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createDT(tag, values)
                : StringElement.createDT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putFL(int tag) {
        return put(ValueElement.createFL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putFL(int tag, float value) {
        return put(ValueElement.createFL(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putFL(int tag, float[] values) {
        return put(
            values != null
                ? ValueElement.createFL(tag, values)
                : ValueElement.createFL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putFL(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? ValueElement.createFL(tag, Float.parseFloat(value))
                : ValueElement.createFL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putFL(int tag, String[] values) {
        return put(
            values != null
                ? ValueElement.createFL(tag, StringUtils.parseFloats(values))
                : ValueElement.createFL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putFD(int tag) {
        return put(ValueElement.createFD(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putFD(int tag, double value) {
        return put(ValueElement.createFD(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putFD(int tag, double[] values) {
        return put(
            values != null
                ? ValueElement.createFD(tag, values)
                : ValueElement.createFD(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putFD(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? ValueElement.createFD(tag, Double.parseDouble(value))
                : ValueElement.createFD(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putFD(int tag, String[] values) {
        return put(
            values != null
                ? ValueElement.createFD(tag, StringUtils.parseDoubles(values))
                : ValueElement.createFD(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putIS(int tag) {
        return put(StringElement.createIS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putIS(int tag, int value) {
        return put(StringElement.createIS(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putIS(int tag, int[] values) {
        return put(
            values != null
                ? StringElement.createIS(tag, values)
                : StringElement.createIS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putIS(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? StringElement.createIS(tag, value)
                : StringElement.createIS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putIS(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createIS(tag, values)
                : StringElement.createIS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putLO(int tag) {
        return put(StringElement.createLO(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putLO(int tag, String value) {
        return put(
            value != null
                ? StringElement.createLO(tag, value, getCharset())
                : StringElement.createLO(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putLO(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createLO(tag, values, getCharset())
                : StringElement.createLO(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putLT(int tag) {
        return put(StringElement.createLT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putLT(int tag, String value) {
        return put(
            value != null
                ? StringElement.createLT(tag, value, getCharset())
                : StringElement.createLT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putLT(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createLT(tag, values, getCharset())
                : StringElement.createLT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putOB(int tag) {
        return put(ValueElement.createOB(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putOB(int tag, byte[] value) {
        return put(
            value != null
                ? ValueElement.createOB(tag, value)
                : ValueElement.createOB(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putOB(int tag, ByteBuffer value) {
        return put(
            value != null
                ? ValueElement.createOB(tag, value)
                : ValueElement.createOB(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putOBsq(int tag) {
        return put(FragmentElement.createOB(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putOF(int tag) {
        return put(ValueElement.createOF(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putOF(int tag, float[] value) {
        return put(
            value != null
                ? ValueElement.createOF(tag, value)
                : ValueElement.createOF(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putOF(int tag, ByteBuffer value) {
        return put(
            value != null
                ? ValueElement.createOF(tag, value)
                : ValueElement.createOF(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putOFsq(int tag) {
        return put(FragmentElement.createOF(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putOW(int tag) {
        return put(ValueElement.createOW(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putOW(int tag, short[] value) {
        return put(
            value != null
                ? ValueElement.createOW(tag, value)
                : ValueElement.createOW(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putOW(int tag, ByteBuffer value) {
        return put(
            value != null
                ? ValueElement.createOW(tag, value)
                : ValueElement.createOW(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putOWsq(int tag) {
        return put(FragmentElement.createOW(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putPN(int tag) {
        return put(StringElement.createPN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putPN(int tag, PersonName value) {
        return put(
            value != null
                ? StringElement.createPN(tag, value, getCharset())
                : StringElement.createPN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putPN(int tag, PersonName[] values) {
        return put(
            values != null
                ? StringElement.createPN(tag, values, getCharset())
                : StringElement.createPN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putPN(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? StringElement.createPN(
                    tag,
                    new PersonNameImpl(value),
                    getCharset())
                : StringElement.createPN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putPN(int tag, String[] values) {
        if (values == null) {
            return StringElement.createPN(tag);
        }
        PersonName[] a = new PersonName[values.length];
        for (int i = 0; i < a.length; ++i) {
            a[i] = new PersonNameImpl(values[i]);
        }
        return put(StringElement.createPN(tag, a, getCharset()));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putSH(int tag) {
        return put(StringElement.createSH(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putSH(int tag, String value) {
        return put(
            value != null
                ? StringElement.createSH(tag, value, getCharset())
                : StringElement.createSH(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putSH(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createSH(tag, values, getCharset())
                : StringElement.createSH(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putSL(int tag) {
        return put(ValueElement.createSL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putSL(int tag, int value) {
        return put(ValueElement.createSL(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putSL(int tag, int[] values) {
        return put(
            values != null
                ? ValueElement.createSL(tag, values)
                : ValueElement.createSL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putSL(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? ValueElement.createSL(
                    tag,
                    StringUtils.parseInt(
                        value,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE))
                : ValueElement.createSL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putSL(int tag, String[] values) {
        return put(
            ValueElement.createSL(
                tag,
                StringUtils.parseInts(
                    values,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE)));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putSQ(int tag) {
        throw new UnsupportedOperationException();
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putSS(int tag) {
        return put(ValueElement.createSS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putSS(int tag, int value) {
        return put(ValueElement.createSS(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putSS(int tag, int[] values) {
        return put(
            values != null
                ? ValueElement.createSS(tag, values)
                : ValueElement.createSS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putSS(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? ValueElement.createSS(
                    tag,
                    StringUtils.parseInt(
                        value,
                        Short.MIN_VALUE,
                        Short.MAX_VALUE))
                : ValueElement.createSS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putSS(int tag, String[] values) {
        return put(
            values != null
                ? ValueElement.createSS(
                    tag,
                    StringUtils.parseInts(
                        values,
                        Short.MIN_VALUE,
                        Short.MAX_VALUE))
                : ValueElement.createSS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putST(int tag) {
        return put(StringElement.createST(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putST(int tag, String value) {
        return put(
            value != null
                ? StringElement.createST(tag, value, getCharset())
                : StringElement.createST(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putST(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createST(tag, values, getCharset())
                : StringElement.createST(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putTM(int tag) {
        return put(StringElement.createTM(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putTM(int tag, Date value) {
        return put(
            value != null
                ? StringElement.createTM(tag, value)
                : StringElement.createTM(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putTM(int tag, Date[] values) {
        return put(
            values != null
                ? StringElement.createTM(tag, values)
                : StringElement.createTM(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag   Description of the Parameter
     * @param  from  Description of the Parameter
     * @param  to    Description of the Parameter
     * @return       Description of the Return Value
     */
    public DcmElement putTM(int tag, Date from, Date to) {
        return put(
            from != null
                || to != null
                    ? StringElement.createTM(tag, from, to)
                    : StringElement.createTM(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putTM(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? StringElement.createTM(tag, value)
                : StringElement.createTM(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putTM(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createTM(tag, values)
                : StringElement.createTM(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putUI(int tag) {
        return put(StringElement.createUI(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUI(int tag, String value) {
        return put(
            value != null
                ? StringElement.createUI(tag, value)
                : StringElement.createUI(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putUI(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createUI(tag, values)
                : StringElement.createUI(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putUL(int tag) {
        return put(ValueElement.createUL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUL(int tag, int value) {
        return put(ValueElement.createUL(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putUL(int tag, int[] values) {
        return put(
            values != null
                ? ValueElement.createUL(tag, values)
                : StringElement.createUI(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUL(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? ValueElement.createUL(
                    tag,
                    StringUtils.parseInt(value, 0L, 0xFFFFFFFFL))
                : ValueElement.createUL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putUL(int tag, String[] values) {
        return put(
            values != null
                ? ValueElement.createUL(
                    tag,
                    StringUtils.parseInts(values, 0L, 0xFFFFFFFFL))
                : ValueElement.createUL(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putUN(int tag) {
        return put(ValueElement.createUN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUN(int tag, byte[] value) {
        return put(
            value != null
                ? ValueElement.createUN(tag, value)
                : ValueElement.createUN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUN(int tag, ByteBuffer value) {
        return put(
            value != null
                ? ValueElement.createUN(tag, value)
                : ValueElement.createUN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putUNsq(int tag) {
        return put(FragmentElement.createUN(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putUS(int tag) {
        return put(ValueElement.createUS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUS(int tag, int value) {
        return put(ValueElement.createUS(tag, value));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putUS(int tag, int[] values) {
        return put(
            values != null
                ? ValueElement.createUS(tag, values)
                : ValueElement.createUS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUS(int tag, String value) {
        return put(
            value != null && value.length() != 0
                ? ValueElement.createUS(
                    tag,
                    StringUtils.parseInt(value, 0L, 0xFFFFL))
                : ValueElement.createUS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putUS(int tag, String[] values) {
        return put(
            values != null
                ? ValueElement.createUS(
                    tag,
                    StringUtils.parseInts(values, 0L, 0xFFFFL))
                : ValueElement.createUS(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putUT(int tag) {
        return put(StringElement.createUT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putUT(int tag, String value) {
        return put(
            value != null
                ? StringElement.createUT(tag, value, getCharset())
                : StringElement.createUT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putUT(int tag, String[] values) {
        return put(
            values != null
                ? StringElement.createUT(tag, values, getCharset())
                : StringElement.createUT(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putXX(int tag) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  bytes  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putXX(int tag, ByteBuffer bytes) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), bytes);
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putXX(int tag, String value) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), value);
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putXX(int tag, String[] values) {
        return putXX(tag, VRMap.DEFAULT.lookup(tag), values);
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putXXsq(int tag) {
        return putXXsq(tag, VRMap.DEFAULT.lookup(tag));
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @param  vr   Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putXXsq(int tag, int vr) {
        switch (vr) {
            case VRs.OB :
                return putOBsq(tag);
            case VRs.OF :
                return putOFsq(tag);
            case VRs.OW :
                return putOWsq(tag);
            case VRs.UN :
                return putUNsq(tag);
            default :
                throw new IllegalArgumentException(
                    Tags.toString(tag) + " " + VRs.toString(vr));
        }
    }

    /**
     *  Description of the Method
     *
     * @param  tag  Description of the Parameter
     * @param  vr   Description of the Parameter
     * @return      Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr) {
        switch (vr) {
            case VRs.AE :
                return putAE(tag);
            case VRs.AS :
                return putAS(tag);
            case VRs.AT :
                return putAT(tag);
            case VRs.CS :
                return putCS(tag);
            case VRs.DA :
                return putDA(tag);
            case VRs.DS :
                return putDS(tag);
            case VRs.DT :
                return putDT(tag);
            case VRs.FL :
                return putFL(tag);
            case VRs.FD :
                return putFD(tag);
            case VRs.IS :
                return putIS(tag);
            case VRs.LO :
                return putLO(tag);
            case VRs.LT :
                return putLT(tag);
            case VRs.OB :
                return putOB(tag);
            case VRs.OF :
                return putOF(tag);
            case VRs.OW :
                return putOW(tag);
            case VRs.PN :
                return putPN(tag);
            case VRs.SH :
                return putSH(tag);
            case VRs.SL :
                return putSL(tag);
            case VRs.SQ :
                return ((Dataset) this).putSQ(tag);
            case VRs.SS :
                return putSS(tag);
            case VRs.ST :
                return putST(tag);
            case VRs.TM :
                return putTM(tag);
            case VRs.UI :
                return putUI(tag);
            case VRs.UN :
                return putUN(tag);
            case VRs.UL :
                return putUL(tag);
            case VRs.US :
                return putUS(tag);
            case VRs.UT :
                return putUT(tag);
            default :
                log.warn(Tags.toString(tag) + " with illegal VR Code: "
                        + Integer.toHexString(vr) + "H");
                return putXX(tag, VRMap.DEFAULT.lookup(tag));
        }
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  vr     Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr, ByteBuffer value) {
        if (value == null) {
            return putXX(tag, vr);
        }
        switch (vr) {
            case VRs.AE :
                return put(StringElement.createAE(tag, value));
            case VRs.AS :
                return put(StringElement.createAS(tag, value));
            case VRs.AT :
                return put(ValueElement.createAT(tag, value));
            case VRs.CS :
                return put(StringElement.createCS(tag, value));
            case VRs.DA :
                return put(StringElement.createDA(tag, value));
            case VRs.DS :
                return put(StringElement.createDS(tag, value));
            case VRs.DT :
                return put(StringElement.createDT(tag, value));
            case VRs.FL :
                return put(ValueElement.createFL(tag, value));
            case VRs.FD :
                return put(ValueElement.createFD(tag, value));
            case VRs.IS :
                return put(StringElement.createIS(tag, value));
            case VRs.LO :
                return put(StringElement.createLO(tag, value));
            case VRs.LT :
                return put(StringElement.createLT(tag, value));
            case VRs.OB :
                return put(ValueElement.createOB(tag, value));
            case VRs.OF :
                return put(ValueElement.createOF(tag, value));
            case VRs.OW :
                return put(ValueElement.createOW(tag, value));
            case VRs.PN :
                return put(StringElement.createPN(tag, value));
            case VRs.SH :
                return put(StringElement.createSH(tag, value));
            case VRs.SL :
                return put(ValueElement.createSL(tag, value));
            case VRs.SS :
                return put(ValueElement.createSS(tag, value));
            case VRs.ST :
                return put(StringElement.createST(tag, value));
            case VRs.TM :
                return put(StringElement.createTM(tag, value));
            case VRs.UI :
                return put(StringElement.createUI(tag, value));
            case VRs.UN :
                return put(ValueElement.createUN(tag, value));
            case VRs.UL :
                return put(ValueElement.createUL(tag, value));
            case VRs.US :
                return put(ValueElement.createUS(tag, value));
            case VRs.UT :
                return put(StringElement.createUT(tag, value));
            default :
                log.warn(Tags.toString(tag) + " with illegal VR Code: "
                        + Integer.toHexString(vr) + "H");
                return putXX(tag, VRMap.DEFAULT.lookup(tag), value);
        }
    }

    /**
     *  Description of the Method
     *
     * @param  tag    Description of the Parameter
     * @param  vr     Description of the Parameter
     * @param  value  Description of the Parameter
     * @return        Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr, String value) {
        if (value == null) {
            return putXX(tag, vr);
        }
        switch (vr) {
            case VRs.AE :
                return putAE(tag, value);
            case VRs.AS :
                return putAS(tag, value);
            case VRs.AT :
                return putAT(tag, value);
            case VRs.CS :
                return putCS(tag, value);
            case VRs.DA :
                return putDA(tag, value);
            case VRs.DS :
                return putDS(tag, value);
            case VRs.DT :
                return putDT(tag, value);
            case VRs.FL :
                return putFL(tag, value);
            case VRs.FD :
                return putFD(tag, value);
            case VRs.IS :
                return putIS(tag, value);
            case VRs.LO :
                return putLO(tag, value);
            case VRs.LT :
                return putLT(tag, value);
            case VRs.OB:
            case VRs.OF:
            case VRs.OW:
                throw new IllegalArgumentException(
                        Tags.toString(tag) + " " + VRs.toString(vr));
            case VRs.PN :
                return putPN(tag, value);
            case VRs.SH :
                return putSH(tag, value);
            case VRs.SL :
                return putSL(tag, value);
            case VRs.SS :
                return putSS(tag, value);
            case VRs.ST :
                return putST(tag, value);
            case VRs.TM :
                return putTM(tag, value);
            case VRs.UI :
                return putUI(tag, value);
            case VRs.UN:
                throw new IllegalArgumentException(
                        Tags.toString(tag) + " " + VRs.toString(vr));
            case VRs.UL :
                return putUL(tag, value);
            case VRs.US :
                return putUS(tag, value);
            case VRs.UT :
                return putUT(tag, value);
            default :
                log.warn(Tags.toString(tag) + " with illegal VR Code: "
                        + Integer.toHexString(vr) + "H");
                return putXX(tag, VRMap.DEFAULT.lookup(tag), value);
        }
    }

    /**
     *  Description of the Method
     *
     * @param  tag     Description of the Parameter
     * @param  vr      Description of the Parameter
     * @param  values  Description of the Parameter
     * @return         Description of the Return Value
     */
    public DcmElement putXX(int tag, int vr, String[] values) {
        if (values == null) {
            return putXX(tag, vr);
        }
        switch (vr) {
            case VRs.AE :
                return putAE(tag, values);
            case VRs.AS :
                return putAS(tag, values);
            case VRs.AT :
                return putAT(tag, values);
            case VRs.CS :
                return putCS(tag, values);
            case VRs.DA :
                return putDA(tag, values);
            case VRs.DS :
                return putDS(tag, values);
            case VRs.DT :
                return putDT(tag, values);
            case VRs.FL :
                return putFL(tag, values);
            case VRs.FD :
                return putFD(tag, values);
            case VRs.IS :
                return putIS(tag, values);
            case VRs.LO :
                return putLO(tag, values);
            case VRs.LT :
                return putLT(tag, values);
            case VRs.OB:
            case VRs.OF:
            case VRs.OW:
                throw new IllegalArgumentException(
                        Tags.toString(tag) + " " + VRs.toString(vr));
            case VRs.PN :
                return putPN(tag, values);
            case VRs.SH :
                return putSH(tag, values);
            case VRs.SL :
                return putSL(tag, values);
            case VRs.SS :
                return putSS(tag, values);
            case VRs.ST :
                return putST(tag, values);
            case VRs.TM :
                return putTM(tag, values);
            case VRs.UI :
                return putUI(tag, values);
            case VRs.UN:
                throw new IllegalArgumentException(
                        Tags.toString(tag) + " " + VRs.toString(vr));
            case VRs.UL :
                return putUL(tag, values);
            case VRs.US :
                return putUS(tag, values);
            case VRs.UT :
                return putUT(tag, values);
            default :
                log.warn(Tags.toString(tag) + " with illegal VR Code: "
                        + Integer.toHexString(vr) + "H");
                return putXX(tag, VRMap.DEFAULT.lookup(tag), values);
        }
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public Iterator iterator() {
        return list.iterator();
    }

    /**
     *  Description of the Method
     *
     * @param  dcmObj  Description of the Parameter
     */
    public void putAll(DcmObject dcmObj) {
        for (Iterator it = dcmObj.iterator(); it.hasNext();) {
            DcmElement el = (DcmElement) it.next();
            if (el.isEmpty()) {
                putXX(el.tag(), el.vr());
            } else {
                DcmElement sq;
                switch (el.vr()) {
                    case VRs.SQ :
                        sq = putSQ(el.tag());
                        for (int i = 0, n = el.vm(); i < n; ++i) {
                            sq.addNewItem().putAll(el.getItem(i));
                        }
                        break;
                    case VRs.OB :
                    case VRs.OF :
                    case VRs.OW :
                    case VRs.UN :
                        if (el.hasDataFragments()) {
                            sq = putXXsq(el.tag(), el.vr());
                            for (int i = 0, n = el.vm(); i < n; ++i) {
                                sq.addDataFragment(el.getDataFragment(i));
                            }
                            break;
                        }
                    default :
                        putXX(el.tag(), el.vr(), el.getByteBuffer());
                        break;
                }
            }
        }
    }

    /**
     *  Description of the Method
     *
     * @param  grTag            Description of the Parameter
     * @param  grLen            Description of the Parameter
     * @param  handler          Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    protected void write(int grTag, int grLen, DcmHandler handler)
        throws IOException {
        byte[] b4 =
            {
                (byte) grLen,
                (byte) (grLen >>> 8),
                (byte) (grLen >>> 16),
                (byte) (grLen >>> 24)};
        long el1Pos = ((DcmElement) list.get(0)).getStreamPosition();
        handler.startElement(grTag, VRs.UL, el1Pos == -1L ? -1L : el1Pos - 12);
        handler.value(b4, 0, 4);
        handler.endElement();
        for (int i = 0, n = list.size(); i < n; ++i) {
            DcmElement el = (DcmElement) list.get(i);
            int len = el.length();
            handler.startElement(el.tag(), el.vr(), el.getStreamPosition());
            ByteBuffer bb = el.getByteBuffer(ByteOrder.LITTLE_ENDIAN);
            handler.value(bb.array(), bb.arrayOffset(), bb.limit());
            handler.endElement();
        }
    }

    /**
     *  Description of the Method
     *
     * @param  out              Description of the Parameter
     * @param  encParam         Description of the Parameter
     * @param  tag              Description of the Parameter
     * @param  vr               Description of the Parameter
     * @param  len              Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public void writeHeader(
        ImageOutputStream out,
        DcmEncodeParam encParam,
        int tag,
        int vr,
        int len)
        throws IOException {
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(tag >> 16);
            out.write(tag >> 24);
            out.write(tag >> 0);
            out.write(tag >> 8);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(tag >> 24);
            out.write(tag >> 16);
            out.write(tag >> 8);
            out.write(tag >> 0);
        }
        if (vr != VRs.NONE && encParam.explicitVR) {
            out.write(vr >> 8);
            out.write(vr >> 0);
            if (VRs.isLengthField16Bit(vr)) {
                if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    out.write(len >> 0);
                    out.write(len >> 8);
                } else {
                    out.write(len >> 8);
                    out.write(len >> 0);
                }
                return;
            } else {
                out.write(0);
                out.write(0);
            }
        }
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(len >> 0);
            out.write(len >> 8);
            out.write(len >> 16);
            out.write(len >> 24);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len >> 0);
        }
    }

    /**
     *  Description of the Method
     *
     * @param  out              Description of the Parameter
     * @param  encParam         Description of the Parameter
     * @param  tag              Description of the Parameter
     * @param  vr               Description of the Parameter
     * @param  len              Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public void writeHeader(
        OutputStream out,
        DcmEncodeParam encParam,
        int tag,
        int vr,
        int len)
        throws IOException {
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(tag >> 16);
            out.write(tag >> 24);
            out.write(tag >> 0);
            out.write(tag >> 8);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(tag >> 24);
            out.write(tag >> 16);
            out.write(tag >> 8);
            out.write(tag >> 0);
        }
        if (vr != VRs.NONE && encParam.explicitVR) {
            out.write(vr >> 8);
            out.write(vr >> 0);
            if (VRs.isLengthField16Bit(vr)) {
                if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    out.write(len >> 0);
                    out.write(len >> 8);
                } else {
                    out.write(len >> 8);
                    out.write(len >> 0);
                }
                return;
            } else {
                out.write(0);
                out.write(0);
            }
        }
        if (encParam.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            out.write(len >> 0);
            out.write(len >> 8);
            out.write(len >> 16);
            out.write(len >> 24);
        } else { // order == ByteOrder.BIG_ENDIAN
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len >> 0);
        }
    }

}
