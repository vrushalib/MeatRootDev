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

package com.konakart.bl.modules.ordertotal.rewardpoints;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.torque.TorqueException;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.Promotion;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for creating a number of reward points based on the
 * value of an order.
 * 
 * The promotion may be activated only if:
 * <ul>
 * <li>The total amount of the order is greater than a minimum amount</li>
 * <li>The total number of products ordered is greater than a minimum amount</li>
 * <li>The total number of a single product ordered is greater than a minimum amount</li>
 * </ul>
 * The calculation may be configured by:
 * <ul>
 * <li>Considering the amount of the order before or after tax.</li>
 * </ul>
 */
public class RewardPoints extends BaseOrderTotalModule implements OrderTotalInterface
{
    /** Module name must be the same as the class name although it can be all in lowercase */
    public static String code = "ot_reward_points";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.rewardpoints.RewardPoints";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otRewardPointsMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_REWARD_POINTS_SORT_ORDER = "MODULE_ORDER_TOTAL_REWARD_POINTS_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_REWARD_POINTS_STATUS = "MODULE_ORDER_TOTAL_REWARD_POINTS_STATUS";

    // Message Catalog Keys
    private final static String MODULE_ORDER_TOTAL_REWARD_POINTS_TITLE = "module.order.total.rewardpoints.title";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
     * 
     */
    public RewardPoints(KKEngIf eng) throws TorqueException, KKException, DataSetException
    {
        super.init(eng);

        StaticData sd = staticDataHM.get(getStoreId());

        if (sd == null)
        {
            synchronized (mutex)
            {
                sd = staticDataHM.get(getStoreId());
                if (sd == null)
                {
                    setStaticVariables();
                }
            }
        }
    }

