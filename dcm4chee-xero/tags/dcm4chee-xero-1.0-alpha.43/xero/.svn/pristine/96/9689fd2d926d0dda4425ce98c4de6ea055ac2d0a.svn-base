package org.dcm4chee.xero.parsers;

import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;

public abstract class SingleValueParser implements Parser,MetaDataUser {
   String key;
   
   public Object parse(Map<String, Object> sourceMap) throws ParserException {
	  Object srcVObj = sourceMap.get(key);
	  if( srcVObj==null ) return null;
	  String srcValue;
	  if( srcVObj instanceof String[] ) {
		 srcValue = ((String[]) srcVObj)[0];
	  }
	  else if( srcVObj instanceof String ) {
		 srcValue = (String) srcVObj;
	  }
	  else {
		 srcValue = srcVObj.toString();
	  }
	  return parse((String) srcValue);
   }
   
   abstract public Object parse(String value) throws ParserException;

   public void setMetaData(MetaDataBean metaDataBean) {
	  key = metaDataBean.getChildName();
   }

}
