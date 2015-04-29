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

package com.konakartadmin.modules.payment.authorizenet;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * Authorize.Net payment module
 * 
 * These definitions are used to allow the Administration Application to define the payment module's
 * configuration parameters.
 */
public class Authorizenet extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_AUTHORIZENET");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_AUTHORIZENET_TEXT_TITLE");
    }

    /**
     * @return the implementation filename.
     */
    public String getImplementationFileName()
    {
        return "Authorizenet";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "authorizenet";
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

        int i = 0;
        int groupId = 6;

        configs[i++] = new KKConfiguration(
        /* title */"Enable Authorize.net Module",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_STATUS",
        /* value */"true",
        /* description */"Do you want to accept Authorize.Net payments? ('true' or 'false')",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Sort order of display.",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_SORT_ORDER",
        /* value */"0",
        /* description */"Sort order of display. Lowest is displayed first.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Payment Zone",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_ZONE",
        /* value */"0",
        /* description */"If a zone is selected, only enable this payment method for that zone.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Login Id",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_LOGIN",
        /* value */"testing",
        /* description */"The login id used for the Authorize.Net service",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Transaction Key",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_TXNKEY",
        /* value */"Test",
        /* description */"Transaction Key used for encrypting data",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_REQUEST_URL",
        /* value */"https://secure.authorize.net/gateway/transact.dll",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"ARB Request URL",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_ARB_REQUEST_URL",
        /* value */"https://apitest.authorize.net/xml/v1/request.api",
        /* description */"URL for Automated Recurring Billing used by"
                + " KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Test Mode",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_TESTMODE",
        /* value */"true",
        /* description */"If set to true, the authorize.net module will be in test mode",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now,
        /* returnByApi */true);


        configs[i] = new KKConfiguration(
        /* title */"Show CVV field",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_SHOW_CVV",
        /* value */"true",
        /* description */"If set to true, the CVV entry field will"
                + " be shown when entering credit card details",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Use Direct Post Integration",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_DIRECT_POST",
        /* value */"false",
        /* description */"If set to true, direct post will be used instead of AIM",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"MD5 Hash Key",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_MD5_HASH_KEY",
        /* value */"",
        /* description */"Leave the MD5HashKey empty, unless you have"
                + " explicitly set it in the merchant interface",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Direct Post Relay URL",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_RELAY_URL",
        /* value */"https://host:port/konakart/AuthNetCallback.action",
        /* description */"URL used by AuthorizeNet Direct Post to callback into KonaKart."
                + " This would typically be HTTPS and reachable from the internet",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Direct Post Callback Username",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_CALLBACK_USERNAME",
        /* value */"myuser",
        /* description */"Valid username for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Direct Post Callback Password",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_CALLBACK_PASSWORD",
        /* value */"mypassword",
        /* description */"Valid password for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Enable Customer Information Manager",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM",
        /* value */"false",
        /* description */"If set to true, AuthorizeNet will be used to manage credit cards",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now,
        /* returnByApi */true);
        
        configs[i++] = new KKConfiguration(
        /* title */"CIM Web Service URL",
        /* key */"MODULE_PAYMENT_AUTHORIZENET_CIM_WEB_SERVICE_URL",
        /* value */"https://apitest.authorize.net/xml/v1/request.api",
        /* description */"URL for sending CIM XML messages."
                + " For production use https://api.authorize.net/xml/v1/request.api",
        /* groupId */groupId,
        /* sortO */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);


        return configs;
    }
}
