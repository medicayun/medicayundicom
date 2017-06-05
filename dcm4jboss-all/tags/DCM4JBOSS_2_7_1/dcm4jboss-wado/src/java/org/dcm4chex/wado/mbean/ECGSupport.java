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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Driver;
import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DataSource;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.WADOSupport.NeedRedirectionException;
import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.dcm4chex.wado.mbean.ecg.WaveformGroup;
import org.dcm4chex.wado.mbean.ecg.WaveformInfo;
import org.dcm4chex.wado.mbean.ecg.xml.FOPCreator;
import org.dcm4chex.wado.mbean.ecg.xml.SVGCreator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ECGSupport {

	public static final String CONTENT_TYPE_SVGXML = "image/svg+xml";
	private static Logger log = Logger.getLogger( ECGSupport.class.getName() );
	
    private static final DcmObjectFactory factory = DcmObjectFactory.getInstance();
	private RIDSupport ridSupport;
	
    private final Driver fop = new Driver();
	

    public ECGSupport( RIDSupport ridSupport ) {
    	this.ridSupport = ridSupport;
    }
	/**
	 * @param reqObj
	 * @param ds
	 * @return
	 */
	public WADOResponseObject getECGDocument(RIDRequestObject reqObj, Dataset ds) {
		String contentType = reqObj.getParam("preferredContentType");
		if ( contentType.equals( CONTENT_TYPE_SVGXML ) || contentType.equals( "image/svg" )) {
			contentType = CONTENT_TYPE_SVGXML;
			if ( ridSupport.checkContentType( reqObj, new String[]{ CONTENT_TYPE_SVGXML } ) == null ) {
				return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_BAD_REQUEST, "Display actor doesnt accept preferred content type!");
			}
		} else if ( contentType.equals( RIDSupport.CONTENT_TYPE_PDF) ) {
			if ( ridSupport.checkContentType( reqObj, new String[]{ RIDSupport.CONTENT_TYPE_PDF } ) == null ) {
				return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_BAD_REQUEST, "Display actor doesnt accept preferred content type!");
			}
		} else {
			return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_ACCEPTABLE, "preferredContentType '"+contentType+"' is not supported! Only 'application/pdf' and 'image/svg+xml' are supported !");
		}
		log.info("get ECG document!");
		WADOCache cache = WADOCacheImpl.getRIDCache();
		File outFile = cache.getFileObject(null, null, reqObj.getParam("documentUID"), contentType );
		try {
			if ( outFile.exists() && ! ridSupport.isOutdated( outFile, reqObj.getParam("documentUID"))) {
				return new WADOStreamResponseObjectImpl( new FileInputStream( outFile ),contentType, HttpServletResponse.SC_OK, null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Dataset fullDS = getDataset( ds );
			if ( fullDS == null ) {
				return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Requested document not found::"+ds.getString( Tags.SOPClassUID ) );
			} else {
				if ( contentType.equals( RIDSupport.CONTENT_TYPE_PDF ) ) {
					return handlePDF( fullDS, outFile );
				} else {
					return handleSVG( fullDS, outFile );
				}
			}
		} catch (NeedRedirectionException e) {
			return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_NOT_FOUND, "Requested Document is not on this Server! Try to get document from:"+e.getHostname() );
		} catch (Exception e) {
			return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error:"+e.getMessage() );
		} 
	}
	
	/**
	 * @param string
	 * @return
     * @throws NeedRedirectionException
     * @throws IOException
	 */
	private Dataset getDataset(Dataset ds) throws IOException, NeedRedirectionException {
		String iuid = ds.getString( Tags.SOPInstanceUID );
		File file = ridSupport.getWADOSupport().getDICOMFile(ds.getString( Tags.StudyInstanceUID),
															 ds.getString( Tags.SeriesInstanceUID),
															 iuid );
		if ( log.isDebugEnabled() ) log.debug("DCM file for "+ds.getString( Tags.SOPInstanceUID )+":"+file);
		if ( file == null ) return null;
		Dataset dsFile;
		if ( ! ridSupport.isUseOrigFile()   ) {
			
				DataSource dsrc = null;
				try {
					dsrc = (DataSource) ridSupport.getMBeanServer().invoke(ridSupport.getFileSystemMgtName(),
			                "getDatasourceOfInstance",
			                new Object[] { iuid },
			                new String[] { String.class.getName() } );
			        
			    } catch (Exception e) {
			        log.error("Failed to get updated DICOM file", e);
			    }
			    file = new File( file + ".dcm" );
			    file.deleteOnExit();
			    OutputStream os = new BufferedOutputStream( new FileOutputStream( file ) );
			    dsrc.writeTo( os, null);
			    os.close();
			    dsFile =loadDataset(file);
			    file.delete();
			}
		else {
			dsFile = loadDataset(file);
		}
		return dsFile;
	}
	private Dataset loadDataset(File file) throws IOException {
	    BufferedInputStream bis = new BufferedInputStream( new FileInputStream(file));
	    Dataset ds = factory.newDataset();
	    try {
	        ds.readFile(bis, null, -1); 
	    } finally {
	        try {
	            bis.close();
	        } catch (IOException ignore) {
	        }
	    }
	    if ( log.isDebugEnabled() ) log.debug("Dataset for file "+file+" :"+ds);
	    return ds;
    }
	
    /**
	 * @param ds
     * @param outFile
	 * @return
	 */
	private WADOResponseObject handleSVG(Dataset ds, File outFile) {
		OutputStream out = null;
		try { 
			DcmElement elem = ds.get( Tags.WaveformSeq);
			WaveformGroup wfgrp = new WaveformGroup( ds.getString(Tags.SOPClassUID), elem, 0, 
					ridSupport.getWaveformCorrection() );//TODO all groups
			if ( log.isDebugEnabled() ) log.debug( wfgrp );
			WaveformInfo wfInfo = new WaveformInfo( ds );

			SVGCreator svgCreator = new SVGCreator( wfgrp, wfInfo, new Float( 27.6f ), new Float(20.3f) );
//			SVGCreator svgCreator = new SVGCreator( wfgrp, wfInfo, null, new Float(40.0f) );
			out= new FileOutputStream( outFile );
			svgCreator.toXML( out );
			out.close();
			return new WADOStreamResponseObjectImpl( new FileInputStream( outFile), CONTENT_TYPE_SVGXML, HttpServletResponse.SC_OK, null);
		} catch ( Throwable t ) {
			if ( out != null )
				try {
					out.close();
				} catch (IOException e) {}
			if ( outFile.exists() ) outFile.delete();
			log.error("Cant create SVG for Waveform!", t);
			log.error("Waveform Dataset:");log.error(ds);
			return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating waveform SVG! Reason:"+t.getMessage());
		}
	}
	
	private WADOResponseObject handlePDF(Dataset ds, File outFile) {
		OutputStream out = null;
		OutputStream tmpOut = null;
		File tmpFile = null;
		try { 
	        outFile.getParentFile().mkdirs();
	        out = new FileOutputStream( outFile );
	        tmpFile = new File( outFile.toString()+".fop");
	        tmpFile.deleteOnExit();
	        tmpOut = new FileOutputStream( tmpFile);
	        
			DcmElement elem = ds.get( Tags.WaveformSeq);
			int nrOfWFGroups = elem.vm();
			WaveformGroup[] wfgrps = new WaveformGroup[ nrOfWFGroups ];
			for ( int i = 0 ; i < nrOfWFGroups ; i++ ) {
				wfgrps[i] = new WaveformGroup( ds.getString(Tags.SOPClassUID),elem, i,
						ridSupport.getWaveformCorrection());
			}
			WaveformInfo wfInfo = new WaveformInfo( ds );

			FOPCreator fopCreator = new FOPCreator( wfgrps, wfInfo, new Float( 28.6f ), new Float(20.3f) );
			fopCreator.toXML( tmpOut );
	        tmpOut.close();
	        fop.setRenderer(Driver.RENDER_PDF);
	        fop.setOutputStream( out );
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
	        Transformer t = tf.newTransformer();
	        t.transform(new StreamSource( new FileInputStream(tmpFile)), new SAXResult( fop.getContentHandler() ) );
	        out.close();
			tmpFile.delete();
			return new WADOStreamResponseObjectImpl(new FileInputStream( outFile ), RIDSupport.CONTENT_TYPE_PDF, HttpServletResponse.SC_OK, null);
		} catch ( Throwable t ) {
			try {
				if ( out != null ) out.close();
				if ( tmpOut != null ) tmpOut.close();
			} catch (IOException e) {}
			if ( outFile.exists() ) outFile.delete();
 			if ( tmpFile.exists() ) tmpFile.delete();
			log.error("Cant create PDF for Waveform!", t);
			log.error("Waveform Dataset:");log.error(ds);
			return new WADOStreamResponseObjectImpl( null, RIDSupport.CONTENT_TYPE_HTML, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating waveform PDF! Reason:"+t.getMessage());
		}
	}
	
	
}
