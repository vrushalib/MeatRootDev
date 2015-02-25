package com.konakart.actions;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.EmailOptions;
import com.konakart.app.KKException;
import com.konakart.app.OrderStatusHistory;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderStatusHistoryIf;

/**
 * Gets called after returning from Payu
 */
public class PayuResponseAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private static final String CUSTOM = "udf1";
    private String comment;
	 //original key - "TCg9WT" and original salt - "k1rj3ntq"
	private static final String SALT = "k1rj3ntq";
	private static final String  MERCHANT_KEY = "TCg9WT";
	private static final String PIPE = "|";
	
    public String execute(){    
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        String sessionId = null;
        KKAppEng kkAppEng = null;
        
        try
        {
            if (request == null)
            {
            	return KKLOGIN;
            }

            sessionId = request.getParameter(PayuResponseAction.CUSTOM);
            System.out.println("session id in payuresponseaction:"+sessionId);
            if (sessionId == null)
            {
                System.out.println("The callback from Payu did not contain the 'udf1' parameter or it has null value.");
                return KKLOGIN;
            }

            // Get an instance of the KonaKart engine
            kkAppEng = this.getKKAppEng(request, response);

            /*
             * Use the session of the logged in user to initialize kkAppEng
             */
            try
            {
                kkAppEng.getEng().checkSession(sessionId);
                System.out.println("Session id exists and it is valid");
            } catch (KKException e){
                System.out.println("The SessionId from PayU Callback is not valid or it has timed out: "+ sessionId);
                return KKLOGIN;
            }

            // Log in the user
            kkAppEng.getCustomerMgr().loginBySession(sessionId);
            System.out.println("User logged in");
           
         // Check that order is there and valid
         			OrderIf checkoutOrder = kkAppEng.getOrderMgr().getCheckoutOrder();
         			if (checkoutOrder == null) {
         				return "Checkout";
         			}
            
         	System.out.println("status:"+request.getParameter("status")+", unmapped status:" + request.getParameter("unmappedstatus"));
         			
          //Status - failure and pending should be treated as failed transactions only (as per payu's integration doc)
			if(request.getParameter("status").equals(TransactionStatus.FAILURE.toString())
				|| request.getParameter("status").equals(TransactionStatus.PENDING.toString()) || 
				!isTransactionTamperProof(request, checkoutOrder)){
				checkoutOrder.setStatus(com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS);
				kkAppEng.getOrderMgr().addPaymentDetailsToOrder("payment declined");
				// Save the order
				int orderId = kkAppEng.getOrderMgr().saveOrder(	/* sendEmail */false, null);
				// delete the basket
				kkAppEng.getBasketMgr().emptyBasket();
				return "TransactionFailed";
			}
			
			//set the transaction id as tracking number for the reference
			checkoutOrder.setTrackingNumber(request.getParameter("txnid"));
			
			// Set the comment
			String escapedComment = escapeFormInput(getComment());
			OrderStatusHistoryIf osh = new OrderStatusHistory();
			osh.setComments(escapedComment);
			OrderStatusHistoryIf[] oshArray = new OrderStatusHistoryIf[1];
			oshArray[0] = osh;
			osh.setUpdatedById(kkAppEng.getOrderMgr()
					.getIdForUserUpdatingOrder(checkoutOrder));
			checkoutOrder.setStatusTrail(oshArray);

			// To set the delivery time - morning/evening
			// For now it is set to "m" by default. 
			//It should be removed when evening slot will be started and should be changed to the value selected by a user
			String deliverySlot = "m"; 
			checkoutOrder.setCustom1(deliverySlot);

			// To set the delivery date for order
			// For now its tomorrow's date by default
			Calendar c = new GregorianCalendar();
			c.add(Calendar.DATE, 1);
			checkoutOrder.setCustom2(new SimpleDateFormat("yyyy-MM-dd")
					.format(c.getTime()));
			

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
			/* sets  the status of a transaction as per the internal database of PayU. PayU’s system has several intermediate
			status which are used for tracking various activities internal to the system*/
			checkoutOrder.setCustom3(request.getParameter("unmappedstatus"));
			
			/*sets unique reference number created for each transaction at PayU’s end */
			checkoutOrder.setCustom4(request.getParameter("mihpayid"));
			
			// Set the order status
			checkoutOrder
					.setStatus(com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS);
			kkAppEng.getOrderMgr().addPaymentDetailsToOrder("payu");
			int orderId = kkAppEng.getOrderMgr().saveOrder(	/* sendEmail */true, getEmailOptions(kkAppEng));
			// Update the inventory
			kkAppEng.getOrderMgr().updateInventory(orderId);
			// If we received no exceptions, delete the basket
			kkAppEng.getBasketMgr().emptyBasket();
			
			return "CheckoutFinished";
        }
        catch(Exception e){
        	e.printStackTrace();
        	 return KKLOGIN;
        }

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
	 * @param type
	 * @param str
	 * @return hash value of the string
	 */
	public String hashCal(String type,String str){
		byte[] hashseq=str.getBytes();
		StringBuffer hexString = new StringBuffer();
		try{
			MessageDigest algorithm = MessageDigest.getInstance(type);
			algorithm.reset();
			algorithm.update(hashseq);
			byte messageDigest[] = algorithm.digest();
	
			for (int i=0;i<messageDigest.length;i++) {
				String hex=Integer.toHexString(0xFF & messageDigest[i]);
				if(hex.length()==1) hexString.append("0");
				hexString.append(hex);
			}
			
		}catch(NoSuchAlgorithmException nsae){
			System.out.println(nsae.getMessage());
			nsae.printStackTrace();
		}
		return hexString.toString();
	}
    
    /**
	 * Verifies the hash returned by PayU in the response. This is to make sure that the transaction hasn’t been tampered with.
	 * @param request
	 * @param checkoutOrder
	 * @return true/false
	 */
	public Boolean isTransactionTamperProof(HttpServletRequest request, OrderIf checkoutOrder){
//		sha512(SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key)
		String hash = request.getParameter("hash");
		StringBuffer hashString = new StringBuffer();
		hashString = hashString.append(SALT).append(PIPE).append(request.getParameter("status")).append(PIPE).append(PIPE).append(PIPE).append(PIPE)
				.append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(request.getParameter("udf1")).append(PIPE)
				.append(request.getParameter("email")).append(PIPE).append(request.getParameter("firstname")).append(PIPE)
				.append(request.getParameter("productinfo")).append(PIPE).append(request.getParameter("amount")).append(PIPE)
				.append(request.getParameter("txnid")).append(PIPE).append(MERCHANT_KEY);
		String calculatedHash = hashCal("SHA-512", hashString.toString());
		System.out.println("hashstring:"+hashString);
		System.out.println("hash:"+hash+" \ncalculated hash:"+ calculatedHash);
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
