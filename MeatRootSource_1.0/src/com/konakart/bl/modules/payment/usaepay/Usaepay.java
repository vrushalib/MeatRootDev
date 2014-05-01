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

package com.konakart.bl.modules.payment.usaepay;

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

/**
 * USA ePay payment module. This payment module allows for credit card credentials to be collected
 * directly from a KonaKart page. All communication to the USA ePay server is done from the KonaKart
 * server.
 */
public class Usaepay extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "usaepay";

    private static String bundleName = BaseModule.basePackage + ".payment.usaepay.Usaepay";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "usaepayMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_USAEPAY_STATUS = "MODULE_PAYMENT_USAEPAY_STATUS";

    /**
     * The UsaePayZone zone, if greater than zero, should reference a GeoZone. If the
     * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_USAEPAY_ZONE = "MODULE_PAYMENT_USAEPAY_ZONE";

    private final static String MODULE_PAYMENT_USAEPAY_ORDER_STATUS_ID = "MODULE_PAYMENT_USAEPAY_ORDER_STATUS_ID";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_USAEPAY_SORT_ORDER = "MODULE_PAYMENT_USAEPAY_SORT_ORDER";

    /**
     * To perform a transaction in test mode
     */
    private final static String MODULE_PAYMENT_USAEPAY_TESTMODE = "MODULE_PAYMENT_USAEPAY_TESTMODE";

    /**
     * To show post code field
     */
    private final static String MODULE_PAYMENT_USAEPAY_SHOW_POSTCODE = "MODULE_PAYMENT_USAEPAY_SHOW_POSTCODE";

    /**
     * To show address field
     */
    private final static String MODULE_PAYMENT_USAEPAY_SHOW_ADDRESS = "MODULE_PAYMENT_USAEPAY_SHOW_ADDRESS";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_USAEPAY_SHOW_CVV = "MODULE_PAYMENT_USAEPAY_SHOW_CVV";

    /**
     * The merchant key
     */
    private final static String MODULE_PAYMENT_USAEPAY_KEY = "MODULE_PAYMENT_USAEPAY_KEY";

    /**
     * The UsaePay Url used to POST the payment request. "https://www.usaepay.com/gate.php "
     */
    private final static String MODULE_PAYMENT_USAEPAY_REQUEST_URL = "MODULE_PAYMENT_USAEPAY_REQUEST_URL";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_USAEPAY_TEXT_TITLE = "module.payment.usaepay.text.title";

    private final static String MODULE_PAYMENT_USAEPAY_TEXT_DESCRIPTION = "module.payment.usaepay.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Usaepay(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_KEY);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_USAEPAY_KEY must be set to the key"
                            + " released by USA ePay");
        }
        staticData.setUsaePayKey(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_USAEPAY_REQUEST_URL must be set to the URL for"
                            + " sending the request to USA ePay. (i.e. https://www.usaepay.com/gate.php)");
        }
        staticData.setUsaePayRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_TESTMODE);
        if (conf == null)
        {
            staticData.setTestMode(true);
        } else
        {
            if (conf.getValue().trim().equalsIgnoreCase("false"))
            {
                staticData.setTestMode(false);
            } else
            {
                staticData.setTestMode(true);
            }
        }

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_SHOW_ADDRESS);
        if (conf == null)
        {
            staticData.setShowAddress(true);
        } else
        {
            if (conf.getValue().trim().equalsIgnoreCase("false"))
            {
                staticData.setShowAddress(false);
            } else
            {
                staticData.setShowAddress(true);
            }
        }

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_SHOW_POSTCODE);
        if (conf == null)
        {
            staticData.setShowPostcode(true);
        } else
        {
            if (conf.getValue().trim().equalsIgnoreCase("false"))
            {
                staticData.setShowPostcode(false);
            } else
            {
                staticData.setShowPostcode(true);
            }
        }

        conf = getConfiguration(MODULE_PAYMENT_USAEPAY_SHOW_CVV);
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
     * Return a payment details object for USA ePay payment module
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
         * The USA ePay zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
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
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.SERVER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_USAEPAY_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_USAEPAY_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getUsaePayRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        parmList.add(new NameValue("UMkey", sd.getUsaePayKey()));

        // USA ePay only requires details of the final price. No tax, subtotal etc.
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

        parmList.add(new NameValue("UMamount", total.toString()));
        parmList.add(new NameValue("UMinvoice", order.getId()));

        parmList.add(new NameValue("UMtestmode", (sd.isTestMode() ? "1" : "0")));
        parmList.add(new NameValue("UMcustid", order.getCustomerId()));
        parmList.add(new NameValue("UMcurrency", order.getCurrency().getCode()));

        // Set the billing name from the billing address Id

        String[] bNames = getFirstAndLastNamesFromAddress(order.getBillingAddrId());
        if (bNames != null)
        {
            parmList.add(new NameValue("UMbillfname", bNames[0]));
            parmList.add(new NameValue("UMbilllname", bNames[1]));
        }
        
        // Set the billing address
        parmList.add(new NameValue("UMbillcompany", order.getCustomerCompany()));
        parmList.add(new NameValue("UMbillstreet", order.getBillingStreetAddress()));
        parmList.add(new NameValue("UMbillcity", order.getBillingCity()));
        parmList.add(new NameValue("UMbillstate", getZoneCodeForZoneName(order.getBillingState()))); 
        parmList.add(new NameValue("UMbillzip", order.getBillingPostcode()));
        parmList.add(new NameValue("UMbillphone", order.getCustomerTelephone()));
        parmList.add(new NameValue("UMcustemail", order.getCustomerEmail()));
        parmList.add(new NameValue("UMbillcountry", order.getBillingCountry()));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(sd.isShowAddress());
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(sd.isShowPostcode());
        pDetails.setShowType(false); // USAePay doesn't require the card type

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
        return isAvailable(MODULE_PAYMENT_USAEPAY_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // The USA ePay Url used to POST the payment request.
        // "https://www.usaepay.com/gate.php "
        private String usaePayRequestUrl;

        // The merchant key
        private String usaePayKey;

        // Put the gateway in test mode
        private boolean testMode = true;

        // Show the post code field on the UI
        private boolean showPostcode = true;

        // Show the address field on the UI
        private boolean showAddress = true;

        // Show the CVV field on the UI
        private boolean showCVV = true;

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
         * @return the usaePayRequestUrl
         */
        public String getUsaePayRequestUrl()
        {
            return usaePayRequestUrl;
        }

        /**
         * @param usaePayRequestUrl
         *            the usaePayRequestUrl to set
         */
        public void setUsaePayRequestUrl(String usaePayRequestUrl)
        {
            this.usaePayRequestUrl = usaePayRequestUrl;
        }

        /**
         * @return the usaePayKey
         */
        public String getUsaePayKey()
        {
            return usaePayKey;
        }

        /**
         * @param usaePayKey
         *            the usaePayKey to set
         */
        public void setUsaePayKey(String usaePayKey)
        {
            this.usaePayKey = usaePayKey;
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
