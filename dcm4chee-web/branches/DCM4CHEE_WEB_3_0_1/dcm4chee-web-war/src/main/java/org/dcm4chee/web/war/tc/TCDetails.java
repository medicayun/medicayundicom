package org.dcm4chee.web.war.tc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.AcquisitionModality;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Category;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Level;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.PatientSex;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.YesNo;
import org.dcm4chee.web.war.folder.delegate.TarRetrieveDelegate;
import org.dcm4chee.web.war.folder.delegate.WADODelegate;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static Logger log = LoggerFactory.getLogger(TCDetails.class);

    private String abstr;

    private List<AcquisitionModality> acquisitionModalities;

    private String anatomy;

    private String authorAffiliation;

    private String authorContact;

    private String authorName;

    private Category category;

    private String diagnosis;

    private YesNo diagnosisConfirmed;

    private String diffDiagnosis;

    private String discussion;

    private String finding;

    private String history;

    private List<String> keywords;

    private Level level;

    private String organSystem;

    private String pathology;

    private PatientSex patientSex;

    private String patientSpecies;

    private List<String> bibliographicReferences;

    private String title;

    private DicomCode anatomyCode;

    private DicomCode diagnosisCode;

    private DicomCode diffDiagnosisCode;

    private DicomCode findingCode;

    private DicomCode keywordCode;

    private DicomCode pathologyCode;

    private List<InstanceRef> instanceRefs;

    private TCDetails(DicomObject object) {
        parse(object);
    }

    public static TCDetails create(TCModel model) throws IOException {
        String fsID = model.getFileSystemId();
        String fileID = model.getFileId();

        DicomInputStream dis = null;

        try {
            dis = new DicomInputStream(
                    fsID.startsWith("tar:") ? TarRetrieveDelegate.getInstance()
                            .retrieveFileFromTar(fsID, fileID)
                            : new java.io.File(fsID, fileID));

            return new TCDetails(dis.readDicomObject());
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }

    public String getAbstr() {
        return abstr;
    }

    public List<AcquisitionModality> getAcquisitionModalities() {
        return acquisitionModalities;
    }

    public String getAnatomy() {
        return anatomy;
    }

    public String getAuthorAffiliation() {
        return authorAffiliation;
    }

    public String getAuthorContact() {
        return authorContact;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Category getCategory() {
        return category;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public YesNo getDiagnosisConfirmed() {
        return diagnosisConfirmed;
    }

    public String getDiffDiagnosis() {
        return diffDiagnosis;
    }

    public String getDiscussion() {
        return discussion;
    }

    public String getFinding() {
        return finding;
    }

    public String getHistory() {
        return history;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Level getLevel() {
        return level;
    }

    public String getOrganSystem() {
        return organSystem;
    }

    public String getPathology() {
        return pathology;
    }

    public PatientSex getPatientSex() {
        return patientSex;
    }

    public String getPatientSpecies() {
        return patientSpecies;
    }

    public List<String> getBibliographicReferences() {
        if (bibliographicReferences != null) {
            return Collections.unmodifiableList(bibliographicReferences);
        } else {
            return Collections.emptyList();
        }
    }

    public String getTitle() {
        return title;
    }

    public DicomCode getAnatomyCode() {
        return anatomyCode;
    }

    public DicomCode getDiagnosisCode() {
        return diagnosisCode;
    }

    public DicomCode getDiffDiagnosisCode() {
        return diffDiagnosisCode;
    }

    public DicomCode getFindingCode() {
        return findingCode;
    }

    public DicomCode getKeywordCode() {
        return keywordCode;
    }

    public DicomCode getPathologyCode() {
        return pathologyCode;
    }

    public List<InstanceRef> getReferencedInstances() {
        if (instanceRefs != null) {
            return Collections.unmodifiableList(instanceRefs);
        }

        return Collections.emptyList();
    }

    @SuppressWarnings({ "unchecked" })
    public List<InstanceRef> getReferencedImages() {
        List<InstanceRef> refs = instanceRefs != null ? new ArrayList<InstanceRef>(
                instanceRefs) : (List) Collections.emptyList();

        for (Iterator<InstanceRef> it = refs.iterator(); it.hasNext();) {
            if (!it.next().isImage()) {
                it.remove();
            }
        }

        return refs;
    }

    public String getLocalizedStringValue(TCQueryFilterKey key, Component c) {
        return this.getLocalizedStringValue(key, c, null);
    }

    public String getLocalizedStringValue(TCQueryFilterKey key, Component c,
            TCKeywordCatalogueProvider catProv) {
        if (TCQueryFilterKey.Category.equals(key)) {
            return getCategory() != null ? c.getString("tc.category."
                    + getCategory().name().toLowerCase()) : null;
        } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
            return getDiagnosisConfirmed() != null ? c.getString("tc.yesno."
                    + getDiagnosisConfirmed().name().toLowerCase()) : null;
        } else if (TCQueryFilterKey.Level.equals(key)) {
            return getLevel() != null ? c.getString("tc.level."
                    + getLevel().name().toLowerCase()) : null;
        } else if (TCQueryFilterKey.PatientSex.equals(key)) {
            return getPatientSex() != null ? c.getString("tc.patientsex."
                    + getPatientSex().name().toLowerCase()) : null;
        } else {
            return getStringValue(key, catProv);
        }
    }

    public String getStringValue(TCQueryFilterKey key) {
        return getStringValue(key);
    }

    public String getStringValue(TCQueryFilterKey key,
            TCKeywordCatalogueProvider catProv) {
        String s = null;

        if (TCQueryFilterKey.Abstract.equals(key)) {
            s = getAbstr();
        } else if (TCQueryFilterKey.AcquisitionModality.equals(key)) {
            s = concatStrings(getAcquisitionModalities());
        } else if (TCQueryFilterKey.Anatomy.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getAnatomy() != null) {
                    s = getAnatomy();
                }
            }

            if (s == null) {
                s = getCodeAsString(getAnatomyCode());
            }
        } else if (TCQueryFilterKey.AuthorAffiliation.equals(key)) {
            s = getAuthorAffiliation();
        } else if (TCQueryFilterKey.AuthorContact.equals(key)) {
            s = getAuthorContact();
        } else if (TCQueryFilterKey.AuthorName.equals(key)) {
            s = getAuthorName();
        } else if (TCQueryFilterKey.BibliographicReference.equals(key)) {
            s = concatStrings(this.getBibliographicReferences());
        } else if (TCQueryFilterKey.Category.equals(key)) {
            s = getCategory() != null ? getCategory().toString() : null;
        } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
            s = getDiagnosisConfirmed() != null ? getDiagnosisConfirmed()
                    .toString() : null;
        } else if (TCQueryFilterKey.Diagnosis.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getDiagnosis() != null) {
                    s = getDiagnosis();
                }
            }

            if (s == null) {
                s = getCodeAsString(getDiagnosisCode());
            }
        } else if (TCQueryFilterKey.DifferentialDiagnosis.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getDiffDiagnosis() != null) {
                    s = getDiffDiagnosis();
                }
            }

            if (s == null) {
                s = getCodeAsString(getDiffDiagnosisCode());
            }
        } else if (TCQueryFilterKey.Discussion.equals(key)) {
            s = getDiscussion();
        } else if (TCQueryFilterKey.Finding.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getFinding() != null) {
                    s = getFinding();
                }
            }

            if (s == null) {
                s = getCodeAsString(getFindingCode());
            }
        } else if (TCQueryFilterKey.History.equals(key)) {
            s = getHistory();
        } else if (TCQueryFilterKey.Keyword.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getKeywords() != null) {
                    s = concatStrings(getKeywords());
                }
            }

            if (s == null) {
                s = getCodeAsString(getKeywordCode());
            }
        } else if (TCQueryFilterKey.Level.equals(key)) {
            s = getLevel() != null ? getLevel().toString() : null;
        } else if (TCQueryFilterKey.OrganSystem.equals(key)) {
            s = getOrganSystem();

        } else if (TCQueryFilterKey.Pathology.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getPathology() != null) {
                    s = getPathology();
                }
            }

            if (s == null) {
                s = getCodeAsString(getPathologyCode());
            }
        } else if (TCQueryFilterKey.PatientSex.equals(key)) {
            s = getPatientSex() != null ? getPatientSex().toString() : null;
        } else if (TCQueryFilterKey.PatientSpecies.equals(key)) {
            s = getPatientSpecies();
        } else if (TCQueryFilterKey.Title.equals(key)) {
            s = getTitle();
        }

        return s;
    }

    private String getCodeAsString(DicomCode code) {
        return code != null ? code.toString() : null;
    }

    private String concatStrings(List<?> list) {
        if (list != null) {
            Iterator<?> it = list.iterator();
            StringBuilder sbuilder = new StringBuilder();
            if (it.hasNext())
                sbuilder.append(it.next());
            while (it.hasNext()) {
                sbuilder.append(", ");
                sbuilder.append(it.next().toString());
            }
            return sbuilder.toString();
        }

        return null;
    }

    private void parse(DicomObject o) {
        DicomElement content = o != null ? o.get(Tag.ContentSequence) : null;
        DicomElement ref = o != null ? o
                .get(Tag.CurrentRequestedProcedureEvidenceSequence) : null;

        int refCount = ref != null ? ref.countItems() : -1;
        if (refCount > 0) {
            for (int i = 0; i < refCount; i++) {
                DicomObject studyRef = ref.getDicomObject(i);
                String stuid = studyRef != null ? studyRef
                        .getString(Tag.StudyInstanceUID) : null;

                if (stuid != null) {
                    DicomElement seriesSeq = studyRef
                            .get(Tag.ReferencedSeriesSequence);
                    int seriesCount = seriesSeq != null ? seriesSeq
                            .countItems() : -1;

                    if (seriesCount > 0) {
                        for (int j = 0; j < seriesCount; j++) {
                            DicomObject seriesRef = seriesSeq.getDicomObject(j);
                            String suid = seriesRef != null ? seriesRef
                                    .getString(Tag.SeriesInstanceUID) : null;

                            if (suid != null) {
                                DicomElement instanceSeq = seriesRef
                                        .get(Tag.ReferencedSOPSequence);
                                int instanceCount = instanceSeq != null ? instanceSeq
                                        .countItems() : -1;

                                if (instanceCount > 0) {
                                    for (int k = 0; k < instanceCount; k++) {
                                        DicomObject instanceRef = instanceSeq
                                                .getDicomObject(k);
                                        String iuid = instanceRef
                                                .getString(Tag.ReferencedSOPInstanceUID);
                                        String cuid = instanceRef
                                                .getString(Tag.ReferencedSOPClassUID);

                                        InstanceRef iref = new InstanceRef(
                                                stuid, suid, iuid, cuid);

                                        if (instanceRefs == null) {
                                            instanceRefs = new ArrayList<InstanceRef>();
                                        }

                                        instanceRefs.add(iref);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (content != null) {
            int count = content.countItems();
            for (int i = 0; i < count; i++) {
                try {
                    DicomObject item = content.getDicomObject(i);

                    if (item != null) {
                        String valueType = item.getString(Tag.ValueType);
                        DicomCode conceptName = new DicomCode(
                                item.getNestedDicomObject(Tag.ConceptNameCodeSequence));

                        if ("TEXT".equalsIgnoreCase(valueType)) {
                            if (conceptName.equals(TCQueryFilterKey.Abstract
                                    .getCode())) {
                                abstr = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Anatomy.getCode())) {
                                anatomy = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AuthorName
                                            .getCode())) {
                                authorName = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AuthorContact
                                            .getCode())) {
                                authorContact = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AuthorAffiliation
                                            .getCode())) {
                                authorAffiliation = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.BibliographicReference
                                            .getCode())) {
                                String s = getTextValue(item);
                                if (s != null) {
                                    if (bibliographicReferences == null) {
                                        bibliographicReferences = new ArrayList<String>();
                                    }

                                    bibliographicReferences.add(s);
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Diagnosis
                                            .getCode())) {
                                diagnosis = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DifferentialDiagnosis
                                            .getCode())) {
                                diffDiagnosis = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Discussion
                                            .getCode())) {
                                discussion = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Finding.getCode())) {
                                finding = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.History.getCode())) {
                                history = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Keyword.getCode())) {
                                String keyword = getTextValue(item);

                                if (keyword != null) {
                                    if (keywords == null) {
                                        keywords = new ArrayList<String>();
                                    }

                                    if (!keywords.contains(keyword)) {
                                        keywords.add(keyword);
                                    }
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.OrganSystem
                                            .getCode())) {
                                organSystem = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Pathology
                                            .getCode())) {
                                pathology = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.PatientSex
                                            .getCode())) {
                                patientSex = PatientSex.get(getTextValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.PatientSpecies
                                            .getCode())) {
                                patientSpecies = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Title.getCode())) {
                                title = getTextValue(item);
                            }
                        } else if ("CODE".equalsIgnoreCase(valueType)) {
                            if (conceptName.equals(TCQueryFilterKey.Category
                                    .getCode())) {
                                category = Category.get(getCodeValue(item)
                                        .toCode());
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AcquisitionModality
                                            .getCode())) {
                                AcquisitionModality m = AcquisitionModality
                                        .get(getCodeValue(item).toCode());

                                if (m != null) {
                                    if (acquisitionModalities == null) {
                                        acquisitionModalities = new ArrayList<AcquisitionModality>();
                                    }

                                    if (!acquisitionModalities.contains(m)) {
                                        acquisitionModalities.add(m);
                                    }
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DiagnosisConfirmed
                                            .getCode())) {
                                diagnosisConfirmed = YesNo.get(getCodeValue(
                                        item).toCode());
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Level.getCode())) {
                                level = Level.get(getCodeValue(item).toCode());
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Anatomy.getCode())) {
                                anatomyCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Diagnosis
                                            .getCode())) {
                                diagnosisCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DifferentialDiagnosis
                                            .getCode())) {
                                diffDiagnosisCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Finding.getCode())) {
                                findingCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Keyword.getCode())) {
                                keywordCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Pathology
                                            .getCode())) {
                                pathologyCode = getCodeValue(item);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Parsing TC attribute failed! Skipped...", e);
                }
            }
        }
    }

    private String getTextValue(DicomObject object) {
        return object != null ? object.getString(Tag.TextValue) : null;
    }

    private DicomCode getCodeValue(DicomObject object) {
        return object != null ? new DicomCode(
                object.getNestedDicomObject(Tag.ConceptCodeSequence)) : null;
    }

    public static class InstanceRef implements Serializable {

        private static final long serialVersionUID = 1L;

        private String stuid;

        private String suid;

        private String iuid;

        private String cuid;

        public InstanceRef(String stuid, String suid, String iuid, String cuid) {
            this.stuid = stuid;
            this.suid = suid;
            this.iuid = iuid;
            this.cuid = cuid;
        }

        public String getStudyUID() {
            return stuid;
        }

        public String getSeriesUID() {
            return suid;
        }

        public String getInstanceUID() {
            return iuid;
        }

        public String getClassUID() {
            return cuid;
        }

        public boolean isImage() {
            return WADODelegate.IMAGE == WADODelegate.getInstance()
                    .getRenderType(cuid);
        }
    }

    public static class DicomCode implements Serializable {

        private static final long serialVersionUID = 1L;

        private String value;

        private String designator;

        private String meaning;

        private String version;

        public DicomCode(String designator, String value, String meaning,
                String version) {
            this.value = value;
            this.designator = designator;
            this.meaning = meaning;
            this.version = version;
        }

        public DicomCode(String designator, String value, String meaning) {
            this(designator, value, meaning, null);
        }

        public DicomCode(DicomObject dataset) {
            this.value = dataset.getString(Tag.CodeValue);
            this.designator = dataset.getString(Tag.CodingSchemeDesignator);
            this.meaning = dataset.getString(Tag.CodeMeaning);
            this.version = dataset.getString(Tag.CodingSchemeVersion);
        }

        public static DicomCode fromString(String designator, String s) {
            if (designator != null && s != null) {
                if (s.startsWith("(")) {
                    String value = s.substring(1, s.indexOf(")"));
                    String meaning = s.substring(s.indexOf(")") + 2);

                    return new DicomCode(designator, value, meaning);
                } else {
                    return new DicomCode(designator, s, null);
                }
            }

            return null;
        }

        public String getValue() {
            return value;
        }

        public String getDesignator() {
            return designator;
        }

        public String getMeaning() {
            return meaning;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            StringBuffer sbuf = new StringBuffer();

            if (meaning != null) {
                sbuf.append("(");
            }

            sbuf.append(value);

            if (meaning != null) {
                sbuf.append(") ");
                sbuf.append(meaning);
            }

            return sbuf.toString();
        }

        public Code toCode() {
            DicomObject dataset = new BasicDicomObject();
            dataset.putString(Tag.CodingSchemeDesignator, VR.SH, designator);
            dataset.putString(Tag.CodeValue, VR.SH, value);
            dataset.putString(Tag.CodeMeaning, VR.LO, meaning == null ? ""
                    : meaning);

            if (version != null) {
                dataset.putString(Tag.CodingSchemeVersion, null, version);
            }

            return new Code(dataset);
        }

        public boolean equals(Code code) {
            if (code != null) {
                return value.equals(code.getCodeValue())
                        && designator.equals(code.getCodingSchemeDesignator())
                        && (version == null || version.equals(code
                                .getCodingSchemeVersion()));
            }

            return false;
        }
    }
}
