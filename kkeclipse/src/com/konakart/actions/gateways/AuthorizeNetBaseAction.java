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

package com.konakart.actions.gateways;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.PaymentDetailsIf;

/**
 * This is the base action class for AuthorizeNet containing some common code
 */
public class AuthorizeNetBaseAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(AuthorizeNetBaseAction.class);

    private static final long serialVersionUID = 1L;

    protected String loginId;

    protected String transactionKey;

    protected String webServiceURL;

    /**
     * Get information from the AuthorizeNet module
     * 
     * @param kkAppEng
     * @throws KKException
     */
    protected void getAuthNetAuthentication(KKAppEng kkAppEng) throws KKException
    {
        NameValue nv = new NameValue("CIM", "CIM");
        PaymentDetailsIf pd = kkAppEng.getEng().getPaymentDetailsCustom(/* sessionId */null,
                "authorizenet", new NameValue[]
                { nv });
        if (pd == null || pd.getParameters() == null || pd.getParameters().length != 3)
        {
            throw new KKException("Unexpected reply from AuthorizeNet payment module");
        }

        for (int i = 0; i < pd.getParameters().length; i++)
        {
            NameValueIf parm = pd.getParameters()[i];
            if (parm.getName().equals("loginId"))
            {
                this.loginId = parm.getValue();
            } else if (parm.getName().equals("transactionKey"))
            {
                this.transactionKey = parm.getValue();
            } else if (parm.getName().equals("webServiceURL"))
            {
                this.webServiceURL = parm.getValue();
            } else
            {
                throw new KKException("Unexpected parameter " + parm.getName()
                        + " in reply from AuthorizeNet payment module");
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("Details received from AuthorizeNet Module:"
                    + "\n    loginId                                         = " + this.loginId
                    + "\n    transactionKey                                  = "
                    + this.transactionKey
                    + "\n    webServiceURL                                   = "
                    + this.webServiceURL);
        }
    }

    /**
     * Common code to send a message and receive a response
     * 
     * @param kkAppEng
     * @param msg
     * @param methodName
     * @param retAttr
     * @param custId
     * @param profileId
     * @return Returns the value of the attribute called retAttr
     * @throws Exception
     */
    protected String sendMsgToGateway(KKAppEng kkAppEng, StringBuffer msg, String methodName,
            String retAttr, int custId, String profileId) throws Exception
    {
        // Get a response from the gateway
        String gatewayResp = getGatewayResponse(msg, methodName);

        // Now process the XML response
        String ret = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                boolean val = validateGatewayResponse(kkAppEng, msg, methodName, custId, profileId,
                        doc);
                if (val)
                {
                    NodeList list = doc.getElementsByTagName(retAttr);
                    if (list != null && list.getLength() == 1)
                    {
                        Node node = list.item(0);
                        Text datanode = (Text) node.getFirstChild();
                        ret = datanode.getData();
                    }
                    if (log.isDebugEnabled())
                    {
                        log.debug("AuthorizeNet " + methodName + " response data:" + "\n    "
                                + retAttr + "                              = " + ret);
                    }
                }
                return ret;

            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing AuthorizeNet " + methodName + " response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        return retAttr;

    }

    /**
     * Common code to validate the gateway response.
     * 
     * @param kkAppEng
     * @param msg
     * @param methodName
     * @param custId
     * @param profileId
     * @param doc
     * @return Returns true if there were no gateway errors
     */
    protected boolean validateGatewayResponse(KKAppEng kkAppEng, StringBuffer msg,
            String methodName, int custId, String profileId, Document doc)
    {

        String resultCode = null;
        String code = null;
        String text = null;

        // get all elements
        int count = 0;
        NodeList list = doc.getElementsByTagName("*");
        for (int i = 0; i < list.getLength(); i++)
        {
            if (count == 3)
            {
                break;
            }
            Node node = list.item(i);
            String name = node.getNodeName();
            if (name != null)
            {
                if (name.equals("resultCode"))
                {
                    Text datanode = (Text) node.getFirstChild();
                    resultCode = datanode.getData();
                    count++;
                } else if (name.equals("code"))
                {
                    Text datanode = (Text) node.getFirstChild();
                    code = datanode.getData();
                    count++;
                } else if (name.equals("text"))
                {
                    Text datanode = (Text) node.getFirstChild();
                    text = datanode.getData();
                    count++;
                }
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("AuthorizeNet " + methodName + " response data:"
                    + "\n    resultCode                                   = " + resultCode
                    + "\n    code                                         = " + code
                    + "\n    text                                         = " + text);
        }

        if (resultCode != null && resultCode.equalsIgnoreCase("Error"))
        {
            String errorMsg = kkAppEng.getMsg("after.login.body.gateway.problem", new String[]
            { "" });
            addActionError(errorMsg);
            log.warn("AuthorizeNet " + methodName + " response data" + " for customer id : "
                    + custId + ((profileId != null) ? (" profile id : " + profileId) : "")
                    + "\n    resultCode                                   = " + resultCode
                    + "\n    code                                         = " + code
                    + "\n    text                                         = " + text);
            return false;
        }
        return true;
    }

    /**
     * Get a response from the gateway
     * 
     * @param msg
     * @param methodName
     * @return Returns the gateway response
     * @throws Exception
     */
    protected String getGatewayResponse(StringBuffer msg, String methodName) throws Exception
    {
        PaymentDetails pd = new PaymentDetails();
        pd.setRequestUrl(this.webServiceURL);

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, pd, null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + pd.getRequestUrl() + " : "
                        + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("GatewayResp (" + methodName + ") =\n" + gatewayResp);
        }

        return gatewayResp;
    }

    /**
     * Create the authentication part of the message
     * 
     * @return Returns the authentication part of the message
     */
    protected StringBuffer getMsgAuthentication()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<merchantAuthentication>");
        sb.append("<name>" + this.loginId + "</name>");
        sb.append("<transactionKey>" + this.transactionKey + "</transactionKey>");
        sb.append("</merchantAuthentication>");
        return sb;
    }

    /**
     * Create the full message
     * 
     * @param name
     * @param innerMsg
     * @return Returns the full message
     */
    protected StringBuffer getMessage(String name, StringBuffer innerMsg)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<" + name + " xmlns=\"AnetApi/xml/v1/schema/AnetApiSchema.xsd\">");
        sb.append(getMsgAuthentication());
        sb.append(innerMsg);
        sb.append("</" + name + ">");
        return sb;
    }

    /**
     * Add things specific to AuthorizeNet to the connection
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
        connection.setRequestProperty("content-type", "text/xml");
    }
    
    /**
     * Authorize.net returns a response as delimiter separated variables. In order to make them
     * readable, we tag each one with a description before saving in the ipnHistory table.
     * 
     * @param position
     * @return Response Description
     */
    protected String getRespDesc(int position)
    {
        String ret = "unknown";
        switch (position)
        {
        case 1:
            ret = "Response Code";
            break;
        case 2:
            ret = "Response Subcode";
            break;
        case 3:
            ret = "Response Reason Code";
            break;
        case 4:
            ret = "Response Reason Text";
            break;
        case 5:
            ret = "Approval Code";
            break;
        case 6:
            ret = "AVS Result Code";
            break;
        case 7:
            ret = "Transaction ID";
            break;
        case 8:
            ret = "Invoice Number";
            break;
        case 9:
            ret = "Description";
            break;
        case 10:
            ret = "Amount";
            break;
        case 11:
            ret = "Method";
            break;
        case 12:
            ret = "Transaction Type";
            break;
        case 13:
            ret = "Customer ID";
            break;
        case 14:
            ret = "Cardholder First Name";
            break;
        case 15:
            ret = "Cardholder Last Name";
            break;
        case 16:
            ret = "Company";
            break;
        case 17:
            ret = "Billing Address";
            break;
        case 18:
            ret = "City";
            break;
        case 19:
            ret = "State";
            break;
        case 20:
            ret = "Zip";
            break;
        case 21:
            ret = "Country";
            break;
        case 22:
            ret = "Phone";
            break;
        case 23:
            ret = "Fax";
            break;
        case 24:
            ret = "Email";
            break;
        case 25:
            ret = "Ship to First Name";
            break;
        case 26:
            ret = "Ship to Last Name";
            break;
        case 27:
            ret = "Ship to Company";
            break;
        case 28:
            ret = "Ship to Address";
            break;
        case 29:
            ret = "Ship to City";
            break;
        case 30:
            ret = "Ship to State";
            break;
        case 31:
            ret = "Ship to Zip";
            break;
        case 32:
            ret = "Ship to Country";
            break;
        case 33:
            ret = "Tax Amount";
            break;
        case 34:
            ret = "Duty Amount";
            break;
        case 35:
            ret = "Freight Amount";
            break;
        case 36:
            ret = "Tax Exempt Flag";
            break;
        case 37:
            ret = "PO Number";
            break;
        case 38:
            ret = "MD5 Hash";
            break;
        case 39:
            ret = "(CVV2/CVC2/CID)Response Code";
            break;
        case 40:
            ret = "(CAVV) Response Code";
            break;
        case 51:
            ret = "Card Number";
            break;
        case 52:
            ret = "Card Type";
            break;
        default:
            break;
        }

        return ret;
    }
    
    /**
     * Adds a description to the Gateway Result
     * 
     * @param code
     * @return Returns a result with an added description
     */
    protected String getGatewayResultDescription(String code)
    {
        if (code == null)
        {
            return code;
        }
        String ret = code;
        if (code.equals("1"))
        {
            ret += " - This transaction has been approved.";
        } else if (code.equals("2"))
        {
            ret += " - This transaction has been declined.";
        } else if (code.equals("3"))
        {
            ret += " - There has been an error processing this transaction.";
        } else if (code.equals("4"))
        {
            ret += " - This transaction is being held for review.";
        }
        return ret;
    }


}
