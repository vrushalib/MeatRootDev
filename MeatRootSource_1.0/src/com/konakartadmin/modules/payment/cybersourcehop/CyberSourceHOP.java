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

package com.konakartadmin.modules.payment.cybersourcehop;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * CyberSource Hosted Order Post payment module
 */
public class CyberSourceHOP extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_CYBERSOURCEHOP");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_CYBERSOURCEHOP_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "CyberSourceHOP";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "cybersourcehop";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[10];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        // 1
        configs[i] = new KKConfiguration(
                /* title */"CyberSource HOP Status",
                /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_STATUS",
                /* value */"true",
                /* description */"If set to false, the CyberSource Hosted Order Post module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now,
                /* return_by_api */ true);

        // 2
        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_SORT_ORDER",
                /* value */"0",
                /* description */"Sort Order of CyberSource Hosted Order Post module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
                /* title */"CyberSource Payment Zone",
                /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_ZONE",
                /* value */"0",
                /* description */"Zone where the CyberSource module can be used. Otherwise it is disabled.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"tep_get_zone_class_title",
                /* setFun */"tep_cfg_pull_down_zone_classes(",
                /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
                /* title */"Shared Secret",
                /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_SHARED_SECRET",
                /* value */"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBGFuLLGLSJnU0eRdnF3J45YjwuFF0h5/D7uAPhDhrYFM7cJBF/JPEpYrVpR94fZnHqbF13HPvqxzZu3PFXBUVxIqVTjWbFRT2qGJjDUGeg4MzbRxNup2T6veM8D2t0juzLiml18ZKjL8aMqyadRGFGJFa8/8Fz4Z3uZXj/io+lQIDAQAB",
                /* description */"Shared secret (HMAC = Hash Message Authentication Code). Used to encrypt "
                        + " and decrypt communication between KonaKart and CyberSource."
                        + " Generate this value using your Merchant Account on CyberSource.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
                /* title */"Serial Number",
                /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_SERIAL_NUMBER",
                /* value */"3112245556300176056165",
                /* description */"Shared secret (HMAC = Hash Message Authentication Code). Used to encrypt "
                        + " and decrypt communication between KonaKart and CyberSource."
                        + " Generate this value using your Merchant Account on CyberSource.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_REQUEST_URL",
        /* value */"https://orderpagetest.ic3.com/hop/orderform.jsp",
        /* description */"URL used by KonaKart to send the transaction details. "
                + "(Production URL is https://orderpage.ic3.com/hop/orderform.jsp)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"Response URL",
        /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_RESPONSE_URL",
        /* value */"http://host:port/konakart/CyberSourceHOPResponse.action",
        /* description */"URL to return to after a CyberSource decision",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Environment",
        /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_ENVIRONMENT",
        /* value */"TEST",
        /* description */"Environment - 'TEST' or 'PRODUCTION'",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('TEST', 'PRODUCTION')",
        /* dateAdd */now);
        
        // 9
        configs[i] = new KKConfiguration(
        /* title */"Merchant Account Id",
        /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_MERCHANT_ACC",
        /* value */"Your Merchant Account Id",
        /* description */"CyberSource Merchant Account Id",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 10
        configs[i] = new KKConfiguration(
        /* title */"Gateway Version",
        /* key */"MODULE_PAYMENT_CYBERSOURCEHOP_VERSION",
        /* value */"7",
        /* description */"CyberSource Gateway version number",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}