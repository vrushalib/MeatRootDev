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

package com.konakartadmin.modules.ordertotal.shipping;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Shipping order total module
 * 
 */
public class Shipping extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_SHIPPING");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_SHIPPING_TEXT_TITLE");
    }

    /**
     * @return the implementation filename - for compatibility with osCommerce we use the php name
     */
    public String getImplementationFileName()
    {
        return "ot_shipping.php";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "ot_shipping";
    }
    
    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[5];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        configs[i++] = new KKConfiguration("Display Shipping",
                "MODULE_ORDER_TOTAL_SHIPPING_STATUS", "true",
                "Do you want to display the order shipping cost?", 6, 1, "",
                "tep_cfg_select_option(array('true', 'false'), ", now);
        configs[i++] = new KKConfiguration("Sort Order", "MODULE_ORDER_TOTAL_SHIPPING_SORT_ORDER",
                "30", "Sort order of display.", 6, 2, "", "", now);
        configs[i++] = new KKConfiguration("Allow Free Shipping",
                "MODULE_ORDER_TOTAL_SHIPPING_FREE_SHIPPING", "false",
                "Do you want to allow free shipping?", 6, 3, "",
                "tep_cfg_select_option(array('true', 'false'), ", now);
        configs[i++] = new KKConfiguration("Free Shipping For Orders Over",
                "MODULE_ORDER_TOTAL_SHIPPING_FREE_SHIPPING_OVER", "50",
                "Provide free shipping for orders over the set amount.", 6, 4,
                "currencies->format", "", now);
        configs[i++] = new KKConfiguration("Provide Free Shipping For Orders Made",
                "MODULE_ORDER_TOTAL_SHIPPING_DESTINATION", "national",
                "Provide free shipping for orders sent to the set destination.", 6, 5, "",
                "tep_cfg_select_option(array('national', 'international', 'both'), ", now);

        return configs;
    }
}
