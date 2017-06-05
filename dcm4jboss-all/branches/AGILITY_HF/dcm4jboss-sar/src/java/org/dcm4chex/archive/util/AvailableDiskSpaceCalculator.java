//
/////////////////////////////////////////////////////////////////
//                 C O P Y R I G H T  (c) 2013
//             A G F A - G E V A E R T  G R O U P
//                    All Rights Reserved
/////////////////////////////////////////////////////////////////
//
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
//                    Agfa-Gevaert Group
//      The copyright notice above does not evidence any
//     actual or intended publication of such source code.
//
/////////////////////////////////////////////////////////////////
//
//
package org.dcm4chex.archive.util;

import java.io.File;

public interface AvailableDiskSpaceCalculator {

    long calculateAvailableSpace(File f);
    
}
