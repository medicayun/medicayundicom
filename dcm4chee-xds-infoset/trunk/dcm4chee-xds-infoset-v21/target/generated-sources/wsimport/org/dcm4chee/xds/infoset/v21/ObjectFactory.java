
package org.dcm4chee.xds.infoset.v21;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.dcm4chee.xds.infoset.v21 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Value_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "Value");
    private final static QName _Description_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "Description");
    private final static QName _ExtrinsicObject_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "ExtrinsicObject");
    private final static QName _SQLQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1", "SQLQuery");
    private final static QName _Association_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "Association");
    private final static QName _SQLQueryResult_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1", "SQLQueryResult");
    private final static QName _ResponseOption_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1", "ResponseOption");
    private final static QName _RegistryPackage_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "RegistryPackage");
    private final static QName _LeafRegistryObjectList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "LeafRegistryObjectList");
    private final static QName _LocalizedString_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "LocalizedString");
    private final static QName _InternationalString_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "InternationalString");
    private final static QName _Name_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "Name");
    private final static QName _RegistryObject_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "RegistryObject");
    private final static QName _ExternalIdentifier_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "ExternalIdentifier");
    private final static QName _Slot_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "Slot");
    private final static QName _Classification_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "Classification");
    private final static QName _RegistryObjectList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "RegistryObjectList");
    private final static QName _ValueList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "ValueList");
    private final static QName _RegistryEntry_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "RegistryEntry");
    private final static QName _ObjectRef_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", "ObjectRef");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.dcm4chee.xds.infoset.v21
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExternalIdentifierType }
     * 
     */
    public ExternalIdentifierType createExternalIdentifierType() {
        return new ExternalIdentifierType();
    }

    /**
     * Create an instance of {@link SubmitObjectsRequest }
     * 
     */
    public SubmitObjectsRequest createSubmitObjectsRequest() {
        return new SubmitObjectsRequest();
    }

    /**
     * Create an instance of {@link ResponseOptionType }
     * 
     */
    public ResponseOptionType createResponseOptionType() {
        return new ResponseOptionType();
    }

    /**
     * Create an instance of {@link ExtrinsicObjectType }
     * 
     */
    public ExtrinsicObjectType createExtrinsicObjectType() {
        return new ExtrinsicObjectType();
    }

    /**
     * Create an instance of {@link SlotType1 }
     * 
     */
    public SlotType1 createSlotType1() {
        return new SlotType1();
    }

    /**
     * Create an instance of {@link RegistryError }
     * 
     */
    public RegistryError createRegistryError() {
        return new RegistryError();
    }

    /**
     * Create an instance of {@link RegistryObjectListType }
     * 
     */
    public RegistryObjectListType createRegistryObjectListType() {
        return new RegistryObjectListType();
    }

    /**
     * Create an instance of {@link RegistryResponse }
     * 
     */
    public RegistryResponse createRegistryResponse() {
        return new RegistryResponse();
    }

    /**
     * Create an instance of {@link ObjectRefList }
     * 
     */
    public ObjectRefList createObjectRefList() {
        return new ObjectRefList();
    }

    /**
     * Create an instance of {@link ObjectRefType }
     * 
     */
    public ObjectRefType createObjectRefType() {
        return new ObjectRefType();
    }

    /**
     * Create an instance of {@link AdhocQueryRequest }
     * 
     */
    public AdhocQueryRequest createAdhocQueryRequest() {
        return new AdhocQueryRequest();
    }

    /**
     * Create an instance of {@link InternationalStringType }
     * 
     */
    public InternationalStringType createInternationalStringType() {
        return new InternationalStringType();
    }

    /**
     * Create an instance of {@link ClassificationType }
     * 
     */
    public ClassificationType createClassificationType() {
        return new ClassificationType();
    }

    /**
     * Create an instance of {@link RegistryObjectType }
     * 
     */
    public RegistryObjectType createRegistryObjectType() {
        return new RegistryObjectType();
    }

    /**
     * Create an instance of {@link ValueListType }
     * 
     */
    public ValueListType createValueListType() {
        return new ValueListType();
    }

    /**
     * Create an instance of {@link RegistryPackageType }
     * 
     */
    public RegistryPackageType createRegistryPackageType() {
        return new RegistryPackageType();
    }

    /**
     * Create an instance of {@link LeafRegistryObjectListType }
     * 
     */
    public LeafRegistryObjectListType createLeafRegistryObjectListType() {
        return new LeafRegistryObjectListType();
    }

    /**
     * Create an instance of {@link RegistryErrorList }
     * 
     */
    public RegistryErrorList createRegistryErrorList() {
        return new RegistryErrorList();
    }

    /**
     * Create an instance of {@link RootElement }
     * 
     */
    public RootElement createRootElement() {
        return new RootElement();
    }

    /**
     * Create an instance of {@link LocalizedStringType }
     * 
     */
    public LocalizedStringType createLocalizedStringType() {
        return new LocalizedStringType();
    }

    /**
     * Create an instance of {@link RegistryEntryType }
     * 
     */
    public RegistryEntryType createRegistryEntryType() {
        return new RegistryEntryType();
    }

    /**
     * Create an instance of {@link AdhocQueryResponse }
     * 
     */
    public AdhocQueryResponse createAdhocQueryResponse() {
        return new AdhocQueryResponse();
    }

    /**
     * Create an instance of {@link AssociationType1 }
     * 
     */
    public AssociationType1 createAssociationType1() {
        return new AssociationType1();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "Value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "Description")
    public JAXBElement<InternationalStringType> createDescription(InternationalStringType value) {
        return new JAXBElement<InternationalStringType>(_Description_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtrinsicObjectType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "ExtrinsicObject")
    public JAXBElement<ExtrinsicObjectType> createExtrinsicObject(ExtrinsicObjectType value) {
        return new JAXBElement<ExtrinsicObjectType>(_ExtrinsicObject_QNAME, ExtrinsicObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1", name = "SQLQuery")
    public JAXBElement<String> createSQLQuery(String value) {
        return new JAXBElement<String>(_SQLQuery_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssociationType1 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "Association")
    public JAXBElement<AssociationType1> createAssociation(AssociationType1 value) {
        return new JAXBElement<AssociationType1>(_Association_QNAME, AssociationType1 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryObjectListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1", name = "SQLQueryResult")
    public JAXBElement<RegistryObjectListType> createSQLQueryResult(RegistryObjectListType value) {
        return new JAXBElement<RegistryObjectListType>(_SQLQueryResult_QNAME, RegistryObjectListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseOptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1", name = "ResponseOption")
    public JAXBElement<ResponseOptionType> createResponseOption(ResponseOptionType value) {
        return new JAXBElement<ResponseOptionType>(_ResponseOption_QNAME, ResponseOptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryPackageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "RegistryPackage")
    public JAXBElement<RegistryPackageType> createRegistryPackage(RegistryPackageType value) {
        return new JAXBElement<RegistryPackageType>(_RegistryPackage_QNAME, RegistryPackageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LeafRegistryObjectListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "LeafRegistryObjectList")
    public JAXBElement<LeafRegistryObjectListType> createLeafRegistryObjectList(LeafRegistryObjectListType value) {
        return new JAXBElement<LeafRegistryObjectListType>(_LeafRegistryObjectList_QNAME, LeafRegistryObjectListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocalizedStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "LocalizedString")
    public JAXBElement<LocalizedStringType> createLocalizedString(LocalizedStringType value) {
        return new JAXBElement<LocalizedStringType>(_LocalizedString_QNAME, LocalizedStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "InternationalString")
    public JAXBElement<InternationalStringType> createInternationalString(InternationalStringType value) {
        return new JAXBElement<InternationalStringType>(_InternationalString_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "Name")
    public JAXBElement<InternationalStringType> createName(InternationalStringType value) {
        return new JAXBElement<InternationalStringType>(_Name_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryObjectType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "RegistryObject")
    public JAXBElement<RegistryObjectType> createRegistryObject(RegistryObjectType value) {
        return new JAXBElement<RegistryObjectType>(_RegistryObject_QNAME, RegistryObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "ExternalIdentifier")
    public JAXBElement<ExternalIdentifierType> createExternalIdentifier(ExternalIdentifierType value) {
        return new JAXBElement<ExternalIdentifierType>(_ExternalIdentifier_QNAME, ExternalIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SlotType1 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "Slot")
    public JAXBElement<SlotType1> createSlot(SlotType1 value) {
        return new JAXBElement<SlotType1>(_Slot_QNAME, SlotType1 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "Classification")
    public JAXBElement<ClassificationType> createClassification(ClassificationType value) {
        return new JAXBElement<ClassificationType>(_Classification_QNAME, ClassificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryObjectListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "RegistryObjectList")
    public JAXBElement<RegistryObjectListType> createRegistryObjectList(RegistryObjectListType value) {
        return new JAXBElement<RegistryObjectListType>(_RegistryObjectList_QNAME, RegistryObjectListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "ValueList")
    public JAXBElement<ValueListType> createValueList(ValueListType value) {
        return new JAXBElement<ValueListType>(_ValueList_QNAME, ValueListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryEntryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "RegistryEntry")
    public JAXBElement<RegistryEntryType> createRegistryEntry(RegistryEntryType value) {
        return new JAXBElement<RegistryEntryType>(_RegistryEntry_QNAME, RegistryEntryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1", name = "ObjectRef")
    public JAXBElement<ObjectRefType> createObjectRef(ObjectRefType value) {
        return new JAXBElement<ObjectRefType>(_ObjectRef_QNAME, ObjectRefType.class, null, value);
    }

}
