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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.actions.BaseAction;
import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.EmailOptions;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.RegExpUtils;
import com.konakart.util.Utils;

/**
 * Base Gateway Action for KonaKart application.
 */
public class BaseGatewayAction extends BaseAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(BaseGatewayAction.class);

    private static final long serialVersionUID = 1L;

    /**
     * Validate the order and ensure that we are using the correct payment gateway
     * 
     * @param order
     * @param code
     * @throws KKAppException
     */
    protected void validateOrder(OrderIf order, String code) throws KKAppException
    {
        if (order == null)
        {
            throw new KKAppException("There is no order.");
        }

        if (order.getPaymentDetails() == null)
        {
            throw new KKAppException("There is no PaymentDetails object attached to the order.");
        }

        if (order.getPaymentDetails().getCode() == null)
        {
            throw new KKAppException(
                    "The PaymentDetails object contains a null code so we cannot determine which payment gateway to use.");
        }

        if (!order.getPaymentDetails().getCode().equals(code))
        {
            throw new KKAppException("The PaymentDetails object contains the gateway code: "
                    + order.getPaymentDetails().getCode()
                    + " which does not equal the code of the gateway being used: " + code);
        }
    }

    /**
     * Sends data to the payment gateway via a POST. Parameters are received from the PaymentDetails
     * object and the credit card parameters that have just been input by the customer are sent in a
     * separate list; the ccParmList. Any miscellaneous parameters can also be added to the
     * ccParmList if required.
     * 
     * @param pd
     *            the PaymentDetails object
     * @param ccParmList
     * @return The response to the post
     * @throws IOException
     */
    public String postData(PaymentDetailsIf pd, List<NameValueIf> ccParmList) throws IOException
    {
        return postData(null, pd, ccParmList);
    }

    /**
     * Sends data to the payment gateway via a POST. Parameters are received from the PaymentDetails
     * object and the credit card parameters that have just been input by the customer are sent in a
     * separate list; the ccParmList. Any miscellaneous parameters can also be added to the
     * ccParmList if required. If postData is not null, then the data it contains is used instead of
     * the data from the PaymentDetails and ccParmList.
     * 
     * @param postData
     *            The data to be posted. Can be null.
     * @param pd
     *            the PaymentDetails object
     * @param ccParmList
     * @return The response to the post
     * @throws IOException
     */
    public String postData(StringBuffer postData, PaymentDetailsIf pd, List<NameValueIf> ccParmList)
            throws IOException
    {
        URL url = new URL(pd.getRequestUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        if (pd.getReferrer() != null && pd.getReferrer().length() > 1)
        {
            connection.setRequestProperty("Referer", pd.getReferrer());
        }

        // This one is deprecated but we still need to call it
        customizeConnection(connection);

        // This is the one that should be used from v5.5.0.0
        customizeConnection(connection, pd, ccParmList);

        PrintWriter out = new PrintWriter(connection.getOutputStream());

        StringBuffer sb = (postData == null) ? getGatewayRequest(pd, ccParmList) : postData;

        if (log.isDebugEnabled())
        {
            log.debug("Post URL = " + pd.getRequestUrl());
            log.debug("Post string =\n" + RegExpUtils.maskCreditCard(sb.toString()));
        }

        // Send the message
        out.print(sb.toString());
        out.close();

        // Get back the response
        StringBuffer respSb = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
     * Sends data to the payment gateway via a GET. Parameters are received from the PaymentDetails
     * object and the credit card parameters that have just been input by the customer are send in a
     * separate list; the ccParmList
     * 
     * @param pd
     * @param ccParmList
     * @return The response to the post
     * @throws IOException
     */
    public String getData(PaymentDetailsIf pd, List<NameValueIf> ccParmList) throws IOException
    {
        // Construct data for GET
        String urlStr = pd.getRequestUrl();
        StringBuffer sbRequest = getGatewayRequest(pd, ccParmList);

        if (log.isDebugEnabled())
        {
            log.debug("GET URL = " + urlStr + RegExpUtils.maskCreditCard(sbRequest.toString()));
        }
        URL url = new URL(urlStr + sbRequest.toString());

        // Send data
        URLConnection conn = url.openConnection();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sbReply = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null)
        {
            sbReply.append(line);
        }
        rd.close();
        String result = sbReply.toString();

        return result;
    }

    /**
     * This method can be specialized in the super class to customize the format of the request
     * 
     * @param pd
     *            the PaymentDetails
     * @param ccParmList
     *            the credit card parameters
     * @return a StringBuffer containing the gateway request
     */
    protected StringBuffer getGatewayRequest(PaymentDetailsIf pd, List<NameValueIf> ccParmList)
    {
        // Create the message from the parameters in the PaymentDetails object
        StringBuffer sb = new StringBuffer();

        if (pd.getParameters() != null)
        {
            for (int i = 0; i < pd.getParameters().length; i++)
            {
                NameValueIf nv = pd.getParameters()[i];
                if (i > 0)
                {
                    sb.append("&");
                }
                sb.append(nv.getName());
                sb.append("=");
                sb.append(nv.getValue());
            }
        }

        // Add the credit card parameters
        if (ccParmList != null)
        {
            for (Iterator<NameValueIf> iter = ccParmList.iterator(); iter.hasNext();)
            {
                NameValueIf nv = iter.next();
                sb.append("&");
                sb.append(nv.getName());
                sb.append("=");
                sb.append(nv.getValue());
            }
        }

        if (log.isDebugEnabled())
        {
            int padding = 25;

            String logStr = "";
            if (pd.getParameters() != null)
            {
                for (int i = 0; i < pd.getParameters().length; i++)
                {
                    NameValueIf nv = pd.getParameters()[i];
                    logStr += "\n    " + Utils.padRight(nv.getName(), padding) + " = "
                            + nv.getValue();
                }
            }

            if (ccParmList != null)
            {
                for (Iterator<NameValueIf> iter = ccParmList.iterator(); iter.hasNext();)
                {
                    NameValueIf nv = iter.next();
                    logStr += "\n    " + Utils.padRight(nv.getName(), padding) + " = "
                            + RegExpUtils.maskCreditCard(nv.getValue());
                }
            }

            log.debug("Post source data:" + logStr);
        }

        return sb;
    }

    /**
     * @param kkAppEng
     * @return Returns the logFileDirectory. We look it up every time.
     */
    public String getLogFileDirectory(KKAppEng kkAppEng)
    {
        try
        {
            String conf = kkAppEng.getConfig(ConfigConstants.KONAKART_LOG_FILE_DIRECTORY);
            if (conf != null)
            {
                return conf + System.getProperty("file.separator");
            }
        } catch (Exception e)
        {
            if (e.getMessage().contains("return by API"))
            {
                if (log.isWarnEnabled())
                {
                    log.warn(e.getMessage());
                }
            } else
            {
                // unexpected
                e.printStackTrace();
            }
        }

        return "";
    }

    /**
     * This method is optionally called from the sub class to load up the parameters into a
     * HashTable for efficient subsequent processing
     * 
     * @param pd
     *            PaymentDetails object
     * @param ccParmList
     *            name value pair list of CC parameters
     * @return a hash map containing the parameters for rapid lookup
     */
    protected HashMap<String, String> hashParameters(PaymentDetailsIf pd,
            List<NameValueIf> ccParmList)
    {
        HashMap<String, String> paramHash = new HashMap<String, String>();

        if (pd != null)
        {
            for (int c = 0; c < pd.getParameters().length; c++)
            {
                paramHash.put(pd.getParameters()[c].getName(), pd.getParameters()[c].getValue());
            }
        }

        if (ccParmList != null)
        {
            for (int c = 0; c < ccParmList.size(); c++)
            {
                paramHash.put(ccParmList.get(c).getName(), ccParmList.get(c).getValue());
            }
        }

        return paramHash;
    }

    /**
     * Get the value of the parameter with the specified name from the PaymentDetails object
     * 
     * @param paramName
     *            parameter name to look up
     * @param pd
     *            PaymentDetails object
     * @return value of the parameter or null if the parameter is not found
     */
    protected String getParameterFromPaymentDetails(String paramName, PaymentDetailsIf pd)
    {
        String value = null;

        if (pd != null && pd.getParameters() != null)
        {
            for (int c = 0; c < pd.getParameters().length; c++)
            {
                if (paramName.equals(pd.getParameters()[c].getName()))
                {
                    return pd.getParameters()[c].getValue();
                }
            }
        }

        return value;
    }

    /**
     * This method is normally specialized in the sub class to customize the connection
     * 
     * @param connection
     *            the HTTP connection object
     * @param pd
     *            the Payment Details
     * @param paramList
     *            Additional parameters (typically credit card details but can also be any
     *            miscellaneous extra parameters that may be required)
     */
    protected void customizeConnection(HttpURLConnection connection, PaymentDetailsIf pd,
            List<NameValueIf> paramList)
    {
    }

    /**
     * This method is normally specialized in the sub class to customize the connection
     * 
     * @param connection
     */
    @Deprecated
    protected void customizeConnection(HttpURLConnection connection)
    {
    }

    /**
     * Send an order confirmation eMail. The template used is different if the order is successful
     * or not.
     * 
     * @param kkAppEng
     * @param orderId
     * @param success
     * @throws KKException
     */
    protected void sendOrderConfirmationMail(KKAppEng kkAppEng, int orderId, boolean success)
            throws KKException
    {
        String countryCode = kkAppEng.getLocale().substring(0, 2);

        sendOrderConfirmationMail(kkAppEng.getEng(), kkAppEng.getSessionId(), countryCode, orderId,
                success);
    }

    /**
     * Send an order confirmation eMail. The template used is different if the order is successful
     * or not.
     * 
     * @param eng
     * @param sessionId
     * @param countryCode
     * @param orderId
     * @param success
     * @throws KKException
     */
    protected void sendOrderConfirmationMail(KKEngIf eng, String sessionId, String countryCode,
            int orderId, boolean success) throws KKException
    {
        EmailOptionsIf options = new EmailOptions();

        // Default behaviour is not to create or attach the PDF invoice
        options.setCreateInvoice(false);
        options.setAttachInvoice(false);

        options.setCountryCode(countryCode);

        if (success)
        {
            options.setTemplateName("OrderConfPaymentSuccess");

            // Attach the invoice to the confirmation email (Enterprise Only). Defaults to false.
            // options.setAttachInvoice(true);

            // Create the invoice (if not already present) for attaching to the confirmation email
            // (Enterprise Only). Defaults to false.
            // options.setCreateInvoice(true);

        } else
        {
            options.setTemplateName("OrderConfPaymentFailure");
        }

        eng.sendOrderConfirmationEmail1(sessionId, orderId, /* langIdForOrder */
                -1, options);
    }

    /**
     * Saves an IPN History record. The KKAppEng should have a logged-in user with a valid
     * sessionId.
     * 
     * @param kkAppEng
     *            the KKAppEng application engine which should contain a valid sessionId
     * @param orderId
     *            the orderId involved in the transaction
     * @param moduleCode
     *            the code of the module
     * @param gatewayFullResponse
     *            full response from the gateway
     * @param gatewayResult
     *            summary gateway response
     * @param gatewayTransactionId
     *            an Id from the gateway that identifies the transaction
     * @param konakartResultDescription
     *            KonaKart result description
     * @param konakartResultId
     *            KonaKart result code
     * @throws KKException
     */
    protected void saveIPNrecord(KKAppEng kkAppEng, int orderId, String moduleCode,
            String gatewayFullResponse, String gatewayResult, String gatewayTransactionId,
            String konakartResultDescription, int konakartResultId) throws KKException
    {
        String txType = null;
        BigDecimal txAmount = null;

        saveIPNrecord(kkAppEng, orderId, moduleCode, gatewayFullResponse, gatewayResult,
                gatewayTransactionId, konakartResultDescription, konakartResultId, txType, txAmount);
    }

    /**
     * Saves an IPN History record. The KKAppEng should have a logged-in user with a valid
     * sessionId.
     * 
     * @param kkAppEng
     *            the KKAppEng application engine which should contain a valid sessionId
     * @param orderId
     *            the orderId involved in the transaction
     * @param moduleCode
     *            the code of the module
     * @param gatewayFullResponse
     *            full response from the gateway
     * @param gatewayResult
     *            summary gateway response
     * @param gatewayTransactionId
     *            an Id from the gateway that identifies the transaction
     * @param konakartResultDescription
     *            KonaKart result description
     * @param konakartResultId
     *            KonaKart result code
     * @param txType
     *            Transaction Type
     * @param txAmount
     *            Transaction Amount
     * @throws KKException
     */
    protected void saveIPNrecord(KKAppEng kkAppEng, int orderId, String moduleCode,
            String gatewayFullResponse, String gatewayResult, String gatewayTransactionId,
            String konakartResultDescription, int konakartResultId, String txType,
            BigDecimal txAmount) throws KKException
    {
        try
        {
            IpnHistoryIf ipnHistory = new IpnHistory();
            ipnHistory.setOrderId(orderId);
            ipnHistory.setModuleCode(moduleCode);
            ipnHistory.setDateAdded(new GregorianCalendar());
            ipnHistory.setGatewayFullResponse(gatewayFullResponse);
            ipnHistory.setGatewayResult(Utils.trim(gatewayResult, 128));
            ipnHistory.setGatewayTransactionId(gatewayTransactionId);
            ipnHistory.setKonakartResultDescription(Utils.trim(konakartResultDescription, 255));
            ipnHistory.setKonakartResultId(konakartResultId);
            ipnHistory.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
            ipnHistory.setTxType(txType);
            ipnHistory.setTxAmount(txAmount);
            kkAppEng.getEng().saveIpnHistory(kkAppEng.getSessionId(), ipnHistory);
        } catch (KKException e)
        {
            log.warn("Exception occured trying to save IPN History record: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Add more parameters to the PaymentDetails object. Normally this method would be on the
     * PaymentDetails class but it's placed here instead because of the automatic code generation
     * that occurs on the PaymentDetails class for web services, JSON and RMI etc.
     * 
     * @param pd
     *            the PaymentDetails object
     * @param newParameters
     *            The parameters to set.
     */
    protected void addParameters(PaymentDetailsIf pd, List<NameValueIf> newParameters)
    {
        List<NameValueIf> parmList = new ArrayList<NameValueIf>();

        // Add the new parameters to our temporary list
        if (newParameters != null)
        {
            for (int p = 0; p < newParameters.size(); p++)
            {
                parmList.add(newParameters.get(p));
            }
        }

        // Add the existing parameters to our temporary list
        if (pd.getParameters() != null)
        {
            for (int p = 0; p < pd.getParameters().length; p++)
            {
                parmList.add(pd.getParameters()[p]);
            }
        }

        // Now replace the parameters with a new set
        NameValue[] nvArray = new NameValue[parmList.size()];
        parmList.toArray(nvArray);
        pd.setParameters(nvArray);
    }

    /**
     * 
     * @param request
     * @return Returns the IP address
     */
    protected String getCustomerIPAddress(HttpServletRequest request)
    {
        return request.getRemoteAddr();
    }
}
