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

package com.konakartadmin.modules.payment.paypal;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * PayPal payment module
 * 
 */
public class Paypal extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_PAYPAL");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_PAYPAL_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Paypal";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "paypal";
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
        /* title */"PayPal Status",
        /* key */"MODULE_PAYMENT_PAYPAL_STATUS",
        /* value */"true",
        /* description */"If set to false, the PayPal module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_PAYPAL_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of PayPal module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"PayPal Payment Zone",
        /* key */"MODULE_PAYMENT_PAYPAL_ZONE",
        /* value */"0",
        /* description */"Zone where PayPal module can be used. Otherwise it is disabled.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Callback username",
        /* key */"MODULE_PAYMENT_PAYPAL_CALLBACK_USERNAME",
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
        /* key */"MODULE_PAYMENT_PAYPAL_CALLBACK_PASSWORD",
        /* value */"mypassword",
        /* description */"Valid password for KonaKart. Used by the callback"
                + " code to log into KonaKart in order to make an engine call",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Callback URL",
        /* key */"MODULE_PAYMENT_PAYPAL_CALLBACK_URL",
        /* value */"http://host:port/konakart/PayPalCallback.action",
        /* description */"URL used by PayPal to callback into KonaKart."
                + " This would typically be HTTPS",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Return URL",
        /* key */"MODULE_PAYMENT_PAYPAL_RETURN_URL",
        /* value */"http://host:port/konakart/CheckoutFinished.action",
        /* description */"URL to return to when leaving PayPal web"
                + " site after a successful transaction",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Cancel URL",
        /* key */"MODULE_PAYMENT_PAYPAL_CANCEL_URL",
        /* value */"http://host:port/konakart/CheckoutPaymentError.action",
        /* description */"URL to return to when leaving PayPal web"
                + " site after an unsuccesful transaction",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Test Mode",
        /* key */"MODULE_PAYMENT_PAYPAL_TEST_MODE",
        /* value */"true",
        /* description */"Forces KonaKart to use the PayPal Sandbox",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"PayPal Id",
        /* key */"MODULE_PAYMENT_PAYPAL_ID",
        /* value */"paypalId",
        /* description */"The merchant PayPal Id",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Transaction Currency",
        /* key */"MODULE_PAYMENT_PAYPAL_CURRENCY",
        /* value */"Selected Currency",
        /* description */"Currency to use for PayPal transaction",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}
