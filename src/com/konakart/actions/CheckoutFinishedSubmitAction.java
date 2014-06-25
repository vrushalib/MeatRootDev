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
import com.konakart.al.NotifiedProductItem;

/**
 * Gets called after submitting the page that allows you to be notified of products after a
 * successful checkout.
 */
public class CheckoutFinishedSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;
    
    private ArrayList<NotifiedProductItem> itemList = new ArrayList<NotifiedProductItem>();

    private boolean globalNotificationBool;

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

            // Call the engine to remove any notified products
            if (getItemList() != null)
            {
                for (Iterator<NotifiedProductItem> iter = getItemList().iterator(); iter
                        .hasNext();)
                {
                    NotifiedProductItem item = iter.next();
                    // In this case, the remove flag means to add the product
                    if (item.isRemove())
                    {
                        kkAppEng.getCustomerMgr().addProductNotificationsToCustomer(
                                item.getProdId());
                    }
                }
            }

            // Remove checkout order
            kkAppEng.getOrderMgr().setCheckoutOrder(null);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the itemList
     */
    public ArrayList<NotifiedProductItem> getItemList()
    {
        return itemList;
    }

    /**
     * @param itemList the itemList to set
     */
    public void setItemList(ArrayList<NotifiedProductItem> itemList)
    {
        this.itemList = itemList;
    }

    /**
     * @return the globalNotificationBool
     */
    public boolean isGlobalNotificationBool()
    {
        return globalNotificationBool;
    }

    /**
     * @param globalNotificationBool the globalNotificationBool to set
     */
    public void setGlobalNotificationBool(boolean globalNotificationBool)
    {
        this.globalNotificationBool = globalNotificationBool;
    }

}
