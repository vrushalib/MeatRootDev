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

package com.konakartadmin.modules.payment.commideavanguard;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.PaymentModule;

/**
 * Commidea Vanguard payment module
 */
public class CommideaVanguard extends PaymentModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_COMMIDEA_VANGUARD");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_PAYMENT_COMMIDEA_VANGUARD_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "CommideaVanguard";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "commideavanguard";
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[25];
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
        /* title */"Commidea Vanguard Status",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_STATUS",
        /* value */"true",
        /* description */"If set to false, the Commidea module will be unavailable",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);

        // 2
        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of the Commidea module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
        /* title */"Commidea Payment Zone",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_ZONE",
        /* value */"0",
        /* description */"Zone where the Commidea module can be used. Otherwise it is disabled.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"System ID",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_ID",
        /* value */"Your_System_ID",
        /* description */"System ID from Commidea",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
        /* title */"System GUID",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_SYSTEM_GUID",
        /* value */"Your_System_GUID",
        /* description */"System GUID from Commidea",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"Passcode",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_PASSCODE",
        /* value */"Your_Passcode",
        /* description */"Passcode",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"Account Id",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_ID",
        /* value */"Your_Account_ID",
        /* description */"Account Id",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Account Passcode",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_ACCOUNT_PASSCODE",
        /* value */"Your_Account_Passcode",
        /* description */"Account Passcode",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        // 9
        configs[i] = new KKConfiguration(
        /* title */"Commidea Request URL",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_REQUEST_URL",
        /* value */"https://txn-test.cxmlpg.com/xml4/commideagateway.asmx",
        /* description */"URL where the Commidea XML requests are posted.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 10
        configs[i] = new KKConfiguration(
        /* title */"Commidea Credit Card Post URL",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_POST_URL",
        /* value */"https://vg-test.cxmlpg.com/vanguard.aspx",
        /* description */"URL where the Commidea credit card details are posted.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 11
        configs[i] = new KKConfiguration(
        /* title */"Credit Card Redirect URL",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_CC_REDIRECT_URL",
        /* value */"http://host:port/konakart/CommideaVanguard1.action",
        /* description */"URL to redirect the customers browser to after receiving "
                + "the post of the credit card details.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 12
        configs[i] = new KKConfiguration(
        /* title */"3D Secure Redirect URL",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_3D_REDIRECT_URL",
        /* value */"http://host:port/konakart/CommideaVanguard2.action",
        /* description */"URL to redirect the customers browser to after a 3D Secure validation.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 13
        configs[i] = new KKConfiguration(
        /* title */"ISO 4217 Currency Code",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_CURRENCY_CODE",
        /* value */"826",
        /* description */"ISO 4217 Currency Code for the currency being used.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 14
        configs[i] = new KKConfiguration(
        /* title */"ISO 3166 Country Code",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_COUNTRY_CODE",
        /* value */"826",
        /* description */"ISO 3166 Country Code for the country where the store is based.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 15
        configs[i] = new KKConfiguration(
        /* title */"Merchant URL",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_URL",
        /* value */"http://www.yourwebsitename.com",
        /* description */"The fully qualified URL of your web site",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 16
        configs[i] = new KKConfiguration(
        /* title */"Merchant Name",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_MERCHANT_NAME",
        /* value */"Your_Name",
        /* description */"The MerchantName must match the name shown online "
                + "to the cardholder at the merchant's site and the name "
                + "submitted by the merchant's acquirer in the settlement transaction",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 17
        configs[i] = new KKConfiguration(
        /* title */"Processing Identifier",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_PROCESSING_IDENTIFIER",
        /* value */"2",
        /* description */"This indicates the type of processing that needs to be undertaken. "
                + "Current available values are as follows:"
                + "1 - Auth and Charge, 2 - Auth Only, 3 - Charge Only",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 18
        configs[i] = new KKConfiguration(
        /* title */"Acquirer Id",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_ACQUIRER_ID",
        /* value */"2",
        /* description */"Acquirer reference number. Look at Integration Guide for mappings.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 19
        configs[i] = new KKConfiguration(
        /* title */"Visa Merchant Bank Id",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_BANK_ID",
        /* value */"123456",
        /* description */"Six digit assigned Bank Identification Number issued by "
                + "the merchant's member bank or processor",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 20
        configs[i] = new KKConfiguration(
        /* title */"Visa Merchant Number",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_NUMBER",
        /* value */"22270438",
        /* description */"Unique ID number which is assigned by the signing "
                + "merchant's acquirer, bank or processor and used to identify "
                + "the merchant within the VisaNet system",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 21
        configs[i] = new KKConfiguration(
        /* title */"Visa Merchant Password",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_VISA_MERCHANT_PASSWORD",
        /* value */"12345678",
        /* description */"The alphanumeric merchant password is provided by the acquirer",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        // 22
        configs[i] = new KKConfiguration(
        /* title */"MasterCard Merchant Bank Id",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_BANK_ID",
        /* value */"542515",
        /* description */"Six digit assigned Bank Identification Number issued by "
                + "the merchant's member bank or processor",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 23
        configs[i] = new KKConfiguration(
        /* title */"MasterCard Merchant Number",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_NUMBER",
        /* value */"83589362",
        /* description */"Unique ID number which is assigned by the signing "
                + "merchant's acquirer, bank or processor and used to identify "
                + "the merchant within the SecureCode system",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 24
        configs[i] = new KKConfiguration(
        /* title */"MasterCard Merchant Password",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_MC_MERCHANT_PASSWORD",
        /* value */"12345678",
        /* description */"The alphanumeric merchant password is provided by the acquirer",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);
        
        // 25
        configs[i] = new KKConfiguration(
        /* title */"Return Token Id",
        /* key */"MODULE_PAYMENT_COMMIDEA_VANGUARD_RETURN_TOKEN_ID",
        /* value */"false",
        /* description */"If set to true, a token id is returned and the transaction can be completed later",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now);


        return configs;
    }
}