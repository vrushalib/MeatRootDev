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
package com.konakart.bl.modules.payment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.Country;
import com.konakart.app.Currency;
import com.konakart.app.GeoZone;
import com.konakart.app.KKException;
import com.konakart.app.Language;
import com.konakart.app.Order;
import com.konakart.app.PaymentDetails;
import com.konakart.app.Zone;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.BaseMgr;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.LanguageMgr;
import com.konakart.bl.modules.BaseModule;
import com.konakart.blif.ConfigurationMgrIf;
import com.konakart.blif.PaymentMgrIf;

/**
 * PaymentMgr
 */
public class PaymentMgr extends BaseMgr implements PaymentMgrIf
{
    /*
     * Static data
     */

    /** the log */
    protected static Log log = LogFactory.getLog(PaymentMgr.class);

    protected static String mutex = "paymentMgrMutex";

    /** Hash Map that contains the static data */
    protected static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys
    protected static final String MODULE_PAYMENT_INSTALLED = "MODULE_PAYMENT_INSTALLED";

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     */
    public PaymentMgr(KKEngIf eng) throws Exception
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
     * @throws Exception
     */
    public void refreshConfigs() throws Exception
    {
        synchronized (mutex)
        {
            if (log.isInfoEnabled())
            {
                log.info("Refresh configs for PaymentMgr of storeId " + getStoreId());
            }

            StaticData sd = staticDataHM.get(getStoreId());
            if (sd == null)
            {
                sd = new StaticData();
                staticDataHM.put(getStoreId(), sd);
            }

            ConfigurationMgrIf mgr = getConfigMgr();

            if (sd.getPaymentModuleList() == null)
            {
                sd.setPaymentModuleList(new ArrayList<String>());
            } else
            {
                sd.getPaymentModuleList().clear();
            }

            String modulesString = mgr.getConfigurationValue(false, MODULE_PAYMENT_INSTALLED);
            if (modulesString != null)
            {
                String[] modulesStringArray = modulesString.split(";");
                for (int i = 0; i < modulesStringArray.length; i++)
                {
                    // Remove any extension that the file name may have such as php
                    String[] moduleNameExtArray = modulesStringArray[i].split("\\.");
                    sd.getPaymentModuleList().add(getJavaModuleName(moduleNameExtArray[0]));
                    if (log.isDebugEnabled())
                    {
                        log.debug("Payment Module Defined: " + moduleNameExtArray[0]);
                    }
                }
            }

            String storeName = mgr.getConfigurationValue(false, ConfigConstants.STORE_NAME);
            if (storeName == null)
            {
                sd.setStoreName("Not Set");
            } else
            {
                sd.setStoreName(storeName);
            }

            sd.setDisplayPriceWithTax(mgr.getConfigurationValueAsBool(false,
                    ConfigConstants.DISPLAY_PRICE_WITH_TAX, true));

            // Now we need to get a list of Payment modules to refresh them all
            for (Iterator<String> iter = sd.getPaymentModuleList().iterator(); iter.hasNext();)
            {
                String moduleName = iter.next();
                // Instantiate the module
                if (moduleName != null)
                {
                    try
                    {
                        PaymentInterface paymentModule = getPaymentModuleForName(moduleName);
                        paymentModule.setStaticVariables();
                    } catch (Exception e)
                    {
                        log.error("Could not instantiate the Payment Module " + moduleName
                                + " in order to refresh its configuration.", e);
                    }
                }
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
        String baseName = BaseModule.basePackage + ".payment.";
        String s1 = moduleName.substring(0, 1);
        String s2 = moduleName.substring(1, moduleName.length());
        String retName = baseName + moduleName.toLowerCase() + "." + s1.toUpperCase() + s2;
        return retName;
    }

    /**
     * Each payment module is called and asked to return a PaymentDetails object. This method is
     * called during the checkout process before the order has been saved and confirmed by the user.
     * Therefore it does not require the exact details for the payment (i.e. the parameters passed
     * to a payment gateway) since this will be done later for the selected payment module. The
     * returned PaymentDetails object contains the information required for the user to select it
     * and based on the zone of the billing address the module may decide that it cannot process
     * payment for that zone and so not even return a PaymentDetails object.
     * 
     * @param order
     * @param languageId
     * @return Returns an array of PaymentDetail objects
     * @throws Exception
     */
    public PaymentDetails[] getPaymentGateways(OrderIf order, int languageId) throws Exception
    {
        return getPaymentGatewaysPrivate(order, null, languageId);
    }

    /**
     * Return the PaymentDetail object for one payment gateway.
     * 
     * @param order
     * @param moduleName
     *            can be module name on its own or it can be "moduleName~~moduleSubCode" if the
     *            module has a subCode
     * @param languageId
     * @return Returns an array of PaymentDetail objects
     * @throws Exception
     */
    public PaymentDetails getPaymentGateway(OrderIf order, String moduleName, int languageId)
            throws Exception
    {
        checkRequired(moduleName, "String", "moduleName");

        int dividerIdx = moduleName.indexOf("~~");
        String moduleCode = moduleName;
        String subCode = null;

        if (dividerIdx != -1)
        {
            moduleCode = moduleName.substring(0, dividerIdx);
            subCode = moduleName.substring(dividerIdx + 2);
        }

        if (log.isDebugEnabled())
        {
            log.debug("paymentCode = " + moduleName + " => code = " + moduleCode + " subCode = "
                    + subCode);
        }

        PaymentDetails[] paymentDetails = getPaymentGatewaysPrivate(order, moduleCode, languageId);
        if (paymentDetails != null && paymentDetails.length > 0)
        {
            if (subCode != null)
            {
                // Find the payment details by subCode
                for (int p = 0; p < paymentDetails.length; p++)
                {
                    PaymentDetails pd = paymentDetails[p];
                    if (pd.getSubCode().equals(subCode))
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Returning gateway with correct subCode " + "\n"
                                    + pd.toString());
                        }
                        return pd;
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Payment module with code " + moduleCode + " and subCode = "
                            + subCode + " not found");
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("Returning first gateway of " + paymentDetails.length + "\n"
                        + paymentDetails[0].toString());
            }
            return paymentDetails[0];
        }

        if (log.isDebugEnabled())
        {
            log.debug("Returning null");
        }
        return null;
    }

