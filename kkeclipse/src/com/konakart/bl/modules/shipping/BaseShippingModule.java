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
package com.konakart.bl.modules.shipping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.GeoZone;
import com.konakart.app.KKException;
import com.konakart.bl.modules.BaseModule;

/**
 * The Base class for shipping modules
 */
public class BaseShippingModule extends BaseModule
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BaseShippingModule.class);
    
    /**
     * Ensures that the zone is within one of the Geo Zones contained in the ShippingInfo object.
     * 
     * @param info
     * @param zone
     * @throws KKException
     */
    protected void checkZone(ShippingInfo info, int zone) throws KKException
    {
        boolean found = false;
        if (info.getDeliveryGeoZoneArray() != null && info.getDeliveryGeoZoneArray().length > 0)
        {
            for (int i = 0; i < info.getDeliveryGeoZoneArray().length; i++)
            {
                GeoZone gz = info.getDeliveryGeoZoneArray()[i];
                if (gz.getGeoZoneId() == zone)
                {
                    found = true;
                }
            }
        }
        if (!found)
        {
            throw new KKException(
                    "The delivery address of the order is not within the GeoZone, id = " + zone);
        }
    }

    /**
     * If there are no products it throws an exception since shipping isn't required. They may be
     * all digital download products.
     * 
     * @param info ShippingInfo object - this defines the items to be shipped
     * @throws KKException
     */
    protected void checkForProducts(ShippingInfo info) throws KKException
    {
        if (info.getNumProducts() == 0)
        {
            throw new KKException("There are no products to be shipped");
        }
    }
}
