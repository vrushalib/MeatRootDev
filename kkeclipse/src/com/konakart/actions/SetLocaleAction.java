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
import com.konakart.bl.ProductMgr;

/**
 * Changes the locale.
 */
public class SetLocaleAction extends BaseAction
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
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // The locale is passed as a parameter
            String locale = request.getParameter("locale");

            if (locale == null)
            {
                throw new KKAppException("The locale parameter must be initialised");
            }

            if (log.isDebugEnabled())
            {
                log.debug("Locale set from application = " + locale);
            }

            /*
             * The locale should be in the form languageCode_countryCode (i.e. en_GB, it_IT). We
             * need to split up the two codes.
             */
            String[] codes = locale.split("_");
            if (codes.length != 2)
            {
                throw new KKAppException("The locale parameter " + locale
                        + " doesn't have the correct format");
            }

            /*
             * Set the current locale in KonaKart so that calls to the engine use this locale.
             */
            kkAppEng.setLocale(codes[0], codes[1]);

            // Save the current locale in a cookie
            setKKCookie(CUSTOMER_LOCALE, locale, request, response, kkAppEng);

            // Edit the customer to set the new locale
            if (custId > -1 && kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getId() == custId)
            {
                kkAppEng.getCustomerMgr().editCustomerLocale(locale);
            }

            // Reset the state of the objects connected to the session. i.e. Selected product etc.
            kkAppEng.reset();

            // Clear the navigation
            kkAppEng.getNav().clear();

            // Get the new products for all categories
            kkAppEng.getProductMgr().fetchNewProductsArray(/* categoryId */ProductMgr.DONT_INCLUDE, /* fillDescription */
            false, /* force refresh */true);

            // Get the recently viewed products
            kkAppEng.getProductMgr().fetchRecentlyViewedProductsArray(/* fillDescription */false);

            // Fetch all sale items
            kkAppEng.getProductMgr().fetchSpecialsArray(/* categoryId */ProductMgr.DONT_INCLUDE, /* searchInSubCats */
            false, /* fillDescription */false, /* forceRefresh */true);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }
}
