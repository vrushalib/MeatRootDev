//
// (c) 2013 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.bl.modules.ordertotal.paymentcharge;

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
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object to add an extra charge to the order depending on the
 * payment method chosen.
 */
public class PaymentCharge extends BaseOrderTotalModule implements OrderTotalInterface
{

    private static String code = "ot_payment_charge";

    private static String bundleName = BaseModule.basePackage
            + ".ordertotal.paymentcharge.PaymentCharge";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otPaymentChargeMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys
    private final static String MODULE_ORDER_TOTAL_PAYMENT_CHARGE_STATUS = "MODULE_ORDER_TOTAL_PAYMENT_CHARGE_STATUS";

    private final static String MODULE_ORDER_TOTAL_PAYMENT_CHARGE_SORT_ORDER = "MODULE_ORDER_TOTAL_PAYMENT_CHARGE_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_PAYMENT_CHARGE_CHARGES = "MODULE_ORDER_TOTAL_PAYMENT_CHARGE_CHARGES";

    // Message Catalogue Keys
    private final static String MODULE_ORDER_TOTAL_PAYMENT_CHARGE_TITLE = "module.order.total.paymentcharge.title";

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
    public PaymentCharge(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        conf = getConfiguration(MODULE_ORDER_TOTAL_PAYMENT_CHARGE_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_ORDER_TOTAL_PAYMENT_CHARGE_CHARGES);
        if (conf != null)
        {
            staticData.setCharges(conf.getValue());
        } else
        {
            if (log.isWarnEnabled())
            {
                log.warn("Could not find the MODULE_ORDER_TOTAL_PAYMENT_CHARGE_CHARGES configuration variable");
            }
        }
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_ORDER_TOTAL_PAYMENT_CHARGE_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the payment charge
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

        if (order.getPaymentModuleCode() == null || order.getPaymentModuleCode().length() == 0)
        {
            return null;
        }

        String moduleCode = order.getPaymentModuleCode();
        if (order.getPaymentModuleSubCode() != null && order.getPaymentModuleSubCode().length() > 0)
        {
            moduleCode = moduleCode + "-" + order.getPaymentModuleSubCode();
        }

        BigDecimal charge = sd.getCharge(moduleCode);

        // Don't return a value if no charge
        if (charge == null || charge.compareTo(new BigDecimal(0)) == 0)
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

        /*
         * Display charge using a single Order Total
         */
        ot = new OrderTotal();
        ot.setSortOrder(sd.getSortOrder());
        ot.setClassName(code);
        ot.setValue(charge);
        ot.setText(getCurrMgr().formatPrice(charge, order.getCurrencyCode()));
        StringBuffer title = new StringBuffer();
        title.append(rb.getString(MODULE_ORDER_TOTAL_PAYMENT_CHARGE_TITLE));
        title.append(":");
        ot.setTitle(title.toString());

        /*
         * Add the amount to the order. 
         */
        order.setTotalIncTax(order.getTotalIncTax().add(charge));
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

        private HashMap<String, BigDecimal> chargeMap = new HashMap<String, BigDecimal>();

        /**
         * Return the charge based on the payment module code
         * 
         * @param moduleCode
         * @return Returns the charge based on the payment module code
         */
        public BigDecimal getCharge(String moduleCode)
        {
            return chargeMap.get(moduleCode);
        }

        /**
         * Receives a string in the format paymentModuleCode-paymentModuleSubCode:charge. Example
         * paypal:5.00,authorizenet:3.00. It loads the charges into the hash map.
         * 
         * @param charges
         * @throws KKException
         */
        public void setCharges(String charges) throws KKException
        {
            if (log.isDebugEnabled())
            {
                log.debug("Value in Payment Charge configuration variable = " + charges);
            }
            chargeMap.clear();
            if (charges == null || charges.length() == 0)
            {
                return;
            }
            String[] chargeArray = charges.split(",");
            for (int i = 0; i < chargeArray.length; i++)
            {
                String chargePairStr = chargeArray[i];
                String[] chargePair = chargePairStr.split(":");
                if (chargePair.length != 2)
                {
                    throw new KKException(
                            "The value in the Payment Charge configuration variable is not formatted correctly : "
                                    + charges);
                }

                String moduleCode = chargePair[0];
                BigDecimal charge = null;
                try
                {
                    charge = new BigDecimal(chargePair[1]);
                } catch (Exception e)
                {
                    throw new KKException(
                            "Could not parse the charge correctly for the module code = "
                                    + moduleCode
                                    + ". The charge must be a decimal number. We found a value of "
                                    + chargePair[1]);
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Adding to the hash map : " + moduleCode + " - " + charge);
                }

                chargeMap.put(moduleCode, charge);
            }
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
    }
}
