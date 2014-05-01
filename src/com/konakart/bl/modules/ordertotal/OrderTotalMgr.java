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
package com.konakart.bl.modules.ordertotal;

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

import com.konakart.app.KKException;
import com.konakart.app.Language;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.bl.BaseMgr;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.LanguageMgr;
import com.konakart.bl.modules.BaseModule;
import com.konakart.blif.ConfigurationMgrIf;
import com.konakart.blif.OrderTotalMgrIf;

/**
 * Order Total Manager
 */
public class OrderTotalMgr extends BaseMgr implements OrderTotalMgrIf
{
    /*
     * Static data
     */
    /** the log */
    protected static Log log = LogFactory.getLog(OrderTotalMgr.class);

    protected static String mutex = "orderTotalMgrMutex";

    protected static String mutex1 = "orderTotalMgrMutex1";

    /** Hash Map that contains the static data */
    protected static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    /*
     * Static final data
     */
    /** ot_total */
    public static final String ot_total = "ot_total";

    /** ot_shipping */
    public static final String ot_shipping = "ot_shipping";

    /** ot_subtotal */
    public static final String ot_subtotal = "ot_subtotal";

    /** ot_tax */
    public static final String ot_tax = "ot_tax";

    // Configuration Keys
    protected final static String MODULE_ORDER_TOTAL_INSTALLED = "MODULE_ORDER_TOTAL_INSTALLED";

    // Module Class names
    /** ot_total */
    public static final String TOTAL = "ot_total";

    /** ot_subtotal */
    public static final String SUBTOTAL = "ot_subtotal";

    /** ot_shipping */
    public static final String SHIPPING = "ot_shipping";

    /** ot_tax */
    public static final String TAX = "ot_tax";

    /** ot_loworderfee */
    public static final String LOWORDERFEE = "ot_loworderfee";

