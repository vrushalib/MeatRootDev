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

package com.konakart.bl.modules.ordertotal.productrewardpoints;

import java.math.BigDecimal;
import java.util.ArrayList;
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
 * Module that creates an OrderTotal object for allocating reward points on a single product.
 * 
 * The promotion may be activated only if:
 * <ul>
 * <li>The total amount of the order is greater than a minimum amount</li>
 * <li>The total number of a single product ordered is greater than a minimum amount</li>
 * </ul>
 * The calculation may be configured by:
 * <ul>
 * <li>Considering the value of the product before or after tax.</li>
 * </ul>
 * 
 */
public class ProductRewardPoints extends BaseOrderTotalModule implements OrderTotalInterface
{
    private static String code = "ot_product_reward_points";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.productrewardpoints.ProductRewardPoints";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otProductRewardPointsMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_SORT_ORDER = "MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_STATUS = "MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_STATUS";

    // Message Catalogue Keys
    private final static String MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_TITLE = "module.order.total.productrewardpoints.title";

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
    public ProductRewardPoints(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
        conf = getConfiguration(MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_SORT_ORDER);
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
        return isAvailable(MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the reward points
     * <p>
     * Custom field usage:
     * <p>
     * <ul>
     * <li>custom1 = Minimum Order Value</li>
     * <li>custom2 = Minimum quantity of a single product</li>
     * <li>custom3 = Discount applied to pre-tax value if set to true</li>
     * <li>custom4 = Points multiplier used to calculate number of points allocated</li>
     * </ul>
     * <p>
     * The returned OrderTotal custom attributes are populated as follows:
     * <ul>
     * <li>custom1 = Product Id</li>
     * <li>custom2 = Product SKU</li>
     * <li>custom3 = Encoded Product Id containing the product id and ids of the options.</li>
     * <li>custom4 = Coupon Id.</li>
     * </ul>
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
        OrderTotal ot = null;
        ArrayList<OrderTotal> otList = new ArrayList<OrderTotal>();
        HashMap<String, BigDecimal> prodDiscountMap = new HashMap<String, BigDecimal>();

        /*
         * If products have been discounted, then we store the discounts in the map so that we can
         * subtract the discount from the product price further down so that we don't allocate too
         * many points.
         */
        if (getOrderTotalList() != null && getOrderTotalList().size() > 0)
        {
            for (Iterator<OrderTotal> iterator = getOrderTotalList().iterator(); iterator
                    .hasNext();)
            {
                OrderTotal discountOt = iterator.next();
                if (discountOt.getClassName().equals("ot_product_discount")
                        || discountOt.getClassName().equals("ot_buy_x_get_y_free"))
                {
                    String encodedProdId = discountOt.getCustom3();
                    if (encodedProdId != null)
                    {
                        BigDecimal currentDiscount = prodDiscountMap.get(encodedProdId);
                        BigDecimal newDiscount = null;
                        if (currentDiscount != null)
                        {
                            newDiscount = currentDiscount.add(discountOt.getValue());
                        } else
                        {
                            newDiscount = discountOt.getValue();
                        }
                        prodDiscountMap.put(encodedProdId, newDiscount);
                    }
                }
            }
        }

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

                // Need to order at least this quantity of a single product for promotion to apply
                int minProdQuantity = getCustomInt(promotion.getCustom2(), 2);

                // If set to true, discount is applied to pre-tax value. Only relevant for
                // percentage discount.
                applyBeforeTax = getCustomBoolean(promotion.getCustom3(), 3);

                // Points multiplier
                BigDecimal pointsMultiplier = getCustomBigDecimal(promotion.getCustom4(), 4);

                // Don't bother going any further if no points will be allocated
                if (pointsMultiplier == null || pointsMultiplier.equals(new BigDecimal(0)))
                {
                    continue;
                }

                // Get the order value
                BigDecimal orderValue = null;
                if (applyBeforeTax)
                {
                    /*
                     * We do the following calculation instead of using order.getTotalExTax() since
                     * this will not contain any promotional discounts that may have been added
                     */
                    orderValue = order.getTotalIncTax().subtract(order.getTax());
                } else
                {
                    orderValue = order.getTotalIncTax();
                }

                // If promotion doesn't cover any of the products in the order then go on to the
                // next promotion
                if (promotion.getApplicableProducts() == null
                        || promotion.getApplicableProducts().length == 0)
                {
                    continue;
                }

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

                // Loop through promotion products to determine whether to allocate points
                for (int j = 0; j < promotion.getApplicableProducts().length; j++)
                {
                    OrderProductIf op = promotion.getApplicableProducts()[j];
                    if (op != null && op.getQuantity() >= minProdQuantity)
                    {
                        String encodedProdId = getBasketMgr()
                                .createEncodedProduct(op.getProductId(), op.getOpts());

                        // Get the current total price of the product(s)
                        BigDecimal currentPrice = null;
                        if (applyBeforeTax)
                        {
                            currentPrice = op.getFinalPriceExTax();
                        } else
                        {
                            currentPrice = op.getFinalPriceIncTax();
                        }
                        BigDecimal discount = prodDiscountMap.get(encodedProdId);
                        if (discount != null)
                        {
                            currentPrice = currentPrice.subtract(discount);
                        }

                        // Allocate the points
                        BigDecimal points = (currentPrice.multiply(pointsMultiplier)).setScale(0,
                                BigDecimal.ROUND_HALF_UP);

                        // Points for each product
                        if (op.getQuantity() > 1)
                        {
                            BigDecimal pointsPerProduct = points
                                    .divide(new BigDecimal(op.getQuantity()))
                                    .setScale(0, BigDecimal.ROUND_HALF_UP);
                            if (op.getRefundPoints() > 0)
                            {
                                op.setRefundPoints(op.getRefundPoints() + pointsPerProduct.intValue());
                            } else
                            {
                                op.setRefundPoints(pointsPerProduct.intValue());
                            }                            
                        } else
                        {
                            op.setRefundPoints(op.getRefundPoints() + points.intValue());
                            if (op.getRefundPoints() > 0)
                            {
                                op.setRefundPoints(op.getRefundPoints() + points.intValue());
                            } else
                            {
                                op.setRefundPoints(points.intValue());
                            }                            
                        }

                        /*
                         * Create a new Order Total module for each product
                         */
                        OrderTotal singleOt = new OrderTotal();
                        singleOt.setPromotionId(promotion.getId());
                        singleOt.setSortOrder(sd.getSortOrder());
                        singleOt.setClassName(code);
                        singleOt.setPromotions(new Promotion[]
                        { promotion });
                        singleOt.setValue(points);
                        singleOt.setText(points.toString());
                        singleOt.setTitle(
                                rb.getString(MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_TITLE) + " "
                                        + op.getName());
                        singleOt.setCustom1(Integer.toString(op.getProductId()));
                        singleOt.setCustom2(op.getSku());
                        singleOt.setCustom3(encodedProdId);
                        if (promotion.getCoupon() != null)
                        {
                            singleOt.setCustom4(Integer.toString(promotion.getCoupon().getId()));
                        }

                        /*
                         * If the promotion is cumulative we add the OT to the list. Otherwise we
                         * search the list for the same product and if an OT already exists we keep
                         * the OT that awards more points.
                         */
                        if (promotion.isCumulative())
                        {
                            otList.add(singleOt);
                            order.setPointsAwarded(
                                    order.getPointsAwarded() + singleOt.getValue().intValue());
                            if (promotion.getCoupon() != null)
                            {
                                setCouponIds(order,
                                        Integer.toString(promotion.getCoupon().getId()));
                            }
                            setPromotionIds(order, Integer.toString(singleOt.getPromotionId()));
                        } else
                        {
                            boolean addToList = false;
                            boolean found = false;
                            for (Iterator<OrderTotal> iterator = otList.iterator(); iterator
                                    .hasNext();)
                            {
                                OrderTotal ot1 = iterator.next();
                                // Custom3 - encoded product id
                                if (ot1.getCustom3().equals(singleOt.getCustom3()))
                                {
                                    found = true;
                                    if (ot1.getValue().intValue() < singleOt.getValue().intValue())
                                    {
                                        // Remove the current OT because it's less than the new one
                                        // Reduce the points on the order
                                        order.setPointsAwarded(order.getPointsAwarded()
                                                - ot1.getValue().intValue());
                                        // Remove the coupon id from the order
                                        if (ot1.getCustom4() != null)
                                        {
                                            removeCouponId(order, ot1.getCustom4());
                                        }
                                        // Remove the promotion id from the order
                                        removePromotionId(order,
                                                Integer.toString(ot1.getPromotionId()));
                                        // Remove it
                                        iterator.remove();
                                        addToList = true;
                                    }
                                    break;
                                }
                            }
                            if (!found || (found && addToList))
                            {
                                otList.add(singleOt);
                                order.setPointsAwarded(
                                        order.getPointsAwarded() + singleOt.getValue().intValue());
                                if (promotion.getCoupon() != null)
                                {
                                    setCouponIds(order,
                                            Integer.toString(promotion.getCoupon().getId()));
                                }
                                setPromotionIds(order, Integer.toString(singleOt.getPromotionId()));
                            }
                        }
                    }
                }
            }

            /*
             * If we have more than one discounted product we create an array of order totals (one
             * for each product) and add the array to the order total to be returned.
             */
            if (otList.size() > 1)
            {
                OrderTotal[] otArray = new OrderTotal[otList.size()];
                otArray = otList.toArray(otArray);
                ot = new OrderTotal();
                ot.setOrderTotals(otArray);
                return ot;
            } else if (otList.size() == 1)
            {
                ot = otList.get(0);
                return ot;
            } else
            {
                return null;
            }
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
