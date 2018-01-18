//
// (c) 2004-2016 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakartadmin.modules.ordertotal.productrewardpoints;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ModuleInterface;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Product Reward Points order total module
 * 
 */
public class ProductRewardPoints extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_PRODUCT_REWARD_POINTS");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "ProductRewardPoints";
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
        return "ot_product_reward_points";
    }

    /**
     * @return an array of configuration values for this order total module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[6];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"Reward Points Module Status",
        /* key */"MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_STATUS",
        /* value */"true",
        /* description */"If set to false, the Product Reward Points promotion will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true'='true','false'='false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_SORT_ORDER",
                /* value */"59",
                /* description */"Sort Order of Product Reward Points module on the UI. Lowest is displayed first.",
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
        /* key */"MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_MIN_ORDER_VALUE",
        /* value */"custom1",
        /* description */"The points are only created if the total of the order,"
                + " equals or is greater than this minimum value",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"double(0,null)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Min quantity for a product",
        /* key */"MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_MIN_SINGLE_PROD_QTY",
        /* value */"custom2",
        /* description */"The points are only created if the quantity of at least one"
                + " single product, equals or is greater than this minimum value.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"integer(0,null)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Calc points on amount",
        /* key */"MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_APPLY_BEFORE_TAX",
        /* value */"custom3",
        /* description */"Determines whether the calculation for determining the"
                + " number of points is done on amounts before or after tax.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"choice('true'='BeforeTax','false'='AfterTax')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Points multiplier",
        /* key */"MODULE_ORDER_TOTAL_PRODUCT_REWARD_POINTS_MULTIPLIER",
        /* value */"custom4",
        /* description */"The order total is multiplied by this number to calculate"
                + " the number of points.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"double(0,null)",
        /* dateAdd */now);
        
        return configs;
    }
}
