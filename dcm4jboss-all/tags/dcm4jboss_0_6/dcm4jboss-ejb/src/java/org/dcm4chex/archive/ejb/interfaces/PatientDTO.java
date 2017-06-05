/*
 * $Id: PatientDTO.java 979 2004-02-18 12:22:45Z umbertoc $ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
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
package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 979 $ $Date: 2004-02-18 20:22:45 +0800 (周三, 18 2月 2004) $
 * @since 14.01.2004
 */
public class PatientDTO implements Serializable
{

	public static final String DATE_FORMAT = "yyyy/MM/dd";
	private int pk;
	private String patientID=null;
	private String patientName=null;
	private String patientSex=null;
	private String patientBirthDate=null;
	private List studies = new ArrayList();

	/**
	 * @return
	 */
	public final String getPatientBirthDate()
	{
		return patientBirthDate;
	}

	/**
	 * @param patientBirthDate
	 */
	public final void setPatientBirthDate(String patientBirthDate)
	{
		this.patientBirthDate = patientBirthDate;
	}

	/**
	 * @return
	 */
	public final String getPatientID()
	{
		return patientID;
	}

	/**
	 * @param patientID
	 */
	public final void setPatientID(String patientID)
	{
		this.patientID = patientID;
	}

	/**
	 * @return
	 */
	public final String getPatientName()
	{
		return patientName;
	}

	/**
	 * @param patientName
	 */
	public final void setPatientName(String patientName)
	{
		this.patientName = patientName;
	}

	/**
	 * @return
	 */
	public final String getPatientSex()
	{
		return patientSex;
	}

	/**
	 * @param patientSex
	 */
	public final void setPatientSex(String patientSex)
	{
		this.patientSex = patientSex;
	}

	/**
	 * @return
	 */
	public final int getPk()
	{
		return pk;
	}

	/**
	 * @param pk
	 */
	public final void setPk(int pk)
	{
		this.pk = pk;
	}

	/**
	 * @return
	 */
	public final List getStudies()
	{
		return studies;
	}

	/**
	 * @param studies
	 */
	public final void setStudies(List studies)
	{
		this.studies = studies;
	}

	public final String getPatientBirthDay()
	{
		String res = null;
		try
		{
			Calendar c = Calendar.getInstance();
			if (getPatientBirthDate() != null)
			{
				c.setTime(new SimpleDateFormat(DATE_FORMAT).parse(getPatientBirthDate()));
				res = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
			}
		}
		catch (ParseException e)
		{
		}

		return res;
	}

	public final String getPatientBirthMonth()
	{
		String res = null;
		try
		{
			Calendar c = Calendar.getInstance();
			if (getPatientBirthDate() != null)
			{
				c.setTime(new SimpleDateFormat(DATE_FORMAT).parse(getPatientBirthDate()));
				res = String.valueOf(c.get(Calendar.MONTH)+1);
			}
		}
		catch (ParseException e)
		{
		}
		return res;
	}

	public final String getPatientBirthYear()
	{
		String res = null;
		try
		{

			Calendar c = Calendar.getInstance();
			if (getPatientBirthDate() != null)
			{
				c.setTime(new SimpleDateFormat(DATE_FORMAT).parse(getPatientBirthDate()));
				res = String.valueOf(c.get(Calendar.YEAR));
			}
		}
		catch (ParseException e)
		{
		}

		return res;
	}
}
