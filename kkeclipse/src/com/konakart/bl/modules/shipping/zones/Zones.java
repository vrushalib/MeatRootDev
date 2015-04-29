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

package com.konakart.bl.modules.shipping.zones;

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

import com.konakart.app.Country;
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
 * This shipping module implements a rate per item weight per zone . The items passed to this module
 * have already been split up into individual packages based on the maximum weight allowed per
 * single package. If MODULE_SHIPPING_ZONES_TAX_CLASS is greater than zero, then tax is added if the
 * shipping address is in a taxable zone. The handling charge defined by
 * MODULE_SHIPPING_ZONES_HANDLING_ is also added.
 */
public class Zones extends BaseShippingModule implements ShippingInterface
{// Module name must be the same as the class name although it can be all in lowercase
    private static String code = "zones";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.zones.Zones";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "zonesMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_ZONES_COUNTRIES_ = "MODULE_SHIPPING_ZONES_COUNTRIES_";

    private final static String MODULE_SHIPPING_ZONES_COST_ = "MODULE_SHIPPING_ZONES_COST_";

    private final static String MODULE_SHIPPING_ZONES_HANDLING_ = "MODULE_SHIPPING_ZONES_HANDLING_";

    private final static String MODULE_SHIPPING_ZONES_SORT_ORDER = "MODULE_SHIPPING_ZONES_SORT_ORDER";

    private final static String MODULE_SHIPPING_ZONES_TAX_CLASS = "MODULE_SHIPPING_ZONES_TAX_CLASS";

    private final static String MODULE_SHIPPING_ZONES_STATUS = "MODULE_SHIPPING_ZONES_STATUS";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_ZONES_TEXT_TITLE = "module.shipping.zones.text.title";

    private final static String MODULE_SHIPPING_ZONES_TEXT_DESCRIPTION = "module.shipping.zones.text.description";

    private final static String MODULE_SHIPPING_ZONES_TEXT_WAY = "module.shipping.zones.text.way";

    private final static String MODULE_SHIPPING_ZONES_TEXT_UNITS = "module.shipping.zones.text.units";

    private final static String MODULE_SHIPPING_ZONES_INVALID_ZONE = "module.shipping.zones.invalid.zone";

    // private final static String MODULE_SHIPPING_ZONES_UNDEFINED_RATE =
    // "module.shipping.zones.undefined.rate";

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
    public Zones(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        // Get the country object for the shipping country name
        Country shippingCountry = getEng().getCountryPerName(order.getDeliveryCountry());
        if (shippingCountry == null)
        {
            throw new KKException("A country matching the name " + order.getDeliveryCountry()
                    + " cannot be found in the database");
        }

        // Get the zone
        Integer Zone = sd.getCountryToZoneMap().get(shippingCountry.getIsoCode2());
        if (Zone == null)
        {
            quote.setResponseText(rb.getString(MODULE_SHIPPING_ZONES_INVALID_ZONE));
            throw new KKException("The delivery address is not within a valid shipping zone");
        }

        // Get the weight cost list for this zone
        List<WeightCost> weightCostList = sd.getZoneToWeightCostMap().get(Zone);

        // Go through the weight cost list and figure out the cost
        BigDecimal cost = new BigDecimal(0);

        // There is a list of weights since the total order weight may exceed the maximum weight for
        // a single package and so it has already been split up by the manager calling this module.
        for (Iterator<BigDecimal> iter = info.getOrderWeightList().iterator(); iter.hasNext();)
        {
            BigDecimal totalWeight = iter.next().add(info.getBoxWeight());
            for (Iterator<WeightCost> iter1 = weightCostList.iterator(); iter1.hasNext();)
            {
                WeightCost wc = iter1.next();
                if (totalWeight.compareTo(wc.getWeight()) == -1)
                {
                    cost = cost.add(wc.getCost());
                    break;
                }
            }
        }

        // Set all of the cost attributes
        BigDecimal handlingCost = sd.getZoneToHandlingMap().get(Zone);
        quote.setCost(cost);
        quote.setHandlingCost(handlingCost);
        BigDecimal costPlusHandling = cost.add(handlingCost);
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

        // Create the return string Shipping to IT : 17 lb(s)
        StringBuffer retTextBuf = new StringBuffer();
        retTextBuf.append(rb.getString(MODULE_SHIPPING_ZONES_TEXT_WAY));
        retTextBuf.append(" ");
        retTextBuf.append(shippingCountry.getIsoCode2());
        retTextBuf.append(" : ");
        for (Iterator<BigDecimal> iter = info.getOrderWeightList().iterator(); iter.hasNext();)
        {
            retTextBuf.append(iter.next().add(info.getBoxWeight()));
            retTextBuf.append(" ");
            retTextBuf.append(rb.getString(MODULE_SHIPPING_ZONES_TEXT_UNITS));
            retTextBuf.append(" ");
        }
        retTextBuf.deleteCharAt(retTextBuf.length() - 1);
        quote.setResponseText(retTextBuf.toString());

        return quote;
    }

