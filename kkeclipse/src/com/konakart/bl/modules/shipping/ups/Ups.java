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

package com.konakart.bl.modules.shipping.ups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.torque.TorqueException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import com.workingdogs.village.DataSetException;

/**
 * This shipping module communicates with UPS to retrieve shipping rates. Using the Admin App you
 * must set up the following configuration variables:
 * 
 * UPS Module:
 * 
 * <ul>
 * <li>UPS Access License Number</li>
 * <li>Request URL (test: https://wwwcie.ups.com/ups.app/xml/Rate production:
 * https://onlinetools.ups.com/ups.app/xml/Rate)</li>
 * <li>UPS UserId</li>
 * <li>UPS Password</li>
 * <li>Return quote for a defined service code : Defaults to false so that quotes are returned for
 * all services that UPS gives for the specified delivery address.</li>
 * <li>UPS Service code : If the above is set to true, then we only return a quote for this service</li>
 * <li>UPS Service codes to exclude : Comma separated list of service codes to exclude</li>
 * <li>Measurement Unit : LBS or KGS</li>
 * <li>Packaging type by weight : Defaults to 1000:00 where 00 is the packaging code (unknown
 * packaging) for up to 1000 Lbs or Kgs. You could set it to something like 10:25,25:24,100:00 which
 * means that for up to 10lbs we will ask for package code 25. For 10 to 25 lbs we will ask for
 * package code 24 and for 25 to 100 lbs we will ask for package code 00. Before setting specific
 * package codes you need to be sure that UPS supports the package for the destination.</li>
 * <li>Handling fee : Defaults to 0. It is added to the charge returned by UPS.</li>
 * <li></li>
 * </ul>
 * 
 * Shipping and Packaging under Configuration
 * 
 * <ul>
 * <li>Country of Origin : Country from which packages will be shipped.</li>
 * <li>Postal Code : Postal code of area from which packages will be shipped</li>
 * <li>Maximum package weight : This is used to split the order into multiple packages . We make
 * multiple calls to UPS and sum the costs to create a total.</li>
 * <li>Package tare weight : This is added to the weight of the items being shipped per package</li>
 * </ul>
 */
public class Ups extends BaseShippingModule implements ShippingInterface
{// Module name must be the same as the class name although it can be all in lowercase
    private static String code = "ups";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.ups.Ups";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "upsMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys

    private final static String MODULE_SHIPPING_UPS_LICENSE_NUMBER = "MODULE_SHIPPING_UPS_LICENSE_NUMBER";

    private final static String MODULE_SHIPPING_UPS_URL = "MODULE_SHIPPING_UPS_URL";

    private final static String MODULE_SHIPPING_UPS_USERID = "MODULE_SHIPPING_UPS_USERID";

    private final static String MODULE_SHIPPING_UPS_PASSWORD = "MODULE_SHIPPING_UPS_PASSWORD";

    private final static String MODULE_SHIPPING_UPS_RATE_OR_SHOP = "MODULE_SHIPPING_UPS_RATE_OR_SHOP";

    private final static String MODULE_SHIPPING_UPS_SERVICE_CODE = "MODULE_SHIPPING_UPS_SERVICE_CODE";

    private final static String MODULE_SHIPPING_UPS_SERVICE_CODES_EXCLUDE = "MODULE_SHIPPING_UPS_SERVICE_CODES_EXCLUDE";

    private final static String MODULE_SHIPPING_UPS_ZONE = "MODULE_SHIPPING_UPS_ZONE";

    private final static String MODULE_SHIPPING_UPS_SORT_ORDER = "MODULE_SHIPPING_UPS_SORT_ORDER";

    private final static String MODULE_SHIPPING_UPS_TAX_CLASS = "MODULE_SHIPPING_UPS_TAX_CLASS";

    private final static String MODULE_SHIPPING_UPS_STATUS = "MODULE_SHIPPING_UPS_STATUS";

    private final static String MODULE_SHIPPING_UPS_MEASUREMENT_UNIT = "MODULE_SHIPPING_UPS_MEASUREMENT_UNIT";

