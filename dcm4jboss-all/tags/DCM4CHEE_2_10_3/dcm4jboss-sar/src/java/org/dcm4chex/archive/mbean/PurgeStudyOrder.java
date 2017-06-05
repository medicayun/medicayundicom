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

package org.dcm4chex.archive.mbean;

import java.io.Serializable;

import org.dcm4chex.archive.common.BaseJmsOrder;

/**
 * JMS order for purging a study
 * 
 * @author fang.yang@agfa.com
 * @version $Revision: 2527 $ $Date: 2006-06-16 15:49:52 +0800 (周五, 16 6月 2006) $
 * @since Jun 1, 2006
 *
 */
public class PurgeStudyOrder extends BaseJmsOrder implements Serializable {
		
	private static final long serialVersionUID = -7002420976375597207L;

	private Long studyPk = null;
	private Long fsPk = null;
	private boolean deleteUncommited = false;
	
	public PurgeStudyOrder(Long studyPk, Long fsPk, boolean deleteUncommited) {
		this.studyPk = studyPk;
		this.fsPk = fsPk;
		this.deleteUncommited = deleteUncommited;
	}
		
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append("\tStudy PK: ").append(studyPk).append("\n");
		sb.append("\tFileSystem PK: ").append(fsPk).append("\n");
		sb.append("\tDelete uncommited: ").append(deleteUncommited).append("\n");
		return sb.toString();
	}

	public boolean isDeleteUncommited() {
		return deleteUncommited;
	}

	public void setDeleteUncommited(boolean deleteUncommited) {
		this.deleteUncommited = deleteUncommited;
	}

	public Long getFsPk() {
		return fsPk;
	}

	public void setFsPk(Long fsPk) {
		this.fsPk = fsPk;
	}

	public Long getStudyPk() {
		return studyPk;
	}

	public void setStudyPk(Long studyPk) {
		this.studyPk = studyPk;
	}

}
