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

package com.konakart.bl.modules.ordertotal.tax;

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
import com.konakart.app.TaxRate;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object for the amount of tax for the order.
 */
public class Tax extends BaseOrderTotalModule implements OrderTotalInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = OrderTotalMgr.ot_tax;

    private static String bundleName = BaseModule.basePackage + ".ordertotal.tax.Tax";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otTaxMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_TAX_SORT_ORDER = "MODULE_ORDER_TOTAL_TAX_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_TAX_STATUS = "MODULE_ORDER_TOTAL_TAX_STATUS";

    // Message Catalogue Keys

    private final static String MODULE_ORDER_TOTAL_TAX_TITLE = "module.order.total.tax.title";

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
    public Tax(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        conf = getConfiguration(MODULE_ORDER_TOTAL_TAX_SORT_ORDER);
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
        return isAvailable(MODULE_ORDER_TOTAL_TAX_STATUS);
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
        OrderTotal ot;
        StaticData sd = staticDataHM.get(getStoreId());

        // Return null if there is no tax to pay
        if (order.getTax().compareTo(new BigDecimal(0)) == 0)
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

//        boolean showTaxOnMultipleLines = true;
//        if (order.getTaxRateObjectArray() != null && order.getTaxRateObjectArray().length > 1
//                && showTaxOnMultipleLines)
//        {
//            int scale = getTaxMgr().getTaxScale();
//            ArrayList<OrderTotal> otList = new ArrayList<OrderTotal>();
//
//            // Get the total tax rate
//            BigDecimal totalTaxRate = new BigDecimal(0);
//            for (int i = 0; i < order.getTaxRateObjectArray().length; i++)
//            {
//                TaxRate tr = (TaxRate) order.getTaxRateObjectArray()[i];
//                totalTaxRate = totalTaxRate.add(tr.getRate());
//            }
//
//            for (int i = 0; i < order.getTaxRateObjectArray().length; i++)
//            {
//                TaxRate tr = (TaxRate) order.getTaxRateObjectArray()[i];
//
//                OrderTotal singleOt = new OrderTotal();
//                singleOt.setSortOrder(sd.getSortOrder());
//                singleOt.setClassName(code);
//                singleOt.setTitle(tr.getDescription());
//                BigDecimal multiplier = tr.getRate().divide(totalTaxRate, 5, BigDecimal.ROUND_HALF_UP);
//                BigDecimal taxValue = (order.getTax().multiply(multiplier)).setScale(scale, BigDecimal.ROUND_HALF_UP);
//                singleOt.setValue(taxValue);
//                singleOt.setText(getCurrMgr().formatPrice(taxValue, order.getCurrencyCode()));
//                otList.add(singleOt);
//            }
//            ot = new OrderTotal();
//            ot.setSortOrder(sd.getSortOrder());
//            ot.setClassName(code);
//            ot.setOrderTotals(otList.toArray(new OrderTotal[0]));
//            return ot;
//        }

        /*
         * Display tax using a single Order Total
         */
        ot = new OrderTotal();
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
            title.append(rb.getString(MODULE_ORDER_TOTAL_TAX_TITLE));
        }
        title.append(":");
        ot.setTitle(title.toString());
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
