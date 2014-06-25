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

package com.konakartadmin.modules.payment.worldpayxmlredirect;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * WorldPay XML Redirect payment module
 */
public class WorldPayXMLRedirect extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_WP_XML_REDIRECT");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_WP_XML_REDIRECT_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "WorldPayXMLRedirect";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "worldpayxmlredirect";
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

        // 1
        configs[i] = new KKConfiguration(
        /* title */"WorldPay API Status",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_STATUS",
        /* value */"true",
        /* description */"If set to false, the WorldPay API module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        // 2
        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of WorldPay API module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
        /* title */"WorldPay Payment Zone",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_ZONE",
        /* value */"0",
        /* description */"Zone where the WorldPay module can be used. Otherwise it is disabled.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"Merchant Code",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_MERCHANT_CODE",
        /* value */"MYMERCHANT",
        /* description */"Merchant Code",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
        /* title */"XML Password",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_XML_PASSWORD",
        /* value */"password",
        /* description */"Password when sending XML",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"Shared Secret",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_SHARED_SECRET",
        /* value */"MIGfMA0GCSqGSIb3DQEB",
        /* description */"Shared secret used to create a hash value in order to verify "
                + " the hash value created by WorldPay on the redirect URL.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
                /* title */"Request URL",
                /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_REQUEST_URL",
                /* value */"https://secure-test.worldpay.com/jsp/merchant/xml/paymentService.jsp",
                /* description */"URL used by KonaKart to send the transaction details. "
                        + "(Production URL is  https://secure.worldpay.com/jsp/merchant/xml/paymentService.jsp)",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Response URL",
        /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_RESPONSE_URL",
        /* value */"http://localhost:8780/konakart/WorldPayXMLRedirectResponse.action",
        /* description */"URL used by WorldPay to redirect the user when payment has terminated.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 9
        configs[i] = new KKConfiguration(
                /* title */"Include Methods",
                /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_INCLUDE_METHODS",
                /* value */"ALL",
                /* description */"Used to selectively include payment methods. They should be separated by commas. "
                        + "(e.g. AMEX-SSL,VISA-SSL,ECMC-SSL)",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 10
        configs[i] = new KKConfiguration(
                /* title */"Exclude Methods",
                /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_EXCLUDE_METHODS",
                /* value */"",
                /* description */"Used to selectively exclude payment methods. They should be separated by commas. "
                        + "(e.g. CB-SSL,DINERS-SSL,DISCOVER-SSL)",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 11
        configs[i] = new KKConfiguration(
                /* title */"Use order number as unique order identifier",
                /* key */"MODULE_PAYMENT_WP_XML_REDIRECT_USE_ORDER_NUMBER",
                /* value */"true",
                /* description */"If set to true, the orde number is used as the order identifier in the request. Otherwise the unique numeric order id is used.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now);

        return configs;
    }
}