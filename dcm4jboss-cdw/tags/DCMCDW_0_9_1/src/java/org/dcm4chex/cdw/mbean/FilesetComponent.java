/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4166 $ $Date: 2004-07-28 13:48:59 +0800 (周三, 28 7月 2004) $
 * @since 19.07.2004
 *
 */
class FilesetComponent implements Comparable {

    private static final String[] PROMPTS = { "File-set", "Patient", "Study",
            "Series", "Instance"};

    public static final int ROOT = 0;

    public static final int PATIENT = 1;

    public static final int STUDY = 2;

    public static final int SERIES = 3;

    public static final int INSTANCE = 4;

    private final String id;

    private final String comparable;

    private FilesetComponent parent = null;

    private ArrayList childs = null;

    private long size = 0L;

    private final String filePath;

    private final int level;

    public final long size() {
        return size;
    }

    public final int level() {
        return level;
    }

    public final FilesetComponent root() {
        return parent != null ? parent.root() : this;
    }

    public final FilesetComponent parent() {
        return parent;
    }

    public final List childs() {
        return Collections.unmodifiableList(childs);
    }

    public final String id() {
        return id;
    }

    private static String padWithLeadingZeros(String s) {
        return "0000000000".substring(Math.min(10, s.length())) + s;
    }

    public static FilesetComponent makeRootFilesetComponent() {
        return new FilesetComponent(null, null, "", ROOT);
    }

    public static FilesetComponent makePatientFilesetComponent(Dataset ds,
            String filePath) {
        return new FilesetComponent(ds.getString(Tags.PatientID), ds.getString(
                Tags.PatientName, ""), filePath, PATIENT);
    }

    public static FilesetComponent makeStudyFilesetComponent(Dataset ds,
            String filePath) {
        return new FilesetComponent(ds.getString(Tags.StudyInstanceUID), ds
                .getString(Tags.StudyDate, "")
                + ds.getString(Tags.StudyTime, ""), filePath, STUDY);
    }

    public static FilesetComponent makeSeriesFilesetComponent(Dataset ds,
            String filePath) {
        return new FilesetComponent(ds.getString(Tags.SeriesInstanceUID), ds
                .getString(Tags.Modality, "")
                + padWithLeadingZeros(ds.getString(Tags.SeriesNumber, "")),
                filePath, SERIES);
    }

    public static FilesetComponent makeInstanceFilesetComponent(Dataset ds,
            String filePath) {
        return new FilesetComponent(ds.getString(Tags.SOPInstanceUID),
                padWithLeadingZeros(ds.getString(Tags.InstanceNumber, "")),
                filePath, INSTANCE);
    }

    public String toString() {
        return PROMPTS[level]
                + "[id="
                + id
                + ", size="
                + size
                + (level < INSTANCE ? (", #" + PROMPTS[level + 1] + "=" + childs
                        .size())
                        : "") + "]";
    }

    private FilesetComponent(String id, String comparable, String filePath,
            int level) {
        this.id = id;
        this.comparable = comparable;
        this.filePath = filePath;
        this.level = level;
    }

    public boolean isEmpty() {
        return size == 0L;
    }

    public int compareTo(Object o) {
        return comparable.compareTo(((FilesetComponent) o).comparable);
    }

    public void addChild(FilesetComponent child) {
        if (child.parent != null)
                throw new IllegalStateException("Has already a parent: "
                        + child);
        if (childs == null) childs = new ArrayList();
        childs.add(child);
        incSize(child.size);
        child.parent = this;
    }

    public void removeChild(FilesetComponent child) {
        if (child.parent != this)
                throw new IllegalStateException("Not a child: " + child);
        childs.remove(child);
        incSize(-child.size);
        child.parent = null;
    }

    public void incSize(long delta) {
        this.size += delta;
        if (parent != null) parent.incSize(delta);
    }

    public FilesetComponent takeChilds(long maxSize) {
        FilesetComponent dest = newFilesetComponent();
        for (int i = 0; i < childs.size();) {
            FilesetComponent child = (FilesetComponent) childs.get(i);
            if (dest.size + child.size <= maxSize) {
                removeChild(child);
                dest.addChild(child);
            } else
                ++i;
        }
        return dest;
    }

    private FilesetComponent newFilesetComponent() {
        FilesetComponent result = new FilesetComponent(id, comparable,
                filePath, level);
        if (parent != null) parent.newFilesetComponent().addChild(result);
        return result;
    }

    public String getFilePath() {
        return filePath;
    }
}
