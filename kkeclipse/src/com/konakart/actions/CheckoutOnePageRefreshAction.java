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
import com.konakart.al.json.OptionJson;
import com.konakart.al.json.OrderJson;
import com.konakart.al.json.OrderProductJson;
import com.konakart.al.json.OrderTotalJson;
import com.konakart.al.json.ShippingQuoteJson;
import com.konakart.app.NameValue;
import com.konakart.app.ShippingQuote;
import com.konakart.appif.OptionIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.OrderTotalIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.appif.ShippingQuoteIf;

/**
 * Gets called before viewing the checkout delivery page.
 */
public class CheckoutOnePageRefreshAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String storeId;

    private String shipping;

    private String payment;

    private String couponCode;

    private String giftCertCode;

    private String rewardPoints;

    private String deliveryAddrId;

    private String billingAddrId;

    private OrderJson order;

    private String formattedDeliveryAddr;

    private String formattedBillingAddr;

    private String timeout;

    private boolean displayPriceWithTax;

    private String qtyMsg;

    private boolean otValid = true;

    private String xsrf_token;

    private NameValue[] paymentMethods;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId = -1;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "Checkout", xsrf_token);
            if (custId < 0)
            {
                timeout = "true";
                return SUCCESS;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            OrderIf checkoutOrder = kkAppEng.getOrderMgr().getCheckoutOrder();
            if (checkoutOrder == null)
            {
                throw new KKAppException("A Checkout Order does not exist");
            }

            boolean isMultiVendorMode = kkAppEng.isMultiVendor()
                    && checkoutOrder.getVendorOrders() != null;

            // Set the coupon code
            if (couponCode != null)
            {
                checkoutOrder.setCouponCode(couponCode);
                kkAppEng.getOrderMgr().setCouponCode(couponCode);
            }

            // Set the gift certificate code
            if (giftCertCode != null)
            {
                checkoutOrder.setGiftCertCode(giftCertCode);
                kkAppEng.getOrderMgr().setGiftCertCode(giftCertCode);
            }

            // Set the reward points
            if (kkAppEng.getRewardPointMgr().isEnabled())
            {
                int pointsAvailable = kkAppEng.getRewardPointMgr().pointsAvailable();
                if (rewardPoints != null && rewardPoints.length() > 0 && pointsAvailable > 0)
                {
                    try
                    {
                        checkoutOrder.setPointsRedeemed(Integer.parseInt(rewardPoints));
                        kkAppEng.getOrderMgr().setRewardPoints(Integer.parseInt(rewardPoints));
                    } catch (Exception e)
                    {
                    }
                }
            } else
            {
                checkoutOrder.setPointsRedeemed(0);
                kkAppEng.getOrderMgr().setRewardPoints(0);
            }
            if (shipping != null)
            {
                if (isMultiVendorMode)
                {
                    // Create a shipping quote for the parent order
                    ShippingQuoteIf sumOfVendorQuotes = new ShippingQuote();
                    sumOfVendorQuotes.setTax(new BigDecimal(0));
                    sumOfVendorQuotes.setTotalExTax(new BigDecimal(0));
                    sumOfVendorQuotes.setTotalIncTax(new BigDecimal(0));

                    for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
                    {
                        OrderIf vOrder = checkoutOrder.getVendorOrders()[i];
                        if (vOrder.getStoreId() != null && storeId != null
                                && vOrder.getStoreId().equals(storeId))
                        {
                            kkAppEng.getOrderMgr().addShippingQuoteToVendorOrder(shipping, vOrder,
                                    null);
                        }

                        // Add values to quote for main order
                        if (vOrder.getShippingQuote() != null)
                        {
                            sumOfVendorQuotes.setTax(sumOfVendorQuotes.getTax().add(
                                    vOrder.getShippingQuote().getTax()));
                            sumOfVendorQuotes.setTotalExTax(sumOfVendorQuotes.getTotalExTax().add(
                                    vOrder.getShippingQuote().getTotalExTax()));
                            sumOfVendorQuotes.setTotalIncTax(sumOfVendorQuotes.getTotalIncTax()
                                    .add(vOrder.getShippingQuote().getTotalIncTax()));
                        }
                    }
                    sumOfVendorQuotes.setTitle(kkAppEng.getMsg("common.shipping"));
                    checkoutOrder.setShippingQuote(sumOfVendorQuotes);
                } else
                {
                    // Attach the shipping quote to the order
                    kkAppEng.getOrderMgr().addShippingQuoteToOrder(shipping, null);
                }
            }

            // Attach the payment detail to the order
            if (payment != null)
            {
                kkAppEng.getOrderMgr().addPaymentDetailsToOrder(payment);
            }

            // Change the delivery address
            if (deliveryAddrId != null)
            {
                int addrId = -1;
                try
                {
                    addrId = Integer.parseInt(deliveryAddrId);
                } catch (Exception e)
                {
                }
                kkAppEng.getOrderMgr().setCheckoutOrderShippingAddress(addrId);
            }

            // Change the billing address
            if (billingAddrId != null)
            {
                int addrId = -1;
                try
                {
                    addrId = Integer.parseInt(billingAddrId);
                } catch (Exception e)
                {
                }

                // set the new billing address
                kkAppEng.getOrderMgr().setCheckoutOrderBillingAddress(addrId);

                // Get the current codes
                String currentCode = "";
                String currentSubCode = null;
                if (checkoutOrder.getPaymentDetails() != null)
                {
                    currentCode = checkoutOrder.getPaymentDetails().getCode();
                    currentSubCode = checkoutOrder.getPaymentDetails().getSubCode();
                }

                // Reset the order payment details
                checkoutOrder.setPaymentDetails(null);
                checkoutOrder.setPaymentMethod(null);
                checkoutOrder.setPaymentModuleCode(null);
                checkoutOrder.setPaymentModuleSubCode(null);

                // Get payment gateways from the engine for the new billing address
                PaymentDetailsIf[] pdArray = kkAppEng.getOrderMgr().createPaymentGatewayList();
                if (pdArray != null && pdArray.length > 0)
                {
                    // Create a single code
                    String code = currentCode
                            + ((currentSubCode == null) ? "" : ("~~" + currentSubCode));
                    boolean added = kkAppEng.getOrderMgr().addPaymentDetailsToOrder(code);
                    if (!added)
                    {
                        // The current Payment Gateway isn't available for the new address so
                        // add the top of the list
                        code = pdArray[0].getCode()
                                + ((pdArray[0].getSubCode() == null) ? "" : ("~~" + pdArray[0]
                                        .getSubCode()));
                        kkAppEng.getOrderMgr().addPaymentDetailsToOrder(code);

                        // Create an array to populate the drop list in the UI
                        paymentMethods = new NameValue[pdArray.length];
                        for (int i = 0; i < pdArray.length; i++)
                        {
                            PaymentDetailsIf pd = pdArray[i];
                            paymentMethods[i] = new NameValue(pd.getCode(), pd.getDescription());
                        }
                    }
                } else
                {
                    paymentMethods = new NameValue[1];
                    paymentMethods[0] = new NameValue("-1",
                            kkAppEng.getMsg("one.page.checkout.no.payment.methods"));
                }
            }

            // Call the engine to get the Order Totals
            kkAppEng.getOrderMgr().populateCheckoutOrderWithOrderTotals();

            /*
             * Check to see whether all compulsory order totals are present. The code in the
             * KKAppEngCallouts class may be modified to match your requirements.
             */
            otValid = new KKAppEngCallouts().validateOrderTotals(kkAppEng, checkoutOrder);

            // Create the main order
            order = new OrderJson();
            if (checkoutOrder.getOrderTotals() != null && checkoutOrder.getOrderTotals().length > 0)
            {
                OrderTotalJson[] otJsonArray = getJsonOrderTotals(kkAppEng,
                        checkoutOrder.getOrderTotals());
                order.setOrderTotals(otJsonArray);
            }

            // Don't need to populate the order products in multi-vendor mode
            if (!isMultiVendorMode)
            {
                if (checkoutOrder.getOrderProducts() != null
                        && checkoutOrder.getOrderProducts().length > 0)
                {
                    OrderProductJson[] opJsonArray = getJsonOrderProducts(kkAppEng,
                            checkoutOrder.getOrderProducts());
                    order.setOrderProducts(opJsonArray);
                }
            }

            // Create the multi vendor orders. Only need to populate the order products.
            if (isMultiVendorMode)
            {
                OrderJson[] orderArray = new OrderJson[checkoutOrder.getVendorOrders().length];
                for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
                {
                    OrderIf order = checkoutOrder.getVendorOrders()[i];
                    OrderJson orderJson = new OrderJson();

                    if (order.getOrderProducts() != null && order.getOrderProducts().length > 0)
                    {
                        OrderProductJson[] opJsonArray = getJsonOrderProducts(kkAppEng,
                                order.getOrderProducts());
                        orderJson.setOrderProducts(opJsonArray);
                    }
                    orderJson.setStoreId(order.getStoreId());

                    if (order.getShippingQuote() != null)
                    {
                        ShippingQuoteJson quote = new ShippingQuoteJson();
                        quote.setFormattedTotalExTax(kkAppEng.formatPrice(order.getShippingQuote()
                                .getTotalExTax()));
                        quote.setFormattedTotalIncTax(kkAppEng.formatPrice(order.getShippingQuote()
                                .getTotalIncTax()));
                        quote.setTitle(order.getShippingQuote().getTitle());
                        orderJson.setShippingQuote(quote);
                    }

                    if (order.getOrderTotals() != null && order.getOrderTotals().length > 0)
                    {
                        for (int j = 0; j < order.getOrderTotals().length; j++)
                        {
                            OrderTotalIf ot = order.getOrderTotals()[j];
                            if (kkAppEng.isTaxModule(ot.getClassName()))
                            {
                                OrderTotalJson[] otjArray = getJsonOrderTotals(kkAppEng,
                                        new OrderTotalIf[]
                                        { ot });
                                orderJson.setOrderTotals(otjArray);
                            }
                        }
                    }

                    orderArray[i] = orderJson;
                }
                order.setVendorOrders(orderArray);
            }

            formattedDeliveryAddr = kkAppEng.removeCData(checkoutOrder
                    .getDeliveryFormattedAddress());
            deliveryAddrId = Integer.toString(checkoutOrder.getDeliveryAddrId());
            formattedBillingAddr = kkAppEng.removeCData(checkoutOrder.getBillingFormattedAddress());
            billingAddrId = Integer.toString(checkoutOrder.getBillingAddrId());

            displayPriceWithTax = kkAppEng.displayPriceWithTax();

            // Messages
            qtyMsg = kkAppEng.getMsg("common.quantity");

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * Returns an array of Json OrderTotals
     * 
     * @param kkAppEng
     * @param otArray
     * @return Returns an array of Json OrderTotals
     * @throws KKAppException
     */
    OrderTotalJson[] getJsonOrderTotals(KKAppEng kkAppEng, OrderTotalIf[] otArray)
            throws KKAppException
    {
        OrderTotalJson[] otJsonArray = new OrderTotalJson[otArray.length];
        for (int i = 0; i < otArray.length; i++)
        {
            OrderTotalIf ot = otArray[i];
            OrderTotalJson otClone = new OrderTotalJson();
            otJsonArray[i] = otClone;
            otClone.setClassName(ot.getClassName());
            otClone.setTitle(ot.getTitle());
            otClone.setText(ot.getText());
            if ((ot.getClassName().equals("ot_reward_points"))
                    || (ot.getClassName().equals("ot_free_product")))
            {
                otClone.setValue(ot.getValue().toPlainString());
            } else
            {
                otClone.setValue(kkAppEng.formatPrice(ot.getValue()));
            }
        }
        return otJsonArray;
    }

    /**
     * Returns an array of Json OrderProducts
     * 
     * @param kkAppEng
     * @param opArray
     * @return Returns an array of Json OrderProducts
     * @throws KKAppException
     */
    OrderProductJson[] getJsonOrderProducts(KKAppEng kkAppEng, OrderProductIf[] opArray)
            throws KKAppException
    {
        OrderProductJson[] opJsonArray = new OrderProductJson[opArray.length];
        for (int i = 0; i < opArray.length; i++)
        {
            OrderProductIf op = opArray[i];
            OrderProductJson opClone = new OrderProductJson();
            opJsonArray[i] = opClone;
            opClone.setFormattedFinalPriceExTax(kkAppEng.formatPrice(op.getFinalPriceExTax()));
            opClone.setFormattedFinalPriceIncTax(kkAppEng.formatPrice(op.getFinalPriceIncTax()));
            opClone.setFormattedTaxRate((op.getTaxRate().setScale(1,
                    java.math.BigDecimal.ROUND_HALF_UP)).toPlainString());
            opClone.setName(op.getName());
            opClone.setQuantity(op.getQuantity());
            opClone.setProductId(op.getProductId());

            if (op.getOpts() != null && op.getOpts().length > 0)
            {
                OptionJson[] optArray = new OptionJson[op.getOpts().length];
                for (int j = 0; j < op.getOpts().length; j++)
                {
                    OptionIf opt = op.getOpts()[j];
                    OptionJson optClone = new OptionJson();
                    optClone.setName(opt.getName());
                    optClone.setQuantity(opt.getQuantity());
                    optClone.setType(opt.getType());
                    optClone.setValue(opt.getValue());
                    if (opt.getCustomerText() != null)
                    {
                        optClone.setCustomerText(opt.getCustomerText());
                    }
                    if (opt.getCustomerPrice() != null)
                    {
                        optClone.setFormattedCustPrice(kkAppEng.formatPrice(opt.getCustomerPrice()));
                    }
                    optArray[j] = optClone;
                }
                opClone.setOpts(optArray);
            }
        }
        return opJsonArray;
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
     * @return the deliveryAddrId
     */
    public String getDeliveryAddrId()
    {
        return deliveryAddrId;
    }

    /**
     * @param deliveryAddrId
     *            the deliveryAddrId to set
     */
    public void setDeliveryAddrId(String deliveryAddrId)
    {
        this.deliveryAddrId = deliveryAddrId;
    }

    /**
     * @return the billingAddrId
     */
    public String getBillingAddrId()
    {
        return billingAddrId;
    }

    /**
     * @param billingAddrId
     *            the billingAddrId to set
     */
    public void setBillingAddrId(String billingAddrId)
    {
        this.billingAddrId = billingAddrId;
    }

    /**
     * @return the formattedDeliveryAddr
     */
    public String getFormattedDeliveryAddr()
    {
        return formattedDeliveryAddr;
    }

    /**
     * @param formattedDeliveryAddr
     *            the formattedDeliveryAddr to set
     */
    public void setFormattedDeliveryAddr(String formattedDeliveryAddr)
    {
        this.formattedDeliveryAddr = formattedDeliveryAddr;
    }

    /**
     * @return the formattedBillingAddr
     */
    public String getFormattedBillingAddr()
    {
        return formattedBillingAddr;
    }

    /**
     * @param formattedBillingAddr
     *            the formattedBillingAddr to set
     */
    public void setFormattedBillingAddr(String formattedBillingAddr)
    {
        this.formattedBillingAddr = formattedBillingAddr;
    }

    /**
     * @return the timeout
     */
    public String getTimeout()
    {
        return timeout;
    }

    /**
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(String timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return the displayPriceWithTax
     */
    public boolean isDisplayPriceWithTax()
    {
        return displayPriceWithTax;
    }

    /**
     * @param displayPriceWithTax
     *            the displayPriceWithTax to set
     */
    public void setDisplayPriceWithTax(boolean displayPriceWithTax)
    {
        this.displayPriceWithTax = displayPriceWithTax;
    }

    /**
     * @return the qtyMsg
     */
    public String getQtyMsg()
    {
        return qtyMsg;
    }

    /**
     * @param qtyMsg
     *            the qtyMsg to set
     */
    public void setQtyMsg(String qtyMsg)
    {
        this.qtyMsg = qtyMsg;
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
     * @return the storeId
     */
    public String getStoreId()
    {
        return storeId;
    }

    /**
     * @param storeId
     *            the storeId to set
     */
    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }

    /**
     * @return the order
     */
    public OrderJson getOrder()
    {
        return order;
    }

    /**
     * @param order
     *            the order to set
     */
    public void setOrder(OrderJson order)
    {
        this.order = order;
    }

    /**
     * @return the xsrf_token
     */
    public String getXsrf_token()
    {
        return xsrf_token;
    }

    /**
     * @param xsrf_token
     *            the xsrf_token to set
     */
    public void setXsrf_token(String xsrf_token)
    {
        this.xsrf_token = xsrf_token;
    }

    /**
     * @return the paymentMethods
     */
    public NameValue[] getPaymentMethods()
    {
        return paymentMethods;
    }

    /**
     * @param paymentMethods
     *            the paymentMethods to set
     */
    public void setPaymentMethods(NameValue[] paymentMethods)
    {
        this.paymentMethods = paymentMethods;
    }
}
