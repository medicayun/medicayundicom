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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author umberto cappellini
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DTO2Dataset
{

    private static DcmObjectFactory dofFactory = DcmObjectFactory.getInstance();
    
	private DTO2Dataset()
	{
	}

	public static Dataset toDataset(PatientDTO patient)
	{
	    Dataset ds = dofFactory.newDataset();
	    String s = patient.getSpecificCharacterSet();
	    if (s != null && s.length() != 0) {
	    	ds.putCS(Tags.SpecificCharacterSet, s);
	    }
	    s = patient.getPatientID();
	    if (s != null && s.length() != 0) {
	    	ds.putLO(Tags.PatientID, s);
	    }
	    s = patient.getIssuerOfPatientID();
	    if (s != null && s.length() != 0) {
	    	ds.putLO(Tags.IssuerOfPatientID, s);
	    }
	    updateDataset(ds, patient);
	    return ds;
	}
	
    public static void updateDataset(
		Dataset toUpdate,
		PatientDTO patient) //works by reference
	{
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
