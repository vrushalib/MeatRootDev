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

package com.konakart.bl.modules.payment.worldpayxmlredirect;

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
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * WorldPay XML Redirect payment module. This payment module allows for credit card credentials to
 * be collected from a WorldPay page.
 */
public class WorldPayXMLRedirect extends BasePaymentModule implements PaymentInterface
{
    /** Module name must be the same as the class name although it can be all in lowercase */
    public static final String WP_XML_REDIRECT_GATEWAY_CODE = "WorldPayXMLRedirect";

    private static String bundleName = BaseModule.basePackage
            + ".payment.worldpayxmlredirect.WorldPayXMLRedirect";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "WorldPayXMLRedirectMutex";

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_STATUS = "MODULE_PAYMENT_WP_XML_REDIRECT_STATUS";

    /**
     * The WorldPay zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress
     * of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_ZONE = "MODULE_PAYMENT_WP_XML_REDIRECT_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_SORT_ORDER = "MODULE_PAYMENT_WP_XML_REDIRECT_SORT_ORDER";

    /**
     * Merchant Code
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_MERCHANT_CODE = "MODULE_PAYMENT_WP_XML_REDIRECT_MERCHANT_CODE";

    /**
     * Password for sending the XML request
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_XML_PASSWORD = "MODULE_PAYMENT_WP_XML_REDIRECT_XML_PASSWORD";

    /**
     * Shared secret used to create a hash value in order to verify the hash value created by
     * WorldPay on the redirect URL.
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_SHARED_SECRET = "MODULE_PAYMENT_WP_XML_REDIRECT_SHARED_SECRET";

    /**
     * URL used by KonaKart to send the transaction details
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_REQUEST_URL = "MODULE_PAYMENT_WP_XML_REDIRECT_REQUEST_URL";

    /**
     * "Used to selectively include payment methods. They should be separated by commas. (e.g.
     * AMEX-SSL,VISA-SSL,ECMC-SSL)
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_INCLUDE_METHODS = "MODULE_PAYMENT_WP_XML_REDIRECT_INCLUDE_METHODS";

    /**
     * Used to selectively exclude payment methods. They should be separated by commas. (e.g.
     * CB-SSL,DINERS-SSL,DISCOVER-SSL)"
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_EXCLUDE_METHODS = "MODULE_PAYMENT_WP_XML_REDIRECT_EXCLUDE_METHODS";

    /**
     * URL to redirect to when transaction has terminated
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_RESPONSE_URL = "MODULE_PAYMENT_WP_XML_REDIRECT_RESPONSE_URL";

    /**
     * If set to true, the order number is used as the order identifier in the request. Otherwise
     * the unique numeric order id is used.
     */
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_USE_ORDER_NUMBER = "MODULE_PAYMENT_WP_XML_REDIRECT_USE_ORDER_NUMBER";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_TITLE = "module.payment.worldpayxmlredirect.text.title";

    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_DESCRIPTION = "module.payment.worldpayxmlredirect.text.description";

    private final static String MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_PAYMENT_PAGE_DESCRIPTION = "module.payment.worldpayxmlredirect.text.payment.page.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public WorldPayXMLRedirect(KKEngIf eng) throws KKException
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

        String reqUrl = getConfigurationValue(MODULE_PAYMENT_WP_XML_REDIRECT_REQUEST_URL);
        if (reqUrl == null || reqUrl.length() == 0)
        {
            throw new KKException(
                    "The Configuration parameter "
                            + MODULE_PAYMENT_WP_XML_REDIRECT_REQUEST_URL
                            + " must be set to the URL for"
                            + " sending the request to WorldPay. (e.g. https://secure-test.worldpay.com/jsp/merchant/xml/paymentService.jsp)");
        }
        staticData.setRequestUrl(reqUrl);

        String merchantCode = getConfigurationValue(
                MODULE_PAYMENT_WP_XML_REDIRECT_MERCHANT_CODE);
        if (merchantCode == null || merchantCode.length() == 0)
        {
            throw new KKException("The Configuration parameter "
                    + MODULE_PAYMENT_WP_XML_REDIRECT_MERCHANT_CODE
                    + " must be set to the Merchant Code.");
        }
        staticData.setMerchantCode(merchantCode);

