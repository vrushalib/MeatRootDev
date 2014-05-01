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
// Original version contributed by Chris Derham (Atomus Ltd)
//

package com.konakart.actions.ipn;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.actions.gateways.BaseGatewayAction;
import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.payment.moneybookers.MoneyBookersSignature;

/**
 * This class is an Action class for what to do when a payment notification callback is received
 * from MoneyBookers.
 */
public class MoneyBookersAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(MoneyBookersAction.class);

    // Module name must be the same as the class name although it can be all in lower case in order
    // to remain compatible with osCommerce.
    private static String code = "moneyBookers";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET2 = -2;

    private static final String RET2_DESC = "MD5 Signature did not match";

    private static final int RET3 = -3;

    private static final String RET3_DESC = "Unable to obtain order number";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There has been an unexpected exception. Please look at the log.";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "MoneyBookers payment successful. MoneyBookers TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "MoneyBookers payment not successful. MoneyBookers Payment Status = ";

    private static final String STATUS_PROCESSED = "2";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // MoneyBookers Callback parameters
        // Name = pay_to_email Value = merchant@merchant.com
        // Name = pay_from_email Value = payer@moneybookers.com
        // Name = merchant_id Value = 100005
        // Name = customer_id Value = 200005
        // Name = transaction_id Value = A205220
        // Name = mb_transaction_id Value = 200234
        // Name = mb_amount Value = 25.46
        // Name = mb_currency Value = GBP
        // Name = status Value = -2 failed / 2 processed / 0 pending / -1 cancelled
        // Name = md5sig Value = 327638C253A4637199CEBA6642371F20
        // Name = amount Value = 39.60
        // Name = currency Value = EUR
        // Name = payment_type Value = MBD - MB Direct, WLT - e-wallet or PBT - pending bank
        // transfer
        // Name = merchant_fields Value = field1=value1

        String merchantId = null;
        String transactionId = null;
        String secretWord = null;
        String mbAmount = null;
        String mbCurrency = null;
        String status = null;
        String md5sig = null;
        String orderIdString = null;

        if (log.isDebugEnabled())
        {
            log.debug("*********** MoneyBookers Callback");
        }

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setOrderId(-1);
        ipnHistory.setModuleCode(code);

        String sessionId = null;

        KKAppEng kkAppEng = null;
        try
        {

            if (request == null)
            {
                return null;
            }

            /*
             * Get the uuid from the request so that we can look up the SSO Token
             */
            String uuid = request.getParameter("uuid");
            if (uuid == null)
            {
                throw new Exception(
                        "The callback from MoneyBookers did not contain the 'uuid' parameter.");
            }

            // Get an instance of the KonaKart engine
            kkAppEng = this.getKKAppEng(request, response);

            SSOTokenIf token = kkAppEng.getEng().getSSOToken(uuid, /* deleteToken */true);
            if (token == null)
            {
                throw new Exception("The SSOToken from the MoneyBookers callback is null");
            }

            // Get the secret word from the SSO token
            secretWord = token.getCustom1();

            /*
             * Use the session of the logged in user to initialise kkAppEng
             */
            try
            {
                kkAppEng.getEng().checkSession(token.getSessionId());
            } catch (KKException e)
            {
                throw new Exception(
                        "The SessionId from the SSOToken in the MoneyBookers Callback is not valid: "
                                + token.getSessionId());
            }

            // Log in the user
            kkAppEng.getCustomerMgr().loginBySession(token.getSessionId());
            sessionId = token.getSessionId();

            // See if we need to send an email, by looking at the configuration
            String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
            boolean sendEmail = false;
            if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
            {
                sendEmail = true;
            }

            // Process the parameters sent in the callback
            StringBuffer sb = new StringBuffer();
            Enumeration<?> en = request.getParameterNames();
            while (en.hasMoreElements())
            {
                String paramName = (String) en.nextElement();
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
                    if (paramName.equalsIgnoreCase("merchant_id"))
                    {
                        merchantId = paramValue;
                    } else if (paramName.equalsIgnoreCase("transaction_id"))
                    {
                        transactionId = paramValue;
                    } else if (paramName.equalsIgnoreCase("mb_amount"))
                    {
                        mbAmount = paramValue;
                    } else if (paramName.equalsIgnoreCase("mb_currency"))
                    {
                        mbCurrency = paramValue;
                    } else if (paramName.equalsIgnoreCase("status"))
                    {
                        status = paramValue;
                    } else if (paramName.equalsIgnoreCase("md5sig"))
                    {
                        md5sig = paramValue;
                    } else if (paramName.equalsIgnoreCase("orderId"))
                    {
                        orderIdString = paramValue;
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("MoneyBookers CallBack data:\n" + sb.toString());
            }

            // Fill more details of the IPN history class
            ipnHistory.setGatewayResult(status);
            ipnHistory.setGatewayFullResponse(sb.toString());
            ipnHistory.setGatewayTransactionId(transactionId);

            MoneyBookersSignature moneyBookersSignature = new MoneyBookersSignature();
            moneyBookersSignature.setSecretWord(secretWord);
            moneyBookersSignature.setMerchantId(merchantId);
            moneyBookersSignature.setMbAmount(mbAmount);
            moneyBookersSignature.setMbCurrency(mbCurrency);
            moneyBookersSignature.setStatus(status);
            moneyBookersSignature.setTransactionId(transactionId);

            // We save all of this data in the database to keep a record of the callback
            /*
             * Get the order id
             */
            int orderId = -1;
            if (orderIdString != null)
            {
                try
                {
                    orderId = Integer.parseInt(orderIdString);
                } catch (NumberFormatException e)
                {
                    // no-op
                }
            }
            ipnHistory.setOrderId(orderId);

            if (!moneyBookersSignature.matches(md5sig))
            {
                log.warn("MoneyBookers MD5 Signature does not match");
                ipnHistory.setKonakartResultDescription(RET2_DESC);
                ipnHistory.setKonakartResultId(RET2);
                kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                return null;
            }

            if (orderId < 0)
            {
                ipnHistory.setKonakartResultDescription(RET3_DESC);
                ipnHistory.setKonakartResultId(RET3);
                kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                return null;
            }

            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            // If successful, we update the inventory as well as changing the state of the order.
            if (status != null && status.equalsIgnoreCase(STATUS_PROCESSED))
            {
                String comment = ORDER_HISTORY_COMMENT_OK + transactionId;
                kkAppEng.getEng().updateOrder(sessionId, orderId,
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, sendEmail, comment,
                        updateOrder);
                // If the order payment was approved we update the inventory
                kkAppEng.getEng().updateInventory(sessionId, orderId);
                // If we expect no more communication from MoneyBookers for this order we can delete
                // the SecretKey
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, orderId, /* success */true);
                }
            } else
            {
                String comment = ORDER_HISTORY_COMMENT_KO + status;
                kkAppEng.getEng().updateOrder(sessionId, orderId,
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, sendEmail, comment,
                        updateOrder);
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, orderId, /* success */false);
                }
            }

            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);

            return null;

        } catch (Exception e)
        {
            try
            {
                if (sessionId != null)
                {
                    ipnHistory.setKonakartResultDescription(RET4_DESC);
                    ipnHistory.setKonakartResultId(RET4);
                    if (kkAppEng != null)
                    {
                        kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                    }
                }
            } catch (KKException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return null;
        } finally
        {
            if (sessionId != null && kkAppEng != null)
            {
                try
                {
                    kkAppEng.getEng().logout(sessionId);
                } catch (KKException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
