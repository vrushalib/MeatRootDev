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
package com.konakart.bl.modules.payment;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;

import com.konakart.app.GeoZone;
import com.konakart.app.KKEng;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.AddressIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.blif.MultiStoreMgrIf;
import com.konakart.db.KKBasePeer;
import com.konakart.db.KKCriteria;
import com.konakart.om.BaseZonesToGeoZonesPeer;
import com.workingdogs.village.Record;

/**
 * Base class for Payment Modules.
 */
public class BasePaymentModule extends BaseModule
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(BasePaymentModule.class);

    /**
     * Ensures that the geoZoneId is within one of the Geo Zones contained in the PaymentInfo
     * object.
     * 
     * @param info
     * @param geoZoneId
     * @throws KKException
     */
    protected void checkZone(PaymentInfo info, int geoZoneId) throws KKException
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
        } else if (info.getCountry() != null)
        {
            if (isGeoZoneMappedToCountry(geoZoneId, info.getCountry().getId()))
            {
                return;
            }
        }
        if (!found)
        {
            throw new KKException(
                    "The billing address of the order is not within the GeoZone, id = " + geoZoneId);
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
     * Look up the first name and last name using the AddressId. Return them as two Strings in an
     * array. If there is no such address null is returned.
     * 
     * @param addrId
     *            the id of the address to look up
     * @return a String array where the first element is the first name and the second element is
     *         the surname. Returns null if the address cannot be found.
     * @throws Exception
     */
    protected String[] getFirstAndLastNamesFromAddress(int addrId) throws Exception
    {
        String[] result = null;

        if (addrId > 0)
        {
            AddressIf addr = getCustMgr().getAddress(addrId);

            if (addr != null)
            {
                result = new String[2];

                if (addr.getFirstName() != null)
                {
                    result[0] = addr.getFirstName();
                } else
                {
                    result[0] = "";
                }
                if (addr.getLastName() != null)
                {
                    result[1] = addr.getLastName();
                } else
                {
                    result[1] = "";
                }
            }
        }

        return result;
    }

    /**
     * Split the name into first name and surname using simple logic... The surname is assumed to be
     * the last whitespace-separated item; everything before that is used as the first name.
     * 
     * @deprecated Replaced by a more robust solution where the address is looked up
     *             {@link #splitStreetAddressIntoNumberAndStreet(String streetAddress)}
     * @param name
     *            the name as a String to convert.
     * @return a String array where the first element is the first name and the second element is
     *         the surname.
     */
    @Deprecated
    protected String[] splitNameIntoFirstAndLastNames(String name)
    {
        String[] result = new String[2];

        if (name != null)
        {
            String[] names = name.split(" ");
            int len = names.length;
            if (len >= 2)
            {
                StringBuffer firstName = new StringBuffer();
                for (int i = 0; i < len - 1; i++)
                {
                    if (firstName.length() == 0)
                    {
                        firstName.append(names[i]);
                    } else
                    {
                        firstName.append(" ");
                        firstName.append(names[i]);
                    }
                }
                result[0] = firstName.toString();
                result[1] = names[len - 1];
            }
        }

        return result;
    }

    /**
     * Split the street address into house number and street using simple logic... The house number
     * is assumed to be the number at the start of the String if present. The rest is assumed to be
     * the street.
     * 
     * @param streetAddress
     *            the streetAddress as a String to convert.
     * @return a String array where the first element is the house number (if found) and the second
     *         element is the street address without the house number if that was found.
     */
    protected String[] splitStreetAddressIntoNumberAndStreet(String streetAddress)
    {
        String[] result = new String[2];

        // Set the defaults for when we don't manage to find the House Number Successfully
        result[0] = null;
        result[1] = streetAddress;

        if (streetAddress != null)
        {
            String[] parts = streetAddress.split(" ");
            int len = parts.length;
            if (len >= 2)
            {
                // Is the first digit a number?
                if (parts[0].charAt(0) >= '0' && parts[0].charAt(0) <= '9')
                {
                    result[0] = streetAddress.substring(0, parts[0].length());
                    result[1] = streetAddress.substring(parts[0].length()).trim();
                }
            }
        }

        return result;
    }

    /**
     * This method is optionally called from the sub class to load up the parameters into a
     * HashTable for efficient subsequent processing
     * 
     * @param pd
     *            PaymentDetails object
     * @param ccParmList
     *            name value pair list of CC parameters
     * @return a hash map containing the parameters for rapid lookup
     */
    protected HashMap<String, String> hashParameters(PaymentDetailsIf pd,
            List<NameValueIf> ccParmList)
    {
        HashMap<String, String> paramHash = new HashMap<String, String>();

        if (pd != null)
        {
            if (pd.getParameters() != null)
            {
                for (int c = 0; c < pd.getParameters().length; c++)
                {
                    paramHash
                            .put(pd.getParameters()[c].getName(), pd.getParameters()[c].getValue());
                }
            }
        }

        if (ccParmList != null)
        {
            for (int c = 0; c < ccParmList.size(); c++)
            {
                paramHash.put(ccParmList.get(c).getName(), ccParmList.get(c).getValue());
            }
        }

        return paramHash;
    }

    /**
     * Add more parameters to the PaymentDetails object. Normally this method would be on the
     * PaymentDetails class but it's placed here instead because of the automatic code generation
     * that occurs on the PaymentDetails class for web services, JSON and RMI etc.
     * 
     * @param pd
     *            the PaymentDetails object
     * @param newParameters
     *            The parameters to set.
     */
    protected void addParameters(PaymentDetailsIf pd, List<NameValueIf> newParameters)
    {
        List<NameValueIf> parmList = new ArrayList<NameValueIf>();

        // Add the new parameters to our temporary list
        if (newParameters != null)
        {
            for (int p = 0; p < newParameters.size(); p++)
            {
                parmList.add(newParameters.get(p));
            }
        }

        // Add the existing parameters to our temporary list
        if (pd.getParameters() != null)
        {
            for (int p = 0; p < pd.getParameters().length; p++)
            {
                parmList.add(pd.getParameters()[p]);
            }
        }

        // Now replace the parameters with a new set
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pd.setParameters(nvArray);
    }

    /**
     * Return the IP address of the current machine
     * 
     * @return the IP address of the current machine as a String
     */
    protected String getIPAddress() throws KKException
    {
        try
        {
            InetAddress ownIP = InetAddress.getLocalHost();

            if (log.isDebugEnabled())
            {
                log.debug("IP of this system is " + ownIP.getHostAddress());
            }

            return ownIP.getHostAddress();
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new KKException("Problem finding IP Address", e);
        }
    }

    /**
     * Method used to return any custom information required from the payment module. When used,
     * this method will typically be overridden in the module.
     * 
     * @param sessionId
     * @param parameters
     * @return Returns information in a PaymentDetails object
     * @throws Exception
     */
    public PaymentDetails getPaymentDetailsCustom(String sessionId, NameValueIf[] parameters)
            throws Exception
    {
        return null;
    }
}
