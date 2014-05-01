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

package com.konakart.bl.modules.payment.chronopay;

import java.math.BigDecimal;
import java.security.MessageDigest;
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
import com.konakart.app.Zone;
import com.konakart.app.ZoneSearch;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.appif.ZoneSearchIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * Chronopay IPN module
 */
public class Chronopay extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "chronopay";

    private static String bundleName = BaseModule.basePackage + ".payment.chronopay.Chronopay";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "chronopayMutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_STATUS = "MODULE_PAYMENT_CHRONOPAY_STATUS";

    /**
     * The ChronoPayZone zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_ZONE = "MODULE_PAYMENT_CHRONOPAY_ZONE";

    private final static String MODULE_PAYMENT_CHRONOPAY_ORDER_STATUS_ID = "MODULE_PAYMENT_CHRONOPAY_ORDER_STATUS_ID";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_SORT_ORDER = "MODULE_PAYMENT_CHRONOPAY_SORT_ORDER";

    /**
     * The product Id that identifies the store (format:NNNNNN-NNNN-NNNN)
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_PRODUCT_ID = "MODULE_PAYMENT_CHRONOPAY_PRODUCT_ID";

    /**
     * The ChronoPay Url used to POST the payment request.
     * "https://secure.chronopay.com/index_shop.cgi"
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_REQUEST_URL = "MODULE_PAYMENT_CHRONOPAY_REQUEST_URL";

    /**
     * This URL is used by the ChronoPay IPN functionality to call back into the application with
     * the results of the payment transaction. It must be a URL that is visible from the internet.
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_CALLBACK_URL = "MODULE_PAYMENT_CHRONOPAY_CALLBACK_URL";

    /**
     * Callback username and password
     */
    private static final String MODULE_PAYMENT_CHRONOPAY_CALLBACK_USERNAME = "MODULE_PAYMENT_CHRONOPAY_CALLBACK_USERNAME";

    private static final String MODULE_PAYMENT_CHRONOPAY_CALLBACK_PASSWORD = "MODULE_PAYMENT_CHRONOPAY_CALLBACK_PASSWORD";

    /**
     * Shared secret key used for hashing
     */
    private static final String MODULE_PAYMENT_CHRONOPAY_SECRET_KEY = "MODULE_PAYMENT_CHRONOPAY_SECRET_KEY";

    /**
     * This URL is used by ChronoPay to redirect the user's browser when returning from the payment
     * gateway after declining the payment. If it is in the form
     * http://host:port/konakart/CatalogCheckoutExternalPaymentErrorPage.do, then the string
     * host:port is substituted automatically with the correct value. URL for redirecting in case of
     * successful payment is set as product parameter (access url) on the ChronoPay web site..
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_DECLINE_URL = "MODULE_PAYMENT_CHRONOPAY_DECLINE_URL";

    /**
     * This URL is used by ChronoPay to redirect the user's browser when returning from the payment
     * gateway after approving the payment. If it is in the form
     * http://host:port/konakart/CheckoutFinished.do, then the string host:port is substituted
     * automatically with the correct value. URL for redirecting in case of successful payment is
     * set as product parameter (access url) on the ChronoPay web site..
     */
    private final static String MODULE_PAYMENT_CHRONOPAY_SUCCESS_URL = "MODULE_PAYMENT_CHRONOPAY_SUCCESS_URL";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_CHRONOPAY_TEXT_TITLE = "module.payment.chronopay.text.title";

    private final static String MODULE_PAYMENT_CHRONOPAY_TEXT_DESCRIPTION = "module.payment.chronopay.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Chronopay(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_CALLBACK_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_CALLBACK must be set to the Callback Url for the"
                            + " IPN functionality (i.e. https://myhost/konacart/ChronoPayCallback.do).");
        }
        staticData.setChronoPayCallbackUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_CALLBACK_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_CALLBACK_USERNAME must be set to the Callback Username for the"
                            + " IPN functionality.");
        }
        staticData.setChronoPayCallbackUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_CALLBACK_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_CALLBACK must be set to the Callback Password for the"
                            + " IPN functionality.");
        }
        staticData.setChronoPayCallbackPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_SECRET_KEY);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_SECRET_KEY must be set to the shared secret key");
        }
        staticData.setChronoPaySecretKey(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_DECLINE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_DECLINE_URL must be set to the return URL for"
                            + " when the request is declined. (i.e. http://{host:port}/konakart/CatalogCheckoutExternalPaymentErrorPage.do)");
        }
        staticData.setChronoPayDeclineUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_SUCCESS_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_SUCCESS_URL must be set to the return URL for"
                            + " when the request is approved. (i.e. http://{host:port}/konakart/CheckoutFinished.do)");
        }
        staticData.setChronoPaySuccessUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_PRODUCT_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_PRODUCT_ID must be set to the product Id"
                            + " released by ChronoPay in the format NNNNNN-NNNN-NNNN");
        }
        staticData.setChronoPayProductId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CHRONOPAY_REQUEST_URL must be set to the return URL for"
                            + " sending the request to ChronoPay. (i.e. https://secure.chronopay.com/index_shop.cgi)");
        }
        staticData.setChronoPayRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_CHRONOPAY_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Return a payment details object for ChronoPay IPN module
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
         * The ChronoPayZone zone, if greater than zero, should reference a GeoZone. If the
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
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.BROWSER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_CHRONOPAY_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_CHRONOPAY_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getChronoPayRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();
        parmList.add(new NameValue("product_id", sd.getChronoPayProductId()));
        parmList.add(new NameValue("order_id", order.getId()));
        parmList.add(new NameValue("product_name", "Order #" + order.getId() + " from "
                + info.getStoreName()));

        // Chronopay only requires details of the final price. No tax, subtotal etc.
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
        String sessionId = getEng().login(sd.getChronoPayCallbackUsername(),
                sd.getChronoPayCallbackPassword());
        if (sessionId == null)
        {
            throw new KKException(
                    "Unable to log into the engine using the Chronopay Callback Username and Password");
        }
        ssoToken.setSessionId(sessionId);
        ssoToken.setCustom1(String.valueOf(order.getId()));
        ssoToken.setCustom2(sd.getChronoPaySecretKey());

        /*
         * Save the SSOToken with a valid sessionId and the order id in custom1
         */
        String uuid = getEng().saveSSOToken(ssoToken);

        parmList.add(new NameValue("product_price", total.toString()));

        // Create hash
        String sign = md5(sd.getChronoPayProductId() + "-" + total.toString() + "-"
                + sd.getChronoPaySecretKey());
        System.out.println("string being signed = " + sd.getChronoPayProductId() + "-"
                + total.toString() + "-" + sd.getChronoPaySecretKey());
        parmList.add(new NameValue("sign", sign));

        // Normal Callback
        sd.setChronoPayCallbackUrl(sd.getChronoPayCallbackUrl().replaceFirst(hostPortSubstitute,
                info.getHostAndPort()));
        parmList.add(new NameValue("cb_url", sd.getChronoPayCallbackUrl()));
        parmList.add(new NameValue("cb_type", "P"));

        // Call back if payment is declined
        sd.setChronoPayDeclineUrl(sd.getChronoPayDeclineUrl().replaceFirst(hostPortSubstitute,
                info.getHostAndPort()));
        parmList.add(new NameValue("decline_url", sd.getChronoPayDeclineUrl()));

        // Call back if payment is approved
        sd.setChronoPaySuccessUrl(sd.getChronoPaySuccessUrl().replaceFirst(hostPortSubstitute,
                info.getHostAndPort()));
        parmList.add(new NameValue("success_url", sd.getChronoPaySuccessUrl()));

        // Set one of the custom variables with the secret key
        parmList.add(new NameValue("cs1", uuid));
        parmList.add(new NameValue("cs2", order.getId()));

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue("f_name", bNames[0]));
            parmList.add(new NameValue("s_name", bNames[1]));
        }

        // Set the billing address
        parmList.add(new NameValue("street", order.getBillingStreetAddress()));
        parmList.add(new NameValue("city", order.getBillingCity()));
        parmList.add(new NameValue("zip", order.getBillingPostcode()));
        parmList.add(new NameValue("phone", order.getCustomerTelephone()));
        parmList.add(new NameValue("email", order.getCustomerEmail()));

        // Country requires the three letter country code
        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("country", country.getIsoCode3()));
            if (country.getIsoCode3() != null
                    && (country.getIsoCode3().equalsIgnoreCase("USA") || country.getIsoCode3()
                            .equalsIgnoreCase("CAN")))
            {
                ZoneSearchIf search = new ZoneSearch();
                search.setName(order.getBillingState());
                search.setCountryId(country.getId());
                Zone[] zones = getTaxMgr().searchForZones(search);
                if (zones != null && zones.length == 1)
                {
                    parmList.add(new NameValue("state", zones[0].getZoneCode()));
                }
            }
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
        return isAvailable(MODULE_PAYMENT_CHRONOPAY_STATUS);
    }

    /**
     * Calculates a hex MD5 based on input.
     * 
     * @param message
     *            String to calculate MD5 of.
     */
    private String md5(String message) throws java.security.NoSuchAlgorithmException
    {
        MessageDigest md5 = null;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        } catch (java.security.NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
            if (log.isDebugEnabled())
            {
                log.debug(ex);
            }
            throw ex;
        }
        byte[] dig = md5.digest(message.getBytes());
        StringBuffer cod = new StringBuffer();
        for (int i = 0; i < dig.length; ++i)
        {
            cod.append(Integer.toHexString(0x0100 + (dig[i] & 0x00FF)).substring(1));
        }
        return cod.toString();
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // Callback called by Gateway after a transaction
        private String chronoPayCallbackUrl;

        private String chronoPayCallbackUsername;

        private String chronoPayCallbackPassword;

        // Secret key for creating MD5 Hash
        private String chronoPaySecretKey;

        // Redirect URL used by gateway when payment has been declined.
        private String chronoPayDeclineUrl;

        // Redirect URL used by gateway when payment has been approved.
        private String chronoPaySuccessUrl;

        // The product Id that identifies the store (format:NNNNNN-NNNN-NNNN)
        private String chronoPayProductId;

        // The ChronoPay Url used to POST the payment request.
        // "https://secure.chronopay.com/index_shop.cgi"
        private String chronoPayRequestUrl;

        private int zone;

        private int orderStatusId;

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
         * @return the chronoPayCallbackUrl
         */
        public String getChronoPayCallbackUrl()
        {
            return chronoPayCallbackUrl;
        }

        /**
         * @param chronoPayCallbackUrl
         *            the chronoPayCallbackUrl to set
         */
        public void setChronoPayCallbackUrl(String chronoPayCallbackUrl)
        {
            this.chronoPayCallbackUrl = chronoPayCallbackUrl;
        }

        /**
         * @return the chronoPayDeclineUrl
         */
        public String getChronoPayDeclineUrl()
        {
            return chronoPayDeclineUrl;
        }

        /**
         * @param chronoPayDeclineUrl
         *            the chronoPayDeclineUrl to set
         */
        public void setChronoPayDeclineUrl(String chronoPayDeclineUrl)
        {
            this.chronoPayDeclineUrl = chronoPayDeclineUrl;
        }

        /**
         * @return the chronoPayProductId
         */
        public String getChronoPayProductId()
        {
            return chronoPayProductId;
        }

        /**
         * @param chronoPayProductId
         *            the chronoPayProductId to set
         */
        public void setChronoPayProductId(String chronoPayProductId)
        {
            this.chronoPayProductId = chronoPayProductId;
        }

        /**
         * @return the chronoPayRequestUrl
         */
        public String getChronoPayRequestUrl()
        {
            return chronoPayRequestUrl;
        }

        /**
         * @param chronoPayRequestUrl
         *            the chronoPayRequestUrl to set
         */
        public void setChronoPayRequestUrl(String chronoPayRequestUrl)
        {
            this.chronoPayRequestUrl = chronoPayRequestUrl;
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
         * @return the orderStatusId
         */
        public int getOrderStatusId()
        {
            return orderStatusId;
        }

        /**
         * @param orderStatusId
         *            the orderStatusId to set
         */
        public void setOrderStatusId(int orderStatusId)
        {
            this.orderStatusId = orderStatusId;
        }

        /**
         * @return the chronoPayCallbackUsername
         */
        public String getChronoPayCallbackUsername()
        {
            return chronoPayCallbackUsername;
        }

        /**
         * @param chronoPayCallbackUsername
         *            the chronoPayCallbackUsername to set
         */
        public void setChronoPayCallbackUsername(String chronoPayCallbackUsername)
        {
            this.chronoPayCallbackUsername = chronoPayCallbackUsername;
        }

        /**
         * @return the chronoPayCallbackPassword
         */
        public String getChronoPayCallbackPassword()
        {
            return chronoPayCallbackPassword;
        }

        /**
         * @param chronoPayCallbackPassword
         *            the chronoPayCallbackPassword to set
         */
        public void setChronoPayCallbackPassword(String chronoPayCallbackPassword)
        {
            this.chronoPayCallbackPassword = chronoPayCallbackPassword;
        }

        /**
         * @return the chronoPaySuccessUrl
         */
        public String getChronoPaySuccessUrl()
        {
            return chronoPaySuccessUrl;
        }

        /**
         * @param chronoPaySuccessUrl
         *            the chronoPaySuccessUrl to set
         */
        public void setChronoPaySuccessUrl(String chronoPaySuccessUrl)
        {
            this.chronoPaySuccessUrl = chronoPaySuccessUrl;
        }

        /**
         * @return the chronoPaySecretKey
         */
        public String getChronoPaySecretKey()
        {
            return chronoPaySecretKey;
        }

        /**
         * @param chronoPaySecretKey
         *            the chronoPaySecretKey to set
         */
        public void setChronoPaySecretKey(String chronoPaySecretKey)
        {
            this.chronoPaySecretKey = chronoPaySecretKey;
        }

    }

}
