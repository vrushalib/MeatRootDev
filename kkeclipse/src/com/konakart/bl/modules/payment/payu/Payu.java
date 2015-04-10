package com.konakart.bl.modules.payment.payu;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.PaymentDetails;
import com.konakart.app.SSOToken;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;
import com.konakart.bl.modules.payment.BasePaymentModule;
import com.konakart.bl.modules.payment.PaymentInfo;
import com.konakart.bl.modules.payment.PaymentInterface;

/**
 * PayU Module.
 */
public class Payu extends BasePaymentModule implements PaymentInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "payu";

    private static String bundleName = BaseModule.basePackage + ".payment.payu.Payu";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    private static String mutex = "payuMutex";

    // Configuration Keys
    private final static String MODULE_PAYMENT_PAYU_STATUS = "MODULE_PAYMENT_PAYU_STATUS";

    private final static String MODULE_PAYMENT_PAYU_ORDER_STATUS_ID = "MODULE_PAYMENT_PAYU_ORDER_STATUS_ID";

    private final static String MODULE_PAYMENT_PAYU_SORT_ORDER = "MODULE_PAYMENT_PAYU_SORT_ORDER";
    
    /**
     * This URL is used by payu to redirect the user's browser when returning from the payment
     * gateway after successful transaction.
     */
    private final static String MODULE_PAYMENT_PAYU_SUCCESS_URL = "MODULE_PAYMENT_PAYU_SUCCESS_URL";

    /**
     * This URL is used by payu to redirect the user's browser when returning from the payment
     * gateway after failed transaction. 
     */
    private final static String MODULE_PAYMENT_PAYU_FAILURE_URL = "MODULE_PAYMENT_PAYU_FAILURE_URL";
    
    /**
     * This URL is used by payu to redirect when a user cancels a transaction on payu
     */
    private final static String MODULE_PAYMENT_PAYU_CANCEL_URL = "MODULE_PAYMENT_PAYU_CANCEL_URL";


    /**
     * If set to true, the module will use the test server for payu. Otherwise the live URL will be used.
     */
    private final static String MODULE_PAYMENT_PAYU_TEST_MODE = "MODULE_PAYMENT_PAYU_TEST_MODE";
    
    private final static String MODULE_PAYMENT_PAYU_DROP_CATEGORIES = "MODULE_PAYMENT_PAYU_DROP_CATEGORIES";
    
    // Message Catalogue Keys

    private final static String MODULE_PAYMENT_PAYU_TEXT_TITLE = "module.payment.payu.text.title";

    private final static String MODULE_PAYMENT_PAYU_TEXT_DESCRIPTION = "module.payment.payu.text.description";
    
    /**
     * Used for test payu server
     */
    private static final String PAYU_TEST_MERCHANT_KEY = "VPcm4L";
    private static final String PAYU_TEST_SALT = "OmM6jqjz";
    
    /**
     * Used for actual Payu server
     */
    private static final String PAYU_MERCHANT_KEY = "TCg9WT";
    private static final String PAYU_SALT = "k1rj3ntq";
    
    private static final String PIPE = "|";
    
    
    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws KKException
     */
    public Payu(KKEngIf eng) throws KKException
    {
        super.init(eng);

        StaticData sd = staticDataHM.get(getStoreId());

        if (sd == null)
        {
            synchronized (mutex)
            {
                sd = staticDataHM.get(getStoreId());
                if (sd == null)
                {
                    setStaticVariables();
                }
            }
        }
    }

    /**
     * Sets some static variables during setup
     * 
     * @throws KKException
     * 
     */
    public void setStaticVariables() throws KKException
    {
    	KKConfiguration conf;
        StaticData staticData = staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            staticData = new StaticData();
            staticDataHM.put(getStoreId(), staticData);
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYU_SUCCESS_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYU_SUCCESS_URL must be set to the return URL for"
                            + " the successful transaction.");
        }
        staticData.setPayuSuccessUrl(conf.getValue());

        conf = getConfiguration(MODULE_PAYMENT_PAYU_FAILURE_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_PAYMENT_PAYU_FAILURE_URL must be set to the return URL for"
                            + " for the failed transactions");
        }
        staticData.setPayuFailedUrl(conf.getValue());
        
        conf = getConfiguration(MODULE_PAYMENT_PAYU_CANCEL_URL);
        if (conf != null)
        {
        	staticData.setPayuCancelUrl(conf.getValue());
        }
        
        conf = getConfiguration(MODULE_PAYMENT_PAYU_DROP_CATEGORIES);
        if (conf != null)
        {
            staticData.setPayuDropCategories(conf.getValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYU_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYU_ORDER_STATUS_ID);
        if (conf == null)
        {
            staticData.setOrderStatusId(0);
        } else
        {
            staticData.setOrderStatusId(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_PAYMENT_PAYU_TEST_MODE);
        if (conf == null)
        {
            staticData.setPayuTestMode(true);
            staticData.setPayuMerchantKey(PAYU_TEST_MERCHANT_KEY);
            staticData.setPayuSalt(PAYU_TEST_SALT);
        } else
        {
            if (conf.getValue().equalsIgnoreCase("false"))
            {
                staticData.setPayuTestMode(false);
                staticData.setPayuMerchantKey(PAYU_MERCHANT_KEY);
                staticData.setPayuSalt(PAYU_SALT);
                
            } else
            {
                staticData.setPayuTestMode(true);
                staticData.setPayuMerchantKey(PAYU_TEST_MERCHANT_KEY);
                staticData.setPayuSalt(PAYU_TEST_SALT);
            }
        }
    }

    /**
     * Return a payment details object for PayU
     * @param order
     * @param info
     * @return Returns information in a PaymentDetails object
     * @throws Exception
     */
    public PaymentDetails getPaymentDetails(Order order, PaymentInfo info) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());
        
     // Get the scale for currency calculations
        int scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, info
                .getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        PaymentDetails pDetails = new PaymentDetails();
        pDetails.setCode(code);
        pDetails.setSortOrder(sd.getSortOrder());
        pDetails.setOrderStatusId(sd.getOrderStatusId());
        pDetails.setPaymentType(PaymentDetails.BROWSER_PAYMENT_GATEWAY);
        pDetails.setDescription(rb.getString(MODULE_PAYMENT_PAYU_TEXT_DESCRIPTION));
        pDetails.setTitle(rb.getString(MODULE_PAYMENT_PAYU_TEXT_TITLE));

     // Return now if the full payment details aren't required
        if (info.isReturnDetails() == false)
        {
            return pDetails;
        }

        pDetails.setPostOrGet("post");
        if (sd.isPayuTestMode())
        {
            pDetails.setRequestUrl("https://test.payu.in/_payment");
        } else
        {
            pDetails.setRequestUrl("https://secure.payu.in/_payment");
        }

        List<NameValue> parmList = new ArrayList<NameValue>();
        BigDecimal total = null;
        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
        }

        if (total == null)
        {
            throw new KKException(
                    "An Order Total was not found so the payment could not be processed through Payu.");
        }

        /*
         * Create a session here which will be used by the IPN callback
         */
        SSOTokenIf ssoToken = new SSOToken();
     //   String sessionId = getEng().login("poojabihani11@gmail.com", "princess");//new KKAppEng(getEng().getEngConf()).getSessionId();
        String sessionId = order.getCustom4();//sessionId
        System.out.println("sessionid in payu:"+sessionId);
        if (sessionId == null)
        {
            throw new KKException(
                    "Unable to get sessionId of currently logged in user");
        }
        ssoToken.setSessionId(sessionId);
        ssoToken.setCustom1(String.valueOf(order.getId()));
        /*
         * Save the SSOToken with a valid sessionId and the order id in custom1
         */
        String uuid = getEng().saveSSOToken(ssoToken);
        
        //Mandatory params
        StringBuffer hashString = new StringBuffer("");
        parmList.add(new NameValue("key", sd.getPayuMerchantKey()));
        hashString.append(sd.getPayuMerchantKey()).append(PIPE);
        String txnId = order.getTrackingNumber();
        parmList.add(new NameValue("txnid", txnId));
        hashString.append(txnId).append(PIPE);
        parmList.add(new NameValue("amount", total.toString()));
        hashString.append(total.toString()).append(PIPE);
        String productInfo = getProductsForPayment(order.getOrderProducts());
        parmList.add(new NameValue("productinfo", productInfo));
        hashString.append(productInfo).append(PIPE);
        parmList.add(new NameValue("firstname", order.getDeliveryName()));
        hashString.append(order.getDeliveryName()).append(PIPE);
        parmList.add(new NameValue("email", order.getCustomerEmail()));
        hashString.append(order.getCustomerEmail()).append(PIPE);
        parmList.add(new NameValue("udf1", uuid));
        hashString.append(uuid).append(PIPE);
        hashString.append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE).append(PIPE);
        parmList.add(new NameValue("salt", sd.getPayuSalt()));
        hashString.append(sd.getPayuSalt());
        parmList.add(new NameValue("phone", order.getDeliveryTelephone()));
        parmList.add(new NameValue("surl", sd.getPayuSuccessUrl()));
        parmList.add(new NameValue("furl", sd.getPayuFailedUrl()));
        
        System.out.println("hashstring:"+hashString);
        parmList.add(new NameValue("hash", getHashValue(hashString.toString())));
        //optional params
        parmList.add(new NameValue("curl", sd.getPayuCancelUrl()));
        parmList.add(new NameValue("drop_category", sd.getPayuDropCategories()));
       
        // Put the parameters into an array
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pDetails.setParameters(nvArray);
        System.out.println(pDetails.toString());
        if (log.isDebugEnabled())
        {
            log.debug(pDetails.toString());
        }

        return pDetails;
    }
    
    private String getHashValue(String hashString){
		String hash = hashCal("SHA-512",hashString);
		 System.out.println("hash:"+hash);
		 return hash;
    }
    
    /* Creates a string for product description to send to the payment gateway- productName_quantity_pricePerUnit (separated by space)*/
	private String getProductsForPayment( com.konakart.appif.OrderProductIf [] orderProducts){
		StringBuffer prods = new StringBuffer();
		for(int i = 0; i < orderProducts.length; i++){
			prods.append(orderProducts[i].getName() + "_" + orderProducts[i].getQuantity() + "_" + orderProducts[i].getPrice());
			prods.append(" ");
		}
	//	return prods.substring(0, prods.length()-1); // to remove last comma	
	return prods.toString().trim();
	}
	
	private String hashCal(String type,String str){
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
			System.out.println("No such algorithm as "+type+" exists");
		}
		return hexString.toString();
	}

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_PAYMENT_PAYU_STATUS);
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private int zone;

        private int orderStatusId;
        
        private String payuSuccessUrl;

        private String payuFailedUrl;
        
        private String payuCancelUrl;

        private boolean payuTestMode;
        
        private String payuMerchantKey;
       
        private String payuSalt;
        
        private String payuDropCategories;


        /**
         * @return the sortOrder
         */
        public int getSortOrder()
        {
            return sortOrder;
        }

        /**
         * @param sortOrder
         *            the sortOrder to set
         */
        public void setSortOrder(int sortOrder)
        {
            this.sortOrder = sortOrder;
        }

        /**
         * @return the zone
         */
        public int getZone()
        {
            return zone;
        }

        /**
         * @param zone
         *            the zone to set
         */
        public void setZone(int zone)
        {
            this.zone = zone;
        }

        /**
         * @return the orderStatusId
         */
        public int getOrderStatusId()
        {
            return orderStatusId;
        }

        /**
         * @param orderStatusId
         *            the orderStatusId to set
         */
        public void setOrderStatusId(int orderStatusId)
        {
            this.orderStatusId = orderStatusId;
        }

		/**
		 * @return the payuSuccessUrl
		 */
		public String getPayuSuccessUrl() {
			return payuSuccessUrl;
		}

		/**
		 * @param payuSuccessUrl the payuSuccessUrl to set
		 */
		public void setPayuSuccessUrl(String payuSuccessUrl) {
			this.payuSuccessUrl = payuSuccessUrl;
		}

		/**
		 * @return the payuFailedUrl
		 */
		public String getPayuFailedUrl() {
			return payuFailedUrl;
		}

		/**
		 * @param payuFailedUrl the payuFailedUrl to set
		 */
		public void setPayuFailedUrl(String payuFailedUrl) {
			this.payuFailedUrl = payuFailedUrl;
		}

		/**
		 * @return the payuTestMode
		 */
		public boolean isPayuTestMode() {
			return payuTestMode;
		}

		/**
		 * @param payuTestMode the payuTestMode to set
		 */
		public void setPayuTestMode(boolean payuTestMode) {
			this.payuTestMode = payuTestMode;
		}

		/**
		 * @return the payuCancelUrl
		 */
		public String getPayuCancelUrl() {
			return payuCancelUrl;
		}

		/**
		 * @param payuCancelUrl the payuCancelUrl to set
		 */
		public void setPayuCancelUrl(String payuCancelUrl) {
			this.payuCancelUrl = payuCancelUrl;
		}

		/**
		 * @return the payuMerchantKey
		 */
		public String getPayuMerchantKey() {
			return payuMerchantKey;
		}

		/**
		 * @param payuMerchantKey the payuMerchantKey to set
		 */
		public void setPayuMerchantKey(String payuMerchantKey) {
			this.payuMerchantKey = payuMerchantKey;
		}

		/**
		 * @return the payuSalt
		 */
		public String getPayuSalt() {
			return payuSalt;
		}

		/**
		 * @param payuSalt the payuSalt to set
		 */
		public void setPayuSalt(String payuSalt) {
			this.payuSalt = payuSalt;
		}

		/**
		 * @return the payuDropCategories
		 */
		public String getPayuDropCategories() {
			return payuDropCategories;
		}

		/**
		 * @param payuDropCategories the payuDropCategories to set
		 */
		public void setPayuDropCategories(String payuDropCategories) {
			this.payuDropCategories = payuDropCategories;
		}

		
    }

}