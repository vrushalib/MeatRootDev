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

package com.konakart.bl.modules.payment.barclaycardsmartpayhosted;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.PaymentDetails;
import com.konakart.app.SSOToken;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.appif.StoreIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * Barclaycard SmartPay Hosted IPN module
 */
public class BarclaycardSmartPayHosted extends BasePaymentModule implements PaymentInterface
{
    /**
     * Module name must be the same as the class name although it can be all in lower case
     */
    public static String BC_SPAY_HOSTED_GATEWAY_CODE = "BarclaycardSmartPayHosted";

    private static String bundleName = BaseModule.basePackage
            + ".payment.barclaycardsmartpayhosted.BarclaycardSmartPayHosted";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "barclaycardSmartPayHostedMutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_STATUS = "MODULE_PAYMENT_BC_SPAY_HOSTED_STATUS";

    /**
     * The BarclaycardSmartPayHostedZone zone, if greater than zero, should reference a GeoZone. If
     * the DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_ZONE = "MODULE_PAYMENT_BC_SPAY_HOSTED_ZONE";

    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_ORDER_STATUS_ID = "MODULE_PAYMENT_BC_SPAY_HOSTED_ORDER_STATUS_ID";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_SORT_ORDER = "MODULE_PAYMENT_BC_SPAY_HOSTED_SORT_ORDER";

    /**
     * The BarclaycardSmartPayHosted Url used to POST the payment request.
     * "https://secure.barclaycardSmartPayHosted.com/index_shop.cgi"
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_REQUEST_URL = "MODULE_PAYMENT_BC_SPAY_HOSTED_REQUEST_URL";

    /**
     * The BarclaycardSmartPayHosted Response URL. This is where the final result is posted.
     * "https://host:port/konakart/BarclaycardSmartPayHostedResponse.action"
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_RESPONSE_URL = "MODULE_PAYMENT_BC_SPAY_HOSTED_RESPONSE_URL";

    /**
     * We use the HMAC key to encrypt and decrypt messages
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_HMAC_KEY = "MODULE_PAYMENT_BC_SPAY_HOSTED_HMAC_KEY";

    /**
     * Define the Skin Code for the hosted page
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_SKIN_CODE = "MODULE_PAYMENT_BC_SPAY_HOSTED_SKIN_CODE";

    /**
     * Merchant Account
     */
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_MERCHANT_ACCOUNT = "MODULE_PAYMENT_BC_SPAY_HOSTED_MERCHANT_ACCOUNT";

    private static final String MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_USERNAME = "MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_USERNAME";

    private static final String MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_PASSWORD = "MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_PASSWORD";

    private static final String MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_AUTH = "MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_AUTH";

    private static final String MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_USERNAME = "MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_USERNAME";

    private static final String MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_PASSWORD = "MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_PASSWORD";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_TEXT_TITLE = "module.payment.barclaycardSmartPayHosted.text.title";

