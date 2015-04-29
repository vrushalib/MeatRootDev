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
import com.konakart.appif.ProductSearchIf;
import com.konakart.appif.ProductsIf;
import com.konakart.bl.ConfigConstants;

/**
 * Gets called to perform a quick search based on the searchText parameter.
 */
public class QuickSearchAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String searchText;

    private boolean searchInDesc = false;

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

            if (log.isDebugEnabled())
            {
                log.debug("Search text from application = " + searchText);
            }

            ProductSearchIf ps = new ProductSearch();
            ps.setReturnCategoryFacets(true);
            ps.setReturnManufacturerFacets(true);
            ps.setManufacturerId(ProductSearch.SEARCH_ALL);
            ps.setCategoryId(ProductSearch.SEARCH_ALL);
            ps.setWhereToSearch((searchInDesc) ? ProductSearch.SEARCH_IN_PRODUCT_DESCRIPTION : 0);
            ps.setTokenizeSolrInput(true);
            // Set facets if not using slider. Use default values for now
            if (!kkAppEng.getConfigAsBoolean(ConfigConstants.PRICE_FACETS_SLIDER, true))
            {
                PriceFacetOptions pfo = new PriceFacetOptions();
                pfo.setCreateEmptyFacets(false);
                ps.setPriceFacetOptions(pfo);
            }

            if (getSearchText() != null && getSearchText().length() > 0)
            {
                ps.setSearchText(getSearchText());
            }
            ProductsIf prods = kkAppEng.getProductMgr().fetchProducts(null, ps);
            
            // Try to get some spelling suggestions if no results returned
            if (prods != null && getSearchText() != null && getSearchText().length() > 0
                    && (prods.getProductArray() == null || prods.getProductArray().length == 0)
                    && kkAppEng.isGetSpellingSuggestions())
            {

                kkAppEng.getProductMgr().getSuggestedSpellingItems(getSearchText());
            }

            // Set the SEARCH_STRING customer tag for this customer
            if (getSearchText() != null && getSearchText().length() > 0)
            {
                kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_SEARCH_STRING, getSearchText());
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.navigation.results"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the searchText
     */
    public String getSearchText()
    {
        return searchText;
    }

    /**
     * @param searchText
     *            the searchText to set
     */
    public void setSearchText(String searchText)
    {
        this.searchText = searchText;
    }

    /**
     * @return the searchInDesc
     */
    public boolean isSearchInDesc()
    {
        return searchInDesc;
    }

    /**
     * @param searchInDesc
     *            the searchInDesc to set
     */
    public void setSearchInDesc(boolean searchInDesc)
    {
        this.searchInDesc = searchInDesc;
    }
}
