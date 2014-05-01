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

package com.konakart.bl.modules.shipping.item;

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
import com.konakart.app.ShippingQuote;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.shipping.BaseShippingModule;
import com.konakart.bl.modules.shipping.ShippingInfo;
import com.konakart.bl.modules.shipping.ShippingInterface;
import com.workingdogs.village.DataSetException;

/**
 * This shipping module implements a flat rate per item which is set in the configuration property
 * MODULE_SHIPPING_ITEM_COST . If MODULE_SHIPPING_ITEM_TAX_CLASS is greater than zero, then tax is
 * added if the shipping address is in a taxable zone. The handling charge defined by
 * MODULE_SHIPPING_ITEM_HANDLING is also added.
 */
public class Item extends BaseShippingModule implements ShippingInterface
{// Module name must be the same as the class name although it can be all in lowercase
    private static String code = "item";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.item.Item";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "itemMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_ITEM_STATUS = "MODULE_SHIPPING_ITEM_STATUS";

    private final static String MODULE_SHIPPING_ITEM_COST = "MODULE_SHIPPING_ITEM_COST";

    private final static String MODULE_SHIPPING_ITEM_HANDLING = "MODULE_SHIPPING_ITEM_HANDLING";

    private final static String MODULE_SHIPPING_ITEM_TAX_CLASS = "MODULE_SHIPPING_ITEM_TAX_CLASS";

    private final static String MODULE_SHIPPING_ITEM_ZONE = "MODULE_SHIPPING_ITEM_ZONE";

    private final static String MODULE_SHIPPING_ITEM_SORT_ORDER = "MODULE_SHIPPING_ITEM_SORT_ORDER";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_ITEM_TEXT_TITLE = "module.shipping.item.text.title";

    private final static String MODULE_SHIPPING_ITEM_TEXT_DESCRIPTION = "module.shipping.item.text.description";

    private final static String MODULE_SHIPPING_ITEM_TEXT_WAY = "module.shipping.item.text.way";

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
    public Item(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
     * From the ShippingCountry we find the iso code of the country and determine its zone.
     * 
     * @param order
     * @return Returns a ShippingQuote object
     * @throws KKException
     */
    public ShippingQuote getQuote(Order order, ShippingInfo info) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());
        // Throws an exception if there are no physical products. They may be all digital download
        // products.
        checkForProducts(info);

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, info
                .getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        // Get a partially filled ShippingQuote object
        ShippingQuote quote = this.getShippingQuote(rb);

        /*
         * The global parameter zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
         */
        if (sd.getZone() > 0)
        {
            checkZone(info, sd.getZone());
        }

        // Create the return string
        StringBuffer retTextBuf = new StringBuffer();
        retTextBuf.append(rb.getString(MODULE_SHIPPING_ITEM_TEXT_WAY));
        quote.setResponseText(retTextBuf.toString());

        // Return the cost
        BigDecimal orderCost = sd.getCost().multiply(new BigDecimal(info.getNumProducts()));
        quote.setCost(orderCost);
        quote.setHandlingCost(sd.getHandling());
        BigDecimal costPlusHandling = orderCost.add(sd.getHandling());
        if (sd.getTaxClass() > 0)
        {
            quote.setTax(getEng().getTax(costPlusHandling, info.getDeliveryCountry().getId(),
                    info.getDeliveryZone().getZoneId(), sd.getTaxClass()));
            quote.setTotalExTax(costPlusHandling);
            quote.setTotalIncTax(quote.getTax().add(costPlusHandling));
        } else
        {
            quote.setTax(new BigDecimal(0));
            quote.setTotalExTax(costPlusHandling);
            quote.setTotalIncTax(costPlusHandling);
        }
        return quote;
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

        conf = getConfiguration(MODULE_SHIPPING_ITEM_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_ITEM_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_ITEM_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_ITEM_COST);
        if (conf == null)
        {
            staticData.setCost(new BigDecimal(0));
        } else
        {
            staticData.setCost(new BigDecimal(conf.getValue()));
        }

        conf = getConfiguration(MODULE_SHIPPING_ITEM_HANDLING);
        if (conf == null)
        {
            staticData.setHandling(new BigDecimal(0));
        } else
        {
            staticData.setHandling(new BigDecimal(conf.getValue()));
        }
    }

    /**
     * 
     * @param rb
     * @return A ShippingQuote object
     * @throws KKException
     */
    private ShippingQuote getShippingQuote(ResourceBundle rb) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        ShippingQuote quote = new ShippingQuote();

        // Populate some attributes from static data
        quote.setCode(code);
        quote.setModuleCode(code);
        quote.setSortOrder(sd.getSortOrder());
        quote.setIcon(icon);
        quote.setTaxClass(sd.getTaxClass());

        // Populate locale specific attributes from the resource bundle
        quote.setDescription(rb.getString(MODULE_SHIPPING_ITEM_TEXT_DESCRIPTION));
        quote.setTitle(rb.getString(MODULE_SHIPPING_ITEM_TEXT_TITLE));

        return quote;
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_ITEM_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private int taxClass;

        private int zone;

        private BigDecimal cost;

        private BigDecimal handling;

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
         * @return the taxClass
         */
        public int getTaxClass()
        {
            return taxClass;
        }

        /**
         * @param taxClass
         *            the taxClass to set
         */
        public void setTaxClass(int taxClass)
        {
            this.taxClass = taxClass;
        }

        /**
         * @return the zone
         */
        public int getZone()
        {
            return zone;
        }

        /**
         * @param zone
         *            the zone to set
         */
        public void setZone(int zone)
        {
            this.zone = zone;
        }

        /**
         * @return the cost
         */
        public BigDecimal getCost()
        {
            return cost;
        }

        /**
         * @param cost
         *            the cost to set
         */
        public void setCost(BigDecimal cost)
        {
            this.cost = cost;
        }

        /**
         * @return the handling
         */
        public BigDecimal getHandling()
        {
            return handling;
        }

        /**
         * @param handling
         *            the handling to set
         */
        public void setHandling(BigDecimal handling)
        {
            this.handling = handling;
        }
    }

}
