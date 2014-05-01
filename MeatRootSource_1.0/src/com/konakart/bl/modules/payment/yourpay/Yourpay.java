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

package com.konakart.bl.modules.payment.yourpay;

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
 * YourPay IPN module
 */
public class Yourpay extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "yourpay";

    private static String bundleName = BaseModule.basePackage + ".payment.yourpay.Yourpay";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "yourpayMutex";

    // Configuration Keys

    private final static String MODULE_PAYMENT_YOURPAY_STATUS = "MODULE_PAYMENT_YOURPAY_STATUS";

    private final static String MODULE_PAYMENT_YOURPAY_STORENAME = "MODULE_PAYMENT_YOURPAY_STORENAME";

    private final static String MODULE_PAYMENT_YOURPAY_ZONE = "MODULE_PAYMENT_YOURPAY_ZONE";

    private final static String MODULE_PAYMENT_YOURPAY_ORDER_STATUS_ID = "MODULE_PAYMENT_YOURPAY_ORDER_STATUS_ID";

    private final static String MODULE_PAYMENT_YOURPAY_SORT_ORDER = "MODULE_PAYMENT_YOURPAY_SORT_ORDER";

    /**
     * This URL is used to set the referrer on posts to YourPay. It has to match the URL defined in
     * the ourPay merchant account to ensure the caller is allowed for the merchant.
     */
    private final static String MODULE_PAYMENT_YOURPAY_REFERRER_URL = "MODULE_PAYMENT_YOURPAY_REFERRER_URL";

    /**
     * If set to true, the module will request the CCV field, otherwise it won't.
     */
    private final static String MODULE_PAYMENT_YOURPAY_ADD_CCV = "MODULE_PAYMENT_YOURPAY_ADD_CCV";

    /**
     * If set to true, the module will use the YourPay sandbox. Otherwise the live URL will be used.
     */
    private final static String MODULE_PAYMENT_YOURPAY_TEST_MODE = "MODULE_PAYMENT_YOURPAY_TEST_MODE";

    // Message Catalogue Keys

    private final static String MODULE_PAYMENT_YOURPAY_TEXT_TITLE = "module.payment.yourpay.text.title";

    private final static String MODULE_PAYMENT_YOURPAY_TEXT_DESCRIPTION = "module.payment.yourpay.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Yourpay(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_STORENAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_YOURPAY_STORENAME must be set to the YourPay Storename of the merchant.");
        }
        staticData.setYourPayStorename(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_REFERRER_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_YOURPAY_REFERRER_URL must be set to the Url"
                            + " set in the YourPay merchant settings for the Order Submission Form");
        }
        staticData.setYourPayReferrerUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_ADD_CCV);
        if (conf == null)
        {
            staticData.setRequestCCV(false);
        } else
        {
            if (conf.getValue().equalsIgnoreCase("true"))
            {
                staticData.setRequestCCV(true);
            } else
            {
                staticData.setRequestCCV(false);
            }
        }

        conf = getConfiguration(MODULE_PAYMENT_YOURPAY_TEST_MODE);
        if (conf == null)
        {
            staticData.setYourPayTestMode(true);
        } else
        {
            if (conf.getValue().equalsIgnoreCase("false"))
            {
                staticData.setYourPayTestMode(false);
            } else
            {
                staticData.setYourPayTestMode(true);
            }
        }
     }

    /**
     * Return a payment details object for YourPay IPN module
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
         * The yourPayZone zone, if greater than zero, should reference a GeoZone. If the
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
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_YOURPAY_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_YOURPAY_TEXT_TITLE));
        pDetails.setReferrer(sd.getYourPayReferrerUrl());

        // Return now if the full payment details aren't required
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");

        if (sd.isYourPayTestMode())
        {
            // Previous URL:
            //pDetails.setRequestUrl("https://www.staging.yourpay.com/lpcentral/servlet/lppay");
            
            // new URL:
            pDetails.setRequestUrl("https://www.staging.linkpointcentral.com/lpc/servlet/lppay");
        } else
        {
            pDetails.setRequestUrl("https://secure.linkpt.net/lpcentral/servlet/lppay");
        }

        List<NameValue> parmList = new ArrayList<NameValue>();

        // Let YourPay generate an id for the transaction
        // parmList.add(new NameValue("oid", order.getId()));

        parmList.add(new NameValue("comments", "Order #" + order.getId() + " from "
                + info.getStoreName()));

        // Get the Order Total values

        BigDecimal subtotal = null, tax = null, shipping = null, total = null;
        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
            if (ot.getClassName().equals(OrderTotalMgr.ot_subtotal))
            {
                subtotal = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
            if (ot.getClassName().equals(OrderTotalMgr.ot_tax))
            {
                tax = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
            if (ot.getClassName().equals(OrderTotalMgr.ot_shipping))
            {
                shipping = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
        }

        // If we are displaying prices with tax, we have to subtract the tax from the sub-total,
        // otherwise PayPal adds the tax twice
        if (info.isDisplayPriceWithTax() && tax != null && subtotal != null)
        {
            subtotal = subtotal.subtract(tax);
        }

        if (subtotal != null)
        {
            parmList.add(new NameValue("subtotal", subtotal.toString()));
        }

        if (tax != null)
        {
            parmList.add(new NameValue("tax", tax.toString()));
        }

        if (shipping != null)
        {
            parmList.add(new NameValue("shipping", shipping.toString()));
        }

        if (total != null)
        {
            parmList.add(new NameValue("chargetotal", total.toString()));
        }

        parmList.add(new NameValue("storename", sd.getYourPayStorename()));
        parmList.add(new NameValue("txntype", "sale"));

        // With txnorg == eci I kept getting Payment declined due to insufficient funds.
        // moto doesn't seem right but it worked, however, eci must be the correct choice..
        // parmList.add(new NameValue("txnorg", "moto"));
        parmList.add(new NameValue("txnorg", "eci"));

        parmList.add(new NameValue("2000", "Submit"));
        parmList.add(new NameValue("email", order.getCustomerEmail()));
        parmList.add(new NameValue("phone", order.getCustomerTelephone()));
        parmList.add(new NameValue("mode", "payonly"));

        // parmList.add(new NameValue("authenticateTransaction", "false"));

        // We don't set these - instead we control the forwarding in Struts
        // -----------------------------------------------------------------
        // parmList.add(new NameValue("responseURL", yourPayCallbackUrl));
        // yourPaySuccessfulReturnUrl = yourPaySuccessfulReturnUrl.replaceFirst(hostPortSubstitute,
        // info.getHostAndPort());
        // yourPayUnsuccessfulReturnUrl = yourPayUnsuccessfulReturnUrl.replaceFirst(
        // hostPortSubstitute, info.getHostAndPort());
        // parmList.add(new NameValue("responseSuccessURL", yourPaySuccessfulReturnUrl));
        // parmList.add(new NameValue("responseFailURL", yourPayUnsuccessfulReturnUrl));

        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);

        // Set the fields that should be enabled in the UI when gathering Credit Card details
        pDetails.setShowAddr(true);
        pDetails.setShowCVV(sd.isRequestCCV());
        pDetails.setShowPostcode(true);
        pDetails.setShowType(false);
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
        return isAvailable(MODULE_PAYMENT_YOURPAY_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private String yourPayStorename;

        private String yourPayReferrerUrl;

        private boolean yourPayTestMode;

        private boolean requestCCV;

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
         * @return the yourPayStorename
         */
        public String getYourPayStorename()
        {
            return yourPayStorename;
        }

        /**
         * @param yourPayStorename
         *            the yourPayStorename to set
         */
        public void setYourPayStorename(String yourPayStorename)
        {
            this.yourPayStorename = yourPayStorename;
        }

        /**
         * @return the yourPayReferrerUrl
         */
        public String getYourPayReferrerUrl()
        {
            return yourPayReferrerUrl;
        }

        /**
         * @param yourPayReferrerUrl
         *            the yourPayReferrerUrl to set
         */
        public void setYourPayReferrerUrl(String yourPayReferrerUrl)
        {
            this.yourPayReferrerUrl = yourPayReferrerUrl;
        }

        /**
         * @return the yourPayTestMode
         */
        public boolean isYourPayTestMode()
        {
            return yourPayTestMode;
        }

        /**
         * @param yourPayTestMode
         *            the yourPayTestMode to set
         */
        public void setYourPayTestMode(boolean yourPayTestMode)
        {
            this.yourPayTestMode = yourPayTestMode;
        }

        /**
         * @return the requestCCV
         */
        public boolean isRequestCCV()
        {
            return requestCCV;
        }

        /**
         * @param requestCCV
         *            the requestCCV to set
         */
        public void setRequestCCV(boolean requestCCV)
        {
            this.requestCCV = requestCCV;
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
