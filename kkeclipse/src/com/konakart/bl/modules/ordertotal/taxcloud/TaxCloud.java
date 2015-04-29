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

package com.konakart.bl.modules.ordertotal.taxcloud;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import net.taxcloud.CartItem;
import net.taxcloud.CartItemResponse;
import net.taxcloud.ExemptionCertificate;
import net.taxcloud.service.AddressService;
import net.taxcloud.service.LookupService;

import org.apache.torque.TorqueException;

import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object based on tax information received from the TaxCloud web
 * service
 */
public class TaxCloud extends BaseOrderTotalModule implements OrderTotalInterface
{
    private static String code = "ot_tax_cloud";

    private static String bundleName = BaseModule.basePackage + ".ordertotal.taxcloud.TaxCloud";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otTaxCloudMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_SORT_ORDER = "MODULE_ORDER_TOTAL_TAX_CLOUD_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_STATUS = "MODULE_ORDER_TOTAL_TAX_CLOUD_STATUS";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_ID = "MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_ID";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_KEY = "MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_KEY";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS1 = "MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS1";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS2 = "MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS2";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_CITY = "MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_CITY";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_STATE = "MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_STATE";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ZIP = "MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ZIP";

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_USPS_USER_ID = "MODULE_ORDER_TOTAL_TAX_CLOUD_USPS_USER_ID";

    // Message Catalog Keys

    private final static String MODULE_ORDER_TOTAL_TAX_CLOUD_TITLE = "module.order.total.taxcloud.title";

    // Shipping TIC
    private static final int SHIPPING_TIC = 11010;

    private static final String SHIPPING_TIC_STR = "11010";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
     * 
     */
    public TaxCloud(KKEngIf eng) throws TorqueException, KKException, DataSetException
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

