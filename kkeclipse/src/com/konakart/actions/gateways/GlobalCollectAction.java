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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.IpnHistory;
import com.konakart.app.NameValue;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.modules.payment.globalcollect.GlobalCollect;
import com.konakart.bl.modules.payment.globalcollect.GlobalCollectUtils;
import com.konakart.util.PrettyXmlPrinter;
import com.konakart.util.Utils;

/**
 * This class is an Action class for card payments at GlobalCollect
 */
public class GlobalCollectAction extends GlobalCollectBaseAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(GlobalCollectAction.class);

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        if (log.isDebugEnabled())
        {
            log.debug(GlobalCollect.GLOBALCOLLECT_GATEWAY_CODE + " payment module called");
        }

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setModuleCode(GlobalCollect.GLOBALCOLLECT_GATEWAY_CODE);
        KKAppEng kkAppEng = null;

        try
        {
            int custId;

            kkAppEng = this.getKKAppEng(request, response);

            custId = this.loggedIn(request, response, kkAppEng, "CheckoutDelivery");

            // Check to see whether the user is logged in
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getOrderMgr().getCheckoutOrder();
            validateOrder(order, GlobalCollect.GLOBALCOLLECT_GATEWAY_CODE);

            // Set the order id for the ipnHistory object
            ipnHistory.setOrderId(order.getId());

            PaymentDetailsIf pd = order.getPaymentDetails();

            // Set the Module Code on the IpnHistory record - special for GlobalCollect
            ipnHistory.setModuleCode(getModuleCodeForIpnRecord(pd));

            // Set Transaction type and amount
            ipnHistory.setTxType(getTxTypeForIpnRecord(pd));

            GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();
            ipnHistory.setTxAmount(globalCollectUtils.getTotalPrice(order));

            // Send first XML message in the payment process

            String redirectUrl = sendOrderWithPayment(kkAppEng, order, ipnHistory, request);

            // Parse the response to see what to do next

            if (redirectUrl == null)
            {
                return "CheckoutError";
            }

            // All go here
            HashMap<String, String> hp = hashParameters(pd, null);
            pd.setRequestUrl(hp.get(GlobalCollect.GLOBALCOLLECT_RETURNED_FORMACTION));
            pd.setPostOrGet("redirect");
            if (log.isDebugEnabled())
            {
                log.debug("Forward to CheckoutExternalPaymentFrame");
            }
            return "CheckoutExternalPaymentFrame";

            // Do a 302 redirect (temporary) to the URL provided by GlobalCollect
            // if (log.isDebugEnabled())
            // {
            // log.debug("Redirect (302) to " + redirectUrl);
            // }
            // response.setHeader("Location", redirectUrl);
            // setupResponseForTemporaryRedirect(response);

            // return null;

        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug(e.getMessage());
            }
            return super.handleException(request, e);
        }
    }

    /**
     * Send an "INSERT_ORDERWITHPAYMENT" message to GlobalCollect and process the response.
     * 
     * @param kkAppEng
     * @param order
     * @param ipnHistory
     * @return Returns the Url to redirect to or null if something went wrong
     * @throws Exception
     */
    private String sendOrderWithPayment(KKAppEng kkAppEng, OrderIf order, IpnHistoryIf ipnHistory,
            HttpServletRequest request) throws Exception
    {
        PaymentDetailsIf pd = order.getPaymentDetails();
        HashMap<String, String> hp = hashParameters(pd, null);

        // Prevent null exceptions
        if (order.getLocale() == null)
        {
            throw new Exception("Locale on order is null");
        }

        StringBuffer msg = new StringBuffer("<XML>");
        msg.append("<REQUEST>");
        msg.append("<ACTION>INSERT_ORDERWITHPAYMENT</ACTION>");

        msg.append("<META>");
        msg.append("<IPADDRESS>" + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_IP)
                + "</IPADDRESS>");
        msg.append("<MERCHANTID>" + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_MERCHANT_ACC)
                + "</MERCHANTID>");
        msg.append("<VERSION>1.0</VERSION>");
        msg.append("</META>");

        msg.append("<PARAMS>");

        msg.append("<ORDER>");
        msg.append("<ORDERID>" + hp.get(GlobalCollect.GLOBALCOLLECT_ORDER_ID) + "</ORDERID>");
        msg.append("<AMOUNT>" + hp.get(GlobalCollect.GLOBALCOLLECT_PAYMENT_AMOUNT) + "</AMOUNT>");
        msg.append("<CURRENCYCODE>" + order.getCurrencyCode() + "</CURRENCYCODE>");
        msg.append("<LANGUAGECODE>" + order.getLocale().substring(0, 2) + "</LANGUAGECODE>");
        msg.append("<MERCHANTREFERENCE>" + hp.get(GlobalCollect.GLOBALCOLLECT_MERCHANT_REF)
                + "</MERCHANTREFERENCE>");
        msg.append("<CUSTOMERID>" + order.getCustomerId() + "</CUSTOMERID>");
        msg.append("<IPADDRESSCUSTOMER>" + getCustomerIPAddress(request) + "</IPADDRESSCUSTOMER>");
        msg.append("<ORDERDATE>" + hp.get(GlobalCollect.GLOBALCOLLECT_TIME_MS) + "</ORDERDATE>");

        // Billing Address Details

        msg.append("<FIRSTNAME>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_FNAME)
                + "</FIRSTNAME>");
        msg.append("<SURNAME>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_LNAME) + "</SURNAME>");
        if (hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_HOUSENUMBER) != null)
        {
            msg.append("<HOUSENUMBER>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_HOUSENUMBER)
                    + "</HOUSENUMBER>");
        }
        msg.append("<STREET>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_STREET1) + "</STREET>");
        msg.append("<CITY>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_CITY) + "</CITY>");
        msg.append("<ZIP>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_POSTCODE) + "</ZIP>");
        msg.append("<STATE>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_STATE) + "</STATE>");
        msg.append("<EMAIL>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_EMAIL) + "</EMAIL>");
        if (hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_COMP) != null)
        {
            msg.append("<COMPANYNAME>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_COMP)
                    + "</COMPANYNAME>");
        }
        msg.append("<COUNTRYCODE>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_CTRY_CODE)
                + "</COUNTRYCODE>");

        // Shipping Address Details

        msg.append("<SHIPPINGFIRSTNAME>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_FNAME)
                + "</SHIPPINGFIRSTNAME>");
        msg.append("<SHIPPINGSURNAME>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_LNAME)
                + "</SHIPPINGSURNAME>");
        if (hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_HOUSENUMBER) != null)
        {
            msg.append("<SHIPPINGHOUSENUMBER>"
                    + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_HOUSENUMBER)
                    + "</SHIPPINGHOUSENUMBER>");
        }
        msg.append("<SHIPPINGSTREET>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_STREET1)
                + "</SHIPPINGSTREET>");
        msg.append("<SHIPPINGCITY>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_CITY)
                + "</SHIPPINGCITY>");
        msg.append("<SHIPPINGZIP>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_POSTCODE)
                + "</SHIPPINGZIP>");
        msg.append("<SHIPPINGSTATE>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_STATE)
                + "</SHIPPINGSTATE>");
        if (hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_COMP) != null)
        {
            msg.append("<SHIPPINGCOMPANYNAME>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_COMP)
                    + "</SHIPPINGCOMPANYNAME>");
        }
        msg.append("<SHIPPINGCOUNTRYCODE>" + hp.get(GlobalCollect.GLOBALCOLLECT_SHIPTO_CTRY_CODE)
                + "</SHIPPINGCOUNTRYCODE>");

        msg.append("</ORDER>");

        msg.append("<PAYMENT>");
        msg.append("<RETURNURL>" + hp.get(GlobalCollect.MODULE_PAYMENT_GLOBALCOLLECT_RESPONSE_URL)
                + "</RETURNURL>");
        msg.append("<PAYMENTPRODUCTID>" + hp.get(GlobalCollect.GLOBALCOLLECT_PAYMENT_PRODUCTID)
                + "</PAYMENTPRODUCTID>");
        msg.append("<HOSTEDINDICATOR>1</HOSTEDINDICATOR>");
        msg.append("<AMOUNT>" + hp.get(GlobalCollect.GLOBALCOLLECT_PAYMENT_AMOUNT) + "</AMOUNT>");
        msg.append("<CURRENCYCODE>" + order.getCurrencyCode() + "</CURRENCYCODE>");

        if (hp.containsKey(GlobalCollect.GLOBALCOLLECT_NUMBEROFINSTALLMENTS))
        {
            String numbInstallments = hp.get(GlobalCollect.GLOBALCOLLECT_NUMBEROFINSTALLMENTS);
            if (!Utils.isBlank(numbInstallments) && !numbInstallments.equals("-1"))
            {
                msg.append("<NUMBEROFINSTALLMENTS>" + numbInstallments + "</NUMBEROFINSTALLMENTS>");
            }
        }

        msg.append("<COUNTRYCODE>" + hp.get(GlobalCollect.GLOBALCOLLECT_BILLTO_CTRY_CODE)
                + "</COUNTRYCODE>");
        msg.append("<LANGUAGECODE>" + order.getLocale().substring(0, 2) + "</LANGUAGECODE>");
        msg.append("</PAYMENT>");

        msg.append("</PARAMS>");
        msg.append("</REQUEST>");
        msg.append("</XML>");

        if (log.isDebugEnabled())
        {
            try
            {
                log.debug("GatewayRequest to https://ps.gcsip.nl/wdl/wdl =\n"
                        + PrettyXmlPrinter.printXml(msg.toString()));
            } catch (Exception e)
            {
                log.debug("Problem parsing the original XML");
                e.printStackTrace();
                log.debug("\n" + msg.toString());
            }
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
            log.debug("Unformatted Initial Response =\n" + gatewayResp);
            try
            {
                log.debug("Formatted Initial Response =\n" + PrettyXmlPrinter.printXml(gatewayResp));
            } catch (Exception e)
            {
                log.debug("Exception pretty-printing Initial Response : " + e.getMessage());
            }
        }

        // Now process the XML response

        String FORMACTION = null;
        String RESPONSE_RESULT = "NOK";
        String STATUSID = null;
        String MAC = null;
        String REQUESTID = null;
        String EXTERNALREFERENCE = null;
        String REF = null;
        String PAYMENTREFERENCE = null;
        String RETURNMAC = null;
        String ERROR_CODE = "-1";
        String ERROR_MSG = "";

        if (gatewayResp == null)
        {
            ERROR_CODE = "-1";
            ERROR_MSG = "Empty response from Gateway";
        } else
        {
            GlobalCollectUtils globalCollectUtils = new GlobalCollectUtils();
            Map<String, String> xmlMap = globalCollectUtils
                    .parseGlobalCollectResponseToMap(gatewayResp);

            ERROR_CODE = xmlMap.get("XML.REQUEST.RESPONSE.ERROR.CODE");
            ERROR_MSG = xmlMap.get("XML.REQUEST.RESPONSE.ERROR.MESSAGE");
            RESPONSE_RESULT = xmlMap.get("XML.REQUEST.RESPONSE.RESULT");
            REQUESTID = xmlMap.get("XML.REQUEST.RESPONSE.META.REQUESTID");

            FORMACTION = xmlMap.get("XML.REQUEST.RESPONSE.ROW.FORMACTION");
            MAC = xmlMap.get("XML.REQUEST.RESPONSE.ROW.MAC");
            RETURNMAC = xmlMap.get("XML.REQUEST.RESPONSE.ROW.RETURNMAC");
            STATUSID = xmlMap.get("XML.REQUEST.RESPONSE.ROW.STATUSID");
            REF = xmlMap.get("XML.REQUEST.RESPONSE.ROW.REF");
            EXTERNALREFERENCE = xmlMap.get("XML.REQUEST.RESPONSE.ROW.EXTERNALREFERENCE");
            PAYMENTREFERENCE = xmlMap.get("XML.REQUEST.RESPONSE.ROW.PAYMENTREFERENCE");

            if (log.isDebugEnabled())
            {
                log.debug("Initial response data:            "
                        + "\n    RESPONSE_RESULT           = " + RESPONSE_RESULT
                        + "\n    EXTERNALREFERENCE         = " + EXTERNALREFERENCE
                        + "\n    REF                       = " + REF
                        + "\n    PAYMENTREFERENCE          = " + PAYMENTREFERENCE
                        + "\n    REQUESTID                 = " + REQUESTID
                        + "\n    STATUSID                  = " + STATUSID
                        + "\n    FORMACTION                = " + FORMACTION
                        + "\n    MAC                       = " + MAC
                        + "\n    RETURNMAC                 = " + RETURNMAC
                        + "\n    ERROR_CODE                = " + ERROR_CODE
                        + "\n    ERROR_MSG                 = " + ERROR_MSG);
            }

            // Save REF and RETURNMAC on PaymentDetails for later

            List<NameValueIf> parmList = new ArrayList<NameValueIf>();

            parmList.add(new NameValue(GlobalCollect.GLOBALCOLLECT_RETURNED_REF, REF));
            parmList.add(new NameValue(GlobalCollect.GLOBALCOLLECT_RETURNED_MAC, RETURNMAC));
            parmList.add(new NameValue(GlobalCollect.GLOBALCOLLECT_RETURNED_FORMACTION, FORMACTION));
            addParameters(pd, parmList);

            if (RESPONSE_RESULT == null)
            {
                RESPONSE_RESULT = "NOK";
            }
        }

        /*
         * Save the IPN History record
         */
        String codePlusTxt = getResultDescription(RET1_DESC + ERROR_CODE + " - " + ERROR_MSG);
        if (RESPONSE_RESULT.equals("OK"))
        {
            ipnHistory.setGatewayTransactionId(REQUESTID);
            ipnHistory.setKonakartResultDescription(RET0_DESC);
            ipnHistory.setKonakartResultId(RET0);
        } else
        {
            ipnHistory.setKonakartResultDescription(codePlusTxt);
            ipnHistory.setKonakartResultId(RET1);
        }

        ipnHistory.setGatewayResult(RESPONSE_RESULT);
        ipnHistory.setGatewayTransactionId(REQUESTID);
        ipnHistory.setGatewayFullResponse(gatewayResp);

        // Problems parsing the XML
        if (log.isDebugEnabled())
        {
            log.debug("Save IPN History Record: " + ipnHistory.toString());
        }
        kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);

        if (RESPONSE_RESULT != null && RESPONSE_RESULT.equals("OK") && FORMACTION != null)
        {
            return FORMACTION;
        }

        return null;
    }
}
