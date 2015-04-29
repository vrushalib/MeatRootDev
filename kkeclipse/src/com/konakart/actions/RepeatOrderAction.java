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
import com.konakart.al.KKNotInStockException;

/**
 * Called to repeat an order. Retrieves the id from the orderId parameter.
 */
public class RepeatOrderAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "MyAccount");
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId,/* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            String orderIdStr = request.getParameter("orderId");
            if (orderIdStr == null)
            {
                return "MyAccount";
            }

            if (log.isDebugEnabled())
            {
                log.debug("Order Id from application = " + orderIdStr);
            }

            int orderId;
            try
            {
                orderId = Integer.parseInt(orderIdStr);
            } catch (Exception e)
            {
                return "MyAccount";
            }

            try
            {
                kkAppEng.getOrderMgr().repeatOrder(orderId, /* addToCurrentBasket */false, /* copyCustomFields */
                true);

                /*
                 * The order just created is in kkAppEng.getOrderMgr().getCheckoutOrder(). The
                 * original order is in kkAppEng.getOrderMgr().getSelectedOrder(). Some further
                 * editing may be done here before leaving the action class. See commented code:
                 */

                // Customize an address
                // kkAppEng.getOrderMgr().getCheckoutOrder().setBillingFormattedAddress(
                // "Peter Smith<br>12, Sunny Lane,<br>Stoke On Trent,<br>Staffs");
            } catch (KKNotInStockException e)
            {
                return "ShowCart";
            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
