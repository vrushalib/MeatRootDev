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
package com.konakart.bl.modules.shipping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.konakart.app.Country;
import com.konakart.app.GeoZone;
import com.konakart.app.Zone;

/**
 * The object used by the engine to receive information from the modules
 */
public class ShippingInfo
{
    private Country originCountry;

    private Country deliveryCountry;

    private Zone deliveryZone;

    private GeoZone[] deliveryGeoZoneArray;

    private String originZip;

    private int numProducts;

    private int numDigitalDownloads;

    private int numBookableProducts;

    private int numVirtualProducts;

    private int numGiftCertificates;

    private int numFreeShipping;

    private BigDecimal maxWeight;

    private BigDecimal boxWeight;

    private BigDecimal boxPadding;

    private List<BigDecimal> orderWeightList;

    private Locale locale;

    /**
     * Returns a string containing the attributes of the ShippingDescription object
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("ShippingInfo:\n");
        str.append("originZip             = ").append(getOriginZip()).append("\n");
        str.append("maxWeight             = ").append(getMaxWeight()).append("\n");
        str.append("boxWeight             = ").append(getBoxWeight()).append("\n");
        str.append("boxPadding            = ").append(getBoxPadding()).append("\n");
        str.append("orderWeightList       = ").append(getOrderWeightList()).append("\n");
        str.append("numProducts           = ").append(getNumProducts()).append("\n");
        str.append("numDigitalDownloads   = ").append(getNumDigitalDownloads()).append("\n");
        str.append("numBookableProducts   = ").append(getNumBookableProducts()).append("\n");
        str.append("numGiftCertificates   = ").append(getNumGiftCertificates()).append("\n");
        str.append("numNumVirtualProducts = ").append(getNumVirtualProducts()).append("\n");
        str.append("numFreeShipping       = ").append(getNumFreeShipping()).append("\n");
        if (locale != null)
        {
            str.append("locale Country        = ").append(getLocale().getCountry()).append("\n");
            str.append("locale Language       = ").append(getLocale().getLanguage()).append("\n");
        } else
        {
            str.append("locale                = ").append("null").append("\n");
        }
        if (deliveryGeoZoneArray != null)
        {
            for (int i = 0; i < deliveryGeoZoneArray.length; i++)
            {
                str.append("Geo Zone              = ").append(deliveryGeoZoneArray[i]).append("\n");
            }
        }
        str.append("originCountry>\n").append(getOriginCountry()).append("\n");
        str.append("deliveryCountry>\n").append(getDeliveryCountry()).append("\n");
        str.append("deliveryZone>\n").append(getDeliveryZone()).append("\n");

        return (str.toString());
    }

    /**
     * @return Returns the boxPadding.
     */
    public BigDecimal getBoxPadding()
    {
        return boxPadding;
    }

    /**
     * @param boxPadding
     *            The boxPadding to set.
     */
    public void setBoxPadding(BigDecimal boxPadding)
    {
        this.boxPadding = boxPadding;
    }

    /**
     * @return Returns the boxWeight.
     */
    public BigDecimal getBoxWeight()
    {
        return boxWeight;
    }

    /**
     * @param boxWeight
     *            The boxWeight to set.
     */
    public void setBoxWeight(BigDecimal boxWeight)
    {
        this.boxWeight = boxWeight;
    }

    /**
     * @return Returns the maxWeight.
     */
    public BigDecimal getMaxWeight()
    {
        return maxWeight;
    }

    /**
     * @param maxWeight
     *            The maxWeight to set.
     */
    public void setMaxWeight(BigDecimal maxWeight)
    {
        this.maxWeight = maxWeight;
    }

    /**
     * @return Returns the originCountry.
     */
    public Country getOriginCountry()
    {
        return originCountry;
    }

    /**
     * @param originCountry
     *            The originCountry to set.
     */
    public void setOriginCountry(Country originCountry)
    {
        this.originCountry = originCountry;
    }

    /**
     * @return Returns the originZip.
     */
    public String getOriginZip()
    {
        return originZip;
    }

