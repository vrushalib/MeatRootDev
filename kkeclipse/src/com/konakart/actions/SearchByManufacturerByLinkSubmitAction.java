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
import com.konakart.app.PriceFacetOptions;
import com.konakart.app.ProductSearch;
import com.konakart.appif.ManufacturerIf;
import com.konakart.appif.ProductSearchIf;
import com.konakart.bl.ConfigConstants;

/**
 * Gets called to search by manufacturers based on the manuId parameter.
 */
public class SearchByManufacturerByLinkSubmitAction extends ManufacturerBaseAction
{
    private static final long serialVersionUID = 1L;

    private String seoManuId;

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

            String manuId = request.getParameter("manuId");
            // If parameter == null action may have been called after an SEO redirect so use
            // seoManuId
            manuId = (manuId == null) ? this.seoManuId : manuId;

            if (manuId == null)
            {
                return WELCOME;
            }

            // Test to see whether the manuId is an integer
            int manuIdInt;
            try
            {
                manuIdInt = new Integer(manuId).intValue();
            } catch (Exception e)
            {
                return WELCOME;
            }

            ManufacturerIf selectedManu = kkAppEng.getProductMgr().getSelectedManufacturer();

            // Check to see whether we are here after the SEO redirect.
            int seoFormat = kkAppEng.getConfigAsInt(ConfigConstants.SEO_URL_FORMAT);
            if (seoFormat == SEO_DIRECTORY)
            {
                if (this.seoManuId != null)
                {
                    if (selectedManu == null || selectedManu.getId() != manuIdInt)
                    {
                        // Instruct the engine to get the data
                        ProductSearchIf ps = new ProductSearch();
                        ps.setReturnCategoryFacets(true);
                        ps.setReturnManufacturerFacets(true);
                        ps.setManufacturerId(manuIdInt);
                        // Set facets if not using slider. Use default values for now
                        if (!kkAppEng.getConfigAsBoolean(ConfigConstants.PRICE_FACETS_SLIDER, true))
                        {
                            PriceFacetOptions pfo = new PriceFacetOptions();
                            pfo.setCreateEmptyFacets(false);
                            ps.setPriceFacetOptions(pfo);
                        }
                        kkAppEng.getProductMgr().fetchProducts(null, ps);
                    }
                    manageRedir(kkAppEng, request);
                    return SUCCESS;
                }
            } else if (seoFormat == SEO_PARAMETERS)
            {
                if (selectedManu != null
                        && selectedManu.getId() == manuIdInt
                        && request.getParameter(kkAppEng.getMsg("seo.product.manufacturer")) != null)
                {
                    manageRedir(kkAppEng, request);
                    return SUCCESS;
                }
            }

            // Instruct the engine to get the data
            ProductSearchIf ps = new ProductSearch();
            ps.setReturnCategoryFacets(true);
            ps.setReturnManufacturerFacets(true);
            ps.setManufacturerId(manuIdInt);
            // Set facets if not using slider. Use default values for now
            if (!kkAppEng.getConfigAsBoolean(ConfigConstants.PRICE_FACETS_SLIDER, true))
            {
                PriceFacetOptions pfo = new PriceFacetOptions();
                pfo.setCreateEmptyFacets(false);
                ps.setPriceFacetOptions(pfo);
            }
            kkAppEng.getProductMgr().fetchProducts(null, ps);

            /*
             * If we have been implemented as a portlet), then we mustn't do a redirect. Also don't
             * do it if SEO is switched off.
             */
            if (kkAppEng.isPortlet()
                    || !(seoFormat == SEO_DIRECTORY || seoFormat == SEO_PARAMETERS))
            {
                manageRedir(kkAppEng, request);
                return SUCCESS;
            }

            // Do a 301 redirect (permanent) so that search engines index the URL
            manageAction(kkAppEng, request, response, seoFormat, SEO_SEARCH_BY_MANU_BY_LINK_CODE);

            return null;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the seoManuId
     */
    public String getSeoManuId()
    {
        return seoManuId;
    }

    /**
     * @param seoManuId
     *            the seoManuId to set
     */
    public void setSeoManuId(String seoManuId)
    {
        this.seoManuId = seoManuId;
    }

}
