package org.antlr.stringtemplate.js;

/**
 * Has fast static methods to split strings
 * @author bwallace
 *
 */
public class StringUtil {


   public static String[] split(String v, char ch, boolean trim) {
	  int splits = 0;
	  for(int i=0,n=v.length(); i<n; i++) {
		 if( v.charAt(i)==ch ) splits++;
	  }
	  String[] ret = new String[splits+1];
	  if( splits==0 ) {
		 if( trim ) v= v.trim(); 
		 ret[0] = v;
		 return ret;
	  }
	  int start = 0;
	  int cnt = 0;
	  for(int i=0,n=v.length(); i<n; i++) {
		 if( v.charAt(i)==ch ) {
			if(start>=i) {
			   ret[cnt] = "";
			}
			else {
			   ret[cnt] = v.substring(start,i);
			   if( trim ) ret[cnt] = ret[cnt].trim();
			}
			cnt++;
			start = i+1;
			if( cnt==splits ) break;
		 }
	  }
	  ret[cnt] = v.substring(start);
	  if( trim ) ret[cnt] = ret[cnt].trim();
	  return ret;
   }
}
