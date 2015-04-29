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
package com.konakart.util;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for calculating tax
 */
public class TaxUtils
{
    /** the log */
    protected static Log log = LogFactory.getLog(TaxUtils.class);

    /** Quantity rule */
    public static final int TAX_ON_TOTAL = 1;

    /** Quantity rule */
    public static final int TAX_PER_ITEM = 2;

    /**
     * Calculates the tax for one or more items
     * 
     * @param taxRate
     *            tax rate as a percentage
     * @param cost
     *            cost of a single item
     * @param quantity
     *            Number of items
     * @param scale
     *            This is the scale used for the precision of the calculations. It is contained in
     *            the ADMIN_CURRENCY_DECIMAL_PLACES configuration variable.
     * @param rule
     *            The rule to be used which should be either TAX_PER_ITEM or TAX_ON_TOTAL.
     *            <ul>
     *            <li>
     *            TaxUtils.TAX_PER_ITEM : The tax is calculated for a single item, to the number of
     *            decimal places defined by scale. Then this value is multiplied by the quantity.
     *            <li>
     *            TaxUtils.TAX_ON_TOTAL : The tax is calculated for the total amount (single item
     *            cost x quantity).
     *            </ul>
     * @return Returns the tax amount
     * 
     */
    public static BigDecimal getTaxAmount(BigDecimal taxRate, BigDecimal cost, int quantity,
            int scale, int rule)
    {
        if (taxRate == null || cost == null || quantity == 0)
        {
            return new BigDecimal(0);
        }

        BigDecimal lTaxRate = taxRate.divide(new BigDecimal(100));

        if (rule == TAX_PER_ITEM)
        {
            BigDecimal taxPerItem = cost.multiply(lTaxRate);
            taxPerItem = taxPerItem.setScale(scale, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalTax = taxPerItem.multiply(new BigDecimal(quantity));
            totalTax = totalTax.setScale(scale, BigDecimal.ROUND_HALF_UP);
            return totalTax;
        } else if (rule == TAX_ON_TOTAL)
        {
            BigDecimal totalPrice = cost.multiply(new BigDecimal(quantity));
            BigDecimal totalTax = totalPrice.multiply(lTaxRate);
            totalTax = totalTax.setScale(scale, BigDecimal.ROUND_HALF_UP);
            return totalTax;
        }

        // Should never get this far
        return new BigDecimal(0);
    }
}