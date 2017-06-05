/*
 * Created on 06.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dcm4chex.wado.mbean.cache.FileToDelContainer;

import junit.framework.TestCase;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FileToDelContainerTest extends TestCase {

	public static final File rootPath = new File("/FileToDelContainerTest");
	
	private static String buffer;
	
	private static List tests;
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(FileToDelContainerTest.class);
	}

	/**
	 * Initialize this test.
	 * <p>
	 * Reads the file test/config/FileToDelTest.cfg and creates files of TEST config lines.<br>
	 * Create a List with all TEST config lines.
	 * 
	 */
	public void setUp() throws Exception {
		System.out.println("path:"+ new File("test").getAbsolutePath());
		File config = new File( "test/config/FileToDelTest.cfg" );
		tests = new ArrayList();
		BufferedReader br = new BufferedReader( new FileReader( config ) );
		String line = br.readLine();
		while ( line != null ) {
			if ( line.startsWith("CREATE")) {
				_writeFile( line );
			} else if ( line.startsWith("TEST")) {
				tests.add( line );
			}
			line = br.readLine();
		}
	}

	/**
	 * Writes a file according to a configuration line.
	 * <p>
	 * <DL><DT>The config line:</DT>
	 * <DD>CREATE|filename|size|lastModified</DD>
	 * <DD></DD>
	 * <DD>filename.....The name of the file.</DD>
	 * <DD>size.........The file size</DD>
	 * <DD>lastModified.The last modified timestamp (dd.MM.yyyy HH:mm:ss)</DD>
	 * </DL>
	 * 
	 * @param line	a CREATE configuration line.
	 */
	private static void _writeFile(String line) {
		StringTokenizer st = new StringTokenizer( line, "|" );
		if ( st.countTokens() != 4 ) {
			System.out.println("Wrong line in config file!");
		}
		st.nextToken();//remove CREATE
		try {
		File f = new File( rootPath, st.nextToken() );
			if ( ! f.getParentFile().exists() ) f.getParentFile().mkdirs();
			int len = Integer.parseInt( st.nextToken() );
			Date date = formatter.parse( st.nextToken() );
			BufferedWriter bw = new BufferedWriter( new FileWriter( f ) );
			String dummy = _getDummyString();
			for ( int i = 0 ; i < len; i++ ) {
				bw.write( dummy );
			}
			bw.close();
			f.setLastModified( date.getTime() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Returns a fixed length (1000) String.
	 * 
	 * @return A String with 100 'A' characters.
	 */
	private static String _getDummyString() {
		if ( buffer == null )  {
			char[] cha = new char[1000];
			for ( int i = 0 ; i < 1000; i++ ) {
				cha[i] = 'A';
			}
			buffer = new String( cha );
		}
		return buffer;
	}

	/**
	 * Performes all tests configured by test/config/FileToDelTest.cfg file.
	 *
	 */
	public void testGetFilesToDelete() {
		if ( tests == null ) return;
		Iterator iter = tests.iterator();
		while ( iter.hasNext() ) {
			_doTest( (String) iter.next() );
		}
		WADOCacheImpl.delTree( rootPath );	
		rootPath.delete();
	}

	/**
	 * <DL><DT>The config line:</DT>
	 * <DD>TEST|desc|makeFree|toDel</DD>
	 * <DD></DD>
	 * <DD>desc.......Description</DD>
	 * <DD>makeFree...Size that should be freed</DD>
	 * <DD>toDel......String with filenames that should be marked for deletion.</DD>
	 * </DL>
	 * 
	 * @param line A TEST configuration line. 
	 */
	private void _doTest(String line) {
		StringTokenizer st = new StringTokenizer( line, "|" );
		st.nextToken();//remove TEST
		String desc = st.nextToken();
		long makeFree = Long.parseLong( st.nextToken() )*_getDummyString().length();
		String toDel = st.nextToken().trim()+",";
		FileToDelContainer c = new FileToDelContainer( rootPath, makeFree );
		Iterator iter = c.getFilesToDelete().iterator();
		StringBuffer sb = new StringBuffer();
		while ( iter.hasNext() ) {
			sb.append(((File)iter.next()).getName()).append(",");
		}
		assertEquals(desc, toDel,sb.toString());
	}


}
