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

package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderTotalIf;

/**
 * Action called before showing the JSP that collects credit card details
 */
public class CheckoutServerPaymentAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String warningMsg;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

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
            if (order == null)
            {
                throw new KKAppException("There is no order.");
            }

            if (order.getPaymentDetails() == null)
            {
                throw new KKAppException("There is no PaymentDetails object attached to the order.");
            }

            if (order.getPaymentDetails().getCode() == null)
            {
                throw new KKAppException(
                        "The PaymentDetails object contains a null code so we cannot determine which payment gateway to use.");
            }

            // Set the warning message
            if (order.getOrderTotals() != null && order.getOrderTotals().length > 0)
            {
                for (int i = 0; i < order.getOrderTotals().length; i++)
                {
                    OrderTotalIf ot = order.getOrderTotals()[i];
                    if (ot.getClassName() != null && ot.getClassName().equals("ot_total"))
                    {
                        if (kkAppEng.getDefaultCurrency().getCode()
                                .equals(kkAppEng.getUserCurrency().getCode()))
                        {
                            warningMsg = kkAppEng.getMsg("checkout.cc.explanation", new String[]
                            { kkAppEng.formatPrice(ot.getValue()) });
                        } else
                        {
                            warningMsg = kkAppEng.getMsg(
                                    "checkout.cc.explanation.other.currency",
                                    new String[]
                                    {
                                            kkAppEng.formatPrice(ot.getValue()),
                                            kkAppEng.formatPrice(ot.getValue(), order.getCurrency()
                                                    .getCode()) });
                        }
                    }
                }
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.checkout"), request);
            kkAppEng.getNav().add(kkAppEng.getMsg("header.payment"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the warningMsg
     */
    public String getWarningMsg()
    {
        return warningMsg;
    }

    /**
     * @param warningMsg
     *            the warningMsg to set
     */
    public void setWarningMsg(String warningMsg)
    {
        this.warningMsg = warningMsg;
    }
}
