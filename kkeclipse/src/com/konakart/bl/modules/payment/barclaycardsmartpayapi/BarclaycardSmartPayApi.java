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

package com.konakart.bl.modules.payment.barclaycardsmartpayapi;

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
import com.konakart.appif.KKEngIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * Barclaycard SmartPay API IPN module
 */
public class BarclaycardSmartPayApi extends BasePaymentModule implements PaymentInterface
{
    /**
     * Module name - make this the same name as this class
     */
    public static String BC_SPAY_API_GATEWAY_CODE = "BarclaycardSmartPayApi";

    private static String bundleName = BaseModule.basePackage + ".payment."
            + BC_SPAY_API_GATEWAY_CODE.toLowerCase() + "." + BC_SPAY_API_GATEWAY_CODE;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = BC_SPAY_API_GATEWAY_CODE + "Mutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_BC_SPAY_API_STATUS = "MODULE_PAYMENT_BC_SPAY_API_STATUS";

    /**
     * The BarclaycardSmartPayApiZone zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_BC_SPAY_API_ZONE = "MODULE_PAYMENT_BC_SPAY_API_ZONE";

    private final static String MODULE_PAYMENT_BC_SPAY_API_ORDER_STATUS_ID = "MODULE_PAYMENT_BC_SPAY_API_ORDER_STATUS_ID";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_BC_SPAY_API_SORT_ORDER = "MODULE_PAYMENT_BC_SPAY_API_SORT_ORDER";

    /**
     * The BarclaycardSmartPayApi Url used to POST the payment request.
     * "https://pal-test.barclaycardsmartpay.com/pal/servlet/soap/Payment"
     */
    public final static String MODULE_PAYMENT_BC_SPAY_API_REQUEST_URL = "MODULE_PAYMENT_BC_SPAY_API_REQUEST_URL";

    /**
     * This URL is used by the BarclaycardSmartPayApi IPN functionality to call back into the
     * application with the results of the 3D Secure transaction. It must be a URL that is visible
     * from the internet. If it is in the form
     * http://host:port/konakart/CatalogCheckoutExternalPaymentErrorPage.do, then the string
     * host:port is substituted automatically with the correct value.
     */
    private final static String MODULE_PAYMENT_BC_SPAY_API_3D_RESPONSE_URL = "MODULE_PAYMENT_BC_SPAY_API_3D_RESPONSE_URL";

    private final static String MODULE_PAYMENT_BC_SPAY_3D_STATUS = "MODULE_PAYMENT_BC_SPAY_3D_STATUS";

    /**
     * Merchant Account
     */
    public final static String MODULE_PAYMENT_BC_SPAY_API_MERCHANT_ACC = "MODULE_PAYMENT_BC_SPAY_API_MERCHANT_ACC";

    /**
     * Username for accessing the SmartPay API
     */
    public final static String MODULE_PAYMENT_BC_SPAY_API_USER_ID = "MODULE_PAYMENT_BC_SPAY_API_USER_ID";

    /**
     * Password for accessing the SmartPay API
     */
    public final static String MODULE_PAYMENT_BC_SPAY_API_PASSWORD = "MODULE_PAYMENT_BC_SPAY_API_PASSWORD";

    /**
     * Required for asynchronous notification
     */
    private static final String MODULE_PAYMENT_BC_SPAY_API_CALLBACK_USERNAME = "MODULE_PAYMENT_BC_SPAY_API_CALLBACK_USERNAME";

    private static final String MODULE_PAYMENT_BC_SPAY_API_CALLBACK_PASSWORD = "MODULE_PAYMENT_BC_SPAY_API_CALLBACK_PASSWORD";

    private static final String MODULE_PAYMENT_BC_SPAY_API_HTTP_AUTH = "MODULE_PAYMENT_BC_SPAY_API_HTTP_AUTH";

    private static final String MODULE_PAYMENT_BC_SPAY_API_HTTP_USERNAME = "MODULE_PAYMENT_BC_SPAY_API_HTTP_USERNAME";

