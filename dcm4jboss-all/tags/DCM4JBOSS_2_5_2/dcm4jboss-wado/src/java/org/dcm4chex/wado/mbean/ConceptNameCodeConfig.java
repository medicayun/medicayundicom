/*
 * Created on 24.08.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ConceptNameCodeConfig {

    private static final DcmObjectFactory factory = DcmObjectFactory.getInstance();
	
    private Map conceptNameCodeLists = new HashMap();

	private static final List EMPTY_LIST = new ArrayList();
	
	/**
	 * @return Returns the concept name codes for cardiology encapsulated pdf.
	 */
	public List getConceptNameCodes(String key) {
		List l = (List) conceptNameCodeLists.get(key);
		return l != null ? l : EMPTY_LIST;
	}
	/**
	 * @param names List of concept name codes.
	 */
	public void setConceptNameCodes(String key, List conceptNames) {
		conceptNameCodeLists.put( key, conceptNames );
	}
	public List setConceptNameCodes(String key, String conceptNames) {
		List l = parseConceptNameCodeString( conceptNames );
		conceptNameCodeLists.put( key, l);
		return l;
	}

	/**
	 * @return Returns the concept name codes for cardiology encapsulated pdf.
	 */
	public String getConceptNameCodeString(String key) {
		List l = (List) conceptNameCodeLists.get(key);
		return l == null ? null : toConceptNameCodeString(l);
	}
	
	
	public List parseConceptNameCodeString( String conceptNames ) {
		if ( conceptNames != null && conceptNames.trim().length() > 0 ) {
	        StringTokenizer st = new StringTokenizer(conceptNames, "\r\n;");
	        List names = new ArrayList();
	        StringTokenizer st1;
	        while ( st.hasMoreTokens() ) {
	        	st1 = new StringTokenizer( st.nextToken(), ":");
	        	names.add( createCodeDS( st1.nextToken(), st1.hasMoreTokens() ? st1.nextToken():"LN" ) );
	        }
	        return names;
		}
		return null;
		
	}
	
	public String toConceptNameCodeString( List names ) {
		if ( names == null ) return null;
		StringBuffer sb = new StringBuffer();
		Dataset ds;
		for ( Iterator iter = names.iterator() ; iter.hasNext() ; ) {
			ds = (Dataset) iter.next();
			sb.append(ds.getString(Tags.CodeValue)).append(":");
			sb.append(ds.getString(Tags.CodingSchemeDesignator)).append('\r').append('\n');
		}
		return sb.toString();
	}
	
	public static List getDefaultCardiologyConceptNameCodes() {
		List cardiologyConceptNameCodes = new ArrayList();
		cardiologyConceptNameCodes.add( createCodeDS( "18745-0", "LN" ) );//Cardiac Catheteization Report
		cardiologyConceptNameCodes.add( createCodeDS( "11522-0", "LN" ) );//Echocardiography Report
		cardiologyConceptNameCodes.add( createCodeDS( "10001", "99SUPP97" ) );//Quantitavie Arteriography report //a cardio report?
		cardiologyConceptNameCodes.add( createCodeDS( "122291", "DCM" ) );//CT/MR Cardiovascular Report
		cardiologyConceptNameCodes.add( createCodeDS( "122292", "DCM" ) );//Quantitative Ventriculography Report
		cardiologyConceptNameCodes.add( createCodeDS( "125200", "DCM" ) );//Adult Echocardiography Procedure Report
		return cardiologyConceptNameCodes;
	}
	public static List getDefaultRadiologyConceptNameCodes() {
		List radiologyConceptNameCodes = new ArrayList();
		radiologyConceptNameCodes.add( createCodeDS( "11540-2", "LN" ) );//CT Abdomen Report
		radiologyConceptNameCodes.add( createCodeDS( "11538-6", "LN" ) );//CT Chest Report
		radiologyConceptNameCodes.add( createCodeDS( "11539-4", "LN" ) );//CT Head Report
		radiologyConceptNameCodes.add( createCodeDS( "18747-6", "LN" ) );//CT Report
		radiologyConceptNameCodes.add( createCodeDS( "18748-4", "LN" ) );//Diagnostic Imaging Report
		radiologyConceptNameCodes.add( createCodeDS( "18760-9", "LN" ) );//Ultrasound Report
		radiologyConceptNameCodes.add( createCodeDS( "11541-0", "LN" ) );//MRI Head Report
		radiologyConceptNameCodes.add( createCodeDS( "18755-9", "LN" ) );//MRI Report
		radiologyConceptNameCodes.add( createCodeDS( "18756-7", "LN" ) );//MRI Spine Report
		radiologyConceptNameCodes.add( createCodeDS( "18757-5", "LN" ) );//Nuclear Medicine Report
		radiologyConceptNameCodes.add( createCodeDS( "11525-3", "LN" ) );//Ultrasound Obstetric and Gyn Report
		radiologyConceptNameCodes.add( createCodeDS( "18758-3", "LN" ) );//PET Scan Report
		radiologyConceptNameCodes.add( createCodeDS( "11528-7", "LN" ) );//Radiology Report
		return radiologyConceptNameCodes;
	}
	
	private static Dataset createCodeDS( String value, String design ) {
		Dataset ds = factory.newDataset();
        ds.putSH(Tags.CodeValue, value);
        ds.putSH(Tags.CodingSchemeDesignator, design);
        return ds;
	}
	
}
