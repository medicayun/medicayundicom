package org.dcm4chex.webview;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class InstanceNumberComparator implements InstanceComparator {

    public int[] getTags() {
        return new int[]{ Tag.InstanceNumber };
    }

    public int compare(Object arg0, Object arg1) {
        String in0 = ((DicomObject) arg0).getString(Tag.InstanceNumber);
        String in1 = ((DicomObject) arg1).getString(Tag.InstanceNumber);
        if ( in0 == null ) {
            return in1 == null ? 0 : 1;
        } else if ( in1 == null ) {
            return 0;
        } else {
            if ( Character.isDigit(in0.charAt(0)) && Character.isDigit(in1.charAt(0))) {
                try {
                    return new Integer(in0).compareTo( new Integer(in1) );
                } catch ( NumberFormatException ignore ) {}
            }
            return in0.compareTo(in1);
        }
    }

}
