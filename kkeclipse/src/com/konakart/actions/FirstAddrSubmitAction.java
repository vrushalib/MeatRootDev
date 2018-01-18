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
import com.konakart.app.Address;
import com.konakart.appif.AddressIf;
import com.konakart.appif.CustomerIf;

/**
 * Gets called after submitting the edit address page.
 */
public class FirstAddrSubmitAction extends BaseAction
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

    private int countryChange;
    
    private String goToCheckout = "";

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // Create a new object here since it may be used if an exception occurs and so has to be
        // visible.
        AddressIf addr = new Address();
        KKAppEng kkAppEng = null;

        try
        {
            int custId;

            kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "FirstAddr");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Determine whether there has been a country change
            if (getCountryChange() == 1)
            {
                kkAppEng.getCustomerMgr().setSelectedCountry(getCountryId());
                setState(null);
                return "ChangedCountry";
            }

            CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            AddressIf custAddr = null;
            if (kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses() != null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses().length > 0)
            {
                custAddr = kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses()[0];
            }

            // Copy the inputs from the form to an address object
            if (kkAppEng.getCustomerMgr().isNoGender())
            {
                addr.setGender(escapeFormInput(getGender()));
            } else
            {
                addr.setGender(cust.getGender());
            }

            if (kkAppEng.getCustomerMgr().isNoName())
            {
                addr.setFirstName(escapeFormInput(getFirstName()));
                addr.setLastName(escapeFormInput(getLastName()));
            } else
            {
                addr.setFirstName(cust.getFirstName());
                addr.setLastName(cust.getLastName());
            }

            if (custAddr != null)
            {
                addr.setId(custAddr.getId());
            }
            addr.setCity(escapeFormInput(getCity()));
            addr.setCompany(escapeFormInput(getCompany()));
            addr.setCountryId(getCountryId());
            addr.setCustomerId(cust.getId());
            addr.setPostcode(escapeFormInput(getPostcode()));
            addr.setStreetAddress(escapeFormInput(getStreetAddress()));
            addr.setStreetAddress1(escapeFormInput(getStreetAddress1()));
            addr.setSuburb(escapeFormInput(getSuburb()));
            addr.setTelephoneNumber(escapeFormInput(getTelephoneNumber()));
            addr.setTelephoneNumber1(escapeFormInput(getTelephoneNumber1()));
            addr.setEmailAddr(escapeFormInput(getEmailAddrOptional()));
            addr.setIsPrimary(true);
            if (kkAppEng.getCustomerMgr().getSelectedZones() == null
                    || kkAppEng.getCustomerMgr().getSelectedZones().length == 0)
            {
                addr.setState(escapeFormInput(getState()));
            } else
            {
                // Don't escape since we try to match the state
                addr.setState(getState());
            }

            if (custAddr != null)
            {
                kkAppEng.getCustomerMgr().editCustomerAddress(addr);
            } else
            {
                kkAppEng.getCustomerMgr().addAddressToCustomer(addr);
            }

            /* If the customer never had a name or a gender then we have to add them to the customer */
            if (kkAppEng.getCustomerMgr().isNoGender() || kkAppEng.getCustomerMgr().isNoName()
                    || kkAppEng.getCustomerMgr().isNoTelephone())
            {
                if (kkAppEng.getCustomerMgr().isNoGender())
                {
                    cust.setGender(escapeFormInput(getGender()));
                }
                if (kkAppEng.getCustomerMgr().isNoName())
                {
                    cust.setFirstName(escapeFormInput(getFirstName()));
                    cust.setLastName(escapeFormInput(getLastName()));
                }
                if (kkAppEng.getCustomerMgr().isNoTelephone())
                {
                    cust.setTelephoneNumber(escapeFormInput(getTelephoneNumber()));
                }
                kkAppEng.getCustomerMgr().editCustomer(cust);
            }

            // Add a message to say all OK
            addActionMessage(kkAppEng.getMsg("first.addr.body.addedok"));
            
            if (getGoToCheckout().equalsIgnoreCase("true"))
            {
                return "OnePageCheckout";
            }

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
     * @return the countryChange
     */
    public int getCountryChange()
    {
        return countryChange;
    }

    /**
     * @param countryChange
     *            the countryChange to set
     */
    public void setCountryChange(int countryChange)
    {
        this.countryChange = countryChange;
    }

    /**
     * @return the telephoneNumber
     */
    public String getTelephoneNumber()
    {
        return telephoneNumber;
    }

    /**
     * @param telephoneNumber
     *            the telephoneNumber to set
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
     * @param telephoneNumber1
     *            the telephoneNumber1 to set
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
     * @param emailAddrOptional
     *            the emailAddrOptional to set
     */
    public void setEmailAddrOptional(String emailAddrOptional)
    {
        this.emailAddrOptional = emailAddrOptional;
    }

    /**
     * @return the goToCheckout
     */
    public String getGoToCheckout()
    {
        return goToCheckout;
    }


    /**
     * @param goToCheckout the goToCheckout to set
     */
    public void setGoToCheckout(String goToCheckout)
    {
        this.goToCheckout = goToCheckout;
    }

}
