//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.bl.modules.payment.cybersourcesa;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.StoreIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;
import com.konakart.bl.modules.payment.cybersourcesa.CyberSourceSAHMACTools;
import com.konakart.util.Utils;

/**
 * CyberSource Secure Acceptance module
 */
public class CyberSourceSA extends BasePaymentModule implements PaymentInterface
{
    /**
     * Module name - make this the same name as this class
     */
    public static String CYBERSOURCESA_GATEWAY_CODE = "CyberSourceSA";

    private static String bundleName = BaseModule.basePackage + ".payment."
            + CYBERSOURCESA_GATEWAY_CODE.toLowerCase() + "." + CYBERSOURCESA_GATEWAY_CODE;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = CYBERSOURCESA_GATEWAY_CODE + "Mutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_CYBERSOURCESA_STATUS = "MODULE_PAYMENT_CYBERSOURCESA_STATUS";

    /**
     * Used to set the operating environment
     */
    private final static String MODULE_PAYMENT_CYBERSOURCESA_ENVIRONMENT = "MODULE_PAYMENT_CYBERSOURCESA_ENVIRONMENT";

    /**
     * The CyberSource Zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_CYBERSOURCESA_ZONE = "MODULE_PAYMENT_CYBERSOURCESA_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_CYBERSOURCESA_SORT_ORDER = "MODULE_PAYMENT_CYBERSOURCESA_SORT_ORDER";

    /**
     * The CyberSource Url used to POST the payment request.
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_REQUEST_URL = "MODULE_PAYMENT_CYBERSOURCESA_REQUEST_URL";

    /**
     * This URL is used by the CyberSource IPN functionality to call back into the application with
     * the results of the transaction. It must be a URL that is visible from the internet. If it is
     * in the form http://host:port/konakart/CyberSourceResponseSA.do, then the string host:port is
     * substituted automatically with the correct value.
     */
    private final static String MODULE_PAYMENT_CYBERSOURCESA_RESPONSE_URL = "MODULE_PAYMENT_CYBERSOURCESA_RESPONSE_URL";

    /**
     * Merchant Account
     */
    // public final static String MODULE_PAYMENT_CYBERSOURCESA_MERCHANT_ACC =
    // "MODULE_PAYMENT_CYBERSOURCESA_MERCHANT_ACC";

    /**
     * Merchant Profile Id
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_PROFILE_ID = "MODULE_PAYMENT_CYBERSOURCESA_PROFILE_ID";

    /**
     * Username for accessing the CyberSource SA API
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_USER_ID = "MODULE_PAYMENT_CYBERSOURCESA_USER_ID";

    /**
     * Password for accessing the CyberSource SA API
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_PASSWORD = "MODULE_PAYMENT_CYBERSOURCESA_PASSWORD";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_CYBERSOURCESA_TEXT_TITLE = "module.payment.cybersourcesa.text.title";

    private final static String MODULE_PAYMENT_CYBERSOURCESA_TEXT_DESCRIPTION = "module.payment.cybersourcesa.text.description";

    /**
     * Shared Secret Part 1
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET1 = "MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET1";

    /**
     * Shared Secret Part 2
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET2 = "MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET2";

    /**
     * Serial Number
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_ACCESS_KEY = "MODULE_PAYMENT_CYBERSOURCESA_ACCESS_KEY";

    /**
     * Gateway Version Number
     */
    public final static String MODULE_PAYMENT_CYBERSOURCESA_VERSION = "MODULE_PAYMENT_CYBERSOURCESA_VERSION";

    /**
     * Access Key
     */
    public final static String CYBERSOURCESA_ACCESS_KEY = "access_key";

    /**
     * Allow Updates
     */
    // public final static String CYBERSOURCESA_ALLOW_UPDATES = "allow_payment_token_update";

    /**
     * Amount
     */
    public final static String CYBERSOURCESA_AMOUNT = "amount";

    /**
     * Bill To Company
     */
    public final static String CYBERSOURCESA_BILLTO_COMP = "bill_to_company_name";

