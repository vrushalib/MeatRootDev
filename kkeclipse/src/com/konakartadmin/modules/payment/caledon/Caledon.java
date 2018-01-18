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

package com.konakartadmin.modules.payment.caledon;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * Caledon payment module
 * 
 * These definitions are used to allow the Administration Application to define the payment module's
 * configuration parameters.
 */
public class Caledon extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_CALEDON");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_CALEDON_TEXT_TITLE");
    }

    /**
     * @return the implementation filename.
     */
    public String getImplementationFileName()
    {
        return "caledon";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "caledon";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[9];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i++] = new KKConfiguration(
        /* title */"Enable Caledon Module",
        /* key */"MODULE_PAYMENT_CALEDON_STATUS",
        /* value */"true",
        /* description */"Do you want to accept Caledon payments? ('true' or 'false')",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"choice('true'='true','false'='false')",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Sort order of display.",
        /* key */"MODULE_PAYMENT_CALEDON_SORT_ORDER",
        /* value */"0",
        /* description */"Sort order of display. Lowest is displayed first.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Payment Zone",
        /* key */"MODULE_PAYMENT_CALEDON_ZONE",
        /* value */"0",
        /* description */"If a zone is selected, only enable this payment method for that zone.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Terminal Id",
        /* key */"MODULE_PAYMENT_CALEDON_TERMINAL_ID",
        /* value */"Enter terminal id",
        /* description */"The Terminal ID used for the Caledon service",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
        /* title */"Password for Terminal ID",
        /* key */"MODULE_PAYMENT_CALEDON_PASSWORD",
        /* value */"Enter password",
        /* description */"Password for this terminal ID",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"AMEX Terminal Id",
        /* key */"MODULE_PAYMENT_CALEDON_TERMINAL_ID_AMEX",
        /* value */"Enter AMEX terminal id",
        /* description */"The Terminal ID used for the Caledon service for American Express",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i++] = new KKConfiguration(
        /* title */"Password for AMEX Terminal ID",
        /* key */"MODULE_PAYMENT_CALEDON_PASSWORD_AMEX",
        /* value */"Enter password",
        /* description */"Password for the American Express terminal ID ",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_CALEDON_REQUEST_URL",
        /* value */"https://lt3a.caledoncard.com/",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Show CVV field",
                /* key */"MODULE_PAYMENT_CALEDON_SHOW_CVV",
                /* value */"true",
                /* description */"If set to true, the CVV entry field will be shown when entering credit card details",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true'='true','false'='false')",
                /* dateAdd */now);

        return configs;
    }
}
