/*$Id: ReferencedContentImpl.java 3581 2002-10-07 13:58:30Z gunterze $/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.*;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class ReferencedContentImpl extends ContentImpl
        implements org.dcm4che.srom.ReferencedContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected Content refContent;
    protected int[] refContentID;

    // Constructors --------------------------------------------------
    ReferencedContentImpl(KeyObject owner, Content refContent) {
        super(owner);
        if (refContent.getOwnerDocument() != owner)
            throw new IllegalArgumentException();

        if (refContent.getParent() == null)
            throw new IllegalArgumentException();
        
        if (refContent instanceof ReferencedContent)
            throw new IllegalArgumentException();
        
        this.refContent = refContent;
    }
    
    ReferencedContentImpl(KeyObject owner, int[] refContentID) {
        super(owner);
        this.refContentID = refContentID;
//        this.refContent = owner.getContent(this.refContentID = refContentID);
    }

    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        if (newOwner != owner)
            throw new IllegalArgumentException("" + newOwner);
        
        return new ReferencedContentImpl(newOwner, getRefContent().getID());
    }

    // Methodes ------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(refContent != null ? "=>" : "->");
        appendID(sb, refContent != null ? refContent.getID() : refContentID);
        return sb.toString();
    }
    
    public void toDataset(Dataset ds) {
        ds.putCS(Tags.RelationshipType, relation.toString());
        ds.putUL(Tags.RefContentItemIdentifier, getRefContent().getID());
    }    
    
    public Content getRefContent() {
        if (refContent != null)
            return refContent;

        refContent = owner.getContent(refContentID);
        if (refContent == null)
            throw new NoSuchElementException(ContentImpl.promptID(refContentID));

        return refContent;
    }

    public Content insertBefore(RelationType rel, Content newnode, Content refnode) {
        throw new UnsupportedOperationException();
    }
    
    public ValueType getValueType() {
        return getRefContent().getValueType();
    }
/*    
    public Content getFirstChild() {
        return getRefContent().getFirstChild();
    }

    public Content getLastChild() {
        return getRefContent().getLastChild();
    }
  */  
    public Date getObservationDateTime(boolean inherit) {
        return getRefContent().getObservationDateTime(inherit);
    }

    public Code getName() {
        return getRefContent().getName();
    }

    public void setName(Code newName) {
        throw new UnsupportedOperationException();
    }

    public Template getTemplate() {
        return getRefContent().getTemplate();
    }
}
