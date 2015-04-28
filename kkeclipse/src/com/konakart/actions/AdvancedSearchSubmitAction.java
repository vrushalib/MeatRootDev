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

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;

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
 * Performs a search based on data from the SearchProduct
 */
public class AdvancedSearchSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String priceFromStr;

    private String priceToStr;

    private Date dateAddedFrom;

    private Date dateAddedTo;

    private int manufacturerId = -100;

    private int categoryId = -100;

    private boolean searchInDescription = false;

    private String searchText;

    private String custom1;

    private String custom2;

    private String custom3;

    private String custom4;

    private String custom5;

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

            // Instantiate a ProductSearch object required by the engine
            ProductSearchIf ps = new ProductSearch();
            ps.setReturnCategoryFacets(true);
            ps.setReturnManufacturerFacets(true);

            // Set facets if not using slider. Use default values for now
            if (!kkAppEng.getConfigAsBoolean(ConfigConstants.PRICE_FACETS_SLIDER, true))
            {
                PriceFacetOptions pfo = new PriceFacetOptions();
                pfo.setCreateEmptyFacets(false);
                ps.setPriceFacetOptions(pfo);
            }

            // Populate the ProductSearch object from the form
            ps.setCategoryId(getCategoryId());

            if (getDateAddedFrom() != null)
            {
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(getDateAddedFrom());
                ps.setDateAddedFrom(gc);
            }

            if (getDateAddedTo() != null)
            {
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(getDateAddedTo());
                ps.setDateAddedTo(gc);
            }

            ps.setManufacturerId(getManufacturerId());

            if (getPriceFromStr() != null && getPriceFromStr().length() > 0)
            {
                ps.setPriceFrom(new BigDecimal(getPriceFromStr()));

                /*
                 * If the user currency is different to the default currency then we need to convert
                 * it into the default currency.
                 */
                if (!kkAppEng.getDefaultCurrency().getCode()
                        .equals(kkAppEng.getUserCurrency().getCode()))
                {
                    ps.setPriceFrom(ps.getPriceFrom().divide(kkAppEng.getUserCurrency().getValue(),
                            5, BigDecimal.ROUND_UP));
                }
            }

            if (getPriceToStr() != null && getPriceToStr().length() > 0)
            {
                ps.setPriceTo(new BigDecimal(getPriceToStr()));

                /*
                 * If the user currency is different to the default currency then we need to convert
                 * it into the default currency.
                 */
                if (!kkAppEng.getDefaultCurrency().getCode()
                        .equals(kkAppEng.getUserCurrency().getCode()))
                {
                    ps.setPriceTo(ps.getPriceTo().divide(kkAppEng.getUserCurrency().getValue(), 5,
                            BigDecimal.ROUND_UP));
                }
            }

            if (getSearchText() != null && getSearchText().length() > 0)
            {
                ps.setSearchText(getSearchText());
                ps.setTokenizeSolrInput(true);
            }

            if (isSearchInDescription())
            {
                ps.setWhereToSearch(ProductSearch.SEARCH_IN_PRODUCT_DESCRIPTION);
            } else
            {
                ps.setWhereToSearch(0);
            }

            // Set the SEARCH_STRING customer tag for this customer
            if (getSearchText() != null && getSearchText().length() > 0)
            {
                kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_SEARCH_STRING, getSearchText());
            }

            // Call the engine to do the product search
            ProductsIf prods = kkAppEng.getProductMgr().fetchProducts(null, ps);

            // Try to get some spelling suggestions if no results returned
            if (prods != null && getSearchText() != null && getSearchText().length() > 0
                    && (prods.getProductArray() == null || prods.getProductArray().length == 0)
                    && kkAppEng.isGetSpellingSuggestions())
            {

                kkAppEng.getProductMgr().getSuggestedSpellingItems(getSearchText());
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.navigation.results"), request);
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the manufacturerId
     */
    public int getManufacturerId()
    {
        return manufacturerId;
    }

    /**
     * @param manufacturerId
     *            the manufacturerId to set
     */
    public void setManufacturerId(int manufacturerId)
    {
        this.manufacturerId = manufacturerId;
    }

    /**
     * @return the categoryId
     */
    public int getCategoryId()
    {
        return categoryId;
    }

    /**
     * @param categoryId
     *            the categoryId to set
     */
    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }

    /**
     * @return the searchInDescription
     */
    public boolean isSearchInDescription()
    {
        return searchInDescription;
    }

    /**
     * @param searchInDescription
     *            the searchInDescription to set
     */
    public void setSearchInDescription(boolean searchInDescription)
    {
        this.searchInDescription = searchInDescription;
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
     * @return the custom1
     */
    public String getCustom1()
    {
        return custom1;
    }

    /**
     * @param custom1
     *            the custom1 to set
     */
    public void setCustom1(String custom1)
    {
        this.custom1 = custom1;
    }

    /**
     * @return the custom2
     */
    public String getCustom2()
    {
        return custom2;
    }

    /**
     * @param custom2
     *            the custom2 to set
     */
    public void setCustom2(String custom2)
    {
        this.custom2 = custom2;
    }

    /**
     * @return the custom3
     */
    public String getCustom3()
    {
        return custom3;
    }

    /**
     * @param custom3
     *            the custom3 to set
     */
    public void setCustom3(String custom3)
    {
        this.custom3 = custom3;
    }

    /**
     * @return the custom4
     */
    public String getCustom4()
    {
        return custom4;
    }

    /**
     * @param custom4
     *            the custom4 to set
     */
    public void setCustom4(String custom4)
    {
        this.custom4 = custom4;
    }

    /**
     * @return the custom5
     */
    public String getCustom5()
    {
        return custom5;
    }

    /**
     * @param custom5
     *            the custom5 to set
     */
    public void setCustom5(String custom5)
    {
        this.custom5 = custom5;
    }

    /**
     * @return the dateAddedFrom
     */
    public Date getDateAddedFrom()
    {
        return dateAddedFrom;
    }

    /**
     * @param dateAddedFrom
     *            the dateAddedFrom to set
     */
    public void setDateAddedFrom(Date dateAddedFrom)
    {
        this.dateAddedFrom = dateAddedFrom;
    }

    /**
     * @return the dateAddedTo
     */
    public Date getDateAddedTo()
    {
        return dateAddedTo;
    }

    /**
     * @param dateAddedTo
     *            the dateAddedTo to set
     */
    public void setDateAddedTo(Date dateAddedTo)
    {
        this.dateAddedTo = dateAddedTo;
    }

    /**
     * @return the priceFromStr
     */
    public String getPriceFromStr()
    {
        return priceFromStr;
    }

    /**
     * @param priceFromStr
     *            the priceFromStr to set
     */
    public void setPriceFromStr(String priceFromStr)
    {
        this.priceFromStr = priceFromStr;
    }

    /**
     * @return the priceToStr
     */
    public String getPriceToStr()
    {
        return priceToStr;
    }

    /**
     * @param priceToStr
     *            the priceToStr to set
     */
    public void setPriceToStr(String priceToStr)
    {
        this.priceToStr = priceToStr;
    }

}
