/*$Id: DcmImageReaderConf.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.imageio.plugins;

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DcmImageReaderConf {
    
    public static DcmImageReaderConf getInstance() {
        return instance;
    }
    
    private static DcmImageReaderConf instance = new DcmImageReaderConf();
    private final ClassLoader classloader;
    private String[] extraStreamMetadataFormatNames = null;
    private List formatNameList = Collections.EMPTY_LIST;
    private String[] extraStreamMetadataFormatClassNames = null;
    private String[] extraStreamMetadataFormatFilterResource = null;
    private String[] extraStreamMetadataFormatStyleResource = null;
    private Dataset[] datasetFilter = null;
    private Templates[] transformerTemplates = null;

    /** Creates a new instance of DcmImageReaderConf */
    private DcmImageReaderConf() {        
        classloader = Thread.currentThread().getContextClassLoader();
        String conf = System.getProperty(
                "dcm4cheri.imageio.plugins.DcmImageReader.config",
                "DcmImageReader.properties");

        InputStream in = classloader.getResourceAsStream(conf);
        if (in == null) {
            return;
        }
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
        StringTokenizer tk = new StringTokenizer(
                p.getProperty("extraStreamMetadataFormatNames",""),
                " ,\t");
        int n = tk.countTokens();
        if (n == 0) {
            return;
        }
        extraStreamMetadataFormatNames = new String[n];
        extraStreamMetadataFormatClassNames = new String[n];
        extraStreamMetadataFormatFilterResource = new String[n];
        extraStreamMetadataFormatStyleResource = new String[n];
        for (int i = 0; i < n; ++i) {
            String name = tk.nextToken();
            extraStreamMetadataFormatNames[i] = name;
            extraStreamMetadataFormatClassNames[i] =
                    p.getProperty(name + ".class");
            extraStreamMetadataFormatFilterResource[i] =
                    p.getProperty(name + ".filter");
            extraStreamMetadataFormatStyleResource[i] =
                    p.getProperty(name + ".style");
        }
        formatNameList = Arrays.asList(extraStreamMetadataFormatNames);
        datasetFilter = new Dataset[n];
        transformerTemplates = new Templates[n];
    }

    public String[] getExtraStreamMetadataFormatNames() {
        return extraStreamMetadataFormatNames;
    }

    public String[] getExtraStreamMetadataFormatClassNames() {
        return extraStreamMetadataFormatClassNames;
    }

    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
    
    public boolean contains(String formatName) {
        return formatNameList.indexOf(formatName) != -1;
    }

    public Dataset getFilterDataset(String formatName) {
        int index = formatNameList.indexOf(formatName);
        if (index == -1
                || extraStreamMetadataFormatFilterResource[index] == null) {
            return null;
        }
        if (datasetFilter[index] != null) {
            return datasetFilter[index];
        }
        InputStream in = classloader.getResourceAsStream(
                extraStreamMetadataFormatFilterResource[index]);
        if (in == null) {
            throw new ConfigurationError("Could not open resource "
                    + extraStreamMetadataFormatFilterResource[index], null);
        }
        try {
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            p.parse(in, ds.getSAXHandler());
            return (datasetFilter[index] = ds);
        } catch (Exception ex) {
            throw new ConfigurationError("Could not parse resource "
                    + extraStreamMetadataFormatFilterResource[index], ex);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
    }

    public TransformerHandler getTransformerHandler(String formatName) {
        int index = formatNameList.indexOf(formatName);
        if (index == -1
                || extraStreamMetadataFormatStyleResource[index] == null) {
            return null;
        }
        try {
            SAXTransformerFactory tf =
                    (SAXTransformerFactory)TransformerFactory.newInstance();        
            if (transformerTemplates[index] == null) {
                InputStream in = classloader.getResourceAsStream(
                        extraStreamMetadataFormatStyleResource[index]);
                if (in == null) {
                    throw new ConfigurationError("Could not open resource "
                            + extraStreamMetadataFormatStyleResource[index], null);
                }
                try {
                    transformerTemplates[index] =
                        tf.newTemplates( new StreamSource(in));
                } finally {
                    try { in.close(); } catch (IOException ignore) {}
                }
            }
            return tf.newTransformerHandler(transformerTemplates[index]);
        } catch (Exception ex) {
            throw new ConfigurationError("Could not parse resource "
                    + extraStreamMetadataFormatStyleResource[index], ex);
        }
    }
}
