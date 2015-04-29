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

package com.konakartadmin.modules.payment.barclaycardsmartpayapi;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * Barclaycard SmartPay API payment module
 */
public class BarclaycardSmartPayApi extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_BC_SPAY_API");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_BC_SPAY_API_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "BarclaycardSmartPayApi";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "barclaycardsmartpayapi";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[15];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        // 1
        configs[i] = new KKConfiguration(
                /* title */"Barclaycard SmartPay API Status",
                /* key */"MODULE_PAYMENT_BC_SPAY_API_STATUS",
                /* value */"true",
                /* description */"If set to false, the Barclaycard SmartPay API module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now);

        // 2
        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_PAYMENT_BC_SPAY_API_SORT_ORDER",
                /* value */"0",
                /* description */"Sort Order of Barclaycard SmartPay API module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
                /* title */"Barclaycard SmartPay API Payment Zone",
                /* key */"MODULE_PAYMENT_BC_SPAY_API_ZONE",
                /* value */"0",
                /* description */"Zone where Barclaycard SmartPay API module can be used. Otherwise it is disabled.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"tep_get_zone_class_title",
                /* setFun */"tep_cfg_pull_down_zone_classes(",
                /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"Callback KonaKart Username",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_CALLBACK_USERNAME",
        /* value */"admin@konakart.com",
        /* description */"Valid username for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
        /* title */"Callback KonaKart Password",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_CALLBACK_PASSWORD",
        /* value */"princess",
        /* description */"Valid password for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);
        
        // 6
        configs[i] = new KKConfiguration(
        /* title */"HMAC Key - Shared Key",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_HMAC_KEY",
        /* value */"Your_HMAC_Key",
        /* description */"Shared key (HMAC = Hash Message Authentication Code). Used to encrypt "
                + " and decrypt communication between KonaKart and Barclaycard SmartPay."
                + " Set this same value in your Merchant Account on Barclaycard SmartPay.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        // 7
        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_REQUEST_URL",
        /* value */"https://pal-test.barclaycardsmartpay.com/pal/servlet/soap/Payment",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Enable 3D Secure",
        /* key */"MODULE_PAYMENT_BC_SPAY_3D_STATUS",
        /* value */"false",
        /* description */"If set to true, a 3D secure check will be included otherwise no 3D "
                + "Secure check will be carried out",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        // 9
        configs[i] = new KKConfiguration(
        /* title */"3D Secure Response URL",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_3D_RESPONSE_URL",
        /* value */"http://host:port/konakart/BarclaycardSmartPayApiResponse.action",
        /* description */"URL to return to after a 3D Secure validation.  Not used if the 3D"
                + " Secure is not enabled",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 10
        configs[i] = new KKConfiguration(
        /* title */"Merchant Account",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_MERCHANT_ACC",
        /* value */"Your Merchant Account",
        /* description */"Barclaycard SmartPay API Merchant Account Name",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 11
        configs[i] = new KKConfiguration(
        /* title */"SmartPay User Id",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_USER_ID",
        /* value */"Your SmartPay User Id",
        /* description */"Merchant's SmartPay account user id.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 12
        configs[i] = new KKConfiguration(
        /* title */"SmartPay Password",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_PASSWORD",
        /* value */"Your SmartPay Password",
        /* description */"Merchant's SmartPay account password.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        // 13
        configs[i] = new KKConfiguration(
                /* title */"Require HTTP Authentication on Notifications",
                /* key */"MODULE_PAYMENT_BC_SPAY_API_HTTP_AUTH",
                /* value */"false",
                /* description */"If set to true, the Barclaycard SmartPay API notification messages will be authenticated."
                        + " The credentials below for this must match those in your Merchant Account.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now);

        // 14
        configs[i] = new KKConfiguration(
        /* title */"HTTP Notification Username",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_HTTP_USERNAME",
        /* value */"Your HTTP Username",
        /* description */"HTTP user for Notifications from SmartPay. "
                + "Must match the user name specified in your Merchant Account.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 15
        configs[i] = new KKConfiguration(
        /* title */"HTTP Notification Password",
        /* key */"MODULE_PAYMENT_BC_SPAY_API_HTTP_PASSWORD",
        /* value */"Your HTTP Password",
        /* description */"HTTP password for Notifications from SmartPay. "
                + "Must match the password specified in your Merchant Account.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        return configs;
    }
}