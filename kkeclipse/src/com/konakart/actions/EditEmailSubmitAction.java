//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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
import com.konakart.app.Customer;
import com.konakart.appif.CustomerIf;

/**
 * Gets called after submitting the edit customer page.
 */
public class EditEmailSubmitAction extends BaseAction
{

    private static final long serialVersionUID = 1L;

    private String emailAddr;

    private String password;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "EditCustomer");

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

            // Check the password
            boolean matches = kkAppEng.getEng().validatePassword(kkAppEng.getSessionId(), password);
            if (!matches)
            {
                addActionError(kkAppEng.getMsg("edit.email.body.password.match"));
                return "ApplicationError";
            }

            CustomerIf currentCustomer = kkAppEng.getCustomerMgr().getCurrentCustomer();

            CustomerIf cust = new Customer();

            // Attributes from current customer
            cust.setId(currentCustomer.getId());
            cust.setType(currentCustomer.getType());
            cust.setGroupId(currentCustomer.getGroupId());

            // Copy the inputs from the form to a customer object
            cust.setEmailAddr(escapeFormInput(getEmailAddr()));

            // Call the engine edit customer method
            try
            {
                kkAppEng.getCustomerMgr().editCustomer(cust);
            } catch (Exception e)
            {
                addActionError(kkAppEng.getMsg("edit.customer.body.user.exists"));
                return "ApplicationError";
            }

            // Add a message to say all OK
            addActionMessage(kkAppEng.getMsg("edit.email.body.ok"));

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the emailAddr
     */
    public String getEmailAddr()
    {
        return emailAddr;
    }

    /**
     * @param emailAddr
     *            the emailAddr to set
     */
    public void setEmailAddr(String emailAddr)
    {
        this.emailAddr = emailAddr;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

}
