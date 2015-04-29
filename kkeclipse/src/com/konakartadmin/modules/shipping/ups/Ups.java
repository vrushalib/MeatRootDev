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

package com.konakartadmin.modules.shipping.ups;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;

import com.konakart.app.ExportOrderOptions;
import com.konakart.app.ExportOrderResponse;
import com.konakart.util.KKConstants;
import com.konakart.util.Utils;
import com.konakartadmin.app.AdminCountry;
import com.konakartadmin.app.AdminOrder;
import com.konakartadmin.app.KKAdminException;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.appif.KKAdminIf;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.bl.KonakartAdminConstants;
import com.konakartadmin.modules.ShippingModule;

/**
 * UPS shipping module
 */
public class Ups extends ShippingModule
{
    private static String MODULE_CODE = "ups";

    // private final static String MODULE_SHIPPING_UPS_LICENSE_NUMBER =
    // "MODULE_SHIPPING_UPS_LICENSE_NUMBER";

    private final static String MODULE_SHIPPING_UPS_USERID = "MODULE_SHIPPING_UPS_USERID";

    private final static String SHIPPING_ORIGIN_COUNTRY = "SHIPPING_ORIGIN_COUNTRY";

    private final static String SHIPPING_ORIGIN_ZIP = "SHIPPING_ORIGIN_ZIP";

    private final static String SHIP_FROM_CITY = "SHIP_FROM_CITY";

    private final static String SHIP_FROM_STREET_ADDRESS = "SHIP_FROM_STREET_ADDRESS";

