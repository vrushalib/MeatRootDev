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

package com.konakart.actions.ipn;

import java.math.BigDecimal;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.authorize.ResponseField;
import net.authorize.sim.Result;

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
 * This class is an Action class that receives the relay response when using AuthorizeNet Direct
 * Post
 */
public class AuthorizeNetCallbackAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(AuthorizeNetCallbackAction.class);

    private static String code = "authorizenet";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There has been an unexpected exception. Please look at the log.";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payment successful. TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payment not successful.";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String transactionType = null, transactionId = null;

        if (log.isDebugEnabled())
        {
            log.debug("*********** AuthorizeNet Callback");
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
                throw new Exception("The callback from AuthorizeNet did not contain a request.");
            }

            /*
             * Get the uuid from the request so that we can look up the SSO Token
             */
            String uuid = request.getParameter("kk_uuid");
            if (uuid == null)
            {
                throw new Exception(
                        "The callback from AuthorizeNet did not contain the 'kk_uuid' parameter.");
            }

            // Get an instance of the KonaKart engine
            kkAppEng = this.getKKAppEng(request, response);

            SSOTokenIf token = kkAppEng.getEng().getSSOToken(uuid, /* deleteToken */false);
            if (token == null)
            {
                throw new Exception("The SSOToken from the AuthorizeNet callback is null");
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

            // Set the name of the Admin class that can be used to perform captures or credits
            ipnHistory
                    .setAdminPaymentClass("com.konakartadmin.modules.payment.authorizenet.AdminPayment");

            /*
             * Use the session of the logged in user to initialise kkAppEng
             */
            try
            {
                kkAppEng.getEng().checkSession(token.getSessionId());
            } catch (KKException e)
            {
                throw new Exception(
                        "The SessionId from the SSOToken in the AuthorizeNet Callback is not valid: "
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

            Result result = null;
            try
            {
                result = Result.createResult(/* apiLoginId */token.getCustom2(), /* MD5HashKey */
                        token.getCustom3(), request.getParameterMap());
            } catch (Exception e1)
            {
                throw new Exception("Unable to create a net.authorize.sim.Result", e1);
            }

            if (result == null)
            {
                throw new Exception("The net.authorize.sim.Result is null");
            }

            int responseCode = result.getResponseCode().getCode();
            int responseReasonCode = result.getReasonResponseCode().getResponseReasonCode();
            String responseReasonText = result.getResponseMap().get(
                    ResponseField.RESPONSE_REASON_TEXT.getFieldName());
            if (result.isApproved())
            {
                transactionId = result.getResponseMap().get(
                        ResponseField.TRANSACTION_ID.getFieldName());
            }

            ipnHistory.setGatewayResult("code-" + responseCode + " Reason-" + responseReasonCode
                    + "-" + responseReasonText);
            ipnHistory.setGatewayTransactionId(transactionId);
            ipnHistory.setGatewayCreditId(transactionId); // Used for future credit transactions

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

                // Capture important variables
                if (paramName != null)
                {
                    if (paramName.equalsIgnoreCase("x_amount"))
                    {
                        try
                        {
                            ipnHistory.setTxAmount(new BigDecimal(paramValue));
                        } catch (Exception e)
                        {
                        }
                    } else if (paramName.equalsIgnoreCase("x_type"))
                    {
                        ipnHistory.setTxType(paramValue);
                    } else if (paramName.equalsIgnoreCase("x_account_number"))
                    {
                        // Save last 4 digits of the CC number in custom1
                        if (paramValue != null && paramValue.length() >= 4)
                        {
                            ipnHistory.setCustom1(paramValue.substring(paramValue.length() - 4,
                                    paramValue.length()));
                        }
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("AuthorizeNet CallBack data:");
                log.debug(sb.toString());
            }

            // Save the full response
            ipnHistory.setGatewayFullResponse(sb.toString());

            /*
             * Update the order. If successful, we update the inventory as well as changing the
             * state of the order.
             */
            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            String comment = null;
            if (result.isApproved())
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
                createResponse(kkAppEng, /* OK */true, request, response);

                // Delete the SSO token
                kkAppEng.getEng().getSSOToken(uuid, /* deleteToken */true);

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
                createResponse(kkAppEng, /* OK */false, request, response);
            }

            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);

            // Only logout if approved since customer may try again otherwise.
            if (result.isApproved())
            {
                kkAppEng.getEng().logout(sessionId);
            }

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
                        createResponse(kkAppEng, /* OK */false, request, response);
                    }
                }
            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The response is an HTML page which immediately redirects to the desired page.
     * 
     * @param kkAppEng
     * @param ok
     * @param request
     * @param response
     * @throws Exception
     */
    private void createResponse(KKAppEng kkAppEng, boolean ok, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        String url = kkAppEng.getConfig(ConfigConstants.KK_BASE_URL);
        if (url == null || url.length() == 0)
        {
            throw new KKException("The KK_BASE_URL has not been defined");
        }
        if (ok)
        {
            url += "CheckoutFinished.action";
        } else
        {
            url += "AuthorizenetDPM.action?e=t";
        }

        if (log.isDebugEnabled())
        {
            log.debug("document.location = " + url);
        }
        
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // Create page to return
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        sb.append("<html><head></head><body><script type=\"text/javascript\">");
        sb.append("document.location = \"");
        sb.append(url);
        sb.append("\";");
        sb.append("</script></body></html>");
        response.getWriter().append(sb);
    }
}
