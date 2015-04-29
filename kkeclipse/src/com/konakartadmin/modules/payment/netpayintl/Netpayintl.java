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
// Original version written by Steven Lohrenz (steven@stevenlohrenz.com) 
// based on a KonaKart example.  
//

package com.konakartadmin.modules.payment.netpayintl;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * PayPal Netpay International payment module
 * 
 * These definitions are used to allow the Administration Application to define the payment module's
 * configuration parameters.
 */
public class Netpayintl extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_NETPAYINTL");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_NETPAYINTL_TEXT_TITLE");
    }

    /**
     * This must be the class name of the implementation class in com.konakart.bl.modules.payment.*.
     * 
     * @return the implementation filename.
     */
    public String getImplementationFileName()
    {
        return "Netpayintl";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "netpayintl";
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
        int groupId = 6;

        configs[i++] = new KKConfiguration(
        /* title */"Enable the Netpay International Module",
        /* key */"MODULE_PAYMENT_NETPAYINTL_STATUS",
        /* value */"true",
        /* description */"Do you want to accept Netpay International payments? ('true' or 'false')",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Sort order of display.",
        /* key */"MODULE_PAYMENT_NETPAYINTL_SORT_ORDER",
        /* value */"0",
        /* description */"Sort order of display. Lowest is displayed first.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Payment Zone",
        /* key */"MODULE_PAYMENT_NETPAYINTL_ZONE",
        /* value */"0",
        /* description */"If a zone is selected, only enable this payment method for that zone.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Merchant's User Id",
        /* key */"MODULE_PAYMENT_NETPAYINTL_LOGIN",
        /* value */"Merchant Id",
        /* description */"The Merchant user id used for the accessing the Netpay International service",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_NETPAYINTL_REQUEST_URL",
        /* value */"https://process.netpay-intl.com/member/remote_charge.asp",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}