    /**
     * @param order
     * @param moduleName
     * @param languageId
     * @return Returns an array of PaymentDetail objects
     * @throws Exception
     */
    protected PaymentDetails[] getPaymentGatewaysPrivate(OrderIf order, String moduleName,
            int languageId) throws Exception
    {
        checkRequired(order, "Order", "order");

        StaticData sd = staticDataHM.get(getStoreId());

        // Ensure that the currency object is instantiated
        addCurrencyToOrder((Order) order);

        // Calculate the totals for the order
        int scale = getTaxMgr().getTaxScale();
        int taxQuantityRule = getTaxMgr().getTaxQuantityRule();

        ((Order) order).calculateTotals(scale, taxQuantityRule);

        // Get a partially filled payment info object
        PaymentInfo info = getPaymentInfo((Order) order, languageId);

        // Don't return details for the payment
        info.setReturnDetails(false);

        // If we have been passed the module name, then we only get a payment detail for this module
        String jModuleName = null;
        if (moduleName != null)
        {
            // Check that the module is in our list of installed modules
            boolean found = false;
            jModuleName = getJavaModuleName(moduleName);
            for (Iterator<String> iter = sd.getPaymentModuleList().iterator(); iter.hasNext();)
            {
                String modName = iter.next();
                if (modName != null && modName.equals(jModuleName))
                {
                    found = true;
                }
            }
            if (!found)
            {
                return null;
            }

            if (log.isDebugEnabled())
            {
                log.debug("PaymentModule Found in list of Available modules : " + moduleName
                        + " (" + jModuleName + ")");
            }

            PaymentInterface paymentModule = getPaymentModuleForName(jModuleName);
            if (paymentModule.isAvailable())
            {
                PaymentDetails payDet = paymentModule.getPaymentDetails((Order) order, info);

                // There may be multiple Payment Details on the PaymentDetails object returned
                if (payDet.getPaymentDetails() != null && payDet.getPaymentDetails().length > 0)
                {
                    return (PaymentDetails[]) payDet.getPaymentDetails();
                }

                return new PaymentDetails[]
                { payDet };
            }
            return null;
        }

        // Create a return object to contain a list of PaymentDetails
        List<PaymentDetails> retList = new ArrayList<PaymentDetails>();

        // Now we need to get a list of Payment modules and to get a quote from each one
        for (Iterator<String> iter = sd.getPaymentModuleList().iterator(); iter.hasNext();)
        {
            String modName = iter.next();
            // Instantiate the module
            if (modName != null)
            {
                try
                {
                    PaymentInterface paymentModule = getPaymentModuleForName(modName);
                    if (paymentModule.isAvailable())
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("PaymentModule Available: " + modName);
                        }

                        PaymentDetails payDet = paymentModule
                                .getPaymentDetails((Order) order, info);

                        // There may be multiple Payment Details on the PaymentDetails object
                        // returned
                        if (payDet.getPaymentDetails() != null
                                && payDet.getPaymentDetails().length > 0)
                        {
                            for (int i = 0; i < payDet.getPaymentDetails().length; i++)
                            {
                                PaymentDetailsIf subPayDet = payDet.getPaymentDetails()[i];
                                retList.add((PaymentDetails) subPayDet);
                                if (log.isDebugEnabled())
                                {
                                    log.debug("PaymentModule " + modName
                                            + "\n\t SubTypes Available: " + " code = "
                                            + subPayDet.getCode() + " subCode = "
                                            + subPayDet.getSubCode() + " title = "
                                            + subPayDet.getTitle());
                                }
                            }
                        } else
                        {
                            retList.add(payDet);
                        }
                    }
                } catch (KKException e)
                {
                    log.warn("Called the getPaymentDetails method on module " + modName
                            + ". The module isn't available because of the following problem: "
                            + e.getMessage());
                } catch (Exception e)
                {
                    log.error("Could not instantiate the Payment Module " + moduleName + " ("
                            + jModuleName + ")", e);
                }
            }
        }

        // Sort the return list
        Collections.sort(retList, new SortOrderComparator());

        // Convert the return list into a return array, and then return it
        PaymentDetails[] retArray = new PaymentDetails[retList.size()];
        int i = 0;
        for (Iterator<PaymentDetails> iter = retList.iterator(); iter.hasNext();)
        {
            PaymentDetails payDet = iter.next();
            retArray[i++] = payDet;
        }

        return retArray;
    }

    /**
     * This method is called once a payment gateway has been selected and an order has been saved in
     * the database. The order is read from the database and the details of the payment are
     * retrieved from the order. This ensures that the payment request matches the order exactly.
     * <p>
     * The module matching the module code is called in order to get a fully populated
     * PaymentDetails object containing all of the required parameters etc. for the payment gateway.
     * 
     * @param sessionId
     * @param moduleCode
     * @param orderId
     * @param hostAndPort
     * @param languageId
     * @return Return an array of PaymentDetail objects
     * @throws Exception
     */
    public PaymentDetails getPaymentDetails(String sessionId, String moduleCode, int orderId,
            String hostAndPort, int languageId) throws Exception
    {
        checkRequired(sessionId, "String", "sessionId");
        checkRequired(moduleCode, "String", "moduleCode");
        checkRequired(hostAndPort, "String", "hostPort");

        // Get the order
        Order order = getOrderMgr().getOrder(sessionId, orderId, languageId);
        if (order == null)
        {
            throw new KKException("Cannot find order for Id = " + orderId);
        }

        return getPaymentDetailsPerOrder(sessionId, moduleCode, order, hostAndPort, languageId);
    }

    /**
     * This method is called once a payment gateway has been selected even if the order has not been
     * saved in the database. The details of the payment are retrieved from the order.
     * <p>
     * The module matching the module code is called in order to get a fully populated
     * PaymentDetails object containing all of the required parameters etc. for the payment gateway.
     * 
     * @param sessionId
     * @param moduleCode
     * @param order
     * @param hostAndPort
     * @param languageId
     * @return Return an array of PaymentDetail objects
     * @throws Exception
     */
    public PaymentDetails getPaymentDetailsPerOrder(String sessionId, String moduleCode,
            OrderIf order, String hostAndPort, int languageId) throws Exception
    {
        checkRequired(sessionId, "String", "sessionId");
        checkRequired(moduleCode, "String", "moduleCode");
        checkRequired(hostAndPort, "String", "hostPort");
        checkRequired(order, "Order", "order");

        if (order.getOrderTotals() == null)
        {
            throw new KKException("The order with Id = " + order.getId() + " has no OrderTotals");
        }

        // Ensure that the currency object is instantiated
        addCurrencyToOrder((Order) order);

        // Get a partially filled payment info object
        PaymentInfo info = getPaymentInfo((Order) order, languageId);

        // Return details for the payment
        info.setReturnDetails(true);

        // Set the host and port for redirection
        info.setHostAndPort(hostAndPort);

        // Instantiate the payment module
        String moduleName = getJavaModuleName(moduleCode);

        if (moduleName != null)
        {
            try
            {
                PaymentInterface paymentModule = getPaymentModuleForName(moduleName);
                if (paymentModule.isAvailable())
                {
                    PaymentDetails payDet = paymentModule.getPaymentDetails((Order) order, info);
                    return payDet;
                }
            } catch (KKException e)
            {
                log.warn("Called the getPaymentDetails method on module " + moduleName
                        + ". The module isn't available because of the following problem: "
                        + e.getMessage());
            } catch (Exception e)
            {
                log.error("Could not instantiate the Payment Module " + moduleName, e);
            }
        }
        return null;

    }

    /**
     * Returns a partially filled payment info object
     * 
     * @param order
     * @param languageId
     * @return Returns a PaymentInfo object
     * @throws Exception
     */
    protected PaymentInfo getPaymentInfo(Order order, int languageId) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());

        PaymentInfo info = new PaymentInfo();

        // Add some static data
        info.setStoreName(sd.getStoreName());
        info.setDisplayPriceWithTax(sd.displayPriceWithTax);

        // Add the locale
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
        info.setLocale(new Locale(lang.getCode()));

        // Add GeoZones
        // Get the delivery country from the database
        Country billingCountry = getTaxMgr().getCountryPerName(order.getBillingCountry());
        if (billingCountry == null)
        {
            throw new KKException("Cannot find the delivery country " + order.getBillingCountry()
                    + " in the database");
        }

        // Get the billing zone using the country and state
        Zone zone = null;

        if (order.getBillingState() != null && order.getBillingState().length() > 0)
        {
            zone = getTaxMgr().getZonePerCountryAndCode(billingCountry.getId(),
                    order.getBillingState());
        }

        if (zone != null)
        {
            // Get an array of GeoZones from the zone. i.e. The zone could be Florida, USA and
            // we want to know whether there are one or more GeoZones for this zone
            GeoZone[] geoZones = getTaxMgr().getGeoZonesPerZone(zone);
            info.setDeliveryGeoZoneArray(geoZones);
        }

        return info;
    }

    /**
     * Ensure that the order has the currency object instantiated.
     * 
     * @param order
     * @throws Exception
     */
    protected void addCurrencyToOrder(Order order) throws Exception
    {

        // Ensure that the currency object is instantiated
        if (order.getCurrency() == null)
        {
            Currency curr = getCurrMgr().getCurrency(order.getCurrencyCode());
            if (curr == null)
            {
                throw new KKException("Cannot find a currency for currency code = "
                        + order.getCurrencyCode());
            }
            order.setCurrency(curr);
        }
    }

    protected class SortOrderComparator implements Comparator<Object>
    {
        /**
         * @param o1
         * @param o2
         * @return Return Do objects compare
         */
        public int compare(Object o1, Object o2)
        {
            PaymentDetails pd1 = (PaymentDetails) o1;
            PaymentDetails pd2 = (PaymentDetails) o2;
            if (pd1.getSortOrder() > pd2.getSortOrder())
            {
                return 1;
            } else if (pd1.getSortOrder() < pd2.getSortOrder())
            {
                return -1;
            } else
            {
                // The same sort order so now compare the subSortOrder
                if (pd1.getSubSortOrder() > pd2.getSubSortOrder())
                {
                    return 1;
                } else if (pd1.getSubSortOrder() < pd2.getSubSortOrder())
                {
                    return -1;
                }

                return 0;
            }
        }
    }

    /**
     * Called to instantiate a payment module. It determines whether the module has a constructor
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
    PaymentInterface getPaymentModuleForName(String moduleName) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException
    {
        Class<?> paymentModuleClass = Class.forName(moduleName);
        PaymentInterface paymentModule = null;
        Constructor<?>[] constructors = paymentModuleClass.getConstructors();
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
            paymentModule = (PaymentInterface) engConstructor.newInstance(getEng());
            if (log.isDebugEnabled())
            {
                log.debug("Called KKEngIf constructor for " + moduleName);
            }
        } else
        {
            paymentModule = (PaymentInterface) paymentModuleClass.newInstance();
            if (log.isDebugEnabled())
            {
                log.debug("Called empty constructor for " + moduleName);
            }
        }
        return paymentModule;
    }

    /**
     * Used to store the static data of this manager
     */
    protected class StaticData
    {
        String storeName;

        boolean displayPriceWithTax;

        // Configuration data
        List<String> paymentModuleList;

        /**
         * @return Returns the storeName.
         */
        public String getStoreName()
        {
            return storeName;
        }

        /**
         * @param storeName
         *            The storeName to set.
         */
        public void setStoreName(String storeName)
        {
            this.storeName = storeName;
        }

        /**
         * @return Returns the displayPriceWithTax.
         */
        public boolean isDisplayPriceWithTax()
        {
            return displayPriceWithTax;
        }

        /**
         * @param displayPriceWithTax
         *            The displayPriceWithTax to set.
         */
        public void setDisplayPriceWithTax(boolean displayPriceWithTax)
        {
            this.displayPriceWithTax = displayPriceWithTax;
        }

        /**
         * @return Returns the paymentModuleList.
         */
        public List<String> getPaymentModuleList()
        {
            return paymentModuleList;
        }

        /**
         * @param paymentModuleList
         *            The paymentModuleList to set.
         */
        public void setPaymentModuleList(List<String> paymentModuleList)
        {
            this.paymentModuleList = paymentModuleList;
        }
    }
}
