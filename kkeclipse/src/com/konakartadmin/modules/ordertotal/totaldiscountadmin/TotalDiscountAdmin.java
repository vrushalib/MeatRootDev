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

package com.konakartadmin.modules.ordertotal.totaldiscountadmin;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ModuleInterface;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Total Discount order total module where the discount is entered by an administrator
 * 
 */
public class TotalDiscountAdmin extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_TOTAL_DISCOUNT_ADMIN");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "TotalDiscountAdmin";
    }
    
    /**
     * @return the module sub-type
     */
    public int getModuleSubType()
    {
        return ModuleInterface.MODULE_SUB_TYPE_PROMOTION;
    }
    
    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "ot_total_discount_admin";
    }

    /**
     * @return an array of configuration values for this order total module
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
                /* title */"Total Discount Module Status",
                /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_STATUS",
                /* value */"true",
                /* description */"If set to false, all of the Total Discount promotions will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true'='true','false'='false')",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_SORT_ORDER",
                /* value */"20",
                /* description */"Sort Order of Total Discount module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        /*
         * We don't want the following to be visible since they are used to map data onto the custom
         * fields of the promotion object
         */
        configs[i] = new KKConfiguration(
        /* title */"Minimum Order Value",
        /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_MIN_ORDER_VALUE",
        /* value */"custom1",
        /* description */"The discount only applies if the total of the order,"
                + " equals or is greater than this minimum value",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"double(0,null)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Min total quantity",
        /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_MIN_TOTAL_QUANTITY",
        /* value */"custom2",
        /* description */"The discount only applies if the number of products ordered,"
                + " equals or is greater than this minimum value",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"integer(0,null)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Min quantity for a product",
        /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_MIN_SINGLE_PROD_QUANTITY",
        /* value */"custom3",
        /* description */"The discount only applies on the total if the quantity of at least one"
                + " single product, equals or is greater than this minimum value.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"integer(0,null)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Discount Type",
        /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_PERCENT",
        /* value */"custom5",
        /* description */"The type of discount - either a percentage or an amount",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"choice('true'='Percent','false'='Amount')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Apply discount",
        /* key */"MODULE_ORDER_TOTAL_TOTAL_DISCOUNT_ADMIN_APPLY_BEFORE_TAX",
        /* value */"custom6",
        /* description */"Determines whether all calculations are"
                + " done on amounts before or after tax.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"choice('true'='BeforeTax','false'='AfterTax')",
        /* dateAdd */now);

        return configs;
    }
}
