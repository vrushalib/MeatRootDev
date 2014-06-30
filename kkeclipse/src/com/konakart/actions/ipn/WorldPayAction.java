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
// Original version written by Steven Lohrenz (steven@stevenlohrenz.com) 
// based on a KonaKart example.  
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

/**
 * This class is an Action class for what to do when a payment notification callback is received
 * from WorldPay.
 */
public class WorldPayAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(WorldPayAction.class);

    // Module name must be the same as the class name although it can be all in lowercase in order
    // to remain compatible with osCommerce.
    private static String code = "worldpay";

    // WorldPay constants
    private static final String TRANSACTION_STATUS_PARAMETER = "transStatus";

    private static final String TRANSACTION_ID_PARAMETER = "transId";

    private static final String SECRET_KEY_PARAMETER = "M_cs1";

    private static final String ORDER_ID_PARAMETER = "M_cs2";

    // Transaction results
    private static final String SUCCESSFUL_TRANSACTION_VALUE = "Y";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There has been an unexpected exception. Please look at the log.";

    private static final int RET5 = -5;

    private static final String RET5_DESC = "The order id from the secret key, does not match the order id received in the callback parameter";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "WorldPay payment successful. WorldPay TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "WorldPay payment not successful. WorldPay Payment Status = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // WorldPay Callback parameters

        String transactionType = null, transactionId = null;
        int orderIdFromGateway = -1;

        if (log.isDebugEnabled())
        {
            log.debug("*********** WorldPay Callback");
        }

        // Create the outside of try / catch since they are needed in the case of a general
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
            String uuid = request.getParameter(WorldPayAction.SECRET_KEY_PARAMETER);
            if (uuid == null)
            {
                throw new Exception(
                        "The callback from WorldPay did not contain the 'M_cs1' parameter.");
            }

            // Get an instance of the KonaKart engine
            kkAppEng = this.getKKAppEng(request, response);

            SSOTokenIf token = kkAppEng.getEng().getSSOToken(uuid, /* deleteToken */true);
            if (token == null)
            {
                throw new Exception("The SSOToken from the WorldPay callback is null");
            }

            try
            {
                // Get the order id from custom1
                int orderId = Integer.parseInt(token.getCustom1());
                ipnHistory.setOrderId(orderId);
            } catch (Exception e)
            {
                throw new Exception("The SSOToken does not contain an order id");
            }

            /*
             * Use the session of the logged in user to initialise kkAppEng
             */
            try
            {
                kkAppEng.getEng().checkSession(token.getSessionId());
            } catch (KKException e)
            {
                throw new Exception(
                        "The SessionId from the SSOToken in the WorldPay Callback is not valid: "
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

                // Capture important variables so that we can determine whether the
                // transaction was successful or not
                if (paramName != null)
                {
                    if (WorldPayAction.TRANSACTION_STATUS_PARAMETER.equalsIgnoreCase(paramName))
                    {
                        transactionType = paramValue;
                    } else if (WorldPayAction.TRANSACTION_ID_PARAMETER.equalsIgnoreCase(paramName))
                    {
                        transactionId = paramValue;
                    } else if (WorldPayAction.ORDER_ID_PARAMETER.equalsIgnoreCase(paramName))
                    {
                        try
                        {
                            Integer orderIdInteger = new Integer(paramValue);
                            orderIdFromGateway = orderIdInteger.intValue();
                        } catch (Exception e)
                        {
                            if (log.isInfoEnabled())
                            {
                                log.info("WorldPay callback. Parameter M_cs2 should be the order id but is not a number : "
                                        + paramValue);
                            }
                        }
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("WorldPay CallBack data:");
                log.debug(sb.toString());
            }

            // Fill more details of the IPN history class
            ipnHistory.setGatewayResult(transactionType);
            ipnHistory.setGatewayFullResponse(sb.toString());
            ipnHistory.setGatewayTransactionId(transactionId);

            /*
             * Flag an error if the order id from the secret key doesn't match the order id received
             * in a callback parameter.
             */
            if (ipnHistory.getOrderId() != orderIdFromGateway)
            {
                ipnHistory.setKonakartResultDescription(RET5_DESC);
                ipnHistory.setKonakartResultId(RET5);
                kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                return null;
            }

            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            // If successful, we update the inventory as well as changing the state of the
            // order.
            String comment = null;
            if (SUCCESSFUL_TRANSACTION_VALUE.equalsIgnoreCase(transactionType))
            {
                comment = ORDER_HISTORY_COMMENT_OK + transactionId;
                kkAppEng.getEng().updateOrder(sessionId, ipnHistory.getOrderId(),
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, sendEmail, comment,
                        updateOrder);
                // If the order payment was approved we update the inventory
                kkAppEng.getEng().updateInventory(sessionId, ipnHistory.getOrderId());
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, ipnHistory.getOrderId(), /* success */
                            true);
                }
            } else
            {
                comment = ORDER_HISTORY_COMMENT_KO + transactionType;
                kkAppEng.getEng().updateOrder(sessionId, ipnHistory.getOrderId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, sendEmail, comment,
                        updateOrder);
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, ipnHistory.getOrderId(), /* success */
                            false);
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
