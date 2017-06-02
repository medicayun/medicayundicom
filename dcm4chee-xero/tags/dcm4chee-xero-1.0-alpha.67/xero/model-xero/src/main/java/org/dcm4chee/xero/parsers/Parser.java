package org.dcm4chee.xero.parsers;

import java.util.Map;

public interface Parser {

   public Object parse(Map<String,Object> sourceMap) throws ParserException;
}