    // Hash Map containing Service Code to Service Type mapping
    private static HashMap<String, String> serviceTypeMap = new HashMap<String, String>();

    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_UPS");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_UPS_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "ups";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return MODULE_CODE;
    }

    /**
     * @return an array of configuration values for this payment module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[14];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();
        int groupId = 6;
        int i = 0;
        configs[i++] = new KKConfiguration(
        /* title */"Enable UPS Shipping",
        /* key */"MODULE_SHIPPING_UPS_STATUS",
        /* value */"True",
        /* description */"Do you want to offer UPS shipping?",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"tep_cfg_select_option(array('True', 'False'),",
        /* dateAdd */now,
        /* returnByApi */true);

        configs[i++] = new KKConfiguration(
        /* title */"Tax Class",
        /* key */"MODULE_SHIPPING_UPS_TAX_CLASS",
        /* value */"0",
        /* description */"Use the following tax class on the shipping fee.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_tax_class_title",
        /* setFun */"tep_cfg_pull_down_tax_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Shipping Zone",
        /* key */"MODULE_SHIPPING_UPS_ZONE",
        /* value */"0",
        /* description */"If a zone is selected, only enable this shipping method for that zone.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"tep_get_zone_class_title",
        /* setFun */"tep_cfg_pull_down_zone_classes(",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Sort Order",
        /* key */"MODULE_SHIPPING_UPS_SORT_ORDER",
        /* value */"0",
        /* description */"Sort order of display.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"UPS Access License Number",
        /* key */"MODULE_SHIPPING_UPS_LICENSE_NUMBER",
        /* value */"",
        /* description */"Your UPS access license number for using the service.",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Request URL",
        /* key */"MODULE_SHIPPING_UPS_URL",
        /* value */"https://onlinetools.ups.com/ups.app/xml/Rate",
        /* description */"The URL where the XML request is sent",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"UPS UserId",
        /* key */"MODULE_SHIPPING_UPS_USERID",
        /* value */"",
        /* description */"The UPS UserId for using the service",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"UPS Password",
        /* key */"MODULE_SHIPPING_UPS_PASSWORD",
        /* value */"",
        /* description */"The UPS password for using the service",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"Return quote for a defined service code",
                /* key */"MODULE_SHIPPING_UPS_RATE_OR_SHOP",
                /* value */"False",
                /* description */"Set to true in order to choose a single rate. Set to false to shop for all available rates",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"tep_cfg_select_option(array('True', 'False'),",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"UPS Service code",
                /* key */"MODULE_SHIPPING_UPS_SERVICE_CODE",
                /* value */"",
                /* description */"Only used if we want a single quote for a defined service code. In this case it defines the service code that we want.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"UPS Service codes to exclude",
                /* key */"MODULE_SHIPPING_UPS_SERVICE_CODES_EXCLUDE",
                /* value */"",
                /* description */"Comma separated list of service codes that will be excluded from the quotes returned to the customer (i.e. 01,07,12).",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Measurement Unit",
        /* key */"MODULE_SHIPPING_UPS_MEASUREMENT_UNIT",
        /* value */"LBS",
        /* description */"Should be set to KGS or LBS",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        configs[i++] = new KKConfiguration(
                /* title */"Packaging type by weight",
                /* key */"MODULE_SHIPPING_UPS_PACKAGING_TYPE",
                /* value */"1000:00",
                /* description */"Example (10:25,25:24,1000:00): weight 5-10 = box code 25; weight 10-25 = box code 24 etc.",
                /* groupId */groupId,
                /* sort Order */i,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        configs[i++] = new KKConfiguration(
        /* title */"Handling Fee",
        /* key */"MODULE_SHIPPING_UPS_HANDLING",
        /* value */"0",
        /* description */"Handling fee for this shipping method",
        /* groupId */groupId,
        /* sort Order */i,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }

    public ExportOrderResponse exportOrderForShipping(AdminOrder order, ExportOrderOptions options,
            KKAdminIf adminEng) throws KKAdminException
    {
        if (log.isInfoEnabled())
        {
            log.info("Export a file for UPS");
        }

        setServiceTypeMap();

        String exportBaseDir = getOrderExportBase(adminEng);

        String orderExportFileName = exportBaseDir + String.valueOf(order.getId()) + ".xml";
        String storeName = getConfigVariable(adminEng, KonakartAdminConstants.STORE_NAME);
        String storeOwner = getConfigVariable(adminEng, KonakartAdminConstants.STORE_OWNER);
        String upsAccountNum = getConfigVariable(adminEng, MODULE_SHIPPING_UPS_USERID);
        String shipOriginZip = getConfigVariable(adminEng, SHIPPING_ORIGIN_ZIP);
        String shipFromCity = getConfigVariable(adminEng, SHIP_FROM_CITY);
        String shipFromAddress = getConfigVariable(adminEng, SHIP_FROM_STREET_ADDRESS);
        String shipOriginCountry = getConfigVariable(adminEng, SHIPPING_ORIGIN_COUNTRY);
        AdminCountry deliveryCountry = order.getDeliveryCountryObject();
        String serviceType = serviceTypeMap.get(order.getShippingServiceCode());

        try
        {
            File orderExportFile = new File(orderExportFileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(orderExportFile));

            bw.write("<?xml version=\"1.0\" encoding=\"windows-1252\"?>");
            bw.write("<OpenShipments xmlns=\"x-schema:OpenShipments.xdr\">");
            bw.write("<OpenShipment ShipmentOption=\"\" ProcessStatus=\"\">");

            bw.write("<ShipTo>");
            bw.write("<CustomerID>" + order.getCustomerId() + "</CustomerID>");
            bw.write("<CompanyOrName>" + order.getDeliveryCompany() + "</CompanyOrName>");
            bw.write("<Attention>" + order.getDeliveryName() + "</Attention>");
            bw.write("<Address1>" + order.getDeliveryStreetAddress() + "</Address1>");
            bw.write("<Address2>" + order.getDeliveryStreetAddress1() + "</Address2>");
            bw.write("<CountryTerritory>" + deliveryCountry.getIsoCode2() + "</CountryTerritory>");
            bw.write("<PostalCode>" + order.getDeliveryPostcode() + "</PostalCode>");
            bw.write("<CityOrTown>" + order.getDeliveryCity() + "</CityOrTown>");

            if (deliveryCountry.getIsoCode2().equals("US")
                    || deliveryCountry.getIsoCode2().equals("CA"))
            {
                // US and Canada only
                bw.write("<StateProvinceCounty>" + order.getDeliveryZoneObject().getZoneCode()
                        + "</StateProvinceCounty>");
            }

            bw.write("<Telephone>" + order.getDeliveryTelephone() + "</Telephone>");
            bw.write("<EmailAddress>" + order.getCustomerEmail() + "</EmailAddress>");
            bw.write("</ShipTo>");

            bw.write("<ShipFrom>");
            bw.write("<CompanyOrName>" + storeName + "</CompanyOrName>");
            bw.write("<Attention>" + storeOwner + "</Attention>");
            bw.write("<CountryTerritory>" + shipOriginCountry + "</CountryTerritory>");
            bw.write("<Address1>" + shipFromAddress + "</Address1>");
            bw.write("<PostalCode>" + shipOriginZip + "</PostalCode>");
            bw.write("<CityOrTown>" + shipFromCity + "</CityOrTown>");
            bw.write("<UpsAccountNumber>" + upsAccountNum + "</UpsAccountNumber>");
            bw.write("</ShipFrom>");

            bw.write("<ShipmentInformation>");
            bw.write("<ServiceType>" + serviceType + "</ServiceType>");
            bw.write("<NumberOfPackages>1</NumberOfPackages>");
            bw
                    .write("<ShipmentActualWeight>" + getWeightOfOrder(order)
                            + "</ShipmentActualWeight>");
            bw.write("<DescriptionOfGoods>Regular Shipment</DescriptionOfGoods>");
            bw.write("<Reference1>" + "Order Id:     " + order.getId() + "</Reference1>");
            bw.write("<Reference2>" + "Order Number: " + order.getOrderNumber() + "</Reference2>");
            bw.write("<Reference3>" + "Shipping Quote Code: " + order.getShippingServiceCode()
                    + "</Reference3>");
            bw.write("<BillingOption>PP</BillingOption>");
            bw.write("</ShipmentInformation>");

            bw.write("</OpenShipment>");
            bw.write("</OpenShipments>");

            bw.close();

            ExportOrderResponse response = new ExportOrderResponse();
            response.setCode(KKConstants.EXP_ORDER_SUCCESSFUL);
            response.setConfirmationText("Order exported successfully for UPS to "
                    + orderExportFileName);
            return response;

        } catch (Exception e)
        {
            throw new KKAdminException("Problem exporting order: " + e.getMessage());
        }
    }

    /**
     * Defines mappings between UPS service codes and service types.
     */
    private void setServiceTypeMap()
    {
        if (serviceTypeMap.isEmpty())
        {
            serviceTypeMap.put("01", "1DA" /* "UPS Next Day Air" */);
            serviceTypeMap.put("02", "2DA" /* "UPS Second Day Air" */);
            serviceTypeMap.put("03", "GND" /* "UPS Ground" */);
            serviceTypeMap.put("07", "ES" /* "UPS Worldwide ExpressSM" */);
            serviceTypeMap.put("08", "EX" /* "UPS Worldwide ExpeditedSM" */);
            serviceTypeMap.put("11", "ST" /* "UPS Standard" */);
            serviceTypeMap.put("12", "3DS" /* "UPS Three-Day Select" */);
            serviceTypeMap.put("13", "1DP" /* "UPS Next Day Air Saver" */);
            serviceTypeMap.put("14", "1DM" /* "UPS Next Day Air Early A.M. SM" */);
            serviceTypeMap.put("54", "EP" /* "UPS Worldwide Express PlusSM" */);
            serviceTypeMap.put("59", "2DM" /* "UPS Second Day Air A.M." */);
            serviceTypeMap.put("65", "1DP" /* "UPS Saver" */);
            serviceTypeMap.put("82", "ST" /* "UPS Today StandardSM" */);
            serviceTypeMap.put("83", "EX" /* "UPS Today Dedicated CourierSM" */);
            serviceTypeMap.put("84", "EX" /* "UPS Today Intercity" */);
            serviceTypeMap.put("85", "ES" /* "UPS Today Express" */);
            serviceTypeMap.put("86", "SV" /* "UPS Today Express Saver" */);
        }
    }
}
