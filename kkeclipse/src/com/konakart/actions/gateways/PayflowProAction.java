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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
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
import com.konakart.util.Utils;

/**
 * This class is an Action class for sending credit card details to Payflow Pro and receiving
 * confirmation
 */
public class PayflowProAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(PayflowProAction.class);

    // Module name must be the same as the class name - must match the Struts forwarding name
    private static String code = "PayflowPro";

    private static final String APPROVED_CODE = "0";

    private static final String APPROVED_UNDER_REVIEW_CODE = "126";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Payflow Pro payment successful. Payflow Pro TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Payflow Pro payment not successful. Payflow Pro Reply = ";

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String errorDesc = null;
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
            parmList.add(new NameValue("ACCT", pd.getCcNumber()));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue("CVV2", pd.getCcCVV()));
            }
            parmList.add(new NameValue("EXPDATE", pd.getCcExpiryMonth() + pd.getCcExpiryYear()));

            // Do the post
            String gatewayResp = postData(pd, parmList);

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            // Process the parameters returned in the response
            StringBuffer sb = new StringBuffer();
            String[] parms = gatewayResp.split("&");
            if (parms != null)
            {
                for (int i = 0; i < parms.length; i++)
                {
                    String parmAndValue = parms[i];
                    String params[] = parmAndValue.split("=");

                    String param = params[0];
                    String value = params[1];

                    sb.append(param);
                    sb.append(" = ");
                    sb.append(value);

                    if (param.equals("RESULT"))
                    {
                        sb.append(" (" + getRespDesc(value) + ")");
                        gatewayResult = value;
                        if (gatewayResult != null && gatewayResult.length() > 125)
                        {
                            ipnHistory.setGatewayResult(Utils.trim(gatewayResult, 125) + "..");
                        } else
                        {
                            ipnHistory.setGatewayResult(gatewayResult);
                        }
                    } else if (param.equals("RESPMSG"))
                    {
                        errorDesc = value;
                    } else if (param.equals("PNREF"))
                    {
                        transactionId = value;
                        ipnHistory.setGatewayTransactionId(value);
                    }

                    sb.append("\n");
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("errorDesc     = " + errorDesc);
                log.debug("gatewayResult = " + gatewayResult);
                log.debug("transactionId = " + transactionId);
                log.debug("Formatted Payflow Pro response data:\n" + sb.toString());
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
            // inventory as well as changing the state of the order.

            // Note that we treat a 126 (Authorised by under fraud review) as OK here.

            if (gatewayResult != null
                    && (gatewayResult.equals(APPROVED_CODE) || gatewayResult
                            .equals(APPROVED_UNDER_REVIEW_CODE)))
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

                if (log.isDebugEnabled())
                {
                    log.debug("Save IPN history: " + ipnHistory.toString());
                }
                ipnHistory.setGatewayResult(Utils.trim(gatewayResp, 125) + "..");

                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // If we received no exceptions, delete the basket
                kkAppEng.getBasketMgr().emptyBasket();

                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */true);
                }

                return "Approved";
            }
            String comment = ORDER_HISTORY_COMMENT_KO + errorDesc;
            kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                    sendEmail, comment, updateOrder);

            // Save the ipnHistory
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());

            if (log.isDebugEnabled())
            {
                log.debug("Save IPN history:" + ipnHistory.toString());
            }
            kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

            if (sendEmail)
            {
                sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
            }

            String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
            { errorDesc });
            addActionError(msg);

            // Redirect the user back to the credit card screen
            return "TryAgain";
        } catch (Exception e)
        {
            if (log.isWarnEnabled())
            {
                log.warn("Exception sending payment details to " + code + " : " + e.getMessage());
            }

            try
            {
                String msg = RET4_DESC + e.getMessage();
                if (msg.length() > 255)
                {
                    msg = msg.substring(0, 251) + "..";
                }

                ipnHistory.setKonakartResultDescription(msg);
                ipnHistory.setKonakartResultId(RET4);

                if (kkAppEng != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Save IPN history:" + ipnHistory.toString());
                    }
                    kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);
                } else
                {
                    log.warn("kkAppEng is null - cannot save IPN History - this is unexpected");
                }
            } catch (KKException e1)
            {
                return super.handleException(request, e1);
            }
            return super.handleException(request, e);
        }
    }

    /**
     * This method is specialized to customize the format of the request
     * 
     * @param pd
     *            the PaymentDetails
     * @param ccParmList
     *            the credit card parameters
     */
    protected StringBuffer getGatewayRequest(PaymentDetailsIf pd, List<NameValueIf> ccParmList)
    {
        // Create the message from the parameters in the PaymentDetails object
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pd.getParameters().length; i++)
        {
            NameValueIf nv = pd.getParameters()[i];
            if (i > 0)
            {
                sb.append("&");
            }

            String value = cleanValue(nv.getValue());

            sb.append(nv.getName());
            sb.append("[" + value.length() + "]");
            sb.append("=");
            sb.append(value);
        }

        // Add the credit card parameters
        for (Iterator<NameValueIf> iter = ccParmList.iterator(); iter.hasNext();)
        {
            NameValueIf nv = iter.next();
            sb.append("&");

            String value = cleanValue(nv.getValue());

            sb.append(nv.getName());
            sb.append("[" + value.length() + "]");
            sb.append("=");
            sb.append(value);
        }

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest = \n" + RegExpUtils.maskCreditCard(sb.toString()));
        }

        return sb;
    }

    private String cleanValue(String inValue)
    {
        if (inValue == null)
        {
            return "";
        }

        String newValue = Utils.removeCharFromString(inValue, '"');

        return newValue;
    }

    /**
     * Payflow Pro returns a RESULT code which we translate here to a descriptive String.
     * 
     * @param _code
     *            the RESULT code
     * @return Response Description
     */
    private String getRespDesc(String _code)
    {
        int resultCode = -1;

        try
        {
            resultCode = Integer.valueOf(_code);
        } catch (NumberFormatException e)
        {
            // just in case the code isn't a number
            return "Format of RESULT code unexpected";
        }

        if (resultCode < 0)
        {
            return "Communication error";
        }

        String ret = "unknown";
        switch (resultCode)
        {
        case 0:
            ret = "Approved";
            break;
        case 1:
            ret = "User authentication failed";
            break;
        case 2:
            ret = "Invalid tender type";
            break;
        case 3:
            ret = "Invalid transaction type";
            break;
        case 4:
            ret = "Invalid amount format";
            break;
        case 5:
            ret = "Invalid merchant information";
            break;
        case 6:
            ret = "Invalid or unsupported currency code";
            break;
        case 7:
            ret = "Field format error";
            break;
        case 8:
            ret = "Not a transaction server";
            break;
        case 9:
            ret = "Too many parameters or invalid stream";
            break;
        case 10:
            ret = "Too many line items";
            break;
        case 11:
            ret = "Client time-out waiting for response";
            break;
        case 12:
            ret = "Declined";
            break;
        case 13:
            ret = "Referral";
            break;
        case 19:
            ret = "Original transaction ID not found";
            break;
        case 20:
            ret = "Cannot find the customer reference number";
            break;
        case 22:
            ret = "Invalid ABA number";
            break;
        case 23:
            ret = "Invalid account number";
            break;
        case 24:
            ret = "Invalid expiration date";
            break;
        case 25:
            ret = "Invalid Host Mapping";
            break;
        case 26:
            ret = "Invalid vendor account";
            break;
        case 27:
            ret = "Insufficient partner permissions";
            break;
        case 28:
            ret = "Insufficient user permissions";
            break;
        case 29:
            ret = "Invalid XML document";
            break;
        case 30:
            ret = "Duplicate transaction";
            break;
        case 31:
            ret = "Error in adding the recurring profile";
            break;
        case 32:
            ret = "Error in modifying the recurring profile";
            break;
        case 33:
            ret = "Error in canceling the recurring profile";
            break;
        case 34:
            ret = "Error in forcing the recurring profile";
            break;
        case 35:
            ret = "Error in reactivating the recurring profile";
            break;
        case 36:
            ret = "OLTP Transaction failed";
            break;
        case 37:
            ret = "Invalid recurring profile ID";
            break;
        case 50:
            ret = "Insufficient funds available in account";
            break;
        case 51:
            ret = "Exceeds per transaction limit";
            break;
        case 99:
            ret = "General error. See RESPMSG.";
            break;
        case 100:
            ret = "Transaction type not supported by host";
            break;
        case 101:
            ret = "Time-out value too small";
            break;
        case 102:
            ret = "Processor not available";
            break;
        case 103:
            ret = "Error reading response from host";
            break;
        case 104:
            ret = "Timeout waiting for processor response";
            break;
        case 105:
            ret = "Credit error";
            break;
        case 106:
            ret = "Host not available";
            break;
        case 107:
            ret = "Duplicate suppression time-out";
            break;
        case 108:
            ret = "Void error";
            break;
        case 109:
            ret = "Time-out waiting for host response";
            break;
        case 110:
            ret = "Referenced auth (against order) Error";
            break;
        case 111:
            ret = "Capture error";
            break;
        case 112:
            ret = "Failed AVS check";
            break;
        case 113:
            ret = "Merchant sale exceeds the sales cap";
            break;
        case 114:
            ret = "Card Security Code (CSC) Mismatch.";
            break;
        case 115:
            ret = "System busy, try again later";
            break;
        case 116:
            ret = "VPS Internal error";
            break;
        case 117:
            ret = "Failed merchant rule check";
            break;
        case 118:
            ret = "Invalid keywords found in string fields";
            break;
        case 120:
            ret = "Attempt to reference a failed transaction";
            break;
        case 121:
            ret = "Not enabled for feature";
            break;
        case 122:
            ret = "Merchant sale exceeds the credit cap";
            break;
        case 125:
            ret = "Declined by filters";
            break;
        case 126:
            ret = "Authorized but triggered a Fraud Filter";
            break;
        case 127:
            ret = "Not processed by filters";
            break;
        case 128:
            ret = "Declined by merchant";
            break;
        case 132:
            ret = "Card has not been submitted for update";
            break;
        case 133:
            ret = "Data mismatch in HTTP retry request";
            break;
        case 150:
            ret = "Issuing bank timed out";
            break;
        case 151:
            ret = "Issuing bank unavailable";
            break;
        case 201:
            ret = "Order error";
            break;
        case 1000:
            ret = "Generic host error";
            break;

        default:
            break;
        }

        return ret;
    }
}
