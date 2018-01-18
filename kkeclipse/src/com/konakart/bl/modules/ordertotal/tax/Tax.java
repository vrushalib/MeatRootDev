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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.torque.TorqueException;

import com.konakart.app.Country;
import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.TaxRate;
import com.konakart.app.Zone;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.blif.MultiStoreMgrIf;
import com.konakart.db.KKBasePeer;
import com.konakart.db.KKCriteria;
import com.konakart.om.BaseTaxClassPeer;
import com.konakart.util.JavaUtils;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

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

    /*
     * Set to true to show different tax rates on multiple lines. For this to work, the tax classes
     * must have the tax code set
     */
    private boolean showTaxOnMultipleLines = false;

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
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
        } else
        {
            if (!updateStaticVariablesNow(staticData.getLastUpdatedMS()))
            {
                return;
            }
        }

        conf = getConfiguration(MODULE_ORDER_TOTAL_TAX_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        staticData.setLastUpdatedMS(System.currentTimeMillis());

        staticData.getTaxClassMap().clear();

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

        StaticData sd = staticDataHM.get(getStoreId());

        // Just in case some discounts have made the total negative, we set it to zero
        if (order.getTax().signum() < 0)
        {
            order.setTax(new BigDecimal(0));
        }

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

        if (showTaxOnMultipleLines)
        {
            return getMultipleOT(order, sd, rb);
        }

        return getSingleOT(order, sd, rb);
    }

    /**
     * Display tax using a single order total
     * 
     * @param order
     * @param sd
     * @param rb
     * @return Returns a single Order Total
     * @throws Exception
     */
    protected OrderTotal getSingleOT(Order order, StaticData sd, ResourceBundle rb)
            throws Exception
    {
        OrderTotal ot = new OrderTotal();
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

    /**
     * Display tax using an order total for each tax rate
     * 
     * @param order
     * @param sd
     * @param rb
     * @return Returns an order total containing an array of order totals (one for each tax rate)
     * @throws Exception
     */
    protected OrderTotal getMultipleOT(Order order, StaticData sd, ResourceBundle rb)
            throws Exception
    {
        // Keep the order totals in a hash map
        HashMap<Integer, OrderTotal> otMap = new HashMap<Integer, OrderTotal>();
        int scale = getTaxMgr().getTaxScale();
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProductIf op = order.getOrderProducts()[i];
            if (op.getTaxCode() == null || op.getTaxCode().length() == 0)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Order Product doesn't have a tax code. All Tax Classes must be given a tax code.");
                }
                continue;
            }
            if (log.isDebugEnabled())
            {
                log.debug(op.toString());
            }

            // Get the tax class id from the tax code on the order product
            int taxClassId = getTaxClassIdFromTaxCode(op.getTaxCode());
            if (log.isDebugEnabled())
            {
                log.debug("taxClassId = " + taxClassId);
            }

            // Get an array of tax rates for the order product
            TaxRate[] taxRates = getTaxRateObjects(order, op, taxClassId);
            if (taxRates == null || taxRates.length == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("No tax rates");
                }
                continue;
            }
            if (log.isDebugEnabled())
            {
                for (int j = 0; j < taxRates.length; j++)
                {
                    TaxRate tr = taxRates[j];
                    log.debug(tr);
                }
            }

            /*
             * For each order product split up the tax for each of the tax rates and create an OT
             * module for each tax rate
             */
            BigDecimal totalTaxRate = op.getTaxRate();
            for (int j = 0; j < taxRates.length; j++)
            {
                TaxRate tr = taxRates[j];
                BigDecimal multiplier = tr.getRate().divide(totalTaxRate, 8,
                        BigDecimal.ROUND_HALF_UP);
                BigDecimal taxValue = (op.getTax().multiply(multiplier)).setScale(scale,
                        BigDecimal.ROUND_HALF_UP);

                OrderTotal singleOt = otMap.get(tr.getId());
                if (singleOt == null)
                {
                    // Create a new OT module for this tax rate
                    singleOt = new OrderTotal();
                    otMap.put(tr.getId(), singleOt);
                    singleOt.setSortOrder(sd.getSortOrder());
                    singleOt.setClassName(code);
                    singleOt.setTitle(tr.getDescription());
                    singleOt.setValue(taxValue);
                    singleOt.setText(getCurrMgr().formatPrice(taxValue, order.getCurrencyCode()));
                } else
                {
                    // Add the new value to the existing OT object
                    BigDecimal newVal = singleOt.getValue().add(taxValue);
                    singleOt.setValue(newVal);
                    singleOt.setText(getCurrMgr().formatPrice(newVal, order.getCurrencyCode()));
                }
            }
        }

        // Return all of the order total modules
        OrderTotal ot = new OrderTotal();
        ot.setSortOrder(sd.getSortOrder());
        ot.setClassName(code);
        ot.setOrderTotals(otMap.values().toArray(new OrderTotal[0]));
        return ot;
    }

    /**
     * @param order
     * @param op
     * @param taxClassId
     * @return Returns the tax rate objects for a tax class id, a country and a zone
     * @throws Exception
     */
    protected TaxRate[] getTaxRateObjects(Order order, OrderProductIf op, int taxClassId)
            throws Exception
    {
        int countryId, zoneId;

        // If zone and country objects are null, then we try and get them
        if (order.getDeliveryCountryObject() == null || order.getDeliveryZoneObject() == null)
        {
            getOrderCountryAndZone(order);
        }

        // If they are still null we set the ids to 0. If tax is set for all zones or all countries
        // then we disregard the country or zone so we must still check for tax.
        if (order.getDeliveryCountryObject() == null)
        {
            countryId = 0;
        } else
        {
            countryId = order.getDeliveryCountryObject().getId();
        }

        if (order.getDeliveryZoneObject() == null)
        {
            zoneId = 0;
        } else
        {
            zoneId = order.getDeliveryZoneObject().getZoneId();
        }

        TaxRate[] taxRates = getTaxMgr().getTaxRateObjects(countryId, zoneId, taxClassId);

        return taxRates;
    }

    /**
     * 
     * @param code
     * @return Returns the tax class id for the tax class code
     * @throws Exception
     */
    private int getTaxClassIdFromTaxCode(String code) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());
        Integer taxClassId = sd.getTaxClassMap().get(code);
        if (taxClassId != null)
        {
            return taxClassId.intValue();
        }

        KKCriteria c = getNewCriteria(isMultiStoreShareCustomersOrProducts());
        c.addSelectColumn(BaseTaxClassPeer.TAX_CLASS_ID);
        c.add(BaseTaxClassPeer.TAX_CODE, code);
        List<Record> rows = KKBasePeer.doSelect(c);
        if (rows.size() > 0)
        {
            if (rows.size() > 1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Multiple Tax Classes found for code = " + code
                            + " . The code should be unique for each tax class.");
                }
            }
            int id = rows.get(0).getValue(1).asInt();
            sd.getTaxClassMap().put(code, id);
            return id;
        }
        return 0;
    }

    /**
     * Gets a new KKCriteria object with the option of it being for all stores when in multi-store
     * single db mode.
     * 
     * @param allStores
     * 
     * @return Returns a new KKCriteria object
     */
    protected KKCriteria getNewCriteria(boolean allStores)
    {
        MultiStoreMgrIf mgr = getMultiStoreMgr();
        if (mgr != null)
        {
            return mgr.getNewCriteria(allStores);
        }

        KKCriteria crit = new KKCriteria();
        return crit;
    }

    /**
     * Returns true if we need to share customers or products in multi-store single db mode
     * 
     * @return true if we're sharing customers or products
     * @throws KKException
     */
    protected boolean isMultiStoreShareCustomersOrProducts() throws KKException
    {
        if (getEng().getEngConf() != null)
        {
            return getEng().getEngConf().isCustomersShared()
                    || getEng().getEngConf().isProductsShared();
        }

        return false;
    }

    /**
     * Instantiate the Country and Zone objects for the order
     * 
     * @param order
     * 
     * @throws Exception
     */
    protected void getOrderCountryAndZone(Order order) throws Exception
    {

        Country deliveryCountry = getTaxMgr().getCountryPerName(order.getDeliveryCountry());
        order.setDeliveryCountryObject(deliveryCountry);
        if (deliveryCountry != null)
        {
            // Get the delivery zone using the country and state
            Zone zone = null;

            if (order.getDeliveryState() != null && order.getDeliveryState().length() > 0)
            {
                zone = getTaxMgr().getZonePerCountryAndCode(deliveryCountry.getId(),
                        order.getDeliveryState());
                order.setDeliveryZoneObject(zone);
            }
        }
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

        // Stores the tax class ids for tax class code
        private HashMap<String, Integer> taxClassMap = new HashMap<String, Integer>();

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

        /**
         * @return the taxClassMap
         */
        public HashMap<String, Integer> getTaxClassMap()
        {
            return taxClassMap;
        }

        /**
         * @param taxClassMap
         *            the taxClassMap to set
         */
        public void setTaxClassMap(HashMap<String, Integer> taxClassMap)
        {
            this.taxClassMap = taxClassMap;
        }
    }
}
