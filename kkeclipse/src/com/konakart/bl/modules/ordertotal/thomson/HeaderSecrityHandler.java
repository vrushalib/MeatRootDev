//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is free software; you can redistribute 
// it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package com.konakart.bl.modules.ordertotal.thomson;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The purpose of this Handler is to add username and password tokens to the SOAP message
 */
public class HeaderSecrityHandler implements SOAPHandler<SOAPMessageContext>
{
    private String uName = "not-set";

    private String pWord = "not-set";

    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(HeaderSecrityHandler.class);

    /**
     * @param username
     */
    public void setUName(String username)
    {
        uName = username;
    }

    /**
     * @param password
     */
    public void setPWord(String password)
    {
        pWord = password;
    }

    /**
     * @return the uName
     */
    public String getUName()
    {
        return uName;
    }

    /**
     * @return the pWord
     */
    public String getPWord()
    {
        return pWord;
    }

    public boolean handleMessage(SOAPMessageContext smc)
    {
        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty.booleanValue())
        {
            SOAPMessage message = smc.getMessage();

            if (log.isInfoEnabled())
            {
                log.info("Adding Credentials : " + getUName() + "/" + getPWord());
            }

            try
            {
                SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
                envelope.setPrefix("soapenv");
                envelope.getBody().setPrefix("soapenv");

                SOAPHeader header = envelope.getHeader();
                
                if (header == null)
                {               
                    header = envelope.addHeader();
                }
                
                SOAPElement security = header
                        .addChildElement("Security", "wsse",
                                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

                SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
                usernameToken.addAttribute(new QName("wsu:Id"), "UsernameToken-1");
                usernameToken.setAttribute("wsu:Id", "UsernameToken-1");

                usernameToken
                        .addAttribute(new QName("xmlns:wsu"),
                                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

                SOAPElement username = usernameToken.addChildElement("Username", "wsse");
                username.addTextNode(getUName());

                SOAPElement password = usernameToken.addChildElement("Password", "wsse");
                password.setAttribute("Type",
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
                password.addTextNode(getPWord());

                SOAPElement encodingType = usernameToken.addChildElement("Nonce", "wsse");
                encodingType
                        .setAttribute("EncodingType",
                                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
                encodingType.addTextNode("Encoding");
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return outboundProperty;
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public Set getHeaders()
    {
        // Not Implemented
        return null;
    }

    public boolean handleFault(SOAPMessageContext context)
    {
        // Not Implemented
        return true;
    }

    public void close(MessageContext context)
    {
        // Not Implemented
    }
}
