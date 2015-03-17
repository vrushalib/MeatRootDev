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
import com.konakart.al.KKAppException;
import com.konakart.app.EmailOptions;
import com.konakart.app.OrderStatusHistory;
import com.konakart.app.PaymentDetails;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderStatusHistoryIf;
import com.konakart.appif.PaymentDetailsIf;

/**
 * Gets called after submitting the checkout confirmation page. Where we go from
 * here depends on the payment gateway chosen.
 */
public class CheckoutConfirmationSubmitAction extends BaseAction {
	private static final long serialVersionUID = 1L;

	private String comment;

	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		try {
			int custId;

			KKAppEng kkAppEng = this.getKKAppEng(request, response);

			custId = this.loggedIn(request, response, kkAppEng, "Checkout");

			// Check to see whether the user is logged in
			if (custId < 0) {
				return KKLOGIN;
			}

			// Ensure we are using the correct protocol. Redirect if not.
			String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */
					false);
			if (redirForward != null) {
				setupResponseForSSLRedirect(response, redirForward);
				return null;
			}

			// Ensure that the user hasn't submitted the order and then got back
			// to here using the
			// back button. We check to see whether the basket is null
			// Check to see whether there is something in the cart
			CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
			if (cust.getBasketItems() == null
					|| cust.getBasketItems().length == 0) {
				return "ShowCartItems";
			}

			// Get the host name and port number
			String hostAndPort = "";
			if (!kkAppEng.isPortlet()) {
				// Throws exception if called when running as a portlet
				hostAndPort = request.getServerName() + ":"
						+ request.getServerPort();
			}

			// Check that order is there and valid
			OrderIf checkoutOrder = kkAppEng.getOrderMgr().getCheckoutOrder();
			if (checkoutOrder == null) {
				return "Checkout";
			}

			// Set the comment
			String escapedComment = escapeFormInput(getComment());
			OrderStatusHistoryIf osh = new OrderStatusHistory();
			osh.setComments(escapedComment);
			OrderStatusHistoryIf[] oshArray = new OrderStatusHistoryIf[1];
			oshArray[0] = osh;
			osh.setUpdatedById(kkAppEng.getOrderMgr()
					.getIdForUserUpdatingOrder(checkoutOrder));
			checkoutOrder.setStatusTrail(oshArray);

			// Uncomment this for Canada Post - it sets the packaging details
			// checkoutOrder.setCustom4(checkoutOrder.getShippingQuote().getCustom4());

			/*
			 * Check to see whether the order total is set to 0. Don't bother
			 * with a payment gateway if it is.
			 */
			BigDecimal orderTotal = checkoutOrder.getTotalIncTax();
			if (orderTotal != null
					&& orderTotal.compareTo(java.math.BigDecimal.ZERO) == 0) {
				// Set the order status
				checkoutOrder
						.setStatus(com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS);

				// Save the order
				int orderId = kkAppEng.getOrderMgr().saveOrder(
						/* sendEmail */true, getEmailOptions(kkAppEng));

				// Update the inventory
				kkAppEng.getOrderMgr().updateInventory(orderId);

				// If we received no exceptions, delete the basket
				kkAppEng.getBasketMgr().emptyBasket();

				return "CheckoutFinished";
			}

