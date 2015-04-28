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

package com.konakartadmin.modules.shipping.table;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ShippingModule;

/**
 * Table Rate shipping module
 * 
 */
public class Table extends ShippingModule
{
    private static String MODULE_CODE = "table";
    
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_TABLE");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_TABLE_TEXT_TITLE");
    }

    /**
     * @return the implementation filename - for compatibility with osCommerce we use the php name
     */
    public String getImplementationFileName()
    {
        return "table.php";
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
            configs = new KKConfiguration[7];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        configs[i++] = new KKConfiguration("Enable Table Method", "MODULE_SHIPPING_TABLE_STATUS",
                "True", "Do you want to offer table rate shipping?", 6, 0, "",
                "tep_cfg_select_option(array('True', 'False'),", now);
        configs[i++] = new KKConfiguration(
                "Shipping Table",
                "MODULE_SHIPPING_TABLE_COST",
                "25:8.50,50:5.50,10000:0.00",
                "The shipping cost is based on the total cost or weight of items. Example: 25:8.50,50:5.50,etc.. Up to 25 charge 8.50, from there to 50 charge 5.50, etc",
                6, 0, "", "", now);
        configs[i++] = new KKConfiguration(
                "Table Method",
                "MODULE_SHIPPING_TABLE_MODE",
                "weight",
                "The shipping cost is based on the order total or the total weight of the items ordered.",
                6, 0, "", "tep_cfg_select_option(array('weight', 'price'),", now);
        configs[i++] = new KKConfiguration("Handling Fee", "MODULE_SHIPPING_TABLE_HANDLING", "0",
                "Handling fee for this shipping method.", 6, 0, "", "", now);
        configs[i++] = new KKConfiguration("Tax Class", "MODULE_SHIPPING_TABLE_TAX_CLASS", "0",
                "Use the following tax class on the shipping fee.", 6, 0,
                "tep_get_tax_class_title", "tep_cfg_pull_down_tax_classes(", now);
        configs[i++] = new KKConfiguration("Shipping Zone", "MODULE_SHIPPING_TABLE_ZONE", "0",
                "If a zone is selected, only enable this shipping method for that zone.", 6, 0,
                "tep_get_zone_class_title", "tep_cfg_pull_down_zone_classes(", now);
        configs[i++] = new KKConfiguration("Sort Order", "MODULE_SHIPPING_TABLE_SORT_ORDER", "0",
                "Sort order of display.", 6, 0, "", "", now);

        return configs;
    }
}
