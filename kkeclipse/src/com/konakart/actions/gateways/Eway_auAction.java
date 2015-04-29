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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import com.konakart.bl.modules.payment.eway_au.Eway_au;

/**
 * This class is an Action class for sending credit card details to eWay and receiving confirmation
 */
public class Eway_auAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(Eway_auAction.class);

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected Gateway Response. Response = ";

    private static final String RET3_DESC = "There was an unexpected Gateway Response.";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "eWay payment successful. eWay TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "eWay payment not successful. eWay Reply = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String message = null;
        String errorMessage = null;
        String gatewayResult = null;
        String transactionId = null;

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(Eway_au.EWAY_AU_GATEWAY_CODE);
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
            validateOrder(order, Eway_au.EWAY_AU_GATEWAY_CODE);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            PaymentDetailsIf pd = order.getPaymentDetails();

            // Create a parameter list for the credit card details.
            List<NameValueIf> parmList = new ArrayList<NameValueIf>();

            parmList.add(new NameValue(Eway_au.EWAY_AU_CARD_NUMBER, URLEncoder.encode(
                    pd.getCcNumber(), "UTF-8")));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(Eway_au.EWAY_AU_CARD_CVV2, URLEncoder.encode(
                        pd.getCcCVV(), "UTF-8")));
            }

            parmList.add(new NameValue(Eway_au.EWAY_AU_CARD_EXPIRY_MONTH, URLEncoder.encode(
                    pd.getCcExpiryMonth(), "UTF-8")));
            parmList.add(new NameValue(Eway_au.EWAY_AU_CARD_EXPIRY_YEAR, URLEncoder.encode(
                    pd.getCcExpiryYear(), "UTF-8")));

            parmList.add(new NameValue(Eway_au.EWAY_AU_CARDHOLDERS_NAME, URLEncoder.encode(
                    pd.getCcOwner(), "UTF-8")));

            // Do the post
            String gatewayResp = null;
            try
            {
                gatewayResp = postData(pd, parmList);
            } catch (Exception e)
            {
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

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            // Now process the XML response

            int txReturnAmount = 0;

            String txTrxnNumber = "";

            String txTrxnReference = "";

            String txTrxnOption1 = "";

            String txTrxnOption2 = "";

            String txTrxnOption3 = "";

            boolean txTrxnStatus = false;

            String txAuthCode = "";

            String txTrxnError = "";

            double txBeagleScore = -1;

            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String root = rootnode.getNodeName();

                if (!root.equals("ewayResponse"))
                {
                    throw new Exception("Bad root element in eWay response: " + root);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name.equals("ewayResponse"))
                    {
                        continue;
                    }
                    Text textnode = (Text) node.getFirstChild();
                    String value = "";
                    if (textnode != null)
                    {
                        value = textnode.getNodeValue();
                    }

                    if (name.equals("ewayTrxnError"))
                    {
                        txTrxnError = value;
                    } else if (name.equals("ewayTrxnStatus"))
                    {
                        if (value.toLowerCase().trim().equals("true"))
                        {
                            txTrxnStatus = true;
                        }
                    } else if (name.equals("ewayTrxnNumber"))
                    {
                        txTrxnNumber = value;
                    } else if (name.equals("ewayTrxnOption1"))
                    {
                        txTrxnOption1 = value;
                    } else if (name.equals("ewayTrxnOption2"))
                    {
                        txTrxnOption2 = value;
                    } else if (name.equals("ewayTrxnOption3"))
                    {
                        txTrxnOption3 = value;
                    } else if (name.equals("ewayReturnAmount"))
                    {
                        if (!value.equals(""))
                        {
                            txReturnAmount = Integer.parseInt(value);
                        }
                    } else if (name.equals("ewayAuthCode"))
                    {
                        txAuthCode = value;
                    } else if (name.equals("ewayTrxnReference"))
                    {
                        txTrxnReference = value;
                    } else if (name.equals("ewayBeagleScore"))
                    {
                        if (!value.equals(""))
                        {
                            txBeagleScore = Double.parseDouble(value);
                        }
                    } else
                    {
                        if (log.isWarnEnabled())
                        {
                            log.warn("Unknown field in eWay response: " + name);
                        }
                    }
                }
            } catch (Exception e)
            {
                // Problems parsing the XML

                if (log.isWarnEnabled())
                {
                    log.warn("Problems parsing eWay response: " + e.getMessage());
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

            gatewayResult = txTrxnStatus ? "Successful" : "Failed";
            ipnHistory.setGatewayResult(gatewayResult);

            transactionId = txTrxnNumber;
            ipnHistory.setGatewayTransactionId(transactionId);

            message = txAuthCode;
            errorMessage = txTrxnError;

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(gatewayResp);

            if (log.isDebugEnabled())
            {
                log.debug("eWay response data:\n\t" + " txReturnAmount = " + txReturnAmount
                        + "\n\t txTrxnNumber = " + txTrxnNumber + "\n\t txTrxnReference = "
                        + txTrxnReference + "\n\t txTrxnOption1 = " + txTrxnOption1
                        + "\n\t txTrxnOption2 = " + txTrxnOption2 + "\n\t txTrxnOption3 = "
                        + txTrxnOption3 + "\n\t txTrxnStatus = " + txTrxnStatus
                        + "\n\t txAuthCode = " + txAuthCode + "\n\t txTrxnError = " + txTrxnError
                        + "\n\t txBeagleScore = " + txBeagleScore);
            }

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
            if (gatewayResult.equals("Successful"))
            {
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

            } else if (gatewayResult.equals("Failed"))
            {
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
                /*
                 * We only get to here if there was an error from the gateway
                 */
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

        if (desc.length() < 256)
        {
            return desc;
        }

        return desc.substring(0, 255);
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
        HashMap<String, String> hashedParams = hashParameters(pd, ccParmList);

        // Create the message from the parameters in the PaymentDetails object
        StringBuffer sb = new StringBuffer();
        sb.append("<ewaygateway>");
        sb.append("<ewayCustomerID>" + hashedParams.get(Eway_au.EWAY_AU_MERCHANT_ID)
                + "</ewayCustomerID>");
        sb.append("<ewayTotalAmount>" + hashedParams.get(Eway_au.EWAY_AU_PAYMENT_AMOUNT)
                + "</ewayTotalAmount>");
        sb.append("<ewayCustomerFirstName></ewayCustomerFirstName>");
        sb.append("<ewayCustomerLastName></ewayCustomerLastName>");
        sb.append("<ewayCustomerEmail>" + hashedParams.get(Eway_au.EWAY_AU_CUST_EMAIL)
                + "</ewayCustomerEmail>");
        sb.append("<ewayCustomerAddress></ewayCustomerAddress>");
        sb.append("<ewayCustomerPostcode></ewayCustomerPostcode>");
        sb.append("<ewayCustomerInvoiceDescription></ewayCustomerInvoiceDescription>");
        sb.append("<ewayCustomerInvoiceRef></ewayCustomerInvoiceRef>");
        sb.append("<ewayCardHoldersName>" + hashedParams.get(Eway_au.EWAY_AU_CARDHOLDERS_NAME)
                + "</ewayCardHoldersName>");
        sb.append("<ewayCardNumber>" + hashedParams.get(Eway_au.EWAY_AU_CARD_NUMBER)
                + "</ewayCardNumber>");
        sb.append("<ewayCardExpiryMonth>" + hashedParams.get(Eway_au.EWAY_AU_CARD_EXPIRY_MONTH)
                + "</ewayCardExpiryMonth>");
        sb.append("<ewayCardExpiryYear>" + hashedParams.get(Eway_au.EWAY_AU_CARD_EXPIRY_YEAR)
                + "</ewayCardExpiryYear>");
        sb.append("<ewayTrxnNumber></ewayTrxnNumber>");
        sb.append("<ewayOption1></ewayOption1>");
        sb.append("<ewayOption2></ewayOption2>");
        sb.append("<ewayOption3></ewayOption3>");
        sb.append("<ewayCVN>");
        if (pd.isShowCVV())
        {
            sb.append(hashedParams.get(Eway_au.EWAY_AU_CARD_CVV2));
        }
        sb.append("</ewayCVN>");
        sb.append("</ewaygateway>");
        return sb;
    }

    /**
     * Add things specific to eWay to the connection
     */
    @Deprecated
    protected void customizeConnection(HttpURLConnection connection)
    {
        // connection.setInstanceFollowRedirects(false);
    }
}
