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

package com.konakart.bl.modules.payment.commideavanguard;

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
 * Commidea Vanguard module
 */
public class CommideaVanguard extends BasePaymentModule implements PaymentInterface
{
    /**
     * Module name - make this the same name as this class
     */
    public static String COMMIDEA_VANGUARD_GATEWAY_CODE = "CommideaVanguard";

    private static String bundleName = BaseModule.basePackage + ".payment."
            + COMMIDEA_VANGUARD_GATEWAY_CODE.toLowerCase() + "." + COMMIDEA_VANGUARD_GATEWAY_CODE;

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = COMMIDEA_VANGUARD_GATEWAY_CODE + "Mutex";

    private static final String hostPortSubstitute = "host:port";

    // Configuration Keys

    /**
     * Used to put the gateway online / offline
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_STATUS = "MODULE_PAYMENT_COMMIDEA_VANGUARD_STATUS";

    /**
     * The order for displaying this payment gateway on the UI
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_SORT_ORDER = "MODULE_PAYMENT_COMMIDEA_VANGUARD_SORT_ORDER";

    /**
     * The Commidea zone, if greater than zero, should reference a GeoZone. If the DeliveryAddress
     * of the order isn't within that GeoZone, then we throw an exception
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_ZONE = "MODULE_PAYMENT_COMMIDEA_VANGUARD_ZONE";

    /**
     * 3D secure uses this URL to redirect the customers browser to after finishing the 3D secure
     * process. If it is in the form http://host:port/konakart/CommideaVanguard2.do, then the string
     * host:port is substituted automatically with the correct value.
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_3D_REDIRECT_URL = "MODULE_PAYMENT_COMMIDEA_VANGUARD_3D_REDIRECT_URL";

    /**
     * Commidea uses this URL to redirect the customers browser to after receiving the post of the
     * credit card details. If it is in the form http://host:port/konakart/CommideaVanguard1.do,
     * then the string host:port is substituted automatically with the correct value.
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_REDIRECT_URL = "MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_REDIRECT_URL";

    /**
     * URL where the request is posted to
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_REQUEST_URL = "MODULE_PAYMENT_COMMIDEA_VANGUARD_REQUEST_URL";

    /**
     * URL where the credit card details are posted to
     */
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_POST_URL = "MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_POST_URL";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_ID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_ID";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_GUID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_GUID";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_PASSCODE = "MODULE_PAYMENT_COMMIDEA_VANGUARD_PASSCODE";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_ID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_ID";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_PASSCODE = "MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_PASSCODE";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_CURRENCY_CODE = "MODULE_PAYMENT_COMMIDEA_VANGUARD_CURRENCY_CODE";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_COUNTRY_CODE = "MODULE_PAYMENT_COMMIDEA_VANGUARD_COUNTRY_CODE";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_URL = "MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_URL";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_NAME = "MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_NAME";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_PROCESSING_IDENTIFIER = "MODULE_PAYMENT_COMMIDEA_VANGUARD_PROCESSING_IDENTIFIER";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_ACQUIRER_ID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_ACQUIRER_ID";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_BANK_ID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_BANK_ID";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_NUMBER = "MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_NUMBER";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_PASSWORD = "MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_PASSWORD";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_BANK_ID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_BANK_ID";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_NUMBER = "MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_NUMBER";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_PASSWORD = "MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_PASSWORD";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_RETURN_TOKEN_ID = "MODULE_PAYMENT_COMMIDEA_VANGUARD_RETURN_TOKEN_ID";

    // Message Catalogue Keys
    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_TEXT_TITLE = "module.payment.commidea.vanguard.text.title";

