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
import com.konakart.al.KKAppException;

/**
 * Gets called before delete address page.
 */
public class DeleteAddrAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);
            
            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "DeleteAddr");
            if (custId < 0)
            {
                return KKLOGIN;
            }

            String addrId = request.getParameter("addrId");

            if (addrId != null)
            {
                kkAppEng.getCustomerMgr().setSelectedAddrFromId(new Integer(addrId).intValue());
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            if (kkAppEng.getCustomerMgr().getSelectedAddr() != null
                    && kkAppEng.getCustomerMgr().getSelectedAddr().getIsPrimary())
            {
                addActionError(kkAppEng.getMsg("address.book.body.deleteerror"));
                return "AddressBook";
            }

            /*
             * If we had to login and have been sent back here, then the parameter will no longer be
             * there, but we we may have already set the selected addr before calling the login page
             */
            if (addrId == null && kkAppEng.getCustomerMgr().getSelectedAddr() == null)
            {
                throw new KKAppException("The addr Id parameter must be initialised");
            }

            if (log.isDebugEnabled())
            {
                log.debug("Addr Id from application = " + addrId);
            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.update.entry"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