    private static final String MODULE_PAYMENT_BC_SPAY_API_HTTP_PASSWORD = "MODULE_PAYMENT_BC_SPAY_API_HTTP_PASSWORD";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_BC_SPAY_API_TEXT_TITLE = "module.payment.barclaycardSmartPayApi.text.title";

    private final static String MODULE_PAYMENT_BC_SPAY_API_TEXT_DESCRIPTION = "module.payment.barclaycardSmartPayApi.text.description";

    /**
     * 3D-Secure Status
     */
    public static final String BC_SPAY_API_3D_STATUS = "3D_STATUS";

    /**
     * 3D-Secure Response URL
     */
    public static final String BC_SPAY_API_3D_RESPONSE_URL = "3D_RESPONSE_URL";

    /**
     * Merchant Account
     */
    public static final String BC_SPAY_API_MERCHANT_ACCOUNT = "MERCHANT_ACCOUNT";

    /**
     * paResponse
     */
    public static final String BC_SPAY_API_PARESPONSE = "PARESPONSE";

    /**
     * paRequest
     */
    public static final String BC_SPAY_API_PAREQUEST = "PaReq";

    /**
     * MD
     */
    public static final String BC_SPAY_API_MD = "MD";

    /**
     * Merchant Reference
     */
    public static final String BC_SPAY_API_MERCHANT_REF = "MERCHANT_REF";

    /**
     * Currency Code
     */
    public static final String BC_SPAY_API_CURRENCY_CODE = "CURRENCY_CODE";

    /**
     * Payment Amount
     */
    public static final String BC_SPAY_API_PAYMENT_AMOUNT = "PAYMENT_AMOUNT";

    /**
     * CCV number
     */
    public static final String BC_SPAY_API_CARD_CVV2 = "CARD_CVV2";

    /**
     * CC number
     */
    public static final String BC_SPAY_API_CARD_NUMBER = "CARD_NUMBER";

    /**
     * CC Expiry Month
     */
    public static final String BC_SPAY_API_CARD_EXPIRY_MONTH = "CARD_EXPIRY_MONTH";

    /**
     * CC Expiry Year
     */
    public static final String BC_SPAY_API_CARD_EXPIRY_YEAR = "CARD_EXPIRY_YEAR";

    /**
     * Card holder's name
     */
    public static final String BC_SPAY_API_CARDHOLDERS_NAME = "CARDHOLDERS_NAME";

    /**
     * Customer's email address
     */
    public static final String BC_SPAY_API_CUST_EMAIL = "CUSTOMER_EMAIL";

    /**
     * Shopper's Reference
     */
    public static final String BC_SPAY_API_CUST_REFERENCE = "BC_SPAY_API_CUST_REFERENCE";

    /**
     * User Agent
     */
    public static final String BC_SPAY_API_USER_AGENT = "BC_SPAY_API_USER_AGENT";

    /**
     * Accept Header on request
     */
    public static final String BC_SPAY_API_ACCEPT = "BC_SPAY_API_ACCEPT";

    /**
     * SmartPay UserId for the HTTP Authentication
     */
    public final static String BC_SPAY_API_USER_ID = "BC_SPAY_API_USER_ID";

