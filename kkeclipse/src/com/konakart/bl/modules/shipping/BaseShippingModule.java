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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;

import com.konakart.app.GeoZone;
import com.konakart.app.KKEng;
import com.konakart.app.KKException;
import com.konakart.bl.modules.BaseModule;
import com.konakart.blif.MultiStoreMgrIf;
import com.konakart.db.KKBasePeer;
import com.konakart.db.KKCriteria;
import com.konakart.om.BaseZonesToGeoZonesPeer;
import com.workingdogs.village.Record;

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
     * Ensures that the geoZoneId is within one of the Geo Zones contained in the ShippingInfo
     * object.
     * 
     * @param info
     * @param geoZoneId
     * @throws KKException
     */
    protected void checkZone(ShippingInfo info, int geoZoneId) throws KKException
    {
        boolean found = false;
        if (info.getDeliveryGeoZoneArray() != null && info.getDeliveryGeoZoneArray().length > 0)
        {
            for (int i = 0; i < info.getDeliveryGeoZoneArray().length; i++)
            {
                GeoZone gz = info.getDeliveryGeoZoneArray()[i];
                if (gz.getGeoZoneId() == geoZoneId)
                {
                    found = true;
                    break;
                }
            }
        } else if (info.getDeliveryCountry() != null)
        {
            if (isGeoZoneMappedToCountry(geoZoneId, info.getDeliveryCountry().getId()))
            {
                return;
            }
        }
        if (!found)
        {
            throw new KKException(
                    "The delivery address of the order is not within the GeoZone, id = "
                            + geoZoneId);
        }
    }

    /**
     * In some cases no zones may be defined for a country but a geo zone may be defined to map to
     * all zones of that country. On these occasions the DeliveryGeoZoneArray in the ShippingInfo
     * object is null so the checkZone method never returns true.
     * <p>
     * This method does a search in the zones_to_geo_zones table for any entries matching the
     * geoZoneId, countryId and zoneId of 0 which is the id that is set when the mapping is for all
     * zones of the country.
     * 
     * @param geoZoneId
     * @param countryId
     * @return Returns true if there is a mapping
     * @throws KKException
     * @throws TorqueException
     */
    protected boolean isGeoZoneMappedToCountry(int geoZoneId, int countryId)
    {
        try
        {
            KKCriteria c = getNewCriteria(isMultiStoreShareCustomers());
            c.addSelectColumn(BaseZonesToGeoZonesPeer.ZONE_ID);
            c.add(BaseZonesToGeoZonesPeer.ZONE_ID, 0);
            c.add(BaseZonesToGeoZonesPeer.ZONE_COUNTRY_ID, countryId);
            c.add(BaseZonesToGeoZonesPeer.GEO_ZONE_ID, geoZoneId);
            List<Record> rows = KKBasePeer.doSelect(c);
            if (rows == null || rows.size() == 0)
            {
                return false;
            }
            return true;
        } catch (Exception e)
        {
            log.error("Unexpected exception in isGeoZoneMappedToCountry method", e);
            return false;
        }
    }

    /**
     * Returns true if we need to share customers in multi-store single db mode
     * 
     * @return the multiStoreShareCustomers
     * @throws KKException
     */
    protected boolean isMultiStoreShareCustomers() throws KKException
    {
        KKEng eng = getEng();
        if (eng == null)
        {
            throw new KKException("This manager has been instantiated with KKEng set to null");
        }

        if (eng.getEngConf() != null)
        {
            return eng.getEngConf().isCustomersShared();
        }

        return false;
    }

    /**
     * Gets a new KKCriteria object with the option of it being for all stores when in multi-store
     * single db mode.
     * 
     * @return Returns a new KKCriteria object
     */
    protected KKCriteria getNewCriteria(boolean allStores)
    {
        MultiStoreMgrIf mgr = getMultiStoreMgr();
        if (mgr != null)
        {
            return mgr.getNewCriteria(allStores);
        }

        KKCriteria crit = new KKCriteria();
        return crit;
    }

    /**
     * If there are no products it throws an exception since shipping isn't required. They may be
     * all digital download products.
     * 
     * @param info
     *            ShippingInfo object - this defines the items to be shipped
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
