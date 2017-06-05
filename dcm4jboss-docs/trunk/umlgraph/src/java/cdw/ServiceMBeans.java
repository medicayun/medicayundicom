package cdw;

/**
 * @depend - - - SpoolDir
 * @depend - - - LabelPrint
 * @depend - - - CDRecordQueue
 */
class CDRecordMediaWriter {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.CDRecordMediaWriter
 * @opt !hide
 */
class cdRecordMediaWriter {}

class CDRecordQueue {}

class MakeIsoImageQueue {}

class MediaComposerQueue {}

/**
 * @depend - - - SpoolDir
 * @depend - - - LabelPrint
 * @depend - - - NeroCmdQueue
 */
class NeroCmdMediaWriter {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.NeroCmdMediaWriter
 * @opt !hide
 */
class neroCmdMediaWriter {}

class NeroCmdQueue {}

class DcmServer {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.DcmServer
 * @opt !hide
 */
class dcmServer {}

class LabelPrint {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.LabelPrint
 * @opt !hide
 */
class labelPrint {}

/**
 * @depend - - - SpoolDir
 * @depend - - - MakeIsoImageQueue
 */
class MakeIsoImage {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.MakeIsoImage
 * @opt !hide
 */
class makeIsoImage {}

/**
 * @depend - - - SpoolDir
 * @depend - - - MakeIsoImage
 * @depend - - - MediaComposerQueue
 * @depend - - - MakeIsoImageQueue
 */
class MediaComposer {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.MediaComposer
 * @opt !hide
 */
class mediaComposer {}

/**
 * @depend - - - DcmServer
 * @depend - - - SpoolDir
 * @depend - - - MediaComposerQueue
 */
class MediaCreationMgtSCP {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.MediaCreationMgtSCP
 * @opt !hide
 */
class mediaCreationMgtSCP {}

class Scheduler {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.Scheduler
 * @opt !hide
 */
class scheduler {}

/**
 * @depend - - - Scheduler
 */
class SpoolDir {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.SpoolDir
 * @opt !hide
 */
class spoolDir {}

/**
 * @depend - - - DcmServer
 * @depend - - - SpoolDir
 */
class StoreSCP {}

/**
 * @view
 * @opt hide
 * 
 * @match context cdw.StoreSCP
 * @opt !hide
 */
class storeSCP {}

