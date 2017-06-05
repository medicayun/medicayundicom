/*
 * $Id: FolderForm.java 998 2004-02-20 10:34:33Z umbertoc $ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.ejb.interfaces.InstanceDTO;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.ejb.interfaces.SeriesDTO;
import org.dcm4chex.archive.ejb.interfaces.StudyDTO;
import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 998 $ $Date: 2004-02-20 18:34:33 +0800 (周五, 20 2月 2004) $
 * @since 28.01.2004
 */
public class FolderForm
{
	static final String FOLDER_ATTRNAME = "folderFrom";
	static FolderForm getFolderForm(HttpServletRequest request)
	{
		FolderForm form =
			(FolderForm) request.getSession().getAttribute(FOLDER_ATTRNAME);
		if (form == null)
		{
			form = new FolderForm();
			request.getSession().setAttribute(FOLDER_ATTRNAME, form);
		}
		return form;
	}

	public static final int LIMIT = 10;

	private String patientID;
	private String patientName;
	private String accessionNumber;
	private String studyID;
	private String studyDateRange;
	private String modality;

	private StudyFilterDTO studyFilter = null;
	private List patients;
	private final Set stickyPatients = new HashSet();
	private final Set stickyStudies = new HashSet();
	private final Set stickySeries = new HashSet();
	private final Set stickyInstances = new HashSet();
	private int offset;
	private int total;

	public final int getLimit()
	{
		return LIMIT;
	}

	public final String getAccessionNumber()
	{
		return accessionNumber;
	}

	public final void setAccessionNumber(String accessionNumber)
	{
		this.accessionNumber = accessionNumber;
	}

	public final String getModality()
	{
		return modality;
	}

	public final void setModality(String modality)
	{
		this.modality = modality;
	}

	public final String getPatientID()
	{
		return patientID;
	}

	public final void setPatientID(String patientID)
	{
		this.patientID = patientID;
	}

	public final String getPatientName()
	{
		return patientName;
	}

	public final void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	public final String getStudyDateRange()
	{
		return studyDateRange;
	}

	public final void setStudyDateRange(String studyDateRange)
	{
		this.studyDateRange = studyDateRange;
	}

	public final String getStudyID()
	{
		return studyID;
	}

	public final void setStudyID(String studyID)
	{
		this.studyID = studyID;
	}

	public final void setTotal(int total)
	{
		this.total = total;
	}

	public final Set getStickyInstances()
	{
		return stickyInstances;
	}

	public final Set getStickyPatients()
	{
		return stickyPatients;
	}

	public final Set getStickySeries()
	{
		return stickySeries;
	}

	public final Set getStickyStudies()
	{
		return stickyStudies;
	}

	public final int getOffset()
	{
		return offset;
	}

	public final List getPatients()
	{
		return patients;
	}

	public final void setFilter(String filter)
	{
		offset = 0;
		total = -1;
		studyFilter = null;
	}

	public final void setNext(String next)
	{
		offset += LIMIT;
	}

	public final void setPrev(String prev)
	{
		offset = Math.max(0, offset - LIMIT);
	}

	public final StudyFilterDTO getStudyFilter()
	{
		if (studyFilter == null)
		{
			studyFilter = new StudyFilterDTO();
			studyFilter.setPatientID(patientID);
			studyFilter.setPatientName(patientName);
			studyFilter.setAccessionNumber(accessionNumber);
			studyFilter.setStudyID(studyID);
			studyFilter.setStudyDateRange(studyDateRange);
			studyFilter.setModality(modality);
		}
		return studyFilter;
	}

	public final int getTotal()
	{
		return total;
	}

	public void updatePatients(List newPatients)
	{
		List sticky = patients;
		patients = newPatients;
		if (sticky != null)
		{
			for (int i = sticky.size(); --i >= 0;)
			{
				PatientDTO pat = (PatientDTO) sticky.get(i);
				if (isSticky(pat))
				{
					mergeSticky(pat);
				}
			}
		}
	}

	private void mergeSticky(PatientDTO stickyPat)
	{
		for (int i = patients.size(); --i >= 0;)
		{
			PatientDTO pat = (PatientDTO) patients.get(i);
			if (pat.getPk() == stickyPat.getPk())
			{
				List stickyStudies = stickyPat.getStudies();
				for (int j = stickyStudies.size(); --j >= 0;)
				{
					mergeSticky(
						(StudyDTO) stickyStudies.get(j),
						pat.getStudies());
				}
				return;
			}
		}
		patients.add(0, stickyPat);
	}

	private void mergeSticky(StudyDTO stickyStudy, List studies)
	{
		for (int i = studies.size(); --i >= 0;)
		{
			StudyDTO study = (StudyDTO) studies.get(i);
			if (study.getPk() == stickyStudy.getPk())
			{
				List stickySeries = stickyStudy.getSeries();
				for (int j = stickySeries.size(); --j >= 0;)
				{
					mergeSticky(
						(SeriesDTO) stickySeries.get(j),
						study.getSeries());
				}
				return;
			}
		}
		studies.add(0, stickyStudy);
	}

