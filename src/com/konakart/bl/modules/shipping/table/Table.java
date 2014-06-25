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

package com.konakart.bl.modules.shipping.table;

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
import com.konakart.app.ShippingQuote;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.shipping.BaseShippingModule;
import com.konakart.bl.modules.shipping.ShippingInfo;
import com.konakart.bl.modules.shipping.ShippingInterface;
import com.konakart.bl.modules.shipping.WeightCost;
import com.workingdogs.village.DataSetException;

/**
 * This shipping module implements a rate per item weight or a rate based on the total cost. The
 * items passed to this module have already been split up into individual packages based on the
 * maximum weight allowed per single package. If MODULE_SHIPPING_TABLE_TAX_CLASS is greater than
 * zero, then tax is added if the shipping address is in a taxable zone. The handling charge defined
 * by MODULE_SHIPPING_TABLE_HANDLING is also added.
 */
public class Table extends BaseShippingModule implements ShippingInterface
{// Module name must be the same as the class name although it can be all in lowercase
    private static String code = "table";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.table.Table";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "tableMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_TABLE_STATUS = "MODULE_SHIPPING_TABLE_STATUS";

    private final static String MODULE_SHIPPING_TABLE_COST = "MODULE_SHIPPING_TABLE_COST";

    private final static String MODULE_SHIPPING_TABLE_HANDLING = "MODULE_SHIPPING_TABLE_HANDLING";

    private final static String MODULE_SHIPPING_TABLE_TAX_CLASS = "MODULE_SHIPPING_TABLE_TAX_CLASS";

    private final static String MODULE_SHIPPING_TABLE_ZONE = "MODULE_SHIPPING_TABLE_ZONE";

    private final static String MODULE_SHIPPING_TABLE_SORT_ORDER = "MODULE_SHIPPING_TABLE_SORT_ORDER";

    private final static String MODULE_SHIPPING_TABLE_MODE = "MODULE_SHIPPING_TABLE_MODE";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_TABLE_TEXT_TITLE = "module.shipping.table.text.title";

    private final static String MODULE_SHIPPING_TABLE_TEXT_DESCRIPTION = "module.shipping.table.text.description";

    private final static String MODULE_SHIPPING_TABLE_TEXT_WAY = "module.shipping.table.text.way";

    // private final static String MODULE_SHIPPING_TABLE_TEXT_WEIGHT =
    // "module.shipping.table.text.weight";