    /**
     * Bill To First Name
     */
    public final static String CYBERSOURCESA_BILLTO_FNAME = "bill_to_forename";

    /**
     * Bill To Last Name
     */
    public final static String CYBERSOURCESA_BILLTO_LNAME = "bill_to_surname";

    /**
     * Bill To Street 1
     */
    public final static String CYBERSOURCESA_BILLTO_STREET1 = "bill_to_address_line1";

    /**
     * Bill To City
     */
    public final static String CYBERSOURCESA_BILLTO_CITY = "bill_to_address_city";

    /**
     * Bill To State
     */
    public final static String CYBERSOURCESA_BILLTO_STATE = "bill_to_address_state";

    /**
     * Bill To Postal Code
     */
    public final static String CYBERSOURCESA_BILLTO_POSTCODE = "bill_to_address_postal_code";

    /**
     * Bill To Country
     */
    public final static String CYBERSOURCESA_BILLTO_COUNTRY = "bill_to_address_country";

    /**
     * Bill To Phone Number
     */
    public final static String CYBERSOURCESA_BILLTO_PHONE = "bill_to_phone";

    /**
     * Bill To Email
     */
    public final static String CYBERSOURCESA_BILLTO_EMAIL = "bill_to_email";

    /**
     * Credit Card Type
     */
    public final static String CYBERSOURCESA_CARD_TYPE = "card_cardType";

    /**
     * Credit Card Expiry Month
     */
    public final static String CYBERSOURCESA_CARD_EXP_MONTH = "card_expirationMonth";

    /**
     * Credit Card Expiry Year
     */
    public final static String CYBERSOURCESA_CARD_EXP_YEAR = "card_expirationYear";

    /**
     * Credit Card Account Number
     */
    public final static String CYBERSOURCESA_CARD_NUMBER = "card_accountNumber";

    /**
     * Credit Card CCV number
     */
    public final static String CYBERSOURCESA_CARD_CCV = "card_cvNumber";

    /**
     * Credit Card Owner
     */
    public final static String CYBERSOURCESA_CARD_OWNER = "card_owner";

    /**
     * Card holder's name
     */
    public static final String CYBERSOURCESA_CARDHOLDERS_NAME = "CARDHOLDERS_NAME";

    /**
     * Currency
     */
    public final static String CYBERSOURCESA_CURRENCY = "currency";

    /**
     * Customer's email address
     */
    // public static final String CYBERSOURCESA_CUST_EMAIL = "CUSTOMER_EMAIL";

    /**
     * Shopper's Reference
     */
    public static final String CYBERSOURCESA_CUST_REFERENCE = "CUST_REFERENCE";

    /**
     * Decline URL
     */
    //public final static String CYBERSOURCESA_DECLINE_URL = "orderPage_declineResponseURL";

    /**
     * Environment in response
     */
    //public final static String CYBERSOURCESA_ENVIRONMENT = "orderPage_environment";

    /**
     * Ignore AVS check
     */
    public final static String CYBERSOURCESA_IGNORE_AVS = "ignore_avs";

    /**
     * Locale in format "en-gb"
     */
    public final static String CYBERSOURCESA_LOCALE = "locale";

    /**
     * Merchant Account
     */
    // public static final String CYBERSOURCESA_MERCHANT_ACCOUNT = "merchantID";

    /**
     * Merchant Data 1
     */
    public static final String CYBERSOURCESA_MERCHANT_DATA1 = "merchant_defined_data1";

    /**
     * Merchant Data 2
     */
    public static final String CYBERSOURCESA_MERCHANT_DATA2 = "merchant_defined_data2";

    /**
     * Merchant Profile Id
     */
    public static final String CYBERSOURCESA_PROFILE_ID = "profile_id";

    /**
     * Payment Amount
     */
    public static final String CYBERSOURCESA_PAYMENT_AMOUNT = "amount";

    /**
     * CyberSource Receipt Response URL
     */
    public final static String CYBERSOURCESA_RESPONSE_RECEIPT_URL = "override_custom_receipt_page";

    /**
     * CyberSource Decline Response URL
     */
    // public final static String CYBERSOURCESA_RESPONSE_DECLINE_URL =
    // "orderPage_declineResponseURL";

