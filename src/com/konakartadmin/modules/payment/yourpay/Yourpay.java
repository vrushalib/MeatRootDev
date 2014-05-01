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

package com.konakartadmin.modules.payment.yourpay;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * YourPay payment module
 */
public class Yourpay extends PaymentModule
{
    /**
     * @return the configuration key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_YOURPAY");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_YOURPAY_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Yourpay";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "yourpay";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[7];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"YourPay Status",
        /* key */"MODULE_PAYMENT_YOURPAY_STATUS",
        /* value */"true",
        /* description */"If set to false, the YourPay module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_YOURPAY_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of YourPay module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"YourPay Payment Zone",
        /* key */"MODULE_PAYMENT_YOURPAY_ZONE",
        /* value */"0",
        /* description */"Zone where YourPay module can be used. Otherwise it is disabled.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"YourPay Storename",
                /* key */"MODULE_PAYMENT_YOURPAY_STORENAME",
                /* value */"YourPay Storename",
                /* description */"The YourPay Merchant Storename (or store number)",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Referrer URL",
        /* key */"MODULE_PAYMENT_YOURPAY_REFERRER_URL",
        /* value */"https://www.yourdomain.com:8780/konakart/EditCartSubmit.action",
        /* description */"URL checked by YourPay to verify the referrer is the same as defined in "
                + "the merchant settings for the Order Submission Form",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Add CCV field",
                /* key */"MODULE_PAYMENT_YOURPAY_ADD_CCV",
                /* value */"false",
                /* description */"Selects whether or not to request the credit card CCV number",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Test Mode",
                /* key */"MODULE_PAYMENT_YOURPAY_TEST_MODE",
                /* value */"true",
                /* description */"Forces KonaKart to use the YourPay Sandbox",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
                /* dateAdd */now);

        return configs;
    }
}
