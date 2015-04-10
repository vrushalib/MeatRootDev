package com.konakart.actions.ipn;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.actions.gateways.BaseGatewayAction;
import com.konakart.al.KKAppEng;
import com.konakart.app.EmailOptions;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.SSOTokenIf;

/**
 * Gets called after returning from Payu
 */
public class PayuAction extends BaseGatewayAction {
	private static final long serialVersionUID = 1L;
	
	protected Log log = LogFactory.getLog(PayuAction.class);

	private static final String CUSTOM = "udf1";
	private String comment;
	// original key - "TCg9WT" and original salt - "k1rj3ntq" 
	// Test key : VPcm4L, Salt : OmM6jqjz
	private static final String SALT = "k1rj3ntq";
	private static final String MERCHANT_KEY = "TCg9WT";
	
	//Used for payu sandbox
	private static final String TEST_SALT = "OmM6jqjz";
	private static final String MERCHANT_TEST_KEY = "VPcm4L";
	private static final String PIPE = "|";
	private static final String code = "payu";
    private static final int RET4 = -4;
    private static final String RET4_DESC = "There has been an unexpected exception. Please look at the log.";

	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		String sessionId = null;
		KKAppEng kkAppEng = null;
		String paymentStatus = null;
		String txnId = null;
		
		 // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setOrderId(-1);
        ipnHistory.setModuleCode(code);


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
			false);
			if (token == null) {
				throw new Exception(
						"The SSOToken from the PayPal callback is null");
			}
			sessionId = token.getSessionId();
			try {
				// Get the order id from custom1
				int orderId = Integer.parseInt(token.getCustom1());
				System.out.println("order id in response:"+orderId);
				ipnHistory.setOrderId(orderId);
			} catch (Exception e) {
				throw new Exception("The SSOToken does not contain an order id");
			}

			/*
			 * Use the session of the logged in user to initialize kkAppEng
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
			
			 // Process the parameters sent in the callback
            StringBuffer sb = new StringBuffer();
            Enumeration<?> en = request.getParameterNames();
            while (en.hasMoreElements())
            {
                String paramName = (String) en.nextElement();
                String paramValue = request.getParameter(paramName);
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append(paramName);
                sb.append(" = ");
                sb.append(paramValue);

                // Capture important variables so that we can determine whether the transaction
                // was successful or not
                if (paramName != null)
                {
                    if (paramName.equalsIgnoreCase("status"))
                    {
                        paymentStatus = paramValue;
                    } else if (paramName.equalsIgnoreCase("txnid"))
                    {
                        txnId = paramValue;
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("PayPal CallBack data:");
                log.debug(sb.toString());
            }

            // Fill more details of the IPN history class
            ipnHistory.setGatewayResult(paymentStatus);
            ipnHistory.setGatewayFullResponse(sb.toString());
            ipnHistory.setGatewayTransactionId(txnId);
            ipnHistory.setGatewayCaptureId(request.getParameter("mihpayid"));

            // If successful, we update the inventory as well as changing the state of the
            // order.
            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());
            
            if(paymentStatus.equals(TransactionStatus.SUCCESS.toString()) && isTransactionTamperProof(request)){
	            int orderId = ipnHistory.getOrderId();
	            kkAppEng.getEng().updateOrder(sessionId, orderId,com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, 
	            		/*sendEmail*/true, comment, updateOrder);
	             // If the order payment was approved we update the inventory
	            kkAppEng.getEng().updateInventory(sessionId, orderId);
	            kkAppEng.getEng().sendOrderConfirmationEmail1(sessionId, orderId, /* langIdForOrder */ -1, getEmailOptions(kkAppEng));
	            kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
	            return "CheckoutFinished";
            }
            System.out.println("Payu status:"+paymentStatus+"  Payu Unmapped status:"+ request.getParameter("unmappedstatus"));
            //Transaction failed. Update order status.
            kkAppEng.getEng().updateOrder(sessionId, ipnHistory.getOrderId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /*sendEmail*/ false, comment,
                    updateOrder);
            kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
			return "TransactionFailed";

		} catch (Exception e) {
		            try
		            {
		                if (sessionId != null)
		                {
		                    ipnHistory.setKonakartResultDescription(RET4_DESC);
		                    ipnHistory.setKonakartResultId(RET4);
		                    if (kkAppEng != null)
		                    {
		                        kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
		                    }
		                }
		            } catch (KKException e1)
		            {
		                e1.printStackTrace();
		            }
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
	public Boolean isTransactionTamperProof(HttpServletRequest request) {
		// sha512(SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key)
		String hash = request.getParameter("hash");
		String key = request.getParameter("key");
		StringBuffer hashString = new StringBuffer();
		hashString = hashString.append(getSalt(key)).append(PIPE)
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
				.append(key);
		String calculatedHash = hashCal("SHA-512", hashString.toString());
		System.out.println("hashstring:" + hashString);
		System.out.println("hash:" + hash + " \ncalculated hash:"
				+ calculatedHash);
		return hash.equals(calculatedHash);
	}
	
	private String getSalt(String key){
		if(key == null || key.isEmpty()){
			return null;
		}
		if(key.equals(MERCHANT_KEY)){
			return SALT;
		}
		if(key.equals(MERCHANT_TEST_KEY)){
			return TEST_SALT;
		}
		return null;
	}

}
