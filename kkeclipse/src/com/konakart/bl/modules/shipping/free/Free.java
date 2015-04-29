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

package com.konakart.bl.modules.shipping.free;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.torque.TorqueException;

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
 * This shipping module implements a free rate regardless of zone. It applies to all products and
 * always returns an amount of 0.
 */
public class Free extends BaseShippingModule implements ShippingInterface
{// Module name must be the same as the class name although it can be all in lowercase
    private static String code = "free";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.free.Free";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "freeMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_FREE_SORT_ORDER = "MODULE_SHIPPING_FREE_SORT_ORDER";

    private final static String MODULE_SHIPPING_FREE_STATUS = "MODULE_SHIPPING_FREE_STATUS";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_FREE_TEXT_TITLE = "module.shipping.free.text.title";

    private final static String MODULE_SHIPPING_FREE_TEXT_DESCRIPTION = "module.shipping.free.text.description";

    private final static String MODULE_SHIPPING_FREE_TEXT_NOTE = "module.shipping.free.text.note";

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
    public Free(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
     * Return a quote of 0 every time, regardless of the ShippingInfo or Order
     * 
     * @param order
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    public ShippingQuote getQuote(Order order, ShippingInfo info) throws Exception
    {
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

        // Create the return string
        StringBuffer retTextBuf = new StringBuffer();
        retTextBuf.append(rb.getString(MODULE_SHIPPING_FREE_TEXT_NOTE));
        quote.setResponseText(retTextBuf.toString());

        // Return the cost attributes
        quote.setCost(new BigDecimal(0));
        quote.setTax(new BigDecimal(0));
        quote.setTotalExTax(new BigDecimal(0));
        quote.setTotalIncTax(new BigDecimal(0));

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
        StaticData staticData = staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            staticData = new StaticData();
            staticDataHM.put(getStoreId(), staticData);
        }

        staticData.setSortOrder(getConfigurationValueAsIntWithDefault(
                MODULE_SHIPPING_FREE_SORT_ORDER, 0));
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
        quote.setDescription(rb.getString(MODULE_SHIPPING_FREE_TEXT_DESCRIPTION));
        quote.setTitle(rb.getString(MODULE_SHIPPING_FREE_TEXT_TITLE));

        return quote;
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_FREE_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private int taxClass = 0;

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
    }
}
