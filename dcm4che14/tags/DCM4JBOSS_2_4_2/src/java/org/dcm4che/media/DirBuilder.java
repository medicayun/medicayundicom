/*****************************************************************************
 *                                                                           *
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
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.media;

import org.dcm4che.data.Dataset;

import java.io.File;
import java.io.IOException;

/** Builder facade for {@link DirWriter} to generate and insert Directory
 * Records refering DICOM files in the DICOM Directory (= DICOMDIR file).
 * <p>Can only be used for DICOM Directory with scheme
 * <pre>
 * PATIENT
 *  STUDY
 *    SERIES
 *      IMAGE|PRESENTATION|SR DOCUMENT|KEY OBJECT DOC|...
 * </pre>
 * <p>{@link DirRecord} objects will be generated according associated
 * {@link DirBuilderPref}, specified in factory method.
 * {@link DirBuilderFactory#newDirBuilder}
 * <p><code>DirBuilder</code> also take care, that there will be only
 * <ul>
 * <li> one <code>PATIENT</code> record with the same value of
 * <code>Patient ID (0010,0020)</code>,
 * <li> one <code>STUDY</code> record with the same value of
 * <code>Study Instance UID (0020,000D)</code> with the same parent
 * <code>PATIENT</code> record,
 * <li> one <code>SERIES</code> record with the same value of
 * <code>Series Instance UID (0020,000E)</code> with the same parent
 * <code>STUDY</code> record,
 * </ul>
 * in the associated DICOM Directory.
 *
 * @see DirBuilderFactory#newDirBuilder
 * @see DirBuilderPref
 * @see DirWriter
 * @see DirRecord
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3505 $ $Date: 2002-07-19 22:59:46 +0800 (周五, 19 7月 2002) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2002/07/16 gunter zeilinger:</b>
 * <ul>
 * <li> add javadoc comments
 * </ul>
 */
public interface DirBuilder {
   
   /** Add (up to 4) {@link DirRecord} objects refering the DICOM object
    * in the specified <code>file</code> to the associated DICOM Directory.
    * <p>The function may internally invoke {@link DirWriter#commit} of the
    * associated <code>DirWriter</codeS. Therefore, the operation cannot be
    * undo by {@link DirWriter#rollback}!
    *
    * @param file DICOM file
    * @throws IOException if an IO error occurs, writing the record into the
    *                     DICOM Directory.
    * @return number of added {@link DirRecord} objects.
    */   
   int addFileRef(File file) throws IOException;
   
   /** Add (up to 4) {@link DirRecord} objects refering the DICOM object
    * with a specified File IDs to the associated DICOM Directory.
    * <p>The function may internally invoke {@link DirWriter#commit} of the
    * associated <code>DirWriter</code>. Therefore, the operation cannot be
    * undo by {@link DirWriter#rollback}!
    *
    * @param fileIDs File ID components
    * @param ds DICOM Data Set
    * @throws IOException if an IO error occurs, writing the record into the
    *                     DICOM Directory.
    * @return number of added {@link DirRecord} objects.
    */   
   int addFileRef(String[] fileIDs, Dataset ds) throws IOException;
   
   /** Close the DICOM Dictionary (= DICOMDIR file).
    * @throws IOException  If an I/O error occurs */   
   void close() throws IOException;
   
   /** Get underlying {@link DirWriter} object
    * @return underlying {@link DirWriter} object.
    */   
   DirWriter getDirWriter();       
}

