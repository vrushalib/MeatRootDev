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

package com.konakart.bl.modules.payment.cybersource;

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
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;
import com.konakart.util.Utils;

/**
 * CyberSource IPN module
 */
public class CyberSource extends BasePaymentModule implements PaymentInterface
{
    /**
     * Module name - make this the same name as this class
     */
    public static String CYBERSOURCE_GATEWAY_CODE = "CyberSource";

    private static String bundleName = BaseModule.basePackage + ".payment."
            + CYBERSOURCE_GATEWAY_CODE.toLowerCase() + "." + CYBERSOURCE_GATEWAY_CODE;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = CYBERSOURCE_GATEWAY_CODE + "Mutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_CYBERSOURCE_STATUS = "MODULE_PAYMENT_CYBERSOURCE_STATUS";

    /**
     * Used to set the operating environment
     */
    private final static String MODULE_PAYMENT_CYBERSOURCE_ENVIRONMENT = "MODULE_PAYMENT_CYBERSOURCE_ENVIRONMENT";

    /**
     * The CyberSource Zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_CYBERSOURCE_ZONE = "MODULE_PAYMENT_CYBERSOURCE_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_CYBERSOURCE_SORT_ORDER = "MODULE_PAYMENT_CYBERSOURCE_SORT_ORDER";

    /**
     * The CyberSource Url used to POST the payment request.
     * "https://pal-test.barclaycardsmartpay.com/pal/servlet/soap/Payment"
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_REQUEST_URL = "MODULE_PAYMENT_CYBERSOURCE_REQUEST_URL";

    /**
     * This URL is used by the CyberSource IPN functionality to call back into the application with
     * the results of the transaction. It must be a URL that is visible from the internet. If it is
     * in the form http://host:port/konakart/CyberSourceResponse.do, then the string host:port is
     * substituted automatically with the correct value.
     */
    private final static String MODULE_PAYMENT_CYBERSOURCE_RESPONSE_URL = "MODULE_PAYMENT_CYBERSOURCE_RESPONSE_URL";

    /**
     * Merchant Account
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_MERCHANT_ACC = "MODULE_PAYMENT_CYBERSOURCE_MERCHANT_ACC";

    /**
     * Username for accessing the SmartPay API
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_USER_ID = "MODULE_PAYMENT_CYBERSOURCE_USER_ID";

    /**
     * Password for accessing the SmartPay API
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_PASSWORD = "MODULE_PAYMENT_CYBERSOURCE_PASSWORD";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_CYBERSOURCE_TEXT_TITLE = "module.payment.cybersource.text.title";

    private final static String MODULE_PAYMENT_CYBERSOURCE_TEXT_DESCRIPTION = "module.payment.cybersource.text.description";

    /**
     * Shared Secret
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_SHARED_SECRET = "MODULE_PAYMENT_CYBERSOURCE_SHARED_SECRET";

    /**
     * Serial Number
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_SERIAL_NUMBER = "MODULE_PAYMENT_CYBERSOURCE_SERIAL_NUMBER";

    /**
     * Gateway Version Number
     */
    public final static String MODULE_PAYMENT_CYBERSOURCE_VERSION = "MODULE_PAYMENT_CYBERSOURCE_VERSION";

    /**
     * Merchant Account
     */
    public static final String CYBERSOURCE_MERCHANT_ACCOUNT = "merchantID";

    /**
     * Merchant Reference
     */
    public static final String CYBERSOURCE_MERCHANT_REF = "MERCHANT_REF";

    /**
     * Payment Amount
     */
    public static final String CYBERSOURCE_PAYMENT_AMOUNT = "amount";

    /**
     * Card holder's name
     */
    public static final String CYBERSOURCE_CARDHOLDERS_NAME = "CARDHOLDERS_NAME";

    /**
     * Customer's email address
     */
    public static final String CYBERSOURCE_CUST_EMAIL = "CUSTOMER_EMAIL";

    /**
     * Shopper's Reference
     */
    public static final String CYBERSOURCE_CUST_REFERENCE = "CUST_REFERENCE";

    /**
     * CyberSource Receipt Response URL
     */
    public final static String CYBERSOURCE_RESPONSE_RECEIPT_URL = "orderPage_receiptResponseURL";

    /**
     * CyberSource Decline Response URL
     */
    public final static String CYBERSOURCE_RESPONSE_DECLINE_URL = "orderPage_declineResponseURL";

    /**
     * Shared Secret
     */
    public final static String CYBERSOURCE_SHARED_SECRET = "SHARED_SECRET";

