package com.konakart.actions.ipn;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.actions.gateways.BaseGatewayAction;
import com.konakart.al.KKAppEng;
import com.konakart.app.EmailOptions;
import com.konakart.app.KKException;
import com.konakart.app.OrderStatusHistory;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.OrderStatusHistoryIf;
import com.konakart.appif.SSOTokenIf;

/**
 * Gets called after returning from Payu
 */
public class PayuAction extends BaseGatewayAction {
	private static final long serialVersionUID = 1L;

	private static final String CUSTOM = "udf1";
	private String comment;
	// original key - "TCg9WT" and original salt - "k1rj3ntq" - test : Merchant
	// key : VPcm4L, Salt : OmM6jqjz
	private static final String SALT = "k1rj3ntq";
	private static final String MERCHANT_KEY = "TCg9WT";
	private static final String PIPE = "|";

	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		String sessionId = null;
		KKAppEng kkAppEng = null;

		try {
			if (request == null) {
				return null;
			}
			/*
			 * Get the uuid from the request so that we can look up the SSO
			 * Token
			 */
			String uuid = request.getParameter(PayuAction.CUSTOM);
			System.out.println("uuid in payuaction: " + uuid);
			if (uuid == null) {
				throw new Exception(
						"The callback from PayPal did not contain the 'custom' parameter.");
			}

			// Get an instance of the KonaKart engine
			kkAppEng = this.getKKAppEng(request, response);
			SSOTokenIf token = kkAppEng.getEng().getSSOToken(uuid, /* deleteToken */
			true);
			if (token == null) {
				throw new Exception(
						"The SSOToken from the PayPal callback is null");
			}
			sessionId = token.getSessionId();
		/*	try {
				// Get the order id from custom1
				int orderId = Integer.parseInt(token.getCustom1());
			} catch (Exception e) {
				throw new Exception("The SSOToken does not contain an order id");
			}*/

			/*
			 * Use the session of the logged in user to initialise kkAppEng
			 */
			try {
				kkAppEng.getEng().checkSession(sessionId);
				System.out.println("Session is valid");
			} catch (KKException e) {
				throw new Exception(
						"The SessionId from the SSOToken in the PayPal Callback is not valid: "
								+ token.getSessionId());
			}

			// Log in the user
			kkAppEng.getCustomerMgr().loginBySession(sessionId);
			System.out.println("User logged in");

			// Check that order is there and valid
			OrderIf checkoutOrder = kkAppEng.getOrderMgr().getCheckoutOrder();
			if (checkoutOrder == null) {
				return "Checkout";
			}

			System.out.println("status:" + request.getParameter("status")
					+ ", unmapped status:"
					+ request.getParameter("unmappedstatus"));

			// Status - failure and pending should be treated as failed
			// transactions only (as per payu's integration doc)
			String status = request.getParameter("status");
			if (status.equals(TransactionStatus.FAILURE.toString())
					|| status.equals(TransactionStatus.PENDING.toString())
					|| !isTransactionTamperProof(request, checkoutOrder)) {
				addFailedTransactionDetailsToOrder(kkAppEng, checkoutOrder);
				return "TransactionFailed";
			}

			addComment(kkAppEng, checkoutOrder);
			addDeliverySlotAndDeliveryDate(request, checkoutOrder);
			/*
			 * Check to see whether the order total is set to 0. Don't bother
			 * with a payment gateway if it is.
			 */
			if (isOrderTotalZero(kkAppEng, checkoutOrder)) {
				System.out.println("Order total is set to 0");
				return "CheckoutFinished";
			}

			addPayuTransactionDetails(checkoutOrder, request);
			kkAppEng.getOrderMgr().addPaymentDetailsToOrder("payu");
			int orderId = kkAppEng.getOrderMgr().saveOrder(
			/* sendEmail */true, getEmailOptions(kkAppEng));
			// Update the inventory
			kkAppEng.getOrderMgr().updateInventory(orderId);
			// If we received no exceptions, delete the basket
			kkAppEng.getBasketMgr().emptyBasket();

			return "CheckoutFinished";
		} catch (Exception e) {
			e.printStackTrace();
			return KKLOGIN;
		}

	}

	public void addFailedTransactionDetailsToOrder(KKAppEng kkAppEng,
			OrderIf checkoutOrder) throws Exception {
		try {
			checkoutOrder
					.setStatus(com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS);
			kkAppEng.getOrderMgr().addPaymentDetailsToOrder("payment declined");
			// Save the order
			kkAppEng.getOrderMgr().saveOrder(
			/* sendEmail */false, null);
			// delete the basket
			kkAppEng.getBasketMgr().emptyBasket();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	public boolean isOrderTotalZero(KKAppEng kkAppEng, OrderIf checkoutOrder)
			throws Exception {
		BigDecimal orderTotal = checkoutOrder.getTotalIncTax();
		try {
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

				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		return false;
	}

	public void addPayuTransactionDetails(OrderIf checkoutOrder,
			HttpServletRequest request) {
		// set the transaction id as tracking number for the reference
		checkoutOrder.setTrackingNumber(request.getParameter("txnid"));

		/*
		 * sets the status of a transaction as per the internal database of
		 * PayU. PayU’s system has several intermediate status which are used
		 * for tracking various activities internal to the system
		 */
		checkoutOrder.setCustom3(request.getParameter("unmappedstatus"));

		/*
		 * sets unique reference number created for each transaction at PayU’s
		 * end
		 */
		checkoutOrder.setCustom4(request.getParameter("mihpayid"));

		// Set the order status
		checkoutOrder
				.setStatus(com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS);
	}

	/**
	 * To set the delivery time - morning/afternoon and delivery date today / tomorrow- depending on the time at which order is placed
	 * special case for zorabian products
	 * @param checkoutOrder
	 */
	public void addDeliverySlotAndDeliveryDate(HttpServletRequest request, OrderIf checkoutOrder) {
		/*String deliverySlot = null;
		String deliveryDay = null;
		Date today = new Date();
		Time now = new Time(today.getTime());

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 00); // 12 AM
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 00);
		Time twelveAm = new Time(cal.getTime().getTime());

		cal.set(Calendar.HOUR_OF_DAY, 6); // 6 AM
		Time sixAm = new Time(cal.getTime().getTime());

		cal.set(Calendar.HOUR_OF_DAY, 19); // 7 PM
		Time sevenPm = new Time(cal.getTime().getTime());

		cal.set(Calendar.HOUR_OF_DAY, 20); // 8:30 PM
		cal.set(Calendar.MINUTE, 30);
		Time eightThirtyPm = new Time(cal.getTime().getTime());

		if (orderContainsZorabianProduct(checkoutOrder)) {
			System.out.println("Order contains zorabian product(s)");
			deliverySlot = MORNING;
			if (now.after(sevenPm)) {
				deliveryDay = getDateAfterTomorrow();
			} else if (now.before(sixAm)) {
				deliveryDay = getDateTomorrow();
			}
		} else {
			if (now.after(twelveAm) && now.before(sixAm)) {
				deliverySlot = AFTERNOON;
				deliveryDay = getDateToday();
			} else if (now.before(eightThirtyPm)) {
				deliverySlot = MORNING;
				deliveryDay = getDateTomorrow();
			} else {
				deliverySlot = AFTERNOON;
				deliveryDay = getDateTomorrow();
			}
		}
		System.out.println("Delivery slot:" + deliverySlot + "  Delivery Day: "
				+ deliveryDay); */
		checkoutOrder.setCustom1(request.getParameter("udf2"));
		checkoutOrder.setCustom2(request.getParameter("udf3"));

	}

	public boolean orderContainsZorabianProduct(OrderIf checkoutOrder) {
		boolean flag = false;
		OrderProductIf[] products = checkoutOrder.getOrderProducts();
		for (OrderProductIf prod : products) {
			if (prod.getProduct().getManufacturerName()
					.equalsIgnoreCase("zorabian")) {
				flag = true;
			}
		}
		return flag;
	}

	public String getDateToday() {
		return new SimpleDateFormat("MMM dd, yyyy").format(new Date());
	}

	public String getDateTomorrow() {
		Calendar c = new GregorianCalendar();
		c.add(Calendar.DATE, 1);
		return (new SimpleDateFormat("MMM dd, yyyy").format(c.getTime()));
	}

	public String getDateAfterTomorrow() {
		Calendar c = new GregorianCalendar();
		c.add(Calendar.DATE, 2);
		return (new SimpleDateFormat("MMM dd, yyyy").format(c.getTime()));
	}

	public void addComment(KKAppEng kkAppEng, OrderIf checkoutOrder) {
		// Set the comment
		String escapedComment = escapeFormInput(getComment());
		OrderStatusHistoryIf osh = new OrderStatusHistory();
		osh.setComments(escapedComment);
		OrderStatusHistoryIf[] oshArray = new OrderStatusHistoryIf[1];
		oshArray[0] = osh;
		osh.setUpdatedById(kkAppEng.getOrderMgr().getIdForUserUpdatingOrder(
				checkoutOrder));
		checkoutOrder.setStatusTrail(oshArray);
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
	 * Calculates the hash depending on the value of algorithm type
	 * 
	 * @param type
	 * @param str
	 * @return hash value of the string
	 */
	public String hashCal(String type, String str) {
		byte[] hashseq = str.getBytes();
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest algorithm = MessageDigest.getInstance(type);
			algorithm.reset();
			algorithm.update(hashseq);
			byte messageDigest[] = algorithm.digest();

			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append("0");
				hexString.append(hex);
			}

		} catch (NoSuchAlgorithmException nsae) {
			System.out.println(nsae.getMessage());
			nsae.printStackTrace();
		}
		return hexString.toString();
	}

	/**
	 * Verifies the hash returned by PayU in the response. This is to make sure
	 * that the transaction hasn’t been tampered with.
	 * 
	 * @param request
	 * @param checkoutOrder
	 * @return true/false
	 */
	public Boolean isTransactionTamperProof(HttpServletRequest request,
			OrderIf checkoutOrder) {
		// sha512(SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key)
		String hash = request.getParameter("hash");
		StringBuffer hashString = new StringBuffer();
		hashString = hashString.append(SALT).append(PIPE)
				.append(request.getParameter("status")).append(PIPE)
				.append(PIPE).append(PIPE).append(PIPE).append(PIPE)
				.append(PIPE).append(PIPE).append(PIPE).append(request.getParameter("udf3"))
				.append(PIPE).append(request.getParameter("udf2"))
				.append(PIPE).append(request.getParameter("udf1")).append(PIPE)
				.append(request.getParameter("email")).append(PIPE)
				.append(request.getParameter("firstname")).append(PIPE)
				.append(request.getParameter("productinfo")).append(PIPE)
				.append(request.getParameter("amount")).append(PIPE)
				.append(request.getParameter("txnid")).append(PIPE)
				.append(MERCHANT_KEY);
		String calculatedHash = hashCal("SHA-512", hashString.toString());
		System.out.println("hashstring:" + hashString);
		System.out.println("hash:" + hash + " \ncalculated hash:"
				+ calculatedHash);
		return hash.equals(calculatedHash);
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