	private void mergeSticky(SeriesDTO stickySerie, List series)
	{
		for (int i = series.size(); --i >= 0;)
		{
			SeriesDTO serie = (SeriesDTO) series.get(i);
			if (serie.getPk() == stickySerie.getPk())
			{
				List stickyInstances = stickySerie.getInstances();
				for (int j = stickyInstances.size(); --j >= 0;)
				{
					mergeSticky(
						(InstanceDTO) stickyInstances.get(j),
						serie.getInstances());
				}
				return;
			}
		}
		series.add(0, stickySerie);
	}

	private void mergeSticky(InstanceDTO stickyInst, List instances)
	{
		for (int i = instances.size(); --i >= 0;)
		{
			InstanceDTO inst = (InstanceDTO) instances.get(i);
			if (inst.getPk() == stickyInst.getPk())
			{
				return;
			}
		}
		instances.add(0, stickyInst);
	}

	private boolean isSticky(PatientDTO patientDTO)
	{
		boolean sticky = stickyPatients.contains("" + patientDTO.getPk());
		for (Iterator it = patientDTO.getStudies().iterator(); it.hasNext();)
		{
			if (isSticky((StudyDTO) it.next()))
			{
				sticky = true;
			} else
			{
				it.remove();
			}
		}
		return sticky;
	}

	private boolean isSticky(StudyDTO studyDTO)
	{
		boolean sticky = stickyStudies.contains("" + studyDTO.getPk());
		for (Iterator it = studyDTO.getSeries().iterator(); it.hasNext();)
		{
			if (isSticky((SeriesDTO) it.next()))
			{
				sticky = true;
			} else
			{
				it.remove();
			}
		}
		return sticky;
	}

	private boolean isSticky(SeriesDTO seriesDTO)
	{
		boolean sticky = stickySeries.contains("" + seriesDTO.getPk());
		for (Iterator it = seriesDTO.getInstances().iterator(); it.hasNext();)
		{
			if (isSticky((InstanceDTO) it.next()))
			{
				sticky = true;
			} else
			{
				it.remove();
			}
		}
		return sticky;
	}

	private boolean isSticky(InstanceDTO instanceDTO)
	{
		return stickyInstances.contains("" + instanceDTO.getPk());
	}

	public PatientDTO getPatientByPk(int patPk)
	{
		for (int i = 0, n = patients.size(); i < n; i++)
		{
			PatientDTO pat = (PatientDTO) patients.get(i);
			if (pat.getPk() == patPk)
			{
				return pat;
			}
		}
		return null;
	}

	public StudyDTO getStudyByPk(int patPk, int studyPk)
	{
		return getStudyByPk(getPatientByPk(patPk), studyPk);
	}

	public StudyDTO getStudyByPk(PatientDTO patient, int studyPk)
	{
		if (patient == null)
		{
			return null;
		}
		List studies = patient.getStudies();
		for (int i = 0, n = studies.size(); i < n; i++)
		{
			StudyDTO study = (StudyDTO) studies.get(i);
			if (study.getPk() == studyPk)
			{
				return study;
			}
		}
		return null;
	}

	public SeriesDTO getSeriesByPk(int patPk, int studyPk, int seriesPk)
	{
		return getSeriesByPk(getStudyByPk(patPk, studyPk), seriesPk);
	}

	public SeriesDTO getSeriesByPk(StudyDTO study, int seriesPk)
	{
		if (study == null)
		{
			return null;
		}
		List series = study.getSeries();
		for (int i = 0, n = series.size(); i < n; i++)
		{
			SeriesDTO serie = (SeriesDTO) series.get(i);
			if (serie.getPk() == seriesPk)
			{
				return serie;
			}
		}
		return null;
	}

	public void removeStickies()
	{
		PatientDTO patient;
		StudyDTO study;
		SeriesDTO series;
		InstanceDTO instance;
		for (Iterator patient_iter = patients.iterator();patient_iter.hasNext();)
		{
			patient = (PatientDTO) patient_iter.next();
			if (stickyPatients.contains(String.valueOf(patient.getPk())))
			{
				patient_iter.remove();
				stickyPatients.remove(String.valueOf(patient.getPk()));
			} else
				for (Iterator study_iter = patient.getStudies().iterator();
					study_iter.hasNext();
					)
				{
					study = (StudyDTO) study_iter.next();
					if (stickyStudies.contains(String.valueOf(study.getPk())))
					{
						study_iter.remove();
						stickyStudies.remove(String.valueOf(study.getPk()));
					} else
						for (Iterator series_iter =
							study.getSeries().iterator();
							series_iter.hasNext();
							)
						{
							series = (SeriesDTO) series_iter.next();
							if (stickySeries.contains(String.valueOf(series.getPk())))
							{
								series_iter.remove();
								stickySeries.remove(String.valueOf(series.getPk()));
							} else
								for (Iterator instance_iter =
									series.getInstances().iterator();
									instance_iter.hasNext();
									)
								{
									instance = (InstanceDTO) instance_iter.next();
									if (isSticky(instance))
									{
										instance_iter.remove();
										stickyInstances.remove(String.valueOf(instance.getPk()));
									}
								}
						}
				}
		}
	}
}