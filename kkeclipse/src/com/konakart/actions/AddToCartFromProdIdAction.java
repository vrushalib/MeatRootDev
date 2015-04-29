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
import com.konakart.al.json.BasketJson;
import com.konakart.al.json.WishListJson;
import com.konakart.appif.ProductIf;

/**
 * Adds a product to the cart based on the parameter "prodId"
 */
public class AddToCartFromProdIdAction extends AddToCartOrWishListBaseAction
{
    private static final long serialVersionUID = 1L;
    
    private String xsrf_token;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            setImgBase(kkAppEng.getImageBase());

            if (getProdId() == -1)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("No prodId parameter present");
                }
                return SUCCESS;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Product Id of selected product from application = " + getProdId());
            }

            // Get the product from its Id
            kkAppEng.getProductMgr().fetchSelectedProduct(getProdId());
            ProductIf selectedProd = kkAppEng.getProductMgr().getSelectedProduct();
            if (selectedProd == null)
            {
                return SUCCESS;
            }

            /*
             * If the product has options then we can't add it to the cart directly. We must first
             * go to the product details page so that the options can be selected.
             */
            if (selectedProd.getOpts() != null && selectedProd.getOpts().length > 0)
            {
                /*
                 * Set just the product id. In the Javascript we work out the redirect URL so that
                 * it works also when we are running as a portlet
                 */
                setRedirectURL(Integer.toString(getProdId()));
                return SUCCESS;
            }

            // Common code for adding to cart
            this.addToCart(kkAppEng, selectedProd, null, 1);

            // Common code for setting messages
            this.setMsgs(kkAppEng);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the prodId
     */
    public int getProdId()
    {
        return prodId;
    }

    /**
     * @param prodId
     *            the prodId to set
     */
    public void setProdId(int prodId)
    {
        this.prodId = prodId;
    }

    /**
     * @return the numberOfItems
     */
    public int getNumberOfItems()
    {
        return numberOfItems;
    }

    /**
     * @param numberOfItems
     *            the numberOfItems to set
     */
    public void setNumberOfItems(int numberOfItems)
    {
        this.numberOfItems = numberOfItems;
    }

    /**
     * @return the basketTotal
     */
    public String getBasketTotal()
    {
        return basketTotal;
    }

    /**
     * @param basketTotal
     *            the basketTotal to set
     */
    public void setBasketTotal(String basketTotal)
    {
        this.basketTotal = basketTotal;
    }

    /**
     * @return the redirectURL
     */
    public String getRedirectURL()
    {
        return redirectURL;
    }

    /**
     * @param redirectURL
     *            the redirectURL to set
     */
    public void setRedirectURL(String redirectURL)
    {
        this.redirectURL = redirectURL;
    }

    /**
     * @return the checkoutMsg
     */
    public String getCheckoutMsg()
    {
        return checkoutMsg;
    }

    /**
     * @param checkoutMsg
     *            the checkoutMsg to set
     */
    public void setCheckoutMsg(String checkoutMsg)
    {
        this.checkoutMsg = checkoutMsg;
    }

    /**
     * @return the imgBase
     */
    public String getImgBase()
    {
        return imgBase;
    }

    /**
     * @param imgBase
     *            the imgBase to set
     */
    public void setImgBase(String imgBase)
    {
        this.imgBase = imgBase;
    }

    /**
     * @return the subtotalMsg
     */
    public String getSubtotalMsg()
    {
        return subtotalMsg;
    }

    /**
     * @param subtotalMsg
     *            the subtotalMsg to set
     */
    public void setSubtotalMsg(String subtotalMsg)
    {
        this.subtotalMsg = subtotalMsg;
    }

    /**
     * @return the shoppingCartMsg
     */
    public String getShoppingCartMsg()
    {
        return shoppingCartMsg;
    }

    /**
     * @param shoppingCartMsg
     *            the shoppingCartMsg to set
     */
    public void setShoppingCartMsg(String shoppingCartMsg)
    {
        this.shoppingCartMsg = shoppingCartMsg;
    }

    /**
     * @return the quantityMsg
     */
    public String getQuantityMsg()
    {
        return quantityMsg;
    }

    /**
     * @param quantityMsg
     *            the quantityMsg to set
     */
    public void setQuantityMsg(String quantityMsg)
    {
        this.quantityMsg = quantityMsg;
    }

    /**
     * @return the items
     */
    public BasketJson[] getItems()
    {
        return items;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(BasketJson[] items)
    {
        this.items = items;
    }

    /**
     * @return the wlItems
     */
    public WishListJson[] getWlItems()
    {
        return wlItems;
    }

    /**
     * @param wlItems
     *            the wlItems to set
     */
    public void setWlItems(WishListJson[] wlItems)
    {
        this.wlItems = wlItems;
    }

    /**
     * @return the xsrf_token
     */
    public String getXsrf_token()
    {
        return xsrf_token;
    }

    /**
     * @param xsrf_token the xsrf_token to set
     */
    public void setXsrf_token(String xsrf_token)
    {
        this.xsrf_token = xsrf_token;
    }

}
