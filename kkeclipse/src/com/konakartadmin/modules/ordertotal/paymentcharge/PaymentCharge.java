//
// (c) 2013 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakartadmin.modules.ordertotal.paymentcharge;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Payment Charge order total module
 * 
 */
public class PaymentCharge extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_PAYMENT_CHARGE");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_PAYMENT_CHARGE_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "PaymentCharge";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "ot_payment_charge";
    }

    /**
     * @return an array of configuration values for this order total module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[3];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"Payment Charge Module Status",
        /* key */"MODULE_ORDER_TOTAL_PAYMENT_CHARGE_STATUS",
        /* value */"true",
        /* description */"If set to false, the Payment Charge module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_ORDER_TOTAL_PAYMENT_CHARGE_SORT_ORDER",
                /* value */"24",
                /* description */"Sort Order of Payment Charge module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i] = new KKConfiguration(
                /* title */"Payment Charges",
                /* key */"MODULE_ORDER_TOTAL_PAYMENT_CHARGE_CHARGES",
                /* value */"",
                /* description */"Format should be paymentModuleCode-paymentModuleSubCode:charge. Example paypal:5.00,authorizenet:3.00 . paymentModuleSubCode is optional.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        return configs;
    }
}
