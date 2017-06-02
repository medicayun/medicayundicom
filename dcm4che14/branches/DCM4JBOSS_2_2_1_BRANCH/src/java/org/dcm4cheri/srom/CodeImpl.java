/*$Id: CodeImpl.java 3576 2002-09-27 10:23:05Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.Code;

import org.apache.log4j.Logger;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class CodeImpl implements Code {
    // Constants -----------------------------------------------------
    static final Code[] EMPTY_ARRAY = {};
    private static final int MEANING_PROMPT_LEN = 32;

    // Attributes ----------------------------------------------------
    static Logger log = Logger.getLogger(CodeImpl.class);
    
    private final String codeValue;
    private final String codingSchemeDesignator;
    private final String codingSchemeVersion;
    private final String codeMeaning;

    // Constructors --------------------------------------------------
    private String check(String val, String prompt, String def) {
        if (val == null || val.length() == 0) {
            log.warn(prompt);
            return def;
        }
        return val;
    }
    
    public CodeImpl(
        String codeValue,
        String codingSchemeDesignator,
        String codingSchemeVersion,
        String codeMeaning)
    {
        this.codeValue = check(codeValue,
            "Missing (0008,0100) Code Value - insert \"__NULL__\"",
            "__NULL__");
        this.codingSchemeDesignator = check(codingSchemeDesignator,
            "Missing (0008,0102) Coding Scheme Designator - insert \"99NULL\"",
            "99NULL");
        this.codingSchemeVersion = codingSchemeVersion;
        this.codeMeaning = check(codeMeaning,
            "Missing (0008,0104) Code Meaning - insert \"__NULL__\"",
            "__NULL__");
    }

    public CodeImpl(Dataset ds) throws DcmValueException { 
        this(ds.getString(Tags.CodeValue),
            ds.getString(Tags.CodingSchemeDesignator),
            ds.getString(Tags.CodingSchemeVersion),
            ds.getString(Tags.CodeMeaning));
    }
    
    public static Code newCode(Dataset ds) throws DcmValueException {
        return ds != null ? new CodeImpl(ds) : null;
    }
        
    public static Code[] newCodes(DcmElement sq) throws DcmValueException {
        if (sq == null || !sq.isEmpty())
            return EMPTY_ARRAY;
        Code[] a = new Code[sq.vm()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = new CodeImpl(sq.getItem(i));
        }
        return a;
    }
    // Methodes ------------------------------------------------------
    public final String getCodeValue() {
        return codeValue;
    }
    public final String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }
    public final String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }
    public final String getCodeMeaning() {
        return codeMeaning;
    }
    
    //compares code value,coding scheme designator only
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CodeImpl))
            return false;
        CodeImpl o = (CodeImpl)obj;
        if (!codeValue.equals(o.codeValue))
            return false;
        if (!codingSchemeDesignator.equals(o.codingSchemeDesignator))
            return false;
        return (codingSchemeVersion == null
                ? o.codingSchemeVersion == null
                : codingSchemeVersion.equals(o.codingSchemeVersion));
    }        

    public int hashCode() { return codeValue.hashCode(); }
    
    public String toString() {
        return "(" + codeValue
             + "," + codingSchemeDesignator
             + ",\"" + (codeMeaning.length() <= MEANING_PROMPT_LEN ? codeMeaning
                        : (codeMeaning.substring(0,MEANING_PROMPT_LEN) + ".."))
             + "\")";
    }

    public void toDataset(Dataset ds) {
        ds.putSH(Tags.CodeValue, codeValue);
        ds.putSH(Tags.CodingSchemeDesignator, codingSchemeDesignator);
        if (codingSchemeVersion != null) {
            ds.putSH(Tags.CodingSchemeVersion, codingSchemeVersion);
        }
        ds.putLO(Tags.CodeMeaning, codeMeaning);
     }    
}