    private final static String MODULE_PAYMENT_COMMIDEA_VANGUARD_TEXT_DESCRIPTION = "module.payment.commidea.vanguard.text.description";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public CommideaVanguard(KKEngIf eng) throws KKException
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

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_REQUEST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_REQUEST_URL must be set to the URL for"
                            + " sending the request to Commidea. (e.g. https://webcomtest.commidea.com/vanguard/vanguard.aspx)");
        }
        staticData.setRequestUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_POST_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_POST_URL must be set to the URL for"
                            + " posting the credit card details to Commidea. (e.g. https://vg-test.cxmlpg.com/commideagateway/commideagateway.asmx)");
        }
        staticData.setCcDetailPostUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_REDIRECT_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_REDIRECT_URL must be set "
                            + "to the Redirect Url after entering the credit card details");
        }
        staticData.setRedirectUrlCC(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_3D_REDIRECT_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_3D_REDIRECT_URL must be set "
                            + "to the 3D Secure Redirect Url");
        }
        staticData.setRedirectUrl3D(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_ID must be set to "
                            + "the Commidea System Id");
        }
        staticData.setSystemId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_GUID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_GUID must be set to "
                            + "the Commidea System GUID");
        }
        staticData.setSystemGuid(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_PASSCODE);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_PASSCODE must be set to "
                            + "the Commidea Passcode");
        }
        staticData.setPasscode(conf.getValue());
        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_ID must be set to "
                            + "the Commidea Account Id");
        }
        staticData.setAccountId(conf.getValue());
        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_PASSCODE);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_PASSCODE must be set to "
                            + "the Commidea Account Passcode");
        }
        staticData.setAccountPasscode(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_URL must be set to "
                            + "the Merchant URL");
        }
        staticData.setMerchantURL(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_NAME);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_NAME must be set to "
                            + "the Merchant Name");
        }
        staticData.setMerchantName(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_PROCESSING_IDENTIFIER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_PROCESSING_IDENTIFIER must be set to "
                            + "the Processing Identifier");
        }
        staticData.setProcessingIdentifier(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_ACQUIRER_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_ACQUIRER_ID must be set to "
                            + "the Acquirer Id");
        }
        staticData.setAcquirerId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_BANK_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_BANK_ID must be set to "
                            + "the Visa Merchant Bank Id");
        }
        staticData.setVisaBankId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_NUMBER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_NUMBER must be set to "
                            + "the Visa Merchant Number");
        }
        staticData.setVisaNumber(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_PASSWORD must be set to "
                            + "the Visa Merchant Password");
        }
        staticData.setVisaPassword(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_BANK_ID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_BANK_ID must be set to "
                            + "the MasterCard Merchant Bank Id");
        }
        staticData.setMcBankId(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_NUMBER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_NUMBER must be set to "
                            + "the MasterCard Merchant Number");
        }
        staticData.setMcNumber(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_PASSWORD must be set to "
                            + "the MasterCard Merchant Password");
        }
        staticData.setMcPassword(conf.getValue());

        int currencyCode = getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_COMMIDEA_VANGUARD_CURRENCY_CODE, -1);
        if (currencyCode == -1)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_CURRENCY_CODE must be set "
                            + "with a valid ISO 4217 Currency Code.");
        }
        staticData.setCurrencyCode(currencyCode);

        int countryCode = getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_COMMIDEA_VANGUARD_COUNTRY_CODE, -1);
        if (countryCode == -1)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_COUNTRY_CODE must be set "
                            + "with a valid ISO 3166 Country Code.");
        }
        staticData.setCountryCode(countryCode);

        Boolean getTokenId = getConfigurationValueAsBool(
                MODULE_PAYMENT_COMMIDEA_VANGUARD_RETURN_TOKEN_ID, false);
        if (getTokenId == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_COMMIDEA_VANGUARD_RETURN_TOKEN_ID must be set "
                            + "to true or false.");
        }
        staticData.setGetTokenId(getTokenId.booleanValue());

        staticData.setSortOrder(getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_COMMIDEA_VANGUARD_ZONE, 0));

        staticData.setSortOrder(getConfigurationValueAsIntWithDefault(
                MODULE_PAYMENT_COMMIDEA_VANGUARD_SORT_ORDER, 0));
    }

    /**
     * Return a payment details object for Commidea IPN module
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
         * The CommideaZone zone, if greater than zero, should reference a GeoZone. If the
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
        pDetails.setCode(COMMIDEA_VANGUARD_GATEWAY_CODE);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.BROWSER_IN_FRAME_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_COMMIDEA_VANGUARD_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_COMMIDEA_VANGUARD_TEXT_TITLE));
        pDetails.setPreProcessCode(COMMIDEA_VANGUARD_GATEWAY_CODE);

        // Return now if the full payment details aren't required. This happens when the manager
        // just wants a list of payment gateways to display in the UI.
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        List<NameValue> parmList = new ArrayList<NameValue>();

        pDetails.setPostOrGet("post");
        pDetails.setRequestUrl(sd.getRequestUrl());

        // Commidea only requires details of the final price. No tax, sub-total etc.
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
        parmList.add(new NameValue("currencyexponent", scale));
        parmList.add(new NameValue("transactiondisplayamount", total.toPlainString()));
        total = total.movePointRight(scale);
        total.setScale(0);
        parmList.add(new NameValue("transactionamount", total.toPlainString()));
        parmList.add(new NameValue("SystemID", sd.getSystemId()));
        parmList.add(new NameValue("SystemGUID", sd.getSystemGuid()));
        parmList.add(new NameValue("Passcode", sd.getPasscode()));
        parmList.add(new NameValue("mkaccountid", sd.getAccountId()));
        parmList.add(new NameValue("accountpasscode", sd.getAccountPasscode()));
        parmList.add(new NameValue("ccDetailPostUrl", sd.getCcDetailPostUrl()));
        parmList.add(new NameValue("returnurl", sd.getRedirectUrlCC().replaceFirst(
                hostPortSubstitute, info.getHostAndPort())));
        parmList.add(new NameValue("TermUrl", sd.getRedirectUrl3D().replaceFirst(
                hostPortSubstitute, info.getHostAndPort())));
        parmList.add(new NameValue("transactioncurrencycode", sd.getCurrencyCode()));
        parmList.add(new NameValue("currencycode", sd.getCurrencyCode()));
        parmList.add(new NameValue("terminalcountrycode", sd.getCountryCode()));
        parmList.add(new NameValue("merchantcountrycode", sd.getCountryCode()));
        parmList.add(new NameValue("getTokenId", ((sd.isGetTokenId()) ? "true" : "false")));

        parmList.add(new NameValue("merchanturl", sd.getMerchantURL()));
        parmList.add(new NameValue("merchantname", sd.getMerchantName()));

        parmList.add(new NameValue("processingidentifier", sd.getProcessingIdentifier()));
        parmList.add(new NameValue("mkacquirerid", sd.getAcquirerId()));
        parmList.add(new NameValue("visamerchantbankid", sd.getVisaBankId()));
        parmList.add(new NameValue("visamerchantnumber", sd.getVisaNumber()));
        parmList.add(new NameValue("visamerchantpassword", sd.getVisaPassword()));
        parmList.add(new NameValue("mcmmerchantbankid", sd.getMcBankId()));
        parmList.add(new NameValue("mcmmerchantnumber", sd.getMcNumber()));
        parmList.add(new NameValue("mcmmerchantpassword", sd.getMcPassword()));

        parmList.add(new NameValue("cardholdername", order.getBillingName()));
        parmList.add(new NameValue("address1", order.getBillingStreetAddress()));
        parmList.add(new NameValue("postcode", order.getBillingPostcode()));

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
        return isAvailable(MODULE_PAYMENT_COMMIDEA_VANGUARD_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // Account details from Commidea
        private String systemId;

        private String SystemGuid;

        private String passcode;

        private String accountId;

        private String accountPasscode;

        // Redirect URL for credit card details
        private String redirectUrlCC;

        // Redirect URL for 3D secure
        private String redirectUrl3D;

        // The Commidea Url used to POST the XML requests.
        private String requestUrl;

        // The Commidea Url used to POST the credit card details.
        private String ccDetailPostUrl;;

        private int zone;

        private int orderStatusId;

        private int countryCode;

        private int currencyCode;

        private String merchantURL;

        private String merchantName;

        private String processingIdentifier;

        private String acquirerId;

        private String visaBankId;

        private String visaNumber;

        private String visaPassword;

        private String mcBankId;

        private String mcNumber;

        private String mcPassword;

        private boolean getTokenId;

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
         * @return the systemId
         */
        public String getSystemId()
        {
            return systemId;
        }

        /**
         * @param systemId
         *            the systemId to set
         */
        public void setSystemId(String systemId)
        {
            this.systemId = systemId;
        }

        /**
         * @return the systemGuid
         */
        public String getSystemGuid()
        {
            return SystemGuid;
        }

        /**
         * @param systemGuid
         *            the systemGuid to set
         */
        public void setSystemGuid(String systemGuid)
        {
            SystemGuid = systemGuid;
        }

        /**
         * @return the passcode
         */
        public String getPasscode()
        {
            return passcode;
        }

        /**
         * @param passcode
         *            the passcode to set
         */
        public void setPasscode(String passcode)
        {
            this.passcode = passcode;
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
         * @return the accountId
         */
        public String getAccountId()
        {
            return accountId;
        }

        /**
         * @param accountId
         *            the accountId to set
         */
        public void setAccountId(String accountId)
        {
            this.accountId = accountId;
        }

        /**
         * @return the accountPasscode
         */
        public String getAccountPasscode()
        {
            return accountPasscode;
        }

        /**
         * @param accountPasscode
         *            the accountPasscode to set
         */
        public void setAccountPasscode(String accountPasscode)
        {
            this.accountPasscode = accountPasscode;
        }

        /**
         * @return the redirectUrlCC
         */
        public String getRedirectUrlCC()
        {
            return redirectUrlCC;
        }

        /**
         * @param redirectUrlCC
         *            the redirectUrlCC to set
         */
        public void setRedirectUrlCC(String redirectUrlCC)
        {
            this.redirectUrlCC = redirectUrlCC;
        }

        /**
         * @return the redirectUrl3D
         */
        public String getRedirectUrl3D()
        {
            return redirectUrl3D;
        }

        /**
         * @param redirectUrl3D
         *            the redirectUrl3D to set
         */
        public void setRedirectUrl3D(String redirectUrl3D)
        {
            this.redirectUrl3D = redirectUrl3D;
        }

        /**
         * @return the ccDetailPostUrl
         */
        public String getCcDetailPostUrl()
        {
            return ccDetailPostUrl;
        }

        /**
         * @param ccDetailPostUrl
         *            the ccDetailPostUrl to set
         */
        public void setCcDetailPostUrl(String ccDetailPostUrl)
        {
            this.ccDetailPostUrl = ccDetailPostUrl;
        }

        /**
         * @return the countryCode
         */
        public int getCountryCode()
        {
            return countryCode;
        }

        /**
         * @param countryCode
         *            the countryCode to set
         */
        public void setCountryCode(int countryCode)
        {
            this.countryCode = countryCode;
        }

        /**
         * @return the currencyCode
         */
        public int getCurrencyCode()
        {
            return currencyCode;
        }

        /**
         * @param currencyCode
         *            the currencyCode to set
         */
        public void setCurrencyCode(int currencyCode)
        {
            this.currencyCode = currencyCode;
        }

        /**
         * @return the merchantURL
         */
        public String getMerchantURL()
        {
            return merchantURL;
        }

        /**
         * @param merchantURL
         *            the merchantURL to set
         */
        public void setMerchantURL(String merchantURL)
        {
            this.merchantURL = merchantURL;
        }

        /**
         * @return the merchantName
         */
        public String getMerchantName()
        {
            return merchantName;
        }

        /**
         * @param merchantName
         *            the merchantName to set
         */
        public void setMerchantName(String merchantName)
        {
            this.merchantName = merchantName;
        }

        /**
         * @return the processingIdentifier
         */
        public String getProcessingIdentifier()
        {
            return processingIdentifier;
        }

        /**
         * @param processingIdentifier
         *            the processingIdentifier to set
         */
        public void setProcessingIdentifier(String processingIdentifier)
        {
            this.processingIdentifier = processingIdentifier;
        }

        /**
         * @return the acquirerId
         */
        public String getAcquirerId()
        {
            return acquirerId;
        }

        /**
         * @param acquirerId
         *            the acquirerId to set
         */
        public void setAcquirerId(String acquirerId)
        {
            this.acquirerId = acquirerId;
        }

        /**
         * @return the visaBankId
         */
        public String getVisaBankId()
        {
            return visaBankId;
        }

        /**
         * @param visaBankId
         *            the visaBankId to set
         */
        public void setVisaBankId(String visaBankId)
        {
            this.visaBankId = visaBankId;
        }

        /**
         * @return the visaNumber
         */
        public String getVisaNumber()
        {
            return visaNumber;
        }

        /**
         * @param visaNumber
         *            the visaNumber to set
         */
        public void setVisaNumber(String visaNumber)
        {
            this.visaNumber = visaNumber;
        }

        /**
         * @return the visaPassword
         */
        public String getVisaPassword()
        {
            return visaPassword;
        }

        /**
         * @param visaPassword
         *            the visaPassword to set
         */
        public void setVisaPassword(String visaPassword)
        {
            this.visaPassword = visaPassword;
        }

        /**
         * @return the mcBankId
         */
        public String getMcBankId()
        {
            return mcBankId;
        }

        /**
         * @param mcBankId
         *            the mcBankId to set
         */
        public void setMcBankId(String mcBankId)
        {
            this.mcBankId = mcBankId;
        }

        /**
         * @return the mcNumber
         */
        public String getMcNumber()
        {
            return mcNumber;
        }

        /**
         * @param mcNumber
         *            the mcNumber to set
         */
        public void setMcNumber(String mcNumber)
        {
            this.mcNumber = mcNumber;
        }

        /**
         * @return the mcPassword
         */
        public String getMcPassword()
        {
            return mcPassword;
        }

        /**
         * @param mcPassword
         *            the mcPassword to set
         */
        public void setMcPassword(String mcPassword)
        {
            this.mcPassword = mcPassword;
        }

        /**
         * @return the getTokenId
         */
        public boolean isGetTokenId()
        {
            return getTokenId;
        }

        /**
         * @param getTokenId
         *            the getTokenId to set
         */
        public void setGetTokenId(boolean getTokenId)
        {
            this.getTokenId = getTokenId;
        }
    }
}
