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

package com.konakart.bl.modules.shipping.uspsint;

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
import com.konakart.util.Utils;
import com.workingdogs.village.DataSetException;

/**
 * This shipping module communicates with USPSINT to retrieve shipping rates. Using the Admin App
 * you must set up the following configuration variables:
 * 
 * USPSINT Module:
 * 
 * <ul>
 * <li>USPSINT User Id which you need to receive from USPSINT</li>
 * <li>Request URL (production: http://production.shippingapis.com/ShippingAPI.dll)</li>
 * </li>
 * <li>Mail Type. Defines the type of mail. Valid values are:
 * <ul>
 * <li>PACKAGE</li>
 * <li>ENVELOPE</li>
 * <li>MATTER FOR THE BLIND</li>
 * <li>POSTCARDS OR AEROGRAMME</li>
 * </ul>
 * <li>USPSINT Service codes to exclude. You can decide to exclude certain ones. The excluded rates
 * are defined by comma separated codes. The codes are:
 * <ul>
 * <li>1 Express Mail International</li>
 * <li>2 Priority Mail International</li>
 * <li>4 Global Express Guaranteed (Document and Non-document)</li>
 * <li>5 Global Express Guaranteed Document used</li>
 * <li>6 Global Express Guaranteed Non-Document Rectangular shape</li>
 * <li>7 Global Express Guaranteed Non-Document Non- Rectangular</li>
 * <li>8 Priority Mail Flat Rate Envelope</li>
 * <li>9 Priority Mail Flat Rate Box</li>
 * <li>10 Express Mail International Flat Rate Envelope</li>
 * <li>11 Priority Mail Large Flat Rate Box</li>
 * <li>12 Global Express Guaranteed Envelope</li>
 * <li>13 First Class Mail International Letters</li>
 * <li>14 First Class Mail International Flats</li>
 * <li>15 First Class Mail International Parcels</li>
 * <li>21 PostCards</li>
 * </ul>
 * <li>Machinable Package can be set to true or false. The USPSINT definitions for machinable can be
 * found here: http://pe.usps.com/text/dmm300/101.htm</li>
 * <li>Include insurance can be set to true or false. When set to true, the quote includes insurance
 * that covers the cost of the products.</li>
 * <li>Handling fee : Defaults to 0. It is added to the charge returned by USPSINT.</li>
 * <li></li>
 * </ul>
 * 
 * Shipping and Packaging under Configuration
 * 
 * <ul>
 * <li>Country of Origin : Country from which packages will be shipped.</li>
 * <li>Postal Code : Postal code of area from which packages will be shipped</li>
 * <li>Maximum package weight : This is used to split the order into multiple packages . We make
 * multiple calls to USPSINT and sum the costs to create a total.</li>
 * <li>Package tare weight : This is added to the weight of the items being shipped per package</li>
 * </ul>
 */
public class Uspsint extends BaseShippingModule implements ShippingInterface
{
    // Module name must be the same as the class name although it can be all in lowercase
    private static String code = "uspsint";

    private static String icon = "";

    private static String bundleName = BaseModule.basePackage + ".shipping.uspsint.Uspsint";

    private static HashMap<Locale, ResourceBundle> resourceBundleMap = new HashMap<Locale, ResourceBundle>();

    private static String mutex = "uspsintMutex";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // Configuration Keys
    private final static String MODULE_SHIPPING_USPSINT_TAX_CLASS = "MODULE_SHIPPING_USPSINT_TAX_CLASS";

    private final static String MODULE_SHIPPING_USPSINT_STATUS = "MODULE_SHIPPING_USPSINT_STATUS";

    private final static String MODULE_SHIPPING_USPSINT_ZONE = "MODULE_SHIPPING_USPSINT_ZONE";

    private final static String MODULE_SHIPPING_USPSINT_SORT_ORDER = "MODULE_SHIPPING_USPSINT_SORT_ORDER";

