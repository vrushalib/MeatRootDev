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
import com.konakart.appif.AddressIf;
import com.konakart.appif.WishListIf;

/**
 * Gets called before the edit gift registry page.
 */
public class EditGiftRegistryAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Name of the gift registry
     */
    private String registryName;

    /**
     * Description for the gift registry
     */
    private String description;

    /**
     * First name of gift registry customer
     */
    private String firstName;

    /**
     * Last name of gift registry customer
     */
    private String lastName;

    /**
     * First name of 2nd gift registry customer. i.e. Wedding list will have two names
     */
    private String firstName1;

    /**
     * Last name of 2nd gift registry customer. i.e. Wedding list will have two names
     */
    private String lastName1;

    /**
     * City of gift registry customer
     */
    private String customerCity;

    /**
     * State of gift registry customer
     */
    private String customerState;

    /**
     * Birth date of gift registry customer
     */
    private String customerBirthDate;

    /**
     * If set to true, the gift registry is public. Otherwise it is private.
     */
    private String publicWishList;

    /**
     * URL that points to a page containing event details
     */
    private String linkURL;

    /**
     * Type of list. i.e. Wish List, Wedding List etc.
     */
    private int listType;

    /**
     * Shipping address to be used for shipping list items
     */
    private int addressId;

    /**
     * Date of the event
     */
    private String eventDateString;
    
    /**
     * Id of the wish list
     */
    private int wishListId=-1;

    /**
     * Custom fields
     */
    private String custom1;

    private String custom2;

    private String custom3;

    private String custom4;

    private String custom5;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, null);

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

            WishListIf currentWishList = null;
            if (wishListId == -1)
            {
                /*
                 * If the wishListId parameter doesn't exist, we can use the current wish list if it
                 * exists. This could be used in the case of the back button
                 */
                if (kkAppEng.getWishListMgr().getCurrentWishList() != null)
                {
                    currentWishList = kkAppEng.getWishListMgr().getCurrentWishList();
                } else
                {
                    return "MyAccount";
                }
            } else
            {
                // Set the current wish list
                if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                        && kkAppEng.getCustomerMgr().getCurrentCustomer().getWishLists() != null)
                {
                    for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                            .getWishLists().length; i++)
                    {
                        WishListIf wl = kkAppEng.getCustomerMgr().getCurrentCustomer()
                                .getWishLists()[i];
                        if (wl != null && wl.getId() == wishListId)
                        {
                            currentWishList = wl;
                            kkAppEng.getWishListMgr().setCurrentWishList(wl);
                            break;
                        }
                    }
                }

                // If we can't find the wish list then we can't edit it
                if (currentWishList == null)
                {
                    return "MyAccount";
                }
            }

            // Set the attributes picked up by the JSP
            setFirstName1(noNull(currentWishList.getCustomer1FirstName()));
            setLastName1(noNull(currentWishList.getCustomer1LastName()));
            setFirstName(noNull(currentWishList.getCustomerFirstName()));
            setLastName(noNull(currentWishList.getCustomerLastName()));
            setLinkURL(noNull(currentWishList.getLinkUrl()));
            setRegistryName(noNull(currentWishList.getName()));
            setPublicWishList((currentWishList.isPublicWishList()) ? "true" : "false");
            setEventDateString(noNull(kkAppEng.getDateAsString(currentWishList.getEventDate())));

            // Ensure that the current customer has his addresses populated
            kkAppEng.getCustomerMgr().populateCurrentCustomerAddresses(/* force */false);

            // Populate the address attribute of the current wish list
            if (kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses() != null)
            {
                for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses().length; i++)
                {
                    AddressIf addr = kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses()[i];
                    if (addr.getId() == currentWishList.getAddressId())
                    {
                        currentWishList.setAddress(addr);
                    }
                }
            }

            /*
             * If an address doesn't exist for the address id, set the id to a valid address and
             * save the wish list
             */
            if (currentWishList.getAddress() == null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses() != null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses().length > 0)
            {
                currentWishList.setAddress(kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getAddresses()[0]);
                currentWishList.setAddressId(kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getAddresses()[0].getId());

                // Edit the wish list
                kkAppEng.getWishListMgr().editWishList(currentWishList);

                // Refresh the customer's wish list
                kkAppEng.getWishListMgr().fetchCustomersWishLists();

            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.customer.editweddinglist"), request);
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the registryName
     */
    public String getRegistryName()
    {
        return registryName;
    }

    /**
     * @param registryName
     *            the registryName to set
     */
    public void setRegistryName(String registryName)
    {
        this.registryName = registryName;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
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
     * @return the firstName1
     */
    public String getFirstName1()
    {
        return firstName1;
    }

    /**
     * @param firstName1
     *            the firstName1 to set
     */
    public void setFirstName1(String firstName1)
    {
        this.firstName1 = firstName1;
    }

    /**
     * @return the lastName1
     */
    public String getLastName1()
    {
        return lastName1;
    }

    /**
     * @param lastName1
     *            the lastName1 to set
     */
    public void setLastName1(String lastName1)
    {
        this.lastName1 = lastName1;
    }

    /**
     * @return the customerCity
     */
    public String getCustomerCity()
    {
        return customerCity;
    }

    /**
     * @param customerCity
     *            the customerCity to set
     */
    public void setCustomerCity(String customerCity)
    {
        this.customerCity = customerCity;
    }

    /**
     * @return the customerState
     */
    public String getCustomerState()
    {
        return customerState;
    }

    /**
     * @param customerState
     *            the customerState to set
     */
    public void setCustomerState(String customerState)
    {
        this.customerState = customerState;
    }

    /**
     * @return the customerBirthDate
     */
    public String getCustomerBirthDate()
    {
        return customerBirthDate;
    }

    /**
     * @param customerBirthDate
     *            the customerBirthDate to set
     */
    public void setCustomerBirthDate(String customerBirthDate)
    {
        this.customerBirthDate = customerBirthDate;
    }

    /**
     * @return the publicWishList
     */
    public String getPublicWishList()
    {
        return publicWishList;
    }

    /**
     * @param publicWishList
     *            the publicWishList to set
     */
    public void setPublicWishList(String publicWishList)
    {
        this.publicWishList = publicWishList;
    }

    /**
     * @return the linkURL
     */
    public String getLinkURL()
    {
        return linkURL;
    }

    /**
     * @param linkURL
     *            the linkURL to set
     */
    public void setLinkURL(String linkURL)
    {
        this.linkURL = linkURL;
    }

    /**
     * @return the listType
     */
    public int getListType()
    {
        return listType;
    }

    /**
     * @param listType
     *            the listType to set
     */
    public void setListType(int listType)
    {
        this.listType = listType;
    }

    /**
     * @return the addressId
     */
    public int getAddressId()
    {
        return addressId;
    }

    /**
     * @param addressId
     *            the addressId to set
     */
    public void setAddressId(int addressId)
    {
        this.addressId = addressId;
    }

    /**
     * @return the eventDateString
     */
    public String getEventDateString()
    {
        return eventDateString;
    }

    /**
     * @param eventDateString
     *            the eventDateString to set
     */
    public void setEventDateString(String eventDateString)
    {
        this.eventDateString = eventDateString;
    }

    /**
     * @return the custom1
     */
    public String getCustom1()
    {
        return custom1;
    }

    /**
     * @param custom1
     *            the custom1 to set
     */
    public void setCustom1(String custom1)
    {
        this.custom1 = custom1;
    }

    /**
     * @return the custom2
     */
    public String getCustom2()
    {
        return custom2;
    }

    /**
     * @param custom2
     *            the custom2 to set
     */
    public void setCustom2(String custom2)
    {
        this.custom2 = custom2;
    }

    /**
     * @return the custom3
     */
    public String getCustom3()
    {
        return custom3;
    }

    /**
     * @param custom3
     *            the custom3 to set
     */
    public void setCustom3(String custom3)
    {
        this.custom3 = custom3;
    }

    /**
     * @return the custom4
     */
    public String getCustom4()
    {
        return custom4;
    }

    /**
     * @param custom4
     *            the custom4 to set
     */
    public void setCustom4(String custom4)
    {
        this.custom4 = custom4;
    }

    /**
     * @return the custom5
     */
    public String getCustom5()
    {
        return custom5;
    }

    /**
     * @param custom5
     *            the custom5 to set
     */
    public void setCustom5(String custom5)
    {
        this.custom5 = custom5;
    }

    /**
     * @return the wishListId
     */
    public int getWishListId()
    {
        return wishListId;
    }

    /**
     * @param wishListId the wishListId to set
     */
    public void setWishListId(int wishListId)
    {
        this.wishListId = wishListId;
    }

}
