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
 * Gets called before the write review page.
 */
public class WriteReviewAction extends BaseAction
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
            custId = this.loggedIn(request, response, kkAppEng, "WriteReview");
            if (custId < 0)
            {
                return KKLOGIN;
            }

            String prodId = request.getParameter("prodId");

            // prodId may be set to null if we are forced to login and then sent back to this
            // action. However, in this case the selected product has already been set, so we use
            // that.
            if (prodId == null && kkAppEng.getProductMgr().getSelectedProduct() == null)
            {
                throw new KKAppException(
                        "The product Id for the review cannot be set to null because the selectedProduct is also set to null");
            } else if (prodId != null && kkAppEng.getProductMgr().getSelectedProduct() != null)
            {
                int prodIdInt = new Integer(prodId).intValue();

                if (kkAppEng.getProductMgr().getSelectedProduct().getId() != prodIdInt)
                {
                    kkAppEng.getProductMgr().fetchSelectedProduct(prodIdInt);
                }
            } else if (prodId != null && kkAppEng.getProductMgr().getSelectedProduct() == null)
            {
                kkAppEng.getProductMgr().fetchSelectedProduct(new Integer(prodId).intValue());
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId,/* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.write.review"), request);
            
            kkAppEng.getReviewMgr().setShowTab(true);
            
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }
}
