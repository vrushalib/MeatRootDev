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

package com.konakartadmin.modules.ordertotal.buyxgetyfree;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ModuleInterface;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Buy X Get Y Free order total module
 */
public class BuyXGetYFree extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_BUY_X_GET_Y_FREE");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "BuyXGetYFree";
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
        return "ot_buy_x_get_y_free";
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
                /* title */"Buy X Get Y Free Status",
                /* key */"MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_STATUS",
                /* value */"true",
                /* description */"If set to false, all of the Buy X Get Y promotions will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('True', 'False')",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_SORT_ORDER",
                /* value */"22",
                /* description */"Sort Order of Buy X Get Y module on the UI. Lowest is displayed first.",
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
                /* title */"Buy Quantity",
                /* key */"MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_BUY_QUANTITY",
                /* value */"custom1",
                /* description */"The quantity that the customer must buy",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"invisible",
                /* setFun */"integer(0,null)",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Free Quantity",
                /* key */"MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_FREE_QUANTITY",
                /* value */"custom2",
                /* description */"The quantity that the customer gets for free",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"invisible",
                /* setFun */"integer(0,null)",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Minimum Order Value",
                /* key */"MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_MIN_ORDER_VALUE",
                /* value */"custom3",
                /* description */"The discount only applies if the total of the order,"
                        + " equals or is greater than this minimum value",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"invisible",
                /* setFun */"double(0,null)",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Apply discount before tax",
        /* key */"MODULE_ORDER_TOTAL_BUY_X_GET_Y_FREE_APPLY_BEFORE_TAX",
        /* value */"custom4",
        /* description */"Determines whether all calculations are"
                + " done on amounts before or after tax.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"invisible",
        /* setFun */"choice('True', 'False')",
        /* dateAdd */now);

        return configs;
    }
}
