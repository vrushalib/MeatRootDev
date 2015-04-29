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

package com.konakart.bl.modules.ordertotal.buyxgetyfree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import com.konakart.appif.OrderProductIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for applying a Buy X Get Y Free discount. The discount
 * may be applied on prices before or after tax.
 * 
 * The promotion may be activated on a product only if:
 * <ul>
 * <li>The total amount of the order is greater than a minimum amount</li>
 * </ul>
 * 
 * There may be multiple valid promotions applicable for an order. If this is the case, the logic
 * applied is the following: All cumulative promotions are summed into one order total object. Then
 * we loop through the order total objects and choose the one that offers the largest discount.
 */
public class BuyXGetYFree extends BaseOrderTotalModule implements OrderTotalInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "ot_buy_x_get_y_free";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.buyxgetyfree.BuyXGetYFree";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otBuyXGetYFreeMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_SORT_ORDER = "MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_STATUS = "MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_STATUS";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
     */
    public BuyXGetYFree(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
        conf = getConfiguration(MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_SORT_ORDER);
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
        return isAvailable(MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the discount amount.
     * <p>
     * Custom field usage:
     * <p>
     * <ul>
     * <li>custom2 = Buy Quantity</li>
     * <li>custom3 = Free Quantity</li>
     * <li>custom1 = Minimum Order Value</li>
     * <li>custom4 = Apply discount before tax</li>
     * </ul>
     * If the promotion applies to multiple products, we create an array of order total objects and
     * attach the array to the order total that we return (ot.setOrderTotals(otArray)). The reason
     * for doing this is to get a line item of the order for each discounted product. We still need
     * to populate the order total that we return with the total discount amount because this will
     * be used to compare this promotion with other promotions in order to decide which one to use.
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

        // Get the scale
        int scale = getTaxMgr().getTaxScale();

        // Get the promotions
        Promotion[] promArray = getPromMgr().getPromotions(code, order);

        if (log.isDebugEnabled())
        {
            if (promArray != null)
            {
                log.debug("Promotions found for order = " + promArray.length);
                for (int i = 0; i < promArray.length; i++)
                {
                    Promotion promotion = promArray[i];
                    log.debug(promotion.toString());
                }
            }
        }

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

                // Buy Quantity - the "X" in Buy X Get Y Free
                // Need to order at least this quantity of a single product for promotion to apply
                int buyProdQuantity = getCustomInt(promotion.getCustom1(), 1);

                // Free Quantity - the "Y" in Buy X Get Y Free
                int freeProdQuantity = getCustomInt(promotion.getCustom2(), 2);

                // Minimum value for order
                BigDecimal minTotalOrderVal = getCustomBigDecimal(promotion.getCustom3(), 3);

                // If set to true, discount is applied to pre-tax value. Only relevant for
                // percentage discount.
                applyBeforeTax = getCustomBoolean(promotion.getCustom4(), 4);

                if (log.isDebugEnabled())
                {
                    log.debug("buyProdQuantity             = " + buyProdQuantity);
                    log.debug("freeProdQuantity            = " + freeProdQuantity);
                    log.debug("minTotalOrderVal            = " + minTotalOrderVal);
                    log.debug("applyBeforeTax              = " + applyBeforeTax);
                }

                // Don't bother going any further if there is no free products
                if (freeProdQuantity <= 0)
                {
                    continue;
                }

                // Get the order value
                BigDecimal orderValue = null;
                if (applyBeforeTax)
                {
                    orderValue = order.getSubTotalExTax();
                } else
                {
                    orderValue = order.getSubTotalIncTax();
                }

                // If promotion doesn't cover any of the products in the order then go on to the
                // next promotion
                if (promotion.getApplicableProducts() == null
                        || promotion.getApplicableProducts().length == 0)
                {
                    continue;
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
                        // If we haven't reached the minimum amount then continue to the next
                        // promotion
                        continue;
                    }
                }

                // Continue if promotion has no applicable products (should never happen)
                if (promotion.getApplicableProducts() == null)
                {
                    continue;
                }

                /*
                 * Create a new Order Total module for each discounted product and store in this
                 * list
                 */
                ArrayList<OrderTotal> otList = new ArrayList<OrderTotal>();

                // Loop through promotion products to determine whether to apply a discount
                boolean firstLoop = true;
                for (int j = 0; j < promotion.getApplicableProducts().length; j++)
                {
                    OrderProductIf op = promotion.getApplicableProducts()[j];
                    int buyPlusFree = buyProdQuantity + freeProdQuantity;
                    if (op != null && op.getQuantity() >= buyPlusFree)
                    {
                        // Get the current total price of the product (divide by quantity)
                        BigDecimal currentPrice = null;
                        BigDecimal qty = new BigDecimal(op.getQuantity());
                        if (applyBeforeTax)
                        {
                            currentPrice = op.getFinalPriceExTax().divide(qty, scale,
                                    BigDecimal.ROUND_HALF_UP);
                        } else
                        {
                            currentPrice = op.getFinalPriceIncTax().divide(qty, scale,
                                    BigDecimal.ROUND_HALF_UP);
                        }

                        // Apply the discount
                        BigDecimal discountPerBuyFreeGroup = currentPrice.multiply(new BigDecimal(
                                Math.floor(op.getQuantity() / buyPlusFree)));
                        BigDecimal discount = discountPerBuyFreeGroup.multiply(new BigDecimal(
                                freeProdQuantity));

                        if (log.isDebugEnabled())
                        {
                            log.debug("buyPlusFree                 = " + buyPlusFree);
                            log.debug("currentPrice per item       = " + currentPrice);
                            log.debug("Qty                         = " + op.getQuantity());
                            log.debug("discount per buy/free Group = " + discount);
                            log.debug("overall discount            = " + discount);
                        }

                        // Determine whether it is the first discounted product or not
                        String formattedDiscount = getCurrMgr().formatPrice(discount,
                                order.getCurrencyCode());
                        if (firstLoop)
                        {
                            // Set the order total attributes
                            ot.setValue(discount);
                            if (op.getTaxRate() != null)
                            {
                                BigDecimal taxDiscount = null;
                                if (applyBeforeTax)
                                {
                                    taxDiscount = discount.multiply(op.getTaxRate()).divide(
                                            new BigDecimal(100));
                                } else
                                {
                                    taxDiscount = getTaxFromTotal(discount,
                                            op.getTaxRate().divide(new BigDecimal(100)), scale);
                                }
                                ot.setTax(taxDiscount);
                            }

                            ot.setText("-" + formattedDiscount);
                            // Title looks like "-10EUR Philips TV"
                            ot.setTitle("-" + formattedDiscount + " " + op.getName());
                        } else
                        {
                            // Set the order total attributes
                            ot.setValue(ot.getValue().add(discount));
                            if (op.getTaxRate() != null)
                            {
                                BigDecimal taxDiscount = null;
                                if (applyBeforeTax)
                                {
                                    taxDiscount = discount.multiply(op.getTaxRate()).divide(
                                            new BigDecimal(100));
                                } else
                                {
                                    taxDiscount = getTaxFromTotal(discount,
                                            op.getTaxRate().divide(new BigDecimal(100)), scale);
                                }
                                if (ot.getTax() == null)
                                {
                                    ot.setTax(taxDiscount);
                                } else
                                {
                                    ot.setTax(ot.getTax().add(taxDiscount));
                                }
                            }
                            String formattedTotalDiscount = getCurrMgr().formatPrice(ot.getValue(),
                                    order.getCurrencyCode());
                            ot.setText("-" + formattedTotalDiscount);
                            ot.setTitle(ot.getTitle() + ", " + "-" + formattedDiscount + " "
                                    + op.getName());
                        }
                        firstLoop = false;

                        if (log.isDebugEnabled())
                        {
                            log.debug(ot.toString());
                        }

                        /*
                         * Create a new Order Total module for each product
                         */
                        OrderTotal singleOt = new OrderTotal();
                        singleOt.setSortOrder(sd.getSortOrder());
                        singleOt.setClassName(code);
                        singleOt.setValue(discount);
                        singleOt.setText("-" + formattedDiscount);
                        singleOt.setTitle("-" + formattedDiscount + " " + op.getName() + ":");
                        otList.add(singleOt);
                    }
                }

                /*
                 * If we have more than one discounted product we create an array of order totals
                 * (one for each product) and add the array to the order total to be returned.
                 */
                if (otList.size() > 1)
                {
                    OrderTotal[] otArray = new OrderTotal[otList.size()];
                    int k = 0;
                    for (Iterator<OrderTotal> iterator = otList.iterator(); iterator.hasNext();)
                    {
                        OrderTotal lot = iterator.next();
                        otArray[k++] = lot;
                    }
                    ot.setOrderTotals(otArray);
                }

                if (ot.getValue() != null)
                {
                    ot.setValue(ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP));
                    myOrderTotalList.add(ot);
                }
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
