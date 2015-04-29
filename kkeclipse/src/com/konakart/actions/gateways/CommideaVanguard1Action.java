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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.modules.payment.commideavanguard.CommideaVanguard;

/**
 * This class is an Action class which is called by Commidea after receiving the credit card details
 */
public class CommideaVanguard1Action extends CommideaVanguardBaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        /*
         * Create these outside of try / catch since they are needed in the case of a general
         * exception
         */
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(CommideaVanguard.COMMIDEA_VANGUARD_GATEWAY_CODE);
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
            validateOrder(order, CommideaVanguard.COMMIDEA_VANGUARD_GATEWAY_CODE);

            // Set the order id and customer id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());
            ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());

            PaymentDetailsIf pd = order.getPaymentDetails();

            // Put the parameters created by the payment gateway in a hash map
            parmMap = hashParameters(pd, null);

            String retCode = vggetcarddetailsrequest(kkAppEng, order, ipnHistory);
            if (retCode != null && retCode.equals("1"))
            {
                // Payer Authentication is supported

                // Determine whether payer authentication is required
                retCode = vgpayerauthenrollmentcheckrequest(kkAppEng, order,
                        clearIpnHistory(ipnHistory));
                if (retCode != null && retCode.equalsIgnoreCase("Y"))
                {
                    return "3DSecure";
                } else if (retCode != null
                        && (retCode.equalsIgnoreCase("N") || retCode.equalsIgnoreCase("U")))
                {
                    /*
                     * Am authentication check request is sent with a null paRes even though
                     * authentication wasn't performed since the card isn't enrolled.
                     */
                    vgpayerauthauthenticationcheckrequest(kkAppEng, order, ipnHistory, /* paRes */
                            null, "N");

                    // Perform the transaction
                    String txnResult = vgtransactionrequest(kkAppEng, order, ipnHistory);
                    if (txnResult == null)
                    {
                        return super.handleException(request, new KKException(
                                "Unexpected Gateway Response"));
                    } else if (txnResult.equalsIgnoreCase("CHARGED")
                            || txnResult.equalsIgnoreCase("APPROVED")
                            || txnResult.equalsIgnoreCase("AUTHORISED")
                            || txnResult.equalsIgnoreCase("AUTHONLY"))
                    {
                        /*
                         * Send a request to Commidea to get the token id if required
                         */
                        String getTokenId = parmMap.get("getTokenId");
                        if (getTokenId != null && getTokenId.equalsIgnoreCase("true"))
                        {
                            vgtokenregistrationrequest(kkAppEng, order, ipnHistory);
                        }
                        finishUp(kkAppEng, order, /* approved */true, /* transactionId */order
                                .getPaymentDetails().getCustom2());
                        return "Approved";
                    } else
                    {
                        finishUp(kkAppEng, order, /* approved */false, /* transactionId */order
                                .getPaymentDetails().getCustom2());
                        return "CheckoutError";
                    }
                } else
                {
                    return super.handleException(request, new KKException(
                            "Unexpected Gateway Response"));
                }

            } else if (retCode != null && retCode.equalsIgnoreCase("0"))
            {
                // Payer Authentication isn't supported (i.e. AMEX)

                // Perform the transaction
                String txnResult = vgtransactionrequest(kkAppEng, order,
                        clearIpnHistory(ipnHistory));
                if (txnResult == null)
                {
                    return super.handleException(request, new KKException(
                            "Unexpected Gateway Response"));
                } else if (txnResult.equalsIgnoreCase("CHARGED")
                        || txnResult.equalsIgnoreCase("APPROVED")
                        || txnResult.equalsIgnoreCase("AUTHORISED")
                        || txnResult.equalsIgnoreCase("AUTHONLY"))
                {
                    /*
                     * Send a request to Commidea to get the token id if required
                     */
                    String getTokenId = parmMap.get("getTokenId");
                    if (getTokenId != null && getTokenId.equalsIgnoreCase("true"))
                    {
                        vgtokenregistrationrequest(kkAppEng, order, ipnHistory);
                    }
                    finishUp(kkAppEng, order, /* approved */true, /* transactionId */order
                            .getPaymentDetails().getCustom2());
                    return "Approved";
                } else
                {
                    finishUp(kkAppEng, order, /* approved */false, /* transactionId */order
                            .getPaymentDetails().getCustom2());
                    return "CheckoutError";
                }

            } else
            {
                // Set the message and return
                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { RET3_DESC });
                addActionError(msg);

                // Get a new session
                retCode = vggeneratesessionrequest(kkAppEng, order, ipnHistory);
                if (retCode == null || !retCode.equals("0"))
                {
                    return super.handleException(request, new KKException(
                            "Unexpected Gateway Response"));
                }

                // Redirect the user back to the credit card screen
                return "CreditCard";
            }

        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug(RET4_DESC + e.getMessage());
            }
            try
            {
                ipnHistory.setKonakartResultDescription(getResultDescription(RET4_DESC
                        + e.getMessage()));
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
