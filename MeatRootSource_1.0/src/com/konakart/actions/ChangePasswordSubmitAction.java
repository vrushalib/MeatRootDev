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
 * Gets called after submitting the change password form.
 */
public class ChangePasswordSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String currentPassword;

    private String password;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        KKAppEng kkAppEng;
        try
        {
            kkAppEng = this.getKKAppEng(request, response);
        } catch (Exception e1)
        {
            return super.handleException(request, e1);
        }

        try
        {
            int custId;

            custId = this.loggedIn(request, response, kkAppEng, "ChangePassword");

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

            // Call the engine to change the password
            kkAppEng.getCustomerMgr().changePassword(getCurrentPassword(), getPassword());

            return SUCCESS;

        } catch (Exception e)
        {
            return getForward(request, e, "com.konakart.app.KKPasswordDoesntMatchException",
                    kkAppEng.getMsg("change.password.body.error"), "Error");
        }
    }

    /**
     * @return the currentPassword
     */
    public String getCurrentPassword()
    {
        return currentPassword;
    }

    /**
     * @param currentPassword
     *            the currentPassword to set
     */
    public void setCurrentPassword(String currentPassword)
    {
        this.currentPassword = currentPassword;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

}
