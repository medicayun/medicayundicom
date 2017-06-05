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
			sb.append(ds.getString(Tags.CodingSchemeDesignator)).append(System.getProperty("line.separator", "\n"));
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
