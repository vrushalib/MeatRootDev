//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.bl.modules.shipping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.Country;
import com.konakart.app.GeoZone;
import com.konakart.app.KKException;
import com.konakart.app.Language;
import com.konakart.app.Order;
import com.konakart.app.OrderProduct;
import com.konakart.app.ShippingQuote;
import com.konakart.app.Zone;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.BaseMgr;
import com.konakart.bl.LanguageMgr;
import com.konakart.bl.ProductMgr;
import com.konakart.bl.modules.BaseModule;
import com.konakart.blif.ConfigurationMgrIf;
import com.konakart.blif.ProductMgrIf;
import com.konakart.blif.ShippingMgrIf;
import com.konakart.blif.TaxMgrIf;
import com.konakart.util.KKConstants;

/**
 * Shipping Manager
 */
public class ShippingMgr extends BaseMgr implements ShippingMgrIf
{
    /*
     * Static data
     */

    /** the log */
    protected static Log log = LogFactory.getLog(ShippingMgr.class);

    protected static String mutex = "shippingMgrMutex";

    protected static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    protected static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    /*
     * Static final data
     */

    // Configuration Keys
    protected final static String SHIPPING_ORIGIN_COUNTRY = "SHIPPING_ORIGIN_COUNTRY";

    protected final static String SHIPPING_ORIGIN_ZIP = "SHIPPING_ORIGIN_ZIP";

    protected final static String SHIPPING_MAX_WEIGHT = "SHIPPING_MAX_WEIGHT";

    protected final static String SHIPPING_BOX_WEIGHT = "SHIPPING_BOX_WEIGHT";

    protected final static String SHIPPING_BOX_PADDING = "SHIPPING_BOX_PADDING";

    protected final static String MODULE_SHIPPING_INSTALLED = "MODULE_SHIPPING_INSTALLED";

    protected final static String FREE_SHIPPING = "MODULE_ORDER_TOTAL_SHIPPING_FREE_SHIPPING";

    protected final static String FREE_SHIPPING_OVER = "MODULE_ORDER_TOTAL_SHIPPING_FREE_SHIPPING_OVER";

    protected final static String SHIPPING_DESTINATION = "MODULE_ORDER_TOTAL_SHIPPING_DESTINATION";

    protected final static String STORE_COUNTRY = "STORE_COUNTRY";

    protected final static String NATIONAL = "national";

    protected final static String INTERNATIONAL = "international";

    protected final static String BOTH = "both";

    // Message Catalogue Keys

    protected final static String FREE_SHIPPING_TITLE = "free.shipping.title";

    protected final static String FREE_SHIPPING_DESCRIPTION = "free.shipping.description";

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     */
    public ShippingMgr(KKEngIf eng) throws Exception
    {
        super.init(eng, log);

        StaticData sd = staticDataHM.get(getStoreId());

        if (sd == null)
        {
            synchronized (mutex)
            {
                sd = staticDataHM.get(getStoreId());
                if (sd == null)
                {
                    refreshConfigs();
                }
            }
        }
    }

    /**
     * Refresh Configuration Variables
     * 
     * @throws Exception
     */
    public void refreshConfigs() throws Exception
    {
        synchronized (mutex)
        {
            if (log.isInfoEnabled())
            {
                log.info("Refresh configs for ShippingMgr of storeId " + getStoreId());
            }

            ConfigurationMgrIf mgr = getConfigMgr();

            StaticData sd = staticDataHM.get(getStoreId());
            if (sd == null)
            {
                sd = new StaticData();
                staticDataHM.put(getStoreId(), sd);
            }

            int originCountry = mgr.getConfigurationValueAsInt(false, SHIPPING_ORIGIN_COUNTRY);
            if (originCountry != KKConstants.NOT_SET)
            {
                sd.setShippingOriginCountry(getTaxMgr().getCountryPerId(originCountry));
            }

            String originZip = mgr.getConfigurationValue(false, SHIPPING_ORIGIN_ZIP);
            if (originZip != null)
            {
                sd.setShippingOriginZip(originZip);
            }

            try
            {
                sd.setShippingMaxWeight(mgr.getConfigurationValueAsBigDecimal(false,
                        SHIPPING_MAX_WEIGHT, new BigDecimal(1000000)));
            } catch (Exception e1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem setting " + SHIPPING_MAX_WEIGHT + " : " + e1.getMessage());
                }
                sd.setShippingMaxWeight(new BigDecimal(1000000));
            }

            try
            {
                sd.setShippingBoxWeight(mgr.getConfigurationValueAsBigDecimal(false,
                        SHIPPING_BOX_WEIGHT, new BigDecimal(0)));
            } catch (Exception e1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem setting " + SHIPPING_BOX_WEIGHT + " : " + e1.getMessage());
                }
                sd.setShippingBoxWeight(new BigDecimal(0));
            }