    /**
     * Environment in response
     */
    public final static String CYBERSOURCE_ENVIRONMENT = "orderPage_environment";
    
    /**
     * Serial Number
     */
    public final static String CYBERSOURCE_SERIAL_NUMBER = "orderPage_serialNumber";

    /**
     * Gateway Version Number
     */
    public final static String CYBERSOURCE_VERSION_NUMBER = "orderPage_version";

    /**
     * Currency
     */
    public final static String CYBERSOURCE_CURRENCY = "currency";

    /**
     * Bill To Company
     */
    public final static String CYBERSOURCE_BILLTO_COMP = "billTo_company";

    /**
     * Bill To First Name
     */
    public final static String CYBERSOURCE_BILLTO_FNAME = "billTo_firstName";

    /**
     * Bill To Last Name
     */
    public final static String CYBERSOURCE_BILLTO_LNAME = "billTo_lastName";

    /**
     * Bill To Street 1
     */
    public final static String CYBERSOURCE_BILLTO_STREET1 = "billTo_street1";

    /**
     * Bill To City
     */
    public final static String CYBERSOURCE_BILLTO_CITY = "billTo_city";

    /**
     * Bill To State
     */
    public final static String CYBERSOURCE_BILLTO_STATE = "billTo_state";

    /**
     * Bill To Postal Code
     */
    public final static String CYBERSOURCE_BILLTO_POSTCODE = "billTo_postalCode";

    /**
     * Bill To Country
     */
    public final static String CYBERSOURCE_BILLTO_COUNTRY = "billTo_country";

    /**
     * Bill To Phone Number
     */
    public final static String CYBERSOURCE_BILLTO_PHONE = "billTo_phoneNumber";

    /**
     * Bill To Email
     */
    public final static String CYBERSOURCE_BILLTO_EMAIL = "billTo_email";

    /**
     * Ship To Company
     */
    public final static String CYBERSOURCE_SHIPTO_COMP = "shipTo_company";

    /**
     * Ship To First Name
     */
    public final static String CYBERSOURCE_SHIPTO_FNAME = "shipTo_firstName";

    /**
     * Ship To Last Name
     */
    public final static String CYBERSOURCE_SHIPTO_LNAME = "shipTo_lastName";

    /**
     * Ship To Street 1
     */
    public final static String CYBERSOURCE_SHIPTO_STREET1 = "shipTo_street1";

    /**
     * Ship To City
     */
    public final static String CYBERSOURCE_SHIPTO_CITY = "shipTo_city";

    /**
     * Ship To State
     */
    public final static String CYBERSOURCE_SHIPTO_STATE = "shipTo_state";

    /**
     * Ship To Postal Code
     */
    public final static String CYBERSOURCE_SHIPTO_POSTCODE = "shipTo_postalCode";

    /**
     * Ship To Country
     */
    public final static String CYBERSOURCE_SHIPTO_COUNTRY = "shipTo_country";

    /**
     * Credit Card Type
     */
    public final static String CYBERSOURCE_CARD_TYPE = "card_cardType";

    /**
     * Credit Card Expiry Month
     */
    public final static String CYBERSOURCE_CARD_EXP_MONTH = "card_expirationMonth";

    /**
     * Credit Card Expiry Year
     */
    public final static String CYBERSOURCE_CARD_EXP_YEAR = "card_expirationYear";

    /**
     * Credit Card Account Number
     */
    public final static String CYBERSOURCE_CARD_NUMBER = "card_accountNumber";

    /**
     * Credit Card CCV number
     */
    public final static String CYBERSOURCE_CARD_CCV = "card_cvNumber";

    /**
     * Credit Card Owner
     */
    public final static String CYBERSOURCE_CARD_OWNER = "card_owner";

    /**
     * Transaction Type
     */
    public final static String CYBERSOURCE_TRAN_TYPE = "orderPage_transactionType";

    /**
     * Decline URL
     */
    public final static String CYBERSOURCE_DECLINE_URL = "orderPage_declineResponseURL";

    /**
     * Receipt URL
     */
    public final static String CYBERSOURCE_RECEIPT_URL = "orderPage_receiptResponseURL";

    /**
     * Ignore AVS check
     */
    public final static String CYBERSOURCE_IGNORE_AVS = "orderPage_ignoreAVS";

    /**
     * Signature
     */
    public final static String CYBERSOURCE_SIGNATURE = "orderPage_signaturePublic";

