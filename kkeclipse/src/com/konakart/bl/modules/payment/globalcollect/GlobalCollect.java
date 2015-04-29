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

package com.konakart.bl.modules.payment.globalcollect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;
import com.konakart.util.PrettyXmlPrinter;
import com.konakart.util.Utils;

/**
 * GlobalCollect Hosted Order Post module
 */
public class GlobalCollect extends BasePaymentModule implements PaymentInterface
{
    /**
     * Module name - make this the same name as this class
     */
    public static String GLOBALCOLLECT_GATEWAY_CODE = "GlobalCollect";

    private static String bundleName = BaseModule.basePackage + ".payment."
            + GLOBALCOLLECT_GATEWAY_CODE.toLowerCase() + "." + GLOBALCOLLECT_GATEWAY_CODE;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = GLOBALCOLLECT_GATEWAY_CODE + "Mutex";

    private static String getPaymentProductsMutex = GLOBALCOLLECT_GATEWAY_CODE
            + "GetPaymentProductsMutex";

    private static final String hostPortSubstitute = "host:port";

    private static final String PRE_PROCESS_CODE = "GlobalCollect";

    /** Not sure if we use these now... */
    protected static final int PAYMENT_METHOD_CARD_ONLINE = 1;

    protected static final int PAYMENT_METHOD_DD = 3;

    protected static final int PAYMENT_METHOD_BANK_TRANSFER = 7;

    protected static final int PAYMENT_METHOD_REALTIME_BANK_TRANSFER = 8;

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_GLOBALCOLLECT_STATUS = "MODULE_PAYMENT_GLOBALCOLLECT_STATUS";

    /**
     * Custom Class
     */
    private final static String MODULE_PAYMENT_GLOBALCOLLECT_CUSTOM_CLASS = "MODULE_PAYMENT_GLOBALCOLLECT_CUSTOM_CLASS";

    /**
     * The GlobalCollect Zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_GLOBALCOLLECT_ZONE = "MODULE_PAYMENT_GLOBALCOLLECT_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_GLOBALCOLLECT_SORT_ORDER = "MODULE_PAYMENT_GLOBALCOLLECT_SORT_ORDER";

    /**
     * The GlobalCollect Url used to POST the payment request.
     */
    public final static String MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL = "MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL";

    /**
     * The GlobalCollect Products that will be allowed - with ordering defined
     */
    public final static String MODULE_PAYMENT_GLOBALCOLLECT_PRODS = "MODULE_PAYMENT_GLOBALCOLLECT_PRODS";

    /**
     * This URL is used by the GlobalCollect IPN functionality to call back into the application
     * with the results of the transaction. It must be a URL that is visible from the internet. If
     * it is in the form http://host:port/konakart/GlobalCollectResponse.do, then the string
     * host:port is substituted automatically with the correct value.
     */
    public final static String MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL = "MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_GLOBALCOLLECT_TEXT_TITLE = "module.payment.globalcollect.text.title";

    private final static String MODULE_PAYMENT_GLOBALCOLLECT_TEXT_DESCRIPTION = "module.payment.globalcollect.text.description";

    /**
     * Module Parameter - Merchant Account
     */
    public final static String MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC = "MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC";

    /**
     * Module Parameter - Server IP Address
     */
    public final static String MODULE_PAYMENT_GLOBALCOLLECT_IP = "MODULE_PAYMENT_GLOBALCOLLECT_IP";

    /**
     * Module Parameter - Time-based OrderId
     */
    public final static String MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID = "MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID";

    /**
     * Payment ProductId
     */
    public final static String GLOBALCOLLECT_PAYMENT_PRODUCTID = "GLOBALCOLLECT_PAYMENT_PRODUCTID";

    /**
     * Bill To Company
     */
    public final static String GLOBALCOLLECT_BILLTO_COMP = "GLOBALCOLLECT_BILLTO_COMP";

    /**
     * Bill To First Name
     */
    public final static String GLOBALCOLLECT_BILLTO_FNAME = "GLOBALCOLLECT_BILLTO_FNAME";

    /**
     * Bill To Last Name
     */
    public final static String GLOBALCOLLECT_BILLTO_LNAME = "GLOBALCOLLECT_BILLTO_LNAME";

    /**
     * Bill To Street 1
     */
    public final static String GLOBALCOLLECT_BILLTO_HOUSENUMBER = "GLOBALCOLLECT_BILLTO_HOUSENUMBER";

    /**
     * Bill To Street 1
     */
    public final static String GLOBALCOLLECT_BILLTO_STREET1 = "GLOBALCOLLECT_BILLTO_STREET1";

    /**
     * Bill To City
     */
    public final static String GLOBALCOLLECT_BILLTO_CITY = "GLOBALCOLLECT_BILLTO_CITY";

    /**
     * Bill To State
     */
    public final static String GLOBALCOLLECT_BILLTO_STATE = "GLOBALCOLLECT_BILLTO_STATE";