        conf = getConfiguration(MODULE_ORDER_TOTAL_TAX_CLOUD_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        staticData.setLoginId(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_ID));
        staticData.setLoginKey(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_API_LOGIN_KEY));
        staticData.setAddress1(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS1));
        staticData.setAddress2(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ADDRESS2));
        staticData.setCity(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_CITY));
        staticData.setState(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_STATE));
        staticData.setZip(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_STORE_ZIP));
        staticData.setUspsId(getConfigurationValue(MODULE_ORDER_TOTAL_TAX_CLOUD_USPS_USER_ID));

        // Verify and cache the origin address
        net.taxcloud.Address origin = new net.taxcloud.Address();
        origin.setAddress1(staticData.getAddress1());
        origin.setAddress2(staticData.getAddress2());
        origin.setCity(staticData.getCity());
        origin.setState(staticData.getState());
        origin.setZip5(staticData.getZip());
        net.taxcloud.Address originVerified = verifyAddress(staticData, origin);
        if (originVerified == null)
        {
            throw new KKException(
                    "The store address for store "
                            + getStoreId()
                            + " could not be verified using the USPS verification service probably due to a communications problem.");
        }
        validateAddress(origin, originVerified);
        staticData.setOriginAddr(originVerified);
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_ORDER_TOTAL_TAX_CLOUD_STATUS);
    }

    /**
     * Create and return an OrderTotal object for the tax amount.
     * 
     * @param order
     * @param dispPriceWithTax
     * @param locale
     * @return Returns an OrderTotal object for this module
     * @throws Exception
     */
    public OrderTotal getOrderTotal(Order order, boolean dispPriceWithTax, Locale locale)
            throws Exception
    {
        OrderTotal ot;
        StaticData sd = staticDataHM.get(getStoreId());

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, locale);
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + locale.getCountry());
        }
        ot = new OrderTotal();
        ot.setSortOrder(sd.getSortOrder());
        ot.setClassName(code);

        // Call the TaxCloud service
        callTaxCloud(sd, order, ot);

        // Set the title of the order total
        StringBuffer title = new StringBuffer();
        title.append(rb.getString(MODULE_ORDER_TOTAL_TAX_CLOUD_TITLE));
        title.append(":");
        ot.setTitle(title.toString());

        return ot;
    }

    /**
     * Call the TaxCloud tax service which calculates the total tax for the order (including
     * shipping) and populates the Order Total and Order objects with the tax information.
     * 
     * @param sd
     * @param order
     * @param ot
     * @throws Exception
     * @throws DataSetException
     * @throws TorqueException
     */
    private void callTaxCloud(StaticData sd, Order order, OrderTotal ot) throws TorqueException,
            DataSetException, Exception
    {

        // Look up tax rates from TaxCloud
        LookupService lookupService = new LookupService();

        String apiLoginID = sd.getLoginId();
        String apiKey = sd.getLoginKey();
        String customerId = String.valueOf(order.getCustomerId());
        String cartId = order.getLifecycleId();

        // Figure out whether there is a shipping charge
        boolean isShipping = false;
        ShippingQuoteIf shippingQuote = order.getShippingQuote();
        if (shippingQuote != null && shippingQuote.getTotalExTax() != null
                && shippingQuote.getTotalExTax().compareTo(new BigDecimal(0)) == 1)
        {
            isShipping = true;
        }

        /*
         * Create an array of cart items to send to TaxCloud. Every item needs a TIC (Taxability
         * Information Code) which is contained in each order product object in the TaxCode
         * attribute. If we need to calculate tax for shipping we make the CartItem array longer by
         * one.
         */
        int arrayLength = (isShipping) ? order.getOrderProducts().length + 1 : order
                .getOrderProducts().length;
        CartItem[] cartItems = new CartItem[arrayLength];
        int cartIndex = 0;
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProductIf orderProduct = order.getOrderProducts()[i];
            CartItem cartItem = new CartItem();
            cartItem.setIndex(cartIndex);
            cartItem.setItemID(String.valueOf(orderProduct.getProductId()));
            if (orderProduct.getTaxCode() == null || orderProduct.getTaxCode().length() == 0)
            {
                throw new KKException("The TaxClass object for the product with id = "
                        + orderProduct.getProductId()
                        + " does not have a TIC (Taxability Information Code) set.");
            }
            int ticID;
            try
            {
                ticID = Integer.parseInt(orderProduct.getTaxCode());
            } catch (Exception e)
            {
                throw new KKException(
                        "The TaxClass object for the product with id = "
                                + orderProduct.getProductId()
                                + " does not have a numeric TIC (Taxability Information Code) set. The value found = "
                                + orderProduct.getTaxCode() + " is invalid.");
            }
            cartItem.setTIC(ticID);

            // Must call TaxCloud with final price of a single product
            int scale = getTaxMgr().getTaxScale();
            BigDecimal singleProdPrice = (orderProduct.getFinalPriceExTax()).divide(new BigDecimal(
                    orderProduct.getQuantity()), scale, BigDecimal.ROUND_HALF_UP);
            cartItem.setPrice(singleProdPrice.floatValue());
            cartItem.setQty(orderProduct.getQuantity());
            cartItems[cartIndex] = cartItem;
            cartIndex++;
        }

        if (isShipping)
        {
            // Add shipping charges to the cart (shipping charges are taxable)
            CartItem cartItem = new CartItem();
            cartItem.setIndex(cartIndex);
            cartItem.setItemID(SHIPPING_TIC_STR);
            cartItem.setTIC(SHIPPING_TIC);
            cartItem.setPrice(shippingQuote.getTotalExTax().floatValue());
            cartItem.setQty(1);
            cartItems[cartIndex] = cartItem;
        }

        net.taxcloud.Address destination = new net.taxcloud.Address();
        destination.setAddress1(order.getDeliveryStreetAddress());
        destination.setAddress2("");
        destination.setCity(order.getDeliveryCity());
        destination.setState(order.getDeliveryState());
        destination.setZip5(order.getDeliveryPostcode());
        net.taxcloud.Address destinationVerified = verifyAddress(sd, destination);
        if (destinationVerified == null)
        {
            throw new KKException(
                    "The destination address could not be verified using the USPS verification service probably due to a communications problem.");
        }
        validateAddress(destination, destinationVerified);

        // Now set the verified address back into the Order
        order.setDeliveryStreetAddress(destinationVerified.getAddress1());
        order.setDeliveryCity(destinationVerified.getCity());
        order.setDeliveryState(destinationVerified.getState());
        order.setDeliveryPostcode(destinationVerified.getZip5() + "-"
                + destinationVerified.getZip4());

        boolean deliveredBySeller = true;
        ExemptionCertificate exemptionCertificate = null;

        CartItemResponse[] cartItemsResponse = null;
        try
        {
            if (log.isDebugEnabled())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("Calling tax cloud tax service with the following data:\n");
                sb.append("apiLoginID =  ").append(apiLoginID).append("\n");
                sb.append("apiKey =      ").append(apiKey).append("\n");
                sb.append("customerId =  ").append(customerId).append("\n");
                sb.append("cartId =      ").append(cartId).append("\n");
                if (sd.getOriginAddr() != null)
                {
                    sb.append("Store Address:\n");
                    sb.append("Address1 =    ").append(sd.getOriginAddr().getAddress1())
                            .append("\n");
                    sb.append("Address2 =    ").append(sd.getOriginAddr().getAddress2())
                            .append("\n");
                    sb.append("City =        ").append(sd.getOriginAddr().getCity()).append("\n");
                    sb.append("State =       ").append(sd.getOriginAddr().getState()).append("\n");
                    sb.append("Zip4 =        ").append(sd.getOriginAddr().getZip4()).append("\n");
                    sb.append("Zip5 =        ").append(sd.getOriginAddr().getZip5()).append("\n");
                }
                sb.append("Delivery Address:\n");
                sb.append("Address1 =    ").append(destinationVerified.getAddress1()).append("\n");
                sb.append("Address2 =    ").append(destinationVerified.getAddress2()).append("\n");
                sb.append("City =        ").append(destinationVerified.getCity()).append("\n");
                sb.append("State =       ").append(destinationVerified.getState()).append("\n");
                sb.append("Zip4 =        ").append(destinationVerified.getZip4()).append("\n");
                sb.append("Zip5 =        ").append(destinationVerified.getZip5()).append("\n");

                sb.append("CartItems:\n");
                for (int i = 0; i < cartItems.length; i++)
                {
                    CartItem item = cartItems[i];
                    sb.append("ItemID =        ").append(item.getItemID()).append("\n");
                    sb.append("TIC =           ").append(item.getTIC()).append("\n");
                    sb.append("Qty =           ").append(item.getQty()).append("\n");
                    sb.append("Price =         ").append(item.getPrice()).append("\n");
                }
                log.debug(sb);
            }
            // Call TaxCloud to look up tax rates
            cartItemsResponse = lookupService.lookup(apiLoginID, apiKey, customerId, cartId,
                    cartItems, sd.getOriginAddr(), destinationVerified, deliveredBySeller,
                    exemptionCertificate);
        } catch (Exception e)
        {
            log.error("Problem calling TaxCloud: ", e);
        }
        if (cartItemsResponse == null || cartItemsResponse.length == 0)
        {
            throw new KKException(
                    "TaxCloud returned an empty response from the lookupService.lookup() API call.");
        }

        if (log.isDebugEnabled())
        {
            log.debug("Response from TaxCloud lookup:");
            for (int i = 0; i < cartItemsResponse.length; i++)
            {
                CartItemResponse r = cartItemsResponse[i];
                log.debug("CartItem [index=" + r.getCartItemIndex() + ", tax=" + r.getTaxAmount()
                        + "]");
            }
        }

        float taxAmount = 0;
        for (int i = 0; i < cartItemsResponse.length - 1; i++)
        {
            CartItemResponse cartItemResponse = cartItemsResponse[i];
            taxAmount = taxAmount + cartItemResponse.getTaxAmount();
        }

        // Now get the tax amount for the shipping charges
        CartItemResponse cartItemResponse = cartItemsResponse[cartItemsResponse.length - 1];
        taxAmount = taxAmount + cartItemResponse.getTaxAmount();

        // Set the Order Total with the total tax amount
        BigDecimal taxAmountBD = new BigDecimal(taxAmount);
        ot.setValue(taxAmountBD);
        ot.setText(getCurrMgr().formatPrice(taxAmountBD, order.getCurrencyCode()));

        // Set the order with tax information
        order.setTax(taxAmountBD);
        order.setTotalIncTax(order.getTotalExTax().add(taxAmountBD));
    }

    /**
     * Checks that USPS could validate the address and throw an exception if it couldn't.
     * 
     * @param addr
     * @throws KKException
     */
    void validateAddress(net.taxcloud.Address addrIn, net.taxcloud.Address addrOut)
            throws KKException
    {
        StringBuffer msg = new StringBuffer();
        if (addrOut.getAddress1() == null)
        {
            msg.append("USPS could not verify the address: " + addrIn.getAddress1() + "\n");
        }
        if (addrOut.getCity() == null)
        {
            msg.append("USPS could not verify the city: " + addrIn.getCity() + "\n");
        }
        if (addrOut.getState() == null)
        {
            msg.append("USPS could not verify the state: " + addrIn.getState() + "\n");
        }
        if (addrOut.getZip4() == null || addrOut.getZip5() == null)
        {
            msg.append("USPS could not verify the zip: " + addrIn.getZip5() + "\n");
        }
        if (msg.length() > 0)
        {
            msg.insert(0, "\n");
            throw new KKException(msg.toString());
        }
    }

    private net.taxcloud.Address verifyAddress(StaticData sd, net.taxcloud.Address address)
    {

        String uspsUserID = sd.getUspsId();
        AddressService addressService = new AddressService();

        try
        {
            address = addressService.verifyAddress(uspsUserID, address);
        } catch (Exception e)
        {
            log.error("Error in verifyAddress: ", e);
        }
        return address;
    }

    /**
     * Commit the Order transaction - Call TaxCloud to authorize and capture the transaction
     * 
     * @param order
     * @throws Exception
     *             if something unexpected happens
     */
    public void commitOrder(OrderIf order) throws Exception
    {
        try
        {
            StaticData sd = staticDataHM.get(getStoreId());

            // Call TaxCloud to authorize and capture the transaction

            net.taxcloud.service.AuthorizedService authorizedService = new net.taxcloud.service.AuthorizedService();
            String apiLoginId = sd.getLoginId();
            String apiKey = sd.getLoginKey();
            String customerId = String.valueOf(order.getCustomerId());

            // Unique ID for cart generated when order was created
            String cartId = order.getLifecycleId();

            // Unique ID for order
            String orderId = String.valueOf(order.getId());

            Calendar dateAuthorized = Calendar.getInstance();

            boolean authorized = authorizedService.authorized(apiLoginId, apiKey, customerId,
                    cartId, orderId, dateAuthorized);

            if (log.isDebugEnabled())
            {
                log.debug("Authorized returned: " + authorized + " for\n customerId = "
                        + customerId + "\n cartId = " + cartId + "\n orderId = " + orderId);
            }

            if (authorized)
            {
                net.taxcloud.service.CapturedService capturedService = new net.taxcloud.service.CapturedService();
                boolean captured = capturedService.captured(apiLoginId, apiKey, orderId);
                log.debug("Captured returned: " + captured + " for orderId = " + orderId);
            }
        } catch (Exception e)
        {
            log.warn("Problem authorizing / capturing TaxCloud transaction");
            e.printStackTrace();
        }
    }

    public int getSortOrder()
    {
        StaticData sd;
        try
        {
            sd = staticDataHM.get(getStoreId());
            return sd.getSortOrder();
        } catch (KKException e)
        {
            log.error("Can't get the store id", e);
            return 0;
        }
    }

    public String getCode()
    {
        return code;
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private net.taxcloud.Address originAddr;

        private int sortOrder = -1;

        private String loginId;

        private String loginKey;

        private String address1;

        private String address2;

        private String city;

        private String state;

        private String zip;

        private String uspsId;

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
         * @return the loginId
         */
        public String getLoginId()
        {
            return loginId;
        }

        /**
         * @param loginId
         *            the loginId to set
         */
        public void setLoginId(String loginId)
        {
            this.loginId = loginId;
        }

        /**
         * @return the loginKey
         */
        public String getLoginKey()
        {
            return loginKey;
        }

        /**
         * @param loginKey
         *            the loginKey to set
         */
        public void setLoginKey(String loginKey)
        {
            this.loginKey = loginKey;
        }

        /**
         * @return the address1
         */
        public String getAddress1()
        {
            return address1;
        }

        /**
         * @param address1
         *            the address1 to set
         */
        public void setAddress1(String address1)
        {
            this.address1 = address1;
        }

        /**
         * @return the address2
         */
        public String getAddress2()
        {
            return address2;
        }

        /**
         * @param address2
         *            the address2 to set
         */
        public void setAddress2(String address2)
        {
            this.address2 = address2;
        }

        /**
         * @return the city
         */
        public String getCity()
        {
            return city;
        }

        /**
         * @param city
         *            the city to set
         */
        public void setCity(String city)
        {
            this.city = city;
        }

        /**
         * @return the state
         */
        public String getState()
        {
            return state;
        }

        /**
         * @param state
         *            the state to set
         */
        public void setState(String state)
        {
            this.state = state;
        }

        /**
         * @return the zip
         */
        public String getZip()
        {
            return zip;
        }

        /**
         * @param zip
         *            the zip to set
         */
        public void setZip(String zip)
        {
            this.zip = zip;
        }

        /**
         * @return the uspsId
         */
        public String getUspsId()
        {
            return uspsId;
        }

        /**
         * @param uspsId
         *            the uspsId to set
         */
        public void setUspsId(String uspsId)
        {
            this.uspsId = uspsId;
        }

        /**
         * @return the originAddr
         */
        public net.taxcloud.Address getOriginAddr()
        {
            return originAddr;
        }

        /**
         * @param originAddr
         *            the originAddr to set
         */
        public void setOriginAddr(net.taxcloud.Address originAddr)
        {
            this.originAddr = originAddr;
        }
    }
}
