//
// (c) 2012 DS Data Systems UK Ltd, All rights reserved.
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
import com.konakart.appif.ContentIf;
import com.konakart.util.KKConstants;

/**
 * Gets called before displaying the Help page
 */
public class HelpAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String helpContent;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, null);

            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
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

            // Get the content
            ContentIf[] content = null;

            if (kkAppEng.getContentMgr().isEnabled())
            {
                content = kkAppEng.getContentMgr().getContentForId(1, KKConstants.CONTENTID_HELP);
            }

            if (content != null && content.length > 0)
            {
                helpContent = content[0].getDescription().getContent();
            } else
            {
                helpContent = kkAppEng.getMsg("common.add.info");
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.help"), request);
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the helpContent
     */
    public String getHelpContent()
    {
        return helpContent;
    }

    /**
     * @param helpContent
     *            the helpContent to set
     */
    public void setHelpContent(String helpContent)
    {
        this.helpContent = helpContent;
    }
}
