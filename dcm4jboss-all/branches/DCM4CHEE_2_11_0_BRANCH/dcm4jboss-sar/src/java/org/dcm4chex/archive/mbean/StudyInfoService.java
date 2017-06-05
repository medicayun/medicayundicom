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

package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.ejb.jdbc.RetrieveStudyDatesCmd;
import org.dcm4chex.archive.ejb.jdbc.WadoQueryCmd;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision: 3340 $ $Date: 2007-05-13 00:00:31 +0800 (周日, 13 5月 2007) $
 * @since 17.02.2005
 */
public class StudyInfoService extends ServiceMBeanSupport {

	private static Logger log = Logger.getLogger(
            StudyInfoService.class.getName());

	DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private boolean noMatchForNoValue = true;

	public StudyInfoService() {
	}

	public final boolean isNoMatchForNoValue() {
        return noMatchForNoValue;
    }

    public final void setNoMatchForNoValue(boolean noMatchForNoValue) {
        this.noMatchForNoValue = noMatchForNoValue;
    }

    protected void startService() throws Exception {
	}

	protected void stopService() throws Exception {
	}

	private Dataset getQueryDS(String level, String uid) {
		Dataset dsQ = dof.newDataset();
		if ("STUDY".equals(level))
			dsQ.putUI(Tags.StudyInstanceUID, uid);
		else if ("SERIES".equals(level))
			dsQ.putUI(Tags.SeriesInstanceUID, uid);
		else if ("IMAGE".equals(level))
			dsQ.putUI(Tags.SOPInstanceUID, uid);
		else
			throw new IllegalArgumentException(
					"Argument level must be either STUDY,SERIES or IMAGE! level:"
							+ level);
		dsQ.putCS(Tags.QueryRetrieveLevel, level);
		return dsQ;
	}

	public boolean checkOutdated(Date date, String level, String uid) {
		Dataset dsQ = getQueryDS(level, uid);
		RetrieveStudyDatesCmd cmd = null;
		try {
			cmd = RetrieveStudyDatesCmd.create(dsQ);
			Date mrDate = cmd.getMostRecentUpdatedTime();
			return date.before(mrDate);
		} catch (SQLException x) {
			log.error("Error while RetrieveStudyDatesCmd!", x);
		} finally {
			if (cmd != null)
				cmd.close();
		}
		return true;
	}

	public Dataset retrieveStudyInfo(String level, Dataset dsQ)
			throws SQLException, IOException {

		WadoQueryCmd queryCmd = null;
		Dataset dsAll = dof.newDataset();

		// general attributes
		dsAll.putUI(Tags.SOPClassUID, UIDs.TianiStudyInfo);
		dsAll
				.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance()
						.createUID());

		try {
			queryCmd = WadoQueryCmd.create(dsQ, false, noMatchForNoValue );
			queryCmd.execute();
			HashMap data = new HashMap();
			while (queryCmd.next()) {
				//
				// Need to convert flat result set to hierachical hierarchical
				//				
				queryCmd.fillDatasetHierarchical(data);
			}

			// Convert to dataset

			// referenced patient sequence
			DcmElement psq = dsAll.putSQ(Tags.RefPatientSeq);
			for (Iterator pit = data.values().iterator(); pit.hasNext();) {
				WadoQueryCmd.KeyData pkd = (WadoQueryCmd.KeyData) pit.next();
				psq.addItem(pkd.ds);

				if (pkd.seq.size() == 0)
					continue;

				// referenced study sequence
				DcmElement ssq = pkd.ds.putSQ(Tags.RefStudySeq);
				for (Iterator sit = pkd.seq.values().iterator(); sit.hasNext();) {
					WadoQueryCmd.KeyData skd = (WadoQueryCmd.KeyData) sit
							.next();
					ssq.addItem(skd.ds);

					if (skd.seq.size() == 0)
						continue;

					// referenced series sequence
					DcmElement rsq = skd.ds.putSQ(Tags.RefSeriesSeq);
					for (Iterator rit = skd.seq.values().iterator(); rit
							.hasNext();) {
						WadoQueryCmd.KeyData rkd = (WadoQueryCmd.KeyData) rit
								.next();
						rsq.addItem(rkd.ds);

						if (rkd.seq.size() == 0)
							continue;

						// referenced image instance sequence
						DcmElement isq = rkd.ds.putSQ(Tags.RefInstanceSeq);
						for (Iterator iit = rkd.seq.values().iterator(); iit
								.hasNext();) {
							WadoQueryCmd.KeyData ikd = (WadoQueryCmd.KeyData) iit
									.next();
							isq.addItem(ikd.ds);

							if (ikd.seq.size() == 0)
								continue;

							// referenced image location sequence
							DcmElement lsq = ikd.ds.putSQ(Tags.RefImageSeq);
							for (Iterator lit = ikd.seq.values().iterator(); lit
									.hasNext();) {
								WadoQueryCmd.KeyData lkd = (WadoQueryCmd.KeyData) lit
										.next();
								lsq.addItem(lkd.ds);
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			queryCmd.close();
		}

		return dsAll;
	}
}
