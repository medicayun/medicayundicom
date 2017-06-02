/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.TagDictionary;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version $Revision: 3857 $ $Date: 2005-01-31 06:10:25 +0800 (周一, 31 1月 2005) $
 */
public class Dcm2Xml {

    private static final DcmParserFactory pfact = DcmParserFactory
            .getInstance();

    private OutputStream out = System.out;

    private URL xslt = null;

    private LinkedList xsltParams = new LinkedList();

    private boolean xsltInc = false;

    private int[] excludeTags = {};
    
    private int excludeValueLengthLimit = Integer.MAX_VALUE;

    private TagDictionary dict = DictionaryFactory.getInstance()
            .getDefaultTagDictionary();

    private File baseDir;

    /** Creates a new instance of Dcm2xml */
    public Dcm2Xml() {
    }

    public final void setExcludeValueLengthLimit(int excludeValueLengthLimit) {
        this.excludeValueLengthLimit = excludeValueLengthLimit;
    }
    
    public final void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setTagDictionary(TagDictionary dict) {
        this.dict = dict;
    }

    public void setXslt(URL xslt) {
        this.xslt = xslt;
    }

    public void setXsltInc(boolean xsltInc) {
        this.xsltInc = xsltInc;
    }

    public void addXsltParam(String expr) {
        if (expr.indexOf('=') <= 0) { throw new IllegalArgumentException(expr); }
        this.xsltParams.add(expr);
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        TransformerHandler th = null;
        if (xslt != null) {
            if (xsltInc) {
                tf.setAttribute("http://xml.apache.org/xalan/features/incremental",
                                Boolean.TRUE);
            }
            th = tf.newTransformerHandler(new StreamSource(xslt.openStream(),
                    xslt.toExternalForm()));
            Transformer t = th.getTransformer();
            for (Iterator it = xsltParams.iterator(); it.hasNext();) {
                String s = (String) it.next();
                int eqPos = s.indexOf('=');
                t.setParameter(s.substring(0, eqPos), s.substring(eqPos + 1));
            }
        } else {
            th = tf.newTransformerHandler();
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        }
        th.setResult(new StreamResult(out));
        return th;
    }

    public void process(String file) throws IOException,
            TransformerConfigurationException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(file)));
        try {
            DcmParser parser = pfact.newDcmParser(in);
            parser.setSAXHandler2(getTransformerHandler(),
                    dict,
                    excludeTags,
                    excludeValueLengthLimit,
                    baseDir);
            parser.parseDcmFile(null, -1);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {
            }
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("TXT", LongOpt.NO_ARGUMENT, null, 'T');
        longopts[1] = new LongOpt("XSL", LongOpt.REQUIRED_ARGUMENT, null, 'X');

        Getopt g = new Getopt("dcm2xml.jar", args, "bo:ID:Xx:L:d:", longopts,
                true);

        Dcm2Xml dcm2xml = new Dcm2Xml();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'b':
                dcm2xml.setTagDictionary(null);
                break;
            case 'o':
                dcm2xml.setOut(new BufferedOutputStream(new FileOutputStream(g
                        .getOptarg())));
                break;
            case 'T':
                dcm2xml.setXslt(Dcm2Xml.class.getResource("/Dcm2Xml2.xsl"));
                break;
            case 'I':
                dcm2xml.setXsltInc(true);
                break;
            case 'D':
                dcm2xml.addXsltParam(g.getOptarg());
                break;
            case 'x':
                dcm2xml.addExcludeTag(toTag(g.getOptarg()));
                break;
            case 'X':
                dcm2xml.addExcludeTag(Tags.PixelData);
                break;
            case 'L':
                dcm2xml.setExcludeValueLengthLimit(Integer.parseInt(g.getOptarg()));
                break;
            case 'd':
                dcm2xml.setBaseDir(new File(g.getOptarg()));
                break;
            case '?':
                exit("");
                break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc == 0) {
            exit("dcm2xml.jar: Missing argument\n");
        }

        if (argc > 1) {
            exit("dcm2xml.jar: To many arguments\n");
        }
        dcm2xml.process(args[optind]);
    }

    private void addExcludeTag(int tag) {
        int[] tmp = new int[excludeTags.length + 1];
        System.arraycopy(excludeTags, 0, tmp, 0, excludeTags.length);
        tmp[excludeTags.length] = tag;
        excludeTags = tmp;
    }

    private static int toTag(String s) {
        try {
            return (int) Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return Tags.forName(s);
        }
    }

    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE = 
              "Usage: java -jar dcm2xml.jar <dcm_file> [-o <xml_file>]\n"
            + "  [-bX] [-x <tag> [,...]] [-L <maxValLen>] [-d <basedir>]\n"
            + "  [[--TXT | --XSL <xsl_file>] [-I][-D<param>=<value> [,...]]]\n\n"
            + "Transform the specified DICOM file <dcm_file> into XML and optionally apply\n"
            + "XSLT with the specified XSL stylesheet <xsl_file> to the XML presentation.\n\n"
            + "Options:\n"
            + " -o <xml_file>      Place output in <xml_file> instead in standard output.\n"
            + " -b                 Brief format: exclude attribute names from XML output.\n"
            + " -X                 Exclude pixel data from XML output. Same as -xPixelData\n"
            + " -x <tag>           Exclude value of specified tag from XML output.\n"
            + "                    Format: ggggeeee or attribute name\n"
            + " -L <maxValLen>     Exclude values which length exceeds the specified limit\n"
            + "                    from XML output.\n"
            + " -d <basedir>       file excluded values into directory <basedir>.\n"
            + " -T, --TXT          Apply default XSLT to produce text output:\n"
            + "                     -Dmaxlen=<maximal line length> [79]\n"
            + "                     -Dvallen=<displayed value length> [64]\n"
            + "                     -Dvaltail=<truncation position from value tail>. [8]\n"
            + "                     -Dellipsis=<truncation mark>. ['...']\n"
            + " --XSL <xsl_file>   Apply XSLT with specified XSL stylesheet <xsl_file>.\n"
            + " -I                 Enable incremental XSLT (only usable with XALAN)\n"
            + " -D<param>=<value>  Set XSL parameter to specified value.\n";
}