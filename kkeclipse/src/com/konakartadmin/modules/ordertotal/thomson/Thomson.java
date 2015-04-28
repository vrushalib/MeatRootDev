//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakartadmin.modules.ordertotal.thomson;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OrderTotalModule;

/**
 * Thomson Reuters order total module
 */
public class Thomson extends OrderTotalModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_THOMSON");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_ORDER_TOTAL_THOMSON_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Thomson";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "Thomson";
    }
    
    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[39];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        configs[i] = new KKConfiguration(
        /* title */"Enable Thomson Reuters Tax",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_STATUS",
        /* value */"true",
        /* description */"Do you want to display the order tax value from Thomson Reuters?",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('True', 'False')",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i] = new KKConfiguration(
        /* title */"Save Messages to Database",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SAVE_MSG_DB",
        /* value */"true",
        /* description */"Do you want to save the request and response messages in the database?",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('True', 'False')",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i] = new KKConfiguration(
        /* title */"Commit Orders to Thomson Reuters",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_COMMIT_STATUS",
        /* value */"false",
        /* description */"Do you want to commit the order to Thomson Reuters?",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('True', 'False')",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i] = new KKConfiguration(
        /* title */"Sort Order",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SORT_ORDER",
        /* value */"1",
        /* description */"Sort order of display.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Company Role",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_COMPANY_ROLE",
        /* value */"S",
        /* description */"Company Role (eg S)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Company Name",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_COMPANY_NAME",
        /* value */"",
        /* description */"Company Name (eg ABC Inc)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"External Company Id",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_EXT_COMPANY_ID",
        /* value */"",
        /* description */"External Company Id (obtain from Thomson Reuters)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Seller Role",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_ROLE",
        /* value */"",
        /* description */"Sellor Role (typically a tax number known to Thomson Reuters)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Tax Service Username (WSS)",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_USERNAME",
        /* value */"",
        /* description */"Tax Service Username (to access the Thomson Reuters Tax Service using WSS Authentication)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Tax Service Password (WSS)",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_PASSWORD",
        /* value */"",
        /* description */"Tax Service Password (to access the Thomson Reuters Tax Service using WSS Authentication)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Add Header Credentials (WSS)",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SECURE",
        /* value */"true",
        /* description */"Add the defined Username and Password to requests sent to the Tax Service using WSS Authentication",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('True', 'False')",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Tax Service Username (SOAP CALC)",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_BODY_USERNAME",
        /* value */"",
        /* description */"Tax Service Username (to add to the body of requests for SOAP CALC Authentication)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Tax Service Password (SOAP CALC)",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_BODY_PASSWORD",
        /* value */"",
        /* description */"Tax Service Password (to add to the body of requests for SOAP CALC Authentication)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Add Body Credentials (SOAP CALC)",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_BODY_SECURE",
        /* value */"true",
        /* description */"Add the defined Username and Password to the body of requests for SOAP CALC Authentication",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('True', 'False')",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Custom Tax Code Code Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CODE_FIELD",
        /* value */"-1",
        /* description */"Field on the order product that stores the 4 '|' separated tax codes - used with the Custom option for the 4 tax codes below",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"US Commodity Code Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD",
        /* value */"1",
        /* description */"Field on the order product that stores the US Commodity Code. If Custom is selected specify the field where it's stored in the 'Custom Tax Code Code Field'",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4,5=label.custom)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"EU Commodity Code Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD",
        /* value */"2",
        /* description */"Field on the order product that stores the EU Commodity Code. If Custom is selected specify the field where it's stored in the 'Custom Tax Code Code Field'",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4,5=label.custom)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"MOSS Indicator Code Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD",
        /* value */"3",
        /* description */"Field on the order product that stores the MOSS Indicator Code. If Custom is selected specify the field where it's stored in the 'Custom Tax Code Code Field'",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4,5=label.custom)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Transaction Type Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD",
        /* value */"4",
        /* description */"Field on the order product that stores the Transaction Type Code. If Custom is selected specify the field where it's stored in the 'Custom Tax Code Code Field'",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4,5=label.custom)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"VAT Included Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD",
        /* value */"5",
        /* description */"Field on the order product that stores the VAT Included Identifier (for ATTRIBUTE4)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4,5=label.custom)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Exemptions Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD",
        /* value */"5",
        /* description */"Field on the order product that stores the Exemptions Identifier (for ATTRIBUTE5)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.prod.cust1,2=label.prod.cust2,3=label.prod.cust3,4=label.prod.cust4,5=label.custom)",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Tax Identifier Field",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_TAX_IDENTIFIER_FIELD",
        /* value */"1",
        /* description */"Field on the customer that stores the Tax Identifier (eg the VAT number)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"option(-1=label.notSet,1=label.cust.cust1,2=label.cust.cust2,3=label.cust.cust3,4=label.cust.cust4)",
        /* dateAdd */now);
        
        // Removed the taxId option for now, just for compatibility with 7.2.0.2
        // Add this at back some point:  "0=label.taxId,"  
        
        configs[i] = new KKConfiguration(
        /* title */"Seller Street Address",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_STREET",
        /* value */"",
        /* description */"Seller Street Address",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Seller City",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_CITY",
        /* value */"",
        /* description */"Seller City",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Seller County",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTY",
        /* value */"",
        /* description */"Seller County Code (eg OFE)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Seller State",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_STATE",
        /* value */"",
        /* description */"Seller State Code (eg NY, CA)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Seller Province",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_PROVINCE",
        /* value */"",
        /* description */"Seller Province Code (eg NS)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Seller ZipCode / Postcode",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_POSTCODE",
        /* value */"",
        /* description */"Seller Postcode / ZipCode",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Seller Country",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTRY",
        /* value */"",
        /* description */"2-Character Seller Country Code (eg. GB, US, CA)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From Street Address",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STREET",
        /* value */"",
        /* description */"Ship From Street Address",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From City",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_CITY",
        /* value */"",
        /* description */"Ship From City",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From County",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTY",
        /* value */"",
        /* description */"Ship From County Code (eg OFE)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From State",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STATE",
        /* value */"",
        /* description */"Ship From State Code (eg NY, CA)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From Province",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_PROVINCE",
        /* value */"",
        /* description */"Ship From Province Code (eg NS)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From ZipCode / Postcode",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_POSTCODE",
        /* value */"",
        /* description */"Ship From Postcode / ZipCode",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Ship From Country",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTRY",
        /* value */"",
        /* description */"2-Character Ship From Country (eg. GB, US, CA)",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Tax Service Endpoint URL",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_TAX_URL",
        /* value */"http://host:8080/sabrix/services/taxcalculationservice/2011-09-01/taxcalculationservice",
        /* description */"Thomson Reuters Tax Service Endpoint URL eg. http://10.128.1.237:8080/sabrix/services/taxcalculationservice/2011-09-01/taxcalculationservice",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i] = new KKConfiguration(
        /* title */"Calling System Number",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_CALLING_SYS",
        /* value */"",
        /* description */"Calling System Number - Customer Specific",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);
        
        configs[i] = new KKConfiguration(
        /* title */"Custom Class",
        /* key */"MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CLASS",
        /* value */"com.konakart.bl.modules.ordertotal.thomson.ThomsonCustom",
        /* description */"the name of a custom class that imlpements "
                + "com.konakart.bl.modules.ordertotal.thomson.ThomsonCustomIf",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* return_by_api */false);
        return configs;
    }
}
