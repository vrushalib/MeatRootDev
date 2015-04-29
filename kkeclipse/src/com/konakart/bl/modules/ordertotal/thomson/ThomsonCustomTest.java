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

package com.konakart.bl.modules.ordertotal.thomson;

import com.konakart.app.Country;
import com.konakart.app.Zone;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.bl.modules.ordertotal.thomson.Thomson.StaticData;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.ZoneAddressType;

/**
 * Thomson Custom Utilities - this provides a Test implementation.
 */
public class ThomsonCustomTest extends ThomsonCustomBase
{
    /**
     * Constructor
     * 
     * @param _eng
     *            a KKENgIf engine
     * @param _module
     *            the instance of the module
     */
    public ThomsonCustomTest(KKEngIf _eng,
            com.konakart.bl.modules.ordertotal.thomson.Thomson _module)
    {
        super(_eng, _module);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public ZoneAddressType getShipToAddress(StaticData sd, OrderIf order, Country deliveryCountry,
            Zone deliveryZone)
    {
        return super.getShipToAddress(sd, order, deliveryCountry, deliveryZone);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public ZoneAddressType getBillToAddress(StaticData sd, OrderIf order, Country billingCountry,
            Zone billingZone)
    {
        return super.getBillToAddress(sd, order, billingCountry, billingZone);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public String getCustomFieldValue(String fieldName, String customCodeFieldValue, StaticData sd,
            OrderProductIf orderProduct)
    {
        if (log.isInfoEnabled())
        {
            log.info("fieldName = " + fieldName + " customCodeFieldValue = " + customCodeFieldValue);
        }

        if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD)
        {
            log.info("return US COMM CODE");
            return "US COMM CODE";
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD)
        {
            log.info("return EU COMM CODE");
            return "EU COMM CODE";
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD)
        {
            log.info("return M");
            return "M";
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD)
        {
            log.info("return GS");
            return "GS";
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD)
        {
            return "";
        } else if (fieldName == Thomson.MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD)
        {
            return "";
        } else
        {
            log.warn("Unknown field : " + fieldName);
        }

        return null;
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public void addUserElements(StaticData sd, OrderProductIf orderProduct, IndataLineType line)
    {
        super.addUserElements(sd, orderProduct, line);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public void addUserElementsShipping(StaticData sd, OrderIf order, IndataLineType line)
    {
        super.addUserElementsShipping(sd, order, line);
    }
}