            try
            {
                sd.setShippingBoxPadding(mgr.getConfigurationValueAsBigDecimal(false,
                        SHIPPING_BOX_PADDING, new BigDecimal(0)));
            } catch (Exception e1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem setting " + SHIPPING_BOX_PADDING + " : " + e1.getMessage());
                }
                sd.setShippingBoxPadding(new BigDecimal(0));
            }

            if (sd.getShippingModuleList() == null)
            {
                sd.setShippingModuleList(new ArrayList<String>());
            } else
            {
                sd.getShippingModuleList().clear();
            }

            String modsInstalled = mgr.getConfigurationValue(false, MODULE_SHIPPING_INSTALLED);
            if (log.isDebugEnabled())
            {
                log.debug("MODULE_SHIPPING_INSTALLED = " + modsInstalled);
            }

            if (modsInstalled != null)
            {
                String[] modulesStringArray = modsInstalled.split(";");
                for (int i = 0; i < modulesStringArray.length; i++)
                {
                    // Remove any extension that the file name may have such as php
                    String[] moduleNameExtArray = modulesStringArray[i].split("\\.");
                    sd.getShippingModuleList().add(getJavaModuleName(moduleNameExtArray[0]));
                    if (log.isDebugEnabled())
                    {
                        log.debug("Shipping Module Defined: " + moduleNameExtArray[0]);
                    }
                }
            }

            try
            {
                sd.setFreeShipping(mgr.getConfigurationValueAsBool(false, FREE_SHIPPING, false));
            } catch (Exception e1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem setting " + FREE_SHIPPING + " : " + e1.getMessage());
                }
                sd.setFreeShipping(false);
            }

            try
            {
                sd.setFreeShippingOver(mgr.getConfigurationValueAsBigDecimal(false,
                        FREE_SHIPPING_OVER, new BigDecimal(0)));
            } catch (Exception e1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem setting " + FREE_SHIPPING_OVER + " : " + e1.getMessage());
                }
                sd.setFreeShippingOver(new BigDecimal(0));
            }

            String shipDestination = mgr.getConfigurationValue(false, SHIPPING_DESTINATION);
            if (shipDestination != null)
            {
                sd.setShippingDestination(shipDestination);
            }

            int storeCountryInt = KKConstants.NOT_SET;
            try
            {
                storeCountryInt = mgr.getConfigurationValueAsInt(false, STORE_COUNTRY);
            } catch (Exception e1)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem setting " + STORE_COUNTRY + " : " + e1.getMessage());
                }
            }

            if (storeCountryInt != KKConstants.NOT_SET)
            {
                sd.setStoreCountryId(storeCountryInt);
            }

            // Now we need to get a list of Shipping modules to refresh them all
            for (Iterator<String> iter = sd.getShippingModuleList().iterator(); iter.hasNext();)
            {
                String moduleName = iter.next();
                // Instantiate the module
                if (moduleName != null)
                {
                    try
                    {
                        ShippingInterface shippingModule = getShippingModuleForName(moduleName);
                        shippingModule.setStaticVariables();
                    } catch (Exception e)
                    {
                        log.error("Could not instantiate the Shipping Module " + moduleName
                                + " in order to refresh its configuration.", e);
                    }
                }
            }
        }
    }

    /**
     * 
     * @return Returns a partially filled in ShippingInfo object
     * @throws KKException
     */
    protected ShippingInfo getShippingInfo() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        ShippingInfo sh = new ShippingInfo();
        sh.setBoxPadding(sd.getShippingBoxPadding());
        sh.setBoxWeight(sd.getShippingBoxWeight());
        sh.setMaxWeight(sd.getShippingMaxWeight());
        sh.setOriginCountry(sd.getShippingOriginCountry());
        sh.setOriginZip(sd.getShippingOriginZip());
        return sh;
    }

    /**
     * @param order
     * @param languageId
     * @return An array of shipping quotes
     * @throws Exception
     */
    public ShippingQuote[] getShippingQuotes(OrderIf order, int languageId) throws Exception
    {
        return getShippingQuotesPrivate(order, null, languageId);
    }

    /**
     * @param order
     * @param moduleName
     * @param languageId
     * @return A shipping quote
     * @throws Exception
     */
    public ShippingQuote getShippingQuote(OrderIf order, String moduleName, int languageId)
            throws Exception
    {
        checkRequired(moduleName, "String", "moduleName");

        ShippingQuote[] quotes = getShippingQuotesPrivate(order, moduleName, languageId);
        if (quotes != null && quotes.length > 0)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Shipping Quote Returned for Module: " + moduleName);
            }
            return quotes[0];
        }

        if (log.isDebugEnabled())
        {
            log.debug("No Shipping Quote Returned for Module: " + moduleName);
        }
        return null;
    }

    /**
     * @param order
     * @param languageId
     * @return An array of shipping quotes
     * @throws Exception
     */
    protected ShippingQuote[] getShippingQuotesPrivate(OrderIf order, String moduleName,
            int languageId) throws Exception
    {
        checkRequired(order, "Order", "order");

        if (log.isDebugEnabled())
        {
            if (moduleName != null)
            {
                log.debug("get Quote for Module : " + moduleName);
            }
        }

        StaticData sd = staticDataHM.get(getStoreId());

        // Instantiate a Tax Mgr object
        TaxMgrIf taxMgr = getTaxMgr();

        // Check that the order is valid
        checkOrder((Order) order);

        // Calculate the totals for the order
        int scale = getTaxMgr().getTaxScale();
        int taxQuantityRule = getTaxMgr().getTaxQuantityRule();
        ((Order) order).calculateTotals(scale, taxQuantityRule);

        Language lang;
        if (languageId == LanguageMgr.DEFAULT_LANG)
        {
            lang = getLangMgr().getDefaultLanguage();
        } else
        {
            lang = getLangMgr().getLanguagePerId(languageId);
        }

        if (lang == null)
        {
            throw new KKException("A language object could not be found for a language Id of "
                    + languageId);
        }

        // Get a partially filled in ShippingInfo object
        ShippingInfo info = getShippingInfo();

        // Get the delivery country from the database
        Country deliveryCountry = taxMgr.getCountryPerName(order.getDeliveryCountry());
        if (deliveryCountry == null)
        {
            throw new KKException("Cannot find the delivery country " + order.getDeliveryCountry()
                    + " in the database");
        }
        info.setDeliveryCountry(deliveryCountry);

        // Get the delivery zone using the country and state
        Zone zone = null;

        if (order.getDeliveryState() != null && order.getDeliveryState().length() > 0)
        {
            zone = taxMgr.getZonePerCountryAndCode(deliveryCountry.getId(),
                    order.getDeliveryState());
        }
        if (zone != null)
        {
            info.setDeliveryZone(zone);
            // Get an array of GeoZones from the zone. i.e. The zone could be Florida, USA and
            // we want to know whether there are one or more GeoZones for this zone
            GeoZone[] geoZones = taxMgr.getGeoZonesPerZone(zone);
            info.setDeliveryGeoZoneArray(geoZones);
        }

        // Determine whether shipping is free
        if (sd.isFreeShipping())
        {
            boolean free = false;

            // Free shipping for national deliveries. If shippingDestimation is null, we assume that
            // shipping is free for all destinations.
            if (sd.getShippingDestination() == null)
            {
                free = true;
            } else
            {
                if (sd.getShippingDestination().equalsIgnoreCase(NATIONAL))
                {
                    // Free shipping for national deliveries
                    if (sd.getStoreCountryId() == deliveryCountry.getId())
                    {
                        free = true;
                    }
                } else if (sd.getShippingDestination().equalsIgnoreCase(INTERNATIONAL))
                {
                    // Free shipping for international deliveries
                    if (sd.getStoreCountryId() != deliveryCountry.getId())
                    {
                        free = true;
                    }
                } else if (sd.getShippingDestination().equalsIgnoreCase(BOTH))
                {
                    // Free shipping for all deliveries
                    free = true;
                }
            }
            if (order.getTotalIncTax().compareTo(sd.getFreeShippingOver()) >= 0 && free)
            {
                // The total cost is above the minimum required for free shipping
                ShippingQuote quote = new ShippingQuote();
                quote.setCode("free_mgr");
                quote.setFree(true);
                quote.setTotalIncTax(new BigDecimal(0));
                quote.setTotalExTax(new BigDecimal(0));
                quote.setTax(new BigDecimal(0));
                quote.setFreeShippingOver(sd.getFreeShippingOver());

                // Get the resource bundle for the messages
                ResourceBundle rb = getResourceBundle(new Locale(lang.getCode()));
                if (rb == null)
                {
                    throw new KKException("Cannot find messages for the locale " + lang.getCode());
                }
                quote.setDescription(rb.getString(FREE_SHIPPING_DESCRIPTION));
                quote.setTitle(rb.getString(FREE_SHIPPING_TITLE));
                quote.setResponseText(rb.getString(FREE_SHIPPING_DESCRIPTION));
                ShippingQuote[] retArray = new ShippingQuote[1];
                retArray[0] = quote;
                return retArray;
            }
        }

        // Calculate the weight and number of products
        int numProducts = 0, numDigitalDownloads = 0, numBookableProds = 0, numVirtualProds = 0, numGiftCertificates = 0, numFreeShipping = 0;
        BigDecimal weight = new BigDecimal(0);
        BigDecimal maxWeight = sd.getShippingMaxWeight().subtract(sd.getShippingBoxWeight());
        List<BigDecimal> orderWeightList = new ArrayList<BigDecimal>();
        ProductMgrIf pMgr = getProdMgr();
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProduct op = (OrderProduct) order.getOrderProducts()[i];

            // Don't consider digital downloads, gift certificates, bookable or virtual products
            if (op.getType() == ProductMgr.DIGITAL_DOWNLOAD)
            {
                numDigitalDownloads += op.getQuantity();
                continue;
            }

            if (op.getType() == ProductMgr.GIFT_CERTIFICATE_PRODUCT_TYPE)
            {
                numGiftCertificates += op.getQuantity();
                continue;
            }

            if (op.getType() == ProductMgr.BOOKABLE_PRODUCT_TYPE)
            {
                numBookableProds += op.getQuantity();
                continue;
            }
            
            if (op.getType() == ProductMgr.VIRTUAL_PRODUCT_TYPE)
            {
                numVirtualProds += op.getQuantity();
                continue;
            }

            // Don't consider free shipping products
            if (op.getType() == ProductMgr.FREE_SHIPPING
                    || op.getType() == ProductMgr.FREE_SHIPPING_BUNDLE_PRODUCT_TYPE)
            {
                numFreeShipping += op.getQuantity();
                continue;
            }

            if (op.getProduct() == null)
            {
                op.setProduct(pMgr.getProduct(null, op.getProductId(), languageId));
            }

            // Increment product count
            numProducts += op.getQuantity();

            // Check that we don't exceed max package weight
            if (op.getProduct().getWeight().compareTo(maxWeight) > 0)
            {
                throw new KKException("The product " + op.getProduct().getName() + " weighs "
                        + op.getProduct().getWeight()
                        + " which exceeds the maximum shipping weight of " + maxWeight
                        + " for a single item.");
            }

            // Iterate through all products to divide them into individual packages based on their
            // weight and the maximum package weight allowed.
            for (int j = 0; j < op.getQuantity(); j++)
            {
                BigDecimal newWeight = weight.add(op.getProduct().getWeight());
                if (newWeight.compareTo(maxWeight) > 0)
                {
                    // Before creating a new package, lets iterate through the current ones to see
                    // if it will fit
                    boolean added = false;
                    for (Iterator<BigDecimal> iter = orderWeightList.iterator(); iter.hasNext();)
                    {
                        BigDecimal packageWeight = iter.next();
                        if (packageWeight.add(op.getProduct().getWeight()).compareTo(maxWeight) <= 0)
                        {
                            packageWeight.add(op.getProduct().getWeight());
                            added = true;
                            break;
                        }
                    }

                    // If it doesn't fit, we need to create a new package
                    if (!added)
                    {
                        orderWeightList.add(weight);
                        weight = new BigDecimal(0).add(op.getProduct().getWeight());
                    }
                } else
                {
                    weight = newWeight;
                }
            }
        }
        orderWeightList.add(weight);

        // Add number of products to ShippingInfo
        info.setNumProducts(numProducts);

        // Add number of digital download products to ShippingInfo
        info.setNumDigitalDownloads(numDigitalDownloads);

        // Add number of gift certificates to ShippingInfo
        info.setNumGiftCertificates(numGiftCertificates);

        // Add number of bookable products to ShippingInfo
        info.setNumBookableProducts(numBookableProds);
        
        // Add number of virtual products to ShippingInfo
        info.setNumVirtualProducts(numVirtualProds);
        
        // Add number of free shipping products to ShippingInfo
        info.setNumFreeShipping(numFreeShipping);

        // Add list of weights to ShippingInfo
        info.setOrderWeightList(orderWeightList);

        // Add locale to Shipping Info
        info.setLocale(new Locale(lang.getCode()));

        // If we have been passed the module name, then we only get a quote for this module
        if (moduleName != null)
        {
            // Check that the module is in our list of installed modules
            boolean found = false;
            String compareName = getJavaModuleName(moduleName);
            for (Iterator<String> iter = sd.getShippingModuleList().iterator(); iter.hasNext();)
            {
                String modName = iter.next();
                if (modName != null && modName.equals(compareName))
                {
                    found = true;
                }
            }
            if (!found)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Unable to find shipping module with name = " + moduleName);
                }
                return null;
            }

            moduleName = getJavaModuleName(moduleName);
            ShippingInterface shippingModule = getShippingModuleForName(moduleName);
            if (shippingModule.isAvailable())
            {
                ShippingQuote quote = shippingModule.getQuote((Order) order, info);
                return new ShippingQuote[]
                { quote };
            }
            return null;

        }

        // Create a return object to contain a list of ShippingQuotes
        List<ShippingQuoteIf> retList = new ArrayList<ShippingQuoteIf>();

        // Now we need to get a list of Shipping modules and to get a quote from each one
        for (Iterator<String> iter = sd.getShippingModuleList().iterator(); iter.hasNext();)
        {
            String modName = iter.next();
            // Instantiate the module
            if (modName != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Getting quotes for shipping module : " + modName);
                }
                try
                {
                    ShippingInterface shippingModule = getShippingModuleForName(modName);
                    if (shippingModule.isAvailable())
                    {
                        ShippingQuote quote = shippingModule.getQuote((Order) order, info);
                        if (quote != null)
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("Quote returned from Shipping Module : " + modName
                                        + " : " + quote.toString());
                            }
                            if (quote.getQuotes() != null)
                            {
                                for (int i = 0; i < quote.getQuotes().length; i++)
                                {
                                    ShippingQuoteIf multiQuote = quote.getQuotes()[i];
                                    retList.add(multiQuote);
                                }
                            } else
                            {
                                retList.add(quote);
                            }
                        } else
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("Quote returned from Shipping Module : " + modName
                                        + " : Null");
                            }
                        }
                    }
                } catch (KKException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Called the getQuote method on module " + modName
                                + ". The module isn't available because of the following problem: "
                                + e.getMessage());
                    }
                } catch (Exception e)
                {
                    log.error("Could not instantiate the Shipping Module " + moduleName, e);
                }
            }
        }

        // Sort the return list
        Collections.sort(retList, new SortOrderComparator());

        // Convert the return list into a return array, and then return it
        ShippingQuote[] retArray = new ShippingQuote[retList.size()];
        int i = 0;
        for (Iterator<ShippingQuoteIf> iter = retList.iterator(); iter.hasNext();)
        {
            ShippingQuote quote = (ShippingQuote) iter.next();
            retArray[i++] = quote;
        }

        return retArray;
    }

    protected class SortOrderComparator implements Comparator<Object>
    {
        /**
         * @param o1
         * @param o2
         * @return Return Do objects compare
         * 
         */
        public int compare(Object o1, Object o2)
        {
            ShippingQuote sq1 = (ShippingQuote) o1;
            ShippingQuote sq2 = (ShippingQuote) o2;
            if (sq1.getSortOrder() > sq2.getSortOrder())
            {
                return 1;
            } else if (sq1.getSortOrder() < sq2.getSortOrder())
            {
                return -1;
            } else
            {
                return 0;
            }
        }
    }

    /**
     * Whatever the module name is, the package name is the module name in lowercase and the class
     * name starts with an upper case character
     * 
     * @param moduleName
     * @return Returns the java compatible name
     */
    protected String getJavaModuleName(String moduleName)
    {
        if (moduleName == null || moduleName.length() == 0)
        {
            return null;
        }
        String baseName = BaseModule.basePackage + ".shipping.";
        String s1 = moduleName.substring(0, 1);
        String s2 = moduleName.substring(1, moduleName.length());
        String retName = baseName + moduleName.toLowerCase() + "." + s1.toUpperCase() + s2;
        return retName;
    }

    /**
     * Checks the order to see whether all compulsory attributes are present
     * 
     * @param order
     * @throws KKException
     */
    protected void checkOrder(Order order) throws KKException
    {
        if (order.getDeliveryCountry() == null)
        {
            throw new KKException("The order id = " + order.getId()
                    + "does not have a valid delivery country");
        }

        if (order.getOrderProducts() == null)
        {
            throw new KKException("The order id = " + order.getId() + " has no products");
        }
    }

    /**
     * The resource bundle is fetched for the locale and stored in a static hash table. Next time
     * around it is retrieved from the hash table.
     * 
     * @param locale
     * @return The resource bundle referenced by the locale
     * @throws KKException
     */
    public ResourceBundle getResourceBundle(Locale locale) throws KKException
    {
        synchronized (mutex)
        {
            ResourceBundle rb = resourceBundleMap.get(locale);
            if (rb == null)
            {
                rb = ResourceBundle
                        .getBundle(BaseModule.basePackage + ".shipping.Shipping", locale);
                resourceBundleMap.put(locale, rb);
                return rb;
            }
            return rb;
        }
    }

    /**
     * Called to instantiate a shipping module. It determines whether the module has a constructor
     * where KKEng is passed in and if it does, then this constructor is used. Otherwise the empty
     * constructor is used.
     * 
     * @param moduleName
     * @return Returns an instantiated Payment Module
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    ShippingInterface getShippingModuleForName(String moduleName) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException
    {
        Class<?> ShippingModuleClass = Class.forName(moduleName);
        ShippingInterface shippingModule = null;
        Constructor<?>[] constructors = ShippingModuleClass.getConstructors();
        Constructor<?> engConstructor = null;
        if (constructors != null && constructors.length > 0)
        {
            for (int i = 0; i < constructors.length; i++)
            {
                Constructor<?> constructor = constructors[i];
                Class<?>[] parmTypes = constructor.getParameterTypes();
                if (parmTypes != null && parmTypes.length == 1)
                {
                    String parmName = parmTypes[0].getName();
                    if (parmName != null && parmName.equals("com.konakart.appif.KKEngIf"))
                    {
                        engConstructor = constructor;
                    }
                }
            }
        }

        if (engConstructor != null)
        {
            shippingModule = (ShippingInterface) engConstructor.newInstance(getEng());
            if (log.isDebugEnabled())
            {
                log.debug("Called KKEngIf constructor for " + moduleName);
            }
        } else
        {
            shippingModule = (ShippingInterface) ShippingModuleClass.newInstance();
            if (log.isDebugEnabled())
            {
                log.debug("Called empty constructor for " + moduleName);
            }
        }
        return shippingModule;
    }

    /**
     * Used to store the static data of this manager
     */
    protected class StaticData
    {
        Country shippingOriginCountry;

        String shippingOriginZip;

        BigDecimal shippingMaxWeight;

        BigDecimal shippingBoxWeight;

        BigDecimal shippingBoxPadding;

        List<String> shippingModuleList;

        boolean freeShipping;

        BigDecimal freeShippingOver;

        String shippingDestination;

        int storeCountryId;

        /**
         * @return Returns the shippingOriginCountry.
         */
        public Country getShippingOriginCountry()
        {
            return shippingOriginCountry;
        }

        /**
         * @param shippingOriginCountry
         *            The shippingOriginCountry to set.
         */
        public void setShippingOriginCountry(Country shippingOriginCountry)
        {
            this.shippingOriginCountry = shippingOriginCountry;
        }

        /**
         * @return Returns the shippingOriginZip.
         */
        public String getShippingOriginZip()
        {
            return shippingOriginZip;
        }

        /**
         * @param shippingOriginZip
         *            The shippingOriginZip to set.
         */
        public void setShippingOriginZip(String shippingOriginZip)
        {
            this.shippingOriginZip = shippingOriginZip;
        }

        /**
         * @return Returns the shippingMaxWeight.
         */
        public BigDecimal getShippingMaxWeight()
        {
            return shippingMaxWeight;
        }

        /**
         * @param shippingMaxWeight
         *            The shippingMaxWeight to set.
         */
        public void setShippingMaxWeight(BigDecimal shippingMaxWeight)
        {
            this.shippingMaxWeight = shippingMaxWeight;
        }

        /**
         * @return Returns the shippingBoxWeight.
         */
        public BigDecimal getShippingBoxWeight()
        {
            return shippingBoxWeight;
        }

        /**
         * @param shippingBoxWeight
         *            The shippingBoxWeight to set.
         */
        public void setShippingBoxWeight(BigDecimal shippingBoxWeight)
        {
            this.shippingBoxWeight = shippingBoxWeight;
        }

        /**
         * @return Returns the shippingBoxPadding.
         */
        public BigDecimal getShippingBoxPadding()
        {
            return shippingBoxPadding;
        }

        /**
         * @param shippingBoxPadding
         *            The shippingBoxPadding to set.
         */
        public void setShippingBoxPadding(BigDecimal shippingBoxPadding)
        {
            this.shippingBoxPadding = shippingBoxPadding;
        }

        /**
         * @return Returns the shippingModuleList.
         */
        public List<String> getShippingModuleList()
        {
            return shippingModuleList;
        }

        /**
         * @param shippingModuleList
         *            The shippingModuleList to set.
         */
        public void setShippingModuleList(List<String> shippingModuleList)
        {
            this.shippingModuleList = shippingModuleList;
        }

        /**
         * @return Returns the freeShipping.
         */
        public boolean isFreeShipping()
        {
            return freeShipping;
        }

        /**
         * @param freeShipping
         *            The freeShipping to set.
         */
        public void setFreeShipping(boolean freeShipping)
        {
            this.freeShipping = freeShipping;
        }

        /**
         * @return Returns the freeShippingOver.
         */
        public BigDecimal getFreeShippingOver()
        {
            return freeShippingOver;
        }

        /**
         * @param freeShippingOver
         *            The freeShippingOver to set.
         */
        public void setFreeShippingOver(BigDecimal freeShippingOver)
        {
            this.freeShippingOver = freeShippingOver;
        }

        /**
         * @return Returns the shippingDestination.
         */
        public String getShippingDestination()
        {
            return shippingDestination;
        }

        /**
         * @param shippingDestination
         *            The shippingDestination to set.
         */
        public void setShippingDestination(String shippingDestination)
        {
            this.shippingDestination = shippingDestination;
        }

        /**
         * @return Returns the storeCountryId.
         */
        public int getStoreCountryId()
        {
            return storeCountryId;
        }

        /**
         * @param storeCountryId
         *            The storeCountryId to set.
         */
        public void setStoreCountryId(int storeCountryId)
        {
            this.storeCountryId = storeCountryId;
        }

    }

}
