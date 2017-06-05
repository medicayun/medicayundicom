/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chex.archive.hl7;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.regenstrief.xhl7.HL7XMLLiterate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 18, 2009
 */
class MessageTypeMatcher {

    private String messageType;
    private String triggerEvent;
    private String segment;
    private int field;
    private String value;

    public MessageTypeMatcher(String pattern) {
        int messageTypeEnd = pattern.indexOf('^');
        if (messageTypeEnd < 0) {
            throw new IllegalArgumentException(pattern);
        }
        messageType = pattern.substring(0, messageTypeEnd);
        int triggerEventEnd = pattern.indexOf('[', messageTypeEnd+1);
        if (triggerEventEnd == -1) {
            triggerEvent = pattern.substring(messageTypeEnd+1);
        } else {
            int valueEnd = pattern.length()-1;
            if (pattern.charAt(valueEnd) != ']') {
                throw new IllegalArgumentException(pattern);
            }
            triggerEvent = pattern.substring(messageTypeEnd+1,
                    triggerEventEnd);
            int segmentEnd = triggerEventEnd + 4;
            if (valueEnd < segmentEnd + 3) {
                throw new IllegalArgumentException(pattern);
            }
            if (pattern.charAt(segmentEnd) != '-') {
                throw new IllegalArgumentException(pattern);
            }
            segment = pattern.substring(triggerEventEnd+1, segmentEnd);
            int fieldEnd = pattern.indexOf('=', segmentEnd + 2);
            if (fieldEnd == -1) {
                throw new IllegalArgumentException(pattern);
            }
            try {
                field = Integer.parseInt(
                        pattern.substring(segmentEnd+1, fieldEnd));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(pattern);
            }
            if (field <= 0) {
                throw new IllegalArgumentException(pattern);
            }
            value = pattern.substring(fieldEnd+1, valueEnd);
        }
    }

    public StringBuffer toString(StringBuffer sb) {
        sb.append(messageType).append('^').append(triggerEvent);
        if (segment != null) {
            sb.append('[').append(segment).append('-').append(field)
                .append('=').append(value).append(']');
        }
        return sb;
    }

    public String toString() {
        return toString(new StringBuffer()).toString();
    }

    public boolean match(MSH msh, Document msg) {
        if (!messageType.equals(msh.messageType)
                || !triggerEvent.equals(msh.triggerEvent)) {
            return false;
        }
        if (segment == null) {
            return true;
        }
        Element seg = msg.getRootElement().element(segment);
        if (seg == null) {
            return false;
        }
        List<Element> fds = seg.elements(HL7XMLLiterate.TAG_FIELD);
        if (fds.size() <= field) {
            return false;
        }
        Element fd = fds.get(field-1);
        return value.equals(maskNull(fd.getText()));
    }

    private static String maskNull(String s) {
        return s != null ? s : "";
    }
}
