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

package com.konakartadmin.modules.shipping.zones;

import java.text.MessageFormat;
import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ShippingModule;

/**
 * Zones shipping module
 * 
 */
public class Zones extends ShippingModule
{
    private static String MODULE_CODE = "zones";
    
    private int numZones = 1;

    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_ZONES");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_ZONES_TEXT_TITLE");
    }

    /**
     * @return the implementation filename - for compatibility with osCommerce we use the php name
     */
    public String getImplementationFileName()
    {
        return "zones.php";
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
        if (configs != null && configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        setNumZones(getMsgs().getString("MODULE_SHIPPING_ZONES_NUMBER_OF_ZONES"));

        int zoneCount = getNumZones();

        configs = new KKConfiguration[3 + (3 * zoneCount)];

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        configs[i++] = new KKConfiguration("Enable Zones Method", "MODULE_SHIPPING_ZONES_STATUS",
                "True", "Do you want to offer zone rate shipping?", 6, 0, "",
                "tep_cfg_select_option(array('True', 'False'), ", now);
        configs[i++] = new KKConfiguration("Tax Class", "MODULE_SHIPPING_ZONES_TAX_CLASS", "0",
                "Use the following tax class on the shipping fee.", 6, 0,
                "tep_get_tax_class_title", "tep_cfg_pull_down_tax_classes(", now);
        configs[i++] = new KKConfiguration("Sort Order", "MODULE_SHIPPING_ZONES_SORT_ORDER", "0",
                "Sort order of display.", 6, 0, "", "", now);

        for (int z = 0; z < zoneCount; z++)
        {
            String defaultCountries = "";
            if (z == 0)
            {
                defaultCountries = "US,CA";
            }

            Object[] zone = new Object[] { new Integer(z+1) };

            configs[i++] = new KKConfiguration(
                    MessageFormat.format("Zone {0} Countries", zone),
                    MessageFormat.format("MODULE_SHIPPING_ZONES_COUNTRIES_{0}", zone),
                    defaultCountries,
                    MessageFormat
                            .format(
                                    "Comma separated list of two character ISO country codes that are part of Zone {0}",
                                    zone), 6, 0, "", "", now);
            configs[i++] = new KKConfiguration(
                    MessageFormat.format("Zone {0} Shipping Table", zone),
                    MessageFormat.format("MODULE_SHIPPING_ZONES_COST_{0}", zone),
                    "3:8.50,7:10.50,99:20.00",
                    MessageFormat
                            .format(
                                    "Shipping rates to Zone {0} destinations based on a group of maximum order weights. Example: 3:8.50,7:10.50,... Weights less than or equal to 3 would cost 8.50 for destinations in this Zone.",
                                    zone), 6, 0, "", "", now);
            configs[i++] = new KKConfiguration(
                    MessageFormat.format("Zone {0} Handling Fee", zone), MessageFormat.format(
                            "MODULE_SHIPPING_ZONES_HANDLING_{0}", zone), "0",
                    "Handling Fee for this shipping zone", 6, 0, "", "", now);
        }
        return configs;
    }

    /**
     * @return the numZones
     */
    public int getNumZones()
    {
        return numZones;
    }

    /**
     * @param numZones
     *            the numZones to set
     */
    public void setNumZones(int numZones)
    {
        this.numZones = numZones;
    }

    /**
     * @param numZonesStr
     *            the numZones to set
     */
    public void setNumZones(String numZonesStr)
    {
        setNumZones(Integer.valueOf(numZonesStr).intValue());
    }
}
