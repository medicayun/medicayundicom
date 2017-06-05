/*  HL7XMLLiterate - interface with static definitions of things important
 *                   to both HL7XMLReader and HL7XMLWriter.
 *
 *  Copyright (C) 2002, 2003 Regenstrief Institute. All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Written and maintained by Gunther Schadow <gschadow@regenstrief.org>
 *  Regenstrief Institute for Health Care
 *  1050 Wishard Blvd., Indianapolis, IN 46202, USA.
 *
 * $Id: HL7XMLLiterate.java 1357 2004-12-30 00:55:56Z gunterze $
 */
package org.regenstrief.xhl7;

/** An interface with static definitions of things important to both
    HL7XMLReader and HL7XMLWriter. Not all standard HL7 things are
    mentioned here, since they are not expected to change much. What
    is mentioned here are our local conventions, tag names, etc.
    that we could decide to change.

    @author Gunther Schadow
    @version $Id: HL7XMLLiterate.java 1357 2004-12-30 00:55:56Z gunterze $ 
*/
public interface HL7XMLLiterate {
  /** Default delimiter characters. */
  static final String DEFAULT_DELIMITERS = "|^~\\&";

  /** Escape sequence characters for delimiters in text. */
  static final String DELIMITER_ESCAPES = "FSRET";

  /** Position of field delimiter in the delimiter string. */
  static final int N_DEL_FIELD = 0;
  /** Position of component delimiter in the delimiter string. */
  static final int N_DEL_COMPONENT = 1;
  /** Position of repeat delimiter in the delimiter string. */
  static final int N_DEL_REPEAT = 2;
  /** Position of escape delimiter in the delimiter string. */
  static final int N_DEL_ESCAPE = 3;
  /** Position of subcomponent delimiter in the delimiter string. */
  static final int N_DEL_SUBCOMPONENT = 4;

  /** Number of delimiters. */
  static final int NUMBER_OF_DELIMITERS = 5;

  /** Namespace URI used by the XML format. */
  static final String NAMESPACE_URI = "";
  // NOTICE: Changing this is NOT trivial, leave it empty unless you
  // want to do real work.

  /** Tag used for the root element in the document. */
  static final String TAG_ROOT = "hl7";
  /** Attribute name for field delimiter. */
  static final String ATT_DEL_FIELD = "fieldDelimiter";
  /** Attribute name for component delimiter. */
  static final String ATT_DEL_COMPONENT = "componentDelimiter";
  /** Attribute name for repeat delimiter. */
  static final String ATT_DEL_REPEAT = "repeatDelimiter";
  /** Attribute name for escape delimiter. */
  static final String ATT_DEL_ESCAPE = "escapeDelimiter";
  /** Attribute name for subcomponent delimiter. */
  static final String ATT_DEL_SUBCOMPONENT = "subcomponentDelimiter";
  /** Tag name for field. */
  static final String TAG_FIELD = "field";
  /** Tag name for component. */
  static final String TAG_COMPONENT = "component";
  /** Tag name for repetition. */
  static final String TAG_REPEAT = "repeat";
  /** Tag name for escape. */
  static final String TAG_ESCAPE = "escape";
  /** Tag name for subcomponent. */
  static final String TAG_SUBCOMPONENT = "subcomponent";

  /** Attribute data type for CDATA. */
  static final String CDATA = "CDATA";
}
