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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.NotifiedProductItem;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.ProductIf;

/**
 * Gets called before the edit notified products page.
 */
public class EditNotifiedProductsAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private ArrayList<NotifiedProductItem> itemList;

    private boolean globalNotificationBool;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "EditNotifiedProducts");

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

            // Make sure that the customer has his notified products set
            kkAppEng.getCustomerMgr().fetchProductNotificationsPerCustomer();

            // Set the current values to be displayed
            CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            if (cust != null)
            {
                setGlobalNotificationBool((kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getGlobalProdNotifier() == 1) ? true : false);
                if (cust.getProductNotifications() != null
                        && cust.getProductNotifications().length > 0)
                {
                    itemList = new ArrayList<NotifiedProductItem>();
                    for (int i = 0; i < cust.getProductNotifications().length; i++)
                    {
                        ProductIf p = cust.getProductNotifications()[i];
                        NotifiedProductItem npf = new NotifiedProductItem(p.getId(), p.getName());
                        itemList.add(npf);
                    }
                }
            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.product.notifications"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the globalNotificationBool
     */
    public boolean isGlobalNotificationBool()
    {
        return globalNotificationBool;
    }

    /**
     * @param globalNotificationBool
     *            the globalNotificationBool to set
     */
    public void setGlobalNotificationBool(boolean globalNotificationBool)
    {
        this.globalNotificationBool = globalNotificationBool;
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

}
