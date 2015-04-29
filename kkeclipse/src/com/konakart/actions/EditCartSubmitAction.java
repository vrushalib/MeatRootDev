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
import com.konakart.appif.BasketIf;

/**
 * Gets called after editing the cart.
 */
public class EditCartSubmitAction extends BaseAction
{

    private static final long serialVersionUID = 1L;

    private String goToCheckout = "";

    private String couponCode;

    private String giftCertCode;

    private String rewardPoints;

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
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            /*
             * Determine whether action class has been called to change quantity of cart or delete
             * an item
             */
            String action = request.getParameter("action");
            String id = request.getParameter("id");
            String qtyStr = request.getParameter("qty");

            if (action != null && id != null)
            {

                int basketId;
                try
                {
                    basketId = Integer.parseInt(id);
                } catch (Exception e1)
                {
                    return "ShowCart";
                }

                /*
                 * We need to find the Basket object corresponding to the id passed in as a
                 * parameter and we remove it or update it if required.
                 */
                if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                        && kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems() != null)
                {
                    for (int j = 0; j < kkAppEng.getCustomerMgr().getCurrentCustomer()
                            .getBasketItems().length; j++)
                    {
                        BasketIf b = kkAppEng.getCustomerMgr().getCurrentCustomer()
                                .getBasketItems()[j];
                        if (b.getId() == basketId)
                        {
                            if (action.equals("r"))
                            {
                                // remove the basket item
                                kkAppEng.getBasketMgr().removeFromBasket(b, /* refresh */false);

                                // insert an event
                                insertCustomerEvent(kkAppEng, ACTION_REMOVE_FROM_CART,
                                        b.getProductId());
                            } else if (action.equals("p"))
                            {
                                int numCanAdd = 0;
                                if (b.getProduct() == null)
                                {
                                    numCanAdd = kkAppEng.getQuotaMgr().getQuotaForProduct(
                                            b.getProductId(), b.getOpts());
                                } else
                                {
                                    numCanAdd = kkAppEng.getQuotaMgr().getQuotaForProduct(
                                            b.getProduct(), b.getOpts());
                                }
                                if (numCanAdd > 0)
                                {
                                    b.setQuantity(b.getQuantity() + 1);
                                    kkAppEng.getBasketMgr().updateBasket(b, /* refresh */false);
                                } else
                                {
                                    // Add a message to say quota has been reached
                                    addActionError(kkAppEng.getMsg("common.quota.reached",
                                            new String[]
                                            { String.valueOf(kkAppEng.getQuotaMgr()
                                                    .getQuotaForProduct(b.getProductId(),
                                                            b.getOpts())) }));
                                }

                            } else if (action.equals("m"))
                            {
                                b.setQuantity(b.getQuantity() - 1);
                                // update the basket item quantity
                                if (b.getQuantity() == 0)
                                {
                                    // remove the basket item
                                    kkAppEng.getBasketMgr().removeFromBasket(b, /* refresh */
                                    false);

                                    // insert an event
                                    insertCustomerEvent(kkAppEng, ACTION_REMOVE_FROM_CART,
                                            b.getProductId());
                                } else
                                {
                                    kkAppEng.getBasketMgr().updateBasket(b, /* refresh */false);
                                }
                            } else if (action.equals("q"))
                            {
                                int quantity = 0;
                                if (qtyStr != null)
                                {
                                    try
                                    {
                                        quantity = Integer.parseInt(qtyStr);
                                    } catch (Exception e)
                                    {
                                        return "ShowCart";
                                    }
                                } else
                                {
                                    return "ShowCart";
                                }

                                if (quantity <= 0)
                                {
                                    // remove the basket item
                                    kkAppEng.getBasketMgr().removeFromBasket(b, /* refresh */
                                    false);

                                    // insert an event
                                    insertCustomerEvent(kkAppEng, ACTION_REMOVE_FROM_CART,
                                            b.getProductId());
                                } else if (quantity > b.getQuantity())
                                {
                                    int numCanAdd = 0;
                                    if (b.getProduct() == null)
                                    {
                                        numCanAdd = kkAppEng.getQuotaMgr().getQuotaForProduct(
                                                b.getProductId(), b.getOpts());
                                    } else
                                    {
                                        numCanAdd = kkAppEng.getQuotaMgr().getQuotaForProduct(
                                                b.getProduct(), b.getOpts());
                                    }
                                    if (numCanAdd >= quantity)
                                    {
                                        b.setQuantity(quantity);
                                        kkAppEng.getBasketMgr().updateBasket(b, /* refresh */false);
                                    } else if (numCanAdd > 0)
                                    {
                                        b.setQuantity(numCanAdd);
                                        kkAppEng.getBasketMgr().updateBasket(b, /* refresh */false);
                                        // Add a message to say quota has been reached
                                        addActionError(kkAppEng.getMsg("common.quota.reached",
                                                new String[]
                                                { String.valueOf(kkAppEng.getQuotaMgr()
                                                        .getQuotaForProduct(b.getProductId(),
                                                                b.getOpts())) }));
                                    } else
                                    {
                                        // Add a message to say quota has been reached
                                        addActionError(kkAppEng.getMsg("common.quota.reached",
                                                new String[]
                                                { String.valueOf(kkAppEng.getQuotaMgr()
                                                        .getQuotaForProduct(b.getProductId(),
                                                                b.getOpts())) }));
                                    }
                                } else if (quantity < b.getQuantity())
                                {
                                    b.setQuantity(quantity);
                                    // update the basket item quantity
                                    if (b.getQuantity() == 0)
                                    {
                                        // remove the basket item
                                        kkAppEng.getBasketMgr().removeFromBasket(b, /* refresh */
                                        false);

                                        // insert an event
                                        insertCustomerEvent(kkAppEng, ACTION_REMOVE_FROM_CART,
                                                b.getProductId());
                                    } else
                                    {
                                        kkAppEng.getBasketMgr().updateBasket(b, /* refresh */false);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } else
            {
                /*
                 * Set the coupon code in the order manager
                 */
                if (getCouponCode() == null || getCouponCode().length() == 0)
                {
                    kkAppEng.getOrderMgr().setCouponCode(null);
                } else
                {
                    kkAppEng.getOrderMgr().setCouponCode(getCouponCode());
                }

                /*
                 * Set the gift certificate code in the order manager
                 */
                if (getGiftCertCode() == null || getGiftCertCode().length() == 0)
                {
                    kkAppEng.getOrderMgr().setGiftCertCode(null);
                } else
                {
                    kkAppEng.getOrderMgr().setGiftCertCode(getGiftCertCode());
                }

                /*
                 * Set the reward points in the order manager. If someone tries to use more points
                 * than those available, then use the points available
                 */
                kkAppEng.getOrderMgr().setRewardPoints(0);
                if (kkAppEng.getRewardPointMgr().isEnabled())
                {
                    int pointsAvailable = kkAppEng.getRewardPointMgr().pointsAvailable();
                    if (getRewardPoints() != null && getRewardPoints().length() > 0
                            && pointsAvailable > 0)
                    {
                        try
                        {
                            int points = Integer.parseInt(getRewardPoints());
                            if (points > pointsAvailable)
                            {
                                kkAppEng.getOrderMgr().setRewardPoints(pointsAvailable);
                            } else if (points >= 0)
                            {
                                kkAppEng.getOrderMgr().setRewardPoints(points);
                            }
                        } catch (Exception e)
                        {
                        }
                    }
                }
            }

            // Update the basket data
            kkAppEng.getBasketMgr().getBasketItemsPerCustomer();

            kkAppEng.getNav().set(kkAppEng.getMsg("header.cart.contents"), request);

            if (getGoToCheckout().equalsIgnoreCase("true"))
            {
                return "Checkout";
            }
            return "ShowCart";
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the goToCheckout
     */
    public String getGoToCheckout()
    {
        return goToCheckout;
    }

    /**
     * @param goToCheckout
     *            the goToCheckout to set
     */
    public void setGoToCheckout(String goToCheckout)
    {
        this.goToCheckout = goToCheckout;
    }

    /**
     * @return the couponCode
     */
    public String getCouponCode()
    {
        return couponCode;
    }

    /**
     * @param couponCode
     *            the couponCode to set
     */
    public void setCouponCode(String couponCode)
    {
        this.couponCode = couponCode;
    }

    /**
     * @return the rewardPoints
     */
    public String getRewardPoints()
    {
        return rewardPoints;
    }

    /**
     * @param rewardPoints
     *            the rewardPoints to set
     */
    public void setRewardPoints(String rewardPoints)
    {
        this.rewardPoints = rewardPoints;
    }

    /**
     * @return the giftCertCode
     */
    public String getGiftCertCode()
    {
        return giftCertCode;
    }

    /**
     * @param giftCertCode
     *            the giftCertCode to set
     */
    public void setGiftCertCode(String giftCertCode)
    {
        this.giftCertCode = giftCertCode;
    }

}
