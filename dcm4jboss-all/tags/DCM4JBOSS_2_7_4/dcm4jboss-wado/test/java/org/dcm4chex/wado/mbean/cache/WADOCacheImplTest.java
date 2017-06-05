/*
 * Created on 03.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.cache;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import junit.framework.TestCase;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOCacheImplTest extends TestCase {

	static WADOCache cache = WADOCacheImpl.getWADOCache();

	public static void main(String[] args) {
		junit.textui.TestRunner.run(WADOCacheImplTest.class);
	}

	/*
	 * Class under test for File getIconFile(String, String, String)
	 * <p>
	 * Test: put a number of Icons to the cache and check if getIconFile 
	 * returns not null.<br>
	 * Tests also if getIconFile returns null for a non existing cache entry.
	 */
	public void testGetIconFileDefault() throws Exception {
		long l = System.currentTimeMillis();
		int iStudyMax = 3;
		int iSeriesMax = 3;
		int iInstancesMax = 3;
		for ( int iStudy = 0 ; iStudy < iStudyMax ; iStudy++ ) {
			for ( int iSeries = 0 ; iSeries < iSeriesMax ; iSeries++ ) {
				for ( int iInstances = 0 ; iInstances < iInstancesMax ; iInstances++ ) {
					cache.putImage(_getBI(),"study"+iStudy,
									   "series"+iSeries,
									   "instance"+iInstances);
				}
			}
		}

		for ( int iStudy = 0 ; iStudy < iStudyMax ; iStudy++ ) {
			for ( int iSeries = 0 ; iSeries < iSeriesMax ; iSeries++ ) {
				for ( int iInstances = 0 ; iInstances < iInstancesMax ; iInstances++ ) {
					File f = cache.getImageFile("study"+iStudy,"series"+iSeries,"instance"+iInstances);
					assertTrue("study,series,instance("+iStudy+","+iSeries+","+iInstances+" is not in cache", f != null);
				}
			}
		}
		

		File f = cache.getImageFile("study2not","series0","instance0");
		assertTrue("study2not,series0,instance0 is in cache", f == null);

		System.out.println("testGetIconFile: time needed:"+(System.currentTimeMillis()-l) );;
		
	}

	/*
	 * Class under test for void getIconFile( String, String, String, String, String)
	 * <p>
	 * Test: put a number of Icons to the cache and check if getIconFile 
	 * returns not null.<br>
	 * Tests also if getIconFile returns null for a non existing cache entry.
	 */
	public void testGetIconFileSpecialSize() throws Exception {
		long l = System.currentTimeMillis();
		cache.clearCache();
		int iStudyMax = 3;
		int iSeriesMax = 2;
		int iInstancesMax = 3;
		for ( int iStudy = 0 ; iStudy < iStudyMax ; iStudy++ ) {
			for ( int iSeries = 0 ; iSeries < iSeriesMax ; iSeries++ ) {
				for ( int iInstances = 0 ; iInstances < iInstancesMax ; iInstances++ ) {
					for ( int iRows = 100 ; iRows < 400 ; iRows+=50 ) {
						for ( int iColumn = iRows ; iColumn < iRows+70 ; iColumn+=20 ) {
							cache.putImage(_getBI(),"study"+iStudy,
											   "series"+iSeries,
											   "instance"+iInstances,
											   String.valueOf(iRows),
											   String.valueOf(iColumn) );
						}
					}
				}
			}
		}

		for ( int iStudy = 0 ; iStudy < iStudyMax ; iStudy++ ) {
			for ( int iSeries = 0 ; iSeries < iSeriesMax ; iSeries++ ) {
				for ( int iInstances = 0 ; iInstances < iInstancesMax ; iInstances++ ) {
					for ( int iRows = 100 ; iRows < 400 ; iRows+=50 ) {
						for ( int iColumn = iRows ; iColumn < iRows+70 ; iColumn+=20 ) {
							File f = cache.getImageFile("study"+iStudy,
														"series"+iSeries,
														"instance"+iInstances,
														String.valueOf(iRows),
														String.valueOf(iColumn));
							assertTrue("study,series,instance,row,column("+iStudy+","+iSeries+","+iInstances+","+iRows+","+iColumn+" is not in cache", f != null);
						}
					}
				}
			}
		}
		

		File f = cache.getImageFile("study2not","series0","instance0");
		assertTrue("study2not,series0,instance0 is in cache", f == null);

		System.out.println("testGetIconFile_Special: time needed:"+(System.currentTimeMillis()-l) );
	}

	public void testClearCache() {
		try {
			long l = System.currentTimeMillis();
			cache.putImage(_getBI(),"study0","series0","instance0");
			cache.putImage(_getBI(),"study0","series0","instance1");
			cache.putImage(_getBI(),"study0","series1","instance0");
			cache.putImage(_getBI(),"study0","series1","instance1");
			cache.putImage(_getBI(),"study1","series0","instance0");
			cache.clearCache();
			File f;
			f = cache.getImageFile("study0","series0","instance0");
			assertTrue("study0,series0,instance0 is in cache", f == null);
			f = cache.getImageFile("study0","series0","instance1");
			assertTrue("study0,series0,instance1 is in cache", f == null);
			System.out.println("testClearCache: time needed:"+(System.currentTimeMillis()-l) );;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testFreeDiskSpaceFG() {
		_prepareCleanTest();
		cache.freeDiskSpace(false);
		System.out.println("******************\nAfter clean:\nfree space:"+cache.showFreeSpace());
		System.out.println("minFreeSpace:"+cache.getMinFreeSpace()+ "  diff:"+(cache.showFreeSpace() - cache.getMinFreeSpace() ) );
		System.out.println("preferred:"+cache.getPreferredFreeSpace()+ "  diff:"+(cache.getPreferredFreeSpace() - cache.showFreeSpace() ) );
	}

	public void testFreeDiskSpaceBG() {
		_prepareCleanTest();
		cache.freeDiskSpace(true);
		System.out.println("******************\nAfter clean:\nfree space:"+cache.showFreeSpace());
		System.out.println("minFreeSpace:"+cache.getMinFreeSpace()+ "  diff:"+(cache.showFreeSpace() - cache.getMinFreeSpace() ) );
		System.out.println("preferred:"+cache.getPreferredFreeSpace()+ "  diff:"+(cache.getPreferredFreeSpace() - cache.showFreeSpace() ) );
	}
	
	/**
	 * 
	 */
	private void _prepareCleanTest() {
		int iStudyMax = 3; File studyDir;
		int iSeriesMax = 3; File seriesDir;
		int iInstancesMax = 3; File instanceFile;
		String dummy = _getDummyString();
		long l = 0;
		try {
			for ( int iStudy = 0 ; iStudy < iStudyMax ; iStudy++ ) {
				studyDir = new File ( ((WADOCacheImpl)cache).getAbsCacheRoot(), "study"+iStudy );
				if ( ! studyDir.exists() ) studyDir.mkdirs();
				for ( int iSeries = 0 ; iSeries < iSeriesMax ; iSeries++ ) {
					seriesDir = new File ( studyDir, "series"+iSeries );
					if ( ! seriesDir.exists() ) seriesDir.mkdirs();
					for ( int iInstances = 0 ; iInstances < iInstancesMax ; iInstances++ ) {
						instanceFile = new File( seriesDir, "instance"+iInstances );
						l += _writeFile( instanceFile, dummy );
					}
				}
			}
		} catch ( Exception x ) {
			x.printStackTrace();
		}
		long currFree = cache.showFreeSpace();
		( (WADOCacheImpl) cache).setMinFreeSpace( currFree + l / 10 );
		( (WADOCacheImpl) cache).setPreferredFreeSpace( currFree + l / 2 );
		
	}
	private static String _getDummyString() {
		char[] cha = new char[1000];
		for ( int i = 0 ; i < 1000; i++ ) {
			cha[i] = 'A';
		}
		return new String( cha );
	}
	
	private static long _writeFile(File file, String s) {
		long l = 0;
		try {
			if ( ! file.getParentFile().exists() ) file.getParentFile().mkdirs();
			int len = (int) (System.currentTimeMillis() & 0x03f);
			BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
			for ( int i = 0 ; i < len; i++ ) {
				bw.write( s );
			}
			l += len*1000;
			bw.close();
			file.setLastModified( System.currentTimeMillis() - System.currentTimeMillis()/4 );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}
	
	private BufferedImage bi = null;
	private BufferedImage _getBI() {
		if ( bi != null ) return bi;
		File file = new File("test/config/test.jpg");
		try {
			JPEGImageDecoder dec = JPEGCodec.createJPEGDecoder( new FileInputStream(file) );

			bi = dec.decodeAsBufferedImage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bi;
	}

}
