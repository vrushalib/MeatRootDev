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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.ProductFilter;

/**
 * Filters the search based on price limits
 */
public class FilterSearchByPriceAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String priceFromStr;

    private String priceToStr;

    private String taxMultiplier;

    private long timestamp;

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

            ProductFilter filter = new ProductFilter();

            try
            {
                String from = request.getParameter("from");
                if (from != null && from.length() > 0)
                {
                    filter.setPriceFrom(new BigDecimal(from));
                }

                String to = request.getParameter("to");
                if (to != null && to.length() > 0)
                {
                    filter.setPriceTo(new BigDecimal(to));
                }

                String t = request.getParameter("t");
                if (t != null && t.length() > 0)
                {
                    timestamp = Long.parseLong(t);
                }

                /*
                 * Convert the price from the slider if the currency isn't the default
                 */
                if (getPriceFromStr() != null && getPriceFromStr().length() > 0)
                {
                    if (kkAppEng.getUserCurrency().getId() != kkAppEng.getDefaultCurrency().getId())
                    {
                        BigDecimal conversionRate = kkAppEng.getUserCurrency().getValue();
                        if (conversionRate != null)
                        {
                            filter.setPriceFrom(new BigDecimal(getPriceFromStr()).divide(
                                    conversionRate, 5, BigDecimal.ROUND_UP));
                        }
                    } else
                    {
                        filter.setPriceFrom(new BigDecimal(getPriceFromStr()));
                    }
                }

                if (getPriceToStr() != null && getPriceToStr().length() > 0)
                {
                    if (kkAppEng.getUserCurrency().getId() != kkAppEng.getDefaultCurrency().getId())
                    {
                        BigDecimal conversionRate = kkAppEng.getUserCurrency().getValue();
                        if (conversionRate != null)
                        {
                            filter.setPriceTo(new BigDecimal(getPriceToStr()).divide(
                                    conversionRate, 5, BigDecimal.ROUND_UP));
                        }
                    } else
                    {
                        filter.setPriceTo(new BigDecimal(getPriceToStr()));
                    }
                }

                if (kkAppEng.displayPriceWithTax() && taxMultiplier != null
                        && taxMultiplier.length() > 0)
                {
                    BigDecimal taxMult = new BigDecimal(taxMultiplier);
                    if (filter.getPriceFrom() != null
                            && !filter.getPriceFrom().equals(new BigDecimal(0)))
                    {
                        BigDecimal priceFrom = filter.getPriceFrom().divide(taxMult, 5,
                                BigDecimal.ROUND_UP);
                        filter.setPriceFrom(priceFrom);
                    }

                    if (filter.getPriceTo() != null
                            && !filter.getPriceTo().equals(new BigDecimal(0)))
                    {
                        BigDecimal priceTo = filter.getPriceTo().divide(taxMult, 5,
                                BigDecimal.ROUND_UP);
                        filter.setPriceTo(priceTo);
                    }
                }

                kkAppEng.getProductMgr().filterProducts(filter, timestamp);
            } catch (Exception e)
            {
            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

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

    /**
     * @return the timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * @return the taxMultiplier
     */
    public String getTaxMultiplier()
    {
        return taxMultiplier;
    }

    /**
     * @param taxMultiplier
     *            the taxMultiplier to set
     */
    public void setTaxMultiplier(String taxMultiplier)
    {
        this.taxMultiplier = taxMultiplier;
    }

}
