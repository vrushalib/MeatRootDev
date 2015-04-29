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

/**
 * Gets called to delete a gift registry
 */
public class DeleteGiftRegistryAction extends BaseAction
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

            custId = this.loggedIn(request, response, kkAppEng, null);

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

            String wishListId = request.getParameter("wishListId");
            if (wishListId == null)
            {
                return SUCCESS;

            }
            // Test to see whether the wish list is an integer
            int wishListIdInt;
            try
            {
                wishListIdInt = new Integer(wishListId).intValue();
            } catch (Exception e)
            {
                return SUCCESS;
            }

            // Delete the wish list
            kkAppEng.getWishListMgr().deleteWishList(wishListIdInt);

            // Refresh the customer's wish list
            kkAppEng.getWishListMgr().fetchCustomersWishLists();

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

}
