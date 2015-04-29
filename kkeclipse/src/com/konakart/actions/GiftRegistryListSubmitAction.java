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
import com.konakart.app.Basket;
import com.konakart.appif.BasketIf;
import com.konakart.appif.WishListIf;
import com.konakart.appif.WishListItemIf;

/**
 * Gets called to add items from a gift registry to the cart
 */
public class GiftRegistryListSubmitAction extends BaseAction
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

            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
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

            /*
             * Get the parameters
             */
            String action = request.getParameter("action");
            String itemId = request.getParameter("id");
            String wlId = request.getParameter("wid");
            int wishListItemId = -1;
            int wishListId = -1;
            if (action != null && itemId != null && wlId != null)
            {
                if (!action.equals("a"))
                {
                    return SUCCESS;
                }
                try
                {
                    wishListItemId = Integer.parseInt(itemId);
                } catch (Exception e1)
                {
                    return SUCCESS;
                }
                try
                {
                    wishListId = Integer.parseInt(wlId);
                } catch (Exception e1)
                {
                    return SUCCESS;
                }
            } else
            {
                return SUCCESS;
            }

            // Find the wish list
            WishListIf wishList = null;
            WishListItemIf[] wishListItems = null;
            if (kkAppEng.getWishListMgr().getCurrentWishList() == null
                    || kkAppEng.getWishListMgr().getCurrentWishList().getId() != wishListId)
            {
                wishList = kkAppEng.getWishListMgr().fetchWishList(wishListId);
                if (wishList != null)
                {
                    wishListItems = wishList.getWishListItems();
                }
            } else
            {
                wishList = kkAppEng.getWishListMgr().getCurrentWishList();
                wishListItems = kkAppEng.getWishListMgr().getCurrentWishListItems();
            }

            if (wishList == null || wishListItems == null)
            {
                return WELCOME;
            }

            /*
             * Add the product to the cart
             */
            for (int j = 0; j < wishListItems.length; j++)
            {
                WishListItemIf wli = wishListItems[j];
                if (wli.getId() == wishListItemId)
                {
                    if (kkAppEng.getQuotaMgr().canAddToBasket(wli.getProductId(), wli.getOpts()) > 0)
                    {
                        // add the item to the cart
                        BasketIf b = new Basket();
                        b.setQuantity(1);
                        b.setOpts(wli.getOpts());
                        b.setProductId(wli.getProductId());
                        b.setWishListId(wishList.getId());
                        b.setWishListItemId(wishListItemId);
                        kkAppEng.getBasketMgr().addToBasket(b, /* refresh */true);
                    } else
                    {
                        // Add a message to say quota has been reached
                        addActionMessage(kkAppEng.getMsg(
                                "common.quota.reached",
                                new String[]
                                { String.valueOf(kkAppEng.getQuotaMgr().getQuotaForProduct(
                                        wli.getProductId(), wli.getOpts())) }));
                    }

                }
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.weddinglist.contents"), request);

            // This is only needed if we forward back to the wedding list instead of the cart
            // request.setAttribute("wishListId", wishList.getId());

            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

}
