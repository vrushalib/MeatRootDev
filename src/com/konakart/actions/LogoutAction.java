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
import com.konakart.appif.CustomerTagIf;

/**
 * Gets called to logout.
 */
public class LogoutAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Get recently viewed products before logging out
            CustomerTagIf prodsViewedTagCust = kkAppEng.getCustomerTagMgr().getCustomerTag(
                    TAG_PRODUCTS_VIEWED);

            // Log out
            kkAppEng.getCustomerMgr().logout();

            // Manage cookies
            manageCookieLogout(request, response, kkAppEng);

            // Set recently viewed products for the guest customer if changed while logged in
            CustomerTagIf prodsViewedTagGuest = kkAppEng.getCustomerTagMgr().getCustomerTag(
                    TAG_PRODUCTS_VIEWED);
            updateRecentlyViewedProducts(kkAppEng, prodsViewedTagCust, prodsViewedTagGuest);

            kkAppEng.getNav().set(kkAppEng.getMsg("header.logout.page"));

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
