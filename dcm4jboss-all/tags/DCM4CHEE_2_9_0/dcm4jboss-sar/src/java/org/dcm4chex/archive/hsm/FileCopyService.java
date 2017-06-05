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
 * Portions created by the Initial Developer are Copyright (C) 2005
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

package org.dcm4chex.archive.hsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dcm4che.util.BufferedOutputStream;
import org.dcm4che.util.MD5Utils;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2461 $ $Date: 2006-05-05 21:23:21 +0800 (周五, 05 5月 2006) $
 * @since Nov 9, 2005
 */
public class FileCopyService extends AbstractFileCopyService {


	protected void process(FileCopyOrder order) throws Exception {
		String destPath = order.getDestinationFileSystemPath();
		List fileInfos = order.getFileInfos();
		byte[] buffer = new byte[bufferSize];
		Storage storage = getStorageHome().create();
		Exception ex = null;
		MessageDigest digest = null;
		if (verifyCopy)
	        digest  = MessageDigest.getInstance("MD5");
		for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
			FileInfo finfo = (FileInfo) iter.next();
			File src = FileUtils.toFile(finfo.basedir + '/' + finfo.fileID);
			File dst = FileUtils.toFile(destPath + '/' + finfo.fileID);
			try {
				copy(src, dst, buffer);
				byte[] md5sum0 = finfo.md5 != null ? MD5Utils.toBytes(finfo.md5) : null;
				if (md5sum0 != null && digest != null) {
					byte[] md5sum = MD5Utils.md5sum(dst, digest, buffer);
                    if (!Arrays.equals(md5sum0, md5sum))
				    {
				    	String prompt = "md5 sum of copy " + dst
				    		+ " differs from md5 sum in DB for file " + src;
				    	log.warn(prompt);
				    	throw new IOException(prompt);
				    }
				}
				storage.storeFile(finfo.sopIUID, finfo.tsUID, destPath, 
                        finfo.fileID,
						(int) finfo.size, md5sum0, fileStatus);
				iter.remove();
			} catch (Exception e) {
				dst.delete();
				ex = e;
			}
		}
		if (ex != null)
			throw ex;
	}

	private void copy(File src, File dst, byte[] buffer) throws IOException {
		FileInputStream fis = new FileInputStream(src);
		try {
			File dir = dst.getParentFile();
            if (dir.mkdirs()) {
            	log.info("M-WRITE dir:" + dir);
            }
            log.info("M-WRITE file:" + dst);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(dst), buffer);
			try {
				bos.copyFrom(fis, (int) src.length());
			} finally {
				bos.close();
			}
		} catch (IOException e) {
			dst.delete();
			throw e;
		} finally {
			fis.close();
		}
	}

}