    /**
     * Receipt URL
     */
    // public final static String CYBERSOURCESA_RECEIPT_URL = "override_custom_receipt_page";

    /**
     * Reference
     */
    public final static String CYBERSOURCESA_REFERENCE_NUM = "reference_number";

    /**
     * Shared Secret
     */
    public final static String CYBERSOURCESA_SHARED_SECRET = "SHARED_SECRET";

    /**
     * Ship To Company
     */
    public final static String CYBERSOURCESA_SHIPTO_COMP = "ship_to_company";

    /**
     * Ship To First Name
     */
    public final static String CYBERSOURCESA_SHIPTO_FNAME = "ship_to_foreName";

    /**
     * Ship To Last Name
     */
    public final static String CYBERSOURCESA_SHIPTO_LNAME = "ship_to_surname";

    /**
     * Ship To Street 1
     */
    public final static String CYBERSOURCESA_SHIPTO_STREET1 = "ship_to_address_line1";

    /**
     * Ship To City
     */
    public final static String CYBERSOURCESA_SHIPTO_CITY = "ship_to_address_city";

    /**
     * Ship To State
     */
    public final static String CYBERSOURCESA_SHIPTO_STATE = "ship_to_address_state";

    /**
     * Ship To Postal Code
     */
    public final static String CYBERSOURCESA_SHIPTO_POSTCODE = "ship_to_address_postal_code";

    /**
     * Ship To Country
     */
    public final static String CYBERSOURCESA_SHIPTO_COUNTRY = "ship_to_address_country";

    /**
     * Signature
     */
    public final static String CYBERSOURCESA_SIGNATURE = "signature";

    /**
     * signed_date_time
     */
    public final static String CYBERSOURCESA_SIGNED_DATE_TIME = "signed_date_time";

    /**
     * signed_field_names
     */
    public final static String CYBERSOURCESA_SIGNED_FIELD_NAMES = "signed_field_names";

    /**
     * Transaction Type
     */
    public final static String CYBERSOURCESA_TRAN_TYPE = "transaction_type";

    /**
     * Transaction UUID
     */
    public final static String CYBERSOURCESA_TRAN_UUID = "transaction_uuid";

    /**
     * Timestamp
     */
    // public final static String CYBERSOURCESA_TIMESTAMP = "orderPage_timestamp";

    /**
     * unsigned fields
     */
    public final static String[] CYBERSOURCESA_UNSIGNED_FIELDS = {};

    /**
     * Gateway Version Number
     */
    // public final static String CYBERSOURCESA_VERSION_NUMBER = "orderPage_version";

    /**
     * unsigned_field_names
     */
    public final static String CYBERSOURCESA_UNSIGNED_FIELD_NAMES = "unsigned_field_names";

