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

package com.konakart.bl.modules.ordertotal.redeempoints;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
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
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for redeeming a number of reward points.
 * 
 * The promotion may be activated only if:
 * <ul>
 * <li>The total amount of the order is greater than a minimum amount</li>
 * <li>The total number of products ordered is greater than a minimum amount</li>
 * <li>The total number of a single product ordered is greater than a minimum amount</li>
 * </ul>
 */
public class RedeemPoints extends BaseOrderTotalModule implements OrderTotalInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "ot_redeem_points";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.redeempoints.RedeemPoints";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otRedeemPointsMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_REDEEM_POINTS_SORT_ORDER = "MODULE_ORDER_TOTAL_REDEEM_POINTS_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_REDEEM_POINTS_STATUS = "MODULE_ORDER_TOTAL_REDEEM_POINTS_STATUS";

    // Message Catalog Keys
    private final static String MODULE_ORDER_TOTAL_REDEEM_POINTS_TITLE = "module.order.total.redeempoints.title";

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
    public RedeemPoints(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
        conf = getConfiguration(MODULE_ORDER_TOTAL_REDEEM_POINTS_SORT_ORDER);
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
        return isAvailable(MODULE_ORDER_TOTAL_REDEEM_POINTS_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the number points redeemed.
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
            throw new KKException("A resource file cannot be found for the country "
                    + locale.getCountry());
        }

        // Get the promotions
        Promotion[] promArray = getPromMgr().getPromotions(code, order);

        if (promArray != null && promArray.length > 0)
        {
            if (promArray.length > 1)
            {
                log
                        .warn("There is more than one active Redeem Points promotion module. Only one of these modules will be used.");
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

            // If set to true, the pre-tax value of the order total is taken.
            boolean applyBeforeTax = getCustomBoolean(promotion.getCustom4(), 4);

            // Points multiplier
            BigDecimal pointsMultiplier = getCustomBigDecimal(promotion.getCustom5(), 5);

            // Get the order value
            BigDecimal orderValue = null;
            if (applyBeforeTax)
            {
                orderValue = order.getSubTotalExTax();
            } else
            {
                orderValue = order.getSubTotalIncTax();
            }

            // If promotion doesn't cover any of the products in the order then leave
            if (promotion.getApplicableProducts() == null
                    || promotion.getApplicableProducts().length == 0)
            {
                return null;
            }

            // If number of points redeemed is zero then leave
            if (order.getPointsRedeemed() <= 0)
            {
                return null;
            }

            ot = new OrderTotal();
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

            // How many points does the customer have ?
            int customerPoints = getRewardPointMgr().pointsAvailable(order.getCustomerId());
            if (customerPoints == 0)
            {
                order.setPointsRedeemed(0);
                return null;
            }

            if (order.getPointsRedeemed() > customerPoints)
            {
                order.setPointsRedeemed(customerPoints);
            }

            // Calculate the amount redeemed
            BigDecimal amount = (new BigDecimal(order.getPointsRedeemed())
                    .multiply(pointsMultiplier));
            int scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();
            amount = amount.setScale(scale, BigDecimal.ROUND_HALF_UP);

            if (amount.compareTo(order.getTotalIncTax()) == 1)
            {
                /*
                 * If the amount is greater than the order total, we reduce he number of points used
                 * to match the order total.
                 */
                BigDecimal points = order.getTotalIncTax().divide(pointsMultiplier, 0,
                        BigDecimal.ROUND_HALF_UP);
                order.setPointsRedeemed(points.intValue());
                amount = order.getTotalIncTax();
                order.setTotalIncTax(new BigDecimal(0));
            } else
            {
                order.setTotalIncTax(order.getTotalIncTax().subtract(amount));
            }

            // Set the order total attributes
            ot.setValue(amount);
            ot.setText("-" + getCurrMgr().formatPrice(amount, order.getCurrencyCode()));
            // Title looks like "10 Reward Points Redeemed:"
            ot.setTitle(order.getPointsRedeemed() + " "
                    + rb.getString(MODULE_ORDER_TOTAL_REDEEM_POINTS_TITLE) + ":");
            
            
            /*
             * We need to reduce the tax amount. This is done differently depending on whether
             * the discount is applied to the amount before or after tax.
             */
            BigDecimal total = null;
            if (order.getShippingQuote() != null && order.getShippingQuote().getTax() != null
                    && order.getShippingQuote().getTax().compareTo(new BigDecimal(0)) != 0)
            {
                // Use total including shipping cost
                total = order.getTotalExTax();
            } else
            {
                // Use subtotal that doesn't include shipping
                total = order.getSubTotalExTax();
            }

            if (total != null && total.compareTo(new BigDecimal(0)) != 0
                    && order.getTax() != null)
            {
                BigDecimal averageTaxRate = order.getTax().divide(total, 6,
                        BigDecimal.ROUND_HALF_UP);
                if (applyBeforeTax)
                {
                    // Calculate the tax discount based on the average tax
                    BigDecimal taxDiscount = amount.multiply(averageTaxRate);
                    taxDiscount = taxDiscount.setScale(scale, BigDecimal.ROUND_HALF_UP);
                    ot.setTax(taxDiscount);
                    order.setTax(order.getTax().subtract(taxDiscount));
                    order.setTotalIncTax(order.getTotalIncTax().subtract(taxDiscount));
                } else
                {
                    BigDecimal taxDiscount = getTaxFromTotal(amount, averageTaxRate, scale);
                    ot.setTax(taxDiscount);
                    order.setTax(order.getTax().subtract(taxDiscount));
                }
            }
            
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
