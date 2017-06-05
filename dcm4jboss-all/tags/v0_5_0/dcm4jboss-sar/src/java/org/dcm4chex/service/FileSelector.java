/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import java.util.Arrays;
import java.util.List;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.Association;
import org.dcm4che.net.PresContext;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 901 $
 * @since 21.11.2003
 */
final class FileSelector
{
    private final Association as;

    public FileSelector(Association as)
    {
        this.as = as;
    }

    public FileSelection select(FileInfo[] infos, String retrieveAET)
    {
        FileSelection retval = null;
        List pcList = as.listAcceptedPresContext(infos[0].sopCUID);
        int score = 0;
		for (int fileIndex = 0; fileIndex < infos.length; ++fileIndex)
        {
			FileInfo fileInfo = infos[fileIndex];
			String[] fileRetrieveAETs = StringUtils.split(fileInfo.retrieveAETs, '\\');
			if (Arrays.asList(fileRetrieveAETs).indexOf(retrieveAET) != -1) {
				for (int pcIndex = 0, n = pcList.size(); pcIndex < n; ++pcIndex)
	            {
					PresContext pc = (PresContext) pcList.get(pcIndex);
	                int tmp = score(fileInfo.tsUID, pc.getTransferSyntaxUID());
	                if (tmp > score)
	                {
	                    retval = new FileSelection(fileInfo, pc);
	                    score = tmp;
	                }
	            }
			}
        }
        return retval;
    }

    private static int score(String tsFile, String tsTransfer)
    {
        if (tsFile.equals(UIDs.ImplicitVRLittleEndian))
        {
            if (tsTransfer.equals(UIDs.ImplicitVRLittleEndian))
            {
                return 49;
            } else if (tsTransfer.equals(UIDs.ExplicitVRLittleEndian))
            {
                return 48;
            } else
            {
                throw new UnsupportedOperationException("tsTransfer:" + tsFile);
            }
        } else if (tsFile.equals(UIDs.ExplicitVRLittleEndian))
        {
            if (tsTransfer.equals(UIDs.ImplicitVRLittleEndian))
            {
                return 47;
            } else if (tsTransfer.equals(UIDs.ExplicitVRLittleEndian))
            {
                return 50;
            } else
            {
                throw new UnsupportedOperationException("tsTransfer:" + tsFile);
            }
        } else
        {
            throw new UnsupportedOperationException("tsFile:" + tsFile);
        }
    }

    private static boolean isNativeTS(String ts)
    {
        return ts.equals(UIDs.ImplicitVRLittleEndian)
            || ts.equals(UIDs.ExplicitVRLittleEndian);
    }
}
