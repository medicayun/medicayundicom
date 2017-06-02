package org.dcm4chee.docstore.util;

import java.io.File;
import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.dcm4chee.docstore.Availability;
import org.jboss.mx.util.MBeanServerLocator;

public class FileSystemInfo {

    private static Logger log = Logger.getLogger( FileSystemInfo.class.getName() );

    private static ObjectName dfCmdName;

    private static MBeanServer server;

    public static ObjectName getFilesystemMgtName() {
        return dfCmdName;
    }

    public static void setDFCmdServiceName(String name) {
        try {
            dfCmdName = new ObjectName(name);
        } catch (Exception e) {
            log.error("Cant set FilesystemMgtName! name:"+name);
        }
    }

    public static long freeSpace(String path) throws IOException, InstanceNotFoundException, MBeanException, ReflectionException {
        return ((Long)getServer().invoke(dfCmdName, "freeSpace",
                new Object[] {path},
                new String[] {String.class.getName()})).longValue();
    }

    public static Availability getFileSystemAvailability(File baseDir, long minFree) {
        if ( ! baseDir.isDirectory() ) {
            log.warn(baseDir+" is not a directory! Set Availability to UNAVAILABLE!");
            return Availability.UNAVAILABLE;
        } else {
            try {
                long free = freeSpace(baseDir.getPath());
                log.debug("check Filesystem availability for doc store! path:"+baseDir.getPath()+ " free:"+free);
                return free < minFree ? Availability.UNAVAILABLE : Availability.ONLINE;
            } catch (Exception x) {
                log.error("Can not get free space for "+baseDir+" ! Set Availability to UNAVAILABLE!",x);
                return Availability.UNAVAILABLE;
            }
        }
    }

    private static MBeanServer getServer() {
        if ( server == null ) {
            server = MBeanServerLocator.locate();
        }
        return server;
    }

}
