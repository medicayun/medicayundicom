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

package org.dcm4chex.archive.ejb.entity;

import java.sql.Timestamp;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3286 $ $Date: 2007-04-19 01:38:05 +0800 (周四, 19 4月 2007) $
 * @since 01.01.2005
 * 
 * @ejb.bean name="StudyOnFileSystem"
 *          type="CMP"
 *          view-type="local"
 *          local-jndi-name="ejb/StudyOnFileSystem"
 *          primkey-field="pk"
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="study_on_fs"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal findByStudyAndFileSystem(java.lang.String suid, java.lang.String dirPath)"
 * 	           query="" transaction-type="Supports"
 * @jboss.query signature="org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal findByStudyAndFileSystem(java.lang.String suid, java.lang.String dirPath)"
 *             query="SELECT OBJECT(sof) FROM StudyOnFileSystem sof WHERE sof.study.studyIuid=?1 AND sof.fileSystem.directoryPath=?2"
 *             strategy="on-find" eager-load-group="*"
 * @ejb.finder signature="java.util.Collection findByRetrieveAETAndAccessBefore(java.lang.String aet, java.sql.Timestamp tsBefore)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByRetrieveAETAndAccessBefore(java.lang.String aet, java.sql.Timestamp tsBefore)"
 *             query="SELECT DISTINCT OBJECT(sof) FROM StudyOnFileSystem sof, IN (sof.study.series) s WHERE sof.fileSystem.retrieveAET = ?1 AND sof.accessTime < ?2 AND sof.fileSystem.status >= 0 AND s.seriesStatus = 0 ORDER BY sof.accessTime ASC"
 *             strategy="on-find" eager-load-group="*"
 * @ejb.finder signature="java.util.Collection findByRetrieveAETAndAccessAfter(java.lang.String aet, java.sql.Timestamp tsAfter, int limit )"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByRetrieveAETAndAccessAfter(java.lang.String aet, java.sql.Timestamp tsAfter, int limit )"
 *             query="SELECT DISTINCT OBJECT(sof) FROM StudyOnFileSystem sof, IN (sof.study.series) s WHERE sof.fileSystem.retrieveAET = ?1 AND sof.fileSystem.status >= 0 AND sof.accessTime > ?2 AND s.seriesStatus = 0 ORDER BY sof.accessTime ASC LIMIT ?3"
 *             strategy="on-find" eager-load-group="*"
 */
public abstract class StudyOnFileSystemBean implements EntityBean {

    private static final Logger log = Logger
            .getLogger(StudyOnFileSystemBean.class);

    /**
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Long getPk();

    public abstract void setPk(Long pk);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="access_time"
     */
    public abstract java.sql.Timestamp getAccessTime();

    public abstract void setAccessTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.relation name="study-sof"
     *               role-name="sof-of-study"
     *               cascade-delete="yes"
     *               target-ejb="Study"
     *               target-role-name="study-of-sof"
     *               target-multiple="yes" 
     * @jboss.relation fk-column="study_fk" related-pk-field="pk"
     */
    public abstract StudyLocal getStudy();

    public abstract void setStudy(StudyLocal study);

    /**
     * @ejb.interface-method
     * @ejb.relation name="filesystem-sof"
     *               role-name="sof-of-filesystem"
     *               cascade-delete="yes" 
     * 	             target-ejb="FileSystem"
     *               target-role-name="filesystem-of-sof"
     *               target-multiple="yes"
     * @jboss.relation fk-column="filesystem_fk" 
     *                 related-pk-field="pk"
     */
    public abstract FileSystemLocal getFileSystem();

    public abstract void setFileSystem(FileSystemLocal fs);

    /**
     * @ejb.create-method
     */
    public Long ejbCreate(StudyLocal study, FileSystemLocal fs)
            throws CreateException {
        setAccessTime(new Timestamp(System.currentTimeMillis()));
        return null;
    }

    public void ejbPostCreate(StudyLocal study, FileSystemLocal fs)
            throws CreateException {
        setStudy(study);
        setFileSystem(fs);
    }

    /**
     * @ejb.interface-method
     */
    public void touch() {
        setAccessTime(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * @ejb.interface-method
     */
    public String asString() {
        StudyLocal study = getStudy();
        FileSystemLocal fs = getFileSystem();
        return "StudyOnFileSystem[" 
        	+ (study == null ? "null" : study.asString())
        	+ "@"
        	+ (fs == null ? "null" : fs.asString())
        	+ "]"; 
    }

    /**
     * @ejb.select query="SELECT OBJECT(f) FROM File f WHERE f.instance.series.study.pk = ?1 AND f.fileSystem.pk = ?2"
     *             transaction-type="Supports"
     */
    public abstract Collection ejbSelectFiles(java.lang.Long study_fk, java.lang.Long filesystem_fk)
            throws FinderException;

    /**    
     * @ejb.interface-method
     */
    public Collection getFiles() throws FinderException {    	
        return ejbSelectFiles(getStudy().getPk(), getFileSystem().getPk());
    }
}
