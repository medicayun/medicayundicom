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

package org.dcm4chex.wado.mbean.cache;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.config.DeleterThresholds;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author franz.willer
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class WADOCacheImpl implements WADOCache {

    public static final String DEFAULT_CACHE_ROOT = "/wadocache";

    public static final String DEFAULT_WADO_SUBDIR = "default";

    public static final String DEFAULT_RID_SUBDIR = "rid";

    public static final String DEFAULT_WADO_EXT_SUBDIR = "wfind";
    
    public static final String DEFAULT_IMAGE_QUALITY = "75";

    /** Buffer size for read/write */
    private static final int BUF_LEN = 65536;
    
    public static final String NEWLINE = System.getProperty("line.separator",
            "\n");

    private static final int[] PRIMES = new int[] { 3, 5, 7, 13, 23, 37, 47,
            59, 71, 83, 97, 107, 127, 137, 149, 163, 173, 191, 211, 223, 233,
            251, 263, 277, 293, 307, 317, 331, 347, 359, 373, 383, 397, 409,
            419, 431, 443, 457, 467, 479, 491, 503, 521, 541, 557, 569, 587,
            599, 613, 631, 641, 653, 673, 683, 701, 719, 733, 743, 757, 769,
            787, 797, 809, 821, 839, 853, 863, 877, 887, 907, 919, 929, 941,
            953, 967, 977, 991, 1009, 1019, 1031, 1049, 1061, 1087, 1097, 1109,
            1123 };

    private static WADOCacheImpl singletonWADO = null;

    private static WADOCacheImpl singletonRID = null;

    private static WADOCacheImpl singletonWADOExt = null;

    private static Logger log = Logger.getLogger(WADOCacheImpl.class.getName());

    /** the root folder where this cache stores the image files. */
    private File cacheRoot = null;

    /** The config string that is used to set cacheRoot. */
    private String cacheRootCfgString = null;

    /**
     * Default subdirectory. this is used to split directory structer for WADO
     * and RID cache and for WADO cache to split 'default' requests and requests
     * with rows and columns.
     */
    private String defaultSubdir = "default";

    /** holds the min cache size. */
    private long preferredFreeSpace = 300000000;

    private DeleterThresholds deleterThresholds = new DeleterThresholds(
            "23:50MB", true);

    private int[] directoryTree;

    /**
     * Flag to indicate if client side redirection should be used if DICOM
     * object is not locally available.
     */
    private boolean clientRedirect = false;

    /**
     * Flag to indicate if caching should be used in case of server side
     * redirect,
     */
    private boolean redirectCaching = true;

    private String imageQuality = DEFAULT_IMAGE_QUALITY;
    
    public final String getImageQuality() {
        return imageQuality;
    }

    public final void setImageQuality(String imageQuality) {
        int intval = Integer.parseInt( imageQuality );
        if (intval <= 0 || intval > 100) {
            throw new IllegalArgumentException("imageQuality: "
                    + imageQuality + " not between 1 and 100.");
        }
        this.imageQuality = imageQuality;
    }
        
    /**
     * Creates a WADOCacheImpl instance..
     */
    private WADOCacheImpl() {

    }

    /**
     * Returns the singleton instance of WADOCache.
     * 
     * @return
     */
    public static WADOCache getWADOCache() {
        if (singletonWADO == null) {
            singletonWADO = new WADOCacheImpl();
            singletonWADO.defaultSubdir = DEFAULT_WADO_SUBDIR;
        }
        return singletonWADO;
    }

    /**
     * Returns the singleton instance of WADOCache.
     * 
     * @return
     */
    public static WADOCache getRIDCache() {
        if (singletonRID == null) {
            singletonRID = new WADOCacheImpl();
            singletonRID.defaultSubdir = DEFAULT_RID_SUBDIR;
        }
        return singletonRID;
    }

    /**
     * Returns the singleton instance of WADOCache.
     * 
     * @return
     */
    public static WADOCache getWADOExtCache() {
        if (singletonWADOExt == null) {
            singletonWADOExt = new WADOCacheImpl();
            singletonWADOExt.defaultSubdir = DEFAULT_WADO_EXT_SUBDIR;
        }
        return singletonWADOExt;
    }

    /**
     * Get an image as BufferedImage from cache.
     * <p>
     * This method returns the image from the default path of this cache.<br>
     * 
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * @param rows
     *            Image height in pixel.
     * @param columns
     *            Image width in pixel.
     * @param region
     *            Image region defined by two points in opposing corners
     * @param windowWidth
     *            Decimal string representing the contrast of the image.
     * @param windowCenter
     *            Decimal string representing the luminosity of the image.
     * @param imageQuality
     *            Integer string (1-100) representing required quality of
     *            the image to be returned within the range 1 to 100
     * 
     * @return The image if in cache or null.
     */
    public BufferedImage getImage(String studyUID, String seriesUID,
            String instanceUID, String rows, String columns, String region,
            String windowWidth, String windowCenter, String imageQuality,
            String suffix) {
        return _readJpgFile(getImageFile(studyUID, seriesUID, instanceUID,
                rows, columns, region, windowWidth, windowCenter, imageQuality,
                suffix));
    }

    /**
     * Get a region of an image of special size from cache.
     * <p>
     * This method use a image size (rows and columns) and a region (two points)
     * to search on a special path of this cache.
     * 
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * @param rows
     *            Image height in pixel.
     * @param columns
     *            Image width in pixel.
     * @param region
     *            Image region defined by two points in opposing corners
     * @param windowWidth
     *            Decimal string representing the contrast of the image.
     * @param windowCenter
     *            Decimal string representing the luminosity of the image.
     * @param imageQuality
     *            Integer string (1-100) representing required quality of
     *            the image to be returned within the range 1 to 100
     * 
     * @return The File object of the image if in cache or null.
     */
    public File getImageFile(String studyUID, String seriesUID,
            String instanceUID, String rows, String columns, String region,
            String windowWidth, String windowCenter, String imageQuality,
            String suffix) {
        File file = this._getImageFile(rows + "-" + columns + "-" + region
                + "-" + windowWidth + "-" + windowCenter + "-" 
                + maskNull(imageQuality, this.imageQuality),
                studyUID, seriesUID, instanceUID, suffix, null);
        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis()); // set last
                                                                // modified
                                                                // because File
                                                                // has only
                                                                // lastModified
                                                                // timestamp
                                                                // visible.
            return file;
        } else {
            return null;
        }
    }

    private static String maskNull(String val, String defval) {
        return val != null ? val : defval;
    }

    /**
     * Put a region of an image of special size to this cache.
     * <p>
     * Stores the image on a special path of this cache.
     * 
     * @param image
     *            The image (with special size)
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * @param rows
     *            Image height in pixel.
     * @param columns
     *            Image width in pixel.
     * @param region
     *            Image region defined by two points in opposing corners
     * @param windowWidth
     *            Decimal string representing the contrast of the image.
     * @param windowCenter
     *            Decimal string representing the luminosity of the image.
     * @param imageQuality
     *            Integer string (1-100) representing required quality of
     *            the image to be returned within the range 1 to 100
     * 
     * @return The File object of the image in this cache.
     * @throws IOException
     */
    public File putImage(BufferedImage image, String studyUID,
            String seriesUID, String instanceUID, String rows, String columns,
            String region, String windowWidth, String windowCenter,
            String imageQuality, String suffix) throws IOException {
        imageQuality = maskNull(imageQuality, this.imageQuality);
        File file = this._getImageFile(rows + "-" + columns + "-" + region
                + "-" + windowWidth + "-" + windowCenter + "-" + imageQuality,
                studyUID, seriesUID, instanceUID, suffix, null);
        _writeImageFile(image, file, imageQuality);
        return file;
    }

    /**
     * Puts a stream to this cache.
     * 
     * @param stream
     *            The InputStream to store.
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * @param rows
     *            Image height in pixel.
     * @param columns
     *            Image width in pixel.
     * @param region
     *            Rectangular region of the image (defined by two points)
     * @param windowWidth
     *            Decimal string representing the contrast of the image.
     * @param windowCenter
     *            Decimal string representing the luminosity of the image.
     * @param imageQuality
     *            Integer string (1-100) representing required quality of
     *            the image to be returned within the range 1 to 100
     * 
     * @return The stored File object.
     * 
     * @throws IOException
     */
    public File putStream(InputStream stream, String studyUID,
            String seriesUID, String instanceUID, String rows, String columns,
            String region, String windowWidth, String windowCenter,
            String imageQuality, String suffix) throws IOException {
        File file;

        file = this._getImageFile(rows + "-" + columns + "-" + region + "-"
                + windowWidth + "-" + windowCenter + "-" 
                + maskNull(imageQuality, imageQuality),
                studyUID, seriesUID, instanceUID, suffix, null);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        byte[] buf = new byte[BUF_LEN];
        try {
            int len = stream.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = stream.read(buf);
            }
        } finally {
            stream.close();
            out.flush();
            out.close();
        }
        return file;
    }

    /**
     * Return the File object to get or store a file for given arguments.
     * <p>
     * If the cache object referenced with arguments is'nt in this cache the
     * returned file object exists() method will result false!
     * 
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * 
     * @return File object to get or store a file.
     */
    public File getFileObject(String studyUID, String seriesUID,
            String instanceUID, String contentType) {
        return this._getImageFile(defaultSubdir, studyUID, seriesUID,
                instanceUID, null, contentType);
    }

    /**
     * Clears this cache.
     * <p>
     * Remove all images in this cache.
     */
    public void clearCache() {
        log.info("Clear cache called: cacheRoot:" + getAbsCacheRoot());
        if (this == singletonWADO) {
            log.info("Clear WADO cache!");
            File[] files = getAbsCacheRoot().listFiles();
            if (files == null) {
                log
                        .warn("WADO cache not cleared! Reason: cache root is not a directory or cant be read.");
                return;
            }
            for (int i = 0, len = files.length; i < len; i++) {
                if (!files[i].getName().equals(DEFAULT_RID_SUBDIR)) {
                    delTree(files[i]);
                    if (!files[i].getName().equals(DEFAULT_WADO_SUBDIR)) {// dont
                                                                            // del
                                                                            // default
                                                                            // dir
                        files[i].delete();
                    }
                }
            }
        } else if (this == singletonWADOExt) {
            log.info("Clear WADOExt cache!");
            delTree(new File(this.getAbsCacheRoot(), DEFAULT_WADO_EXT_SUBDIR));
        } else {
            log.info("Clear RID cache!");
            delTree(new File(this.getAbsCacheRoot(), DEFAULT_RID_SUBDIR));
        }
    }

    /**
     * Deletes all files in given directory.
     * <p>
     * Do nothing if <code>file</code> is not a directory or is
     * <code>null</code>!
     * 
     * @param file
     *            A directory.
     */
    public static void delTree(File file) {
        if (file == null)
            return;
        if (!file.isDirectory())
            return;

        File[] files = file.listFiles();
        if (files == null) {
            log.warn("WADOCache: File " + file
                    + " should be a directory! But listFiles returns null!");
            return;
        }
        for (int i = 0, len = files.length; i < len; i++) {
            if (files[i].isDirectory()) {
                delTree(files[i]);
                if (!files[i].delete()) {
                    log
                            .warn("WADOCache: Diretory can't be deleted:"
                                    + files[i]);
                }
            } else {
                if (!files[i].delete()) {
                    log.warn("WADOCache: File can't be deleted:" + files[i]);
                }
            }
        }
    }

    /**
     * Removes old entries of this chache to free disk space.
     * <p>
     * This method can be called to run on the same thread ( e.g. if started via
     * JMX console) or in a seperate thread (if clean is handled automatically
     * by WADOCache).
     * <DL>
     * <DD> 1) check if clean is necessary:
     * <code>showFreeSpace &lt; getMinFreeSpace </code></DD>
     * <DD> 2) delete the oldest files until
     * <code>showFreeSpace &gt; getPreferredFreeSpace</code></DD>
     * </DL>
     * 
     * @param background
     *            If true, clean runs in a seperate thread.
     * @throws IOException
     */
    public void freeDiskSpace(boolean background) throws IOException {
        long currFree = showFreeSpace();
        if (log.isDebugEnabled())
            log.debug("WADOCache.freeDiskSpace: free:" + currFree
                    + " minFreeSpace:" + getMinFreeSpace());
        long minFree = getMinFreeSpace();
        if (currFree < minFree) {
            long pref = getPreferredFreeSpace();
            final long sizeToDel = (pref > minFree ? pref : minFree) - currFree;
            if (background) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        _clean(sizeToDel);
                    }
                });
                t.start();
            } else {
                _clean(sizeToDel);
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("WADOCache.freeDiskSpace: nothing todo");
        }
    }

    /**
     * Deletes old files to free the given amount of disk space.
     * <p>
     * If a directory is empty after deleting a file, the directory will also be
     * deleted.
     */
    private void _clean(long sizeToDel) {
        log.info("Free disk space for "
                + (this == singletonWADO ? "WADO" : "RID") + " cache!");
        FileToDelContainer ftd = new FileToDelContainer(new File(
                getAbsCacheRoot(), defaultSubdir), sizeToDel);
        if (this == singletonWADO) {
            File[] files = getAbsCacheRoot().listFiles();
            if (files != null) {
                for (int i = 0, len = files.length; i < len; i++) {
                    if (!files[i].getName().equals(DEFAULT_RID_SUBDIR)
                            && !files[i].getName().equals(DEFAULT_WADO_SUBDIR)) {
                        ftd.searchDirectory(new File(this.getAbsCacheRoot(),
                                files[i].getName()));
                    }
                }
            }
        }
        Iterator iter = ftd.getFilesToDelete().iterator();
        File file;
        while (iter.hasNext()) {
            file = (File) iter.next();
            file.delete();
            _deleteDirWhenEmpty(file.getParentFile());
        }
    }

    /**
     * Deletes the given directory and all parents if they are empty.
     * 
     * @param dir
     *            Directory
     */
    private void _deleteDirWhenEmpty(File dir) {
        if (dir.equals(getAbsCacheRoot()))
            return;
        if (dir == null)
            return;
        File[] files = dir.listFiles();
        if (files != null && files.length == 0) {
            dir.delete();
            _deleteDirWhenEmpty(dir.getParentFile());
        }
    }

    public String getDeleterThresholds() {
        return deleterThresholds.toString();
    }

    public void setDeleterThresholds(String s) {
        this.deleterThresholds = new DeleterThresholds(s, false);// does not
                                                                    // support
                                                                    // time
                                                                    // based
                                                                    // tresholds
    }

    public long getMinFreeSpace() {
        return deleterThresholds.getDeleterThreshold(Calendar.getInstance())
                .getFreeSize(0l);
    }

    /**
     * Returns the free space that should be remain after cleaning the cache.
     * <p>
     * This value is used as lower watermark of the cleaning process.
     * 
     * @return Preferred free diskspace in bytes.
     */
    public long getPreferredFreeSpace() {
        return preferredFreeSpace;
    }

    /**
     * Setter of preferredFreeSpace.
     * 
     * @param preferredFreeSpace
     *            The preferredFreeSpace to set.
     */
    public void setPreferredFreeSpace(long preferredFreeSpace) {
        this.preferredFreeSpace = preferredFreeSpace;
    }

    /**
     * Returns current free disk space in bytes.
     * 
     * @return disk space available on the drive where this cache is stored.
     * @throws IOException
     */
    public long showFreeSpace() throws IOException {
        File dir = getAbsCacheRoot();
        long free = FileSystemUtils.freeSpace(dir.getPath());
        log.info("getFreeDiskSpace from :" + dir + " free:" + free);
        return free;
    }

    /**
     * Returns the root directory of this cache.
     * <p>
     * Returns the absolute or relative path as set with setCacheRoot.
     * 
     * @return root directory of this cache (absolute or relative).
     */
    public String getCacheRoot() {
        if (cacheRootCfgString == null) {
            setCacheRoot(DEFAULT_CACHE_ROOT);
        }
        return cacheRootCfgString;
    }

    public File getAbsCacheRoot() {
        if (cacheRoot == null)
            setCacheRoot(DEFAULT_CACHE_ROOT);
        return cacheRoot;
    }

    public void setCacheRoot(String newRoot) {
        if (newRoot == null)
            return;
        cacheRootCfgString = newRoot;
        File newRootFile = new File(newRoot);
        if (!newRootFile.isAbsolute()) {
            try {
                File serverHomeDir = ServerConfigLocator.locate()
                        .getServerHomeDir();
                newRootFile = new File(serverHomeDir, newRoot);
            } catch (Throwable t) {
            }
        }
        if (cacheRoot != null) {
            try {
                cacheRoot.renameTo(newRootFile); // move only possible if
                                                    // same partition.
            } catch (Throwable t) {
                // TODO copy cache to new dest?
                delTree(cacheRoot);
                cacheRoot.delete();
            }
        }
        cacheRoot = newRootFile;

        if (!cacheRoot.exists())
            new File(cacheRoot, defaultSubdir).mkdirs();

    }

    /**
     * @return Returns the clientRedirect.
     */
    public boolean isClientRedirect() {
        return clientRedirect;
    }

    /**
     * @param clientRedirect
     *            The clientRedirect to set.
     */
    public void setClientRedirect(boolean clientRedirect) {
        this.clientRedirect = clientRedirect;
    }

    /**
     * @return Returns the redirectCaching.
     */
    public boolean isRedirectCaching() {
        return redirectCaching;
    }

    /**
     * @param redirectCaching
     *            The redirectCaching to set.
     */
    public void setRedirectCaching(boolean redirectCaching) {
        this.redirectCaching = redirectCaching;
    }

    /**
     * Returns the File object references the file where the image is placed
     * within this cache.
     * <p>
     * The subdir argument is used to seperate default images and special sized
     * images.
     * <p>
     * The directory and file names for studyID, seriesID and instaneID are
     * calculated with <code>_getSubDirName</code>
     * 
     * <DL>
     * <DT>The File object was build like:</DT>
     * <DD>
     * &lt;root&gt;/[&lt;subdir&gt;/&lt;]studyID&gt;/&lt;seriesID&gt;/&lt;instanceID&gt;</DD>
     * </DL>
     * 
     * @param subdir
     *            The subdirectory
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * @param contentType
     *            TODO
     * 
     * @return
     */
    private File _getImageFile(String subdir, String studyUID,
            String seriesUID, String instanceUID, String suffix,
            String contentType) {
        if (contentType == null)
            contentType = "image/jpg";// use jpg instead of jpeg here because
                                        // for extension jpeg is set to jpg.
        File file = getAbsCacheRoot();
        if (subdir != null)
            file = new File(this.getAbsCacheRoot(), subdir);
        String ext = getFileExtension(contentType);
        if (ext.length() < 1)
            file = new File(file, contentType.replace('/', '_'));
        if (directoryTree == null) {
            if (studyUID != null)
                file = new File(file, _getSubDirName(studyUID));
            if (seriesUID != null)
                file = new File(file, _getSubDirName(seriesUID));
        } else {
            file = new File(file, _getSubDirName(instanceUID));
        }
        if (suffix != null)
            instanceUID += suffix;
        file = new File(file, instanceUID + ext);
        return file;
    }

    /**
     * @param contentType
     * @return
     */
    private String getFileExtension(String contentType) {
        int pos = contentType.indexOf("/");
        String ext = "";
        if (pos != -1) {
            ext = contentType.substring(pos + 1);
            if (ext.equalsIgnoreCase("jpeg"))
                ext = "jpg";
            else if (ext.equalsIgnoreCase("svg+xml"))
                ext = "svg";
            // do some other mapping here;
            ext = "." + ext;
        }
        return ext.toLowerCase();
    }

    /**
     * Returns the directory name for given UID. <p/> This implementation use
     * directoryTree and the uid hashvalue to build a subdirectory(ies). <p/> If
     * directoryTree is null, following file path is used:
     * &lt;StudyIUID&gt;/&lt;SeriesIUID&gt;/&lt;instanceIUID&gt.</dd>
     * The size of directoryTree specify the depth of the tree.<br/> The values
     * in directoryTree specify the max. number of subdirectories in the
     * corresponding directory.<br/> Therefore prime values should be used to
     * achieve an evenly distributed cache.
     * 
     * @param uid
     *            A unique DICOM identifier
     * 
     * @return directory name.
     */
    private String _getSubDirName(String uid) {
        if (directoryTree == null)
            return uid;
        int hash = uid.hashCode();
        StringBuffer sb = new StringBuffer();
        int modulo;
        for (int i = 0; i < directoryTree.length; i++) {
            if (directoryTree[i] == 0) {
                sb.append(Integer.toHexString(hash)).append(File.separatorChar);
            } else {
                modulo = hash % directoryTree[i];
                if (modulo < 0) {
                    modulo *= -1;
                }
                sb.append(modulo).append(File.separatorChar);
            }
        }
        return sb.toString();
    }

    /**
     * @return Returns the directoryTree.
     */
    public String getDirectoryTree() {
        if (directoryTree == null)
            return "NONE";
        StringBuffer sb = new StringBuffer();
        sb.append(directoryTree[0]);
        for (int i = 1; i < directoryTree.length; i++) {
            sb.append('/').append(directoryTree[i]);
        }
        return sb.toString();
    }

    /**
     * @param directoryTree
     *            The directoryTree to set.
     */
    public void setDirectoryTree(String primes) {
        if ("NONE".equals(primes)) {
            this.directoryTree = null;
        } else {
            StringTokenizer st = new StringTokenizer(primes, "/");
            directoryTree = new int[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                directoryTree[i] = Integer.parseInt(st.nextToken());
            }
        }
    }

    public void computeDirectoryStructure(long cacheSize, long fileSize,
            int maxSubDirPerDir) {
        if (maxSubDirPerDir < 0) {
            directoryTree = null;
            return;
        }
        long nrOfFiles = cacheSize / fileSize;
        int dirDepth = 0;
        for (long l = maxSubDirPerDir; l < nrOfFiles; l *= maxSubDirPerDir, dirDepth++)
            ;
        this.directoryTree = new int[dirDepth];
        int idx = 0, subIdx = -1, fileIdx = -1;
        for (; idx < PRIMES.length; ++idx) {
            if (maxSubDirPerDir < PRIMES[idx]) {
                break;
            }
        }
        if (idx != 0)
            idx--;
        for (int i = 0, len = dirDepth; i < len; i++) {
            directoryTree[i] = PRIMES[idx--];
        }
    }

    /**
     * Writes an image to the given file.
     * 
     * @param image
     *            The image.
     * @param file
     *            The file within this cache to store the image.
     * 
     * @throws IOException
     */
    private void _writeImageFile(BufferedImage bi, File file, String imageQuality)
            throws IOException {
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Can not create directory:"
                        + file.getParentFile());
            }
        }
        Iterator writers = ImageIO.getImageWritersByFormatName("JPEG");
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream out = ImageIO.createImageOutputStream(file);
        writer.setOutput(out);
        try {
            log.debug("Create JPEG (" + imageQuality 
                    + " quality) for WADO request. file: " + file);
            ImageWriteParam iwparam = writer.getDefaultWriteParam();
            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality(Float.parseFloat(imageQuality)/100);
            writer.write(null, new IIOImage(bi, null, null), iwparam);
            out.flush();
        } catch (Throwable x) {
            log.error("Can not create JPEG for WADO request. file:" + file);
            if (file.exists()) {
                file.delete();
                log.error("Cache File removed:" + file);
            }
            if (x instanceof IOException) {
                throw (IOException) x;
            }
            IOException ioe = new IOException("Failed to write image file ("
                    + file + ")! Reason:" + x.getMessage());
            ioe.initCause(x);
            throw ioe;
        } finally {
            out.close();
            writer.dispose();
        }
    }

    /**
     * Reads an jpg file into a BufferedImage.
     * 
     * @param jpgFile
     * @return BufferedImage from jpg file.
     */
    private BufferedImage _readJpgFile(File jpgFile) {
        if (jpgFile == null)
            return null;
        // TODO real work!
        return null;
    }

}
