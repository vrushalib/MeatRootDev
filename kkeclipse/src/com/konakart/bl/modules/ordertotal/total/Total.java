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

package com.konakart.bl.modules.ordertotal.total;

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
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.util.JavaUtils;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for the total amount of the order.
 */
public class Total extends BaseOrderTotalModule implements OrderTotalInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = OrderTotalMgr.ot_total;

    private static String bundleName = BaseModule.basePackage + ".ordertotal.total.Total";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otTotalMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_TOTAL_SORT_ORDER = "MODULE_ORDER_TOTAL_TOTAL_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_TOTAL_STATUS = "MODULE_ORDER_TOTAL_TOTAL_STATUS";

    // Message Catalogue Keys

    private final static String MODULE_ORDER_TOTAL_TOTAL_TITLE = "module.order.total.total.title";

    // private final static String MODULE_ORDER_TOTAL_TOTAL_DESCRIPTION =
    // "module.order.total.total.description";

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
    public Total(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
        } else
        {
            if (!updateStaticVariablesNow(staticData.getLastUpdatedMS()))
            {
                return;
            }
        }

        conf = getConfiguration(MODULE_ORDER_TOTAL_TOTAL_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        staticData.setLastUpdatedMS(System.currentTimeMillis());

        if (log.isInfoEnabled())
        {
            if (log.isDebugEnabled())
            {
                log.debug(JavaUtils.dumpAllStackTraces(".*JavaUtils.dumpAllStackTraces.*",
                        "(.*AllStackTraces.*|.*java.lang.Thread..*)"));
            }
            String staticD = "Configuration data for " + code + " on " + getStoreId();
            staticD += "\n\t\t SortOrder          = " + staticData.getSortOrder();
            staticD += "\n\t\t LastUpdated        = " + staticData.getLastUpdatedMS();
            log.info(staticD);
        }
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_ORDER_TOTAL_TOTAL_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the total order amount.
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

        /*
         * Commented code which demonstrates how this module could edit order total modules which
         * have already been called by the OrderTotalMgr.
         */
//        if (getOrderTotalList() != null)
//        {
//            for (Iterator<OrderTotal> iterator = getOrderTotalList().iterator(); iterator.hasNext();)
//            {
//                OrderTotal ot = iterator.next();
//                if (ot.getClassName().equals(OrderTotalMgr.ot_subtotal))
//                {
//                    // Modify the sub-total order total
//                }
//                System.out.println(ot.getClassName() + " - " + ot.getText());
//            }
//        }

        /*
         * Commented code which demonstrates how to get the original order when an order is being
         * edited. This could be useful to figure out how much a customer needs to pay if he's
         * already paid for the original order.
         */
//        if (order.getArchiveId() != null)
//        {
//            int idOfOrderToBeArchived = -1;
//            try
//            {
//                idOfOrderToBeArchived = Integer.parseInt(order.getArchiveId());
//            } catch (Exception e)
//            {
//                log.warn("Order (" + order.getId() + ") archiveId contains a non integer value - "
//                        + order.getArchiveId());
//            }
//            if (idOfOrderToBeArchived > -1)
//            {
//                /*
//                 * Fetch the order to be archived with any archived orders it may already have
//                 */
//                OrderSearchIf orderSearch = new OrderSearch();
//                orderSearch.setPopulateArchivedOrdersAttribute(true);
//                orderSearch.setOrderId(idOfOrderToBeArchived);
//                Orders ret = getOrderMgr().searchForOrdersPerCustomer(order.getCustomerId(), /* dataDesc */
//                null, orderSearch, LanguageMgr.DEFAULT_LANG);
//
//                if (ret != null && ret.getOrderArray() != null && ret.getOrderArray().length == 1
//                        && ret.getOrderArray()[0].getOrderTotals() != null)
//                {
//                    /*
//                     * Get the original total that was paid by the customer to figure out how much
//                     * to charge now. The original order may also have an archived order etc.
//                     */
//                    OrderIf originalOrder = ret.getOrderArray()[0];
//                    BigDecimal total = null;
//                    int scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();
//                    for (int i = 0; i < originalOrder.getOrderTotals().length; i++)
//                    {
//                        OrderTotal ot = (OrderTotal) originalOrder.getOrderTotals()[i];
//                        if (ot.getClassName().equals(OrderTotalMgr.ot_total))
//                        {
//                            total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
//                            System.out.println("Total of original order is " + total.toString());
//                        }
//                    }
//                }
//            }
//        }

        OrderTotal ot;
        StaticData sd = staticDataHM.get(getStoreId());

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, locale);
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + locale.getCountry());
        }

        /*
         * Just in case some discounts have made the total negative, we set it to zero
         */
        if (order.getTotalIncTax().signum() < 0)
        {
            order.setTotalIncTax(new BigDecimal(0));
        }

        ot = new OrderTotal();
        ot.setSortOrder(sd.getSortOrder());
        ot.setClassName(code);
        ot.setTitle(rb.getString(MODULE_ORDER_TOTAL_TOTAL_TITLE) + ":");
        ot.setText("<b>"
                + getCurrMgr().formatPrice(order.getTotalIncTax(), order.getCurrencyCode())
                + "</b>");
        ot.setValue(order.getTotalIncTax());
        return ot;
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

        // lastUpdatedMS
        private long lastUpdatedMS = -1;

        /**
         * @return the lastUpdatedMS
         */
        public long getLastUpdatedMS()
        {
            return lastUpdatedMS;
        }

        /**
         * @param lastUpdatedMS
         *            the lastUpdatedMS to set
         */
        public void setLastUpdatedMS(long lastUpdatedMS)
        {
            this.lastUpdatedMS = lastUpdatedMS;
        }

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
