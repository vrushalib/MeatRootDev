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

package com.konakart.bl.modules.ordertotal.freeproduct;

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
import com.konakart.app.Product;
import com.konakart.app.Promotion;
import com.konakart.app.PromotionResult;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.LanguageIf;
import com.konakart.appif.ProductIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for awarding a free product. The product to be awarded
 * can have a status set to false so that it isn't enabled and cannot be searched for and bought
 * normally through the store.<br>
 * The SKU of the free product is saved in the Custom1 field of the Order Total. The id of the free
 * product is saved in the Custom2 field of the Order Total.
 * 
 * The promotion may be activated only if:
 * <ul>
 * <li>The total amount of the order is greater than a minimum amount</li>
 * <li>The total number of products ordered is greater than a minimum amount</li>
 * <li>The total number of a single product ordered is greater than a minimum amount</li>
 * </ul>
 * 
 */
public class FreeProduct extends BaseOrderTotalModule implements OrderTotalInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "ot_free_product";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.freeproduct.FreeProduct";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otFreeProductMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_FREE_PRODUCT_SORT_ORDER = "MODULE_ORDER_TOTAL_FREE_PRODUCT_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_FREE_PRODUCT_STATUS = "MODULE_ORDER_TOTAL_FREE_PRODUCT_STATUS";

    // Message Catalog Keys
    private final static String MODULE_ORDER_TOTAL_FREE_PRODUCT_TITLE = "module.order.total.freeproduct.title";

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
    public FreeProduct(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
        conf = getConfiguration(MODULE_ORDER_TOTAL_FREE_PRODUCT_SORT_ORDER);
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
        return isAvailable(MODULE_ORDER_TOTAL_FREE_PRODUCT_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the free product.
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
            OrderTotal[] otArray = new OrderTotal[promArray.length];
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

                // If set to true, the pre-tax value of the order total is taken.
                boolean applyBeforeTax = getCustomBoolean(promotion.getCustom4(), 4);

                // Free Product Id
                int prodId = getCustomInt(promotion.getCustom5(), 5);

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
                    continue;
                }

                // Does promotion only apply to a min order value ?
                if (minTotalOrderVal != null)
                {
                    if (orderValue.compareTo(minTotalOrderVal) < 0)
                    {
                        // If we haven't reached the minimum amount then leave
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
                        // If we haven't reached the minimum total then leave
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
                        // If we haven't reached the minimum total then leave
                        continue;
                    }
                }

                String prodName = "";
                String prodSku = "";
                String prodIdStr = "";
                LanguageIf lang = getLangMgr().getLanguagePerCode(locale.getLanguage());
                if (lang != null)
                {
                    ProductIf prod = getProdMgr().getProduct(null, prodId, lang.getId());
                    if (prod != null)
                    {
                        prodName = prod.getName();
                        prodSku = prod.getSku();
                        prodIdStr = Integer.toString(prod.getId());
                    } else
                    {
                        log.warn("Cannot find product for product id = " + prodId
                                + " and language id = " + lang.getId());
                    }
                } else
                {
                    log.warn("Cannot find language for code " + locale.getLanguage());
                }

                otArray[i] = new OrderTotal();
                otArray[i].setSortOrder(sd.getSortOrder());
                otArray[i].setClassName(code);
                otArray[i].setPromotions(new Promotion[]
                { promotion });
                otArray[i].setValue(new BigDecimal(0));
                otArray[i].setText("");
                // Title looks like "Free Gift: MS Mouse"
                otArray[i].setTitle(rb.getString(MODULE_ORDER_TOTAL_FREE_PRODUCT_TITLE) + ":");
                otArray[i].setText(prodName);
                // Save details of the free product so from the order it is clear what needs to be
                // shipped
                otArray[i].setCustom1(prodSku);
                otArray[i].setCustom2(prodIdStr);
            }

            ArrayList<OrderTotal> otList = new ArrayList<OrderTotal>();
            for (int j = 0; j < otArray.length; j++)
            {
                OrderTotal ot = otArray[j];
                if (ot != null)
                {
                    otList.add(ot);
                }
            }

            if (otList.size() == 0)
            {
                // No active promotions
                return null;
            } else if (otList.size() == 1)
            {
                // One active promotion
                return otList.get(0);
            } else
            {
                // >1 Active promotion
                OrderTotal[] otArray1 = new OrderTotal[otList.size()];
                int k = 0;
                for (Iterator<OrderTotal> iterator = otList.iterator(); iterator.hasNext();)
                {
                    OrderTotal lot = iterator.next();
                    otArray1[k++] = lot;
                }
                OrderTotal ot = new OrderTotal();
                ot.setSortOrder(sd.getSortOrder());
                ot.setClassName(code);
                ot.setOrderTotals(otArray1);
                return ot;
            }
        }
        // Return null if there are no promotions
        return null;
    }

    /**
     * Returns an object containing the promotion discount. This method is used to apply the
     * promotion to a single product. In this case, since the result of the promotion isn't a
     * discount but a product id, we convert the product id to a decimal and return it in the
     * <code>value</code> attribute of the PromotionResult object.
     * 
     * @param product
     * @param promotion
     * @return Returns a PromotionResult object
     * @throws Exception
     */
    public PromotionResult getPromotionResult(Product product, Promotion promotion)
            throws Exception
    {

        // Get the free product id stored in custom5
        BigDecimal freeProdId = getCustomBigDecimal(promotion.getCustom5(), 5);

        PromotionResult pd = new PromotionResult();
        pd.setPromotionId(promotion.getId());
        pd.setOrderTotalCode(code);
        pd.setValue(freeProdId);

        return pd;
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
