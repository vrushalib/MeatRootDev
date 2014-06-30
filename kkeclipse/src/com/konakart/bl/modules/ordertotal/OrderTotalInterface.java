//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.bl.modules.ordertotal;

import java.util.List;
import java.util.Locale;

import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.Product;
import com.konakart.app.Promotion;
import com.konakart.app.PromotionResult;

/**
 * 
 * 
 */
public interface OrderTotalInterface
{
    /**
     * @param order
     * @param dispPriceWithTax
     * @param locale
     * @return Returns an OrderTotal object for this module
     * @throws Exception
     */
    public OrderTotal getOrderTotal(Order order, boolean dispPriceWithTax, Locale locale)
            throws Exception;

    /**
     * @return True or False
     * @throws KKException
     */
    public boolean isAvailable() throws KKException;

    /**
     * @return Return the order in which the module gets called and displayed
     */
    public int getSortOrder();

    /**
     * @return Returns the code for the module
     */
    public String getCode();

    /**
     * Refreshes the static variables from the copy in the database
     * 
     * @throws KKException
     */
    public void setStaticVariables() throws KKException;

    /**
     * A list of the current order totals so that an order total module can modify an order total
     * that was previously called.
     * 
     * @return the orderTotalList
     */
    public List<OrderTotal> getOrderTotalList();

    /**
     * A list of the current order totals so that an order total module can modify an order total
     * that was previously called.
     * 
     * @param orderTotalList
     *            the orderTotalList to set
     */
    public void setOrderTotalList(List<OrderTotal> orderTotalList);

    /**
     * Returns an object containing the promotion discount. This method is used to apply the
     * promotion to a single product.
     * 
     * @param product
     * @param promotion
     * @return Returns a PromotionResult object
     * @throws Exception
     */
    public PromotionResult getPromotionResult(Product product, Promotion promotion)
            throws Exception;

}
