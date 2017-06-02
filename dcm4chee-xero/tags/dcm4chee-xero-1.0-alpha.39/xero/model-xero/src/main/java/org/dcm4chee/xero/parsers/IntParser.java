package org.dcm4chee.xero.parsers;

/**
 * Parses integer request parameter values.
 * @author bwallace
 */
public class IntParser extends SingleValueParser {

   @Override
   public Object parse(String value) throws ParserException {
	  return Integer.parseInt(value);
   }

}
