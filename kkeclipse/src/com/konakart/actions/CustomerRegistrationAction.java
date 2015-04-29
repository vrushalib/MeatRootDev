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
import com.konakart.bl.ConfigConstants;

/**
 * Gets called before displaying the customer registration page.
 */
public class CustomerRegistrationAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String gender;

    private String firstName;

    private String lastName;

    private String birthDateString;

    private String emailAddr;

    private String telephoneNumber;

    private String telephoneNumber1;

    // Optional - used for address
    private String addrTelephone;

    // Optional - used for address
    private String addrTelephone1;

    // Optional - used for address
    private String addrEmail;

    private String faxNumber;

    private String password;

    private String passwordConfirmation;

    private String newsletter;

    private boolean newsletterBool;

    private boolean setAsPrimaryBool = false;

    private int productNotifications;

    private String company;
    
    private String taxId;

    private String streetAddress;

    private String streetAddress1;

    private String suburb;

    private String postcode;

    private String city;

    private String state;

    private int countryId = 0;

    private String customerCustom1;

    private String customerCustom2;

    private String customerCustom3;

    private String customerCustom4;

    private String customerCustom5;

    private String addressCustom1;

    private String addressCustom2;

    private String addressCustom3;

    private String addressCustom4;

    private String addressCustom5;

    private boolean allowNoRegister = false;

    private boolean forceReg = false;
    
    /**
     * Entry point when country has been changed but in force registration mode
     * @return Returns the output of the execute method
     */
    public String forceRegistration(){
        forceReg = true;
        return execute();
    }

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, null);

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */true);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Can we checkout without registration ?
            if (!forceReg)
            {
                allowNoRegister = kkAppEng.getConfigAsBoolean(
                        ConfigConstants.ALLOW_CHECKOUT_WITHOUT_REGISTRATION, false);
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.customer.registration"), request);

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
     * @return the birthDateString
     */
    public String getBirthDateString()
    {
        return birthDateString;
    }

    /**
     * @param birthDateString
     *            the birthDateString to set
     */
    public void setBirthDateString(String birthDateString)
    {
        this.birthDateString = birthDateString;
    }

    /**
     * @return the emailAddr
     */
    public String getEmailAddr()
    {
        return emailAddr;
    }

    /**
     * @param emailAddr
     *            the emailAddr to set
     */
    public void setEmailAddr(String emailAddr)
    {
        this.emailAddr = emailAddr;
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
     * @return the addrTelephone
     */
    public String getAddrTelephone()
    {
        return addrTelephone;
    }

    /**
     * @param addrTelephone
     *            the addrTelephone to set
     */
    public void setAddrTelephone(String addrTelephone)
    {
        this.addrTelephone = addrTelephone;
    }

    /**
     * @return the addrTelephone1
     */
    public String getAddrTelephone1()
    {
        return addrTelephone1;
    }

    /**
     * @param addrTelephone1
     *            the addrTelephone1 to set
     */
    public void setAddrTelephone1(String addrTelephone1)
    {
        this.addrTelephone1 = addrTelephone1;
    }

    /**
     * @return the addrEmail
     */
    public String getAddrEmail()
    {
        return addrEmail;
    }

    /**
     * @param addrEmail
     *            the addrEmail to set
     */
    public void setAddrEmail(String addrEmail)
    {
        this.addrEmail = addrEmail;
    }

    /**
     * @return the faxNumber
     */
    public String getFaxNumber()
    {
        return faxNumber;
    }

    /**
     * @param faxNumber
     *            the faxNumber to set
     */
    public void setFaxNumber(String faxNumber)
    {
        this.faxNumber = faxNumber;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the passwordConfirmation
     */
    public String getPasswordConfirmation()
    {
        return passwordConfirmation;
    }

    /**
     * @param passwordConfirmation
     *            the passwordConfirmation to set
     */
    public void setPasswordConfirmation(String passwordConfirmation)
    {
        this.passwordConfirmation = passwordConfirmation;
    }

    /**
     * @return the newsletter
     */
    public String getNewsletter()
    {
        return newsletter;
    }

    /**
     * @param newsletter
     *            the newsletter to set
     */
    public void setNewsletter(String newsletter)
    {
        this.newsletter = newsletter;
    }

    /**
     * @return the newsletterBool
     */
    public boolean isNewsletterBool()
    {
        return newsletterBool;
    }

    /**
     * @param newsletterBool
     *            the newsletterBool to set
     */
    public void setNewsletterBool(boolean newsletterBool)
    {
        this.newsletterBool = newsletterBool;
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
     * @return the productNotifications
     */
    public int getProductNotifications()
    {
        return productNotifications;
    }

    /**
     * @param productNotifications
     *            the productNotifications to set
     */
    public void setProductNotifications(int productNotifications)
    {
        this.productNotifications = productNotifications;
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
     * @return the customerCustom1
     */
    public String getCustomerCustom1()
    {
        return customerCustom1;
    }

    /**
     * @param customerCustom1
     *            the customerCustom1 to set
     */
    public void setCustomerCustom1(String customerCustom1)
    {
        this.customerCustom1 = customerCustom1;
    }

    /**
     * @return the customerCustom2
     */
    public String getCustomerCustom2()
    {
        return customerCustom2;
    }

    /**
     * @param customerCustom2
     *            the customerCustom2 to set
     */
    public void setCustomerCustom2(String customerCustom2)
    {
        this.customerCustom2 = customerCustom2;
    }

    /**
     * @return the customerCustom3
     */
    public String getCustomerCustom3()
    {
        return customerCustom3;
    }

    /**
     * @param customerCustom3
     *            the customerCustom3 to set
     */
    public void setCustomerCustom3(String customerCustom3)
    {
        this.customerCustom3 = customerCustom3;
    }

    /**
     * @return the customerCustom4
     */
    public String getCustomerCustom4()
    {
        return customerCustom4;
    }

    /**
     * @param customerCustom4
     *            the customerCustom4 to set
     */
    public void setCustomerCustom4(String customerCustom4)
    {
        this.customerCustom4 = customerCustom4;
    }

    /**
     * @return the customerCustom5
     */
    public String getCustomerCustom5()
    {
        return customerCustom5;
    }

    /**
     * @param customerCustom5
     *            the customerCustom5 to set
     */
    public void setCustomerCustom5(String customerCustom5)
    {
        this.customerCustom5 = customerCustom5;
    }

    /**
     * @return the addressCustom1
     */
    public String getAddressCustom1()
    {
        return addressCustom1;
    }

    /**
     * @param addressCustom1
     *            the addressCustom1 to set
     */
    public void setAddressCustom1(String addressCustom1)
    {
        this.addressCustom1 = addressCustom1;
    }

    /**
     * @return the addressCustom2
     */
    public String getAddressCustom2()
    {
        return addressCustom2;
    }

    /**
     * @param addressCustom2
     *            the addressCustom2 to set
     */
    public void setAddressCustom2(String addressCustom2)
    {
        this.addressCustom2 = addressCustom2;
    }

    /**
     * @return the addressCustom3
     */
    public String getAddressCustom3()
    {
        return addressCustom3;
    }

    /**
     * @param addressCustom3
     *            the addressCustom3 to set
     */
    public void setAddressCustom3(String addressCustom3)
    {
        this.addressCustom3 = addressCustom3;
    }

    /**
     * @return the addressCustom4
     */
    public String getAddressCustom4()
    {
        return addressCustom4;
    }

    /**
     * @param addressCustom4
     *            the addressCustom4 to set
     */
    public void setAddressCustom4(String addressCustom4)
    {
        this.addressCustom4 = addressCustom4;
    }

    /**
     * @return the addressCustom5
     */
    public String getAddressCustom5()
    {
        return addressCustom5;
    }

    /**
     * @param addressCustom5
     *            the addressCustom5 to set
     */
    public void setAddressCustom5(String addressCustom5)
    {
        this.addressCustom5 = addressCustom5;
    }

    /**
     * @return the forceReg
     */
    public boolean isForceReg()
    {
        return forceReg;
    }

    /**
     * @param forceReg
     *            the forceReg to set
     */
    public void setForceReg(boolean forceReg)
    {
        this.forceReg = forceReg;
    }

    /**
     * @return the allowNoRegister
     */
    public boolean isAllowNoRegister()
    {
        return allowNoRegister;
    }

    /**
     * @param allowNoRegister
     *            the allowNoRegister to set
     */
    public void setAllowNoRegister(boolean allowNoRegister)
    {
        this.allowNoRegister = allowNoRegister;
    }

    /**
     * @return the taxId
     */
    public String getTaxId()
    {
        return taxId;
    }

    /**
     * @param taxId the taxId to set
     */
    public void setTaxId(String taxId)
    {
        this.taxId = taxId;
    }

}
