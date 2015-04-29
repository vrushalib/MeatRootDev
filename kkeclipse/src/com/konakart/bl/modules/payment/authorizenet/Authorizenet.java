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

import net.authorize.sim.Fingerprint;

import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.PaymentDetails;
import com.konakart.app.SSOToken;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.SSOTokenIf;
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

    private static final String hostPortSubstitute = "host:port";

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

    /**
     * If set to true, direct post will be used instead of AIM
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_DIRECT_POST = "MODULE_PAYMENT_AUTHORIZENET_DIRECT_POST";

    /**
     * Leave the MD5HashKey empty, unless you have explicitly set it in the merchant interface
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_MD5_HASH_KEY = "MODULE_PAYMENT_AUTHORIZENET_MD5_HASH_KEY";

    /**
     * URL used by AuthorizeNet Direct Post to callback into KonaKart. This would typically be HTTPS
     * and reachable from the internet.
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_RELAY_URL = "MODULE_PAYMENT_AUTHORIZENET_RELAY_URL";

    /**
     * Username and password used to log into the engine by the callback from AuthorizeNet
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_CALLBACK_USERNAME = "MODULE_PAYMENT_AUTHORIZENET_CALLBACK_USERNAME";

    private final static String MODULE_PAYMENT_AUTHORIZENET_CALLBACK_PASSWORD = "MODULE_PAYMENT_AUTHORIZENET_CALLBACK_PASSWORD";

    /**
     * If set to true, CIM functionality is enabled
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM = "MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM";

    /**
     * URL used by KonaKart to send CIM XML messages
     */
    private final static String MODULE_PAYMENT_AUTHORIZENET_CIM_WEB_SERVICE_URL = "MODULE_PAYMENT_AUTHORIZENET_CIM_WEB_SERVICE_URL";

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
        staticData.setTestMode(getConfigurationValueAsBool(MODULE_PAYMENT_AUTHORIZENET_TESTMODE,
                true));
        staticData.setZone(getConfigurationValueAsIntWithDefault(MODULE_PAYMENT_AUTHORIZENET_ZONE,
                0));
        staticData.setSortOrder(getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER, 0));
        staticData.setShowCVV(getConfigurationValueAsBool(MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV,
                true));
        staticData.setUseDirectPost(getConfigurationValueAsBool(
                MODULE_PAYMENT_AUTHORIZENET_DIRECT_POST, false));

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_MD5_HASH_KEY);
        if (confVal == null || confVal.length() == 0)
        {
            staticData.setMd5HashKey(null);
        } else
        {
            staticData.setMd5HashKey(confVal);
        }

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_RELAY_URL);
        if ((confVal == null || confVal.length() == 0) && staticData.isUseDirectPost())
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_RELAY_URL must be set to the URL used for calling back"
                            + " into KonaKart from Authorize.Net. (e.g. https://host:port/konakart/AuthNetCallback.action)");
        }
        staticData.setDirectPostRelayUrl(confVal);

        staticData.setCimEnabled(getConfigurationValueAsBool(
                MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM, false));

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_CIM_WEB_SERVICE_URL);
        if ((confVal == null || confVal.length() == 0) && staticData.isCimEnabled())
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_CIM_WEB_SERVICE_URL must be set to the URL used for sending"
                            + " CIM XML messages . (e.g. https://apitest.authorize.net/xml/v1/request.api for test"
                            + " and https://api.authorize.net/xml/v1/request.api for production)");
        }
        staticData.setCimWebServiceUrl(confVal);

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_CALLBACK_USERNAME);
        if ((confVal == null || confVal.length() == 0) && staticData.isUseDirectPost())
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_CALLBACK_USERNAME must be set to the Callback Username for the"
                            + " callback functionality.");
        }
        staticData.setCallbackUsername(confVal);

        confVal = getConfigurationValue(MODULE_PAYMENT_AUTHORIZENET_CALLBACK_PASSWORD);
        if ((confVal == null || confVal.length() == 0) && staticData.isUseDirectPost())
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_AUTHORIZENET_CALLBACK_PASSWORD must be set to the Callback Password for the"
                            + " callback functionality.");
        }
        staticData.setCallbackPassword(confVal);

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
        if (sd.isUseDirectPost())
        {
            pDetails.setPaymentType(PaymentDetails.BROWSER_IN_FRAME_PAYMENT_GATEWAY);
            pDetails.setPreProcessCode("AuthorizenetDPM");
        } else
        {
            pDetails.setPaymentType(PaymentDetails.SERVER_PAYMENT_GATEWAY);
        }
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

        parmList.add(new NameValue("x_invoice_num", order.getId()));
        parmList.add(new NameValue("x_login", sd.getAuthorizeNetLoginId()));
        parmList.add(new NameValue("x_type", "AUTH_CAPTURE"));
        parmList.add(new NameValue("x_test_request", (sd.isTestMode() ? "TRUE" : "FALSE")));

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

        // Set the billing information

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

        parmList.add(new NameValue("x_method", "CC"));

        if (sd.isUseDirectPost())
        {
            /*
             * Only required when using when using direct post. Currency code isn't sent because the
             * AuthorizeNet SDK Fingerprint class doesn't accept it as input when creating the hash
             * and so AuthorizeNet replies with an error. The docs say that if you are attempting to
             * pass the field x_currency_code with your payment form request, you must include this
             * field in your fingerprint hash generation. Since Authorize.Net currently handles
             * transaction amounts in the merchant's local currency by default, you may alternately
             * stop passing x_currency_code. Therefore we don't pass it.
             */
            Fingerprint fingerprint = Fingerprint.createFingerprint(sd.getAuthorizeNetLoginId(),
                    sd.getAuthorizeNetTxnKey(), order.getId(), total.toString());

            long x_fp_sequence = fingerprint.getSequence();
            long x_fp_timestamp = fingerprint.getTimeStamp();
            String x_fp_hash = fingerprint.getFingerprintHash();

            parmList.add(new NameValue("x_version", "3.1"));
            parmList.add(new NameValue("x_fp_hash", x_fp_hash));
            parmList.add(new NameValue("x_relay_url", sd.getDirectPostRelayUrl().replaceFirst(
                    hostPortSubstitute, info.getHostAndPort())));
            parmList.add(new NameValue("x_fp_sequence", Long.toString(x_fp_sequence)));
            parmList.add(new NameValue("x_fp_timestamp", Long.toString(x_fp_timestamp)));

            /*
             * Create a session here which will be used by the IPN callback
             */
            SSOTokenIf ssoToken = new SSOToken();
            String sessionId = getEng().login(sd.getCallbackUsername(), sd.getCallbackPassword());
            if (sessionId == null)
            {
                throw new KKException(
                        "Unable to log into the engine using the AuthorizeNet Callback Username and Password");
            }
            ssoToken.setSessionId(sessionId);
            ssoToken.setCustom1(String.valueOf(order.getId()));
            ssoToken.setCustom2(sd.getAuthorizeNetLoginId());
            ssoToken.setCustom3(sd.getMd5HashKey());

            /*
             * Save the SSOToken
             */
            String uuid = getEng().saveSSOToken(ssoToken);
            parmList.add(new NameValue("kk_uuid", uuid));

        } else
        {
            // Required for AIM
            parmList.add(new NameValue("x_delim_data", "True"));
            parmList.add(new NameValue("x_relay_response", "False"));
            parmList.add(new NameValue("x_tran_key", sd.getAuthorizeNetTxnKey()));
            parmList.add(new NameValue("x_delim_char", ","));
            parmList.add(new NameValue("x_encap_char", ""));
            parmList.add(new NameValue("x_currency_code", order.getCurrency().getCode()));
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
     * Method used to return any custom information required from the payment module. In this case
     * we use it to return the authentication information required by the CIM implementation.
     * 
     * @param sessionId
     * @param parameters
     * @return Returns information in a PaymentDetails object
     * @throws Exception
     */
    public PaymentDetails getPaymentDetailsCustom(String sessionId, NameValueIf[] parameters)
            throws Exception
    {
        if (parameters != null && parameters.length == 1 && parameters[0] != null
                && parameters[0].getValue() != null && parameters[0].getValue().equals("CIM"))
        {
            StaticData sd = staticDataHM.get(getStoreId());
            List<NameValue> parmList = new ArrayList<NameValue>();
            parmList.add(new NameValue("loginId", sd.getAuthorizeNetLoginId()));
            parmList.add(new NameValue("transactionKey", sd.getAuthorizeNetTxnKey()));
            parmList.add(new NameValue("webServiceURL", sd.getCimWebServiceUrl()));

            PaymentDetails pDetails = new PaymentDetails();
            NameValue[] nvArray = new NameValue[parmList.size()];
            parmList.toArray(nvArray);
            pDetails.setParameters(nvArray);

            return pDetails;
        }
        return null;

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

        // URL used by AuthorizeNet Direct Post to callback into KonaKart
        // "https://host:port/konakart/AuthNetCallback.action"
        private String directPostRelayUrl;

        // URL for sending CIM XML messages
        private String cimWebServiceUrl;

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

        // Use direct post instead of AIM
        private boolean useDirectPost = false;

        // Enable CIM functionality
        private boolean cimEnabled = false;

        // Leave the MD5HashKey empty, unless you have explicitly set it in the merchant interface
        private String md5HashKey = null;

        // username used by the callback to login to the engine
        private String callbackUsername;

        // password used by the callback to login to the engine
        private String callbackPassword;

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

        /**
         * @return the useDirectPost
         */
        public boolean isUseDirectPost()
        {
            return useDirectPost;
        }

        /**
         * @param useDirectPost
         *            the useDirectPost to set
         */
        public void setUseDirectPost(boolean useDirectPost)
        {
            this.useDirectPost = useDirectPost;
        }

        /**
         * @return the md5HashKey
         */
        public String getMd5HashKey()
        {
            return md5HashKey;
        }

        /**
         * @param md5HashKey
         *            the md5HashKey to set
         */
        public void setMd5HashKey(String md5HashKey)
        {
            this.md5HashKey = md5HashKey;
        }

        /**
         * @return the directPostRelayUrl
         */
        public String getDirectPostRelayUrl()
        {
            return directPostRelayUrl;
        }

        /**
         * @param directPostRelayUrl
         *            the directPostRelayUrl to set
         */
        public void setDirectPostRelayUrl(String directPostRelayUrl)
        {
            this.directPostRelayUrl = directPostRelayUrl;
        }

        /**
         * @return the callbackUsername
         */
        public String getCallbackUsername()
        {
            return callbackUsername;
        }

        /**
         * @param callbackUsername
         *            the callbackUsername to set
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
         * @param callbackPassword
         *            the callbackPassword to set
         */
        public void setCallbackPassword(String callbackPassword)
        {
            this.callbackPassword = callbackPassword;
        }

        /**
         * @return the cimWebServiceUrl
         */
        public String getCimWebServiceUrl()
        {
            return cimWebServiceUrl;
        }

        /**
         * @param cimWebServiceUrl
         *            the cimWebServiceUrl to set
         */
        public void setCimWebServiceUrl(String cimWebServiceUrl)
        {
            this.cimWebServiceUrl = cimWebServiceUrl;
        }

        /**
         * @return the cimEnabled
         */
        public boolean isCimEnabled()
        {
            return cimEnabled;
        }

        /**
         * @param cimEnabled
         *            the cimEnabled to set
         */
        public void setCimEnabled(boolean cimEnabled)
        {
            this.cimEnabled = cimEnabled;
        }

    }

}