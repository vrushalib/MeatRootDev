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

import com.konakart.al.ProductFetch;
import com.konakart.al.KKAppEng;
import com.konakart.app.ProductSearch;
import com.konakart.appif.ProductSearchIf;

/**
 * Gets called to fetch a list of products with special prices from a defined category
 */
public class ShowSpecialsAction extends BaseAction
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

            custId = this.loggedIn(request, response, kkAppEng, null);

            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId,/* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            String catId = request.getParameter("catId");
            int catIdInt = -1;
            if (catId != null)
            {
                try
                {
                    catIdInt = new Integer(catId).intValue();
                } catch (Exception e)
                {

                }
            }
 
            ProductFetch options = new ProductFetch();
            options.setGetSpecials(true);
            
            ProductSearchIf ps = null;
            if (catIdInt != -1)
            {
                ps = new ProductSearch();
                ps.setCategoryId(catIdInt);
                ps.setSearchInSubCats(true);              
            }
            
            kkAppEng.getProductMgr().fetchProducts(null, ps, options, null);

            kkAppEng.getNav().set(kkAppEng.getMsg("header.specials"), request);
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

}
