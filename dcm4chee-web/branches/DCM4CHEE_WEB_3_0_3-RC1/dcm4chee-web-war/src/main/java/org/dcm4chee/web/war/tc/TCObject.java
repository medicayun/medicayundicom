package org.dcm4chee.web.war.tc;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.common.util.FileUtils;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryLocal;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.AcquisitionModality;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Category;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Level;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.PatientSex;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.YesNo;
import org.dcm4chee.web.war.folder.delegate.TarRetrieveDelegate;
import org.dcm4chee.web.war.tc.keywords.TCKeyword;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 04, 2011
 */
public class TCObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static Logger log = LoggerFactory.getLogger(TCObject.class);
    
    private String iuid;

    protected String abstr;

    protected List<AcquisitionModality> acquisitionModalities;

    protected ITextOrCode anatomy;

    protected String authorAffiliation;

    protected String authorContact;

    protected String authorName;

    protected Category category;

    protected ITextOrCode diagnosis;

    protected YesNo diagnosisConfirmed;

    protected ITextOrCode diffDiagnosis;

    protected String discussion;

    protected ITextOrCode finding;

    protected String history;

    protected List<ITextOrCode> keywords;

    protected Level level;

    protected String organSystem;

    protected ITextOrCode pathology;
    
    protected Integer patientAge;

    protected PatientSex patientSex;

    protected String patientSpecies;

    protected List<String> bibliographicReferences;

    protected String title;

    private List<TCReferencedStudy> studyRefs;
    
    private List<TCReferencedInstance> instanceRefs;
    
    private List<TCReferencedImage> imageRefs;
    
    protected TCObject(DicomObject object) {
        parse(object);
    }

    public static TCObject create(TCModel model) throws IOException {
        String fsID = model.getFileSystemId();
        String fileID = model.getFileId();

        DicomInputStream dis = null;
        try {
        	dis = new DicomInputStream(fsID.startsWith("tar:") ? 
        			TarRetrieveDelegate.getInstance().retrieveFileFromTar(fsID, fileID) :
        				FileUtils.resolve(new File(fsID, fileID)));
            return new TCObject(dis.readDicomObject());
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }
    
    public String getUID()
    {
        return iuid;
    }
    
    public String getAbstr() {
        return abstr;
    }

    public List<AcquisitionModality> getAcquisitionModalities() {
        return acquisitionModalities;
    }

    public ITextOrCode getAnatomy() {
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

    public ITextOrCode getDiagnosis() {
        return diagnosis;
    }

    public YesNo getDiagnosisConfirmed() {
        return diagnosisConfirmed;
    }

    public ITextOrCode getDiffDiagnosis() {
        return diffDiagnosis;
    }

    public String getDiscussion() {
        return discussion;
    }

    public ITextOrCode getFinding() {
        return finding;
    }

    public String getHistory() {
        return history;
    }
    
    public int getKeywordCount() {
        return keywords!=null ? keywords.size() : 0;
    }
 
    public List<ITextOrCode> getKeywords() {
        return keywords;
    }

    public Level getLevel() {
        return level;
    }

    public String getOrganSystem() {
        return organSystem;
    }

    public ITextOrCode getPathology() {
        return pathology;
    }
    
    public Integer getPatientAge() {
        return patientAge;
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

    public List<TCReferencedStudy> getReferencedStudies() {
        if (studyRefs != null) {
            return Collections.unmodifiableList(studyRefs);
        }

        return Collections.emptyList();
    }
    
    public List<TCReferencedInstance> getReferencedInstances()
    {
        if (instanceRefs==null)
        {
            instanceRefs = new ArrayList<TCReferencedInstance>();
            
            List<TCReferencedStudy> studies = getReferencedStudies();
            for (TCReferencedStudy study : studies)
            {
                for (TCReferencedSeries series : study.getSeries())
                {
                    for (TCReferencedInstance instance : series.getInstances())
                    {
                        if (!instanceRefs.contains(instance))
                        {
                            instanceRefs.add(instance);
                        }
                    }
                }
            }
        }
        
        return instanceRefs;
    }
    
    public List<TCReferencedImage> getReferencedImages()
    {
        if (imageRefs==null)
        {
            imageRefs = new ArrayList<TCReferencedImage>();
            
            List<TCReferencedStudy> studies = getReferencedStudies();

            for (TCReferencedStudy study : studies)
            {
                for (TCReferencedSeries series : study.getSeries())
                {
                    for (TCReferencedImage image : series.getImages())
                    {
                    	if (image.isImage() && !imageRefs.contains(image))
                        {
                            imageRefs.add(image);
                        }
                    }
                }
            }
        }
        
        return imageRefs;
    }
    
    public Object getValue(TCQueryFilterKey key) {
        Object value = null;

        if (TCQueryFilterKey.Abstract.equals(key)) {
            value = getAbstr();
        } else if (TCQueryFilterKey.AcquisitionModality.equals(key)) {
            value = concatStringValues(getAcquisitionModalities());
        } else if (TCQueryFilterKey.Anatomy.equals(key)) {
            value = getAnatomy();
        } else if (TCQueryFilterKey.AuthorAffiliation.equals(key)) {
            value = getAuthorAffiliation();
        } else if (TCQueryFilterKey.AuthorContact.equals(key)) {
            value = getAuthorContact();
        } else if (TCQueryFilterKey.AuthorName.equals(key)) {
            value = getAuthorName();
        } else if (TCQueryFilterKey.BibliographicReference.equals(key)) {
            value = getBibliographicReferences();
        } else if (TCQueryFilterKey.Category.equals(key)) {
            value = getCategory() != null ? getCategory() : null;
        } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
            value = getDiagnosisConfirmed() != null ? getDiagnosisConfirmed() : null;
        } else if (TCQueryFilterKey.Diagnosis.equals(key)) {
            value = getDiagnosis();
        } else if (TCQueryFilterKey.DifferentialDiagnosis.equals(key)) {
            value = getDiffDiagnosis();
        } else if (TCQueryFilterKey.Discussion.equals(key)) {
            value = getDiscussion();
        } else if (TCQueryFilterKey.Finding.equals(key)) {
            value = getFinding();
        } else if (TCQueryFilterKey.History.equals(key)) {
            value = getHistory();
        } else if (TCQueryFilterKey.Keyword.equals(key)) {
            value = getKeywords();
        } else if (TCQueryFilterKey.Level.equals(key)) {
            value = getLevel() != null ? getLevel() : null;
        } else if (TCQueryFilterKey.OrganSystem.equals(key)) {
            value = getOrganSystem();
        } else if (TCQueryFilterKey.Pathology.equals(key)) {
            value = getPathology();
        } else if (TCQueryFilterKey.PatientAge.equals(key)) {
            value = getPatientAge() != null ? getPatientAge() : null;
        } else if (TCQueryFilterKey.PatientSex.equals(key)) {
            value = getPatientSex() != null ? getPatientSex() : null;
        } else if (TCQueryFilterKey.PatientSpecies.equals(key)) {
            value = getPatientSpecies();
        } else if (TCQueryFilterKey.Title.equals(key)) {
            value = getTitle();
        }

        return value;
    }


    public String getValueAsLocalizedString(TCQueryFilterKey key, Component c) {
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
            return getValueAsString(key);
        }
    }

    public String getValueAsString(TCQueryFilterKey key) {
        return toStringValue(getValue(key));
    }

    private String concatStringValues(List<?> list) {
        if (list != null) {
            Iterator<?> it = list.iterator();
            StringBuilder sbuilder = new StringBuilder();
            if (it.hasNext())
                sbuilder.append(toStringValue(it.next()));
            while (it.hasNext()) {
                sbuilder.append("; ");
                sbuilder.append(toStringValue(it.next()));
            }
            return sbuilder.toString();
        }

        return null;
    }

    private void parse(DicomObject o) {
        iuid = o.getString(Tag.SOPInstanceUID);
        
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
                    TCReferencedStudy study = new TCReferencedStudy(stuid);
                    
                    DicomElement seriesSeq = studyRef
                            .get(Tag.ReferencedSeriesSequence);
                    int seriesCount = seriesSeq != null ? seriesSeq
                            .countItems() : -1;
                    int instanceCount = 0;
                    
                    if (seriesCount > 0) {
                        for (int j = 0; j < seriesCount; j++) {
                            DicomObject seriesRef = seriesSeq.getDicomObject(j);
                            String suid = seriesRef != null ? seriesRef
                                    .getString(Tag.SeriesInstanceUID) : null;

                            if (suid != null) {
                                TCReferencedSeries series = new TCReferencedSeries(suid, study);
                                
                                DicomElement instanceSeq = seriesRef
                                        .get(Tag.ReferencedSOPSequence);
                                instanceCount = instanceSeq != null ? instanceSeq
                                        .countItems() : -1;
                                
                                if (instanceCount > 0) {
                                    TCQueryLocal ejb = (TCQueryLocal) JNDIUtils
                                            .lookup(TCQueryLocal.JNDI_NAME);
                                    
                                    study.addSeries(series);

                                    Map<String, TCReferencedImage> images = null;
                                    Map<String, Integer> instanceNumbers = ejb.getInstanceNumbers(suid);
                                    
                                    for (int k = 0; k < instanceCount; k++) {
                                        DicomObject instanceRef = instanceSeq
                                                .getDicomObject(k);
                                        String iuid = instanceRef
                                                .getString(Tag.ReferencedSOPInstanceUID);
                                        String cuid = instanceRef
                                                .getString(Tag.ReferencedSOPClassUID);
                                        Integer instanceNumber = instanceNumbers.get(iuid);
                                        
                                        if (TCReferencedInstance.isImage(cuid))
                                        {
                                        	TCReferencedImage image = new TCReferencedImage(series, iuid, cuid,
                                        			instanceNumber!=null?instanceNumber:-1);
                                        	series.addInstance(image);
                                        	
                                        	if (images==null)
                                        	{
                                        		images = new HashMap<String, TCReferencedImage>();
                                        	}
                                        	
                                        	images.put(iuid, image);
                                        }
                                        else
                                        {
                                        	series.addInstance(
                                                new TCReferencedInstance(series, iuid, cuid, 
                                                		instanceNumber!=null?instanceNumber:-1));
                                        }
                                    }
                                    
                                    if (images!=null && !images.isEmpty())
                                    {
                                        Map<String, Integer> frames = ejb.findMultiframeInstances(
                                        		stuid, suid, images.keySet().toArray(new String[0]));
                                        
                                        if (frames!=null && !frames.isEmpty())
                                        {
                                        	for (Map.Entry<String, Integer> me : frames.entrySet())
                                        	{
                                        		TCReferencedImage image = images.get(me.getKey());
                                        		series.removeInstance(image);
                                        		
                                        		for (int n=1; n<=me.getValue(); n++)
                                        		{
                                        			series.addInstance(new TCReferencedImage(
                                        					series, image.getInstanceUID(), image.getClassUID(), 
                                        					image.getInstanceNumber(), n));
                                        		}
                                        	}
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (instanceCount>0)
                    {
                        if (studyRefs==null)
                        {
                            studyRefs = new ArrayList<TCReferencedStudy>();
                        }
                        studyRefs.add(study);
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
                                    .equals(TCQueryFilterKey.AcquisitionModality
                                            .getCode())) {
                                AcquisitionModality m = AcquisitionModality
                                        .get(getTextValue(item));

                                if (m != null) {
                                    if (acquisitionModalities == null) {
                                        acquisitionModalities = new ArrayList<AcquisitionModality>();
                                    }

                                    if (!acquisitionModalities.contains(m)) {
                                        acquisitionModalities.add(m);
                                    }
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Anatomy.getCode())) {
                                anatomy = TextOrCode.text(getTextValue(item));
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
                                diagnosis = TextOrCode.text(getTextValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DifferentialDiagnosis
                                            .getCode())) {
                                diffDiagnosis = TextOrCode.text(getTextValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Discussion
                                            .getCode())) {
                                discussion = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Finding.getCode())) {
                                finding = TextOrCode.text(getTextValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.History.getCode())) {
                                history = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Keyword.getCode())) {
                                addKeywordImpl(TextOrCode.text(getTextValue(item)));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.OrganSystem
                                            .getCode())) {
                                organSystem = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Pathology
                                            .getCode())) {
                                pathology = TextOrCode.text(getTextValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.PatientAge
                                            .getCode())) {
                                try
                                {
                                    patientAge = Integer.valueOf(getTextValue(item));
                                }
                                catch (Exception e)
                                {
                                    log.warn("Parsing patient age failed! Skipped..");
                                }
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
                                category = Category.get(toCode(getCodeValue(item)));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AcquisitionModality
                                            .getCode())) {
                                AcquisitionModality m = AcquisitionModality
                                        .get(toCode(getCodeValue(item)));

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
                                diagnosisConfirmed = YesNo.get(toCode(getCodeValue(item)));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Level.getCode())) {
                                level = Level.get(toCode(getCodeValue(item)));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Anatomy.getCode())) {
                                anatomy = TextOrCode.code(getCodeValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Diagnosis
                                            .getCode())) {
                                diagnosis = TextOrCode.code(getCodeValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DifferentialDiagnosis
                                            .getCode())) {
                                diffDiagnosis = TextOrCode.code(getCodeValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Finding.getCode())) {
                                finding = TextOrCode.code(getCodeValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Keyword.getCode())) {
                                addKeywordImpl(TextOrCode.code(getCodeValue(item)));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Pathology
                                            .getCode())) {
                                pathology = TextOrCode.code(getCodeValue(item));
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
        return object == null ? null : object.contains(Tag.ConceptCodeSequence) ?
            new DicomCode(object.getNestedDicomObject(Tag.ConceptCodeSequence)) : null;
    }
    
    public Code toCode(DicomCode c) {
        return c == null ? null : c.toCode();
    }

    private String toStringValue(Object value) {
        if (value instanceof List) {
            return concatStringValues((List<?>)value);
        }
        else if (value!=null) {
            return value.toString();
        }
        else {
            return null;
        }
    }
    
    protected void addKeywordImpl(ITextOrCode keyword) {
        if (keywords==null) {
            keywords = new ArrayList<ITextOrCode>(2);
        }
        if (!keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }
    
    protected void removeKeywordImpl(ITextOrCode keyword) {
        if (keywords!=null) {
            keywords.remove(keyword);
        }
    }
    
    protected static <T extends Object> T convertValue(Object v, Class<T> valueClass) throws IllegalArgumentException
    {
        return convertValue(v, valueClass, null);
    }
        
    @SuppressWarnings("unchecked")
    protected static <T extends Object> T convertValue(Object v, Class<T> valueClass, TCKeywordCatalogue cat) throws IllegalArgumentException
    {
        if (v==null)
        {
            return null;
        }
        else if (String.class.isAssignableFrom(valueClass))
        {
            if (DicomCode.class.isAssignableFrom(v.getClass()))
            {
                return (T) ((DicomCode)v).toString();
            }
            else if (ITextOrCode.class.isAssignableFrom(v.getClass())) {
                return (T) ((ITextOrCode)v).getText();
            }
            else
            {
                return (T) v.toString();
            }
        }
        else if (DicomCode.class.isAssignableFrom(valueClass))
        {
            if (DicomCode.class.isAssignableFrom(v.getClass()))
            {
                return (T) v;
            }
            else if (TCKeyword.class.isAssignableFrom(v.getClass()))
            {
                return (T) ((TCKeyword)v).getCode();
            }
            else if (ITextOrCode.class.isAssignableFrom(v.getClass())) {
                return (T) ((ITextOrCode)v).getCode();
            }
            else if (String.class.isAssignableFrom(v.getClass()) && cat!=null)
            {
                TCKeyword keyword = cat.findKeyword((String)v);
                
                DicomCode code = keyword!=null?keyword.getCode():null;
                
                if (code!=null)
                {
                    return (T) code;
                }
            }
        }
        else if (ITextOrCode.class.isAssignableFrom(valueClass)) {
            if (ITextOrCode.class.isAssignableFrom(v.getClass())) {
                return (T) v;
            }
            else if (DicomCode.class.isAssignableFrom(v.getClass())) {
                return (T) TextOrCode.code((DicomCode)v);
            }
            else if (String.class.isAssignableFrom(v.getClass())) {
                return (T) TextOrCode.text((String)v);
            }
        }
        else if (Enum.class.isAssignableFrom(valueClass))
        {
            if (valueClass.isAssignableFrom(v.getClass()))
            {
                return (T) v;
            }
            else
            {
                for (Object o : valueClass.getEnumConstants())
                {
                    if (((Enum<?>)o).name().equals(v.toString().trim()))
                    {
                        return (T) o;
                    }
                }
            }

            throw new IllegalArgumentException("Unable to convert enum (" + valueClass + "): No enum constant found for name '" + v.toString() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else if (AcquisitionModality.class.isAssignableFrom(valueClass))
        {
        	if (valueClass.isAssignableFrom(v.getClass()))
        	{
        		return (T) v;
        	}
        	else
        	{
        		AcquisitionModality modality = AcquisitionModality.get(v.toString().trim());
        		if (modality!=null)
        		{
        			return (T) modality;
        		}
        	}
        }
        
        throw new IllegalArgumentException("Unable to convert from " + v.getClass() + " to " + valueClass); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected <T extends Object> List<T> convertValues(Object v, Class<T> valueClass) throws IllegalArgumentException
    {
        return convertValues(v, valueClass, null);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T extends Object> List<T> convertValues(Object v, Class<T> valueClass, TCKeywordCatalogue cat) throws IllegalArgumentException
    {
        if (v==null)
        {
            return null;
        }
        else
        {
            Object[] values = null;
            
            if (v instanceof List)
            {
                values = new Object[((List<?>)v).size()];
                for (int i=0; i<values.length; i++)
                {
                    values[i] = convertValue(((List<?>)v).get(i), valueClass, cat);
                }
            }
            else
            {
                values = new Object[] {convertValue(v, valueClass, cat)};
            }
            
            return (List) Arrays.asList(values);
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
        @Override
        public boolean equals(Object o) {
            if (o != null && (o instanceof DicomCode)) {
                DicomCode code = (DicomCode) o;
                return value.equals(code.value)
                        && designator.equals(code.designator)
                        && (version == null || version.equals(code.version));
            }

            return false;
        }
        @Override
        public int hashCode() {
            return (value+"_"+designator+"_"+version).hashCode();
        }
    }
    
    public interface ITextOrCode extends Serializable {
        public String getText();
        public DicomCode getCode();
    }
    
    public static class TextOrCode implements ITextOrCode
    {
        private static final long serialVersionUID = 1L;
        
        private String text;
        private DicomCode code;
        
        private TextOrCode(String text) {
            this.text = text;
        }
        private TextOrCode(DicomCode code) {
            this.code = code;
        }
        public static TextOrCode text(String text) {
            return new TextOrCode(text);
        }
        public static TextOrCode code(DicomCode code) {
            return new TextOrCode(code);
        }
        @Override
        public String getText() {
            return text;
        }
        @Override
        public DicomCode getCode() {
            return code;
        }
        @Override
        public String toString() {
            if (code!=null) {
                return code.toString();
            }
            else if (text!=null) {
                return text;
            }
            return "";
        }
    }
}