			int paymentType = kkAppEng.getOrderMgr().getPaymentType();
			if (paymentType == PaymentDetails.BROWSER_PAYMENT_GATEWAY
					|| paymentType == PaymentDetails.BROWSER_IN_FRAME_PAYMENT_GATEWAY) {
				/*
				 * This payment gateway is a type where the customer enters the
				 * credit card details on a browser window belonging to the
				 * gateway. The result is normally returned through a callback.
				 * Therefore we don't update the inventory here, but leave it
				 * for the callback action which will do it if the payment was
				 * approved.
				 */

				// Set the order status
				checkoutOrder
						.setStatus(com.konakart.bl.OrderMgr.WAITING_PAYMENT_STATUS);

				// Save the order
				int orderId = kkAppEng.getOrderMgr().saveOrder(
						/* sendEmail */true, getEmailOptions(kkAppEng));

				// Get a fully populated PaymentDetails object and attach it to
				// the order
				PaymentDetailsIf pd = kkAppEng.getEng().getPaymentDetails(
						kkAppEng.getSessionId(),
						checkoutOrder.getPaymentDetails().getCode(), orderId,
						hostAndPort, kkAppEng.getLangId());
				checkoutOrder.setPaymentDetails(pd);
				checkoutOrder.setPaymentMethod(pd.getTitle());
				checkoutOrder.setPaymentModuleCode(pd.getCode());
				checkoutOrder.setPaymentModuleSubCode(pd.getSubCode());

				// Save pd.getCustom1() on the session with key: orderId +
				// "-CUSTOM1"
				kkAppEng.setCustomConfig(
						Integer.toString(orderId) + "-CUSTOM1", pd.getCustom1());
				// Save pd.getCustom2() on the session with key: orderId +
				// "-CUSTOM2"
				kkAppEng.setCustomConfig(
						Integer.toString(orderId) + "-CUSTOM2", pd.getCustom2());

				// If we received no exceptions, delete the basket
				kkAppEng.getBasketMgr().emptyBasket();

				if (pd.getPreProcessCode() != null) {
					return pd.getPreProcessCode();
				}

				if (paymentType == PaymentDetails.BROWSER_IN_FRAME_PAYMENT_GATEWAY) {
					return "CheckoutExternalPaymentFrame";
				}
				return "CheckoutExternalPayment";
			} else if (paymentType == PaymentDetails.SERVER_PAYMENT_GATEWAY) {
				/*
				 * This payment gateway is a type where the customer enters the
				 * credit card details on a browser window belonging to
				 * KonaKart. The details are passed to the KonaKart server which
				 * communicates with the Gateway server side. A response is
				 * returned immediately but we still save the order and change
				 * the state later.
				 * 
				 * Some notes on this:
				 * 
				 * -- If we save it after receiving payment notification,
				 * something may go wrong and we would have a payment
				 * notification for an unknown order.
				 * 
				 * -- If we save it after receiving payment notification, we
				 * don't have an order id to send to the gateway. The order id
				 * often appears in the email response from the gateway in order
				 * to match the response to the order.
				 * 
				 * -- We save the order with a pending status but don't send an
				 * email immediately. If the payment is approved, we change the
				 * status and then send an email.
				 * 
				 * -- If the payment request is never made, we keep the order in
				 * the database with a pending status.
				 * 
				 * -- If the payment is never approved, we keep the order in the
				 * database with a payment declined status. If the user made at
				 * least one attempt to pay for the order, we should also have
				 * an ipnHistory object with details of the gateway transaction.
				 */
				// Set the order status
				checkoutOrder
						.setStatus(com.konakart.bl.OrderMgr.WAITING_PAYMENT_STATUS);

				// Save the order
				int orderId = kkAppEng.getOrderMgr().saveOrder(
						/* sendEmail */false, null);

				// Get a fully populated PaymentDetails object and attach it to
				// the order
				PaymentDetailsIf pd = kkAppEng.getEng().getPaymentDetails(
						kkAppEng.getSessionId(),
						checkoutOrder.getPaymentDetails().getCode(), orderId,
						hostAndPort, kkAppEng.getLangId());
				checkoutOrder.setPaymentDetails(pd);
				checkoutOrder.setPaymentMethod(pd.getTitle());
				checkoutOrder.setPaymentModuleCode(pd.getCode());
				checkoutOrder.setPaymentModuleSubCode(pd.getSubCode());

				// Save pd.getCustom1() on the session with key: orderId +
				// "-CUSTOM1"
				kkAppEng.setCustomConfig(
						Integer.toString(orderId) + "-CUSTOM1", pd.getCustom1());
				// Save pd.getCustom2() on the session with key: orderId +
				// "-CUSTOM2"
				kkAppEng.setCustomConfig(
						Integer.toString(orderId) + "-CUSTOM2", pd.getCustom2());

				return "CheckoutServerPayment";
			} else if (paymentType == PaymentDetails.COD) {
				/*
				 * Cash On Delivery. The order is saved with a pending status
				 * and the inventory is updated.
				 */
				System.out.println("User selected cash on delivery");
				addDeliverySlotAndDeliveryDate(request, checkoutOrder);
				// Set the order status
				checkoutOrder
						.setStatus(com.konakart.bl.OrderMgr.PENDING_STATUS);

				kkAppEng.getOrderMgr().addPaymentDetailsToOrder("cod");

				// Save the order
				int orderId = kkAppEng.getOrderMgr().saveOrder(
						/* sendEmail */true, getEmailOptions(kkAppEng));

				// Update the inventory
				kkAppEng.getOrderMgr().updateInventory(orderId);

				// If we received no exceptions, delete the basket
				kkAppEng.getBasketMgr().emptyBasket();

				return "CheckoutFinished";
			} else {
				throw new KKAppException("This Payment Type is not supported");
			}
		} catch (Exception e) {
			return super.handleException(request, e);
		}
	}

	public void addDeliverySlotAndDeliveryDate(HttpServletRequest request, OrderIf checkoutOrder) {
		checkoutOrder.setCustom1(request.getParameter("udf2"));
		checkoutOrder.setCustom2(request.getParameter("udf3"));
	}


	/**
	 * Instantiate an EmailOptions object. Edit this method if you have
	 * installed Enterprise Extensions and want to attach an invoice to the
	 * eMail.
	 * 
	 * @param kkAppEng
	 * @return Returns a populated EmailOptions object
	 */
	private EmailOptionsIf getEmailOptions(KKAppEng kkAppEng) {
		EmailOptionsIf options = new EmailOptions();
		options.setCountryCode(kkAppEng.getLocale().substring(0, 2));
		options.setTemplateName("OrderConfReceived");

		// Attach the invoice to the confirmation email (Enterprise Only).
		// Defaults to false.
		// options.setAttachInvoice(true);

		// Create the invoice (if not already present) for attaching to the
		// confirmation email
		// (Enterprise Only). Defaults to false.
		// options.setCreateInvoice(true);

		return options;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
}