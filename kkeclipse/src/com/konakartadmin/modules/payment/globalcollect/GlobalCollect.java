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

package com.konakartadmin.modules.payment.globalcollect;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * GlobalCollect payment module
 */
public class GlobalCollect extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_GLOBALCOLLECT");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_GLOBALCOLLECT_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "GlobalCollect";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "globalcollect";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[10];
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
        /* title */"GlobalCollect Status",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_STATUS",
        /* value */"true",
        /* description */"If set to false, the GlobalCollect module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now,
        /* return_by_api */true);

        // 2
        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_PAYMENT_GLOBALCOLLECT_SORT_ORDER",
                /* value */"0",
                /* description */"Sort Order of GlobalCollect module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
                /* title */"GlobalCollect Payment Zone",
                /* key */"MODULE_PAYMENT_GLOBALCOLLECT_ZONE",
                /* value */"0",
                /* description */"Zone where the GlobalCollect module can be used. Otherwise it is disabled.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"tep_get_zone_class_title",
                /* setFun */"tep_cfg_pull_down_zone_classes(",
                /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_REQUEST_URL",
        /* value */"https://ps.gcsip.nl/wdl/wdl",
        /* description */"URL used by KonaKart to send the transaction details. "
                + "(Production URL is https://ps.gcsip.com/wdl/wdl)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
        /* title */"Response URL",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL",
        /* value */"http://host:port/konakart/GlobalCollectResponse.action",
        /* description */"URL to return to after a GlobalCollect decision",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"Merchant Account Id",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC",
        /* value */"Your Merchant Account Id",
        /* description */"GlobalCollect Merchant Account Id",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"Server IP Address",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_IP",
        /* value */"212.159.71.187",
        /* description */"Server IP Address - You have to tell Global Collect what this is",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"Supported Product Ids",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_PRODS",
        /* value */"1,2,3,11,114,122,705,840,1001",
        /* description */"Comma-separated list of supported Payment Products",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 9
        // This is used to implement customer-specific functionality
        configs[i] = new KKConfiguration(
        /* title */"Custom Class",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_CUSTOM_CLASS",
        /* value */"com.konakart.bl.modules.payment.globalcollect.GlobalCollectCustom",
        /* description */"the name of a custom class that imlpements "
                + "com.konakart.bl.modules.payment.globalcollect.GlobalCollectCustomIf",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* return_by_api */false);

        // 10
        // This is only used for testing so is made invisible
        configs[i] = new KKConfiguration(
        /* title */"Time-based OrderId",
        /* key */"MODULE_PAYMENT_GLOBALCOLLECT_TIME_ORDERID",
        /* value */"false",
        /* description */"If set to true, a time-based order Id will be generated",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now,
        /* return_by_api */true);

        return configs;
    }
}