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
import com.konakart.appif.AddressIf;
import com.konakart.appif.WishListIf;

/**
 * Gets called when moving on to the next page of the checkout process after the delivery address
 * page
 */
public class ChangeGiftRegistryAddrSubmitAction extends BaseAction
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

            custId = this.loggedIn(request, response, kkAppEng, "MyAccount");

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
            
            String addrIdStr = request.getParameter("addrId");
            int addrIdInt;
            try
            {
                addrIdInt = new Integer(addrIdStr).intValue();
            } catch (Exception e)
            {
                return WELCOME;
            }
            

            WishListIf currentWishList = kkAppEng.getWishListMgr().getCurrentWishList();
            if (currentWishList != null)
            {
                currentWishList.setAddressId(addrIdInt);

                // Edit the wish list
                kkAppEng.getWishListMgr().editWishList(currentWishList);

                // Refresh the customer's wish list
                kkAppEng.getWishListMgr().fetchCustomersWishLists();

                // Set the selected address object onto the current wish list
                if (kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses() != null)
                {
                    for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                            .getAddresses().length; i++)
                    {
                        AddressIf addr = kkAppEng.getCustomerMgr().getCurrentCustomer()
                                .getAddresses()[i];
                        if (addr.getId() == currentWishList.getAddressId())
                        {
                            currentWishList.setAddress(addr);
                        }
                    }
                }

            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

}
