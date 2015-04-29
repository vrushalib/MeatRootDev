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
import com.konakart.appif.StoreIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.payment.barclaycardsmartpayapi.BarclaycardSmartPayApi;
import com.konakart.util.PrettyXmlPrinter;
import com.konakart.util.RegExpUtils;
import com.konakart.util.Utils;

/**
 * This class is an Action class for sending credit card details to Barclaycard SmartPay and
 * receiving confirmation
 */
public class BarclaycardSmartPayApiAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BarclaycardSmartPayApiAction.class);

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected Gateway Response. Response = ";

    private static final int RET2 = -2;

    private static final String RET2_DESC = "Redirect Shopper to 3D Secure check";

    private static final String RET3_DESC = "There was an unexpected Gateway Response.";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.

    private static final String ORDER_HISTORY_COMMENT_OK = "Barclaycard SmartPay payment successful. Barclaycard SmartPay TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Barclaycard SmartPay payment not successful. Barclaycard SmartPay Reply = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String message = null;
        String errorMessage = null;
        String gatewayResult = null;
        String transactionId = null;

        if (log.isDebugEnabled())
        {
            log.debug(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE + " payment module called");
        }

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);
        KKAppEng kkAppEng = null;

        try
        {
            int custId;

            kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "Checkout");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, BarclaycardSmartPayApi.BC_SPAY_API_GATEWAY_CODE);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            PaymentDetailsIf pd = order.getPaymentDetails();

            String storeId = "?";
            StoreIf store = kkAppEng.getEng().getStore();
            if (store != null)
            {
                storeId = store.getStoreId();
            }

            // The merchant reference is sent back in the notification so we use it to save the
            // token UUID
            String merchantReference = pd.getCustom5();

            // Create a parameter list for the credit card details.
            List<NameValueIf> parmList = new ArrayList<NameValueIf>();

            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_MERCHANT_REF,
                    merchantReference));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_CARD_NUMBER, pd
                    .getCcNumber()));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_CARD_CVV2, pd
                        .getCcCVV()));
            }

            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_CARD_EXPIRY_MONTH, pd
                    .getCcExpiryMonth()));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_CARD_EXPIRY_YEAR, "20"
                    + pd.getCcExpiryYear()));
            parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_CARDHOLDERS_NAME, pd
                    .getCcOwner()));

            // Add User Agent details if we need 3D Secure
            if (getParameterFromPaymentDetails(BarclaycardSmartPayApi.BC_SPAY_API_3D_STATUS, pd)
                    .equalsIgnoreCase("true"))
            {
                parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_USER_AGENT, request
                        .getHeader("user-agent")));
                parmList.add(new NameValue(BarclaycardSmartPayApi.BC_SPAY_API_ACCEPT, request
                        .getHeader("accept")));
            }

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
            String md = null;
            String paRequest = null;
            String pspReference = null;
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
                            || name.equals("ns1:paymentResult") || name.equals("soap:Fault"))
                    {
                        continue;
                    }

                    Text textnode = null;
                    Node firstChildNode = node.getFirstChild();
                    try
                    {
                        textnode = (Text) firstChildNode;
                    } catch (java.lang.ClassCastException cce)
                    {
                        // This is unexpected - we only process TextNodes
                        if (log.isDebugEnabled())
                        {
                            log.debug("Not a Text Node - parsing child node : "
                                    + firstChildNode.getNodeName() + " = "
                                    + firstChildNode.getNodeValue() + " (type "
                                    + firstChildNode.getNodeType() + ")");
                        }
                        continue;
                    }
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
                    } else if (name == "key")
                    {
                        // ignore
                    } else if (name == "value")
                    {
                        // ignore
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

            gatewayResult = resultCode;
            ipnHistory.setGatewayResult(gatewayResult);

            transactionId = pspReference;
            ipnHistory.setGatewayTransactionId(transactionId);

            message = resultCode;
            errorMessage = refusalReason;

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

            // Determine whether we need to redirect the customer off to a 3D Secure check
            if (gatewayResult != null && gatewayResult.equals("RedirectShopper"))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Redirect Shopper to 3D Secure check");
                }
                /*
                 * Redirect to 3D Secure check
                 */

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET2_DESC);
                ipnHistory.setKonakartResultId(RET2);
                ipnHistory.setOrderId(order.getId());
                ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                pd.setPostOrGet("post");
                pd.setRequestUrl(issuerUrl);

                List<NameValue> parmList3d = new ArrayList<NameValue>();

                String termUrl = getParameterFromPaymentDetails(
                        BarclaycardSmartPayApi.BC_SPAY_API_3D_RESPONSE_URL, pd);

                parmList3d.add(new NameValue("MD", md));
                parmList3d.add(new NameValue("PaReq", paRequest));
                parmList3d.add(new NameValue("TermUrl", termUrl + "?merchantReference="
                        + merchantReference + "&storeId=" + storeId));

                // Put the parameters into an array - overwrite the parameters that were there
                NameValue[] nvArray = new NameValue[parmList3d.size()];
                parmList3d.toArray(nvArray);
                pd.setParameters(nvArray);

                if (log.isDebugEnabled())
                {
                    String str = "Post these parameters to the 3D-Secure check at "
                            + pd.getRequestUrl();

                    int padding = 25;
                    for (int c = 0; c < pd.getParameters().length; c++)
                    {
                        str += "\n    " + Utils.padRight(pd.getParameters()[c].getName(), padding)
                                + " = "
                                + RegExpUtils.maskCreditCard(pd.getParameters()[c].getValue());
                    }
                    log.debug(str);
                }

                return "Redirect3dSecure";
            }
            // Determine whether the request was successful or not. If successful, we update the
            // inventory as well as changing the state of the order
            else if (gatewayResult != null && gatewayResult.equals("Authorised"))
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
            if (log.isDebugEnabled())
            {
                log.debug(RET4_DESC + e.getMessage());
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

        StringBuffer msg = new StringBuffer("<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soap:Body>"
                + "<ns1:authorise xmlns:ns1=\"http://payment.services.adyen.com\">"
                + "<ns1:paymentRequest>" + "<amount xmlns=\"http://payment.services.adyen.com\">"
                + "<currency xmlns=\"http://common.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CURRENCY_CODE)
                + "</currency>"
                + "<value xmlns=\"http://common.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_PAYMENT_AMOUNT)
                + "</value>"
                + "</amount>"
                + "<card xmlns=\"http://payment.services.adyen.com\">"
                + "<cvc>"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CARD_CVV2)
                + "</cvc>"
                + "<expiryMonth>"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CARD_EXPIRY_MONTH)
                + "</expiryMonth>"
                + "<expiryYear>"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CARD_EXPIRY_YEAR)
                + "</expiryYear>"
                + "<holderName>"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CARDHOLDERS_NAME)
                + "</holderName>"
                + "<number>"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CARD_NUMBER)
                + "</number>"
                + "</card>"
                + "<merchantAccount xmlns=\"http://payment.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_MERCHANT_ACCOUNT)
                + "</merchantAccount>"
                + "<reference xmlns=\"http://payment.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_MERCHANT_REF)
                + "</reference>"
                + "<shopperEmail xmlns=\"http://payment.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CUST_EMAIL)
                + "</shopperEmail>"
                + "<shopperReference xmlns=\"http://payment.services.adyen.com\">"
                + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_CUST_REFERENCE) + "</shopperReference>");

        if (hp.get(BarclaycardSmartPayApi.BC_SPAY_API_3D_STATUS).equalsIgnoreCase("true"))
        {
            msg.append("<browserInfo xmlns=\"http://payment.services.adyen.com\">"
                    + "<acceptHeader xmlns=\"http://common.services.adyen.com\">"
                    + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_ACCEPT) + "</acceptHeader>"
                    + "<userAgent xmlns=\"http://common.services.adyen.com\">"
                    + hp.get(BarclaycardSmartPayApi.BC_SPAY_API_USER_AGENT) + "</userAgent>"
                    + "</browserInfo>");
        }

        msg.append("</ns1:paymentRequest>" + "</ns1:authorise>" + "</soap:Body>"
                + "</soap:Envelope>");

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
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
            log.debug("Add 'authorise' as SOAPAction");
        }
        connection.setRequestProperty("SOAPAction", "authorise");
    }
}
