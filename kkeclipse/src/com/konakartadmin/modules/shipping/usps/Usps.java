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

package com.konakartadmin.modules.shipping.usps;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ShippingModule;

/**
 * USPS shipping module
 * 
 */
public class Usps extends ShippingModule
{
    private static String MODULE_CODE = "usps";
    
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_USPS");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_USPS_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "usps";
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
            configs = new KKConfiguration[13];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();
        int groupId = 6;
        int i = 0;
        configs[i++] = new KKConfiguration(
        /* title */"Enable USPS Shipping",
        /* key */"MODULE_SHIPPING_USPS_STATUS",
        /* value */"True",
        /* description */"Do you want to offer USPS shipping?",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('True', 'False'),",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i++] = new KKConfiguration(
        /* title */"Tax Class",
        /* key */"MODULE_SHIPPING_USPS_TAX_CLASS",
        /* value */"0",
        /* description */"Use the following tax class on the shipping fee.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_tax_class_title",
        /* setFun */"tep_cfg_pull_down_tax_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Shipping Zone",
        /* key */"MODULE_SHIPPING_USPS_ZONE",
        /* value */"0",
        /* description */"If a zone is selected, only enable this shipping method for that zone.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Sort Order",
        /* key */"MODULE_SHIPPING_USPS_SORT_ORDER",
        /* value */"0",
        /* description */"Sort order of display.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"USPS UserId",
        /* key */"MODULE_SHIPPING_USPS_USERID",
        /* value */"",
        /* description */"The USPS UserId for using the service",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"Request URL",
                /* key */"MODULE_SHIPPING_USPS_URL",
                /* value */"http://production.shippingapis.com/ShippingAPI.dll",
                /* description */"The URL where the XML request is sent",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"USPS Service code",
                /* key */"MODULE_SHIPPING_USPS_SERVICE_CODE",
                /* value */"ALL",
                /* description */"Defines the service code that we want to use (i.e. FIRST CLASS, PRIORITY or ALL to return all available services).",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
                /* title */"USPS Service codes to exclude",
                /* key */"MODULE_SHIPPING_USPS_SERVICE_CODES_EXCLUDE",
                /* value */"",
                /* description */"Comma separated list of service codes that will be excluded from the quotes returned to the customer (i.e. 0,1,2,3 etc.).",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
                /* title */"Size  by weight",
                /* key */"MODULE_SHIPPING_USPS_SIZE",
                /* value */"71:REGULAR",
                /* description */"Example (10:REGULAR,25:LARGE,71:OVERSIZE): weight 0-9 = Regular; weight 10-24 = Large; 25-70 = Oversize",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
                /* title */"Container Type",
                /* key */"MODULE_SHIPPING_USPS_CONTAINER",
                /* value */"VARIABLE",
                /* description */"VARIABLE, RECTANGULAR, NONRECTANGULAR, FLAT RATE BOX etc.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);
       
        configs[i++] = new KKConfiguration(
                /* title */"Machinable Package",
                /* key */"MODULE_SHIPPING_USPS_MACHINABLE",
                /* value */"True",
                /* description */"Set to true if the package is machinable. Lower rates normally apply if machinable.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"tep_cfg_select_option(array('True', 'False'),",
                /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
                /* title */"First Class Package",
                /* key */"MODULE_SHIPPING_USPS_FIRST_CLASS_PACKAGE",
                /* value */"PARCEL",
                /* description */"First Class packaging must be either LETTER, FLAT or PARCEL.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
                /* title */"Handling Fee",
                /* key */"MODULE_SHIPPING_USPS_HANDLING",
                /* value */"0",
                /* description */"Handling fee for this shipping method",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        return configs;
    }
}
