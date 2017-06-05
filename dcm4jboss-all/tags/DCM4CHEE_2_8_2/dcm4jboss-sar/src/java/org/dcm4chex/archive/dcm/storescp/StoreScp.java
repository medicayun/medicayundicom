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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.dcm.storescp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.ObjectNotFoundException;
import javax.xml.transform.Templates;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.util.BufferedOutputStream;
import org.dcm4che.util.DAFormat;
import org.dcm4che.util.TMFormat;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.codec.CompressCmd;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.config.CompressionRules;
import org.dcm4chex.archive.config.IssuerOfPatientIDRules;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.util.XSLTUtils;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2514 $
 * @since 03.08.2003
 */
public class StoreScp extends DcmServiceBase implements AssociationListener {

    private static final String STORE_XSL = "cstorerq.xsl";
    private static final String STORE_XML = "-cstorerq.xml";
    private static final String RECEIVE_BUFFER = "RECEIVE_BUFFER";
    private static final String ASSOC_PK = "ASSOC_PK";

	private static final int[] TYPE1_ATTR = { Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID, Tags.SOPInstanceUID, Tags.SOPClassUID, };

    final StoreScpService service;

    private final Logger log;

	private boolean studyDateInFilePath = false;
	private boolean yearInFilePath = true;
	private boolean monthInFilePath = true;
	private boolean dayInFilePath = true;
	private boolean hourInFilePath = false;
	
	private boolean acceptMissingPatientID = true;
	private boolean acceptMissingPatientName = true;
	private String[] generatePatientID = null;
	private IssuerOfPatientIDRules issuerOfPatientIDRules = 
			new IssuerOfPatientIDRules("PACS-:TIANI");

	private boolean serializeDBUpdate = false;
    private int updateDatabaseMaxRetries = 2;
    private int maxCountUpdateDatabaseRetries = 0;

    private boolean storeDuplicateIfDiffMD5 = true;
    private boolean storeDuplicateIfDiffHost = true;
    private long updateDatabaseRetryInterval = 0L;

    private CompressionRules compressionRules = new CompressionRules("");

    private String[] coerceWarnCallingAETs = {};
	private String[] ignorePatientIDCallingAETs = {};

    private boolean checkIncorrectWorklistEntry = true;
    
	public StoreScp(StoreScpService service) {
        this.service = service;
        this.log = service.getLog();
    }

	public final boolean isAcceptMissingPatientID() {
		return acceptMissingPatientID;
	}

	public final void setAcceptMissingPatientID(boolean accept) {
		this.acceptMissingPatientID = accept;
	}

	public final boolean isAcceptMissingPatientName() {
		return acceptMissingPatientName;
	}

	public final void setAcceptMissingPatientName(boolean accept) {
		this.acceptMissingPatientName = accept;
	}

	public final boolean isSerializeDBUpdate() {
		return serializeDBUpdate;
	}

	public final void setSerializeDBUpdate(boolean serialize) {
		this.serializeDBUpdate = serialize;
	}