    /**
     * From the configuration data, we fill a static hash table with the mapping info
     * 
     * @throws KKException
     * 
     */
    private void createCountryToZoneMap() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        Integer zone = new Integer(1);
        boolean keepLooping = true;
        do
        {
            KKConfiguration conf = getConfiguration(
                    MODULE_SHIPPING_ZONES_COUNTRIES_ + zone.intValue());
            if (conf != null)
            {
                String countries = conf.getValue();
                if (countries != null)
                {
                    String[] countriesArray = countries.split(",");
                    for (int i = 0; i < countriesArray.length; i++)
                    {
                        sd.getCountryToZoneMap().put(countriesArray[i], zone);
                    }
                }
                zone = new Integer(zone.intValue() + 1);
            } else
            {
                keepLooping = false;
            }
        } while (keepLooping);
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

        conf = getConfiguration(MODULE_SHIPPING_ZONES_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_ZONES_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        // Create the static maps from the configuration info
        if (staticData.getCountryToZoneMap() == null)
        {
            staticData.setCountryToZoneMap(new HashMap<String, Integer>());
        } else
        {
            staticData.getCountryToZoneMap().clear();
        }
        createCountryToZoneMap();

        if (staticData.getZoneToWeightCostMap() == null)
        {
            staticData.setZoneToWeightCostMap(new HashMap<Integer, List<WeightCost>>());
        } else
        {
            staticData.getZoneToWeightCostMap().clear();
        }
        createZoneToWeightCostMap();

        if (staticData.getZoneToHandlingMap() == null)
        {
            staticData.setZoneToHandlingMap(new HashMap<Integer, BigDecimal>());
        } else
        {
            staticData.getZoneToHandlingMap().clear();
        }

        createZoneToHandlingMap();
    }

    /**
     * From the configuration data, we fill a static hash table with the mapping info of zone to
     * handling charge
     * 
     * @throws KKException
     * 
     */
    private void createZoneToHandlingMap() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        Integer zone = new Integer(1);
        boolean keepLooping = true;
        do
        {
            KKConfiguration conf = getConfiguration(
                    MODULE_SHIPPING_ZONES_HANDLING_ + zone.intValue());
            if (conf != null)
            {
                String handling = conf.getValue();
                sd.getZoneToHandlingMap().put(zone, new BigDecimal(handling));
                zone = new Integer(zone.intValue() + 1);
            } else
            {
                keepLooping = false;
            }
        } while (keepLooping);
    }

    /**
     * From the configuration data, we fill a static hash table with the mapping info
     * 
     * @throws KKException
     */
    private void createZoneToWeightCostMap() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        Integer zone = new Integer(1);
        boolean keepLooping = true;
        do
        {
            KKConfiguration conf = getConfiguration(
                    MODULE_SHIPPING_ZONES_COST_ + zone.intValue());
            if (conf != null)
            {
                List<WeightCost> weightCostList = new ArrayList<WeightCost>();
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
                            weightCostList.add(wc);
                        }
                    }
                }
                sd.getZoneToWeightCostMap().put(zone, weightCostList);
                zone = new Integer(zone.intValue() + 1);
            } else
            {
                keepLooping = false;
            }
        } while (keepLooping);
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
        quote.setDescription(rb.getString(MODULE_SHIPPING_ZONES_TEXT_DESCRIPTION));
        quote.setTitle(rb.getString(MODULE_SHIPPING_ZONES_TEXT_TITLE));

        return quote;
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_ZONES_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private HashMap<String, Integer> countryToZoneMap = null;

        private HashMap<Integer, List<WeightCost>> zoneToWeightCostMap = null;

        private HashMap<Integer, BigDecimal> zoneToHandlingMap = null;

        private int sortOrder = -1;

        private int taxClass;

        /**
         * @return the countryToZoneMap
         */
        public HashMap<String, Integer> getCountryToZoneMap()
        {
            return countryToZoneMap;
        }

        /**
         * @param countryToZoneMap
         *            the countryToZoneMap to set
         */
        public void setCountryToZoneMap(HashMap<String, Integer> countryToZoneMap)
        {
            this.countryToZoneMap = countryToZoneMap;
        }

        /**
         * @return the zoneToWeightCostMap
         */
        public HashMap<Integer, List<WeightCost>> getZoneToWeightCostMap()
        {
            return zoneToWeightCostMap;
        }

        /**
         * @param zoneToWeightCostMap
         *            the zoneToWeightCostMap to set
         */
        public void setZoneToWeightCostMap(HashMap<Integer, List<WeightCost>> zoneToWeightCostMap)
        {
            this.zoneToWeightCostMap = zoneToWeightCostMap;
        }

        /**
         * @return the zoneToHandlingMap
         */
        public HashMap<Integer, BigDecimal> getZoneToHandlingMap()
        {
            return zoneToHandlingMap;
        }

        /**
         * @param zoneToHandlingMap
         *            the zoneToHandlingMap to set
         */
        public void setZoneToHandlingMap(HashMap<Integer, BigDecimal> zoneToHandlingMap)
        {
            this.zoneToHandlingMap = zoneToHandlingMap;
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
