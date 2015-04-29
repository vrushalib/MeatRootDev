//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.bl.modules.ordertotal.thomson;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.torque.TorqueException;

import com.konakart.app.Country;
import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderTotal;
import com.konakart.app.Zone;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.ordertotal.BaseOrderTotalModule;
import com.konakart.bl.modules.ordertotal.OrderTotalInterface;
import com.konakart.util.Utils;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataInvoiceType;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.IndataType;
import com.sabrix.services.taxcalculationservice._2011_09_01.MessageType;
import com.sabrix.services.taxcalculationservice._2011_09_01.OutdataLineType;
import com.sabrix.services.taxcalculationservice._2011_09_01.QuantitiesType;
import com.sabrix.services.taxcalculationservice._2011_09_01.QuantityType;
import com.sabrix.services.taxcalculationservice._2011_09_01.RegistrationsType;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationRequest;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationResponse;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationService;
import com.sabrix.services.taxcalculationservice._2011_09_01.TaxCalculationService_Service;
import com.sabrix.services.taxcalculationservice._2011_09_01.VersionType;
import com.sabrix.services.taxcalculationservice._2011_09_01.ZoneAddressType;
import com.workingdogs.village.DataSetException;

/**
 * Module that creates an OrderTotal object based on tax information received from the Avalara web
 * service
 */
public class Thomson extends BaseOrderTotalModule implements OrderTotalInterface
{
    /**
     * Module code
     */
    public static String code = "Thomson";

    private static String bundleName = BaseModule.basePackage + ".ordertotal.thomson.Thomson";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "otThomsonMutex";

    private static SimpleDateFormat thomsonDateFormat = new SimpleDateFormat("yyyyMMdd");

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_ORDER_TOTAL_THOMSON_BODY_PASSWORD = "MODULE_ORDER_TOTAL_THOMSON_BODY_PASSWORD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_BODY_SECURE = "MODULE_ORDER_TOTAL_THOMSON_BODY_SECURE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_BODY_USERNAME = "MODULE_ORDER_TOTAL_THOMSON_BODY_USERNAME";

    private final static String MODULE_ORDER_TOTAL_THOMSON_CALLING_SYS = "MODULE_ORDER_TOTAL_THOMSON_CALLING_SYS";

    private final static String MODULE_ORDER_TOTAL_THOMSON_COMMIT_STATUS = "MODULE_ORDER_TOTAL_THOMSON_COMMIT_STATUS";

    /** */
    public final static String MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD = "MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_COMPANY_NAME = "MODULE_ORDER_TOTAL_THOMSON_COMPANY_NAME";

    private final static String MODULE_ORDER_TOTAL_THOMSON_COMPANY_ROLE = "MODULE_ORDER_TOTAL_THOMSON_COMPANY_ROLE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CODE_FIELD = "MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CODE_FIELD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CLASS = "MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CLASS";

    private final static String MODULE_ORDER_TOTAL_THOMSON_EXT_COMPANY_ID = "MODULE_ORDER_TOTAL_THOMSON_EXT_COMPANY_ID";

    /** Exemptions code Config variable */
    public final static String MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD = "MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD";

    /** MOSS code Config variable */
    public final static String MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD = "MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_PASSWORD = "MODULE_ORDER_TOTAL_THOMSON_PASSWORD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SAVE_MSG_DB = "MODULE_ORDER_TOTAL_THOMSON_SAVE_MSG_DB";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SECURE = "MODULE_ORDER_TOTAL_THOMSON_SECURE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_CITY = "MODULE_ORDER_TOTAL_THOMSON_SELLER_CITY";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTY = "MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTY";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTRY = "MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTRY";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_ROLE = "MODULE_ORDER_TOTAL_THOMSON_SELLER_ROLE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_POSTCODE = "MODULE_ORDER_TOTAL_THOMSON_SELLER_POSTCODE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_PROVINCE = "MODULE_ORDER_TOTAL_THOMSON_SELLER_PROVINCE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_STATE = "MODULE_ORDER_TOTAL_THOMSON_SELLER_STATE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SELLER_STREET = "MODULE_ORDER_TOTAL_THOMSON_SELLER_STREET";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_CITY = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_CITY";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTY = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTY";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTRY = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTRY";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_PROVINCE = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_PROVINCE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_POSTCODE = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_POSTCODE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STATE = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STATE";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STREET = "MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STREET";

    private final static String MODULE_ORDER_TOTAL_THOMSON_SORT_ORDER = "MODULE_ORDER_TOTAL_THOMSON_SORT_ORDER";

    private final static String MODULE_ORDER_TOTAL_THOMSON_STATUS = "MODULE_ORDER_TOTAL_THOMSON_STATUS";

    private final static String MODULE_ORDER_TOTAL_THOMSON_TAX_IDENTIFIER_FIELD = "MODULE_ORDER_TOTAL_THOMSON_TAX_IDENTIFIER_FIELD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_TAX_URL = "MODULE_ORDER_TOTAL_THOMSON_TAX_URL";

    // Message Catalog Keys

    private final static String MODULE_ORDER_TOTAL_THOMSON_TITLE = "module.order.total.thomson.title";

    /** Transaction Type Config variable */
    public final static String MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD = "MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD";

    // Message Catalog Keys

    /** US Commodity Code Config variable */
    public final static String MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD = "MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD";

    private final static String MODULE_ORDER_TOTAL_THOMSON_USERNAME = "MODULE_ORDER_TOTAL_THOMSON_USERNAME";

    /** VAT Included Config variable */
    public final static String MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD = "MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD";

    private final String POINT_OF_TITLE_TRANSFER = "I";

