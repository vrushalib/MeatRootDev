package com.konakartadmin.modules.payment.payu;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * PayU payment module
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
            setConfigKeyStub(super.getConfigKeyStub() + "_PAYU");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_PAYU_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Payu";
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
            configs = new KKConfiguration[7];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"PayU Status",
        /* key */"MODULE_PAYMENT_PAYU_STATUS",
        /* value */"true",
        /* description */"If set to false, the Payu module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_PAYU_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of Payu module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Success URL",
        /* key */"MODULE_PAYMENT_PAYU_SUCCESS_URL",
        /* value */"http://meatroot.com/PayuResponse.action",
        /* description */"URL used by PayU to callback Konakart in case of successful transaction",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Failure URL",
        /* key */"MODULE_PAYMENT_PAYU_FAILURE_URL",
        /* value */"http://meatroot.com/PayuResponse.action",
        /* description */"URL used by PayU to callback Konakart in case of failed transaction",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
                /* title */"Cancel URL",
                /* key */"MODULE_PAYMENT_PAYU_CANCEL_URL",
                /* value */"http://meatroot.com/PayuResponse.action",
                /* description */"URL used by PayU when a user cancels a transaction",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
                /* title */"Drop categories",
                /* key */"MODULE_PAYMENT_PAYU_DROP_CATEGORIES",
                /* value */"EMI,COD",
                /* description */"Option to disable the unwanted payment methods in payu",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);


        configs[i] = new KKConfiguration(
        /* title */"Test Mode",
        /* key */"MODULE_PAYMENT_PAYU_TEST_MODE",
        /* value */"true",
        /* description */"Forces KonaKart to use the Test server of payu",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('true', 'false'), ",
        /* dateAdd */now);

        return configs;
    }
}
