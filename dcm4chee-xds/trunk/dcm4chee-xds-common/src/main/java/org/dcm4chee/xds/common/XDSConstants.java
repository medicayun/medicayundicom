package org.dcm4chee.xds.common;

public interface XDSConstants {

	public static final String SLOT_HASH = "hash";
	public static final String SLOT_SIZE = "size";
	public static final String SLOT_REPOSITORY_UNIQUE_ID = "repositoryUniqueId";
	public static final String SLOT_URI = "URI";

	public static final String XDS_A_STATUS_SUCCESS = "Success";
	public static final String XDS_A_STATUS_FAILURE = "Failure";
	public static final String XDS_B_STATUS_SUCCESS = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
	public static final String XDS_B_STATUS_FAILURE = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure";
	public static final String XDS_B_STATUS_PARTIAL_SUCCESS = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:PartialSuccess";
	public static final String SUBMIT_OBJECTS_REQUEST = "SubmitObjectsRequest";
	public static final String SOAP_HEADER_ACTION = "Action";
	public static final String SOAP_HEADER_TO = "To";
	public static final String SOAP_HEADER_MSG_ID = "MessageID";
	public static final String SOAP_HEADER_MUST_UNDERSTAND = "mustUnderstand";

	public static final String TAG_XDSB_DOCUMENT = "Document";
	public static final String TAG_XDSB_RETRIEVE_DOC_SET_REPSONSE = "RetrieveDocumentSetResponse";
	public static final String XOP_INCLUDE = "Include";

	public static final String NS_XOP = "http://www.w3.org/2004/08/xop/include";
	public static final String NS_WS_ADDRESSING = "http://www.w3.org/2005/08/addressing";
	
//Namespace URN
	public static final String NS_URN_IHE_ITI_XDS_B_2007 = "urn:ihe:iti:xds-b:2007";
	public static final String NS_URN_RIM_3_0 = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
	public static final String NS_URN_REGISTRY_3_0 = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0";
	public static final String NS_URN_LCM_3_0 = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0";

	public static final String NS_URN_RIM_2_1 = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1";
	public static final String NS_URN_REGISTRY_2_1 = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1";

//IHE URN
	public static final String URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b";
	public static final String URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B_RESPONSE = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse";
	public static final String URN_IHE_ITI_2007_REGISTER_DOCUMENT_SET_B = "urn:ihe:iti:2007:RegisterDocumentSet-b";
	public static final String URN_IHE_ITI_2007_REGISTER_DOCUMENT_SET_B_RESPONSE = "urn:ihe:iti:2007:RegisterDocumentSet-bResponse";
	public static final String URN_IHE_ITI_2007_RETRIEVE_DOCUMENT_SET = "urn:ihe:iti:2007:RetrieveDocumentSet";
	public static final String URN_IHE_ITI_2007_RETRIEVE_DOCUMENT_SET_RESPONSE = "urn:ihe:iti:2007:RetrieveDocumentSetResponse";
	public static final String URN_IHE_ITI_2007_REGISTRY_STORED_QUERY = "urn:ihe:iti:2007:RegistryStoredQuery";
	public static final String URN_IHE_ITI_2007_REGISTRY_STORED_QUERY_RESPONSE = "urn:ihe:iti:2007:RegistryStoredQueryResponse";

//ERROR	const
        public static final String XDS_ERR_REPOSITORY_ERROR = "XDSRepositoryError";
        public static final String XDS_ERR_REGISTRY_ERROR = "XDSRegistryError";
	public static final String XDS_ERR_MISSING_DOCUMENT_METADATA = "XDSMissingDocumentMetadata";
	public static final String XDS_ERR_MISSING_DOCUMENT = "XDSMissingDocument";
        public static final String XDS_ERR_PATID_DOESNOT_MATCH = "XDSPatientIdDoesNotMatch";
        public static final String XDS_ERR_NON_IDENTICAL_HASH = "XDSNonIdenticalHash";
	public static final String XDS_ERR_REG_NOT_AVAIL = "XDSRegistryNotAvailable";
	public static final String XDS_ERR_REP_BUSY = "XDSRepositoryBusy";
	public static final String XDS_ERR_REP_OUT_OF_RESOURCES = "XDSRepositoryOutOfResources";
	public static final String XDS_ERR_REP_METADATA_ERROR = "XDSRepositoryMetadataError";
	public static final String XDS_ERR_SEVERITY_WARNING = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Warning";
	public static final String XDS_ERR_SEVERITY_ERROR = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error";
	public static final String XDS_ERR_MISSING_REGISTRY_PACKAGE = "RegistryPackage missing";
	public static final String XDS_ERR_WRONG_REPOSITORY_UNIQUE_ID = "XDSRepositoryWrongRepositoryUniqueId";
	public static final String XDS_ERR_DOCUMENT_UNIQUE_ID = "XDSDocumentUniqueIdError";
	
}
