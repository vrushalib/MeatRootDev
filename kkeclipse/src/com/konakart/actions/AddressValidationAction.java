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

package com.konakart.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.json.BasketJson;
import com.konakart.app.AddrValidationResult;
import com.konakart.app.Address;
import com.konakart.appif.CountryIf;
import com.konakart.bl.modules.others.AddrValidationInterface;

/**
 * Validates an address. Currently only USPS address validation is implemented. This is a module
 * that can be activated through the Admin App.
 */
public class AddressValidationAction extends AddToCartOrWishListBaseAction
{
    private static final long serialVersionUID = 1L;

    private String city;

    private int countryId = -1;

    private String country;

    private String postcode;

    private String state;

    private int zoneId = -1;

    private String streetAddress;

    private String streetAddress1;

    private String suburb;

    private AddrValidationResult ret;

    private String popupMsg;

    private boolean performedCheck = false;
    
    private String xsrf_token;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            CountryIf country = kkAppEng.getEng().getCountry(getCountryId());
            if (country.getIsoCode3().equals("USA"))
            {
                AddrValidationInterface addrVal = getUSPSAddrVal(kkAppEng);
                if (addrVal != null)
                {
                    Address addr = new Address();

                    addr.setCity(getCity());
                    addr.setCountryId(getCountryId());
                    addr.setPostcode(getPostcode());
                    addr.setStreetAddress(getStreetAddress());
                    addr.setStreetAddress1(getStreetAddress1());
                    addr.setSuburb(getSuburb());
                    addr.setState(getState());
                    addr.setAddressFormatId(country.getAddressFormatId());

                    ret = (AddrValidationResult) addrVal.validateAddress(addr);

                    performedCheck = true;

                    if (ret.isError())
                    {
                        popupMsg = kkAppEng.getMsg("address.validate.error");
                    } else
                    {
                        popupMsg = kkAppEng.getMsg("address.validate.confirm");
                    }
                }
            }

