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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.PrettyXmlPrinter;
import com.konakart.util.RegExpUtils;

/**
 * This class is a Base Action class for sending the Generate Session Request to Commidea. It
 * contains methods that manage the various messages that are sent to Commidea.
 */
public class CommideaVanguardBaseAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(CommideaVanguardBaseAction.class);

    // Return codes and descriptions
    protected static final int RET0 = 0;

    protected static final String RET0_DESC = "Transaction OK";

    protected static final int RET1 = -1;

    protected static final String RET1_DESC = "There was an unexpected Gateway Response. Response = ";

    protected static final String RET3_DESC = "There was an unexpected Gateway Response.";

    protected static final int RET4 = -4;

    protected static final String RET4_DESC = "There was an unexpected exception. Exception message = ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "Credit Card payment successful. TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "Credit Card payment not successful.";

    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    // Map containing the parameters created by the payment gateway
    protected HashMap<String, String> parmMap = null;

    private static final long serialVersionUID = 1L;

    /**
     * Common code to finish up after the transaction request to Commidea
     * 
     * @param kkAppEng
     * @param order
     * @param approved
     * @param transactionId
     * @throws Exception
     */
    protected void finishUp(KKAppEng kkAppEng, OrderIf order, boolean approved, String transactionId)
            throws Exception
    {
        // See if we need to send an email, by looking at the configuration
        String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
        boolean sendEmail = false;
        if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
        {
            sendEmail = true;
        }

        OrderUpdateIf updateOrder = new OrderUpdate();
        updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

        if (approved)
        {
            String comment = ORDER_HISTORY_COMMENT_OK + transactionId;
            kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, /* customerNotified */
                    sendEmail, comment, updateOrder);

            // Update the inventory
            kkAppEng.getOrderMgr().updateInventory(order.getId());

            // If we received no exceptions, delete the basket
            kkAppEng.getBasketMgr().emptyBasket();

            if (sendEmail)
            {
                sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */true);
            }

        } else
        {
            String comment = ORDER_HISTORY_COMMENT_KO;
            kkAppEng.getEng().updateOrder(kkAppEng.getSessionId(), order.getId(),
                    com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, /* customerNotified */
                    sendEmail, comment, updateOrder);

            if (sendEmail)
            {
                sendOrderConfirmationMail(kkAppEng, order.getId(), /* success */false);
            }
        }
    }

    /**
     * Use this to truncate the result description so that it fits in the database column OK
     * 
     * @param desc
     *            the result description (which may be too long)
     * @return a truncated result description
     */
    protected String getResultDescription(String desc)
    {
        if (desc == null)
        {
            return null;
        }

        return desc.substring(0, Math.min(255, desc.length()));
    }

    /**
     * Returns the header which is common for all requests
     * 
     * @param msgType
     * @param order
     * @param sendAttempt
     * @return Returns the client header which is common for all requests
     */
    protected String getHeader(String msgType, OrderIf order, int sendAttempt)
    {

        String header1 = "<?xml version=\"1.0\"?>"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "<soap:Body>"
                + "<ProcessMsg xmlns=\"https://www.commidea.webservices.com\">" + "<Message>"
                + "<ClientHeader xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + "<SystemID>"
                + parmMap.get("SystemID") + "</SystemID>" + "<SystemGUID>"
                + parmMap.get("SystemGUID") + "</SystemGUID>" + "<Passcode>"
                + parmMap.get("Passcode") + "</Passcode>" + "<SendAttempt>" + sendAttempt
                + "</SendAttempt>";

        String header2 = "";
        if (!msgType.equalsIgnoreCase("VGGENERATESESSIONREQUEST"))
        {
            header2 = "<ProcessingDB>" + getProcessingDB(order.getPaymentDetails().getCustom1())
                    + "</ProcessingDB>";
        }

        String header3 = "<CDATAWrapping>true</CDATAWrapping>" + "</ClientHeader>"
                + "<MsgType  xmlns=\"https://www.commidea.webservices.com\">" + msgType
                + "</MsgType>" + "<MsgData xmlns=\"https://www.commidea.webservices.com\">"
                + "<![CDATA[<?xml version=\"1.0\"?>";

        return header1 + header2 + header3;
    }

    /**
     * Returns the footer which is common for all requests
     * 
     * @return Returns the footer which is common for all requests
     */
    protected String getFooter()
    {
        String footer = "]]></MsgData>" + "</Message>" + "</ProcessMsg>" + "</soap:Body>"
                + "</soap:Envelope>";
        return footer;
    }

    /**
     * Method that manages the VGGENERATESESSIONREQUEST and the VGGENERATESESSIONRESPONSE. The
     * method places the following data in the custom fields of the PaymentDetails object so that
     * they can be picked up by the JSP that posts the credit card details to Commidea.
     * <ul>
     * <li>Custom1 - sessionguid + ";" + processingDB</li>
     * <li>Custom2 - sessionpasscode</li>
     * <li>Custom4 - ccDetailPostUrl</li>
     * </ul>
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns the Commidea error code
     * @throws Exception
     */
    protected String vggeneratesessionrequest(KKAppEng kkAppEng, OrderIf order,
            IpnHistoryIf ipnHistory) throws Exception
    {

        StringBuffer msg = new StringBuffer();
        msg.append(getHeader("VGGENERATESESSIONREQUEST", order, /* sendAttempt */0));

        String req = "<vggeneratesessionrequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"VANGUARD\">"
                + "<returnurl>"
                + parmMap.get("returnurl")
                + "</returnurl>"
                + "<fullcapture>true</fullcapture>" + "</vggeneratesessionrequest>";
        msg.append(req);

        msg.append(getFooter());

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest (VGGENERATESESSIONREQUEST) =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp (VGGENERATESESSIONRESPONSE) =\n" + gatewayResp);
            try
            {
                log.debug("Formatted GatewayResp (VGGENERATESESSIONRESPONSE) =\n"
                        + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway response (VGGENERATESESSIONRESPONSE) : "
                        + e.getMessage());
            }
        }

        // Now process the XML response

        String sessionguid = null;
        String sessionpasscode = null;
        String errorcode = null;
        String errordescription = null;
        String processingDB = null;
        String errorMsgCode = null;
        String errorMsgTxt = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "soap:Envelope")
                {
                    throw new KKException("Unexpected root element in VGGENERATESESSIONRESPONSE: "
                            + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null && name.equals("ClientHeader"))
                    {
                        NodeList nodeList = node.getChildNodes();
                        int listLength = nodeList.getLength();
                        for (int j = 0; j < listLength; j++)
                        {
                            Node innerNode = nodeList.item(j);
                            if (innerNode.getNodeName().equals("ProcessingDB"))
                            {
                                processingDB = innerNode.getFirstChild().getNodeValue();
                            }
                        }
                    } else if (name != null && name.equals("MsgData"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        String xml = kkAppEng.removeCData(datanode.getData());
                        ByteArrayInputStream bais1 = new ByteArrayInputStream(xml.getBytes());
                        Document doc1 = builder.parse(bais1);
                        NodeList list1 = doc1.getElementsByTagName("*");
                        for (int j = 0; j < list1.getLength(); j++)
                        {
                            Node node1 = list1.item(j);
                            String name1 = node1.getNodeName();
                            if (name1.equals("sessionguid"))
                            {
                                sessionguid = getNodeValue(node1);
                            } else if (name1.equals("sessionpasscode"))
                            {
                                sessionpasscode = getNodeValue(node1);
                            } else if (name1.equals("errorcode"))
                            {
                                errorcode = getNodeValue(node1);
                            } else if (name1.equals("errordescription"))
                            {
                                errordescription = getNodeValue(node1);
                            } else if (name1.equals("errormessage"))
                            {
                                errordescription = getNodeValue(node1);
                            } else if (name1.equals("CODE"))
                            {
                                errorMsgCode = getNodeValue(node1);
                            } else if (name1.equals("MSGTXT"))
                            {
                                errorMsgTxt = getNodeValue(node1);
                            }
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Commidea VGGENERATESESSIONRESPONSE response data:"
                            + "\n    sessionguid               = " + sessionguid
                            + "\n    sessionpasscode           = " + sessionpasscode
                            + "\n    ProcessingDB              = " + processingDB
                            + "\n    errorcode                 = " + errorcode
                            + "\n    errordescription          = " + errordescription
                            + "\n    errorMsgCode              = " + errorMsgCode
                            + "\n    errorMsgTxt               = " + errorMsgTxt);
                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Commidea VGGENERATESESSIONRESPONSE response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        /*
         * Add response data to PaymentDetails Object so that this data can be picked up by the JSP
         * that will post the credit card details to commidea
         */
        order.getPaymentDetails().setCustom1(sessionguid + ";" + processingDB);
        order.getPaymentDetails().setCustom2(sessionpasscode);
        order.getPaymentDetails().setCustom4(parmMap.get("ccDetailPostUrl"));

        /*
         * Save the IPN History record
         */
        errorMsgCode = (errorMsgCode != null) ? errorMsgCode : errorcode;
        errorMsgTxt = (errorMsgTxt != null) ? errorMsgTxt : errordescription;
        String codePlusTxt = getResultDescription(RET1_DESC + errorMsgCode
                + ((errorMsgTxt == null) ? "" : " : " + errorMsgTxt));
        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
        } else
        {
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
        }

        ipnHistory.setGatewayResult(sessionguid);
        ipnHistory.setGatewayTransactionId("vggeneratesessionrequest");
        ipnHistory.setGatewayFullResponse(gatewayResp);
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        return errorMsgCode;
    }

    /**
     * Method that manages the VGGETCARDDETAILSREQUEST and the VGGETCARDDETAILSRESPONSE.
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns "1" or "0" depending on whether the authentication enrollment check request
     *         should be made. i.e. Should be "0" for AMEX cards
     * @throws Exception
     */
    protected String vggetcarddetailsrequest(KKAppEng kkAppEng, OrderIf order,
            IpnHistoryIf ipnHistory) throws Exception
    {

        StringBuffer msg = new StringBuffer();
        msg.append(getHeader("VGGETCARDDETAILSREQUEST", order, /* sendAttempt */0));

        String req = "<vggetcarddetailsrequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns=\"VANGUARD\">"
                + "<sessionguid>"
                + getSessionId(order.getPaymentDetails().getCustom1())
                + "</sessionguid>" + "</vggetcarddetailsrequest>";

        msg.append(req);

        msg.append(getFooter());

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest (VGGETCARDDETAILSREQUEST) =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp (VGGETCARDDETAILSRESPONSE) =\n" + gatewayResp);
            try
            {
                log.debug("Formatted GatewayResp (VGGETCARDDETAILSRESPONSE) =\n"
                        + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway response (VGGETCARDDETAILSRESPONSE) : "
                        + e.getMessage());
            }
        }

        // Now process the XML response

        String sessionguid = null;
        String mkcardschemeid = null;
        String issuenolength = null;
        String startdaterequired = null;
        String csclength = null;
        String allowpayerauth = null;
        String cpcoption = null;
        String errorcode = null;
        String errordescription = null;
        String errorMsgCode = null;
        String errorMsgTxt = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "soap:Envelope")
                {
                    throw new KKException("Unexpected root element in VGGETCARDDETAILSRESPONSE: "
                            + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null && name.equals("MsgData"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        String xml = kkAppEng.removeCData(datanode.getData());
                        ByteArrayInputStream bais1 = new ByteArrayInputStream(xml.getBytes());
                        Document doc1 = builder.parse(bais1);
                        NodeList list1 = doc1.getElementsByTagName("*");
                        for (int j = 0; j < list1.getLength(); j++)
                        {
                            Node node1 = list1.item(j);
                            String name1 = node1.getNodeName();
                            if (name1.equals("sessionguid"))
                            {
                                sessionguid = getNodeValue(node1);
                            } else if (name1.equals("errorcode"))
                            {
                                errorcode = getNodeValue(node1);
                            } else if (name1.equals("errordescription"))
                            {
                                errordescription = getNodeValue(node1);
                            } else if (name1.equals("mkcardschemeid"))
                            {
                                mkcardschemeid = getNodeValue(node1);
                            } else if (name1.equals("issuenolength"))
                            {
                                issuenolength = getNodeValue(node1);
                            } else if (name1.equals("startdaterequired"))
                            {
                                startdaterequired = getNodeValue(node1);
                            } else if (name1.equals("csclength"))
                            {
                                csclength = getNodeValue(node1);
                            } else if (name1.equals("allowpayerauth"))
                            {
                                allowpayerauth = getNodeValue(node1);
                            } else if (name1.equals("cpcoption"))
                            {
                                cpcoption = getNodeValue(node1);
                            } else if (name1.equals("CODE"))
                            {
                                errorMsgCode = getNodeValue(node1);
                            } else if (name1.equals("MSGTXT"))
                            {
                                errorMsgTxt = getNodeValue(node1);
                            }
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Commidea VGGETCARDDETAILSRESPONSE response data:"
                            + "\n    mkcardschemeid            = " + mkcardschemeid
                            + "\n    issuenolength             = " + issuenolength
                            + "\n    startdaterequired         = " + startdaterequired
                            + "\n    csclength                 = " + csclength
                            + "\n    allowpayerauth            = " + allowpayerauth
                            + "\n    cpcoption                 = " + cpcoption
                            + "\n    sessionguid               = " + sessionguid
                            + "\n    errorcode                 = " + errorcode
                            + "\n    errordescription          = " + errordescription
                            + "\n    errorMsgCode              = " + errorMsgCode
                            + "\n    errorMsgTxt               = " + errorMsgTxt);

                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Commidea VGGETCARDDETAILSRESPONSE response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        /*
         * Save the IPN History record
         */
        errorMsgCode = (errorMsgCode != null) ? errorMsgCode : errorcode;
        errorMsgTxt = (errorMsgTxt != null) ? errorMsgTxt : errordescription;
        String codePlusTxt = getResultDescription(RET1_DESC + errorMsgCode
                + ((errorMsgTxt == null) ? "" : " : " + errorMsgTxt));
        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
        } else
        {
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
        }

        ipnHistory.setGatewayResult(allowpayerauth);
        ipnHistory.setGatewayTransactionId("vggetcarddetailsrequest");
        ipnHistory.setGatewayFullResponse(gatewayResp);
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            return allowpayerauth;
        }

        return errorMsgCode;
    }

    /**
     * Method that manages the VGPAYERAUTHENROLLMENTCHECKREQUEST and the
     * VGPAYERAUTHENROLLMENTCHECKRESPONSE. The method places the following data in the custom fields
     * of the PaymentDetails object.
     * <ul>
     * <li>Custom2 - payerauthrequestid</li>
     * <li>Custom3 - acsurl</li>
     * <li>Custom4 - pareq</li>
     * <li>Custom5 - TermUrl</li>
     * </ul>
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns the Commidea error code if there is an error, otherwise returns "Y" or "N"
     *         depending whether the customer is enrolled or not
     * @throws Exception
     */
    protected String vgpayerauthenrollmentcheckrequest(KKAppEng kkAppEng, OrderIf order,
            IpnHistoryIf ipnHistory) throws Exception
    {
        StringBuffer msg = new StringBuffer();
        msg.append(getHeader("VGPAYERAUTHENROLLMENTCHECKREQUEST", order,/* sendAttempt */0));

        String visamerchantbankid = parmMap.get("visamerchantbankid");
        String visamerchantnumber = parmMap.get("visamerchantnumber");
        String visamerchantpassword = parmMap.get("visamerchantpassword");
        String mcmmerchantbankid = parmMap.get("mcmmerchantbankid");
        String mcmmerchantnumber = parmMap.get("mcmmerchantnumber");
        String mcmmerchantpassword = parmMap.get("mcmmerchantpassword");

        String req = "<vgpayerauthenrollmentcheckrequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns=\"VANGUARD\">"
                + "<sessionguid>"
                + getSessionId(order.getPaymentDetails().getCustom1())
                + "</sessionguid>"
                + "<mkaccountid>"
                + parmMap.get("mkaccountid")
                + "</mkaccountid>"
                + "<mkacquirerid>"
                + parmMap.get("mkacquirerid")
                + "</mkacquirerid>"
                + "<merchantname>"
                + parmMap.get("merchantname")
                + "</merchantname>"
                + "<merchantcountrycode>"
                + parmMap.get("merchantcountrycode")
                + "</merchantcountrycode>"
                + "<merchanturl>"
                + parmMap.get("merchanturl")
                + "</merchanturl>"
                + ((visamerchantbankid == null || visamerchantbankid.length() == 0) ? ""
                        : "<visamerchantbankid>" + visamerchantbankid + "</visamerchantbankid>")
                + ((visamerchantnumber == null || visamerchantnumber.length() == 0) ? ""
                        : "<visamerchantnumber>" + visamerchantnumber + "</visamerchantnumber>")
                + ((visamerchantpassword == null || visamerchantpassword.length() == 0) ? ""
                        : "<visamerchantpassword>" + visamerchantpassword + "</visamerchantpassword>")
                + ((mcmmerchantbankid == null || mcmmerchantbankid.length() == 0) ? ""
                        : "<mcmmerchantbankid>" + mcmmerchantbankid + "</mcmmerchantbankid>")
                + ((mcmmerchantnumber == null || mcmmerchantnumber.length() == 0) ? ""
                        : "<mcmmerchantnumber>" + mcmmerchantnumber + "</mcmmerchantnumber>")
                + ((mcmmerchantpassword == null || mcmmerchantpassword.length() == 0) ? ""
                        : "<mcmmerchantpassword>" + mcmmerchantpassword + "</mcmmerchantpassword>")
                + "<currencycode>"
                + parmMap.get("currencycode")
                + "</currencycode>"
                + "<currencyexponent>"
                + parmMap.get("currencyexponent")
                + "</currencyexponent>"
                // + "<browseracceptheader>" + "xxx" + "</browseracceptheader>"
                // + "<browseruseragentheader>" + "xxx" + "</browseruseragentheader>"
                + "<transactionamount>"
                + parmMap.get("transactionamount")
                + "</transactionamount>"
                + "<transactiondisplayamount>"
                + parmMap.get("transactiondisplayamount")
                + "</transactiondisplayamount>" + "</vgpayerauthenrollmentcheckrequest>";

        msg.append(req);

        msg.append(getFooter());

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest (VGPAYERAUTHENROLLMENTCHECKREQUEST) =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp (VGPAYERAUTHENROLLMENTCHECKRESPONSE) =\n"
                    + gatewayResp);
            try
            {
                log.debug("Formatted GatewayResp (VGPAYERAUTHENROLLMENTCHECKRESPONSE) =\n"
                        + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway response (VGPAYERAUTHENROLLMENTCHECKRESPONSE) : "
                        + e.getMessage());
            }
        }

        // Now process the XML response

        String sessionguid = null;
        String payerauthrequestid = null;
        String enrolled = null;
        String acsurl = null;
        String pareq = null;
        String errorcode = null;
        String errordescription = null;
        String errorMsgCode = null;
        String errorMsgTxt = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "soap:Envelope")
                {
                    throw new KKException(
                            "Unexpected root element in VGPAYERAUTHENROLLMENTCHECKRESPONSE: "
                                    + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null && name.equals("MsgData"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        String xml = kkAppEng.removeCData(datanode.getData());
                        ByteArrayInputStream bais1 = new ByteArrayInputStream(xml.getBytes());
                        Document doc1 = builder.parse(bais1);
                        NodeList list1 = doc1.getElementsByTagName("*");
                        for (int j = 0; j < list1.getLength(); j++)
                        {
                            Node node1 = list1.item(j);
                            String name1 = node1.getNodeName();
                            if (name1.equals("sessionguid"))
                            {
                                sessionguid = getNodeValue(node1);
                            } else if (name1.equals("errorcode"))
                            {
                                errorcode = getNodeValue(node1);
                            } else if (name1.equals("errordescription"))
                            {
                                errordescription = getNodeValue(node1);
                            } else if (name1.equals("payerauthrequestid"))
                            {
                                payerauthrequestid = getNodeValue(node1);
                            } else if (name1.equals("enrolled"))
                            {
                                enrolled = getNodeValue(node1);
                            } else if (name1.equals("acsurl"))
                            {
                                acsurl = getNodeValue(node1);
                            } else if (name1.equals("pareq"))
                            {
                                pareq = getNodeValue(node1);
                            } else if (name1.equals("CODE"))
                            {
                                errorMsgCode = getNodeValue(node1);
                            } else if (name1.equals("MSGTXT"))
                            {
                                errorMsgTxt = getNodeValue(node1);
                            }
                        }
                    }
                }

                order.getPaymentDetails().setCustom2(payerauthrequestid);
                order.getPaymentDetails().setCustom3(acsurl);
                order.getPaymentDetails().setCustom4(pareq);
                order.getPaymentDetails().setCustom5(parmMap.get("TermUrl"));

                if (log.isDebugEnabled())
                {
                    log.debug("Commidea VGPAYERAUTHENROLLMENTCHECKRESPONSE response data:"
                            + "\n    payerauthrequestid     = " + payerauthrequestid
                            + "\n    enrolled               = " + enrolled
                            + "\n    acsurl                 = " + acsurl
                            + "\n    pareq                  = " + pareq
                            + "\n    sessionguid            = " + sessionguid
                            + "\n    errorcode              = " + errorcode
                            + "\n    errordescription       = " + errordescription
                            + "\n    errorMsgCode           = " + errorMsgCode
                            + "\n    errorMsgTxt            = " + errorMsgTxt);

                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Commidea VGPAYERAUTHENROLLMENTCHECKRESPONSE response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        /*
         * Save the IPN History record
         */
        errorMsgCode = (errorMsgCode != null) ? errorMsgCode : errorcode;
        errorMsgTxt = (errorMsgTxt != null) ? errorMsgTxt : errordescription;
        String codePlusTxt = getResultDescription(RET1_DESC + errorMsgCode
                + ((errorMsgTxt == null) ? "" : " : " + errorMsgTxt));
        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
        } else
        {
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
        }

        ipnHistory.setGatewayResult(enrolled);
        ipnHistory.setGatewayTransactionId("vgpayerauthenrollmentcheckrequest");
        ipnHistory.setGatewayFullResponse(gatewayResp);
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            return enrolled;
        }

        return errorMsgCode;
    }

    /**
     * Method that manages the VGPAYERAUTHAUTHENTICATIONCHECKREQUEST and the
     * VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE.
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @param paRes
     * @param enrolled
     * @return Returns the Commidea error code if there is an error, otherwise returns "Y" or "N" or
     *         some other value depending whether the customer was authenticated or not
     * @throws Exception
     */
    protected String vgpayerauthauthenticationcheckrequest(KKAppEng kkAppEng, OrderIf order,
            IpnHistoryIf ipnHistory, String paRes, String enrolled) throws Exception
    {

        StringBuffer msg = new StringBuffer();
        msg.append(getHeader("VGPAYERAUTHAUTHENTICATIONCHECKREQUEST", order, /* sendAttempt */0));

        String req = "<vgpayerauthauthenticationcheckrequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns=\"VANGUARD\">"
                + "<sessionguid>"
                + getSessionId(order.getPaymentDetails().getCustom1())
                + "</sessionguid>"
                + "<payerauthrequestid>"
                + order.getPaymentDetails().getCustom2()
                + "</payerauthrequestid>"
                + "<enrolled>"
                + enrolled
                + "</enrolled>"
                + ((paRes == null) ? "" : "<pares>" + paRes + "</pares>")
                + "</vgpayerauthauthenticationcheckrequest>";

        msg.append(req);

        msg.append(getFooter());

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest (VGPAYERAUTHAUTHENTICATIONCHECKREQUEST) =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp (VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE) =\n"
                    + gatewayResp);
            try
            {
                log.debug("Formatted GatewayResp (VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE) =\n"
                        + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway response (VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE) : "
                        + e.getMessage());
            }
        }

        // Now process the XML response

        String sessionguid = null;
        String payerauthrequestid = null;
        String atsdata = null;
        String authenticationstatus = null;
        String authenticationcertificate = null;
        String authenticationcavv = null;
        String authenticationeci = null;
        String authenticationtime = null;
        String errorcode = null;
        String errordescription = null;
        String errorMsgCode = null;
        String errorMsgTxt = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "soap:Envelope")
                {
                    throw new KKException(
                            "Unexpected root element in VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE: "
                                    + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null && name.equals("MsgData"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        String xml = kkAppEng.removeCData(datanode.getData());
                        ByteArrayInputStream bais1 = new ByteArrayInputStream(xml.getBytes());
                        Document doc1 = builder.parse(bais1);
                        NodeList list1 = doc1.getElementsByTagName("*");
                        for (int j = 0; j < list1.getLength(); j++)
                        {
                            Node node1 = list1.item(j);
                            String name1 = node1.getNodeName();
                            if (name1.equals("sessionguid"))
                            {
                                sessionguid = getNodeValue(node1);
                            } else if (name1.equals("errorcode"))
                            {
                                errorcode = getNodeValue(node1);
                            } else if (name1.equals("errordescription"))
                            {
                                errordescription = getNodeValue(node1);
                            } else if (name1.equals("payerauthrequestid"))
                            {
                                payerauthrequestid = getNodeValue(node1);
                            } else if (name1.equals("atsdata"))
                            {
                                atsdata = getNodeValue(node1);
                            } else if (name1.equals("authenticationstatus"))
                            {
                                authenticationstatus = getNodeValue(node1);
                            } else if (name1.equals("authenticationcertificate"))
                            {
                                authenticationcertificate = getNodeValue(node1);
                            } else if (name1.equals("authenticationcavv"))
                            {
                                authenticationcavv = getNodeValue(node1);
                            } else if (name1.equals("authenticationeci"))
                            {
                                authenticationeci = getNodeValue(node1);
                            } else if (name1.equals("authenticationtime"))
                            {
                                authenticationtime = getNodeValue(node1);
                            } else if (name1.equals("CODE"))
                            {
                                errorMsgCode = getNodeValue(node1);
                            } else if (name1.equals("MSGTXT"))
                            {
                                errorMsgTxt = getNodeValue(node1);
                            }
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Commidea VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE response data:"
                            + "\n    payerauthrequestid         = "
                            + payerauthrequestid
                            + "\n    atsdata                    = "
                            + atsdata
                            + "\n    authenticationstatus       = "
                            + authenticationstatus
                            + "\n    authenticationcertificate  = "
                            + authenticationcertificate
                            + "\n    authenticationcavv         = "
                            + authenticationcavv
                            + "\n    authenticationeci          = "
                            + authenticationeci
                            + "\n    authenticationtime         = "
                            + authenticationtime
                            + "\n    sessionguid                = "
                            + sessionguid
                            + "\n    errorcode                  = "
                            + errorcode
                            + "\n    errordescription           = "
                            + errordescription
                            + "\n    errorMsgCode               = "
                            + errorMsgCode
                            + "\n    errorMsgTxt                = " + errorMsgTxt);

                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Commidea VGPAYERAUTHAUTHENTICATIONCHECKRESPONSE response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        /*
         * Save the IPN History record
         */
        errorMsgCode = (errorMsgCode != null) ? errorMsgCode : errorcode;
        errorMsgTxt = (errorMsgTxt != null) ? errorMsgTxt : errordescription;
        String codePlusTxt = getResultDescription(RET1_DESC + errorMsgCode
                + ((errorMsgTxt == null) ? "" : " : " + errorMsgTxt));
        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
        } else
        {
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
        }

        ipnHistory.setGatewayResult(authenticationstatus);
        ipnHistory.setGatewayTransactionId("vgpayerauthauthenticationcheckrequest");
        ipnHistory.setGatewayFullResponse(gatewayResp);
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        if (errorMsgCode != null && errorMsgCode.equals("0"))
        {
            return authenticationstatus;
        }

        return errorMsgCode;
    }

    /**
     * Method that manages the VGTRANSACTIONREQUEST and the VGTRANSACTIONRESPONSE. The method places
     * the following data in the custom fields of the PaymentDetails object.
     * <ul>
     * <li>Custom2 - authcode</li>
     * </ul>
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns the Commidea error code
     * @throws Exception
     */
    protected String vgtransactionrequest(KKAppEng kkAppEng, OrderIf order, IpnHistoryIf ipnHistory)
            throws Exception
    {
        StringBuffer msg = new StringBuffer();
        msg.append(getHeader("VGTRANSACTIONREQUEST", order, /* sendAttempt */0));

        String req = "<vgtransactionrequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns=\"VANGUARD\">"
                + "<sessionguid>"
                + getSessionId(order.getPaymentDetails().getCustom1())
                + "</sessionguid>"
                + "<accountid>"
                + parmMap.get("mkaccountid")
                + "</accountid>"
                + "<txntype>"
                + "01"
                + "</txntype>"
                + "<transactioncurrencycode>"
                + parmMap.get("transactioncurrencycode")
                + "</transactioncurrencycode>"
                + "<apacsterminalcapabilities>"
                + "4298"
                + "</apacsterminalcapabilities>"
                + "<capturemethod>"
                + "12"
                + "</capturemethod>"
                + "<processingidentifier>"
                + parmMap.get("processingidentifier")
                + "</processingidentifier>"
                + "<txnvalue>"
                + parmMap.get("transactiondisplayamount")
                + "</txnvalue>"
                + "<terminalcountrycode>"
                + parmMap.get("terminalcountrycode")
                + "</terminalcountrycode>"
                + "<accountpasscode>"
                + parmMap.get("accountpasscode")
                + "</accountpasscode>"
                + "<returnhash>" + "1" + "</returnhash>" + "</vgtransactionrequest>";

        msg.append(req);

        msg.append(getFooter());

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest (VGTRANSACTIONREQUEST) =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp (VGTRANSACTIONRESPONSE) =\n" + gatewayResp);
            try
            {
                log.debug("Formatted GatewayResp (VGTRANSACTIONRESPONSE) =\n"
                        + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway response (VGTRANSACTIONRESPONSE) : "
                        + e.getMessage());
            }
        }

        // Now process the XML response

        String sessionguid = null;
        String transactionid = null;
        String errormsg = null;
        String authcode = null;
        String authmessage = null;
        String txnresult = null;
        String errorMsgCode = null;
        String errorMsgTxt = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "soap:Envelope")
                {
                    throw new KKException("Unexpected root element in VGTRANSACTIONRESPONSE: "
                            + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null && name.equals("MsgData"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        String xml = kkAppEng.removeCData(datanode.getData());
                        ByteArrayInputStream bais1 = new ByteArrayInputStream(xml.getBytes());
                        Document doc1 = builder.parse(bais1);
                        NodeList list1 = doc1.getElementsByTagName("*");
                        for (int j = 0; j < list1.getLength(); j++)
                        {
                            Node node1 = list1.item(j);
                            String name1 = node1.getNodeName();
                            if (name1.equals("sessionguid"))
                            {
                                sessionguid = getNodeValue(node1);
                            } else if (name1.equals("transactionid"))
                            {
                                transactionid = getNodeValue(node1);
                            } else if (name1.equals("errormsg"))
                            {
                                errormsg = getNodeValue(node1);
                            } else if (name1.equals("authcode"))
                            {
                                authcode = getNodeValue(node1);
                            } else if (name1.equals("authmessage"))
                            {
                                authmessage = getNodeValue(node1);
                            } else if (name1.equals("txnresult"))
                            {
                                txnresult = getNodeValue(node1);
                            } else if (name1.equals("CODE"))
                            {
                                errorMsgCode = getNodeValue(node1);
                            } else if (name1.equals("MSGTXT"))
                            {
                                errorMsgTxt = getNodeValue(node1);
                            }
                        }
                    }
                }

                order.getPaymentDetails().setCustom2(authcode);

                if (log.isDebugEnabled())
                {
                    log.debug("Commidea VGTRANSACTIONRESPONSE response data:"
                            + "\n    transactionid          = " + transactionid
                            + "\n    errormsg               = " + errormsg
                            + "\n    authcode               = " + authcode
                            + "\n    authmessage            = " + authmessage
                            + "\n    txnresult              = " + txnresult
                            + "\n    sessionguid            = " + sessionguid
                            + "\n    errorMsgCode           = " + errorMsgCode
                            + "\n    errorMsgTxt            = " + errorMsgTxt);
                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Commidea VGTRANSACTIONRESPONSE response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        /*
         * Save the IPN History record
         */
        if (errorMsgCode != null)
        {
            ipnHistory.setKonakartResultDescription(RET1_DESC + errorMsgCode);
            ipnHistory.setKonakartResultId(RET1);
        } else
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
        }

        ipnHistory.setGatewayResult("Authcode=" + authcode + "  Authmessage=" + authmessage);
        ipnHistory.setGatewayTransactionId((transactionid == null) ? "vgtransactionrequest"
                : transactionid);
        ipnHistory.setGatewayFullResponse(gatewayResp);
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        return txnresult;
    }

    /**
     * Add things specific to Commidea Vanguard to the connection
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
        connection.setRequestProperty("content-type", "text/xml");
    }

    /**
     * Utility to get the value of a node
     * 
     * @param nodeIn
     * @return The value of nodeIn
     */
    private String getNodeValue(Node nodeIn)
    {
        if (nodeIn.getFirstChild() != null)
        {
            return nodeIn.getFirstChild().getNodeValue();
        }
        return "";
    }

    /**
     * Clears the IpnHistory object
     * 
     * @param in
     * @return Returns the cleared IpnHistory object
     */
    protected IpnHistoryIf clearIpnHistory(IpnHistoryIf in)
    {
        in.setGatewayResult(null);
        in.setGatewayTransactionId(null);
        in.setGatewayFullResponse(null);
        in.setKonakartResultDescription(null);
        in.setKonakartResultId(0);
        return in;
    }

    /**
     * Get the sessionId from the encoded String
     * 
     * @param encodedSession
     * @return Returns the sessionId from the encoded String
     */
    protected String getSessionId(String encodedSession)
    {
        if (encodedSession != null)
        {
            return (encodedSession.split(";"))[0];
        }
        return "";
    }

    /**
     * Get the processingDB from the encoded String
     * 
     * @param encodedDB
     * @return Returns the processingDB from the encoded String
     */
    protected String getProcessingDB(String encodedDB)
    {
        if (encodedDB != null)
        {
            String[] tmpArray = encodedDB.split(";");
            if (tmpArray.length == 2)
            {
                return tmpArray[1];
            }
            log.warn("Unable to decode ProcessingDB. Encoded string = " + encodedDB);
            return "";
        }
        return "";
    }

    /**
     * Method that manages the VGTOKENREGISTRATIONREQUEST and the VGTOKENREGISTRATIONRESPONSE.
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns "1" or "0" depending on whether the authentication enrollment check request
     *         should be made. i.e. Should be "0" for AMEX cards
     * @throws Exception
     */
    protected String vgtokenregistrationrequest(KKAppEng kkAppEng, OrderIf order,
            IpnHistoryIf ipnHistory) throws Exception
    {

        StringBuffer msg = new StringBuffer();
        msg.append(getHeader("VGTOKENREGISTRATIONREQUEST", order, /* sendAttempt */0));

        // Token expiration date formatted to DDMMCCYY (e.g. 14092011)
        long timeInMillis = System.currentTimeMillis();
        Date expiryDate = new Date(timeInMillis + (DAY_IN_MILLIS * 365L));
        GregorianCalendar expiryGC = new GregorianCalendar();
        expiryGC.setTime(expiryDate);
        int day = expiryGC.get(Calendar.DAY_OF_MONTH);
        int month = expiryGC.get(Calendar.MONTH) + 1;
        int year = expiryGC.get(Calendar.YEAR);
        String dayStr = (day < 10) ? "0" + day : "" + day;
        String monthStr = (month < 10) ? "0" + month : "" + month;
        String dateStr = dayStr + monthStr + year;

        String req = "<vgtokenregistrationrequest xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns=\"VANGUARD\">"
                + "<sessionguid>"
                + getSessionId(order.getPaymentDetails().getCustom1())
                + "</sessionguid>"
                + "<purchase>"
                + "true"
                + "</purchase>"
                + "<refund>"
                + "true"
                + "</refund>"
                + "<cashback>"
                + "false"
                + "</cashback>"
                + "<tokenexpirationdate>"
                + dateStr + "</tokenexpirationdate>" + "</vgtokenregistrationrequest>";

        msg.append(req);

        msg.append(getFooter());

        if (log.isDebugEnabled())
        {
            log.debug("GatewayRequest (VGTOKENREGISTRATIONREQUEST) =\n"
                    + RegExpUtils.maskCreditCard(PrettyXmlPrinter.printXml(msg.toString())));
        }

        String gatewayResp = null;
        try
        {
            gatewayResp = postData(msg, order.getPaymentDetails(), null);
        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Problem posting request to " + order.getPaymentDetails().getRequestUrl()
                        + " : " + e.getMessage());
            }
            throw e;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Unformatted GatewayResp (VGTOKENREGISTRATIONRESPONSE) =\n" + gatewayResp);
            try
            {
                log.debug("Formatted GatewayResp (VGTOKENREGISTRATIONRESPONSE) =\n"
                        + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing gateway response (VGTOKENREGISTRATIONRESPONSE) : "
                        + e.getMessage());
            }
        }

        // Now process the XML response

        String sessionguid = null;
        String tokenid = null;
        String errorcode = null;
        String errordescription = null;

        if (gatewayResp != null)
        {
            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                ByteArrayInputStream bais = new ByteArrayInputStream(gatewayResp.getBytes());
                Document doc = builder.parse(bais);

                // get the root node
                Node rootnode = doc.getDocumentElement();
                String rootName = rootnode.getNodeName();

                if (rootName != "soap:Envelope")
                {
                    throw new KKException(
                            "Unexpected root element in VGTOKENREGISTRATIONRESPONSE: " + rootName);
                }

                // get all elements
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node node = list.item(i);
                    String name = node.getNodeName();
                    if (name != null && name.equals("MsgData"))
                    {
                        Text datanode = (Text) node.getFirstChild();
                        String xml = kkAppEng.removeCData(datanode.getData());
                        ByteArrayInputStream bais1 = new ByteArrayInputStream(xml.getBytes());
                        Document doc1 = builder.parse(bais1);
                        NodeList list1 = doc1.getElementsByTagName("*");
                        for (int j = 0; j < list1.getLength(); j++)
                        {
                            Node node1 = list1.item(j);
                            String name1 = node1.getNodeName();
                            if (name1.equals("sessionguid"))
                            {
                                sessionguid = getNodeValue(node1);
                            } else if (name1.equals("errorcode"))
                            {
                                errorcode = getNodeValue(node1);
                            } else if (name1.equals("errordescription"))
                            {
                                errordescription = getNodeValue(node1);
                            } else if (name1.equals("tokenid"))
                            {
                                tokenid = getNodeValue(node1);
                            }
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("Commidea VGTOKENREGISTRATIONRESPONSE response data:"
                            + "\n    sessionguid               = " + sessionguid
                            + "\n    errorcode                 = " + errorcode
                            + "\n    errordescription          = " + errordescription
                            + "\n    tokenId                   = " + tokenid);
                }
            } catch (Exception e)
            {
                // Problems parsing the XML
                if (log.isDebugEnabled())
                {
                    log.debug("Problems parsing Commidea VGTOKENREGISTRATIONRESPONSE response: "
                            + e.getMessage());
                }
                throw e;
            }
        }

        /*
         * Save the IPN History record
         */
        String codePlusTxt = getResultDescription(RET1_DESC + errorcode
                + ((errordescription == null) ? "" : " : " + errordescription));
        if (errorcode != null && errorcode.equals("0"))
        {
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
            ipnHistory.setGatewayResult(tokenid);
        } else
        {
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
            ipnHistory.setGatewayResult("ERROR");
        }

        ipnHistory.setGatewayTransactionId("vgtokenregistrationrequest");
        ipnHistory.setGatewayFullResponse(gatewayResp);
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        return errorcode;
    }

}
