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

package com.konakart.bl.modules.shipping.freeproduct;

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
import com.konakart.appif.OrderProductIf;
import com.konakart.bl.ProductMgr;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.shipping.BaseShippingModule;
import com.konakart.bl.modules.shipping.ShippingInfo;
import com.konakart.bl.modules.shipping.ShippingInterface;
import com.workingdogs.village.DataSetException;

/**
 * This shipping module returns an exception if the order contains any physical products that
 * require paid shipping so that the actual shipping cost is taken care of by another module. It
 * only returns a quote of 0 if the order contains no physical products that require paid shipping
 * but does contain physical products that don't require paid shipping.
 */
public class FreeProduct extends BaseShippingModule implements ShippingInterface
{// Module name must be the same as the class name although it can be all in lowercase
    private static String code = "freeproduct";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.freeproduct.FreeProduct";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "freeProductMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_FREE_PROD_SORT_ORDER = "MODULE_SHIPPING_FREE_PROD_SORT_ORDER";

    private final static String MODULE_SHIPPING_FREE_PROD_STATUS = "MODULE_SHIPPING_FREE_PROD_STATUS";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_FREE_PROD_TEXT_TITLE = "module.shipping.freeproduct.text.title";

    private final static String MODULE_SHIPPING_FREE_PROD_TEXT_DESCRIPTION = "module.shipping.freeproduct.text.description";

    // private final static String MODULE_SHIPPING_FREE_PROD_TEXT_NOTE =
    // "module.shipping.freeproduct.text.note";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
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
     * Return a quote of 0 if there are no physical products without free shipping. Otherwise throw
     * an exception.
     * 
     * @param order
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    public ShippingQuote getQuote(Order order, ShippingInfo info) throws Exception
    {
        // Check the products
        if (info.getNumProducts() > 0)
        {
            throw new KKException("There are some physical products in the order");
        }

        // Check the free shipping products
        if (!(info.getNumFreeShipping() > 0))
        {
            throw new KKException("There are no free shipping products in the order");
        }

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
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProductIf op = order.getOrderProducts()[i];
            if (op.getType() == ProductMgr.FREE_SHIPPING
                    || op.getType() == ProductMgr.FREE_SHIPPING_BUNDLE_PRODUCT_TYPE)
            {
                retTextBuf.append(op.getName());
                retTextBuf.append(", ");
            }
        }

        // Remove final comma and space
        retTextBuf.delete(retTextBuf.length() - 2, retTextBuf.length());
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
                MODULE_SHIPPING_FREE_PROD_SORT_ORDER, 0));
    }

    /**
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
        quote.setDescription(rb.getString(MODULE_SHIPPING_FREE_PROD_TEXT_DESCRIPTION));
        quote.setTitle(rb.getString(MODULE_SHIPPING_FREE_PROD_TEXT_TITLE));

        return quote;
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_FREE_PROD_STATUS);
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
