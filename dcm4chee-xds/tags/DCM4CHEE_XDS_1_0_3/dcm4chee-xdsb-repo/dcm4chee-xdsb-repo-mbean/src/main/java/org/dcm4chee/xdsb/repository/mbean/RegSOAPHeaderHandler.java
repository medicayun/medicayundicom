package org.dcm4chee.xdsb.repository.mbean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.utils.SOAPUtil;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.attachment.MimeConstants;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegSOAPHeaderHandler extends GenericSOAPHandler
{
    private Logger log = LoggerFactory.getLogger(RegSOAPHeaderHandler.class);
    private static Set<QName> HEADERS = new HashSet<QName>();

    static {
        HEADERS.add( new AddressingConstantsImpl().getActionQName());
    }

    public Set getHeaders()
    {
        log.info("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ getHeaders :"+HEADERS);
        return Collections.unmodifiableSet(HEADERS);
    }

    protected boolean handleInbound(MessageContext msgContext) {
        log.info("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleInbound");
        return handleAnybound(msgContext);
    }
    protected boolean handleAnybound(MessageContext msgContext) {
        try {
            SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
            if (log.isDebugEnabled()) {
                log.debug("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
                SOAPUtil.getInstance().logSOAPMessage(log, msg, true);
                log.debug("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
            }
            MimeHeaders headers = msg.getMimeHeaders();
            logMimeHdrContentType(headers);
            log.info("SOAPMessage:"+msg.getClass().getName());
            log.info("set contentType to :"+MimeConstants.TYPE_SOAP12);
            headers.setHeader(MimeConstants.CONTENT_TYPE, MimeConstants.TYPE_SOAP12);
            SOAPHeaderElement hdr = msg.getSOAPHeader().addHeaderElement(
                    new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_ACTION, "wsa"));
            hdr.setMustUnderstand(true);
            hdr.setValue(XDSConstants.URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B);
            headers = msg.getMimeHeaders();
            logMimeHdrContentType(headers);
            log.info("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message with added Action header.");
            SOAPUtil.getInstance().logSOAPMessage(log, msg, true);
            log.info("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
        } catch (Exception e) {
            log.error("Error:",e);
        }
        log.debug("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleOutbound end.");
        return true;
    }

    private void logMimeHdrContentType(MimeHeaders headers) {
        if (headers == null ) {
            log.info("MimeHeaders is null!");
            return;
        }
        String[] type = headers.getHeader(MimeConstants.CONTENT_TYPE);
        if ( type == null) {
            log.info("No contentType in MimeHeaders!");
            return;
        }
        for ( int i = 0; i < type.length ; i++) {
            log.info("contentType("+i+"):"+type[i]);
        }
    }
    protected boolean handleOutbound(MessageContext msgContext) {
        log.info("@@@@@@@@@@@@ Reg Client @@@@@@@@@@@@@@@@ handleOutbound");
        return handleAnybound(msgContext);
    }
}