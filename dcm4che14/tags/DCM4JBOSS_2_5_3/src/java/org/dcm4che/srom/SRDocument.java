/*$Id: SRDocument.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2001 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

package org.dcm4che.srom;

/** 
 * The <code>SRDocument</code> interface represents the entire <i>DICOM SR 
 * document</i>. Conceptually, it is the root of the document tree, and provides 
 * the primary access to the document's data.
 * <p>Since <i>Content Items</i> cannot exist outside the context of a
 * <code>SRDocument</code>, the <code>SRDocument</code> interface also contains
 * the factory methods needed to create the different types of {@link Content
 * Content} objects. The <code>Content</code> objects created have a 
 * <code>ownerDocument</code> attribute which associates them with the 
 * <code>SRDocument</code> within whose context they were created.
 * <p>Additionally to the content tree, a <code>SRDocument</code> contents
 * <i>Patient</i>, <i>Study</i>, <i>Series</i>, <i>Equipment</i>,
 * <i>SR Document General</i> and <i>SOP Common</i> related data.
 * <p>Factory method: {@link SRDocumentFactory#newSRDocument}.
 *
 * @author gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see SRDocumentFactory#newSRDocument
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex A.35 Structured Report Document Information Object Definitions"
 */
public interface SRDocument extends KeyObject {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /** 
     * Returns whether the content of this document is complete or partial.
     *
     * @return <code>true</code> if the content is complete, 
     *   <code>false</code> otherwise.
     */ 
    public boolean isComplete();
    
    /** 
     * Sets whether this document is complete.
     *
     * @param complete a <code>boolean</code> indicating whether the content 
     *   of this document is complete.
     */ 
    public void setComplete(boolean complete);

    /** 
     * Returns <i>Completion Flag Description</i>.
     *
     * @return <i>Completion Flag Description</i>.
     */ 
    public String getCompletionFlagDescription();
    
    /** 
     * Sets <i>Completion Flag Description</i>.
     *
     * @param desc new <i>Completion Flag Description</i>.
     */ 
    public void setCompletionFlagDescription(String desc);

    /** 
     * Returns whether this document is verified.
     *
     * @return <code>true</code> if this document is verified, 
     *   <code>false</code> otherwise.
     */ 
    public boolean isVerified();
    
    /** 
     * Sets whether this document is verified.
     *
     * @param verified a <code>boolean</code> indicating whether this document 
     *   is verified.
     */ 
    public void setVerified(boolean verified);
    
    /** 
     * Returns array of <code>Verification</code> objects, specifing all
     * <i>Verifying Observer</i> of this document.
     *
     * @return list of <code>Verification</code> objects. 
     */
    public Verification[] getVerifications();

    /** 
     * Sets array of <code>Verification</code> objects, specifing all
     * <i>Verifying Observer</i> of this document.
     *
     * @param vers new array of <code>Verification</code> objects. 
     *
     * @see SRDocumentFactory#newVerification
     */
    public void setVerifications(Verification[] vers);    
    
    /** 
     * Adds a new <code>Verification</code> object, specifing a
     * <i>Verifying Observer</i> of this document. Ignores the new
     * <code>Verification</code> object, if the document already
     * contains an equal <code>Verification</code> object.
     *
     * @param ver specifies one <i>Verifying Observer</i>.
     * @return <code>true</code> if <code>ver</code> was added to this document,
     *   <code>false</code> if the document already contains <code>ver</code>.
     */
    public boolean addVerification(Verification ver);
    
    /** 
     * Removes the specified <code>Verification</code> object, from the list of
     * <i>Verifying Observers</i> of this document.
     *
     * @return <code>true</code> if the <i>Verifying Observer</i> list of this
     *   document contained <code>ver</code>; <code>false</code> otherwise.
     */
    public boolean removeVerification(Verification ver);

    /** 
     * Returns array of <code>SOPInstanceRef</code> objects, specifing all
     * <i>Predecessor Documents</i> of this document.
     *
     * @return references to <i>Predecessor Documents</i>. 
     */
    public SOPInstanceRef[] getPredecessorDocuments();

    /** 
     * Sets array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Predecessor Documents</i> of this document.
     *
     * @param refs new <i>Predecessor Documents</i> references. 
     */
    public void setPredecessorDocuments(SOPInstanceRef[] refs);
    
