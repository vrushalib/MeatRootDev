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

package com.konakartadmin.modules.shipping.fedex;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.ShippingModule;

/**
 * FedEx shipping module
 */
public class Fedex extends ShippingModule
{
    private static String MODULE_CODE = "fedex";
    
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_FEDEX");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_FEDEX_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "fedex";
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
            configs = new KKConfiguration[16];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();
        int groupId = 6;
        int i = 0;
        configs[i++] = new KKConfiguration(
        /* title */"Enable FedEx Shipping",
        /* key */"MODULE_SHIPPING_FEDEX_STATUS",
        /* value */"True",
        /* description */"Do you want to offer FedEx shipping?",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('True', 'False'),",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i++] = new KKConfiguration(
        /* title */"Tax Class",
        /* key */"MODULE_SHIPPING_FEDEX_TAX_CLASS",
        /* value */"0",
        /* description */"Use the following tax class on the shipping fee.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_tax_class_title",
        /* setFun */"tep_cfg_pull_down_tax_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Shipping Zone",
        /* key */"MODULE_SHIPPING_FEDEX_ZONE",
        /* value */"0",
        /* description */"If a zone is selected, only enable this shipping method for that zone.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Sort Order",
        /* key */"MODULE_SHIPPING_FEDEX_SORT_ORDER",
        /* value */"0",
        /* description */"Sort order of display.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"FedEx key",
        /* key */"MODULE_SHIPPING_FEDEX_KEY",
        /* value */"",
        /* description */"Your FedEx key for using the service.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"FedEx password",
        /* key */"MODULE_SHIPPING_FEDEX_PASSWORD",
        /* value */"",
        /* description */"Your FedEx password for using the service.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"FedEx account number",
        /* key */"MODULE_SHIPPING_FEDEX_ACCOUNT_NUMBER",
        /* value */"",
        /* description */"Your FedEx account number for using the service.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"FedEx meter number",
        /* key */"MODULE_SHIPPING_FEDEX_METER_NUMBER",
        /* value */"",
        /* description */"Your FedEx meter number for using the service.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */" FedEx Service type",
                /* key */"MODULE_SHIPPING_FEDEX_SERVICE_TYPE",
                /* value */"",
                /* description */"Only used if we want a single quote for a defined service type. In this case it defines the service type that we want. i.e FEDEX_2_DAY, INTERNATIONAL_PRIORITY etc.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */" FedEx Service codes to exclude",
                /* key */"MODULE_SHIPPING_FEDEX_SERVICE_TYPES_EXCLUDE",
                /* value */"",
                /* description */"Comma separated list of service types that will be excluded from the quotes returned to the customer (i.e. FEDEX_GROUND,FIRST_OVERNIGHT,STANDARD_OVERNIGHT).",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Measurement Unit",
        /* key */"MODULE_SHIPPING_FEDEX_MEASUREMENT_UNIT",
        /* value */"LBS",
        /* description */"Should be set to KGS or LBS",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"Packaging type by weight",
                /* key */"MODULE_SHIPPING_FEDEX_PACKAGING_TYPE",
                /* value */"1000:YOUR_PACKAGING",
                /* description */"Example (10:FEDEX_10KG_BOX,25:FEDEX_25KG_BOX,100:YOUR_PACKAGING): weight 0-10 = FEDEX_10KG_BOX; weight 10-25 = FEDEX_25KG_BOX etc.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Handling Fee",
        /* key */"MODULE_SHIPPING_FEDEX_HANDLING",
        /* value */"0",
        /* description */"Handling fee for this shipping method",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"The end point of the FedEx service",
                /* key */"MODULE_SHIPPING_FEDEX_END_POINT_URL",
                /* value */"https://gatewaybeta.fedex.com:443/web-services",
                /* description */"The end point of the FedEx service (i.e. https://gatewaybeta.fedex.com:443/web-services)",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"The type of drop off used",
                /* key */"MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE",
                /* value */"REGULAR_PICKUP",
                /* description */"The type of drop off used. i.e. REGULAR_PICKUP, BUSINESS_SERVICE_CENTER, DROP_BOX, REQUEST_COURIER, STATION",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"The rate request type",
        /* key */"MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE",
        /* value */"ACCOUNT",
        /* description */"The rate request type i.e. ACCOUNT, LIST, MULTIWEIGHT",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}
