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
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.modules.ordertotal.thomson.Thomson.StaticData;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationResponse;
import com.sabrix.services.taxcalculationservice._2011_09_01.ZoneAddressType;

/**
 * Thomson Custom Utilities - this provides a default implementation. It is expected that Customers
 * may implement their own versions of this class then specify the name of the class in the module
 * configuration parameters.
 */
public class ThomsonCustom extends ThomsonCustomBase
{
    /**
     * Constructor
     * 
     * @param _eng
     *            a KKENgIf engine
     * @param _module
     *            the instance of the module
     */
    public ThomsonCustom(KKEngIf _eng, com.konakart.bl.modules.ordertotal.thomson.Thomson _module)
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
        return super.getCustomFieldValue(fieldName, customCodeFieldValue, sd, orderProduct);
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

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public String getGROSS_AMOUNTForProduct(StaticData sd, OrderProductIf orderProduct,
            OrderIf order)
    {
        return super.getGROSS_AMOUNTForProduct(sd, orderProduct, order);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public String getGROSS_AMOUNTForShipping(StaticData sd, ShippingQuoteIf shipping, OrderIf order)
    {
        return super.getGROSS_AMOUNTForShipping(sd, shipping, order);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */
    public void adjustOrderAfterTaxCalculation(StaticData sd, TaxCalculationResponse response,
            OrderIf order)
    {
        super.adjustOrderAfterTaxCalculation(sd, response, order);
    }

    /**
     * Use the default implementation. Customers can create their own versions in their own class if
     * they wish.
     */    
    public void calculateTotals(StaticData sd, OrderIf order)
    {
        super.calculateTotals(sd, order);
    }
}
