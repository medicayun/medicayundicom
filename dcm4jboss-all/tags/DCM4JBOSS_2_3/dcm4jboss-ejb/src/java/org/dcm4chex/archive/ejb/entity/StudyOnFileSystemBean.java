/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1491 $ $Date: 2005-02-07 08:40:46 +0800 (周一, 07 2月 2005) $
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
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal findByStudyAndFileSystem(java.lang.String suid, java.lang.String dirPath)"
 * 	           query="SELECT OBJECT(sof) FROM StudyOnFileSystem sof WHERE sof.study.studyIuid=?1 AND sof.fileSystem.directoryPath=?2"
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection ejbSelectGeneric(java.lang.String jbossQl, java.lang.Object[] args)"
 *              dynamic="true"
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
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

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
    public Integer ejbCreate(StudyLocal study, FileSystemLocal fs)
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
     * @ejb.home-method
     */
    public java.util.Collection ejbHomeListOnFileSystems(Set dirPaths, Timestamp tsBefore )
            throws FinderException {
        if (dirPaths.isEmpty())
            return Collections.EMPTY_LIST;
        StringBuffer jbossQl = new StringBuffer(
                "SELECT OBJECT(s) FROM StudyOnFileSystem s WHERE s.fileSystem.directoryPath");
        Iterator iter = dirPaths.iterator();
        String dirPath1 = (String) iter.next();
        Object[] args = null;
        if (!iter.hasNext()) {
            jbossQl.append(" = '").append(dirPath1).append("'");
        } else {
            jbossQl.append(" IN ('").append(dirPath1);
            while (iter.hasNext()) {
                jbossQl.append("', '").append( iter.next() );
            }
            jbossQl.append("')");
        }
        if ( tsBefore != null ) {
        	jbossQl.append(" AND s.accessTime < ?1");
            args = new Object[]{ tsBefore };
        }
        jbossQl.append(" ORDER BY s.accessTime ASC");
        if (log.isDebugEnabled())
            log.debug("Execute JBossQL: " + jbossQl);
        // call dynamic-ql query
        return ejbSelectGeneric(jbossQl.toString(), args);
    }

    /**
     * @ejb.select query=""
     *             transaction-type="Supports"
     */
    public abstract Collection ejbSelectGeneric(String jbossQl, Object[] args)
            throws FinderException;
    
    /**
     * @ejb.select query="SELECT OBJECT(f) FROM StudyOnFileSystem sof, File f WHERE sof.pk = ?1 AND f.fileSystem = sof.fileSystem AND f.instance.series.study = sof.study"
     *             transaction-type="Supports"
     */
    public abstract Collection ejbSelectFiles(java.lang.Integer pk)
            throws FinderException;

    /**    
     * @ejb.interface-method
     */
    public Collection getFiles() throws FinderException {
        return ejbSelectFiles(getPk());
    }
}
