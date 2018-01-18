//
// (c) 2004-2015 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakartadmin.modules.payment.usaepay;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * USA ePay payment module
 * 
 */
public class Usaepay extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_USAEPAY");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_USAEPAY_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Usaepay";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "usaepay";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[9];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"USA ePay Status",
        /* key */"MODULE_PAYMENT_USAEPAY_STATUS",
        /* value */"true",
        /* description */"If set to false, the USA ePay module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true'='true','false'='false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_USAEPAY_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of USA ePay module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"USA ePay Payment Zone",
        /* key */"MODULE_PAYMENT_USAEPAY_ZONE",
        /* value */"0",
        /* description */"Zone where USA ePay module can be used. Otherwise it is disabled.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_USAEPAY_REQUEST_URL",
        /* value */"https://www.usaepay.com/gate.php",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"USA ePay Merchant key",
        /* key */"MODULE_PAYMENT_USAEPAY_KEY",
        /* value */"",
        /* description */"The USA ePay merchant key",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"USA ePay Test Mode",
        /* key */"MODULE_PAYMENT_USAEPAY_TESTMODE",
        /* value */"true",
        /* description */"If set to true, the USA ePay module will be in test mode",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true'='true','false'='false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Show PostCode field",
                /* key */"MODULE_PAYMENT_USAEPAY_SHOW_POSTCODE",
                /* value */"true",
                /* description */"If set to true, the postcode entry field will be shown when entering credit card details",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true'='true','false'='false')",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Show Address field",
                /* key */"MODULE_PAYMENT_USAEPAY_SHOW_ADDRESS",
                /* value */"true",
                /* description */"If set to true, the address entry field will be shown when entering credit card details",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true'='true','false'='false')",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Show CVV field",
                /* key */"MODULE_PAYMENT_USAEPAY_SHOW_CVV",
                /* value */"true",
                /* description */"If set to true, the CVV entry field will be shown when entering credit card details",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true'='true','false'='false')",
                /* dateAdd */now);

        return configs;
    }
}
