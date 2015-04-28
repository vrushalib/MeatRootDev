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

package com.konakart.bl.modules.payment.payflowpro;

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
import com.konakart.appif.CountryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;
import com.konakart.util.Utils;

/**
 * Payflow Pro module. This payment module allows for credit card credentials to be collected
 * directly from a KonaKart page. All communication to the Payflow Pro server is done from the
 * KonaKart server.
 */
public class PayflowPro extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "PayflowPro";

    private static String bundleName = BaseModule.basePackage + ".payment.payflowpro." + code;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "PayflowProMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_STATUS = "MODULE_PAYMENT_PAYFLOWPRO_STATUS";

    /**
     * The https://api-3t.paypal.com/2.0/ zone, if greater than zero, should reference a GeoZone. If
     * the DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_ZONE = "MODULE_PAYMENT_PAYFLOWPRO_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_SORT_ORDER = "MODULE_PAYMENT_PAYFLOWPRO_SORT_ORDER";

    /**
     * The Payflow Pro Url used to POST the payment request.
     * "https://pilot-payflowpro.paypal.com/transaction" (live)
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_REQUEST_URL = "MODULE_PAYMENT_PAYFLOWPRO_REQUEST_URL";

    /**
     * The Payflow Pro API Login ID for this installation
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_LOGIN = "MODULE_PAYMENT_PAYFLOWPRO_LOGIN";

    /**
     * The Payflow Pro API VENDOR for this installation
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_VENDOR = "MODULE_PAYMENT_PAYFLOWPRO_VENDOR";

    /**
     * The Payflow Pro API PARTNER for this installation
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_PARTNER = "MODULE_PAYMENT_PAYFLOWPRO_PARTNER";

    /**
     * The Payflow Pro Signature for the merchant
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_PWD = "MODULE_PAYMENT_PAYFLOWPRO_PWD";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_PAYFLOWPRO_SHOW_CVV = "MODULE_PAYMENT_PAYFLOWPRO_SHOW_CVV";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_PAYFLOWPRO_TEXT_TITLE = "module.payment.payflowpro.text.title";

    private final static String MODULE_PAYMENT_PAYFLOWPRO_TEXT_DESCRIPTION = "module.payment.payflowpro.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public PayflowPro(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYFLOWPRO_REQUEST_URL must be set to the URL for"
                            + " sending the request to Payflow Pro. (e.g. https://api-3t.sandbox.paypal.com/nvp)");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_LOGIN);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYFLOWPRO_LOGIN must be set to the"
                            + " Payflow Pro API User ID for this installation");
        }
        staticData.setLoginId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_VENDOR);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYFLOWPRO_VENDOR must be set to the"
                            + " Payflow Pro API VENDOR ID for this installation");
        }
        staticData.setVendor(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_PARTNER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYFLOWPRO_PARTNER must be set to the"
                            + " Payflow Pro API PARTNER for this installation");
        }
        staticData.setPartner(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_PWD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYFLOWPRO_PWD must be set to the"
                            + " Current Payflow Pro Password for this merchant");
        }
        staticData.setPwd(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYFLOWPRO_SHOW_CVV);
        if (conf == null)
        {
            staticData.setShowCVV(true);
        } else
        {
            if (conf.getValue().trim().equalsIgnoreCase("false"))
            {
                staticData.setShowCVV(false);
            } else
            {
                staticData.setShowCVV(true);
            }
        }
    }

    /**
     * Return a payment details object for Payflow Pro IPN module
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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_PAYFLOWPRO_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_PAYFLOWPRO_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("USER", sd.getLoginId()));
        parmList.add(new NameValue("VENDOR", sd.getVendor()));
        parmList.add(new NameValue("PARTNER", sd.getPartner()));
        parmList.add(new NameValue("PWD", sd.getPwd()));
        // parmList.add(new NameValue("SIGNATURE",
        // "A5G.2wnxuz5dRHXv496ke0JPmjiiAmSbXDV7kXsPpAnmmynaifF1JV3b"));

        parmList.add(new NameValue("TENDER", "C")); // Credit Card
        parmList.add(new NameValue("TRXTYPE", "S")); // Sale Transaction
        // parmList.add(new NameValue("METHOD", "DoDirectPayment")); // Not in the Documentation

        // Payflow Pro requires details of the final price - inclusive of tax.
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

        parmList.add(new NameValue("AMT", Utils.removeStringFromString(total.toString(), order
                .getCurrency().getThousandsPoint())));
        parmList.add(new NameValue("CURRENCY", order.getCurrency().getCode()));
        parmList.add(new NameValue("INVNUM", order.getId()));
        parmList.add(new NameValue("CUSTREF", order.getOrderNumber()));
        parmList.add(new NameValue("EMAIL", order.getCustomerEmail()));

        // Set the billing address

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue("FIRSTNAME", Utils.trim(bNames[0], 25)));
            parmList.add(new NameValue("LASTNAME", Utils.trim(bNames[1], 25)));
        }

        StringBuffer addrSB = new StringBuffer();
        addrSB.append(order.getBillingStreetAddress());
        if (order.getBillingSuburb() != null && order.getBillingSuburb().length() > 0)
        {
            addrSB.append(", ");
            addrSB.append(order.getBillingSuburb());
        }
        parmList.add(new NameValue("STREET", Utils.trim(addrSB, 100)));

        // Country requires the two letter country code
        CountryIf country = getEng().getCountryPerName(order.getBillingCountry());
        if (country != null)
        {
            parmList.add(new NameValue("COUNTRY", country.getIsoCode2()));
        }

        parmList.add(new NameValue("CITY", Utils.trim(order.getBillingCity(), 40)));
        parmList.add(new NameValue("STATE", Utils.trim(order.getBillingState(), 40)));
        parmList.add(new NameValue("ZIP", Utils.trim(Utils.removeCharFromString(order
                .getBillingPostcode(), ' '), 9)));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(false);
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(false);
        pDetails.setShowType(false); // Payflow Pro doesn't require the card type
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
        return isAvailable(MODULE_PAYMENT_PAYFLOWPRO_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // The Payflow Pro Url used to POST the payment request.
        // "https://api-3t.sandbox.paypal.com/nvp"
        private String requestUrl;

        // Login ID
        private String loginId;

        // Login Password
        private String pwd;

        // Vendor
        private String vendor;
        
        // Partner
        private String partner;
        
        // Show the CVV entry field on the UI
        private boolean showCVV = true;

        // zone where Payflow Pro will be made available
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

        /**
         * @return the vendor
         */
        public String getVendor()
        {
            return vendor;
        }

        /**
         * @param vendor the vendor to set
         */
        public void setVendor(String vendor)
        {
            this.vendor = vendor;
        }

        /**
         * @return the partner
         */
        public String getPartner()
        {
            return partner;
        }

        /**
         * @param partner the partner to set
         */
        public void setPartner(String partner)
        {
            this.partner = partner;
        }

    }
}