    // private final static String MODULE_SHIPPING_TABLE_TEXT_AMOUNT =
    // "module.shipping.table.text.amount";

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
    public Table(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        BigDecimal cost = new BigDecimal(0);
        if (sd.getMode().equalsIgnoreCase(sd.getWeight()))
        {
            // Go through the weight cost list and figure out the cost

            /*
             * There is a list of weights since the total order weight may exceed the maximum weight
             * for a single package and so it has already been split up by the manager calling this
             * module.
             */
            for (Iterator<BigDecimal> iter = info.getOrderWeightList().iterator(); iter.hasNext();)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Box Weight    = " + info.getBoxWeight());
                }
                BigDecimal totalWeight = iter.next().add(info.getBoxWeight());
                if (log.isDebugEnabled())
                {
                    log.debug("Total Weight  = " + totalWeight);
                }

                for (Iterator<WeightCost> iter1 = sd.getWeightCostList().iterator(); iter1
                        .hasNext();)
                {
                    WeightCost wc = iter1.next();
                    if (totalWeight.compareTo(wc.getWeight()) == -1)
                    {
                        cost = cost.add(wc.getCost());
                        if (log.isDebugEnabled())
                        {
                            log.debug("Total Cost    = " + cost);
                        }
                        break;
                    }
                }
            }
        } else if (sd.getMode().equalsIgnoreCase(sd.getPrice()))
        {
            for (Iterator<WeightCost> iter1 = sd.getWeightCostList().iterator(); iter1.hasNext();)
            {
                WeightCost wc = iter1.next();
                if (order.getTotalIncTax().compareTo(wc.getWeight()) == -1)
                {
                    cost = wc.getCost();
                    break;
                }
            }
        } else
        {
            throw new KKException(
                    "The mode (MODULE_SHIPPING_TABLE_MODE) must be set to price or weight");
        }

        // Set all of the cost attributes
        quote.setCost(cost);
        quote.setHandlingCost(sd.getHandling());
        BigDecimal costPlusHandling = cost.add(sd.getHandling());

        if (log.isDebugEnabled())
        {
            log.debug("Cost+Handling = " + costPlusHandling);
        }

        if (sd.getTaxClass() > 0 && info.getDeliveryZone() != null)
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

        // Create the return string
        StringBuffer retTextBuf = new StringBuffer();
        retTextBuf.append(rb.getString(MODULE_SHIPPING_TABLE_TEXT_WAY));
        quote.setResponseText(retTextBuf.toString());

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

        conf = getConfiguration(MODULE_SHIPPING_TABLE_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_TABLE_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_TABLE_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_TABLE_HANDLING);
        if (conf == null)
        {
            staticData.setHandling(new BigDecimal(0));
        } else
        {
            staticData.setHandling(new BigDecimal(conf.getValue()));
        }

        conf = getConfiguration(MODULE_SHIPPING_TABLE_MODE);
        if (conf == null)
        {
            staticData.setMode("");
        } else
        {
            staticData.setMode(new String(conf.getValue()));
        }

        // Create the static list
        createWeightCostList();
    }

    /**
     * From the configuration data, we fill a list with the weight cost info
     * 
     * @throws KKException
     */
    private void createWeightCostList() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        KKConfiguration conf = getConfiguration(MODULE_SHIPPING_TABLE_COST);
        if (conf != null)
        {
            if (sd.getWeightCostList() == null)
            {
                sd.setWeightCostList(new ArrayList<WeightCost>());
            } else
            {
                sd.getWeightCostList().clear();
            }

            String weightCosts = conf.getValue();
            if (weightCosts != null)
            {
                String[] weightCostsArray = weightCosts.split(",");
                for (int i = 0; i < weightCostsArray.length; i++)
                {
                    String weightCost = weightCostsArray[i];
                    String[] weightCostArray = weightCost.split(":");
                    if (weightCostArray.length == 2)
                    {
                        WeightCost wc = new WeightCost(new BigDecimal(weightCostArray[0]),
                                new BigDecimal(weightCostArray[1]));
                        sd.getWeightCostList().add(wc);
                    }
                }
            }
            
            if (log.isDebugEnabled())
            {
                log.debug("WeightCost List: (defined as " + weightCosts + ")");
                for (int i = 0; i < sd.getWeightCostList().size(); i++)
                {
                    WeightCost wc = sd.getWeightCostList().get(i);
                    log.debug(i + ") \t" + wc.getWeight() + " \t" + wc.getCost());
                }
            }
        }
    }

    /**
     * Get a partially-filled ShippingQuote
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
        quote.setDescription(rb.getString(MODULE_SHIPPING_TABLE_TEXT_DESCRIPTION));
        quote.setTitle(rb.getString(MODULE_SHIPPING_TABLE_TEXT_TITLE));

        return quote;
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_TABLE_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private int taxClass;

        private int zone;

        private BigDecimal handling;

        private String mode;

        private List<WeightCost> weightCostList = null;

        private final String weight = "weight";

        private final String price = "price";

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

        /**
         * @return the mode
         */
        public String getMode()
        {
            return mode;
        }

        /**
         * @param mode
         *            the mode to set
         */
        public void setMode(String mode)
        {
            this.mode = mode;
        }

        /**
         * @return the weightCostList
         */
        public List<WeightCost> getWeightCostList()
        {
            return weightCostList;
        }

        /**
         * @param weightCostList
         *            the weightCostList to set
         */
        public void setWeightCostList(List<WeightCost> weightCostList)
        {
            this.weightCostList = weightCostList;
        }

        /**
         * @return the weight
         */
        public String getWeight()
        {
            return weight;
        }

        /**
         * @return the price
         */
        public String getPrice()
        {
            return price;
        }

    }
}
