//
// (c) 2016 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.bl.modules.ordertotal.totaldiscountadmin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
 * Module that creates an OrderTotal object for applying a percentage discount or an amount discount
 * on the order. The discount may be applied on the total value of the order before or after tax.
 * 
 * The promotion may be activated only if:
 * <ul>
 * <li>The total amount of the order is greater than a minimum amount</li>
 * <li>The total number of products ordered is greater than a minimum amount</li>
 * <li>The total number of a single product ordered is greater than a minimum amount</li>
 * </ul>
 * 
 * The discount is entered by an administrator when editing the order, rather than being defined when the order is created.
 */
public class TotalDiscountAdmin extends BaseOrderTotalModule implements OrderTotalInterface
{

    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "ot_total_discount_admin";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.totaldiscountadmin.TotalDiscountAdmin";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otTotalDiscountAdminMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_SORT_ORDER = "MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_STATUS = "MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_STATUS";

    // Message Catalogue Keys
    private final static String MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_TITLE = "module.order.total.totaldiscountadmin.title";

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
    public TotalDiscountAdmin(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        conf = getConfiguration(MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_SORT_ORDER);
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
        return isAvailable(MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the discount amount.
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
        
        /*
         * The discount to be applied is retrieved from the order
         */
        BigDecimal discountApplied = order.getAdminDiscount();
        if (discountApplied == null || discountApplied.equals(new BigDecimal(0)))
        {
            return null;
        }
        
        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, locale);
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + locale.getCountry());
        }

        // Get the promotions
        Promotion[] promArray = getPromMgr().getPromotions(code, order);

        // List to contain an order total for each promotion
        List<OrderTotal> myOrderTotalList = new ArrayList<OrderTotal>();

        boolean applyBeforeTax = true;

        if (promArray != null)
        {

            for (int i = 0; i < promArray.length; i++)
            {
                Promotion promotion = promArray[i];

                /*
                 * Get the configuration parameters from the promotion
                 */

                // Minimum value for order
                BigDecimal minTotalOrderVal = getCustomBigDecimal(promotion.getCustom1(), 1);

                // Minimum total quantity of products ordered
                int minTotalQuantity = getCustomInt(promotion.getCustom2(), 2);

                // Need to order at least this quantity of a single product for promotion to apply
                int minProdQuantity = getCustomInt(promotion.getCustom3(), 3);

                // If set to true it is a percentage. Otherwise it is an amount.
                boolean percentageDiscount = getCustomBoolean(promotion.getCustom5(), 5);

                // If set to true, discount is applied to pre-tax value. Only relevant for
                // percentage discount.
                applyBeforeTax = getCustomBoolean(promotion.getCustom6(), 6);

                // If promotion doesn't cover any of the products in the order then go on to the
                // next promotion
                if (promotion.getApplicableProducts() == null
                        || promotion.getApplicableProducts().length == 0)
                {
                    continue;
                }

                ot = new OrderTotal();
                ot.setPromotionId(promotion.getId());
                ot.setSortOrder(sd.getSortOrder());
                ot.setClassName(code);
                ot.setPromotions(new Promotion[]
                { promotion });
                if (percentageDiscount)
                {
                    ot.setDiscountPercent(discountApplied);
                } else
                {
                    ot.setDiscountAmount(discountApplied);
                }

                // Does promotion only apply to a min order value ?
                if (minTotalOrderVal != null)
                {
                    BigDecimal orderValue = null;
                    if (applyBeforeTax)
                    {
                        orderValue = order.getSubTotalExTax();
                    } else
                    {
                        orderValue = order.getSubTotalIncTax();
                    }

                    if (orderValue.compareTo(minTotalOrderVal) < 0)
                    {
                        // If we haven't reached the minimum amount then continue to the next
                        // promotion
                        continue;
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
                        // If we haven't reached the minimum total then continue to the next
                        // promotion
                        continue;
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
                        // If we haven't reached the minimum total then continue to the next
                        // promotion
                        continue;
                    }
                }

                // Apply the discount
                BigDecimal discount = null;
                int scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();

                if (percentageDiscount)
                {
                    /*
                     * Get the current order value. If discounts have already been applied we have
                     * to calculate the percentage discount on the current discounted total rather
                     * than the original total.
                     */
                    BigDecimal currentOrderValue = null;
                    if (applyBeforeTax)
                    {
                        currentOrderValue = order.getTotalExTax();
                        if (order.getShippingQuote() != null)
                        {
                            currentOrderValue = currentOrderValue.subtract(order.getShippingQuote()
                                    .getTotalExTax());
                        }
                    } else
                    {
                        currentOrderValue = order.getTotalIncTax();
                        if (order.getShippingQuote() != null)
                        {
                            currentOrderValue = currentOrderValue.subtract(order.getShippingQuote()
                                    .getTotalIncTax());
                        }
                    }

                    // Apply a percentage discount
                    discount = new BigDecimal(0);
                    discount = (currentOrderValue.multiply(discountApplied)).divide(new BigDecimal(
                            100));
                    discount = discount.setScale(scale, BigDecimal.ROUND_HALF_UP);

                    // Set the order total attributes
                    ot.setValue(discount);
                    ot.setText("-" + getCurrMgr().formatPrice(discount, order.getCurrencyCode()));
                    // Title looks like "10% discount:"
                    ot.setTitle(discountApplied + "% "
                            + rb.getString(MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_TITLE));
                } else
                {
                    // Apply an amount based discount

                    // Set the order total attributes
                    ot.setValue(discountApplied);
                    String formattedDiscount = getCurrMgr().formatPrice(discountApplied,
                            order.getCurrencyCode());
                    ot.setText("-" + formattedDiscount);
                    // Title looks like "10EUR discount:"
                    ot.setTitle(rb.getString(MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_TITLE));
                    discount = discountApplied;
                }
                
                // Get the tax portion of the discount
                BigDecimal tax = getTaxForDiscount(order, discount, scale, applyBeforeTax);
                ot.setTax(tax);

                myOrderTotalList.add(ot);
            }
        } else
        {
            // Return null if there are no promotions
            return null;
        }

        // Call a helper method to decide which OrderTotal we should return
        OrderTotal retOT = getDiscountOrderTotalFromList(order, myOrderTotalList, applyBeforeTax);

        return retOT;

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