    /**
     * Timestamp
     */
    public final static String CYBERSOURCE_TIMESTAMP = "orderPage_timestamp";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public CyberSource(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_ENVIRONMENT);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_ENVIRONMENT is not set.  It must"
                            + " be set to the either TEST or PRODUCTION");
        } else if (!(conf.getValue().equals("TEST") || conf.getValue().equals("PRODUCTION")))
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_ENVIRONMENT (currently set to "
                            + conf.getValue() + ") must"
                            + " be set to the either TEST or PRODUCTION");
        }
        staticData.setEnvironment(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_REQUEST_URL must be set to the URL for"
                            + " sending the request to CyberSource.");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_RESPONSE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_RESPONSE_URL must be set "
                            + "to the Response URL");
        }
        staticData.setResponseUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_MERCHANT_ACC);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_MERCHANT_ACC must be set to "
                            + "the CyberSource Merchant Account");
        }
        staticData.setMerchantAccount(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_SHARED_SECRET);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_SHARED_SECRET must be set to "
                            + "the CyberSource Shared Secret");
        }
        staticData.setSharedSecret(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_SERIAL_NUMBER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_SERIAL_NUMBER must be set to "
                            + "the CyberSource Serial Number");
        }
        staticData.setSerialNumber(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_VERSION);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCE_VERSION must be set to "
                            + "the CyberSource Gateway Version Number");
        }
        staticData.setGatewayVersion(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCE_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Return a payment details object for CyberSource IPN module
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
         * The CyberSourceZone zone, if greater than zero, should reference a GeoZone. If the
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
        pDetails.setCode(CYBERSOURCE_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setPaymentType(PaymentDetails.SERVER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_CYBERSOURCE_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_CYBERSOURCE_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        // This gateway only requires details of the final price. No tax, sub-total etc.
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

        // total = total.movePointRight(scale);
        // total.setScale(0);

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue(CYBERSOURCE_MERCHANT_ACCOUNT, sd.getMerchantAccount()));
        parmList.add(new NameValue(CYBERSOURCE_SERIAL_NUMBER, sd.getSerialNumber()));
        parmList.add(new NameValue(CYBERSOURCE_VERSION_NUMBER, sd.getGatewayVersion()));

        // Put the shared secret on custom1 for security
        pDetails.setCustom1(sd.getSharedSecret());
        
        // Put the environment on custom2
        pDetails.setCustom2(sd.getEnvironment());

        // parmList.add(new NameValue(CYBERSOURCE_3D_STATUS, sd.isCheck3dSecure() ? "true" :
        // "false"));

        // if (sd.isCheck3dSecure())
        // {
        // parmList.add(new NameValue(CYBERSOURCE_3D_RESPONSE_URL, sd.getResponseUrl()
        // .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        // }

        parmList.add(new NameValue(CYBERSOURCE_RESPONSE_RECEIPT_URL, sd.getResponseUrl()
                .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        parmList.add(new NameValue(CYBERSOURCE_RESPONSE_DECLINE_URL, sd.getResponseUrl()
                .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        parmList.add(new NameValue(CYBERSOURCE_CUST_EMAIL, order.getCustomerEmail()));

        // parmList.add(new NameValue("product_id", sd.getProductId()));
        // parmList.add(new NameValue("product_name", "Order #" + order.getId() + " from "
        // + info.getStoreName()));

        parmList.add(new NameValue(CYBERSOURCE_PAYMENT_AMOUNT, total.toString()));
        parmList.add(new NameValue(CYBERSOURCE_CURRENCY, order.getCurrencyCode()));

        // parmList.add(new NameValue("detail1_description", "Description"));
        // parmList.add(new NameValue("detail1_text", "Goods/Services"));

        // general parameters

        // parmList.add(new NameValue("recipient_description", "CyberSource"));
        // parmList.add(new NameValue("transaction_id", order.getOrderNumber()));
        // parmList.add(new NameValue("language", "EN"));
        // parmList.add(new NameValue("hide_login", "1"));
        // parmList.add(new NameValue("pay_from_email", order.getCustomerEmail()));

        // Normal Callback
        // sd.setCallbackUrl(sd.getCallbackUrl()
        // .replaceFirst(hostPortSubstitute, info.getHostAndPort()));
        // parmList.add(new NameValue("status_url", sd.getCallbackUrl()));

        // Call back if payment is declined
        // sd.setDeclineUrl(sd.getDeclineUrl()
        // .replaceFirst(hostPortSubstitute, info.getHostAndPort()));
        // parmList.add(new NameValue("cancel_url", sd.getDeclineUrl()));

        // Call back if payment is accepted
        // sd.setAcceptUrl(sd.getAcceptUrl().replaceFirst(
        // hostPortSubstitute, info.getHostAndPort()));
        // parmList.add(new NameValue("return_url", sd.getAcceptUrl()));

        // parmList.add(new NameValue("merchant_fields", "orderId, platform"));
        // parmList.add(new NameValue("platform", "konakart"));
        // parmList.add(new NameValue("orderId", order.getId()));

        // ---------------------------------------------------------------------------------------
        // Set the billing details

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue(CYBERSOURCE_BILLTO_FNAME, bNames[0]));
            parmList.add(new NameValue(CYBERSOURCE_BILLTO_LNAME, bNames[1]));
        }
        
        if (!Utils.isBlank(order.getBillingCompany()))
        {
            parmList.add(new NameValue(CYBERSOURCE_BILLTO_COMP, order.getBillingCompany()));
        }

        parmList.add(new NameValue(CYBERSOURCE_BILLTO_STREET1, order.getBillingStreetAddress()));
        parmList.add(new NameValue(CYBERSOURCE_BILLTO_CITY, order.getBillingCity()));
        parmList.add(new NameValue(CYBERSOURCE_BILLTO_STATE, order.getBillingState()));
        parmList.add(new NameValue(CYBERSOURCE_BILLTO_COUNTRY, order.getBillingCountry()));
        parmList.add(new NameValue(CYBERSOURCE_BILLTO_POSTCODE, order.getBillingPostcode()));
        parmList.add(new NameValue(CYBERSOURCE_BILLTO_PHONE, order.getBillingTelephone()));
        parmList.add(new NameValue(CYBERSOURCE_BILLTO_EMAIL, order.getCustomerEmail()));

        // Country requires the three letter country code
        // CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        // if (country != null)
        // {
        // parmList.add(new NameValue("country", country.getIsoCode3()));
        // }

        // ---------------------------------------------------------------------------------------
        // Set the delivery details

        // Set the delivery names from the delivery address Id

        String[] dNames = getFirstAndLastNamesFromAddress(order.getDeliveryAddrId());
        if (dNames != null)
        {
            parmList.add(new NameValue(CYBERSOURCE_SHIPTO_FNAME, dNames[0]));
            parmList.add(new NameValue(CYBERSOURCE_SHIPTO_LNAME, dNames[1]));
        }

        if (!Utils.isBlank(order.getDeliveryCompany()))
        {
            parmList.add(new NameValue(CYBERSOURCE_SHIPTO_COMP, order.getDeliveryCompany()));
        }

        parmList.add(new NameValue(CYBERSOURCE_SHIPTO_STREET1, order.getDeliveryStreetAddress()));
        parmList.add(new NameValue(CYBERSOURCE_SHIPTO_CITY, order.getDeliveryCity()));
        parmList.add(new NameValue(CYBERSOURCE_SHIPTO_STATE, order.getDeliveryState()));
        parmList.add(new NameValue(CYBERSOURCE_SHIPTO_COUNTRY, order.getDeliveryCountry()));
        parmList.add(new NameValue(CYBERSOURCE_SHIPTO_POSTCODE, order.getDeliveryPostcode()));

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
        return isAvailable(MODULE_PAYMENT_CYBERSOURCE_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // Merchant Account
        private String merchantAccount;

        // Shared Secret
        private String sharedSecret;

        // Serial Number
        private String serialNumber;

        // Callback called by Gateway after a 3D Secure transaction
        private String responseUrl;

        // The CyberSource Url used to POST the payment request.
        private String requestUrl;

        private int zone;

        private String gatewayVersion;

        private String environment;

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
         * @return the serialNumber
         */
        public String getSerialNumber()
        {
            return serialNumber;
        }

        /**
         * @param serialNumber
         *            the serialNumber to set
         */
        public void setSerialNumber(String serialNumber)
        {
            this.serialNumber = serialNumber;
        }

        /**
         * @return the gatewayVersion
         */
        public String getGatewayVersion()
        {
            return gatewayVersion;
        }

        /**
         * @param gatewayVersion
         *            the gatewayVersion to set
         */
        public void setGatewayVersion(String gatewayVersion)
        {
            this.gatewayVersion = gatewayVersion;
        }

        /**
         * @return the environment
         */
        public String getEnvironment()
        {
            return environment;
        }

        /**
         * @param environment
         *            the environment to set
         */
        public void setEnvironment(String environment)
        {
            this.environment = environment;
        }

    }
}
