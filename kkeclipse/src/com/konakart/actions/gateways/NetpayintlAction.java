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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
 * This class is an Action class for sending credit card details to Netpay International and
 * receiving confirmation
 */
public class NetpayintlAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(NetpayintlAction.class);

    // Module name must be the same as the class name - must match the Struts forwarding name
    private static String code = "Netpayintl";

    private static final String APPROVED_CODE = "000";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Netpay International payment successful. Netpay International TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Netpay International payment not successful. Netpay International Reply = ";

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
            parmList.add(new NameValue("CardNum", pd.getCcNumber()));

            // Always need CCV2 for Netpay International
            parmList.add(new NameValue("CVV2", pd.getCcCVV()));
            parmList.add(new NameValue("ExpMonth", pd.getCcExpiryMonth()));
            parmList.add(new NameValue("ExpYear", "20" + pd.getCcExpiryYear()));
            parmList.add(new NameValue("Member", Utils.trim(pd.getCcOwner(), 50)));

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
                    String value;

                    if (params.length == 2)
                    {
                        value = params[1];
                    } else
                    {
                        value = "";
                    }

                    sb.append(param);
                    sb.append(" = ");
                    sb.append(value);

                    if (param.equals("Reply"))
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
                    } else if (param.equals("ReplyDesc"))
                    {
                        errorDesc = value;
                    } else if (param.equals("TransID"))
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
                log.debug("Formatted Netpay International response data:\n" + sb.toString());
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
            if (gatewayResult != null && gatewayResult.equals(APPROVED_CODE))
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
                    log.debug("Save IPN history:" + ipnHistory.toString());
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
            sb.append("=");
            try
            {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e)
            {
                log.warn("UnsupportedEncodingException when encoding " + value + " for "
                        + nv.getName());
                sb.append(value);
            }
        }

        // Add the credit card parameters
        for (Iterator<NameValueIf> iter = ccParmList.iterator(); iter.hasNext();)
        {
            NameValueIf nv = iter.next();
            sb.append("&");

            String value = cleanValue(nv.getValue());

            sb.append(nv.getName());
            sb.append("=");
            try
            {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e)
            {
                log.warn("UnsupportedEncodingException when encoding " + value + " for "
                        + nv.getName());
                sb.append(value);
            }
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
     * Netpay International returns a RESULT code which we translate here to a descriptive String.
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

        String ret = "unknown";
        switch (resultCode)
        {
        case 0:
            ret = "Approved";
            break;
        case 1:
            ret = "Transaction is pending authorization";
            break;

        case 1001:
            ret = "Test Enviroment Only - Soft Decline (Call)";
            break;
        case 1002:
            ret = "Test Enviroment Only - Insufficient Funds";
            break;
        case 500:
            ret = "Merchant company number is invalid";
            break;
        case 501:
            ret = "Merchant is not activated";
            break;
        case 502:
            ret = "Merchant is unauthorized to use this service";
            break;
        case 503:
            ret = "Merchant unauthorized to debit the type of credit card";
            break;
        case 504:
            ret = "Merchant 'authorization only' option is inactive";
            break;
        case 505:
            ret = "Charge amount not in allowed range";
            break;
        case 506:
            ret = "Data is missing, check CardNum, Amount, Currency, Payments, Expiration Date and TypeCredit";
            break;
        case 507:
            ret = "Credit card number is invalid";
            break;
        case 508:
            ret = "Invalid data range - currency, typeCredit, data length";
            break;
        case 509:
            ret = "Credit card number is blocked";
            break;
        case 510:
            ret = "Credit card has expired";
            break;
        case 511:
            ret = "Missing card holder Name";
            break;
        case 512:
            ret = "Missing or incorrect length of card verification number (cvv2)";
            break;
        case 513:
            ret = "Missing government issued id number";
            break;
        case 514:
            ret = "Missing card holder phone number";
            break;
        case 515:
            ret = "Missing card holder email address";
            break;
        case 516:
            ret = "Missing client ip";
            break;
        case 517:
            ret = "Full name is invalid";
            break;
        case 518:
            ret = "TermCode is missing, invalid or not configured";
            break;
        case 519:
            ret = "This card is not certified";
            break;
        case 520:
            ret = "Internal error";
            break;
        case 521:
            ret = "Unable to complete transaction (communication failure)";
            break;
        case 522:
            ret = "System blocked transaction (duplicate transaction with in the last 5 minutes)";
            break;
        case 523:
            ret = "System Error";
            break;
        case 524:
            ret = "Processing System Error";
            break;
        case 525:
            ret = "Daily volume limit exceeded";
            break;
        case 526:
            ret = "Invalid request source";
            break;
        case 527:
            ret = "Service not allowed from your IP Address";
            break;
        case 528:
            ret = "MD5 Signature not correct";
            break;
        case 529:
            ret = "Duplicate transaction";
            break;
        case 531:
            ret = "Currency is invalid or not setup for processing";
            break;
        case 532:
            ret = "RefTransID - can't find original transaction";
            break;
        case 533:
            ret = "Unable to refund more then the original transaction`s Amount";
            break;
        case 535:
            ret = "Invalid RefTransID parameter";
            break;
        case 536:
            ret = "Initial pre-auth transaction not found: check TransApprovalID and Currency";
            break;
        case 537:
            ret = "TrmCode specified does not exist";
            break;
        case 538:
            ret = "Captured amount is higher that authorized";
            break;
        case 540:
            ret = "Billing address - missing address";
            break;
        case 541:
            ret = "Billing address - missing city name";
            break;
        case 542:
            ret = "Billing address - missing zip code";
            break;
        case 543:
            ret = "Billing address - missing or invalid state";
            break;
        case 544:
            ret = "Billing address - missing or invalid country";
            break;
        case 550:
            ret = "Recurring transactions - merchant is unauthorized to use this service";
            break;
        case 551:
            ret = "Recurring transactions - data is missing";
            break;
        case 55555:
            ret = "Returned by financial institution- (NOT PROCCESED/DECLINED), no charge back fee has been applied";
            break;
        case 560:
            ret = "Not Exist in PPC List";
            break;
        case 561:
            ret = "This credit card has been temporarily blocked.";
            break;
        case 562:
            ret = "Card is blacklisted";
            break;
        case 563:
            ret = "This credit card has been temporarily blocked.";
            break;
        case 580:
            ret = "Did not pass fraud detection test";
            break;
        case 581:
            ret = "Negativ Country List Block";
            break;
        case 582:
            ret = "Country IP is blocked";
            break;
        case 583:
            ret = "Weekly charge count limit reached for this credit card";
            break;
        case 584:
            ret = "Weekly charge amount limit reached for this credit card";
            break;
        case 585:
            ret = "Charge count limit reached for this credit card";
            break;
        case 586:
            ret = "Charge amount limit reached for this credit card";
            break;
        case 587:
            ret = "Reached daily limit of failed charges/Blocked 12h";
            break;
        case 588:
            ret = "Incorrect charge amount";
            break;
        case 589:
            ret = "Blocked group of credit cards";
            break;
        case 590:
            ret = "Input parameter is out of range";
            break;
        case 591:
            ret = "Input parameter is too long";
            break;
        case 592:
            ret = "Processor is not available";
            break;
        case 593:
            ret = "Monthly charge count limit reached for this credit card";
            break;
        case 594:
            ret = "Monthly charge amount limit reached for this credit card";
            break;
        case 595:
            ret = "Integration mode - Wrong credit card number";
            break;
        case 596:
            ret = "Integration mode - amount after decimal is equal to or above 50";
            break;
        case 597:
            ret = "Daily charge count limit reached for this credit card";
            break;
        case 598:
            ret = "Daily charge amount limit reached for this credit card";
            break;
        case 599:
            ret = "Declined by issuing bank";
            break;

        default:
            break;
        }

        return ret;
    }
}
