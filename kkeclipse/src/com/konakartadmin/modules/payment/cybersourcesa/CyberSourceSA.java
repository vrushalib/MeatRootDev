//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakartadmin.modules.payment.cybersourcesa;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * CyberSource SA (Secure Acceptance) payment module
 */
public class CyberSourceSA extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_CYBERSOURCESA");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_CYBERSOURCESA_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "CyberSourceSA";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "cybersourcesa";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[12];
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
                /* title */"CyberSource SA Status",
                /* key */"MODULE_PAYMENT_CYBERSOURCESA_STATUS",
                /* value */"true",
                /* description */"If set to false, the CyberSource Hosted Order Post module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now,
                /* return_by_api */true);

        // 2
        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_PAYMENT_CYBERSOURCESA_SORT_ORDER",
                /* value */"0",
                /* description */"Sort Order of CyberSource Hosted Order Post module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
                /* title */"CyberSource Payment Zone",
                /* key */"MODULE_PAYMENT_CYBERSOURCESA_ZONE",
                /* value */"0",
                /* description */"Zone where the CyberSource module can be used. Otherwise it is disabled.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"tep_get_zone_class_title",
                /* setFun */"tep_cfg_pull_down_zone_classes(",
                /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
                /* title */"Shared Secret Part 1",
                /* key */"MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET1",
                /* value */"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQ",
                /* description */"Shared secret part1 (HMAC = Hash Message Authentication Code). Used to encrypt"
                        + " and decrypt communication between KonaKart and CyberSource."
                        + " Generate this value using your Merchant Account on CyberSource."
                        + " Part1 and Part2 are concatenated.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
                /* title */"Shared Secret Part2",
                /* key */"MODULE_PAYMENT_CYBERSOURCESA_SHARED_SECRET2",
                /* value */"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQ",
                /* description */"Shared secret part2 (HMAC = Hash Message Authentication Code). Used to encrypt"
                        + " and decrypt communication between KonaKart and CyberSource."
                        + " Generate this value using your Merchant Account on CyberSource."
                        + " Part1 and Part2 are concatenated.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"Access Key",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_ACCESS_KEY",
        /* value */"3112245556300176056165",
        /* description */"Access key.  Used to encrypt "
                + " and decrypt communication between KonaKart and CyberSource."
                + " Generate this value using your Merchant Account on CyberSource.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_REQUEST_URL",
        /* value */"https://testsecureacceptance.cybersource.com/pay",
        /* description */"URL used by KonaKart to send the transaction details. "
                + "(Production URL is https://secureacceptance.cybersource.com/pay)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Response URL",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_RESPONSE_URL",
        /* value */"http://host:port/konakart/CyberSourceSAResponse.action",
        /* description */"URL to return to after a CyberSource decision",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 9
        configs[i] = new KKConfiguration(
        /* title */"Environment",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_ENVIRONMENT",
        /* value */"TEST",
        /* description */"Environment - 'TEST' or 'PRODUCTION'",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('TEST', 'PRODUCTION')",
        /* dateAdd */now);

        // 10
        configs[i] = new KKConfiguration(
        /* title */"Merchant Account Id",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_MERCHANT_ACC",
        /* value */"Your Merchant Account Id",
        /* description */"CyberSource Merchant Account Id",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 11
        configs[i] = new KKConfiguration(
        /* title */"Merchant Profile Id",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_PROFILE_ID",
        /* value */"Your Merchant Profile Id",
        /* description */"CyberSource Merchant Profile Id",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 12
        configs[i] = new KKConfiguration(
        /* title */"Gateway Version",
        /* key */"MODULE_PAYMENT_CYBERSOURCESA_VERSION",
        /* value */"7",
        /* description */"CyberSource Gateway version number",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}