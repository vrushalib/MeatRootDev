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
import com.konakart.app.KKException;

/**
 * Gets called normally from an eMail where a customer clicks on a link to confirm his registration.
 */
public class EnableCustomerSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            /*
             * Check input parameters
             */
            String secretKey = request.getParameter("key");
            if (secretKey == null || secretKey.length() == 0)
            {
                log.debug("EnableCustomerSubmitAction called with no key parameter");
                return WELCOME;
            }

            /*
             * Attempt to enable the customer
             */
            try
            {
                kkAppEng.getEng().enableCustomer(secretKey);
            } catch (KKException e)
            {
                log.debug("enableCustomer() not successful. Exception message :" + e.getMessage());
                return WELCOME;
            }

            return "Login";

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }
}
