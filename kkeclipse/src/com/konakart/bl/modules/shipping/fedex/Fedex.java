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

package com.konakart.bl.modules.shipping.fedex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.axis.types.PositiveInteger;
import org.apache.torque.TorqueException;

import com.fedex.rate.stub.Address;
import com.fedex.rate.stub.ClientDetail;
import com.fedex.rate.stub.DropoffType;
import com.fedex.rate.stub.Notification;
import com.fedex.rate.stub.NotificationSeverityType;
import com.fedex.rate.stub.PackagingType;
import com.fedex.rate.stub.RateAndServiceOptions;
import com.fedex.rate.stub.RateAvailableServicesReply;
import com.fedex.rate.stub.RateAvailableServicesRequest;
import com.fedex.rate.stub.RatePortType;
import com.fedex.rate.stub.RateRequestPackageSummary;
import com.fedex.rate.stub.RateRequestType;
import com.fedex.rate.stub.RateServiceLocator;
import com.fedex.rate.stub.RatedShipmentDetail;
import com.fedex.rate.stub.ServiceType;
import com.fedex.rate.stub.TransactionDetail;
import com.fedex.rate.stub.VersionId;
import com.fedex.rate.stub.WebAuthenticationCredential;
import com.fedex.rate.stub.WebAuthenticationDetail;
import com.fedex.rate.stub.Weight;
import com.fedex.rate.stub.WeightUnits;
import com.konakart.app.KKConfiguration;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.ShippingQuote;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.shipping.BaseShippingModule;
import com.konakart.bl.modules.shipping.ShippingInfo;
import com.konakart.bl.modules.shipping.ShippingInterface;
import com.konakart.util.ExceptionUtils;
import com.workingdogs.village.DataSetException;

/**
 * This shipping module communicates with FedEx via a FedEx web service to retrieve shipping rates.
 * Using the Admin App you must set up the following configuration variables:
 * 
 * FedEx Module:
 * 
 * <ul>
 * <li>MODULE_SHIPPING_FEDEX_KEY: FedEx Key</li>
 * <li>MODULE_SHIPPING_FEDEX_PASSWORD: FedEx Password</li>
 * <li>MODULE_SHIPPING_FEDEX_ACCOUNT_NUMBER: FedEx Account Number</li>
 * <li>MODULE_SHIPPING_FEDEX_METER_NUMBER: FedEx Meter Number</li>
 * <li>MODULE_SHIPPING_FEDEX_SERVICE_TYPE: FedEx Service type : If this is set, then we only return
 * a quote for this service</li>
 * <li>MODULE_SHIPPING_FEDEX_SERVICE_TYPES_EXCLUDE: FedEx Service types to exclude : Comma separated
 * list of service types to exclude</li>
 * <li>MODULE_SHIPPING_FEDEX_MEASUREMENT_UNIT: Measurement Unit : LBS or KGS</li>
 * <li>MODULE_SHIPPING_FEDEX_PACKAGING_TYPE: Packaging type by weight : Defaults to
 * 1000:YOUR_PACKAGING where YOUR_PACKAGING is the packaging type for up to 1000 Lbs or Kgs. You
 * could set it to something like 10:FEDEX_10KG_BOX,25:FEDEX_25KG_BOX,100:YOUR_PACKAGING which means
 * that from weight 0 to 10 we use a FEDEX_10KG_BOX. Then from weight 10 to 25 we use a
 * FEDEX_25KG_BOX etc. Before setting specific package codes you need to be sure that FedEx supports
 * the package for the destination.</li>
 * <li>MODULE_SHIPPING_FEDEX_HANDLING: Handling fee : Defaults to 0. It is added to the charge
 * returned by FedEx.</li>
 * <li>MODULE_SHIPPING_FEDEX_END_POINT_URL : The end point of the FedEx service (i.e.
 * https://gatewaybeta.fedex.com:443/web-services)</li>
 * <li>MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE : The type of drop off used. i.e. REGULAR_PICKUP,
 * BUSINESS_SERVICE_CENTER, DROP_BOX, REQUEST_COURIER, STATION</li>
 * <li>MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE : The rate request type i.e. ACCOUNT, LIST,
 * MULTIWEIGHT</li>
 * </ul>
 * 
 * Shipping and Packaging under Configuration
 * 
 * <ul>
 * <li>Country of Origin : Country from which packages will be shipped.</li>
 * <li>Postal Code : Postal code of area from which packages will be shipped</li>
 * <li>Maximum package weight : This is used to split the order into multiple packages . We make
 * multiple calls to FedEx and sum the costs to create a total.</li>
 * <li>Package tare weight : This is added to the weight of the items being shipped per package</li>
 * </ul>
 */
