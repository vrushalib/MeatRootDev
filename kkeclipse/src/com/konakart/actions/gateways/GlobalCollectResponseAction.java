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

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.payment.globalcollect.GlobalCollect;
import com.konakart.bl.modules.payment.globalcollect.GlobalCollectUtils;
import com.konakart.util.PrettyXmlPrinter;

/**
 * This class is an Action class for what to do when a message is received from GlobalCollect.
 * <p>
 * The result could be one of two types. The first is the first acknowledgement of the order and
 * payment request which contains a FORMACTION tag. The second is the message received at the end of
 * the payment process. We differentiate between the two message types by the presence or absence of
 * the FORMACTION tag.
 */
public class GlobalCollectResponseAction extends GlobalCollectBaseAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(GlobalCollectResponseAction.class);

    private static HashMap<String, String> statusesHash = new HashMap<String, String>();

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected problem: ";

    private static final int RET2 = -2;

    private static final String RET2_DESC = "Not Successful: ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payment successful. TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payment not successful. Decison = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String reconciliationID = null;
        String merchantReference = null;
        String decision = null;
        String customerEmail = null;
        String returnRef = null;
        String returnMAC = null;
        String fraudResult = null;
        String cvvResult = null;

        KKAppEng kkAppEng = null;

        if (log.isDebugEnabled())
        {
            log.debug(GlobalCollect.GLOBALCOLLECT_GATEWAY_CODE + " Response Action");
        }

        try
        {
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
                        if (paramName.equalsIgnoreCase("REF"))
                        {
                            returnRef = paramValue;
                        } else if (paramName.equalsIgnoreCase("RETURNMAC"))
                        {
                            returnMAC = paramValue;
                        } else if (paramName.equalsIgnoreCase("EXTERNALREFERENCE"))
                        {
                            merchantReference = paramValue;
                        } else if (paramName.equalsIgnoreCase("FRAUDRESULT"))
                        {
                            fraudResult = paramValue;
                        } else if (paramName.equalsIgnoreCase("CVVRESULT"))
                        {
                            cvvResult = paramValue;
                        } else
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("Un-processed parameter in response:  '" + paramName
                                        + "' = '" + paramValue + "'");
                            }
                        }
                    }
                }
            }

            String fullGatewayResponse = sb.toString();

            if (log.isDebugEnabled())
            {
                log.debug(GlobalCollect.GLOBALCOLLECT_GATEWAY_CODE + " Raw Response data:\n"
                        + fullGatewayResponse);
                log.debug("\n    REF                    = " + returnRef
                        + "\n    RETURNMAC              = " + returnMAC
                        + "\n    EXTERNALREFERENCE      = " + merchantReference
                        + "\n    FRAUDRESULT            = " + fraudResult
                        + "\n    CVVRESULT              = " + cvvResult);
            }

            // Get an instance of the KonaKart engine
            // kkAppEng = this.getKKAppEng(request); // v3.2 code
            kkAppEng = this.getKKAppEng(request, response); // v4.1 code

            int custId = this.loggedIn(request, response, kkAppEng, "CheckoutDelivery");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                if (log.isInfoEnabled())
                {
                    log.info("Customer is not logged in");
                }
                return KKLOGIN;
            }

            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();

            if (order == null)
            {
                throw new Exception("Unexpected Problem. Checkout order is null.");
            }

            customerEmail = order.getCustomerEmail();
            PaymentDetailsIf pd = order.getPaymentDetails();
            HashMap<String, String> hp = hashParameters(pd, null);

            // Pick out the values from the merchantReference
            // Split the merchantReference into orderId, orderNumber and store information

            if (merchantReference == null)
            {
                String comment = "A merchant reference wasn't received from GlobalCollect";

                // OK for bank transfers
                if (log.isDebugEnabled())
                {
                    log.debug(comment);
                }

                if (log.isDebugEnabled())
                {
                    log.debug("RETURNED_FORMACTION = "
                            + hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_FORMACTION));
                }

                if (hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_FORMACTION).contains(
                        "SHOW_INSTRUCTIONS"))
                {
                    return "CheckoutAwaitBankTransfer";
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { comment });
                addActionError(msg);

                return "CheckoutError";
            }
            StringTokenizer st = new StringTokenizer(merchantReference, "~");

            int orderIdMR = -1;
            String orderIdStrMR = null;
            String timeStrMR = null;

            if (st.hasMoreTokens())
            {
                orderIdStrMR = st.nextToken();
                orderIdMR = Integer.parseInt(orderIdStrMR);
            }
            if (st.hasMoreTokens())
            {
                timeStrMR = st.nextToken();
            }

            if (log.isDebugEnabled())
            {
                log.debug("Derived from merchantReference:         \n"
                        + "    OrderId                = " + orderIdMR + "\n"
                        + "    Time                   = " + timeStrMR + "\n");
            }

            // Verify the correct orderId was returned in the merchant reference

            if (order.getId() != orderIdMR)
            {
                throw new Exception("Unexepcted OrderId in merchant response from GlobalCollect. "
                        + "Received " + orderIdStrMR + " Expected " + order.getId());
            }

            // Verify the RETURNMAC and REF

            if (log.isDebugEnabled())
            {
                log.debug("Check saved RETURNMAC and REF match those just sent:\n"
                        + "    Saved RETURNMAC        = "
                        + hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_MAC)
                        + "\n    Saved RETURNREF        = "
                        + hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_REF));
            }

            if (returnMAC == null || !returnMAC.endsWith(hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_MAC)))
            {
                throw new Exception("Unexepcted MAC returned by GlobalCollect. " + "Received "
                        + returnMAC + " Expected "
                        + hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_MAC));
            }

            if (returnRef == null || !returnRef.endsWith(hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_REF)))
            {
                throw new Exception("Unexepcted REF returned by GlobalCollect. " + "Received "
                        + returnRef + " Expected "
                        + hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_REF));
            }

            // REF =
            // The MerchantID (10 digits)
            // The OrderID (10 digits)
            // The EffortID (5 digits)
            // The AttemptID (5 digits)

            String merchantIdStr = returnRef.substring(0, 10);
            String orderIdStr = returnRef.substring(10, 20);
            String effortIdStr = returnRef.substring(20, 25);
            String attemptIdStr = returnRef.substring(25, 30);

            // Just log these for debug purposes only

            if (log.isDebugEnabled())
            {
                log.debug("Derived from REF parameter in response from GlobalCollect:\n"
                        + "    merchantIdStr          = " + merchantIdStr + "\n"
                        + "    orderIdStr             = " + orderIdStr + "\n"
                        + "    effortId               = " + effortIdStr + "\n"
                        + "    attemptid              = " + attemptIdStr + "\n");
            }

            // Verify the merchantId returned in the REF parameter

            if (!merchantIdStr.endsWith(hp
                    .get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC)))
            {
                throw new Exception("Unexepcted merchant Id specified by GlobalCollect. "
                        + "Received " + merchantIdStr + " Expected "
                        + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC));
            }

            int orderId = -1;

            if (hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID).equals("T"))
            {
                orderId = order.getId();
            } else
            {
                orderId = Integer.parseInt(orderIdStr);
            }

            if (hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID).equals("T"))
            {
                if (!hp.get(GlobalCollect.GLOBALCOLLECT_ORDER_ID).equals(orderIdStr))
                {
                    throw new Exception("Unexepcted OrderId in response from GlobalCollect. "
                            + "Received " + orderIdStr + " Expected "
                            + hp.get(GlobalCollect.GLOBALCOLLECT_ORDER_ID));
                }
            } else if (order.getId() != orderId)
            {
                throw new Exception("Unexepcted OrderId in response from GlobalCollect. "
                        + "Received " + orderId + " Expected " + order.getId());
            }

            // This gateway only requires details of the final price. No tax, sub-total etc.
            GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();
            BigDecimal total = globalCollectUtils.getTotalPrice(order);

            // Find out from GlobalCollect what the status is now

            IpnHistoryIf ipnHistory = new IpnHistory();
            ipnHistory.setModuleCode(getModuleCodeForIpnRecord(order.getPaymentDetails()));
            ipnHistory.setOrderId(orderId);
            ipnHistory.setTxType(getTxTypeForIpnRecord(order.getPaymentDetails()));
            ipnHistory.setTxAmount(total);

            String statusCode = "NOK";

            // Reset the request URL

            if (log.isDebugEnabled())
            {
                log.debug("Reset RequestURL to "
                        + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL));
            }
            pd.setRequestUrl(hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL));

            // Reset postOrGet to get
            pd.setPostOrGet("post");

            // Find the status of the order

            statusCode = sendOrderStatusRequest(kkAppEng, order, ipnHistory, request, hp);

            fullGatewayResponse = ipnHistory.getGatewayFullResponse();
            reconciliationID = ipnHistory.getGatewayTransactionId();

            // We get results returned to us here in the ipnHistory object

            // If we didn't receive a decision, we log a warning and return
            if (statusCode == null)
            {
                decision = "No Decision";
                String msg = "No decision returned for the "
                        + GlobalCollect.GLOBALCOLLECT_GATEWAY_CODE + " module";
                saveIPNrecord(kkAppEng, orderId, ipnHistory.getModuleCode(), fullGatewayResponse,
                        decision, reconciliationID, RET1_DESC + msg, RET1, ipnHistory.getTxType(),
                        ipnHistory.getTxAmount());
                throw new Exception(msg);
            }

            int statusId = ipnHistory.getKonakartResultId();
            String statusIdStr = String.valueOf(statusId);
            int paymentProductId = Integer.valueOf(pd.getSubCode());
            boolean paymentRejected = isPaymentUnsuccessful(statusId, paymentProductId);

            if (paymentRejected)
            {
                decision = "NOT OK - " + statusId;
            } else
            {
                decision = statusCode + " - " + statusId;
            }

            // See if we need to send an email, by looking at the configuration
            String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
            boolean sendEmail = false;
            if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
            {
                sendEmail = true;
            }

            // If we didn't receive an OK decision, we let the user Try Again

            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            if (!statusCode.equals("OK") || paymentRejected)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Payment Not Approved for orderId: " + orderId + " for customer: "
                            + customerEmail + " reason: " + getStatusDescription(statusIdStr));
                }
                saveIPNrecord(kkAppEng, orderId, ipnHistory.getModuleCode(), fullGatewayResponse,
                        decision, reconciliationID, RET2_DESC + getStatusDescription(statusIdStr),
                        RET2, ipnHistory.getTxType(), ipnHistory.getTxAmount());

                String comment = ORDER_HISTORY_COMMENT_KO + decision;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), orderId,
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, sendEmail, comment,
                        updateOrder);
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, orderId, /* success */false);
                }

                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { comment });
                addActionError(msg);

                return "CheckoutError";
            }

            // If successful, we forward to "Approved"

            if (log.isDebugEnabled())
            {
                log.debug("Payment Approved for orderId " + orderId + " for customer "
                        + customerEmail);
            }
            saveIPNrecord(kkAppEng, orderId, ipnHistory.getModuleCode(), fullGatewayResponse,
                    decision, reconciliationID, RET0_DESC + " : "
                            + getStatusDescription(statusIdStr), RET0, ipnHistory.getTxType(),
                    ipnHistory.getTxAmount());

            if (!paymentRejected)
            {
                // This section if the payment was completely successful

                if (log.isDebugEnabled())
                {
                    log.debug("Payment Successful so update Order and Inventory");
                }

                String comment = ORDER_HISTORY_COMMENT_OK + reconciliationID;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), orderId,
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, sendEmail, comment,
                        updateOrder);

                // If the order payment was approved we update the inventory
                kkAppEng.getEng().updateInventory(kkAppEng.getSessionId(), orderId);
            }

            if (sendEmail)
            {
                sendOrderConfirmationMail(kkAppEng, orderId, /* success */true);
            }

            // If we received no exceptions, delete the basket
            kkAppEng.getBasketMgr().emptyBasket();

            if (log.isDebugEnabled())
            {
                log.debug("Forward to Approved");
            }
            return "Approved";

        } catch (Exception e)
        {
            e.printStackTrace();
            return super.handleException(request, e);
        }
    }

    /**
     * Return true if the payment is unsuccessful
     * 
     * @param statusId
     *            the payment status Id returned from GlobalCollect
     * @param paymentProductId
     *            the payment product Id
     * @return true if the payment is unsuccessful
     */
    private boolean isPaymentUnsuccessful(int statusId, int paymentProductId)
    {
        if (log.isDebugEnabled())
        {
            log.debug("statusId = " + statusId + "  paymentProductId = " + paymentProductId);
        }

        if (statusId < 50)
        {
            // Not successful
            if (log.isDebugEnabled())
            {
                log.debug("statusId < 50 - Payment Unsuccessful");
            }
            return true;
        }

        if (statusId == 50 || statusId == 650)
        {
            if (log.isDebugEnabled())
            {
                log.debug("statusId == 50 or 650 - Payment Unsuccessful");
            }
            // Status Not obtained from the bank
            return true;
        }

        if (statusId >= 800 && paymentProductId != 11)
        {
            if (log.isDebugEnabled())
            {
                log.debug("statusId >= 800 && not product 11 - Payment Successful");
            }
            // Payment successful and was confirmed to WebCollect
            return false;
        }

        if (statusId >= 800 && paymentProductId == 11)
        {
            if (log.isDebugEnabled())
            {
                log.debug("statusId >= 800 && product 11 - Payment Unsuccessful");
            }
            // Unclear but WebCollect stopped trying to find the status from the 3rd Party
            return true;
        }


        if (log.isDebugEnabled())
        {
            log.debug("At End - Payment Unsuccessful");
        }
        return true;
    }

    /**
     * Send an "INSERT_ORDERWITHPAYMENT" message to GlobalCollect and process the response.
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns the Url to redirect to or null if something went wrong
     * @throws Exception
     */
    private String sendOrderStatusRequest(KKAppEng kkAppEng, OrderIf order,
            IpnHistoryIf ipnHistory, HttpServletRequest request, HashMap<String, String> hp)
            throws Exception
    {
        PaymentDetailsIf pd = order.getPaymentDetails();

        // Prevent null exceptions
        if (order.getLocale() == null)
        {
            throw new Exception("Locale on order is null");
        }

        StringBuffer msg = new StringBuffer("<XML>");
        msg.append("<REQUEST>");
        msg.append("<ACTION>GET_ORDERSTATUS</ACTION>");

        msg.append("<META>");
        msg.append("<IPADDRESS>" + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_IP)
                + "</IPADDRESS>");
        msg.append("<MERCHANTID>" + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC)
                + "</MERCHANTID>");
        msg.append("<VERSION>2.0</VERSION>");
        msg.append("</META>");

        msg.append("<PARAMS>");

        msg.append("<ORDER>");
        msg.append("<ORDERID>" + hp.get(GlobalCollect.GLOBALCOLLECT_ORDER_ID) + "</ORDERID>");
        msg.append("</ORDER>");

        msg.append("</PARAMS>");
        msg.append("</REQUEST>");
        msg.append("</XML>");

        if (log.isDebugEnabled())
        {
            try
            {
                log.debug("GatewayRequest to https://ps.gcsip.nl/wdl/wdl =\n"
                        + PrettyXmlPrinter.printXml(msg.toString()));
            } catch (Exception e)
            {
                log.debug("Problem parsing the original XML");
                e.printStackTrace();
                log.debug("\n" + msg.toString());
            }
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted Status Response =\n" + gatewayResp);
            try
            {
                log.debug("Formatted Status Response =\n" + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing Status Response : " + e.getMessage());
            }
        }

        // Now process the XML response

        String RESPONSE_RESULT = "NOK";
        String STATUSID = null;
        String REQUESTID = null;
        String ERROR_MSG = "Unknown error from gateway"; // set a default
        int kkResultId = -1;

        if (gatewayResp == null)
        {
            ERROR_MSG = "Empty response from Gateway";
        } else
        {
            GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();
            Map<String, String> xmlMap = globalCollectUtils
                    .parseGlobalCollectResponseToMap(gatewayResp);

            // ERROR_CODE = xmlMap.get("XML.REQUEST.RESPONSE.ERROR.CODE");
            // ERROR_MSG = xmlMap.get("XML.REQUEST.RESPONSE.ERROR.MESSAGE");
            RESPONSE_RESULT = xmlMap.get("XML.REQUEST.RESPONSE.RESULT");
            REQUESTID = xmlMap.get("XML.REQUEST.RESPONSE.META.REQUESTID");
            STATUSID = xmlMap.get("XML.REQUEST.RESPONSE.STATUS.STATUSID");

            // REQUESTID = getNodeValue(node1);
            // EXTERNALREFERENCE = getNodeValue(node1);
            // REF = getNodeValue(node1);
            // PAYMENTREFERENCE = getNodeValue(node1);

            if (log.isDebugEnabled())
            {
                log.debug("Status response data:" + "\n XML.REQUEST.RESPONSE.RESULT           = "
                        + RESPONSE_RESULT + "\n XML.REQUEST.RESPONSE.META.REQUESTID   = "
                        + REQUESTID + "\n XML.REQUEST.RESPONSE.STATUS.STATUSID  = " + STATUSID);
            }

            if (STATUSID != null)
            {
                try
                {
                    kkResultId = Integer.valueOf(STATUSID);
                } catch (Exception e)
                {
                    // Can't recover from this easily
                    log.warn("Couldn't convert STATUSID (" + STATUSID + ") to an Integer");
                    e.printStackTrace();
                }
            }
        }

        /*
         * Save the IPN History record
         */

        if (RESPONSE_RESULT == null)
        {
            String codePlusTxt = getResultDescription(RET1_DESC + ERROR_MSG);
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
            RESPONSE_RESULT = "NOK";
        } else if (RESPONSE_RESULT.equals("OK"))
        {
            // The RESPONSE_RESULT could be OK but still be a rejection

            int paymentProductId = Integer.valueOf(pd.getSubCode());
            if (isPaymentUnsuccessful(kkResultId, paymentProductId))
            {
                ipnHistory.setKonakartResultDescription(RET2_DESC);
            } else
            {
                ipnHistory.setKonakartResultDescription(RET0_DESC);
            }
            ipnHistory.setGatewayTransactionId(REQUESTID);
            ipnHistory.setKonakartResultId(kkResultId);
        } else
        {
            String codePlusTxt = getResultDescription(RET1_DESC + ERROR_MSG);
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
        }

        ipnHistory.setGatewayFullResponse(gatewayResp);

        if (log.isDebugEnabled())
        {
            log.debug("Return IPN History Record: " + ipnHistory.toString());
        }

        return RESPONSE_RESULT;
    }

    private String getStatusDescription(String code)
    {
        if (code == null)
        {
            return "Unknown Status";
        }

        String statusDesc = getStatusesHash().get(code);

        if (statusDesc == null)
        {
            return code;
        }

        return statusDesc;
    }

    /**
     * @return the statusesHash
     */
    public static HashMap<String, String> getStatusesHash()
    {
        if (statusesHash.isEmpty())
        {
            statusesHash.put("0", "CREATED");
            statusesHash.put("55", "PENDING AT CONSUMER");
            statusesHash.put("70", "BANK IS IN DOUBT");
            statusesHash.put("100", "REJECTED");
            statusesHash.put("120", "REJECTED BY BANK");
            statusesHash.put("125", "CANCELLED AT BANK");
            statusesHash.put("130", "FAILED");
            statusesHash.put("140", "EXPIRED AT BANK");
            statusesHash.put("150", "TIMED OUT AT BANK");
            statusesHash.put("160", "DENIED");
            statusesHash.put("500", "FINAL - PAYMENT UNSUCCESSFUL");
            statusesHash.put("525", "CHALLENGED");
            statusesHash.put("550", "REFERRED");
            statusesHash.put("600", "PENDING");
            statusesHash.put("800", "READY");
            statusesHash.put("1000", "PAID");
            statusesHash.put("1010", "ACCOUNT DEBITED");
            statusesHash.put("2100", "REJECTED BY GLOBALCOLLECT");
            statusesHash.put("2110", "REJECTED BY BANK");
        }

        return statusesHash;
    }
}
