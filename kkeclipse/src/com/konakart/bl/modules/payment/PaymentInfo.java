//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.bl.modules.payment;

import java.util.Locale;

import com.konakart.app.Country;
import com.konakart.app.GeoZone;

/**
 * PaymentInfo
 */
public class PaymentInfo
{
    private GeoZone[] deliveryGeoZoneArray;

    private Country country;

    private Locale locale;

    private String storeName;

    private boolean displayPriceWithTax;

    private String hostAndPort;

    private boolean returnDetails;

    /**
     * The default implementation of the PaymentMgr fills this array with the Billing Geo Zones
     * rather than the Delivery Geo Zones as the name would suggest. The array of zones may be used
     * to determine whether the payment module is active or not.
     * 
     * @return Returns the deliveryGeoZoneArray.
     */
    public GeoZone[] getDeliveryGeoZoneArray()
    {
        return deliveryGeoZoneArray;
    }

    /**
     * The default implementation of the PaymentMgr fills this array with the Billing Geo Zones
     * rather than the Delivery Geo Zones as the name would suggest. The array of zones may be used
     * to determine whether the payment module is active or not.
     * 
     * @param deliveryGeoZoneArray
     *            The deliveryGeoZoneArray to set.
     */
    public void setDeliveryGeoZoneArray(GeoZone[] deliveryGeoZoneArray)
    {
        this.deliveryGeoZoneArray = deliveryGeoZoneArray;
    }

    /**
     * The default implementation of the PaymentMgr fills this attribute with the billing country.
     * 
     * @return the country
     */
    public Country getCountry()
    {
        return country;
    }

    /**
     * The default implementation of the PaymentMgr fills this attribute with the billing country.
     * 
     * @param country
     *            the country to set
     */
    public void setCountry(Country country)
    {
        this.country = country;
    }

    /**
     * @return Returns the locale.
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * @param locale
     *            The locale to set.
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * @return Returns the storeName.
     */
    public String getStoreName()
    {
        return storeName;
    }

    /**
     * @param storeName
     *            The storeName to set.
     */
    public void setStoreName(String storeName)
    {
        this.storeName = storeName;
    }

    /**
     * @return Returns the displayPriceWithTax.
     */
    public boolean isDisplayPriceWithTax()
    {
        return displayPriceWithTax;
    }

    /**
     * @param displayPriceWithTax
     *            The displayPriceWithTax to set.
     */
    public void setDisplayPriceWithTax(boolean displayPriceWithTax)
    {
        this.displayPriceWithTax = displayPriceWithTax;
    }

    /**
     * @return Returns the returnDetails.
     */
    public boolean isReturnDetails()
    {
        return returnDetails;
    }

    /**
     * @param returnDetails
     *            The returnDetails to set.
     */
    public void setReturnDetails(boolean returnDetails)
    {
        this.returnDetails = returnDetails;
    }

    /**
     * @return Returns the hostAndPort.
     */
    public String getHostAndPort()
    {
        return hostAndPort;
    }

    /**
     * @param hostAndPort
     *            The hostAndPort to set.
     */
    public void setHostAndPort(String hostAndPort)
    {
        this.hostAndPort = hostAndPort;
    }
}
