package org.dcm4chee.xero.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.testng.annotations.Test;

/** Tests that the shared meta-data is accessible */
public class SharedTest {
   MetaDataBean mdb = StaticMetaData.getMetaData("xero-view.metadata");
	MetaDataBean model = mdb.getChild("static");
	Map<String,Object> params = new HashMap<String,Object>();
	Map<String,Object> map = FilterUtil.getModel(params,model);
	
	
   @Test
   public void sharedDataAccessibleTest() {
   	assert model.getValue("shared")!=null;
   	Object sti = FilterUtil.getPath(map,"shared.studyTrayItems");
   	assert sti!=null;
   	List<?> items = (List<?>) sti;
   	assert items.size()>0;
   }
}
