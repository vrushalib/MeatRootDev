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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.CustomerRegistration;
import com.konakart.app.CustomerTag;
import com.konakart.app.EmailOptions;
import com.konakart.appif.CountryIf;
import com.konakart.appif.CustomerRegistrationIf;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.bl.ConfigConstants;

/**
 * Gets called after submitting the customer registration page.
 */
public class CustomerRegistrationSubmitAction extends BaseAction
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

    private int countryChange;

    private boolean allowNoRegister = false;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // Create a new object here since it may be used if an exception occurs and so has to be
        // visible.
        CustomerRegistrationIf cr = new CustomerRegistration();
        KKAppEng kkAppEng = null;
        String exceptionMsg = "";

        try
        {
            int custId;
            Date birthDate = null;
            CustomerTag ct = null;

            kkAppEng = this.getKKAppEng(request, response);

            // Msg used in the Exception block
            exceptionMsg = kkAppEng.getMsg("register.customer.body.user.exists");

            custId = this.loggedIn(request, response, kkAppEng, null);
            if (custId > 0 && allowNoRegister)
            {
                // The customer has already registered so don't register again
                kkAppEng.setForwardAfterLogin(null);
                return "Checkout";
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */true);
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
                if (allowNoRegister)
                {
                    return "ChangedCountryNoForce";
                }
                return "ChangedCountryForce";
            }

            // Copy the inputs from the form to the customer registration object
            cr.setCity(escapeFormInput(getCity()));
            cr.setCompany(escapeFormInput(getCompany()));
            cr.setTaxIdentifier(escapeFormInput(getTaxId()));
            cr.setCountryId(getCountryId());
            cr.setEmailAddr(escapeFormInput(getEmailAddr()));
            cr.setFaxNumber(escapeFormInput(getFaxNumber()));
            cr.setFirstName(escapeFormInput(getFirstName()));
            cr.setGender(escapeFormInput(getGender()));
            cr.setLastName(escapeFormInput(getLastName()));
            cr.setNewsletter(escapeFormInput(getNewsletter()));
            cr.setPassword(getPassword());
            cr.setPostcode(escapeFormInput(getPostcode()));
            cr.setProductNotifications(getProductNotifications());
            cr.setState(escapeFormInput(getState()));
            cr.setStreetAddress(escapeFormInput(getStreetAddress()));
            cr.setStreetAddress1(escapeFormInput(getStreetAddress1()));
            cr.setSuburb(escapeFormInput(getSuburb()));
            cr.setTelephoneNumber(escapeFormInput(getTelephoneNumber()));
            cr.setTelephoneNumber1(escapeFormInput(getTelephoneNumber1()));
            cr.setAddressCustom1(escapeFormInput(getAddressCustom1()));
            cr.setAddressCustom2(escapeFormInput(getAddressCustom2()));
            cr.setAddressCustom3(escapeFormInput(getAddressCustom3()));
            cr.setAddressCustom4(escapeFormInput(getAddressCustom4()));
            cr.setAddressCustom5(escapeFormInput(getAddressCustom5()));
            cr.setCustomerCustom1(escapeFormInput(getCustomerCustom1()));
            cr.setCustomerCustom2(escapeFormInput(getCustomerCustom2()));
            cr.setCustomerCustom3(escapeFormInput(getCustomerCustom3()));
            cr.setCustomerCustom4(escapeFormInput(getCustomerCustom4()));
            cr.setCustomerCustom5(escapeFormInput(getCustomerCustom5()));

            // Set the date
            if (getBirthDateString() != null && !getBirthDateString().equals(""))
            {
                SimpleDateFormat sdf = new SimpleDateFormat(kkAppEng.getMsg("date.format"));
                sdf.setLenient(false);
                try
                {
                    birthDate = sdf.parse(getBirthDateString());
                } catch (Exception e)
                {
                    addActionError(kkAppEng.getMsg("register.customer.body.dob.error"));
                    return "ApplicationError";
                }
                birthDate = sdf.parse(getBirthDateString());
                if (birthDate != null)
                {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(birthDate);
                    cr.setBirthDate(gc);
                }
            }

            // Set the newsletter
            if (isNewsletterBool())
            {
                cr.setNewsletter("1");
            } else
            {
                cr.setNewsletter("0");
            }

            // un-comment if the customer shouldn't be enabled immediately
            // cr.setEnabled(false);

            int customerId;
            if (allowNoRegister)
            {
                // Call the engine registration method
                String randomPassword = String.valueOf(System.currentTimeMillis());
                cr.setPassword(randomPassword);
                customerId = kkAppEng.getCustomerMgr().forceRegisterCustomer(cr);
                login(kkAppEng, request, cr.getEmailAddr(), randomPassword);
                kkAppEng.setForwardAfterLogin(null);
                return "Checkout";
            }

            // Call the engine registration method
            customerId = kkAppEng.getCustomerMgr().registerCustomer(cr);

            /*
             * If you only want to enable the customer after confirmation, you must save an SSOToken
             * with the customer id. The secret key must be passed in a mail (within a link) to the
             * customer so that when he clicks on the link he calls EnableCustomerSubmitAction. e.g.
             * http
             * ://localhost:8780/konakart/EnableCustomer.action?key=70168e16-eb49-45c4-b47b-e2a2f8c0e6f5
             */
            // SSOTokenIf token = new SSOToken();
            // token.setCustomerId(customerId);
            // String secretKey = kkAppEng.getEng().saveSSOToken(token);
            // System.out.println("key = " + secretKey);

            // Send a welcome email
            EmailOptionsIf options = new EmailOptions();
            options.setCountryCode(kkAppEng.getLocale().substring(0, 2));
            options.setTemplateName(com.konakart.bl.EmailMgr.WELCOME_TEMPLATE);
            kkAppEng.getEng().sendWelcomeEmail1(customerId, options);

            // Set this to false if you don't want to login automatically after registration. Note
            // that customer tags won't be set and the reward points won't be allocated.
            // Now log in the customer
            login(kkAppEng, request, getEmailAddr(), getPassword());
            kkAppEng.getNav().set(kkAppEng.getMsg("header.my.account"), request);

            /*
             * Set customer tags
             */

            // Set the BIRTH_DATE customer tag for this customer
            if (birthDate != null)
            {
                ct = new CustomerTag();
                ct.setValueAsDate(birthDate);
                ct.setName(TAG_BIRTH_DATE);
                kkAppEng.getCustomerTagMgr().insertCustomerTag(ct);
            }

            // Set the COUNTRY_CODE customer tag for this customer
            CountryIf country = kkAppEng.getEng().getCountry(getCountryId());
            if (country != null && country.getIsoCode3() != null)
            {
                kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_COUNTRY_CODE,
                        country.getIsoCode3());
            }

            // Set the IS_MALE customer tag for this customer
            ct = new CustomerTag();
            ct.setName(TAG_IS_MALE);
            ct.setValueAsBoolean(false);
            if (getGender() != null && getGender().equalsIgnoreCase("m"))
            {
                ct.setValueAsBoolean(true);
            }
            kkAppEng.getCustomerTagMgr().insertCustomerTag(ct);

            // Set reward points if applicable
            if (kkAppEng.getRewardPointMgr().isEnabled())
            {
                String pointsStr = kkAppEng.getConfig(ConfigConstants.REGISTRATION_REWARD_POINTS);
                if (pointsStr != null)
                {
                    int points = 0;
                    try
                    {
                        points = Integer.parseInt(pointsStr);
                        kkAppEng.getRewardPointMgr().addPoints(points, "REG",
                                kkAppEng.getMsg("reward.points.registration"));
                    } catch (Exception e)
                    {
                        log.warn("The REGISTRATION_REWARD_POINTS configuration variable has been set with a non numeric value: "
                                + pointsStr);
                    }
                }
            }

            if (kkAppEng.getForwardAfterLogin() != null
                    && kkAppEng.getForwardAfterLogin().equalsIgnoreCase("Checkout"))
            {
                kkAppEng.setForwardAfterLogin(null);
                return "Checkout";
            }

            return SUCCESS;

        } catch (Exception e)
        {
            /*
             * An exception could occur if the user already exists in which case we let the customer
             * try again with a different user name.
             */
            return getForward(request, e, "com.konakart.app.KKUserExistsException", exceptionMsg,
                    "ApplicationError");
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
     * @param taxId
     *            the taxId to set
     */
    public void setTaxId(String taxId)
    {
        this.taxId = taxId;
    }
}
