/*$Id: FilterDataset.java 3871 2005-03-02 15:27:55Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.FileFormat;

import org.dcm4che.dict.Tags;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.stream.ImageInputStream;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class FilterDataset extends BaseDatasetImpl implements Dataset {
    
    protected final BaseDatasetImpl backend;
    
    /** Creates a new instance of DatasetView */
    public FilterDataset(Dataset backend) {
        this.backend = (BaseDatasetImpl)backend;
    }
                    
    protected abstract boolean filter(int tag);
 
    public boolean contains(int tag) {
        return filter(tag) && backend.contains(tag);
    }

    public int vm(int tag) {
        return filter(tag) ? backend.vm(tag) : -1;
    }

    public int size() {
        int count = 0;
        for (Iterator iter = backend.iterator(); iter.hasNext();) {
            if (filter(((DcmElement)iter.next()).tag())) {
                ++count;
            }
        }
        return count;
    }

    public DcmElement get(int tag) {
        return filter(tag) ? backend.get(tag) : null;
    }
    
    public Iterator iterator() {
        final Iterator backendIter = backend.iterator();
        return new Iterator() {
            private DcmElement next = findNext();
            private DcmElement findNext() {
                DcmElement el;
                while (backendIter.hasNext()) {
                    if (filter((el = (DcmElement)backendIter.next()).tag())) {
                        return el;
                    }
                }
                return null;
            }
            
            public boolean hasNext() {
                return next != null;
            }
            
            public Object next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                DcmElement retval = next;
                next = findNext();
                return retval;
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Charset getCharset() {
        return backend.getCharset();
    }
    
    public Dataset getParent() {
        return backend.getParent();
    }

    public void setPrivateCreatorID(String privateCreatorID) {
        backend.setPrivateCreatorID(privateCreatorID);
    }       

    public String getPrivateCreatorID() {
        return backend.getPrivateCreatorID();
    }
    
    public long getItemOffset() {
        return backend.getItemOffset();
    }

    public DcmHandler getDcmHandler() {
        throw new UnsupportedOperationException();
    }

    public DefaultHandler getSAXHandler() {
        throw new UnsupportedOperationException();
    }
    
    protected DcmElement put(DcmElement el) {
       if (!filter(el.tag())) {
          throw new IllegalArgumentException(
            "" + el + " does not fit in this sub DataSet");
       }
       return backend.put(el);
    }
    
    public DcmElement remove(int tag) {
        return filter(tag) ? backend.remove(tag) : null;
    }
    
    public void clear() {
      ArrayList toRemove = new ArrayList();
      for (Iterator iter = backend.iterator(); iter.hasNext();) {
         DcmElement el = (DcmElement)iter.next();
         if (filter(el.tag())) {
            toRemove.add(el);
         }
      }
      for (int i = 0, n = toRemove.size(); i < n; ++i) {
         backend.remove(((DcmElement)toRemove.get(i)).tag());
      }
    }

    public Dataset setItemOffset(long itemOffset) {
        throw new UnsupportedOperationException();
    }
    
    public void readDataset(InputStream in, DcmDecodeParam param, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }    

    public void readFile(InputStream in, FileFormat format, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }    
    
    public void readFile(ImageInputStream iin, FileFormat format, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFile(File f, FileFormat format, int stopTag)
	    	throws IOException {
        throw new UnsupportedOperationException();
	}
    
    static final class Selection extends FilterDataset {
        private final Dataset filter;
        Selection(Dataset backend, Dataset filter) {
            super(backend);
            this.filter = filter;
        }

        protected  boolean filter(int tag) {
            return filter == null || filter.contains(tag);
        }

        public DcmElement get(int tag) {
            if (filter == null) {
                return backend.get(tag);
            }
            DcmElement filterEl = filter.get(tag);
            if (filterEl == null) {
                return null;
            }
            
            DcmElement el = backend.get(tag);
            if (!(el instanceof SQElement)) {
                return el;
            }
            if (!(filterEl instanceof SQElement)) {
                log.warn("VR mismatch - dataset:" + el
                        + ", filter:" + filterEl);
                return el;
            }
            Dataset item = filterEl.getItem();
            if (item == null || item.isEmpty()) {
                return el;
            }
            return new FilterSQElement((SQElement)el, item);
        }
    }

    static final class ExcludePrivate extends FilterDataset {
        ExcludePrivate(Dataset backend) {
            super(backend);
        }

        protected  boolean filter(int tag) {
            return !Tags.isPrivate(tag);
        }

        public DcmElement get(int tag) {
            if (Tags.isPrivate(tag)) return null;
            DcmElement el = backend.get(tag);
            if (!(el instanceof SQElement)) {
                return el;
            }
            return new ExcludePrivateSQElement((SQElement)el);
        }
    }
    
    static final class Segment extends FilterDataset {
        private long fromTag;
        private long toTag;
        Segment(Dataset backend, int fromTag, int toTag) {
            super(backend);
            this.fromTag = fromTag & 0xFFFFFFFFL;
            this.toTag = toTag & 0xFFFFFFFFL;
            if (this.fromTag > this.toTag) {
               throw new IllegalArgumentException(
                  "fromTag:" + Tags.toString(fromTag)
                  + " greater toTag:" + Tags.toString(toTag));
            }
        }
        
        public int size() {
            int count = 0;
            long ltag;
            for (Iterator iter = backend.iterator(); iter.hasNext();) {
                ltag = ((DcmElement)iter.next()).tag() & 0xFFFFFFFFL;
                if (ltag < fromTag) continue;
                if (ltag >= toTag) break;
                ++count;
            }
            return count;
        }

        protected boolean filter(int tag) {
            long ltag = tag & 0xFFFFFFFF;
            return ltag >= fromTag && ltag < toTag;
        }
    } 

    static final int[] EMPTY_INT = {};
    
    static final class TagFilter extends FilterDataset {
        private final int[] tags;
		private final boolean exclude;
		private final boolean excludePrivate;
        TagFilter(Dataset backend, int[] tags, boolean exclude, 
                boolean excludePrivate) {
            super(backend);
            this.tags = tags == null ? EMPTY_INT : (int[]) tags.clone();
            this.exclude = exclude;
            this.excludePrivate = excludePrivate;
            Arrays.sort(this.tags);
        }
        
        protected boolean filter(int tag) {
            return !(excludePrivate && Tags.isPrivate(tag))
            	&& Arrays.binarySearch(tags, tag) < 0 ? exclude : !exclude;
        }
        
    } 
        
}