    public final String getGeneratePatientID() {
    	if (generatePatientID == null)
    		return "NONE";
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < generatePatientID.length; i++) {
			sb.append(generatePatientID[i]);
		}
		return sb.toString();
	}

	public final void setGeneratePatientID(String pattern) {
		if (pattern.equalsIgnoreCase("NONE"))
		{
			this.generatePatientID = null;
			return;
		}
		int pl = pattern.indexOf('#');
		int pr = pl != -1 ? pattern.lastIndexOf('#') : -1;
		int sl = pattern.indexOf('$');
		int sr = sl != -1 ? pattern.lastIndexOf('$') : -1;		
		if (pl == -1 && sl == -1)
		{
			this.generatePatientID = new String[] { pattern };
		} else if (pl != -1 && sl != -1)
		{
			this.generatePatientID = pl < sl 
				? split(pattern, pl, pr, sl, sr)
				: split(pattern, sl, sr, pl, pr);
				
		} else {
			this.generatePatientID = pl != -1 
				? split(pattern, pl, pr)
				: split(pattern, sl, sr);
		}
			
	}

	private String[] split(String pattern, int l1, int r1) {
		return new String[] { pattern.substring(0, l1),
				pattern.substring(l1, r1 + 1), pattern.substring(r1 + 1), };
	}
	
	private String[] split(String pattern, int l1, int r1, int l2, int r2) {
		if (r1 > l2)
			throw new IllegalArgumentException(pattern);
		return new String[] { pattern.substring(0, l1),
				pattern.substring(l1, r1 + 1), pattern.substring(r1 + 1, l2),
				pattern.substring(l2, r2 + 1), pattern.substring(r2 + 1) };
	}

	public final String getIssuerOfPatientIDRules() {
		return issuerOfPatientIDRules.toString();
	}

	public final void setIssuerOfPatientIDRules(String rules) {
		this.issuerOfPatientIDRules = new IssuerOfPatientIDRules(rules);
	}

    public final String getIgnorePatientIDCallingAETs() {
        return StringUtils.toString(ignorePatientIDCallingAETs, '\\');
	}

	public final void setIgnorePatientIDCallingAETs(String aets) {
        ignorePatientIDCallingAETs = StringUtils.split(aets, '\\');
	}

	public final String getCoerceWarnCallingAETs() {
        return StringUtils.toString(coerceWarnCallingAETs, '\\');
    }

    public final void setCoerceWarnCallingAETs(String aets) {
        coerceWarnCallingAETs = StringUtils.split(aets, '\\');
    }

	public final boolean isStudyDateInFilePath() {
		return studyDateInFilePath;
	}

	public final void setStudyDateInFilePath(boolean studyDateInFilePath) {
		this.studyDateInFilePath = studyDateInFilePath;
	}

	public final boolean isYearInFilePath() {
		return yearInFilePath;
	}

	public final void setYearInFilePath(boolean yearInFilePath) {
		this.yearInFilePath = yearInFilePath;
	}

	public final boolean isMonthInFilePath() {
		return monthInFilePath;
	}

	public final void setMonthInFilePath(boolean monthInFilePath) {
		this.monthInFilePath = monthInFilePath;
	}

    public final boolean isDayInFilePath() {
		return dayInFilePath;
	}

	public final void setDayInFilePath(boolean dayInFilePath) {
		this.dayInFilePath = dayInFilePath;
	}

	public final boolean isHourInFilePath() {
		return hourInFilePath;
	}

	public final void setHourInFilePath(boolean hourInFilePath) {
		this.hourInFilePath = hourInFilePath;
	}

	public final boolean isStoreDuplicateIfDiffHost() {
        return storeDuplicateIfDiffHost;
    }

    public final void setStoreDuplicateIfDiffHost(boolean storeDuplicate) {
        this.storeDuplicateIfDiffHost = storeDuplicate;
    }

    public final boolean isStoreDuplicateIfDiffMD5() {
        return storeDuplicateIfDiffMD5;
    }

    public final void setStoreDuplicateIfDiffMD5(boolean storeDuplicate) {
        this.storeDuplicateIfDiffMD5 = storeDuplicate;
    }

    public final CompressionRules getCompressionRules() {
        return compressionRules;
    }

    public final void setCompressionRules(CompressionRules compressionRules) {
        this.compressionRules = compressionRules;
    }

    public final int getUpdateDatabaseMaxRetries() {
        return updateDatabaseMaxRetries;
    }

    public final void setUpdateDatabaseMaxRetries(int updateDatabaseMaxRetries) {
        this.updateDatabaseMaxRetries = updateDatabaseMaxRetries;
    }

    public final int getMaxCountUpdateDatabaseRetries() {
        return maxCountUpdateDatabaseRetries;
    }

    public final void setMaxCountUpdateDatabaseRetries(int count) {
        this.maxCountUpdateDatabaseRetries = count;
    }

    public final long getUpdateDatabaseRetryInterval() {
        return updateDatabaseRetryInterval;
    }

    public final void setUpdateDatabaseRetryInterval(long interval) {
        this.updateDatabaseRetryInterval = interval;
    }

	/**
	 * @return Returns the checkIncorrectWorklistEntry.
	 */
	public boolean isCheckIncorrectWorklistEntry() {
		return checkIncorrectWorklistEntry;
	}
    
	/**
	 * @param checkIncorrectWorklistEntry The checkIncorrectWorklistEntry to set.
	 */
	public void setCheckIncorrectWorklistEntry(
			boolean checkIncorrectWorklistEntry) {
		this.checkIncorrectWorklistEntry = checkIncorrectWorklistEntry;
	}
	
	
    protected void doCStore(ActiveAssociation activeAssoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        String iuid = rqCmd.getAffectedSOPInstanceUID();
        String cuid = rqCmd.getAffectedSOPClassUID();
        InputStream in = rq.getDataAsStream();
        Association assoc = activeAssoc.getAssociation();
        String callingAET = assoc.getCallingAET();
        File file = null;
        try {		
            List duplicates = new QueryFilesCmd(iuid).getFileDTOs();
            if (!(duplicates.isEmpty() || storeDuplicateIfDiffMD5 
                    || storeDuplicateIfDiffHost && !containsLocal(duplicates))) {
                log.info("Received Instance[uid=" + iuid
                        + "] already exists - ignored");
                return;
            }

            DcmDecodeParam decParam = DcmDecodeParam.valueOf(rq
                    .getTransferSyntaxUID());
            Dataset ds = objFact.newDataset();
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, Tags.PixelData);
			log.debug("Dataset:\n");
			log.debug(ds);
            service.logDIMSE(assoc, STORE_XML, ds);
            checkDataset(assoc, rqCmd, ds);
            if ( isCheckIncorrectWorklistEntry() && checkIncorrectWorklistEntry(ds) ) {
                log.info("Received Instance[uid=" + iuid
                        + "] ignored! Reason: Incorrect Worklist entry selected!");
                return;
            }            	
            FileSystemDTO fsDTO = service.selectStorageFileSystem();
            File baseDir = FileUtils.toFile(fsDTO.getDirectoryPath());
            file = makeFile(baseDir, ds);
            String compressTSUID = parser.getReadTag() == Tags.PixelData
                    && parser.getReadLength() != -1 ? compressionRules
                    .getTransferSyntaxFor(assoc, ds) : null;
            String tsuid = compressTSUID != null ? compressTSUID : rq
                    .getTransferSyntaxUID();
			ds.setFileMetaInfo(objFact.newFileMetaInfo(
					rqCmd.getAffectedSOPClassUID(),
					rqCmd.getAffectedSOPInstanceUID(),
                    tsuid ));

            byte[] md5sum = storeToFile(parser, ds, file, getByteBuffer(assoc));
            if (md5sum != null && ignoreDuplicate(duplicates, md5sum)) {
                log.info("Received Instance[uid=" + iuid
                        + "] already exists - ignored");
                deleteFailedStorage(file);
                return;
            }

            final int baseDirPathLength = baseDir.getPath().length();
            final String filePath = file.getPath().substring(
                    baseDirPathLength + 1).replace(File.separatorChar, '/');
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putAE(PrivateTags.CallingAET, assoc.getCallingAET());
            ds.putAE(PrivateTags.CalledAET, assoc.getCalledAET());
            ds.putAE(Tags.RetrieveAET, fsDTO.getRetrieveAET());
            Dataset coerced = null;
    		try {
    			Templates stylesheet = service.getCoercionTemplatesFor(callingAET, STORE_XSL);
    			if (stylesheet != null)
    			{
    				coerced = XSLTUtils.coerce(ds, stylesheet,
    						toXsltParam(assoc, new Date()));
    			}
    		} catch (Exception e) {
    			log.warn("Coercion of attributes failed:", e);
    		}
            Storage store = getStorage(assoc);
            Long assocpk = (Long) assoc.getProperty(ASSOC_PK);
            if (assocpk == null) {
                assocpk = store.initAssociation(assoc.getCallingAET(),
                        assoc.getCalledAET(), fsDTO.getRetrieveAET());
                assoc.putProperty(ASSOC_PK, assocpk);
            } else {
                String seriuid = ds.getString(Tags.SeriesInstanceUID);
                SeriesStored seriesStored = store.checkSeriesStored(assocpk,
                        seriuid);
                if (seriesStored != null) {
                    log.debug("Send SeriesStoredNotification - series changed");
                    doAfterSeriesIsStored(store, assoc.getSocket(), seriesStored);
                    store.resetAssociation(assocpk);
                }
            }
            Dataset coercedElements = updateDB(store, assocpk , ds,
                    fsDTO.getDirectoryPath(), filePath, file, md5sum);
            ds.putAll(coercedElements, Dataset.MERGE_ITEMS);
            if (coerced == null)
            	coerced = coercedElements;
            else
            	coerced.putAll(coercedElements, Dataset.MERGE_ITEMS);
            if (coerced.isEmpty()
                    || !contains(coerceWarnCallingAETs, assoc.getCallingAET())) {
                rspCmd.putUS(Tags.Status, Status.Success);
            } else {
                int[] coercedTags = new int[coerced.size()];
                Iterator it = coerced.iterator();
                for (int i = 0; i < coercedTags.length; i++) {
                    coercedTags[i] = ((DcmElement) it.next()).tag();
                }
                rspCmd.putAT(Tags.OffendingElement, coercedTags);
                rspCmd.putUS(Tags.Status, Status.CoercionOfDataElements);
            }
        } catch (DcmServiceException e) {
            log.warn(e.getMessage(), e);
            deleteFailedStorage(file);
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            deleteFailedStorage(file);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private boolean checkIncorrectWorklistEntry(Dataset ds) throws Exception {
        Dataset refPPS = ds.getItem(Tags.RefPPSSeq);
        if (refPPS == null) {
            return false;
        }
        String ppsUID = refPPS.getString(Tags.RefSOPInstanceUID);
        if ( ppsUID == null ) {
            return false;
        }
        Dataset mpps;
        try {
            mpps = getMPPSManager().getMPPS(ppsUID);
        } catch (ObjectNotFoundException e) {
            return false;
        }
        Dataset item = mpps.getItem(Tags.PPSDiscontinuationReasonCodeSeq);
        return item != null && "110514".equals(item.getString(Tags.CodeValue)) && 
                "DCM".equals(item.getString(Tags.CodingSchemeDesignator));
    }

    private MPPSManager getMPPSManager() throws CreateException,
            RemoteException, HomeFactoryException {
        return ((MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME)).create();
    }

	private Map toXsltParam(Association a, Date now) {
		HashMap param = new HashMap();
		param.put("calling", a.getCallingAET());
		param.put("called", a.getCalledAET());
		param.put("date", new DAFormat().format(now));
		param.put("time", new TMFormat().format(now));
		return null;
	}
    
	private byte[] getByteBuffer(Association assoc) {
		byte[] buf = (byte[]) assoc.getProperty(RECEIVE_BUFFER);
		if (buf == null) {
			buf = new byte[service.getBufferSize()];
			assoc.putProperty(RECEIVE_BUFFER, buf);
		}
		return buf;
	}

	private boolean containsLocal(List duplicates) {
        for (int i = 0, n = duplicates.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) duplicates.get(i);
            if (service.isLocalRetrieveAET(dto.getRetrieveAET()))
                return true;
        }
        return false;
    }

    private boolean ignoreDuplicate(List duplicates, byte[] md5sum) {
        for (int i = 0, n = duplicates.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) duplicates.get(i);
            if (storeDuplicateIfDiffMD5
                    && !Arrays.equals(md5sum, dto.getFileMd5()))
                continue;
            if (storeDuplicateIfDiffHost
                    && !service.isLocalRetrieveAET(dto.getRetrieveAET()))
                continue;
            return true;
        }
        return false;
    }

    private void deleteFailedStorage(File file) {
        if (file == null)
            return;
        log.info("M-DELETE file:" + file);
        file.delete();
        // purge empty series and study directory
        File seriesDir = file.getParentFile();
        if (seriesDir.delete())
            seriesDir.getParentFile().delete();
    }

    protected Dataset updateDB(Storage storage, Long assocpk, Dataset ds,
            String baseDir, String filePath, File file, byte[] md5)
            throws DcmServiceException, CreateException, HomeFactoryException,
            IOException {
        int retry = 0;
        for (;;) {
            try {
                if (serializeDBUpdate) {
                    synchronized (storage) {
                        return storage.store(assocpk, ds, baseDir, filePath,
                                (int) file.length(), md5);
                    }
                } else {
                    return storage.store(assocpk, ds, baseDir, filePath,
                            (int) file.length(), md5);
                }
            } catch (Exception e) {
                ++retry;
                if (retry > updateDatabaseMaxRetries) {
                    service.getLog().error(
                            "failed to update DB with entries for received "
                                    + file, e);
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                }
                maxCountUpdateDatabaseRetries = Math.max(retry,
                        maxCountUpdateDatabaseRetries);
                service.getLog().warn(
                        "failed to update DB with entries for received " + file
                                + " - retry", e);
                try {
                    Thread.sleep(updateDatabaseRetryInterval);
                } catch (InterruptedException e1) {
                    log.warn("update Database Retry Interval interrupted:", e1);
                }
            }
        }
    }

    Storage getStorage(Association assoc) throws RemoteException, CreateException,
            HomeFactoryException {
        Storage store = (Storage) assoc.getProperty(StorageHome.JNDI_NAME);
        if (store == null) {
            store = service.getStorage();
            assoc.putProperty(StorageHome.JNDI_NAME, store);
        }
        return store;
    }

    private File makeFile(File basedir, Dataset ds) throws IOException {
        Calendar date = Calendar.getInstance();
		if (studyDateInFilePath) {
			Date studyDate = ds.getDate(Tags.StudyDate, Tags.StudyTime);
			if (studyDate != null)
				date.setTime(studyDate);
		}
		StringBuffer filePath = new StringBuffer();
		if (yearInFilePath) {
			filePath.append(String.valueOf(date.get(Calendar.YEAR)));
			filePath.append(File.separatorChar);
		}
		if (monthInFilePath) {
			filePath.append(String.valueOf(date.get(Calendar.MONTH) + 1));
			filePath.append(File.separatorChar);
		}
		if (dayInFilePath) {
			filePath.append(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
			filePath.append(File.separatorChar);
		}
		if (hourInFilePath) {
			filePath.append(String.valueOf(date.get(Calendar.HOUR_OF_DAY)));
			filePath.append(File.separatorChar);
		}
		filePath.append(toHex(ds.getString(Tags.StudyInstanceUID).hashCode()));
		filePath.append(File.separatorChar);
		filePath.append(toHex(ds.getString(Tags.SeriesInstanceUID).hashCode()));
        File dir = new File(basedir, filePath.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
		return FileUtils.createNewFile(dir, ds.getString(Tags.SOPInstanceUID).hashCode());
    }

    private byte[] storeToFile(DcmParser parser, Dataset ds, File file, byte[] buffer)
    		throws Exception {
        log.info("M-WRITE file:" + file);
        MessageDigest md = null;
        BufferedOutputStream bos = null;
        if (service.isMd5sum()) {
            md = MessageDigest.getInstance("MD5");
            DigestOutputStream dos = new DigestOutputStream(
                    new FileOutputStream(file), md);
            bos = new BufferedOutputStream(dos, buffer);
        } else {
            bos = new BufferedOutputStream(new FileOutputStream(file), buffer);
        }
        try {
            DcmDecodeParam decParam = parser.getDcmDecodeParam();
            String tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
			DcmEncodeParam encParam = DcmEncodeParam.valueOf(tsuid);
            CompressCmd compressCmd = null;
            if (!decParam.encapsulated && encParam.encapsulated) {
                compressCmd = CompressCmd.createCompressCmd(ds, tsuid);
                compressCmd.coerceDataset(ds);
            }
            ds.writeFile(bos, encParam);
            if (parser.getReadTag() == Tags.PixelData) {
	            int len = parser.getReadLength();
	            InputStream in = parser.getInputStream();
	            if (encParam.encapsulated) {
	                ds.writeHeader(bos, encParam, Tags.PixelData, VRs.OB, -1);
	                if (decParam.encapsulated) {
	                    parser.parseHeader();
	                    while (parser.getReadTag() == Tags.Item) {
	                        len = parser.getReadLength();
	                        ds.writeHeader(bos, encParam, Tags.Item, VRs.NONE, len);
	                        bos.copyFrom(in, len);
	                        parser.parseHeader();
	                    }
	                } else {
	                    int read = compressCmd.compress(decParam.byteOrder, parser
	                            .getInputStream(), bos);
	                    in.skip(parser.getReadLength() - read);
	                }
	                ds.writeHeader(bos, encParam, Tags.SeqDelimitationItem,
	                        VRs.NONE, 0);
	            } else {
	                ds.writeHeader(bos, encParam, Tags.PixelData, parser
	                        .getReadVR(), len);
	                bos.copyFrom(in, len);
	            }
	            parser.parseDataset(decParam, -1);
	            ds.subSet(Tags.PixelData, -1).writeDataset(bos, encParam);
            }
        } finally {
            // We don't want to ignore the IOException since in rare cases the close() may cause
            // exception due to running out of physical space while the File System still holds
            // some internally cached data. In this case, we do want to fail this C-STORE.
            bos.close();
        }
        return md != null ? md.digest(): null;
    }

    private void checkDataset(Association assoc, Command rqCmd, Dataset ds)
            throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) {
                throw new DcmServiceException(
                        Status.DataSetDoesNotMatchSOPClassError,
                        "Missing Type 1 Attribute "
                                + Tags.toString(TYPE1_ATTR[i]));
            }
        }
        if (!rqCmd.getAffectedSOPInstanceUID().equals(
                ds.getString(Tags.SOPInstanceUID))) {
            throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Instance UID in Dataset differs from Affected SOP Instance UID");
        }
        if (!rqCmd.getAffectedSOPClassUID().equals(
                ds.getString(Tags.SOPClassUID))) {
            throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "SOP Class UID in Dataset differs from Affected SOP Class UID");
        }
		String pid = ds.getString(Tags.PatientID);
		String pname = ds.getString(Tags.PatientName);
		if (pid == null && !acceptMissingPatientID) {
            throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "Acceptance of objects without Patient ID is disabled");			
		}
		if (pname == null && !acceptMissingPatientName) {
            throw new DcmServiceException(
                    Status.DataSetDoesNotMatchSOPClassError,
                    "Acceptance of objects without Patient Name is disabled");			
		}
		if (contains(ignorePatientIDCallingAETs, assoc.getCallingAET())) {
			log.info("Ignore Patient ID " + pid 
					+ " for Patient Name " + pname
					+ " in object received from " + assoc.getCallingAET());
			pid = null;
			ds.putLO(Tags.PatientID, pid);
		}
		if (pid == null && generatePatientID != null) {
			if (generatePatientID != null) {
				pid = generatePatientID(ds);
				ds.putLO(Tags.PatientID, pid);
				log.info("Add generated Patient ID " + pid 
						+ " for Patient Name " + pname);
			}
		}
		if (pid != null) {
			String issuer = ds.getString(Tags.IssuerOfPatientID);
			if (issuer == null) {
				issuer = issuerOfPatientIDRules.issuerOf(pid);
				if (issuer != null) {
					ds.putLO(Tags.IssuerOfPatientID, issuer);
					log.info("Add missing Issuer Of Patient ID " + issuer
							+ " for Patient ID " + pid 
							+ " and Patient Name " + pname);					
				}
			}
		}		
    }

    private boolean contains(Object[] a, Object e) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(e)) {
                return true;
            }
        }
        return false;
    }

    
    private String generatePatientID(Dataset ds) {
    	if (generatePatientID.length == 1)
    		return generatePatientID[0];
    	
    	int suidHash = ds.getString(Tags.StudyInstanceUID).hashCode();
    	String pname = ds.getString(Tags.PatientName);
    	// generate different Patient IDs for different studies
    	// if no Patient Name
    	int pnameHash = pname == null || pname.length() == 0 ? suidHash
    			:  37 * ds.getString(Tags.PatientName).hashCode()
    					+ ds.getString(Tags.PatientBirthDate, "").hashCode();
    	
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < generatePatientID.length; i++) {
			append(sb, generatePatientID[i], pnameHash, suidHash);
		}
		return sb.toString();
	}
	
	private void append(StringBuffer sb, String s, int pnameHash, int suidHash) {
		final int l = s.length();
		if (l == 0)
			return;
		char c = s.charAt(0);
		if (c != '#' && c != '$')
		{
			sb.append(s);
			return;
		}
		String v = Long.toString((c == '#' ? pnameHash : suidHash) & 0xffffffffL);
		for (int i = v.length() - l; i < 0; i++)
			sb.append('0');
		sb.append(v);
	}

	private static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4) {
            ch8[i] = HEX_DIGIT[val & 0xf];
        }
        return String.valueOf(ch8);
    }

    // Implementation of AssociationListener

    public void write(Association src, PDU pdu) {
    }

    public void received(Association src, PDU pdu) {
    }

    public void write(Association src, Dimse dimse) {
    }

    public void received(Association src, Dimse dimse) {
    }

    public void error(Association src, IOException ioe) {
    }

    public void closing(Association assoc) {
    }

    public void closed(Association assoc) {
        Long assocpk = (Long) assoc.getProperty(ASSOC_PK);
        if (assocpk != null) {
            try {
                Storage store = getStorage(assoc);
                SeriesStored seriesStored = store.checkSeriesStored(assocpk,
                        null);
                if (seriesStored != null) {
                    log.debug("Send SeriesStoredNotification - association closed");
                    doAfterSeriesIsStored(store, assoc.getSocket(), seriesStored);
                }
                store.removeAssociation(assocpk);
                store.remove();
            } catch (Exception e) {
                log.error("Clean up on Association close failed:", e);
            }
        }
        if ( service.isFreeDiskSpaceOnDemand() ) {
        	service.callFreeDiskSpace();
        }
    }

	/**
	 * Finalize a stored series.
	 * <p>
	 * <dl>
	 * <dd>1) update database</dd>
	 * <dd>2) update study access time.</dd>
	 * <dd>3) Create Audit log entries for instances stored</dd>
	 * <dd>4) send SeriesStored JMX notification</dd>
	 * </dl>
	 * 
	 * @param s	The Association socket or null if series is stored local (e.g. undelete)
	 * @param seriesStored
	 */
	protected void doAfterSeriesIsStored(Storage store, Socket s,
            SeriesStored seriesStored) {
        Dataset ian = seriesStored.getIAN();       
        updateDBSeries(store, 
                ian.getItem(Tags.RefSeriesSeq).getString(Tags.SeriesInstanceUID));
        updateDBStudy(store, ian.getString(Tags.StudyInstanceUID));
		service.logInstancesStored(s, seriesStored);
		service.sendJMXNotification(seriesStored);
	}
	
    private void updateDBStudy(Storage store, final String suid) {
        int retry = 0;
        for (;;) {
            try {
				if (serializeDBUpdate) {
	                synchronized (store) {
	                    store.updateStudy(suid);
	                }
				} else {
                    store.updateStudy(suid);					
				}
                return;
            } catch (Exception e) {
                ++retry;
                if (retry > updateDatabaseMaxRetries) {
                    service.getLog().error(
                            "give up update DB Study Record [iuid=" + suid
                                    + "]:", e);
                    ;
                }
                maxCountUpdateDatabaseRetries = Math.max(retry,
                        maxCountUpdateDatabaseRetries);
                service.getLog().warn(
                        "failed to update DB Study Record [iuid=" + suid
                                + "] - retry:", e);
                try {
                    Thread.sleep(updateDatabaseRetryInterval);
                } catch (InterruptedException e1) {
                    log.warn("update Database Retry Interval interrupted:", e1);
                }
            }
        }
    }

    private void updateDBSeries(Storage store, final String seriuid) {
        int retry = 0;
        for (;;) {
            try {
				if (serializeDBUpdate) {
	                synchronized (store) {
	                    store.updateSeries(seriuid);
	                }
				} else {
                    store.updateSeries(seriuid);					
				}
                return;
            } catch (Exception e) {
                ++retry;
                if (retry > updateDatabaseMaxRetries) {
                    service.getLog().error(
                            "give up update DB Series Record [iuid=" + seriuid
                                    + "]:", e);
                }
                maxCountUpdateDatabaseRetries = Math.max(retry,
                        maxCountUpdateDatabaseRetries);
                service.getLog().warn(
                        "failed to update DB Series Record [iuid=" + seriuid
                                + "] - retry", e);
                try {
                    Thread.sleep(updateDatabaseRetryInterval);
                } catch (InterruptedException e1) {
                    log.warn("update Database Retry Interval interrupted:", e1);
                }
            }
        }
    }

}