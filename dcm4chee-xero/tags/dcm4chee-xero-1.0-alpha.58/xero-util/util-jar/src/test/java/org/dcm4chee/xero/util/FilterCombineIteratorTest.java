package org.dcm4chee.xero.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FilterCombineIteratorTest {

    static final String TEST_KEY = "test";
    
    Map<String,Object> params;
    List<String> items;
    TestFilterCombineIterator tfci;
    TestFilterItem tfi;
    
    @BeforeMethod
    public void init() {
        params = new HashMap<String,Object>();
        items = new ArrayList<String>();
        tfi = new TestFilterItem();
    }
    
    static class TestFilterCombineIterator extends FilterCombineIterator<String,String> {
        String origValue;
        
        public TestFilterCombineIterator(Iterator<String> itemsIt, FilterItem<Iterator<String>> filterItem, Map<String, Object> params) {
            super(itemsIt, filterItem, params);
            origValue = (String) params.get(TEST_KEY);
        }

        @Override
        protected void updateParams(String item, Map<String, Object> params) {
            params.put(TEST_KEY,item);
        }

        @Override
        protected void restoreParams(String item, Map<String, Object> params) {
            params.put(TEST_KEY, origValue);
        }
        
    }
    
    static class TestFilterItem extends FilterItem<Iterator<String> > {

        @Override
        public Iterator<String> callNextFilter(Map<String, Object> params) {
            List<String> retList = new ArrayList<String>(2);
            String v = (String) params.get(TEST_KEY);
            retList.add(v+".a");
            retList.add(v+".b");
            return retList.iterator();
        }
        
    }
    
    @Test
    public void test_paramsRestored_afterHasNext() {
        items.add("1");
        items.add("2");
        params.put(TEST_KEY,"1\2");
        tfci = new TestFilterCombineIterator(items.iterator(), tfi, params);
        assert tfci.hasNext();
        String next;
        next = tfci.next();
        assert next.equals("1.a");
        assert tfci.hasNext();
        next = tfci.next();
        assert next.equals("1.b");
        assert tfci.hasNext();
        next = tfci.next();
        assert next.equals("2.a");
        assert tfci.hasNext();
        next = tfci.next();
        assert next.equals("2.b");
        assert !tfci.hasNext();
    }
}
