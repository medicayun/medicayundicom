package org.dcm4chee.xero.parsers;

/**
 * Checks to see that the returned value is a safe string to use - that is, contains no
 * <, & expansions in it.  May expand some common & expansions.
 * @author bwallace
 *
 */
public class StringParser extends SingleValueParser {

   @Override
   public Object parse(String value) throws ParserException {
	  validate(value);
	  return value;
   }

   public static void validate(String value) throws ParserException{
	  for(int i=0, n=value.length(); i<n; i++) {
		 char ch = value.charAt(i);
		 if( ch==0 || ch=='<' || ch=='&' || ch=='"' || ch=='\'' ) {
			throw new ParserException("String contains an illegal value: "+ch);
		 }
	  }
   }
}

