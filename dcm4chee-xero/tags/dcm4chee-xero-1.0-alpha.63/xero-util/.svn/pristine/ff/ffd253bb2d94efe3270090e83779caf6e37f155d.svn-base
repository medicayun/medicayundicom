package org.dcm4chee.xero.template;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.testng.annotations.Test;

/** Tests that the string template filter returns a StringTemplateResponseItem on the correct filter view */
public class StringTemplateFilterTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testStringTemplateFilter() {
		  MetaDataBean root = StaticMetaData.getMetaData("util-test.metadata");
		  MetaDataBean mdb = root.getChild("controller");
		  Filter filter = (Filter) mdb.getValue();
		  FilterItem fi = new FilterItem(mdb);
		  Map<String,Object> params = new HashMap<String,Object>();
		  
		  StringTemplateResponseItem  stri = (StringTemplateResponseItem) filter.filter(fi,params);;
		  assert stri!=null;
	}

}
