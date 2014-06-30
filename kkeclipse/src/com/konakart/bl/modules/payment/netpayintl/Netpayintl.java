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

package com.konakart.bl.modules.payment.netpayintl;

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
import com.konakart.app.Zone;
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;
import com.konakart.util.Utils;

/**
 * Netpay International module. This payment module allows for credit card credentials to be
 * collected directly from a KonaKart page. All communication to the Netpay International server is
 * done from the KonaKart server.
 */
public class Netpayintl extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "Netpayintl";

    private static String bundleName = BaseModule.basePackage + ".payment.netpayintl." + code;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "NetpayintlMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_NETPAYINTL_STATUS = "MODULE_PAYMENT_NETPAYINTL_STATUS";

    /**
     * The zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress of the
     * order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_NETPAYINTL_ZONE = "MODULE_PAYMENT_NETPAYINTL_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_NETPAYINTL_SORT_ORDER = "MODULE_PAYMENT_NETPAYINTL_SORT_ORDER";

    /**
     * The Netpay International Url used to POST the payment request.
     * "https://process.netpay-intl.com/member/remote_charge.asp"
     */
    private final static String MODULE_PAYMENT_NETPAYINTL_REQUEST_URL = "MODULE_PAYMENT_NETPAYINTL_REQUEST_URL";

    /**
     * The Netpay International API Login ID for this installation
     */
    private final static String MODULE_PAYMENT_NETPAYINTL_LOGIN = "MODULE_PAYMENT_NETPAYINTL_LOGIN";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_NETPAYINTL_TEXT_TITLE = "module.payment.netpayintl.text.title";

    private final static String MODULE_PAYMENT_NETPAYINTL_TEXT_DESCRIPTION = "module.payment.netpayintl.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Netpayintl(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_NETPAYINTL_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_NETPAYINTL_REQUEST_URL must be set to the "
                            + "URL for sending the request to Netpay International. "
                            + "(e.g. https://process.netpay-intl.com/member/remote_charge.asp)");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_NETPAYINTL_LOGIN);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_NETPAYINTL_LOGIN must be set to the"
                            + " Netpay International Merchant ID for this installation");
        }
        staticData.setLoginId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_NETPAYINTL_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_NETPAYINTL_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }
    }

    /**
     * Return a payment details object for Netpay International IPN module
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
         * If the zone is greater than zero, it should reference a GeoZone. If the DeliveryAddress
         * of the order isn't within that GeoZone, then we throw an exception
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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_NETPAYINTL_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_NETPAYINTL_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("CompanyNum", sd.getLoginId()));

        // 0 = Debit Transaction
        parmList.add(new NameValue("TransType", "0"));

        // 1 = Debit
        parmList.add(new NameValue("TypeCredit", "1"));

        // 1 installment
        parmList.add(new NameValue("Payments", "1"));

        // Netpay International requires details of the final price - inclusive of tax.
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

        parmList.add(new NameValue("Amount", Utils.removeStringFromString(total.toString(), order
                .getCurrency().getThousandsPoint())));
        parmList.add(new NameValue("Currency", getCurrencyCode(order.getCurrency().getCode())));

        parmList.add(new NameValue("Email", order.getCustomerEmail()));
        parmList.add(new NameValue("PhoneNumber", order.getCustomerTelephone()));

        StringBuffer addrSB = new StringBuffer();
        addrSB.append(order.getBillingStreetAddress());
        if (order.getBillingSuburb() != null && order.getBillingSuburb().length() > 0)
        {
            addrSB.append(", ");
            addrSB.append(order.getBillingSuburb());
        }
        parmList.add(new NameValue("BillingAddress1", Utils.trim(addrSB, 100)));

        parmList.add(new NameValue("BillingCity", Utils.trim(order.getBillingCity(), 60)));
        parmList.add(new NameValue("BillingZipCode", Utils.trim(order.getBillingPostcode(), 15)));

        // Country requires the two letter country code
        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("BillingCountry", country.getIsoCode2()));

            if (country.getIsoCode2().equals("US") || country.getIsoCode2().equals("CA"))
            {
                Zone billingZone = getTaxMgr().getZonePerCountryAndCode(country.getId(),
                        order.getBillingState());
                if (billingZone != null)
                {
                    parmList.add(new NameValue("BillingState", Utils.trim(
                            billingZone.getZoneCode(), 5)));
                } else
                {
                    log.warn("Could not establish Zone code for " + order.getBillingCountry()
                            + " and " + order.getBillingState());
                }
            }
        }

        // Unique Text used to identify one transaction from another
        parmList.add(new NameValue("Order", String.valueOf(order.getId())));

        String bName = order.getBillingName();
        if (bName != null)
        {
            parmList.add(new NameValue("UserName", Utils.trim(bName, 50)));
        }

        // Optional text used mainly to describe the transaction
        parmList.add(new NameValue("Comment", String.valueOf(order.getOrderNumber())));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(false);
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(false);
        pDetails.setShowType(false); // Netpay International doesn't require the card type
        pDetails.setShowOwner(true);

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
        return isAvailable(MODULE_PAYMENT_NETPAYINTL_STATUS);
    }

    /**
     * Translate the 3 character currency code into the Netpay International currency code number
     * 
     * @param kkCurrencyCode
     *            3-character currency code that KonaKart stores
     * @return the Netpay International currency code number
     */
    private String getCurrencyCode(String kkCurrencyCode)
    {
        String codeTable[] =
        { "0", "ILS", "1", "USD", "2", "EUR", "3", "GBP", "4", "AUD", "5", "CAD", "6", "JPY", "7",
                "NOK" };

        for (int c = 0; c < codeTable.length / 2; c += 2)
        {
            if (kkCurrencyCode.equals(codeTable[c + 1]))
            {
                return codeTable[c];
            }
        }

        return kkCurrencyCode;
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // The Netpay International Url used to POST the payment request.
        private String requestUrl;

        // Login ID
        private String loginId;

        // Login Password
        private String pwd;

        // Show the CVV entry field on the UI
        private boolean showCVV = true;

        // zone where Netpay International will be made available
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
         * @return the loginId
         */
        public String getLoginId()
        {
            return loginId;
        }

        /**
         * @param loginId
         *            the loginId to set
         */
        public void setLoginId(String loginId)
        {
            this.loginId = loginId;
        }

        /**
         * @return the pwd
         */
        public String getPwd()
        {
            return pwd;
        }

        /**
         * @param pwd
         *            the pwd to set
         */
        public void setPwd(String pwd)
        {
            this.pwd = pwd;
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