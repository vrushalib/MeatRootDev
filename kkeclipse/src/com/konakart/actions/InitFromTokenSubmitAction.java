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
import com.konakart.appif.CustomerIf;
import com.konakart.appif.SSOTokenIf;

/**
 * Gets called to initiate a user's session based on the data found in the SSO Token object
 */
public class InitFromTokenSubmitAction extends BaseAction
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
                if (log.isDebugEnabled())
                {
                    log.debug("InitFromTokenSubmitAction called with no key parameter");
                }
                return WELCOME;
            }

            /*
             * Get the SSO token
             */
            SSOTokenIf token = kkAppEng.getEng().getSSOToken(secretKey, /* deleteToken */true);

            if (token == null)
            {
                // No token found

                if (log.isDebugEnabled())
                {
                    log.debug("InitFromTokenSubmitAction with secret key but no token found");
                }
                return WELCOME;
            }

            if (token.getSessionId() != null && token.getSessionId().length() > 0)
            {
                /*
                 * Use the session of the logged in user to initialise kkAppEng
                 */
                try
                {
                    kkAppEng.getEng().checkSession(token.getSessionId());
                } catch (KKException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("InitFromTokenSubmitAction called with invalid session Id :"
                                + token.getSessionId());
                    }
                    return WELCOME;
                }

                kkAppEng.getCustomerMgr().loginBySession(token.getSessionId());

                // Set the affiliate id
                String affiliateId = request.getParameter("aid");
                kkAppEng.setAffiliateId(affiliateId);

                return SUCCESS;
            } else if (token.getCustomerId() < 0)
            {
                /*
                 * Set the current customer id with the one in the token and fetch any cart items
                 * that he may have.
                 */
                kkAppEng.getBasketMgr().emptyBasket();
                kkAppEng.getCustomerMgr().logout();
                CustomerIf currentCustomer = kkAppEng.getCustomerMgr().getCurrentCustomer();
                currentCustomer.setId(token.getCustomerId());
                kkAppEng.getBasketMgr().getBasketItemsPerCustomer();
                if (kkAppEng.getWishListMgr().allowWishListWhenNotLoggedIn())
                {
                    kkAppEng.getWishListMgr().fetchCustomersWishLists();
                }

                // Set the affiliate id
                String affiliateId = request.getParameter("aid");
                kkAppEng.setAffiliateId(affiliateId);

                return SUCCESS;
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("InitFromTokenSubmitAction called with token not containing"
                            + " a sessionId or a temp customer id");
                }
                return WELCOME;
            }
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
