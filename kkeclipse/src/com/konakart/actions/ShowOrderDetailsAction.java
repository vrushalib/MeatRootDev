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
import com.konakart.appif.OrderIf;
import com.konakart.util.KKConstants;

/**
 * Called to show the details of a order. Retrieves the id from the orderId parameter.
 */
public class ShowOrderDetailsAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private boolean waitingForApproval = false;

    private boolean canApprove = false;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "ShowOrderDetails");
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

            String orderId = request.getParameter("orderId");
            if (orderId == null)
            {
                return "InvalidOrderId";
            }

            if (log.isDebugEnabled())
            {
                log.debug("Order Id from application = " + orderId);
            }

            OrderIf order = kkAppEng.getOrderMgr().getOrder(new Integer(orderId).intValue());
            if (order == null)
            {
                return "InvalidOrderId";
            }

            if (order.getStatus() == com.konakart.bl.OrderMgr.WAITING_APPROVAL_STATUS)
            {
                waitingForApproval = true;
            }

            canApprove = kkAppEng.getCustomerMgr().getTagValueAsBool(
                    KKConstants.B2B_CAN_APPROVE_ORDERS, false);

            kkAppEng.getNav().add(kkAppEng.getMsg("header.order"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the waitingForApproval
     */
    public boolean isWaitingForApproval()
    {
        return waitingForApproval;
    }

    /**
     * @param waitingForApproval
     *            the waitingForApproval to set
     */
    public void setWaitingForApproval(boolean waitingForApproval)
    {
        this.waitingForApproval = waitingForApproval;
    }

    /**
     * @return the canApprove
     */
    public boolean isCanApprove()
    {
        return canApprove;
    }

    /**
     * @param canApprove
     *            the canApprove to set
     */
    public void setCanApprove(boolean canApprove)
    {
        this.canApprove = canApprove;
    }

}
