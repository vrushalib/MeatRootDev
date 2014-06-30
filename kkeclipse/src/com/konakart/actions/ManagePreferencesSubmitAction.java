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

/**
 * Gets called after submitting the manage preferences form.
 */
public class ManagePreferencesSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private int productPageSize;

    private int orderPageSize;

    private int reviewPageSize;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        KKAppEng kkAppEng;
        try
        {
            kkAppEng = this.getKKAppEng(request, response);
        } catch (Exception e1)
        {
            return super.handleException(request, e1);
        }
        try
        {
            int custId;

            custId = this.loggedIn(request, response, kkAppEng, "ManagePreferences");

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

            /*
             * Save preferences in managers, cookies and tags
             */
            kkAppEng.getProductMgr().setMaxDisplaySearchResults(getProductPageSize());
            setKKCookie(TAG_PROD_PAGE_SIZE, Integer.toString(getProductPageSize()), request,
                    response, kkAppEng);
            kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_PROD_PAGE_SIZE,
                    Integer.toString(getProductPageSize()));

            kkAppEng.getOrderMgr().setPageSize(getOrderPageSize());
            setKKCookie(TAG_ORDER_PAGE_SIZE, Integer.toString(getOrderPageSize()), request,
                    response, kkAppEng);
            kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_ORDER_PAGE_SIZE,
                    Integer.toString(getOrderPageSize()));

            kkAppEng.getReviewMgr().setPageSize(getReviewPageSize());
            setKKCookie(TAG_REVIEW_PAGE_SIZE, Integer.toString(getReviewPageSize()), request,
                    response, kkAppEng);
            kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_REVIEW_PAGE_SIZE,
                    Integer.toString(getReviewPageSize()));

            return SUCCESS;

        } catch (Exception e)
        {
            return getForward(request, e, "com.konakart.app.KKPasswordDoesntMatchException",
                    kkAppEng.getMsg("change.password.body.error"), "Error");
        }
    }

    /**
     * @return the productPageSize
     */
    public int getProductPageSize()
    {
        return productPageSize;
    }

    /**
     * @param productPageSize
     *            the productPageSize to set
     */
    public void setProductPageSize(int productPageSize)
    {
        this.productPageSize = productPageSize;
    }

    /**
     * @return the orderPageSize
     */
    public int getOrderPageSize()
    {
        return orderPageSize;
    }

    /**
     * @param orderPageSize
     *            the orderPageSize to set
     */
    public void setOrderPageSize(int orderPageSize)
    {
        this.orderPageSize = orderPageSize;
    }

    /**
     * @return the reviewPageSize
     */
    public int getReviewPageSize()
    {
        return reviewPageSize;
    }

    /**
     * @param reviewPageSize
     *            the reviewPageSize to set
     */
    public void setReviewPageSize(int reviewPageSize)
    {
        this.reviewPageSize = reviewPageSize;
    }
}