public class Fedex extends BaseShippingModule implements ShippingInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "fedex";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.fedex.Fedex";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "fedexMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_FEDEX_ACCOUNT_NUMBER = "MODULE_SHIPPING_FEDEX_ACCOUNT_NUMBER";

    private final static String MODULE_SHIPPING_FEDEX_METER_NUMBER = "MODULE_SHIPPING_FEDEX_METER_NUMBER";

    private final static String MODULE_SHIPPING_FEDEX_KEY = "MODULE_SHIPPING_FEDEX_KEY";

    private final static String MODULE_SHIPPING_FEDEX_PASSWORD = "MODULE_SHIPPING_FEDEX_PASSWORD";

    private final static String MODULE_SHIPPING_FEDEX_SERVICE_TYPE = "MODULE_SHIPPING_FEDEX_SERVICE_TYPE";

    private final static String MODULE_SHIPPING_FEDEX_SERVICE_TYPES_EXCLUDE = "MODULE_SHIPPING_FEDEX_SERVICE_TYPES_EXCLUDE";

    private final static String MODULE_SHIPPING_FEDEX_ZONE = "MODULE_SHIPPING_FEDEX_ZONE";

    private final static String MODULE_SHIPPING_FEDEX_SORT_ORDER = "MODULE_SHIPPING_FEDEX_SORT_ORDER";

    private final static String MODULE_SHIPPING_FEDEX_TAX_CLASS = "MODULE_SHIPPING_FEDEX_TAX_CLASS";

    private final static String MODULE_SHIPPING_FEDEX_STATUS = "MODULE_SHIPPING_FEDEX_STATUS";

    private final static String MODULE_SHIPPING_FEDEX_MEASUREMENT_UNIT = "MODULE_SHIPPING_FEDEX_MEASUREMENT_UNIT";

    private final static String MODULE_SHIPPING_FEDEX_PACKAGING_TYPE = "MODULE_SHIPPING_FEDEX_PACKAGING_TYPE";

    private final static String MODULE_SHIPPING_FEDEX_HANDLING = "MODULE_SHIPPING_FEDEX_HANDLING";

    private final static String MODULE_SHIPPING_FEDEX_END_POINT_URL = "MODULE_SHIPPING_FEDEX_END_POINT_URL";

    private final static String MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE = "MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE";

    private final static String MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE = "MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_FEDEX_TEXT_TITLE = "module.shipping.fedex.text.title";

    // private final static String MODULE_SHIPPING_FEDEX_TEXT_DESCRIPTION =
    // "module.shipping.fedex.text.description";

    // private final static String MODULE_SHIPPING_FEDEX_TEXT_WAY =
    // "module.shipping.fedex.text.way";

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
    public Fedex(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
     * We get a quote from FedEx. If we only get one quote back then we return a ShippingQuote
     * object with the details of the quote. If we get back more than one quote then we create an
     * array of ShippingQuote objects and attach them to the ShippingQuote object that we return. In
     * this case all of the quotes must be in the array since the quote itself isn't processed.
     * 
     * It becomes more complex when we have the shipment split into a number of packages. The split
     * is done by the manager calling this method and is presented to us in an orderWeightList. For
     * each item in the list, we call FedEx and get back one or more quotes. We then have to add up
     * the quotes and return them as single quotes that can be displayed on the screen.
     * 
     * @param order
     *            The order object
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    public ShippingQuote getQuote(Order order, ShippingInfo info) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());

        // Throws an exception if there are no physical products. They may be all digital download
        // products.
        checkForProducts(info);

        // Get the resource bundle
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap, info
                .getLocale());
        if (rb == null)
        {
            throw new KKException("A resource file cannot be found for the country "
                    + info.getLocale().getCountry());
        }

        /*
         * The global parameter zone, if greater than zero, should reference a GeoZone. If the
         * DeliveryAddress of the order isn't within that GeoZone, then we throw an exception
         */
        if (sd.getZone() > 0)
        {
            checkZone(info, sd.getZone());
        }

        /*
         * There is a list of weights since the total order weight may exceed the maximum weight for
         * a single package and so it has already been split up by the manager calling this module.
         * We must send off a request to FedEx for each package and add up the total cost.
         */
        if (log.isDebugEnabled())
        {
            if (info.getOrderWeightList().size() == 1)
            {
                log.debug("Order consists of 1 package");
            } else
            {
                log.debug("Order consists of " + info.getOrderWeightList().size() + " packages.");
            }
        }

        List<ShippingQuote> quoteList = new ArrayList<ShippingQuote>();
        int index = 0;
        for (Iterator<BigDecimal> iter = info.getOrderWeightList().iterator(); iter.hasNext();)
        {
            BigDecimal weight = iter.next().add(info.getBoxWeight());
            if (log.isDebugEnabled())
            {
                log.debug("Weight" + index + " = " + weight);
                index++;
            }
            ShippingQuote quote = getQuotesFromFedex(order, info, rb, weight);
            quoteList.add(quote);
        }

        /*
         * Now we must match the service codes and add up the total cost for each service code, The
         * service code is in Custom5.
         */
        if (quoteList.size() == 1)
        {
            // We don't have multiple packages so just return the quote
            if (log.isDebugEnabled())
            {
                log.debug("Returning quote : ");
                log.debug(quoteList.get(0).toString());
            }
            return quoteList.get(0);
        }

        if (log.isDebugEnabled())
        {
            log.debug("Multiple package quotes are : ");
            for (Iterator<ShippingQuote> iterator = quoteList.iterator(); iterator.hasNext();)
            {
                ShippingQuote q = iterator.next();
                log.debug(q);
            }
        }

        /*
         * We take the first set of quote(s) and look for matching ones to add the price to. We add
         * the first set to a hash map to make the lookup easier. We also set custom2 to 1 to keep
         * track of how many additions we have done.
         */
        HashMap<String, ShippingQuote> quoteMap = new HashMap<String, ShippingQuote>();
        if (quoteList.get(0).getQuotes() == null)
        {
            quoteList.get(0).setCustom2("1");
            quoteMap.put(quoteList.get(0).getCustom5(), quoteList.get(0));
        } else
        {
            for (int i = 0; i < quoteList.get(0).getQuotes().length; i++)
            {
                ShippingQuote q = (ShippingQuote) quoteList.get(0).getQuotes()[i];
                q.setCustom2("1");
                quoteMap.put(q.getCustom5(), q);
            }
        }

        /*
         * Now that we have the first set in a hash map we go through the others and add them to the
         * ones in the hash map.
         */
        int i = 0;
        for (Iterator<ShippingQuote> iterator = quoteList.iterator(); iterator.hasNext();)
        {
            ShippingQuote quote = iterator.next();

            if (i++ == 0)
            {
                // Miss out the first set of quotes which are in the hash map
                continue;
            }

            if (quote.getQuotes() == null)
            {
                ShippingQuote retQuote = quoteMap.get(quote.getCustom5());
                if (retQuote != null)
                {
                    addQuotePrices(retQuote, quote);
                }
            } else
            {
                for (int j = 0; j < quote.getQuotes().length; j++)
                {
                    ShippingQuote sq = (ShippingQuote) quote.getQuotes()[j];
                    ShippingQuote retQuote = quoteMap.get(sq.getCustom5());
                    if (retQuote != null)
                    {
                        addQuotePrices(retQuote, sq);
                    }
                }
            }
        }

        /*
         * Remove any quotes that haven't got custom2 set to the length of quoteList. We need to do
         * this because every time we get a quote from FedEx we don't necessarily get back the same
         * rate codes . i.e. First time we may get FedEx Saver and FedEx Worldwide Express. The
         * quote for the 2nd package may only contain FedEx Saver. If this is the case we have to
         * make sure that we only return FedEx Saver.
         */

        // Go through the hash map and see how many quotes we can keep
        int count = 0;
        for (Iterator<ShippingQuote> iterator = quoteMap.values().iterator(); iterator.hasNext();)
        {
            ShippingQuote sq = iterator.next();
            if (Integer.parseInt(sq.getCustom2()) == quoteList.size())
            {
                count++;
            }
        }

        ShippingQuote retQuote = quoteList.get(0);

        // If count == 0 we have to throw an exception
        if (count == 0)
        {
            throw new KKException(
                    "The shipment was split up into multiple packages so we made multiple calls to FedEx. FedEx didn't return an identical rate across all calls ");
        }

        // Only do a purge if the number of quotes we can keep isn't equal to the number we got
        // from FedEx. We have to create a new array with the quotes that we can keep.
        if ((retQuote.getQuotes() != null && count != retQuote.getQuotes().length))
        {
            ShippingQuote[] retArray = new ShippingQuote[count];
            int k = 0;
            for (Iterator<ShippingQuote> iterator = quoteMap.values().iterator(); iterator
                    .hasNext();)
            {
                ShippingQuote sq = iterator.next();
                if (Integer.parseInt(sq.getCustom2()) == quoteList.size())
                {
                    retArray[k++] = sq;
                } else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Removing shipping method " + sq.getDescription()
                                + " from quote because it isn't present in all calls to FedEx");
                    }
                }
            }

            retQuote.setQuotes(retArray);
        }

        if (log.isDebugEnabled())
        {
            log.debug("Returning quote : ");
            log.debug(retQuote.toString());
        }

        return retQuote;
    }

    /**
     * Returns the package type based on the weight
     * 
     * @return Returns a PackagingType object
     * @throws KKException
     */
    private PackagingType getPackageForWeight(BigDecimal weight) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        for (Iterator<WeightPackage> iter1 = sd.getWeightPackageList().iterator(); iter1.hasNext();)
        {
            WeightPackage wp = iter1.next();
            if (weight.compareTo(wp.getWeight()) == -1)
            {
                return wp.getPackType();
            }
        }

        // Return a default package if can't find one in the list
        return PackagingType.YOUR_PACKAGING;
    }

    /**
     * Get quotes from FedEx for a single delivery
     * 
     * @param order
     * @param info
     * @param rb
     * @param weight
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    private ShippingQuote getQuotesFromFedex(Order order, ShippingInfo info, ResourceBundle rb,
            BigDecimal weight) throws Exception
    {
        StaticData sd = staticDataHM.get(getStoreId());
        Address originAddress = getOriginAddress(order, info);
        Address destinationAddress = getDestinationAddress(order, info);
        RateRequestPackageSummary packageSummary = getpackageSummary(order, info, weight);
        ClientDetail clientDetail = getClientDetail();
        WebAuthenticationDetail webAuthDetail = getWebAuthenticationDetail();
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setCustomerTransactionId("Konakart Transaction");

        // Initialize the service
        RateServiceLocator service;
        RatePortType port;

        service = new RateServiceLocator();
        if (log.isDebugEnabled())
        {
            log
                    .debug("Current FedEx service endpoint URL = "
                            + service.getRateServicePortAddress());
        }
        service.setRateServicePortEndpointAddress(sd.getEndPointUrl());
        if (log.isDebugEnabled())
        {
            log.debug("FedEx service endpoint URL set to = " + sd.getEndPointUrl());
        }

        port = service.getRateServicePort();

        BigDecimal total = new BigDecimal(0);

        RateAvailableServicesRequest request = new RateAvailableServicesRequest();
        request.setClientDetail(clientDetail);
        request.setWebAuthenticationDetail(webAuthDetail);
        request.setTransactionDetail(transactionDetail);
        request.setVersion(new VersionId("crs", 3, 0, 0));
        request.setOrigin(originAddress);
        request.setDestination(destinationAddress);

        // DropoffType type
        request.setDropoffType(sd.getDropOffType());
        // ServiceType type
        if (sd.getServiceType() != null)
        {
            request.setServiceType(sd.getServiceType());
        }

        // Packaging type
        request.setPackagingType(getPackageForWeight(weight));
        // Ship date
        request.setShipDate(new Date());
        // Package summary
        request.setRateRequestPackageSummary(packageSummary);

        RateRequestType[] rateReqArray = new RateRequestType[1];
        rateReqArray[0] = sd.getRateRequestType();
        request.setRateRequestTypes(rateReqArray);

        if (log.isDebugEnabled())
        {
            log.debug("FedEx drop off type : " + sd.getDropOffType().getValue());
            log.debug("FedEx service type : "
                    + ((sd.getServiceType() == null) ? "None Specified" : sd.getServiceType()
                            .getValue()));
            log.debug("FedEx packaging type : " + request.getPackagingType().getValue());
            log.debug("FedEx rate req type : " + sd.getRateRequestType().getValue());
        }

        List<ShippingQuote> quoteList = new ArrayList<ShippingQuote>();

        boolean excludedQuotes = false;

        // This is the call to the web service passing in a RateAvailableServicesRequest and
        // returning a RateAvailableServicesReply
        RateAvailableServicesReply reply;
        try
        {
            reply = port.rateAvailableServices(request);
        } catch (RuntimeException e)
        {
            log.error(ExceptionUtils.exceptionToString(e));
            throw new KKException(
                    "Exception received while trying to send shipping request to FedEx. See stack trace above.");
        }

        if (reply == null)
        {
            throw new KKException("Null reply received from FedEx.");
        }

        // We print out the reply if debug is enabled
        debugReply(reply);

        // If logging at info level is enabled we print the notifications
        printNotifications(reply.getNotifications());

        if (reply.getHighestSeverity().toString().equals(NotificationSeverityType._SUCCESS)
                || reply.getHighestSeverity().toString().equals(NotificationSeverityType._NOTE)
                || reply.getHighestSeverity().toString().equals(NotificationSeverityType._WARNING))
        {

            // Options for the different rates available
            RateAndServiceOptions rso[] = reply.getOptions();
            if (rso == null || rso.length == 0)
            {
                throw new KKException("reply.getOptions() from FedEx is null or empty.");
            }

            for (int i = 0; i < rso.length; i++)
            {
                if (rso[i].getServiceDetail().getServiceType() != null)
                {
                    String serviceName = rso[i].getServiceDetail().getServiceType().getValue();
                    // Don't add the service if it is in our exclude list
                    if (excludeQuote(serviceName))
                    {
                        log.debug("Excluded quote for service name : " + serviceName);
                        excludedQuotes = true;
                        continue;
                    }
                    ShippingQuote sq = new ShippingQuote();
                    sq.setCode(code);
                    sq.setModuleCode(code);
                    sq.setSortOrder(sd.getSortOrder());
                    sq.setIcon(icon);
                    sq.setTaxClass(sd.getTaxClass());
                    sq.setTitle(rb.getString(MODULE_SHIPPING_FEDEX_TEXT_TITLE));
                    sq.setResponseText(getFriendlyServiceName(serviceName));
                    sq.setDescription(getFriendlyServiceName(serviceName));
                    sq.setCustom5(serviceName);
                    sq.setShippingServiceCode(serviceName);
                    sq.setCost(total);
                    setQuotePrices(sq, info);

                    boolean found = false;
                    if (rso[i].getRatedShipmentDetails() != null)
                    {
                        for (int j = 0; j < rso[i].getRatedShipmentDetails().length; j++)
                        {
                            RatedShipmentDetail rsd = rso[i].getRatedShipmentDetails()[j];

                            if (rsd.getShipmentRateDetail().getTotalNetCharge() != null)

                                if (rsd.getShipmentRateDetail().getTotalNetCharge().getCurrency()
                                        .equalsIgnoreCase(order.getCurrencyCode()))
                                {
                                    total = rsd.getShipmentRateDetail().getTotalNetCharge()
                                            .getAmount();
                                    found = true;
                                    break;
                                }
                        }
                    }
                    if (found)
                    {
                        sq.setCost(total);
                        setQuotePrices(sq, info);
                        quoteList.add(sq);
                    } else
                    {
                        if (log.isInfoEnabled())
                        {
                            log
                                    .info("Could not find a shipping quote from FedEx for service "
                                            + sq.getResponseText()
                                            + " in the same currency of the order which has a currency code of "
                                            + order.getCurrencyCode());
                        }
                    }
                }
            }
        }

        if (quoteList.size() == 0)
        {
            if (excludedQuotes)
            {
                // No quotes from FedEx
                throw new KKException("All the FedEx quotes are on the exclude list.");
            }
            // No quotes from FedEx
            throw new KKException("The FedEx response contained no quotes.");

        } else if (quoteList.size() == 1)
        {
            return quoteList.get(0);
        } else
        {
            // Create an array of quotes
            ShippingQuoteIf[] sqArray = new ShippingQuoteIf[quoteList.size()];
            int i = 0;
            for (Iterator<ShippingQuote> iterator = quoteList.iterator(); iterator.hasNext();)
            {
                ShippingQuote sq = iterator.next();
                sq.setCode(sq.getCode() + "_" + i);
                sqArray[i] = sq;
                i++;
            }

            /*
             * If we have multiple quotes we add them as an array of quotes to the return quote
             * which itself is ignored
             */
            ShippingQuote retQ = new ShippingQuote();
            retQ.setQuotes(sqArray);
            return retQ;
        }
    }

    /**
     * Used to debug the reply from FedEx
     * 
     * @param reply
     */
    private void debugReply(RateAvailableServicesReply reply)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Reply from FedEx Rate Available Service:");

            if (reply == null)
            {
                log.debug("Reply is null");
                return;
            }

            if (reply.getHighestSeverity() != null)
            {
                log.debug("reply.getHighestSeverity(): " + reply.getHighestSeverity().toString());
            } else
            {
                log.debug("reply.getHighestSeverity() is null");
            }

            RateAndServiceOptions rso[] = reply.getOptions();
            if (rso == null)
            {
                log.debug("reply.getOptions() is null. No quotes.");
                return;
            }

            if (rso.length == 0)
            {
                log.debug("reply.getOptions() has length of 0. No quotes.");
                return;
            }

            for (int i = 0; i < rso.length; i++)
            {
                log.debug("\n\nService details");
                if (null != rso[i].getServiceDetail().getDeliveryDate())
                    log.debug("Delivery date: " + rso[i].getServiceDetail().getDeliveryDate()
                            + " day: " + rso[i].getServiceDetail().getDeliveryDay());
                if (null != rso[i].getServiceDetail().getDestinationStationId())
                    log.debug("Destination station id: "
                            + rso[i].getServiceDetail().getDestinationStationId());
                if (null != rso[i].getServiceDetail().getPackagingType())
                    log.debug("Packaging type: "
                            + rso[i].getServiceDetail().getPackagingType().getValue()
                            + " service type: "
                            + rso[i].getServiceDetail().getServiceType().getValue());
                if (null != rso[i].getServiceDetail().getTransitTime())
                    log.debug("Transit time: "
                            + rso[i].getServiceDetail().getTransitTime().getValue());

                if (rso[i].getRatedShipmentDetails() != null)
                {
                    for (int j = 0; j < rso[i].getRatedShipmentDetails().length; j++)
                    {
                        RatedShipmentDetail rsd = rso[i].getRatedShipmentDetails()[j];

                        if (null != rsd.getShipmentRateDetail().getRateType())
                            log.debug("RateType: "
                                    + rsd.getShipmentRateDetail().getRateType().getValue());
                        if (null != rsd.getShipmentRateDetail().getTotalBillingWeight())
                            log.debug("Total billing weight: "
                                    + rsd.getShipmentRateDetail().getTotalBillingWeight()
                                            .getValue()
                                    + " "
                                    + rsd.getShipmentRateDetail().getTotalBillingWeight()
                                            .getUnits().getValue());
                        if (null != rsd.getShipmentRateDetail().getTotalDimWeight())
                            log.debug("Total dim weight: "
                                    + rsd.getShipmentRateDetail().getTotalDimWeight().getValue()
                                    + " "
                                    + rsd.getShipmentRateDetail().getTotalDimWeight().getUnits()
                                            .getValue());
                        if (null != rsd.getShipmentRateDetail().getTotalFreightDiscounts())
                            log.debug("Total freight discount: "
                                    + rsd.getShipmentRateDetail().getTotalFreightDiscounts()
                                            .getAmount()
                                    + " "
                                    + rsd.getShipmentRateDetail().getTotalFreightDiscounts()
                                            .getCurrency());
                        if (null != rsd.getShipmentRateDetail().getTotalNetCharge())
                            log
                                    .debug("Total Net charge: "
                                            + rsd.getShipmentRateDetail().getTotalNetCharge()
                                                    .getAmount()
                                            + " "
                                            + rsd.getShipmentRateDetail().getTotalNetCharge()
                                                    .getCurrency());
                        if (null != rsd.getShipmentRateDetail().getTotalNetFreight())
                            log.debug("Total Net freight: "
                                    + rsd.getShipmentRateDetail().getTotalNetFreight().getAmount()
                                    + " "
                                    + rsd.getShipmentRateDetail().getTotalNetFreight()
                                            .getCurrency());
                        if (null != rsd.getShipmentRateDetail().getTotalSurcharges().getAmount())
                            log.debug("Total surcharges: "
                                    + rsd.getShipmentRateDetail().getTotalSurcharges().getAmount()
                                    + " "
                                    + rsd.getShipmentRateDetail().getTotalSurcharges()
                                            .getCurrency());
                    }
                }
            }
        }
    }

    /**
     * Log exceptions from the FedEx gateway
     * 
     * @param notifications
     */
    private void printNotifications(Notification[] notifications)
    {
        if (log.isInfoEnabled())
        {
            if (!(notifications == null || notifications.length == 0))
            {
                log.info("FedEx Notifications:");
                for (int i = 0; i < notifications.length; i++)
                {
                    Notification n = notifications[i];
                    log.info("  Notification no. " + i + ": ");
                    if (n == null)
                    {
                        log.info("null");
                        continue;
                    }
                    NotificationSeverityType nst = n.getSeverity();
                    log.info("");
                    log.info("    Severity: " + (nst == null ? "null" : nst.getValue()));
                    log.info("    Code: " + n.getCode());
                    log.info("    Message: " + n.getMessage());
                    log.info("    Source: " + n.getSource());
                }
            }
        }
    }

    /**
     * Return true if we need to exclude the quote
     * 
     * @param service
     * @return Returns a boolean
     * @throws KKException
     */
    private boolean excludeQuote(String service) throws KKException
    {
        if (service == null)
        {
            return true;
        }
        StaticData sd = staticDataHM.get(getStoreId());

        String exclude = sd.getExludeServiceMap().get(service);
        if (exclude != null)
        {
            return true;
        }
        return false;
    }

    /**
     * Add handling charges and tax to the total cost
     * 
     * @param quote
     * @param info
     * @throws KKException
     */
    private void setQuotePrices(ShippingQuote quote, ShippingInfo info) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());

        quote.setHandlingCost(sd.getHandling());
        BigDecimal costPlusHandling = quote.getCost().add(sd.getHandling());
        if (sd.getTaxClass() > 0 && info.getDeliveryZone() != null)
        {
            quote.setTax(getEng().getTax(costPlusHandling, info.getDeliveryCountry().getId(),
                    info.getDeliveryZone().getZoneId(), sd.getTaxClass()));
            quote.setTotalExTax(costPlusHandling);
            quote.setTotalIncTax(quote.getTax().add(costPlusHandling));
        } else
        {
            quote.setTax(new BigDecimal(0));
            quote.setTotalExTax(costPlusHandling);
            quote.setTotalIncTax(costPlusHandling);
        }
    }

    /**
     * Adds the quote prices to quote1
     * 
     * @param quote1
     * @param quote1
     * @throws KKException
     */
    private void addQuotePrices(ShippingQuote quote1, ShippingQuote quote2) throws KKException
    {
        quote1.setTax(quote1.getTax().add(quote2.getTax()));
        quote1.setTotalExTax(quote1.getTotalExTax().add(quote2.getTotalExTax()));
        quote1.setTotalIncTax(quote1.getTotalIncTax().add(quote2.getTotalIncTax()));
        // Increment custom2
        quote1.setCustom2(Integer.toString(Integer.parseInt(quote1.getCustom2()) + 1));
    }

    /**
     * Create an Address object for the origin address
     * 
     * @param order
     * @param info
     * @return Returns the origin address
     */
    private Address getOriginAddress(Order order, ShippingInfo info)
    {
        Address origAddress = new Address();

        if (info.getOriginZip() != null)
        {
            origAddress.setPostalCode(info.getOriginZip());
        }

        if (info.getOriginCountry() != null)
        {
            origAddress.setCountryCode(info.getOriginCountry().getIsoCode2());
        }

        if (log.isDebugEnabled())
        {
            log.debug("FedEx Origin Address:");
            log.debug("Origin PostalCode : " + origAddress.getPostalCode());
            log.debug("Origin Country : " + origAddress.getCountryCode());
        }

        return origAddress;
    }

    /**
     * Create an Address object for the destination address
     * 
     * @param order
     * @param info
     * @return Returns the destination address
     */
    private Address getDestinationAddress(Order order, ShippingInfo info)
    {
        Address destAddress = new Address(); // Destination information

        String[] tempAddress = null;
        if (order.getDeliverySuburb() != null && order.getDeliverySuburb().length() > 0)
        {
            tempAddress = new String[2];
            tempAddress[0] = order.getDeliveryStreetAddress();
            tempAddress[1] = order.getDeliverySuburb();
        } else
        {
            tempAddress = new String[1];
            tempAddress[0] = order.getDeliveryStreetAddress();
        }

        destAddress.setStreetLines(tempAddress);
        destAddress.setCity(order.getDeliveryCity());
        destAddress.setPostalCode(order.getDeliveryPostcode());
        destAddress.setCountryCode(info.getDeliveryCountry().getIsoCode2());

        if (log.isDebugEnabled())
        {
            log.debug("FedEx Destination Address:");
            log.debug("Destination PostalCode : " + destAddress.getPostalCode());
            log.debug("Destination Country : " + destAddress.getCountryCode());
        }

        return destAddress;
    }

    /**
     * Create a RateRequestPackageSummary object from the order info
     * 
     * @param order
     * @param info
     * @param weight
     * @return Returns a RateRequestPackageSummary object
     * @throws KKException
     */
    private RateRequestPackageSummary getpackageSummary(Order order, ShippingInfo info,
            BigDecimal weight) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());

        RateRequestPackageSummary packageSummary = new RateRequestPackageSummary();

        Weight weightObj = new Weight();
        weightObj.setValue(weight.setScale(1, BigDecimal.ROUND_HALF_UP));
        if (sd.getMeasurementUnit().equals("KGS"))
        {
            weightObj.setUnits(WeightUnits.KG);
        } else
        {
            weightObj.setUnits(WeightUnits.LB);
        }
        packageSummary.setTotalWeight(weightObj);

        packageSummary.setPieceCount(new PositiveInteger("1"));

        if (log.isDebugEnabled())
        {
            log.debug("FedEx Package Summary:");
            log.debug("Weight Units : " + weightObj.getUnits());
            log.debug("Weight : " + weightObj.getValue());
        }

        // // Set dimensions if available
        // Dimensions dimensions = new Dimensions();
        // dimensions.setLength(new NonNegativeInteger("20"));
        // dimensions.setWidth(new NonNegativeInteger("20"));
        // dimensions.setHeight(new NonNegativeInteger("20"));
        // dimensions.setUnits(LinearUnits.IN);
        // packageSummary.setPerPieceDimensions(dimensions);
        //        
        // // Set special services if required
        // PackageSpecialServicesRequested pssr = new PackageSpecialServicesRequested();
        // packageSummary.setSpecialServicesRequested(pssr);

        return packageSummary;

    }

    /**
     * Create a ClientDetail object
     * 
     * @return Returns a ClientDetail object
     * @throws KKException
     */
    private ClientDetail getClientDetail() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());

        ClientDetail clientDetail = new ClientDetail();
        clientDetail.setAccountNumber(sd.getAccountNumber());
        clientDetail.setMeterNumber(sd.getMeterNumber());
        return clientDetail;
    }

    /**
     * Create a WebAuthenticationDetail object
     * 
     * @return Returns a WebAuthenticationDetail object
     * @throws KKException
     */
    private WebAuthenticationDetail getWebAuthenticationDetail() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());

        WebAuthenticationCredential wac = new WebAuthenticationCredential();
        wac.setKey(sd.getKey());
        wac.setPassword(sd.getPassword());
        return new WebAuthenticationDetail(wac);
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_FEDEX_STATUS);
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

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_PASSWORD must be set to the FedEx password for using the API");
        }
        staticData.setPassword(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_ACCOUNT_NUMBER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_ACCOUNT_NUMBER must be set to the FedEx account number for using the API");
        }
        staticData.setAccountNumber(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_METER_NUMBER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_METER_NUMBER must be set to the FedEx meter number for using the API");
        }
        staticData.setMeterNumber(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_KEY);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_KEY must be set to the FedEx key for using the API");
        }
        staticData.setKey(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_MEASUREMENT_UNIT);
        if (conf == null || !(conf.getValue().equals("KGS") || conf.getValue().equals("LBS")))
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_MEASUREMENT_UNIT must be set to KGS or LBS");
        }
        staticData.setMeasurementUnit(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_HANDLING);
        if (conf == null)
        {
            staticData.setHandling(new BigDecimal(0));
        } else
        {
            staticData.setHandling(new BigDecimal(conf.getValue()));
        }

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_SERVICE_TYPE);

        if (conf != null && conf.getValue() != null && conf.getValue().length() > 0)
        {
            String serviceTypeStr = conf.getValue();

            if (serviceTypeStr.equalsIgnoreCase("EUROPE_FIRST_INTERNATIONAL_PRIORITY"))
            {
                staticData.setServiceType(ServiceType.EUROPE_FIRST_INTERNATIONAL_PRIORITY);
            } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_1_DAY_FREIGHT"))
            {
                staticData.setServiceType(ServiceType.FEDEX_1_DAY_FREIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_2_DAY"))
            {
                staticData.setServiceType(ServiceType.FEDEX_2_DAY);
            } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_2_DAY_FREIGHT"))
            {
                staticData.setServiceType(ServiceType.FEDEX_2_DAY_FREIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_3_DAY_FREIGHT"))
            {
                staticData.setServiceType(ServiceType.FEDEX_3_DAY_FREIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_EXPRESS_SAVER"))
            {
                staticData.setServiceType(ServiceType.FEDEX_EXPRESS_SAVER);
            } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_GROUND"))
            {
                staticData.setServiceType(ServiceType.FEDEX_GROUND);
            } else if (serviceTypeStr.equalsIgnoreCase("FIRST_OVERNIGHT"))
            {
                staticData.setServiceType(ServiceType.FIRST_OVERNIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("GROUND_HOME_DELIVERY"))
            {
                staticData.setServiceType(ServiceType.GROUND_HOME_DELIVERY);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_DISTRIBUTION_FREIGHT"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_DISTRIBUTION_FREIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_ECONOMY"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_ECONOMY);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_ECONOMY_DISTRIBUTION"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_ECONOMY_DISTRIBUTION);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_ECONOMY_FREIGHT"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_ECONOMY_FREIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_FIRST"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_FIRST);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_PRIORITY"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_PRIORITY);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_PRIORITY_DISTRIBUTION"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_PRIORITY_DISTRIBUTION);
            } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_PRIORITY_FREIGHT"))
            {
                staticData.setServiceType(ServiceType.INTERNATIONAL_PRIORITY_FREIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("PRIORITY_OVERNIGHT"))
            {
                staticData.setServiceType(ServiceType.PRIORITY_OVERNIGHT);
            } else if (serviceTypeStr.equalsIgnoreCase("STANDARD_OVERNIGHT"))
            {
                staticData.setServiceType(ServiceType.STANDARD_OVERNIGHT);
            } else
            {
                throw new KKException(
                        "The Configuration MODULE_SHIPPING_FEDEX_SERVICE_TYPE which is set to "
                                + serviceTypeStr + " must be set to a valid service type."
                                + " Valid types are " + "\nEUROPE_FIRST_INTERNATIONAL_PRIORITY"
                                + "\nFEDEX_1_DAY_FREIGHT" + "\nFEDEX_2_DAY"
                                + "\nFEDEX_2_DAY_FREIGHT" + "\nFEDEX_3_DAY_FREIGHT"
                                + "\nFEDEX_EXPRESS_SAVER" + "\nFEDEX_GROUND" + "\nFIRST_OVERNIGHT"
                                + "\nGROUND_HOME_DELIVERY" + "\nINTERNATIONAL_DISTRIBUTION_FREIGHT"
                                + "\nINTERNATIONAL_ECONOMY"
                                + "\nINTERNATIONAL_ECONOMY_DISTRIBUTION"
                                + "\nINTERNATIONAL_ECONOMY_FREIGHT" + "\nINTERNATIONAL_FIRST"
                                + "\nINTERNATIONAL_PRIORITY"
                                + "\nINTERNATIONAL_PRIORITY_DISTRIBUTION"
                                + "\nINTERNATIONAL_PRIORITY_FREIGHT" + "\nPRIORITY_OVERNIGHT"
                                + "\nSTANDARD_OVERNIGHT");
            }
        } else
        {
            staticData.setServiceType(null);
        }

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_END_POINT_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_END_POINT_URL must be set to the end point of the service being used");
        }
        staticData.setEndPointUrl(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE which is set to null, must be set to a valid dropoff type."
                            + " Valid types are REGULAR_PICKUP, BUSINESS_SERVICE_CENTER, DROP_BOX, REQUEST_COURIER, STATION");
        }

        String dropOffTypeStr = conf.getValue();
        if (dropOffTypeStr.equalsIgnoreCase("REGULAR_PICKUP"))
        {
            staticData.setDropOffType(DropoffType.REGULAR_PICKUP);
        } else if (dropOffTypeStr.equalsIgnoreCase("BUSINESS_SERVICE_CENTER"))
        {
            staticData.setDropOffType(DropoffType.BUSINESS_SERVICE_CENTER);
        } else if (dropOffTypeStr.equalsIgnoreCase("DROP_BOX"))
        {
            staticData.setDropOffType(DropoffType.DROP_BOX);
        } else if (dropOffTypeStr.equalsIgnoreCase("REQUEST_COURIER"))
        {
            staticData.setDropOffType(DropoffType.REQUEST_COURIER);
        } else if (dropOffTypeStr.equalsIgnoreCase("STATION"))
        {
            staticData.setDropOffType(DropoffType.STATION);
        } else
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_DROP_OFF_TYPE which is set to "
                            + dropOffTypeStr
                            + " must be set to a valid dropoff type."
                            + " Valid types are REGULAR_PICKUP, BUSINESS_SERVICE_CENTER, DROP_BOX, REQUEST_COURIER, STATION");
        }

        conf = getConfiguration(MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE which is set to null, must be set to a valid rate request type. "
                            + "Valid types are: ACCOUNT, LIST, MULTIWEIGHT");
        }
        String rateReqTypeStr = conf.getValue();

        if (rateReqTypeStr.equalsIgnoreCase("ACCOUNT"))
        {
            staticData.setRateRequestType(RateRequestType.ACCOUNT);
        } else if (rateReqTypeStr.equalsIgnoreCase("LIST"))
        {
            staticData.setRateRequestType(RateRequestType.LIST);
        } else if (rateReqTypeStr.equalsIgnoreCase("MULTIWEIGHT"))
        {
            staticData.setRateRequestType(RateRequestType.MULTIWEIGHT);
        } else
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_RATE_REQUEST_TYPE which is set to "
                            + rateReqTypeStr + " must be set to a valid payment type. "
                            + "Valid types are: ACCOUNT, LIST, MULTIWEIGHT");
        }

        createWeightPackageList();
        setExcludeServiceMap();
    }

    /**
     * From the configuration data, we fill a list with the package that we need to choose based on
     * the weight. The config data should contain something like this :
     * 10:FEDEX_10KG_BOX,25:FEDEX_25KG_BOX,100:YOUR_PACKAGING which means that from weight 0 to 10
     * we use a FEDEX_10KG_BOX. Then from weight 10 to 25 we use a FEDEX_25KG_BOX etc.
     * 
     * @throws Exception
     */
    private void createWeightPackageList() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        KKConfiguration conf = getConfiguration(MODULE_SHIPPING_FEDEX_PACKAGING_TYPE);
        if (conf != null)
        {
            if (sd.getWeightPackageList() == null)
            {
                sd.setWeightPackageList(new ArrayList<WeightPackage>());
            } else
            {
                sd.getWeightPackageList().clear();
            }

            String weightPackages = conf.getValue();
            if (weightPackages != null)
            {
                String[] weightPackagesArray = weightPackages.split(",");
                for (int i = 0; i < weightPackagesArray.length; i++)
                {
                    String weightPackage = weightPackagesArray[i];
                    String[] weightPackageArray = weightPackage.split(":");
                    if (weightPackageArray.length == 2)
                    {
                        PackagingType pt = null;
                        if (weightPackageArray[1].equalsIgnoreCase("FEDEX_10KG_BOX"))
                        {
                            pt = PackagingType.FEDEX_10KG_BOX;
                        } else if (weightPackageArray[1].equalsIgnoreCase("FEDEX_25KG_BOX"))
                        {
                            pt = PackagingType.FEDEX_25KG_BOX;
                        } else if (weightPackageArray[1].equalsIgnoreCase("FEDEX_BOX"))
                        {
                            pt = PackagingType.FEDEX_BOX;
                        } else if (weightPackageArray[1].equalsIgnoreCase("FEDEX_ENVELOPE"))
                        {
                            pt = PackagingType.FEDEX_ENVELOPE;
                        } else if (weightPackageArray[1].equalsIgnoreCase("FEDEX_PAK"))
                        {
                            pt = PackagingType.FEDEX_PAK;
                        } else if (weightPackageArray[1].equalsIgnoreCase("FEDEX_TUBE"))
                        {
                            pt = PackagingType.FEDEX_TUBE;
                        } else if (weightPackageArray[1].equalsIgnoreCase("YOUR_PACKAGING"))
                        {
                            pt = PackagingType.YOUR_PACKAGING;
                        } else
                        {

                            throw new KKException(
                                    "An invalid packaging type was detected within the configuration variable MODULE_SHIPPING_FEDEX_PACKAGING_TYPE."
                                            + " The invalid type is "
                                            + weightPackageArray[1]
                                            + ". Valid types are: "
                                            + "\nFEDEX_10KG_BOX"
                                            + "\nFEDEX_25KG_BOX"
                                            + "\nFEDEX_BOX"
                                            + "\nFEDEX_ENVELOPE"
                                            + "\nFEDEX_PAK"
                                            + "\nFEDEX_TUBE"
                                            + "\nYOUR_PACKAGING");

                        }
                        WeightPackage wp = new WeightPackage(new BigDecimal(weightPackageArray[0]),
                                pt);
                        sd.getWeightPackageList().add(wp);
                    } else
                    {
                        throw new KKException(
                                "FedEx Packaging type by weight must be in the format weight:PackageType,weight1:PackageType1,weight2:PackageType2 etc.");
                    }
                }
            }
        } else
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_FEDEX_PACKAGING_TYPE which is set to null, must be set to a valid value.");
        }

        if (log.isDebugEnabled())
        {
            for (Iterator<WeightPackage> iterator = sd.getWeightPackageList().iterator(); iterator
                    .hasNext();)
            {
                WeightPackage wp = iterator.next();
                log.debug(wp.toString());
            }
        }
    }

    /**
     * Place the service codes that we have to exclude, in a hash map
     * 
     * @throws KKException
     */
    private void setExcludeServiceMap() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        sd.getExludeServiceMap().clear();
        KKConfiguration conf = getConfiguration(
                MODULE_SHIPPING_FEDEX_SERVICE_TYPES_EXCLUDE);
        if (conf != null && conf.getValue() != null && conf.getValue().length() > 0)
        {
            String serviceCodes = conf.getValue();
            String[] codeArray = serviceCodes.split(",");
            for (int i = 0; i < codeArray.length; i++)
            {
                String sCode = codeArray[i];
                sd.getExludeServiceMap().put(sCode, "");
                if (log.isDebugEnabled())
                {
                    log.debug("Exclude service code = " + sCode);
                }
            }
        }
    }

    /**
     * Returns a friendly service name from the service name, that we can show on the UI
     * 
     * @param serviceName
     * @return Returns a friendly service name
     */
    private String getFriendlyServiceName(String serviceTypeStr)
    {
        if (serviceTypeStr == null)
        {
            return "No service";
        }

        if (serviceTypeStr.equalsIgnoreCase("EUROPE_FIRST_INTERNATIONAL_PRIORITY"))
        {
            return ("Fedex Europe First International Priority");
        } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_1_DAY_FREIGHT"))
        {
            return ("Fedex 1 day freight");
        } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_2_DAY"))
        {
            return ("Fedex 2 day");
        } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_2_DAY_FREIGHT"))
        {
            return ("Fedex 2 day freight");
        } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_3_DAY_FREIGHT"))
        {
            return ("Fedex 3 day freight");
        } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_EXPRESS_SAVER"))
        {
            return ("Fedex Express Saver");
        } else if (serviceTypeStr.equalsIgnoreCase("FEDEX_GROUND"))
        {
            return ("Fedex Ground");
        } else if (serviceTypeStr.equalsIgnoreCase("FIRST_OVERNIGHT"))
        {
            return ("Fedex First Overnight");
        } else if (serviceTypeStr.equalsIgnoreCase("GROUND_HOME_DELIVERY"))
        {
            return ("Fedex Ground Home Delivery");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_DISTRIBUTION_FREIGHT"))
        {
            return ("Fedex International Distribution Freight");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_ECONOMY"))
        {
            return ("Fedex International Economy");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_ECONOMY_DISTRIBUTION"))
        {
            return ("Fedex International Economy Distribution");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_ECONOMY_FREIGHT"))
        {
            return ("Fedex International Economy Freight");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_FIRST"))
        {
            return ("Fedex International First");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_PRIORITY"))
        {
            return ("Fedex International Priority");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_PRIORITY_DISTRIBUTION"))
        {
            return ("Fedex International Priority Distribution");
        } else if (serviceTypeStr.equalsIgnoreCase("INTERNATIONAL_PRIORITY_FREIGHT"))
        {
            return ("Fedex International Priority Freight");
        } else if (serviceTypeStr.equalsIgnoreCase("PRIORITY_OVERNIGHT"))
        {
            return ("Fedex Priority Overnight");
        } else if (serviceTypeStr.equalsIgnoreCase("STANDARD_OVERNIGHT"))
        {
            return ("Fedex Standard Overnight");
        } else
        {
            return serviceTypeStr;
        }

    }

    /**
     * Class used to keep the package code for a weight
     */
    private class WeightPackage
    {
        private BigDecimal weight;

        private PackagingType packType;

        /**
         * Constructor
         * 
         * @param weight
         * @param packType
         */
        public WeightPackage(BigDecimal weight, PackagingType packType)
        {
            setWeight(weight);
            setPackType(packType);
        }

        public String toString()
        {
            StringBuffer str = new StringBuffer();
            str.append("WeightPackage:\n");
            str.append("weight          = ").append(getWeight()).append("\n");
            str.append("packType             = ").append(
                    (getPackType() == null) ? "null" : getPackType().getValue()).append("\n");
            return (str.toString());
        }

        /**
         * @return Returns the weight.
         */
        public BigDecimal getWeight()
        {
            return weight;
        }

        /**
         * @param weight
         *            The weight to set.
         */
        public void setWeight(BigDecimal weight)
        {
            this.weight = weight;
        }

        /**
         * @return Returns the packType.
         */
        public PackagingType getPackType()
        {
            return packType;
        }

        /**
         * @param packType
         *            The packType to set.
         */
        public void setPackType(PackagingType packType)
        {
            this.packType = packType;
        }
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private int taxClass;

        private int zone;

        // FedEx key to use the API
        private String key;

        // FedEx password to use the API
        private String password;

        // FedEx account number to use the API
        private String accountNumber;

        // FedEx meter number to use the API
        private String meterNumber;

        // FedEx web service endpoint URL
        private String endPointUrl;

        // FedEx drop off type
        private DropoffType dropOffType;

        // FedEx rate request type
        private RateRequestType rateRequestType;

        // If rate == true this is the service type for which we receive a quote
        private ServiceType serviceType;

        private HashMap<String, String> exludeServiceMap = new HashMap<String, String>();

        // Measurement unit
        private String measurementUnit = "";

        // Handling charge
        private BigDecimal handling;

        // List that contains FedEx package types based on the weight
        private List<WeightPackage> weightPackageList = null;

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
         * @return the taxClass
         */
        public int getTaxClass()
        {
            return taxClass;
        }

        /**
         * @param taxClass
         *            the taxClass to set
         */
        public void setTaxClass(int taxClass)
        {
            this.taxClass = taxClass;
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
         * @return the key
         */
        public String getKey()
        {
            return key;
        }

        /**
         * @param key
         *            the key to set
         */
        public void setKey(String key)
        {
            this.key = key;
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
         * @return the accountNumber
         */
        public String getAccountNumber()
        {
            return accountNumber;
        }

        /**
         * @param accountNumber
         *            the accountNumber to set
         */
        public void setAccountNumber(String accountNumber)
        {
            this.accountNumber = accountNumber;
        }

        /**
         * @return the meterNumber
         */
        public String getMeterNumber()
        {
            return meterNumber;
        }

        /**
         * @param meterNumber
         *            the meterNumber to set
         */
        public void setMeterNumber(String meterNumber)
        {
            this.meterNumber = meterNumber;
        }

        /**
         * @return the endPointUrl
         */
        public String getEndPointUrl()
        {
            return endPointUrl;
        }

        /**
         * @param endPointUrl
         *            the endPointUrl to set
         */
        public void setEndPointUrl(String endPointUrl)
        {
            this.endPointUrl = endPointUrl;
        }

        /**
         * @return the dropOffType
         */
        public DropoffType getDropOffType()
        {
            return dropOffType;
        }

        /**
         * @param dropOffType
         *            the dropOffType to set
         */
        public void setDropOffType(DropoffType dropOffType)
        {
            this.dropOffType = dropOffType;
        }

        /**
         * @return the rateRequestType
         */
        public RateRequestType getRateRequestType()
        {
            return rateRequestType;
        }

        /**
         * @param rateRequestType
         *            the rateRequestType to set
         */
        public void setRateRequestType(RateRequestType rateRequestType)
        {
            this.rateRequestType = rateRequestType;
        }

        /**
         * @return the serviceType
         */
        public ServiceType getServiceType()
        {
            return serviceType;
        }

        /**
         * @param serviceType
         *            the serviceType to set
         */
        public void setServiceType(ServiceType serviceType)
        {
            this.serviceType = serviceType;
        }

        /**
         * @return the exludeServiceMap
         */
        public HashMap<String, String> getExludeServiceMap()
        {
            return exludeServiceMap;
        }

        /**
         * @param exludeServiceMap
         *            the exludeServiceMap to set
         */
        public void setExludeServiceMap(HashMap<String, String> exludeServiceMap)
        {
            this.exludeServiceMap = exludeServiceMap;
        }

        /**
         * @return the measurementUnit
         */
        public String getMeasurementUnit()
        {
            return measurementUnit;
        }

        /**
         * @param measurementUnit
         *            the measurementUnit to set
         */
        public void setMeasurementUnit(String measurementUnit)
        {
            this.measurementUnit = measurementUnit;
        }

        /**
         * @return the handling
         */
        public BigDecimal getHandling()
        {
            return handling;
        }

        /**
         * @param handling
         *            the handling to set
         */
        public void setHandling(BigDecimal handling)
        {
            this.handling = handling;
        }

        /**
         * @return the weightPackageList
         */
        public List<WeightPackage> getWeightPackageList()
        {
            return weightPackageList;
        }

        /**
         * @param weightPackageList
         *            the weightPackageList to set
         */
        public void setWeightPackageList(List<WeightPackage> weightPackageList)
        {
            this.weightPackageList = weightPackageList;
        }
    }
}