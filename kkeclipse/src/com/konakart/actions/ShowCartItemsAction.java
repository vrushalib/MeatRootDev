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

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.CreateOrderOptions;
import com.konakart.app.ShippingQuote;
import com.konakart.appif.BasketIf;
import com.konakart.appif.CreateOrderOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.ShippingQuoteIf;

/**
 * Called to display the new cart items after editing the cart. It uses the EditCartForm.
 */
public class ShowCartItemsAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String couponCode;

    private String giftCertCode;

    private String rewardPoints;

    private int rewardPointsAvailable;

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

            // Set the coupon code from the one saved in the order manager
            if (kkAppEng.getOrderMgr().getCouponCode() == null)
            {
                setCouponCode("");
            } else
            {
                setCouponCode(kkAppEng.getOrderMgr().getCouponCode());
            }

            // Set the GiftCert code from the one saved in the order manager
            if (kkAppEng.getOrderMgr().getGiftCertCode() == null)
            {
                setGiftCertCode("");
            } else
            {
                setGiftCertCode(kkAppEng.getOrderMgr().getGiftCertCode());
            }

            // Set the reward points from the ones saved in the order manager
            if (kkAppEng.getOrderMgr().getRewardPoints() == 0)
            {
                setRewardPoints("");
            } else
            {
                setRewardPoints(String.valueOf(kkAppEng.getOrderMgr().getRewardPoints()));
            }

            // Set the points available
            String rewardPointsEnabled = kkAppEng.getConfig("ENABLE_REWARD_POINTS");
            if (rewardPointsEnabled != null && rewardPointsEnabled.equalsIgnoreCase("TRUE"))
            {
                setRewardPointsAvailable(kkAppEng.getRewardPointMgr().pointsAvailable());
            }

            if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems() != null
                    && kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems().length > 0)
            {

                // We update the basket with the quantities in stock
                BasketIf[] items = kkAppEng.getEng().updateBasketWithStockInfoWithOptions(
                        kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems(),
                        kkAppEng.getBasketMgr().getAddToBasketOptions());
                
                kkAppEng.getCustomerMgr().getCurrentCustomer().setBasketItems(items);

                /*
                 * Create a temporary order to get order totals that we can display in the edit cart
                 * screen. Comment this out if you don't want to show extra information such as
                 * shipping and discounts before checkout.
                 */
                createTempOrder(kkAppEng, custId, items);

            }

            kkAppEng.getNav().set(kkAppEng.getMsg("header.cart.contents"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /*
     * Populate checkout order with a temporary order created before the checkout process really
     * begins. If the customer hasn't registered or logged in yet, we use the default customer to
     * create the order.
     * 
     * With this temporary order we can give the customer useful information on shipping costs and
     * discounts without him having to login.
     */
    private void createTempOrder(KKAppEng kkAppEng, int custId, BasketIf[] items)
    {
        try
        {
            // Reset the checkout order
            kkAppEng.getOrderMgr().setCheckoutOrder(null);

            CreateOrderOptionsIf options = new CreateOrderOptions();
            if (custId < 0)
            {
                options.setUseDefaultCustomer(true);
            } else
            {
                options.setUseDefaultCustomer(false);
            }

            OrderIf order = kkAppEng.getOrderMgr().createCheckoutOrderWithOptions(options);

            if (order == null)
            {
                return;
            }

            /*
             * We set the customer id to that of the guest customer so that promotions with
             * expressions are calculated correctly
             */
            if (custId < 0)
            {
                order.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
            }

            // Populate the order with the coupon code if it exists
            if (getCouponCode() != null && getCouponCode().length() > 0)
            {
                order.setCouponCode(getCouponCode());
            }

            // Populate the order with the GiftCert code if it exists
            if (getGiftCertCode() != null && getGiftCertCode().length() > 0)
            {
                order.setGiftCertCode(getGiftCertCode());
            }

            // Populate the order with the redeemed points
            if (!(getRewardPoints() == null || getRewardPoints().length() == 0))
            {
                try
                {
                    order.setPointsRedeemed(Integer.parseInt(getRewardPoints()));
                } catch (Exception e)
                {
                }
            }

            // Get shipping quotes and select the first one
            kkAppEng.getOrderMgr().createShippingQuotes();
            if (kkAppEng.isMultiVendor() && order.getVendorOrders() != null)
            {
                // Create a shipping quote for the parent order
                ShippingQuoteIf sumOfVendorQuotes = new ShippingQuote();
                sumOfVendorQuotes.setTax(new BigDecimal(0));
                sumOfVendorQuotes.setTotalExTax(new BigDecimal(0));
                sumOfVendorQuotes.setTotalIncTax(new BigDecimal(0));

                for (int i = 0; i < order.getVendorOrders().length; i++)
                {
                    OrderIf vOrder = order.getVendorOrders()[i];
                    ShippingQuoteIf[] vQuotes = kkAppEng.getOrderMgr().getVendorShippingQuoteMap()
                            .get(vOrder.getStoreId());
                    if (vQuotes != null && vQuotes.length > 0)
                    {
                        vOrder.setShippingQuote(vQuotes[0]);

                        // Add values to quote for main order
                        sumOfVendorQuotes.setTax(sumOfVendorQuotes.getTax().add(
                                vOrder.getShippingQuote().getTax()));
                        sumOfVendorQuotes.setTotalExTax(sumOfVendorQuotes.getTotalExTax().add(
                                vOrder.getShippingQuote().getTotalExTax()));
                        sumOfVendorQuotes.setTotalIncTax(sumOfVendorQuotes.getTotalIncTax().add(
                                vOrder.getShippingQuote().getTotalIncTax()));
                    }
                }
                sumOfVendorQuotes.setTitle(kkAppEng.getMsg("common.shipping"));
                order.setShippingQuote(sumOfVendorQuotes);
            } else
            {
                // Attach the shipping quote to the order
                if (kkAppEng.getOrderMgr().getShippingQuotes() != null
                        && kkAppEng.getOrderMgr().getShippingQuotes().length > 0)
                {
                    order.setShippingQuote(kkAppEng.getOrderMgr().getShippingQuotes()[0]);
                }
            }

            // Populate the checkout order with order totals
            kkAppEng.getOrderMgr().populateCheckoutOrderWithOrderTotals();

        } catch (Exception e)
        {
            // If the order can't be created we don't report back an exception
            if (log.isWarnEnabled())
            {
                log.warn("A temporary order could not be created", e);
            }
        }
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
     * @return the rewardPointsAvailable
     */
    public int getRewardPointsAvailable()
    {
        return rewardPointsAvailable;
    }

    /**
     * @param rewardPointsAvailable
     *            the rewardPointsAvailable to set
     */
    public void setRewardPointsAvailable(int rewardPointsAvailable)
    {
        this.rewardPointsAvailable = rewardPointsAvailable;
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
