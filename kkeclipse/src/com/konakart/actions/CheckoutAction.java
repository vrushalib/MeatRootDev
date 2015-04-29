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
import com.konakart.al.KKAppEngCallouts;
import com.konakart.al.KKAppException;
import com.konakart.app.ShippingQuote;
import com.konakart.appif.BasketIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.ConfigConstants;

/**
 * Gets called before viewing the checkout delivery page.
 */
public class CheckoutAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String comment;

    private String shipping;

    private String[] vShipping;

    private String payment;

    private String couponCode;

    private String giftCertCode;

    private String rewardPoints;

    private boolean reset = true;

    private int rewardPointsAvailable;

    private boolean otValid = true;

    private int deliveryId = -1;

    /**
     * Called when we don't want to reset the checkout order but just change the delivery address
     * 
     * @return Returns a forward string
     */
    public String newDeliveryAddr()
    {
        this.reset = false;
        if (deliveryId > -1)
        {
            HttpServletRequest request = ServletActionContext.getRequest();
            HttpServletResponse response = ServletActionContext.getResponse();
            try
            {
                KKAppEng kkAppEng = this.getKKAppEng(request, response);
                kkAppEng.getOrderMgr().setCheckoutOrderShippingAddress(deliveryId);
                OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
                if (order != null)
                {
                    order.setShippingQuote(null);
                }
            } catch (Exception e)
            {
                return super.handleException(request, e);
            }
        }
        return execute();
    }

    /**
     * Called when we don't want to reset the checkout order
     * 
     * @return Returns a forward string
     */
    public String noReset()
    {
        this.reset = false;
        return execute();
    }

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId = -1;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Check to see whether it's a punchout
            boolean punchout = (kkAppEng.getPunchoutDetails() != null);

            // Check to see whether the user is logged in unless it's punchout
            if (!punchout)
            {
                custId = this.loggedIn(request, response, kkAppEng, "Checkout");
                if (custId < 0)
                {
                    // Uncomment to force checkout without registration

                    // String allowNoRegisterStr = kkAppEng
                    // .getConfig(ConfigConstants.ALLOW_CHECKOUT_WITHOUT_REGISTRATION);
                    // if ((allowNoRegisterStr != null &&
                    // allowNoRegisterStr.equalsIgnoreCase("true")))
                    // {
                    // return "DeliveryAddr";
                    // }
                    return KKLOGIN;
                }
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Update the basket data from the database
            kkAppEng.getBasketMgr().getBasketItemsPerCustomer();

            // Check to see whether there is something in the cart
            CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            if (cust.getBasketItems() == null || cust.getBasketItems().length == 0)
            {
                return "ShowCartItems";
            }

            // Check that all cart items have a quantity of at least one
            boolean removed = false;
            for (int i = 0; i < cust.getBasketItems().length; i++)
            {
                BasketIf b = cust.getBasketItems()[i];
                if (b.getQuantity() <= 0)
                {
                    kkAppEng.getBasketMgr().removeFromBasket(b, /* refresh */false);
                    removed = true;
                }
            }

            if (removed)
            {
                // Update the basket data from the database
                kkAppEng.getBasketMgr().getBasketItemsPerCustomer();

                // Check to see whether there is something in the cart
                if (cust.getBasketItems() == null || cust.getBasketItems().length == 0)
                {
                    return "ShowCartItems";
                }
            }

            // Check to see whether we are trying to checkout an item that isn't in stock
            String stockAllowCheckout = kkAppEng.getConfig(ConfigConstants.STOCK_ALLOW_CHECKOUT);
            if (stockAllowCheckout != null && stockAllowCheckout.equalsIgnoreCase("false"))
            {
                // If required, we check to see whether the products are in stock
                BasketIf[] items = kkAppEng.getEng().updateBasketWithStockInfoWithOptions(
                        cust.getBasketItems(), kkAppEng.getBasketMgr().getAddToBasketOptions());
                for (int i = 0; i < items.length; i++)
                {
                    BasketIf basket = items[i];
                    if (basket.getQuantity() > basket.getQuantityInStock())
                    {
                        if (basket.getQuantityInStock() > 0)
                        {
                            addActionError(kkAppEng.getMsg("edit.cart.body.limitedstock.error",
                                    basket.getProduct().getName(),
                                    Integer.toString(basket.getQuantityInStock())));
                        } else
                        {
                            addActionError(kkAppEng.getMsg("edit.cart.body.outofstock.error",
                                    basket.getProduct().getName(),
                                    Integer.toString(basket.getQuantityInStock())));
                        }
                        return "ShowCartItems";
                    }
                }
            }

            // Insert event
            insertCustomerEvent(kkAppEng, ACTION_ENTER_CHECKOUT);

            // Go to punch out JSP if its a punch out
            if (punchout)
            {
                return "PunchoutCheckout";
            }

            /*
             * If we are returning from a change of address we don't want to reset everything.
             * Otherwise we create an order object that we will use for the checkout process
             */
            if (reset)
            {
                /*
                 * New order is populated with coupon code, gift certificate and reward points if
                 * these were entered in the edit cart screen
                 */
                kkAppEng.getOrderMgr().createCheckoutOrder();
            }

            if (kkAppEng.getOrderMgr().getCheckoutOrder() == null)
            {
                throw new KKAppException("A new Order could not be created");
            }

            // Get shipping quotes from the engine
            kkAppEng.getOrderMgr().createShippingQuotes();

            // Get payment gateways from the engine
            kkAppEng.getOrderMgr().createPaymentGatewayList();

            // Populate attributes for JSP page
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            couponCode = order.getCouponCode();
            giftCertCode = order.getGiftCertCode();
            if (order.getPointsRedeemed() > 0)
            {
                rewardPoints = Integer.toString(order.getPointsRedeemed());
            }

            if (kkAppEng.isMultiVendor() && order.getVendorOrders() != null)
            {
                // Create a shipping quote for the parent order
                ShippingQuoteIf sumOfVendorQuotes = new ShippingQuote();
                sumOfVendorQuotes.setTax(new BigDecimal(0));
                sumOfVendorQuotes.setTotalExTax(new BigDecimal(0));
                sumOfVendorQuotes.setTotalIncTax(new BigDecimal(0));
                shipping = "";

                vShipping = new String[order.getVendorOrders().length];
                for (int i = 0; i < order.getVendorOrders().length; i++)
                {
                    OrderIf vOrder = order.getVendorOrders()[i];
                    ShippingQuoteIf[] vQuotes = kkAppEng.getOrderMgr().getVendorShippingQuoteMap()
                            .get(vOrder.getStoreId());
                    String desc = null;
                    if (vOrder.getShippingQuote() != null)
                    {
                        vShipping[i] = vOrder.getShippingQuote().getCode();
                        desc = vOrder.getShippingQuote().getDescription();
                    } else if (vQuotes != null && (vQuotes.length > 0))
                    {
                        vShipping[i] = vQuotes[0].getCode();
                    } else
                    {
                        vShipping[i] = "";
                    }
                    // Attach the shipping quote to the order
                    kkAppEng.getOrderMgr()
                            .addShippingQuoteToVendorOrder(vShipping[i], vOrder, desc);
                    // Add values to quote for main order
                    if (vOrder.getShippingQuote() != null)
                    {
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
                String desc = null;
                if (order.getShippingQuote() != null)
                {
                    shipping = order.getShippingQuote().getCode();
                    desc = order.getShippingQuote().getDescription();
                } else if (kkAppEng.getOrderMgr().getShippingQuotes() != null
                        && (kkAppEng.getOrderMgr().getShippingQuotes().length > 0))
                {
                    shipping = kkAppEng.getOrderMgr().getShippingQuotes()[0].getCode();
                } else
                {
                    shipping = "";
                }

                // Attach the shipping quote to the order
                kkAppEng.getOrderMgr().addShippingQuoteToOrder(shipping, desc);
            }

            if (order.getPaymentDetails() != null)
            {
                PaymentDetailsIf pd = order.getPaymentDetails();
                payment = pd.getCode()
                        + ((pd.getSubCode() == null) ? "" : ("~~" + pd.getSubCode()));
            } else if (kkAppEng.getOrderMgr().getPaymentDetailsArray() != null
                    && kkAppEng.getOrderMgr().getPaymentDetailsArray().length > 0)
            {
                PaymentDetailsIf pd = kkAppEng.getOrderMgr().getPaymentDetailsArray()[0];
                payment = pd.getCode()
                        + ((pd.getSubCode() == null) ? "" : ("~~" + pd.getSubCode()));
            } else
            {
                payment = "";
            }
            // Attach the payment details to the order
            kkAppEng.getOrderMgr().addPaymentDetailsToOrder(payment);

            if (order.getStatusTrail() != null && order.getStatusTrail()[0] != null)
            {
                comment = order.getStatusTrail()[0].getComments();
            }

            // Set the points available
            String rewardPointsEnabled = kkAppEng.getConfig("ENABLE_REWARD_POINTS");
            if (rewardPointsEnabled != null && rewardPointsEnabled.equalsIgnoreCase("TRUE"))
            {
                rewardPointsAvailable = kkAppEng.getRewardPointMgr().pointsAvailable();
            }

            // Call the engine to get the Order Totals
            kkAppEng.getOrderMgr().populateCheckoutOrderWithOrderTotals();

            /*
             * Check to see whether all compulsory order totals are present. The code in the
             * KKAppEngCallouts class may be modified to match your requirements.
             */
            otValid = new KKAppEngCallouts().validateOrderTotals(kkAppEng, kkAppEng.getOrderMgr()
                    .getCheckoutOrder());

            // Ensure that the current customer has his addresses populated
            kkAppEng.getCustomerMgr().populateCurrentCustomerAddresses(/* force */false);

            kkAppEng.getNav().set(kkAppEng.getMsg("header.checkout"));
            kkAppEng.getNav().add(kkAppEng.getMsg("header.order.confirmation"));
            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the comment
     */
    public String getComment()
    {
        if (comment != null)
        {
            comment = escapeFormInput(comment);
        }
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * @return the shipping
     */
    public String getShipping()
    {
        return shipping;
    }

    /**
     * @param shipping
     *            the shipping to set
     */
    public void setShipping(String shipping)
    {
        this.shipping = shipping;
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
     * @return the payment
     */
    public String getPayment()
    {
        return payment;
    }

    /**
     * @param payment
     *            the payment to set
     */
    public void setPayment(String payment)
    {
        this.payment = payment;
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

    /**
     * @return the otValid
     */
    public boolean isOtValid()
    {
        return otValid;
    }

    /**
     * @param otValid
     *            the otValid to set
     */
    public void setOtValid(boolean otValid)
    {
        this.otValid = otValid;
    }

    /**
     * @return the vShipping
     */
    public String[] getvShipping()
    {
        return vShipping;
    }

    /**
     * @param vShipping
     *            the vShipping to set
     */
    public void setvShipping(String[] vShipping)
    {
        this.vShipping = vShipping;
    }

    /**
     * @return the deliveryId
     */
    public int getDeliveryId()
    {
        return deliveryId;
    }

    /**
     * @param deliveryId
     *            the deliveryId to set
     */
    public void setDeliveryId(int deliveryId)
    {
        this.deliveryId = deliveryId;
    }

}
