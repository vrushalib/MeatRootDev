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

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

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

/**
 * This class is an Action class for sending credit card details to BluePay and receiving
 * confirmation
 */
public class BluepayAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BluepayAction.class);

    // Module name must be the same as the class name although it can be all in lowercase in order
    // to remain compatible with osCommerce.
    private static String code = "bluepay";

    // BluePay constants for request

    private static final String PAYMENT_ACCOUNT = "PAYMENT_ACCOUNT";

    private static final String CARD_CVV2 = "CARD_CVV2";

    private static final String CARD_EXPIRE = "CARD_EXPIRE";

    // BluePay constants for response
    private static final String TRANS_ID = "TRANS_ID";

    private static final String STATUS = "STATUS";

    private static final String MESSAGE = "MESSAGE";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected Gateway Response. Response = ";

    private static final String RET3_DESC = "There was an unexpected Gateway Response.";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "BluePay payment successful. BluePay TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "BluePay payment not successful. BluePay Reply = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String message = null;
        String gatewayResult = null;
        String transactionId = null;

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(code);
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
            validateOrder(order, code);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            // Create a parameter list for the credit card details.
            PaymentDetailsIf pd = order.getPaymentDetails();

            List<NameValueIf> parmList = new ArrayList<NameValueIf>();
            parmList.add(new NameValue(PAYMENT_ACCOUNT,
                    URLEncoder.encode(pd.getCcNumber(), "UTF-8")));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(CARD_CVV2, URLEncoder.encode(pd.getCcCVV(), "UTF-8")));
            }
            parmList.add(new NameValue(CARD_EXPIRE, URLEncoder.encode(
                    pd.getCcExpiryMonth() + pd.getCcExpiryYear(), "UTF-8")));

            /*
             * Create the TAMPER_PROOF_SEAL which is md5(SECRET KEY + ACCOUNT_ID + TRANS_TYPE +
             * AMOUNT + MASTER_ID + NAME1 + PAYMENT_ACCOUNT). MASTER_ID and NAME1 aren't used so we
             * leave them out.
             */
            String secretKey = pd.getCustom1();
            if (secretKey == null)
            {
                throw new KKException(
                        "The Configuration MODULE_PAYMENT_BLUEPAY_SECRET_KEY must be set"
                                + " to the secret key supplied by BluePay");
            }

            String accountId = pd.getCustom2();
            if (accountId == null)
            {
                throw new KKException(
                        "The Configuration MODULE_PAYMENT_BLUEPAY_ACCOUNT_ID must be set"
                                + " to the 12-digit BluePay Account ID");
            }

            if (log.isDebugEnabled())
            {
                //log.debug("Secret Key = " + secretKey);
                log.debug("Account Id = " + accountId);
            }

            // Get the amount from the payment details object
            String amount = null;
            for (int i = 0; i < pd.getParameters().length; i++)
            {
                NameValueIf nv = pd.getParameters()[i];
                if (nv.getName().equals("AMOUNT"))
                {
                    amount = nv.getValue();
                    break;
                }
            }

            String tps = md5(secretKey + accountId + "SALE" + amount + pd.getCcNumber());
            parmList.add(new NameValue("TAMPER_PROOF_SEAL", tps));

            // Do the post
            String gatewayResp = null;
            try
            {
                gatewayResp = postData(pd, parmList);
            } catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Exception from Bluepay : " + e.getMessage());
                }

                /*
                 * BluePay gives http 400 exceptions if the expiry date is wrong or the cvv is too
                 * short etc. We have to trap these exceptions and return a more friendly message.
                 */
                // Save the ipnHistory
                ipnHistory.setGatewayFullResponse(e.getMessage());
                ipnHistory.setKonakartResultDescription(RET4_DESC + e.getMessage());
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

            // Process the parameters sent in the response
            StringBuffer sb = new StringBuffer();
            String[] parms = gatewayResp.split("&");
            if (parms != null)
            {
                for (int i = 0; i < parms.length; i++)
                {
                    String parm = parms[i];
                    sb.append(parm);
                    sb.append("\n");

                    String[] nameVal = parm.split("=");
                    if (nameVal != null && nameVal.length == 2)
                    {
                        if (nameVal[0].equalsIgnoreCase(STATUS))
                        {
                            ipnHistory.setGatewayResult(nameVal[1]);
                            gatewayResult = nameVal[1];
                        } else if (nameVal[0].equalsIgnoreCase(TRANS_ID))
                        {
                            ipnHistory.setGatewayTransactionId(nameVal[1]);
                            transactionId = nameVal[1];
                        } else if (nameVal[0].equalsIgnoreCase(MESSAGE))
                        {
                            message = nameVal[1];
                        }
                    }
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("Formatted BluePay response data:");
                log.debug(sb.toString());
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

            // Determine whether the request was successful or not.If successful, we update the
            // inventory as well as changing the state of the order
            if (gatewayResult != null && gatewayResult.equals("1"))
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

            } else if (gatewayResult != null && gatewayResult.equals("0"))
            {
                /*
                 * Payment declined
                 */
                String comment = ORDER_HISTORY_COMMENT_KO + message;
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
                { message });
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
                ipnHistory.setKonakartResultDescription(RET1_DESC + message);
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
     * Calculates a hex MD5 based on input.
     * 
     * @param message
     *            String to calculate MD5 of.
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
        StringBuffer cod = new StringBuffer();
        for (int i = 0; i < dig.length; ++i)
        {
            cod.append(Integer.toHexString(0x0100 + (dig[i] & 0x00FF)).substring(1));
        }
        return cod.toString();
    }

    /**
     * Add things specific to BluePay to the connection
     */
    @Deprecated
    protected void customizeConnection(HttpURLConnection connection)
    {
        // connection.setInstanceFollowRedirects(false);
    }
}
