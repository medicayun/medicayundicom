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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.web.dao.tc;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.QueryParam;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.jboss.annotation.ejb.LocalBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 05, 2011
 */
@Stateless
@LocalBinding(jndiBinding = TCQueryLocal.JNDI_NAME)
public class TCQueryBean implements TCQueryLocal {

    private static final Logger log = LoggerFactory
            .getLogger(TCQueryBean.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public int countMatchingInstances(TCQueryFilter filter, List<String> roles,
            List<String> restrictedSourceAETs) {
        if (roles != null && roles.isEmpty()) {
            return 0;
        }

        boolean doStudyPermissionCheck = roles != null;
        boolean doSourceAETCheck = restrictedSourceAETs != null
                && !restrictedSourceAETs.isEmpty();

        StringBuilder sb = new StringBuilder(64);
        sb.append(" Select COUNT(*) FROM Instance instance");

        if (doStudyPermissionCheck || doSourceAETCheck) {
            sb.append(" LEFT JOIN FETCH instance.series series");

            if (doStudyPermissionCheck) {
                sb.append(" LEFT JOIN FETCH series.study s");
            }
        }

        sb.append(" LEFT JOIN FETCH instance.conceptNameCode sr_code");
        sb.append(" WHERE (instance.sopClassUID = '1.2.840.10008.5.1.4.1.1.88.11')");
        sb.append(" AND (sr_code.codeValue = 'TCE006')");
        sb.append(" AND (sr_code.codingSchemeDesignator = 'IHERADTF')");

        if (doSourceAETCheck) {
            sb.append(" AND (series.sourceAET IN (");
            for (int i = 0; i < restrictedSourceAETs.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }

                sb.append("'").append(restrictedSourceAETs.get(i)).append("'");
            }
            sb.append("))");
        }

        Set<Entry<TCQueryFilterKey, TCQueryFilterValue<?>>> entries = filter
                .getEntries();
        Set<QueryParam[]> paramSets = null;

        if (entries != null) {
            for (Entry<TCQueryFilterKey, TCQueryFilterValue<?>> e : entries) {
                sb.append(" AND (");

                QueryParam[] params = e.getValue().appendSQLWhereConstraint(
                        e.getKey(), sb);
                if (params != null) {
                    if (paramSets == null) {
                        paramSets = new HashSet<QueryParam[]>();
                    }

                    paramSets.add(params);
                }

                sb.append(")");
            }
        }

        if (doStudyPermissionCheck) {
            QueryUtil.appendDicomSecurityFilter(sb);
        }

        Query query = em.createQuery(sb.toString());

        if (doStudyPermissionCheck) {
            query.setParameter("roles", roles);
        }

        if (paramSets != null) {
            for (QueryParam[] paramSet : paramSets) {
                if (paramSet != null) {
                    for (QueryParam param : paramSet) {
                        query.setParameter(param.getKey(), param.getValue());
                    }
                }
            }
        }

        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Instance> findMatchingInstances(TCQueryFilter filter,
            List<String> roles, List<String> restrictedSourceAETs) {
        if (roles != null && roles.isEmpty()) {
            return Collections.emptyList();
        }

        boolean doStudyPermissionCheck = roles != null;
        boolean doSourceAETCheck = restrictedSourceAETs != null
                && !restrictedSourceAETs.isEmpty();

        StringBuilder sb = new StringBuilder(64);
        sb.append(" FROM Instance instance");

        if (doStudyPermissionCheck || doSourceAETCheck) {
            sb.append(" LEFT JOIN FETCH instance.series series");

            if (doStudyPermissionCheck) {
                sb.append(" LEFT JOIN FETCH series.study s");
            }
        }

        sb.append(" LEFT JOIN FETCH instance.conceptNameCode sr_code");
        sb.append(" LEFT JOIN FETCH instance.media");
        sb.append(" WHERE (instance.sopClassUID = '1.2.840.10008.5.1.4.1.1.88.11')");
        sb.append(" AND (sr_code.codeValue = 'TCE006')");
        sb.append(" AND (sr_code.codingSchemeDesignator = 'IHERADTF')");

        if (doSourceAETCheck) {
            sb.append(" AND (series.sourceAET IN (");
            for (int i = 0; i < restrictedSourceAETs.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }

                sb.append("'").append(restrictedSourceAETs.get(i)).append("'");
            }
            sb.append("))");
        }

        Set<Entry<TCQueryFilterKey, TCQueryFilterValue<?>>> entries = filter
                .getEntries();
        Set<QueryParam[]> paramSets = null;

        if (entries != null) {
            for (Entry<TCQueryFilterKey, TCQueryFilterValue<?>> e : entries) {
                sb.append(" AND (");

                QueryParam[] params = e.getValue().appendSQLWhereConstraint(
                        e.getKey(), sb);
                if (params != null) {
                    if (paramSets == null) {
                        paramSets = new HashSet<QueryParam[]>();
                    }

                    paramSets.add(params);
                }

                sb.append(")");
            }
        }

        if (doStudyPermissionCheck) {
            QueryUtil.appendDicomSecurityFilter(sb);
        }

        Query query = em.createQuery(sb.toString());

        if (doStudyPermissionCheck) {
            query.setParameter("roles", roles);
        }

        if (paramSets != null) {
            for (QueryParam[] paramSet : paramSets) {
                if (paramSet != null) {
                    for (QueryParam param : paramSet) {
                        query.setParameter(param.getKey(), param.getValue());
                    }
                }
            }
        }

        log.info("Executing teaching-file query: " + query.toString());
        log.info("Restricted to aets: " + restrictedSourceAETs);

        List<Instance> instances = query.getResultList();

        if (instances != null) {
            for (Instance instance : instances) {
                // touch dicom tree
                instance.getSeries().getSeriesInstanceUID();
                instance.getSeries().getStudy().getStudyInstanceUID();
                instance.getSeries().getStudy().getPatient().getPatientID();

                List<File> files = instance.getFiles();
                if (files != null) {
                    for (File file : files) {
                        file.getFilePath();
                        file.getFileSystem().getDirectoryPath();
                    }
                }
            }

            log.info(instances.size() + " matching teaching-files found!");
        }

        return instances;
    }

}
