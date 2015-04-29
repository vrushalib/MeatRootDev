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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.appif.AddressIf;

/**
 * Gets called before the edit address page.
 */
public class EditAddrAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String city;

    private String company;

    private int countryId = -1;

    private String gender;

    private String firstName;

    private String lastName;

    private String postcode;

    private String state;

    private int zoneId = -1;

    private String streetAddress;

    private String streetAddress1;

    private String suburb;

    private String telephoneNumber;

    private String telephoneNumber1;

    private String emailAddrOptional;

    private boolean setAsPrimaryBool = false;

    // Called from One page checkout to change billing address
    private String opcbilling;
    
    // Called from One page checkout to change delivery address
    private String opcdelivery;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);
            
            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "EditAddr");
            if (custId < 0)
            {
                return KKLOGIN;
            }
            
            String addrId = request.getParameter("addrId");

            // Are we changing billing addr from checkout page?
            opcbilling = request.getParameter("opcbilling");
            
            // Are we changing delivery addr from checkout page?
            opcdelivery = request.getParameter("opcdelivery");

            if (addrId != null)
            {
                kkAppEng.getCustomerMgr().setSelectedAddrFromId(new Integer(addrId).intValue());
                if (kkAppEng.getCustomerMgr().getSelectedAddr() != null)
                {
                    kkAppEng.getCustomerMgr().setSelectedCountry(
                            kkAppEng.getCustomerMgr().getSelectedAddr().getCountryId());
                }
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            /*
             * If we had to login and have been sent back here, then the parameter will no longer be
             * there, but we we may have already set the selected addr before calling the login page
             */
            if (addrId == null && kkAppEng.getCustomerMgr().getSelectedAddr() == null)
            {
                throw new KKAppException("The addr Id parameter must be initialised");
            }

            /*
             * Set the form attributes from the selected address.
             */
            AddressIf addr = kkAppEng.getCustomerMgr().getSelectedAddr();
            if (addr != null)
            {
                city = noNull(addr.getCity());
                company = noNull(addr.getCompany());
                countryId = addr.getCountryId();
                firstName = noNull(addr.getFirstName());
                gender = noNull(addr.getGender());
                lastName = noNull(addr.getLastName());
                postcode = noNull(addr.getPostcode());
                state = noNull(addr.getState());
                zoneId = addr.getZoneId();
                streetAddress = noNull(addr.getStreetAddress());
                streetAddress1 = noNull(addr.getStreetAddress1());
                suburb = noNull(addr.getSuburb());
                telephoneNumber = noNull(addr.getTelephoneNumber());
                telephoneNumber1 = noNull(addr.getTelephoneNumber1());
                emailAddrOptional = noNull(addr.getEmailAddr());
                setAsPrimaryBool = addr.getIsPrimary();
            }

            if (log.isDebugEnabled())
            {
                log.debug("Addr Id from application = " + addrId);
            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.update.entry"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the gender
     */
    public String getGender()
    {
        return gender;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public void setGender(String gender)
    {
        this.gender = gender;
    }

    /**
     * @return the firstName
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * @return the setAsPrimaryBool
     */
    public boolean isSetAsPrimaryBool()
    {
        return setAsPrimaryBool;
    }

    /**
     * @param setAsPrimaryBool
     *            the setAsPrimaryBool to set
     */
    public void setSetAsPrimaryBool(boolean setAsPrimaryBool)
    {
        this.setAsPrimaryBool = setAsPrimaryBool;
    }

    /**
     * @return the company
     */
    public String getCompany()
    {
        return company;
    }

    /**
     * @param company
     *            the company to set
     */
    public void setCompany(String company)
    {
        this.company = company;
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
     * @return the opcbilling
     */
    public String getOpcbilling()
    {
        return opcbilling;
    }

    /**
     * @param opcbilling the opcbilling to set
     */
    public void setOpcbilling(String opcbilling)
    {
        this.opcbilling = opcbilling;
    }

    /**
     * @return the opcdelivery
     */
    public String getOpcdelivery()
    {
        return opcdelivery;
    }

    /**
     * @param opcdelivery the opcdelivery to set
     */
    public void setOpcdelivery(String opcdelivery)
    {
        this.opcdelivery = opcdelivery;
    }

    /**
     * @return the telephoneNumber
     */
    public String getTelephoneNumber()
    {
        return telephoneNumber;
    }

    /**
     * @param telephoneNumber the telephoneNumber to set
     */
    public void setTelephoneNumber(String telephoneNumber)
    {
        this.telephoneNumber = telephoneNumber;
    }

    /**
     * @return the telephoneNumber1
     */
    public String getTelephoneNumber1()
    {
        return telephoneNumber1;
    }

    /**
     * @param telephoneNumber1 the telephoneNumber1 to set
     */
    public void setTelephoneNumber1(String telephoneNumber1)
    {
        this.telephoneNumber1 = telephoneNumber1;
    }

    /**
     * @return the emailAddrOptional
     */
    public String getEmailAddrOptional()
    {
        return emailAddrOptional;
    }

    /**
     * @param emailAddrOptional the emailAddrOptional to set
     */
    public void setEmailAddrOptional(String emailAddrOptional)
    {
        this.emailAddrOptional = emailAddrOptional;
    }


}