        String password = getConfigurationValue(
                MODULE_PAYMENT_WP_XML_REDIRECT_XML_PASSWORD);
        if (password == null || password.length() == 0)
        {
            throw new KKException("The Configuration parameter "
                    + MODULE_PAYMENT_WP_XML_REDIRECT_XML_PASSWORD
                    + " must be set to the password required for sending the XML request.");
        }
        staticData.setPassword(password);

        String responseUrl = getConfigurationValue(
                MODULE_PAYMENT_WP_XML_REDIRECT_RESPONSE_URL);
        if (responseUrl == null || responseUrl.length() == 0)
        {
            throw new KKException(
                    "The Configuration parameter "
                            + MODULE_PAYMENT_WP_XML_REDIRECT_RESPONSE_URL
                            + " must be set to the responseUrl used by WorldPay to redirect the user once the transaction has terminated.");
        }
        staticData.setResponseUrl(responseUrl);

        String sharedSecret = getConfigurationValue(
                MODULE_PAYMENT_WP_XML_REDIRECT_SHARED_SECRET);
        if (sharedSecret == null || sharedSecret.length() == 0)
        {
            throw new KKException(
                    "The Configuration parameter "
                            + MODULE_PAYMENT_WP_XML_REDIRECT_SHARED_SECRET
                            + " must be set to the Shared Secret string so that redirects from WorldPay can be validated.");
        }
        staticData.setSharedSecret(sharedSecret);

        KKConfiguration conf = getConfiguration(MODULE_PAYMENT_WP_XML_REDIRECT_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_WP_XML_REDIRECT_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        String includeMethods = getConfigurationValue(
                MODULE_PAYMENT_WP_XML_REDIRECT_INCLUDE_METHODS);
        staticData.setIncludeMethods(includeMethods);

        String excludeMethods = getConfigurationValue(
                MODULE_PAYMENT_WP_XML_REDIRECT_EXCLUDE_METHODS);
        staticData.setExcludeMethods(excludeMethods);

        conf = getConfiguration(MODULE_PAYMENT_WP_XML_REDIRECT_USE_ORDER_NUMBER);
        if (conf != null && conf.getValue() != null && conf.getValue().equalsIgnoreCase("true"))
        {
            staticData.setUseOrderNumber(true);
        }

    }

    /**
     * Return a payment details object for the payment module
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
         * The zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress of the
         * order isn't within that GeoZone, then we throw an exception
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
        pDetails.setCode(WP_XML_REDIRECT_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setPaymentType(PaymentDetails.BROWSER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        /*
         * Instead of going straight to JSP that posts the parameters and does the redirect, we need
         * to go to a Struts action that creates the XML request, posts it and receives the URL that
         * the customer needs to be redirected to
         */
        pDetails.setPreProcessCode(WP_XML_REDIRECT_GATEWAY_CODE);

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        // Could be Order Number or Order SKU. No need to URL encode if use Order Id.
        if (sd.isUseOrderNumber())
        {
            parmList.add(new NameValue("orderCode", URLEncoder.encode(order.getOrderNumber(),
                    "UTF-8")));
        } else
        {
            parmList.add(new NameValue("orderCode", Integer.toString(order.getId())));
        }

        // Merchant code supplied by WorldPay
        parmList.add(new NameValue("merchantCode", sd.getMerchantCode()));

        // Password used to send XML request
        parmList.add(new NameValue("password", sd.getPassword()));

        // You can add non static data to the description such as the order number or id
        parmList.add(new NameValue("description", rb
                .getString(MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_PAYMENT_PAGE_DESCRIPTION)));

        // After credit card details have been entered, WorldPay send the customer back to this URL
        parmList.add(new NameValue("responseUrl", URLEncoder.encode(sd.getResponseUrl(), "UTF-8")));

