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

package com.konakart.bl.modules.payment.globalcollect;

import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;

/**
 * Global Collect Custom Utilities - this provides a default implementation. It is expected that
 * Customers may implement their own versions of this class then specify the name of the class in
 * the module configuration parameters.
 */
public class GlobalCollectCustom extends GlobalCollectCustomBase
{
    /**
     * Constructor
     * 
     * @param _eng
     *            a KKEngIf engine
     */
    public GlobalCollectCustom(KKEngIf _eng)
    {
        super(_eng);
    }

    /**
     * We'll just use the Base implementation here but Customers can create their own versions that
     * implement custom rules.
     */
    public int getNumberOfInstallments(OrderIf order)
    {
        return super.getNumberOfInstallments(order);
        //log.info("Returning 1 from GlobalCollectCustom");
        //return 1;
    }
}
