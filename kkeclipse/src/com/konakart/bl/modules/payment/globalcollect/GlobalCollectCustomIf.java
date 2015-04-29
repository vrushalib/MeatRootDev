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

package com.konakart.bl.modules.payment.globalcollect;

import com.konakart.appif.OrderIf;

/**
 * Global Collect Utilities - Customers can provide custom classes that implement this interface to
 * provide custom functionality for the module.
 */
public interface GlobalCollectCustomIf
{
    /**
     * Return the number of installments for this order. Return -1 if no installments should be
     * defined.
     * 
     * @param order
     *            the Order which should contain information on it that will indicate the number of
     *            installments.
     * @return the number of installments to define in the PaymentDetails parameter list. Return -1
     *         to indicate that no installments are required.
     */
    public int getNumberOfInstallments(OrderIf order);
}
