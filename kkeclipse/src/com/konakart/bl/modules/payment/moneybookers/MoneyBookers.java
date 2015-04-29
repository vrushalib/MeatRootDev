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
// Original version contributed by Chris Derham (Atomus Ltd)
//

package com.konakart.bl.modules.payment.moneybookers;

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
 * MoneyBookers IPN module
 */
public class MoneyBookers extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "moneyBookers";

    private static String bundleName = BaseModule.basePackage
            + ".payment.moneybookers.MoneyBookers";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "moneyBookersMutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_MONEYBOOKERS_STATUS = "MODULE_PAYMENT_MONEYBOOKERS_STATUS";

    /**
     * The MoneyBookersZone zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_MONEYBOOKERS_ZONE = "MODULE_PAYMENT_MONEYBOOKERS_ZONE";

    private final static String MODULE_PAYMENT_MONEYBOOKERS_ORDER_STATUS_ID = "MODULE_PAYMENT_MONEYBOOKERS_ORDER_STATUS_ID";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_MONEYBOOKERS_SORT_ORDER = "MODULE_PAYMENT_MONEYBOOKERS_SORT_ORDER";

    /**
     * The MoneyBookers Url used to POST the payment request.
     * "https://secure.moneyBookers.com/index_shop.cgi"
     */
    private final static String MODULE_PAYMENT_MONEYBOOKERS_REQUEST_URL = "MODULE_PAYMENT_MONEYBOOKERS_REQUEST_URL";

    /**
     * This URL is used by the MoneyBookers IPN functionality to call back into the application with
     * the results of the payment transaction. It must be a URL that is visible from the internet.
     */
    private final static String MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_URL = "MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_URL";

    private static final String MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_USERNAME = "MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_USERNAME";

    private static final String MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_PASSWORD = "MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_PASSWORD";

    private static final String MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_SECRET_WORD = "MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_SECRET_WORD";

    /**
     * This URL is used by MoneyBookers to redirect the user's browser when returning from the
     * payment gateway after declining the payment. If it is in the form
     * http://host:port/konakart/CatalogCheckoutExternalPaymentErrorPage.do, then the string
     * host:port is substituted automatically with the correct value. URL for redirecting in case of
     * successful payment is set as product parameter (access url) on the MoneyBookers web site..
     */
    private final static String MODULE_PAYMENT_MONEYBOOKERS_DECLINE_URL = "MODULE_PAYMENT_MONEYBOOKERS_DECLINE_URL";

    private final static String MODULE_PAYMENT_MONEYBOOKERS_ACCEPT_URL = "MODULE_PAYMENT_MONEYBOOKERS_ACCEPT_URL";

    private final static String MODULE_PAYMENT_MONEYBOOKERS_PAY_TO_EMAIL = "MODULE_PAYMENT_MONEYBOOKERS_PAY_TO_EMAIL";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_MONEYBOOKERS_TEXT_TITLE = "module.payment.moneyBookers.text.title";

    private final static String MODULE_PAYMENT_MONEYBOOKERS_TEXT_DESCRIPTION = "module.payment.moneyBookers.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public MoneyBookers(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_CALLBACK must be set to the Callback Url for the"
                            + " IPN functionality (i.e. https://myhost/konacart/MoneyBookersCallback.do).");
        }
        staticData.setMoneyBookersCallbackUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_USERNAME must be set to the Callback " +
                    "Username for the IPN functionality.");
        }
        staticData.setMoneyBookersCallbackUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_PASSWORD must be set to the Callback " +
                    "Password for the IPN functionality.");
        }
        staticData.setMoneyBookersCallbackPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_SECRET_WORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_SECRET_WORD must be set to the Callback " +
                    "Secret Word for the IPN functionality.");
        }
        staticData.setMoneyBookersSecretWord(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_DECLINE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_DECLINE_URL must be set to the return URL for"
                            + " when the request is declined. (i.e. http://{host:port}/konakart/CatalogCheckoutExternalPaymentErrorPage.do)");
        }
        staticData.setMoneyBookersDeclineUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_ACCEPT_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_ACCEPT_URL must be set to the return URL for"
                            + " when the request is accepted. (i.e. http://{host:port}/konakart/CheckoutFinished.do)");
        }
        staticData.setMoneyBookersAcceptUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_REQUEST_URL must be set to the return URL for"
                            + " sending the request to MoneyBookers. (i.e. https://secure.moneyBookers.com/index_shop.cgi)");
        }
        staticData.setMoneyBookersRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_PAY_TO_EMAIL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_MONEYBOOKERS_PAY_TO_EMAIL must be set to the email address to pay");
        }
        staticData.setMoneyBookersPayToEmail(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_MONEYBOOKERS_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Return a payment details object for MoneyBookers IPN module
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
         * The MoneyBookersZone zone, if greater than zero, should reference a GeoZone. If the
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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_MONEYBOOKERS_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_MONEYBOOKERS_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getMoneyBookersRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();
        parmList.add(new NameValue("product_id", sd.getMoneyBookersProductId()));
        parmList.add(new NameValue("product_name", "Order #" + order.getId() + " from "
                + info.getStoreName()));

        // moneyBookers only requires details of the final price. No tax, sub-total etc.
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
        String sessionId = getEng().login(sd.getMoneyBookersCallbackUsername(), sd.getMoneyBookersCallbackPassword());
        if (sessionId == null)
        {
            throw new KKException(
                    "Unable to log into the engine using the MoneyBookers Callback Username and Password");
        }
        ssoToken.setSessionId(sessionId);
        ssoToken.setCustom1(sd.getMoneyBookersSecretWord());
        /*
         * Save the SSOToken with a valid sessionId and the order id in custom1
         */
        String uuid = getEng().saveSSOToken(ssoToken);

        parmList.add(new NameValue("merchant_fields", "uuid, orderId, platform"));
        parmList.add(new NameValue("uuid", uuid));
        parmList.add(new NameValue("platform", "21477228"));
        parmList.add(new NameValue("orderId", order.getId()));

        parmList.add(new NameValue("amount", total.toString()));
        parmList.add(new NameValue("currency", order.getCurrencyCode()));
        parmList.add(new NameValue("detail1_description", "Description"));
        parmList.add(new NameValue("detail1_text", "Goods/Services"));

        // general parameters
        parmList.add(new NameValue("pay_to_email", sd.getMoneyBookersPayToEmail()));
        parmList.add(new NameValue("recipient_description", "MoneyBookers"));
        parmList.add(new NameValue("transaction_id", order.getOrderNumber()));
        parmList.add(new NameValue("language", "EN"));
        parmList.add(new NameValue("hide_login", "1"));
        parmList.add(new NameValue("pay_from_email", order.getCustomerEmail()));

        // Normal Callback
        sd.setMoneyBookersCallbackUrl(sd.getMoneyBookersCallbackUrl().replaceFirst(
                hostPortSubstitute, info.getHostAndPort()));
        parmList.add(new NameValue("status_url", sd.getMoneyBookersCallbackUrl()));

        // Call back if payment is declined
        sd.setMoneyBookersDeclineUrl(sd.getMoneyBookersDeclineUrl().replaceFirst(
                hostPortSubstitute, info.getHostAndPort()));
        parmList.add(new NameValue("cancel_url", sd.getMoneyBookersDeclineUrl()));

        // Call back if payment is accepted
        sd.setMoneyBookersAcceptUrl(sd.getMoneyBookersAcceptUrl().replaceFirst(hostPortSubstitute,
                info.getHostAndPort()));
        parmList.add(new NameValue("return_url", sd.getMoneyBookersAcceptUrl()));

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue("firstname", bNames[0]));
            parmList.add(new NameValue("lastname", bNames[1]));
        }

        // Set the billing address
        parmList.add(new NameValue("address", order.getBillingStreetAddress()));
        parmList.add(new NameValue("city", order.getBillingCity()));
        parmList.add(new NameValue("state", order.getBillingState()));
        parmList.add(new NameValue("postal_code", order.getBillingPostcode()));
        parmList.add(new NameValue("phone_number", order.getCustomerTelephone()));
        parmList.add(new NameValue("email", order.getCustomerEmail()));

        // Country requires the three letter country code
        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("country", country.getIsoCode3()));
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
        return isAvailable(MODULE_PAYMENT_MONEYBOOKERS_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // Callback called by Gateway after a transaction
        private String moneyBookersCallbackUrl;

        private String moneyBookersCallbackUsername;

        private String moneyBookersCallbackPassword;

        private String moneyBookersSecretWord;

        // Redirect URL used by gateway when payment has been declined. Url for successful payment
        // is set as a product parameter in the setup section of the MoneyBookers web site
        // (access_url)
        private String moneyBookersDeclineUrl;

        private String moneyBookersAcceptUrl;

        // The product Id that identifies the store (format:NNNNNN-NNNN-NNNN)
        private String moneyBookersProductId;

        private String moneyBookersPayToEmail;

        // The MoneyBookers Url used to POST the payment request.
        // "https://secure.moneyBookers.com/index_shop.cgi"
        private String moneyBookersRequestUrl;

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
         * @return the moneyBookersCallbackUrl
         */
        public String getMoneyBookersCallbackUrl()
        {
            return moneyBookersCallbackUrl;
        }

        /**
         * @param moneyBookersCallbackUrl
         *            the moneyBookersCallbackUrl to set
         */
        public void setMoneyBookersCallbackUrl(String moneyBookersCallbackUrl)
        {
            this.moneyBookersCallbackUrl = moneyBookersCallbackUrl;
        }

        /**
         * @return the moneyBookersDeclineUrl
         */
        public String getMoneyBookersDeclineUrl()
        {
            return moneyBookersDeclineUrl;
        }

        /**
         * @param moneyBookersDeclineUrl
         *            the moneyBookersDeclineUrl to set
         */
        public void setMoneyBookersDeclineUrl(String moneyBookersDeclineUrl)
        {
            this.moneyBookersDeclineUrl = moneyBookersDeclineUrl;
        }

        /**
         * @return the moneyBookersAcceptUrl
         */
        public String getMoneyBookersAcceptUrl()
        {
            return moneyBookersAcceptUrl;
        }

        /**
         * @param moneyBookersAcceptUrl
         *            the moneyBookersAcceptUrl to set
         */
        public void setMoneyBookersAcceptUrl(String moneyBookersAcceptUrl)
        {
            this.moneyBookersAcceptUrl = moneyBookersAcceptUrl;
        }

        /**
         * @return the moneyBookersProductId
         */
        public String getMoneyBookersProductId()
        {
            return moneyBookersProductId;
        }

        /**
         * @param moneyBookersProductId
         *            the moneyBookersProductId to set
         */
        public void setMoneyBookersProductId(String moneyBookersProductId)
        {
            this.moneyBookersProductId = moneyBookersProductId;
        }

        /**
         * @return the moneyBookersPayToEmail
         */
        public String getMoneyBookersPayToEmail()
        {
            return moneyBookersPayToEmail;
        }

        /**
         * @param moneyBookersPayToEmail
         *            the moneyBookersPayToEmail to set
         */
        public void setMoneyBookersPayToEmail(String moneyBookersPayToEmail)
        {
            this.moneyBookersPayToEmail = moneyBookersPayToEmail;
        }

        /**
         * @return the moneyBookersRequestUrl
         */
        public String getMoneyBookersRequestUrl()
        {
            return moneyBookersRequestUrl;
        }

        /**
         * @param moneyBookersRequestUrl
         *            the moneyBookersRequestUrl to set
         */
        public void setMoneyBookersRequestUrl(String moneyBookersRequestUrl)
        {
            this.moneyBookersRequestUrl = moneyBookersRequestUrl;
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
         * @return the moneyBookersCallbackUsername
         */
        public String getMoneyBookersCallbackUsername()
        {
            return moneyBookersCallbackUsername;
        }

        /**
         * @param moneyBookersCallbackUsername
         *            the moneyBookersCallbackUsername to set
         */
        public void setMoneyBookersCallbackUsername(String moneyBookersCallbackUsername)
        {
            this.moneyBookersCallbackUsername = moneyBookersCallbackUsername;
        }

        /**
         * @return the moneyBookersCallbackPassword
         */
        public String getMoneyBookersCallbackPassword()
        {
            return moneyBookersCallbackPassword;
        }

        /**
         * @param moneyBookersCallbackPassword
         *            the moneyBookersCallbackPassword to set
         */
        public void setMoneyBookersCallbackPassword(String moneyBookersCallbackPassword)
        {
            this.moneyBookersCallbackPassword = moneyBookersCallbackPassword;
        }

        /**
         * @return the moneyBookersSecretWord
         */
        public String getMoneyBookersSecretWord()
        {
            return moneyBookersSecretWord;
        }

        /**
         * @param moneyBookersSecretWord
         *            the moneyBookersSecretWord to set
         */
        public void setMoneyBookersSecretWord(String moneyBookersSecretWord)
        {
            this.moneyBookersSecretWord = moneyBookersSecretWord;
        }

    }
}
