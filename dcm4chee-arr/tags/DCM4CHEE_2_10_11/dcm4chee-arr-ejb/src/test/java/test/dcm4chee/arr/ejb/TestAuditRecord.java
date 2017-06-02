package test.dcm4chee.arr.ejb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.jms.BytesMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.persistence.EntityExistsException;

import org.dcm4chee.arr.ejb.AuditRecord;
import org.dcm4chee.arr.ejb.Code;
import org.dcm4chee.arr.ejb.GenericEntityMgmt;
import org.dcm4chee.arr.jmx.UDPServerMBean;
import org.dcm4chee.arr.util.Ejb3Util;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.annotation.ejb.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for AuditRecord related entites
 * 
 * @author Fang Yang (fang.yang@agfa.com)
 * @version $Id: TestAuditRecord.java 142 2006-12-20 21:58:39Z fangatagfa $
 * @since Aug 25, 2006
 */
public class TestAuditRecord {

    private static Logger log = LoggerFactory.getLogger(TestAuditRecord.class);

    protected final static Logger syslog = LoggerFactory.getLogger("syslog");

    private ObjectName objectName;

    private MBeanServer server;

    private QueueSession session = null;

    private QueueConnection conn = null;

    private Queue queue = null;

    private static String queueName = "queue/ARRReceiver";

    @BeforeClass(groups = { "mdb", "integration.jmx", "stress.jmx" })
    public void setUp() throws Exception {
        try {
            server = (MBeanServer) MBeanServerFactory.findMBeanServer(null)
                    .get(0);
            Service service = UDPServerMBean.class.getAnnotation(Service.class);
            objectName = new ObjectName(service.objectName());
            assert server.isRegistered(objectName);

            //
            // Setup queue
            //

            queue = (Queue) getInitialContext().lookup(queueName);

            /**
             * The E-EJB3 stuff only provides a local Conection factory avaialbe
             * under the JNDI name "java:/ConnectionFactory". So we have to use
             * "ConnectionFactory" instead of "java:/ConnectionFactory"
             */
            QueueConnectionFactory factory = (QueueConnectionFactory) getInitialContext()
                    .lookup("ConnectionFactory");

            conn = factory.createQueueConnection();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @AfterClass(groups = { "mdb", "integration.jmx", "stress.jmx" })
    public void tearDown() {
        try {
            GenericEntityMgmt gem = Ejb3Util
                    .getRemoteInterface(GenericEntityMgmt.class);
            List<AuditRecord> arrs = gem.findAll(AuditRecord.class);

            for (AuditRecord ar : arrs) {
                gem.remove(AuditRecord.class, ar.getPk());
            }

            arrs = gem.findAll(AuditRecord.class);
            assert arrs.size() == 0;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        
        try {
            conn.close();
        } catch (Exception e) {
        }
    }

    @Parameters( { "simple-msg" })
    @Test(groups = "mdb")
    public void sendTextMessage(String text) {
        log.info("> sending a text message, which should be ignored");
        try {
            session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);

            TextMessage msg = session.createTextMessage(text);
            QueueSender sender = session.createSender(queue);
            sender.send(msg);

            Thread.sleep(1000);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * A AuditRecord message is sent to the queue and the MDB will pickup and
     * process the message
     * 
     */
    @Test(groups = "mdb")
    public void sendAuditRecord() {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(
                    "ar_example1.xml");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);

            QueueSession session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            BytesMessage msg = session.createBytesMessage();

            InetAddress addr = InetAddress.getLocalHost();

            msg.setStringProperty("sourceHostAddress", addr.getHostAddress());
            msg.setStringProperty("sourceHostName", addr.getHostName());

            msg.writeBytes(bytes);
            QueueSender sender = session.createSender(queue);
            sender.send(msg);

            Thread.sleep(1000); // Make sure the message is processed fully
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            ;
        }
    }
    
    @Test(groups = "mdb", dependsOnMethods = "sendAuditRecord")
    public void duplicatedCode() {
        try {
            // Try to create an already-existing Code to test its uniqueConstraints
            Code code = new Code();
            code.setValue("110104");
            code.setDesignator("DCM");
            
            GenericEntityMgmt mgmt = Ejb3Util.getRemoteInterface(GenericEntityMgmt.class);
            mgmt.persistent(code);
            
        } catch(Exception e) {
            assert e.getCause() instanceof EntityExistsException;
            assert e.getCause().getCause() instanceof ConstraintViolationException;
        }
    }

    @Test(groups = "mdb", dependsOnMethods = "duplicatedCode")
    public void checkData_mdb() {
        doCheckData();
    }

    @Test(groups = "integration.jmx", dependsOnMethods = "sendAuditRecordViaSyslog")
    public void checkData_jmx() {
        doCheckData();
    }

    private void doCheckData() {
        try {
            GenericEntityMgmt gem = Ejb3Util
                    .getRemoteInterface(GenericEntityMgmt.class);
            List<AuditRecord> arrs = gem.findAll(AuditRecord.class);
            assert arrs.size() >= 1;

            for (AuditRecord ar : arrs) {
                gem.remove(AuditRecord.class, ar.getPk());
            }

            arrs = gem.findAll(AuditRecord.class);
            assert arrs.size() == 0;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * A AuditRecord message is sent to SYSLOG deamon vai log4j SYSLOG appender.
     * We assume the deam is "UDPServerMBean" service.
     * 
     */
    @Test(groups = "integration.jmx", dependsOnMethods = "checkData_mdb")
    public void sendAuditRecordViaSyslog() {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(
                    "ar_example1.xml");
            byte[] bytes = new byte[is.available()];
            is.read(bytes);

            // Send it to repository through log4j syslog appender
            syslog.info(new String(bytes));

            Thread.sleep(1000); // Make sure the message is processed fully
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            ;
        }
    }

    @Test(groups = "stress.jmx")
    public void sendALotAuditRecord() {
        ZipFile zip = null;
        try {
            GenericEntityMgmt mgmt = Ejb3Util
                    .getRemoteInterface(GenericEntityMgmt.class);

            zip = new ZipFile(new File(this.getClass().getClassLoader()
                    .getResource("audit_samples.zip").toURI()));
            for (Enumeration list = zip.entries(); list.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) list.nextElement();
                if (entry != null && !entry.isDirectory()) {
                    InputStream entryStream = null;
                    try {
                        // Get an input stream for the entry.
                        entryStream = zip.getInputStream(entry);

                        // Create a memory stream to hold the data
                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = entryStream.read(buffer)) != -1) {
                            bs.write(buffer, 0, bytesRead);
                        }

                        byte[] bytes = bs.toByteArray();

                        // Send it to repository through log4j syslog appender
                        log.debug("... sending: " + entry.getName());
                        syslog.info(new String(bytes));

                        Thread.sleep(250); // don't send too fast
                    } catch (Exception ex) {
                        log.error("failed: " + entry.getName());
                    } finally {
                        try {
                            entryStream.close();
                        } catch (Exception ignore) {
                        }
                        ;
                    }
                }
            }

            // Need to wait util all messages are stored
            int num = 0;
            while (true) {
                List<AuditRecord> arrs = mgmt.findAll(AuditRecord.class);
                if (num == 0 || num != arrs.size())
                    num = arrs.size();
                else
                    break;

                // we assume each message storage does not take more than 2
                // second
                Thread.sleep(2000);
            }

            log.info("Total audit records in database: " + num);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            try {
                zip.close();
            } catch (Exception ignore) {
            }
            ;
        }
    }

    private static InitialContext getInitialContext() throws Exception {
        return new InitialContext();
    }
}