    private final static String MODULE_PAYMENT_BC_SPAY_HOSTED_TEXT_DESCRIPTION = "module.payment.barclaycardSmartPayHosted.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public BarclaycardSmartPayHosted(KKEngIf eng) throws KKException
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
     * Return a payment details object for BarclaycardSmartPayHosted IPN module
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
         * The BarclaycardSmartPayHostedZone zone, if greater than zero, should reference a GeoZone.
         * If the DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
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
        pDetails.setCode(BC_SPAY_HOSTED_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.BROWSER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_BC_SPAY_HOSTED_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_BC_SPAY_HOSTED_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getBarclaycardSmartPayHostedRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

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

        total = total.movePointRight(scale);
        total.setScale(0);
        String storeId = "?";
        StoreIf store = getEng().getStore();
        if (store != null)
        {
            storeId = store.getStoreId();
        }

        int engineMode = getEng().getEngConf().getMode();
        boolean customersShared = getEng().getEngConf().isCustomersShared();
        boolean productsShared = getEng().getEngConf().isProductsShared();
        boolean categoriesShared = getEng().getEngConf().isCategoriesShared();
        String countryCode = order.getLocale().substring(0, 2);

        /*
         * Create a session here which will be used by the IPN callback
         */
        SSOTokenIf ssoToken = new SSOToken();
        String sessionId = getEng().login(sd.getBarclaycardSmartPayHostedCallbackUsername(),
                sd.getBarclaycardSmartPayHostedCallbackPassword());
        if (sessionId == null)
        {
            throw new KKException(
                    "Unable to log into the engine using the BarclaycardSmartPayHosted Callback Username and Password");
        }
        ssoToken.setSessionId(sessionId);
        ssoToken.setCustom1(sd.getBarclaycardSmartPayHostedHTTPAuth() + "~" + order.getId() + "~"
                + countryCode);
        ssoToken.setCustom2(sd.getBarclaycardSmartPayHostedHTTPUsername());
        ssoToken.setCustom3(sd.getBarclaycardSmartPayHostedHTTPPassword());

        if (log.isDebugEnabled())
        {
            log.debug("SSO Token data:         \n" + "    custom1              = "
                    + ssoToken.getCustom1() + "\n" + "    custom2              = "
                    + ssoToken.getCustom2() + "\n" + "    custom3              = "
                    + ssoToken.getCustom3());
        }
        
        /*
         * Save the SSOToken with a valid sessionId and other data in custom fields
         */
        String uuid = getEng().saveSSOToken(ssoToken);

        String merchantReference = uuid;
        String shipBeforeDate = getDateOffsetString(5);
        String skinCode = sd.getBarclaycardSmartPayHostedSkinCode();
        String merchantAccount = sd.getBarclaycardSmartPayHostedMerchantAccount();
        String merchantReturnData = order.getId() + "~" + order.getOrderNumber() + "~" + storeId
                + "~" + engineMode + "~" + customersShared + "~" + productsShared + "~"
                + categoriesShared + "~" + countryCode;
        String sessionValidity = getValidUntilString(4);
        String shopperEmail = order.getBillingEmail();
        if (order.getBillingEmail() == null || order.getBillingEmail().length() == 0)
        {
            shopperEmail = order.getCustomerEmail();
        }
        String shopperReference = String.valueOf(order.getCustomerId());
        String allowedMethods = ""; // optional
        String blockedMethods = ""; // optional
        String offset = ""; // optional
        String shopperStatement = ""; // optional
        String recurringContract = ""; // optional
        String billingAddressType = ""; // means Not supplied!
        String deliveryAddressType = ""; // means Not supplied!
        String resURL = sd.getBarclaycardSmartPayHostedResponseUrl().replaceFirst(
                hostPortSubstitute, info.getHostAndPort());

        String signatureString = total.toString() + order.getCurrencyCode() + shipBeforeDate
                + merchantReference + skinCode + merchantAccount + sessionValidity + shopperEmail
                + shopperReference + recurringContract + allowedMethods + blockedMethods
                + shopperStatement + merchantReturnData + billingAddressType + deliveryAddressType
                + offset;

        if (log.isDebugEnabled())
        {
            log.debug("Signature String         =\n" + signatureString);
        }

        String encodedSignature = BarclaycardSmartPayHostedHMACTools.getBase64EncodedSignature(
                sd.getBarclaycardSmartPayHostedHMACKey(), signatureString);

        if (log.isDebugEnabled())
        {
            log.debug("Encoded Signature String =\n" + encodedSignature);
        }

        parmList.add(new NameValue("merchantReference", merchantReference));
        parmList.add(new NameValue("merchantAccount", merchantAccount));
        parmList.add(new NameValue("paymentAmount", total.toString()));
        parmList.add(new NameValue("currencyCode", order.getCurrencyCode()));
        parmList.add(new NameValue("shipBeforeDate", shipBeforeDate));
        parmList.add(new NameValue("skinCode", skinCode));
        parmList.add(new NameValue("shopperReference", shopperReference));
        parmList.add(new NameValue("shopperEmail", shopperEmail));
        parmList.add(new NameValue("shopperLocale", info.getLocale().toString()));
        parmList.add(new NameValue("billingAddressType", billingAddressType));
        parmList.add(new NameValue("resURL", resURL));
        parmList.add(new NameValue("sessionValidity", sessionValidity));
        parmList.add(new NameValue("merchantReturnData", merchantReturnData));
        parmList.add(new NameValue("merchantSig", encodedSignature));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        if (log.isDebugEnabled())
        {
            log.debug(pDetails.toString());

            log.debug("For posting to test HMAC code:");
            String post = "https://ca-test.barclaycardsmartpay.com/ca/ca/skin/checkhmac.shtml?";
            for (int p = 0; p < nvArray.length; p++)
            {
                if (p > 0)
                {
                    post += "&";
                }
                post += nvArray[p].getName() + "=" + nvArray[p].getValue();
            }
            log.debug("\n" + post);
        }

        return pDetails;
    }

