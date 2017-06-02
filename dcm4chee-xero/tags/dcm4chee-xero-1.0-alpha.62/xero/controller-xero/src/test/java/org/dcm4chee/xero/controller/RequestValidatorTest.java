package org.dcm4chee.xero.controller;

import static org.easymock.classextension.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class RequestValidatorTest {
   private static final Logger log = LoggerFactory.getLogger(RequestValidatorTest.class);
   MetaDataBean mdb = StaticMetaData.getMetaData("xero.metadata");
   MetaDataBean model = mdb.getChild("model");
   MetaDataBean controller = mdb.getChild("controller");
   Filter<Object> query = (Filter<Object>) controller.getValue("query");

   /**
    * Tests that the default study query tests succeeds on valid data.
    */
   @Test
   public void succeedQueryTest() {
	  assert mdb!=null;
	  assert model!=null;
	  assert controller!=null;
	  assert controller.get("query")!=null;
	  Map<String,Object> request = new HashMap<String,Object>();
	  request.put("ModalitiesInStudy", "CT");
	  Map<String,Object> mwd = FilterUtil.getModel(request,model);

	  Map<String,Object> rv = (Map<String,Object>) mwd.get("query");
	  assert rv==null;
	  FilterItem<Object> filterItem = createMock(FilterItem.class);
	  expect(filterItem.callNextFilter(request)).andReturn(null);
	  replay(filterItem);
	  
	  query.filter(filterItem, request);
	  verify(filterItem);
	  rv = (Map<String,Object>) mwd.get("query");
	  assert rv!=null;
	  assert "CT".equals(rv.get("ModalitiesInStudy"));
	  String url = (String) rv.get("url");
	  assert url!=null;
	  log.info("URL for request validator="+url);
	  assert "/wado2/?requestType=STUDY&maxResults=500&ModalitiesInStudy=CT".equals(url);
   }
   
   @Test
   public void test_queryLayout_isAvailable() {
	   Map<?,?> map = (Map<?, ?>) model.getValue();
	   assert FilterUtil.getMap(map,"queryLayout")!=null;
   }
}
