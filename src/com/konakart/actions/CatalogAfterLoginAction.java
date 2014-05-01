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
import com.konakart.bl.ConfigConstants;

/**
 * This Action should always get called before showing the MyAccount page. It fetches data from the
 * DB before displaying the page.
 */
public class CatalogAfterLoginAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private int rewardPointsAvailable;

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
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            /*
             * Populate the customer's orders array with the last three orders he made. We need to
             * get them every time this action is called because the state of an order may change
             * through an IPN call back and this would never get shown if we cached the orders when
             * the customer logs in or just after submitting an order.
             */
            kkAppEng.getOrderMgr().populateCustomerOrders();

            /*
             * Get the digital downloads for this customer. The digital downloads are generated
             * after an order has been paid for. The payment notification may occur through a
             * callback and so need to check every time a customer goes to his "MyAccount" page.
             */
            kkAppEng.getProductMgr().fetchDigitalDownloads();

            /*
             * Get the customer wish lists that he may want to edit from this page
             */
            String giftRegistryEnabled = kkAppEng.getConfig(ConfigConstants.ENABLE_GIFT_REGISTRY);
            if (giftRegistryEnabled != null && giftRegistryEnabled.equalsIgnoreCase("TRUE"))
            {
                kkAppEng.getWishListMgr().fetchCustomersWishLists();
            }

            // Set the points available
            String rewardPointsEnabled = kkAppEng.getConfig("ENABLE_REWARD_POINTS");
            if (rewardPointsEnabled != null && rewardPointsEnabled.equalsIgnoreCase("TRUE"))
            {
                setRewardPointsAvailable(kkAppEng.getRewardPointMgr().pointsAvailable());
            }

            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the rewardPointsAvailable
     */
    public int getRewardPointsAvailable()
    {
        return rewardPointsAvailable;
    }

    /**
     * @param rewardPointsAvailable
     *            the rewardPointsAvailable to set
     */
    public void setRewardPointsAvailable(int rewardPointsAvailable)
    {
        this.rewardPointsAvailable = rewardPointsAvailable;
    }

}