    private String getValidUntilString(int hours)
    {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone tZone = TimeZone.getTimeZone("UTC");
        f.setTimeZone(tZone);
        Calendar cal = GregorianCalendar.getInstance(tZone);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        if (log.isDebugEnabled())
        {
            log.debug("got Time plus " + hours + " hours : " + f.format(cal.getTime()));
        }
        return f.format(cal.getTime());
    }

    private String getDateOffsetString(int days)
    {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        TimeZone tZone = TimeZone.getTimeZone("UTC");
        f.setTimeZone(tZone);
        Calendar cal = GregorianCalendar.getInstance(tZone);
        cal.add(Calendar.DAY_OF_YEAR, days);
        if (log.isDebugEnabled())
        {
            log.debug("got Date plus " + days + " days : " + f.format(cal.getTime()));
        }
        return f.format(cal.getTime());
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

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_REQUEST_URL must be set to the URL for"
                            + " sending payment requests to BarclaycardSmartPayHosted. (e.g. https://secure.barclaycardSmartPayHosted.com/index_shop.cgi)");
        }
        staticData.setBarclaycardSmartPayHostedRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_RESPONSE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_RESPONSE_URL must be set to the response URL for"
                            + " sending results back from BarclaycardSmartPayHosted. (e.g. https://host:port/konakart/BarclaycardSmartPayHostedResponse.action");
        }
        staticData.setBarclaycardSmartPayHostedResponseUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_HMAC_KEY);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_HMAC_KEY must be set to the HMAC Key for your Merchant Account");
        }
        staticData.setBarclaycardSmartPayHostedHMACKey(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_USERNAME must be set to the Callback Username for the"
                            + " Notification functionality.");
        }
        staticData.setBarclaycardSmartPayHostedCallbackUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_PASSWORD must be set to the Callback Pasword for the"
                            + " Notification functionality.");
        }
        staticData.setBarclaycardSmartPayHostedCallbackPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_AUTH);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_AUTH must be set to true or false for the"
                            + " Notification functionality.");
        }
        staticData.setBarclaycardSmartPayHostedHTTPAuth(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_USERNAME must be set to the HTTP Username for the"
                            + " Notification functionality.");
        }
        staticData.setBarclaycardSmartPayHostedHTTPUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_PASSWORD must be set to the HTTP Password for the"
                            + " Notification functionality.");
        }
        staticData.setBarclaycardSmartPayHostedHTTPPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_MERCHANT_ACCOUNT);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_MERCHANT_ACCOUNT must be set to the required Merchant Account");
        }
        staticData.setBarclaycardSmartPayHostedMerchantAccount(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_SKIN_CODE);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_HOSTED_SKIN_CODE must be set to the required skin code");
        }
        staticData.setBarclaycardSmartPayHostedSkinCode(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_HOSTED_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_PAYMENT_BC_SPAY_HOSTED_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // Callback called by Gateway after a transaction
        // private String barclaycardSmartPayHostedCallbackUrl;

        // Redirect URL used by gateway when payment has been declined.
        // private String barclaycardSmartPayHostedDeclineUrl;

        // Redirect URL used by gateway when payment has been accepted
        // private String barclaycardSmartPayHostedAcceptUrl;

        // Defines the Skin that will be used on the hosted service
        private String barclaycardSmartPayHostedSkinCode;

        // The HMAC Key used for encrypting and decrypting messages
        private String barclaycardSmartPayHostedHMACKey;

        // The BarclaycardSmartPayHosted Url used to POST the payment request.
        private String barclaycardSmartPayHostedRequestUrl;

        // The BarclaycardSmartPayHosted Response Url.
        private String barclaycardSmartPayHostedResponseUrl;

        // The BarclaycardSmartPayHosted Merchant Account.
        private String barclaycardSmartPayHostedMerchantAccount;

        // Callback username
        private String barclaycardSmartPayHostedCallbackUsername;

        // Callback Password
        private String barclaycardSmartPayHostedCallbackPassword;

        // HTTP Username
        private String barclaycardSmartPayHostedHTTPUsername;

        // HTTP Password
        private String barclaycardSmartPayHostedHTTPPassword;

        // True of False depending on whether HTTP Authentication is required
        private String barclaycardSmartPayHostedHTTPAuth;

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
         * @return the barclaycardSmartPayHostedRequestUrl
         */
        public String getBarclaycardSmartPayHostedRequestUrl()
        {
            return barclaycardSmartPayHostedRequestUrl;
        }

        /**
         * @param barclaycardSmartPayHostedRequestUrl
         *            the barclaycardSmartPayHostedRequestUrl to set
         */
        public void setBarclaycardSmartPayHostedRequestUrl(
                String barclaycardSmartPayHostedRequestUrl)
        {
            this.barclaycardSmartPayHostedRequestUrl = barclaycardSmartPayHostedRequestUrl;
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
         * @return the barclaycardSmartPayHostedSkinCode
         */
        public String getBarclaycardSmartPayHostedSkinCode()
        {
            return barclaycardSmartPayHostedSkinCode;
        }

        /**
         * @param barclaycardSmartPayHostedSkinCode
         *            the barclaycardSmartPayHostedSkinCode to set
         */
        public void setBarclaycardSmartPayHostedSkinCode(String barclaycardSmartPayHostedSkinCode)
        {
            this.barclaycardSmartPayHostedSkinCode = barclaycardSmartPayHostedSkinCode;
        }

        /**
         * @return the barclaycardSmartPayHostedHMACKey
         */
        public String getBarclaycardSmartPayHostedHMACKey()
        {
            return barclaycardSmartPayHostedHMACKey;
        }

        /**
         * @param barclaycardSmartPayHostedHMACKey
         *            the barclaycardSmartPayHostedHMACKey to set
         */
        public void setBarclaycardSmartPayHostedHMACKey(String barclaycardSmartPayHostedHMACKey)
        {
            this.barclaycardSmartPayHostedHMACKey = barclaycardSmartPayHostedHMACKey;
        }

        /**
         * @return the barclaycardSmartPayHostedMerchantAccount
         */
        public String getBarclaycardSmartPayHostedMerchantAccount()
        {
            return barclaycardSmartPayHostedMerchantAccount;
        }

        /**
         * @param barclaycardSmartPayHostedMerchantAccount
         *            the barclaycardSmartPayHostedMerchantAccount to set
         */
        public void setBarclaycardSmartPayHostedMerchantAccount(
                String barclaycardSmartPayHostedMerchantAccount)
        {
            this.barclaycardSmartPayHostedMerchantAccount = barclaycardSmartPayHostedMerchantAccount;
        }

        /**
         * @return the barclaycardSmartPayHostedResponseUrl
         */
        public String getBarclaycardSmartPayHostedResponseUrl()
        {
            return barclaycardSmartPayHostedResponseUrl;
        }

        /**
         * @param barclaycardSmartPayHostedResponseUrl
         *            the barclaycardSmartPayHostedResponseUrl to set
         */
        public void setBarclaycardSmartPayHostedResponseUrl(
                String barclaycardSmartPayHostedResponseUrl)
        {
            this.barclaycardSmartPayHostedResponseUrl = barclaycardSmartPayHostedResponseUrl;
        }

        /**
         * @return the barclaycardSmartPayHostedCallbackUsername
         */
        public String getBarclaycardSmartPayHostedCallbackUsername()
        {
            return barclaycardSmartPayHostedCallbackUsername;
        }

        /**
         * @param barclaycardSmartPayHostedCallbackUsername
         *            the barclaycardSmartPayHostedCallbackUsername to set
         */
        public void setBarclaycardSmartPayHostedCallbackUsername(
                String barclaycardSmartPayHostedCallbackUsername)
        {
            this.barclaycardSmartPayHostedCallbackUsername = barclaycardSmartPayHostedCallbackUsername;
        }

        /**
         * @return the barclaycardSmartPayHostedCallbackPassword
         */
        public String getBarclaycardSmartPayHostedCallbackPassword()
        {
            return barclaycardSmartPayHostedCallbackPassword;
        }

        /**
         * @param barclaycardSmartPayHostedCallbackPassword
         *            the barclaycardSmartPayHostedCallbackPassword to set
         */
        public void setBarclaycardSmartPayHostedCallbackPassword(
                String barclaycardSmartPayHostedCallbackPassword)
        {
            this.barclaycardSmartPayHostedCallbackPassword = barclaycardSmartPayHostedCallbackPassword;
        }

        /**
         * @return the barclaycardSmartPayHostedHTTPUsername
         */
        public String getBarclaycardSmartPayHostedHTTPUsername()
        {
            return barclaycardSmartPayHostedHTTPUsername;
        }

        /**
         * @param barclaycardSmartPayHostedHTTPUsername
         *            the barclaycardSmartPayHostedHTTPUsername to set
         */
        public void setBarclaycardSmartPayHostedHTTPUsername(
                String barclaycardSmartPayHostedHTTPUsername)
        {
            this.barclaycardSmartPayHostedHTTPUsername = barclaycardSmartPayHostedHTTPUsername;
        }

        /**
         * @return the barclaycardSmartPayHostedHTTPPassword
         */
        public String getBarclaycardSmartPayHostedHTTPPassword()
        {
            return barclaycardSmartPayHostedHTTPPassword;
        }

        /**
         * @param barclaycardSmartPayHostedHTTPPassword
         *            the barclaycardSmartPayHostedHTTPPassword to set
         */
        public void setBarclaycardSmartPayHostedHTTPPassword(
                String barclaycardSmartPayHostedHTTPPassword)
        {
            this.barclaycardSmartPayHostedHTTPPassword = barclaycardSmartPayHostedHTTPPassword;
        }

        /**
         * @return the barclaycardSmartPayHostedHTTPAuth
         */
        public String getBarclaycardSmartPayHostedHTTPAuth()
        {
            return barclaycardSmartPayHostedHTTPAuth;
        }

        /**
         * @param barclaycardSmartPayHostedHTTPAuth
         *            the barclaycardSmartPayHostedHTTPAuth to set
         */
        public void setBarclaycardSmartPayHostedHTTPAuth(String barclaycardSmartPayHostedHTTPAuth)
        {
            this.barclaycardSmartPayHostedHTTPAuth = barclaycardSmartPayHostedHTTPAuth;
        }
    }
}
