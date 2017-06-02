package org.dcm4chee.xero.parsers;

import org.dcm4chee.xero.util.StringUtil;

public class UidParser extends StringParser {

   @Override
   public String[] parse(String value) throws ParserException {
	  value = (String) super.parse(value);
	  return StringUtil.split(value,'\\',true);
   }

}
