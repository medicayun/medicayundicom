package test.dcm4chee.arr.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.dcm4chee.arr.jmx.UDPServerMBean;
import org.jboss.annotation.ejb.Service;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for UDPServer MBean
 * 
 * @author Fang Yang (fang.yang@agfa.com)
 * @version $Id: UDPServerTest.java 62 2006-08-28 13:33:27Z fangatagfa $
 * @since Aug 25, 2006
 */
@Test(groups = "jmx")
public class UDPServerTest {

    private ObjectName objectName;

    private MBeanServer server;

    @BeforeClass
    public void setUp() throws Exception {
        // Need to find its ObjectName first
        server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
        Service service = UDPServerMBean.class.getAnnotation(Service.class);
        objectName = new ObjectName(service.objectName());
        assert server.isRegistered(objectName);
    }

    public void isRunning() throws Exception {
        boolean isRunning = (Boolean) server
                .getAttribute(objectName, "Running");
        assert isRunning;
    }

    @AfterClass
    public void tearDown() {
    }
}
