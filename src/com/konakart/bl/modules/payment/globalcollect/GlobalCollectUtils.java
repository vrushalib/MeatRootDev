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
// Original version contributed by Chris Derham (Atomus Ltd)
//

package com.konakart.bl.modules.payment.globalcollect;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.app.KKException;
import com.konakart.app.OrderTotal;
import com.konakart.appif.OrderIf;
import com.konakart.bl.modules.ordertotal.OrderTotalMgr;

/**
 * Global Collect Utilities - used by Actions and the module code
 */
public class GlobalCollectUtils
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(GlobalCollectUtils.class);

    /**
     * Constructor
     */
    public GlobalCollectUtils()
    {
    }

    /**
     * Calculate the total amount of the order. Some of the payment products are only suitable for a
     * price in a certain range.
     * 
     * @param order
     * @return the total amount of the order
     */
    public BigDecimal getTotalPrice(OrderIf order)
    {
        int scale = 2;

        if (order.getCurrency() != null && order.getCurrency().getDecimalPlaces() != null)
        {
            scale = new Integer(order.getCurrency().getDecimalPlaces()).intValue();
        }

        return getTotalPrice(order, scale);
    }

    /**
     * Calculate the total amount of the order. Some of the payment products are only suitable for a
     * price in a certain range.
     * 
     * @param order
     * @param scale
     * @return the total amount of the order
     */
    public BigDecimal getTotalPrice(OrderIf order, int scale)
    {
        BigDecimal total = null;

        if (order.getOrderTotals() == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Cannot determine total price because order has no order totals yet");
            }
            return total;
        }

        for (int i = 0; i < order.getOrderTotals().length; i++)
        {
            OrderTotal ot = (OrderTotal) order.getOrderTotals()[i];
            if (ot.getClassName().equals(OrderTotalMgr.ot_total))
            {
                total = ot.getValue().setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
        }

        return total;
    }

    /**
     * @param gatewayResp
     * @return a Map of objects found in the XML string returned by the gateway
     * @throws Exception
     */
    public Map<String, String> parseGlobalCollectResponseToMap(String gatewayResp) throws Exception
    {
        return parseGlobalCollectResponseToMap(gatewayResp, null);
    }

    /**
     * @param gatewayResp
     * @param arrayLocation
     * @return a Map of objects found in the XML string returned by the gateway
     * @throws Exception
     */
    public Map<String, String> parseGlobalCollectResponseToMap(String gatewayResp,
            String arrayLocation) throws Exception
    {
        Map<String, String> xmlMap = new HashMap<String, String>();

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);
                int arrayIndx = -1;

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "XML")
                {
                    throw new KKException("Unexpected root element in Initial Response: "
                            + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null)
                    {
                        Node firstNode = node.getFirstChild();
                        if (firstNode == null)
                        {
                            continue;
                        }
                        if (firstNode instanceof Text)
                        {
                            Text dataNode = (Text) firstNode;
                            String path = getXmlPath(firstNode, arrayIndx, arrayLocation);
                            xmlMap.put(path, dataNode.getData());
                            continue;
                        } else
                        {
                            if (arrayLocation != null
                                    && getXmlPath(firstNode).equals(arrayLocation))
                            {
                                arrayIndx++;
                            }
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Map: " + xmlMap);
                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Initial response: " + e.getMessage());
                }
                throw e;
            }
        }

        return xmlMap;
    }

    /**
     * Utility to get the XML path of a node
     * 
     * @param nodeIn
     * @return The XML path of the node
     */
    protected String getXmlPath(Node nodeIn)
    {
        return getXmlPath(nodeIn, -1, null);
    }

    /**
     * Utility to get the XML path of a node
     * 
     * @param nodeIn
     * @param arrayIdx
     * @param arrayLocation
     * @return The XML path of the node
     */
    protected String getXmlPath(Node nodeIn, int arrayIdx, String arrayLocation)
    {
        String path = "";
        Node myNode = nodeIn;

        while (myNode != null)
        {
            myNode = myNode.getParentNode();
            if (myNode != null)
            {
                String name = myNode.getNodeName();

                // if (!name.equals("#document"))
                if (myNode.getParentNode() != null)
                {
                    if (path.length() > 0)
                    {
                        path = name + "." + path;
                    } else
                    {
                        path = name + path;
                    }
                }
            }
        }

        if (arrayIdx >= 0 && arrayLocation != null && path.startsWith(arrayLocation))
        {
            path = path + "." + arrayIdx;
        }

        return path;
    }
}