    /**
     * @param originZip
     *            The originZip to set.
     */
    public void setOriginZip(String originZip)
    {
        this.originZip = originZip;
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
     * @return Returns the orderWeightList.
     */
    public List<BigDecimal> getOrderWeightList()
    {
        return orderWeightList;
    }

    /**
     * @param orderWeightList
     *            The orderWeightList to set.
     */
    public void setOrderWeightList(List<BigDecimal> orderWeightList)
    {
        this.orderWeightList = orderWeightList;
    }

    /**
     * @return Returns the deliveryCountry.
     */
    public Country getDeliveryCountry()
    {
        return deliveryCountry;
    }

    /**
     * @param deliveryCountry
     *            The deliveryCountry to set.
     */
    public void setDeliveryCountry(Country deliveryCountry)
    {
        this.deliveryCountry = deliveryCountry;
    }

    /**
     * @return Returns the deliveryZone.
     */
    public Zone getDeliveryZone()
    {
        return deliveryZone;
    }

    /**
     * @param deliveryZone
     *            The deliveryZone to set.
     */
    public void setDeliveryZone(Zone deliveryZone)
    {
        this.deliveryZone = deliveryZone;
    }

    /**
     * @return Returns the deliveryGeoZoneArray.
     */
    public GeoZone[] getDeliveryGeoZoneArray()
    {
        return deliveryGeoZoneArray;
    }

    /**
     * @param deliveryGeoZoneArray
     *            The deliveryGeoZoneArray to set.
     */
    public void setDeliveryGeoZoneArray(GeoZone[] deliveryGeoZoneArray)
    {
        this.deliveryGeoZoneArray = deliveryGeoZoneArray;
    }

    /**
     * The number of physical products to be shipped
     * 
     * @return Returns the numProducts.
     */
    public int getNumProducts()
    {
        return numProducts;
    }

    /**
     * The number of physical products to be shipped
     * 
     * @param numProducts
     *            The numProducts to set.
     */
    public void setNumProducts(int numProducts)
    {
        this.numProducts = numProducts;
    }

    /**
     * The number of digital download products
     * 
     * @return Returns the numDigitalDownloads.
     */
    public int getNumDigitalDownloads()
    {
        return numDigitalDownloads;
    }

    /**
     * The number of digital download products
     * 
     * @param numDigitalDownloads
     *            The numDigitalDownloads to set.
     */
    public void setNumDigitalDownloads(int numDigitalDownloads)
    {
        this.numDigitalDownloads = numDigitalDownloads;
    }

    /**
     * The number of products that need to be shipped for free.
     * 
     * @return Returns the numFreeShipping.
     */
    public int getNumFreeShipping()
    {
        return numFreeShipping;
    }

    /**
     * The number of products that need to be shipped for free.
     * 
     * @param numFreeShipping
     *            The numFreeShipping to set.
     */
    public void setNumFreeShipping(int numFreeShipping)
    {
        this.numFreeShipping = numFreeShipping;
    }

    /**
     * The number of bookable products
     * 
     * @return the numBookableProducts
     */
    public int getNumBookableProducts()
    {
        return numBookableProducts;
    }

    /**
     * The number of bookable products
     * 
     * @param numBookableProducts
     *            the numBookableProducts to set
     */
    public void setNumBookableProducts(int numBookableProducts)
    {
        this.numBookableProducts = numBookableProducts;
    }

    /**
     * The number of gift certificates
     * 
     * @return the numGiftCertificates
     */
    public int getNumGiftCertificates()
    {
        return numGiftCertificates;
    }

    /**
     * The number of gift certificates
     * 
     * @param numGiftCertificates
     *            the numGiftCertificates to set
     */
    public void setNumGiftCertificates(int numGiftCertificates)
    {
        this.numGiftCertificates = numGiftCertificates;
    }

    /**
     * The number of virtual products such as a service, warranty or donation
     * 
     * @return the numVirtualProducts
     */
    public int getNumVirtualProducts()
    {
        return numVirtualProducts;
    }

    /**
     * The number of virtual products such as a service, warranty or donation
     * 
     * @param numVirtualProducts
     *            the numVirtualProducts to set
     */
    public void setNumVirtualProducts(int numVirtualProducts)
    {
        this.numVirtualProducts = numVirtualProducts;
    }
}
