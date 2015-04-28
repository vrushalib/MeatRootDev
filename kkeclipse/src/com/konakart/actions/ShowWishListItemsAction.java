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

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.WishListMgr;
import com.konakart.al.WishListUIItem;
import com.konakart.appif.OptionIf;
import com.konakart.appif.WishListIf;
import com.konakart.appif.WishListItemIf;

/**
 * Called to display the wish list items normally after editing the wish list or after adding a new
 * item to the wish list It uses the EditWishListForm.
 */
public class ShowWishListItemsAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private ArrayList<WishListUIItem> itemList;

    private BigDecimal finalPriceIncTax;

    private BigDecimal finalPriceExTax;

    private int id;

    private String listName;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Ensure that we are logged in
            custId = this.loggedIn(request, response, kkAppEng, null);

            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
            {
                return KKLOGIN;
            }

            if (custId < 0)
            {
                boolean allowWLBool = kkAppEng.getWishListMgr().allowWishListWhenNotLoggedIn();
                if (allowWLBool && kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
                {
                    custId = kkAppEng.getCustomerMgr().getCurrentCustomer().getId();
                } else
                {
                    return KKLOGIN;
                }
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId,/* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            /*
             * If the current customer has wish list items in his wish list, then we have to create
             * a list of WishListUIItem objects and populate them since these are the objects that
             * we will use to display the wish list items on the screen.
             * 
             * The KonaKart server can accommodate multiple wish lists for each customer. However in
             * this application we assume that there will only ever be one wish list for a customer.
             * 
             * To find the correct wish list we first look for a parameter and then an attribute. If
             * neither exist then we find the wish list of type WishListMgr.WISH_LIST_TYPE
             */

            // Find the wish list
            int wishListIdInt = -1;
            String wishListIdStr = request.getParameter("wishListId");
            if (wishListIdStr != null)
            {
                try
                {
                    wishListIdInt = new Integer(wishListIdStr).intValue();
                } catch (Exception e)
                {
                    return "ShowWishListItems";
                }
            }

            if (wishListIdInt == -1)
            {
                Integer wishListIdIntg = null;
                if (kkAppEng.isPortlet() && request.getSession() != null)
                {
                    wishListIdIntg = (Integer) request.getSession().getAttribute("wishListId");
                    request.getSession().removeAttribute("wishListId");
                } else
                {
                    wishListIdIntg = (Integer) request.getAttribute("wishListId");
                }

                if (wishListIdIntg != null)
                {
                    wishListIdInt = wishListIdIntg.intValue();
                }
            }

            WishListIf wishList = null;
            if (wishListIdInt < 0)
            {
                if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                        && kkAppEng.getCustomerMgr().getCurrentCustomer().getWishLists() != null)
                {
                    for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                            .getWishLists().length; i++)
                    {
                        WishListIf wl = kkAppEng.getCustomerMgr().getCurrentCustomer()
                                .getWishLists()[i];
                        if (wl.getListType() == WishListMgr.WISH_LIST_TYPE)
                        {
                            wishList = wl;
                            kkAppEng.getWishListMgr().setCurrentWishList(wl);
                            break;
                        }
                    }
                }
            } else
            {
                if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                        && kkAppEng.getCustomerMgr().getCurrentCustomer().getWishLists() != null)
                {
                    for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                            .getWishLists().length; i++)
                    {
                        WishListIf wl = kkAppEng.getCustomerMgr().getCurrentCustomer()
                                .getWishLists()[i];
                        if (wl != null && wl.getId() == wishListIdInt)
                        {
                            wishList = wl;
                            kkAppEng.getWishListMgr().setCurrentWishList(wl);
                            setListName(wl.getName());
                            break;
                        }
                    }
                }
            }

            if (wishList != null)
            {
                itemList = new ArrayList<WishListUIItem>();

                WishListItemIf[] items = wishList.getWishListItems();

                for (int i = 0; i < items.length; i++)
                {
                    WishListItemIf wli = items[i];
                    if (wli != null && wli.getProduct() != null)
                    {
                        // Create a WishListUIItem
                        WishListUIItem item = new WishListUIItem(wli.getId(), wli.getProduct()
                                .getId(), wli.getProduct().getName(), kkAppEng.getProdImage(
                                wli.getProduct(), KKAppEng.IMAGE_SMALL), wli.getFinalPriceExTax(),
                                wli.getFinalPriceIncTax(), wli.getPriority(),
                                wli.getQuantityDesired(), wli.getQuantityReceived(),
                                wli.getComments());
                        // Set the options of the new WishListUIItem
                        if (wli.getOpts() != null && wli.getOpts().length > 0)
                        {
                            String[] optNameArray = new String[wli.getOpts().length];
                            for (int j = 0; j < wli.getOpts().length; j++)
                            {
                                OptionIf opt = wli.getOpts()[j];
                                if (opt != null)
                                {
                                    if (opt.getType() == com.konakart.app.Option.TYPE_VARIABLE_QUANTITY)
                                    {
                                        optNameArray[j] = opt.getName() + " " + opt.getQuantity()
                                                + " " + opt.getValue();
                                    } else
                                    {
                                        optNameArray[j] = opt.getName() + " " + opt.getValue();
                                    }
                                } else
                                {
                                    optNameArray[j] = "";
                                }
                            }
                            item.setOptNameArray(optNameArray);
                        }

                        // Add the new item to the list
                        getItemList().add(item);

                        /*
                         * Take into account quantity desired
                         */
                        if (item.getQuantityDesired() > 1)
                        {
                            wishList.setFinalPriceExTax(wishList.getFinalPriceExTax().add(
                                    item.getTotalPriceExTax().multiply(
                                            new BigDecimal(item.getQuantityDesired() - 1))));
                            wishList.setFinalPriceIncTax(wishList.getFinalPriceIncTax().add(
                                    item.getTotalPriceIncTax().multiply(
                                            new BigDecimal(item.getQuantityDesired() - 1))));
                        }
                    }
                }
                setFinalPriceExTax(wishList.getFinalPriceExTax());
                setFinalPriceIncTax(wishList.getFinalPriceIncTax());
                setId(wishList.getId());
            }
            kkAppEng.getNav().set(kkAppEng.getMsg("header.wishlist.contents"), request);

            if (wishList != null && wishList.getListType() != WishListMgr.WISH_LIST_TYPE)
            {
                return "ShowGiftRegistryItems";
            }
            return "ShowWishListItems";

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
     * @return the finalPriceIncTax
     */
    public BigDecimal getFinalPriceIncTax()
    {
        return finalPriceIncTax;
    }

    /**
     * @param finalPriceIncTax
     *            the finalPriceIncTax to set
     */
    public void setFinalPriceIncTax(BigDecimal finalPriceIncTax)
    {
        this.finalPriceIncTax = finalPriceIncTax;
    }

    /**
     * @return the finalPriceExTax
     */
    public BigDecimal getFinalPriceExTax()
    {
        return finalPriceExTax;
    }

    /**
     * @param finalPriceExTax
     *            the finalPriceExTax to set
     */
    public void setFinalPriceExTax(BigDecimal finalPriceExTax)
    {
        this.finalPriceExTax = finalPriceExTax;
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

    /**
     * @return the listName
     */
    public String getListName()
    {
        return listName;
    }

    /**
     * @param listName
     *            the listName to set
     */
    public void setListName(String listName)
    {
        this.listName = listName;
    }
}
