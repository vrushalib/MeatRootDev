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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.KKException;
import com.konakart.appif.ManufacturerIf;

/**
 * Base Action for Manufacturer actions. A number of Other actions extend this action in order to
 * share common code.
 * 
 */
public class ManufacturerBaseAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Common code called by all actions that retrieve products per manufacturer
     * 
     * @param kkAppEng
     * @throws KKException
     * @throws KKAppException
     */
    protected void manageRedir(KKAppEng kkAppEng, HttpServletRequest request)
            throws KKAppException, KKException
    {
        ManufacturerIf manu = kkAppEng.getProductMgr().getSelectedManufacturer();

        // Set page title
        String title = kkAppEng.getMsg("seo.manufacturer.title.template");
        title = title.replace("$manufacturer", manu.getName());
        kkAppEng.setPageTitle(title);

        // Set the meta description
        String description = kkAppEng.getMsg("seo.manufacturer.meta.description.template");
        description = description.replace("$manufacturer", manu.getName());
        kkAppEng.setMetaDescription(description);

        // Set the meta keywords
        String keywords = kkAppEng.getMsg("seo.meta.keywords.template");
        keywords = keywords.replace("$category", "");
        keywords = keywords.replace("$manufacturer", manu.getName());
        keywords = keywords.replace("$name", "");
        keywords = keywords.replace("$model", "");

        kkAppEng.setMetaKeywords(keywords);

        // Set bread crumbs
        String url = "ShowProductsForManufacturer.action?manuId=";
        kkAppEng.getNav().set(manu.getName(), url + manu.getId());

        // Set the MANUFACTURERS_VIEWED customer tag for this customer
        kkAppEng.getCustomerTagMgr().addToCustomerTag(TAG_MANUFACTURERS_VIEWED, manu.getId());

    }

    /**
     * Common code called by all actions that retrieve products per manufacturer to do a redirect in
     * order to add SEO data to the URL. The HttpServletResponse is configured to do the 301
     * redirect.
     * 
     * @param kkAppEng
     * @param request
     * @param response
     * @param seoFormat
     *            Code that defines how to format the URL for SEO
     * @param code
     *            The code that identifies which struts action to call after the redirect
     */
    protected void manageAction(KKAppEng kkAppEng, HttpServletRequest request,
            HttpServletResponse response, int seoFormat, int code)
    {
        // Do a redirect
        ManufacturerIf manu = kkAppEng.getProductMgr().getSelectedManufacturer();
        StringBuffer url = request.getRequestURL();

        if (seoFormat == SEO_DIRECTORY)
        {
            int fromIndex = 0;
            int index = 0;
            while ((index = url.indexOf("/", fromIndex)) > -1)
            {
                fromIndex = index + 1;
            }

            url = new StringBuffer(url.substring(0, fromIndex));

            if (manu.getName() != null && manu.getName().length() > 0)
            {
                try
                {
                    url.append(kkURLEncode(manu.getName()));
                    url.append("/");
                } catch (UnsupportedEncodingException e)
                {
                    log.error("Error encoding manufacturer SEO information for URL", e);
                }
            }
            url.append(code + SEO_DELIM);
            url.append(manu.getId());
            url.append(SEO_TYPE);

        } else if (seoFormat == SEO_PARAMETERS)
        {
            // Add the manufacturer id
            url.append("?manuId=");
            url.append(manu.getId());

            // Add seo friendly info
            if (manu.getName() != null && manu.getName().length() > 0
                    && kkAppEng.getMsg("seo.manufacturer.title.template").length() > 0)
            {
                try
                {
                    url.append("&");
                    url.append(URLEncoder.encode(kkAppEng.getMsg("seo.product.manufacturer"), "UTF-8"));
                    url.append("=");
                    url.append(URLEncoder.encode(manu.getName(), "UTF-8"));
                } catch (UnsupportedEncodingException e)
                {
                    log.error("Error encoding manufacturer SEO information for URL", e);
                }
            }
        }

        // Do a 301 redirect (permanent) so that search engines index URL
        response.setHeader("Location", url.toString());
        setupResponseForSEORedirect(response);

    }
}
