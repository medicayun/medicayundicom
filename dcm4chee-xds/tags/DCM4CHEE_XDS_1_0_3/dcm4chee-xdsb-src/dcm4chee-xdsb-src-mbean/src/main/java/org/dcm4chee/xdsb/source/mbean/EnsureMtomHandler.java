package org.dcm4chee.xdsb.source.mbean;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.extensions.xop.XOPContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnsureMtomHandler extends GenericSOAPHandler
{
    private Logger log = LoggerFactory.getLogger(EnsureMtomHandler.class);

    protected boolean handleInbound(MessageContext msgContext) {
        return true;
    }
    
    protected boolean handleOutbound(MessageContext msgContext) {
        try {
            SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
            CommonMessageContext cmsgContext = (CommonMessageContext) msgContext;
            if ( msg instanceof SOAPMessageImpl ) {
                if ( msg.getAttachments().hasNext()) {
                    log.info("Message has already a attachment! isMTOMEnabled:"+XOPContext.isMTOMEnabled());
                    return true;
                }
                log.info("FORCE MTOM/XOP Message!");
                ByteArrayDataSource DUMMY_PLAIN_DATA_SOURCE = new ByteArrayDataSource("DUMMY", "text/plain");
                msg.addAttachmentPart(msg.createAttachmentPart(new DataHandler(DUMMY_PLAIN_DATA_SOURCE)));
                ((SOAPMessageImpl)msg).setXOPMessage(true);
                Scope currScope=cmsgContext.getCurrentScope();
                cmsgContext.setCurrentScope(Scope.APPLICATION);
                XOPContext.setMTOMEnabled(true);
                cmsgContext.setCurrentScope(currScope);
            } else {
                log.warn("Can't force MTOM/XOP Message! msg is NOT SOAPMessageImpl!");
            }
        } catch (Exception e) {
            log.error("Can't force MTOM/XOP Message! Error:"+e,e);
        }
        return true;
    }
}