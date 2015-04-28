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

package com.konakartadmin.modules.shipping.free;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ShippingModule;

/**
 * Free shipping module
 */
public class Free extends ShippingModule
{
    private static String MODULE_CODE = "free";
    
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_FREE");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_FREE_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Free";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return MODULE_CODE;
    }
    
    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[2];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        configs[i++] = new KKConfiguration("Enable Local Collection", "MODULE_SHIPPING_FREE_STATUS",
                "True", "Do you want to offer free local collection?", 6, 0, "",
                "tep_cfg_select_option(array('True', 'False'),", now);
        configs[i++] = new KKConfiguration("Sort Order", "MODULE_SHIPPING_FREE_SORT_ORDER", "0",
                "Sort order of display.", 6, 0, "", "", now);

        return configs;
    }
}
