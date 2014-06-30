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

package com.konakart.bl.modules.payment.elink;

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
 * Elink module. This payment module allows for credit card credentials to be collected directly
 * from a KonaKart page. All communication to the Elink server is done from the KonaKart server.
 */
public class Elink extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "elink";

    private static String bundleName = BaseModule.basePackage + ".payment.elink.Elink";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "elinkMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_ELINK_STATUS = "MODULE_PAYMENT_ELINK_STATUS";

    /**
     * The Elink zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress of
     * the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_ELINK_ZONE = "MODULE_PAYMENT_ELINK_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_ELINK_SORT_ORDER = "MODULE_PAYMENT_ELINK_SORT_ORDER";

    /**
     * The Elink Url used to POST the payment request.
     * "https://ePaysecure1.transfirst.com/elink/authpd.asp"
     */
    private final static String MODULE_PAYMENT_ELINK_REQUEST_URL = "MODULE_PAYMENT_ELINK_REQUEST_URL";

    /**
     * The Elink API account number for this installation
     */
    private final static String MODULE_PAYMENT_ELINK_ACCOUNT_NUM = "MODULE_PAYMENT_ELINK_ACCOUNT_NUM";

    /**
     * The Elink API account password for this installation
     */
    private final static String MODULE_PAYMENT_ELINK_ACCOUNT_PASSWORD = "MODULE_PAYMENT_ELINK_ACCOUNT_PASSWORD";

    /**
     * Used to make test transactions
     */
    private final static String MODULE_PAYMENT_ELINK_TESTMODE = "MODULE_PAYMENT_ELINK_TESTMODE";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_ELINK_SHOW_CVV = "MODULE_PAYMENT_ELINK_SHOW_CVV";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_ELINK_TEXT_TITLE = "module.payment.elink.text.title";

    private final static String MODULE_PAYMENT_ELINK_TEXT_DESCRIPTION = "module.payment.elink.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Elink(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_ELINK_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_ELINK_REQUEST_URL must be set to the URL for"
                            + " sending the request to Elink. (e.g. https://ePaysecure1.transfirst.com/elink/authpd.asp)");
        }
        staticData.setElinkRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_ELINK_ACCOUNT_NUM);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_ELINK_ACCOUNT_NUM must be set to the"
                            + " Elink account number");
        }
        staticData.setElinkAcctNumber(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_ELINK_ACCOUNT_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_ELINK_ACCOUNT_PASSWORD must be set to the"
                            + " Elink password");
        }
        staticData.setElinkAcctPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_ELINK_TESTMODE);
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

        conf = getConfiguration(MODULE_PAYMENT_ELINK_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_ELINK_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_ELINK_SHOW_CVV);
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
     * Return a payment details object for Elink IPN module
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
         * The Elink zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress
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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_ELINK_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_ELINK_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getElinkRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();
        parmList.add(new NameValue("ePayAccountNum", sd.getElinkAcctNumber()));
        parmList.add(new NameValue("Password", sd.getElinkAcctPassword()));
        parmList.add(new NameValue("TransactionCode", "32"));
        parmList.add(new NameValue("InstallmentNum", 1));
        parmList.add(new NameValue("InstallmentOf", 1));
        parmList.add(new NameValue("eCommerce", "Y"));
        parmList.add(new NameValue("DuplicateChecking", "Y"));

        // Elink requires details of the final price - inclusive of tax.
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
            throw new KKException(
                    "An Order Total was not found and so the transaction could not be sent to the Elink payment gateway.");
        }

        parmList.add(new NameValue("TransactionAmount", total.toString()));
        parmList.add(new NameValue("OrderNum", order.getId()));
        parmList.add(new NameValue("TestTransaction", (sd.isTestMode() ? "Y" : "N")));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(false);
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(true);
        pDetails.setShowType(false); // Elink doesn't require the card type
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
        return isAvailable(MODULE_PAYMENT_ELINK_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {

        private int sortOrder = -1;

        // The Elink Url used to POST the payment request.
        // "https://ePaysecure1.transfirst.com/elink/authpd.asp"
        private String elinkRequestUrl;

        // Account number
        private String elinkAcctNumber;

        // Account password
        private String elinkAcctPassword;

        // Test/Live Mode indicator
        private boolean testMode = true;

        // Show the CVV entry field on the UI
        private boolean showCVV = true;

        // zone where Elink will be made available
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
         * @return the elinkRequestUrl
         */
        public String getElinkRequestUrl()
        {
            return elinkRequestUrl;
        }

        /**
         * @param elinkRequestUrl
         *            the elinkRequestUrl to set
         */
        public void setElinkRequestUrl(String elinkRequestUrl)
        {
            this.elinkRequestUrl = elinkRequestUrl;
        }

        /**
         * @return the elinkAcctNumber
         */
        public String getElinkAcctNumber()
        {
            return elinkAcctNumber;
        }

        /**
         * @param elinkAcctNumber
         *            the elinkAcctNumber to set
         */
        public void setElinkAcctNumber(String elinkAcctNumber)
        {
            this.elinkAcctNumber = elinkAcctNumber;
        }

        /**
         * @return the elinkAcctPassword
         */
        public String getElinkAcctPassword()
        {
            return elinkAcctPassword;
        }

        /**
         * @param elinkAcctPassword
         *            the elinkAcctPassword to set
         */
        public void setElinkAcctPassword(String elinkAcctPassword)
        {
            this.elinkAcctPassword = elinkAcctPassword;
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

    }
}