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

package com.konakart.bl.modules.shipping.usps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
import org.w3c.dom.NamedNodeMap;
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
import com.konakart.util.PrettyXmlPrinter;
import com.konakart.util.Utils;
import com.workingdogs.village.DataSetException;

/**
 * This shipping module communicates with USPS to retrieve shipping rates. Using the Admin App you
 * must set up the following configuration variables:
 * 
 * USPS Module:
 * 
 * <ul>
 * <li>USPS User Id which you need to receive from USPS</li>
 * <li>Request URL (production: http://production.shippingapis.com/ShippingAPI.dll)</li>
 * <li>USPS Service code. This is the code for the service that you want. Valid codes are:
 * <ul>
 * <li>ALL - Returns all available rates</li>
 * <li>FIRST CLASS</li>
 * <li>PRIORITY</li>
 * <li>PRIORITY COMMERCIAL</li>
 * <li>EXPRESS</li>
 * <li>EXPRESS SH</li>
 * <li>EXPRESS HFP</li>
 * <li>EXPRESS COMMERCIAL</li>
 * <li>EXPRESS SH COMMERCIAL</li>
 * <li>EXPRESS HFP COMMERCIAL</li>
 * <li>PARCEL POST</li>
 * <li>BPM</li>
 * <li>MEDIA MAIL</li>
 * <li>LIBRARY MAIL</li>
 * </ul>
 * </li>
 * <li>USPS Service codes to exclude. You could ask for all rates but decide to exclude certain
 * ones. The excluded rates are defined by comma separated codes. The codes are:
 * <ul>
 * <li>0 First-Class</li>
 * <li>1 Priority Mail</li>
 * <li>2 Express Mail Hold for Pickup</li>
 * <li>3 Express Mail PO to Addressee</li>
 * <li>4 Parcel Post</li>
 * <li>5 Bound Printed Matter</li>
 * <li>6 Media Mail</li>
 * <li>7 Library</li>
 * <li>12 First-Class Postcard Stamped</li>
 * <li>13 Express Mail Flat-Rate Envelope</li>
 * <li>16 Priority Mail Flat-Rate Envelope</li>
 * <li>17 Priority Mail Flat-Rate Box</li>
 * <li>18 Priority Mail Keys and IDs</li>
 * <li>19 First-Class Keys and IDs</li>
 * <li>22 Priority Mail Flat-Rate Large Box</li>
 * <li>23 Express Mail Sunday/Holiday</li>
 * <li>25 Express Mail Flat-Rate Envelope Sunday/Holiday</li>
 * <li>27 Express Mail Flat-Rate Envelope Hold For Pickup</li>
 * </ul>
 * </li>
 * <li>Size by weight. For many of the rates, UPS requires a size to be specified. Valid values are:
 * <ul>
 * <li>REGULAR</li>
 * <li>LARGE</li>
 * <li>OVERSIZE</li>
 * </ul>
 * This configuration variable allows you to specify different sizes based on the weight of the
 * delivery. The formatting should look something like this: 10:REGULAR,25:LARGE,71:OVERSIZE which
 * means that a REGULAR size is used from 0 to 9lbs, a LARGE size from 10lbs to 24lbs and an
 * OVERSIZE size from 25lbs to 70lbs.</li>
 * <li>Container Type defines the type of container used for the package. Valid values are:
 * <ul>
 * <li>RECTANGULAR</li>
 * <li>NONRECTANGULAR</li>
 * <li>LG FLAT RATE BOX</li>
 * <li>FLAT RATE BOX</li>
 * <li>FLAT RATE ENV.</li>
 * <li>LG FLAT RATE BOX</li>
 * <li>VARIABLE</li>
 * </ul>
 * </li>
 * <li>Machinable Package can be set to true or false. The USPS definitions for machinable can be
 * found here: http://pe.usps.com/text/dmm300/101.htm</li>
 * <li>When First Class is selected, the package must be LETTER, FLAT or PARCEL.</li>
 * <li>Handling fee : Defaults to 0. It is added to the charge returned by USPS.</li>
 * <li></li>
 * </ul>
 * 
 * Shipping and Packaging under Configuration
 * 
 * <ul>
 * <li>Country of Origin : Country from which packages will be shipped.</li>
 * <li>Postal Code : Postal code of area from which packages will be shipped</li>
 * <li>Maximum package weight : This is used to split the order into multiple packages . We make
 * multiple calls to USPS and sum the costs to create a total.</li>
 * <li>Package tare weight : This is added to the weight of the items being shipped per package</li>
 * </ul>
 */
