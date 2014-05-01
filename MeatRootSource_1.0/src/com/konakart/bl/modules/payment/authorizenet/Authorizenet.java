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

package com.konakart.bl.modules.payment.authorizenet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * Authorize.Net module. This payment module allows for credit card credentials to be collected
 * directly from a KonaKart page. All communication to the Authorize.Net server is done from the
 * KonaKart server. It uses the Advanced Integration Method (AIM).
 * 
 * Note that the following variables need to be defined via the Merchant Interface:
 * <ul>
 * <li>Merchant eMail address</li>
 * <li>Whether a confirmation eMail should be sent to the customer</li>
 * <li>The interface version (3.0, 3.1 etc.)</li>
 * </ul>
 */
public class Authorizenet extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "authorizenet";

    private static String bundleName = BaseModule.basePackage
            + ".payment.authorizenet.Authorizenet";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "authorizenetMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_STATUS = "MODULE_PAYMENT_AUTHORIZENET_STATUS";

    /**
     * The Authorize.Net zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_ZONE = "MODULE_PAYMENT_AUTHORIZENET_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER = "MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER";

    /**
     * The Authorize.Net Url used to POST the payment request.
     * "https://secure.authorize.net/gateway/transact.dll"
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL = "MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL";

    /**
     * The Authorize.Net API Login ID for this installation
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_LOGIN = "MODULE_PAYMENT_AUTHORIZENET_LOGIN";

    /**
     * The Authorize.Net transaction key for this installation
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_TXNKEY = "MODULE_PAYMENT_AUTHORIZENET_TXNKEY";

    /**
     * Used to make test transactions
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_TESTMODE = "MODULE_PAYMENT_AUTHORIZENET_TESTMODE";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV = "MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_AUTHORIZENET_TEXT_TITLE = "module.payment.authorizenet.text.title";

    private final static String MODULE_PAYMENT_AUTHORIZENET_TEXT_DESCRIPTION = "module.payment.authorizenet.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Authorizenet(KKEngIf eng) throws KKException
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
        StaticData staticData = staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            staticData = new StaticData();
            staticDataHM.put(getStoreId(), staticData);
        }

        String confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL);
        if (confVal == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL must be set to the URL for"
                            + " sending the request to Authorize.Net. (e.g. https://secure.authorize.net/gateway/transact.dll)");
        }
        staticData.setAuthorizeNetRequestUrl(confVal);

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_LOGIN);
        if (confVal == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_LOGIN must be set to the"
                            + " Authorize.Net API ID for this installation");
        }
        staticData.setAuthorizeNetLoginId(confVal);

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_TXNKEY);
        if (confVal == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_TXNKEY must be set to the"
                            + " Current Authorize.Net Transaction Key for this installation");
        }

        staticData.setAuthorizeNetTxnKey(confVal);
        staticData.setTestMode(getConfigurationValueAsBool(
                MODULE_PAYMENT_AUTHORIZENET_TESTMODE, true));
        staticData.setZone(getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_AUTHORIZENET_ZONE, 0));
        staticData.setSortOrder(getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER, 0));
        staticData.setShowCVV(getConfigurationValueAsBool(
                MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV, true));
    }

    /**
     * Return a payment details object for Authorize.Net IPN module
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
         * The AuthorizeNetZone zone, if greater than zero, should reference a GeoZone. If the
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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_AUTHORIZENET_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_AUTHORIZENET_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getAuthorizeNetRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("x_delim_data", "True"));
        parmList.add(new NameValue("x_relay_response", "False"));
        parmList.add(new NameValue("x_login", sd.getAuthorizeNetLoginId()));
        parmList.add(new NameValue("x_tran_key", sd.getAuthorizeNetTxnKey()));
        parmList.add(new NameValue("x_delim_char", ","));
        parmList.add(new NameValue("x_encap_char", ""));
        parmList.add(new NameValue("x_method", "CC"));
        parmList.add(new NameValue("x_type", "AUTH_CAPTURE"));

        // AuthorizeNet requires details of the final price - inclusive of tax.
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

        parmList.add(new NameValue("x_amount", total.toString()));
        parmList.add(new NameValue("x_currency_code", order.getCurrency().getCode()));
        parmList.add(new NameValue("x_invoice_num", order.getId())); // TODO
        parmList.add(new NameValue("x_test_request", (sd.isTestMode() ? "TRUE" : "FALSE")));

        // Set the billing address

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue("x_first_name", bNames[0]));
            parmList.add(new NameValue("x_last_name", bNames[1]));
        }

        parmList.add(new NameValue("x_company", order.getBillingCompany()));
        parmList.add(new NameValue("x_city", order.getBillingCity()));
        parmList.add(new NameValue("x_state", order.getBillingState()));
        parmList.add(new NameValue("x_zip", order.getBillingPostcode()));
        parmList.add(new NameValue("x_phone", order.getCustomerTelephone()));
        parmList.add(new NameValue("x_cust_id", order.getCustomerId()));
        parmList.add(new NameValue("x_email", order.getCustomerEmail()));

        StringBuffer addrSB = new StringBuffer();
        addrSB.append(order.getBillingStreetAddress());
        if (order.getBillingSuburb() != null && order.getBillingSuburb().length() > 0)
        {
            addrSB.append(", ");
            addrSB.append(order.getBillingSuburb());
        }
        if (addrSB.length() > 60)
        {
            parmList.add(new NameValue("x_address", addrSB.substring(0, 56) + "..."));
        } else
        {
            parmList.add(new NameValue("x_address", addrSB.toString()));
        }

        // Country requires the two letter country code
        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("x_country", country.getIsoCode2()));
        }

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(false);
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(false);
        pDetails.setShowType(false); // Authorize.net doesn't require the card type
        pDetails.setShowOwner(false);

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
        return isAvailable(MODULE_PAYMENT_AUTHORIZENET_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // The Authorize.Net Url used to POST the payment request.
        // "https://secure.authorize.net/gateway/transact.dll"
        private String authorizeNetRequestUrl;

        // Login ID
        private String authorizeNetLoginId;

        // Transaction Key
        private String authorizeNetTxnKey;

        // Test/Live Mode indicator
        private boolean testMode = true;

        // Show the CVV entry field on the UI
        private boolean showCVV = true;

        // zone where AuthorizeNet will be made available
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
         * @return the authorizeNetRequestUrl
         */
        public String getAuthorizeNetRequestUrl()
        {
            return authorizeNetRequestUrl;
        }

        /**
         * @param authorizeNetRequestUrl
         *            the authorizeNetRequestUrl to set
         */
        public void setAuthorizeNetRequestUrl(String authorizeNetRequestUrl)
        {
            this.authorizeNetRequestUrl = authorizeNetRequestUrl;
        }

        /**
         * @return the authorizeNetLoginId
         */
        public String getAuthorizeNetLoginId()
        {
            return authorizeNetLoginId;
        }

        /**
         * @param authorizeNetLoginId
         *            the authorizeNetLoginId to set
         */
        public void setAuthorizeNetLoginId(String authorizeNetLoginId)
        {
            this.authorizeNetLoginId = authorizeNetLoginId;
        }

        /**
         * @return the authorizeNetTxnKey
         */
        public String getAuthorizeNetTxnKey()
        {
            return authorizeNetTxnKey;
        }

        /**
         * @param authorizeNetTxnKey
         *            the authorizeNetTxnKey to set
         */
        public void setAuthorizeNetTxnKey(String authorizeNetTxnKey)
        {
            this.authorizeNetTxnKey = authorizeNetTxnKey;
        }

        /**
         * @return the testMode
         */
        public boolean isTestMode()
        {
            return testMode;
        }

        /**
         * @param testMode
         *            the testMode to set
         */
        public void setTestMode(boolean testMode)
        {
            this.testMode = testMode;
        }

        /**
         * @return the showCVV
         */
        public boolean isShowCVV()
        {
            return showCVV;
        }

        /**
         * @param showCVV
         *            the showCVV to set
         */
        public void setShowCVV(boolean showCVV)
        {
            this.showCVV = showCVV;
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