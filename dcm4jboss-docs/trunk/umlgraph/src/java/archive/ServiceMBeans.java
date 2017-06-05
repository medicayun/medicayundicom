package archive;

class ExportManagerQueue {}

class FileCopyQueue {}

class HL7SendQueue {}

class IANScuQueue {}

class MCMScuQueue {}

class MPPSScuQueue {}

class MoveScuQueue {}

class PPSExceptionMgtQueue {}

class PPSScuQueue {}

class PrefetchQueue {}

class PurgeStudyQueue {}

class SendmailQueue {}

class StgCmtScuScpQueue {}

class StudyMgtScuQueue {}

class UpgradeToEnhancedMFQueue {}

/**
 * @depend - - - AuditLogger
 * @depend - - - ECHOService
 */
class AE {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.AE
 * @opt !hide
 */
class ae {}

class AuditLogger {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.AuditLogger
 * @opt !hide
 */
class auditLogger {}

/**
 * @depend - - - Scheduler
 */
class CheckStudyPatientService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.CheckStudyPatientService
 * @opt !hide
 */
class checkStudyPatientService {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - Scheduler
 */
class CompressionService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.CompressionService
 * @opt !hide
 */
class compressionService {}

/**
 * @depend - - - Scheduler
 */
class ConsistencyService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ConsistencyService
 * @opt !hide
 */
class consistencyService {}

/**
 * @depend - - - AuditLogger
 * @depend - - - FileSystemMgt
 * @depend - - - HL7Send
 * @depend - - - StoreScp
 * @depend - - - StudyMgtScu
 * @depend - - - MPPSScp
 */
class ContentEditService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ContentEditService
 * @opt !hide
 */
class contentEditService {}

/**
 * @depend - - - DicomSecurity
 * @depend - - - TLSConfig
 */
class DcmServer {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.DcmServer
 * @opt !hide
 */
class dcmServer {}

class DeviceService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.DeviceService
 * @opt !hide
 */
class deviceService {}

/**
 */
class DicomSecurity {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.DicomSecurity
 * @opt !hide
 */
class dicomSecurity {}

/**
 * @depend - - - TLSConfig
 */
class ECHOService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ECHOService
 * @opt !hide
 */
class echoService {}

/**
 * @depend - - - AuditLogger
 * @depend - - - IANScu
 * @depend - - - StoreScp
 * @depend - - - Templates
 * @depend - - - TLSConfig
 * @depend - - - ExportManagerQueue
 * @depend - - - JMS
 */
class ExportManager {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ExportManager
 * @opt !hide
 */
class exportManager {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - StoreScp
 * @depend - - - FileCopyQueue
 * @depend - - - JMS
 */
class FileCopy {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.FileCopy
 * @opt !hide
 */
class fileCopy {}

/**
 * @depend - - - AE
 * @depend - - - Scheduler
 * @depend - - - TarRetriever
 * @depend - - - PurgeStudyQueue
 * @depend - - - JMS
 */
class FileSystemMgt {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.FileSystemMgt
 * @opt !hide
 */
class fileSystemMgt {}

class FixPatientAttributesService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.FixPatientAttributesService
 * @opt !hide
 */
class fixPatientAttributesService {}

/**
 * @depend - - - StoreScp
 * @depend - - - MoveScu
 * @depend - - - ContentEditService
 */
class Forward {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.Forward
 * @opt !hide
 */
class forward {}

/**
 * @depend - - - StoreScp
 * @depend - - - MoveScu
 * @depend - - - Templates
 */
class Forward2 {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.Forward2
 * @opt !hide
 */
class forward2 {}

/**
 */
class GPWLFeed {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.GPWLFeed
 * @opt !hide
 */
class gpwlFeed {}

/**
 * @depend - - - IANScu
 * @depend - - - Templates
 */
class GPWLFeed2 {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.GPWLFeed2
 * @opt !hide
 */
class gpwlFeed2 {}

/**
 * @depend - - - DcmServer
 * @depend - - - AuditLogger
 */
class GPWLScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.GPWLScp
 * @opt !hide
 */
class gpwlScp {}

class GPWLScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.GPWLScu
 * @opt !hide
 */
class gpwlScu {}

/**
 * @depend - - - AuditLogger
 * @depend - - - HL7Server
 * @depend - - - TLSConfig
 * @depend - - - HL7SendQueue
 * @depend - - - JMS
 */
class HL7Send {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.HL7Send
 * @opt !hide
 */
class hl7Send {}

/**
 * @depend - - - HL7Send
 */
class PIXQuery {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.PIXQuery
 * @opt !hide
 */
class pixQuery {}

/**
 * @depend - - - TLSConfig
 * @depend - - - AuditLogger
 * @depend - - - Templates
 */
class HL7Server {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.HL7Server
 * @opt !hide
 */
class hl7Server {}

/**
 * @depend - - - HL7Server
 * @depend - - - Templates
 */
class ADTService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ADTService
 * @opt !hide
 */
class adtService {}

/**
 * @depend - - - HL7Server
 * @depend - - - DeviceService
 * @depend - - - Templates
 */
class ORMService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ORMService
 * @opt !hide
 */
class ormService {}


/**
 * @depend - - - HL7Server
 * @depend - - - TLSConfig
 * @depend - - - MoveScu
 * @depend - - - PrefetchQueue
 * @depend - - - Templates
 * @depend - - - JMS
 */
class Prefetch {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.Prefetch
 * @opt !hide
 */
class prefetch {}

/**
 * @depend - - - HL7Server
 * @depend - - - ExportManager
 * @depend - - - Templates
 */
class MDMService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MDMService
 * @opt !hide
 */
class mdmService {}

/**
 * @depend - - - HL7Server
 * @depend - - - ExportManager
 * @depend - - - Templates
 */
class ORUService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.ORUService
 * @opt !hide
 */
class oruService {}

/**
 * @depend - - - AE
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 * @depend - - - TLSConfig
 */
class HPScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.HPScp
 * @opt !hide
 */
class hpScp {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - StoreScp
 * @depend - - - MPPSScp
 * @depend - - - TLSConfig
 * @depend - - - IANScuQueue
 * @depend - - - JMS
 */
class IANScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.IANScu
 * @opt !hide
 */
class ianScu {}

class JMS {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.JMS
 * @opt !hide
 */
class jms {}

class KeyObjectService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.KeyObjectService
 * @opt !hide
 */
class keyObjectService {}

/**
 * @depend - - - JMS
 * @depend - - - MCMScuQueue
 * @depend - - - Scheduler
 * @depend - - - Sendmail
 * @depend - - - TLSConfig
 */
class MCMScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MCMScu
 * @opt !hide
 */
class mcmScu {}

/**
 * @depend - - - Scheduler
 */
class MD5CheckService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MD5CheckService
 * @opt !hide
 */
class md5CheckService {}

/**
 */
class MigrateOldARRService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MigrateOldARRService
 * @opt !hide
 */
class migrateOldARRService {}

/**
 * @depend - - - MPPSScp
 * @depend - - - HL7Send
 * @depend - - - Templates
 */
class MPPS2ORM {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MPPS2ORM
 * @opt !hide
 */
class mpps2ORM {}

/**
 * @depend - - - Scheduler
 * @depend - - - MPPSScu
 */
class MPPSEmulator {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MPPSEmulator
 * @opt !hide
 */
class mppsEmulator {}

/**
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 * @depend - - - Templates
 */
class MPPSScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MPPSScp
 * @opt !hide
 */
class mppsScp {}

/**
 * @depend - - - MPPSScp
 * @depend - - - TLSConfig
 * @depend - - - MPPSScuQueue
 * @depend - - - JMS
 */
class MPPSScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MPPSScu
 * @opt !hide
 */
class mppsScu {}

/**
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 * @depend - - - MPPSScp
 * @depend - - - MWLScu
 * @depend - - - Templates
 */
class MWLFindScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MWLFindScp
 * @opt !hide
 */
class mwlFindScp {}

/**
 * @depend - - - TLSConfig
 */
class MWLScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MWLScu
 * @opt !hide
 */
class mwlScu {}

/**
 * @depend - - - QueryRetrieveScp
 * @depend - - - TLSConfig
 * @depend - - - MoveScuQueue
 * @depend - - - JMS
 */
class MoveScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.MoveScu
 * @opt !hide
 */
class moveScu {}

/**
 * @depend - - - MPPSScp
 * @depend - - - PPSExceptionMgtQueue
 * @depend - - - JMS
 */
class PPSExceptionMgtService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.PPSExceptionMgtService
 * @opt !hide
 */
class ppsExceptionMgtService {}

/**
 * @depend - - - GPWLScp
 * @depend - - - TLSConfig
 * @depend - - - PPSScuQueue
 * @depend - - - JMS
 */
class PPSScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.PPSScu
 * @opt !hide
 */
class ppsScu {}

/**
 * @depend - - - AE
 * @depend - - - DcmServer
 * @depend - - - DicomSecurity
 * @depend - - - TLSConfig
 * @depend - - - AuditLogger
 * @depend - - - FileSystemMgt
 * @depend - - - StgCmtScuScp
 * @depend - - - TarRetriever
 * @depend - - - Templates
 * @depend - - - PIXQuery
 */
class QueryRetrieveScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.QueryRetrieveScp
 * @opt !hide
 */
class queryRetrieveScp {}

/**
 * @depend - - - AuditLogger
 * @depend - - - FileSystemMgt
 * @depend - - - Scheduler
 */
class RIDService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.RIDService
 * @opt !hide
 */
class ridService {}

class Scheduler {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.Scheduler
 * @opt !hide
 */
class scheduler {}

/**
 * @depend - - - SendmailQueue
 * @depend - - - JMS
 */
class Sendmail {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.Sendmail
 * @opt !hide
 */
class sendmail {}

/**
 * @depend - - - AE
 * @depend - - - DcmServer
 * @depend - - - FileSystemMgt
 * @depend - - - TLSConfig
 * @depend - - - StgCmtScuScpQueue
 * @depend - - - JMS
 */
class StgCmtScuScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StgCmtScuScp
 * @opt !hide
 */
class stgCmtScuScp {}

/**
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 * @depend - - - FileSystemMgt
 * @depend - - - MWLScu
 * @depend - - - Scheduler
 * @depend - - - Templates
 */
class StoreScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StoreScp
 * @opt !hide
 */
class storeScp {}

/**
 * @depend - - - AuditLogger
 * @depend - - - DcmServer
 */
class StudyMgtScp {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StudyMgtScp
 * @opt !hide
 */
class studyMgtScp {}

/**
 * @depend - - - StudyMgtScp
 * @depend - - - TLSConfig
 * @depend - - - StudyMgtScuQueue
 * @depend - - - JMS
 */
class StudyMgtScu {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StudyMgtScu
 * @opt !hide
 */
class studyMgtScu {}

/**
 * @depend - - - HL7Server
 * @depend - - - StoreScp
 * @depend - - - MPPSScp
 */
class StudyPermission {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StudyPermission
 * @opt !hide
 */
class studyPermission {}

/**
 * @depend - - - TLSConfig
 * @depend - - - Scheduler
 */
class StudyReconciliation {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StudyReconciliation
 * @opt !hide
 */
class studyReconciliation {}

/**
 * @depend - - - GPWLScp
 */
class StudyStatus {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.StudyStatus
 * @opt !hide
 */
class studyStatus {}

/**
 * @depend - - - Scheduler
 */
class SyncFileStatus {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.SyncFileStatus
 * @opt !hide
 */
class syncFileStatus {}

/**
 * @depend - - - AuditLogger
 */
class TLSConfig {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.TLSConfig
 * @opt !hide
 */
class tlsConfig {}

class TarRetriever {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.TarRetriever
 * @opt !hide
 */
class tarRetriever {}

class Templates {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.Templates
 * @opt !hide
 */
class templates {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - TarRetriever
 */
class UpdateAttributes {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.UpdateAttributes
 * @opt !hide
 */
class updateAttributes {}

/**
 * @depend - - - FileSystemMgt
 * @depend - - - StoreScp
 * @depend - - - JMS
 * @depend - - - UpgradeToEnhancedMFQueue
 */
class UpgradeToEnhancedMF {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.UpgradeToEnhancedMF
 * @opt !hide
 */
class upgradeToEnhancedMF {}

/**
 * @depend - - - AuditLogger
 * @depend - - - FileSystemMgt
 * @depend - - - Scheduler
 */
class WADOService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.WADOService
 * @opt !hide
 */
class wadoService {}

/**
 * @depend - - - AuditLogger
 * @depend - - - PIXQuery
 * @depend - - - KeyObjectService
 * @depend - - - IANScu
 */
class XDS_I {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.XDS_I
 * @opt !hide
 */
class xds_I {}

class XDSService {}

/**
 * @view
 * @opt hide
 * 
 * @match context archive.XDSService
 * @opt !hide
 */
class xdsService {}