            if (!performedCheck)
            {
                city = null;
                country = null;
                postcode = null;
                streetAddress = null;
                streetAddress1 = null;
                suburb = null;
            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * Instantiates the USPS address validation object and caches it so the second time around it is
     * returned from the cache.
     * 
     * @param kkAppEng
     * @return Returns a cached address validation object if it is enabled
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    protected AddrValidationInterface getUSPSAddrVal(KKAppEng kkAppEng)
            throws ClassNotFoundException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException
    {
        boolean status = kkAppEng.getConfigAsBoolean("MODULE_OTHER_USPS_ADDR_VAL_STATUS", false, /* tryEngIfNotInCache */
                false);
        if (!status)
        {
            return null;
        }

        try
        {
            AddrValidationInterface addrVal = (AddrValidationInterface) kkAppEng.getObjMap().get(
                    "USPSAddrVal");
            if (addrVal == null)
            {
                String className = "com.konakart.bl.modules.others.uspsaddrval.USPSAddrVal";
                Class<?> addrValClass = Class.forName(className);
                Constructor<?>[] constructors = addrValClass.getConstructors();
                Constructor<?> engConstructor = null;
                if (constructors != null && constructors.length > 0)
                {
                    for (int i = 0; i < constructors.length; i++)
                    {
                        Constructor<?> constructor = constructors[i];
                        Class<?>[] parmTypes = constructor.getParameterTypes();
                        if (parmTypes != null && parmTypes.length == 1)
                        {
                            String parmName = parmTypes[0].getName();
                            if (parmName != null && parmName.equals("com.konakart.appif.KKEngIf"))
                            {
                                engConstructor = constructor;
                            }
                        }
                    }
                }

                if (engConstructor != null)
                {
                    addrVal = (AddrValidationInterface) engConstructor.newInstance(kkAppEng
                            .getEng());
                } else
                {
                    log.warn("Could not find constructor to instantiate USPSAddrValidation");
                }

                kkAppEng.getObjMap().put("USPSAddrVal", addrVal);
            }

            return addrVal;
        } catch (Exception e)
        {
            log.error("Unable to instantiate the USPS Address Verification Module", e);
        }
        return null;
    }

    /**
     * @return the prodId
     */
    public int getProdId()
    {
        return prodId;
    }

    /**
     * @param prodId
     *            the prodId to set
     */
    public void setProdId(int prodId)
    {
        this.prodId = prodId;
    }

    /**
     * @return the numberOfItems
     */
    public int getNumberOfItems()
    {
        return numberOfItems;
    }

    /**
     * @param numberOfItems
     *            the numberOfItems to set
     */
    public void setNumberOfItems(int numberOfItems)
    {
        this.numberOfItems = numberOfItems;
    }

    /**
     * @return the basketTotal
     */
    public String getBasketTotal()
    {
        return basketTotal;
    }

    /**
     * @param basketTotal
     *            the basketTotal to set
     */
    public void setBasketTotal(String basketTotal)
    {
        this.basketTotal = basketTotal;
    }

    /**
     * @return the redirectURL
     */
    public String getRedirectURL()
    {
        return redirectURL;
    }

    /**
     * @param redirectURL
     *            the redirectURL to set
     */
    public void setRedirectURL(String redirectURL)
    {
        this.redirectURL = redirectURL;
    }

    /**
     * @return the checkoutMsg
     */
    public String getCheckoutMsg()
    {
        return checkoutMsg;
    }

    /**
     * @param checkoutMsg
     *            the checkoutMsg to set
     */
    public void setCheckoutMsg(String checkoutMsg)
    {
        this.checkoutMsg = checkoutMsg;
    }

    /**
     * @return the imgBase
     */
    public String getImgBase()
    {
        return imgBase;
    }

    /**
     * @param imgBase
     *            the imgBase to set
     */
    public void setImgBase(String imgBase)
    {
        this.imgBase = imgBase;
    }

    /**
     * @return the subtotalMsg
     */
    public String getSubtotalMsg()
    {
        return subtotalMsg;
    }

    /**
     * @param subtotalMsg
     *            the subtotalMsg to set
     */
    public void setSubtotalMsg(String subtotalMsg)
    {
        this.subtotalMsg = subtotalMsg;
    }

    /**
     * @return the shoppingCartMsg
     */
    public String getShoppingCartMsg()
    {
        return shoppingCartMsg;
    }

    /**
     * @param shoppingCartMsg
     *            the shoppingCartMsg to set
     */
    public void setShoppingCartMsg(String shoppingCartMsg)
    {
        this.shoppingCartMsg = shoppingCartMsg;
    }

    /**
     * @return the quantityMsg
     */
    public String getQuantityMsg()
    {
        return quantityMsg;
    }

    /**
     * @param quantityMsg
     *            the quantityMsg to set
     */
    public void setQuantityMsg(String quantityMsg)
    {
        this.quantityMsg = quantityMsg;
    }

    /**
     * @return the city
     */
    public String getCity()
    {
        return city;
    }

    /**
     * @param city
     *            the city to set
     */
    public void setCity(String city)
    {
        this.city = city;
    }

    /**
     * @return the countryId
     */
    public int getCountryId()
    {
        return countryId;
    }

    /**
     * @param countryId
     *            the countryId to set
     */
    public void setCountryId(int countryId)
    {
        this.countryId = countryId;
    }

    /**
     * @return the postcode
     */
    public String getPostcode()
    {
        return postcode;
    }

    /**
     * @param postcode
     *            the postcode to set
     */
    public void setPostcode(String postcode)
    {
        this.postcode = postcode;
    }

    /**
     * @return the state
     */
    public String getState()
    {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state)
    {
        this.state = state;
    }

    /**
     * @return the zoneId
     */
    public int getZoneId()
    {
        return zoneId;
    }

    /**
     * @param zoneId
     *            the zoneId to set
     */
    public void setZoneId(int zoneId)
    {
        this.zoneId = zoneId;
    }

    /**
     * @return the streetAddress
     */
    public String getStreetAddress()
    {
        return streetAddress;
    }

    /**
     * @param streetAddress
     *            the streetAddress to set
     */
    public void setStreetAddress(String streetAddress)
    {
        this.streetAddress = streetAddress;
    }

    /**
     * @return the streetAddress1
     */
    public String getStreetAddress1()
    {
        return streetAddress1;
    }

    /**
     * @param streetAddress1
     *            the streetAddress1 to set
     */
    public void setStreetAddress1(String streetAddress1)
    {
        this.streetAddress1 = streetAddress1;
    }

    /**
     * @return the suburb
     */
    public String getSuburb()
    {
        return suburb;
    }

    /**
     * @param suburb
     *            the suburb to set
     */
    public void setSuburb(String suburb)
    {
        this.suburb = suburb;
    }

    /**
     * @return the country
     */
    public String getCountry()
    {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * @return the performedCheck
     */
    public boolean isPerformedCheck()
    {
        return performedCheck;
    }

    /**
     * @param performedCheck
     *            the performedCheck to set
     */
    public void setPerformedCheck(boolean performedCheck)
    {
        this.performedCheck = performedCheck;
    }

    /**
     * @return the ret
     */
    public AddrValidationResult getRet()
    {
        return ret;
    }

    /**
     * @param ret
     *            the ret to set
     */
    public void setRet(AddrValidationResult ret)
    {
        this.ret = ret;
    }

    /**
     * @return the popupMsg
     */
    public String getPopupMsg()
    {
        return popupMsg;
    }

    /**
     * @param popupMsg
     *            the popupMsg to set
     */
    public void setPopupMsg(String popupMsg)
    {
        this.popupMsg = popupMsg;
    }

    /**
     * @return the items
     */
    public BasketJson[] getItems()
    {
        return items;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(BasketJson[] items)
    {
        this.items = items;
    }

    /**
     * @return the xsrf_token
     */
    public String getXsrf_token()
    {
        return xsrf_token;
    }

    /**
     * @param xsrf_token the xsrf_token to set
     */
    public void setXsrf_token(String xsrf_token)
    {
        this.xsrf_token = xsrf_token;
    }

}
