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

package com.konakart.bl.modules.payment.eway_au;

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
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * eWay Australia payment module. This payment module allows for credit card credentials to be
 * collected directly from a KonaKart page. All communication to the eWay server is done from the
 * KonaKart server.
 */
public class Eway_au extends BasePaymentModule implements PaymentInterface
{
    /** Module name must be the same as the class name although it can be all in lowercase */
    public static final String EWAY_AU_GATEWAY_CODE = "eway_au";

    private static String bundleName = BaseModule.basePackage + ".payment.eway_au.Eway_au";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "eWay_AUMutex";

    // Constants for request

    /**
     * Payment Account (CC Number)
     */
    public static final String EWAY_AU_MERCHANT_ID = "MERCHANT_ID";

    /**
     * Payment Amount
     */
    public static final String EWAY_AU_PAYMENT_AMOUNT = "PAYMENT_AMOUNT";

    /**
     * CCV number
     */
    public static final String EWAY_AU_CARD_CVV2 = "CARD_CVV2";

    /**
     * CC number
     */
    public static final String EWAY_AU_CARD_NUMBER = "CARD_NUMBER";

    /**
     * CC Expiry Month
     */
    public static final String EWAY_AU_CARD_EXPIRY_MONTH = "CARD_EXPIRY_MONTH";

    /**
     * CC Expiry Year
     */
    public static final String EWAY_AU_CARD_EXPIRY_YEAR = "CARD_EXPIRY_YEAR";

    /**
     * Card holder's name
     */
    public static final String EWAY_AU_CARDHOLDERS_NAME = "CARDHOLDERS_NAME";

    /**
     * Customer's email address
     */
    public static final String EWAY_AU_CUST_EMAIL = "CUSTOMER_EMAIL";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_EWAY_AU_STATUS = "MODULE_PAYMENT_EWAY_AU_STATUS";

    /**
     * The eWay zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress of
     * the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_EWAY_AU_ZONE = "MODULE_PAYMENT_EWAY_AU_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_EWAY_AU_SORT_ORDER = "MODULE_PAYMENT_EWAY_AU_SORT_ORDER";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_EWAY_AU_SHOW_CVV = "MODULE_PAYMENT_EWAY_AU_SHOW_CVV";

    /**
     * The merchant account Id
     */
    private final static String MODULE_PAYMENT_EWAY_AU_ACCOUNT_ID = "MODULE_PAYMENT_EWAY_AU_ACCOUNT_ID";

    /**
     * The eWay Url used to POST the payment request.
     * "https://www.eway.com.au/gateway_cvn/xmlpayment.asp"
     */
    private final static String MODULE_PAYMENT_EWAY_AU_REQUEST_URL = "MODULE_PAYMENT_EWAY_AU_REQUEST_URL";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_EWAY_AU_TEXT_TITLE = "module.payment.eway_au.text.title";

    private final static String MODULE_PAYMENT_EWAY_AU_TEXT_DESCRIPTION = "module.payment.eway_au.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Eway_au(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_EWAY_AU_ACCOUNT_ID);
        if (conf == null)
        {
            throw new KKException("The Configuration parameter "
                    + MODULE_PAYMENT_EWAY_AU_ACCOUNT_ID + " must be set"
                    + " to the eWay Account ID");
        }
        staticData.setMerchantAcctId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_EWAY_AU_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration parameter "
                            + MODULE_PAYMENT_EWAY_AU_REQUEST_URL
                            + " must be set to the URL for"
                            + " sending the request to eWay. (e.g. https://www.eway.com.au/gateway_cvn/xmlpayment.asp)");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_EWAY_AU_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_EWAY_AU_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_EWAY_AU_SHOW_CVV);
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
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, info
                .getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        PaymentDetails pDetails = new PaymentDetails();
        pDetails.setCode(EWAY_AU_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.SERVER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_EWAY_AU_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_EWAY_AU_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        // eWay only requires details of the final price. No tax, subtotal etc.
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

        // Convert to cents
        if (scale != 2)
        {
            if (log.isWarnEnabled())
            {
                log.warn("This gateway code assumes the currency has two decimal places");
            }
        }
        total = total.multiply(new BigDecimal("100"));
        total = total.setScale(0);

        parmList.add(new NameValue(EWAY_AU_MERCHANT_ID, URLEncoder.encode(sd.getMerchantAcctId(),
                "UTF-8")));
        parmList.add(new NameValue(EWAY_AU_PAYMENT_AMOUNT, URLEncoder.encode(total.toString(),
                "UTF-8")));
        parmList.add(new NameValue(EWAY_AU_CUST_EMAIL, URLEncoder.encode(order.getCustomerEmail(),
                "UTF-8")));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(sd.isShowAddress());
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(sd.isShowPostcode());
        pDetails.setShowType(false); // eWay doesn't require the card type

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
        return isAvailable(MODULE_PAYMENT_EWAY_AU_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // The eWay Url used to POST the payment request.
        private String requestUrl;

        // The merchant account id
        private String merchantAcctId;

        // Show the post code field on the UI
        private boolean showPostcode = false;

        // Show the address field on the UI
        private boolean showAddress = false;

        // Show the CVV field on the UI
        private boolean showCVV = true;

        private int zone;

        private int orderStatusId = 0;

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
         * @return the merchantAcctId
         */
        public String getMerchantAcctId()
        {
            return merchantAcctId;
        }

        /**
         * @param merchantAcctId
         *            the merchantAcctId to set
         */
        public void setMerchantAcctId(String merchantAcctId)
        {
            this.merchantAcctId = merchantAcctId;
        }

        /**
         * @return the showPostcode
         */
        public boolean isShowPostcode()
        {
            return showPostcode;
        }

        /**
         * @param showPostcode
         *            the showPostcode to set
         */
        public void setShowPostcode(boolean showPostcode)
        {
            this.showPostcode = showPostcode;
        }

        /**
         * @return the showAddress
         */
        public boolean isShowAddress()
        {
            return showAddress;
        }

        /**
         * @param showAddress
         *            the showAddress to set
         */
        public void setShowAddress(boolean showAddress)
        {
            this.showAddress = showAddress;
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
    }
}
