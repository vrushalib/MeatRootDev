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
import com.konakart.app.ReviewSearch;
import com.konakart.appif.ProductIf;
import com.konakart.bl.ConfigConstants;

/**
 * Gets called after submitting the write review page.
 */
public class WriteReviewSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private int rating;

    private String reviewText;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "WriteReview");
            
            // Go to login page if session has timed out
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

            kkAppEng.getReviewMgr().writeReview(getReviewText(), getRating(), custId);

            // Set reward points if applicable
            if (kkAppEng.getRewardPointMgr().isEnabled())
            {
                String pointsStr = kkAppEng.getConfig(ConfigConstants.REVIEW_REWARD_POINTS);
                if (pointsStr != null)
                {
                    int points = 0;
                    try
                    {
                        points = Integer.parseInt(pointsStr);
                        kkAppEng.getRewardPointMgr().addPoints(points, "REV",
                                kkAppEng.getMsg("reward.points.review"));
                    } catch (Exception e)
                    {
                        log.warn("The REVIEW_REWARD_POINTS configuration variable has been set with a non numeric value: "
                                + pointsStr);
                    }
                }
            }

            // Get the latest reviews
            ProductIf prod = kkAppEng.getProductMgr().getSelectedProduct();
            if (prod != null)
            {
                ReviewSearch search = new ReviewSearch();
                search.setProductId(prod.getId());
                kkAppEng.getReviewMgr().fetchReviews(null, search);
            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.reviews"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the rating
     */
    public int getRating()
    {
        return rating;
    }

    /**
     * @param rating
     *            the rating to set
     */
    public void setRating(int rating)
    {
        this.rating = rating;
    }

    /**
     * @return the reviewText
     */
    public String getReviewText()
    {
        return reviewText;
    }

    /**
     * @param reviewText
     *            the reviewText to set
     */
    public void setReviewText(String reviewText)
    {
        this.reviewText = reviewText;
    }

}
