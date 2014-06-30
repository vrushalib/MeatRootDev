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

package com.konakart.actions.gateways;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.appif.CountryIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.modules.payment.worldpayxmlredirect.WorldPayXMLRedirect;

/**
 * This class is an Action class for sending an XML request to WorldPay and receiving a response
 * that contains a URL for redirecting the customer to the WorldPay web site in order to enter the
 * credit card details.
 */
public class WorldPayXMLRedirectAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(WorldPayXMLRedirectAction.class);

    // Return codes and descriptions
    private static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    private static final int RET4 = -4;

    private static final long serialVersionUID = 1L;

    private String url;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(WorldPayXMLRedirect.WP_XML_REDIRECT_GATEWAY_CODE);
        KKAppEng kkAppEng = null;

        try
        {
            int custId;

            kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "Checkout");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Set the customer id for the IPN history object
            ipnHistory.setCustomerId(custId);

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, WorldPayXMLRedirect.WP_XML_REDIRECT_GATEWAY_CODE);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            PaymentDetailsIf pd = order.getPaymentDetails();

            // Do the post
            String gatewayResp = null;
            try
            {
                gatewayResp = postData(pd, null);
            } catch (Exception e)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Problem posting data to WorldPay: " + e.getMessage());
                    e.printStackTrace();
                }

                // Save the ipnHistory
                ipnHistory.setGatewayFullResponse(e.getMessage());
                ipnHistory
                        .setKonakartResultDescription(getResultDescription("Problem posting data to WorldPay: "
                                + e.getMessage()));
                ipnHistory.setKonakartResultId(RET4);
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // Redirect the user to an error screen
                return "CheckoutError";
            }

            gatewayResp = URLDecoder.decode(gatewayResp, "UTF-8");
            if (log.isDebugEnabled())
            {
                log.debug("Unformatted GatewayResp = \n" + gatewayResp);
            }

            // Should look similar to:

            // <?xml version="1.0" encoding="UTF-8"?>
            // <!DOCTYPE paymentService PUBLIC "-//WorldPay//DTD WorldPay PaymentService v1//EN"
            // "http://dtd.worldpay.com/paymentService_v1.dtd">
            // <paymentService version="1.4" merchantCode="MERCHANT">
            // <reply>
            // <orderStatus orderCode="1315239998049">
            // <reference id="123678374">
            // https://secure-test.worldpay.com/jsp/shopper/SelectPaymentMethod.jsp?OrderKey=MERCHANT^1315239998049
            // </reference>
            // </orderStatus>
            // </reply>
            // </paymentService>

            // Now process the XML response
            String redirectUrl = null;

            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name.equals("reference"))
                    {
                        Text textnode = (Text) node.getFirstChild();
                        String value = "";
                        if (textnode != null)
                        {
                            value = textnode.getNodeValue();
                        }
                        redirectUrl = value;
                        break;
                    } else if (name.equals("error"))
                    {
                        String errorCode = "";
                        NamedNodeMap map = node.getAttributes();
                        Node attrNode = map.getNamedItem("code");
                        if (attrNode != null)
                        {
                            errorCode = attrNode.getNodeValue();
                        }

                        Text textnode = (Text) node.getFirstChild();
                        String errorDesc = "";
                        if (textnode != null)
                        {
                            errorDesc = textnode.getNodeValue();
                        }

                        // Save the ipnHistory
                        ipnHistory.setGatewayFullResponse(gatewayResp);
                        ipnHistory
                                .setKonakartResultDescription(getResultDescription("Error from WorldPay: Code = "
                                        + errorCode + " Desc = " + errorDesc));
                        ipnHistory.setKonakartResultId(RET4);
                        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                        // Redirect the user to an error screen
                        return "CheckoutError";
                    }
                }

                if (redirectUrl != null)
                {
                    HashMap<String, String> hp = hashParameters(pd, null);

                    StringBuffer redirectUrlSb = new StringBuffer(redirectUrl);
                    CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();
                    if (log.isDebugEnabled())
                    {
                        log.debug("Customer Locale = " + cust.getLocale());
                    }
                    if (cust.getLocale() != null && cust.getLocale().length() > 3
                            && cust.getLocale().charAt(2) == '_')
                    {
                        String langCode = cust.getLocale().substring(0, 2);
                        redirectUrlSb.append("&language=");
                        redirectUrlSb.append(langCode);
                    }

                    kkAppEng.getCustomerMgr().populateCurrentCustomerAddresses(/* force */false);
                    if (cust.getAddresses() != null && cust.getAddresses().length > 0)
                    {
                        int countryId = cust.getAddresses()[0].getCountryId();
                        CountryIf country = kkAppEng.getEng().getCountry(countryId);
                        if (country != null && country.getIsoCode2() != null)
                        {
                            redirectUrlSb.append("&country=");
                            redirectUrlSb.append(country.getIsoCode2());
                        }
                    }

                    redirectUrlSb.append("&successURL=");
                    redirectUrlSb.append(hp.get("responseUrl") + "?retCode=success");
                    redirectUrlSb.append("&pendingURL=");
                    redirectUrlSb.append(hp.get("responseUrl") + "?retCode=pending");
                    redirectUrlSb.append("&failureURL=");
                    redirectUrlSb.append(hp.get("responseUrl") + "?retCode=failure");

                    if (log.isDebugEnabled())
                    {
                        log.debug("Redirecting customer to : " + redirectUrlSb.toString());
                    }

                    this.url = redirectUrlSb.toString();
                    return "redirect";
                }
                throw new KKAppException("Redirect URL is null");

            } catch (Exception e)
            {
                // Problems parsing the XML

                if (log.isWarnEnabled())
                {
                    log.warn("Problem parsing the XML WorldPay response: "
                            + ((gatewayResp != null) ? gatewayResp : "null"));
                    e.printStackTrace();
                }

                // Save the ipnHistory
                ipnHistory.setGatewayFullResponse(gatewayResp);
                ipnHistory
                        .setKonakartResultDescription(getResultDescription("Problem parsing the XML WorldPay response: "
                                + e.getMessage()));
                ipnHistory.setKonakartResultId(RET4);
                kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

                // Redirect the user to an error screen
                return "CheckoutError";
            }

        } catch (Exception e)
        {
            try
            {
                ipnHistory.setKonakartResultDescription(getResultDescription(RET4_DESC
                        + e.getMessage()));
                ipnHistory.setKonakartResultId(RET4);
                if (kkAppEng != null)
                {
                    kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);
                }
            } catch (KKException e1)
            {
                return super.handleException(request, e1);
            }
            return super.handleException(request, e);
        }
    }

    /**
     * Use this to truncate the result description so that it fits in the database column OK
     * 
     * @param desc
     *            the result description (which may be too long)
     * @return a truncated result description
     */
    private String getResultDescription(String desc)
    {
        if (desc == null)
        {
            return null;
        } else if (desc.length() <= 255)
        {
            return desc;
        }

        return desc.substring(0, 255);
    }

    /**
     * Create the request from the parameters
     * 
     * @param pd
     *            the PaymentDetails
     * @param ccParmList
     *            the credit card parameters
     */
    protected StringBuffer getGatewayRequest(PaymentDetailsIf pd, List<NameValueIf> ccParmList)
    {
        HashMap<String, String> hashedParams = hashParameters(pd, ccParmList);

        String[] includeArray = null;
        String[] excludeArray = null;

        String includes = hashedParams.get("include");
        if (includes != null && includes.length() > 0)
        {
            includeArray = includes.split(",");
        }
        String excludes = hashedParams.get("exclude");
        if (excludes != null && excludes.length() > 0)
        {
            excludeArray = excludes.split(",");
        }

        // Create the message from the parameters in the PaymentDetails object
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<!DOCTYPE paymentService PUBLIC \"-//WorldPay/DTD WorldPay PaymentService v1//EN\" \"http://dtd.worldpay.com/paymentService_v1.dtd\">");
        sb.append("<paymentService version=\"1.4\" merchantCode=\""
                + hashedParams.get("merchantCode") + "\">");
        sb.append("<submit>");
        sb.append("<order orderCode=\"" + hashedParams.get("orderCode") + "\">");
        sb.append("<description>" + hashedParams.get("description") + "</description>");
        sb.append("<amount value=\"" + hashedParams.get("value") + "\" currencyCode=\""
                + hashedParams.get("currencyCode") + "\" exponent=\""
                + hashedParams.get("exponent") + "\"/>");
        sb.append("<orderContent/>");
        sb.append("<paymentMethodMask>");
        if (includeArray != null)
        {
            for (int i = 0; i < includeArray.length; i++)
            {
                String inc = includeArray[i].trim();
                sb.append("<include code=\"" + inc + "\"/>");
            }
        }
        if (excludeArray != null)
        {
            for (int i = 0; i < excludeArray.length; i++)
            {
                String ex = excludeArray[i].trim();
                sb.append("<exclude code=\"" + ex + "\"/>");
            }
        }
        sb.append("</paymentMethodMask>");
        sb.append("<shopper>");
        sb.append("<shopperEmailAddress>" + hashedParams.get("shopperEmailAddress")
                + "</shopperEmailAddress>");
        sb.append("</shopper>");

        String firstName = hashedParams.get("firstName");
        String lastName = hashedParams.get("lastName");
        String street = hashedParams.get("street");
        String postalCode = hashedParams.get("postalCode");
        String city = hashedParams.get("city");
        String countryCode = hashedParams.get("countryCode");
        String telephoneNumber = hashedParams.get("telephoneNumber");
        sb.append("<shippingAddress>");
        sb.append("<address>");
        if (firstName != null && firstName.length() > 0)
        {
            sb.append("<firstName>" + firstName + "</firstName>");
        }
        if (lastName != null && lastName.length() > 0)
        {
            sb.append("<lastName>" + lastName + "</lastName>");
        }
        if (street != null && street.length() > 0)
        {
            sb.append("<street>" + street + "</street>");
        }
        if (postalCode != null && postalCode.length() > 0)
        {
            sb.append("<postalCode>" + postalCode + "</postalCode>");
        }
        if (city != null && city.length() > 0)
        {
            sb.append("<city>" + city + "</city>");
        }
        if (countryCode != null && countryCode.length() > 0)
        {
            sb.append("<countryCode>" + countryCode + "</countryCode>");
        }
        if (telephoneNumber != null && telephoneNumber.length() > 0)
        {
            sb.append("<telephoneNumber>" + telephoneNumber + "</telephoneNumber>");
        }
        sb.append("</address>");
        sb.append("</shippingAddress>");
        sb.append("</order>");
        sb.append("</submit>");
        sb.append("</paymentService>");

        return sb;
    }

    /**
     * Add things specific to WorldPay to the connection
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
        HashMap<String, String> hp = hashParameters(pd, paramList);

        connection.setRequestProperty(
                "Authorization",
                "Basic "
                        + Base64.encodeBase64String((hp.get("merchantCode") + ":" + hp
                                .get("password")).getBytes()));
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
}
