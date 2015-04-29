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

package com.konakartadmin.modules.ordertotal.taxcloud;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Tax Cloud order total module
 * 
 */
public class TaxCloud extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_TAX_CLOUD");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_TAX_CLOUD_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "TaxCloud";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "ot_tax_cloud";
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

        configs[i] = new KKConfiguration(
        /* title */"Display TaxCloud Tax",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_STATUS",
        /* value */"true",
        /* description */"Do you want to display the order tax value from TaxCloud?",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i] = new KKConfiguration(
        /* title */"Sort Order",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_SORT_ORDER",
        /* value */"40",
        /* description */"Sort order of display.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"TaxCloud login Id",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_ID",
        /* value */"",
        /* description */"TaxCloud login id for APIs",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"TaxCloud login key",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_KEY",
        /* value */"",
        /* description */"TaxCloud login key for APIs",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Store Address1",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS1",
        /* value */"",
        /* description */"Store Address1",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Store Address2",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS2",
        /* value */"",
        /* description */"Store Address2",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Store City",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_CITY",
        /* value */"",
        /* description */"Store City",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Store State",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_STATE",
        /* value */"",
        /* description */"Store State",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Store Zip",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ZIP",
        /* value */"",
        /* description */"Store Zip",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"USPS user id",
        /* key */"MODULE_ORDER_TOTAL_TAX_CLOUD_USPS_USER_ID",
        /* value */"",
        /* description */"USPS user id for address verification",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}
