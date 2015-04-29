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
import com.konakart.appif.DataDescriptorIf;

/**
 * Gets called after selecting the number of reviews to display from the drop list in the reviews page 
 */
public class RevPageSizeAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private int numRevs;
    
    private long t;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, null);

            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId,/* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            /*
             * Save preferences in managers, cookies and tags
             */
            kkAppEng.getReviewMgr().setPageSize(numRevs);
            setKKCookie(TAG_REVIEW_PAGE_SIZE, Integer.toString(numRevs), request,
                    response, kkAppEng);
            kkAppEng.getCustomerTagMgr().insertCustomerTag(TAG_REVIEW_PAGE_SIZE,
                    Integer.toString(numRevs));

            DataDescriptorIf dd =  kkAppEng.getReviewMgr().getDataDesc();
            if (dd != null)
            {
                dd.setLimit(numRevs + 1);
                dd.setOffset(0);                
                kkAppEng.getReviewMgr().orderCurrentReviews(dd.getOrderBy(), t);
            }
            kkAppEng.getReviewMgr().setShowTab(true);
             
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the t
     */
    public long getT()
    {
        return t;
    }

    /**
     * @param t the t to set
     */
    public void setT(long t)
    {
        this.t = t;
    }

    /**
     * @return the numRevs
     */
    public int getNumRevs()
    {
        return numRevs;
    }

    /**
     * @param numRevs the numRevs to set
     */
    public void setNumRevs(int numRevs)
    {
        this.numRevs = numRevs;
    }

}