    /**
     * Constructor
     * 
     * @param eng
     * @throws Exception
     */
    public OrderTotalMgr(KKEngIf eng) throws Exception
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
                log.info("Refresh configs for OrderTotalMgr of storeId " + getStoreId());
            }

            StaticData sd = staticDataHM.get(getStoreId());
            if (sd == null)
            {
                sd = new StaticData();
                staticDataHM.put(getStoreId(), sd);
            }

            ConfigurationMgrIf mgr = getConfigMgr();

            List<String> otModuleListStr = new ArrayList<String>();
            String modulesString = mgr.getConfigurationValue(false, MODULE_ORDER_TOTAL_INSTALLED);
            if (modulesString != null)
            {
                String[] modulesStringArray = modulesString.split(";");
                for (int i = 0; i < modulesStringArray.length; i++)
                {
                    // Remove any extension that the file name may have such as php
                    String[] moduleNameExtArray = modulesStringArray[i].split("\\.");
                    otModuleListStr.add(getJavaModuleName(moduleNameExtArray[0]));
                    if (log.isDebugEnabled())
                    {
                        log.debug("Order Total Module Defined: " + moduleNameExtArray[0]);
                    }
                }
            }
            sd.setOrderTotalModuleList(otModuleListStr);

            sd.setDispPriceWithTax(mgr.getConfigurationValueAsBool(false, 
                    ConfigConstants.DISPLAY_PRICE_WITH_TAX, false));

            // Now we need to get a list of order total modules to refresh them all
            List<String> otModuleListStr1 = sd.getOrderTotalModuleListCopy();
            for (Iterator<String> iter = otModuleListStr1.iterator(); iter.hasNext();)
            {
                String moduleName = iter.next();
                // Instantiate the module
                if (moduleName != null)
                {
                    try
                    {
                        OrderTotalInterface orderTotalModule = getOrderTotalModuleForName(moduleName);
                        orderTotalModule.setStaticVariables();
                    } catch (Exception e)
                    {
                        log.error("Could not instantiate the OrderTotal Module " + moduleName
                                + " in order to refresh its configuration.", e);
                    }
                }
            }
        }
    }

    /**
     * Whatever the module name is, the package name is the module name in lowercase and the class
     * name starts with an upper case character.
     * <p>
     * There are however some special cases for osCommerce compatibility
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

        String baseName = BaseModule.basePackage + ".ordertotal.";
        if (moduleName.equals("ot_subtotal"))
        {
            return baseName + "subtotal.Subtotal";
        } else if (moduleName.equals("ot_shipping"))
        {
            return baseName + "shipping.Shipping";
        } else if (moduleName.equals("ot_tax"))
        {
            return baseName + "tax.Tax";
        } else if (moduleName.equals("ot_total"))
        {
            return baseName + "total.Total";
        } else if (moduleName.equals("ot_loworderfee"))
        {
            return baseName + "loworderfee.LowOrderFee";
        } else
        {
            String s1 = moduleName.substring(0, 1);
            String s2 = moduleName.substring(1, moduleName.length());
            String retName = baseName + moduleName.toLowerCase() + "." + s1.toUpperCase() + s2;
            return retName;
        }
    }

    /**
     * @param order
     * @param languageId
     * @return An array of shipping quotes
     * @throws Exception
     */
    public Order getOrderTotals(OrderIf order, int languageId) throws Exception
    {
        checkRequired(order, "Order", "order");

        StaticData sd = staticDataHM.get(getStoreId());

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

        // Remove the current OrderTotals in case there are some and clear other fields
        order.setOrderTotals(null);
        order.setPromotionIds(null);
        order.setCouponIds(null);

        // Calculate the totals for the order
        int scale = getTaxMgr().getTaxScale();
        int taxQuantityRule = getTaxMgr().getTaxQuantityRule();

        ((Order) order).calculateTotals(scale, taxQuantityRule);

        if (order.getTotalIncTax() == null || order.getTotalExTax() == null
                || order.getTax() == null)
        {
            throw new KKException(
                    "The total amounts for the order could not be successfully calculated.");
        }

        // Get a list of tax rate objects used by the order
        order.setTaxRateObjectArray(getOrderMgr().getTaxRateObjectsPerOrder((Order) order));

        // Instantiate a list to contain the OrderTotal module objects
        List<OrderTotalInterface> otModuleList = new ArrayList<OrderTotalInterface>();

        // Instantiate a list to contain the OrderTotal objects
        List<OrderTotal> retList = new ArrayList<OrderTotal>();

        /*
         * Now we need to get a list of order total modules and to get an OrderTotal object from
         * each one. The modules are called in their sort order (each module has a sort order
         * attribute). In order to achieve this, we need to instantiate the modules and then get
         * their sort order. In this way, any module can change the total of the order as long as it
         * is called before the total. A module may want to change order.tax and order.totalIncTax.
         * i.e. You could write a module that gives a 10% discount on the total. In this case it
         * would just take 10% off order.totalIncTax.
         */
        List<String> otModuleListStr = sd.getOrderTotalModuleListCopy();
        for (Iterator<String> iter = otModuleListStr.iterator(); iter.hasNext();)
        {
            String moduleName = iter.next();
            // Instantiate the module
            if (moduleName != null)
            {
                try
                {
                    OrderTotalInterface orderTotalModule = getOrderTotalModuleForName(moduleName);
                    if (orderTotalModule.isAvailable())
                    {
                        otModuleList.add(orderTotalModule);
                    }
                } catch (KKException e)
                {
                    log.info("Attempted to instantiate the module " + moduleName
                            + ". The module isn't available because of the following problem: "
                            + e.getMessage());
                } catch (Exception e)
                {
                    log.error("Could not instantiate the OrderTotal Module " + moduleName, e);
                }
            }
        }

        // Sort the module list
        Collections.sort(otModuleList, new OrderTotalModuleSortOrderComparator());

        // Call each module giving it the current list of order totals
        for (Iterator<OrderTotalInterface> iter = otModuleList.iterator(); iter.hasNext();)
        {
            OrderTotalInterface orderTotalModule = iter.next();
            try
            {
                orderTotalModule.setOrderTotalList(retList);
                OrderTotal ot = orderTotalModule.getOrderTotal((Order) order,
                        sd.isDispPriceWithTax(), new Locale(lang.getCode()));
                if (ot != null)
                {
                    if (ot.getOrderTotals() != null && ot.getOrderTotals().length > 1)
                    {
                        for (int i = 0; i < ot.getOrderTotals().length; i++)
                        {
                            OrderTotal lot = (OrderTotal) ot.getOrderTotals()[i];
                            retList.add(lot);
                        }
                    } else
                    {
                        retList.add(ot);
                    }
                }
            } catch (KKException e)
            {
                log.warn("Called the getOrderTotal method on module " + orderTotalModule.getCode()
                        + ". The module isn't available because of the following problem: "
                        + e.getMessage());
            } catch (Exception e)
            {
                log.error("The OrderTotal Module " + orderTotalModule.getCode()
                        + " threw an exception when calling the getOrderTotal() method", e);
            }
        }

        // Sort the return list
        Collections.sort(retList, new OrderTotalSortOrderComparator());

        // Convert the return list into a return array, and then return it
        OrderTotal[] retArray = new OrderTotal[retList.size()];
        int i = 0;
        for (Iterator<OrderTotal> iter = retList.iterator(); iter.hasNext();)
        {
            OrderTotal ot = iter.next();
            retArray[i++] = ot;
        }

        order.setOrderTotals(retArray);

        // Set the shipping method variable which is calculated from the shipping order total
        ((Order) order).calculateShippingMethod();

        return (Order) order;
    }

    /**
     * Get a list of all installed order total modules
     * 
     * @return Returns an array of OrderTotalInterface objects
     * @throws KKException
     */
    public OrderTotalInterface[] getAllOrderTotals() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());

        // Instantiate a list to contain the OrderTotal module objects
        List<OrderTotalInterface> otModuleList = new ArrayList<OrderTotalInterface>();

        List<String> otModuleListStr = sd.getOrderTotalModuleListCopy();
        for (Iterator<String> iter = otModuleListStr.iterator(); iter.hasNext();)
        {
            String moduleName = iter.next();
            // Instantiate the module
            if (moduleName != null)
            {
                try
                {
                    OrderTotalInterface orderTotalModule = getOrderTotalModuleForName(moduleName);
                    if (orderTotalModule.isAvailable())
                    {
                        otModuleList.add(orderTotalModule);
                    }
                } catch (KKException e)
                {
                    log.info("Attempted to instantiate the module " + moduleName
                            + ". The module isn't available because of the following problem: "
                            + e.getMessage());
                } catch (Exception e)
                {
                    log.error("Could not instantiate the OrderTotal Module " + moduleName, e);
                }
            }
        }

        return otModuleList.toArray(new OrderTotalInterface[0]);
    }

    /**
     * Used to sort the modules before they are called
     */
    protected class OrderTotalModuleSortOrderComparator implements Comparator<Object>
    {
        /**
         * @param o1
         * @param o2
         * @return Return Do objects compare
         * 
         */
        public int compare(Object o1, Object o2)
        {
            OrderTotalInterface ot1 = (OrderTotalInterface) o1;
            OrderTotalInterface ot2 = (OrderTotalInterface) o2;
            if (ot1.getSortOrder() > ot2.getSortOrder())
            {
                return 1;
            } else if (ot1.getSortOrder() < ot2.getSortOrder())
            {
                return -1;
            } else
            {
                return 0;
            }
        }
    }

    /**
     * Used to sort the modules before they are displayed
     */
    protected class OrderTotalSortOrderComparator implements Comparator<Object>
    {
        /**
         * @param o1
         * @param o2
         * @return Return Do objects compare
         */
        public int compare(Object o1, Object o2)
        {
            OrderTotal ot1 = (OrderTotal) o1;
            OrderTotal ot2 = (OrderTotal) o2;
            if (ot1.getSortOrder() > ot2.getSortOrder())
            {
                return 1;
            } else if (ot1.getSortOrder() < ot2.getSortOrder())
            {
                return -1;
            } else
            {
                return 0;
            }
        }
    }

    /**
     * Called to instantiate a orderTotal module. It determines whether the module has a constructor
     * where KKEng is passed in and if it does, then this constructor is used. Otherwise the empty
     * constructor is used.
     * 
     * @param moduleName
     * @return Returns an instantiated OrderTotal Module
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    OrderTotalInterface getOrderTotalModuleForName(String moduleName)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException
    {
        Class<?> orderTotalModuleClass = Class.forName(moduleName);
        OrderTotalInterface orderTotalModule = null;
        Constructor<?>[] constructors = orderTotalModuleClass.getConstructors();
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
            orderTotalModule = (OrderTotalInterface) engConstructor.newInstance(getEng());
            if (log.isDebugEnabled())
            {
                log.debug("Called KKEngIf constructor for " + moduleName);
            }
        } else
        {
            orderTotalModule = (OrderTotalInterface) orderTotalModuleClass.newInstance();
            if (log.isDebugEnabled())
            {
                log.debug("Called empty constructor for " + moduleName);
            }
        }
        return orderTotalModule;
    }

    /**
     * Used to store the static data of this manager
     */
    protected class StaticData
    {
        boolean dispPriceWithTax = false;

        List<String> orderTotalModuleList;

        /**
         * @return Returns the dispPriceWithTax.
         */
        public boolean isDispPriceWithTax()
        {
            return dispPriceWithTax;
        }

        /**
         * @param dispPriceWithTax
         *            The dispPriceWithTax to set.
         */
        public void setDispPriceWithTax(boolean dispPriceWithTax)
        {
            this.dispPriceWithTax = dispPriceWithTax;
        }

        /**
         * @return Returns s copy of the orderTotalModuleList.
         */
        public List<String> getOrderTotalModuleListCopy()
        {
            synchronized (mutex1)
            {
                if (orderTotalModuleList == null)
                {
                    new ArrayList<String>();
                }
                ArrayList<String> otModList = new ArrayList<String>(orderTotalModuleList);
                return otModList;
            }
        }

        /**
         * @param orderTotalModuleList
         *            The orderTotalModuleList to set.
         */
        public void setOrderTotalModuleList(List<String> orderTotalModuleList)
        {
            synchronized (mutex1)
            {
                this.orderTotalModuleList = orderTotalModuleList;
            }
        }
    }
}
