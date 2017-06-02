/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4cheri.image;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Tags;

import com.sun.media.imageio.stream.SegmentedImageInputStream;
import com.sun.media.imageio.stream.StreamSegment;
import com.sun.media.imageio.stream.StreamSegmentMapper;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 3848 $ $Date: 2004-12-26 00:33:31 +0800 (周日, 26 12月 2004) $
 * @since 04.08.2004
 *
 */
public class ItemParser implements StreamSegmentMapper {

    private static final Logger log = Logger.getLogger(ItemParser.class);

    private static final class Item {
        
        int offset;

        long startPos;

        int length;

        final int nextOffset() {
            return offset + length;
        }

        final long nextItemPos() {
            return startPos + length;
        }
        
        public String toString() {
            return "Item[off=" + offset + ", pos=" + startPos + ", len=" + length + "]";
        }
    }

    private final ArrayList items = new ArrayList();
    
    private final DcmParser parser;
    private final ImageInputStream iis;
    private boolean lastItemSeen = false;
    

    public ItemParser(DcmParser parser) throws IOException {
        this.parser = parser;
        this.iis = parser.getImageInputStream();
        parser.parseHeader();
        iis.skipBytes(parser.getReadLength());
        next();
    }
    
    public int getNumberOfDataFragments() {
        while (!lastItemSeen)
            next();
        return items.size();
    }

    public int getOffsetOfDataFragment(int index) {
        while (items.size() <= index)
            if (next() == null)
                throw new IndexOutOfBoundsException(
                        "index:" + index + " >= size:" + items.size());
        return last().offset;
    }
    
    private Item next() {
        if (lastItemSeen)
            return null;
        try {
            if (!items.isEmpty())
                iis.seek(last().nextItemPos());
            parser.parseHeader();
            if (log.isDebugEnabled())
                log.debug("Read "+ Tags.toString(parser.getReadTag())
                        + " #" + parser.getReadLength());
	        if (parser.getReadTag() == Tags.Item) {
	            Item item = new Item();
	            item.startPos = iis.getStreamPosition();
	            item.length = parser.getReadLength();
	            if (!items.isEmpty())
	                item.offset = last().nextOffset();
	            items.add(item);
	            return item;
	        }
        } catch (IOException e) {
            log.warn("i/o error reading next item:", e);
        }
        if (parser.getReadTag() != Tags.SeqDelimitationItem
                || parser.getReadLength() != 0) {
            log.warn("expected (FFFE,E0DD) #0 but read "
                    + Tags.toString(parser.getReadTag())
                    + " #" + parser.getReadLength());            
        }
        lastItemSeen = true;
	    return null;
    }
    
    private Item last() {
        return (Item) items.get(items.size() -1);
    }

    public StreamSegment getStreamSegment(long pos, int len) {
        StreamSegment retval = new StreamSegment();
        getStreamSegment(pos, len, retval);
        return retval;
    }

    public void getStreamSegment(long pos, int len, StreamSegment seg) {
        if (log.isDebugEnabled())
            log.debug("getStreamSegment(pos="+ pos + ", len=" + len + ")");
        Item item = last();
        while (item.nextOffset() <= pos) {
            if ((item = next()) == null) {
                seg.setSegmentLength(-1);
                return;
            }
        }
        int i = items.size() - 1;
        while (item.offset > pos)
            item = (Item) items.get(--i);
        seg.setStartPos(item.startPos + pos - item.offset);
        seg.setSegmentLength(Math.min(
                (int) (item.offset + item.length - pos), len));
        if (log.isDebugEnabled())
            log.debug("return StreamSegment[start=" + seg.getStartPos()
                    + ", len=" + seg.getSegmentLength() + "]");
    }

    public long seekNextFrame(SegmentedImageInputStream siis)
    		throws IOException {
        Item item = last();
        long pos = siis.getStreamPosition();
        int i = items.size() - 1;
        while (item.offset >= pos)
            item = (Item) items.get(--i);
        siis.seek(item.nextOffset());
        iis.seek(item.nextItemPos());
        return item.nextOffset();
    }
    
    public void seekFooter() throws IOException {
        iis.seek(last().nextItemPos());
        parser.parseHeader();
    }
    
}
