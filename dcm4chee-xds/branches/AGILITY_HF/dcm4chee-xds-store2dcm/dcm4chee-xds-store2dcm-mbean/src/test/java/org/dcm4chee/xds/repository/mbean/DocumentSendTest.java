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

package org.dcm4chee.xds.repository.mbean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.dcm4che2.data.Tag;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DimseRSP;
import org.dcm4chee.xds.repository.XDSDocumentWriter;
import org.dcm4chee.xds.repository.XDSDocumentWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class DocumentSendTest extends TestCase {

    private static final XDSDocumentWriterFactory fac =  XDSDocumentWriterFactory.getInstance();
    
	private DocumentSendCfg dcmCfg = new DocumentSendCfg();
	private StgCmtSCU stgCmtScu = new StgCmtSCU(dcmCfg);

    private static Boolean skipRemoteTests;
    
    private static Logger log = LoggerFactory.getLogger(DocumentSendTest.class);
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DocumentSendTest.class);
    }

    public DocumentSendTest(String arg0) {
        super(arg0);
    }
    
    public void setUp() throws IOException {
    	Store2Dcm.getMime2CuidMap().put("default", Store2Dcm.ENCAPSULATED_DOCUMENT_STORAGE_CUID);
    	if ( skipRemoteTests == null ) {
	    	skipRemoteTests = new Boolean( !checkStoreSCP() );
	    	if ( ! skipRemoteTests.booleanValue() ) {
	    		setUpTestFile("large.txt", 10240);//10MB
	    	} else {
	    		log.warn("DocumentSend Tests are disabled! Check your configuration to enable this tests!");
	    		promptBasicDicomConfig();
	    	}
    	}
    }

    private boolean checkStoreSCP() {
    	DocumentSend send = null;
        try {
        	send = getDocumentSend();
            Association assoc = send.open();
            DimseRSP rsp = assoc.cecho();
            rsp.next();
            int status = rsp.getCommand().getInt(Tag.Status);
            log.info("CECHO status:"+status);
            return status == 0;
        } catch (Exception e) {
            log.info("ERROR: Failed to establish association! Skip DocumentSend Tests!", e);
            return false;
        } finally {
        	if ( send != null )
        		send.close();
        }
	}

	private void setUpTestFile(String fn, int sizeInKB) throws IOException {
    	File dir = locateFile("test.pdf").getParentFile();// to get directory
    	File f = new File(dir, fn);
		if (!f.exists()) {
			String buf = "abcdefghijklmnopqrstuvwxyz+ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789";//64 chars
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for ( int i = 0, len=sizeInKB << 4  ; i < len ; i++ ) { //64*16=1KB
				bw.write(buf);
			}
			bw.close();
		}
	}

	public final void testSendDocumentSimple() throws IOException, ParserConfigurationException, SAXException,TransformerException, NoSuchAlgorithmException {
    	if ( skipRemoteTests.booleanValue() ) return;
        File docFile = locateFile("test.doc");
        File submFile = locateFile("test1_subm.xml");
        File xslFile = locateFile("testsend_subm.xsl");
        FileInputStream submStream = new FileInputStream( submFile);
 
        sendDocument(null, docFile, xslFile, submStream, false);
    }
    public final void testSendDocumentAndStgCmt() throws IOException, ParserConfigurationException, SAXException,TransformerException, NoSuchAlgorithmException {
    	if ( skipRemoteTests.booleanValue() ) return;
        File docFile = locateFile("test.doc");
        File submFile = locateFile("test1_subm.xml");
        File xslFile = locateFile("testsend_subm.xsl");
        FileInputStream submStream = new FileInputStream( submFile);
 
        sendDocument(null, docFile, xslFile, submStream, true);
    }
    public final void testSendDocumentPDF() throws IOException, ParserConfigurationException, SAXException,TransformerException, NoSuchAlgorithmException {
    	if ( skipRemoteTests.booleanValue() ) return;
        File docFile = locateFile("test.pdf");
        File submFile = locateFile("pdf_subm.xml");
        File xslFile = locateFile("testsend_subm.xsl");
        FileInputStream submStream = new FileInputStream( submFile);
        
        sendDocument(null, docFile, xslFile, submStream, false);
    }

    public final void testSendDocumentLarge() throws IOException, ParserConfigurationException, SAXException,TransformerException, NoSuchAlgorithmException {
    	if ( skipRemoteTests.booleanValue() ) return;
        File docFile = locateFile("large.txt");
        File submFile = locateFile("test1_subm.xml");
        File xslFile = locateFile("testsend_subm.xsl");
        FileInputStream submStream = new FileInputStream( submFile);
        
        sendDocument(null, docFile, xslFile, submStream, false);
    }

	private void sendDocument(File xmlFile, File docFile, File xslFile,
			FileInputStream submStream, boolean stgCmt) throws FileNotFoundException,
			ParserConfigurationException, SAXException, IOException,
			TransformerException {
		DocumentSend send = null;
		try {
	        if ( stgCmt ) {
	        	stgCmtScu.setRequestStgCmt(true);
	        	stgCmtScu.setStgCmtSynchronized(true);
	        	stgCmtScu.setStgCmtTimeout(10000);
	        }
			XDSDocumentWriter docWriter = fac.getDocumentWriter( docFile ); 
	        Store2Dcm store = new Store2Dcm(xmlFile, docWriter, new StreamSource(submStream), xslFile);
	        send = getDocumentSend();
	        byte[] hash = send.sendDocument(store);
	        if ( hash != null && stgCmtScu != null && stgCmtScu.isRequestStgCmt() ) {
	        	log.debug("+++ call startStgCmtListener +++");
	        	stgCmtScu.startStgCmtListener();
	        	log.debug("+++ after call startStgCmtListener +++");
	        	String tuid = stgCmtScu.requestStgCmt(send.getActiveAssociation(), store.getSOPClassUID(), store.getSOPInstanceUID());
	        	log.info("StgCmt TransactionUID:"+tuid);
	            if (tuid != null) {
	            	if ( stgCmtScu.isStgCmtSynchronized() ) {
	                    log.info("StgCmt Synchronized! Waiting for Storage Commitment Result..");
	                    try {
	                        if ( stgCmtScu.waitForStgCmtResult(store) != null ) {
		                        log.debug("+++ store after wait:"+store);
	                        } else {
	                			log.warn("Wait for StgCmt Response timed out! Check your configuration!");
	                			log.warn("This failure will be ignored!!! (commit the storage anyway)");
	                			promptBasicDicomConfig();
	                			store.setCommitted(true);
	                        }
	                        if (!store.isCommitted()) {
	                        	hash = null;
	                        }
	                    } catch (InterruptedException e) {
	                        log.error("ERROR:" + e.getMessage());
	                    }
	            	}
	            } else if (stgCmtScu.isStgCmtSynchronized() ) {
	            	hash = null; //StgCmt request failed --> error
	            } else {
	            	//retry
	            }
	        	stgCmtScu.stopStgCmtListener();
	        }
	        assertNotNull("Send Document failed!",hash);
		} finally {
			if ( send != null )
				send.close();
		}
	}

	private DocumentSend getDocumentSend() throws IOException {
		DocumentSend docsender = new DocumentSend(dcmCfg);
		dcmCfg.setCallingAET("DOC_SEND_TEST");
		dcmCfg.setCalledAET("DCM4CHEE");
		dcmCfg.setRemoteHost("localhost");
		dcmCfg.setRemotePort(11112);
		dcmCfg.setLocalHost("localhost");
		dcmCfg.setLocalPort(11114);
		return docsender;
	}

    private static File locateFile(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(cl.getResource(name).toString().substring(5));
    }

    private void promptBasicDicomConfig() {
    	log.info("Dicom Configuration:");
		log.info("StoreSCP and StgCmtSCP AET:"+this.dcmCfg.getCalledAET()+"( host:"+dcmCfg.getRemoteHost()+" port:"+dcmCfg.getRemotePort()+")");
		log.info("StgCmtSCU AET:"+this.dcmCfg.getCallingAET()+"( host:"+dcmCfg.getLocalHost()+" port:"+dcmCfg.getLocalPort()+")");
    }
}

