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
package com.konakart.bl.modules.ordertotal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.Product;
import com.konakart.app.Promotion;
import com.konakart.app.PromotionResult;
import com.konakart.appif.CouponIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PromotionIf;
import com.konakart.bl.modules.BaseModule;

/**
 * Base Order Total Module
 */
public class BaseOrderTotalModule extends BaseModule
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BaseOrderTotalModule.class);

    /**
     * A list of the current order totals so that an order total module can modify an order total
     * that was previously called.
     */
    protected List<OrderTotal> orderTotalList;

    /**
     * Utility method that gets an int from one of the promotion custom attributes
     * 
     * @param customAttr
     * @param customId
     * @return int
     * @throws KKException
     */
    protected int getCustomInt(String customAttr, int customId) throws KKException
    {
        try
        {
            int ret = new Integer(customAttr).intValue();
            return ret;
        } catch (Exception e)
        {
            throw new KKException("Custom" + customId + " (" + customAttr
                    + ") must be set to a valid integer.");
        }
    }

    /**
     * Utility method that gets a BigDecimal from one of the promotion custom attributes
     * 
     * @param customAttr
     * @param customId
     * @return BigDecimal
     * @throws KKException
     */
    protected BigDecimal getCustomBigDecimal(String customAttr, int customId) throws KKException
    {
        try
        {
            BigDecimal ret = new BigDecimal(customAttr);
            return ret;
        } catch (Exception e)
        {
            throw new KKException("Custom" + customId + " (" + customAttr
                    + ") must be set to a valid decimal number.");
        }
    }

    /**
     * Utility method that gets a boolean from one of the promotion custom attributes
     * 
     * @param customAttr
     * @param customId
     * @return boolean
     * @throws KKException
     */
    protected boolean getCustomBoolean(String customAttr, int customId) throws KKException
    {
        try
        {
            boolean ret;
            if (customAttr.equalsIgnoreCase("true"))
            {
                ret = true;
                return ret;
            } else if (customAttr.equalsIgnoreCase("false"))
            {
                ret = false;
                return ret;
            } else
            {
                throw new KKException("Custom" + customId + " (" + customAttr
                        + ") must be set to true or false.");
            }
        } catch (Exception e)
        {
            throw new KKException("Custom" + customId + " (" + customAttr
                    + ") must be set to true or false.");
        }
    }

    /**
     * Utility method that gets a String from one of the promotion custom attributes. It ensures
     * that the value isn't null.
     * 
     * @param customAttr
     * @param customId
     * @return String
     * @throws KKException
     */
    protected String getCustomString(String customAttr, int customId) throws KKException
    {
        if (customAttr == null)
        {
            throw new KKException("Custom" + customId + " must be set. It cannot be left empty.");
        }
        return customAttr;
    }

    /**
     * This is a helper method for the discount modules. Many promotions may be relevant for an
     * order. This method receives all of the relative promotions (in the form of Order Total
     * objects) in a list as an input parameter. It sums all of the cumulative promotions into a
     * single Order Total object and then compares all of the order totals that it has, in order to
     * select the one that provides the largest discount.
     * 
     * @param order
     * @param orderTotalsList
     * @return An OrderTotal object
     * @throws Exception
     */
    protected OrderTotal getDiscountOrderTotalFromList(Order order, List<OrderTotal> orderTotalsList)
            throws Exception
    {
        return getDiscountOrderTotalFromList(order, orderTotalsList, /* applyBeforeTax */true);
    }

    /**
     * This is a helper method for the discount modules. Many promotions may be relevant for an
     * order. This method receives all of the relative promotions (in the form of Order Total
     * objects) in a list as an input parameter. It sums all of the cumulative promotions into a
     * single Order Total object and then compares all of the order totals that it has, in order to
     * select the one that provides the largest discount.
     * 
     * @param order
     * @param orderTotalsList
     * @param applyBeforeTax
     *            True when the discount is applied to total before tax is calculated. false when
     *            the discount is applied to the total including tax.
     * @return An OrderTotal object
     * @throws Exception
     */
    protected OrderTotal getDiscountOrderTotalFromList(Order order,
            List<OrderTotal> orderTotalsList, boolean applyBeforeTax) throws Exception
    {
        if (orderTotalsList == null || order == null)
        {
            return null;
        }

        if (orderTotalsList.size() == 1)
        {
            // Simple case with just one applicable order total
            OrderTotal localOt = orderTotalsList.get(0);
            if (localOt == null || localOt.getValue() == null)
            {
                return null;
            }
            // Add colon to title
            localOt.setTitle(localOt.getTitle() + ":");
            // Subtract the discount from the total of the order
            order.setTotalIncTax(order.getTotalIncTax().subtract(localOt.getValue()));
            // Reduce the tax of the order
            if (localOt.getTax() != null)
            {
                order.setTax(order.getTax().subtract(localOt.getTax()));
                if (applyBeforeTax)
                {
                    order.setTotalIncTax(order.getTotalIncTax().subtract(localOt.getTax()));
                }
            }
            // Set the promotion id used in the order
            setPromotionIds(order, Integer.toString(localOt.getPromotions()[0].getId()));
            // Set the coupon id if applicable
            if (localOt.getPromotions()[0].getCoupon() != null)
            {
                setCouponIds(order,
                        Integer.toString(localOt.getPromotions()[0].getCoupon().getId()));
            }
            return localOt;
        } else if (orderTotalsList.size() > 1)
        {
            // Create one order total object for any cumulative promotions and then select the order
            // total object offering the biggest discount
            OrderTotal cumulativeOT = null;
            ArrayList<OrderTotal> cumulativeList = new ArrayList<OrderTotal>();

            // For cumulative promotions we attach all of the promotions to the OrderTotal in an
            // array
            ArrayList<PromotionIf> promotionList = new ArrayList<PromotionIf>();

            for (Iterator<OrderTotal> iter = orderTotalsList.iterator(); iter.hasNext();)
            {
                OrderTotal localOt = iter.next();
                if (localOt == null || localOt.getValue() == null)
                {
                    iter.remove();
                    continue;
                }

                if (localOt.getPromotions()[0].isCumulative())
                {
                    cumulativeList.add(localOt);
                    if (cumulativeOT == null)
                    {
                        cumulativeOT = localOt.getClone();
                    } else
                    {
                        // Add the discounts
                        BigDecimal newDiscount = cumulativeOT.getValue().add(localOt.getValue());
                        cumulativeOT.setValue(newDiscount);
                        cumulativeOT.setText("-"
                                + getCurrMgr().formatPrice(newDiscount, order.getCurrencyCode()));
                        // Merge titles
                        cumulativeOT.setTitle(cumulativeOT.getTitle() + "+" + localOt.getTitle());
                        // Add tax
                        if (cumulativeOT.getTax() != null && localOt.getTax() != null)
                        {
                            cumulativeOT.setTax(cumulativeOT.getTax().add(localOt.getTax()));
                        } else if (cumulativeOT.getTax() == null && localOt.getTax() != null)
                        {
                            cumulativeOT.setTax(localOt.getTax());
                        }
                    }

                    // Add the participating promotions to a list
                    promotionList.add(localOt.getPromotions()[0]);

                    // Remove from list if it is cumulative since it is substituted by cumulativeOT
                    iter.remove();
                }
            }

            // Add the cumulative OrderTotal to the list if it isn't null
            if (cumulativeOT != null)
            {
                // Create a promotions array and add it to the cumulativeOt
                PromotionIf[] promotionArray = new Promotion[promotionList.size()];
                int i = 0;
                for (Iterator<PromotionIf> iter = promotionList.iterator(); iter.hasNext();)
                {
                    PromotionIf prom = iter.next();
                    promotionArray[i++] = prom;
                }
                cumulativeOT.setPromotions(promotionArray);
                cumulativeOT.setOrderTotals(cumulativeList.toArray(new OrderTotal[0]));

                orderTotalsList.add(cumulativeOT);
            }

            // Select biggest order total object
            OrderTotal selectedOT = orderTotalsList.get(0);
            for (Iterator<OrderTotal> iter = orderTotalsList.iterator(); iter.hasNext();)
            {
                OrderTotal localOt = iter.next();
                if (localOt.getValue().compareTo(selectedOT.getValue()) > 0)
                {
                    selectedOT = localOt;
                }
            }

            // Add colon to title
            selectedOT.setTitle(selectedOT.getTitle() + ":");

            // Subtract the discount of the selected OrderTotal from the total of the order
            order.setTotalIncTax(order.getTotalIncTax().subtract(selectedOT.getValue()));

            // Reduce the tax of the order
            if (selectedOT.getTax() != null)
            {
                order.setTax(order.getTax().subtract(selectedOT.getTax()));
                if (applyBeforeTax)
                {
                    order.setTotalIncTax(order.getTotalIncTax().subtract(selectedOT.getTax()));
                }
            }

            // If the order total consists of more than one promotion and / or more than one valid
            // coupon, the promotion and coupon ids are saved in the order as comma separated lists.
            // We need to save these with the order since once the order has been approved we may
            // need to write into the database to update the number of times a coupon has been used
            // or the number of times a promotion has been used by a particular customer etc.

            StringBuffer promotionIds = new StringBuffer();
            StringBuffer couponIds = new StringBuffer();

            for (int i = 0; i < selectedOT.getPromotions().length; i++)
            {
                PromotionIf prom = selectedOT.getPromotions()[i];
                CouponIf coupon = prom.getCoupon();

                if (promotionIds.length() > 0)
                {
                    promotionIds.append(",");
                }
                promotionIds.append(prom.getId());

                if (coupon != null)
                {
                    if (couponIds.length() > 0)
                    {
                        couponIds.append(",");
                    }
                    couponIds.append(coupon.getId());
                }
            }

            // Set the promotion ids used in the order
            setPromotionIds(order, promotionIds.toString());

            // Set the coupon ids if applicable
            if (couponIds.length() > 0)
            {
                setCouponIds(order, couponIds.toString());
            }

            return selectedOT;
        }

        return null;
    }

    /**
     * Sets the promotion ids of an order taking care not to overwrite the existing ones
     * 
     * @param order
     * @param promotionIds
     */
    private void setPromotionIds(Order order, String promotionIds)
    {
        if (order.getPromotionIds() != null && order.getPromotionIds().length() > 0)
        {
            order.setPromotionIds(order.getPromotionIds() + "," + promotionIds.toString());
        } else
        {
            order.setPromotionIds(promotionIds.toString());
        }
    }

    /**
     * Sets the coupon ids of an order taking care not to overwrite the existing ones
     * 
     * @param order
     * @param couponIds
     */
    private void setCouponIds(Order order, String couponIds)
    {
        if (order.getCouponIds() != null && order.getCouponIds().length() > 0)
        {
            order.setCouponIds(order.getCouponIds() + "," + couponIds.toString());
        } else
        {
            order.setCouponIds(couponIds.toString());
        }
    }

    /**
     * A list of the current order totals so that an order total module can modify an order total
     * that was previously called.
     * 
     * @return the orderTotalList
     */
    public List<OrderTotal> getOrderTotalList()
    {
        return this.orderTotalList;
    }

    /**
     * A list of the current order totals so that an order total module can modify an order total
     * that was previously called.
     * 
     * @param orderTotalList
     *            the orderTotalList to set
     */
    public void setOrderTotalList(List<OrderTotal> orderTotalList)
    {
        this.orderTotalList = orderTotalList;
    }

    /**
     * Returns an object containing the promotion discount
     * 
     * @param product
     * @param promotion
     * @return Returns a PromotionResult object
     * @throws Exception
     */
    public PromotionResult getPromotionResult(Product product, Promotion promotion)
            throws Exception
    {
        return null;
    }

    /**
     * Commit the Order transaction. Default implementation does nothing. Typically implemented in a
     * tax service Order Total module such as Avalara.
     * 
     * @param order
     * @throws Exception
     *             if something unexpected happens
     */
    public void commitOrder(OrderIf order) throws Exception
    {
        return;
    }

    /**
     * The total is the total value including tax. The tax rate is the rate / 100 so 10% would be
     * 0.1.
     * <p>
     * If the total = 10 and the tax rate = 0.1 then the method would return 0.9091 because in this
     * case the total of 10 is equivalent to 9.0909 + 0.9091 = 10 .
     * <p>
     * The value returned is (total x taxRate) / (1 + taxRate)
     * 
     * @param total
     * @param taxRate
     * @param scale
     * @return Returns the tax part of the total
     */
    public BigDecimal getTaxFromTotal(BigDecimal total, BigDecimal taxRate, int scale)
    {
        if (total == null || taxRate == null)
        {
            return new BigDecimal(0);
        }

        BigDecimal dividend = total.multiply(taxRate);
        BigDecimal divisor = taxRate.add(new BigDecimal(1));

        BigDecimal retVal = dividend.divide(divisor, scale, BigDecimal.ROUND_HALF_UP);

        return retVal;
    }
}
