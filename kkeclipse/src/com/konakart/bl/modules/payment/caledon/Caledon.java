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

package com.konakart.bl.modules.payment.caledon;

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
 * Caledon module. This payment module allows for credit card credentials to be collected directly
 * from a KonaKart page. All communication to the Caledon server is done from the KonaKart server.
 */
public class Caledon extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name
    private static String code = "caledon";

    private static String bundleName = BaseModule.basePackage + ".payment.caledon.Caledon";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "caledonMutex";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_CALEDON_STATUS = "MODULE_PAYMENT_CALEDON_STATUS";

    /**
     * The Caledon zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress of
     * the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_CALEDON_ZONE = "MODULE_PAYMENT_CALEDON_ZONE";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_CALEDON_SORT_ORDER = "MODULE_PAYMENT_CALEDON_SORT_ORDER";

    /**
     * The Caledon Url used to POST the payment request. https://lt3a.caledoncard.com/
     */
    private final static String MODULE_PAYMENT_CALEDON_REQUEST_URL = "MODULE_PAYMENT_CALEDON_REQUEST_URL";

    /**
     * The Terminal ID used for the Caledon service
     */
    // private final static String MODULE_PAYMENT_CALEDON_TERMINAL_ID =
    // "MODULE_PAYMENT_CALEDON_TERMINAL_ID";

    /**
     * Password for this terminal ID
     */
    // private final static String MODULE_PAYMENT_CALEDON_PASSWORD =
    // "MODULE_PAYMENT_CALEDON_PASSWORD";

    /**
     * To show CVV field
     */
    private final static String MODULE_PAYMENT_CALEDON_SHOW_CVV = "MODULE_PAYMENT_CALEDON_SHOW_CVV";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_CALEDON_TEXT_TITLE = "module.payment.caledon.text.title";

    private final static String MODULE_PAYMENT_CALEDON_TEXT_DESCRIPTION = "module.payment.caledon.text.description";

    private final static String MODULE_PAYMENT_CALEDON_TERMINAL_ID_AMEX = "MODULE_PAYMENT_CALEDON_TERMINAL_ID_AMEX";

    private final static String MODULE_PAYMENT_CALEDON_PASSWORD_AMEX = "MODULE_PAYMENT_CALEDON_PASSWORD_AMEX";

    private final static String MODULE_PAYMENT_CALEDON_TERMINAL_ID = "MODULE_PAYMENT_CALEDON_TERMINAL_ID";

    private final static String MODULE_PAYMENT_CALEDON_PASSWORD = "MODULE_PAYMENT_CALEDON_PASSWORD";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Caledon(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_CALEDON_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_CALEDON_REQUEST_URL must be set to the URL for"
                            + " sending the request to Caledon. (e.g. https://lt3a.caledoncard.com/)");
        }
        staticData.setCaledonRequestUrl(conf.getValue());

        // conf = getConfiguration(MODULE_PAYMENT_CALEDON_TERMINAL_ID);
        // if (conf == null)
        // {
        // throw new KKException(
        // "The Configuration MODULE_PAYMENT_CALEDON_TERMINAL_ID must be set to the"
        // + " Terminal ID for this installation");
        // }
        // caledonTerminalId = conf.getValue();
        //
        // /*
        // * Password isn't always required if the ip address is fixed
        // */
        // conf = getConfiguration(MODULE_PAYMENT_CALEDON_PASSWORD);
        // caledonPassword = conf.getValue();

        staticData.setAmexTermId(getConfigurationValue(MODULE_PAYMENT_CALEDON_TERMINAL_ID_AMEX));
        staticData.setAmexPwd(getConfigurationValue(MODULE_PAYMENT_CALEDON_PASSWORD_AMEX));
        staticData.setOthersTermId(getConfigurationValue(MODULE_PAYMENT_CALEDON_TERMINAL_ID));
        staticData.setOthersPwd(getConfigurationValue(MODULE_PAYMENT_CALEDON_PASSWORD));
        staticData.setZone(getConfigurationValueAsIntWithDefault(MODULE_PAYMENT_CALEDON_ZONE, 0));
        staticData.setSortOrder(getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_CALEDON_SORT_ORDER, 0));
        staticData.setShowCVV(getConfigurationValueAsBool(MODULE_PAYMENT_CALEDON_SHOW_CVV, true));
    }

    /**
     * Return a payment details object for Caledon module
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
         * The caledonZone zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
         */
        if (sd.getZone() > 0)
        {
            checkZone(info, sd.getZone());
        }

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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_CALEDON_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_CALEDON_TEXT_TITLE));

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("get");
        pDetails.setRequestUrl(sd.getCaledonRequestUrl());

        List<NameValue> parmList = new ArrayList<NameValue>();

        /*
         * Terminal ID and password are added in CaledonAction since they can vary based on the
         * Credit Card used
         */
        // parmList.add(new NameValue("TERMID", caledonTerminalId));
        // if (caledonPassword != null && caledonPassword.length() > 0)
        // {
        // parmList.add(new NameValue("PASS", caledonPassword));
        // }
        parmList.add(new NameValue("TYPE", "S"));

        // caledon requires details of the final price in cents - inclusive of tax.
        int total = 0;
        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = (ot.getValue().multiply(new BigDecimal(100))).intValue();
            }
        }

        parmList.add(new NameValue("AMT", String.valueOf(total)));
        parmList.add(new NameValue("REF", order.getId()));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be visible in the UI when gathering Credit Card details
        pDetails.setShowAddr(false);
        pDetails.setShowCVV(sd.isShowCVV());
        pDetails.setShowPostcode(false);
        pDetails.setShowType(true); // Needed to select the correct terminal Id
        pDetails.setShowOwner(false);

        pDetails.setCustom1(sd.getAmexTermId());
        pDetails.setCustom2(sd.getAmexPwd());
        pDetails.setCustom3(sd.getOthersTermId());
        pDetails.setCustom4(sd.getOthersPwd());
        
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
        return isAvailable(MODULE_PAYMENT_CALEDON_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // The Url used to POST the payment request.
        // "https://lt3a.caledoncard.com/"
        private String caledonRequestUrl;

        // Show the CVV field on the UI
        private boolean showCVV = true;

        private int zone;

        private int orderStatusId;

        private String amexTermId;

        private String amexPwd;

        private String othersTermId;

        private String othersPwd;

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

        /**
         * @return the caledonRequestUrl
         */
        public String getCaledonRequestUrl()
        {
            return caledonRequestUrl;
        }

        /**
         * @param caledonRequestUrl
         *            the caledonRequestUrl to set
         */
        public void setCaledonRequestUrl(String caledonRequestUrl)
        {
            this.caledonRequestUrl = caledonRequestUrl;
        }

        /**
         * @return the amexTermId
         */
        public String getAmexTermId()
        {
            return amexTermId;
        }

        /**
         * @param amexTermId
         *            the amexTermId to set
         */
        public void setAmexTermId(String amexTermId)
        {
            this.amexTermId = amexTermId;
        }

        /**
         * @return the amexPwd
         */
        public String getAmexPwd()
        {
            return amexPwd;
        }

        /**
         * @param amexPwd
         *            the amexPwd to set
         */
        public void setAmexPwd(String amexPwd)
        {
            this.amexPwd = amexPwd;
        }

        /**
         * @return the othersTermId
         */
        public String getOthersTermId()
        {
            return othersTermId;
        }

        /**
         * @param othersTermId
         *            the othersTermId to set
         */
        public void setOthersTermId(String othersTermId)
        {
            this.othersTermId = othersTermId;
        }

        /**
         * @return the othersPwd
         */
        public String getOthersPwd()
        {
            return othersPwd;
        }

        /**
         * @param othersPwd
         *            the othersPwd to set
         */
        public void setOthersPwd(String othersPwd)
        {
            this.othersPwd = othersPwd;
        }
    }
}