public class Usps extends BaseShippingModule implements ShippingInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "usps";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.usps.Usps";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "uspsMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys
    private final static String MODULE_SHIPPING_USPS_TAX_CLASS = "MODULE_SHIPPING_USPS_TAX_CLASS";

    private final static String MODULE_SHIPPING_USPS_STATUS = "MODULE_SHIPPING_USPS_STATUS";

    private final static String MODULE_SHIPPING_USPS_ZONE = "MODULE_SHIPPING_USPS_ZONE";

    private final static String MODULE_SHIPPING_USPS_SORT_ORDER = "MODULE_SHIPPING_USPS_SORT_ORDER";

    private final static String MODULE_SHIPPING_USPS_URL = "MODULE_SHIPPING_USPS_URL";

    private final static String MODULE_SHIPPING_USPS_USERID = "MODULE_SHIPPING_USPS_USERID";

    private final static String MODULE_SHIPPING_USPS_SERVICE_CODE = "MODULE_SHIPPING_USPS_SERVICE_CODE";

    private final static String MODULE_SHIPPING_USPS_SERVICE_CODES_EXCLUDE = "MODULE_SHIPPING_USPS_SERVICE_CODES_EXCLUDE";

    private final static String MODULE_SHIPPING_USPS_MACHINABLE = "MODULE_SHIPPING_USPS_MACHINABLE";

    private final static String MODULE_SHIPPING_USPS_FIRST_CLASS_PACKAGE = "MODULE_SHIPPING_USPS_FIRST_CLASS_PACKAGE";

    private final static String MODULE_SHIPPING_USPS_HANDLING = "MODULE_SHIPPING_USPS_HANDLING";

    private final static String MODULE_SHIPPING_USPS_SIZE = "MODULE_SHIPPING_USPS_SIZE";

    private final static String MODULE_SHIPPING_USPS_CONTAINER = "MODULE_SHIPPING_USPS_CONTAINER";

    // Message Catalog Keys
    private final static String MODULE_SHIPPING_USPS_TEXT_TITLE = "module.shipping.usps.text.title";

    // Service types
    private final static String FIRST_CLASS = "FIRST CLASS";

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
    public Usps(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
     * We get a quote from USPS. If we only get one quote back then we return a ShippingQuote object
     * with the details of the quote. If we get back more than one quote then we create an array of
     * ShippingQuote objects and attach them to the ShippingQuote object that we return. In this
     * case all of the quotes must be in the array since the quote itself isn't processed.
     * 
     * It becomes more complex when we have the shipment split into a number of packages. The split
     * is done by the manager calling this method and is presented to us in an orderWeightList. For
     * each item in the list, we call USPS and get back one or more quotes. We then have to add up
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
        ResourceBundle rb = getResourceBundle(mutex, bundleName, resourceBundleMap,
                info.getLocale());
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
         * We must send off a request to USPS for each package and add up the total cost.
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
            ShippingQuote quote = getQuotesFromUSPS(order, info, rb, weight);
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
         * this because every time we get a quote from USPS we don't necessarily get back the same
         * rate codes . i.e. First time we may get USPS Priority Mail and USPS Parcel Post. The
         * quote for the 2nd package may only contain USPS Priority Mail. If this is the case we
         * have to make sure that we only return USPS Priority Mail.
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
            throw new Exception("The shipment was split up into multiple packages so we made "
                    + "multiple calls to USPS. USPS didn't return an identical rate "
                    + "across all calls ");
        }

        // Only do a purge if the number of quotes we can keep isn't equal to the number we got
        // from USPS. We have to create a new array with the quotes that we can keep.
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
                                + " from quote because it isn't present in all calls to USPS");
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
     * Get quotes from USPS for a single delivery
     * 
     * @param order
     * @param info
     * @param rb
     * @param weight
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    private ShippingQuote getQuotesFromUSPS(Order order, ShippingInfo info, ResourceBundle rb,
            BigDecimal weight) throws Exception
    {
        String request = getXmlRequest(order, info, weight);

        String response;
        try
        {
            response = sendRequest(request);
        } catch (KKException e)
        {
            throw e;
        } catch (RuntimeException e)
        {
            log.error(e);
            throw new Exception(
                    "Exception received while trying to send shipping request to USPS. "
                            + "See stack trace above.");
        }

        if (log.isDebugEnabled())
        {
            log.debug("Response from USPS =\n" + PrettyXmlPrinter.printXml(response));
        }

        List<ShippingQuote> quoteList = getQuotesFromResponse(response);

        if (quoteList.size() == 0)
        {
            // No reply from USPS
            String msg = "Unrecognised response from USPS (QuoteList empty):\n" + "request =\n"
                    + request + "\nresponse = \n" + response;
            log.error(msg);
            throw new Exception(msg);
        } else if (quoteList.size() == 1)
        {
            // This could either be an error from USPS or a quote
            ShippingQuote sq = quoteList.get(0);
            if (sq.getCustom1().equals("0"))
            {
                StringBuffer sb = new StringBuffer();
                sb.append("There has been an error returned from USPS for the request :\n");
                sb.append(request);
                sb.append("\n");
                sb.append("The error details are :\n");
                sb.append("Number = ");
                sb.append(sq.getCustom2());
                sb.append("\n");
                sb.append("Source = ");
                sb.append(sq.getCustom3());
                sb.append("\n");
                sb.append("Description = ");
                sb.append(sq.getCustom4());
                sb.append("\n");
                sb.append("HelpContext = ");
                sb.append(sq.getCustom5());
                sb.append("\n");
                log.error(sb.toString());
                throw new Exception(sb.toString());
            }

            if (excludeQuote(sq))
            {
                throw new Exception("USPS only returned one quote, code = " + sq.getCustom5()
                        + " which is on the exclude list.");
            }

            // Populate locale specific attributes from the resource bundle
            sq.setResponseText(sq.getDescription());
            sq.setTitle(rb.getString(MODULE_SHIPPING_USPS_TEXT_TITLE));
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
                sq.setTitle(rb.getString(MODULE_SHIPPING_USPS_TEXT_TITLE));
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
                throw new Exception("USPS returned " + sqArray.length
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
                if (log.isDebugEnabled())
                {
                    log.debug("The quote with Class Id = " + quote.getCustom5()
                            + " has been excluded from the return list");
                }
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
     * Creates a list of ShippingQuote objects from the USPS response
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

        if (cNode.getNodeName().equals("Postage"))
        {
            NamedNodeMap nnm = cNode.getAttributes();
            Node attrValue = null;
            if (nnm != null)
            {
                attrValue = nnm.getNamedItem("CLASSID");
            }
            ShippingQuote sq = new ShippingQuote();
            sq.setCode(code);
            sq.setModuleCode(code);
            sq.setSortOrder(sd.getSortOrder());
            sq.setIcon(icon);
            sq.setTaxClass(sd.getTaxClass());
            sq.setCustom1("1"); // To show that it's a real quote and not an error
            if (attrValue != null)
            {
                sq.setCustom5(attrValue.getTextContent());
            }
            quoteList.add(sq);
        } else if (cNode.getNodeName().equals("Error"))
        {
            ShippingQuote sq = new ShippingQuote();

            sq.setCustom1("0"); // To show that it's for reporting an error
            quoteList.add(sq);
        } else if (cNode.getNodeName().equals("#text"))
        {
            Node parentNode = cNode.getParentNode();
            if (parentNode != null)
            {
                if (parentNode.getNodeName().equals("MailService"))
                {
                    if (currentQuote != null)
                    {
                        String mailService = Utils.replaceRegisteredSuperscriptFromHtml(cNode
                                .getNodeValue());
                        currentQuote.setDescription(mailService);
                        currentQuote.setShippingServiceCode(mailService);
                    }
                } else if (parentNode.getNodeName().equals("Rate"))
                {
                    if (currentQuote != null)
                    {
                        BigDecimal cost = new BigDecimal(cNode.getNodeValue());
                        currentQuote.setCost(cost);
                    }
                } else if (parentNode.getNodeName().equals("Number"))
                {
                    if (currentQuote != null)
                    {
                        String number = cNode.getNodeValue();
                        currentQuote.setCustom2(number);
                    }
                } else if (parentNode.getNodeName().equals("Source"))
                {
                    if (currentQuote != null)
                    {
                        String source = cNode.getNodeValue();
                        currentQuote.setCustom3(source);
                    }
                } else if (parentNode.getNodeName().equals("Description"))
                {
                    if (currentQuote != null)
                    {
                        String description = Utils.replaceRegisteredSuperscriptFromHtml(cNode
                                .getNodeValue());
                        currentQuote.setCustom4(description);
                    }
                } else if (parentNode.getNodeName().equals("HelpContext"))
                {
                    if (currentQuote != null)
                    {
                        String helpContext = cNode.getNodeValue();
                        currentQuote.setCustom5(helpContext);
                    }
                }
            }
        }
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
    private String getXmlRequest(Order order, ShippingInfo info, BigDecimal weight)
            throws KKException
    {
        // <RateV4Request USERID="xxx">
        // --<Package ID="P1">
        // ----<Service>PRIORITY</Service>
        // ----<ZipOrigination>44106</ZipOrigination>
        // ----<ZipDestination>20770</ZipDestination>
        // ----<Pounds>1</Pounds>
        // ----<Ounces>8</Ounces>
        // ----<Container>NONRECTANGULAR</Container>
        // ----<Size>LARGE</Size>
        // ----<Machinable>true</Machinable>
        // --</Package>
        // </RateV4Request>

        StaticData sd = staticDataHM.get(getStoreId());
        StringBuffer ret = new StringBuffer();
        /*
         * Start tags
         */
        ret.append("<RateV4Request USERID=\"").append(sd.getUserid()).append("\">");
        ret.append("<Package ID=\"P1\">");

        /*
         * Request
         */
        ret.append("<Service>").append(sd.getServiceCode()).append("</Service>");
        if (sd.getServiceCode().equals(FIRST_CLASS))
        {
            ret.append("<FirstClassMailType>").append(sd.getFirstClassMailType())
                    .append("</FirstClassMailType>");
        }
        ret.append("<ZipOrigination>").append(info.getOriginZip()).append("</ZipOrigination>");
        ret.append("<ZipDestination>").append(order.getDeliveryPostcode())
                .append("</ZipDestination>");
        int[] weightArray = getPoundsAndOunces(weight);
        ret.append("<Pounds>").append(weightArray[0]).append("</Pounds>");
        ret.append("<Ounces>").append(weightArray[1]).append("</Ounces>");
        ret.append("<Container>").append(sd.getContainerType()).append("</Container>");
        ret.append("<Size>").append(getSizeForWeight(weight)).append("</Size>");
        ret.append("<Machinable>").append(sd.isMachinable()).append("</Machinable>");

        /*
         * End Tags
         */
        ret.append("</Package>");
        ret.append("</RateV4Request>");

        return ret.toString();
    }

    /**
     * Returns the pounds and ounces from a decimal version of pounds. We always round up the
     * ounces.
     * 
     * @param decimalPounds
     * @return pounds and ounces in an integer array
     */
    private int[] getPoundsAndOunces(BigDecimal decimalPounds)
    {
        if (decimalPounds == null)
        {
            return new int[]
            { 0, 0 };
        }

        int pounds = decimalPounds.intValue();
        BigDecimal fraction = decimalPounds.subtract(new BigDecimal(pounds));
        int ounces = fraction.multiply(new BigDecimal(16)).add(new BigDecimal(0.999)).intValue();
        return new int[]
        { pounds, ounces };
    }

    /**
     * Send the request to USPS
     * 
     * @param request
     * @return Returns the response
     * @throws IOException
     * @throws KKException
     */
    private String sendRequest(String request) throws IOException, KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        String reqUrl = sd.getUspsUrl() + "?API=RateV4&XML=" + URLEncoder.encode(request, "UTF-8");

        if (log.isDebugEnabled())
        {
            log.debug("Request to USPS = " + reqUrl + "\n\n");
        }

        URL url = new URL(reqUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setReadTimeout(60000); // Timeout after a minute

        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (Exception e)
        {
            int respCode = conn.getResponseCode();
            String respMsg = conn.getResponseMessage();
            log.error("Exception received from USPS. \nHTTP Response Code = " + respCode
                    + "\nResponse Message =" + respMsg, e);
            throw new KKException("Exception received from USPS. \nHTTP Response Code = "
                    + respCode + "\nResponse Message =" + respMsg);
        }

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
        return isAvailable(MODULE_SHIPPING_USPS_STATUS);
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

        conf = getConfiguration(MODULE_SHIPPING_USPS_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_USPS_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_USPS_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_USPS_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPS_URL must be set to the URL where "
                            + "the XML messages are sent "
                            + "(i.e. http://production.shippingapis.com/ShippingAPI.dll )");
        }
        staticData.setUspsUrl(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_USPS_USERID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPS_USERID must be set to the USPS "
                            + "UserId for using the API");
        }
        staticData.setUserid(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_USPS_HANDLING);
        if (conf == null)
        {
            staticData.setHandling(new BigDecimal(0));
        } else
        {
            staticData.setHandling(new BigDecimal(conf.getValue()));
        }

        conf = getConfiguration(MODULE_SHIPPING_USPS_MACHINABLE);
        if (conf == null)
        {
            staticData.setMachinable(false);
        } else
        {
            if (conf.getValue().equalsIgnoreCase("true"))
            {
                staticData.setMachinable(true);
            } else
            {
                staticData.setMachinable(false);
            }
        }

        conf = getConfiguration(MODULE_SHIPPING_USPS_SERVICE_CODE);
        if ((conf == null || conf.getValue() == null || conf.getValue().length() == 0))
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPS_SERVICE_CODE must be set to a valid service code for receiving a quote.");
        }
        staticData.setServiceCode(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_USPS_FIRST_CLASS_PACKAGE);
        if ((conf == null || conf.getValue() == null || conf.getValue().length() == 0))
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPS_FIRST_CLASS_PACKAGE must be set to a valid package (Letter, Flat or Parcel).");
        }
        staticData.setFirstClassMailType(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_USPS_CONTAINER);
        if ((conf == null || conf.getValue() == null || conf.getValue().length() == 0))
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPS_CONTAINER must be set to a valid container type (i.e. RECTANGULAR).");
        }
        staticData.setContainerType(conf.getValue());

        createWeightSizeList();

        setExcludeServiceMap();
    }

    /**
     * From the configuration data, we fill a list with the package that we need to choose based on
     * the weight. The config data should contain something like this :
     * 10:REGULAR,25:LARGE,70:OVERSIZE which means that from weight 0 to 10 we use a REGULAR
     * package. Then from weight 10 to 25 we use a LARGE package etc.
     * 
     * @throws Exception
     */
    private void createWeightSizeList() throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        KKConfiguration conf = getConfiguration(MODULE_SHIPPING_USPS_SIZE);
        if (conf != null)
        {
            if (sd.getWeightSizeList() == null)
            {
                sd.setWeightSizeList(new ArrayList<WeightSize>());
            } else
            {
                sd.getWeightSizeList().clear();
            }

            String weightSizes = conf.getValue();
            if (weightSizes != null)
            {
                String[] weightSizesArray = weightSizes.split(",");
                for (int i = 0; i < weightSizesArray.length; i++)
                {
                    String weightSize = weightSizesArray[i];
                    String[] weightSizeArray = weightSize.split(":");
                    if (weightSizeArray.length == 2)
                    {
                        WeightSize ws = new WeightSize(new BigDecimal(weightSizeArray[0]),
                                weightSizeArray[1]);
                        sd.getWeightSizeList().add(ws);
                    } else
                    {
                        throw new KKException(
                                "UPS Packaging type by weight must be in the format "
                                        + "weight:PackageCode,weight1:packageCode1,weight2:packageCode2 etc.");
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
            for (Iterator<WeightSize> iterator = sd.getWeightSizeList().iterator(); iterator
                    .hasNext();)
            {
                WeightSize wp = iterator.next();
                log.debug(wp.toString());
            }
        }
    }

    /**
     * Returns the size code based on the weight
     * 
     * @return Returns a string containing the size
     * @throws KKException
     */
    private String getSizeForWeight(BigDecimal weight) throws KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        for (Iterator<WeightSize> iter1 = sd.getWeightSizeList().iterator(); iter1.hasNext();)
        {
            WeightSize wp = iter1.next();
            if (weight.compareTo(wp.getWeight()) == -1)
            {
                return wp.getSize();
            }
        }

        // Return a default size if can't find one in the list
        return "REGULAR";
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
        KKConfiguration conf = getConfiguration(MODULE_SHIPPING_USPS_SERVICE_CODES_EXCLUDE);
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
     * Class used to keep the package size for a weight
     */
    private class WeightSize
    {
        private BigDecimal weight;

        private String size;

        /**
         * Constructor
         * 
         * @param weight
         * @param size
         */
        public WeightSize(BigDecimal weight, String size)
        {
            setWeight(weight);
            setSize(size);
        }

        public String toString()
        {
            StringBuffer str = new StringBuffer();
            str.append("WeightSize:\n");
            str.append("weight          = ").append(getWeight()).append("\n");
            str.append("size             = ").append(getSize()).append("\n");
            return (str.toString());
        }

        /**
         * @return the weight
         */
        public BigDecimal getWeight()
        {
            return weight;
        }

        /**
         * @param weight
         *            the weight to set
         */
        public void setWeight(BigDecimal weight)
        {
            this.weight = weight;
        }

        /**
         * @return the size
         */
        public String getSize()
        {
            return size;
        }

        /**
         * @param size
         *            the size to set
         */
        public void setSize(String size)
        {
            this.size = size;
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

        // URL where XML messages are sent
        private String uspsUrl;

        // UserId for using the API
        private String userid;

        // If true then the package is machinable. This normally results in lower rates.
        private boolean machinable;

        // This is the service code for which we receive a quote
        private String serviceCode;

        // Used to define the package when first class is selected
        private String firstClassMailType;

        // Used to define the type of container used (i.e. RECTANGULAR, FLAT RATE BOX etc.)
        private String containerType;

        private HashMap<String, String> exludeServiceMap = new HashMap<String, String>();

        // List that contains USPS package sizes based on the weight
        private List<WeightSize> weightSizeList = null;

        // Handling charge
        private BigDecimal handling;

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
         * @return the uspsUrl
         */
        public String getUspsUrl()
        {
            return uspsUrl;
        }

        /**
         * @param uspsUrl
         *            the uspsUrl to set
         */
        public void setUspsUrl(String uspsUrl)
        {
            this.uspsUrl = uspsUrl;
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
         * @return the machinable
         */
        public boolean isMachinable()
        {
            return machinable;
        }

        /**
         * @param machinable
         *            the machinable to set
         */
        public void setMachinable(boolean machinable)
        {
            this.machinable = machinable;
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
         * @return the firstClassMailType
         */
        public String getFirstClassMailType()
        {
            return firstClassMailType;
        }

        /**
         * @param firstClassMailType
         *            the firstClassMailType to set
         */
        public void setFirstClassMailType(String firstClassMailType)
        {
            this.firstClassMailType = firstClassMailType;
        }

        /**
         * @return the containerType
         */
        public String getContainerType()
        {
            return containerType;
        }

        /**
         * @param containerType
         *            the containerType to set
         */
        public void setContainerType(String containerType)
        {
            this.containerType = containerType;
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
         * @return the weightSizeList
         */
        public List<WeightSize> getWeightSizeList()
        {
            return weightSizeList;
        }

        /**
         * @param weightSizeList
         *            the weightSizeList to set
         */
        public void setWeightSizeList(List<WeightSize> weightSizeList)
        {
            this.weightSizeList = weightSizeList;
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
    }
}
