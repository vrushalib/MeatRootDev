//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.bl.modules.ordertotal.externaltax;

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
import com.konakart.app.TaxRate;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderTotalIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.util.Utils;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for the amount of tax for the order and removes any
 * External tax line from the list of OrderTotals.
 */
public class ExternalTax extends BaseOrderTotalModule implements OrderTotalInterface
{
    private static String code = OrderTotalMgr.ot_tax;

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.externaltax.ExternalTax";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otExternalTaxMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_EXTERNAL_TAX_SORT_ORDER = "MODULE_ORDER_TOTAL_EXTERNAL_TAX_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_EXTERNAL_TAX_ENABLED = "MODULE_ORDER_TOTAL_EXTERNAL_TAX_ENABLED";

    private final static String MODULE_ORDER_TOTAL_EXTERNAL_TAX_OT_TO_REMOVE = "MODULE_ORDER_TOTAL_EXTERNAL_TAX_OT_TO_REMOVE";

    private final static String MODULE_ORDER_TOTAL_EXTERNAL_TAX_ONLY_IF_PRESENT = "MODULE_ORDER_TOTAL_EXTERNAL_TAX_ONLY_IF_PRESENT";

    // Message Catalogue Keys

    private final static String MODULE_ORDER_TOTAL_EXTERNAL_TAX_TITLE = "module.order.total.externaltax.title";

    // private final static String MODULE_ORDER_TOTAL_TAX_DESCRIPTION =
    // "module.order.total.tax.description";

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
    public ExternalTax(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        conf = getConfiguration(MODULE_ORDER_TOTAL_EXTERNAL_TAX_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        staticData.setRemoveOT(getConfigurationValue(MODULE_ORDER_TOTAL_EXTERNAL_TAX_OT_TO_REMOVE));

        staticData.setOnlyIfPresnt(getConfigurationValueAsBool(
                MODULE_ORDER_TOTAL_EXTERNAL_TAX_ONLY_IF_PRESENT, true));
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_ORDER_TOTAL_EXTERNAL_TAX_ENABLED);
    }

    /**
     * Create and return an OrderTotal object for the tax amount.
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

        if (Utils.isBlank(sd.getRemoveOT()))
        {
            // We can't do anything if there's no external tax module defined
            throw new KKException(
                    "Configuration parameter blank or not defined : MODULE_ORDER_TOTAL_EXTERNAL_TAX_OT_TO_REMOVE");
        }

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, locale);
        if (rb == null)
        {
            log.warn("A resource file cannot be found for the country " + locale.getCountry());

            rb = getResourceBundle(mutex, bundleName, resourceBundleMap, Locale.ENGLISH);
        }

        if (rb == null)
        {
            throw new KKException(
                    "A resource file cannot be found for the customer's locale or English");
        }

        if (getOrderTotalList() == null)
        {
            log.warn("No order totals present on order");
            return null;
        }

        // find the external tax order total to replace
        for (OrderTotalIf ot : getOrderTotalList())
        {
            if (ot.getClassName().equals(sd.getRemoveOT()))
            {
                if (log.isInfoEnabled())
                {
                    log.info("External Tax OT will replace the " + sd.getRemoveOT() + " OT with "
                            + order.getTax());
                }
                setOrderTotalValues(sd, ot, order, rb);
                return null;
            }
        }

        // If we get here we did not find the External OT

        // We don't add a new OT if "OnlyIfPresent" is true
        if (sd.isOnlyIfPresent())
        {
            if (log.isInfoEnabled())
            {
                log.info("Tax OT not added because we only add one if " + sd.getRemoveOT()
                        + " is present");
            }
            return null;
        }

        // Add a new OT despite the External Tax OT not being present

        OrderTotal ot = new OrderTotal();
        setOrderTotalValues(sd, ot, order, rb);
        return ot;
    }

    private void setOrderTotalValues(StaticData sd, OrderTotalIf ot, Order order, ResourceBundle rb)
            throws KKException, TorqueException, DataSetException, Exception
    {
        ot.setSortOrder(sd.getSortOrder());
        ot.setClassName(code);
        ot.setValue(order.getTax());
        ot.setText(getCurrMgr().formatPrice(order.getTax(), order.getCurrencyCode()));
        StringBuffer title = new StringBuffer();
        if (order.getTaxRateObjectArray() != null && order.getTaxRateObjectArray().length > 0)
        {
            for (int i = 0; i < order.getTaxRateObjectArray().length; i++)
            {
                if (i > 0)
                {
                    title.append(" + ");
                }
                TaxRate tr = (TaxRate) order.getTaxRateObjectArray()[i];
                title.append(tr.getDescription());
            }
        } else
        {
            title.append(rb.getString(MODULE_ORDER_TOTAL_EXTERNAL_TAX_TITLE));
        }
        title.append(":");
        ot.setTitle(title.toString());
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

        private String removeOT = null;

        private boolean onlyIfPresent = true;

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

        /**
         * @return the removeOT
         */
        public String getRemoveOT()
        {
            return removeOT;
        }

        /**
         * @param removeOT
         *            the removeOT to set
         */
        public void setRemoveOT(String removeOT)
        {
            this.removeOT = removeOT;
        }

        /**
         * @return the onlyIfPresent
         */
        public boolean isOnlyIfPresent()
        {
            return onlyIfPresent;
        }

        /**
         * @param onlyIfPresent
         *            the onlyIfPresent to set
         */
        public void setOnlyIfPresnt(boolean onlyIfPresent)
        {
            this.onlyIfPresent = onlyIfPresent;
        }
    }
}
