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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.AcquisitionModality;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Category;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Level;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.PatientSex;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.YesNo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 05, 2011
 */
public class TCQueryFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(TCQueryFilter.class);

    private Map<TCQueryFilterKey, TCQueryFilterValue<?>> values;

    public void clear() {
        if (values != null) {
            values.clear();
            values = null;
        }
    }

    public Set<Entry<TCQueryFilterKey, TCQueryFilterValue<?>>> getEntries() {
        return values != null ? values.entrySet() : null;
    }

    public String getAbstract() {
        return (String) getValue(TCQueryFilterKey.Abstract);
    }

    public void setAbstract(String abstr) {
        putValue(TCQueryFilterKey.Abstract, abstr);
    }

    public AcquisitionModality getAcquisitionModality() {
        return (AcquisitionModality) getValue(TCQueryFilterKey.AcquisitionModality);
    }

    public void setAcquisitionModality(AcquisitionModality acquisitionModality) {
        putValue(
                TCQueryFilterKey.AcquisitionModality,
                acquisitionModality != null ? TCQueryFilterValue
                        .create(acquisitionModality) : null);
    }

    public String getAnatomy() {
        try {
            return (String) getValue(TCQueryFilterKey.Anatomy);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'anatomy' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setAnatomy(String anatomy) {
        putValue(TCQueryFilterKey.Anatomy, anatomy);
    }

    public String getAuthorAffiliation() {
        return (String) getValue(TCQueryFilterKey.AuthorAffiliation);
    }

    public void setAuthorAffiliation(String authorAffiliation) {
        putValue(TCQueryFilterKey.AuthorAffiliation, authorAffiliation);
    }

    public String getAuthorContact() {
        return (String) getValue(TCQueryFilterKey.AuthorContact);
    }

    public void setAuthorContact(String authorContact) {
        putValue(TCQueryFilterKey.AuthorContact, authorContact);
    }

    public String getAuthorName() {
        return (String) getValue(TCQueryFilterKey.AuthorName);
    }

    public void setAuthorName(String authorName) {
        putValue(TCQueryFilterKey.AuthorName, authorName);
    }

    public Category getCategory() {
        return (Category) getValue(TCQueryFilterKey.Category);
    }

    public void setCategory(Category category) {
        putValue(TCQueryFilterKey.Category,
                category != null ? TCQueryFilterValue.create(category) : null);
    }

    public String getDiagnosis() {
        try {
            return (String) getValue(TCQueryFilterKey.Diagnosis);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'diagnosis' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setDiagnosis(String diagnosis) {
        putValue(TCQueryFilterKey.Diagnosis, diagnosis);
    }

    public YesNo getDiagnosisConfirmed() {
        return (YesNo) getValue(TCQueryFilterKey.DiagnosisConfirmed);
    }

    public void setDiagnosisConfirmed(YesNo diagnosisConfirmed) {
        putValue(
                TCQueryFilterKey.DiagnosisConfirmed,
                diagnosisConfirmed != null ? TCQueryFilterValue
                        .create(diagnosisConfirmed) : null);
    }

    public String getDiffDiagnosis() {
        try {
            return (String) getValue(TCQueryFilterKey.DifferentialDiagnosis);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'diff-diagnosis' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setDiffDiagnosis(String diffDiagnosis) {
        putValue(TCQueryFilterKey.DifferentialDiagnosis, diffDiagnosis);
    }

    public String getDiscussion() {
        return (String) getValue(TCQueryFilterKey.Discussion);
    }

    public void setDiscussion(String discussion) {
        putValue(TCQueryFilterKey.Discussion, discussion);
    }

    public String getFinding() {
        try {
            return (String) getValue(TCQueryFilterKey.Finding);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'finding' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setFinding(String finding) {
        putValue(TCQueryFilterKey.Finding, finding);
    }

    public String getHistory() {
        return (String) getValue(TCQueryFilterKey.History);
    }

    public void setHistory(String history) {
        putValue(TCQueryFilterKey.History, history);
    }

    public String getKeyword() {
        try {
            return (String) getValue(TCQueryFilterKey.Keyword);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'keyword' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setKeyword(String keyword) {
        putValue(TCQueryFilterKey.Keyword, keyword);
    }

    public Level getLevel() {
        return (Level) getValue(TCQueryFilterKey.Level);
    }

    public void setLevel(Level level) {
        putValue(TCQueryFilterKey.Level,
                level != null ? TCQueryFilterValue.create(level) : null);
    }

    public String getOrganSystem() {
        try {
            return (String) getValue(TCQueryFilterKey.OrganSystem);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'organ-system' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setOrganSystem(String organSystem) {
        putValue(TCQueryFilterKey.OrganSystem, organSystem);
    }

    public String getPathology() {
        try {
            return (String) getValue(TCQueryFilterKey.Pathology);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'pathology' is not of type String. Returning null...",
                    e);

            return null;
        }
    }

    public void setPathology(String pathology) {
        putValue(TCQueryFilterKey.Pathology, pathology);
    }

    public PatientSex getPatientSex() {
        return (PatientSex) getValue(TCQueryFilterKey.PatientSex);
    }

    public void setPatientSex(PatientSex patientSex) {
        putValue(TCQueryFilterKey.PatientSex,
                patientSex != null ? TCQueryFilterValue.create(patientSex)
                        : null);
    }

    public String getPatientSpecies() {
        return (String) getValue(TCQueryFilterKey.PatientSpecies);
    }

    public void setPatientSpecies(String patientSpecies) {
        putValue(TCQueryFilterKey.PatientSpecies, patientSpecies);
    }

    public String getBibliographicReference() {
        return (String) getValue(TCQueryFilterKey.BibliographicReference);
    }

    public void setBibliographicReference(String bibliographicReference) {
        putValue(TCQueryFilterKey.BibliographicReference,
                bibliographicReference);
    }

    public String getTitle() {
        return (String) getValue(TCQueryFilterKey.Title);
    }

    public void setTitle(String title) {
        putValue(TCQueryFilterKey.Title, title);
    }

    public Code getAnatomyCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.Anatomy);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'anatomy' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setAnatomyCode(Code anatomyCode) {
        putValue(TCQueryFilterKey.Anatomy,
                anatomyCode != null ? TCQueryFilterValue.create(anatomyCode)
                        : null);
    }

    public Code getDiagnosisCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.Diagnosis);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'diagnosis' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setDiagnosisCode(Code diagnosisCode) {
        putValue(
                TCQueryFilterKey.Diagnosis,
                diagnosisCode != null ? TCQueryFilterValue
                        .create(diagnosisCode) : null);
    }

    public Code getDiffDiagnosisCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.DifferentialDiagnosis);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'diff-diagnosis' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setDiffDiagnosisCode(Code diffDiagnosisCode) {
        putValue(
                TCQueryFilterKey.DifferentialDiagnosis,
                diffDiagnosisCode != null ? TCQueryFilterValue
                        .create(diffDiagnosisCode) : null);
    }

    public Code getFindingCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.Finding);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'finding' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setFindingCode(Code findingCode) {
        putValue(TCQueryFilterKey.Finding,
                findingCode != null ? TCQueryFilterValue.create(findingCode)
                        : null);
    }

    public Code getKeywordCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.Keyword);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'keyword' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setKeywordCode(Code keywordCode) {
        putValue(TCQueryFilterKey.Keyword,
                keywordCode != null ? TCQueryFilterValue.create(keywordCode)
                        : null);
    }

    public Code getOrganSystemCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.OrganSystem);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'organ-system' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setOrganSystemCode(Code organSystemCode) {
        putValue(
                TCQueryFilterKey.OrganSystem,
                organSystemCode != null ? TCQueryFilterValue
                        .create(organSystemCode) : null);
    }

    public Code getPathologyCode() {
        try {
            return (Code) getValue(TCQueryFilterKey.Pathology);
        } catch (ClassCastException e) {
            log.warn(
                    "TC property 'pathology' not of type Code. Returning null...",
                    e);

            return null;
        }
    }

    public void setPathologyCode(Code pathologyCode) {
        putValue(
                TCQueryFilterKey.Pathology,
                pathologyCode != null ? TCQueryFilterValue
                        .create(pathologyCode) : null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TC search for ");

        if (values != null && !values.isEmpty()) {
            boolean first = true;
            sb.append(values.size() + " attributes (");
            for (Map.Entry<TCQueryFilterKey, TCQueryFilterValue<?>> me : values
                    .entrySet()) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }

                sb.append(me.getKey().name());
                sb.append("=");
                sb.append("'" + me.getValue() + "'");
            }
            sb.append(")");
        } else {
            sb.append("0 attributes");
        }

        return sb.toString();
    }

    public Object getValue(TCQueryFilterKey key) {
        TCQueryFilterValue<?> value = values != null ? values.get(key) : null;

        return value != null ? value.getValue() : null;
    }

    private void putValue(TCQueryFilterKey key, String value) {
        putValue(
                key,
                value != null && value.length() > 0 ? TCQueryFilterValue
                        .create(value) : null);
    }

    private void putValue(TCQueryFilterKey key, TCQueryFilterValue<?> value) {
        if (value == null) {
            if (values != null) {
                values.remove(key);

                if (values.isEmpty()) {
                    values = null;
                }
            }
        } else {
            if (values == null) {
                values = new HashMap<TCQueryFilterKey, TCQueryFilterValue<?>>(3);
            }

            values.put(key, value);
        }
    }

}
