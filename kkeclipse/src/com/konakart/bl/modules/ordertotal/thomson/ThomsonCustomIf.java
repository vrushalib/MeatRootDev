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
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.modules.ordertotal.thomson.Thomson.StaticData;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationResponse;
import com.sabrix.services.taxcalculationservice._2011_09_01.ZoneAddressType;

/**
 * Thomson Utilities - Customers can provide custom classes that implement this interface to provide
 * custom functionality for the module.
 */
public interface ThomsonCustomIf
{
    /**
     * Return a populated ZoneAddressType object for the ShipTo address.
     * 
     * @param sd
     *            Static data - contains the module configuration details
     * @param order
     *            the Order which should contain information on it that will indicate the number of
     *            installments.
     * @param deliveryCountry
     *            Country object - derived from country name on order - it could be null
     * @param deliveryZone
     *            Zone object - derived from country and zone names on the order - it could be null
     * @return a populated ZoneAddressType object.
     */
    public ZoneAddressType getShipToAddress(StaticData sd, OrderIf order, Country deliveryCountry,
            Zone deliveryZone);

    /**
     * Return a populated ZoneAddressType object for the BillTo address.
     * 
     * @param sd
     *            Static data - contains the module configuration details
     * @param order
     *            the Order which should contain information on it that will indicate the number of
     *            installments.
     * @param billingCountry
     *            Country object - derived from country name on order - it could be null
     * @param billingZone
     *            Zone object - derived from country and zone names on the order - it could be null
     * @return a populated ZoneAddressType object.
     */
    public ZoneAddressType getBillToAddress(StaticData sd, OrderIf order, Country billingCountry,
            Zone billingZone);

    /**
     * Custom method for retrieving values from complicated places... expected to be customised to
     * suit local requirements. In the default case we expect that the 4 tax code fields are
     * '|'-separated and set on a single custom field that is identified by the
     * MODULE_ORDER_TOTAL_THOMSON_CUSTON_CODE_FIELD configuration setting.
     * 
     * @param fieldName
     *            Name of the field we are retrieving
     * @param customCodeFieldValue
     *            the value of the custom field (could be in any format)
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param orderProduct
     *            the order product. An order will have one or more orderProducts
     * @return the value for the fieldName specified
     */
    public String getCustomFieldValue(String fieldName, String customCodeFieldValue, StaticData sd,
            OrderProductIf orderProduct);

    /**
     * Add zero or more User Elements to the line object for the product lines
     * 
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param orderProduct
     *            the order product. An order will have one or more orderProducts
     * @param line
     *            the IndataLineType line
     */
    public void addUserElements(StaticData sd, OrderProductIf orderProduct, IndataLineType line);

    /**
     * Add zero or more User Elements to the line object for the shipping line
     * 
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param order
     *            the order. 
     * @param line
     *            the IndataLineType line
     */
    public void addUserElementsShipping(StaticData sd, OrderIf order, IndataLineType line);
    
    /**
     * Get the value to be used for GROSS_AMOUNT for a product
     * @param sd
     * @param orderProduct
     * @param order
     * @return a String representing the value to use for the GROSS_AMOUNT for a product
     */
    public String getGROSS_AMOUNTForProduct(StaticData sd, OrderProductIf orderProduct, OrderIf order);
    
    /**
     * Get the value to be used for GROSS_AMOUNT for shipping
     * @param sd
     * @param shipping
     * @param order
     * @return a String representing the value to use for the GROSS_AMOUNT for shipping
     */
    public String getGROSS_AMOUNTForShipping(StaticData sd, ShippingQuoteIf shipping, OrderIf order);
    
    /**
     * Make any required adjustments to the original order after the call to the tax module.
     * @param sd
     * @param response
     * @param order
     */
    public void adjustOrderAfterTaxCalculation(StaticData sd, TaxCalculationResponse response, OrderIf order);
    
    /**
     * Return the adjustment for freight inclusive shipping.  This will be deducted from the order's total tax. 
     * @param sd
     * @param response
     * @param order
     * @return the adjustment value for freight inclusive shipping
     */
    // public BigDecimal adjustmentForInclusiveShipping(StaticData sd, TaxCalculationResponse response,
    //        OrderIf order);
    
    /**
     * Calculate the totals for the Order after the tax is returned from the External Tax Service
     * @param sd
     * @param order
     */
    public void calculateTotals(StaticData sd, OrderIf order);
}
