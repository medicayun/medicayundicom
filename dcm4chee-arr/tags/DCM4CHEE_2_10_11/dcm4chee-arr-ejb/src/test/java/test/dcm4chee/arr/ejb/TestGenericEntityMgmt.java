package test.dcm4chee.arr.ejb;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

import org.dcm4chee.arr.ejb.ActiveParticipant;
import org.dcm4chee.arr.ejb.AuditRecord;
import org.dcm4chee.arr.ejb.Code;
import org.dcm4chee.arr.ejb.GenericEntityMgmt;
import org.dcm4chee.arr.util.Ejb3Util;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for TestGenericEntityMgmt SLSB by using AuditRecord entities
 * 
 * @author Fang Yang (fang.yang@agfa.com)
 * @version $Id: TestGenericEntityMgmt.java 142 2006-12-20 21:58:39Z fangatagfa $
 * @since Aug 28, 2006
 */
@Test(groups = "slsb")
public class TestGenericEntityMgmt {

    private static Logger log = LoggerFactory.getLogger(TestGenericEntityMgmt.class);

    private GenericEntityMgmt gem;
    private static String queueName = "queue/ARRReceiver";

    @BeforeClass
    public void setUp() throws Exception {
        gem = Ejb3Util.getRemoteInterface(GenericEntityMgmt.class);
        assert (gem != null);

        QueueConnection conn = null;
        InputStream is = null;
        try {
            Queue queue = (Queue) getInitialContext().lookup(queueName);

            /**
             * The E-EJB3 stuff only provides a local Conection factory avaialbe
             * under the JNDI name "java:/ConnectionFactory". So we have to use
             * "ConnectionFactory" instead of "java:/ConnectionFactory"
             */
            QueueConnectionFactory factory = (QueueConnectionFactory) getInitialContext()
                    .lookup("ConnectionFactory");

            conn = factory.createQueueConnection();
            
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
            log.info("Preparing entity data is done");
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
    
    public void findByPk() {
        AuditRecord ar = gem.findByPk(AuditRecord.class, 1);
        assert ar != null;
        
        assert ar.getPk() == 1;
    }
    
    public void findAll() {
        List<AuditRecord> ars = gem.findAll(AuditRecord.class);
        assert ars.size() == 1;
        
        List<Code> cs = gem.findAll(Code.class);
        assert cs.size() == 4;
        
        List<ActiveParticipant> aps = gem.findAll(ActiveParticipant.class);
        assert aps.size() == 3;
    }
    
    public void findByExample() {
        AuditRecord ar = new AuditRecord();
        ar.setEventAction("C");
        List<AuditRecord> ars = gem.findByExample(AuditRecord.class, ar);
        assert ars.size() == 1;        
        AuditRecord ar2 = ars.get(0);
        assert ar2.getEventAction().equals("C");
        
        Code c = new Code();
        c.setValue("110153");
        List<Code> cs = gem.findByExample(Code.class, c);
        assert cs.size() == 1;        
        Code c2 = cs.get(0);
        assert c2.getValue().equals("110153");
        
        ActiveParticipant ap = new ActiveParticipant();
        ap.setUserID("67562");
        List<ActiveParticipant> aps = gem.findByExample(ActiveParticipant.class, ap);
        assert aps.size() == 1;        
        ActiveParticipant ap2 = aps.get(0);
        assert ap2.getUserID().equals("67562");
    }
    
 
    public void query() {
        List<AuditRecord> ars = gem.query(AuditRecord.class, "SELECT a from AuditRecord a WHERE a.eventAction = ?", "C");
        for(AuditRecord ar : ars)
            assert ar.getEventAction().equals("C");
    }
    
    public void merge() {
        AuditRecord ar = gem.findByPk(AuditRecord.class, 1);
        assert ar != null;
        
        ar.setEventAction("CCCC");
        gem.merge(ar);
        
        ar = gem.findByPk(AuditRecord.class, 1);
        assert ar != null;
        
        assert ar.getEventAction().equals("CCCC");
        
        //
        // Change it back
        //
        ar.setEventAction("C");
        gem.merge(ar);
        
        ar = gem.findByPk(AuditRecord.class, 1);
        assert ar != null;
        
        assert ar.getEventAction().equals("C");
    }
    
    public void criteria1() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(AuditRecord.class);
        detachedCriteria.add(Expression.ilike("sourceID", "Reading", MatchMode.START));
        List<AuditRecord> ars = gem.findByCriteria(AuditRecord.class, detachedCriteria);
        assert ars.size() == 1;
    }
    
    public void criteria2() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(AuditRecord.class);
        String[] codeStrings = new String[] {"110104^DCM", "110153^DCM"};
        detachedCriteria.createCriteria("eventID").add(getCodeStringCriteria(codeStrings));
                
        // CONCAT is not currently supported in Criteria API
        //detachedCriteria.createAlias("eventID", "ei").add(Expression.in(getCodeString("ei"), new String[]{"110104^DCM"}));
//        detachedCriteria.createCriteria("eventID").add(Expression.in(
//                "CONCAT(value, CONCAT('^', designator))", 
//                new String[]{"110104^DCM"}));
        
        List<AuditRecord> ars = gem.findByCriteria(AuditRecord.class, detachedCriteria);
        assert ars.size() == 1;
    }
    
    private static Disjunction getCodeStringCriteria(String[] codeStrings) {
        Disjunction disjuncation = Expression.disjunction();
        for(String codeString : codeStrings) {
            String[] arr = codeString.split("\\^");
            disjuncation.add(Expression.conjunction()
                    .add(Expression.eq("value", arr[0]))
                    .add(Expression.eq("designator", arr[1])));
        }
        return disjuncation;
    }

    @AfterClass
    public void tearDown() {
        try {
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
    }
    
    private static InitialContext getInitialContext() throws Exception {
        return new InitialContext();
    }
}
