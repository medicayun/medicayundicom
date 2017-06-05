/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.conf.ConfigurationException;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1795 $ $Date: 2005-06-22 16:08:36 +0800 (周三, 22 6月 2005) $
 * @since Jun 6, 2005
 * 
 * @ejb.bean name="StudyMgt" type="Stateless" view-type="remote"
 *           jndi-name="ejb/StudyMgt"
 *           
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="Study" view-type="local" ref-name="ejb/Study"
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 * @ejb.ejb-ref ejb-name="Instance" view-type="local" ref-name="ejb/Instance"
 * 
 * @ejb.env-entry name="AttributeFilterConfigURL" type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-filter.xml"
 */
public abstract class StudyMgtBean implements SessionBean {

    private static final Logger log = Logger.getLogger(StudyMgtBean.class);
	
	private PatientLocalHome patHome;

	private StudyLocalHome studyHome;

	private SeriesLocalHome seriesHome;

	private InstanceLocalHome instHome;

	private AttributeFilter attrFilter;

	public void setSessionContext(SessionContext arg0) throws EJBException,
			RemoteException {
		Context jndiCtx = null;
		try {
			jndiCtx = new InitialContext();
			patHome = (PatientLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Patient");
			studyHome = (StudyLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Study");
			seriesHome = (SeriesLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Series");
			instHome = (InstanceLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Instance");
            attrFilter = new AttributeFilter((String) jndiCtx
                    .lookup("java:comp/env/AttributeFilterConfigURL"));
		} catch (NamingException e) {
			throw new EJBException(e);
		} catch (ConfigurationException e) {
			throw new EJBException(e);
		} finally {
			if (jndiCtx != null) {
				try {
					jndiCtx.close();
				} catch (NamingException ignore) {
				}
			}
		}
	}

	public void unsetSessionContext() {
		patHome = null;
		studyHome = null;
		seriesHome = null;
		instHome = null;
	}
	
	/**
     * @ejb.interface-method
     */
	public void createStudy(Dataset ds) throws DcmServiceException {
		try {
			checkDuplicateStudy(ds.getString(Tags.StudyInstanceUID));
			studyHome.create(
					ds.subSet(attrFilter.getStudyFilter()), getPatient(ds));
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
	}

	private PatientLocal getPatient(Dataset ds) throws DcmServiceException {
		String pid = ds.getString(Tags.PatientID);
		String issuer = ds.getString(Tags.IssuerOfPatientID);
		try {
			Collection c = issuer != null 
					? patHome.findByPatientIdWithIssuer(pid, issuer)
					: patHome.findByPatientId(pid);
			final int n = c.size();
			switch (n) {
			case 0:
				return patHome.create(ds.subSet(attrFilter.getPatientFilter()));
			case 1:
				return (PatientLocal) c.iterator().next();
			default:
				throw new DcmServiceException(Status.ProcessingFailure,
						"Found " + n + " Patients with id=" + pid
						+ ", issuer=" + issuer);					
			}
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
	}

	private void checkDuplicateStudy(String suid) 
			throws FinderException, DcmServiceException {
		try {
			studyHome.findByStudyIuid(suid);
			throw new DcmServiceException(Status.DuplicateSOPInstance, suid);
		} catch (ObjectNotFoundException e) {
		}
	}
	
	private StudyLocal getStudy(String suid) 
			throws FinderException, DcmServiceException {
		try {
			return studyHome.findByStudyIuid(suid);
		} catch (ObjectNotFoundException e) {
			throw new DcmServiceException(Status.NoSuchSOPClass, suid);
		}
	}

	/**
     * @ejb.interface-method
     */
	public void updateStudy(String iuid, Dataset ds)
			throws DcmServiceException {
		try {
			StudyLocal study = getStudy(iuid);
			if (ds.contains(Tags.PatientID)) {
				PatientLocal prevPat = study.getPatient();
				PatientLocal pat = getPatient(ds);
				if (!pat.isIdentical(prevPat)) {					
					log.info("Move " + study.asString() + " from " + 
							prevPat.asString() + " to " + pat.asString());
					study.setPatient(getPatient(ds));
				}
			}
			Dataset attrs = study.getAttributes(false);
			attrs.putAll(ds.subSet(attrFilter.getStudyFilter()));
			study.setAttributes(attrs);
			DcmElement seriesSq = ds.get(Tags.RefSeriesSeq);
			if (seriesSq != null) {
				Set dirtyStudies = new HashSet();
				Set dirtySeries = new HashSet();
				for (int i = 0, n = seriesSq.vm(); i < n; ++i) {
					updateSeries(seriesSq.getItem(i), study, dirtyStudies,
							dirtySeries);
				}
				updateDerivedSeriesFields(dirtySeries);
				updateDerivedStudyFields(dirtyStudies);
			}
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}	
	}

	private void updateDerivedStudyFields(Set dirtyStudies) 
			throws FinderException {
		for (Iterator it = dirtyStudies.iterator(); it.hasNext();){
			String iuid = (String) it.next();
			StudyLocal study = studyHome.findByStudyIuid(iuid);
			study.updateDerivedFields(true, true, true, true, true, true);
		}
	}

	private void updateDerivedSeriesFields(Set dirtySeries)
			throws FinderException {
		for (Iterator it = dirtySeries.iterator(); it.hasNext();){
			String iuid = (String) it.next();
			SeriesLocal series = seriesHome.findBySeriesIuid(iuid);
			series.updateDerivedFields(true, true, true, true, true);
		}
	}

	private void updateSeries(Dataset ds, StudyLocal study, 
			Set dirtyStudies, Set dirtySeries)
			throws FinderException, CreateException {
		Dataset newAttrs = ds.subSet(attrFilter.getSeriesFilter());
		try {
			SeriesLocal series = seriesHome.findBySeriesIuid(
					ds.getString(Tags.SeriesInstanceUID));
			StudyLocal prevStudy = series.getStudy();
			if (!study.isIdentical(prevStudy)) {
				log.info("Move " + series.asString() + " from " + 
						prevStudy.asString() + " to " + study.asString());
				series.setStudy(study);
				dirtyStudies.add(study.getStudyIuid());
				dirtyStudies.add(prevStudy.getStudyIuid());
			}
			Dataset attrs = series.getAttributes(false);
			String newModality = newAttrs.getString(Tags.Modality);
			if (newModality != null
					&& !newModality.equals(attrs.getString(Tags.Modality))) {
				dirtyStudies.add(study.getStudyIuid());				
			}
			attrs.putAll(newAttrs);
			series.setAttributes(attrs);
			DcmElement sopSq = ds.get(Tags.RefSOPSeq);
			if (sopSq != null) {
				for (int i = 0, n = sopSq.vm(); i < n; ++i) {
					updateInstance(sopSq.getItem(i), series, 
							dirtyStudies, dirtySeries);
				}
			}
		} catch (ObjectNotFoundException e) {
			seriesHome.create(newAttrs, study);
			dirtyStudies.add(study.getStudyIuid());
		}
	}

	private void updateInstance(Dataset ds, SeriesLocal series,
			Set dirtyStudies, Set dirtySeries) 
			throws FinderException, CreateException {
		Dataset newAttrs = ds.subSet(attrFilter.getInstanceFilter());
		try {
			InstanceLocal inst = instHome.findBySopIuid(
					ds.getString(Tags.RefSOPInstanceUID));
			SeriesLocal prevSeries = inst.getSeries();
			if (!series.isIdentical(prevSeries)) {
				log.info("Move " + inst.asString() + " from " + 
						prevSeries.asString() + " to " + series.asString());
				inst.setSeries(series);
				dirtySeries.add(series.getSeriesIuid());
				dirtyStudies.add(series.getStudy().getStudyIuid());
				dirtySeries.add(prevSeries.getSeriesIuid());
				dirtyStudies.add(prevSeries.getStudy().getStudyIuid());
			}
			Dataset attrs = inst.getAttributes(false);
			attrs.putAll(newAttrs);
			inst.setAttributes(attrs);
		} catch (ObjectNotFoundException e) {
			instHome.create(newAttrs, series);
			dirtySeries.add(series.getSeriesIuid());
			dirtyStudies.add(series.getStudy().getStudyIuid());
		}
	}

	
	/**
     * @ejb.interface-method
     */
	public void deleteStudy(String iuid) throws DcmServiceException {
		try {
			getStudy(iuid).remove();			
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (RemoveException e) {
			throw new EJBException(e);
		}	
	}
		
	/**
     * @ejb.interface-method
     */
	public void deleteSeries(String[] iuids) {
		try {
			Set dirtyStudies = new HashSet();
			for (int i = 0; i < iuids.length; i++) {
				SeriesLocal series = seriesHome.findBySeriesIuid(iuids[i]);
				dirtyStudies.add(series.getStudy().getStudyIuid());
				series.remove(); 
			}			
			updateDerivedStudyFields(dirtyStudies);
		} catch (ObjectNotFoundException ignore) {			
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (RemoveException e) {
			throw new EJBException(e);
		}		
	}
		
	/**
     * @ejb.interface-method
     */
	public void deleteInstances(String[] iuids) throws DcmServiceException {
		try {
			Set dirtySeries = new HashSet();
			Set dirtyStudies = new HashSet();
			for (int i = 0; i < iuids.length; i++) {
				InstanceLocal inst = instHome.findBySopIuid(iuids[i]);
				SeriesLocal series = inst.getSeries();
				dirtySeries.add(series.getSeriesIuid());
				dirtyStudies.add(series.getStudy().getStudyIuid());
				inst.remove();
			}
			updateDerivedSeriesFields(dirtySeries);
			updateDerivedStudyFields(dirtyStudies);
		} catch (ObjectNotFoundException ignore) {			
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (RemoveException e) {
			throw new EJBException(e);
		}		
	}
}
