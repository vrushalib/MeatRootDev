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
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.json.BasketJson;
import com.konakart.al.json.WishListJson;
import com.konakart.app.Option;
import com.konakart.appif.OptionIf;
import com.konakart.appif.ProductIf;

/**
 * Adds a product to the cart from the product details page using AJAX
 */
public class AddToCartOrWishListFromPostAction extends AddToCartOrWishListBaseAction
{
    private static final long serialVersionUID = 1L;

    private int[] optionId = new int[20];

    private int[] valueId = new int[20];

    private int[] type = new int[20];

    private String[] quantity = new String[20];

    private String[] custText = new String[20];

    private String[] custPrice = new String[20];

    private int numOptions = 0;

    private String addToWishList = "";

    private int wishListId = -1;

    private String xsrf_token;
    
    private int prodQuantity = 1;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {

            // If true, we are adding to the wish list and not to the basket
            boolean addToWishListB = false;

            if (getAddToWishList() != null && getAddToWishList().equalsIgnoreCase("true"))
            {
                addToWishListB = true;
            }
            // Set to null in case it has been injected with malicious code
            addToWishList = null;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            setImgBase(kkAppEng.getImageBase());

            // Check to see whether the user is logged in if adding to wish list
            if (addToWishListB)
            {
                boolean canAdd = this.checkIfCanAddToWishList(kkAppEng, request, response,
                        xsrf_token);
                if (!canAdd)
                {
                    return SUCCESS;
                }
            }

            /*
             * Get the selected options from the form and place them in an array of option objects
             */
            OptionIf[] opts = null;
            if (getNumOptions() > 0)
            {
                ArrayList<OptionIf> optsList = new ArrayList<OptionIf>();
                for (int i = 0; i < getNumOptions(); i++)
                {
                    OptionIf o = new Option();
                    o.setId(getOptionId()[i]);
                    o.setValueId(getValueId()[i]);
                    o.setType(getType()[i]);
                    o.setCustomerText(getCustText()[i]);
                    try
                    {
                        o.setQuantity(Integer.parseInt(getQuantity()[i]));
                    } catch (Exception e)
                    {
                        o.setQuantity(0);
                    }
                    try
                    {
                        if (getCustPrice()[i] != null && getCustPrice()[i].length() > 0)
                        {
                            o.setCustomerPrice(new BigDecimal(getCustPrice()[i]));
                        }
                    } catch (Exception e)
                    {
                    }
                    if (o.getType() == Option.TYPE_CUSTOMER_TEXT)
                    {
                        if (o.getCustomerText() != null && o.getCustomerText().length() > 0
                                && !addToWishListB)
                        {
                            /*
                             * Only add the option if there is some text since the option could
                             * increase the cost of the product. Doesn't get added to the wishlist.
                             */
                            optsList.add(o);
                        }
                    } else if (o.getType() == Option.TYPE_VARIABLE_QUANTITY)
                    {
                        if (o.getQuantity() > 0 && !addToWishListB)
                        {
                            /*
                             * Only add the option if quantity > 0 since the option could increase
                             * the cost of the product. Doesn't get added to the wishlist.
                             */
                            optsList.add(o);
                        }
                    } else if (o.getType() == Option.TYPE_CUSTOMER_PRICE)
                    {
                        if (o.getCustomerPrice() != null && !addToWishListB)
                        {
                            /*
                             * Only add the option if price has been populated since the option
                             * increases the cost of the product. Doesn't get added to the wishlist.
                             */
                            optsList.add(o);
                        }
                    } else
                    {
                        optsList.add(o);
                    }
                }
                opts = optsList.toArray(new OptionIf[0]);
            }

            /*
             * Ensure that the product exists. It should already be the selected product-
             */
            ProductIf selectedProd = kkAppEng.getProductMgr().getSelectedProduct();
            if (selectedProd == null || selectedProd.getId() != getProdId())
            {
                kkAppEng.getProductMgr().fetchSelectedProduct(getProdId());
                selectedProd = kkAppEng.getProductMgr().getSelectedProduct();
                if (selectedProd == null)
                {
                    return SUCCESS;
                }
            }

            if (addToWishListB)
            {
                // Common code for adding to wish list
                addToWishList(kkAppEng, selectedProd, opts, wishListId);
            } else
            {
                // Common code for adding to cart
                this.addToCart(kkAppEng, selectedProd, opts, prodQuantity);
            }

            // Common code for setting messages
            this.setMsgs(kkAppEng);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the optionId
     */
    public int[] getOptionId()
    {
        return optionId;
    }

    /**
     * @param optionId
     *            the optionId to set
     */
    public void setOptionId(int[] optionId)
    {
        this.optionId = optionId;
    }

    /**
     * @return the valueId
     */
    public int[] getValueId()
    {
        return valueId;
    }

    /**
     * @param valueId
     *            the valueId to set
     */
    public void setValueId(int[] valueId)
    {
        this.valueId = valueId;
    }

    /**
     * @return the type
     */
    public int[] getType()
    {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(int[] type)
    {
        this.type = type;
    }

    /**
     * @return the numOptions
     */
    public int getNumOptions()
    {
        return numOptions;
    }

    /**
     * @param numOptions
     *            the numOptions to set
     */
    public void setNumOptions(int numOptions)
    {
        this.numOptions = numOptions;
    }

    /**
     * @return the addToWishList
     */
    public String getAddToWishList()
    {
        return addToWishList;
    }

    /**
     * @param addToWishList
     *            the addToWishList to set
     */
    public void setAddToWishList(String addToWishList)
    {
        this.addToWishList = addToWishList;
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
     * @return the wishListMsg
     */
    public String getWishListMsg()
    {
        return wishListMsg;
    }

    /**
     * @param wishListMsg
     *            the wishListMsg to set
     */
    public void setWishListMsg(String wishListMsg)
    {
        this.wishListMsg = wishListMsg;
    }

    /**
     * @return the emptyWishListMsg
     */
    public String getEmptyWishListMsg()
    {
        return emptyWishListMsg;
    }

    /**
     * @param emptyWishListMsg
     *            the emptyWishListMsg to set
     */
    public void setEmptyWishListMsg(String emptyWishListMsg)
    {
        this.emptyWishListMsg = emptyWishListMsg;
    }

    /**
     * @return the wishListTotal
     */
    public String getWishListTotal()
    {
        return wishListTotal;
    }

    /**
     * @param wishListTotal
     *            the wishListTotal to set
     */
    public void setWishListTotal(String wishListTotal)
    {
        this.wishListTotal = wishListTotal;
    }

    /**
     * @return the wishListId
     */
    public int getWishListId()
    {
        return wishListId;
    }

    /**
     * @param wishListId
     *            the wishListId to set
     */
    public void setWishListId(int wishListId)
    {
        this.wishListId = wishListId;
    }

    /**
     * @return the custText
     */
    public String[] getCustText()
    {
        return custText;
    }

    /**
     * @param custText
     *            the custText to set
     */
    public void setCustText(String[] custText)
    {
        this.custText = custText;
    }

    /**
     * @return the custPrice
     */
    public String[] getCustPrice()
    {
        return custPrice;
    }

    /**
     * @param custPrice
     *            the custPrice to set
     */
    public void setCustPrice(String[] custPrice)
    {
        this.custPrice = custPrice;
    }

    /**
     * @return the quantity
     */
    public String[] getQuantity()
    {
        return quantity;
    }

    /**
     * @param quantity
     *            the quantity to set
     */
    public void setQuantity(String[] quantity)
    {
        this.quantity = quantity;
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
     * @param xsrf_token
     *            the xsrf_token to set
     */
    public void setXsrf_token(String xsrf_token)
    {
        this.xsrf_token = xsrf_token;
    }

    /**
     * @return the prodQuantity
     */
    public int getProdQuantity()
    {
        return prodQuantity;
    }

    /**
     * @param prodQuantity the prodQuantity to set
     */
    public void setProdQuantity(int prodQuantity)
    {
        this.prodQuantity = prodQuantity;
    }
}
