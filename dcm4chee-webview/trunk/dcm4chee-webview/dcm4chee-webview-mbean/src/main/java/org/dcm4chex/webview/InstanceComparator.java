package org.dcm4chex.webview;

import java.util.Comparator;

public interface InstanceComparator extends Comparator {

    /**
     * Return Tags that should be in the C-FIND request as return attributes.
     * 
     * @return
     */
    int[] getTags();
}
