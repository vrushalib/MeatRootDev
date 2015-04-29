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
import com.konakart.app.DataDescConstants;
import com.konakart.app.DataDescriptor;
import com.konakart.app.ProductSearch;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.bl.ProductMgr;

/**
 * Gets called before viewing the main page.
 */
public class CatalogMainPageAction extends BaseAction
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

            // Reset the state of the objects connected to the session. i.e. Selected product etc.
            kkAppEng.reset();

            // Clear the navigation
            kkAppEng.getNav().clear();

            // Get the new products for all categories
            if (!kkAppEng.getPropertyAsBoolean("main.page.hide.new.arrivals", false))
            {
                kkAppEng.getProductMgr().fetchNewProductsArray(
                /* categoryId */ProductMgr.DONT_INCLUDE, /* fillDescription */
                false, /* force refresh */false);
            }

            // Get the recently viewed products
            if (!kkAppEng.getPropertyAsBoolean("main.page.hide.recently.viewed", false))
            {
                kkAppEng.getProductMgr().fetchRecentlyViewedProductsArray(
                /* fillDescription */false);
            }

            /*
             * Example of how to fetch featured products to show in a carousel on the main page. It
             * returns all products with "featured" in the custom1 attribute. The products are
             * available in the customProducts1 array.
             */
            if (!kkAppEng.getPropertyAsBoolean("main.page.hide.featured", false))
            {
                DataDescriptorIf dd = new DataDescriptor();
                dd.setCustom1("featured");
                dd.setLimit(20);
                dd.setOrderBy(DataDescConstants.ORDER_BY_RATING_DESCENDING);
                kkAppEng.getProductMgr().fetchCustomProducts1Array(new ProductSearch(), dd);
            }
                        
            // Fetch all sale items
            // kkAppEng.getProductMgr().fetchSpecialsArray(/* categoryId */ProductMgr.DONT_INCLUDE,
            // /* searchInSubCats */
            // false, /* fillDescription */false, /* forceRefresh */false);

            /*
             * Example of how to apply promotions to the products without needing to add them to the
             * cart. You can decide not to change the actual price of the product but instead to
             * display information informing that the product is discounted using a type of
             * promotion (i.e. 3 for 2). If you do change the price of the product by setting the
             * "subtractValueFromPrice" booleans, then you need to use the discounted price when
             * adding the product to the cart and modify the promotion to not add the discount as an
             * OrderTotal to the order.
             */
            // PromotionOptions options = new PromotionOptions();
            // options.setSubtractValueFromPriceExTax(true);
            // options.setSubtractValueFromPriceIncTax(true);
            // options.setPromotionRule(PromotionOptions.PRM_RULE_CHOOSE_LARGEST);
            //
            // ProductIf[] newProds = kkAppEng.getEng().getPromotionsPerProducts(
            // kkAppEng.getSessionId(),
            // kkAppEng.getCustomerMgr().getCurrentCustomer().getId(),
            // kkAppEng.getProductMgr().getNewProducts(),
            // kkAppEng.getProductMgr().getAllPromotions(), /* couponCodes */null, options);
            // kkAppEng.getProductMgr().setNewProducts(newProds);
            //
            // ProductIf[] viewedProds = kkAppEng.getEng().getPromotionsPerProducts(
            // kkAppEng.getSessionId(),
            // kkAppEng.getCustomerMgr().getCurrentCustomer().getId(),
            // kkAppEng.getProductMgr().getViewedProducts(),
            // kkAppEng.getProductMgr().getAllPromotions(), /* couponCodes */null, options);
            // kkAppEng.getProductMgr().setViewedProducts(viewedProds);
            //
            // ProductIf[] featuredProds = kkAppEng.getEng().getPromotionsPerProducts(
            // kkAppEng.getSessionId(),
            // kkAppEng.getCustomerMgr().getCurrentCustomer().getId(),
            // kkAppEng.getProductMgr().getCustomProducts1(),
            // kkAppEng.getProductMgr().getAllPromotions(), /* couponCodes */null, options);
            // kkAppEng.getProductMgr().setCustomProducts1(featuredProds);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
