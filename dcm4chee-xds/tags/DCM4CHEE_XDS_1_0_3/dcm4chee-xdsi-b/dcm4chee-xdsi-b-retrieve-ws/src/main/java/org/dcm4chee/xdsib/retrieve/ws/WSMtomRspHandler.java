package org.dcm4chee.xdsib.retrieve.ws;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.extensions.xop.XOPContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSMtomRspHandler extends GenericSOAPHandler
{
    private Logger log = LoggerFactory.getLogger(WSMtomRspHandler.class);

    protected boolean handleInbound(MessageContext msgContext) {
        return true;
    }
    
    protected boolean handleOutbound(MessageContext msgContext) {
        try {
            if ( "true".equals(msgContext.get("DISABLE_FORCE_MTOM_RESPONSE") ) ) {
                log.info("FORCE MTOM/XOP Response Message is disabled! Return with Simple SOAP!");
                return true;
            }
            
            SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
            CommonMessageContext cmsgContext = (CommonMessageContext) msgContext;
            if ( msg instanceof SOAPMessageImpl ) {
                if ( msg.getAttachments().hasNext()) {
                    log.info("Message has already a attachment! isMTOMEnabled:"+XOPContext.isMTOMEnabled());
                    return true;
                }
                log.info("FORCE MTOM/XOP Response Message!");
                ByteArrayDataSource DUMMY_PLAIN_DATA_SOURCE = new ByteArrayDataSource("DUMMY", "text/plain");
                msg.addAttachmentPart(msg.createAttachmentPart(new DataHandler(DUMMY_PLAIN_DATA_SOURCE)));
                ((SOAPMessageImpl)msg).setXOPMessage(true);
                Scope currScope=cmsgContext.getCurrentScope();
                cmsgContext.setCurrentScope(Scope.APPLICATION);
                XOPContext.setMTOMEnabled(true);
                cmsgContext.setCurrentScope(currScope);
            } else {
                log.warn("Can't force MTOM/XOP Response Message! msg is NOT SOAPMessageImpl!");
            }
        } catch (Exception e) {
            log.error("Can't force MTOM/XOP Response Message! Error:"+e,e);
        }
        return true;
    }
}