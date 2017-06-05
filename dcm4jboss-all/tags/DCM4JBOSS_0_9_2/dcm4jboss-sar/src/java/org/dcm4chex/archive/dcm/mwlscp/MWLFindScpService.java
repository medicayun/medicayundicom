/* $Id: MWLFindScpService.java 1119 2004-05-21 09:50:27Z gunterze $
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

package org.dcm4chex.archive.dcm.mwlscp;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1119 $ $Date: 2004-05-21 17:50:27 +0800 (周五, 21 5月 2004) $
 * @since 31.01.2004
 */
public class MWLFindScpService extends AbstractScpService {

    private static final String[] AS_UIDS = { UIDs.ModalityWorklistInformationModelFIND};

    private MWLFindScp mwlFindScp = new MWLFindScp(this);

    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.ModalityWorklistInformationModelFIND, mwlFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.ModalityWorklistInformationModelFIND);
    }

    protected void initPresContexts(AcceptorPolicy policy) {
        addPresContexts(policy, AS_UIDS, getTransferSyntaxUIDs());
    }
}
