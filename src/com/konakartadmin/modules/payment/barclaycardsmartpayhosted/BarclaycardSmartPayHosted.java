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

package com.konakartadmin.modules.payment.barclaycardsmartpayhosted;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * Barclaycard SmartPay Hosted payment module
 */
public class BarclaycardSmartPayHosted extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_BC_SPAY_HOSTED");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_BC_SPAY_HOSTED_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "BarclaycardSmartPayHosted";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "barclaycardsmartpayhosted";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[13];
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
                /* title */"Barclaycard SmartPay Hosted Status",
                /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_STATUS",
                /* value */"false",
                /* description */"If set to false, the Barclaycard SmartPay Hosted module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now);

        // 2
        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_SORT_ORDER",
                /* value */"0",
                /* description */"Sort Order of Barclaycard SmartPay Hosted module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
                /* title */"Barclaycard SmartPay Hosted Payment Zone",
                /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_ZONE",
                /* value */"0",
                /* description */"Zone where Barclaycard SmartPay Hosted module can be used. Otherwise it is disabled.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"tep_get_zone_class_title",
                /* setFun */"tep_cfg_pull_down_zone_classes(",
                /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"Callback KonaKart Username",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_USERNAME",
        /* value */"user@konakart.com",
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
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_CALLBACK_PASSWORD",
        /* value */"password",
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
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_HMAC_KEY",
        /* value */"Your HMAC Key",
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
        /* title */"Skin Code",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_SKIN_CODE",
        /* value */"Your Skin Code",
        /* description */"Skin Code. Used to identify which skin SmartPay should use."
                + " You have to create your skin in your Merchant Account on Barclaycard SmartPay."
                + " Once you have done so, use the skin code here to identify the skin",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_REQUEST_URL",
        /* value */"https://test.barclaycardsmartpay.com/hpp/pay.shtml",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Response URL",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_RESPONSE_URL",
        /* value */"http://host:port/konakart/BarclaycardSmartPayHostedResponse.action",
        /* description */"URL to return to when leaving the Barclaycard SmartPay web site after "
                + "a transaction (which may or may not have been succesful",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 10
        configs[i] = new KKConfiguration(
        /* title */"Merchant Account",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_MERCHANT_ACCOUNT",
        /* value */"Your Merchant Account",
        /* description */"Barclaycard SmartPay Merchant Account Name",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 11
        configs[i] = new KKConfiguration(
                /* title */"Require HTTP Authentication on Notifications",
                /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_AUTH",
                /* value */"false",
                /* description */"If set to true, the Barclaycard SmartPay Hosted notification messages will be authenticated."
                        + " The credentials below for this must match those in your Merchant Account.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now);

        // 12
        configs[i] = new KKConfiguration(
        /* title */"HTTP Notification Username",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_USERNAME",
        /* value */"Your HTTP Username",
        /* description */"HTTP user for Notifications from SmartPay. "
                + "Must match the user name specified in your Merchant Account.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 13
        configs[i] = new KKConfiguration(
        /* title */"HTTP Notification Password",
        /* key */"MODULE_PAYMENT_BC_SPAY_HOSTED_HTTP_PASSWORD",
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