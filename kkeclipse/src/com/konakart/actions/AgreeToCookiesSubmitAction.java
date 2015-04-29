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
 * Gets called when a customer agrees to accept cookies
 */
public class AgreeToCookiesSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String emailAddr;

    private String msg;

    private boolean error = false;
    
    private String xsrf_token;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Set the cookie so that we don't display the message again
            kkAppEng.setAgreedCookies(true);
            setKKCookie(AGREED_COOKIES, "1", request, response, kkAppEng);

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
     * @return the msg
     */
    public String getMsg()
    {
        return msg;
    }

    /**
     * @param msg
     *            the msg to set
     */
    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    /**
     * @return the error
     */
    public boolean isError()
    {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    public void setError(boolean error)
    {
        this.error = error;
    }

    /**
     * @return the xsrf_token
     */
    public String getXsrf_token()
    {
        return xsrf_token;
    }

    /**
     * @param xsrf_token the xsrf_token to set
     */
    public void setXsrf_token(String xsrf_token)
    {
        this.xsrf_token = xsrf_token;
    }

}
