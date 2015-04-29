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

package com.konakart.actions.gateways;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.struts2.ServletActionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.CreditCard;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;

/**
 * This class is an Action class called before displaying the AuthorizeNet DPM credit card entry
 * page
 */
public class AuthorizeNetDPM1Action extends AuthorizeNetBaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            String error = request.getParameter("e");
            if (error != null && kkAppEng != null)
            {
                String msg = kkAppEng.getMsg("checkout.cc.gateway.error", new String[]
                { "" });
                addActionError(msg);
            }

            /*
             * Ensure that we have an order and payment details
             */
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            if (order == null)
            {
                throw new KKAppException("There is no order.");
            }

            if (order.getPaymentDetails() == null)
            {
                throw new KKAppException("There is no PaymentDetails object attached to the order.");
            }

            /*
             * If CIM is enabled we display any stored cards
             */
            if (kkAppEng.getConfigAsBoolean("MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM", false))
            {
                manageCIM(kkAppEng, order.getPaymentDetails());
            }

            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * Add the stored credit cards to the payment details object so that they can be displayed on
     * the UI
     * 
     * @param kkAppEng
     * @param pd
     * @throws Exception
     */
    private void manageCIM(KKAppEng kkAppEng, PaymentDetailsIf pd) throws Exception
    {
        CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
        if (cust == null || cust.getExtReference1() == null
                || cust.getExtReference1().length() == 0)
        {
            return;
        }

        // Get authentication details from the Auth Net module
        getAuthNetAuthentication(kkAppEng);

        // Get a list of payment profiles that have been setup
        StringBuffer sb = new StringBuffer();
        sb.append("<customerProfileId>" + cust.getExtReference1() + "</customerProfileId>");
        sb = getMessage("getCustomerProfileRequest", sb);

        // Get a response from the gateway
        String gatewayResp = getGatewayResponse(sb, "getCustomerProfileRequest");

        // Now process the XML response
        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                boolean val = validateGatewayResponse(kkAppEng, sb, "getCustomerProfileRequest",
                        cust.getId(), cust.getExtReference1(), doc);
                if (val)
                {
                    NodeList list = doc.getElementsByTagName("paymentProfiles");
                    if (list != null && list.getLength() > 0)
                    {
                        CreditCard[] ccArray = new CreditCard[list.getLength()];
                        String firstName = "";
                        int index = -1;
                        list = doc.getElementsByTagName("*");
                        for (int i = 0; i < list.getLength(); i++)
                        {
                            Node node = list.item(i);
                            String name = node.getNodeName();
                            if (name != null)
                            {
                                if (name.equals("paymentProfiles"))
                                {
                                    index++;
                                    ccArray[index] = new CreditCard();
                                } else if (name.equals("firstName"))
                                {
                                    Text datanode = (Text) node.getFirstChild();
                                    firstName = datanode.getData();
                                } else if (name.equals("lastName"))
                                {
                                    Text datanode = (Text) node.getFirstChild();
                                    ccArray[index].setCcOwner(firstName + " " + datanode.getData());
                                } else if (name.equals("customerPaymentProfileId"))
                                {
                                    Text datanode = (Text) node.getFirstChild();
                                    ccArray[index].setCcIdentifier(datanode.getData());
                                } else if (name.equals("cardNumber"))
                                {
                                    Text datanode = (Text) node.getFirstChild();
                                    ccArray[index].setCcNumber(datanode.getData());
                                }
                            }
                        }
                        pd.setCreditCards(ccArray);
                    }
                }

            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing AuthorizeNet customerProfileId response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

    }

}
