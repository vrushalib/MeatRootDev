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
// Original version written by Steven Lohrenz (steven@stevenlohrenz.com) 
// based on a KonaKart example.  
//

package com.konakart.bl.modules.payment.worldpay;

import java.math.BigDecimal;
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
import com.konakart.app.SSOToken;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * WorldPay IPN module
 */
public class Worldpay extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "worldpay";

    private static String bundleName = BaseModule.basePackage + ".payment.worldpay.Worldpay";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "worldpayMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_WORLDPAY_STATUS = "MODULE_PAYMENT_WORLDPAY_STATUS";

    /**
     * The WorldPay zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress
     * of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_WORLDPAY_ZONE = "MODULE_PAYMENT_WORLDPAY_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_WORLDPAY_SORT_ORDER = "MODULE_PAYMENT_WORLDPAY_SORT_ORDER";

    /**
     * The WorldPay Url used to POST the payment request.
     * "https://secure.worldpay.com/index_shop.cgi"
     */
    private final static String MODULE_PAYMENT_WORLDPAY_REQUEST_URL = "MODULE_PAYMENT_WORLDPAY_REQUEST_URL";

    /**
     * The WorldPay ID for this installation
     */
    private final static String MODULE_PAYMENT_WORLDPAY_INST_ID = "MODULE_PAYMENT_WORLDPAY_INST_ID";

    /**
     * Callback username and password
     */
    private static final String MODULE_PAYMENT_WORLDPAY_CALLBACK_USERNAME = "MODULE_PAYMENT_WORLDPAY_CALLBACK_USERNAME";

    private static final String MODULE_PAYMENT_WORLDPAY_CALLBACK_PASSWORD = "MODULE_PAYMENT_WORLDPAY_CALLBACK_PASSWORD";

    /**
     * 100 is testing that always returns successful transaction. 101 is testing that always returns
     * an unsuccessful transaction. 0 is a live site.
     * 
     */
    private final static String MODULE_PAYMENT_WORLDPAY_TEST_MODE = "MODULE_PAYMENT_WORLDPAY_TEST_MODE";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_WORLDPAY_TEXT_TITLE = "module.payment.worldpay.text.title";

    private final static String MODULE_PAYMENT_WORLDPAY_TEXT_DESCRIPTION = "module.payment.worldpay.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Worldpay(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_WORLDPAY_REQUEST_URL must be set to the URL for"
                            + " sending the request to WorldPay. (i.e. https://select.worldpay.com/wcc/purchase)");
        }
        staticData.setWorldPayRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_INST_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_WORLDPAY_INST_ID must be set to the"
                            + " WorldPay ID for this installation");
        }
        staticData.setWorldPayInstId(conf.getValue());
        
        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_CALLBACK_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_WORLDPAY_CALLBACK_USERNAME must be set to the Callback Username for the"
                            + " callback functionality.");
        }
        staticData.setCallbackUsername(conf.getValue());
        
        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_CALLBACK_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_WORLDPAY_CALLBACK_PASSWORD must be set to the Callback Password for the"
                            + " callback functionality.");
        }
        staticData.setCallbackPassword(conf.getValue());
        
        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_TEST_MODE);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_WORLDPAY_TEST_MODE must be set to on eof the following values: "
                            + "100 is testing that always returns successful transaction. "
                            + "101 is testing that always returns an unsuccessful transaction. "
                            + "0 is a live site.");
        }
        staticData.setWorldPayTestMode(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_WORLDPAY_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Return a payment details object for WorldPay IPN module
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
         * The WorldPayZone zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
         */
        if (sd.getZone() > 0)
        {
            checkZone(info, sd.getZone());
        }

        // Get the scale for currency calculations
        int scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap,
                info.getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        PaymentDetails pDetails = new PaymentDetails();
        pDetails.setCode(code);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setPaymentType(PaymentDetails.BROWSER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_WORLDPAY_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_WORLDPAY_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getWorldPayRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();
        /*
         * Obligatory parameters
         */
        parmList.add(new NameValue("instId", sd.getWorldPayInstId()));
        parmList.add(new NameValue("currency", order.getCurrency().getCode()));
        parmList.add(new NameValue("cartId", order.getId()));
        parmList.add(new NameValue("desc", "Order #" + order.getId() + " from "
                + info.getStoreName()));

        // Worldpay only requires details of the final price. No tax, subtotal etc.
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
        
        /*
         * Create a session here which will be used by the IPN callback
         */
        SSOTokenIf ssoToken = new SSOToken();
        String sessionId = getEng().login(sd.callbackUsername, sd.getCallbackPassword());
        if (sessionId == null)
        {
            throw new KKException(
                    "Unable to log into the engine using the Worldpay Callback Username and Password");
        }
        ssoToken.setSessionId(sessionId);
        ssoToken.setCustom1(String.valueOf(order.getId()));
        /*
         * Save the SSOToken with a valid sessionId and the order id in custom1
         */
        String uuid = getEng().saveSSOToken(ssoToken);

        parmList.add(new NameValue("amount", total.toString()));

        /*
         * Optional parameters
         */
        parmList.add(new NameValue("testMode", sd.getWorldPayTestMode()));

        // Set one of the custom variables with the secret key
        parmList.add(new NameValue("M_cs1", uuid));
        parmList.add(new NameValue("M_cs2", order.getId()));

        // Set the billing address
        parmList.add(new NameValue("name", order.getBillingName()));
        parmList.add(new NameValue("postcode", order.getBillingPostcode()));
        parmList.add(new NameValue("tel", order.getCustomerTelephone()));
        parmList.add(new NameValue("email", order.getCustomerEmail()));

        StringBuffer addrSB = new StringBuffer();
        addrSB.append(order.getBillingStreetAddress());
        addrSB.append("&#10;");
        if (order.getBillingSuburb() != null && order.getBillingSuburb().length() > 0)
        {
            addrSB.append(order.getBillingSuburb());
            addrSB.append("&#10;");
        }
        addrSB.append(order.getBillingCity());
        addrSB.append("&#10;");
        if (order.getBillingState() != null && order.getBillingState().length() > 0)
        {
            addrSB.append(order.getBillingState());
            addrSB.append("&#10;");
        }
        parmList.add(new NameValue("address", addrSB.toString()));

        // Country requires the two letter country code
        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("country", country.getIsoCode2()));
        }

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

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
        return isAvailable(MODULE_PAYMENT_WORLDPAY_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // The WorldPay Url used to POST the payment request.
        // "https://secure.worldpay.com/index_shop.cgi"
        private String worldPayRequestUrl;

        private String worldPayInstId;

        private String worldPayTestMode;

        private String callbackUsername;

        private String callbackPassword;

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
         * @return the worldPayRequestUrl
         */
        public String getWorldPayRequestUrl()
        {
            return worldPayRequestUrl;
        }

        /**
         * @param worldPayRequestUrl
         *            the worldPayRequestUrl to set
         */
        public void setWorldPayRequestUrl(String worldPayRequestUrl)
        {
            this.worldPayRequestUrl = worldPayRequestUrl;
        }

        /**
         * @return the worldPayInstId
         */
        public String getWorldPayInstId()
        {
            return worldPayInstId;
        }

        /**
         * @param worldPayInstId
         *            the worldPayInstId to set
         */
        public void setWorldPayInstId(String worldPayInstId)
        {
            this.worldPayInstId = worldPayInstId;
        }

        /**
         * @return the worldPayTestMode
         */
        public String getWorldPayTestMode()
        {
            return worldPayTestMode;
        }

        /**
         * @param worldPayTestMode
         *            the worldPayTestMode to set
         */
        public void setWorldPayTestMode(String worldPayTestMode)
        {
            this.worldPayTestMode = worldPayTestMode;
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
         * @return the callbackUsername
         */
        public String getCallbackUsername()
        {
            return callbackUsername;
        }

        /**
         * @param callbackUsername the callbackUsername to set
         */
        public void setCallbackUsername(String callbackUsername)
        {
            this.callbackUsername = callbackUsername;
        }

        /**
         * @return the callbackPassword
         */
        public String getCallbackPassword()
        {
            return callbackPassword;
        }

        /**
         * @param callbackPassword the callbackPassword to set
         */
        public void setCallbackPassword(String callbackPassword)
        {
            this.callbackPassword = callbackPassword;
        }
    }

}
