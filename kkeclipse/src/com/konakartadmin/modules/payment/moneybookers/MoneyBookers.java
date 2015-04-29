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
// Original version contributed by Chris Derham (Atomus Ltd)
//

package com.konakartadmin.modules.payment.moneybookers;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * MoneyBookers payment module
 */
public class MoneyBookers extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_MONEYBOOKERS");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_MONEYBOOKERS_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "MoneyBookers";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "moneybookers";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[11];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"MoneyBookers Status",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_STATUS",
        /* value */"true",
        /* description */"If set to false, the MoneyBookers module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of MoneyBookers module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"MoneyBookers Payment Zone",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_ZONE",
        /* value */"0",
        /* description */"Zone where MoneyBookers module can be used. Otherwise it is disabled.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Callback Username",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_USERNAME",
        /* value */"myuser",
        /* description */"Valid username for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Callback Password",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_PASSWORD",
        /* value */"mypassword",
        /* description */"Valid password for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Callback Secret Word",
                /* key */"MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_SECRET_WORD",
                /* value */"secretword",
                /* description */"Secret word for KonaKart. Used by the callback code to verify response"
                        + " from MoneyBookers. Set this same value in your Merchant Account on MoneyBookers",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Callback URL",
                /* key */"MODULE_PAYMENT_MONEYBOOKERS_CALLBACK_URL",
                /* value */"http://host:port/konakart/MoneyBookersCallback.action",
                /* description */"URL used by MoneyBookers to callback into KonaKart. This would typically be HTTPS",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Declined return URL",
                /* key */"MODULE_PAYMENT_MONEYBOOKERS_DECLINE_URL",
                /* value */"http://host:port/konakart/CatalogCheckoutExternalPaymentErrorPage.action",
                /* description */"URL to return to when leaving MoneyBookers web site after an unsuccesful transaction",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Accepted return URL",
                /* key */"MODULE_PAYMENT_MONEYBOOKERS_ACCEPT_URL",
                /* value */"http://host:port/konakart/CheckoutFinished.action",
                /* description */"URL to return to when leaving MoneyBookers web site after an accepted transaction",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_REQUEST_URL",
        /* value */"https://www.moneybookers.com/app/payment.pl",
        /* description */"URL used by KonaKart to send the transaction details",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Email to pay",
        /* key */"MODULE_PAYMENT_MONEYBOOKERS_PAY_TO_EMAIL",
        /* value */"",
        /* description */"Moneybookers account to pay funds into",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}