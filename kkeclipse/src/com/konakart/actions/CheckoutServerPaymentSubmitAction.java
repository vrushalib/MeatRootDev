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
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;

/**
 * Action called when the customer clicks the button to submit his credit card details
 */
public class CheckoutServerPaymentSubmitAction extends BaseAction
{

    /** Credit card number */
    private String number;

    /** Credit card CVV */
    private String cvv;

    /** Credit card type */
    private String type;

    /** Credit card expiry month */
    private String expiryMonth;

    /** Credit card expiry year */
    private String expiryYear;

    /** The name of the person on the credit card */
    private String owner;

    /** Postcode used for credit card validation */
    private String postcode;

    /** Street address used for credit card validation */
    private String streetAddress;

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {

            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "Checkout");

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

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            if (order == null)
            {
                throw new KKAppException("There is no order.");
            }

            if (order.getPaymentDetails() == null)
            {
                throw new KKAppException("There is no PaymentDetails object attached to the order.");
            }

            if (order.getPaymentDetails().getCode() == null)
            {
                throw new KKAppException(
                        "The PaymentDetails object contains a null code so we cannot determine which payment gateway to use.");
            }

            /*
             * Fill in the payment details with the CC details
             */
            PaymentDetailsIf pd = order.getPaymentDetails();
            pd.setCcCVV(getCvv());
            pd.setCcExpiryMonth(getExpiryMonth());
            pd.setCcExpiryYear(getExpiryYear());
            pd.setCcNumber(getNumber());
            pd.setCcOwner(getOwner());
            pd.setCcPostcode(getPostcode());
            pd.setCcStreetAddress(getStreetAddress());
            pd.setCcType(getType());

            /*
             * In the Struts config file you must define a forward with the name of the code
             */
            return order.getPaymentDetails().getCode();

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the number
     */
    public String getNumber()
    {
        return number;
    }

    /**
     * @param number
     *            the number to set
     */
    public void setNumber(String number)
    {
        this.number = number;
    }

    /**
     * @return the cvv
     */
    public String getCvv()
    {
        return cvv;
    }

    /**
     * @param cvv
     *            the cvv to set
     */
    public void setCvv(String cvv)
    {
        this.cvv = cvv;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the expiryMonth
     */
    public String getExpiryMonth()
    {
        return expiryMonth;
    }

    /**
     * @param expiryMonth
     *            the expiryMonth to set
     */
    public void setExpiryMonth(String expiryMonth)
    {
        this.expiryMonth = expiryMonth;
    }

    /**
     * @return the expiryYear
     */
    public String getExpiryYear()
    {
        return expiryYear;
    }

    /**
     * @param expiryYear
     *            the expiryYear to set
     */
    public void setExpiryYear(String expiryYear)
    {
        this.expiryYear = expiryYear;
    }

    /**
     * @return the owner
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
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

}