    /**
     * Constructor
     * 
     * @param eng
     *            KKEngIf engine
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
     */
    public Thomson(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
     * get Sort Order of module
     */
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
     * Clear all caches and set some static variables during setup
     * 
     * @throws KKException
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

        // Clear our caches
        staticData.emptyCountryMap();
        staticData.emptyZoneMap();

        conf = getConfiguration(MODULE_ORDER_TOTAL_THOMSON_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        staticData.setCompanyName(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_COMPANY_NAME));
        staticData.setCompanyRole(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_COMPANY_ROLE));
        staticData
                .setExternalCompanyId(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_EXT_COMPANY_ID));
        staticData.setSellerRole(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_ROLE));

        staticData
                .setCustomClassName(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CLASS));

        staticData.setCustomClass(getCustomClass(staticData.getCustomClassName()));

        staticData.setUsername(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_USERNAME));
        staticData.setPassword(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_PASSWORD));
        staticData.setSecure(getConfigurationValueAsBool(MODULE_ORDER_TOTAL_THOMSON_SECURE, false));

        staticData.setBodyUsername(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_BODY_USERNAME));
        staticData.setBodyPassword(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_BODY_PASSWORD));
        staticData.setBodySecure(getConfigurationValueAsBool(MODULE_ORDER_TOTAL_THOMSON_BODY_SECURE, false));

        staticData.setSaveMsgDb(getConfigurationValueAsBool(MODULE_ORDER_TOTAL_THOMSON_SAVE_MSG_DB,
                true));

        staticData.setCommitStatus(getConfigurationValueAsBool(
                MODULE_ORDER_TOTAL_THOMSON_COMMIT_STATUS, false));

        staticData.setCustomCodeCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_CUSTOM_CODE_FIELD, -1));

        staticData.setUsCommodityCodeCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD, 1));
        staticData.setCommodityCodeCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD, 2));
        staticData.setMossCodeCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD, 3));
        staticData.setTrantypeCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD, 4));
        staticData.setVatIncludedCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD, 5));
        staticData.setExemptionsCustomField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD, 5));

        staticData.setTaxIdField(getConfigurationValueAsIntWithDefault(
                MODULE_ORDER_TOTAL_THOMSON_TAX_IDENTIFIER_FIELD, 0));

        staticData.setStreet(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_STREET));
        staticData.setCounty(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTY));
        staticData.setState(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_STATE));
        staticData.setProvince(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_PROVINCE));
        staticData.setCity(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_CITY));
        staticData.setPostcode(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_POSTCODE));
        staticData.setCountry(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SELLER_COUNTRY));

        staticData
                .setShipFromStreet(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STREET));
        staticData
                .setShipFromCounty(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTY));
        staticData
                .setShipFromState(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_STATE));
        staticData
                .setShipFromProvince(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_PROVINCE));
        staticData.setShipFromCity(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_CITY));
        staticData
                .setShipFromPostcode(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_POSTCODE));
        staticData
                .setShipFromCountry(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_SHIPFROM_COUNTRY));

        staticData.setCallingSys(getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_CALLING_SYS));

        staticData.setDefaultCurrency(getConfigurationValue("DEFAULT_CURRENCY"));

        // Set up Tax Service WSDL URL
        String taxServiceUrl = getConfigurationValue(MODULE_ORDER_TOTAL_THOMSON_TAX_URL);
        try
        {
            staticData.setTaxServiceEndpoint(new URL(taxServiceUrl));
        } catch (MalformedURLException e)
        {
            log.warn("Unable to set up Tax Service Endpoint using URL : " + taxServiceUrl + " : "
                    + e.getMessage());
            e.printStackTrace();
        }

        // Verify and cache the two defined addresses

        ZoneAddressType sellerAddr = new ZoneAddressType();

        sellerAddr.setADDRESS1(staticData.getStreet());
        sellerAddr.setCITY(staticData.getCity());
        sellerAddr.setCOUNTRY(staticData.getCountry());
        sellerAddr.setPOSTCODE(staticData.getPostcode());

        if (!Utils.isBlank(staticData.getCounty()))
        {
            sellerAddr.setCOUNTY(staticData.getCounty());
        }

        if (!Utils.isBlank(staticData.getState()))
        {
            sellerAddr.setSTATE(staticData.getState());
        }

        if (!Utils.isBlank(staticData.getProvince()))
        {
            sellerAddr.setPROVINCE(staticData.getProvince());
        }

        staticData.setSellerAddr(sellerAddr);

        // Now the SHIPFROM address

        ZoneAddressType shipFromAddr = new ZoneAddressType();

        shipFromAddr.setADDRESS1(staticData.getShipFromStreet());
        shipFromAddr.setCITY(staticData.getShipFromCity());
        shipFromAddr.setCOUNTRY(staticData.getShipFromCountry());
        shipFromAddr.setPOSTCODE(staticData.getShipFromPostcode());

        if (!Utils.isBlank(staticData.getShipFromCounty()))
        {
            shipFromAddr.setCOUNTY(staticData.getShipFromCounty());
        }

        if (!Utils.isBlank(staticData.getShipFromState()))
        {
            shipFromAddr.setSTATE(staticData.getShipFromState());
        }

        if (!Utils.isBlank(staticData.getShipFromProvince()))
        {
            shipFromAddr.setPROVINCE(staticData.getShipFromProvince());
        }

        staticData.setShipFromAddr(shipFromAddr);
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_ORDER_TOTAL_THOMSON_STATUS);
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

        // Call the Thomson Reuters service - but don't commit here

        boolean commitOrder = false;
        callThomsonReuters(sd, order, ot, commitOrder);

        // Set the title of the order total
        StringBuffer title = new StringBuffer();
        title.append(rb.getString(MODULE_ORDER_TOTAL_THOMSON_TITLE));
        title.append(":");
        ot.setTitle(title.toString());

        return ot;
    }

    /**
     * Commit the Order transaction
     * 
     * @param order
     * @throws Exception
     *             if something unexpected happens
     */
    public void commitOrder(OrderIf order) throws Exception
    {
        if (!getConfigurationValueAsBool(MODULE_ORDER_TOTAL_THOMSON_COMMIT_STATUS, false))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Thomson commit status is false - so no commit will be executed");
            }
            return;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Start of Thomson commit");
        }

        StaticData sd = staticDataHM.get(getStoreId());

        // Call the service - and commit it
        boolean commitOrder = true;
        callThomsonReuters(sd, (Order) order, null, commitOrder);
    }

    /**
     * Call the Thomson Reuters tax service which calculates the total tax for the order (including
     * shipping) and populates the Order Total and Order objects with the tax information.
     * 
     * @param sd
     * @param order
     * @param ot
     * @param commitOrder
     *            true if we should commit this Order
     * @throws Exception
     * @throws DataSetException
     * @throws TorqueException
     */
    private void callThomsonReuters(StaticData sd, Order order, OrderTotal ot, boolean commitOrder)
            throws TorqueException, DataSetException, Exception
    {
        URL taxServiceEndpoint = sd.getTaxServiceEndpoint();
        String customerId = String.valueOf(order.getCustomerId());
        CustomerIf customer = getCustMgr().getCustomerForId(order.getCustomerId());
        // String cartId = order.getLifecycleId();

        // Figure out whether there is a shipping charge
        boolean isShipping = false;
        ShippingQuoteIf shippingQuote = order.getShippingQuote();
        if (shippingQuote != null && shippingQuote.getTotalExTax() != null
                && shippingQuote.getTotalExTax().compareTo(new BigDecimal(0)) == 1)
        {
            isShipping = true;
        }

        String currCode = order.getCurrencyCode();

        String debugProperty = log.isDebugEnabled() ? "true" : "false";
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
                debugProperty);
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump",
                debugProperty);
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", debugProperty);
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", debugProperty);

        if (taxServiceEndpoint == null)
        {
            throw new KKException(
                    "TaxCalculationService Endpoint has not been successfully defined");
        }

        QName wsQName = new QName(
                "http://www.sabrix.com/services/taxcalculationservice/2011-09-01",
                "TaxCalculationService");

        TaxCalculationService_Service taxServiceObject = new TaxCalculationService_Service(
                taxServiceEndpoint, wsQName);

        HeaderHandlerResolver hhResolver = new HeaderHandlerResolver();

        hhResolver.setSecure(sd.isSecure());
        hhResolver.setUName(sd.getUsername());
        hhResolver.setPWord(sd.getPassword());
        hhResolver.setEng(getEng());
        hhResolver.setOrder(order);
        hhResolver.setTxType(commitOrder ? "Audited" : "Not-Audited");
        hhResolver.setMultiStoreMgr(getMultiStoreMgr());
        hhResolver.setSd(sd);

        taxServiceObject.setHandlerResolver(hhResolver);

        TaxCalculationService taxServicePort = taxServiceObject.getTaxCalculationServicePort();

        TaxCalculationRequest request = new TaxCalculationRequest();

        long calcStart = System.currentTimeMillis();

        IndataType inData = new IndataType();

        inData.setVersion(VersionType.G);

        if (sd.isBodySecure())
        {
            inData.setUSERNAME(sd.getBodyUsername());
            inData.setPASSWORD(sd.getBodyPassword());
        }
        
        IndataInvoiceType invoice = new IndataInvoiceType();

        invoice.setEXTERNALCOMPANYID(sd.getExternalCompanyId());
        invoice.setCOMPANYNAME(sd.getCompanyName());
        invoice.setCOMPANYROLE(sd.getCompanyRole());

        invoice.setHOSTSYSTEM("KONAKART");
        invoice.setCALLINGSYSTEMNUMBER(sd.getCallingSys());
        // invoice.setDOCUMENTTYPE("UNKNOWN");

        Country deliveryCountry = getCountry(sd, order.getDeliveryCountry());
        Zone deliveryZone = null;

        if (deliveryCountry != null && !Utils.isBlank(order.getDeliveryState()))
        {
            deliveryZone = getZone(sd, order.getDeliveryState(), deliveryCountry.getId());
        }

        ZoneAddressType shipToAddr = sd.getCustomClass().getShipToAddress(sd, order,
                deliveryCountry, deliveryZone);

        Country billingCountry = getCountry(sd, order.getBillingCountry());
        Zone billingZone = null;

        if (billingCountry != null && !Utils.isBlank(order.getBillingState()))
        {
            billingZone = getZone(sd, order.getBillingState(), billingCountry.getId());
        }

        ZoneAddressType billToAddr = sd.getCustomClass().getBillToAddress(sd, order,
                billingCountry, billingZone);

        logAddress("Bill To Address", billToAddr);
        logAddress("Ship To Address", shipToAddr);

        invoice.setBILLTO(billToAddr);
        invoice.setBUYERPRIMARY(billToAddr);

        if (commitOrder)
        {
            invoice.setISAUDITED("true");
            invoice.setISREPORTED("true");
        } else
        {
            invoice.setISAUDITED("false");
            invoice.setISREPORTED("false");
        }
        invoice.setISREVERSED("false");

        invoice.setCALCULATIONDIRECTION("F");

        if (!Utils.isBlank(currCode))
        {               
            invoice.setCURRENCYCODE(currCode);
        } else
        {
            invoice.setCURRENCYCODE(sd.getDefaultCurrency());  
        }

        invoice.setCUSTOMERNAME(order.getCustomerName());
        invoice.setCUSTOMERNUMBER(customerId);
        // invoice.setDELIVERYTERMS("UNKNOWN");

        if (!Utils.isBlank(order.getOrderNumber()))
        {
            invoice.setINVOICENUMBER(order.getOrderNumber());
        } else if (order.getId() > 0)
        {
            invoice.setINVOICENUMBER(String.valueOf(order.getId()));
        } else
        {
            invoice.setINVOICENUMBER(String.valueOf(order.getLifecycleId()));
        }

        invoice.setINVOICEDATE(getInvoiceDate(order));
        invoice.setFISCALDATE(getInvoiceDate(order));
        // invoice.setTRANSACTIONTYPE("GS"); // GS = Goods

        String buyerVAT = getBuyerRole(sd, customer);

        // Process each line in the Order
        int cartIndex = 1;
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProductIf orderProduct = order.getOrderProducts()[i];
            IndataLineType line = new IndataLineType();

            line.setLINENUMBER(new BigDecimal(cartIndex));
            line.setID(String.valueOf(cartIndex));

            String partNumber = getPartNumber(sd, orderProduct);
            if (!Utils.isBlank(partNumber))
            {
                line.setPARTNUMBER(partNumber);
            }

            String commodityCode = getCommodityCode(sd, orderProduct);
            if (!Utils.isBlank(commodityCode))
            {
                line.setCOMMODITYCODE(commodityCode);
            }

            String usCommodityCode = getUSCommodityCode(sd, orderProduct);
            if (!Utils.isBlank(usCommodityCode))
            {
                line.setPRODUCTCODE(usCommodityCode);
            }

            sd.getCustomClass().addUserElements(sd, orderProduct, line);

            line.setSHIPFROM(sd.getShipFromAddr());
            line.setSELLERPRIMARY(sd.getSellerAddr());

            line.setDESCRIPTION(orderProduct.getName());
            line.setGROSSAMOUNT(sd.getCustomClass().getGROSS_AMOUNTForProduct(sd, orderProduct,
                    order));
            line.setBUYERPRIMARY(billToAddr);
            line.setSUPPLY(billToAddr);
            line.setSHIPTO(shipToAddr);
            line.setDELIVERYTERMS("");

            RegistrationsType regn = new RegistrationsType();
            regn.getSELLERROLE().add(sd.getSellerRole());
            // regn.getMIDDLEMANROLE().add("UNKNOWN");

            if (!Utils.isBlank(buyerVAT))
            {
                regn.getBUYERROLE().add(buyerVAT);
            }

            line.setREGISTRATIONS(regn);
            // line.setCOUNTRYOFORIGIN("UNKNOWN");
            line.setPOINTOFTITLETRANSFER(POINT_OF_TITLE_TRANSFER);

            line.setTRANSACTIONTYPE(getTransType(sd, orderProduct));

            QuantityType qty = new QuantityType();
            qty.setAMOUNT(String.valueOf(orderProduct.getQuantity()));
            qty.setUOM("CPY");
            QuantitiesType qtys = new QuantitiesType();
            qtys.getQUANTITY().add(qty);
            line.setQUANTITIES(qtys);

            invoice.getLINE().add(line);
            cartIndex++;
        }

        if (isShipping)
        {
            // Add shipping charges to the cart (shipping charges are taxable)
            IndataLineType line = new IndataLineType();

            line.setLINENUMBER(new BigDecimal(cartIndex));
            line.setID(String.valueOf(cartIndex));

            line.setSHIPFROM(sd.getShipFromAddr());
            line.setSELLERPRIMARY(sd.getSellerAddr());

            line.setDESCRIPTION("Shipping Charge");
            line.setPRODUCTCODE("FREIGHT");
            line.setGROSSAMOUNT(sd.getCustomClass().getGROSS_AMOUNTForShipping(sd, shippingQuote,
                    order));
            line.setBUYERPRIMARY(billToAddr);
            line.setSUPPLY(billToAddr);
            line.setSHIPTO(shipToAddr);
            line.setDELIVERYTERMS("");

            RegistrationsType regn = new RegistrationsType();
            regn.getSELLERROLE().add(sd.getSellerRole());
            // regn.getMIDDLEMANROLE().add("UNKNOWN");

            if (!Utils.isBlank(buyerVAT))
            {
                regn.getBUYERROLE().add(buyerVAT);
            }

            line.setREGISTRATIONS(regn);

            // line.setCOUNTRYOFORIGIN("UNKNOWN");
            line.setPOINTOFTITLETRANSFER(POINT_OF_TITLE_TRANSFER);
            line.setTRANSACTIONTYPE("GS"); // GS = Goods

            QuantityType qty = new QuantityType();
            qty.setAMOUNT("1");
            qty.setUOM("CPY");
            QuantitiesType qtys = new QuantitiesType();
            qtys.getQUANTITY().add(qty);
            line.setQUANTITIES(qtys);

            sd.getCustomClass().addUserElementsShipping(sd, order, line);

            invoice.getLINE().add(line);
        }

        inData.getINVOICE().add(invoice);

        request.setINDATA(inData);

        TaxCalculationResponse response = taxServicePort.calculateTax(request);

        long calcEnd = System.currentTimeMillis();

        if (log.isDebugEnabled())
        {
            log.debug("Thomsons Tax Calculation Time : " + (calcEnd - calcStart) + "ms");
        }

        if (response.getOUTDATA().getREQUESTSTATUS().getERROR() != null
                && response.getOUTDATA().getREQUESTSTATUS().getERROR().size() > 0)
        {
            String problemStr = response.getOUTDATA().getREQUESTSTATUS().getERROR().get(0)
                    .getDESCRIPTION();
            log.warn("Response : " + problemStr);
            throw new KKException("Error getting Tax from Thomson Reuters : " + problemStr);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t Response : "
                    + "\n\t Success?         : "
                    + response.getOUTDATA().getREQUESTSTATUS().isISSUCCESS()
                    + "\n\t Partial Success? : "
                    + response.getOUTDATA().getREQUESTSTATUS().isISPARTIALSUCCESS()
                    + "\n\t Response under Invoice : "
                    + "\n\t Success?         : "
                    + response.getOUTDATA().getINVOICE().get(0).getREQUESTSTATUS().isISSUCCESS()
                    + "\n\t Partial Success? : "
                    + response.getOUTDATA().getINVOICE().get(0).getREQUESTSTATUS()
                            .isISPARTIALSUCCESS() + " (false is OK)";

            // Print any invoice messages of severity 1 or more

            msg += "\n\t Invoice Messages:";
            for (MessageType msgT : response.getOUTDATA().getINVOICE().get(0).getMESSAGE())
            {
                msg += "\n\t Severity " + msgT.getSEVERITY() + " = " + msgT.getMESSAGETEXT();
            }

            msg += "\n\t Line Messages:";
            for (MessageType msgT : response.getOUTDATA().getINVOICE().get(0).getLINE().get(0)
                    .getMESSAGE())
            {
                msg += "\n\t Severity " + msgT.getSEVERITY() + " = " + msgT.getMESSAGETEXT();
            }

            msg += "\n\t Line Details:";
            int i = 0;
            for (OutdataLineType msgT : response.getOUTDATA().getINVOICE().get(0).getLINE())
            {
                i++;
                msg += "\n\t\t Line " + i + ") " + Utils.padRight(msgT.getDESCRIPTION(), 25) + " x"
                        + Utils.padRight(getQuantity(msgT), 5) + " GrossAmount: "
                        + Utils.padRight(msgT.getGROSSAMOUNT(), 8) + " Tax: "
                        + msgT.getTOTALTAXAMOUNT();
            }

            msg += "\n\t Total Tax Amount : "
                    + response.getOUTDATA().getINVOICE().get(0).getTOTALTAXAMOUNT();

            log.debug(msg);
        }

        if (log.isDebugEnabled())
        {
            String str = "OrderProduct Prices & Tax BEFORE adjustment:";
            for (int i = 0; i < order.getOrderProducts().length; i++)
            {
                OrderProductIf op = order.getOrderProducts()[i];
                str += "\n\t" + Utils.padRight(op.getName(), 30) + " PriceExTax: "
                        + getFormattedPrice(op.getFinalPriceExTax(), currCode) + " Tax: "
                        + getFormattedPrice(op.getTax(), currCode) + " PriceIncTax: "
                        + getFormattedPrice(op.getFinalPriceIncTax(), currCode);
            }
            if (isShipping)
            {
                ShippingQuoteIf sq = order.getShippingQuote();
                str += "\n\t" + Utils.padRight(sq.getTitle(), 30) + " PriceExTax: "
                        + getFormattedPrice(sq.getTotalExTax(), currCode) + " Tax: "
                        + getFormattedPrice(sq.getTax(), currCode) + " PriceIncTax: "
                        + getFormattedPrice(sq.getTotalIncTax(), currCode);
            }

            log.debug(str);
        }

        sd.getCustomClass().adjustOrderAfterTaxCalculation(sd, response, order);

        if (log.isDebugEnabled())
        {
            String str = "OrderProduct Prices & Tax AFTER adjustment:";
            for (int i = 0; i < order.getOrderProducts().length; i++)
            {
                OrderProductIf op = order.getOrderProducts()[i];
                str += "\n\t" + Utils.padRight(op.getName(), 30) + " PriceExTax: "
                        + getFormattedPrice(op.getFinalPriceExTax(), currCode) + " Tax: "
                        + getFormattedPrice(op.getTax(), currCode) + " PriceIncTax: "
                        + getFormattedPrice(op.getFinalPriceIncTax(), currCode);
            }
            if (isShipping)
            {
                ShippingQuoteIf sq = order.getShippingQuote();
                str += "\n\t" + Utils.padRight(sq.getTitle(), 30) + " PriceExTax: "
                        + getFormattedPrice(sq.getTotalExTax(), currCode) + " Tax: "
                        + getFormattedPrice(sq.getTax(), currCode) + " PriceIncTax: "
                        + getFormattedPrice(sq.getTotalIncTax(), currCode);
            }

            log.debug(str);
        }

        // Set the Order Total with the total tax amount
        double taxAmount = Double.parseDouble(response.getOUTDATA().getINVOICE().get(0)
                .getTOTALTAXAMOUNT());

        BigDecimal adjustedTaxAmountBD = new BigDecimal(taxAmount);

        // Make adjustments to the order for shipping anomalies (FREIGHT INCLUSIVE etc)
        // BigDecimal shippingAdjustment = sd.getCustomClass().adjustmentForInclusiveShipping(sd,
        // response, order);
        // BigDecimal adjustedTaxAmountBD = adjustedTaxAmountBD.subtract(shippingAdjustment);

        if (ot != null)
        {
            ot.setValue(adjustedTaxAmountBD);
            ot.setText(getCurrMgr().formatPrice(adjustedTaxAmountBD, currCode));
        }

        // Set the totals on the order
        sd.getCustomClass().calculateTotals(sd, order);
    }

    private String getQuantity(OutdataLineType msgT)
    {
        if (msgT != null && msgT.getQUANTITIES() != null
                && msgT.getQUANTITIES().getQUANTITY() != null
                && msgT.getQUANTITIES().getQUANTITY().size() >= 1
                && msgT.getQUANTITIES().getQUANTITY().get(0).getAMOUNT() != null)
        {
            return msgT.getQUANTITIES().getQUANTITY().get(0).getAMOUNT();
        }

        return "1?";
    }

    private String getFormattedPrice(BigDecimal price, String currCode) throws KKException,
            TorqueException, DataSetException, Exception
    {
        int width = 8;
        return Utils.padLeft(getCurrMgr().formatPrice(price, currCode), width);
    }

    private void logAddress(String name, ZoneAddressType addr)
    {
        if (log.isDebugEnabled())
        {
            String str = "\n\t " + name;

            if (!Utils.isBlank(addr.getADDRESS1()))
            {
                str += "\n\t ADDRESS1  = " + addr.getADDRESS1();
            }
            if (!Utils.isBlank(addr.getADDRESS2()))
            {
                str += "\n\t ADDRESS2  = " + addr.getADDRESS2();
            }
            if (!Utils.isBlank(addr.getADDRESS3()))
            {
                str += "\n\t ADDRESS3  = " + addr.getADDRESS3();
            }
            if (!Utils.isBlank(addr.getDISTRICT()))
            {
                str += "\n\t DISTRICT  = " + addr.getDISTRICT();
            }
            if (!Utils.isBlank(addr.getCOUNTY()))
            {
                str += "\n\t COUNTY    = " + addr.getCOUNTY();
            }
            if (!Utils.isBlank(addr.getPROVINCE()))
            {
                str += "\n\t PROVINCE  = " + addr.getPROVINCE();
            }
            if (!Utils.isBlank(addr.getSTATE()))
            {
                str += "\n\t STATE     = " + addr.getSTATE();
            }
            if (!Utils.isBlank(addr.getCITY()))
            {
                str += "\n\t CITY      = " + addr.getCITY();
            }
            if (!Utils.isBlank(addr.getPOSTCODE()))
            {
                str += "\n\t POSTCODE  = " + addr.getPOSTCODE();
            }
            if (!Utils.isBlank(addr.getCOUNTRY()))
            {
                str += "\n\t COUNTRY   = " + addr.getCOUNTRY();
            }

            log.debug(str);
        }

    }

    private ThomsonCustomIf getCustomClass(String customClassName) throws KKException
    {
        if (Utils.isBlank(customClassName))
        {
            return null;
        }

        Class<?> mgrClass = null;
        try
        {
            mgrClass = Class.forName(customClassName);
        } catch (Exception e)
        {
            throw new KKException("Unable to load the Thomson custom class with name : "
                    + customClassName);
        }

        if (log.isInfoEnabled())
        {
            log.info("Found Thomson custom class with name : " + customClassName);
        }

        String constructorArg1 = "com.konakart.appif.KKEngIf";
        String constructorArg2 = "com.konakart.bl.modules.ordertotal.thomson.Thomson";
        Constructor<?>[] constructors = mgrClass.getConstructors();
        Constructor<?> engConstructor = null;
        if (constructors != null && constructors.length > 0)
        {
            for (int i = 0; i < constructors.length; i++)
            {
                Constructor<?> constructor = constructors[i];
                Class<?>[] parmTypes = constructor.getParameterTypes();
                if (parmTypes != null && parmTypes.length == 2)
                {
                    String parmName1 = parmTypes[0].getName();
                    String parmName2 = parmTypes[1].getName();
                    if (parmName1 != null && parmName1.equals(constructorArg1) && parmName2 != null
                            && parmName2.equals(constructorArg2))
                    {
                        engConstructor = constructor;
                    }
                }
            }
        }

        if (engConstructor == null)
        {
            throw new KKException("Could not find a constructor for the Thomson custom class : "
                    + customClassName + ", that requires parameter of type " + constructorArg1
                    + " and " + constructorArg2);
        }

        try
        {
            return (ThomsonCustomIf) engConstructor.newInstance(getEng(), this);
        } catch (Exception e)
        {
            throw new KKException("Unable to instantiate the Thomson custom class with name : "
                    + customClassName);
        }
    }

    /**
     * Get the MOSS indicator
     * 
     * @param sd
     *            the StaticData object
     * @param orderProduct
     *            the order product
     * @return the MOSS indicator - which could be null
     */
    public String getMossIndicator(StaticData sd, OrderProductIf orderProduct)
    {
        String returnIndicator = null;
        int customField = sd.getMossCodeCustomField();

        if (customField == 1)
        {
            returnIndicator = orderProduct.getCustom1();
        } else if (customField == 2)
        {
            returnIndicator = orderProduct.getCustom2();
        } else if (customField == 3)
        {
            returnIndicator = orderProduct.getCustom3();
        } else if (customField == 4)
        {
            returnIndicator = orderProduct.getCustom4();
        } else if (customField == 5)
        {
            returnIndicator = getCustomField(MODULE_ORDER_TOTAL_THOMSON_MOSS_CODE_FIELD, sd,
                    orderProduct);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t Moss Code Field  = " + customField + "\n\t oProd.Custom1    = "
                    + orderProduct.getCustom1() + "\n\t oProd.Custom2    = "
                    + orderProduct.getCustom2() + "\n\t oProd.Custom3    = "
                    + orderProduct.getCustom3() + "\n\t oProd.Custom4    = "
                    + orderProduct.getCustom4() + "\n\t Moss Code        = " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    /**
     * Get the VAT Included indicator
     * 
     * @param sd
     *            the StaticData object
     * @param orderProduct
     *            the order product
     * @return the VAT Included indicator - which could be null
     */
    public String getVatIncludedIndicator(StaticData sd, OrderProductIf orderProduct)
    {
        String returnIndicator = null;
        int fieldCode = sd.getVatIncludedCustomField();

        if (fieldCode == 1)
        {
            returnIndicator = orderProduct.getCustom1();
        } else if (fieldCode == 2)
        {
            returnIndicator = orderProduct.getCustom2();
        } else if (fieldCode == 3)
        {
            returnIndicator = orderProduct.getCustom3();
        } else if (fieldCode == 4)
        {
            returnIndicator = orderProduct.getCustom4();
        } else if (fieldCode == 5)
        {
            returnIndicator = getCustomField(MODULE_ORDER_TOTAL_THOMSON_VAT_INCLUDED_FIELD, sd,
                    orderProduct);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t VAT Incl Field   = " + fieldCode + "\n\t oProd.Custom1    = "
                    + orderProduct.getCustom1() + "\n\t oProd.Custom2    = "
                    + orderProduct.getCustom2() + "\n\t oProd.Custom3    = "
                    + orderProduct.getCustom3() + "\n\t oProd.Custom4    = "
                    + orderProduct.getCustom4() + "\n\t VAT Incl Code    = " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    /**
     * Get the Exemptions indicator
     * 
     * @param sd
     *            the StaticData object
     * @param orderProduct
     *            the order product
     * @return the Exemptions indicator - which could be null
     */
    public String getExemptionsIndicator(StaticData sd, OrderProductIf orderProduct)
    {
        String returnIndicator = null;
        int fieldCode = sd.getExemptionsCustomField();

        if (fieldCode == 1)
        {
            returnIndicator = orderProduct.getCustom1();
        } else if (fieldCode == 2)
        {
            returnIndicator = orderProduct.getCustom2();
        } else if (fieldCode == 3)
        {
            returnIndicator = orderProduct.getCustom3();
        } else if (fieldCode == 4)
        {
            returnIndicator = orderProduct.getCustom4();
        } else if (fieldCode == 5)
        {
            returnIndicator = getCustomField(MODULE_ORDER_TOTAL_THOMSON_EXEMPTIONS_FIELD, sd,
                    orderProduct);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t Exemptions Field  = " + fieldCode + "\n\t oProd.Custom1    = "
                    + orderProduct.getCustom1() + "\n\t oProd.Custom2    = "
                    + orderProduct.getCustom2() + "\n\t oProd.Custom3    = "
                    + orderProduct.getCustom3() + "\n\t oProd.Custom4    = "
                    + orderProduct.getCustom4() + "\n\t Exemptions Code  = " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    private String getTransType(StaticData sd, OrderProductIf orderProduct)
    {
        String returnIndicator = null;
        int fieldCode = sd.getTrantypeCustomField();

        if (fieldCode == 1)
        {
            returnIndicator = orderProduct.getCustom1();
        } else if (fieldCode == 2)
        {
            returnIndicator = orderProduct.getCustom2();
        } else if (fieldCode == 3)
        {
            returnIndicator = orderProduct.getCustom3();
        } else if (fieldCode == 4)
        {
            returnIndicator = orderProduct.getCustom4();
        } else if (fieldCode == 5)
        {
            returnIndicator = getCustomField(MODULE_ORDER_TOTAL_THOMSON_TRANS_TYPE_FIELD, sd,
                    orderProduct);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t Trans Type Field = " + fieldCode + "\n\t oProd.Custom1    = "
                    + orderProduct.getCustom1() + "\n\t oProd.Custom2    = "
                    + orderProduct.getCustom2() + "\n\t oProd.Custom3    = "
                    + orderProduct.getCustom3() + "\n\t oProd.Custom4    = "
                    + orderProduct.getCustom4() + "\n\t Transaction Type = " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    /**
     * Custom method for retrieving values from complicated places... expected to be customised to
     * suit local requirements. In the default case we expect that the 4 tax code fields are
     * '|'-separated and set on a single custom field that is identified by the
     * MODULE_ORDER_TOTAL_THOMSON_CUSTON_CODE_FIELD configuration setting.
     * 
     * @param field
     *            to identify the field we are retrieving
     * @param sd
     *            the StaticData which caches configuration data for the store
     * @param orderProduct
     *            the order product. An order will have one or more orderProducts
     * @return the value for the field identified
     */
    protected String getCustomField(String field, StaticData sd, OrderProductIf orderProduct)
    {
        String customCodeFieldValue = null;
        int customField = sd.getCustomCodeCustomField();

        if (customField == 1)
        {
            customCodeFieldValue = orderProduct.getCustom1();
        } else if (customField == 2)
        {
            customCodeFieldValue = orderProduct.getCustom2();
        } else if (customField == 3)
        {
            customCodeFieldValue = orderProduct.getCustom3();
        } else if (customField == 4)
        {
            customCodeFieldValue = orderProduct.getCustom4();
        }

        if (log.isDebugEnabled())
        {
            log.debug("Custom Code Field " + customField + " Value = " + customCodeFieldValue);
        }

        return sd.getCustomClass().getCustomFieldValue(field, customCodeFieldValue, sd,
                orderProduct);
    }

    private String getCommodityCode(StaticData sd, OrderProductIf orderProduct)
    {
        String returnIndicator = null;
        int fieldCode = sd.getCommodityCodeCustomField();

        if (fieldCode == 1)
        {
            returnIndicator = orderProduct.getCustom1();
        } else if (fieldCode == 2)
        {
            returnIndicator = orderProduct.getCustom2();
        } else if (fieldCode == 3)
        {
            returnIndicator = orderProduct.getCustom3();
        } else if (fieldCode == 4)
        {
            returnIndicator = orderProduct.getCustom4();
        } else if (fieldCode == 5)
        {
            returnIndicator = getCustomField(MODULE_ORDER_TOTAL_THOMSON_COMMODITY_CODE_FIELD, sd,
                    orderProduct);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t EUComCode Field  = " + fieldCode + "\n\t oProd.Custom1    = "
                    + orderProduct.getCustom1() + "\n\t oProd.Custom2    = "
                    + orderProduct.getCustom2() + "\n\t oProd.Custom3    = "
                    + orderProduct.getCustom3() + "\n\t oProd.Custom4    = "
                    + orderProduct.getCustom4() + "\n\t EUComdty Code    = " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    private String getPartNumber(StaticData sd, OrderProductIf orderProduct)
    {
        return orderProduct.getSku();
    }

    private String getUSCommodityCode(StaticData sd, OrderProductIf orderProduct)
    {
        String returnIndicator = null;
        int fieldCode = sd.getUsCommodityCodeCustomField();

        if (fieldCode == 1)
        {
            returnIndicator = orderProduct.getCustom1();
        } else if (fieldCode == 2)
        {
            returnIndicator = orderProduct.getCustom2();
        } else if (fieldCode == 3)
        {
            returnIndicator = orderProduct.getCustom3();
        } else if (fieldCode == 4)
        {
            returnIndicator = orderProduct.getCustom4();
        } else if (fieldCode == 5)
        {
            returnIndicator = getCustomField(MODULE_ORDER_TOTAL_THOMSON_USCOMMODITY_CODE_FIELD, sd,
                    orderProduct);
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t USComCode Field  = " + fieldCode + "\n\t oProd.Custom1    = "
                    + orderProduct.getCustom1() + "\n\t oProd.Custom2    = "
                    + orderProduct.getCustom2() + "\n\t oProd.Custom3    = "
                    + orderProduct.getCustom3() + "\n\t oProd.Custom4    = "
                    + orderProduct.getCustom4() + "\n\t USComdty Code    = " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    /**
     * Get the Shipping VAT indicator
     * 
     * @param sd
     *            the StaticData object
     * @param order
     *            the order
     * @return the Shipping VAT indicator - which could be null
     */
    public String getShippingVatIndicator(StaticData sd, OrderIf order)
    {
        String returnIndicator = null;

        if (order.getShippingQuote() != null)
        {
            returnIndicator = order.getShippingQuote().getCustom1();
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t Shipping VAT Code= " + returnIndicator;
            log.debug(msg);
        }

        return returnIndicator;
    }

    private String getBuyerRole(StaticData sd, CustomerIf customer)
    {
        // The taxIdentifier attribute is only available in 7.3.0.0
        // String buyerVAT = customer.getTaxIdentifier();

        String buyerVAT = null;
        if (sd.getTaxIdField() == 1)
        {
            buyerVAT = customer.getCustom1();
        } else if (sd.getTaxIdField() == 2)
        {
            buyerVAT = customer.getCustom2();
        } else if (sd.getTaxIdField() == 3)
        {
            buyerVAT = customer.getCustom3();
        } else if (sd.getTaxIdField() == 4)
        {
            buyerVAT = customer.getCustom4();
        }

        if (log.isDebugEnabled())
        {
            String msg = "\n\t TaxFieldId       = " + sd.getTaxIdField()
                    + "\n\t Customer.Custom1 = " + customer.getCustom1()
                    + "\n\t Customer.Custom2 = " + customer.getCustom2()
                    + "\n\t Customer.Custom3 = " + customer.getCustom3()
                    + "\n\t Customer.Custom4 = " + customer.getCustom4()
                    + "\n\t Buyer Role       = " + buyerVAT;
            log.debug(msg);
        }
        return buyerVAT;
    }

    private String getInvoiceDate(Order order)
    {
        if (order.getDatePurchased() != null)
        {
            return getThomsonsDate(order.getDatePurchased());
        }
        if (order.getDateFinished() != null)
        {
            return getThomsonsDate(order.getDateFinished());
        }

        return getThomsonsDate(new GregorianCalendar());
    }

    private String getThomsonsDate(Calendar dt)
    {
        return getThomsonDateFormat().format(dt.getTime());
    }

    /**
     * Get the country by the country name.
     * 
     * @param sd
     *            Static data
     * @param countryName
     *            country name to search for
     * @return the country for the country name or null if the country name can't be found
     * @throws Exception
     */
    private Country getCountry(StaticData sd, String countryName) throws Exception
    {
        Country country = sd.getCountryMap().get(countryName);

        if (country == null)
        {
            country = getTaxMgr().getCountryPerName(countryName);

            if (country == null)
            {
                log.warn("Country with name '" + countryName + "' not found");
                return null;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Add country '" + countryName + "' to countryMap");
            }
            sd.getCountryMap().put(countryName, country);
        }

        if (log.isDebugEnabled())
        {
            log.debug("Return cached country '" + countryName + "'");
        }
        return country;
    }

    /**
     * Get the ISO2 code by the country name.
     * 
     * @param sd
     *            Static data
     * @param countryName
     *            country name to search for
     * @return the ISO2 code for the country or the country name if the ISO2 name can't be found
     */
    public String getCountryISO2(StaticData sd, String countryName)
    {
        Country country = sd.getCountryMap().get(countryName);

        if (country == null)
        {
            log.warn("Country with name '" + countryName + "' not found");
            return countryName;
        }

        if (Utils.isBlank(country.getIsoCode2()))
        {
            log.warn("Country with name '" + countryName + "' has no ISO2 code");
            return countryName;
        }

        return country.getIsoCode2();
    }

    /**
     * Get the zone code by the Zone name and country Id.
     * 
     * @param sd
     *            Static data
     * @param zoneName
     *            zone name to search for
     * @param countryId
     *            the country Id to search for
     * @return the zone for the zone name and country or null if the zone name or zone's code can't
     *         be found
     * @throws Exception
     */
    private Zone getZone(StaticData sd, String zoneName, int countryId) throws Exception
    {
        String zoneKey = zoneName + "_" + countryId;

        Zone zone = sd.getZoneMap().get(zoneKey);

        if (zone == null)
        {
            zone = getTaxMgr().getZonePerCountryAndCode(countryId, zoneName);

            if (zone == null)
            {
                log.warn("Zone with name '" + zoneName + "' in country " + countryId + " not found");
                return null;
            }

            if (Utils.isBlank(zone.getZoneCode()))
            {
                log.warn("Zone with name '" + zoneName + "' in country " + countryId
                        + " has no code");
                return null;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Add zone '" + zoneName + "' in country " + countryId + " to zoneMap");
            }
            sd.getZoneMap().put(zoneKey, zone);
        }

        if (log.isDebugEnabled())
        {
            log.debug("Return cached zone '" + zoneName + "' in country " + countryId);
        }
        return zone;
    }

    /**
     * Get the zone code from the Zone
     * 
     * @param zone
     *            zone object
     * @return the zone code for the zone or the zone name itself if the zone code is empty
     */
    public String getZoneCode(Zone zone)
    {
        if (zone == null)
        {
            log.warn("Zone is empty - unexpected");
            return "";
        }

        if (Utils.isBlank(zone.getZoneCode()))
        {
            log.warn("Zone with name '" + zone.getZoneName() + "' has no zone code");
            return zone.getZoneName();
        }

        return zone.getZoneCode();
    }

    /**
     * @return the Thomson date Format Formatter
     */
    public static SimpleDateFormat getThomsonDateFormat()
    {
        return thomsonDateFormat;
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private String companyName;

        private String companyRole;

        private String customClassName;

        private String externalCompanyId;

        private String sellerRole;

        private String username;

        private String password;

        private boolean secure;

        private String bodyUsername;

        private String bodyPassword;

        private boolean bodySecure;

        private int customCodeCustomField = -1;

        private int usCommodityCodeCustomField = -1;

        private int commodityCodeCustomField = -1;

        private int mossCodeCustomField = -1;

        private int trantypeCustomField = -1;

        private int vatIncludedCustomField = -1;

        private int exemptionsCustomField = -1;

        private int taxIdField = -1;

        private URL taxServiceEndpoint;

        private ZoneAddressType sellerAddr = null;

        private ZoneAddressType shipFromAddr = null;

        private String street;

        private String county;

        private String state;

        private String province;

        private String city;

        private String postcode;

        private String country;

        private String shipFromStreet;

        private String shipFromCounty;

        private String shipFromState;

        private String shipFromCity;

        private String shipFromPostcode;

        private String shipFromProvince;

        private String shipFromCountry;

        private boolean commitStatus = false;

        private boolean saveMsgDb = true;

        private HashMap<String, Country> countryMap = new HashMap<String, Country>();

        private HashMap<String, Zone> zoneMap = new HashMap<String, Zone>();

        private ThomsonCustomIf customClass = null;

        /**
         * @return the companyName
         */
        public String getCompanyName()
        {
            return companyName;
        }

        /**
         * @param companyName
         *            the companyName to set
         */
        public void setCompanyName(String companyName)
        {
            this.companyName = companyName;
        }

        /**
         * @return the commitStatus
         */
        public boolean isCommitStatus()
        {
            return commitStatus;
        }

        /**
         * @param commitStatus
         *            the commitStatus to set
         */
        public void setCommitStatus(boolean commitStatus)
        {
            this.commitStatus = commitStatus;
        }

        /**
         * @return the sellerAddr
         */
        public ZoneAddressType getSellerAddr()
        {
            return sellerAddr;
        }

        /**
         * @param sellerAddr
         *            the sellerAddr to set
         */
        public void setSellerAddr(ZoneAddressType sellerAddr)
        {
            this.sellerAddr = sellerAddr;
        }

        /**
         * @return the shipFromAddr
         */
        public ZoneAddressType getShipFromAddr()
        {
            return shipFromAddr;
        }

        /**
         * @param shipFromAddr
         *            the shipFromAddr to set
         */
        public void setShipFromAddr(ZoneAddressType shipFromAddr)
        {
            this.shipFromAddr = shipFromAddr;
        }

        /**
         * @return the taxServiceEndpoint
         */
        public URL getTaxServiceEndpoint()
        {
            return taxServiceEndpoint;
        }

        /**
         * @param taxServiceEndpoint
         *            the taxServiceEndpoint to set
         */
        public void setTaxServiceEndpoint(URL taxServiceEndpoint)
        {
            this.taxServiceEndpoint = taxServiceEndpoint;
        }

        /**
         * @return the shipFromStreet
         */
        public String getShipFromStreet()
        {
            return shipFromStreet;
        }

        /**
         * @param shipFromStreet
         *            the shipFromStreet to set
         */
        public void setShipFromStreet(String shipFromStreet)
        {
            this.shipFromStreet = shipFromStreet;
        }

        /**
         * @return the shipFromCounty
         */
        public String getShipFromCounty()
        {
            return shipFromCounty;
        }

        /**
         * @param shipFromCounty
         *            the shipFromCounty to set
         */
        public void setShipFromCounty(String shipFromCounty)
        {
            this.shipFromCounty = shipFromCounty;
        }

        /**
         * @return the shipFromState
         */
        public String getShipFromState()
        {
            return shipFromState;
        }

        /**
         * @param shipFromState
         *            the shipFromState to set
         */
        public void setShipFromState(String shipFromState)
        {
            this.shipFromState = shipFromState;
        }

        /**
         * @return the shipFromCity
         */
        public String getShipFromCity()
        {
            return shipFromCity;
        }

        /**
         * @param shipFromCity
         *            the shipFromCity to set
         */
        public void setShipFromCity(String shipFromCity)
        {
            this.shipFromCity = shipFromCity;
        }

        /**
         * @return the shipFromPostcode
         */
        public String getShipFromPostcode()
        {
            return shipFromPostcode;
        }

        /**
         * @param shipFromPostcode
         *            the shipFromPostcode to set
         */
        public void setShipFromPostcode(String shipFromPostcode)
        {
            this.shipFromPostcode = shipFromPostcode;
        }

        /**
         * @return the shipFromCountry
         */
        public String getShipFromCountry()
        {
            return shipFromCountry;
        }

        /**
         * @param shipFromCountry
         *            the shipFromCountry to set
         */
        public void setShipFromCountry(String shipFromCountry)
        {
            this.shipFromCountry = shipFromCountry;
        }

        private String callingSys;

        /**
         * @return the callingSys
         */
        public String getCallingSys()
        {
            return callingSys;
        }

        /**
         * @param callingSys
         *            the callingSys to set
         */
        public void setCallingSys(String callingSys)
        {
            this.callingSys = callingSys;
        }

        private String defaultCurrency;

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
         * @return the companyRole
         */
        public String getCompanyRole()
        {
            return companyRole;
        }

        /**
         * @param companyRole
         *            the companyRole to set
         */
        public void setCompanyRole(String companyRole)
        {
            this.companyRole = companyRole;
        }

        /**
         * @return the externalCompanyId
         */
        public String getExternalCompanyId()
        {
            return externalCompanyId;
        }

        /**
         * @param externalCompanyId
         *            the externalCompanyId to set
         */
        public void setExternalCompanyId(String externalCompanyId)
        {
            this.externalCompanyId = externalCompanyId;
        }

        /**
         * @return the sellerRole
         */
        public String getSellerRole()
        {
            return sellerRole;
        }

        /**
         * @param sellerRole
         *            the sellerRole to set
         */
        public void setSellerRole(String sellerRole)
        {
            this.sellerRole = sellerRole;
        }

        /**
         * @return the commodityCodeCustomField
         */
        public int getCommodityCodeCustomField()
        {
            return commodityCodeCustomField;
        }

        /**
         * @param commodityCodeCustomField
         *            the commodityCodeCustomField to set
         */
        public void setCommodityCodeCustomField(int commodityCodeCustomField)
        {
            this.commodityCodeCustomField = commodityCodeCustomField;
        }

        /**
         * @return the postcode
         */
        public String getPostcode()
        {
            return postcode;
        }

        /**
         * @param postcode
         *            the postcode to set
         */
        public void setPostcode(String postcode)
        {
            this.postcode = postcode;
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
         * @return the country
         */
        public String getCountry()
        {
            return country;
        }

        /**
         * @param country
         *            the country to set
         */
        public void setCountry(String country)
        {
            this.country = country;
        }

        /**
         * @return the street
         */
        public String getStreet()
        {
            return street;
        }

        /**
         * @param street
         *            the street to set
         */
        public void setStreet(String street)
        {
            this.street = street;
        }

        /**
         * @return the county
         */
        public String getCounty()
        {
            return county;
        }

        /**
         * @param county
         *            the county to set
         */
        public void setCounty(String county)
        {
            this.county = county;
        }

        /**
         * @return the defaultCurrency
         */
        public String getDefaultCurrency()
        {
            return defaultCurrency;
        }

        /**
         * @param defaultCurrency
         *            the defaultCurrency to set
         */
        public void setDefaultCurrency(String defaultCurrency)
        {
            this.defaultCurrency = defaultCurrency;
        }

        /**
         * @return the username
         */
        public String getUsername()
        {
            return username;
        }

        /**
         * @param username
         *            the username to set
         */
        public void setUsername(String username)
        {
            this.username = username;
        }

        /**
         * @return the password
         */
        public String getPassword()
        {
            return password;
        }

        /**
         * @param password
         *            the password to set
         */
        public void setPassword(String password)
        {
            this.password = password;
        }

        /**
         * @return the secure
         */
        public boolean isSecure()
        {
            return secure;
        }

        /**
         * @param secure
         *            the secure to set
         */
        public void setSecure(boolean secure)
        {
            this.secure = secure;
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
         * @return the taxIdField
         */
        public int getTaxIdField()
        {
            return taxIdField;
        }

        /**
         * @param taxIdField
         *            the taxIdField to set
         */
        public void setTaxIdField(int taxIdField)
        {
            this.taxIdField = taxIdField;
        }

        /**
         * @return the usCommodityCodeCustomField
         */
        public int getUsCommodityCodeCustomField()
        {
            return usCommodityCodeCustomField;
        }

        /**
         * @param usCommodityCodeCustomField
         *            the usCommodityCodeCustomField to set
         */
        public void setUsCommodityCodeCustomField(int usCommodityCodeCustomField)
        {
            this.usCommodityCodeCustomField = usCommodityCodeCustomField;
        }

        /**
         * @return the mossCodeCustomField
         */
        public int getMossCodeCustomField()
        {
            return mossCodeCustomField;
        }

        /**
         * @param mossCodeCustomField
         *            the mossCodeCustomField to set
         */
        public void setMossCodeCustomField(int mossCodeCustomField)
        {
            this.mossCodeCustomField = mossCodeCustomField;
        }

        /**
         * @return the customCodeCustomField
         */
        public int getCustomCodeCustomField()
        {
            return customCodeCustomField;
        }

        /**
         * @param customCodeCustomField
         *            the customCodeCustomField to set
         */
        public void setCustomCodeCustomField(int customCodeCustomField)
        {
            this.customCodeCustomField = customCodeCustomField;
        }

        /**
         * @return the countryMap
         */
        public HashMap<String, Country> getCountryMap()
        {
            return countryMap;
        }

        /**
         * Empty the countryMap
         */
        public void emptyCountryMap()
        {
            countryMap.clear();
        }

        /**
         * @return the zoneMap
         */
        public HashMap<String, Zone> getZoneMap()
        {
            return zoneMap;
        }

        /**
         * Empty the zoneMap
         */
        public void emptyZoneMap()
        {
            zoneMap.clear();
        }

        /**
         * @return the trantypeCustomField
         */
        public int getTrantypeCustomField()
        {
            return trantypeCustomField;
        }

        /**
         * @param trantypeCustomField
         *            the trantypeCustomField to set
         */
        public void setTrantypeCustomField(int trantypeCustomField)
        {
            this.trantypeCustomField = trantypeCustomField;
        }

        /**
         * @return the shipFromProvince
         */
        public String getShipFromProvince()
        {
            return shipFromProvince;
        }

        /**
         * @param shipFromProvince
         *            the shipFromProvince to set
         */
        public void setShipFromProvince(String shipFromProvince)
        {
            this.shipFromProvince = shipFromProvince;
        }

        /**
         * @return the province
         */
        public String getProvince()
        {
            return province;
        }

        /**
         * @param province
         *            the province to set
         */
        public void setProvince(String province)
        {
            this.province = province;
        }

        /**
         * @return the customClassName
         */
        public String getCustomClassName()
        {
            return customClassName;
        }

        /**
         * @param customClassName
         *            the customClassname to set
         */
        public void setCustomClassName(String customClassName)
        {
            this.customClassName = customClassName;
        }

        /**
         * @return the customClass
         */
        public ThomsonCustomIf getCustomClass()
        {
            return customClass;
        }

        /**
         * @param customClass
         *            the customClass to set
         */
        public void setCustomClass(ThomsonCustomIf customClass)
        {
            this.customClass = customClass;
        }

        /**
         * @return the saveMsgDb
         */
        public boolean isSaveMsgDb()
        {
            return saveMsgDb;
        }

        /**
         * @param saveMsgDb
         *            the saveMsgDb to set
         */
        public void setSaveMsgDb(boolean saveMsgDb)
        {
            this.saveMsgDb = saveMsgDb;
        }

        /**
         * @return the vatIncludedCustomField
         */
        public int getVatIncludedCustomField()
        {
            return vatIncludedCustomField;
        }

        /**
         * @param vatIncludedCustomField
         *            the vatIncludedCustomField to set
         */
        public void setVatIncludedCustomField(int vatIncludedCustomField)
        {
            this.vatIncludedCustomField = vatIncludedCustomField;
        }

        /**
         * @return the exemptionsCustomField
         */
        public int getExemptionsCustomField()
        {
            return exemptionsCustomField;
        }

        /**
         * @param exemptionsCustomField
         *            the exemptionsCustomField to set
         */
        public void setExemptionsCustomField(int exemptionsCustomField)
        {
            this.exemptionsCustomField = exemptionsCustomField;
        }

        /**
         * @return the bodyUsername
         */
        public String getBodyUsername()
        {
            return bodyUsername;
        }

        /**
         * @param bodyUsername the bodyUsername to set
         */
        public void setBodyUsername(String bodyUsername)
        {
            this.bodyUsername = bodyUsername;
        }

        /**
         * @return the bodyPassword
         */
        public String getBodyPassword()
        {
            return bodyPassword;
        }

        /**
         * @param bodyPassword the bodyPassword to set
         */
        public void setBodyPassword(String bodyPassword)
        {
            this.bodyPassword = bodyPassword;
        }

        /**
         * @return the bodySecure
         */
        public boolean isBodySecure()
        {
            return bodySecure;
        }

        /**
         * @param bodySecure the bodySecure to set
         */
        public void setBodySecure(boolean bodySecure)
        {
            this.bodySecure = bodySecure;
        }
    }
}