    /**
     * Sets some static variables during setup
     * 
     * @throws KKException
     * 
     */
    public void setStaticVariables() throws KKException
    {
        KKConfiguration conf;
        StaticData staticData = staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            staticData = new StaticData();
            staticDataHM.put(getStoreId(), staticData);
        }
        conf = getConfiguration(MODULE_ORDER_TOTAL_REWARD_POINTS_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_ORDER_TOTAL_REWARD_POINTS_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the number of reward points allocated.
     * 
     * @param order
     * @param dispPriceWithTax
     * @param locale
     * @return Returns an OrderTotal object for this module
     * @throws Exception
     */
    public OrderTotal getOrderTotal(Order order, boolean dispPriceWithTax, Locale locale)
            throws Exception
    {
        OrderTotal ot;
        StaticData sd = staticDataHM.get(getStoreId());

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, locale);
        if (rb == null)
        {
            throw new KKException(
                    "A resource file cannot be found for the country " + locale.getCountry());
        }

        // Get the promotions
        Promotion[] promArray = getPromMgr().getPromotions(code, order);

        if (promArray != null && promArray.length > 0)
        {
            if (promArray.length > 1)
            {
                log.warn(
                        "There is more than one active Reward Points promotion module. Only one of these modules will be used.");
            }

            Promotion promotion = promArray[0];

            /*
             * Get the configuration parameters from the promotion
             */

            // Minimum value for order
            BigDecimal minTotalOrderVal = getCustomBigDecimal(promotion.getCustom1(), 1);

            // Minimum total quantity of products ordered
            int minTotalQuantity = getCustomInt(promotion.getCustom2(), 2);

            // Need to order at least this quantity of a single product for promotion to apply
            int minProdQuantity = getCustomInt(promotion.getCustom3(), 3);

            // If set to true, points are calculated on pre-tax value.
            boolean applyBeforeTax = getCustomBoolean(promotion.getCustom4(), 4);

            // Points multiplier
            BigDecimal pointsMultiplier = getCustomBigDecimal(promotion.getCustom5(), 5);

            // If set to true, the points calculation includes the shipping cost
            boolean includeShipping = getCustomBoolean(promotion.getCustom6(), 6);

            // Get the order value
            BigDecimal orderValue = null;
            if (applyBeforeTax)
            {
                /*
                 * We do the following calculation instead of using order.getTotalExTax() since this
                 * will not contain any promotional discounts that may have been added
                 */
                orderValue = order.getTotalIncTax().subtract(order.getTax());
            } else
            {
                orderValue = order.getTotalIncTax();
            }

            // Remove shipping charges if configured to not include
            if (!includeShipping && order.getShippingQuote() != null)
            {
                // Determine whether there is a shipping discount
                BigDecimal shippingDiscount = null;
                if (getOrderTotalList() != null)
                {
                    for (Iterator<OrderTotal> iterator = getOrderTotalList().iterator(); iterator
                            .hasNext();)
                    {
                        OrderTotal ot1 = iterator.next();
                        if (ot1.getClassName().equals("ot_shipping_discount"))
                        {
                            shippingDiscount = ot1.getValue();
                            break;
                        }
                    }
                }

                if (applyBeforeTax && order.getShippingQuote().getTotalExTax() != null)
                {
                    orderValue = orderValue.subtract(order.getShippingQuote().getTotalExTax());
                } else if (!applyBeforeTax && order.getShippingQuote().getTotalIncTax() != null)
                {
                    orderValue = orderValue.subtract(order.getShippingQuote().getTotalIncTax());
                }

                // If there was a shipping discount we add it back to the order value
                if (shippingDiscount != null)
                {
                    orderValue = orderValue.add(shippingDiscount);
                }
            }

            // Adjust the amount if there is a payment charge
            BigDecimal paymentCharge = null;
            if (getOrderTotalList() != null)
            {
                for (Iterator<OrderTotal> iterator = getOrderTotalList().iterator(); iterator
                        .hasNext();)
                {
                    OrderTotal ot1 = iterator.next();
                    if (ot1.getClassName().equals("ot_payment_charge"))
                    {
                        paymentCharge = ot1.getValue();
                        break;
                    }
                }
            }

            // If there is a payment charge we deduct it from the order value
            if (paymentCharge != null)
            {
                orderValue = orderValue.subtract(paymentCharge);
            }

            // If promotion doesn't cover any of the products in the order then leave
            if (promotion.getApplicableProducts() == null
                    || promotion.getApplicableProducts().length == 0)
            {
                return null;
            }

            ot = new OrderTotal();
            ot.setPromotionId(promotion.getId());
            ot.setSortOrder(sd.getSortOrder());
            ot.setClassName(code);
            ot.setPromotions(new Promotion[]
            { promotion });

            // Does promotion only apply to a min order value ?
            if (minTotalOrderVal != null)
            {
                if (orderValue.compareTo(minTotalOrderVal) < 0)
                {
                    // If we haven't reached the minimum amount then leave
                    return null;
                }
            }

            // Does promotion only apply to a minimum number of products ordered ?
            if (minTotalQuantity > 0)
            {
                int total = 0;
                for (int j = 0; j < promotion.getApplicableProducts().length; j++)
                {
                    total += promotion.getApplicableProducts()[j].getQuantity();
                }
                if (total < minTotalQuantity)
                {
                    // If we haven't reached the minimum total then leave
                    return null;
                }
            }

            // Does promotion only apply to a minimum number of single products ordered ?
            if (minProdQuantity > 0)
            {
                boolean foundMin = false;
                for (int j = 0; j < promotion.getApplicableProducts().length; j++)
                {
                    if (promotion.getApplicableProducts()[j].getQuantity() >= minProdQuantity)
                    {
                        foundMin = true;
                    }
                }
                if (!foundMin)
                {
                    // If we haven't reached the minimum total then leave
                    return null;
                }
            }

            // Calculate the points
            BigDecimal points = new BigDecimal(0);
            if (pointsMultiplier != null)
            {
                points = (orderValue.multiply(pointsMultiplier)).setScale(0,
                        BigDecimal.ROUND_HALF_UP);
            }

            // Set the order total attributes
            ot.setValue(points);
            ot.setText(points.toString());
            ot.setTitle(rb.getString(MODULE_ORDER_TOTAL_REWARD_POINTS_TITLE) + ":");
            order.setPointsAwarded(points.intValue());

            // Set the points per product on each order total
            for (int j = 0; j < promotion.getApplicableProducts().length; j++)
            {
                OrderProductIf op = promotion.getApplicableProducts()[j];

                BigDecimal opValue = null;
                if (applyBeforeTax)
                {
                    opValue = op.getFinalPriceExTax();
                } else
                {
                    opValue = op.getFinalPriceIncTax();
                }

                if (opValue != null)
                {
                    BigDecimal pointsPerProduct = (opValue.multiply(pointsMultiplier));

                    // Points for each product
                    if (op.getQuantity() > 1)
                    {
                        pointsPerProduct = pointsPerProduct.divide(new BigDecimal(op.getQuantity()));

                    }
                    pointsPerProduct = pointsPerProduct.setScale(0, BigDecimal.ROUND_HALF_UP);
                    if (op.getRefundPoints() > 0)
                    {
                        op.setRefundPoints(op.getRefundPoints() + pointsPerProduct.intValue());
                    } else
                    {
                        op.setRefundPoints(pointsPerProduct.intValue());
                    }
                }
            }

            if (promotion.getCoupon() != null)
            {
                setCouponIds(order, Integer.toString(promotion.getCoupon().getId()));
            }

            setPromotionIds(order, Integer.toString(ot.getPromotionId()));

            return ot;
        }

        // Return null if there are no promotions
        return null;
    }

    public int getSortOrder()
    {
        StaticData sd;
        try
        {
            sd = staticDataHM.get(getStoreId());
            return sd.getSortOrder();
        } catch (KKException e)
        {
            log.error("Can't get the store id", e);
            return 0;
        }
    }

    public String getCode()
    {
        return code;
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        /**
         * @return the sortOrder
         */
        public int getSortOrder()
        {
            return sortOrder;
        }

        /**
         * @param sortOrder
         *            the sortOrder to set
         */
        public void setSortOrder(int sortOrder)
        {
            this.sortOrder = sortOrder;
        }
    }
}
