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
 * This class is an Action class for sending credit card details to USA ePay and receiving
 * confirmation
 */
public class UsaepayAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(UsaepayAction.class);

    // Module name must be the same as the class name although it can be all in lowercase in order
    // to remain compatible with osCommerce.
    private static String code = "usaepay";

    // USA ePay constants for request
    private static final String UMname = "UMname";

    private static final String UMcard = "UMcard";

    private static final String UMcvv2 = "UMcvv2";

    private static final String UMstreet = "UMstreet";

    private static final String UMzip = "UMzip";

    private static final String UMexpir = "UMexpir";

    // USA ePay constants for response
    private static final String UMstatus = "UMstatus";

    private static final String UMrefNum = "UMrefNum";

    private static final String UMerror = "UMerror";

    private static final String Approved = "Approved";

    private static final String Declined = "Declined";

    // private static final String Verification = "Verification";

    private static final String Error = "Error";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET1 = -1;

    private static final String RET1_DESC = "There was an unexpected Gateway Response. Response = ";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "USA ePay payment successful. USA ePay TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "USA ePay payment not successful. USA ePay Reply = ";

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
            parmList.add(new NameValue(UMname, pd.getCcOwner()));
            parmList.add(new NameValue(UMcard, pd.getCcNumber()));
            if (pd.isShowCVV())
            {
                parmList.add(new NameValue(UMcvv2, pd.getCcCVV()));
            }
            if (pd.isShowAddr())
            {
                parmList.add(new NameValue(UMstreet, pd.getCcStreetAddress()));
            }
            if (pd.isShowPostcode())
            {
                parmList.add(new NameValue(UMzip, pd.getCcPostcode()));
            }
            parmList.add(new NameValue(UMexpir, pd.getCcExpiryMonth() + pd.getCcExpiryYear()));

            // Do the post
            String gatewayResp = postData(pd, parmList);

            // Response
            // UMversion=2.9
            // UMstatus=Error
            // UMrefNum=43501025
            // UMauthCode=000000
            // UMavsResult=No%20AVS%20response%20%28Typically%20no%20AVS%20data%20sent%20or%20swiped%20transaction%29
            // UMavsResultCode=
            // UMcvv2Result=No%20CVV2%2FCVC%20data%20available%20for%20transaction.
            // UMcvv2ResultCode=
            // UMvpasResultCode=
            // UMerror=Invalid%20Charge%20Amount
            // UMerrorcode=00000
            // UMresult=E
            // UMbatch=414
            // UMfiller=filled

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            // Process the parameters sent in the callback
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
                        if (nameVal[0].equalsIgnoreCase(UMstatus))
                        {
                            ipnHistory.setGatewayResult(nameVal[1]);
                            gatewayResult = nameVal[1];
                        } else if (nameVal[0].equalsIgnoreCase(UMrefNum))
                        {
                            ipnHistory.setGatewayTransactionId(nameVal[1]);
                            transactionId = nameVal[1];
                        } else if (nameVal[0].equalsIgnoreCase(UMerror))
                        {
                            errorDesc = nameVal[1];
                        }
                    }
                }
            }

            // Put the response in the ipnHistory record
            ipnHistory.setGatewayFullResponse(sb.toString());

            if (log.isDebugEnabled())
            {
                log.debug("Formatted USA ePay response data:");
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
            if (gatewayResult != null && gatewayResult.equals(Approved))
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

            } else if (gatewayResult != null
                    && (gatewayResult.equals(Declined) || gatewayResult.equals(Error)))
            {
                String comment = ORDER_HISTORY_COMMENT_KO + errorDesc;
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
                { errorDesc });
                addActionError(msg);

                // Redirect the user back to the credit card screen
                return "TryAgain";
            } else
            {
                /*
                 * We only get to here if there was an unknown response from the gateway
                 */
                String comment = RET1_DESC + gatewayResult;
                kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                        sendEmail, comment, updateOrder);

                // Save the ipnHistory
                ipnHistory.setKonakartResultDescription(RET1_DESC + gatewayResult);
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
}
