package org.dcm4chee.web.war.tc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.DateUtils;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.web.common.util.FileUtils;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.AcquisitionModality;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Category;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Level;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.PatientSex;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.YesNo;
import org.dcm4chee.web.war.folder.delegate.TarRetrieveDelegate;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCEditableObject extends TCObject {
	
	private static final long serialVersionUID = 1L;

	private final static Logger log = LoggerFactory.getLogger(TCEditableObject.class);

    private DicomObject ds;
    private boolean modified;
    
    private TCEditableObject(DicomObject ds)
    {
        super(ds);
        this.ds = ds;
    }
    
    public static TCEditableObject create(TCModel model) throws IOException {
        String fsID = model.getFileSystemId();
        String fileID = model.getFileId();

        DicomInputStream dis = null;
        try {
        	dis = new DicomInputStream(fsID.startsWith("tar:") ? 
        			TarRetrieveDelegate.getInstance().retrieveFileFromTar(fsID, fileID) :
        				FileUtils.resolve(new File(fsID, fileID)));
            return new TCEditableObject(dis.readDicomObject());
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }
    
    public boolean isModified()
    {
        return modified;
    }
    
    public void setAbstract(String abstr) {
        if (!TCUtilities.equals(this.abstr, abstr))
        {
            this.abstr = abstr;
            this.modified = true;
        }
    }

    public void setAcquisitionModalities(
            List<AcquisitionModality> acquisitionModalities) {
        if (!TCUtilities.equals(this.acquisitionModalities, acquisitionModalities))
        {
            this.acquisitionModalities = acquisitionModalities;
            this.modified = true;
        }
    }

    public void setAnatomy(ITextOrCode anatomy) {
        if (!TCUtilities.equals(this.anatomy, anatomy))
        {
            this.anatomy = anatomy;
            this.modified = true;
        }
    }

    public void setAuthorAffiliation(String authorAffiliation) {
        if (!TCUtilities.equals(this.authorAffiliation, authorAffiliation))
        {
            this.authorAffiliation = authorAffiliation;
            this.modified = true;
        }
    }

    public void setAuthorContact(String authorContact) {
        if (!TCUtilities.equals(this.authorContact, authorContact))
        {
            this.authorContact = authorContact;
            this.modified = true;
        }
    }

    public void setAuthorName(String authorName) {
        if (!TCUtilities.equals(this.authorName, authorName))
        {
            this.authorName = authorName;
            this.modified = true;
        }
    }

    public void setCategory(Category category) {
        if (!TCUtilities.equals(this.category, category))
        {
            this.category = category;
            this.modified = true;
        }
    }

    public void setDiagnosis(ITextOrCode diagnosis) {
        if (!TCUtilities.equals(this.diagnosis, diagnosis))
        {
            this.diagnosis = diagnosis;
            this.modified = true;
        }
    }

    public void setDiagnosisConfirmed(YesNo diagnosisConfirmed) {
        if (!TCUtilities.equals(this.diagnosisConfirmed, diagnosisConfirmed))
        {
            this.diagnosisConfirmed = diagnosisConfirmed;
            this.modified = true;
        }
    }

    public void setDiffDiagnosis(ITextOrCode diffDiagnosis) {
        if (!TCUtilities.equals(this.diffDiagnosis, diffDiagnosis))
        {
            this.diffDiagnosis = diffDiagnosis;
            this.modified = true;
        }
    }

    public void setDiscussion(String discussion) {
        if (!TCUtilities.equals(this.discussion, discussion))
        {
            this.discussion = discussion;
            this.modified = true;
        }
    }

    public void setFinding(ITextOrCode finding) {
        if (!TCUtilities.equals(this.finding, finding)) {
            this.finding = finding;
            this.modified = true;
        }
    }

    public void setHistory(String history) {
        if (!TCUtilities.equals(this.history, history)) {
            this.history = history;
            this.modified = true;
        }
    }
    
    public void setKeywords(List<ITextOrCode> keywords) {
        if (!TCUtilities.equals(this.keywords, keywords)) {
            this.keywords.clear();
            this.keywords.addAll(keywords);
            this.modified = true;
        }
    }
    
    public void setKeywordAt(int index, ITextOrCode keyword) {
        if (keywords!=null) {
            ITextOrCode old = this.keywords.get(index);
            if (old!=null) {
                if (!TCUtilities.equals(old, keyword)) {
                    this.keywords.set(index, keyword);
                    this.modified = true;
                }
            }
        }
    }
    
    public void addKeyword(ITextOrCode keyword) {
        addKeywordImpl(keyword);
    }

    public void removeKeyword(ITextOrCode keyword) {
        removeKeywordImpl(keyword);
    }

    public void setLevel(Level level) {
        if (!TCUtilities.equals(this.level, level))
        {
            this.level = level;
            this.modified = true;
        }
    }

    public void setOrganSystem(String organSystem) {
        if (!TCUtilities.equals(this.organSystem, organSystem))
        {
            this.organSystem = organSystem;
            this.modified = true;
        }
    }

    public void setPathology(ITextOrCode pathology) {
        if (!TCUtilities.equals(this.pathology, pathology))
        {
            this.pathology = pathology;
            this.modified = true;
        }
    }

    public void setPatientSex(PatientSex patientSex) {
        if (!TCUtilities.equals(this.patientSex, patientSex))
        {
            this.patientSex = patientSex;
            this.modified = true;
        }
    }

    public void setPatientSpecies(String patientSpecies) {
        if (!TCUtilities.equals(this.patientSpecies, patientSpecies))
        {
            this.patientSpecies = patientSpecies;
            this.modified = true;
        }
    }

    public void setBibliographicReferences(List<String> bibliographicReferences) {
        if (!TCUtilities.equals(this.bibliographicReferences, bibliographicReferences))
        {
            
            this.bibliographicReferences.clear();
            this.bibliographicReferences.addAll(bibliographicReferences);
            this.modified = true;
        }
    }
        
    public void setBibliographicReference(int index, String ref) 
        throws IndexOutOfBoundsException
    {
        if (this.bibliographicReferences==null && index==0)
        {
            this.bibliographicReferences = new ArrayList<String>();
        }
        
        this.bibliographicReferences.set(index, ref);
        this.modified = true;
    }
    
    public void addBibliographicReference(String ref)
    {
        if (this.bibliographicReferences==null)
        {
            this.bibliographicReferences = new ArrayList<String>();
        }
        
        this.bibliographicReferences.add(ref);
        this.modified = true;
    }
    
    public void removeBibliographicReference(int index)
        throws IndexOutOfBoundsException
    {
        if (this.bibliographicReferences!=null)
        {
            this.bibliographicReferences.remove(index);
            this.modified = true;
        }
    }

    public void setTitle(String title) {
        if (!TCUtilities.equals(this.title, title))
        {
            this.title = title;
            this.modified = true;
        }
    }
    
    public void setValue(TCQueryFilterKey key, Object value)
    {
        try
        {
            if (TCQueryFilterKey.Abstract.equals(key)) {
                setAbstract(convertValue(value, String.class));
            } else if (TCQueryFilterKey.AcquisitionModality.equals(key)) {
                setAcquisitionModalities(convertValues(value, AcquisitionModality.class));
            } else if (TCQueryFilterKey.Anatomy.equals(key)) {
                setAnatomy(convertValue(value, ITextOrCode.class));
            } else if (TCQueryFilterKey.AuthorAffiliation.equals(key)) {
                setAuthorAffiliation(convertValue(value, String.class));
            } else if (TCQueryFilterKey.AuthorContact.equals(key)) {
                setAuthorContact(convertValue(value, String.class));
            } else if (TCQueryFilterKey.AuthorName.equals(key)) {
                setAuthorName(convertValue(value, String.class));
            } else if (TCQueryFilterKey.BibliographicReference.equals(key)) {
                setBibliographicReferences(convertValues(value, String.class));
            } else if (TCQueryFilterKey.Category.equals(key)) {
                setCategory(convertValue(value, Category.class));
            } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
                setDiagnosisConfirmed(convertValue(value, YesNo.class));
            } else if (TCQueryFilterKey.Diagnosis.equals(key)) {
                setDiagnosis(convertValue(value, ITextOrCode.class));
            } else if (TCQueryFilterKey.DifferentialDiagnosis.equals(key)) {
                setDiffDiagnosis(convertValue(value, ITextOrCode.class));
            } else if (TCQueryFilterKey.Discussion.equals(key)) {
                setDiscussion(convertValue(value, String.class));
            } else if (TCQueryFilterKey.Finding.equals(key)) {
                setFinding(convertValue(value, ITextOrCode.class));
            } else if (TCQueryFilterKey.History.equals(key)) {
                setHistory(convertValue(value, String.class));
            } else if (TCQueryFilterKey.Keyword.equals(key)) {
                setKeywords(convertValues(value, ITextOrCode.class));
            } else if (TCQueryFilterKey.Level.equals(key)) {
                setLevel(convertValue(value, Level.class));
            } else if (TCQueryFilterKey.OrganSystem.equals(key)) {
                setOrganSystem(convertValue(value, String.class));
            } else if (TCQueryFilterKey.Pathology.equals(key)) {
                setPathology(convertValue(value, ITextOrCode.class));
            } else if (TCQueryFilterKey.PatientSex.equals(key)) {
                setPatientSex(convertValue(value, PatientSex.class));
            } else if (TCQueryFilterKey.PatientSpecies.equals(key)) {
                setPatientSpecies(convertValue(value, String.class));
            } else if (TCQueryFilterKey.Title.equals(key)) {
                setTitle(convertValue(value, String.class));
            }
        }
        catch (IllegalArgumentException e)
        {
            log.error("Assigning value to teaching-file key '" + key + "' failed!", e);
        }
    }
    
    public DicomObject toDataset()
    {
        BasicDicomObject ds = new BasicDicomObject();

        //keep all non-TF relevant dicom tags
        this.ds.copyTo(ds);
        ds.remove(Tag.ContentSequence); //remove old content
        DicomElement content = ds.putSequence(Tag.ContentSequence); //add new content
        
        //SOP common module; need to create new iuid?
        ds.putString(Tag.SOPInstanceUID, null, UIDUtils.createUID());
        
        //proprietary; used to show some useful info in the GUI when doing c-find
        ds.putString(Tag.ContentLabel, null, getTitle());
        ds.putString(Tag.ContentDescription, null, getAbstr());
        ds.putString(Tag.ContentCreatorName, null, getAuthorName());
        
        //author name
        String authorName = getAuthorName();
        String authorAffiliation = getAuthorAffiliation();
        String authorContact = getAuthorContact();
        
        if (authorName!=null && !authorName.isEmpty())
        {
            DicomObject author = createTextContent(TCQueryFilterKey.AuthorName, authorName);
            
            //conforming to IHE TCE, author affiliation and contact are
            //nested elements of the author element
            if ((authorAffiliation!=null && !authorAffiliation.isEmpty()) ||
                (authorContact!=null && !authorContact.isEmpty()))
            {
                DicomElement authorContent = author.putSequence(Tag.ContentSequence);
                
                if (authorAffiliation!=null && !authorAffiliation.isEmpty())
                {
                    authorContent.addDicomObject(createTextContent(
                            TCQueryFilterKey.AuthorAffiliation, authorAffiliation, "HAS PROPERTIES"));
                }
                if (authorContact!=null && !authorContact.isEmpty())
                {
                    authorContent.addDicomObject(createTextContent(
                            TCQueryFilterKey.AuthorContact, authorContact, "HAS PROPERTIES"));
                }
            }
            
            content.addDicomObject(author);
        }
        
        //author affiliation
        if (authorAffiliation!=null && !authorAffiliation.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.AuthorAffiliation, authorAffiliation));
        }
        
        //author contact
        if (authorContact!=null && !authorContact.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.AuthorContact, authorContact));
        }

        //abstract
        String abstr = getAbstr();
        if (abstr!=null && !abstr.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.Abstract, abstr));
        }
                
        //anatomy
        ITextOrCode anatomy = getAnatomy();
        if (anatomy!=null)
        {
            DicomObject o = createTextOrCodeContent(TCQueryFilterKey.Anatomy, anatomy);
            if (o!=null) {
                content.addDicomObject(o);
            }
        }
        
        //pathology
        ITextOrCode pathology = getPathology();
        if (pathology!=null)
        {
            DicomObject o = createTextOrCodeContent(TCQueryFilterKey.Pathology, pathology);
            if (o!=null) {
                content.addDicomObject(o);
            }
        }
        
        //keywords
        List<ITextOrCode> keywords = getKeywords();
        if (keywords!=null)
        {
            for (ITextOrCode keyword : keywords)
            {
                DicomObject o = createTextOrCodeContent(TCQueryFilterKey.Keyword, keyword);
                if (o!=null) {
                    content.addDicomObject(o);
                }
            }
        }
        
        //history
        String history = getHistory();
        if (history!=null && !history.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.History, history));
        }
        
        //finding
        ITextOrCode finding = getFinding();
        if (finding!=null)
        {
            DicomObject o = createTextOrCodeContent(TCQueryFilterKey.Finding, finding);
            if (o!=null) {
                content.addDicomObject(o);
            }
        }
        
        //discussion
        String discussion = getDiscussion();
        if (discussion!=null && !discussion.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.Discussion, discussion));
        }
        
        //diff.-diagnosis
        ITextOrCode diffDiagnosis = getDiffDiagnosis();
        if (diffDiagnosis!=null)
        {
            DicomObject o = createTextOrCodeContent(TCQueryFilterKey.DifferentialDiagnosis, diffDiagnosis);
            if (o!=null) {
                content.addDicomObject(o);
            }
        }
        
        //diagnosis
        ITextOrCode diagnosis = getDiagnosis();
        if (diagnosis!=null)
        {
            DicomObject o = createTextOrCodeContent(TCQueryFilterKey.Diagnosis, diagnosis);
            if (o!=null) {
                content.addDicomObject(o);
            }
        }
        
        //diagnosis confirmed
        YesNo diagConfirmed = getDiagnosisConfirmed();
        if (diagConfirmed!=null)
        {
            content.addDicomObject(
                    createCodeContent(TCQueryFilterKey.DiagnosisConfirmed, diagConfirmed.getCode()));
        }
        
        //organ-system
        String organSystem = getOrganSystem();
        if (organSystem!=null && !organSystem.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.OrganSystem, organSystem));
        }
        
        //modalities
        List<AcquisitionModality> modalities = getAcquisitionModalities();
        if (modalities!=null && !modalities.isEmpty())
        {
            for (AcquisitionModality modality : modalities)
            {
                if (modality.getCode()!=null)
                {
                    content.addDicomObject(
                            createCodeContent(TCQueryFilterKey.AcquisitionModality, modality.getCode()));
                }
                else if (modality.getString()!=null)
                {
                	content.addDicomObject(createTextContent(
                			TCQueryFilterKey.AcquisitionModality, modality.getString()));
                }
            }
        }
        
        //category
        Category category = getCategory();
        if (category!=null)
        {
            content.addDicomObject(
                    createCodeContent(TCQueryFilterKey.Category, category.getCode()));
        }
        
        //level
        Level level = getLevel();
        if (level!=null)
        {
            content.addDicomObject(
                    createCodeContent(TCQueryFilterKey.Level, level.getCode()));
        }
        
        //patient age
        Integer patientAge = getPatientAge();
        if (patientAge!=null)
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.PatientAge, Integer.toString(patientAge)));
        }
        
        //patient sex
        PatientSex patientSex = getPatientSex();
        if (patientSex!=null)
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.PatientSex, patientSex.getString()));
        }
        
        //patient race
        String patientSpecies = getPatientSpecies();
        if (patientSpecies!=null && !patientSpecies.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.PatientSpecies, patientSpecies));
        }
        
        //title
        String title = getTitle();
        if (title!=null && !title.isEmpty())
        {
            content.addDicomObject(
                    createTextContent(TCQueryFilterKey.Title, title));
        }
        
        //bibliographic references
        List<String> bibliographicReferences = getBibliographicReferences();
        if (bibliographicReferences!=null && !bibliographicReferences.isEmpty())
        {
            for (String bibliographicReference : bibliographicReferences)
            {
                content.addDicomObject(
                        createTextContent(TCQueryFilterKey.BibliographicReference, bibliographicReference));
            }
        }
        
        return ds;
    }
    
    public DicomObject toRejectionNoteDataset()
    {   
        String stuid = ds.getString(Tag.StudyInstanceUID);
        String suid = ds.getString(Tag.SeriesInstanceUID);
        String iuid = ds.getString(Tag.SOPInstanceUID);
        String cuid = ds.getString(Tag.SOPClassUID);
        
        DicomObject title = new BasicDicomObject();
        title.putString(Tag.CodingSchemeDesignator, null, "DCM");
        title.putString(Tag.CodeValue, null, "113001");
        title.putString(Tag.CodeMeaning, null, "Rejected For Quality Reasons");
        
        DicomObject refSOP = new BasicDicomObject();
        refSOP.putString(Tag.ReferencedSOPClassUID, null, cuid);
        refSOP.putString(Tag.ReferencedSOPInstanceUID, null, iuid);
        
        DicomObject refSeries = new BasicDicomObject();
        refSeries.putString(Tag.SeriesInstanceUID, null, suid);
        refSeries.putSequence(Tag.ReferencedSOPSequence).addDicomObject(refSOP);
        
        DicomObject refStudy = new BasicDicomObject();
        refStudy.putString(Tag.StudyInstanceUID, null, stuid);
        refStudy.putSequence(Tag.ReferencedSeriesSequence).addDicomObject(refSeries);
        
        BasicDicomObject ko = new BasicDicomObject();
        ko.putString(Tag.SOPClassUID, null, UID.KeyObjectSelectionDocumentStorage);
        ko.putString(Tag.SOPInstanceUID, null, UIDUtils.createUID());
        ko.putString(Tag.SeriesInstanceUID, null, UIDUtils.createUID());
        ko.putString(Tag.StudyInstanceUID, null, stuid);
        ko.putString(Tag.PatientID, null, ds.getString(Tag.PatientID));
        ko.putString(Tag.IssuerOfPatientID, null, ds.getString(Tag.IssuerOfPatientID));
        ko.putString(Tag.PatientName, null, ds.getString(Tag.PatientName));
        ko.putString(Tag.Modality, null, "KO");
        ko.putString(Tag.ContentDate, null, DateUtils.formatDA(new Date()));
        ko.putString(Tag.ContentTime, null, DateUtils.formatTM(new Date()));
        ko.putSequence(Tag.ConceptNameCodeSequence).addDicomObject(title);
        ko.putSequence(Tag.CurrentRequestedProcedureEvidenceSequence).addDicomObject(refStudy);
        
        return ko;
    }

    private DicomObject createTextContent(TCQueryFilterKey key, String text)
    {
        return createTextContent(key, text, "CONTAINS");
    }
    
    private DicomObject createTextContent(TCQueryFilterKey key, String text, String relationshipType)
    {
        DicomObject ds = new BasicDicomObject();
        ds.putString(Tag.RelationshipType, null, relationshipType);
        ds.putString(Tag.ValueType, null, "TEXT");
        ds.putNestedDicomObject(Tag.ConceptNameCodeSequence, key.getCode().toCodeItem());
        ds.putString(Tag.TextValue, null, text);
        return ds;
    }
    
    private DicomObject createCodeContent(TCQueryFilterKey key, DicomCode code)
    {
        return createCodeContent(key, code.toCode());
    }
    
    private DicomObject createCodeContent(TCQueryFilterKey key, Code code)
    {
        DicomObject ds = new BasicDicomObject();
        ds.putString(Tag.RelationshipType, null, "CONTAINS");
        ds.putString(Tag.ValueType, null, "CODE");
        ds.putNestedDicomObject(Tag.ConceptNameCodeSequence, key.getCode().toCodeItem());
        ds.putNestedDicomObject(Tag.ConceptCodeSequence, code.toCodeItem());
        return ds;
    }
    
    private DicomObject createTextOrCodeContent(TCQueryFilterKey key, ITextOrCode value) {
        if (value!=null) {
            String text = value.getText();
            DicomCode code = value.getCode();
            
            if (code!=null) {
                TCKeywordCatalogueProvider prov = TCKeywordCatalogueProvider.getInstance();   
                if (prov!=null && prov.hasCatalogue(key) && 
                        prov.isCatalogueExclusive(key)) {
                    return createCodeContent(key, code);
                }
                else {
                    return createTextContent(key, code.toString());
                }
            }
            else if (text!=null && !text.trim().isEmpty()) {
                return createTextContent(key, text.trim());
            }
        }
        return null;
    }
}
