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

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.WishListMgr;
import com.konakart.al.WishListUIItem;
import com.konakart.app.Basket;
import com.konakart.appif.BasketIf;
import com.konakart.appif.WishListIf;
import com.konakart.appif.WishListItemIf;

/**
 * Gets called to edit the wish list.
 */
public class EditWishListSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private ArrayList<WishListUIItem> itemList = new ArrayList<WishListUIItem>();

    private int id;

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
            
            if (custId < 0)
            {
                if (kkAppEng.getWishListMgr().allowWishListWhenNotLoggedIn()
                        && kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
                {
                    custId = kkAppEng.getCustomerMgr().getCurrentCustomer().getId();
                } else
                {
                    return KKLOGIN;
                }
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            /*
             * Determine whether action class has been called to remove an item or add it to the
             * wish list
             */
            String action = request.getParameter("action");
            String itemId = request.getParameter("id");
            String wlId = request.getParameter("wid");
            String qtyStr = request.getParameter("qty");
            int wishListItemId = -1;
            int wishListId = -1;
            if (action != null && itemId != null && wlId != null)
            {
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
            }

            /*
             * Find the wish list. It may be passed as an attribute or as a form parameter.
             */
            wishListId = (wishListId == -1) ? getId() : wishListId;
            WishListIf wishList = null;
            if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getWishLists() != null)
            {
                for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer().getWishLists().length; i++)
                {
                    WishListIf wl = kkAppEng.getCustomerMgr().getCurrentCustomer().getWishLists()[i];
                    if (wl.getId() == wishListId)
                    {
                        wishList = wl;
                        kkAppEng.getWishListMgr().setCurrentWishList(wl);
                        break;
                    }
                }
            }

            if (wishList == null || wishList.getWishListItems() == null)
            {
                return SUCCESS;
            }

            if (wishListItemId != -1 && action != null)
            {
                for (int j = 0; j < wishList.getWishListItems().length; j++)
                {
                    WishListItemIf wli = wishList.getWishListItems()[j];
                    if (wli.getId() == wishListItemId)
                    {
                        if (action.equals("a"))
                        {
                            // add the item to the cart
                            if (kkAppEng.getQuotaMgr().canAddToBasket(wli.getProductId(),
                                    wli.getOpts()) > 0)
                            {
                                BasketIf b = new Basket();
                                b.setQuantity(1);
                                b.setOpts(wli.getOpts());
                                b.setProductId(wli.getProductId());
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
                        } else if (action.equals("r"))
                        {
                            // remove the wish list item
                            kkAppEng.getWishListMgr().removeFromWishList(wli);
                        } else if (action.equals("m"))
                        {
                            // subtract 1 from quantity desired
                            // add the wish list item with new quantity
                            wli.setQuantityDesired(((wli.getQuantityDesired() - 1) < 0) ? 0 : (wli
                                    .getQuantityDesired() - 1));
                            kkAppEng.getWishListMgr().addToWishList(wli);
                        } else if (action.equals("p"))
                        {
                            // add 1 to quantity desired
                            // add the wish list item with new quantity
                            wli.setQuantityDesired(wli.getQuantityDesired() + 1);
                            kkAppEng.getWishListMgr().addToWishList(wli);
                        } else if (action.equals("q"))
                        {
                            int quantity = 0;
                            if (qtyStr != null)
                            {
                                try
                                {
                                    quantity = Integer.parseInt(qtyStr);
                                } catch (Exception e)
                                {
                                    return SUCCESS;
                                }
                            } else
                            {
                                return SUCCESS;
                            }
                            if (quantity == 0)
                            {
                                // remove the wish list item
                                kkAppEng.getWishListMgr().removeFromWishList(wli);
                            } else
                            {
                                wli.setQuantityDesired(quantity);
                                kkAppEng.getWishListMgr().addToWishList(wli);
                            }
                        }
                        break;
                    }
                }

            } else
            {
                // If we reach here it's because a priority change is required
                boolean quit = false;
                for (Iterator<WishListUIItem> iterator = itemList.iterator(); iterator.hasNext();)
                {
                    WishListUIItem uiItem = iterator.next();
                    /*
                     * We need to find the WishListItem object corresponding to the WishListUIItem
                     * object and update it if required.
                     */
                    for (int j = 0; j < wishList.getWishListItems().length; j++)
                    {
                        WishListItemIf wli = wishList.getWishListItems()[j];
                        if (wli.getId() == uiItem.getWishListItemId())
                        {
                            if (uiItem.getPriority() != wli.getPriority())
                            {
                                // add the wish list item with new values
                                wli.setPriority(uiItem.getPriority());
                                kkAppEng.getWishListMgr().addToWishList(wli);
                                quit = true;
                                break;
                            }
                        }
                    }
                    if (quit)
                    {
                        break;
                    }
                }
            }

            // Update the wish list data
            kkAppEng.getWishListMgr().fetchCustomersWishLists();

            kkAppEng.getNav().set(kkAppEng.getMsg("header.wishlist.contents"), request);
            if (wishList.getListType() != WishListMgr.WISH_LIST_TYPE)
            {
                /*
                 * When running as a portlet the attribute is lost so we save it in the session
                 */
                if (kkAppEng.isPortlet() && request.getSession() != null)
                {
                    request.getSession().setAttribute("wishListId", wishList.getId());
                } else
                {
                    request.setAttribute("wishListId", wishList.getId());
                }
            }
            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the itemList
     */
    public ArrayList<WishListUIItem> getItemList()
    {
        return itemList;
    }

    /**
     * @param itemList
     *            the itemList to set
     */
    public void setItemList(ArrayList<WishListUIItem> itemList)
    {
        this.itemList = itemList;
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }
}