    /**
     * signed fields
     */
    public final static String[] CYBERSOURCESA_SIGNED_FIELDS =
    { CYBERSOURCESA_ACCESS_KEY, CYBERSOURCESA_PROFILE_ID, CYBERSOURCESA_TRAN_UUID,
            CYBERSOURCESA_SIGNED_FIELD_NAMES, CYBERSOURCESA_UNSIGNED_FIELD_NAMES,
            CYBERSOURCESA_SIGNED_DATE_TIME, CYBERSOURCESA_LOCALE, CYBERSOURCESA_TRAN_TYPE,
            CYBERSOURCESA_REFERENCE_NUM, CYBERSOURCESA_AMOUNT, CYBERSOURCESA_CURRENCY,
            CYBERSOURCESA_RESPONSE_RECEIPT_URL, CYBERSOURCESA_IGNORE_AVS };

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public CyberSourceSA(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_ENVIRONMENT);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_ENVIRONMENT is not set.  It must"
                            + " be set to the either TEST or PRODUCTION");
        } else if (!(conf.getValue().equals("TEST") || conf.getValue().equals("PRODUCTION")))
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_ENVIRONMENT (currently set to "
                            + conf.getValue() + ") must"
                            + " be set to the either TEST or PRODUCTION");
        }
        staticData.setEnvironment(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_REQUEST_URL must be set to the URL for"
                            + " sending the request to CyberSource.");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_RESPONSE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_RESPONSE_URL must be set "
                            + "to the Response Url");
        }
        staticData.setResponseUrl(conf.getValue());

        // conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_MERCHANT_ACC);
        // if (conf == null)
        // {
        // throw new KKException(
        // "The Configuration MODULE_PAYMENT_CYBERSOURCESA_MERCHANT_ACC must be set to "
        // + "the CyberSource Merchant Account");
        // }
        // staticData.setMerchantAccount(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_PROFILE_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_PROFILE_ID must be set to "
                            + "the CyberSource Merchant Profile Id");
        }
        staticData.setProfileId(conf.getValue());

        KKConfiguration conf1 = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET1);
        if (conf1 == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET1 must be set to "
                            + "the CyberSource Shared Secret - Part 1");
        }

        KKConfiguration conf2 = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET2);

        if (conf2 != null && conf2.getValue() != null)
        {
            // If both Part 1 and Part 2 are defined
            staticData.setSharedSecret(conf1.getValue() + conf2.getValue());
        } else
        {
            // If only Part 1 is defined
            staticData.setSharedSecret(conf1.getValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_ACCESS_KEY);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_ACCESS_KEY must be set to "
                            + "the CyberSource Access Key");
        }
        staticData.setAccessKey(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_VERSION);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CYBERSOURCESA_VERSION must be set to "
                            + "the CyberSource Gateway Version Number");
        }
        staticData.setGatewayVersion(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_CYBERSOURCESA_SORT_ORDER);
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
        pDetails.setCode(CYBERSOURCESA_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setPaymentType(PaymentDetails.BROWSER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_CYBERSOURCESA_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_CYBERSOURCESA_TEXT_TITLE));

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

        List<NameValueIf> parmList = new ArrayList<NameValueIf>();

        // parmList.add(new NameValue(CYBERSOURCESA_MERCHANT_ACCOUNT, sd.getMerchantAccount()));
        parmList.add(new NameValue(CYBERSOURCESA_ACCESS_KEY, sd.getAccessKey()));
        parmList.add(new NameValue(CYBERSOURCESA_PROFILE_ID, sd.getProfileId()));
        // parmList.add(new NameValue(CYBERSOURCESA_VERSION_NUMBER, sd.getGatewayVersion()));
        // parmList.add(new NameValue(CYBERSOURCESA_ALLOW_UPDATES, "false"));
        parmList.add(new NameValue(CYBERSOURCESA_SIGNED_DATE_TIME, getUTCDateTime()));

        Set<String> signedFieldsSet = new TreeSet<String>();
        String signedFieldNames = null;
        for (String fld : CYBERSOURCESA_SIGNED_FIELDS)
        {
            signedFieldsSet.add(fld);
            if (signedFieldNames == null)
            {
                signedFieldNames = fld;
            } else
            {
                signedFieldNames += "," + fld;
            }
        }
        parmList.add(new NameValue(CYBERSOURCESA_SIGNED_FIELD_NAMES, signedFieldNames));

        parmList.add(new NameValue(CYBERSOURCESA_REFERENCE_NUM, order.getLifecycleId()));
        parmList.add(new NameValue(CYBERSOURCESA_TRAN_UUID, order.getLifecycleId()));

        // Save the shared secret on custom1
        pDetails.setCustom1(sd.getSharedSecret());

        // Put the environment on custom2
        pDetails.setCustom2(sd.getEnvironment());

        // parmList.add(new NameValue(CYBERSOURCESA_3D_STATUS, sd.isCheck3dSecure() ? "true" :
        // "false"));

        // if (sd.isCheck3dSecure())
        // {
        // parmList.add(new NameValue(CYBERSOURCESA_3D_RESPONSE_URL, sd.getResponseUrl()
        // .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        // }

        parmList.add(new NameValue(CYBERSOURCESA_RESPONSE_RECEIPT_URL, sd.getResponseUrl()
                .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        // parmList.add(new NameValue(CYBERSOURCESA_RESPONSE_DECLINE_URL, sd.getResponseUrl()
        // .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        // parmList.add(new NameValue(CYBERSOURCESA_CUST_EMAIL, order.getCustomerEmail()));

        // parmList.add(new NameValue("product_id", sd.getProductId()));
        // parmList.add(new NameValue("product_name", "Order #" + order.getId() + " from "
        // + info.getStoreName()));

        parmList.add(new NameValue(CYBERSOURCESA_PAYMENT_AMOUNT, total.toString()));
        parmList.add(new NameValue(CYBERSOURCESA_CURRENCY, order.getCurrencyCode()));

        if (!Utils.isBlank(info.getLocale().getVariant()))
        {
            parmList.add(new NameValue(CYBERSOURCESA_LOCALE, info.getLocale().getLanguage() + "-"
                    + info.getLocale().getVariant()));
        } else
        {
            parmList.add(new NameValue(CYBERSOURCESA_LOCALE, info.getLocale().getLanguage()));
        }

        // ---------------------------------------------------------------------------------------
        // Set the billing details

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue(CYBERSOURCESA_BILLTO_FNAME, bNames[0]));
            parmList.add(new NameValue(CYBERSOURCESA_BILLTO_LNAME, bNames[1]));
        }

        if (!Utils.isBlank(order.getBillingCompany()))
        {
            parmList.add(new NameValue(CYBERSOURCESA_BILLTO_COMP, order.getBillingCompany()));
        }

        parmList.add(new NameValue(CYBERSOURCESA_BILLTO_STREET1, order.getBillingStreetAddress()));
        parmList.add(new NameValue(CYBERSOURCESA_BILLTO_CITY, order.getBillingCity()));
        parmList.add(new NameValue(CYBERSOURCESA_BILLTO_STATE, getZoneCodeForZoneName(order
                .getBillingState())));
        parmList.add(new NameValue(CYBERSOURCESA_BILLTO_POSTCODE, order.getBillingPostcode()));

        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue(CYBERSOURCESA_BILLTO_COUNTRY, country.getIsoCode2()));
        }

        if (!Utils.isBlank(order.getBillingTelephone()))
        {
            parmList.add(new NameValue(CYBERSOURCESA_BILLTO_PHONE, order.getBillingTelephone()));
        }

        parmList.add(new NameValue(CYBERSOURCESA_BILLTO_EMAIL, order.getCustomerEmail()));

        // ---------------------------------------------------------------------------------------
        // Set the delivery details

        // Set the delivery names from the delivery address Id

        String[] dNames = getFirstAndLastNamesFromAddress(order.getDeliveryAddrId());
        if (dNames != null)
        {
            parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_FNAME, dNames[0]));
            parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_LNAME, dNames[1]));
        }

        if (!Utils.isBlank(order.getDeliveryCompany()))
        {
            parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_COMP, order.getDeliveryCompany()));
        }

        parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_STREET1, order.getDeliveryStreetAddress()));
        parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_CITY, order.getDeliveryCity()));
        parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_STATE, getZoneCodeForZoneName(order
                .getDeliveryState())));
        parmList.add(new NameValue(CYBERSOURCESA_SHIPTO_POSTCODE, order.getDeliveryPostcode()));

        country = getEng().getCountryPerName(order.getDeliveryCountry());
        if (country != null)
        {
            parmList.add(new NameValue(CYBERSOURCESA_BILLTO_COUNTRY, country.getIsoCode2()));
        }

        // Set the fields that should be visible in the UI when gathering Credit Card details
        // pDetails.setShowAddr(false);
        // pDetails.setShowCVV(true);
        // pDetails.setShowPostcode(false);
        // pDetails.setShowType(true);
        // pDetails.setShowOwner(true);

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

        if (log.isDebugEnabled())
        {
            log.debug("Used to create merchant defined data:          \n"
                    + "    OrderId              = "
                    + order.getId()
                    + "\n"
                    + "    OrderNumber          = "
                    + order.getOrderNumber()
                    + "\n"
                    + "    StoreId              = "
                    + storeId
                    + "\n"
                    + "    EngineMode           = "
                    + engineMode
                    + "\n"
                    + "    CustomersShared      = "
                    + customersShared
                    + "\n"
                    + "    ProductsShared       = "
                    + productsShared
                    + "\n"
                    + "    CategoriesShared     = "
                    + categoriesShared
                    + "\n"
                    + "    CountryCode          = " + countryCode);
        }

        String merchantReference = order.getId() + "~" + order.getOrderNumber() + "~" + storeId
                + "~" + engineMode + "~" + customersShared + "~" + productsShared + "~"
                + categoriesShared + "~" + countryCode;

        parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_MERCHANT_DATA1, merchantReference));
        parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_MERCHANT_DATA2, sd.getEnvironment()));

        parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_TRAN_TYPE, "sale"));
        parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_IGNORE_AVS, "true"));

        // String time = String.valueOf(System.currentTimeMillis());

        // parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_TIMESTAMP, time));

        // Calculate the signature
        HashMap<String, String> hp = hashParameters(pDetails, parmList);

        // Now add the list of unsigned fields that we have

        String unsignedFieldNames = null;

        log.info("Unsigned fields:");

        for (Map.Entry<String, String> entry : hp.entrySet())
        {
            if (!signedFieldsSet.contains(entry.getKey()))
            {
                log.info(entry.getKey());
                if (unsignedFieldNames == null)
                {
                    unsignedFieldNames = entry.getKey();
                } else
                {
                    unsignedFieldNames += "," + entry.getKey();
                }
            }
        }

        parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_UNSIGNED_FIELD_NAMES,
                unsignedFieldNames));

        // Calculate the signature
        hp.put(CyberSourceSA.CYBERSOURCESA_UNSIGNED_FIELD_NAMES, unsignedFieldNames);

        String data = null;

        for (String field : CYBERSOURCESA_SIGNED_FIELDS)
        {
            if (data == null)
            {
                data = field + "=" + hp.get(field);
            } else
            {
                data += "," + field + "=" + hp.get(field);
            }
        }

        log.info("Sign this: \n" + data);
        log.info("Secret Key: \n" + pDetails.getCustom1());

        parmList.add(new NameValue(CyberSourceSA.CYBERSOURCESA_SIGNATURE, CyberSourceSAHMACTools
                .getBase64EncodedSignature(pDetails.getCustom1(), data)));

        // addParameters(pDetails, parmList);

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Remove shared secret for security
        // pd.setCustom1(null);

        if (log.isDebugEnabled())
        {
            // to show what we're about to post to CyberSource

            String postStr = pDetails.getRequestUrl() + "?";

            for (int p = 0; p < pDetails.getParameters().length; p++)
            {
                if (p > 0)
                {
                    postStr += "&";
                }

                if (pDetails.getParameters()[p].getValue() == null)
                {
                    if (pDetails.getParameters()[p].getName() != null)
                    {
                        log.debug("Value for " + pDetails.getParameters()[p].getName() + " is null");
                    }
                } else
                {
                    postStr += pDetails.getParameters()[p].getName() + "="
                            + URLEncoder.encode(pDetails.getParameters()[p].getValue(), "UTF-8");
                }
            }

            log.debug("\n" + postStr);
        }

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
        return isAvailable(MODULE_PAYMENT_CYBERSOURCESA_STATUS);
    }

    private String getUTCDateTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // Merchant Account
        private String merchantAccount;

        // ProfileId
        private String profileId;

        // Shared Secret
        private String sharedSecret;

        // Access Key
        private String accessKey;

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

        /**
         * @return the accessKey
         */
        public String getAccessKey()
        {
            return accessKey;
        }

        /**
         * @param accessKey
         *            the accessKey to set
         */
        public void setAccessKey(String accessKey)
        {
            this.accessKey = accessKey;
        }

        /**
         * @return the profileId
         */
        public String getProfileId()
        {
            return profileId;
        }

        /**
         * @param profileId
         *            the profileId to set
         */
        public void setProfileId(String profileId)
        {
            this.profileId = profileId;
        }

    }
}
