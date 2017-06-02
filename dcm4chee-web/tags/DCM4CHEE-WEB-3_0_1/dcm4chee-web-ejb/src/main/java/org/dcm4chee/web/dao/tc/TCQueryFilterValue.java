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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.web.dao.tc;

import java.io.Serializable;
import java.util.Random;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.entity.Code;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 06, 2011
 */
public abstract class TCQueryFilterValue<T> implements Serializable {
    private T value;

    public TCQueryFilterValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : null;
    }

    public abstract QueryParam[] appendSQLWhereConstraint(TCQueryFilterKey key,
            StringBuilder sb);

    public static TCQueryFilterValue<String> create(String value) {
        return new TCQueryFilterValue<String>(value) {
            @Override
            public QueryParam[] appendSQLWhereConstraint(TCQueryFilterKey key,
                    StringBuilder sb) {
                QueryParam searchStringParam = new QueryParam("searchString",
                        "%" + getValue().replaceAll("\\*","%").toUpperCase() + "%");
                QueryParam conceptNameValueParam = new QueryParam(
                        "conceptNameValue", key.getCode().getCodeValue());
                QueryParam conceptNameDesignatorParam = new QueryParam(
                        "conceptNameDesignator", key.getCode()
                                .getCodingSchemeDesignator());

                sb.append("EXISTS (");
                sb.append("FROM ContentItem content_item");
                sb.append(" INNER JOIN content_item.conceptName concept_name");
                sb.append(" WHERE (instance.sopInstanceUID = content_item.instance.sopInstanceUID)");
                sb.append(" AND (content_item.relationshipType = 'CONTAINS')");
                sb.append(" AND (upper(content_item.textValue) LIKE :"
                        + searchStringParam.getKey() + ")");
                sb.append(" AND (concept_name.codeValue = :"
                        + conceptNameValueParam.getKey() + ")");
                sb.append(" AND (concept_name.codingSchemeDesignator = :"
                        + conceptNameDesignatorParam.getKey() + ")");
                sb.append(")");

                if (key.supportsCodeValue()) {
                    QueryParam conceptCodeMeaningParam = new QueryParam(
                            "conceptCodeMeaning", "%" + getValue().replaceAll("\\*","%").toUpperCase() + "%");

                    sb.append(" OR ");

                    sb.append("EXISTS (");
                    sb.append("FROM ContentItem content_item");
                    sb.append(" INNER JOIN content_item.conceptName concept_name");
                    sb.append(" INNER JOIN content_item.conceptCode concept_code");
                    sb.append(" WHERE (instance.sopInstanceUID = content_item.instance.sopInstanceUID)");
                    sb.append(" AND (content_item.relationshipType = 'CONTAINS')");
                    sb.append(" AND (concept_name.codeValue = :"
                            + conceptNameValueParam.getKey() + ")");
                    sb.append(" AND (concept_name.codingSchemeDesignator = :"
                            + conceptNameDesignatorParam.getKey() + ")");
                    sb.append(" AND (upper(concept_code.codeMeaning) LIKE :"
                            + conceptCodeMeaningParam.getKey() + ")");
                    sb.append(")");

                    return new QueryParam[] { searchStringParam,
                            conceptNameValueParam, conceptNameDesignatorParam,
                            conceptCodeMeaningParam };
                }

                return new QueryParam[] { searchStringParam,
                        conceptNameValueParam, conceptNameDesignatorParam };
            }
        };
    }

    public static TCQueryFilterValue<Code> create(Code code) {
        return new TCQueryFilterValue<Code>(code) {
            @Override
            public QueryParam[] appendSQLWhereConstraint(TCQueryFilterKey key,
                    StringBuilder sb) {
                QueryParam conceptNameValueParam = new QueryParam(
                        "conceptNameValue", key.getCode().getCodeValue());
                QueryParam conceptNameDesignatorParam = new QueryParam(
                        "conceptNameDesignator", key.getCode()
                                .getCodingSchemeDesignator());
                QueryParam conceptCodeValueParam = new QueryParam(
                        "conceptCodeValue", getValue().getCodeValue());
                QueryParam conceptCodeDesignatorParam = new QueryParam(
                        "conceptCodeDesignator", getValue()
                                .getCodingSchemeDesignator());
                QueryParam searchStringParam = new QueryParam("searchString",
                        "%" + getValue().getCodeValue().toUpperCase() + "%");

                sb.append("EXISTS (");
                sb.append("FROM ContentItem content_item");
                sb.append(" INNER JOIN content_item.conceptName concept_name");
                sb.append(" INNER JOIN content_item.conceptCode concept_code");
                sb.append(" WHERE (instance.sopInstanceUID = content_item.instance.sopInstanceUID)");
                sb.append(" AND (content_item.relationshipType = 'CONTAINS')");
                sb.append(" AND (concept_name.codeValue = :"
                        + conceptNameValueParam.getKey() + ")");
                sb.append(" AND (concept_name.codingSchemeDesignator = :"
                        + conceptNameDesignatorParam.getKey() + ")");
                sb.append(" AND (concept_code.codeValue = :"
                        + conceptCodeValueParam.getKey() + ")");
                sb.append(" AND (concept_code.codingSchemeDesignator = :"
                        + conceptCodeDesignatorParam.getKey() + ")");
                sb.append(")");

                sb.append(" OR ");

                sb.append("EXISTS (");
                sb.append("FROM ContentItem content_item");
                sb.append(" INNER JOIN content_item.conceptName concept_name");
                sb.append(" WHERE (instance.sopInstanceUID = content_item.instance.sopInstanceUID)");
                sb.append(" AND (content_item.relationshipType = 'CONTAINS')");
                sb.append(" AND (upper(content_item.textValue) LIKE :"
                        + searchStringParam.getKey() + ")");
                sb.append(" AND (concept_name.codeValue = :"
                        + conceptNameValueParam.getKey() + ")");
                sb.append(" AND (concept_name.codingSchemeDesignator = :"
                        + conceptNameDesignatorParam.getKey() + ")");
                sb.append(")");

                return new QueryParam[] { conceptNameValueParam,
                        conceptNameDesignatorParam, conceptCodeValueParam,
                        conceptCodeDesignatorParam, searchStringParam };
            }
        };
    }

    public static TCQueryFilterValue<Code> create(String designator,
            String value) {
        return create(createCode(designator, value, null));
    }

    public static TCQueryFilterValue<DicomCodeEnum> create(DicomCodeEnum value) {
        return new TCQueryFilterValue<DicomCodeEnum>(value) {
            @Override
            public QueryParam[] appendSQLWhereConstraint(TCQueryFilterKey key,
                    StringBuilder sb) {
                QueryParam conceptNameValueParam = new QueryParam(
                        "conceptNameValue", key.getCode().getCodeValue());
                QueryParam conceptNameDesignatorParam = new QueryParam(
                        "conceptNameDesignator", key.getCode()
                                .getCodingSchemeDesignator());
                QueryParam conceptCodeValueParam = new QueryParam(
                        "conceptCodeValue", getValue().getCode().getCodeValue());
                QueryParam conceptCodeDesignatorParam = new QueryParam(
                        "conceptCodeDesignator", getValue().getCode()
                                .getCodingSchemeDesignator());

                sb.append("EXISTS (");
                sb.append("FROM ContentItem content_item");
                sb.append(" INNER JOIN content_item.conceptName concept_name");
                sb.append(" INNER JOIN content_item.conceptCode concept_code");
                sb.append(" WHERE (instance.sopInstanceUID = content_item.instance.sopInstanceUID)");
                sb.append(" AND (content_item.relationshipType = 'CONTAINS')");
                sb.append(" AND (concept_name.codeValue = :"
                        + conceptNameValueParam.getKey() + ")");
                sb.append(" AND (concept_name.codingSchemeDesignator = :"
                        + conceptNameDesignatorParam.getKey() + ")");
                sb.append(" AND (concept_code.codeValue = :"
                        + conceptCodeValueParam.getKey() + ")");
                sb.append(" AND (concept_code.codingSchemeDesignator = :"
                        + conceptCodeDesignatorParam.getKey() + ")");
                sb.append(")");

                return new QueryParam[] { conceptNameValueParam,
                        conceptNameDesignatorParam, conceptCodeValueParam,
                        conceptCodeDesignatorParam };
            }
        };
    }

    public static TCQueryFilterValue<DicomStringEnum> create(
            DicomStringEnum value) {
        return new TCQueryFilterValue<DicomStringEnum>(value) {
            @Override
            public QueryParam[] appendSQLWhereConstraint(TCQueryFilterKey key,
                    StringBuilder sb) {
                QueryParam searchStringParam = new QueryParam("searchString",
                        getValue().getString().toUpperCase());
                QueryParam conceptNameValueParam = new QueryParam(
                        "conceptNameValue", key.getCode().getCodeValue());
                QueryParam conceptNameDesignatorParam = new QueryParam(
                        "conceptNameDesignator", key.getCode()
                                .getCodingSchemeDesignator());

                sb.append("EXISTS (");
                sb.append("FROM ContentItem content_item");
                sb.append(" INNER JOIN content_item.conceptName concept_name");
                sb.append(" WHERE (instance.sopInstanceUID = content_item.instance.sopInstanceUID)");
                sb.append(" AND (content_item.relationshipType = 'CONTAINS')");
                sb.append(" AND (upper(content_item.textValue) = :"
                        + searchStringParam.getKey() + ")");
                sb.append(" AND (concept_name.codeValue = :"
                        + conceptNameValueParam.getKey() + ")");
                sb.append(" AND (concept_name.codingSchemeDesignator = :"
                        + conceptNameDesignatorParam.getKey() + ")");
                sb.append(")");

                return new QueryParam[] { searchStringParam,
                        conceptNameValueParam, conceptNameDesignatorParam };
            }
        };
    }

    private static Code createCode(String designator, String value,
            String meaning) {
        DicomObject dataset = new BasicDicomObject();
        dataset.putString(Tag.CodingSchemeDesignator, VR.SH, designator);
        dataset.putString(Tag.CodeValue, VR.SH, value);
        dataset.putString(Tag.CodeMeaning, VR.LO, meaning == null ? ""
                : meaning);

        return new Code(dataset);
    }

    private static boolean equals(Code code1, Code code2) {
        return code1.getCodingSchemeDesignator().equals(
                code2.getCodingSchemeDesignator())
                && code1.getCodeValue().equals(code2.getCodeValue());
    }

    public static class QueryParam {
        private static final Random random = new Random();

        private String key;

        private Object value;

        public QueryParam(String key_prefix, Object value) {
            this.key = key_prefix + "_" + random.nextInt(Integer.MAX_VALUE);
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    private static interface DicomCodeEnum {
        public Code getCode();
    }

    private static interface DicomStringEnum {
        public String getString();
    }

    public static enum Level implements DicomCodeEnum {
        Primary("TCE201", "IHERADTF"), Intermediate("TCE202", "IHERADTF"), Advanced(
                "TCE203", "IHERADTF");

        private Code code;

        private Level(String v, String d) {
            code = createCode(d, v, null);
        }

        public Code getCode() {
            return code;
        }

        public static Level get(Code code) {
            if (code != null) {
                for (Level level : values()) {
                    if (TCQueryFilterValue.equals(code, level.getCode())) {
                        return level;
                    }
                }
            }

            return null;
        }
    }

    public static enum YesNo implements DicomCodeEnum {
        Yes("R-0038D", "SRT"), No("R-00339D", "SRT");

        private Code code;

        private YesNo(String v, String d) {
            code = createCode(d, v, null);
        }

        public Code getCode() {
            return code;
        }

        public static YesNo get(Code code) {
            if (code != null) {
                for (YesNo yesNo : values()) {
                    if (TCQueryFilterValue.equals(code, yesNo.getCode())) {
                        return yesNo;
                    }
                }
            }

            return null;
        }
    }

    public static enum PatientSex implements DicomStringEnum {
        Male("M"), Female("F"), Other("O"), Unknown("U");

        private String value;

        private PatientSex(String v) {
            this.value = v;
        }

        public String getString() {
            return value;
        }

        public static PatientSex get(String value) {
            if (value != null) {
                for (PatientSex sex : values()) {
                    if (value.equals(sex.getString())) {
                        return sex;
                    }
                }
            }

            return null;
        }
    }

    public static enum AcquisitionModality implements DicomCodeEnum {
        CR("DCM", "CR"),
        CT("DCM", "CT"),
        DG("DCM", "DG"),
        DX("DCM", "DX"),
        ECG("DCM", "ECG"),
        ES("DCM", "ES"),
        GM("DCM", "GM"),
        IVUS("DCM", "IVUS"),
        MG("DCM", "MG"),
        MR("DCM", "MR"),
        NM("DCM", "NM"),
        PT("DCM", "PT"),
        RF("DCM", "RF"),
        RG("DCM", "RG"),
        SM("DCM", "SM"),
        US("DCM", "US"),
        XA("DCM", "XA"),
        XC("DCM", "XC");

        private Code code;

        private AcquisitionModality(String d, String v) {
            this.code = createCode(d, v, null);
        }

        public Code getCode() {
            return code;
        }

        public static AcquisitionModality get(Code code) {
            if (code != null) {
                for (AcquisitionModality mod : values()) {
                    if (TCQueryFilterValue.equals(code, mod.getCode())) {
                        return mod;
                    }
                }
            }

            return null;
        }
    }

    public static enum Purpose implements DicomCodeEnum {
        ForTeaching("TCE001", "IHERADTF"), ForClinicalTrial("TCE002",
                "IHERADTF"), ForResearch("TCE007", "IHERADTF"), ForPublication(
                "TCE008", "IHERADTF");

        private Code code;

        private Purpose(String v, String d) {
            code = createCode(d, v, null);
        }

        public Code getCode() {
            return code;
        }

        public static Purpose get(Code code) {
            if (code != null) {
                for (Purpose purpose : values()) {
                    if (TCQueryFilterValue.equals(code, purpose.getCode())) {
                        return purpose;
                    }
                }
            }

            return null;
        }
    }

    public static enum Category implements DicomCodeEnum {
        Musculoskeletal("TCE301", "IHERADTF"), Pulmonary("TCE302", "IHERADTF"), Cardiovascular(
                "TCE303", "IHERADTF"), Gastrointestinal("TCE304", "IHERADTF"), Genitourinary(
                "TCE305", "IHERADTF"), Neuro("TCE306", "IHERADTF"), Nuclear(
                "TCE308", "IHERADTF"), Ultrasound("TCE309", "IHERADTF"), VascularAndInterventional(
                "TCE307", "IHERADTF"), Pediatric("TCE310", "IHERADTF"), Breast(
                "TCE311", "IHERADTF");

        private Code code;

        private Category(String v, String d) {
            code = createCode(d, v, null);
        }

        public Code getCode() {
            return code;
        }

        public static Category get(Code code) {
            if (code != null) {
                for (Category category : values()) {
                    if (TCQueryFilterValue.equals(code, category.getCode())) {
                        return category;
                    }
                }
            }

            return null;
        }
    }

}