    /** 
     * Adds a new <code>SOPInstanceRef</code> object, specifing a
     * <i>Predecessor Document</i> of this document. Ignores the new
     * <code>SOPInstanceRef</code> object, if the document already
     * refers an equal <i>Predecessor Document</i>.
     *
     * @param ref reference to <i>Predecessor Document</i>.
     * @return <code>true</code> if <code>ref</code> was added,
     *   <code>false</code> if the document already contains the reference.
     *
     * @see SRDocumentFactory#newSOPInstanceRef
     */
    public boolean addPredecessorDocument(SOPInstanceRef ref);
    
    /** 
     * Removes the specified <code>SOPInstanceRef</code> object, from the list
     * of references of <i>Predecessor Document</i> of this document.
     *
     * @return <code>true</code> if the document contained the reference,
     *   <code>false</code> otherwise.
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     *
     * @see SRDocumentFactory#newSOPInstanceRef
     */
    public boolean removePredecessorDocument(RefSOP ref);
    
    /**
     * Returns array of <code>Code</code> objects, identifing all
     * <i>Performed Procedures</i> pertaining to this document.
     *
     * @return <i>Performed Procedures</i> codes. 
     * @throws NullPointerException if <code>request</code> is
     *   <code>null</code>.
     */
    public Code[] getProcedureCodes();
    
    /**
     * Sets array of <code>Code</code> objects, identifing all
     * <i>Performed Procedures</i> pertaining to this document.
     *
     * @param codes new <i>Performed Procedures</i> codes. 
     *
     * @see SRDocumentFactory#newCode
     */
    public void setProcedureCodes(Code[] codes);

    /**
     * Adds a new <code>Code</code> object, identifing a
     * <i>Performed Procedure</i> pertaining to this document. Ignores the new
     * <code>Code</code> object, if the document already refers the
     * <i>Performed Procedure</i>.
     *
     * @param code identifies one <i>Performed Procedure</i>.
     * @return <code>true</code> if <code>code</code> was added,
     *   <code>false</code> if the document already contained <code>code</code>.
     * @throws NullPointerException if <code>code</code> is <code>null</code>.
     *
     * @see SRDocumentFactory#newCode
     */
    public boolean addProcedureCode(Code code);
    
    /**
     * Removes the specified <code>Code</code> object, from the list
     * of <i>PPerformed Procedure</i> pertaining to this document.
     *
     * @return <code>true</code> if the <i>Performed Procedure</i> list of this
     *   document contained <code>code</code>; <code>false</code> otherwise.
     */
    public boolean removeProcedureCode(Code code);

    /**
     * Returns array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Pertinent Other Evidences</i> of this document.
     *
     * @return references to <i>Pertinent Other Evidences</i>. 
     */
    public SOPInstanceRef[] getOtherEvidence();
    
    /**
     * Returns <code>SOPInstanceRef</code> objects, refering the specified
     * <i>Pertinent Other Evidence</i>.
     *
     * @return reference to <i>Pertinent Other Evidences</i>,
     *   <code>null</code> if no reference to <code>refSOP</code> was found.
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     */
    public SOPInstanceRef findOtherEvidence(RefSOP refSOP);

    /**
     * Sets array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Pertinent Other Evidences</i> of this document.
     *
     * @param refs new <i>Pertinent Other Evidence</i> references. 
     *
     * @see SRDocumentFactory#newSOPInstanceRef
     */
    public void setOtherEvidence(SOPInstanceRef[] refs);
    
    /**
     * Adds a new <code>SOPInstanceRef</code> object, refering a
     * <i>Pertinent Other Evidence</i> of this document. Ignores the new
     * <code>SOPInstanceRef</code> object, if the document already
     * refers an equal <i>Pertinent Other Evidence</i>.
     *
     * @param ref reference to <i>Pertinent Other Evidence</i>.
     * @return <code>true</code> if <code>ref</code> was added,
     *   <code>false</code> if the document already contained the reference.
     *
     * @see SRDocumentFactory#newSOPInstanceRef
     */
    public boolean addOtherEvidence(SOPInstanceRef ref);
    
    /**
     * Removes the specified <code>SOPInstanceRef</code> object, from the list
     * of <i>Pertinent Other Evidences</i> of this document.
     *
     * @return <code>true</code> if the document contained the reference,
     *   <code>false</code> otherwise.
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     */
    public boolean removeOtherEvidence(RefSOP ref);

}//end interface SRDocument
