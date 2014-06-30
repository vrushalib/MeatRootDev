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

/**
 * Gets called before the login page.
 */
public class LoginAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private int rewardPointsAvailable;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;
            
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            /*
             * If we are doing a general login, then reset all of the selected products, categories
             * etc. If we have been redirected to the login page because we wanted to do something
             * that required a qualified session (such as writing a review), then we don't want to
             * reset everything
             */
            if (kkAppEng.getForwardAfterLogin() == null)
            {
                kkAppEng.reset();
            }

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, null);
            if (custId >= 0)
            {
                kkAppEng.getNav().set(kkAppEng.getMsg("header.my.account"), request);
                return "LoggedIn";
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */true);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.login.page"));

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the rewardPointsAvailable
     */
    public int getRewardPointsAvailable()
    {
        return rewardPointsAvailable;
    }

    /**
     * @param rewardPointsAvailable
     *            the rewardPointsAvailable to set
     */
    public void setRewardPointsAvailable(int rewardPointsAvailable)
    {
        this.rewardPointsAvailable = rewardPointsAvailable;
    }

}
