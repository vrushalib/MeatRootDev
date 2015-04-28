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

package com.konakartadmin.modules.ordertotal.externaltax;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * ExternalTax order total module
 */
public class ExternalTax extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_EXTERNAL_TAX");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_EXTERNAL_TAX_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "ExternalTax";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "ot_tax";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[4];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int groupId = 6;
        int i = 0;
        configs[i] = new KKConfiguration(
        /* title */"Enable Module",
        /* key */"MODULE_ORDER_TOTAL_EXTERNAL_TAX_ENABLED",
        /* value */"false",
        /* description */"Do you want to enable the External Tax order total module?",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_ORDER_TOTAL_EXTERNAL_TAX_SORT_ORDER",
        /* value */"39",
        /* description */"Sort Order of display",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Order Total To Remove",
        /* key */"MODULE_ORDER_TOTAL_EXTERNAL_TAX_OT_TO_REMOVE",
        /* value */"ot_thomson",
        /* description */"External tax order total module to remove if present",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Only if External Tax Present",
        /* key */"MODULE_ORDER_TOTAL_EXTERNAL_TAX_ONLY_IF_PRESENT",
        /* value */"true",
        /* description */"Only create a Tax Order Total line if the External Tax Order Total is present",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);
        
        return configs;
    }
}