        // Get the total
        BigDecimal total = null;
        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
                total = total.movePointRight(scale); // Must be integer value for WorldPay
            }
        }

        if (total == null)
        {
            throw new KKException("A Total Order Total was not found");
        }

        parmList.add(new NameValue("value", total.toString()));
        parmList.add(new NameValue("currencyCode", URLEncoder.encode(order.getCurrency().getCode(),
                "UTF-8")));
        parmList.add(new NameValue("exponent", Integer.toString(scale)));
        parmList.add(new NameValue("include", sd.getIncludeMethods()));
        parmList.add(new NameValue("exclude", sd.getExcludeMethods()));

        parmList.add(new NameValue("shopperEmailAddress", URLEncoder.encode(
                order.getCustomerEmail(), "UTF-8")));

        // Set the delivery names from the delivery address Id

        String[] dNames = getFirstAndLastNamesFromAddress(order.getDeliveryAddrId());
        if (dNames != null)
        {
            parmList.add(new NameValue("firstName", URLEncoder.encode(dNames[0],"UTF-8")));
            parmList.add(new NameValue("lastName", URLEncoder.encode(dNames[1], "UTF-8")));
        }

        parmList.add(new NameValue("street", URLEncoder.encode(order.getDeliveryStreetAddress(),
                "UTF-8")));
        parmList.add(new NameValue("city", URLEncoder.encode(order.getDeliveryCity(), "UTF-8")));
        parmList.add(new NameValue("postalCode", URLEncoder.encode(order.getDeliveryPostcode(),
                "UTF-8")));
        parmList.add(new NameValue("telephoneNumber", order.getDeliveryTelephone()));

        // Country requires the two letter country code
        CountryIf country = getEng().getCountryPerName(order.getDeliveryCountry());
        if (country != null)
        {
            parmList.add(new NameValue("countryCode", country.getIsoCode2()));
        }

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);
        
        // Add the shared secret to a custom field
        pDetails.setCustom1(sd.getSharedSecret());

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
        return isAvailable(MODULE_PAYMENT_WP_XML_REDIRECT_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // The WorldPay Url used to POST the payment request.
        private String requestUrl;

        // The merchant code
        private String merchantCode;

        // The password for sending the xml
        private String password;

        private int zone;

        private String sharedSecret;

        private String includeMethods;

        private String excludeMethods;

        private String responseUrl;

        private boolean useOrderNumber = false;

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
         * @return the requestUrl
         */
        public String getRequestUrl()
        {
            return requestUrl;
        }

        /**
         * @param requestUrl
         *            the requestUrl to set
         */
        public void setRequestUrl(String requestUrl)
        {
            this.requestUrl = requestUrl;
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
         * @return the merchantCode
         */
        public String getMerchantCode()
        {
            return merchantCode;
        }

        /**
         * @param merchantCode
         *            the merchantCode to set
         */
        public void setMerchantCode(String merchantCode)
        {
            this.merchantCode = merchantCode;
        }

        /**
         * @return the sharedSecret
         */
        public String getSharedSecret()
        {
            return sharedSecret;
        }

        /**
         * @param sharedSecret
         *            the sharedSecret to set
         */
        public void setSharedSecret(String sharedSecret)
        {
            this.sharedSecret = sharedSecret;
        }

        /**
         * @return the includeMethods
         */
        public String getIncludeMethods()
        {
            return includeMethods;
        }

        /**
         * @param includeMethods
         *            the includeMethods to set
         */
        public void setIncludeMethods(String includeMethods)
        {
            this.includeMethods = includeMethods;
        }

        /**
         * @return the excludeMethods
         */
        public String getExcludeMethods()
        {
            return excludeMethods;
        }

        /**
         * @param excludeMethods
         *            the excludeMethods to set
         */
        public void setExcludeMethods(String excludeMethods)
        {
            this.excludeMethods = excludeMethods;
        }

        /**
         * @return the password
         */
        public String getPassword()
        {
            return password;
        }

        /**
         * @param password
         *            the password to set
         */
        public void setPassword(String password)
        {
            this.password = password;
        }

        /**
         * @return the responseUrl
         */
        public String getResponseUrl()
        {
            return responseUrl;
        }

        /**
         * @param responseUrl
         *            the responseUrl to set
         */
        public void setResponseUrl(String responseUrl)
        {
            this.responseUrl = responseUrl;
        }

        /**
         * @return the useOrderNumber
         */
        public boolean isUseOrderNumber()
        {
            return useOrderNumber;
        }

        /**
         * @param useOrderNumber
         *            the useOrderNumber to set
         */
        public void setUseOrderNumber(boolean useOrderNumber)
        {
            this.useOrderNumber = useOrderNumber;
        }

    }
}
