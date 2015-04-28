//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.payment.barclaycardsmartpayapi.BarclaycardSmartPayApi;
import com.konakart.util.PrettyXmlPrinter;
import com.konakart.util.RegExpUtils;
import com.konakart.util.Utils;

/**
 * This class is an Action class for what to do when a payment result is received from Barclaycard
 * SmartPay API after a 3D secure check.
 * <p>
 * The result could be Authorised, Refused, Cancelled, Pending or Error
 */
public class BarclaycardSmartPayApi3DResponseAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BarclaycardSmartPayApi3DResponseAction.class);

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "Not Authorised ";

    private static final String RET3_DESC = "There was an unexpected Gateway Response. ";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payment successful. Authorisation Code = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payment not successful. Gateway Reply = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String md = null;
        String paRes = null;
        String merchantReference = null;
        String storeId = null;
        String pspReference = null;
        int custId = -1;
        IpnHistoryIf ipnHistory = new IpnHistory();
        KKAppEng kkAppEng = null;

        if (log.isDebugEnabled())
        {
            log.debug(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " Response Action");
        }

        ipnHistory.setOrderId(-1);
        ipnHistory.setCustomerId(-1);
        ipnHistory.setModuleCode(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);

        try
        {
            // Get an instance of the KonaKart engine for the current store
            kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "Checkout");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

            ipnHistory.setCustomerId(custId);

            // Process the parameters sent in the callback
            StringBuffer sb = new StringBuffer();
            if (request != null)
            {
                Enumeration<String> en = request.getParameterNames();
                while (en.hasMoreElements())
                {
                    String paramName = en.nextElement();
                    String paramValue = request.getParameter(paramName);
                    if (sb.length() > 0)
                    {
                        sb.append("\n");
                    }
                    sb.append(paramName);
                    sb.append(" = ");
                    sb.append(paramValue);

                    // Capture important variables so that we can determine whether the transaction
                    // was successful
                    if (paramName != null)
                    {
                        if (paramName.equalsIgnoreCase("MD"))
                        {
                            md = paramValue;
                        } else if (paramName.equalsIgnoreCase("PaRes"))
                        {
                            paRes = paramValue;
                        } else if (paramName.equalsIgnoreCase("merchantReference"))
                        {
                            merchantReference = paramValue;
                        } else if (paramName.equalsIgnoreCase("storeId"))
                        {
                            storeId = paramValue;
                        } else
                        {
                            log.warn("Unknown Parameter in response:  '" + paramName + "' = '"
                                    + paramValue + "'");
                        }
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " Raw Response data:\n"
                        + sb.toString());
                log.debug("\n    MD                        = " + Utils.trim(md, 50) + "..."
                        + "\n    paRes                     = " + Utils.trim(paRes, 50) + "..."
                        + "\n    merchantReference         = " + merchantReference
                        + "\n    storeId                   = " + storeId);
            }

            // Now we have to send a message to SmartPay to accept the

            log.debug("Now send a message to SmartPay using the MD and paRes just received");

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            PaymentDetailsIf pd = order.getPaymentDetails();

            if (log.isDebugEnabled())
            {
                String str = "Parameters on order:";

                int padding = 25;
                for (int c = 0; c < pd.getParameters().length; c++)
                {
                    str += "\n    " + Utils.padRight(pd.getParameters()[c].getName(), padding)
                            + " = " + RegExpUtils.maskCreditCard(pd.getParameters()[c].getValue());
                }
                log.debug(str);
            }

            // Get API user & password from the payment details object
            String user = pd.getCustom1();
            String pwd = pd.getCustom2();

            // Get Merchant Account
            String merchantAccount = pd.getCustom3();

            // Create a parameter list for the post
            List<NameValueIf> parmList = new ArrayList<NameValueIf>();

            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_MD, md));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_PARESPONSE, paRes));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_USER_ID, user));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_PASSWORD, pwd));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_MERCHANT_ACCOUNT,
                    merchantAccount));

            // Add User Agent details if we need 3D Secure
            if (request != null)
            {
                parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_USER_AGENT, request
                        .getHeader("user-agent")));
                parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_ACCEPT, request
                        .getHeader("accept")));
            }

            // Post the request here:
            String requestUrl = pd.getCustom4();
            pd.setRequestUrl(requestUrl);

            // ----------------------------------------------------------------------------------
            // Do the post
            String gatewayResp = null;
            try
            {
                gatewayResp = postData(pd, parmList);
            } catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Problem posting request to " + pd.getRequestUrl() + " : "
                            + e.getMessage());
                }
                // Save the ipnHistory
                ipnHistory.setGatewayFullResponse(e.getMessage());
                ipnHistory.setKonakartResultDescription(getResultDescription(RET4_DESC
                        + e.getMessage()));
                ipnHistory.setKonakartResultId(RET4);
                ipnHistory.setOrderId(order.getId());
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // Set the message and return
                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { RET3_DESC });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            }

            // do not decode the SmartPay response
            // gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp =\n" + gatewayResp);
                try
                {
                    log.debug("Formatted GatewayResp =\n" + PrettyXmlPrinter.printXml(gatewayResp));
                } catch (Exception e)
                {
                    log.debug("Exception pretty-printing gateway response: " + e.getMessage());
                }
            }

            // ----------------------------------------------------------------------------------
            // Now process the XML response

            String additionalData = null;
            String authCode = null;
            String dccAmount = null;
            String dccSignature = null;
            String fraudResult = null;
            String issuerUrl = null;
            String paRequest = null;
            String refusalReason = null;
            String resultCode = null;
            String faultcode = null;
            String faultstring = null;

            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String root = rootnode.getNodeName();

                if (root != "soap:Envelope")
                {
                    throw new Exception(
                            "Unexpected root element in Barclaycard SmartPay response: " + root);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    // if (log.isDebugEnabled())
                    // {
                    // log.debug("Node = " + name);
                    // }
                    if (name.equals("soap:Body") || name.equals("soap:Envelope")
                            || name.equals("ns1:authoriseResponse")
                            || name.equals("ns1:authorise3dResponse")
                            || name.equals("ns1:paymentResult") || name.equals("soap:Fault"))
                    {
                        continue;
                    }

                    Text textnode = (Text) node.getFirstChild();
                    String value = null;
                    if (textnode != null)
                    {
                        value = textnode.getNodeValue();
                    }

                    if (name == "additionalData")
                    {
                        additionalData = value;
                    } else if (name == "authCode")
                    {
                        authCode = value;
                    } else if (name == "dccAmount")
                    {
                        dccAmount = value;
                    } else if (name == "dccSignature")
                    {
                        dccSignature = value;
                    } else if (name == "fraudResult")
                    {
                        fraudResult = value;
                    } else if (name == "issuerUrl")
                    {
                        issuerUrl = value;
                    } else if (name == "md")
                    {
                        md = value;
                    } else if (name == "paRequest")
                    {
                        paRequest = value;
                    } else if (name == "pspReference")
                    {
                        pspReference = value;
                    } else if (name == "refusalReason")
                    {
                        refusalReason = value;
                    } else if (name == "resultCode")
                    {
                        resultCode = value;
                    } else if (name == "faultcode")
                    {
                        faultcode = value;
                    } else if (name == "faultstring")
                    {
                        faultstring = value;
                    } else
                    {
                        if (log.isInfoEnabled())
                        {
                            log.info("Unknown node in Barclaycard SmartPay API response: " + name);
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Barclaycard SmartPay response data:"
                            + "\n    additionalData            = "
                            + additionalData
                            + "\n    authCode                  = "
                            + authCode
                            + "\n    dccAmount                 = "
                            + dccAmount
                            + "\n    dccSignature              = "
                            + dccSignature
                            + "\n    fraudResult               = "
                            + fraudResult
                            + "\n    issuerUrl                 = "
                            + issuerUrl
                            + "\n    md                        = "
                            + md
                            + "\n    paRequest                 = "
                            + paRequest
                            + "\n    pspReference              = "
                            + pspReference
                            + "\n    refusalReason             = "
                            + refusalReason
                            + "\n    resultCode                = "
                            + resultCode
                            + "\n    faultcode                 = "
                            + faultcode
                            + "\n    faultstring               = " + faultstring);
                }
            } catch (Exception e)
            {
                // Problems parsing the XML

                if (log.isWarnEnabled())
                {
                    log.warn("Problems parsing Barclaycard SmartPay response: " + e.getMessage());
                    e.printStackTrace();
                }

                // Save the ipnHistory
                ipnHistory.setGatewayFullResponse(e.getMessage());
                ipnHistory.setKonakartResultDescription(getResultDescription(RET4_DESC
                        + e.getMessage()));
                ipnHistory.setKonakartResultId(RET4);
                ipnHistory.setOrderId(order.getId());
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // Set the message and return
                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { RET3_DESC });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            }

            String gatewayResult = resultCode;
            ipnHistory.setGatewayResult(gatewayResult);

            String transactionId = pspReference;
            ipnHistory.setGatewayTransactionId(transactionId);

            String message = resultCode;
            String errorMessage = refusalReason;

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(gatewayResp);

            // See if we need to send an email, by looking at the configuration
            String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
            boolean sendEmail = false;
            if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
            {
                sendEmail = true;
            }

            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            // Determine whether the request was successful or not. If successful, we update the
            // inventory as well as changing the state of the order
            if (gatewayResult != null && gatewayResult.equals("Authorised"))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Payment Approved");
                }
                /*
                 * Payment approved
                 */
                String comment = ORDER_HISTORY_COMMENT_OK + transactionId;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Update the inventory
                kkAppEng.getOrderMgr().updateInventory(order.getId());

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET0_DESC);
                ipnHistory.setKonakartResultId(RET0);
                ipnHistory.setOrderId(order.getId());
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // If we received no exceptions, delete the basket
                kkAppEng.getBasketMgr().emptyBasket();

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */true);
                }

                return "Approved";

            } else if (gatewayResult != null && gatewayResult.equals("Refused"))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Payment Refused");
                }
                /*
                 * Payment declined
                 */
                String comment = ORDER_HISTORY_COMMENT_KO + errorMessage;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET0_DESC);
                ipnHistory.setKonakartResultId(RET0);
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { errorMessage });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Error from the gateway?");
                }

                /*
                 * We only get to here if there was an error from the gateway
                 */

                // If a Soap Fault we use the faultstring
                if (faultstring != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("SOAP Fault from the gateway: " + faultstring);
                    }
                    message = faultstring;
                    gatewayResult = faultstring;
                }

                String comment = RET1_DESC + message;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(getResultDescription(RET1_DESC + message));
                ipnHistory.setKonakartResultId(RET1);
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { RET1_DESC + gatewayResult });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            }
        } catch (Exception e)
        {
            if (log.isInfoEnabled())
            {
                log.info(RET4_DESC + e.getMessage());
            }

            try
            {
                ipnHistory.setKonakartResultDescription(getResultDescription(RET4_DESC
                        + e.getMessage()));
                ipnHistory.setKonakartResultId(RET4);
                if (kkAppEng != null)
                {
                    kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);
                }
            } catch (KKException e1)
            {
                return super.handleException(request, e1);
            }

            return super.handleException(request, e);
        }
    }

    /**
     * Create the request from the parameters
     * 
     * @param pd
     *            the PaymentDetails
     * @param ccParmList
     *            the credit card parameters
     */
    protected StringBuffer getGatewayRequest(PaymentDetailsIf pd, List<NameValueIf> ccParmList)
    {
        HashMap<String, String> hp = hashParameters(pd, ccParmList);

        String merchantAccount = hp.get(BarclaycardSmartPayApi.BC_SPAY_API_MERCHANT_ACCOUNT);
        String paResponse = hp.get(BarclaycardSmartPayApi.BC_SPAY_API_PARESPONSE);
        String md = hp.get(BarclaycardSmartPayApi.BC_SPAY_API_MD);

        StringBuffer msg = new StringBuffer("<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soap:Body>"
                + "<ns1:authorise3d xmlns:ns1=\"http://payment.services.adyen.com\">"
                + "<ns1:paymentRequest3d>"
                + "<browserInfo xmlns=\"http://payment.services.adyen.com\">"
                + "<acceptHeader xmlns=\"http://common.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_ACCEPT) + "</acceptHeader>"
                + "<userAgent xmlns=\"http://common.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_USER_AGENT) + "</userAgent>"
                + "</browserInfo>");

        if (paResponse == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("paResponse tag not added to request because its value is null");
            }
        } else
        {
            msg = msg.append("<paResponse xmlns=\"http://payment.services.adyen.com\">"
                    + paResponse + "</paResponse>");
        }

        msg = msg.append("<md xmlns=\"http://payment.services.adyen.com\">" + md + "</md>"
                + "<merchantAccount xmlns=\"http://payment.services.adyen.com\">" + merchantAccount
                + "</merchantAccount>" + "</ns1:paymentRequest3d>" + "</ns1:authorise3d>"
                + "</soap:Body>" + "</soap:Envelope>");

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest =\n" + PrettyXmlPrinter.printXml(msg.toString()));
        }

        return msg;
    }

    /**
     * Add things specific to Barclaycard SmartPay to the connection
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
        HashMap<String, String> hp = hashParameters(pd, paramList);

        String userPass = hp.get(BarclaycardSmartPayApi.BC_SPAY_API_USER_ID) + ":"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_PASSWORD);
        String encodedUserPass = Base64.encodeBase64String(userPass.getBytes());
        if (log.isDebugEnabled())
        {
            log.debug("Add " + encodedUserPass + " (" + userPass + ") for HTTP Authentication");
        }
        connection.setRequestProperty("Authorization", "Basic " + encodedUserPass);

        if (log.isDebugEnabled())
        {
            log.debug("Add 'authorise3d' as SOAPAction");
        }
        connection.setRequestProperty("SOAPAction", "authorise3d");
    }

    /**
     * Use this to truncate the result description so that it fits in the database column OK
     * 
     * @param desc
     *            the result description (which may be too long)
     * @return a truncated result description
     */
    private String getResultDescription(String desc)
    {
        if (desc == null)
        {
            return null;
        }

        return desc.substring(0, Math.min(255, desc.length() - 1));
    }
}
