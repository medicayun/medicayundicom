/*                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4cheri.server;

import org.dcm4che.server.DcmHandler;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.AssociationFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *@author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 *@created    June, 2002
 *@version    $Revision: 3656 $ $Date: 2003-06-10 01:17:51 +0800 (周二, 10 6月 2003) $
 */
class DcmHandlerImpl implements DcmHandler
{

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final static AssociationFactory fact =
            AssociationFactory.getInstance();

    private final LinkedList listeners = new LinkedList();
    private AcceptorPolicy policy;
    private DcmServiceRegistry services;

    private int rqTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
    private boolean packPDVs = false;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the DcmHandlerImpl object
     *
     *@param  policy    Description of the Parameter
     *@param  services  Description of the Parameter
     */
    public DcmHandlerImpl(AcceptorPolicy policy, DcmServiceRegistry services)
    {
        setAcceptorPolicy(policy);
        setDcmServiceRegistry(services);
    }

    // Public --------------------------------------------------------
    /**
     *  Sets the acceptorPolicy attribute of the DcmHandlerImpl object
     *
     *@param  policy  The new acceptorPolicy value
     */
    public final void setAcceptorPolicy(AcceptorPolicy policy)
    {
        if (policy == null) {
            throw new NullPointerException();
        }
        this.policy = policy;
    }


    /**
     *  Gets the acceptorPolicy attribute of the DcmHandlerImpl object
     *
     *@return    The acceptorPolicy value
     */
    public final AcceptorPolicy getAcceptorPolicy()
    {
        return policy;
    }


    /**
     *  Sets the dcmServiceRegistry attribute of the DcmHandlerImpl object
     *
     *@param  services  The new dcmServiceRegistry value
     */
    public final void setDcmServiceRegistry(DcmServiceRegistry services)
    {
        if (services == null) {
            throw new NullPointerException();
        }
        this.services = services;
    }


    /**
     *  Gets the dcmServiceRegistry attribute of the DcmHandlerImpl object
     *
     *@return    The dcmServiceRegistry value
     */
    public final DcmServiceRegistry getDcmServiceRegistry()
    {
        return services;
    }


    /**
     *  Sets the rqTimeout attribute of the DcmHandlerImpl object
     *
     *@param  timeout  The new rqTimeout value
     */
    public void setRqTimeout(int timeout)
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout: " + timeout);
        }
        this.rqTimeout = timeout;
    }


    /**
     *  Gets the rqTimeout attribute of the DcmHandlerImpl object
     *
     *@return    The rqTimeout value
     */
    public int getRqTimeout()
    {
        return rqTimeout;
    }


    /**
     *  Gets the dimseTimeout attribute of the DcmHandlerImpl object
     *
     *@return    The dimseTimeout value
     */
    public int getDimseTimeout()
    {
        return dimseTimeout;
    }


    /**
     *  Sets the dimseTimeout attribute of the DcmHandlerImpl object
     *
     *@param  timeout  The new dimseTimeout value
     */
    public void setDimseTimeout(int dimseTimeout)
    {
        if (dimseTimeout < 0) {
            throw new IllegalArgumentException("timeout: " + dimseTimeout);
        }
        this.dimseTimeout = dimseTimeout;
    }


    /**
     *  Gets the soCloseDelay attribute of the DcmHandlerImpl object
     *
     *@return    The soCloseDelay value
     */
    public int getSoCloseDelay()
    {
        return soCloseDelay;
    }


    /**
     *  Sets the soCloseDelay attribute of the DcmHandlerImpl object
     *
     *@param  delay  The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay)
    {
        if (delay < 0) {
            throw new IllegalArgumentException("delay: " + delay);
        }
        this.soCloseDelay = delay;
    }

    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    public boolean isPackPDVs() {
        return packPDVs;
    }
    
    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    public void setPackPDVs(boolean packPDVs) {
        this.packPDVs = packPDVs;
    }
    // DcmHandler implementation -------------------------------------
    /**
     *  Description of the Method
     *
     *@param  s                Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void handle(Socket s)
        throws IOException
    {
        Association assoc = fact.newAcceptor(s);
        assoc.setRqTimeout(rqTimeout);
        assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
        assoc.setPackPDVs(packPDVs);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            assoc.addAssociationListener((AssociationListener) it.next());
        }
        if (assoc.accept(policy) instanceof AAssociateAC) {
            fact.newActiveAssociation(assoc, services).run();
        }
    }


    /**
     *  Adds a feature to the AssociationListener attribute of the
     *  DcmHandlerImpl object
     *
     *@param  l  The feature to be added to the AssociationListener attribute
     */
    public void addAssociationListener(AssociationListener l)
    {
        synchronized (listeners) {
            listeners.add(l);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  l  Description of the Parameter
     */
    public void removeAssociationListener(AssociationListener l)
    {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }


    /**
     *  Gets the sockedClosedByHandler attribute of the DcmHandlerImpl object
     *
     *@return    The sockedClosedByHandler value
     */
    public boolean isSockedClosedByHandler()
    {
        return true;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

