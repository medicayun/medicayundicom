#!/bin/sh
DIRNAME=`dirname $0`
SED_OPTS="-i.BAK";

AuditLogger="s/SyslogHost/AuditRepositoryHostname/g"
AuditLogger="$AuditLogger;s/SyslogPort/AuditRepositoryPort/g"
AuditLogger="$AuditLogger;s/Facility/AuditRepositoryFacility/g"
AuditLogger="$AuditLogger;s/SupressLogForAETs/SupressLogForAETitles/g"

sed $SED_OPTS -e "$AuditLogger" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DAuditLogger.xml

CompressionService="s/TempDir/TempDirectory/g"
CompressionService="$CompressionService;s/AutoPurge/AutoDelete/g"

sed $SED_OPTS -e "$CompressionService" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DCompressionService.xml	

FileSystemMgt="s/DirectoryPathList/DirectoryPaths/g"
FileSystemMgt="$FileSystemMgt;s/ReadOnlyDirectoryPathList/ReadOnlyDirectoryPaths/g"
FileSystemMgt="$FileSystemMgt;s/RetrieveAET/RetrieveAETitle/g"
FileSystemMgt="$FileSystemMgt;s/MinFreeDiskSpace/MinimumFreeDiskSpace/g"
FileSystemMgt="$FileSystemMgt;s/FlushStudiesExternalRetrievable/DeleteStudiesExternallyRetrievable/g"
FileSystemMgt="$FileSystemMgt;s/FlushStudiesOnMedia/DeleteLocalStudiesStoredOnMedia/g"
FileSystemMgt="$FileSystemMgt;s/DeleteStudiesStorageNotCommited/DeleteStudiesFromSystem/g"
FileSystemMgt="$FileSystemMgt;s/StudyCacheTimeout/StudyAgeForDeletion/g"
FileSystemMgt="$FileSystemMgt;s/PurgeFilesInterval/DeleteFilesInterval/g"
FileSystemMgt="$FileSystemMgt;s/PurgeFilesLimit/DeleteFilesLimit/g"
FileSystemMgt="$FileSystemMgt;s/PurgeFilesAfterFreeDiskSpace/DeleteFilesAfterFreeDiskSpace/g"

sed $SED_OPTS -e "$FileSystemMgt" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DFileSystemMgt.xml

MPPSScp="s/CalledAETs/CalledAETitles/g"
MPPSScp="$MPPSScp;s/CallingAETs/CallingAETitles/g"
MPPSScp="$MPPSScp;s/MaxPDULength/MaximumPDULength/g"

sed $SED_OPTS -e "$MPPSScp" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DMPPSScp.xml
	
MWLFindScp="s/MaxPDULength/MaximumPDULength/g"

sed $SED_OPTS -e "$MWLFindScp" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DMWLFindScp.xml

QueryRetrieveScp="s/CalledAETs/CalledAETitles/g"
QueryRetrieveScp="$QueryRetrieveScp;s/CallingAETs/CallingAETitles/g"
QueryRetrieveScp="$QueryRetrieveScp;s/SendNoPixelDataToAETs/SendNoPixelDataToAETitles/g"
QueryRetrieveScp="$QueryRetrieveScp;s/IgnoreUnsupportedSOPClassFailuresByAETs/IgnoreUnsupportedSOPClassFailures/g"
QueryRetrieveScp="$QueryRetrieveScp;s/RequestStgCmtFromAETs/RequestStorageCommitFromAETitles/g"
QueryRetrieveScp="$QueryRetrieveScp;s/MaxBlockedFindRSP/MaximumBlockedFindResponse/g"
QueryRetrieveScp="$QueryRetrieveScp;s/MaxUIDsPerMoveRQ/MaximumUIDsPerMoveRequest/g"
QueryRetrieveScp="$QueryRetrieveScp;s/MaxPDULength/MaximumPDULength/g"
QueryRetrieveScp="$QueryRetrieveScp;s/AcTimeout/AcceptTimeout/g"
QueryRetrieveScp="$QueryRetrieveScp;s/DimseTimeout/DIMSETimeout/g"
QueryRetrieveScp="$QueryRetrieveScp;s/SoCloseDelay/SocketCloseDelay/g"
QueryRetrieveScp="$QueryRetrieveScp;s/EjbProviderURL/EJBProviderURL/g"

sed $SED_OPTS -e "$QueryRetrieveScp" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DQueryRetrieveScp.xml

StgCmtScuScp="s/CalledAETs/CalledAETitles/g"
StgCmtScuScp="$StgCmtScuScp;s/CallingAETs/CallingAETitles/g"
StgCmtScuScp="$StgCmtScuScp;s/ReceiveResultInSameAssocTimeout/ReceiveResultInSameAssociationTimeout/g"
StgCmtScuScp="$StgCmtScuScp;s/ScuRetryIntervalls/SCURetryIntervals/g"
StgCmtScuScp="$StgCmtScuScp;s/ScpRetryIntervalls/SCPRetryIntervals/g"
StgCmtScuScp="$StgCmtScuScp;s/MaxPDULength/MaximumPDULength/g"
StgCmtScuScp="$StgCmtScuScp;s/AcTimeout/AcceptTimeout/g"
StgCmtScuScp="$StgCmtScuScp;s/DimseTimeout/DIMSETimeout/g"
StgCmtScuScp="$StgCmtScuScp;s/SoCloseDelay/SocketCloseDelay/g"
StgCmtScuScp="$StgCmtScuScp;s/EjbProviderURL/EJBProviderURL/g"

sed $SED_OPTS -e "$StgCmtScuScp" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DStgCmtScuScp.xml
	
StoreScp="s/CalledAETs/CalledAETitles/g"
StoreScp="$StoreScp;s/CallingAETs/CallingAETitles/g"
StoreScp="$StoreScp;s/CoerceWarnCallingAETs/WarnForCoercedAETitles/g"
StoreScp="$StoreScp;s/StoreDuplicateIfDiffMD5/StoreDuplicatesIfDifferentMD5/g"
StoreScp="$StoreScp;s/StoreDuplicateIfDiffHost/StoreDuplicatesIfDifferentMD5/g"
StoreScp="$StoreScp;s/UpdateDatabaseMaxRetries/UpdateDatabaseMaximumRetries/g"
StoreScp="$StoreScp;s/MaxCountUpdateDatabaseRetries/UpdateDatabasePerformedRetries/g"
StoreScp="$StoreScp;s/ImageCUIDs/AcceptedImageSOPClasses/g"
StoreScp="$StoreScp;s/OtherCUIDs/AcceptedOtherSOPClasses/g"
StoreScp="$StoreScp;s/MaxPDULength/MaximumPDULength/g"
StoreScp="$StoreScp;s/EjbProviderURL/EJBProviderURL/g"

sed $SED_OPTS -e "$StoreScp" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DStoreScp.xml
	
StudyMgtScu="s/CallingAET/CalledAETitle/g"
StudyMgtScu="$StudyMgtScu;s/RetryIntervalls/RetryIntervals/g"
StudyMgtScu="$StudyMgtScu;s/AcTimeout/AcceptTimeout/g"
StudyMgtScu="$StudyMgtScu;s/DimseTimeout/DIMSETimeout/g"
StudyMgtScu="$StudyMgtScu;s/SoCloseDelay/SocketCloseDelay/g"

sed $SED_OPTS -e "$StudyMgtScu" \
	$DIRNAME/../server/pacs/data/xmbean-attrs/tiani.archive@3Aservice@3DStudyMgtScu.xml
	
	