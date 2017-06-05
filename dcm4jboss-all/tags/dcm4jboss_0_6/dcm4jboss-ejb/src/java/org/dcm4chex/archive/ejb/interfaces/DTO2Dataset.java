/*
 * Created on Feb 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.dcm4chex.archive.ejb.interfaces;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author umberto cappellini
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DTO2Dataset
{

	/**
	 * 
	 */
	public DTO2Dataset()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public static void updtateDataset(
		Dataset toUpdate,
		PatientDTO patient) //works by reference
	{
		// note:Patient ID's couldn't be updated

		toUpdate.putPN(Tags.PatientName, patient.getPatientName());

		try
		{
			if (patient.getPatientBirthDate() == null)
				toUpdate.putDA(Tags.PatientBirthDate);		
			else
			toUpdate.putDA(
				Tags.PatientBirthDate,
				new SimpleDateFormat(PatientDTO.DATE_FORMAT).parse(
					patient.getPatientBirthDate()));
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		toUpdate.putCS(Tags.PatientSex, patient.getPatientSex());
	}
}