    private final static String MODULE_SHIPPING_UPS_PACKAGING_TYPE = "MODULE_SHIPPING_UPS_PACKAGING_TYPE";

    private final static String MODULE_SHIPPING_UPS_HANDLING = "MODULE_SHIPPING_UPS_HANDLING";

    // Message Catalogue Keys

    private final static String MODULE_SHIPPING_UPS_TEXT_TITLE = "module.shipping.ups.text.title";

    // private final static String MODULE_SHIPPING_UPS_TEXT_DESCRIPTION =
    // "module.shipping.ups.text.description";

    // private final static String MODULE_SHIPPING_UPS_TEXT_WAY = "module.shipping.ups.text.way";

    /**
     * Constructor
     * 
     * @param eng
     * 
     * @throws DataSetException
     * @throws KKException
     * @throws TorqueException
     */
    public Ups(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
                    setRateServiceMap();
                }
            }
        }
    }

    /**
     * We get a quote from UPS. If we only get one quote back then we return a ShippingQuote object
     * with the details of the quote. If we get back more than one quote then we create an array of
     * ShippingQuote objects and attach them to the ShippingQuote object that we return. In this
     * case all of the quotes must be in the array since the quote itself isn't processed.
     * 
     * It becomes more complex when we have the shipment split into a number of packages. The split
     * is done by the manager calling this method and is presented to us in an orderWeightList. For
     * each item in the list, we call UPS and get back one or more quotes. We then have to add up
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
         * We must send off a request to UPS for each package and add up the total cost.
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
            ShippingQuote quote = getQuotesFromUPS(order, info, rb, weight);
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
        // size > 1
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
         * this because every time we get a quote from UPS we don't necessarily get back the same
         * rate codes . i.e. First time we may get UPS Saver and UPS Worldwide Express. The quote
         * for the 2nd package may only contain UPS Saver. If this is the case we have to make sure
         * that we only return UPS Saver.
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
            throw new Exception(
                    "The shipment was split up into multiple packages so we made multiple calls to UPS. UPS didn't return an identical rate across all calls ");
        }

        // Only do a purge if the number of quotes we can keep isn't equal to the number we got
        // from UPS. We have to create a new array with the quotes that we can keep.
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
                                + " from quote because it isn't present in all calls to UPS");
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
     * Returns the package code based on the weight
     * 
     * @return Returns a string containing the package code
     * @throws KKException
     */
    private String getPackageForWeight(BigDecimal weight) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        for (Iterator<WeightPackage> iter1 = sd.getWeightPackageList().iterator(); iter1.hasNext();)
        {
            WeightPackage wp = iter1.next();
            if (weight.compareTo(wp.getWeight()) == -1)
            {
                return wp.getPck();
            }
        }

        // Return a default package if can't find one in the list
        return "00";
    }

    /**
     * Get quotes from UPS for a single delivery
     * 
     * @param order
     * @param info
     * @param rb
     * @param weight
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    private ShippingQuote getQuotesFromUPS(Order order, ShippingInfo info, ResourceBundle rb,
            BigDecimal weight) throws Exception
    {
        String accessReq = getAccessRequest();
        String selectionReq = getRatingServiceSelectionRequest(order, info, weight);

        String request = accessReq + selectionReq;

        if (log.isDebugEnabled())
        {
            log.debug("Request to UPS = " + request + "\n\n");
        }

        String response;
        try
        {
            response = sendRequest(request);
        } catch (RuntimeException e)
        {
            e.printStackTrace();
            throw new Exception(
                    "Exception received while trying to send shipping request to UPS. See stack trace above.");
        }

        if (log.isDebugEnabled())
        {
            log.debug("Response from UPS = " + response + "\n\n");
        }

        List<ShippingQuote> quoteList = getQuotesFromResponse(response);

        if (quoteList.size() == 0)
        {
            // No reply from UPS
            throw new Exception("Unrecognised response from UPS:\n" + response);
        } else if (quoteList.size() == 1)
        {
            // This could either be an error from UPS or a quote
            ShippingQuote sq = quoteList.get(0);
            if (sq.getCustom1().equals("0"))
            {
                StringBuffer sb = new StringBuffer();
                sb.append("There has been an error returned from UPS for the request :\n");
                sb.append(request);
                sb.append("\n");
                sb.append("The error details are :\n");
                sb.append("Severity = ");
                sb.append(sq.getCustom3());
                sb.append("\n");
                sb.append("Code = ");
                sb.append(sq.getCustom2());
                sb.append("\n");
                sb.append("Description = ");
                sb.append(sq.getCustom4());
                sb.append("\n");
                throw new Exception(sb.toString());
            }
            if (excludeQuote(sq))
            {
                throw new Exception("UPS only returned one quote, code = " + sq.getCustom5()
                        + " which is on the exclude list.");
            }
            // Populate locale specific attributes from the resource bundle
            sq.setResponseText(sq.getDescription());
            sq.setTitle(rb.getString(MODULE_SHIPPING_UPS_TEXT_TITLE));
            sq.setModuleCode(code);
            setQuotePrices(sq, info);
            return sq;

        } else
        {
            // Fill in the description and title for each quote and create an array of quotes
            ShippingQuoteIf[] sqArray = new ShippingQuoteIf[quoteList.size()];
            int i = 0;
            for (Iterator<ShippingQuote> iterator = quoteList.iterator(); iterator.hasNext();)
            {
                ShippingQuote sq = iterator.next();
                if (excludeQuote(sq))
                {
                    continue;
                }
                sq.setResponseText(sq.getDescription());
                sq.setTitle(rb.getString(MODULE_SHIPPING_UPS_TEXT_TITLE));
                setQuotePrices(sq, info);
                sq.setCode(sq.getCode() + "_" + i);
                sqArray[i] = sq;
                i++;
            }

            // The array may not be fully populated or populated at all since the quotes may have
            // been excluded. We figure out how many quotes we should return.
            int quoteCount = 0;
            for (int j = 0; j < sqArray.length; j++)
            {
                if (sqArray[j] != null)
                {
                    quoteCount++;
                }
            }

            // Throw an exception if all quotes have been excluded
            if (quoteCount == 0)
            {
                throw new Exception("UPS returned " + sqArray.length
                        + " quotes, which are all on the exclude list.");
            }

            // Create a new array of the correct size and populate it with the quotes that we will
            // return
            ShippingQuoteIf[] sqArray1 = new ShippingQuoteIf[quoteCount];
            for (int j = 0; j < sqArray1.length; j++)
            {
                sqArray1[j] = sqArray[j];
            }

            // If we have multiple quotes we add them as an array of quotes to the return quote
            // which itself is ignored
            ShippingQuote retQ = new ShippingQuote();
            retQ.setQuotes(sqArray1);
            return retQ;
        }
    }

    /**
     * Return true if we need to exclude the quote
     * 
     * @param quote
     * @return Returns a boolean
     * @throws KKException
     */
    private boolean excludeQuote(ShippingQuote quote) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        boolean ret = false;
        if (quote.getCustom5() != null)
        {
            String exclude = sd.getExludeServiceMap().get(quote.getCustom5());
            if (exclude != null)
            {
                return true;
            }
        }
        return ret;
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
     * Creates a list of ShippingQuote objects from the UPS response
     * 
     * @param response
     * @return Returns a list of Shipping Quote objects
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws KKException
     */
    private List<ShippingQuote> getQuotesFromResponse(String response)
            throws ParserConfigurationException, SAXException, IOException, KKException
    {
        // Create the return list
        List<ShippingQuote> quoteList = new ArrayList<ShippingQuote>();

        // Create a DOM structure from the return XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        // File file = new File("c:/temp/ups2.xml");
        Document doc = builder.parse(new InputSource(new StringReader(response)));
        traverse(doc, quoteList);

        return quoteList;
    }

    /**
     * Based on the node type we decide what to do
     * 
     * @param cNode
     * @throws KKException
     */
    private void traverse(Node cNode, List<ShippingQuote> quoteList) throws KKException
    {
        switch (cNode.getNodeType())
        {
        case Node.DOCUMENT_NODE:
            processChildren(cNode.getChildNodes(), quoteList);
            break;

        case Node.ELEMENT_NODE:
            processNode(cNode, quoteList);
            processChildren(cNode.getChildNodes(), quoteList);
            break;
        case Node.CDATA_SECTION_NODE:
        case Node.TEXT_NODE:
            if (!cNode.getNodeValue().trim().equals(""))
            {
                processNode(cNode, quoteList);
            }
            break;
        }
    }

    /**
     * Process the child nodes of the node passed in as a parameter
     * 
     * @param nList
     * @throws KKException
     */
    private void processChildren(NodeList nList, List<ShippingQuote> quoteList) throws KKException
    {
        if (nList.getLength() != 0)
        {
            for (int i = 0; i < nList.getLength(); i++)
                traverse(nList.item(i), quoteList);
        }
    }

    /**
     * Process the individual node
     * 
     * @param cNode
     * @throws KKException
     */
    private void processNode(Node cNode, List<ShippingQuote> quoteList) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        ShippingQuote currentQuote = null;

        if (quoteList.size() > 0)
        {
            currentQuote = quoteList.get(quoteList.size() - 1);
        }

        if (cNode.getNodeName().equals("RatedShipment"))
        {
            ShippingQuote sq = new ShippingQuote();
            sq.setCode(code);
            sq.setModuleCode(code);
            sq.setSortOrder(sd.getSortOrder());
            sq.setIcon(icon);
            sq.setTaxClass(sd.getTaxClass());
            sq.setCustom1("1");

            quoteList.add(sq);

        } else if (cNode.getNodeName().equals("#text"))
        {
            Node parentNode = cNode.getParentNode();
            Node grandParentNode = (parentNode != null) ? parentNode.getParentNode() : null;
            Node greatGrandParentNode = (grandParentNode != null) ? grandParentNode.getParentNode()
                    : null;
            if (parentNode != null && parentNode.getNodeName().equals("Code"))
            {
                if (grandParentNode != null && grandParentNode.getNodeName().equals("Service"))
                {
                    if (currentQuote != null)
                    {
                        String serviceDesc = sd.getRateServiceCodeMap().get(cNode.getNodeValue());
                        // If we can't find the description of the service then we return the code.
                        // This should never happen.
                        serviceDesc = serviceDesc == null ? "UPS Service Code = "
                                + cNode.getNodeValue() : serviceDesc;
                        currentQuote.setDescription(serviceDesc);
                        // We save the code anyway in custom5 so that we can decide whether to
                        // return the quote to the customer. Some codes can be excluded.
                        currentQuote.setCustom5(cNode.getNodeValue());
                        currentQuote.setShippingServiceCode(cNode.getNodeValue());
                    }
                }
            } else if (parentNode != null && parentNode.getNodeName().equals("MonetaryValue"))
            {
                if (grandParentNode != null && grandParentNode.getNodeName().equals("TotalCharges"))
                {
                    if (greatGrandParentNode != null
                            && greatGrandParentNode.getNodeName().equals("RatedShipment"))
                    {
                        if (currentQuote != null)
                        {
                            BigDecimal cost = new BigDecimal(cNode.getNodeValue());
                            currentQuote.setCost(cost);
                        }
                    }
                }
            } else if (parentNode != null && parentNode.getNodeName().equals("ResponseStatusCode"))
            {
                if (grandParentNode != null && grandParentNode.getNodeName().equals("Response"))
                {
                    if (cNode.getNodeValue().equals("0"))
                    {
                        // Create a new Shipping quote for the error
                        ShippingQuote sq = new ShippingQuote();
                        quoteList.add(sq);
                        // Error response in custom 1
                        sq.setCustom1(cNode.getNodeValue());
                    }
                }

            } else if (parentNode != null && parentNode.getNodeName().equals("ErrorCode"))
            {
                if (grandParentNode != null && grandParentNode.getNodeName().equals("Error"))
                {
                    if (currentQuote != null)
                    {
                        // Error Code in Custom2
                        currentQuote.setCustom2(cNode.getNodeValue());
                    }
                }
            } else if (parentNode != null && parentNode.getNodeName().equals("ErrorSeverity"))
            {
                if (grandParentNode != null && grandParentNode.getNodeName().equals("Error"))
                {
                    if (currentQuote != null)
                    {
                        // Error Severity in Custom3
                        currentQuote.setCustom3(cNode.getNodeValue());
                    }
                }
            } else if (parentNode != null && parentNode.getNodeName().equals("ErrorDescription"))
            {
                if (grandParentNode != null && grandParentNode.getNodeName().equals("Error"))
                {
                    if (currentQuote != null)
                    {
                        // Error Description in Custom4
                        currentQuote.setCustom4(cNode.getNodeValue());
                    }
                }
            }
        }
    }

    /**
     * Creates the XML access request:
     * 
     * <?xml version="1.0" ?>
     * 
     * <AccessRequest xml:lang='en-US'>
     * 
     * <AccessLicenseNumber>YOURACCESSLICENSENUMBER</AccessLicenseNumber>
     * 
     * <UserId>YOURUSERID</UserId>
     * 
     * <Password>YOURPASSWORD</Password>
     * 
     * </AccessRequest>
     * 
     * @return XML Access Request
     * @throws KKException
     */
    private String getAccessRequest() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        StringBuffer ret = new StringBuffer();
        ret.append("<?xml version=\"1.0\" ?>");
        ret.append("<AccessRequest xml:lang='en-US'>");
        ret.append("<AccessLicenseNumber>").append(sd.getLicense())
                .append("</AccessLicenseNumber>");
        ret.append("<UserId>").append(sd.getUserid()).append("</UserId>");
        ret.append("<Password>").append(sd.getPassword()).append("</Password>");
        ret.append("</AccessRequest>");

        return ret.toString();
    }

    /**
     * Formulate the main part of the request
     * 
     * @param order
     * @param info
     * @param weight
     * @return Return the main part of the request in xml format
     * @throws KKException
     */
    private String getRatingServiceSelectionRequest(Order order, ShippingInfo info,
            BigDecimal weight) throws KKException
    {
        // <Request>
        // --<RequestAction>Rate</RequestAction>
        // --<RequestOption>Rate</RequestOption>
        // </Request>
        // <Shipment>
        // --<Shipper>
        // ----<Name>Imani Carr</Name>
        // ----<AttentionName>AT:United Kingdom</AttentionName>
        // ----<TaxIdentificationNumber>444333787618928</TaxIdentificationNumber>
        // ----<PhoneNumber>3057449002</PhoneNumber>
        // ----<FaxNumber>3054439293</FaxNumber>
        // ----<ShipperNumber>ISGB01</ShipperNumber>
        // ----<Address>
        // ------<AddressLine1>Southam Rd</AddressLine1>
        // ------<AddressLine2 />
        // ------<AddressLine3 />
        // ------<City>Dunchurch</City>
        // ------<StateProvinceCode>Warwickshire</StateProvinceCode>
        // ------<PostalCode>CV226PD</PostalCode>
        // ------<CountryCode>GB</CountryCode>
        // ----</Address>
        // --</Shipper>
        // --<ShipTo>
        // ----<Address>
        // ------<AddressLine1>5, rue de la Bataille</AddressLine1>
        // ------<AddressLine2 />
        // ------<AddressLine3 />
        // ------<City>Neufchateau</City>
        // ------<PostalCode>6840</PostalCode>
        // ------<CountryCode>BE</CountryCode>
        // ----</Address>
        // --</ShipTo>
        // --<ShipFrom>
        // ----<Address>
        // ------<AddressLine1>Southam Rd</AddressLine1>
        // ------<AddressLine2 />
        // ------<AddressLine3 />
        // ------<City>Dunchurch</City>
        // ------<StateProvinceCode>Warwickshire</StateProvinceCode>
        // ------<PostalCode>CV226PD</PostalCode>
        // ------<CountryCode>GB</CountryCode>
        // ----</Address>
        // --</ShipFrom>
        // --<Service>
        // ----<Code>65</Code>
        // --</Service>
        // --<Package>
        // ----<PackagingType>
        // ------<Code>04</Code>
        // ------<Description>UPS 25KG Box</Description>
        // ----</PackagingType>
        // ----<PackageWeight>
        // ------<UnitOfMeasurement>
        // --------<Code>KGS</Code>
        // ------</UnitOfMeasurement>
        // ------<Weight>23</Weight>
        // ----</PackageWeight>
        // --</Package>
        // </Shipment>
        StaticData sd = staticDataHM.get(getStoreId());
        StringBuffer ret = new StringBuffer();
        /*
         * Start tags
         */
        ret.append("<?xml version=\"1.0\" ?>");
        ret.append("<RatingServiceSelectionRequest>");

        /*
         * Request
         */
        ret.append("<Request>");
        ret.append("<RequestAction>").append("Rate").append("</RequestAction>");
        ret.append("<RequestOption>").append(sd.isRate() ? "Rate" : "Shop").append(
                "</RequestOption>");
        ret.append("</Request>");

        /*
         * Shipment
         */
        ret.append("<Shipment>");

        /*
         * Shipper
         */
        ret.append("<Shipper>");
        ret.append("<Address>");
        if (info.getOriginZip() != null)
        {
            ret.append("<PostalCode>").append(info.getOriginZip()).append("</PostalCode>");
        }

        if (info.getOriginCountry() != null)
        {
            ret.append("<CountryCode>").append(info.getOriginCountry().getIsoCode2()).append(
                    "</CountryCode>");
        }
        ret.append("</Address>");
        ret.append("</Shipper>");

        /*
         * ShipTo
         */
        ret.append("<ShipTo>");
        ret.append("<Address>");
        ret.append("<AddressLine1>").append(order.getDeliveryStreetAddress()).append(
                "</AddressLine1>");
        if (order.getDeliverySuburb() != null && order.getDeliverySuburb().length() > 0)
        {
            ret.append("<AddressLine2>").append(order.getDeliverySuburb())
                    .append("</AddressLine2>");
        }
        ret.append("<City>").append(order.getDeliveryCity()).append("</City>");
        ret.append("<PostalCode>").append(order.getDeliveryPostcode()).append("</PostalCode>");
        ret.append("<CountryCode>").append(info.getDeliveryCountry().getIsoCode2()).append(
                "</CountryCode>");
        ret.append("</Address>");
        ret.append("</ShipTo>");

        /*
         * ShipFrom
         */
        ret.append("<ShipFrom>");
        ret.append("<Address>");
        if (info.getOriginZip() != null)
        {
            ret.append("<PostalCode>").append(info.getOriginZip()).append("</PostalCode>");
        }

        if (info.getOriginCountry() != null)
        {
            ret.append("<CountryCode>").append(info.getOriginCountry().getIsoCode2()).append(
                    "</CountryCode>");
        }
        ret.append("</Address>");
        ret.append("</ShipFrom>");

        /*
         * Service
         */
        if (sd.isRate())
        {
            ret.append("<Service>");
            ret.append("<Code>").append(sd.getServiceCode()).append("</Code>");
            ret.append("</Service>");
        }

        /*
         * Package
         */
        ret.append("<Package>");

        /*
         * Packaging Type
         */
        ret.append("<PackagingType>");
        // 01 == Shipper Supplied
        ret.append("<Code>").append(getPackageForWeight(weight)).append("</Code>");
        ret.append("</PackagingType>");

        /*
         * Packaging Weight
         */
        ret.append("<PackageWeight>");
        ret.append("<UnitOfMeasurement>");
        ret.append("<Code>").append(sd.getMeasurementUnit()).append("</Code>");
        ret.append("</UnitOfMeasurement>");
        ret.append("<Weight>").append(weight.setScale(1, BigDecimal.ROUND_HALF_UP)).append(
                "</Weight>");
        ret.append("</PackageWeight>");

        /*
         * End Tags
         */
        ret.append("</Package>");
        ret.append("</Shipment>");
        ret.append("</RatingServiceSelectionRequest>");

        return ret.toString();
    }

    /**
     * Send the request to UPS
     * 
     * @param request
     * @return Returns the response
     * @throws IOException
     * @throws KKException
     */
    private String sendRequest(String request) throws IOException, KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        URL url = new URL(sd.getUpsUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.println(request);
        out.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        // Get the response
        StringBuffer respSb = new StringBuffer();
        String line = in.readLine();
        while (line != null)
        {
            respSb.append(line);
            line = in.readLine();
        }

        in.close();

        return respSb.toString();
    }

    /**
     * Returns true or false
     * 
     * @throws KKException
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable(MODULE_SHIPPING_UPS_STATUS);
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

        conf = getConfiguration(MODULE_SHIPPING_UPS_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_UPS_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_UPS_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_UPS_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_URL must be set to the URL where the XML messages are sent (i.e. https://onlinetools.ups.com/ups.app/xml/Rate )");
        }
        staticData.setUpsUrl(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_UPS_LICENSE_NUMBER);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_LICENSE_NUMBER must be set to the UPS Access License Number for using the API");
        }
        staticData.setLicense(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_UPS_USERID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_USERID must be set to the UPS UserId for using the API");
        }
        staticData.setUserid(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_UPS_PASSWORD);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_PASSWORD must be set to the UPS password for using the API");
        }
        staticData.setPassword(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_UPS_MEASUREMENT_UNIT);
        if (conf == null || !(conf.getValue().equals("KGS") || conf.getValue().equals("LBS")))
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_MEASUREMENT_UNIT must be set to KGS or LBS");
        }
        staticData.setMeasurementUnit(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_UPS_HANDLING);
        if (conf == null)
        {
            staticData.setHandling(new BigDecimal(0));
        } else
        {
            staticData.setHandling(new BigDecimal(conf.getValue()));
        }

        conf = getConfiguration(MODULE_SHIPPING_UPS_RATE_OR_SHOP);
        if (conf == null)
        {
            staticData.setRate(false);
        } else
        {
            if (conf.getValue().equalsIgnoreCase("true"))
            {
                staticData.setRate(true);
            } else
            {
                staticData.setRate(false);
            }
        }

        conf = getConfiguration(MODULE_SHIPPING_UPS_SERVICE_CODE);
        if ((conf == null || conf.getValue() == null || conf.getValue().length() == 0)
                && staticData.isRate())
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_SERVICE_CODE must be set to a valid service code for receiving a quote for a single rate.");
        }
        if (conf != null)
        {
            staticData.setServiceCode(conf.getValue());
        }

        createWeightPackageList();
        setExcludeServiceMap();
    }

    /**
     * Fills a hash map with descriptions for the UPS services and puts in the service code as the
     * key. In the UPS response we only get back the code so we have to get a description to show to
     * the customer so that he can make a selection.
     * 
     * @throws KKException
     */
    private void setRateServiceMap() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        if (sd.getRateServiceCodeMap().isEmpty())
        {
            sd.getRateServiceCodeMap().put("01", "UPS Next Day Air");
            sd.getRateServiceCodeMap().put("02", "UPS Second Day Air");
            sd.getRateServiceCodeMap().put("03", "UPS Ground");
            sd.getRateServiceCodeMap().put("07", "UPS Worldwide ExpressSM");
            sd.getRateServiceCodeMap().put("08", "UPS Worldwide ExpeditedSM");
            sd.getRateServiceCodeMap().put("11", "UPS Standard");
            sd.getRateServiceCodeMap().put("12", "UPS Three-Day Select");
            sd.getRateServiceCodeMap().put("13", "UPS Next Day Air Saver");
            sd.getRateServiceCodeMap().put("14", "UPS Next Day Air Early A.M. SM");
            sd.getRateServiceCodeMap().put("54", "UPS Worldwide Express PlusSM");
            sd.getRateServiceCodeMap().put("59", "UPS Second Day Air A.M.");
            sd.getRateServiceCodeMap().put("65", "UPS Saver");
            sd.getRateServiceCodeMap().put("82", "UPS Today StandardSM");
            sd.getRateServiceCodeMap().put("83", "UPS Today Dedicated CourrierSM");
            sd.getRateServiceCodeMap().put("84", "UPS Today Intercity");
            sd.getRateServiceCodeMap().put("85", "UPS Today Express");
            sd.getRateServiceCodeMap().put("86", "UPS Today Express Saver");
        }
    }

    /**
     * From the configuration data, we fill a list with the package that we need to choose based on
     * the weight. The config data should contain something like this : 5:4,10:6,25:5 which means
     * that from weight 0 to 5 we use package code 4. Then from weight 5 to 10 we use package code 6
     * etc.
     * 
     * @throws Exception
     */
    private void createWeightPackageList() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        KKConfiguration conf = getConfiguration(MODULE_SHIPPING_UPS_PACKAGING_TYPE);
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
                        WeightPackage wp = new WeightPackage(new BigDecimal(weightPackageArray[0]),
                                weightPackageArray[1]);
                        sd.getWeightPackageList().add(wp);
                    } else
                    {
                        throw new KKException(
                                "UPS Packaging type by weight must be in the format weight:PackageCode,weight1:packageCode1,weight2:packageCode2 etc.");
                    }
                }
            }
        } else
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_UPS_PACKAGING_TYPE must be set to a valid value");
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
        KKConfiguration conf = getConfiguration(MODULE_SHIPPING_UPS_SERVICE_CODES_EXCLUDE);
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
     * Class used to keep the package code for a weight
     */
    private class WeightPackage
    {
        private BigDecimal weight;

        private String pck;

        /**
         * Constructor
         * 
         * @param weight
         * @param pck
         */
        public WeightPackage(BigDecimal weight, String pck)
        {
            setWeight(weight);
            setPck(pck);
        }

        public String toString()
        {
            StringBuffer str = new StringBuffer();
            str.append("WeightPackage:\n");
            str.append("weight          = ").append(getWeight()).append("\n");
            str.append("pck             = ").append(getPck()).append("\n");
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
         * @return Returns the pck.
         */
        public String getPck()
        {
            return pck;
        }

        /**
         * @param pck
         *            The pck to set.
         */
        public void setPck(String pck)
        {
            this.pck = pck;
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

        // UPS license number to use the API
        private String license;

        // URL where XML messages are sent
        private String upsUrl;

        // UserId for using the API
        private String userid;

        // Password for using the API
        private String password;

        // If true then we ask for a single rate. If false we shop for all rates.
        private boolean rate;

        // If rate == true this is the service code for which we receive a quote
        private String serviceCode;

        private HashMap<String, String> exludeServiceMap = new HashMap<String, String>();

        // Hash Map containing descriptions for the rate service codes
        private HashMap<String, String> rateServiceCodeMap = new HashMap<String, String>();

        // Measurement unit
        private String measurementUnit = "";

        // Handling charge
        private BigDecimal handling;

        // List that contains UPS package codes based on the weight
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
         * @return the license
         */
        public String getLicense()
        {
            return license;
        }

        /**
         * @param license
         *            the license to set
         */
        public void setLicense(String license)
        {
            this.license = license;
        }

        /**
         * @return the upsUrl
         */
        public String getUpsUrl()
        {
            return upsUrl;
        }

        /**
         * @param upsUrl
         *            the upsUrl to set
         */
        public void setUpsUrl(String upsUrl)
        {
            this.upsUrl = upsUrl;
        }

        /**
         * @return the userid
         */
        public String getUserid()
        {
            return userid;
        }

        /**
         * @param userid
         *            the userid to set
         */
        public void setUserid(String userid)
        {
            this.userid = userid;
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
         * @return the rate
         */
        public boolean isRate()
        {
            return rate;
        }

        /**
         * @param rate
         *            the rate to set
         */
        public void setRate(boolean rate)
        {
            this.rate = rate;
        }

        /**
         * @return the serviceCode
         */
        public String getServiceCode()
        {
            return serviceCode;
        }

        /**
         * @param serviceCode
         *            the serviceCode to set
         */
        public void setServiceCode(String serviceCode)
        {
            this.serviceCode = serviceCode;
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
         * @return the rateServiceCodeMap
         */
        public HashMap<String, String> getRateServiceCodeMap()
        {
            return rateServiceCodeMap;
        }

        /**
         * @param rateServiceCodeMap
         *            the rateServiceCodeMap to set
         */
        public void setRateServiceCodeMap(HashMap<String, String> rateServiceCodeMap)
        {
            this.rateServiceCodeMap = rateServiceCodeMap;
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
