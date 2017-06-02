/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.Dataset;

import org.xml.sax.SAXException;
import java.io.*;
import javax.xml.parsers.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version $Revision: 3798 $ $Date: 2004-09-10 03:48:02 +0800 (周五, 10 9月 2004) $
 */
public class Xml2Dcm {

    private Dataset ds = DcmObjectFactory.getInstance().newDataset();

    private File baseDir = new File(".");

    /** Creates a new instance of Xml2Dcm */
    public Xml2Dcm() {
    }

    public final void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void read(DataInputStream in) throws IOException, DcmValueException {
        ds.clear();
        try {
            ds.readFile(in, null, -1);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void process(String xml_file, DataOutputStream out)
            throws IOException, DcmValueException,
            ParserConfigurationException, SAXException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p = f.newSAXParser();
            p.parse(new File(xml_file), ds.getSAXHandler2(baseDir));
            ds.writeFile(out, null);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        Getopt g = new Getopt("xml2dcm.jar", args, "2i:d:");

        Xml2Dcm xml2dcm = new Xml2Dcm();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'd':
                xml2dcm.setBaseDir(new File(g.getOptarg()));
                break;
            case 'i':
                xml2dcm.read(new DataInputStream(new BufferedInputStream(
                        new FileInputStream(g.getOptarg()))));
                break;
            case '?':
                exit("");
                break;
            }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc < 2) {
            exit("xml2dcm.jar: Missing argument\n");
        }

        if (argc > 2) {
            exit("xml2dcm.jar: To many arguments\n");
        }
        xml2dcm.process(args[optind],
                new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(args[optind + 1]))));
    }

    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE = "Usage:\n\n"
            + " java -jar xml2dcm.jar [-i <dcm_file>] [-d <base_di>] <xml_file> <dcm_file>\n\n"
            + "Create or update DICOM file <dcm_file> according XML specification <xml_file>.\n\n"
            + "Options:\n"
            + " -i <dcm_file>  Update specified DICOM file but store it as new one.\n"
            + " -d <base_dir>  Specifies directory where referenced source files are located\n"
            + "                Default: current working directory.\n";
}