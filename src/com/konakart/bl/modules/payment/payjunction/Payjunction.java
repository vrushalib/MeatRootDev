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

package com.konakart.bl.modules.payment.payjunction;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * PayJunction module. This payment module allows for credit card credentials to be collected
 * directly from a KonaKart page. All communication to the PayJunction server is done from the
 * KonaKart server. It uses the Advanced Integration Method (AIM).
 * 
 * Note that the following variables need to be defined via the Merchant Interface:
 * <ul>
 * <li>Merchant eMail address</li>
 * <li>Whether a confirmation eMail should be sent to the customer</li>
 * <li>The interface version (3.0, 3.1 etc.)</li>
 * </ul>
 */
public class Payjunction extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "payjunction";

    private static String bundleName = BaseModule.basePackage + ".payment.payjunction.Payjunction";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "payjunctionMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_STATUS = "MODULE_PAYMENT_PAYJUNCTION_STATUS";

    /**
     * The PayJunction zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_ZONE = "MODULE_PAYMENT_PAYJUNCTION_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_SORT_ORDER = "MODULE_PAYMENT_PAYJUNCTION_SORT_ORDER";

    /**
     * The PayJunction Url used to POST the payment request.
     * "https://secure.authorize.net/gateway/transact.dll"
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_URL = "MODULE_PAYMENT_PAYJUNCTION_URL";

    /**
     * The PayJunction API Login ID for this installation
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_USERNAME = "MODULE_PAYMENT_PAYJUNCTION_USERNAME";

    /**
     * The PayJunction transaction key for this installation
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_PASSWORD = "MODULE_PAYMENT_PAYJUNCTION_PASSWORD";

    /**
     * PayJunction security code options
     */
    private final static String MODULE_PAYMENT_PAYJUNCTION_SECURITY = "MODULE_PAYMENT_PAYJUNCTION_SECURITY";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_PAYJUNCTION_TEXT_TITLE = "module.payment.payjunction.text.title";

    private final static String MODULE_PAYMENT_PAYJUNCTION_TEXT_DESCRIPTION = "module.payment.payjunction.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Payjunction(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_PAYJUNCTION_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYJUNCTION_URL must be set to the URL for"
                            + " sending the request to PayJunction. (e.g. https://payjunction.com/quick_link)");
        }
        staticData.setPayJunctionRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYJUNCTION_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYJUNCTION_USERNAME must be set to the"
                            + " PayJunction username for this installation");
        }
        staticData.setPayJunctionUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYJUNCTION_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYJUNCTION_PASSWORD must be set to the"
                            + " PayJunction password for this installation");
        }
        staticData.setPayJunctionPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYJUNCTION_SECURITY);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYJUNCTION_SECURITY must be set to the"
                            + " PayJunction security code for this installation");
        }
        staticData.setPayJunctionSecurityCode(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYJUNCTION_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYJUNCTION_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }
   }

    /**
     * Return a payment details object for PayJunction IPN module
     * 
     * @param order
     * @param info
     * @return Returns information in a PaymentDetails object
     * @throws Exception
     */
    public PaymentDetails getPaymentDetails(Order order, PaymentInfo info) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());
        /*
         * The PayJunctionZone zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
         */
        if (sd.getZone() > 0)
        {
            checkZone(info, sd.getZone());
        }

        // Get the scale for currency calculations
        int scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, info
                .getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        PaymentDetails pDetails = new PaymentDetails();
        pDetails.setCode(code);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setPaymentType(PaymentDetails.SERVER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_PAYJUNCTION_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_PAYJUNCTION_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getPayJunctionRequestUrl());

        // PayJunction requires details of the final price - inclusive of tax.
        BigDecimal total = null;
        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
        }

        if (total == null)
        {
            throw new KKException("An Order Total was not found");
        }

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("dc_logon", encode(sd.getPayJunctionUsername())));
        parmList.add(new NameValue("dc_password", encode(sd.getPayJunctionPassword())));
        parmList.add(new NameValue("dc_transaction_type", encode("AUTHORIZATION_CAPTURE")));
        parmList.add(new NameValue("dc_version", encode("1.2")));
        parmList.add(new NameValue("dc_transaction_amount", encode(total.toString())));

        if (sd.getPayJunctionSecurityCode() != null && sd.getPayJunctionSecurityCode().length() > 1)
        {
            parmList.add(new NameValue("dc_security", encode(sd.getPayJunctionSecurityCode())));
        }

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(true);
        pDetails.setShowCVV(true);
        pDetails.setShowPostcode(true);
        pDetails.setShowType(false); // PayJunction doesn't require the card type
        pDetails.setShowOwner(true);

        if (log.isDebugEnabled())
        {
            log.debug(pDetails.toString());
        }

        return pDetails;
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_PAYMENT_PAYJUNCTION_STATUS);
    }

    /**
     * URL-Encodes a value
     * 
     * @param value
     *            Value to be URL-encoded
     * @return URL-encoded value
     */
    private String encode(String value)
    {
        try
        {
            // return URL encoded string
            if (value != null && value.length() > 1)
            {
                return URLEncoder.encode(value, "UTF-8");
            }
        } catch (Exception e)
        {
            log.warn("Error URL-encoding '" + value + "' : " + e);
        }
        return "";
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // The PayJunction URL used to POST the payment request.
        private String payJunctionRequestUrl;

        // Username
        private String payJunctionUsername;

        // Password
        private String payJunctionPassword;

        // Security Code Options
        private String payJunctionSecurityCode;

        // zone where PayJunction will be made available
        private int zone;

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
         * @return the payJunctionRequestUrl
         */
        public String getPayJunctionRequestUrl()
        {
            return payJunctionRequestUrl;
        }

        /**
         * @param payJunctionRequestUrl
         *            the payJunctionRequestUrl to set
         */
        public void setPayJunctionRequestUrl(String payJunctionRequestUrl)
        {
            this.payJunctionRequestUrl = payJunctionRequestUrl;
        }

        /**
         * @return the payJunctionUsername
         */
        public String getPayJunctionUsername()
        {
            return payJunctionUsername;
        }

        /**
         * @param payJunctionUsername
         *            the payJunctionUsername to set
         */
        public void setPayJunctionUsername(String payJunctionUsername)
        {
            this.payJunctionUsername = payJunctionUsername;
        }

        /**
         * @return the payJunctionPassword
         */
        public String getPayJunctionPassword()
        {
            return payJunctionPassword;
        }

        /**
         * @param payJunctionPassword
         *            the payJunctionPassword to set
         */
        public void setPayJunctionPassword(String payJunctionPassword)
        {
            this.payJunctionPassword = payJunctionPassword;
        }

        /**
         * @return the payJunctionSecurityCode
         */
        public String getPayJunctionSecurityCode()
        {
            return payJunctionSecurityCode;
        }

        /**
         * @param payJunctionSecurityCode
         *            the payJunctionSecurityCode to set
         */
        public void setPayJunctionSecurityCode(String payJunctionSecurityCode)
        {
            this.payJunctionSecurityCode = payJunctionSecurityCode;
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

    }
}