    /**
     * Bill To Postal Code
     */
    public final static String GLOBALCOLLECT_BILLTO_POSTCODE = "GLOBALCOLLECT_BILLTO_POSTCODE";

    /**
     * Bill To Country
     */
    public final static String GLOBALCOLLECT_BILLTO_COUNTRY = "GLOBALCOLLECT_BILLTO_COUNTRY";

    /**
     * Bill To CountryCode
     */
    public final static String GLOBALCOLLECT_BILLTO_CTRY_CODE = "GLOBALCOLLECT_BILLTO_CTRY_CODE";

    /**
     * Bill To Phone Number
     */
    public final static String GLOBALCOLLECT_BILLTO_PHONE = "GLOBALCOLLECT_BILLTO_PHONE";

    /**
     * Bill To Email
     */
    public final static String GLOBALCOLLECT_BILLTO_EMAIL = "GLOBALCOLLECT_BILLTO_EMAIL";

    /**
     * Card holder's name
     */
    public static final String GLOBALCOLLECT_CARDHOLDERS_NAME = "CARDHOLDERS_NAME";

    /**
     * Credit Card Type
     */
    public final static String GLOBALCOLLECT_CARD_TYPE = "GLOBALCOLLECT_CARD_TYPE";

    /**
     * Credit Card Expiry Month
     */
    public final static String GLOBALCOLLECT_CARD_EXP_MONTH = "GLOBALCOLLECT_CARD_EXP_MONTH";

    /**
     * Credit Card Expiry Year
     */
    public final static String GLOBALCOLLECT_CARD_EXP_YEAR = "GLOBALCOLLECT_CARD_EXP_YEAR";

    /**
     * Credit Card Account Number
     */
    public final static String GLOBALCOLLECT_CARD_NUMBER = "GLOBALCOLLECT_CARD_NUMBER";

    /**
     * Credit Card CCV number
     */
    public final static String GLOBALCOLLECT_CARD_CCV = "GLOBALCOLLECT_CARD_CCV";

    /**
     * Credit Card Owner
     */
    public final static String GLOBALCOLLECT_CARD_OWNER = "GLOBALCOLLECT_CARD_OWNER";

    /**
     * Customer's email address
     */
    public static final String GLOBALCOLLECT_CUST_EMAIL = "CUSTOMER_EMAIL";

    /**
     * Shopper's Reference
     */
    public static final String GLOBALCOLLECT_CUST_REFERENCE = "CUST_REFERENCE";

    /**
     * Merchant Account
     */
    public static final String GLOBALCOLLECT_MERCHANT_ACCOUNT = "merchantID";

    /**
     * Merchant Reference
     */
    public static final String GLOBALCOLLECT_MERCHANT_REF = "MERCHANT_REF";

    /**
     * Installments
     */
    public static final String GLOBALCOLLECT_NUMBEROFINSTALLMENTS = "NUMBEROFINSTALLMENTS";

    /**
     * Order Id
     */
    public final static String GLOBALCOLLECT_ORDER_ID = "GLOBALCOLLECT_ORDER_ID";

    /**
     * Order Number
     */
    public final static String GLOBALCOLLECT_ORDER_NUMBER = "GLOBALCOLLECT_ORDER_NUMBER";

    /**
     * Payment Parameter - Payment Amount
     */
    public static final String GLOBALCOLLECT_PAYMENT_AMOUNT = "amount";

    /**
     * Returned Reference
     */
    public static final String GLOBALCOLLECT_RETURNED_REF = "REF";

    /**
     * Returned MAC
     */
    public static final String GLOBALCOLLECT_RETURNED_MAC = "RETURNEDMAC";

    /**
     * Returned FORMACTION
     */
    public static final String GLOBALCOLLECT_RETURNED_FORMACTION = "FORMACTION";

    /**
     * Ship To First Name
     */
    public final static String GLOBALCOLLECT_SHIPTO_FNAME = "GLOBALCOLLECT_SHIPTO_FNAME";

    /**
     * Ship To Last Name
     */
    public final static String GLOBALCOLLECT_SHIPTO_LNAME = "GLOBALCOLLECT_SHIPTO_LNAME";

    /**
     * Ship To Street 1
     */
    public final static String GLOBALCOLLECT_SHIPTO_HOUSENUMBER = "GLOBALCOLLECT_SHIPTO_HOUSENUMBER";

    /**
     * Ship To Street 1
     */
    public final static String GLOBALCOLLECT_SHIPTO_STREET1 = "GLOBALCOLLECT_SHIPTO_STREET1";

    /**
     * Ship To City
     */
    public final static String GLOBALCOLLECT_SHIPTO_CITY = "GLOBALCOLLECT_SHIPTO_CITY";

    /**
     * Ship To Company
     */
    public final static String GLOBALCOLLECT_SHIPTO_COMP = "GLOBALCOLLECT_SHIPTO_COMP";

    /**
     * Ship To State
     */
    public final static String GLOBALCOLLECT_SHIPTO_STATE = "GLOBALCOLLECT_SHIPTO_STATE";

