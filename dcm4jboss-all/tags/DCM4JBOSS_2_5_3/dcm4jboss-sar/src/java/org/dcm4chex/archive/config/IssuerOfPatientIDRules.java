/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.config;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1784 $ $Date: 2005-06-09 02:08:32 +0800 (周四, 09 6月 2005) $
 * @since Jun 8, 2005
 */
public class IssuerOfPatientIDRules {
	
	private final Pattern[] patterns;
	private final String[] issuers;

	public IssuerOfPatientIDRules(String rules) {
		StringTokenizer stk = new StringTokenizer(rules, " ;\t\r\n");
		patterns = new Pattern[stk.countTokens()];
		issuers = new String[patterns.length];
		for (int i = 0; i < issuers.length; i++) {
			issuers[i] = stk.nextToken();
			final int beginIssuer = issuers[i].indexOf(':') + 1;
			if (beginIssuer > 0) {
				patterns[i] = Pattern.compile(issuers[i].substring(0,
						beginIssuer - 1));
				issuers[i] = issuers[i].substring(beginIssuer);
			}
		}
	}
	
	public String issuerOf(String pid) {
		for (int i = 0; i < patterns.length; i++) {
			if (patterns[i] == null || patterns[i].matcher(pid).matches()) {
				return issuers[i];
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < issuers.length; i++) {
			if (patterns[i] != null) {
				sb.append(patterns[i].pattern()).append(':');
			}
			sb.append(issuers[i]).append("\r\n");
		}
		return sb.toString();
	}

}
