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

import java.security.MessageDigest;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.payment.worldpayxmlredirect.WorldPayXMLRedirect;

/**
 * This class is an Action class for receiving the result from WorldPay. The customer is redirected
 * back to this action class from WorldPay. The parameters are used to determine whether the
 * transaction was successful or not.
 */
public class WorldPayXMLRedirectResponseAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(WorldPayXMLRedirectResponseAction.class);

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "WorldPay payment successful. WorldPay Order Key = ";

    // private static final String ORDER_HISTORY_COMMENT_PENDING =
    // "WorldPay payment is pending. WorldPay Order Key = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "WorldPay payment not successful. WorldPay Reply = ";

    private static final String ORDER_HISTORY_COMMENT_KO_1 = " for Order Key  = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(WorldPayXMLRedirect.WP_XML_REDIRECT_GATEWAY_CODE);
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

            // Set the customer id for the IPN history object
            ipnHistory.setCustomerId(custId);

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, WorldPayXMLRedirect.WP_XML_REDIRECT_GATEWAY_CODE);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            String orderKey = null;
            String paymentAmount = null;
            String paymentCurrency = null;
            String paymentStatus = null;
            String mac = null;
            String retCode = null;

            StringBuffer fullResponse = new StringBuffer();
            Enumeration<String> en = request.getParameterNames();
            while (en.hasMoreElements())
            {
                String paramName = en.nextElement();
                String paramValue = request.getParameter(paramName);
                fullResponse.append(pad(paramName, 20) + " = ").append(paramValue).append("\n");
                if (paramName.equals("paymentStatus"))
                {
                    paymentStatus = paramValue;
                } else if (paramName.equals("paymentAmount"))
                {
                    paymentAmount = paramValue;
                } else if (paramName.equals("paymentCurrency"))
                {
                    paymentCurrency = paramValue;
                } else if (paramName.equals("orderKey"))
                {
                    orderKey = paramValue;
                } else if (paramName.equals("mac"))
                {
                    mac = paramValue;
                } else if (paramName.equals("retCode"))
                {
                    retCode = paramValue;
                }
            }

            // Set IPN History data
            ipnHistory.setGatewayFullResponse(fullResponse.toString());
            ipnHistory.setGatewayResult(paymentStatus);
            ipnHistory.setGatewayTransactionId(orderKey);

            if (log.isDebugEnabled())
            {
                log.debug("WorldPay response =\n" + fullResponse.toString());
            }

            // If mac isn't null then we check it
            if (mac != null)
            {
                String sharedSecret = "";
                if (order.getPaymentDetails() != null)
                {
                    sharedSecret = order.getPaymentDetails().getCustom1();
                }

                StringBuffer sb = new StringBuffer();
                sb.append(orderKey);
                sb.append(paymentAmount);
                sb.append(paymentCurrency);
                sb.append(paymentStatus);
                sb.append(sharedSecret);

                String hashedValue = md5(sb.toString());

                if (!hashedValue.equals(mac))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("String before hashing = " + sb.toString());
                        log.debug("String after hashing  = " + hashedValue);
                    }

                    if (log.isWarnEnabled())
                    {
                        log.warn("WorldPay Response does not pass security test: ");
                        log.warn("mac from WorldPay : " + mac);
                        log.warn("Hashed String     : " + hashedValue);
                    }

                    // Save the ipnHistory
                    ipnHistory
                            .setKonakartResultDescription(getResultDescription("WorldPay Response does not pass security test:"
                                    + "\nmac from WorldPay : "
                                    + mac
                                    + "\nHashed String : "
                                    + hashedValue));
                    ipnHistory.setKonakartResultId(RET4);
                    kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                    // Redirect the user to an error screen
                    return "CheckoutError";
                }
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

            if (paymentStatus != null && paymentStatus.equals("AUTHORISED"))
            {
                String comment = ORDER_HISTORY_COMMENT_OK + orderKey;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Update the inventory
                kkAppEng.getOrderMgr().updateInventory(order.getId());

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET0_DESC);
                ipnHistory.setKonakartResultId(RET0);
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // If we received no exceptions, delete the basket
                kkAppEng.getBasketMgr().emptyBasket();

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */true);
                }

                return "Approved";

            } else if (retCode != null && retCode.equals("pending"))
            {

                /*
                 * Reach here if paying by bank transfer so we leave the order in a waiting for
                 * payment state although we could create a new "pending" state that differentiates
                 * between orders that haven't been paid for and orders that have been paid but just
                 * waiting for outcome.
                 */
                // String comment = ORDER_HISTORY_COMMENT_PENDING + orderKey;
                // kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                // com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
                // sendEmail, comment, updateOrder);

                // Update the inventory
                // kkAppEng.getOrderMgr().updateInventory(order.getId());

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET0_DESC);
                ipnHistory.setKonakartResultId(RET0);
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // If we received no exceptions, delete the basket
                kkAppEng.getBasketMgr().emptyBasket();

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */true);
                }

                return "Approved";

            } else
            {
                String comment = ORDER_HISTORY_COMMENT_KO + paymentStatus
                        + ORDER_HISTORY_COMMENT_KO_1 + orderKey;
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
                return "CheckoutError";
            }

        } catch (Exception e)
        {
            try
            {
                ipnHistory.setKonakartResultDescription(RET4_DESC + e.getMessage());
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
        } else if (desc.length() <= 255)
        {
            return desc;
        }

        return desc.substring(0, 255);
    }

    /**
     * Calculates a hex MD5 based on input.
     * 
     * @param message
     *            String to calculate MD5 of.
     * 
     */
    private String md5(String message) throws java.security.NoSuchAlgorithmException
    {
        MessageDigest md5 = null;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        } catch (java.security.NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
            if (log.isDebugEnabled())
            {
                log.debug(ex);
            }
            throw ex;
        }
        byte[] dig = md5.digest(message.getBytes());
        StringBuffer code = new StringBuffer();
        for (int i = 0; i < dig.length; ++i)
        {
            code.append(Integer.toHexString(0x0100 + (dig[i] & 0x00FF)).substring(1));
        }
        return code.toString();
    }

    /**
     * Used for reporting purposes
     * 
     * @param str
     * @param chars
     * @return a String right-padded with spaces to the specified length
     */
    private String pad(String str, int chars)
    {
        if (str == null)
        {
            return str;
        }
        int charsToAdd = chars - str.length();
        String tempStr = str;
        for (int i = 0; i < charsToAdd; i++)
        {
            tempStr += " ";
        }
        return tempStr;
    }
}
