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

package com.konakartadmin.modules.shipping.flat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

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
 * Flat Rate shipping module
 */
public class Flat extends ShippingModule
{
    private static String MODULE_CODE = "flat";

    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_FLAT");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_SHIPPING_FLAT_TEXT_TITLE");
    }

    /**
     * @return the implementation filename - for compatibility with osCommerce we use the php name
     */
    public String getImplementationFileName()
    {
        return "flat.php";
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
            configs = new KKConfiguration[5];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        configs[i++] = new KKConfiguration("Enable Flat Shipping", "MODULE_SHIPPING_FLAT_STATUS",
                "True", "Do you want to offer flat rate shipping?", 6, 0, "",
                "tep_cfg_select_option(array('True', 'False'),", now);
        configs[i++] = new KKConfiguration("Shipping Cost", "MODULE_SHIPPING_FLAT_COST", "5.00",
                "The shipping cost for all orders using this shipping method.", 6, 0, "", "", now);
        configs[i++] = new KKConfiguration("Tax Class", "MODULE_SHIPPING_FLAT_TAX_CLASS", "0",
                "Use the following tax class on the shipping fee.", 6, 0,
                "tep_get_tax_class_title", "tep_cfg_pull_down_tax_classes(", now);
        configs[i++] = new KKConfiguration("Shipping Zone", "MODULE_SHIPPING_FLAT_ZONE", "0",
                "If a zone is selected, only enable this shipping method for that zone.", 6, 0,
                "tep_get_zone_class_title", "tep_cfg_pull_down_zone_classes(", now);
        configs[i++] = new KKConfiguration("Sort Order", "MODULE_SHIPPING_FLAT_SORT_ORDER", "0",
                "Sort order of display.", 6, 0, "", "", now);

        return configs;
    }

    public ExportOrderResponse exportOrderForShipping(AdminOrder order, ExportOrderOptions options,
            KKAdminIf adminEng) throws KKAdminException
    {
        if (log.isInfoEnabled())
        {
            log.info("Export a file for Flat - just a trivial example");
        }

        // This is just an example. You will probably need to change this export format for your own
        // needs.

        String exportBaseDir = getOrderExportBase(adminEng);

        String orderExportFileName = exportBaseDir + String.valueOf(order.getId()) + ".xml";
        String storeName = getConfigVariable(adminEng, KonakartAdminConstants.STORE_NAME);
        String storeOwner = getConfigVariable(adminEng, KonakartAdminConstants.STORE_OWNER);
        AdminCountry deliveryCountry = order.getDeliveryCountryObject();

        try
        {
            File orderExportFile = new File(orderExportFileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(orderExportFile));

            bw.write("<?xml version=\"1.0\"?>");
            bw.write("<Order>");

            bw.write("<ShipTo>");
            bw.write("<CustomerID>" + order.getCustomerId() + "</CustomerID>");
            bw.write("<CompanyOrName>" + order.getDeliveryCompany() + "</CompanyOrName>");
            bw.write("<Attention>" + order.getDeliveryName() + "</Attention>");
            bw.write("<Address1>" + order.getDeliveryStreetAddress() + "</Address1>");
            bw.write("<Address2>" + order.getDeliveryStreetAddress1() + "</Address2>");
            bw.write("<CountryTerritory>" + deliveryCountry.getIsoCode2() + "</CountryTerritory>");
            bw.write("<PostalCode>" + order.getDeliveryPostcode() + "</PostalCode>");
            bw.write("<CityOrTown>" + order.getDeliveryCity() + "</CityOrTown>");
            bw.write("<StateProvinceCounty>" + order.getDeliveryZoneObject().getZoneCode()
                    + "</StateProvinceCounty>");

            bw.write("<Telephone>" + order.getDeliveryTelephone() + "</Telephone>");
            bw.write("<EmailAddress>" + order.getCustomerEmail() + "</EmailAddress>");
            bw.write("</ShipTo>");

            bw.write("<ShipFrom>");
            bw.write("<CompanyOrName>" + storeName + "</CompanyOrName>");
            bw.write("<Attention>" + storeOwner + "</Attention>");
            bw.write("</ShipFrom>");

            bw.write("<ShipmentInformation>");
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

            bw.write("</Order>");

            bw.close();

            ExportOrderResponse response = new ExportOrderResponse();
            response.setCode(KKConstants.EXP_ORDER_SUCCESSFUL);
            response.setConfirmationText("Order exported successfully for Flat Shipping Module to "
                    + orderExportFileName);
            return response;

        } catch (Exception e)
        {
            throw new KKAdminException("Problem exporting order: " + e.getMessage());
        }
    }
}