    /**
     * SmartPay Password for the HTTP Authentication
     */
    public final static String BC_SPAY_API_PASSWORD = "BC_SPAY_API_PASSWORD";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public BarclaycardSmartPayApi(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_REQUEST_URL must be set to the return URL for"
                            + " sending the request to BarclaycardSmartPayApi. (e.g. https://secure.barclaycardSmartPayApi.com/index_shop.cgi)");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_3D_STATUS);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_3D_STATUS must be set to true "
                            + "or false.  Set to true to enable the 3D secure check");
        }
        staticData.setCheck3dSecure(new Boolean(conf.getValue()).booleanValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_3D_RESPONSE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_3D_RESPONSE_URL must be set "
                            + "to the 3D Secure Response Url");
        }
        staticData.setResponseUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_MERCHANT_ACC);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_MERCHANT_ACC must be set to "
                            + "the Barclays SmartPay Merchant Account");
        }
        staticData.setMerchantAccount(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_USER_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_USER_ID must be set to "
                            + "the Barclays SmartPay User Id");
        }
        staticData.setSmartPayUserId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_PASSWORD must be set to "
                            + "the Barclays SmartPay Password");
        }
        staticData.setSmartPayPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_CALLBACK_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_CALLBACK_USERNAME must be set to the Callback Username for the"
                            + " Notification functionality.");
        }
        staticData.setCallbackUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_CALLBACK_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_CALLBACK_PASSWORD must be set to the Callback Username for the"
                            + " Notification functionality.");
        }
        staticData.setCallbackPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_HTTP_AUTH);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_HTTP_AUTH must be set to true or false for the"
                            + " Notification functionality.");
        }
        staticData.setHttpAuth(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_HTTP_USERNAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_HTTP_USERNAME must be set to the HTTP Username for the"
                            + " Notification functionality.");
        }
        staticData.setHttpUsername(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_HTTP_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_BC_SPAY_API_HTTP_PASSWORD must be set to the HTTP Password for the"
                            + " Notification functionality.");
        }
        staticData.setHttpPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_BC_SPAY_API_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Return a payment details object for BarclaycardSmartPayApi IPN module
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
         * The BarclaycardSmartPayApiZone zone, if greater than zero, should reference a GeoZone. If
         * the DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
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
        pDetails.setCode(BC_SPAY_API_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.SERVER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_BC_SPAY_API_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_BC_SPAY_API_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        // Pass some info back in the custom fields
        pDetails.setCustom1(sd.getSmartPayUserId());
        pDetails.setCustom2(sd.getSmartPayPassword());
        pDetails.setCustom3(sd.getMerchantAccount());
        pDetails.setCustom4(sd.getRequestUrl());

        // barclaycardSmartPayApi only requires details of the final price. No tax, sub-total etc.
        BigDecimal total = null;

        // null order totals?
        if (order.getOrderTotals() == null)
        {
            log.warn("Order " + order.getId() + " has no Order Totals");
        } else
        {
            for (int i = 0; i < order.getOrderTotals().length; i++)
            {
                OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
                if (ot.getClassName().equals(OrderTotalMgr.ot_total))
                {
                    total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
                }
            }
        }

        if (total == null)
        {
            throw new KKException("An Order Total was not found");
        }

        total = total.movePointRight(scale);
        total.setScale(0);

        /*
         * Create a session here which will be used by the IPN callback. Note that this login can be
         * commented out if not using the callback notification mechanism
         */
        String countryCode = order.getLocale().substring(0, 2);
        SSOTokenIf ssoToken = new SSOToken();
        String sessionId = getEng().login(sd.getCallbackUsername(), sd.getCallbackPassword());
        if (sessionId == null)
        {
            throw new KKException(
                    "Unable to log into the engine using the BarclaycardSmartPayApi Callback Username and Password");
        }
        ssoToken.setSessionId(sessionId);
        ssoToken.setCustom1(sd.getHttpAuth() + "~" + order.getId() + "~" + countryCode);
        ssoToken.setCustom2(sd.getHttpUsername());
        ssoToken.setCustom3(sd.getHttpPassword());

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
        pDetails.setCustom5(uuid);

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue(BC_SPAY_API_MERCHANT_ACCOUNT, sd.getMerchantAccount()));
        parmList.add(new NameValue(BC_SPAY_API_USER_ID, sd.getSmartPayUserId()));
        parmList.add(new NameValue(BC_SPAY_API_PASSWORD, sd.getSmartPayPassword()));
        parmList.add(new NameValue(BC_SPAY_API_3D_STATUS, sd.isCheck3dSecure() ? "true" : "false"));

        if (sd.isCheck3dSecure())
        {
            parmList.add(new NameValue(BC_SPAY_API_3D_RESPONSE_URL, sd.getResponseUrl()
                    .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        }

        parmList.add(new NameValue(BC_SPAY_API_CUST_EMAIL, order.getCustomerEmail()));
        parmList.add(new NameValue(BC_SPAY_API_PAYMENT_AMOUNT, total.toString()));
        parmList.add(new NameValue(BC_SPAY_API_CURRENCY_CODE, order.getCurrencyCode()));

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue("firstname", bNames[0]));
            parmList.add(new NameValue("lastname", bNames[1]));

            String custReference = order.getCustomerId() + "~";
            if (bNames[0] != null)
            {
                custReference += bNames[0];
            }
            if (bNames[1] != null)
            {
                custReference += " " + bNames[1];
            }
            parmList.add(new NameValue(BC_SPAY_API_CUST_REFERENCE, custReference));
        }

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(false);
        pDetails.setShowCVV(true);
        pDetails.setShowPostcode(false);
        pDetails.setShowType(true);
        pDetails.setShowOwner(true);

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
        return isAvailable(MODULE_PAYMENT_BC_SPAY_API_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // Merchant Account
        private String merchantAccount;

        // SmartPay user id
        private String smartPayUserId;

        // SmartPay user id
        private String smartPayPassword;

        // Callback called by Gateway after a 3D Secure transaction
        private String responseUrl;

        // The BarclaycardSmartPayApi Url used to POST the payment request.
        private String requestUrl;

        private int zone;

        private int orderStatusId;

        private boolean check3dSecure;

        private String callbackUsername;

        private String callbackPassword;

        private String httpAuth;

        private String httpUsername;

        private String httpPassword;

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
         * @return the merchantAccount
         */
        public String getMerchantAccount()
        {
            return merchantAccount;
        }

        /**
         * @param merchantAccount
         *            the merchantAccount to set
         */
        public void setMerchantAccount(String merchantAccount)
        {
            this.merchantAccount = merchantAccount;
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
         * @return the smartPayUserId
         */
        public String getSmartPayUserId()
        {
            return smartPayUserId;
        }

        /**
         * @param smartPayUserId
         *            the smartPayUserId to set
         */
        public void setSmartPayUserId(String smartPayUserId)
        {
            this.smartPayUserId = smartPayUserId;
        }

        /**
         * @return the smartPayPassword
         */
        public String getSmartPayPassword()
        {
            return smartPayPassword;
        }

        /**
         * @param smartPayPassword
         *            the smartPayPassword to set
         */
        public void setSmartPayPassword(String smartPayPassword)
        {
            this.smartPayPassword = smartPayPassword;
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
         * @return the check3dSecure
         */
        public boolean isCheck3dSecure()
        {
            return check3dSecure;
        }

        /**
         * @param check3dSecure
         *            the check3dSecure to set
         */
        public void setCheck3dSecure(boolean check3dSecure)
        {
            this.check3dSecure = check3dSecure;
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
         * @return the httpAuth
         */
        public String getHttpAuth()
        {
            return httpAuth;
        }

        /**
         * @param httpAuth
         *            the httpAuth to set
         */
        public void setHttpAuth(String httpAuth)
        {
            this.httpAuth = httpAuth;
        }

        /**
         * @return the httpUsername
         */
        public String getHttpUsername()
        {
            return httpUsername;
        }

        /**
         * @param httpUsername
         *            the httpUsername to set
         */
        public void setHttpUsername(String httpUsername)
        {
            this.httpUsername = httpUsername;
        }

        /**
         * @return the httpPassword
         */
        public String getHttpPassword()
        {
            return httpPassword;
        }

        /**
         * @param httpPassword
         *            the httpPassword to set
         */
        public void setHttpPassword(String httpPassword)
        {
            this.httpPassword = httpPassword;
        }
    }
}
