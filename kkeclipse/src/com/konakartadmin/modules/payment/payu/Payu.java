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

package com.konakartadmin.modules.payment.payu;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * PayU
 * 
 */
public class Payu extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "PAYU");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_PAYU_TEXT_TITLE");
    }

    /**
     * @return the implementation filename - for compatibility with osCommerce we use the php name
     */
    public String getImplementationFileName()
    {
        return "payu.php";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "payu";
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
        configs[i++] = new KKConfiguration("Enable PayU Module",
                "MODULE_PAYMENT_PAYU_STATUS", "True",
                "Do you want to accept online payments using PayU?", 6, 1, "",
                "tep_cfg_select_option(array('True', 'False'),", now);
        configs[i++] = new KKConfiguration("Payment Zone", "MODULE_PAYMENT_PAYU_ZONE", "0",
                "If a zone is selected, only enable this payment method for that zone.", 6, 2,
                "tep_get_zone_class_title", "tep_cfg_pull_down_zone_classes(", now);
        configs[i++] = new KKConfiguration("Set Order Status",
                "MODULE_PAYMENT_PAYU_ORDER_STATUS_ID", "0",
                "Set the status of orders made with this payment module to this value", 6, 0,
                "tep_get_order_status_name", "tep_cfg_pull_down_order_statuses(", now);
        configs[i++] = new KKConfiguration("Sort order of display",
                "MODULE_PAYMENT_PAYU_SORT_ORDER", "0",
                "Sort order of display. Lowest is displayed first.", 6, 0, "", "", now);

        // This last PAYU configuration is not used other than to provide an example of using the
        // FileUpload configuration variable set_function
        
        String miscFilename = null;
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Win"))
        {
            miscFilename = "C:/Temp/payu_misc.properties";
        } else
        {
            miscFilename = "/var/tmp/payu_misc.properties";
        }
        configs[i++] = new KKConfiguration("Miscellaneous Config File",
                "MODULE_PAYMENT_PAYU_MISC_CONFIG_FILE", miscFilename,
                "Miscellaneous Configuration File (not used).", 6, 6, "", "FileUpload", now);

        return configs;
    }
}