    private final static String MODULE_SHIPPING_USPSINT_URL = "MODULE_SHIPPING_USPSINT_URL";

    private final static String MODULE_SHIPPING_USPSINT_USERID = "MODULE_SHIPPING_USPSINT_USERID";

    private final static String MODULE_SHIPPING_USPSINT_MAIL_TYPE = "MODULE_SHIPPING_USPSINT_MAIL_TYPE";

    private final static String MODULE_SHIPPING_USPSINT_INSURANCE = "MODULE_SHIPPING_USPSINT_INSURANCE";

    private final static String MODULE_SHIPPING_USPSINT_SERVICE_CODES_EXCLUDE = "MODULE_SHIPPING_USPSINT_SERVICE_CODES_EXCLUDE";

    private final static String MODULE_SHIPPING_USPSINT_MACHINABLE = "MODULE_SHIPPING_USPSINT_MACHINABLE";

    private final static String MODULE_SHIPPING_USPSINT_HANDLING = "MODULE_SHIPPING_USPSINT_HANDLING";

    // Message Catalog Keys
    private final static String MODULE_SHIPPING_USPSINT_TEXT_TITLE = "module.shipping.uspsint.text.title";

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
    public Uspsint(KKEngIf eng) throws TorqueException, KKException, DataSetException
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
     * We get a quote from USPSINT. If we only get one quote back then we return a ShippingQuote
     * object with the details of the quote. If we get back more than one quote then we create an
     * array of ShippingQuote objects and attach them to the ShippingQuote object that we return. In
     * this case all of the quotes must be in the array since the quote itself isn't processed.
     * 
     * It becomes more complex when we have the shipment split into a number of packages. The split
     * is done by the manager calling this method and is presented to us in an orderWeightList. For
     * each item in the list, we call USPSINT and get back one or more quotes. We then have to add
     * up the quotes and return them as single quotes that can be displayed on the screen.
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
         * We must send off a request to USPSINT for each package and add up the total cost.
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
            ShippingQuote quote = getQuotesFromUSPSINT(order, info, rb, weight);
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
         * this because every time we get a quote from USPSINT we don't necessarily get back the
         * same rate codes . i.e. First time we may get USPSINT Priority Mail and USPSINT Parcel
         * Post. The quote for the 2nd package may only contain USPSINT Priority Mail. If this is
         * the case we have to make sure that we only return USPSINT Priority Mail.
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
                    "The shipment was split up into multiple packages so we made multiple calls to USPSINT. USPSINT didn't return an identical rate across all calls ");
        }

        // Only do a purge if the number of quotes we can keep isn't equal to the number we got
        // from USPSINT. We have to create a new array with the quotes that we can keep.
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
                                + " from quote because it isn't present in all calls to USPSINT");
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
     * Get quotes from USPSINT for a single delivery
     * 
     * @param order
     * @param info
     * @param rb
     * @param weight
     * @return Returns a ShippingQuote object
     * @throws Exception
     */
    private ShippingQuote getQuotesFromUSPSINT(Order order, ShippingInfo info, ResourceBundle rb,
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
                    "Exception received while trying to send shipping request to USPSINT. See stack trace above.");
        }

        if (log.isDebugEnabled())
        {
            log.debug("Response from USPSINT = " + response + "\n\n");
        }

        List<ShippingQuote> quoteList = getQuotesFromResponse(response);

        if (quoteList.size() == 0)
        {
            // No reply from USPSINT
            throw new Exception("Unrecognised response from USPSINT:\n" + response);
        } else if (quoteList.size() == 1)
        {
            // This could either be an error from USPSINT or a quote
            ShippingQuote sq = quoteList.get(0);
            if (sq.getCustom1().equals("0"))
            {
                StringBuffer sb = new StringBuffer();
                sb.append("There has been an error returned from USPSINT for the request :\n");
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
                throw new Exception(sb.toString());
            }

            if (excludeQuote(sq))
            {
                throw new Exception("USPS only returned one quote, code = " + sq.getCustom5()
                        + " which is on the exclude list.");
            }

            // Populate locale specific attributes from the resource bundle
            sq.setResponseText(sq.getDescription());
            sq.setTitle(rb.getString(MODULE_SHIPPING_USPSINT_TEXT_TITLE));
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
                sq.setTitle(rb.getString(MODULE_SHIPPING_USPSINT_TEXT_TITLE));
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
                throw new Exception("USPSINT returned " + sqArray.length
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
     * Creates a list of ShippingQuote objects from the USPSINT response
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

        // Process the quotes
        for (Iterator<ShippingQuote> iterator = quoteList.iterator(); iterator.hasNext();)
        {
            ShippingQuote sq = iterator.next();
            if (sq.getCustom1().equals("1"))
            {
                // Get reply variables from quote
                String insurance = getValue("<Insurance>", "</Insurance>", sq.getCustom2());
                String parcelIndemnityCoverage = getValue("<ParcelIndemnityCoverage>",
                        "</ParcelIndemnityCoverage>", sq.getCustom2());
                String insComment = getValue("<InsComment>", "</InsComment>", sq.getCustom2());
                String svcCommitments = sq.getCustom3();
                String valueOfContents = sq.getCustom4();

                // Pad description out with service commitment (i.e. number of days for delivery)
                if (svcCommitments != null && sq.getDescription() != null)
                {
                    sq.setDescription(sq.getDescription() + " " + svcCommitments);
                }

                /*
                 * Figure out whether insurance is available for requested amount, or whether
                 * available for a smaller amount or whether not available at all.
                 */
                String insQuote = null;
                BigDecimal insuredAmount = null;
                if (insComment != null)
                {
                    if (insComment.equals("SERVICE"))
                    {
                        insQuote = "Insurance not available for this service";
                    } else if (insComment.equals("DESTINATION"))
                    {
                        insQuote = "Insurance is not available to country via this service";
                    }
                }

                if (parcelIndemnityCoverage != null)
                {
                    insuredAmount = new BigDecimal(parcelIndemnityCoverage);
                } else if (valueOfContents != null)
                {
                    insuredAmount = new BigDecimal(valueOfContents);
                }

                /*
                 * State how much the package is insured for or why it isn't insured
                 */
                if (insQuote != null && sq.getDescription() != null)
                {
                    sq.setDescription(sq.getDescription() + ". " + insQuote);
                } else if (insuredAmount != null && sq.getDescription() != null)
                {
                    sq.setDescription(sq.getDescription() + ". Insured for $" + insuredAmount);
                }

                // Add cost of insurance to total
                if (insurance != null && sq.getCost() != null)
                {
                    sq.setCost(sq.getCost().add(new BigDecimal(insurance)));
                }

                sq.setCustom2(null); // set to null since used later
            }
        }

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

        if (cNode.getNodeName().equals("Service"))
        {
            NamedNodeMap nnm = cNode.getAttributes();
            Node attrValue = null;
            if (nnm != null)
            {
                attrValue = nnm.getNamedItem("ID");
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
                if (parentNode.getNodeName().equals("SvcDescription"))
                {
                    if (currentQuote != null)
                    {
                        String svcDescription = cNode.getNodeValue();
                        currentQuote.setDescription(Utils.replaceRegisteredSuperscriptFromHtml(svcDescription));
                    }
                } else if (parentNode.getNodeName().equals("SvcCommitments"))
                {
                    if (currentQuote != null)
                    {
                        String svcCommitments = cNode.getNodeValue();
                        currentQuote.setCustom3(svcCommitments);
                    }
                } else if (parentNode.getNodeName().equals("ValueOfContents"))
                {
                    if (currentQuote != null)
                    {
                        String valueOfContents = cNode.getNodeValue();
                        currentQuote.setCustom4(valueOfContents);
                    }
                } else if (parentNode.getNodeName().equals("Insurance"))
                {
                    if (currentQuote != null)
                    {
                        String insurance = "<Insurance>" + cNode.getNodeValue() + "</Insurance>";
                        if (currentQuote.getCustom2() == null)
                        {
                            currentQuote.setCustom2(insurance);
                        } else
                        {
                            currentQuote.setCustom2(currentQuote.getCustom2() + insurance);
                        }
                    }
                } else if (parentNode.getNodeName().equals("ParcelIndemnityCoverage"))
                {
                    if (currentQuote != null)
                    {
                        String parcelIndemnityCoverage = "<ParcelIndemnityCoverage>"
                                + cNode.getNodeValue() + "</ParcelIndemnityCoverage>";
                        if (currentQuote.getCustom2() == null)
                        {
                            currentQuote.setCustom2(parcelIndemnityCoverage);
                        } else
                        {
                            currentQuote.setCustom2(currentQuote.getCustom2()
                                    + parcelIndemnityCoverage);
                        }
                    }
                } else if (parentNode.getNodeName().equals("InsComment"))
                {
                    if (currentQuote != null)
                    {
                        String insComment = "<InsComment>" + cNode.getNodeValue() + "</InsComment>";
                        if (currentQuote.getCustom2() == null)
                        {
                            currentQuote.setCustom2(insComment);
                        } else
                        {
                            currentQuote.setCustom2(currentQuote.getCustom2() + insComment);
                        }
                    }
                } else if (parentNode.getNodeName().equals("Postage"))
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
                        String description = cNode.getNodeValue();
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
     * Utility to get value from encoded data
     * 
     * @param startTag
     * @param endTag
     * @param data
     * @return Returns the
     */
    private String getValue(String startTag, String endTag, String data)
    {
        if (data == null)
        {
            return null;
        }

        int beginIndex = data.indexOf(startTag) + startTag.length();
        int endIndex = data.indexOf(endTag);

        if (beginIndex > 0 && endIndex > 0 && endIndex > beginIndex)
        {
            return data.substring(beginIndex, endIndex);
        }
        return null;
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
    private String getXmlRequest(Order order, ShippingInfo info, BigDecimal weight) throws KKException
    {
        // <IntlRateRequest USERID="xxx">
        // --<Package ID="P1">
        // ----<Pounds>1</Pounds>
        // ----<Ounces>8</Ounces>
        // ----<Machinable>true</Machinable>
        // ----<MailType>PACKAGE</MailType>
        // ----<ValueOfContents>250</ValueOfContents>
        // ----<Country>Japan</Country>
        // --</Package>
        // </IntlRateRequest>

        StaticData sd = staticDataHM.get(getStoreId());
        StringBuffer ret = new StringBuffer();
        /*
         * Start tags
         */
        ret.append("<IntlRateRequest USERID=\"").append(sd.getUserid()).append("\">");
        ret.append("<Package ID=\"P1\">");

        /*
         * Request
         */
        int[] weightArray = getPoundsAndOunces(weight);
        ret.append("<Pounds>").append(weightArray[0]).append("</Pounds>");
        ret.append("<Ounces>").append(weightArray[1]).append("</Ounces>");
        ret.append("<Machinable>").append(sd.isMachinable()).append("</Machinable>");
        ret.append("<MailType>").append(sd.getMailType()).append("</MailType>");
        if (sd.isIncludeInsurance())
        {
            ret.append("<ValueOfContents>").append(order.getTotalExTax()).append(
                    "</ValueOfContents>");
        }
        ret.append("<Country>").append(order.getDeliveryCountry()).append("</Country>");

        /*
         * End Tags
         */
        ret.append("</Package>");
        ret.append("</IntlRateRequest>");

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
     * Send the request to USPSINT
     * 
     * @param request
     * @return Returns the response
     * @throws IOException
     * @throws KKException
     */
    private String sendRequest(String request) throws IOException, KKException
    {
        StaticData sd = staticDataHM.get(getStoreId());
        String reqUrl = sd.getUspsintUrl() + "?API=IntlRate&XML="
                + URLEncoder.encode(request, "UTF-8");

        if (log.isDebugEnabled())
        {
            log.debug("Request to USPSINT = " + reqUrl + "\n\n");
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
            log.error("Exception received from USPSINT. \nHTTP Response Code = " + respCode
                    + "\nResponse Message =" + respMsg, e);
            throw new KKException("Exception received from USPSINT. \nHTTP Response Code = "
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
        return isAvailable(MODULE_SHIPPING_USPSINT_STATUS);
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

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_SORT_ORDER);
        if (conf == null)
        {
            staticData.setSortOrder(0);
        } else
        {
            staticData.setSortOrder(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_TAX_CLASS);
        if (conf == null)
        {
            staticData.setTaxClass(0);
        } else
        {
            staticData.setTaxClass(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_ZONE);
        if (conf == null)
        {
            staticData.setZone(0);
        } else
        {
            staticData.setZone(new Integer(conf.getValue()).intValue());
        }

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_URL);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPSINT_URL must be set to the URL where the XML messages are sent (i.e. http://production.shippingapis.com/ShippingAPI.dll )");
        }
        staticData.setUspsintUrl(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_USERID);
        if (conf == null)
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPSINT_USERID must be set to the USPSINT UserId for using the API");
        }
        staticData.setUserid(conf.getValue());

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_HANDLING);
        if (conf == null)
        {
            staticData.setHandling(new BigDecimal(0));
        } else
        {
            staticData.setHandling(new BigDecimal(conf.getValue()));
        }

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_MACHINABLE);
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

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_INSURANCE);
        if (conf == null)
        {
            staticData.setIncludeInsurance(false);
        } else
        {
            if (conf.getValue().equalsIgnoreCase("true"))
            {
                staticData.setIncludeInsurance(true);
            } else
            {
                staticData.setIncludeInsurance(false);
            }
        }

        conf = getConfiguration(MODULE_SHIPPING_USPSINT_MAIL_TYPE);
        if ((conf == null || conf.getValue() == null || conf.getValue().length() == 0))
        {
            throw new KKException(
                    "The Configuration MODULE_SHIPPING_USPSINT_MAIL_TYPE must be set to a valid mail type for receiving a quote. i.e. PACKAGE, ENVELOPE, MATTER FOR THE BLIND, POSTCARDS OR AEROGRAMMES");
        }
        staticData.setMailType(conf.getValue());
                
        setExcludeServiceMap();
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
                MODULE_SHIPPING_USPSINT_SERVICE_CODES_EXCLUDE);
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
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        private int taxClass;

        private int zone;

        // URL where XML messages are sent
        private String uspsintUrl;

        // UserId for using the API
        private String userid;

        // If true then the package is machinable. This normally results in lower rates.
        private boolean machinable;

        // Defines the type of mail (i.e. PACKAGE, ENVELOPE, MATTER FOR THE BLIND, POSTCARDS OR
        // AEROGRAMMES)
        private String mailType;

        // If true, insurance is included in the total cost
        private boolean includeInsurance;

        private HashMap<String, String> exludeServiceMap = new HashMap<String, String>();

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
         * @return the uspsintUrl
         */
        public String getUspsintUrl()
        {
            return uspsintUrl;
        }

        /**
         * @param uspsintUrl
         *            the uspsintUrl to set
         */
        public void setUspsintUrl(String uspsintUrl)
        {
            this.uspsintUrl = uspsintUrl;
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
         * @return the mailType
         */
        public String getMailType()
        {
            return mailType;
        }

        /**
         * @param mailType
         *            the mailType to set
         */
        public void setMailType(String mailType)
        {
            this.mailType = mailType;
        }

        /**
         * @return the includeInsurance
         */
        public boolean isIncludeInsurance()
        {
            return includeInsurance;
        }

        /**
         * @param includeInsurance
         *            the includeInsurance to set
         */
        public void setIncludeInsurance(boolean includeInsurance)
        {
            this.includeInsurance = includeInsurance;
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
