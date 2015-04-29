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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
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
import com.konakart.util.RegExpUtils;

/**
 * This class is an Action class for sending credit card details to Caledon and receiving
 * confirmation Typical replies:<br/>
 * TEXT=DECLINED (DUP) &CODE=1007<br/>
 * TEXT=T68414 $12.34&AUTH=T68414&CODE=0000<br/>
 * <p>
 * Note that for testing they need you to use a credit card Expiry Date of August 2014 (still the
 * case despite the date is now after that expiry date - correct in January 2015).
 */
public class CaledonAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(CaledonAction.class);

    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "caledon";

    // Caledon constants for request

    private static final String x_card_num = "CARD";

    private static final String x_exp_date = "EXP";

    private static final String x_card_code = "CVV2";

    // Caledon constants for response
    private static final String respCode = "CODE";

    private static final String respText = "TEXT";

    private static final String respAuth = "AUTH";

    private static final String approved = "0000";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

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

        String gatewayText = null;
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
            parmList.add(new NameValue(x_card_num, pd.getCcNumber()));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(x_card_code, pd.getCcCVV()));
            }
            parmList.add(new NameValue(x_exp_date, pd.getCcExpiryMonth() + pd.getCcExpiryYear()));

            // Add terminal id and pw depending on CC card used
            String caledonTerminalId = null;
            String caledonPassword = null;

            if (log.isDebugEnabled())
            {
                log.debug("CC Type = " + pd.getCcType());
            }

            if (pd.getCcType().equalsIgnoreCase("AMEX"))
            {
                caledonTerminalId = pd.getCustom1();
                caledonPassword = pd.getCustom2();
            } else
            {
                caledonTerminalId = pd.getCustom3();
                caledonPassword = pd.getCustom4();
            }
            if (caledonPassword != null && caledonPassword.length() > 0)
            {
                parmList.add(new NameValue("PASS", caledonPassword));
            }

            if (log.isDebugEnabled())
            {
                log.debug("Before stripping TERMID\n" + pd.toString());
            }

            // Remove TERMID if it's there
            int termIdsFound = 0;
            for (int i = 0; i < pd.getParameters().length; i++)
            {
                NameValue nv = (NameValue) pd.getParameters()[i];
                if (nv.getName().equals("TERMID"))
                {
                    pd.getParameters()[i] = null;
                    termIdsFound++;
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("TERMIDs found : " + termIdsFound);
            }

            // Terminal Id must be first parameter
            NameValue[] nvArray = new NameValue[pd.getParameters().length + 1 - termIdsFound];
            int nxtIdx = 0;
            nvArray[nxtIdx++] = new NameValue("TERMID", caledonTerminalId);
            for (int i = 0; i < pd.getParameters().length; i++)
            {
                NameValue nv = (NameValue) pd.getParameters()[i];
                if (nv != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Set Parameter : " + nv.getName() + " = " + nv.getValue());
                    }
                    nvArray[nxtIdx++] = nv;
                }
            }
            pd.setParameters(nvArray);

            if (log.isDebugEnabled())
            {
                log.debug("After resetting TERMID\n" + pd.toString());
            }

            // Do the GET
            String gatewayResp = getData(pd, parmList);

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            // Process the parameters sent in the callback
            // Typical Replies =
            // TEXT=DECLINED (DUP) &CODE=1007
            // TEXT=T68414 $12.34&AUTH=T68414&CODE=0000
            StringBuffer sb = new StringBuffer();
            String[] parmArray = gatewayResp.split("&");
            for (int i = 0; i < parmArray.length; i++)
            {
                String parm = parmArray[i];
                String[] lParmArray = parm.split("=");
                if (lParmArray.length == 2)
                {
                    String parmName = lParmArray[0];
                    String parmValue = lParmArray[1];
                    if (parmName != null && parmName.equals(respAuth))
                    {
                        ipnHistory.setGatewayTransactionId(parmValue);
                        transactionId = parmValue;
                    } else if (parmName != null && parmName.equals(respCode))
                    {
                        ipnHistory.setGatewayResult(parmValue);
                        gatewayResult = parmValue;
                    } else if (parmName != null && parmName.equals(respText))
                    {
                        gatewayText = parmValue;
                    }

                    sb.append(parmName);
                    sb.append("=");
                    sb.append(parmValue);
                    sb.append("\n");

                } else
                {
                    throw new KKException("Malformed response from Gateway = " + gatewayResp);
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("Formatted Caledon response data:\n" + sb.toString());
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
            if (gatewayResult != null && gatewayResult.equals(approved))
            {
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
            }

            // Get friendly message from message catalog
            String msg = kkAppEng.getMsg("caledon_gateway." + gatewayResult);
            if (!msg.contains("caledon_gateway."))
            {
                gatewayText = msg;
            }

            String comment = ORDER_HISTORY_COMMENT_KO + gatewayText;
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

            String msg1 = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
            { gatewayText });
            addActionError(msg1);

            // Redirect the user back to the credit card screen
            return "TryAgain";

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
     * Sends data to the payment gateway via a GET. Parameters are received from the PaymentDetails
     * object and the credit card parameters that have just been input by the customer are send in a
     * separate list; the ccParmList
     * 
     * TODO: This can be removed in 5.2.0.0 because it has also been placed in
     * BaseGatewayAction.java
     * 
     * @param pd
     * @param ccParmList
     * @return The response to the post
     * @throws IOException
     */
    public String getData(PaymentDetailsIf pd, List<NameValueIf> ccParmList) throws IOException
    {
        // Construct data for GET
        String urlStr = pd.getRequestUrl();
        StringBuffer sbRequest = getGatewayRequest(pd, ccParmList);

        if (log.isDebugEnabled())
        {
            log.debug("GET URL = " + urlStr + RegExpUtils.maskCreditCard(sbRequest.toString()));
        }
        URL url = new URL(urlStr + sbRequest.toString());

        // Send data
        URLConnection conn = url.openConnection();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sbReply = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null)
        {
            sbReply.append(line);
        }
        rd.close();
        String result = sbReply.toString();

        return result;
    }
}
