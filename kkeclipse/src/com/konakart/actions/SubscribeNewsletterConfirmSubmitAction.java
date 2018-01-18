//
// (c) 2017 DS Data Systems UK Ltd, All rights reserved.
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
import com.konakart.app.NotificationOptions;
import com.konakart.appif.SSOTokenIf;

/**
 * Gets called normally from an eMail where a customer clicks on a link to confirm his newsletter
 * subscription.
 */
public class SubscribeNewsletterConfirmSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String subscriptionMessage;

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
                log.debug("SubscribeNewsletterConfirmSubmitAction called with no key parameter");
                return WELCOME;
            }

            /* Get the token */
            SSOTokenIf token = kkAppEng.getEng().getSSOToken(secretKey, /* deleteToken */true);
            if (token == null)
            {
                throw new Exception(
                        "Unable to retrieve a token using the secret key = " + secretKey);
            }

            NotificationOptions options = new NotificationOptions();
            options.setEmailAddr(token.getCustom1());
            options.setNewsletter(true);
            options.setAllProducts(false);
            options.setCustomerId(token.getCustomerId());
            options.setSessionId(token.getSessionId());

            try
            {
                kkAppEng.getEng().addCustomerNotifications(options);
            } catch (Exception e)
            {
                String userExists = "KKUserExistsException";
                if ((e.getCause() != null
                        && e.getCause().getClass().getName().indexOf(userExists) > -1)
                        || (e.getMessage() != null && e.getMessage().indexOf(userExists) > -1))
                {
                    subscriptionMessage = kkAppEng.getMsg("subscribe.newsletter.confirmation.sign.in");
                }
                return SUCCESS;
            }

            subscriptionMessage = kkAppEng.getMsg("subscribe.newsletter.confirmation.ok");

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the subscriptionMessage
     */
    public String getSubscriptionMessage()
    {
        return subscriptionMessage;
    }

    /**
     * @param subscriptionMessage the subscriptionMessage to set
     */
    public void setSubscriptionMessage(String subscriptionMessage)
    {
        this.subscriptionMessage = subscriptionMessage;
    }


}