    /**
     * Ship To Postal Code
     */
    public final static String GLOBALCOLLECT_SHIPTO_POSTCODE = "GLOBALCOLLECT_SHIPTO_POSTCODE";

    /**
     * Ship To Country
     */
    public final static String GLOBALCOLLECT_SHIPTO_COUNTRY = "GLOBALCOLLECT_SHIPTO_COUNTRY";

    /**
     * Bill To CountryCode
     */
    public final static String GLOBALCOLLECT_SHIPTO_CTRY_CODE = "GLOBALCOLLECT_SHIPTO_CTRY_CODE";

    /**
     * Order time
     */
    public final static String GLOBALCOLLECT_TIME_MS = "GLOBALCOLLECT_TIME_MS";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public GlobalCollect(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL must be set to the URL for"
                            + " sending the request to GlobalCollect.");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_CUSTOM_CLASS);
        if (conf == null)
        {
            // Only warn if absent because we can continue without it
            log.warn("The Configuration MODULE_PAYMENT_GLOBALCOLLECT_CUSTOM_CLASS must be set to the name"
                    + " of the custom class for GlobalCollect.");
        }
        staticData.setCustomClass(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL must be set "
                            + "to the Response Url");
        }
        staticData.setResponseUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC must be set to "
                            + "the GlobalCollect Merchant Account");
        }
        staticData.setMerchantAccount(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_IP);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_GLOBALCOLLECT_IP must be set to "
                            + "the Server's IP Address");
        }
        staticData.setIpAddress(conf.getValue());

        conf = getConfiguration("STORE_COUNTRY");
        if (conf == null)
        {
            throw new KKException("The Configuration STORE_COUNTRY must be set to "
                    + "the Store's Country Id");
        }

        int countryId;
        try
        {
            countryId = new Integer(conf.getValue()).intValue();
        } catch (Exception e)
        {
            throw new KKException("The Configuration STORE_COUNTRY is invalid - it must be set to "
                    + "the Store's Country Id");
        }

        CountryIf country;
        try
        {
            country = getTaxMgr().getCountryPerId(countryId);
        } catch (Exception e)
        {
            throw new KKException("Problem retrieving Country using country Id = " + countryId, e);
        }
        staticData.setStoreCountryCode(country.getIsoCode2());

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID);
        if (conf == null)
        {
            staticData.setTimeBasedOrderId(false);
        } else
        {
            staticData.setTimeBasedOrderId(new Boolean(conf.getValue()).booleanValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_GLOBALCOLLECT_PRODS);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_GLOBALCOLLECT_PRODS must be set to "
                            + "the comma-separated list of supported Product Ids");
        }
        StringTokenizer st = new StringTokenizer(conf.getValue(), ",; ");
        int[] prods = new int[st.countTokens()];
        int prod = 0;
        while (st.hasMoreTokens())
        {
            prods[prod++] = Integer.valueOf(st.nextToken());
        }
        staticData.setAllowedProducts(prods);

        // Clear the Payment Products List Cache so that it's re-filled
        staticData.getProductsHM().clear();
    }

    /**
     * Return a payment details object for GlobalCollect IPN module
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
         * The GlobalCollect zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
         */
        if (sd.getZone() > 0)
        {
            checkZone(info, sd.getZone());
        }

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap,
                info.getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        PaymentDetails pDetails = new PaymentDetails();
        pDetails.setCode(GLOBALCOLLECT_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setPaymentType(PaymentDetails.BROWSER_IN_FRAME_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_GLOBALCOLLECT_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_GLOBALCOLLECT_TEXT_TITLE));
        pDetails.setPreProcessCode(PRE_PROCESS_CODE);

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        String billingCountryCode;
        CountryIf billingCountry = getTaxMgr().getCountryPerName(order.getBillingCountry());
        if (billingCountry != null)
        {
            billingCountryCode = billingCountry.getIsoCode2();
        } else
        {
            billingCountryCode = sd.getStoreCountryCode();
        }

        List<NameValueIf> parmList = new ArrayList<NameValueIf>();
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_CTRY_CODE, billingCountryCode));

        String deliveryCountryCode;
        CountryIf deliveryCountry = getTaxMgr().getCountryPerName(order.getDeliveryCountry());
        if (deliveryCountry != null)
        {
            deliveryCountryCode = deliveryCountry.getIsoCode2();
        } else
        {
            deliveryCountryCode = sd.getStoreCountryCode();
        }

        parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_CTRY_CODE, deliveryCountryCode));

        // This gateway only requires details of the final price. No tax, sub-total etc.
        GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();
        BigDecimal total = globalCollectUtils.getTotalPrice(order);

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            // Put the parameters into an array
            NameValue[] nvArray = new NameValue[parmList.size()];
            parmList.toArray(nvArray);
            pDetails.setParameters(nvArray);

            // Access GlobalCollect to find out which payment products are available

            // order.setPaymentDetails(pDetails);
            PaymentProduct[] payProds = getPaymentProducts(sd, order, pDetails, total);

            if (log.isDebugEnabled())
            {
                if (payProds == null)
                {
                    log.debug("No Payment Products found");
                } else
                {
                    log.debug(payProds.length + " Payment Products found");
                }
            }

            if (payProds == null)
            {
                throw new KKException("No payment products available from GlobalCollect. "
                        + "This could be because we couldn't comunicate with GlobalCollect.");
            }

            // If we only have one product will use the attributes at the top level

            if (payProds.length == 1)
            {
                pDetails.setSubCode(String.valueOf(payProds[0].getPaymentProductId()));
                pDetails.setDescription(payProds[0].getPaymentProductName());
                pDetails.setTitle(payProds[0].getPaymentProductName());
                return pDetails;
            }

            // Now we need to create multiple PaymentDetails objects for the products returned

            List<PaymentDetailsIf> pDetailsList = new ArrayList<PaymentDetailsIf>();
            for (int p = 0; p < payProds.length; p++)
            {
                PaymentDetailsIf pd = pDetails.cloneMainAttributes();

                pd.setSubCode(String.valueOf(payProds[p].getPaymentProductId()));
                pd.setSubSortOrder(payProds[p].getSortOrder());
                pd.setTitle(payProds[p].getPaymentProductName());
                pd.setDescription(payProds[p].getPaymentProductName());

                pDetailsList.add(pd);
            }

            // Now sort that list of payment details
            Collections.sort(pDetailsList, new SortOrderComparator());

            pDetails.setPaymentDetails(pDetailsList.toArray(new PaymentDetails[0]));

            return pDetails;
        }

        if (total == null)
        {
            throw new KKException("An Order Total was not found");
        }

        parmList.add(new NameValue(MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID, (sd
                .isTimeBasedOrderId() ? "T" : "F")));
        parmList.add(new NameValue(MODULE_PAYMENT_GLOBALCOLLECT_IP, sd.getIpAddress()));
        parmList.add(new NameValue(MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL, sd.getRequestUrl()));
        parmList.add(new NameValue(MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC, sd
                .getMerchantAccount()));
        parmList.add(new NameValue(MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL, sd.getResponseUrl()
                .replaceFirst(hostPortSubstitute, info.getHostAndPort())));
        parmList.add(new NameValue(GLOBALCOLLECT_CUST_EMAIL, order.getCustomerEmail()));
        parmList.add(new NameValue(GLOBALCOLLECT_PAYMENT_AMOUNT, removeCurrencySymbols(total
                .toString())));

        // ----------------------------------------------------------------------------------------
        // Add Custom values

        GlobalCollectCustomIf customClass = getCustomClass(sd.getCustomClass(), getEng());

        if (customClass != null)
        {
            int numInstallments = customClass.getNumberOfInstallments(order);
            if (numInstallments != -1)
            {
                parmList.add(new NameValue(GLOBALCOLLECT_NUMBEROFINSTALLMENTS, Integer
                        .toString(numInstallments)));
            }
        }

        // ---------------------------------------------------------------------------------------
        // Set the billing details

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_FNAME, bNames[0]));
            parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_LNAME, bNames[1]));
        }

        if (!Utils.isBlank(order.getBillingCompany()))
        {
            parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_COMP, order.getBillingCompany()));
        }

        String[] addressParts = splitStreetAddressIntoNumberAndStreet(order
                .getBillingStreetAddress());

        if (addressParts[0] != null)
        {
            parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_HOUSENUMBER, addressParts[0]));
        }
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_STREET1, addressParts[1]));
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_CITY, order.getBillingCity()));
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_STATE, order.getBillingState()));
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_COUNTRY, order.getBillingCountry()));
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_POSTCODE, order.getBillingPostcode()));
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_PHONE, order.getBillingTelephone()));
        parmList.add(new NameValue(GLOBALCOLLECT_BILLTO_EMAIL, order.getCustomerEmail()));

        // ---------------------------------------------------------------------------------------
        // Set the delivery details

        // Set the delivery name from the delivery address Id

        String[] dNames = getFirstAndLastNamesFromAddress(order.getDeliveryAddrId());
        if (dNames != null)
        {
            parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_FNAME, dNames[0]));
            parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_LNAME, dNames[1]));
        }

        if (!Utils.isBlank(order.getDeliveryCompany()))
        {
            parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_COMP, order.getDeliveryCompany()));
        }

        String[] deliveryAddressParts = splitStreetAddressIntoNumberAndStreet(order
                .getDeliveryStreetAddress());

        if (deliveryAddressParts[0] != null)
        {
            parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_HOUSENUMBER, deliveryAddressParts[0]));
        }
        parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_STREET1, deliveryAddressParts[1]));

        parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_CITY, order.getDeliveryCity()));
        parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_STATE, order.getDeliveryState()));
        parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_COUNTRY, order.getDeliveryCountry()));
        parmList.add(new NameValue(GLOBALCOLLECT_SHIPTO_POSTCODE, order.getDeliveryPostcode()));

        Date now = new Date();
        String timeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
        parmList.add(new NameValue(GlobalCollect.GLOBALCOLLECT_TIME_MS, timeStr));

        // Order Details

        if (sd.isTimeBasedOrderId())
        {
            String timeBasedOrderId = String.valueOf(System.currentTimeMillis());
            if (log.isDebugEnabled())
            {
                log.debug("time now in MS: " + timeBasedOrderId);
            }
            parmList.add(new NameValue(GLOBALCOLLECT_ORDER_ID, timeBasedOrderId
                    .substring(timeBasedOrderId.length() - 10)));
        } else
        {
            parmList.add(new NameValue(GLOBALCOLLECT_ORDER_ID, order.getId()));
        }
        parmList.add(new NameValue(GLOBALCOLLECT_ORDER_NUMBER, order.getOrderNumber()));

        if (log.isDebugEnabled())
        {
            log.debug("Used to create merchantReference:          \n"
                    + "    OrderId              = " + order.getId() + "\n"
                    + "    TimeStr              = " + timeStr);
        }

        String merchantReference = Utils.trim(order.getId() + "~" + timeStr, 50, false);

        parmList.add(new NameValue(GlobalCollect.GLOBALCOLLECT_MERCHANT_REF, merchantReference));

        pDetails.setSubCode(order.getPaymentModuleSubCode());
        parmList.add(new NameValue(GlobalCollect.GLOBALCOLLECT_PAYMENT_PRODUCTID, order
                .getPaymentModuleSubCode()));

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
     * Get the Payment Products that are available. We keep these in a cache (keyed by country code
     * and locale) because it's quite expensive getting them from GlobalCollect.
     * 
     * @param sd
     * @param order
     * @param total
     *            total amount of order
     * @return Returns an array of supported Payment products
     * @throws Exception
     */
    private PaymentProduct[] getPaymentProducts(StaticData sd, OrderIf order, PaymentDetailsIf pd,
            BigDecimal total) throws Exception
    {
        // Prevent null exceptions
        if (order.getLocale() == null)
        {
            throw new Exception("Locale on order is null");
        }

        HashMap<String, String> hp = hashParameters(pd, null);

        // First we'll look to see if the products list is in our cache

        String hashKey = hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_CTRY_CODE) + "~"
                + order.getLocale().substring(0, 2);

        PaymentProduct[] prodsFromCache = sd.getProductsHM().get(hashKey);
        if (prodsFromCache != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Returning " + prodsFromCache.length + " Payment Products from cache");
            }
            return prodsFromCache;
        }

        return getPaymentProductsFromGateway(sd, order, pd, total, hp, hashKey);
    }

    /**
     * Get the Payment Products that are available from the Gateway.
     * 
     * @param sd
     * @param order
     * @param total
     *            total amount of order
     * @param hp
     *            hashed parameters
     * @param hashkey
     * @return Returns an array of supported Payment products
     * @throws Exception
     */
    private PaymentProduct[] getPaymentProductsFromGateway(StaticData sd, OrderIf order,
            PaymentDetailsIf pd, BigDecimal total, HashMap<String, String> hp, String hashKey)
            throws Exception
    {
        // We need to synchronize this to avoid having multiple threads doing the lookup

        synchronized (getPaymentProductsMutex)
        {
            PaymentProduct[] prodsFromCache = sd.getProductsHM().get(hashKey);
            if (prodsFromCache != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Returning " + prodsFromCache.length + " Payment Products from cache");
                }
                return prodsFromCache;
            }

            StringBuffer msg = new StringBuffer("<XML>");
            msg.append("<REQUEST>");
            msg.append("<ACTION>GET_PAYMENTPRODUCTS</ACTION>");

            msg.append("<META>");
            msg.append("<IPADDRESS>" + sd.getIpAddress() + "</IPADDRESS>");
            msg.append("<MERCHANTID>" + sd.getMerchantAccount() + "</MERCHANTID>");
            msg.append("<VERSION>1.0</VERSION>");
            msg.append("</META>");

            msg.append("<PARAMS>");

            msg.append("<GENERAL>");
            msg.append("<LANGUAGECODE>" + order.getLocale().substring(0, 2) + "</LANGUAGECODE>");
            msg.append("<COUNTRYCODE>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_CTRY_CODE)
                    + "</COUNTRYCODE>");
            msg.append("</GENERAL>");

            msg.append("</PARAMS>");
            msg.append("</REQUEST>");
            msg.append("</XML>");

            if (log.isDebugEnabled())
            {
                try
                {
                    log.debug("GatewayRequest to " + pd.getRequestUrl() + " =\n"
                            + PrettyXmlPrinter.printXml(msg.toString()));
                } catch (Exception e)
                {
                    log.debug("Problem parsing the original XML");
                    e.printStackTrace();
                    log.debug("\n" + msg.toString());
                }
            }

            String gatewayResp = null;
            try
            {
                gatewayResp = postData(msg, pd);
            } catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Problem posting request to " + pd.getRequestUrl() + " : "
                            + e.getMessage());
                }
                throw e;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Unformatted Status Response =\n" + gatewayResp);
                try
                {
                    log.debug("Formatted Status Response =\n"
                            + PrettyXmlPrinter.printXml(gatewayResp));
                } catch (Exception e)
                {
                    log.debug("Exception pretty-printing Status Response : " + e.getMessage());
                }
            }

            // Now process the XML response

            GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();

            String RESPONSE_RESULT = "NOK";
            String REQUESTID = null;

            PaymentProduct[] payPs = null;

            if (gatewayResp == null)
            {
                log.warn("Null response from GlobalCollect gateway");
                return null;
            } else
            {
                Map<String, String> xmlMap = globalCollectUtils.parseGlobalCollectResponseToMap(
                        gatewayResp, "XML.REQUEST.RESPONSE.ROW");

                RESPONSE_RESULT = xmlMap.get("XML.REQUEST.RESPONSE.RESULT");
                REQUESTID = xmlMap.get("XML.REQUEST.RESPONSE.META.REQUESTID");

                if (log.isDebugEnabled())
                {
                    log.debug("Status response data:"
                            + "\n XML.REQUEST.RESPONSE.RESULT           = " + RESPONSE_RESULT
                            + "\n XML.REQUEST.RESPONSE.META.REQUESTID   = " + REQUESTID);
                }

                if (RESPONSE_RESULT == null || RESPONSE_RESULT.equals("NOK"))
                {
                    String errorCode = xmlMap.get("XML.REQUEST.RESPONSE.ERROR.CODE");
                    String errorMesg = xmlMap.get("XML.REQUEST.RESPONSE.ERROR.MESSAGE");

                    log.warn("Error returned from Global Collect : "
                            + (errorCode != null ? " Code: " + errorCode : "")
                            + (errorMesg != null ? " Message: " + errorMesg : ""));
                    return null;
                }

                // Now to pick out the Payment Products

                List<PaymentProduct> payProds = new ArrayList<PaymentProduct>();
                for (int arrayIndx = 0; xmlMap
                        .containsKey("XML.REQUEST.RESPONSE.ROW.PAYMENTPRODUCTID." + arrayIndx); arrayIndx++)
                {
                    PaymentProduct pp = new PaymentProduct();
                    pp.setCurrencyCode(xmlMap.get("XML.REQUEST.RESPONSE.ROW.CURRENCYCODE."
                            + arrayIndx));
                    pp.setMaxAmount(getBigDecimal(
                            xmlMap.get("XML.REQUEST.RESPONSE.ROW.MAXAMOUNT." + arrayIndx),
                            new BigDecimal("-1")));
                    pp.setMinAmount(getBigDecimal(
                            xmlMap.get("XML.REQUEST.RESPONSE.ROW.MINAMOUNT." + arrayIndx),
                            new BigDecimal("-1")));
                    pp.setOrderTypeIndicator(getInt(
                            xmlMap.get("XML.REQUEST.RESPONSE.ROW.ORDERTYPEINDICATOR." + arrayIndx),
                            -1));
                    pp.setPaymentMethodName(xmlMap
                            .get("XML.REQUEST.RESPONSE.ROW.PAYMENTMETHODNAME." + arrayIndx));
                    pp.setPaymentProductId(getInt(
                            xmlMap.get("XML.REQUEST.RESPONSE.ROW.PAYMENTPRODUCTID." + arrayIndx),
                            -1));
                    pp.setPaymentProductName(xmlMap
                            .get("XML.REQUEST.RESPONSE.ROW.PAYMENTPRODUCTNAME." + arrayIndx));

                    if (total != null && pp.getMinAmount().compareTo(total) > 0)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Found: " + pp.getPaymentProductId() + " = "
                                    + pp.getPaymentProductName() + " - Minimum Aount ("
                                    + pp.getMinAmount() + ") > order amount (" + total + ")");
                        }
                    } else if (total != null && pp.getMaxAmount().compareTo(total) < 0)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Found: " + pp.getPaymentProductId() + " = "
                                    + pp.getPaymentProductName() + " - Maximum Aount ("
                                    + pp.getMinAmount() + ") < order amount (" + total + ")");
                        }
                    } else
                    {
                        // Find the index of the Allowed Product
                        int apIdx = -1;
                        for (int ap = 0; ap < sd.getAllowedProducts().length; ap++)
                        {
                            if (sd.getAllowedProducts()[ap] == pp.getPaymentProductId())
                            {
                                apIdx = ap;
                                break;
                            }
                        }

                        if (apIdx != -1)
                        {
                            // The product was found in our allowed products list - so add it
                            pp.setSortOrder(apIdx);

                            payProds.add(pp);

                            if (log.isDebugEnabled())
                            {
                                log.debug("Found: " + pp.getPaymentProductId() + " = "
                                        + pp.getPaymentProductName());
                            }
                        } else
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("Payment Product not added (not on defined list) : "
                                        + pp.getPaymentProductId() + " = "
                                        + pp.getPaymentProductName());
                            }
                        }
                    }
                }

                payPs = payProds.toArray(new PaymentProduct[0]);
            }

            // Save the results in the Cache
            sd.getProductsHM().put(hashKey, payPs);

            return payPs;
        }
    }

    private BigDecimal getBigDecimal(String str, BigDecimal def)
    {
        if (str == null)
        {
            return def;
        }

        try
        {
            return new BigDecimal(str);
        } catch (Exception e)
        {
            // Unexpected
            e.printStackTrace();
            return def;
        }
    }

    private int getInt(String str, int def)
    {
        if (str == null)
        {
            return def;
        }

        try
        {
            return Integer.parseInt(str);
        } catch (Exception e)
        {
            // Unexpected
            e.printStackTrace();
            return def;
        }
    }

    private GlobalCollectCustomIf getCustomClass(String customClassName, KKEngIf _eng)
            throws Exception
    {
        if (Utils.isBlank(customClassName))
        {
            return null;
        }

        Class<?> mgrClass = null;
        try
        {
            mgrClass = Class.forName(customClassName);
        } catch (Exception e)
        {
            throw new KKException(
                    "Unable to instantiate the GlobalCollect custom class with name : "
                            + customClassName);
        }

        if (log.isInfoEnabled())
        {
            log.info("Found GlobalCollect custom class with name : " + customClassName);
        }

        String constructorArg = "com.konakart.appif.KKEngIf";
        Constructor<?>[] constructors = mgrClass.getConstructors();
        Constructor<?> engConstructor = null;
        if (constructors != null && constructors.length > 0)
        {
            for (int i = 0; i < constructors.length; i++)
            {
                Constructor<?> constructor = constructors[i];
                Class<?>[] parmTypes = constructor.getParameterTypes();
                if (parmTypes != null && parmTypes.length == 1)
                {
                    String parmName = parmTypes[0].getName();
                    if (parmName != null && parmName.equals(constructorArg))
                    {
                        engConstructor = constructor;
                    }
                }
            }
        }

        if (engConstructor == null)
        {
            throw new KKException(
                    "Could not find a constructor for the GlobalCollect custom class : "
                            + customClassName + ", that requires one parameter of type "
                            + constructorArg);
        }

        return (GlobalCollectCustomIf) engConstructor.newInstance(_eng);
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_PAYMENT_GLOBALCOLLECT_STATUS);
    }

    /**
     * Sends data to the payment gateway via a POST.
     * 
     * @param postData
     *            The data to be posted.
     * @param pd
     *            the PaymentDetails object
     * @return The response to the post
     * @throws IOException
     */
    public String postData(StringBuffer postData, PaymentDetailsIf pd) throws IOException
    {
        URL url = new URL(pd.getRequestUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        if (pd.getReferrer() != null && pd.getReferrer().length() > 1)
        {
            connection.setRequestProperty("Referer", pd.getReferrer());
        }

        connection.setRequestProperty("content-type", "text/xml; charset=utf-8");

        PrintWriter out = new PrintWriter(connection.getOutputStream());

        StringBuffer sb = postData;

        if (log.isDebugEnabled())
        {
            log.debug("Post URL = " + pd.getRequestUrl());
            log.debug("Post string =\n" + sb.toString());
        }

        // Send the message
        out.print(sb.toString());
        out.close();

        // Get back the response
        StringBuffer respSb = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = in.readLine();

        while (line != null)
        {
            respSb.append(line);
            line = in.readLine();
        }

        in.close();

        return respSb.toString();
    }

    private String removeCurrencySymbols(String amount)
    {
        String justDigits = amount.replace(".", "");
        return justDigits.replace(",", "");
    }

    protected class SortOrderComparator implements Comparator<Object>
    {
        /**
         * @param o1
         * @param o2
         * @return Return Do objects compare
         */
        public int compare(Object o1, Object o2)
        {
            PaymentDetails pd1 = (PaymentDetails) o1;
            PaymentDetails pd2 = (PaymentDetails) o2;
            if (pd1.getSubSortOrder() > pd2.getSubSortOrder())
            {
                return 1;
            } else if (pd1.getSubSortOrder() < pd2.getSubSortOrder())
            {
                return -1;
            } else
            {
                return 0;
            }
        }
    }

    /**
     * Used to store the payment products returned from GlobalCollect
     */
    protected class PaymentProduct
    {
        private String paymentMethodName = null;

        private int paymentProductId = -1;

        private String paymentProductName = null;

        private int orderTypeIndicator = -1;

        private int sortOrder = -1;

        private BigDecimal minAmount = null;

        private BigDecimal maxAmount = null;

        private String currencyCode = null;

        /**
         * @return the paymentMethodName
         */
        public String getPaymentMethodName()
        {
            return paymentMethodName;
        }

        /**
         * @param paymentMethodName
         *            the paymentMethodName to set
         */
        public void setPaymentMethodName(String paymentMethodName)
        {
            this.paymentMethodName = paymentMethodName;
        }

        /**
         * @return the paymentProductId
         */
        public int getPaymentProductId()
        {
            return paymentProductId;
        }

        /**
         * @param paymentProductId
         *            the paymentProductId to set
         */
        public void setPaymentProductId(int paymentProductId)
        {
            this.paymentProductId = paymentProductId;
        }

        /**
         * @return the paymentProductName
         */
        public String getPaymentProductName()
        {
            return paymentProductName;
        }

        /**
         * @param paymentProductName
         *            the paymentProductName to set
         */
        public void setPaymentProductName(String paymentProductName)
        {
            this.paymentProductName = paymentProductName;
        }

        /**
         * @return the orderTypeIndicator
         */
        public int getOrderTypeIndicator()
        {
            return orderTypeIndicator;
        }

        /**
         * @param orderTypeIndicator
         *            the orderTypeIndicator to set
         */
        public void setOrderTypeIndicator(int orderTypeIndicator)
        {
            this.orderTypeIndicator = orderTypeIndicator;
        }

        /**
         * @return the minAmount
         */
        public BigDecimal getMinAmount()
        {
            return minAmount;
        }

        /**
         * @param minAmount
         *            the minAmount to set
         */
        public void setMinAmount(BigDecimal minAmount)
        {
            this.minAmount = minAmount;
        }

        /**
         * @return the maxAmount
         */
        public BigDecimal getMaxAmount()
        {
            return maxAmount;
        }

        /**
         * @param maxAmount
         *            the maxAmount to set
         */
        public void setMaxAmount(BigDecimal maxAmount)
        {
            this.maxAmount = maxAmount;
        }

        /**
         * @return the currencyCode
         */
        public String getCurrencyCode()
        {
            return currencyCode;
        }

        /**
         * @param currencyCode
         *            the currencyCode to set
         */
        public void setCurrencyCode(String currencyCode)
        {
            this.currencyCode = currencyCode;
        }

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
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // Merchant Account
        private String merchantAccount;

        // Callback called by Gateway after a 3D Secure transaction
        private String responseUrl;

        // The GlobalCollect Url used to POST the payment request.
        private String requestUrl;

        private int zone;

        private String mac;

        private String gatewayVersion;

        private String ipAddress;

        private String storeCountryCode;

        private boolean timeBasedOrderId;

        private String customClass;

        /** Array of Allowed products */
        private int[] allowedProducts;

        /** Product list hash */
        private Map<String, PaymentProduct[]> productsHM = Collections
                .synchronizedMap(new HashMap<String, PaymentProduct[]>());

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
         * @return the ipAddress
         */
        public String getIpAddress()
        {
            return ipAddress;
        }

        /**
         * @param ipAddress
         *            the ipAddress to set
         */
        public void setIpAddress(String ipAddress)
        {
            this.ipAddress = ipAddress;
        }

        /**
         * @return the mac
         */
        public String getMac()
        {
            return mac;
        }

        /**
         * @param mac
         *            the mac to set
         */
        public void setMac(String mac)
        {
            this.mac = mac;
        }

        /**
         * @return the storeCountryCode
         */
        public String getStoreCountryCode()
        {
            return storeCountryCode;
        }

        /**
         * @param storeCountryCode
         *            the storeCountryCode to set
         */
        public void setStoreCountryCode(String storeCountryCode)
        {
            this.storeCountryCode = storeCountryCode;
        }

        /**
         * @return the timeBasedOrderId
         */
        public boolean isTimeBasedOrderId()
        {
            return timeBasedOrderId;
        }

        /**
         * @param timeBasedOrderId
         *            the timeBasedOrderId to set
         */
        public void setTimeBasedOrderId(boolean timeBasedOrderId)
        {
            this.timeBasedOrderId = timeBasedOrderId;
        }

        /**
         * @return the allowedProducts
         */
        public int[] getAllowedProducts()
        {
            return allowedProducts;
        }

        /**
         * @param allowedProducts
         *            the allowedProducts to set
         */
        public void setAllowedProducts(int[] allowedProducts)
        {
            this.allowedProducts = allowedProducts;
        }

        /**
         * @return the productsHM
         */
        public Map<String, PaymentProduct[]> getProductsHM()
        {
            return productsHM;
        }

        /**
         * @param productsHM
         *            the productsHM to set
         */
        public void setProductsHM(Map<String, PaymentProduct[]> productsHM)
        {
            this.productsHM = productsHM;
        }

        /**
         * @return the customClass
         */
        public String getCustomClass()
        {
            return customClass;
        }

        /**
         * @param customClass
         *            the customClass to set
         */
        public void setCustomClass(String customClass)
        {
            this.customClass = customClass;
        }
    }
}
