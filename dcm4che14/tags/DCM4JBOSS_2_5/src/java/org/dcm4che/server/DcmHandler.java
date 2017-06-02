/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4che.server;

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceRegistry;

/**
 * @author     <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @since      June, 2002
 * @version    $Revision: 3646 $ $Date: 2003-05-10 06:12:03 +0800 (周六, 10 5月 2003) $
 */
public interface DcmHandler extends Server.Handler
{
    /**
     *  Adds a feature to the AssociationListener attribute of the DcmHandler
     *  object
     *
     * @param  l  The feature to be added to the AssociationListener attribute
     */
    void addAssociationListener(AssociationListener l);


    /**
     *  Description of the Method
     *
     * @param  l  Description of the Parameter
     */
    void removeAssociationListener(AssociationListener l);


    /**
     *  Sets the acceptorPolicy attribute of the DcmHandler object
     *
     * @param  policy  The new acceptorPolicy value
     */
    void setAcceptorPolicy(AcceptorPolicy policy);


    /**
     *  Gets the acceptorPolicy attribute of the DcmHandler object
     *
     * @return    The acceptorPolicy value
     */
    AcceptorPolicy getAcceptorPolicy();


    /**
     *  Sets the dcmServiceRegistry attribute of the DcmHandler object
     *
     * @param  services  The new dcmServiceRegistry value
     */
    void setDcmServiceRegistry(DcmServiceRegistry services);


    /**
     *  Gets the dcmServiceRegistry attribute of the DcmHandler object
     *
     * @return    The dcmServiceRegistry value
     */
    DcmServiceRegistry getDcmServiceRegistry();


    /**
     *  Getter for property rqTimeout.
     *
     * @return    Value of property rqTimeout.
     */
    int getRqTimeout();


    /**
     *  Setter for property rqTimeout.
     *
     * @param  timeout  The new rqTimeout value
     */
    void setRqTimeout(int timeout);


    /**
     *  Getter for property dimseTimeout.
     *
     * @return    Value of property dimseTimeout.
     */
    int getDimseTimeout();


    /**
     *  Setter for property dimseTimeout.
     *
     * @param  dimseTimeout  New value of property dimseTimeout.
     */
    void setDimseTimeout(int dimseTimeout);


    /**
     *  Getter for property soCloseDelay.
     *
     * @return    Value of property soCloseDelay.
     */
    int getSoCloseDelay();


    /**
     *  Setter for property soCloseDelay.
     *
     * @param  soCloseDelay  New value of property soCloseDelay.
     */
    void setSoCloseDelay(int soCloseDelay);


    /**
     *  Getter for property packPDVs.
     *
     * @return    Value of property packPDVs.
     */
    boolean isPackPDVs();


    /**
     *  Setter for property packPDVs.
     *
     * @param  packPDVs  New value of property packPDVs.
     */
    void setPackPDVs(boolean packPDVs);
}

