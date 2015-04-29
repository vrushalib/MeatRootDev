package com.konakart.al;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.FetchProductOptions;
import com.konakart.appif.FetchProductOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderTotalIf;

/**
 * Callouts to add custom code to the KKAppEng
 */
public class KKAppEngCallouts
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(KKAppEngCallouts.class);

    /**
     * Called at the start of startup
     * 
     * @param eng
     */
    public void beforeStartup(KKAppEng eng)
    {
        // System.out.println("Set product options for current customer");
        FetchProductOptionsIf fpo = new FetchProductOptions();
        fpo.setCalcQuantityForBundles(false);
        // fpo.setCatalogId("cat1");
        // fpo.setUseExternalPrice(true);
        // fpo.setUseExternalQuantity(true);
        eng.setFetchProdOptions(fpo);
    }

    /**
     * Called at the end of startup
     * 
     * @param eng
     */
    public void afterStartup(KKAppEng eng)
    {
    }

    /**
     * Called at the end of the RefreshCachedData method of KKAppEng
     * 
     * @param eng
     */
    public void afterRefreshCaches(KKAppEng eng)
    {
    }

    /**
     * Called by the CustomerMgr after a login has been successful
     * 
     * @param eng
     */
    public void afterLogin(KKAppEng eng)
    {

    }

    /**
     * Called after an order has been populated with order totals so that a verification can be made
     * to determine whether all compulsory modules exist. If shipping or tax modules call an
     * external service, the service may be down and if this is the case, the customer should not be
     * allowed to confirm the order. The logic will probably have to use the delivery address to
     * determine which modules should be present. e.g. USPS for US addresses and USPS International
     * for addresses outside of the US.
     * 
     * You may also implement a backup database driven order total module that only returns a value
     * if the service driven order total module doesn't return a value. In this case it may not be
     * necessary to make the check.
     * 
     * This method may be edited to match your requirements.
     * 
     * @param eng
     * @param order
     * @return Returns true if all the necessary order total modules are present
     */
    public boolean validateOrderTotals(KKAppEng eng, OrderIf order)
    {
        if (order == null)
        {
            return false;
        }

        /*
         * Add the order total modules to a hash map for fast lookup
         */
        HashMap<String, OrderTotalIf> otMap = new HashMap<String, OrderTotalIf>();
        if (order.getOrderTotals() != null)
        {
            for (int i = 0; i < order.getOrderTotals().length; i++)
            {
                OrderTotalIf ot = order.getOrderTotals()[i];
                otMap.put(ot.getClassName(), ot);
            }
        }
        if (order.getDeliveryCountryObject().getIsoCode3().equalsIgnoreCase("USA"))
        {
            if (eng.getConfigAsBoolean("MODULE_ORDER_TOTAL_TAX_CLOUD_STATUS", false, /* tryEngIfNotInCache */
                    false))
            {
                OrderTotalIf taxCloud = otMap.get("ot_tax_cloud");
                if (taxCloud == null)
                {
                    log.error("TaxCloud Module didn't return a value for the address: \n"
                            + order.getDeliveryFormattedAddress());
                    return false;
                }
            }
            if (eng.getConfigAsBoolean("MODULE_SHIPPING_USPS_STATUS", false, /* tryEngIfNotInCache */
                    false))
            {
                OrderTotalIf usps = otMap.get("ot_shipping");
                if (usps == null)
                {
                    log.error("USPS Module didn't return a value for the address: \n"
                            + order.getDeliveryFormattedAddress());
                    return false;
                }
            }
        } else if (order.getDeliveryCountryObject().getIsoCode3().equalsIgnoreCase("CAN"))
        {
            if (eng.getConfigAsBoolean("MODULE_SHIPPING_CANADA_POST_STATUS", false, /* tryEngIfNotInCache */
                    false))
            {
                OrderTotalIf canadaPost = otMap.get("ot_shipping");
                if (canadaPost == null)
                {
                    log.error("Canada Post Module didn't return a value for the address: \n"
                            + order.getDeliveryFormattedAddress());
                    return false;
                }
            }
        } else
        {
            if (eng.getConfigAsBoolean("MODULE_SHIPPING_USPSINT_STATUS", false, /* tryEngIfNotInCache */
                    false))
            {
                OrderTotalIf uspsInt = otMap.get("ot_shipping");
                if (uspsInt == null)
                {
                    log.error("USPS International Module didn't return a value for the address: \n"
                            + order.getDeliveryFormattedAddress());
                    return false;
                }
            }
        }

        if (eng.getConfigAsBoolean("MODULE_SHIPPING_FEDEX_STATUS", false, /* tryEngIfNotInCache */
                false))
        {
            OrderTotalIf fedex = otMap.get("ot_shipping");
            if (fedex == null)
            {
                log.error("Fedex Module didn't return a value for the address: \n"
                        + order.getDeliveryFormattedAddress());
                return false;
            }
        }

        if (eng.getConfigAsBoolean("MODULE_SHIPPING_UPS_STATUS", false, /* tryEngIfNotInCache */
                false))
        {
            OrderTotalIf ups = otMap.get("ot_shipping");
            if (ups == null)
            {
                log.error("UPS Module didn't return a value for the address: \n"
                        + order.getDeliveryFormattedAddress());
                return false;
            }
        }

        /*
         * Check that a shipping module is always present. It should be present even for virtual
         * goods. If this check isn't made we risk not charging shipping for the cases where a
         * destination address may not be covered by any shipping module.
         */
        OrderTotalIf shipping = otMap.get("ot_shipping");
        if (shipping == null)
        {
            log.error("No shipping module found: \n" + order.getDeliveryFormattedAddress());
            return false;
        }
        
        /*
         * Check that a payment method is always present on the order. 
         */
        if (order.getPaymentDetails() == null)
        {
            log.error("No payment method found: \n" + order.getBillingFormattedAddress());
            return false;
        }

        return true;
    }

}
